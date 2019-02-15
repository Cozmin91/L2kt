package com.l2kt.gameserver.network.serverpackets

import com.l2kt.Config
import com.l2kt.gameserver.data.sql.ClanTable
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.Summon
import com.l2kt.gameserver.model.actor.instance.Monster
import com.l2kt.gameserver.model.actor.instance.Pet
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.actor.template.NpcTemplate

abstract class AbstractNpcInfo(cha: Creature) : L2GameServerPacket() {
    protected var x: Int = 0
    protected var y: Int = 0
    protected var z: Int = 0
    protected var _heading: Int = 0
    protected var _idTemplate: Int = 0
    protected var _isAttackable: Boolean = false
    protected var _isSummoned: Boolean = false
    protected var _mAtkSpd: Int = 0
    protected var _pAtkSpd: Int = 0
    protected var _runSpd: Int = 0
    protected var _walkSpd: Int = 0
    protected var _rhand: Int = 0
    protected var _lhand: Int = 0
    protected var _chest: Int = 0
    protected var _enchantEffect: Int = 0
    protected var _collisionHeight: Double = 0.toDouble()
    protected var _collisionRadius: Double = 0.toDouble()
    protected var _clanCrest: Int = 0
    protected var _allyCrest: Int = 0
    protected var allyId: Int = 0
    protected var _clanId: Int = 0

    protected var name = ""
    protected var title = ""

    init {
        _isSummoned = cha.isShowSummonAnimation
        x = cha.x
        y = cha.y
        z = cha.z
        _heading = cha.heading
        _mAtkSpd = cha.mAtkSpd
        _pAtkSpd = cha.pAtkSpd
        _runSpd = cha.stat.baseRunSpeed
        _walkSpd = cha.stat.baseWalkSpeed
    }

    /**
     * Packet for Npcs
     */
    class NpcInfo(private val _npc: Npc, attacker: Creature) : AbstractNpcInfo(_npc) {

        init {

            _enchantEffect = _npc.enchantEffect
            _isAttackable = _npc.isAutoAttackable(attacker)

            // Support for polymorph.
            if (_npc.polyType == WorldObject.PolyType.NPC) {
                _idTemplate = _npc.polyTemplate.idTemplate
                _rhand = _npc.polyTemplate.rightHand
                _lhand = _npc.polyTemplate.leftHand
                _collisionHeight = _npc.polyTemplate.collisionHeight
                _collisionRadius = _npc.polyTemplate.collisionRadius
            } else {
                _idTemplate = _npc.template.idTemplate
                _rhand = _npc.rightHandItem
                _lhand = _npc.leftHandItem
                _collisionHeight = _npc.collisionHeight
                _collisionRadius = _npc.collisionRadius
            }

            if (_npc.template.isUsingServerSideName)
                name = _npc.name

            if (_npc.isChampion)
                title = "Champion"
            else if (_npc.template.isUsingServerSideTitle)
                title = _npc.title

            if (Config.SHOW_NPC_LVL && _npc is Monster)
                title = "Lv " + _npc.getLevel() + (if (_npc.getTemplate().aggroRange > 0) "* " else " ") + title

            // NPC crest system
            if (Config.SHOW_NPC_CREST && _npc.castle != null && _npc.castle.ownerId != 0) {
                val clan = ClanTable.getClan(_npc.castle.ownerId)!!
                _clanCrest = clan.crestId
                _clanId = clan.clanId
                _allyCrest = clan.allyCrestId
                allyId = clan.allyId
            }
        }

        override fun writeImpl() {
            writeC(0x16)

            writeD(_npc.objectId)
            writeD(_idTemplate + 1000000)
            writeD(if (_isAttackable) 1 else 0)

            writeD(x)
            writeD(y)
            writeD(z)
            writeD(_heading)

            writeD(0x00)

            writeD(_mAtkSpd)
            writeD(_pAtkSpd)
            writeD(_runSpd)
            writeD(_walkSpd)
            writeD(_runSpd)
            writeD(_walkSpd)
            writeD(_runSpd)
            writeD(_walkSpd)
            writeD(_runSpd)
            writeD(_walkSpd)

            writeF(_npc.stat.movementSpeedMultiplier.toDouble())
            writeF(_npc.stat.attackSpeedMultiplier.toDouble())

            writeF(_collisionRadius)
            writeF(_collisionHeight)

            writeD(_rhand)
            writeD(_chest)
            writeD(_lhand)

            writeC(1) // name above char
            writeC(if (_npc.isRunning) 1 else 0)
            writeC(if (_npc.isInCombat) 1 else 0)
            writeC(if (_npc.isAlikeDead) 1 else 0)
            writeC(if (_isSummoned) 2 else 0)

            writeS(name)
            writeS(title)

            writeD(0x00)
            writeD(0x00)
            writeD(0x00)

            writeD(_npc.abnormalEffect)

            writeD(_clanId)
            writeD(_clanCrest)
            writeD(allyId)
            writeD(_allyCrest)

            writeC(if (_npc.isFlying) 2 else 0)
            writeC(0x00)

            writeF(_collisionRadius)
            writeF(_collisionHeight)

            writeD(_enchantEffect)
            writeD(if (_npc.isFlying) 1 else 0)
        }
    }

