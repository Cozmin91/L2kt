package com.l2kt.gameserver.skills.funcs

import com.l2kt.gameserver.model.actor.Summon
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.skills.Formulas
import com.l2kt.gameserver.skills.Stats
import com.l2kt.gameserver.skills.basefuncs.Func

object FuncAtkAccuracy : Func(Stats.ACCURACY_COMBAT, 0x10, null, null) {

    override fun calc(env: Env) {
        val level = env.character!!.level

        env.addValue(Formulas.BASE_EVASION_ACCURACY[env.character!!.dex] + level)

        if (env.character is Summon)
            env.addValue((if (level < 60) 4 else 5).toDouble())
    }
}