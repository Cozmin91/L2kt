package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.Creature

/**
 * format dddddd dddh (h)
 */
class MagicSkillUse : L2GameServerPacket {
    private val _targetId: Int
    private val _skillId: Int
    private val _skillLevel: Int
    private val _hitTime: Int
    private val _reuseDelay: Int
    private val _charObjId: Int
    private val _x: Int
    private val _y: Int
    private val _z: Int
    private val _targetx: Int
    private val _targety: Int
    private val _targetz: Int
    private var _success = false

    constructor(
        cha: Creature,
        target: Creature,
        skillId: Int,
        skillLevel: Int,
        hitTime: Int,
        reuseDelay: Int,
        crit: Boolean
    ) : this(cha, target, skillId, skillLevel, hitTime, reuseDelay) {
        _success = crit
    }

    constructor(cha: Creature, target: Creature, skillId: Int, skillLevel: Int, hitTime: Int, reuseDelay: Int) {
        _charObjId = cha.objectId
        _targetId = target.objectId
        _skillId = skillId
        _skillLevel = skillLevel
        _hitTime = hitTime
        _reuseDelay = reuseDelay
        _x = cha.x
        _y = cha.y
        _z = cha.z
        _targetx = target.x
        _targety = target.y
        _targetz = target.z
    }

    constructor(cha: Creature, skillId: Int, skillLevel: Int, hitTime: Int, reuseDelay: Int) {
        _charObjId = cha.objectId
        _targetId = cha.targetId
        _skillId = skillId
        _skillLevel = skillLevel
        _hitTime = hitTime
        _reuseDelay = reuseDelay
        _x = cha.x
        _y = cha.y
        _z = cha.z
        _targetx = cha.x
        _targety = cha.y
        _targetz = cha.z
    }

    override fun writeImpl() {
        writeC(0x48)
        writeD(_charObjId)
        writeD(_targetId)
        writeD(_skillId)
        writeD(_skillLevel)
        writeD(_hitTime)
        writeD(_reuseDelay)
        writeD(_x)
        writeD(_y)
        writeD(_z)
        if (_success) {
            writeD(0x01)
            writeH(0x00)
        } else
            writeD(0x00)
        writeD(_targetx)
        writeD(_targety)
        writeD(_targetz)
    }
}