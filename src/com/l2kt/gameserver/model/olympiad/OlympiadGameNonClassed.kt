package com.l2kt.gameserver.model.olympiad

import com.l2kt.Config

/**
 * @author DS
 */
class OlympiadGameNonClassed private constructor(id: Int, opponents: Array<Participant>) :
    OlympiadGameNormal(id, opponents) {

    override val type: CompetitionType
        get() = CompetitionType.NON_CLASSED

    override val divider: Int
        get() = Config.ALT_OLY_DIVIDER_NON_CLASSED

    override val reward: Array<IntArray>
        get() = Config.ALT_OLY_NONCLASSED_REWARD

    companion object {
        fun createGame(id: Int, list: MutableList<Int>): OlympiadGameNonClassed? {
            val opponents = createListOfParticipants(list) ?: return null

            return OlympiadGameNonClassed(id, opponents)
        }
    }
}