package com.l2kt.gameserver.skills.basefuncs

import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.skills.Stats
import com.l2kt.gameserver.skills.conditions.Condition
import java.lang.reflect.Constructor
import java.util.logging.Level
import java.util.logging.Logger

/**
 * @author mkizub
 */
class FuncTemplate(
    var attachCond: Condition?,
    val applayCond: Condition?,
    pFunc: String,
    val stat: Stats,
    val order: Int,
    val lambda: Lambda
) {
    val func: Class<*>
    val constructor: Constructor<*>

    init {

        try {
            func = Class.forName("com.l2kt.gameserver.skills.basefuncs.Func$pFunc")
        } catch (e: ClassNotFoundException) {
            throw RuntimeException(e)
        }

        try {
            constructor = func.getConstructor(
                Stats::class.java, Integer.TYPE, Any::class.java, Lambda::class.java// value for function
            )
        } catch (e: NoSuchMethodException) {
            throw RuntimeException(e)
        }

    }

    fun getFunc(env: Env, owner: Any): Func? {
        if (attachCond != null && !attachCond!!.test(env))
            return null

        return try {
            val f = constructor.newInstance(stat, order, owner, lambda) as Func
            if (applayCond != null)
                f.setCondition(applayCond)
            f
        } catch (e: Exception) {
            _log.log(Level.WARNING, "", e)
            null
        }

    }

    companion object {
        private val _log = Logger.getLogger(FuncTemplate::class.java.name)
    }
}