package com.l2kt.gameserver.skills.funcs

import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.skills.Formulas
import com.l2kt.gameserver.skills.Stats
import com.l2kt.gameserver.skills.basefuncs.Func

object FuncMAtkMod : Func(Stats.MAGIC_ATTACK, 0x20, null, null) {

    override fun calc(env: Env) {
        val intb = Formulas.INT_BONUS[env.character!!.int]
        val lvlb = env.character!!.levelMod

        env.mulValue(lvlb * lvlb * (intb * intb))
    }
}