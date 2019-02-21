package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.Summon
import com.l2kt.gameserver.model.actor.instance.Pet
import com.l2kt.gameserver.model.actor.instance.Servitor

class PetInfo(private val _summon: Summon, private val _val: Int) : L2GameServerPacket() {
    private var _maxFed: Int = 0
    private var _curFed: Int = 0

    init {

        if (_summon is Pet) {
            val pet = _summon
            _curFed = pet.currentFed
            _maxFed = pet.petData!!.maxMeal
        } else if (_summon is Servitor) {
            val sum = _summon
            _curFed = sum.timeRemaining
            _maxFed = sum.totalLifeTime
        }
    }

    override fun writeImpl() {
        writeC(0xb1)
        writeD(_summon.summonType)
        writeD(_summon.objectId)
        writeD(_summon.template.idTemplate + 1000000)
        writeD(0) // 1=attackable

        writeD(_summon.x)
        writeD(_summon.y)
        writeD(_summon.z)
        writeD(_summon.heading)
        writeD(0)
        writeD(_summon.mAtkSpd)
        writeD(_summon.pAtkSpd)

        val _runSpd = _summon.stat.baseRunSpeed
        val _walkSpd = _summon.stat.baseWalkSpeed
        writeD(_runSpd) // base run speed
        writeD(_walkSpd) // base walk speed
        writeD(_runSpd) // swim run speed
        writeD(_walkSpd) // swim walk speed
        writeD(_runSpd)
        writeD(_walkSpd)
        writeD(_runSpd) // fly run speed
        writeD(_walkSpd) // fly walk speed

        writeF(_summon.stat.movementSpeedMultiplier.toDouble()) // movement multiplier
        writeF(1.0) // attack speed multiplier
        writeF(_summon.collisionRadius)
        writeF(_summon.collisionHeight)
        writeD(_summon.weapon) // right hand weapon
        writeD(_summon.armor) // body armor
        writeD(0) // left hand weapon
        writeC(if (_summon.owner != null) 1 else 0) // when pet is dead and player exit game, pet doesn't show master name
        writeC(1) // isRunning (it is always 1, walking mode is calculated from multiplier)
        writeC(if (_summon.isInCombat) 1 else 0) // attacking 1=true
        writeC(if (_summon.isAlikeDead) 1 else 0) // dead 1=true
        writeC(if (_summon.isShowSummonAnimation) 2 else _val) // 0=teleported 1=default 2=summoned
        writeS(_summon.name)
        writeS(_summon.title)
        writeD(1)
        writeD((if (_summon.owner != null) _summon.owner.pvpFlag else 0).toInt()) // 0 = white,2= purple
        writeD(if (_summon.owner != null) _summon.owner.karma else 0) // karma
        writeD(_curFed) // how fed it is
        writeD(_maxFed) // max fed it can be
        writeD(_summon.currentHp.toInt()) // current hp
        writeD(_summon.maxHp) // max hp
        writeD(_summon.currentMp.toInt()) // current mp
        writeD(_summon.maxMp) // max mp
        writeD(_summon.stat.sp) // sp
        writeD(_summon.level) // lvl
        writeQ(_summon.stat.exp)
        writeQ(_summon.expForThisLevel) // 0% absolute value
        writeQ(_summon.expForNextLevel) // 100% absoulte value
        writeD(if (_summon is Pet) _summon.getInventory()!!.totalWeight else 0) // weight
        writeD(_summon.maxLoad) // max weight it can carry
        writeD(_summon.getPAtk(null)) // patk
        writeD(_summon.getPDef(null)) // pdef
        writeD(_summon.getMAtk(null, null)) // matk
        writeD(_summon.getMDef(null, null)) // mdef
        writeD(_summon.accuracy) // accuracy
        writeD(_summon.getEvasionRate(null)) // evasion
        writeD(_summon.getCriticalHit(null, null)) // critical
        writeD(_summon.moveSpeed) // speed
        writeD(_summon.pAtkSpd) // atkspeed
        writeD(_summon.mAtkSpd) // casting speed

        writeD(_summon.abnormalEffect) // abnormal visual effect
        writeH(if (_summon.isMountable) 1 else 0) // ride button
        writeC(0) // c2

        writeH(0) // ??
        writeC(if (_summon.owner != null) _summon.owner.team else 0) // team aura (1 = blue, 2 = red)
        writeD(_summon.soulShotsPerHit) // How many soulshots this servitor uses per hit
        writeD(_summon.spiritShotsPerHit) // How many spiritshots this servitor uses per hit
    }
}