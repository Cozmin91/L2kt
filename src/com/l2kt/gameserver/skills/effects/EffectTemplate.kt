package com.l2kt.gameserver.skills.effects

import com.l2kt.gameserver.model.ChanceCondition
import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.skills.AbnormalEffect
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.skills.basefuncs.FuncTemplate
import com.l2kt.gameserver.skills.basefuncs.Lambda
import com.l2kt.gameserver.skills.conditions.Condition
import com.l2kt.gameserver.templates.skills.L2SkillType
import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger

class EffectTemplate(
    val attachCond: Condition?,
    val applayCond: Condition?,
    func: String,
    val lambda: Lambda,
    val counter: Int,
    val period: Int // in seconds
    ,
    val abnormalEffect: AbnormalEffect,
    val stackType: String,
    val stackOrder: Float,
    val icon: Boolean,
    val effectPower: Double // to handle chance
    ,
    val effectType: L2SkillType? // to handle resistances etc...
    ,
    val triggeredId: Int,
    val triggeredLevel: Int,
    val chanceCondition: ChanceCondition?
) {

    private val _func: Class<*>
    private val _constructor: Constructor<*>
    var funcTemplates: MutableList<FuncTemplate>? = null

    init {

        try {
            _func = Class.forName("com.l2kt.gameserver.skills.effects.Effect$func")
        } catch (e: ClassNotFoundException) {
            throw RuntimeException(e)
        }

        try {
            _constructor = _func.getConstructor(Env::class.java, EffectTemplate::class.java)
        } catch (e: NoSuchMethodException) {
            throw RuntimeException(e)
        }

    }

    fun getEffect(env: Env): L2Effect? {
        if (attachCond != null && !attachCond.test(env))
            return null
        try {
            return _constructor.newInstance(env, this) as L2Effect
        } catch (e: IllegalAccessException) {
            _log.log(Level.WARNING, "", e)
            return null
        } catch (e: InstantiationException) {
            _log.log(Level.WARNING, "", e)
            return null
        } catch (e: InvocationTargetException) {
            _log.log(
                Level.WARNING,
                "Error creating new instance of Class " + _func + " Exception was: " + e.targetException.message,
                e.targetException
            )
            return null
        }

    }

    fun attach(f: FuncTemplate) {
        if (funcTemplates == null)
            funcTemplates = ArrayList()

        funcTemplates!!.add(f)
    }

    companion object {
        internal var _log = Logger.getLogger(EffectTemplate::class.java.name)
    }
}