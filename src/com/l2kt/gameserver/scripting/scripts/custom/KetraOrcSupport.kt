package com.l2kt.gameserver.scripting.scripts.custom

import com.l2kt.commons.lang.StringUtil
import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.network.serverpackets.WarehouseWithdrawList
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.templates.skills.L2SkillType

/**
 * This script supports :
 *
 *  * Ketra Orc Village functions
 *  * Quests failures && alliance downgrade if you kill an allied mob.
 *  * Petrification effect in case an allied player helps a neutral or enemy.
 *
 */
class KetraOrcSupport : Quest(-1, "custom") {
    init {

        addFirstTalkId(KADUN, WAHKAN, ASEFA, ATAN, JAFF, JUMARA, KURFA)
        addTalkId(ASEFA, JAFF, KURFA)
        addStartNpc(JAFF, KURFA)

        // Verify if the killer didn't kill an allied mob. Test his party aswell.
        addKillId(*KETRAS)

        // Verify if an allied is healing/buff an enemy. Petrify him if it's the case.
        addSkillSeeId(*KETRAS)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = noQuestMsg
        val st = player?.getQuestState(name) ?: return htmltext

        if (StringUtil.isDigit(event)) {
            val buffInfo = BUFF[Integer.parseInt(event)]
            if (st.getQuestItemsCount(HORN) >= buffInfo[1]) {
                htmltext = "31372-4.htm"
                st.takeItems(HORN, buffInfo[1])
                npc?.target = player
                npc?.doCast(SkillTable.getInfo(buffInfo[0], 1))
                npc?.setCurrentHpMp(npc.maxHp.toDouble(), npc.maxMp.toDouble())
            }
        } else if (event == "Withdraw") {
            if (player.warehouse.size == 0)
                htmltext = "31374-0.htm"
            else {
                player.sendPacket(ActionFailed.STATIC_PACKET)
                player.activeWarehouse = player.warehouse
                player.sendPacket(WarehouseWithdrawList(player, 1))
            }
        } else if (event == "Teleport") {
            when (player.allianceWithVarkaKetra) {
                4 -> htmltext = "31376-4.htm"
                5 -> htmltext = "31376-5.htm"
            }
        }

        return htmltext
    }

