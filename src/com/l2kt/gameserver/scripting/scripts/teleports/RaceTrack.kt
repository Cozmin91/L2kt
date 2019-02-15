package com.l2kt.gameserver.scripting.scripts.teleports

import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.scripting.Quest
import java.util.*

class RaceTrack : Quest(-1, "teleports") {
    init {
        RETURN_LOCATIONS[30320] = Location(-80826, 149775, -3043) // RICHLIN
        RETURN_LOCATIONS[30256] = Location(-12672, 122776, -3116) // BELLA
        RETURN_LOCATIONS[30059] = Location(15670, 142983, -2705) // TRISHA
        RETURN_LOCATIONS[30080] = Location(83400, 147943, -3404) // CLARISSA
        RETURN_LOCATIONS[30899] = Location(111409, 219364, -3545) // FLAUEN
        RETURN_LOCATIONS[30177] = Location(82956, 53162, -1495) // VALENTIA
        RETURN_LOCATIONS[30848] = Location(146331, 25762, -2018) // ELISA
        RETURN_LOCATIONS[30233] = Location(116819, 76994, -2714) // ESMERALDA
        RETURN_LOCATIONS[31320] = Location(43835, -47749, -792) // ILYANA
        RETURN_LOCATIONS[31275] = Location(147930, -55281, -2728) // TATIANA
        RETURN_LOCATIONS[31964] = Location(87386, -143246, -1293) // BILIA
        RETURN_LOCATIONS[31210] = Location(12882, 181053, -3560) // RACE TRACK GK
    }

    init {

        addStartNpc(30320, 30256, 30059, 30080, 30899, 30177, 30848, 30233, 31320, 31275, 31964, 31210)
        addTalkId(RACE_MANAGER, 30320, 30256, 30059, 30080, 30899, 30177, 30848, 30233, 31320, 31275, 31964, 31210)
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        val st = player.getQuestState(name)

        if (RETURN_LOCATIONS.containsKey(npc.npcId)) {
            player.teleToLocation(12661, 181687, -3560, 0)
            st!!.state = Quest.STATE_STARTED
            st["id"] = Integer.toString(npc.npcId)
        } else if (st!!.isStarted && npc.npcId == RACE_MANAGER) {
            player.teleToLocation(RETURN_LOCATIONS[st.getInt("id")], 0)
            st.exitQuest(true)
        }

        return null
    }

    companion object {
        private const val RACE_MANAGER = 30995

        private val RETURN_LOCATIONS = HashMap<Int, Location>()
    }
}