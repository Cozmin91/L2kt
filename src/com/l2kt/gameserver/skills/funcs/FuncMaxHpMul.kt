package com.l2kt.gameserver.skills.funcs

import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.skills.Formulas
import com.l2kt.gameserver.skills.Stats
import com.l2kt.gameserver.skills.basefuncs.Func

object FuncMaxHpMul : Func(Stats.MAX_HP, 0x20, null, null) {

    override fun calc(env: Env) {
        env.mulValue(Formulas.CON_BONUS[env.character!!.con])
    }
}