    /**
     * Packet for summons
     */
    class SummonInfo(private val _summon: Summon, attacker: Player, `val`: Int) : AbstractNpcInfo(_summon) {
        private val _owner: Player? = _summon.owner
        private var _summonAnimation = 0

        init {

            _summonAnimation = `val`
            if (_summon.isShowSummonAnimation)
                _summonAnimation = 2 // override for spawn

            _isAttackable = _summon.isAutoAttackable(attacker)
            _rhand = _summon.weapon
            _lhand = 0
            _chest = _summon.armor
            _enchantEffect = _summon.template.enchantEffect
            title = if (_owner == null || !_owner.isOnline) "" else _owner.name
            _idTemplate = _summon.template.idTemplate

            _collisionHeight = _summon.collisionHeight
            _collisionRadius = _summon.collisionRadius

            // NPC crest system
            if (Config.SHOW_SUMMON_CREST && _owner != null && _owner.clan != null) {
                val clan = ClanTable.getClan(_owner.clanId)!!
                _clanCrest = clan.crestId
                _clanId = clan.clanId
                _allyCrest = clan.allyCrestId
                allyId = clan.allyId
            }
        }

        override fun writeImpl() {
            if (_owner != null && _owner.appearance.invisible)
                return

            writeC(0x16)

            writeD(_summon.objectId)
            writeD(_idTemplate + 1000000)
            writeD(if (_isAttackable) 1 else 0)

            writeD(x)
            writeD(y)
            writeD(z)
            writeD(_heading)

            writeD(0x00)

            writeD(_mAtkSpd)
            writeD(_pAtkSpd)
            writeD(_runSpd)
            writeD(_walkSpd)
            writeD(_runSpd)
            writeD(_walkSpd)
            writeD(_runSpd)
            writeD(_walkSpd)
            writeD(_runSpd)
            writeD(_walkSpd)

            writeF(_summon.stat.movementSpeedMultiplier.toDouble())
            writeF(_summon.stat.attackSpeedMultiplier.toDouble())

            writeF(_collisionRadius)
            writeF(_collisionHeight)

            writeD(_rhand)
            writeD(_chest)
            writeD(_lhand)

            writeC(1) // name above char
            writeC(if (_summon.isRunning) 1 else 0)
            writeC(if (_summon.isInCombat) 1 else 0)
            writeC(if (_summon.isAlikeDead) 1 else 0)
            writeC(_summonAnimation)

            writeS(name)
            writeS(title)

            writeD(if (_summon is Pet) 0x00 else 0x01)
            writeD(_summon.pvpFlag.toInt())
            writeD(_summon.karma)

            writeD(_summon.abnormalEffect)

            writeD(_clanId)
            writeD(_clanCrest)
            writeD(allyId)
            writeD(_allyCrest)

            writeC(0x00)
            writeC(_summon.team)

            writeF(_collisionRadius)
            writeF(_collisionHeight)

            writeD(_enchantEffect)
            writeD(0x00)
        }
    }

    /**
     * Packet for morphed PCs
     */
    class PcMorphInfo(private val _pc: Player, private val _template: NpcTemplate) : AbstractNpcInfo(_pc) {
        private val _swimSpd: Int = _pc.stat.baseSwimSpeed

        init {
            _rhand = _template.rightHand
            _lhand = _template.leftHand

            _collisionHeight = _template.collisionHeight
            _collisionRadius = _template.collisionRadius

            _enchantEffect = _template.enchantEffect
        }

        override fun writeImpl() {
            writeC(0x16)

            writeD(_pc.objectId)
            writeD(_pc.polyId + 1000000)
            writeD(1)

            writeD(x)
            writeD(y)
            writeD(z)
            writeD(_heading)

            writeD(0x00)

            writeD(_mAtkSpd)
            writeD(_pAtkSpd)
            writeD(_runSpd)
            writeD(_walkSpd)
            writeD(_swimSpd)
            writeD(_swimSpd)
            writeD(_runSpd)
            writeD(_walkSpd)
            writeD(_runSpd)
            writeD(_walkSpd)

            writeF(_pc.stat.movementSpeedMultiplier.toDouble())
            writeF(_pc.stat.attackSpeedMultiplier.toDouble())

            writeF(_collisionRadius)
            writeF(_collisionHeight)

            writeD(_rhand)
            writeD(0)
            writeD(_lhand)

            writeC(1) // name above char
            writeC(if (_pc.isRunning) 1 else 0)
            writeC(if (_pc.isInCombat) 1 else 0)
            writeC(if (_pc.isAlikeDead) 1 else 0)
            writeC(0) // 0 = teleported, 1 = default, 2 = summoned

            writeS(name)
            writeS(title)

            writeD(0x00)
            writeD(0x00)
            writeD(0x00)

            writeD(_pc.abnormalEffect)

            writeD(0x00)
            writeD(0x00)
            writeD(0x00)
            writeD(0x00)

            writeC(0x00)
            writeC(0x00)

            writeF(_collisionRadius)
            writeF(_collisionHeight)

            writeD(_enchantEffect)
            writeD(0x00)
        }
    }
}