package com.l2kt.gameserver.skills.conditions

import com.l2kt.gameserver.skills.Env

class ConditionLogicAnd : Condition() {
    var conditions = _emptyConditions.filterNotNull()

    override var listener: ConditionListener?
        get() = super.listener
        set(listener) {
            if (listener != null) {
                for (c in conditions)
                    c.listener = this
            } else {
                for (c in conditions)
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
        conditions = tmp.filterNotNull()
    }

    override fun testImpl(env: Env): Boolean {
        for (c in conditions) {
            if (!c.test(env))
                return false
        }
        return true
    }

    companion object {
        private val _emptyConditions = arrayOfNulls<Condition>(0)
    }
}