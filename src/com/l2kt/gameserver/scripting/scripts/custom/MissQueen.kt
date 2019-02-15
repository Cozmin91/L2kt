package com.l2kt.gameserver.scripting.scripts.custom

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.location.SpawnLocation
import com.l2kt.gameserver.scripting.Quest

class MissQueen : Quest(-1, "custom") {
    init {

        // Spawn the 11 NPCs.
        for (loc in LOCATIONS)
            addSpawn(31760, loc, false, 0, false)

        addStartNpc(31760)
        addTalkId(31760)
        addFirstTalkId(31760)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = event
        val st = player?.getQuestState(qn)

        if (event.equals("newbie_coupon", ignoreCase = true)) {
            if (player?.classId?.level() == 0 && player.level >= 6 && player.level <= 25 && player.pkKills <= 0) {
                if (st!!.getInt("reward_1") == 1)
                    htmltext = "31760-01.htm"
                else {
                    st.state = Quest.STATE_STARTED
                    htmltext = "31760-02.htm"
                    st["reward_1"] = "1"
                    st.giveItems(COUPON_ONE, 1)
                }
            } else
                htmltext = "31760-03.htm"
        } else if (event.equals("traveller_coupon", ignoreCase = true)) {
            if (player?.classId?.level() == 1 && player.level >= 6 && player.level <= 25 && player.pkKills <= 0) {
                if (st!!.getInt("reward_2") == 1)
                    htmltext = "31760-04.htm"
                else {
                    st.state = Quest.STATE_STARTED
                    htmltext = "31760-05.htm"
                    st["reward_2"] = "1"
                    st.giveItems(COUPON_TWO, 1)
                }
            } else
                htmltext = "31760-06.htm"
        }

        return htmltext
    }

    override fun onFirstTalk(npc: Npc, player: Player): String? {
        var st = player.getQuestState(qn)
        if (st == null)
            st = newQuestState(player)

        return "31760.htm"
    }

    companion object {
        private const val qn = "MissQueen"

        // Rewards
        private const val COUPON_ONE = 7832
        private const val COUPON_TWO = 7833

        // Miss Queen locations
        private val LOCATIONS = arrayOf(
            SpawnLocation(116224, -181728, -1378, 0),
            SpawnLocation(114885, -178092, -832, 0),
            SpawnLocation(45472, 49312, -3072, 53000),
            SpawnLocation(47648, 51296, -2994, 38500),
            SpawnLocation(11340, 15972, -4582, 14000),
            SpawnLocation(10968, 17540, -4572, 55000),
            SpawnLocation(-14048, 123184, -3120, 32000),
            SpawnLocation(-44979, -113508, -199, 32000),
            SpawnLocation(-84119, 243254, -3730, 8000),
            SpawnLocation(-84336, 242156, -3730, 24500),
            SpawnLocation(-82032, 150160, -3127, 16500)
        )
    }
}