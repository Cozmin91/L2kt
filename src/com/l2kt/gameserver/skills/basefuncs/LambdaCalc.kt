package com.l2kt.gameserver.skills.basefuncs

import com.l2kt.gameserver.skills.Env
import java.util.*

/**
 * @author mkizub
 */
class LambdaCalc : Lambda() {
    private val _funcs: MutableList<Func>

    val funcs: List<Func>
        get() = _funcs

    init {
        _funcs = ArrayList()
    }

    override fun calc(env: Env): Double {
        val saveValue = env.value
        try {
            env.value = 0.0
            for (f in _funcs)
                f.calc(env)

            return env.value
        } finally {
            env.value = saveValue
        }
    }

    fun addFunc(f: Func) {
        _funcs.add(f)
    }
}