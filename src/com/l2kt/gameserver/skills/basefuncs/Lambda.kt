package com.l2kt.gameserver.skills.basefuncs

import com.l2kt.gameserver.skills.Env

/**
 * @author mkizub
 */
abstract class Lambda {
    abstract fun calc(env: Env): Double
}