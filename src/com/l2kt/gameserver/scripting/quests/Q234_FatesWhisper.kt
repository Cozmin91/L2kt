package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.data.ItemTable
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.*

class Q234_FatesWhisper : Quest(234, "Fate's Whispers") {
    init {
        CHEST_SPAWN[25035] = 31027
        CHEST_SPAWN[25054] = 31028
        CHEST_SPAWN[25126] = 31029
        CHEST_SPAWN[25220] = 31030
    }

    init {
        WEAPONS[79] = "Sword of Damascus"
        WEAPONS[97] = "Lance"
        WEAPONS[171] = "Deadman's Glory"
        WEAPONS[175] = "Art of Battle Axe"
        WEAPONS[210] = "Staff of Evil Spirits"
        WEAPONS[234] = "Demon Dagger"
        WEAPONS[268] = "Bellion Cestus"
        WEAPONS[287] = "Bow of Peril"
        WEAPONS[2626] = "Samurai Dual-sword"
        WEAPONS[7883] = "Guardian Sword"
        WEAPONS[7889] = "Wizard's Tear"
        WEAPONS[7893] = "Kaim Vanul's Bones"
        WEAPONS[7901] = "Star Buster"
    }

    init {

        setItemsIds(PIPETTE_KNIFE, RED_PIPETTE_KNIFE)

        addStartNpc(31002)
        addTalkId(31002, 30182, 30847, 30178, 30833, 31028, 31029, 31030, 31027)

        // The 4 bosses which spawn chests
        addKillId(25035, 25054, 25126, 25220)

        // Baium
        addAttackId(29020)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player!!.getQuestState(qn) ?: return htmltext

        if (event.equals("31002-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30182-01c.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_ITEMGET)
            st.giveItems(INFERNIUM_VARNISH, 1)
        } else if (event.equals("30178-01a.htm", ignoreCase = true)) {
            st["cond"] = "6"
            st.playSound(QuestState.SOUND_MIDDLE)
        } else if (event.equals("30833-01b.htm", ignoreCase = true)) {
            st["cond"] = "7"
            st.playSound(QuestState.SOUND_MIDDLE)
            st.giveItems(PIPETTE_KNIFE, 1)
        } else if (event.startsWith("selectBGrade_")) {
            if (st.getInt("bypass") == 1)
                return null

            val bGradeId = event.replace("selectBGrade_", "")
            st["weaponId"] = bGradeId
            htmltext = getHtmlText("31002-13.htm").replace("%weaponname%", WEAPONS[st.getInt("weaponId")] ?: "")
        } else if (event.startsWith("confirmWeapon")) {
            st["bypass"] = "1"
            htmltext = getHtmlText("31002-14.htm").replace("%weaponname%", WEAPONS[st.getInt("weaponId")] ?: "")
        } else if (event.startsWith("selectAGrade_")) {
            if (st.getInt("bypass") == 1) {
                val itemId = st.getInt("weaponId")
                if (st.hasQuestItems(itemId)) {
                    val aGradeItemId = Integer.parseInt(event.replace("selectAGrade_", ""))

                    htmltext = getHtmlText("31002-12.htm").replace(
                        "%weaponname%",
                        ItemTable.getTemplate(aGradeItemId)!!.name
                    )
                    st.takeItems(itemId, 1)
                    st.giveItems(aGradeItemId, 1)
                    st.giveItems(STAR_OF_DESTINY, 1)
                    player.broadcastPacket(SocialAction(player, 3))
                    st.playSound(QuestState.SOUND_FINISH)
                    st.exitQuest(false)
                } else
                    htmltext = getHtmlText("31002-15.htm").replace("%weaponname%", WEAPONS[itemId] ?: "")
            } else
                htmltext = "31002-16.htm"
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 75) "31002-01.htm" else "31002-02.htm"

            Quest.STATE_STARTED -> {
                val cond = st.getInt("cond")
                when (npc.npcId) {
                    31002 -> if (cond == 1) {
                        if (!st.hasQuestItems(REIRIA_SOUL_ORB))
                            htmltext = "31002-04b.htm"
                        else {
                            htmltext = "31002-05.htm"
                            st["cond"] = "2"
                            st.playSound(QuestState.SOUND_MIDDLE)
                            st.takeItems(REIRIA_SOUL_ORB, 1)
                        }
                    } else if (cond == 2) {
                        if (!st.hasQuestItems(KERMON_INFERNIUM_SCEPTER) || !st.hasQuestItems(GOLKONDA_INFERNIUM_SCEPTER) || !st.hasQuestItems(
                                HALLATE_INFERNIUM_SCEPTER
                            )
                        )
                            htmltext = "31002-05c.htm"
                        else {
                            htmltext = "31002-06.htm"
                            st["cond"] = "3"
                            st.playSound(QuestState.SOUND_MIDDLE)
                            st.takeItems(GOLKONDA_INFERNIUM_SCEPTER, 1)
                            st.takeItems(HALLATE_INFERNIUM_SCEPTER, 1)
                            st.takeItems(KERMON_INFERNIUM_SCEPTER, 1)
                        }
                    } else if (cond == 3) {
                        if (!st.hasQuestItems(INFERNIUM_VARNISH))
                            htmltext = "31002-06b.htm"
                        else {
                            htmltext = "31002-07.htm"
                            st["cond"] = "4"
                            st.playSound(QuestState.SOUND_MIDDLE)
                            st.takeItems(INFERNIUM_VARNISH, 1)
                        }
                    } else if (cond == 4) {
                        if (!st.hasQuestItems(REORIN_HAMMER))
                            htmltext = "31002-07b.htm"
                        else {
                            htmltext = "31002-08.htm"
                            st["cond"] = "5"
                            st.playSound(QuestState.SOUND_MIDDLE)
                            st.takeItems(REORIN_HAMMER, 1)
                        }
                    } else if (cond > 4 && cond < 8)
                        htmltext = "31002-08b.htm"
                    else if (cond == 8) {
                        htmltext = "31002-09.htm"
                        st["cond"] = "9"
                        st.playSound(QuestState.SOUND_MIDDLE)
                        st.takeItems(REORIN_MOLD, 1)
                    } else if (cond == 9) {
                        if (st.getQuestItemsCount(CRYSTAL_B) < 984)
                            htmltext = "31002-09b.htm"
                        else {
                            htmltext = "31002-BGradeList.htm"
                            st["cond"] = "10"
                            st.playSound(QuestState.SOUND_MIDDLE)
                            st.takeItems(CRYSTAL_B, 984)
                        }
                    } else if (cond == 10) {
                        // If a weapon is selected
                        if (st.getInt("bypass") == 1) {
                            // If you got it in the inventory
                            val itemId = st.getInt("weaponId")
                            htmltext =
                                    getHtmlText(if (st.hasQuestItems(itemId)) "31002-AGradeList.htm" else "31002-15.htm").replace(
                                        "%weaponname%",
                                        WEAPONS[itemId] ?: ""
                                    )
                        } else
                            htmltext = "31002-BGradeList.htm"// B weapon is still not selected
                    }

                    30182 -> if (cond == 3)
                        htmltext = if (!st.hasQuestItems(INFERNIUM_VARNISH)) "30182-01.htm" else "30182-02.htm"

                    30847 -> if (cond == 4 && !st.hasQuestItems(REORIN_HAMMER)) {
                        htmltext = "30847-01.htm"
                        st.playSound(QuestState.SOUND_ITEMGET)
                        st.giveItems(REORIN_HAMMER, 1)
                    } else if (cond >= 4 && st.hasQuestItems(REORIN_HAMMER))
                        htmltext = "30847-02.htm"

                    30178 -> if (cond == 5)
                        htmltext = "30178-01.htm"
                    else if (cond > 5)
                        htmltext = "30178-02.htm"

                    30833 -> if (cond == 6)
                        htmltext = "30833-01.htm"
                    else if (cond == 7) {
                        if (st.hasQuestItems(PIPETTE_KNIFE) && !st.hasQuestItems(RED_PIPETTE_KNIFE))
                            htmltext = "30833-02.htm"
                        else {
                            htmltext = "30833-03.htm"
                            st["cond"] = "8"
                            st.playSound(QuestState.SOUND_MIDDLE)
                            st.takeItems(RED_PIPETTE_KNIFE, 1)
                            st.giveItems(REORIN_MOLD, 1)
                        }
                    } else if (cond > 7)
                        htmltext = "30833-04.htm"

                    31027 -> if (cond == 1 && !st.hasQuestItems(REIRIA_SOUL_ORB)) {
                        htmltext = "31027-01.htm"
                        st.playSound(QuestState.SOUND_ITEMGET)
                        st.giveItems(REIRIA_SOUL_ORB, 1)
                    } else
                        htmltext = "31027-02.htm"

                    31028, 31029, 31030 -> {
                        val itemId = npc.npcId - 26361
                        if (cond == 2 && !st.hasQuestItems(itemId)) {
                            htmltext = npc.npcId.toString() + "-01.htm"
                            st.playSound(QuestState.SOUND_ITEMGET)
                            st.giveItems(itemId, 1)
                        } else
                            htmltext = npc.npcId.toString() + "-02.htm"
                    }
                }
            }

            Quest.STATE_COMPLETED -> htmltext = Quest.alreadyCompletedMsg
        }

        return htmltext
    }

