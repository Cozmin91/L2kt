package com.l2kt.gameserver.skills.funcs

import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.skills.Formulas
import com.l2kt.gameserver.skills.Stats
import com.l2kt.gameserver.skills.basefuncs.Func

object FuncMAtkCritical : Func(Stats.MCRITICAL_RATE, 0x09, null, null) {

    override fun calc(env: Env) {
        val player = env.character
        if (player is Player) {
            if (player.activeWeaponInstance != null)
                env.mulValue(Formulas.WIT_BONUS[player.wit])
        } else
            env.mulValue(Formulas.WIT_BONUS[player!!.wit])

        env.baseValue = env.value
    }
}