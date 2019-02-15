package com.l2kt.gameserver.skills.funcs

import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.skills.Stats
import com.l2kt.gameserver.skills.basefuncs.Func

object FuncHennaMEN : Func(Stats.STAT_MEN, 0x10, null, null) {

    override fun calc(env: Env) {
        val player = env.player
        if (player != null)
            env.addValue(player.hennaStatMEN.toDouble())
    }
}