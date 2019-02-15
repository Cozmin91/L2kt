package com.l2kt.gameserver.skills.basefuncs

import com.l2kt.gameserver.skills.Env

/**
 * @author mkizub
 */
class LambdaStats(private val _stat: StatsType) : Lambda() {
    enum class StatsType {
        PLAYER_LEVEL,
        TARGET_LEVEL,
        PLAYER_MAX_HP,
        PLAYER_MAX_MP
    }

    override fun calc(env: Env): Double {
        return when (_stat) {
            LambdaStats.StatsType.PLAYER_LEVEL -> (if (env.character == null) 1 else env.character!!.level).toDouble()

            LambdaStats.StatsType.TARGET_LEVEL -> (if (env.target == null) 1 else env.target!!.level).toDouble()

            LambdaStats.StatsType.PLAYER_MAX_HP -> (if (env.character == null) 1 else env.character!!.maxHp).toDouble()

            LambdaStats.StatsType.PLAYER_MAX_MP -> (if (env.character == null) 1 else env.character!!.maxMp).toDouble()


        }
    }
}