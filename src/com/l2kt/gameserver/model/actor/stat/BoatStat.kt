package com.l2kt.gameserver.model.actor.stat

import com.l2kt.gameserver.model.actor.Boat

class BoatStat(activeChar: Boat) : CreatureStat(activeChar) {
    private var _moveSpeed = 0
    var rotationSpeed = 0

    override val moveSpeed: Float
        get() = _moveSpeed.toFloat()

    fun setMoveSpeed(speed: Int) {
        _moveSpeed = speed
    }
}