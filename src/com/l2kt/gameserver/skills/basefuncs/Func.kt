package com.l2kt.gameserver.skills.basefuncs

import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.skills.Stats
import com.l2kt.gameserver.skills.conditions.Condition

/**
 * A Func object is a component of a Calculator created to manage and dynamically calculate the effect of a character property (ex : MAX_HP, REGENERATE_HP_RATE...).<br></br>
 * In fact, each calculator is a table of Func object in which each Func represents a mathematics function:<br></br>
 * FuncAtkAccuracy -> Math.sqrt(_player.getDEX())*6+_player.getLevel()<br></br>
 * When the calc method of a calculator is launched, each mathematics function is called according to its priority <B>_order</B>.<br></br>
 * Indeed, Func with lowest priority order is executed first and Funcs with the same order are executed in unspecified order.<br></br>
 * The result of the calculation is stored in the value property of an Env class instance.
 */
abstract class Func (val stat: Stats, val order: Int, val funcOwner: Any?, var _lambda: Lambda?) {

    /**
     * Function may be disabled by attached condition.
     */
    var cond: Condition? = null

    /**
     * Add a condition to the Func.
     * @param pCond
     */
    fun setCondition(pCond: Condition) {
        cond = pCond
    }

    /**
     * Run the mathematics function of the Func.
     * @param env
     */
    abstract fun calc(env: Env)
}
