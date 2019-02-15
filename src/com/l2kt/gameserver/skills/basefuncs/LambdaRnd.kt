package com.l2kt.gameserver.skills.basefuncs

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.skills.Env

/**
 * @author mkizub
 */
class LambdaRnd(private val _max: Lambda, private val _linear: Boolean) : Lambda() {

    override fun calc(env: Env): Double {
        return _max.calc(env) * if (_linear) Rnd.nextDouble() else Rnd.nextGaussian()
    }
}