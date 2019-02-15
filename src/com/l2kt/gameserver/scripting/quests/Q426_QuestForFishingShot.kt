package com.l2kt.gameserver.scripting.quests

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import java.util.*

class Q426_QuestForFishingShot : Quest(426, "Quest for Fishing Shot") {
    init {
        MOBS1[20005] = 45
        MOBS1[20013] = 100
        MOBS1[20016] = 100
        MOBS1[20017] = 115
        MOBS1[20030] = 105
        MOBS1[20132] = 70
        MOBS1[20038] = 135
        MOBS1[20044] = 125
        MOBS1[20046] = 100
        MOBS1[20047] = 100
        MOBS1[20050] = 140
        MOBS1[20058] = 140
        MOBS1[20063] = 160
        MOBS1[20066] = 170
        MOBS1[20070] = 180
        MOBS1[20074] = 195
        MOBS1[20077] = 205
        MOBS1[20078] = 205
        MOBS1[20079] = 205
        MOBS1[20080] = 220
        MOBS1[20081] = 370
        MOBS1[20083] = 245
        MOBS1[20084] = 255
        MOBS1[20085] = 265
        MOBS1[20087] = 565
        MOBS1[20088] = 605
        MOBS1[20089] = 250
        MOBS1[20100] = 85
        MOBS1[20103] = 110
        MOBS1[20105] = 110
        MOBS1[20115] = 190
        MOBS1[20120] = 20
        MOBS1[20131] = 45
        MOBS1[20135] = 360
        MOBS1[20157] = 235
        MOBS1[20162] = 195
        MOBS1[20176] = 280
        MOBS1[20211] = 170
        MOBS1[20225] = 160
        MOBS1[20227] = 180
        MOBS1[20230] = 260
        MOBS1[20232] = 245
        MOBS1[20234] = 290
        MOBS1[20241] = 700
        MOBS1[20267] = 215
        MOBS1[20268] = 295
        MOBS1[20269] = 255
        MOBS1[20270] = 365
        MOBS1[20271] = 295
        MOBS1[20286] = 700
        MOBS1[20308] = 110
        MOBS1[20312] = 45
        MOBS1[20317] = 20
        MOBS1[20324] = 85
        MOBS1[20333] = 100
        MOBS1[20341] = 100
        MOBS1[20346] = 85
        MOBS1[20349] = 850
        MOBS1[20356] = 165
        MOBS1[20357] = 140
        MOBS1[20363] = 70
        MOBS1[20368] = 85
        MOBS1[20371] = 100
        MOBS1[20386] = 85
        MOBS1[20389] = 90
        MOBS1[20403] = 110
        MOBS1[20404] = 95
        MOBS1[20433] = 100
        MOBS1[20436] = 140
        MOBS1[20448] = 45
        MOBS1[20456] = 20
        MOBS1[20463] = 85
        MOBS1[20470] = 45
        MOBS1[20471] = 85
        MOBS1[20475] = 20
        MOBS1[20478] = 110
        MOBS1[20487] = 90
        MOBS1[20511] = 100
        MOBS1[20525] = 20
        MOBS1[20528] = 100
        MOBS1[20536] = 15
        MOBS1[20537] = 15
        MOBS1[20538] = 15
        MOBS1[20539] = 15
        MOBS1[20544] = 15
        MOBS1[20550] = 300
        MOBS1[20551] = 300
        MOBS1[20552] = 650
        MOBS1[20553] = 335
        MOBS1[20554] = 390
        MOBS1[20555] = 350
        MOBS1[20557] = 390
        MOBS1[20559] = 420
        MOBS1[20560] = 440
        MOBS1[20562] = 485
        MOBS1[20573] = 545
        MOBS1[20575] = 645
        MOBS1[20630] = 350
        MOBS1[20632] = 475
        MOBS1[20634] = 960
        MOBS1[20636] = 495
        MOBS1[20638] = 540
        MOBS1[20641] = 680
        MOBS1[20643] = 660
        MOBS1[20644] = 645
        MOBS1[20659] = 440
        MOBS1[20661] = 575
        MOBS1[20663] = 525
        MOBS1[20665] = 680
        MOBS1[20667] = 730
        MOBS1[20766] = 210
        MOBS1[20781] = 270
        MOBS1[20783] = 140
        MOBS1[20784] = 155
        MOBS1[20786] = 170
        MOBS1[20788] = 325
        MOBS1[20790] = 390
        MOBS1[20792] = 620
        MOBS1[20794] = 635
        MOBS1[20796] = 640
        MOBS1[20798] = 850
        MOBS1[20800] = 740
        MOBS1[20802] = 900
        MOBS1[20804] = 775
        MOBS1[20806] = 805
        MOBS1[20833] = 455
        MOBS1[20834] = 680
        MOBS1[20836] = 785
        MOBS1[20837] = 835
        MOBS1[20839] = 430
        MOBS1[20841] = 460
        MOBS1[20845] = 605
        MOBS1[20847] = 570
        MOBS1[20849] = 585
        MOBS1[20936] = 290
        MOBS1[20937] = 315
        MOBS1[20939] = 385
        MOBS1[20940] = 500
        MOBS1[20941] = 460
        MOBS1[20943] = 345
        MOBS1[20944] = 335
        MOBS1[21100] = 125
        MOBS1[21101] = 155
        MOBS1[21103] = 215
        MOBS1[21105] = 310
        MOBS1[21107] = 600
        MOBS1[21117] = 120
        MOBS1[21023] = 170
        MOBS1[21024] = 175
        MOBS1[21025] = 185
        MOBS1[21026] = 200
        MOBS1[21034] = 195
        MOBS1[21125] = 12
        MOBS1[21263] = 650
        MOBS1[21520] = 880
        MOBS1[21526] = 970
        MOBS1[21536] = 985
        MOBS1[21602] = 555
        MOBS1[21603] = 750
        MOBS1[21605] = 620
        MOBS1[21606] = 875
        MOBS1[21611] = 590
        MOBS1[21612] = 835
        MOBS1[21617] = 615
        MOBS1[21618] = 875
        MOBS1[21635] = 775
        MOBS1[21638] = 165
        MOBS1[21639] = 185
        MOBS1[21641] = 195
        MOBS1[21644] = 170
    }

