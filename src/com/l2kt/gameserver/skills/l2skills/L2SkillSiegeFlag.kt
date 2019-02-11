package com.l2kt.gameserver.skills.l2skills

import com.l2kt.gameserver.data.manager.CastleManager
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.actor.instance.SiegeFlag
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.model.entity.Siege
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.idfactory.IdFactory
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.templates.StatsSet

class L2SkillSiegeFlag(set: StatsSet) : L2Skill(set) {
    private val _isAdvanced: Boolean = set.getBool("isAdvanced", false)

    override fun useSkill(activeChar: Creature, targets: Array<WorldObject>) {
        if (activeChar !is Player)
            return

        val player = activeChar.getActingPlayer()

        if (!checkIfOkToPlaceFlag(player, true))
            return

        // Template initialization
        val npcDat = StatsSet()

        npcDat.set("id", 35062)
        npcDat.set("type", "")

        npcDat.set("name", player!!.clan.name)
        npcDat.set("usingServerSideName", true)

        npcDat.set("hp", if (_isAdvanced) 100000 else 50000)
        npcDat.set("mp", 0)

        npcDat.set("radius", 10)
        npcDat.set("height", 80)

        npcDat.set("pAtk", 0)
        npcDat.set("mAtk", 0)
        npcDat.set("pDef", 500)
        npcDat.set("mDef", 500)

        npcDat.set("runSpd", 0) // Have to keep this, static object MUST BE 0 (critical error otherwise).

        // Spawn a new flag.
        val flag = SiegeFlag(player, IdFactory.getInstance().nextId, NpcTemplate(npcDat))
        flag.currentHp = flag.maxHp.toDouble()
        flag.heading = player.heading
        flag.spawnMe(player.position)
    }

    companion object {

        /**
         * @param player : The player placing the flag.
         * @param isCheckOnly : If false, send a notification to the player telling him why it failed.
         * @return true if the player can place a flag.
         */
        fun checkIfOkToPlaceFlag(player: Player?, isCheckOnly: Boolean): Boolean {
            val siege = CastleManager.getInstance().getActiveSiege(player!!)

            val sm: SystemMessage
            sm = when {
                siege == null || !siege.checkSide(player.clan, Siege.SiegeSide.ATTACKER) -> SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(247)
                !player.isClanLeader -> SystemMessage.getSystemMessage(SystemMessageId.ONLY_CLAN_LEADER_CAN_ISSUE_COMMANDS)
                player.clan.flag != null -> SystemMessage.getSystemMessage(SystemMessageId.NOT_ANOTHER_HEADQUARTERS)
                !player.isInsideZone(ZoneId.HQ) -> SystemMessage.getSystemMessage(SystemMessageId.NOT_SET_UP_BASE_HERE)
                !player.getKnownTypeInRadius(SiegeFlag::class.java, 400).isEmpty() -> SystemMessage.getSystemMessage(SystemMessageId.HEADQUARTERS_TOO_CLOSE)
                else -> return true
            }

            if (!isCheckOnly)
                player.sendPacket(sm)

            return false
        }
    }
}