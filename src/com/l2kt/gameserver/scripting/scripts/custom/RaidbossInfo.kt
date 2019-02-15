package com.l2kt.gameserver.scripting.scripts.custom

import com.l2kt.commons.lang.StringUtil
import com.l2kt.gameserver.data.SpawnTable
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.scripting.Quest
import java.util.*

class RaidbossInfo : Quest(-1, "custom") {
    init {

        for (npcId in NPCs) {
            addStartNpc(npcId)
            addTalkId(npcId)
        }

        // Add all Raid Bosses locations.
        for (spawn in SpawnTable.spawnTable) {
            if (spawn.template.isType(BOSS_CLASS_TYPE))
                RADARS[spawn.npcId] = spawn.loc
        }
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        val st = player?.getQuestState(qn) ?: return event

        if (StringUtil.isDigit(event)) {
            val rbid = event.toInt()

            if (RADARS.containsKey(rbid)) {
                val loc = RADARS[rbid]!!
                st.addRadar(loc.x, loc.y, loc.z)
            }
            st.exitQuest(true)
            return null
        }
        return event
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        return "info.htm"
    }

    companion object {
        private const val qn = "RaidbossInfo"
        private const val BOSS_CLASS_TYPE = "RaidBoss"

        private val RADARS = HashMap<Int, Location>()

        private val NPCs = intArrayOf(
            31729,
            31730,
            31731,
            31732,
            31733,
            31734,
            31735,
            31736,
            31737,
            31738,
            31775,
            31776,
            31777,
            31778,
            31779,
            31780,
            31781,
            31782,
            31783,
            31784,
            31785,
            31786,
            31787,
            31788,
            31789,
            31790,
            31791,
            31792,
            31793,
            31794,
            31795,
            31796,
            31797,
            31798,
            31799,
            31800,
            31801,
            31802,
            31803,
            31804,
            31805,
            31806,
            31807,
            31808,
            31809,
            31810,
            31811,
            31812,
            31813,
            31814,
            31815,
            31816,
            31817,
            31818,
            31819,
            31820,
            31821,
            31822,
            31823,
            31824,
            31825,
            31826,
            31827,
            31828,
            31829,
            31830,
            31831,
            31832,
            31833,
            31834,
            31835,
            31836,
            31837,
            31838,
            31839,
            31840,
            31841
        )
    }
}