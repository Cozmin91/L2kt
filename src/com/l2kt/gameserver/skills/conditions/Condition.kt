package com.l2kt.gameserver.skills.conditions

import com.l2kt.gameserver.skills.Env

abstract class Condition : ConditionListener {
    open var listener: ConditionListener? = null
        set(listener) {
            field = listener
            notifyChanged()
        }

    var message: String? = null

    var messageId: Int = 0

    var isAddName = false
        private set
    private var _result: Boolean = false

    fun addName() {
        isAddName = true
    }

    fun test(env: Env): Boolean {
        val res = testImpl(env)
        if (listener != null && res != _result) {
            _result = res
            notifyChanged()
        }
        return res
    }

    internal abstract fun testImpl(env: Env): Boolean

    override fun notifyChanged() {
        if (listener != null)
            listener!!.notifyChanged()
    }
}