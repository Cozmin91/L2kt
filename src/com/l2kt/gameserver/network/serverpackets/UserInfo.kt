package com.l2kt.gameserver.network.serverpackets

import com.l2kt.Config
import com.l2kt.gameserver.data.manager.CursedWeaponManager
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.itemcontainer.Inventory
import com.l2kt.gameserver.skills.AbnormalEffect

class UserInfo(private val _activeChar: Player) : L2GameServerPacket() {
    private var _relation: Int = 0

    init {

        _relation = if (_activeChar.isClanLeader) 0x40 else 0

        if (_activeChar.siegeState.toInt() == 1)
            _relation = _relation or 0x180
        if (_activeChar.siegeState.toInt() == 2)
            _relation = _relation or 0x80
    }

    override fun writeImpl() {
        writeC(0x04)

        writeD(_activeChar.x)
        writeD(_activeChar.y)
        writeD(_activeChar.z)
        writeD(_activeChar.heading)
        writeD(_activeChar.objectId)

        writeS(if (_activeChar.polyTemplate != null) _activeChar.polyTemplate!!.name else _activeChar.name)

        writeD(_activeChar.race.ordinal)
        writeD(_activeChar.appearance.sex.ordinal)

        if (_activeChar.classIndex == 0)
            writeD(_activeChar.classId.id)
        else
            writeD(_activeChar.baseClass)

        writeD(_activeChar.level)
        writeQ(_activeChar.exp)
        writeD(_activeChar.str)
        writeD(_activeChar.dex)
        writeD(_activeChar.con)
        writeD(_activeChar.int)
        writeD(_activeChar.wit)
        writeD(_activeChar.men)
        writeD(_activeChar.maxHp)
        writeD(_activeChar.currentHp.toInt())
        writeD(_activeChar.maxMp)
        writeD(_activeChar.currentMp.toInt())
        writeD(_activeChar.sp)
        writeD(_activeChar.currentLoad)
        writeD(_activeChar.maxLoad)

        writeD(if (_activeChar.activeWeaponItem != null) 40 else 20) // 20 no weapon, 40 weapon equipped

        writeD(_activeChar.inventory.getPaperdollObjectId(Inventory.PAPERDOLL_HAIRALL))
        writeD(_activeChar.inventory.getPaperdollObjectId(Inventory.PAPERDOLL_REAR))
        writeD(_activeChar.inventory.getPaperdollObjectId(Inventory.PAPERDOLL_LEAR))
        writeD(_activeChar.inventory.getPaperdollObjectId(Inventory.PAPERDOLL_NECK))
        writeD(_activeChar.inventory.getPaperdollObjectId(Inventory.PAPERDOLL_RFINGER))
        writeD(_activeChar.inventory.getPaperdollObjectId(Inventory.PAPERDOLL_LFINGER))
        writeD(_activeChar.inventory.getPaperdollObjectId(Inventory.PAPERDOLL_HEAD))
        writeD(_activeChar.inventory.getPaperdollObjectId(Inventory.PAPERDOLL_RHAND))
        writeD(_activeChar.inventory.getPaperdollObjectId(Inventory.PAPERDOLL_LHAND))
        writeD(_activeChar.inventory.getPaperdollObjectId(Inventory.PAPERDOLL_GLOVES))
        writeD(_activeChar.inventory.getPaperdollObjectId(Inventory.PAPERDOLL_CHEST))
        writeD(_activeChar.inventory.getPaperdollObjectId(Inventory.PAPERDOLL_LEGS))
        writeD(_activeChar.inventory.getPaperdollObjectId(Inventory.PAPERDOLL_FEET))
        writeD(_activeChar.inventory.getPaperdollObjectId(Inventory.PAPERDOLL_BACK))
        writeD(_activeChar.inventory.getPaperdollObjectId(Inventory.PAPERDOLL_RHAND))
        writeD(_activeChar.inventory.getPaperdollObjectId(Inventory.PAPERDOLL_HAIR))
        writeD(_activeChar.inventory.getPaperdollObjectId(Inventory.PAPERDOLL_FACE))

        writeD(_activeChar.inventory.getPaperdollItemId(Inventory.PAPERDOLL_HAIRALL))
        writeD(_activeChar.inventory.getPaperdollItemId(Inventory.PAPERDOLL_REAR))
        writeD(_activeChar.inventory.getPaperdollItemId(Inventory.PAPERDOLL_LEAR))
        writeD(_activeChar.inventory.getPaperdollItemId(Inventory.PAPERDOLL_NECK))
        writeD(_activeChar.inventory.getPaperdollItemId(Inventory.PAPERDOLL_RFINGER))
        writeD(_activeChar.inventory.getPaperdollItemId(Inventory.PAPERDOLL_LFINGER))
        writeD(_activeChar.inventory.getPaperdollItemId(Inventory.PAPERDOLL_HEAD))
        writeD(_activeChar.inventory.getPaperdollItemId(Inventory.PAPERDOLL_RHAND))
        writeD(_activeChar.inventory.getPaperdollItemId(Inventory.PAPERDOLL_LHAND))
        writeD(_activeChar.inventory.getPaperdollItemId(Inventory.PAPERDOLL_GLOVES))
        writeD(_activeChar.inventory.getPaperdollItemId(Inventory.PAPERDOLL_CHEST))
        writeD(_activeChar.inventory.getPaperdollItemId(Inventory.PAPERDOLL_LEGS))
        writeD(_activeChar.inventory.getPaperdollItemId(Inventory.PAPERDOLL_FEET))
        writeD(_activeChar.inventory.getPaperdollItemId(Inventory.PAPERDOLL_BACK))
        writeD(_activeChar.inventory.getPaperdollItemId(Inventory.PAPERDOLL_RHAND))
        writeD(_activeChar.inventory.getPaperdollItemId(Inventory.PAPERDOLL_HAIR))
        writeD(_activeChar.inventory.getPaperdollItemId(Inventory.PAPERDOLL_FACE))

        // c6 new h's
        writeH(0x00)
        writeH(0x00)
        writeH(0x00)
        writeH(0x00)
        writeH(0x00)
        writeH(0x00)
        writeH(0x00)
        writeH(0x00)
        writeH(0x00)
        writeH(0x00)
        writeH(0x00)
        writeH(0x00)
        writeH(0x00)
        writeH(0x00)
        writeD(_activeChar.inventory.getPaperdollAugmentationId(Inventory.PAPERDOLL_RHAND))
        writeH(0x00)
        writeH(0x00)
        writeH(0x00)
        writeH(0x00)
        writeH(0x00)
        writeH(0x00)
        writeH(0x00)
        writeH(0x00)
        writeH(0x00)
        writeH(0x00)
        writeH(0x00)
        writeH(0x00)
        writeD(_activeChar.inventory.getPaperdollAugmentationId(Inventory.PAPERDOLL_LHAND))
        writeH(0x00)
        writeH(0x00)
        writeH(0x00)
        writeH(0x00)
        // end of c6 new h's

        writeD(_activeChar.getPAtk(null))
        writeD(_activeChar.pAtkSpd)
        writeD(_activeChar.getPDef(null))
        writeD(_activeChar.getEvasionRate(null))
        writeD(_activeChar.accuracy)
        writeD(_activeChar.getCriticalHit(null, null))
        writeD(_activeChar.getMAtk(null, null))

        writeD(_activeChar.mAtkSpd)
        writeD(_activeChar.pAtkSpd)

        writeD(_activeChar.getMDef(null, null))

        writeD(_activeChar.pvpFlag.toInt())
        writeD(_activeChar.karma)

        val _runSpd = _activeChar.stat.baseRunSpeed
        val _walkSpd = _activeChar.stat.baseWalkSpeed
        val _swimSpd = _activeChar.stat.baseSwimSpeed
        writeD(_runSpd) // base run speed
        writeD(_walkSpd) // base walk speed
        writeD(_swimSpd) // swim run speed
        writeD(_swimSpd) // swim walk speed
        writeD(0)
        writeD(0)
        writeD(if (_activeChar.isFlying) _runSpd else 0) // fly run speed
        writeD(if (_activeChar.isFlying) _walkSpd else 0) // fly walk speed
        writeF(_activeChar.stat.movementSpeedMultiplier.toDouble()) // run speed multiplier
        writeF(_activeChar.stat.attackSpeedMultiplier.toDouble()) // attack speed multiplier

        val pet = _activeChar.pet
        if (_activeChar.mountType != 0 && pet != null) {
            writeF(pet.collisionRadius)
            writeF(pet.collisionHeight)
        } else {
            writeF(_activeChar.collisionRadius)
            writeF(_activeChar.collisionHeight)
        }

        writeD(_activeChar.appearance.hairStyle.toInt())
        writeD(_activeChar.appearance.hairColor.toInt())
        writeD(_activeChar.appearance.face.toInt())
        writeD(if (_activeChar.isGM) 1 else 0) // builder level

        writeS(if (_activeChar.polyType != WorldObject.PolyType.DEFAULT) "Morphed" else _activeChar.title)

        writeD(_activeChar.clanId)
        writeD(_activeChar.clanCrestId)
        writeD(_activeChar.allyId)
        writeD(_activeChar.allyCrestId)
        // 0x40 leader rights
        // siege flags: attacker - 0x180 sword over name, defender - 0x80 shield, 0xC0 crown (|leader), 0x1C0 flag (|leader)
        writeD(_relation)
        writeC(_activeChar.mountType)
        writeC(_activeChar.storeType.id)
        writeC(if (_activeChar.hasDwarvenCraft()) 1 else 0)
        writeD(_activeChar.pkKills)
        writeD(_activeChar.pvpKills)

        writeH(_activeChar.cubics.size)
        for (id in _activeChar.cubics.keys)
            writeH(id)

        writeC(if (_activeChar.isInPartyMatchRoom) 1 else 0)

        if (_activeChar.appearance.invisible && _activeChar.isGM)
            writeD(_activeChar.abnormalEffect or AbnormalEffect.STEALTH.mask)
        else
            writeD(_activeChar.abnormalEffect)
        writeC(0x00)

        writeD(_activeChar.clanPrivileges)

        writeH(_activeChar.recomLeft)
        writeH(_activeChar.recomHave)
        writeD(if (_activeChar.mountNpcId > 0) _activeChar.mountNpcId + 1000000 else 0)
        writeH(_activeChar.inventoryLimit)

        writeD(_activeChar.classId.id)
        writeD(0x00) // special effects? circles around player...
        writeD(_activeChar.maxCp)
        writeD(_activeChar.currentCp.toInt())
        writeC(if (_activeChar.isMounted) 0 else _activeChar.enchantEffect)

        if (_activeChar.team == 1 || Config.PLAYER_SPAWN_PROTECTION > 0 && _activeChar.isSpawnProtected)
            writeC(0x01) // team circle around feet 1= Blue, 2 = red
        else if (_activeChar.team == 2)
            writeC(0x02) // team circle around feet 1= Blue, 2 = red
        else
            writeC(0x00) // team circle around feet 1= Blue, 2 = red

        writeD(_activeChar.clanCrestLargeId)
        writeC(if (_activeChar.isNoble) 1 else 0)
        writeC(if (_activeChar.isHero || _activeChar.isGM && Config.GM_HERO_AURA) 1 else 0)

        writeC(if (_activeChar.isFishing) 1 else 0)
        writeLoc(_activeChar.fishingStance.loc)

        writeD(_activeChar.appearance.nameColor)

        writeC(if (_activeChar.isRunning) 0x01 else 0x00) // changes the Speed display on Status Window

        writeD(_activeChar.pledgeClass)
        writeD(_activeChar.pledgeType)

        writeD(_activeChar.appearance.titleColor)

        if (_activeChar.isCursedWeaponEquipped)
            writeD(CursedWeaponManager.getCurrentStage(_activeChar.cursedWeaponEquippedId) - 1)
        else
            writeD(0x00)
    }
}