    override fun onAttack(npc: Npc, attacker: Creature, damage: Int, skill: L2Skill?): String? {
        val player = attacker.actingPlayer

        val st = checkPlayerCondition(player, npc, "cond", "7") ?: return null

        if (player!!.activeWeaponItem != null && player.activeWeaponItem.itemId == PIPETTE_KNIFE && !st.hasQuestItems(
                RED_PIPETTE_KNIFE
            )
        ) {
            st.playSound(QuestState.SOUND_ITEMGET)
            st.takeItems(PIPETTE_KNIFE, 1)
            st.giveItems(RED_PIPETTE_KNIFE, 1)
        }

        return null
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        addSpawn(CHEST_SPAWN[npc.npcId] ?: -1, npc, true, 120000, false)

        return null
    }

    companion object {
        private val qn = "Q234_FatesWhisper"

        // Items
        private val REIRIA_SOUL_ORB = 4666
        private val KERMON_INFERNIUM_SCEPTER = 4667
        private val GOLKONDA_INFERNIUM_SCEPTER = 4668
        private val HALLATE_INFERNIUM_SCEPTER = 4669

        private val INFERNIUM_VARNISH = 4672
        private val REORIN_HAMMER = 4670
        private val REORIN_MOLD = 4671

        private val PIPETTE_KNIFE = 4665
        private val RED_PIPETTE_KNIFE = 4673

        private val CRYSTAL_B = 1460

        // Reward
        private val STAR_OF_DESTINY = 5011

        // Chest Spawn
        private val CHEST_SPAWN = HashMap<Int, Int>()

        // Weapons
        private val WEAPONS = HashMap<Int, String>()
    }
}