    init {
        MOBS2[20579] = 420
        MOBS2[20639] = 280
        MOBS2[20646] = 145
        MOBS2[20648] = 120
        MOBS2[20650] = 460
        MOBS2[20651] = 260
        MOBS2[20652] = 335
        MOBS2[20657] = 630
        MOBS2[20658] = 570
        MOBS2[20808] = 50
        MOBS2[20809] = 865
        MOBS2[20832] = 700
        MOBS2[20979] = 980
        MOBS2[20991] = 665
        MOBS2[20994] = 590
        MOBS2[21261] = 170
        MOBS2[21263] = 795
        MOBS2[21508] = 100
        MOBS2[21510] = 280
        MOBS2[21511] = 995
        MOBS2[21512] = 995
        MOBS2[21514] = 185
        MOBS2[21516] = 495
        MOBS2[21517] = 495
        MOBS2[21518] = 255
        MOBS2[21636] = 950
    }

    init {
        MOBS3[20655] = 110
        MOBS3[20656] = 150
        MOBS3[20772] = 105
        MOBS3[20810] = 50
        MOBS3[20812] = 490
        MOBS3[20814] = 775
        MOBS3[20816] = 875
        MOBS3[20819] = 280
        MOBS3[20955] = 670
        MOBS3[20978] = 555
        MOBS3[21058] = 355
        MOBS3[21060] = 45
        MOBS3[21075] = 110
        MOBS3[21078] = 610
        MOBS3[21081] = 955
        MOBS3[21264] = 920
    }

    init {
        MOBS4[20815] = 205
        MOBS4[20822] = 100
        MOBS4[20824] = 665
        MOBS4[20825] = 620
        MOBS4[20983] = 205
        MOBS4[21314] = 145
        MOBS4[21316] = 235
        MOBS4[21318] = 280
        MOBS4[21320] = 355
        MOBS4[21322] = 430
        MOBS4[21376] = 280
        MOBS4[21378] = 375
        MOBS4[21380] = 375
        MOBS4[21387] = 640
        MOBS4[21393] = 935
        MOBS4[21395] = 855
        MOBS4[21652] = 375
        MOBS4[21655] = 640
        MOBS4[21657] = 935
    }

