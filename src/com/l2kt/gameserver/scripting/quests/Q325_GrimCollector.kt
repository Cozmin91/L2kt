package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.holder.IntIntHolder
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.*

class Q325_GrimCollector : Quest(325, "Grim Collector") {
    init {
        DROPLIST[20026] =
                Arrays.asList(
                    IntIntHolder(ZOMBIE_HEAD, 30),
                    IntIntHolder(ZOMBIE_HEART, 50),
                    IntIntHolder(ZOMBIE_LIVER, 75)
                )
        DROPLIST[20029] =
                Arrays.asList(
                    IntIntHolder(ZOMBIE_HEAD, 30),
                    IntIntHolder(ZOMBIE_HEART, 52),
                    IntIntHolder(ZOMBIE_LIVER, 75)
                )
        DROPLIST[20035] = Arrays.asList(
            IntIntHolder(SKULL, 5),
            IntIntHolder(RIB_BONE, 15),
            IntIntHolder(SPINE, 29),
            IntIntHolder(THIGH_BONE, 79)
        )
        DROPLIST[20042] = Arrays.asList(
            IntIntHolder(SKULL, 6),
            IntIntHolder(RIB_BONE, 19),
            IntIntHolder(ARM_BONE, 69),
            IntIntHolder(THIGH_BONE, 86)
        )
        DROPLIST[20045] = Arrays.asList(
            IntIntHolder(SKULL, 9),
            IntIntHolder(SPINE, 59),
            IntIntHolder(ARM_BONE, 77),
            IntIntHolder(THIGH_BONE, 97)
        )
        DROPLIST[20051] = Arrays.asList(
            IntIntHolder(SKULL, 9),
            IntIntHolder(RIB_BONE, 59),
            IntIntHolder(SPINE, 79),
            IntIntHolder(ARM_BONE, 100)
        )
        DROPLIST[20457] =
                Arrays.asList(
                    IntIntHolder(ZOMBIE_HEAD, 40),
                    IntIntHolder(ZOMBIE_HEART, 60),
                    IntIntHolder(ZOMBIE_LIVER, 80)
                )
        DROPLIST[20458] = Arrays.asList(
            IntIntHolder(ZOMBIE_HEAD, 40),
            IntIntHolder(ZOMBIE_HEART, 70),
            IntIntHolder(ZOMBIE_LIVER, 100)
        )
        DROPLIST[20514] = Arrays.asList(
            IntIntHolder(SKULL, 6),
            IntIntHolder(RIB_BONE, 21),
            IntIntHolder(SPINE, 30),
            IntIntHolder(ARM_BONE, 31),
            IntIntHolder(THIGH_BONE, 64)
        )
        DROPLIST[20515] = Arrays.asList(
            IntIntHolder(SKULL, 5),
            IntIntHolder(RIB_BONE, 20),
            IntIntHolder(SPINE, 31),
            IntIntHolder(ARM_BONE, 33),
            IntIntHolder(THIGH_BONE, 69)
        )
    }

    init {

        setItemsIds(
            ZOMBIE_HEAD,
            ZOMBIE_HEART,
            ZOMBIE_LIVER,
            SKULL,
            RIB_BONE,
            SPINE,
            ARM_BONE,
            THIGH_BONE,
            COMPLETE_SKELETON,
            ANATOMY_DIAGRAM
        )

        addStartNpc(CURTIS)
        addTalkId(CURTIS, VARSAK, SAMED)

        for (npcId in DROPLIST.keys)
            addKillId(npcId)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn) ?: return htmltext

