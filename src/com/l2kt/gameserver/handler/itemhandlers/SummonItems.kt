package com.l2kt.gameserver.handler.itemhandlers

import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.gameserver.data.xml.NpcData
import com.l2kt.gameserver.data.xml.SummonItemData
import com.l2kt.gameserver.extensions.toSelfAndKnownPlayers
import com.l2kt.gameserver.handler.IItemHandler
import com.l2kt.gameserver.model.L2Spawn
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.instance.ChristmasTree
import com.l2kt.gameserver.model.actor.instance.Pet
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.MagicSkillLaunched
import com.l2kt.gameserver.network.serverpackets.MagicSkillUse
import com.l2kt.gameserver.network.serverpackets.SetupGauge
import com.l2kt.gameserver.network.serverpackets.SetupGauge.GaugeColor
import com.l2kt.gameserver.network.serverpackets.SystemMessage

class SummonItems : IItemHandler {
    override fun useItem(playable: Playable, item: ItemInstance, forceUse: Boolean) {
        if (playable !is Player)
            return

        if (playable.isSitting) {
            playable.sendPacket(SystemMessageId.CANT_MOVE_SITTING)
            return
        }

        if (playable.isInObserverMode)
            return

        if (playable.isAllSkillsDisabled || playable.isCastingNow)
            return

        val sitem = SummonItemData.getSummonItem(item.itemId)

        if ((playable.pet != null || playable.isMounted) && sitem.value > 0) {
            playable.sendPacket(SystemMessageId.SUMMON_ONLY_ONE)
            return
        }

        if (playable.isAttackingNow) {
            playable.sendPacket(SystemMessageId.YOU_CANNOT_SUMMON_IN_COMBAT)
            return
        }

        val npcId = sitem.id
        if (npcId == 0)
            return

        val npcTemplate = NpcData.getTemplate(npcId) ?: return

        playable.stopMove(null)

        when (sitem.value) {
            0 // static summons (like Christmas tree)
            -> try {
                for (ch in playable.getKnownTypeInRadius(ChristmasTree::class.java, 1200)) {
                    if (npcTemplate.npcId == ChristmasTree.SPECIAL_TREE_ID) {
                        playable.sendPacket(
                            SystemMessage.getSystemMessage(SystemMessageId.CANNOT_SUMMON_S1_AGAIN).addCharName(
                                ch
                            )
                        )
                        return
                    }
                }

                if (playable.destroyItem("Summon", item.objectId, 1, null, false)) {
                    val spawn = L2Spawn(npcTemplate)
                    spawn.loc = playable.position
                    spawn.setRespawnState(false)

                    val npc = spawn.doSpawn(true)
                    npc!!.title = playable.name
                    npc.setIsRunning(false) // broadcast info
                }
            } catch (e: Exception) {
                playable.sendPacket(SystemMessageId.TARGET_CANT_FOUND)
            }

            1 // pet summons
            -> {
                val oldTarget = playable.target
                playable.target = playable
                playable.toSelfAndKnownPlayers(MagicSkillUse(playable, 2046, 1, 5000, 0))
                playable.target = oldTarget
                playable.sendPacket(SetupGauge(GaugeColor.BLUE, 5000))
                playable.sendPacket(SystemMessageId.SUMMON_A_PET)
                playable.setIsCastingNow(true)

                ThreadPool.schedule(PetSummonFinalizer(playable, npcTemplate, item), 5000)
            }
            2 // wyvern
            -> playable.mount(sitem.id, item.objectId)
        }
    }

    // TODO: this should be inside skill handler
    internal class PetSummonFinalizer(
        private val _activeChar: Player,
        private val _npcTemplate: NpcTemplate,
        private val _item: ItemInstance?
    ) : Runnable {

        override fun run() {
            _activeChar.sendPacket(MagicSkillLaunched(_activeChar, 2046, 1))
            _activeChar.setIsCastingNow(false)

            // check for summon item validity
            if (_item == null || _item.ownerId != _activeChar.objectId || _item.location != ItemInstance.ItemLocation.INVENTORY)
                return

            // Owner has a pet listed in world.
            if (World.getPet(_activeChar.objectId) != null)
                return

            // Add the pet instance to world.
            val pet = Pet.restore(_item, _npcTemplate, _activeChar) ?: return

            World.addPet(_activeChar.objectId, pet)

            _activeChar.pet = pet

            pet.setRunning()
            pet.title = _activeChar.name
            pet.spawnMe()
            pet.startFeed()
            pet.followStatus = true
        }
    }
}
