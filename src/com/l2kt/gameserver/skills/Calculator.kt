package com.l2kt.gameserver.skills

import com.l2kt.gameserver.skills.basefuncs.Func
import java.util.*

/**
 * A calculator is created to manage and dynamically calculate the effect of a character property (ex : MAX_HP, REGENERATE_HP_RATE...). In fact, each calculator is a table of Func object in which each Func represents a mathematic function : <BR></BR>
 * <BR></BR>
 * FuncAtkAccuracy -> Math.sqrt(_player.getDEX())*6+_player.getLevel()<BR></BR>
 * <BR></BR>
 * When the calc method of a calculator is launched, each mathematic function is called according to its priority <B>_order</B>. Indeed, Func with lowest priority order is executed first and Funcs with the same order are executed in unspecified order. The result of the calculation is stored in the
 * value property of an Env class instance.<BR></BR>
 * <BR></BR>
 * Method addFunc and removeFunc permit to add and remove a Func object from a Calculator.<BR></BR>
 * <BR></BR>
 */
class Calculator() {

    /** Table of Func object  */
    private var _functions: Array<Func?> = emptyArray()

    constructor(c: Calculator) : this() {
        _functions = c._functions
    }

    /**
     * @return the number of Funcs in the Calculator.
     */
    fun size(): Int {
        return _functions.size
    }

    /**
     * Add a Func to the Calculator.
     * @param f
     */
    @Synchronized
    fun addFunc(f: Func) {
        val funcs = _functions
        val tmp = arrayOfNulls<Func>(funcs.size + 1)

        val order = f.order
        var i = 0

        while (i < funcs.size && order >= funcs[i]!!.order) {
            tmp[i] = funcs[i]
            i++
        }

        tmp[i] = f

        while (i < funcs.size) {
            tmp[i + 1] = funcs[i]
            i++
        }

        _functions = tmp
    }

    /**
     * Remove a Func from the Calculator.
     * @param f
     */
    @Synchronized
    fun removeFunc(f: Func) {
        val funcs = _functions
        val tmp = arrayOfNulls<Func>(funcs.size - 1)

        var i = 0

        while (i < funcs.size && f !== funcs[i]) {
            tmp[i] = funcs[i]
            i++
        }

        if (i == funcs.size)
            return

        i++
        while (i < funcs.size) {
            tmp[i - 1] = funcs[i]
            i++
        }

        _functions = if (tmp.isEmpty())
            emptyArray()
        else
            tmp
    }

    /**
     * Remove each Func with the specified owner of the Calculator.
     * @param owner
     * @return a list containing all left stats.
     */
    @Synchronized
    fun removeOwner(owner: Any): List<Stats> {
        val modifiedStats = ArrayList<Stats>()

        for (func in _functions.filterNotNull()) {
            if (func.funcOwner === owner) {
                modifiedStats.add(func.stat)
                removeFunc(func)
            }
        }
        return modifiedStats
    }

    /**
     * Run each Func of the Calculator.
     * @param env
     */
    fun calc(env: Env) {
        for (func in _functions)
            func?.calc(env)
    }
}