package com.l2kt.gameserver.skills.funcs

import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.skills.Stats
import com.l2kt.gameserver.skills.basefuncs.Func

object FuncHennaWIT : Func(Stats.STAT_WIT, 0x10, null, null) {

    override fun calc(env: Env) {
        val player = env.player
        if (player != null)
            env.addValue(player.hennaStatWIT.toDouble())
    }
}