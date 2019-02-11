package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.itemcontainer.Inventory

class GMViewCharacterInfo(private val _activeChar: Player) : L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0x8f)

        writeD(_activeChar.x)
        writeD(_activeChar.y)
        writeD(_activeChar.z)
        writeD(_activeChar.heading)
        writeD(_activeChar.objectId)
        writeS(_activeChar.name)
        writeD(_activeChar.race.ordinal)
        writeD(_activeChar.appearance.sex.ordinal)
        writeD(_activeChar.classId.id)
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
        writeD(0x28) // unknown

        writeD(_activeChar.inventory!!.getPaperdollObjectId(Inventory.PAPERDOLL_HAIRALL))
        writeD(_activeChar.inventory!!.getPaperdollObjectId(Inventory.PAPERDOLL_REAR))
        writeD(_activeChar.inventory!!.getPaperdollObjectId(Inventory.PAPERDOLL_LEAR))
        writeD(_activeChar.inventory!!.getPaperdollObjectId(Inventory.PAPERDOLL_NECK))
        writeD(_activeChar.inventory!!.getPaperdollObjectId(Inventory.PAPERDOLL_RFINGER))
        writeD(_activeChar.inventory!!.getPaperdollObjectId(Inventory.PAPERDOLL_LFINGER))
        writeD(_activeChar.inventory!!.getPaperdollObjectId(Inventory.PAPERDOLL_HEAD))
        writeD(_activeChar.inventory!!.getPaperdollObjectId(Inventory.PAPERDOLL_RHAND))
        writeD(_activeChar.inventory!!.getPaperdollObjectId(Inventory.PAPERDOLL_LHAND))
        writeD(_activeChar.inventory!!.getPaperdollObjectId(Inventory.PAPERDOLL_GLOVES))
        writeD(_activeChar.inventory!!.getPaperdollObjectId(Inventory.PAPERDOLL_CHEST))
        writeD(_activeChar.inventory!!.getPaperdollObjectId(Inventory.PAPERDOLL_LEGS))
        writeD(_activeChar.inventory!!.getPaperdollObjectId(Inventory.PAPERDOLL_FEET))
        writeD(_activeChar.inventory!!.getPaperdollObjectId(Inventory.PAPERDOLL_BACK))
        writeD(_activeChar.inventory!!.getPaperdollObjectId(Inventory.PAPERDOLL_RHAND))
        writeD(_activeChar.inventory!!.getPaperdollObjectId(Inventory.PAPERDOLL_HAIR))
        writeD(_activeChar.inventory!!.getPaperdollObjectId(Inventory.PAPERDOLL_FACE))

        writeD(_activeChar.inventory!!.getPaperdollItemId(Inventory.PAPERDOLL_HAIRALL))
        writeD(_activeChar.inventory!!.getPaperdollItemId(Inventory.PAPERDOLL_REAR))
        writeD(_activeChar.inventory!!.getPaperdollItemId(Inventory.PAPERDOLL_LEAR))
        writeD(_activeChar.inventory!!.getPaperdollItemId(Inventory.PAPERDOLL_NECK))
        writeD(_activeChar.inventory!!.getPaperdollItemId(Inventory.PAPERDOLL_RFINGER))
        writeD(_activeChar.inventory!!.getPaperdollItemId(Inventory.PAPERDOLL_LFINGER))
        writeD(_activeChar.inventory!!.getPaperdollItemId(Inventory.PAPERDOLL_HEAD))
        writeD(_activeChar.inventory!!.getPaperdollItemId(Inventory.PAPERDOLL_RHAND))
        writeD(_activeChar.inventory!!.getPaperdollItemId(Inventory.PAPERDOLL_LHAND))
        writeD(_activeChar.inventory!!.getPaperdollItemId(Inventory.PAPERDOLL_GLOVES))
        writeD(_activeChar.inventory!!.getPaperdollItemId(Inventory.PAPERDOLL_CHEST))
        writeD(_activeChar.inventory!!.getPaperdollItemId(Inventory.PAPERDOLL_LEGS))
        writeD(_activeChar.inventory!!.getPaperdollItemId(Inventory.PAPERDOLL_FEET))
        writeD(_activeChar.inventory!!.getPaperdollItemId(Inventory.PAPERDOLL_BACK))
        writeD(_activeChar.inventory!!.getPaperdollItemId(Inventory.PAPERDOLL_RHAND))
        writeD(_activeChar.inventory!!.getPaperdollItemId(Inventory.PAPERDOLL_HAIR))
        writeD(_activeChar.inventory!!.getPaperdollItemId(Inventory.PAPERDOLL_FACE))

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
        writeH(0x00)
        writeH(0x00)
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

        writeD(_activeChar.pvpFlag.toInt()) // 0-non-pvp 1-pvp = violett name
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

        writeF(_activeChar.collisionRadius) // scale
        writeF(_activeChar.collisionHeight) // y offset ??!? fem dwarf 4033
        writeD(_activeChar.appearance.hairStyle.toInt())
        writeD(_activeChar.appearance.hairColor.toInt())
        writeD(_activeChar.appearance.face.toInt())
        writeD(if (_activeChar.isGM) 0x01 else 0x00) // builder level

        writeS(_activeChar.title)
        writeD(_activeChar.clanId) // pledge id
        writeD(_activeChar.clanCrestId) // pledge crest id
        writeD(_activeChar.allyId) // ally id
        writeC(_activeChar.mountType) // mount type
        writeC(_activeChar.storeType.id)
        writeC(if (_activeChar.hasDwarvenCraft()) 1 else 0)
        writeD(_activeChar.pkKills)
        writeD(_activeChar.pvpKills)

        writeH(_activeChar.recomLeft)
        writeH(_activeChar.recomHave) // Blue value for name (0 = white, 255 = pure blue)
        writeD(_activeChar.classId.id)
        writeD(0x00) // special effects? circles around player...
        writeD(_activeChar.maxCp)
        writeD(_activeChar.currentCp.toInt())

        writeC(if (_activeChar.isRunning) 0x01 else 0x00) // changes the Speed display on Status Window

        writeC(321)

        writeD(_activeChar.pledgeClass) // changes the text above CP on Status Window

        writeC(if (_activeChar.isNoble) 0x01 else 0x00)
        writeC(if (_activeChar.isHero) 0x01 else 0x00)

        writeD(_activeChar.appearance.nameColor)
        writeD(_activeChar.appearance.titleColor)
    }
}