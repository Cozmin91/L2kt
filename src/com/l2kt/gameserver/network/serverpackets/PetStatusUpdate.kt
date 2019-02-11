package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.Summon
import com.l2kt.gameserver.model.actor.instance.Pet
import com.l2kt.gameserver.model.actor.instance.Servitor

class PetStatusUpdate(private val _summon: Summon) : L2GameServerPacket() {
    private val _maxHp: Int = _summon.maxHp
    private val _maxMp: Int = _summon.maxMp
    private var _maxFed: Int = 0
    private var _curFed: Int = 0

    init {
        if (_summon is Pet) {
            val pet = _summon
            _curFed = pet.currentFed
            _maxFed = pet.petData.maxMeal
        } else if (_summon is Servitor) {
            val sum = _summon
            _curFed = sum.timeRemaining
            _maxFed = sum.totalLifeTime
        }
    }

    override fun writeImpl() {
        writeC(0xb5)
        writeD(_summon.summonType)
        writeD(_summon.objectId)
        writeD(_summon.x)
        writeD(_summon.y)
        writeD(_summon.z)
        writeS(_summon.title)
        writeD(_curFed)
        writeD(_maxFed)
        writeD(_summon.currentHp.toInt())
        writeD(_maxHp)
        writeD(_summon.currentMp.toInt())
        writeD(_maxMp)
        writeD(_summon.level)
        writeQ(_summon.stat.exp)
        writeQ(_summon.expForThisLevel)// 0% absolute value
        writeQ(_summon.expForNextLevel)// 100% absolute value
    }
}