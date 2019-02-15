package com.l2kt.gameserver.scripting.scripts.custom

import com.l2kt.commons.lang.StringUtil
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.scripting.Quest
import java.util.*

/**
 * @authors DrLecter (python), Plim (java)
 * @notes Formerly based on Elektra's script
 */
class EchoCrystals : Quest(-1, "custom") {
    init {
        SCORES[4410] = ScoreData(4411, "01", "02", "03")
        SCORES[4409] = ScoreData(4412, "04", "05", "06")
        SCORES[4408] = ScoreData(4413, "07", "08", "09")
        SCORES[4420] = ScoreData(4414, "10", "11", "12")
        SCORES[4421] = ScoreData(4415, "13", "14", "15")
        SCORES[4419] = ScoreData(4417, "16", "05", "06")
        SCORES[4418] = ScoreData(4416, "17", "05", "06")
        addStartNpc(31042, 31043)
        addTalkId(31042, 31043)
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        var htmltext = ""
        val st = player?.getQuestState(qn)

        if (st != null && StringUtil.isDigit(event)) {
            val score = Integer.parseInt(event)
            if (SCORES.containsKey(score)) {
                val crystal = SCORES[score]!!.crystalId
                val ok = SCORES[score]!!.okMsg
                val noadena = SCORES[score]!!.noAdenaMsg
                val noscore = SCORES[score]!!.noScoreMsg

                when {
                    st.getQuestItemsCount(score) == 0 -> htmltext = npc?.npcId.toString() + "-" + noscore + ".htm"
                    st.getQuestItemsCount(ADENA) < COST -> htmltext = npc?.npcId.toString() + "-" + noadena + ".htm"
                    else -> {
                        st.takeItems(ADENA, COST)
                        st.giveItems(crystal, 1)
                        htmltext = npc?.npcId.toString() + "-" + ok + ".htm"
                    }
                }
            }
        }

        return htmltext
    }

    override fun onTalk(npc: Npc, player: Player): String? {
        return "1.htm"
    }

    private inner class ScoreData(val crystalId: Int, val okMsg: String, val noAdenaMsg: String, val noScoreMsg: String)

    companion object {
        private const val qn = "EchoCrystals"

        private const val ADENA = 57
        private const val COST = 200

        private val SCORES = HashMap<Int, ScoreData>()
    }
}