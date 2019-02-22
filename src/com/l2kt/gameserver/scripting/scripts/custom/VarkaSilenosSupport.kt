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
 *  * Varka Orc Village functions
 *  * Quests failures && alliance downgrade if you kill an allied mob.
 *
 */
class VarkaSilenosSupport : Quest(-1, "custom") {
    init {

        addFirstTalkId(ASHAS, NARAN, UDAN, DIYABU, HAGOS, SHIKON, TERANU)
        addTalkId(UDAN, HAGOS, TERANU)
        addStartNpc(HAGOS, TERANU)

        // Verify if the killer didn't kill an allied mob. Test his party aswell.
        addKillId(*VARKAS)

        // Verify if an allied is healing/buff an enemy. Petrify him if it's the case.
        addSkillSeeId(*VARKAS)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = noQuestMsg
        val st = player?.getQuestState(name) ?: return htmltext

        if (StringUtil.isDigit(event)) {
            val buffInfo = BUFF[Integer.parseInt(event)]
            if (st.getQuestItemsCount(SEED) >= buffInfo[1]) {
                htmltext = "31379-4.htm"
                st.takeItems(SEED, buffInfo[1])
                npc?.target = player
                npc?.doCast(SkillTable.getInfo(buffInfo[0], 1))
                npc?.setCurrentHpMp(npc.maxHp.toDouble(), npc.maxMp.toDouble())
            }
        } else if (event == "Withdraw") {
            if (player.warehouse.size == 0)
                htmltext = "31381-0.htm"
            else {
                player.sendPacket(ActionFailed.STATIC_PACKET)
                player.activeWarehouse = player.warehouse
                player.sendPacket(WarehouseWithdrawList(player, 1))
            }
        } else if (event == "Teleport") {
            when (player.allianceWithVarkaKetra) {
                -4 -> htmltext = "31383-4.htm"
                -5 -> htmltext = "31383-5.htm"
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
            ASHAS -> if (allianceLevel < 0)
                htmltext = "31377-friend.htm"
            else
                htmltext = "31377-no.htm"

            NARAN -> if (allianceLevel < 0)
                htmltext = "31378-friend.htm"
            else
                htmltext = "31378-no.htm"

            UDAN -> {
                st!!.state = Quest.STATE_STARTED
                if (allianceLevel > -1)
                    htmltext = "31379-3.htm"
                else if (allianceLevel > -3 && allianceLevel < 0)
                    htmltext = "31379-1.htm"
                else if (allianceLevel < -2) {
                    if (st.hasQuestItems(SEED))
                        htmltext = "31379-4.htm"
                    else
                        htmltext = "31379-2.htm"
                }
            }

            DIYABU -> if (player.karma >= 1)
                htmltext = "31380-pk.htm"
            else if (allianceLevel >= 0)
                htmltext = "31380-no.htm"
            else if (allianceLevel == -1 || allianceLevel == -2)
                htmltext = "31380-1.htm"
            else
                htmltext = "31380-2.htm"

            HAGOS -> when (allianceLevel) {
                -1 -> htmltext = "31381-1.htm"
                -2, -3 -> htmltext = "31381-2.htm"
                else -> if (allianceLevel >= 0)
                    htmltext = "31381-no.htm"
                else if (player.warehouse.size == 0)
                    htmltext = "31381-3.htm"
                else
                    htmltext = "31381-4.htm"
            }

            SHIKON -> when (allianceLevel) {
                -2 -> htmltext = "31382-1.htm"
                -3, -4 -> htmltext = "31382-2.htm"
                -5 -> htmltext = "31382-3.htm"
                else -> htmltext = "31382-no.htm"
            }

            TERANU -> if (allianceLevel >= 0)
                htmltext = "31383-no.htm"
            else if (allianceLevel < 0 && allianceLevel > -4)
                htmltext = "31383-1.htm"
            else if (allianceLevel == -4)
                htmltext = "31383-2.htm"
            else
                htmltext = "31383-3.htm"
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer
        if (player != null) {
            val party = player.party
            if (party != null) {
                for (member in party.members)
                    testVarkaDemote(member)
            } else
                testVarkaDemote(player)
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
        if (caster?.isAlliedWithVarka == true) {
            // Caster's skill is a positive effect ? Go further.
            when (skill?.skillType) {
                L2SkillType.BUFF, L2SkillType.HEAL, L2SkillType.HEAL_PERCENT, L2SkillType.HEAL_STATIC, L2SkillType.BALANCE_LIFE, L2SkillType.HOT -> for (target in targets as Array<Creature>) {
                    // Character isn't existing, is dead or is current caster, we drop check.
                    if (target.isDead || target === caster)
                        continue

                    // Target isn't a summon nor a player, we drop check.
                    if (target !is Playable)
                        continue

                    // Retrieve the player behind that target.
                    val player = target.actingPlayer

                    // If player is neutral or enemy, go further.
                    if (!player!!.isAlliedWithVarka) {
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
        private const val qn = "VarkaSilenosSupport"

        private const val ASHAS = 31377 // Hierarch
        private const val NARAN = 31378 // Messenger
        private const val UDAN = 31379 // Buffer
        private const val DIYABU = 31380 // Grocer
        private const val HAGOS = 31381 // Warehouse Keeper
        private const val SHIKON = 31382 // Trader
        private const val TERANU = 31383 // Teleporter

        private const val SEED = 7187

        private val VARKAS = intArrayOf(
            21350,
            21351,
            21353,
            21354,
            21355,
            21357,
            21358,
            21360,
            21361,
            21362,
            21369,
            21370,
            21364,
            21365,
            21366,
            21368,
            21371,
            21372,
            21373,
            21374,
            21375
        )

        private val BUFF = arrayOf(
            intArrayOf(4359, 2), // Focus: Requires 2 Nepenthese Seeds
            intArrayOf(4360, 2), // Death Whisper: Requires 2 Nepenthese Seeds
            intArrayOf(4345, 3), // Might: Requires 3 Nepenthese Seeds
            intArrayOf(4355, 3), // Acumen: Requires 3 Nepenthese Seeds
            intArrayOf(4352, 3), // Berserker: Requires 3 Nepenthese Seeds
            intArrayOf(4354, 3), // Vampiric Rage: Requires 3 Nepenthese Seeds
            intArrayOf(4356, 6), // Empower: Requires 6 Nepenthese Seeds
            intArrayOf(4357, 6)
        )// Haste: Requires 6 Nepenthese Seeds

        /**
         * Names of missions which will be automatically dropped if the alliance is broken.
         */
        private val varkaMissions = arrayOf(
            "Q611_AllianceWithVarkaSilenos",
            "Q612_WarWithKetraOrcs",
            "Q613_ProveYourCourage",
            "Q614_SlayTheEnemyCommander",
            "Q615_MagicalPowerOfFire_Part1",
            "Q616_MagicalPowerOfFire_Part2"
        )

        /**
         * That method drops current alliance and retrograde badge.<BR></BR>
         * If any Varka quest is in progress, it stops the quest (and drop all related qItems) :
         * @param player The player to check.
         */
        private fun testVarkaDemote(player: Player) {
            if (player.isAlliedWithVarka) {
                // Drop the alliance (old friends become aggro).
                player.allianceWithVarkaKetra = 0

                val inventory = player.inventory

                // Drop by 1 the level of that alliance (symbolized by a quest item).
                for (i in 7225 downTo 7221) {
                    val item = inventory!!.getItemByItemId(i)
                    if (item != null) {
                        // Destroy the badge.
                        player.destroyItemByItemId("Quest", i, item.count, player, true)

                        // Badge lvl 1 ; no addition of badge of lower level.
                        if (i != 7221)
                            player.addItem("Quest", i - 1, 1, player, true)

                        break
                    }
                }

                for (mission in varkaMissions) {
                    val pst = player.getQuestState(mission)
                    pst?.exitQuest(true)
                }
            }
        }
    }
}