package com.l2kt.gameserver.skills.funcs

import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.skills.Formulas
import com.l2kt.gameserver.skills.Stats
import com.l2kt.gameserver.skills.basefuncs.Func

object FuncAtkEvasion : Func(Stats.EVASION_RATE, 0x10, null, null) {

    override fun calc(env: Env) {
        env.addValue(Formulas.BASE_EVASION_ACCURACY[env.character!!.dex] + env.character!!.level)
    }
}