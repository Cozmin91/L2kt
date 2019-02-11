package com.l2kt.gameserver.skills.conditions

import com.l2kt.gameserver.skills.Env

class ConditionLogicAnd : Condition() {
    var conditions: Array<Condition?> = _emptyConditions

    override var listener: ConditionListener?
        get() = super.listener
        set(listener) {
            if (listener != null) {
                for (c in conditions.filterNotNull())
                    c.listener = this
            } else {
                for (c in conditions.filterNotNull())
                    c.listener = null
            }
            super.listener = listener
        }

    fun add(condition: Condition?) {
        if (condition == null)
            return
        if (listener != null)
            condition.listener = this
        val len = conditions.size
        val tmp = arrayOfNulls<Condition>(len + 1)

        System.arraycopy(conditions, 0, tmp, 0, len)
        tmp[len] = condition
        conditions = tmp
    }

    override fun testImpl(env: Env): Boolean {
        for (c in conditions.filterNotNull()) {
            if (!c.test(env))
                return false
        }
        return true
    }

    companion object {
        private val _emptyConditions = arrayOfNulls<Condition>(0)
    }
}