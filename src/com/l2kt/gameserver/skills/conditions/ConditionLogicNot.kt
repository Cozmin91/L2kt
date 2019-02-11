package com.l2kt.gameserver.skills.conditions

import com.l2kt.gameserver.skills.Env

class ConditionLogicNot
    (private val _condition: Condition) : Condition() {

    override var listener: ConditionListener?
        get() = super.listener
        set(listener) {
            if (listener != null)
                _condition.listener = this
            else
                _condition.listener = null
            super.listener = listener
        }

    init {
        if (listener != null)
            _condition.listener = this
    }

    override fun testImpl(env: Env): Boolean {
        return !_condition.test(env)
    }
}
