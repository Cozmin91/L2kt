package com.l2kt.gameserver.scripting.tasks

import com.l2kt.gameserver.data.manager.RaidPointManager
import com.l2kt.gameserver.data.sql.ClanTable
import com.l2kt.gameserver.model.pledge.Clan
import com.l2kt.gameserver.scripting.ScheduledQuest
import java.util.*

/**
 * Each month players hunt Raid Bosses and get raid points. At the end of the month top 100 players from the list get clan reputation points.<br></br>
 * <br></br>
 * The points are added after the first weekly game maintenance of the month.<br></br>
 * <br></br>
 * Only the players belonging to clans level 5 or higher can get such points.
 */
class RaidPointReset : ScheduledQuest(-1, "tasks") {

    public override fun onStart() {
        // Calculate ranks for the first 100 players.
        val ranks = RaidPointManager.winners

        // A temporary Map used to gather points for each clan. In the end we reward all clans.
        val rewards = HashMap<Clan, Int>()

        // Iterate clans.
        for (clan in ClanTable.clans) {
            // Don't bother with low level clans.
            if (clan.level < 5)
                continue

            // Iterate ranks.
            for ((key, value) in ranks) {
                // Don't bother if checked player isn't member of the clan.
                if (!clan.isMember(key))
                    continue

                var points: Int
                when (value) {
                    1 -> points = 1250
                    2 -> points = 900
                    3 -> points = 700
                    4 -> points = 600
                    5 -> points = 450
                    6 -> points = 350
                    7 -> points = 300
                    8 -> points = 200
                    9 -> points = 150
                    10 -> points = 100
                    else -> points = if (value <= 50) 25 else 12
                }
                (rewards).merge(
                    clan,
                    points
                ) { a, b -> Integer.sum(a, b) }
            }
        }

        // Iterate and reward CRPs to clans.
        for ((key, value) in rewards)
            key.addReputationScore(value)

        // Cleanup the data.
        RaidPointManager.cleanUp()
    }

    public override fun onEnd() {}
}