    init {
        MOBS5[20828] = 935
        MOBS5[21061] = 530
        MOBS5[21069] = 825
        MOBS5[21382] = 125
        MOBS5[21384] = 400
        MOBS5[21390] = 750
        MOBS5[21654] = 400
        MOBS5[21656] = 750
    }

    init {
        MOBSspecial[20829] = intArrayOf(115, 6)
        MOBSspecial[20859] = intArrayOf(890, 8)
        MOBSspecial[21066] = intArrayOf(5, 5)
        MOBSspecial[21068] = intArrayOf(565, 11)
        MOBSspecial[21071] = intArrayOf(400, 12)
    }

    init {

        setItemsIds(SWEET_FLUID)

        addStartNpc(
            31562,
            31563,
            31564,
            31565,
            31566,
            31567,
            31568,
            31569,
            31570,
            31571,
            31572,
            31573,
            31574,
            31575,
            31576,
            31577,
            31578,
            31579,
            31696,
            31697,
            31989,
            32007
        )
        addTalkId(
            31562,
            31563,
            31564,
            31565,
            31566,
            31567,
            31568,
            31569,
            31570,
            31571,
            31572,
            31573,
            31574,
            31575,
            31576,
            31577,
            31578,
            31579,
            31696,
            31697,
            31989,
            32007
        )

        for (mob in MOBS1.keys)
            addKillId(mob)
        for (mob in MOBS2.keys)
            addKillId(mob)
        for (mob in MOBS3.keys)
            addKillId(mob)
        for (mob in MOBS4.keys)
            addKillId(mob)
        for (mob in MOBS5.keys)
            addKillId(mob)
        for (mob in MOBSspecial.keys)
            addKillId(mob)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (event.equals("03.htm", ignoreCase = true)) {
            st.state = Quest.STATE_STARTED
            st["cond"] = "1"
            st.playSound(QuestState.SOUND_ACCEPT)
        } else if (event.equals("08.htm", ignoreCase = true)) {
            st.playSound(QuestState.SOUND_FINISH)
            st.exitQuest(true)
        }

        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        var st = player.getQuestState(qn)
        var htmltext = Quest.noQuestMsg
        if (st == null)
            st = newQuestState(player)

        when (st.state) {
            Quest.STATE_CREATED -> htmltext = "01.htm"

            Quest.STATE_STARTED -> htmltext = if (st.hasQuestItems(SWEET_FLUID)) "05.htm" else "04.htm"
        }

        return htmltext
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        val player = killer.actingPlayer

        val st = getRandomPartyMemberState(player, npc, Quest.STATE_STARTED) ?: return null

        val npcId = npc.npcId
        var drop = 0
        var chance = 0

        if (MOBS1.containsKey(npcId))
            chance = MOBS1[npcId]!!
        else if (MOBS2.containsKey(npcId)) {
            chance = MOBS2[npcId]!!
            drop = 1
        } else if (MOBS3.containsKey(npcId)) {
            chance = MOBS3[npcId]!!
            drop = 2
        } else if (MOBS4.containsKey(npcId)) {
            chance = MOBS4[npcId]!!
            drop = 3
        } else if (MOBS5.containsKey(npcId)) {
            chance = MOBS5[npcId]!!
            drop = 4
        } else if (MOBSspecial.containsKey(npcId)) {
            chance = MOBSspecial[npcId]!![0]
            drop = MOBSspecial[npcId]!![1]
        }

        if (Rnd[1000] <= chance)
            drop++

        if (drop != 0) {
            st.playSound(QuestState.SOUND_ITEMGET)
            st.rewardItems(SWEET_FLUID, drop)
        }
        return null
    }

    companion object {
        private val qn = "Q426_QuestForFishingShot"

        private val SWEET_FLUID = 7586

        private val MOBS1 = HashMap<Int, Int>()

        private val MOBS2 = HashMap<Int, Int>()

        private val MOBS3 = HashMap<Int, Int>()

        private val MOBS4 = HashMap<Int, Int>()

        private val MOBS5 = HashMap<Int, Int>()

        private val MOBSspecial = HashMap<Int, IntArray>()
    }
}