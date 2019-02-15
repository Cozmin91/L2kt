package com.l2kt.gameserver.skills.funcs

import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.skills.Formulas
import com.l2kt.gameserver.skills.Stats
import com.l2kt.gameserver.skills.basefuncs.Func

object FuncMAtkSpeed : Func(Stats.MAGIC_ATTACK_SPEED, 0x20, null, null) {

    override fun calc(env: Env) {
        env.mulValue(Formulas.WIT_BONUS[env.character!!.wit])
    }
}