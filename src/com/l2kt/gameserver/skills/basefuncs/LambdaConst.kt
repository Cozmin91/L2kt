package com.l2kt.gameserver.skills.basefuncs

import com.l2kt.gameserver.skills.Env

/**
 * @author mkizub
 */
class LambdaConst(private val _value: Double) : Lambda() {

    override fun calc(env: Env): Double {
        return _value
    }
}