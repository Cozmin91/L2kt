package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.template.NpcTemplate

class ServerObjectInfo(private val _npc: Npc, actor: Creature) : L2GameServerPacket() {

    private val _idTemplate: Int = (_npc.template as NpcTemplate).idTemplate
    private val _name: String = _npc.name

    private val _x: Int = _npc.x
    private val _y: Int = _npc.y
    private val _z: Int = _npc.z
    private val _heading: Int = _npc.heading

    private val _collisionHeight: Double = _npc.collisionHeight
    private val _collisionRadius: Double = _npc.collisionRadius

    private val _isAttackable: Boolean = _npc.isAutoAttackable(actor)

    override fun writeImpl() {
        writeC(0x8C)
        writeD(_npc.objectId)
        writeD(_idTemplate + 1000000)
        writeS(_name)
        writeD(if (_isAttackable) 1 else 0)
        writeD(_x)
        writeD(_y)
        writeD(_z)
        writeD(_heading)
        writeF(1.0) // movement multiplier
        writeF(1.0) // attack speed multiplier
        writeF(_collisionRadius)
        writeF(_collisionHeight)
        writeD((if (_isAttackable) _npc.currentHp.toInt() else 0).toInt())
        writeD(if (_isAttackable) _npc.maxHp else 0)
        writeD(0x01) // object type
        writeD(0x00) // special effects
    }
}