    override fun onFirstTalk(npc: Npc, player: Player): String? {
        var htmltext = noQuestMsg
        var st = player.getQuestState(qn)
        if (st == null)
            st = newQuestState(player)

        val allianceLevel = player.allianceWithVarkaKetra

        when (npc.npcId) {
            KADUN -> if (allianceLevel > 0)
                htmltext = "31370-friend.htm"
            else
                htmltext = "31370-no.htm"

            WAHKAN -> if (allianceLevel > 0)
                htmltext = "31371-friend.htm"
            else
                htmltext = "31371-no.htm"

            ASEFA -> {
                st!!.state = Quest.STATE_STARTED
                if (allianceLevel < 1)
                    htmltext = "31372-3.htm"
                else if (allianceLevel in 1..2)
                    htmltext = "31372-1.htm"
                else if (allianceLevel > 2) {
                    if (st.hasQuestItems(HORN))
                        htmltext = "31372-4.htm"
                    else
                        htmltext = "31372-2.htm"
                }
            }

            ATAN -> if (player.karma >= 1)
                htmltext = "31373-pk.htm"
            else if (allianceLevel <= 0)
                htmltext = "31373-no.htm"
            else if (allianceLevel == 1 || allianceLevel == 2)
                htmltext = "31373-1.htm"
            else
                htmltext = "31373-2.htm"

            JAFF -> when (allianceLevel) {
                1 -> htmltext = "31374-1.htm"
                2, 3 -> htmltext = "31374-2.htm"
                else -> if (allianceLevel <= 0)
                    htmltext = "31374-no.htm"
                else if (player.warehouse.size == 0)
                    htmltext = "31374-3.htm"
                else
                    htmltext = "31374-4.htm"
            }

            JUMARA -> when (allianceLevel) {
                2 -> htmltext = "31375-1.htm"
                3, 4 -> htmltext = "31375-2.htm"
                5 -> htmltext = "31375-3.htm"
                else -> htmltext = "31375-no.htm"
            }

            KURFA -> if (allianceLevel <= 0)
                htmltext = "31376-no.htm"
            else if (allianceLevel in 1..3)
                htmltext = "31376-1.htm"
            else if (allianceLevel == 4)
                htmltext = "31376-2.htm"
            else
                htmltext = "31376-3.htm"
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer
        if (player != null) {
            val party = player.party
            if (party != null) {
                for (partyMember in party.members)
                    testKetraDemote(partyMember)
            } else
                testKetraDemote(player)
        }
        return null
    }

    override fun onSkillSee(
        npc: Npc,
        caster: Player?,
        skill: L2Skill?,
        targets: Array<WorldObject>,
        isPet: Boolean
    ): String? {
        // Caster is an allied.
        if (caster?.isAlliedWithKetra == true) {
            // Caster's skill is a positive effect ? Go further.
            when (skill?.skillType) {
                L2SkillType.BUFF, L2SkillType.HEAL, L2SkillType.HEAL_PERCENT, L2SkillType.HEAL_STATIC, L2SkillType.BALANCE_LIFE, L2SkillType.HOT -> for (target in targets as Array<Creature>) {
                    // Character isn't existing, is dead or is current caster, we drop check.
                    if (target == null || target.isDead || target === caster)
                        continue

                    // Target isn't a summon nor a player, we drop check.
                    if (target !is Playable)
                        continue

                    // Retrieve the player behind that target.
                    val player = target.actingPlayer

                    // If player is neutral or enemy, go further.
                    if (!player!!.isAlliedWithKetra) {
                        // If the NPC got that player registered in aggro list, go further.
                        if ((npc as Attackable).aggroList.containsKey(player)) {
                            // Save current target for future use.
                            val oldTarget = npc.getTarget()

                            // Curse the heretic or his pet.
                            npc.setTarget(if (isPet && player.pet != null) caster.pet else caster)
                            npc.doCast(SkillTable.FrequentSkill.VARKA_KETRA_PETRIFICATION.skill)

                            // Revert to old target && drop the loop.
                            npc.setTarget(oldTarget)
                            break
                        }
                    }
                }
            }
        }

        // Continue normal behavior.
        return super.onSkillSee(npc, caster, skill, targets, isPet)
    }

    companion object {
        private const val qn = "KetraOrcSupport"

        private const val KADUN = 31370 // Hierarch
        private const val WAHKAN = 31371 // Messenger
        private const val ASEFA = 31372 // Soul Guide
        private const val ATAN = 31373 // Grocer
        private const val JAFF = 31374 // Warehouse Keeper
        private const val JUMARA = 31375 // Trader
        private const val KURFA = 31376 // Gate Keeper

        private const val HORN = 7186

        private val KETRAS = intArrayOf(
            21324,
            21325,
            21327,
            21328,
            21329,
            21331,
            21332,
            21334,
            21335,
            21336,
            21338,
            21339,
            21340,
            21342,
            21343,
            21344,
            21345,
            21346,
            21347,
            21348,
            21349
        )

        private val BUFF = arrayOf(
            intArrayOf(4359, 2), // Focus: Requires 2 Buffalo Horns
            intArrayOf(4360, 2), // Death Whisper: Requires 2 Buffalo Horns
            intArrayOf(4345, 3), // Might: Requires 3 Buffalo Horns
            intArrayOf(4355, 3), // Acumen: Requires 3 Buffalo Horns
            intArrayOf(4352, 3), // Berserker: Requires 3 Buffalo Horns
            intArrayOf(4354, 3), // Vampiric Rage: Requires 3 Buffalo Horns
            intArrayOf(4356, 6), // Empower: Requires 6 Buffalo Horns
            intArrayOf(4357, 6)
        )// Haste: Requires 6 Buffalo Horns

        /**
         * Names of missions which will be automatically dropped if the alliance is broken.
         */
        private val ketraMissions = arrayOf(
            "Q605_AllianceWithKetraOrcs",
            "Q606_WarWithVarkaSilenos",
            "Q607_ProveYourCourage",
            "Q608_SlayTheEnemyCommander",
            "Q609_MagicalPowerOfWater_Part1",
            "Q610_MagicalPowerOfWater_Part2"
        )

        /**
         * That method drops current alliance and retrograde badge.<BR></BR>
         * If any Varka quest is in progress, it stops the quest (and drop all related qItems) :
         * @param player The player to check.
         */
        private fun testKetraDemote(player: Player) {
            if (player.isAlliedWithKetra) {
                // Drop the alliance (old friends become aggro).
                player.allianceWithVarkaKetra = 0

                val inventory = player.inventory

                // Drop by 1 the level of that alliance (symbolized by a quest item).
                for (i in 7215 downTo 7211) {
                    val item = inventory!!.getItemByItemId(i)
                    if (item != null) {
                        // Destroy the badge.
                        player.destroyItemByItemId("Quest", i, item.count, player, true)

                        // Badge lvl 1 ; no addition of badge of lower level.
                        if (i != 7211)
                            player.addItem("Quest", i - 1, 1, player, true)

                        break
                    }
                }

                for (mission in ketraMissions) {
                    val pst = player.getQuestState(mission)
                    pst?.exitQuest(true)
                }
            }
        }
    }
}