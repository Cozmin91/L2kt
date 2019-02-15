package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player

/**
 * format dddc dddh (ddc)
 */
class Attack (attacker: Creature, val soulshot: Boolean, val _ssGrade: Int) : L2GameServerPacket() {

    private val _attackerObjId: Int = attacker.objectId
    private val _x: Int = attacker.x
    private val _y: Int = attacker.y
    private val _z: Int = attacker.z
    private var _hits: Array<Hit>? = null

    inner class Hit internal constructor(
        target: WorldObject,
        val _damage: Int,
        miss: Boolean,
        crit: Boolean,
        shld: Byte
    ) {
        var _targetId: Int = 0
        var _flags: Int = 0

        init {
            run{
                _targetId = target.objectId

                if (miss) {
                    _flags = HITFLAG_MISS
                    return@run
                }

                if (soulshot)
                    _flags = HITFLAG_USESS or _ssGrade

                if (crit)
                    _flags = _flags or HITFLAG_CRIT

                if (shld > 0 && !(target is Player && target.isInOlympiadMode))
                    _flags = _flags or HITFLAG_SHLD
            }
        }
    }

    fun createHit(target: WorldObject, damage: Int, miss: Boolean, crit: Boolean, shld: Byte): Hit {
        return Hit(target, damage, miss, crit, shld)
    }

    fun hit(vararg hits: Hit) {
        if (_hits == null) {
            _hits = hits as Array<Hit>?
            return
        }

        // this will only happen with pole attacks
        val tmp = arrayOfNulls<Hit>(hits.size + _hits!!.size)
        System.arraycopy(_hits, 0, tmp, 0, _hits!!.size)
        System.arraycopy(hits, 0, tmp, _hits!!.size, hits.size)
        _hits = tmp as Array<Hit>?
    }

    /**
     * @return True if the Server-Client packet Attack contains at least 1 hit.
     */
    fun hasHits(): Boolean {
        return _hits != null
    }

    override fun writeImpl() {
        writeC(0x05)

        writeD(_attackerObjId)
        writeD(_hits!![0]._targetId)
        writeD(_hits!![0]._damage)
        writeC(_hits!![0]._flags)
        writeD(_x)
        writeD(_y)
        writeD(_z)
        writeH(_hits!!.size - 1)
        // prevent sending useless packet while there is only one target.
        if (_hits!!.size > 1) {
            for (i in 1 until _hits!!.size) {
                writeD(_hits!![i]._targetId)
                writeD(_hits!![i]._damage)
                writeC(_hits!![i]._flags)
            }
        }
    }

    companion object {
        const val HITFLAG_USESS = 0x10
        const val HITFLAG_CRIT = 0x20
        const val HITFLAG_SHLD = 0x40
        const val HITFLAG_MISS = 0x80
    }
}