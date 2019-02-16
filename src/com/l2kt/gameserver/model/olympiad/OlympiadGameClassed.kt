package com.l2kt.gameserver.model.olympiad

import com.l2kt.Config
import com.l2kt.commons.random.Rnd

/**
 * @author DS
 */
class OlympiadGameClassed private constructor(id: Int, opponents: Array<Participant>) :
    OlympiadGameNormal(id, opponents) {

    override val type: CompetitionType
        get() = CompetitionType.CLASSED

    override val divider: Int
        get() = Config.ALT_OLY_DIVIDER_CLASSED

    override val reward: Array<IntArray>
        get() = Config.ALT_OLY_CLASSED_REWARD

    companion object {
        fun createGame(id: Int, classList: MutableList<MutableList<Int>>?): OlympiadGameClassed? {
            if (classList == null || classList.isEmpty())
                return null

            var list: MutableList<Int>?
            var opponents: Array<Participant>?
            while (!classList.isEmpty()) {
                list = Rnd[classList]
                if (list == null || list.size < 2) {
                    classList.remove(list)
                    continue
                }

                opponents = createListOfParticipants(list)
                if (opponents == null) {
                    classList.remove(list)
                    continue
                }

                return OlympiadGameClassed(id, opponents)
            }
            return null
        }
    }
}