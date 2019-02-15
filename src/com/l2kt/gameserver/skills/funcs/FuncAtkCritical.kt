package com.l2kt.gameserver.skills.funcs

import com.l2kt.gameserver.model.actor.Summon
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.skills.Formulas
import com.l2kt.gameserver.skills.Stats
import com.l2kt.gameserver.skills.basefuncs.Func

object FuncAtkCritical : Func(Stats.CRITICAL_RATE, 0x09, null, null) {

    override fun calc(env: Env) {
        if (env.character !is Summon)
            env.mulValue(Formulas.DEX_BONUS[env.character!!.dex])

        env.mulValue(10.0)

        env.baseValue = env.value
    }
}