        if (event.equals("30336-03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("30434-03.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_ITEMGET)
            st.giveItems(ANATOMY_DIAGRAM, 1)
        } else if (event.equals("30434-06.htm", ignoreCase = true)) {
            st.takeItems(ANATOMY_DIAGRAM, -1)
            payback(st)
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        } else if (event.equals("30434-07.htm", ignoreCase = true)) {
            payback(st)
        } else if (event.equals("30434-09.htm", ignoreCase = true)) {
            val skeletons = st.getQuestItemsCount(COMPLETE_SKELETON)
            if (skeletons > 0) {
                st.playSound(QuestState.SOUND_MIDDLE)
                st.takeItems(COMPLETE_SKELETON, -1)
                st.rewardItems(57, 543 + 341 * skeletons)
            }
        } else if (event.equals("30342-03.htm", ignoreCase = true)) {
            if (!st.hasQuestItems(SPINE, ARM_BONE, SKULL, RIB_BONE, THIGH_BONE))
                htmltext = "30342-02.htm"
            else {
                st.takeItems(SPINE, 1)
                st.takeItems(SKULL, 1)
                st.takeItems(ARM_BONE, 1)
                st.takeItems(RIB_BONE, 1)
                st.takeItems(THIGH_BONE, 1)

                if (Rnd[10] < 9)
                    st.giveItems(COMPLETE_SKELETON, 1)
                else
                    htmltext = "30342-04.htm"
            }
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            return htmltext

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = if (player.level < 15) "30336-01.htm" else "30336-02.htm"

            Quest.STATE_STARTED -> when (npc.npcId) {
                CURTIS -> htmltext = if (!st.hasQuestItems(ANATOMY_DIAGRAM)) "30336-04.htm" else "30336-05.htm"

                SAMED -> if (!st.hasQuestItems(ANATOMY_DIAGRAM))
                    htmltext = "30434-01.htm"
                else {
                    if (getNumberOfPieces(st) == 0)
                        htmltext = "30434-04.htm"
                    else
                        htmltext = if (!st.hasQuestItems(COMPLETE_SKELETON)) "30434-05.htm" else "30434-08.htm"
                }

                VARSAK -> htmltext = "30342-01.htm"
            }
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        val player = killer?.actingPlayer

        val st = checkPlayerState(player, npc, Quest.STATE_STARTED) ?: return null

        if (st.hasQuestItems(ANATOMY_DIAGRAM)) {
            val chance = Rnd[100]
            for (drop in DROPLIST[npc.npcId] ?: emptyList()) {
                if (chance < drop.value) {
                    st.dropItemsAlways(drop.id, 1, 0)
                    break
                }
            }
        }

        return null
    }

    companion object {
        private val qn = "Q325_GrimCollector"

        // Items
        private val ANATOMY_DIAGRAM = 1349
        private val ZOMBIE_HEAD = 1350
        private val ZOMBIE_HEART = 1351
        private val ZOMBIE_LIVER = 1352
        private val SKULL = 1353
        private val RIB_BONE = 1354
        private val SPINE = 1355
        private val ARM_BONE = 1356
        private val THIGH_BONE = 1357
        private val COMPLETE_SKELETON = 1358

        // NPCs
        private val CURTIS = 30336
        private val VARSAK = 30342
        private val SAMED = 30434

        private val DROPLIST = HashMap<Int, List<IntIntHolder>>()

        private fun getNumberOfPieces(st: QuestState): Int {
            return st.getQuestItemsCount(ZOMBIE_HEAD) + st.getQuestItemsCount(SPINE) + st.getQuestItemsCount(ARM_BONE) + st.getQuestItemsCount(
                ZOMBIE_HEART
            ) + st.getQuestItemsCount(ZOMBIE_LIVER) + st.getQuestItemsCount(SKULL) + st.getQuestItemsCount(RIB_BONE) + st.getQuestItemsCount(
                THIGH_BONE
            ) + st.getQuestItemsCount(COMPLETE_SKELETON)
        }

        private fun payback(st: QuestState) {
            val count = getNumberOfPieces(st)
            if (count > 0) {
                var reward =
                    30 * st.getQuestItemsCount(ZOMBIE_HEAD) + 20 * st.getQuestItemsCount(ZOMBIE_HEART) + 20 * st.getQuestItemsCount(
                        ZOMBIE_LIVER
                    ) + 100 * st.getQuestItemsCount(SKULL) + 40 * st.getQuestItemsCount(RIB_BONE) + 14 * st.getQuestItemsCount(
                        SPINE
                    ) + 14 * st.getQuestItemsCount(ARM_BONE) + 14 * st.getQuestItemsCount(THIGH_BONE) + 341 * st.getQuestItemsCount(
                        COMPLETE_SKELETON
                    )
                if (count > 10)
                    reward += 1629

                if (st.hasQuestItems(COMPLETE_SKELETON))
                    reward += 543

                st.takeItems(ZOMBIE_HEAD, -1)
                st.takeItems(ZOMBIE_HEART, -1)
                st.takeItems(ZOMBIE_LIVER, -1)
                st.takeItems(SKULL, -1)
                st.takeItems(RIB_BONE, -1)
                st.takeItems(SPINE, -1)
                st.takeItems(ARM_BONE, -1)
                st.takeItems(THIGH_BONE, -1)
                st.takeItems(COMPLETE_SKELETON, -1)

                st.rewardItems(57, reward)
            }
        }
    }
}