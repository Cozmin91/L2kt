package com.l2kt.gameserver.skills.funcs

import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.skills.Stats
import com.l2kt.gameserver.skills.basefuncs.Func

object FuncHennaCON : Func(Stats.STAT_CON, 0x10, null, null) {

    override fun calc(env: Env) {
        val player = env.player
        if (player != null)
            env.addValue(player.hennaStatCON.toDouble())
    }
}