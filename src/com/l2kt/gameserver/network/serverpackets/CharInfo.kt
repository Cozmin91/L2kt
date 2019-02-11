package com.l2kt.gameserver.network.serverpackets

import com.l2kt.Config
import com.l2kt.gameserver.data.manager.CursedWeaponManager
import com.l2kt.gameserver.data.xml.NpcData
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.itemcontainer.Inventory
import com.l2kt.gameserver.skills.AbnormalEffect

class CharInfo(private val _activeChar: Player) : L2GameServerPacket() {
    private val _inv = _activeChar.inventory

    override fun writeImpl() {
        var gmSeeInvis = false

        if (_activeChar.appearance.invisible) {
            val tmp = client.activeChar
            if (tmp != null && tmp.isGM)
                gmSeeInvis = true
        }

        writeC(0x03)
        writeD(_activeChar.x)
        writeD(_activeChar.y)
        writeD(_activeChar.z)
        writeD(_activeChar.heading)
        writeD(_activeChar.objectId)
        writeS(_activeChar.name)
        writeD(_activeChar.race.ordinal)
        writeD(_activeChar.appearance.sex.ordinal)

        if (_activeChar.classIndex == 0)
            writeD(_activeChar.classId.id)
        else
            writeD(_activeChar.baseClass)

        writeD(_inv!!.getPaperdollItemId(Inventory.PAPERDOLL_HAIRALL))
        writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_HEAD))
        writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_RHAND))
        writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_LHAND))
        writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_GLOVES))
        writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_CHEST))
        writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_LEGS))
        writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_FEET))
        writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_BACK))
        writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_RHAND))
        writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_HAIR))
        writeD(_inv.getPaperdollItemId(Inventory.PAPERDOLL_FACE))

        // c6 new h's
        writeH(0x00)
        writeH(0x00)
        writeH(0x00)
        writeH(0x00)
        writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_RHAND))
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
        writeD(_inv.getPaperdollAugmentationId(Inventory.PAPERDOLL_LHAND))
        writeH(0x00)
        writeH(0x00)
        writeH(0x00)
        writeH(0x00)

        writeD(_activeChar.pvpFlag.toInt())
        writeD(_activeChar.karma)

        writeD(_activeChar.mAtkSpd)
        writeD(_activeChar.pAtkSpd)

        writeD(_activeChar.pvpFlag.toInt())
        writeD(_activeChar.karma)

        val _runSpd = _activeChar.stat.baseRunSpeed
        val _walkSpd = _activeChar.stat.baseWalkSpeed
        val _swimSpd = _activeChar.stat.baseSwimSpeed
        writeD(_runSpd) // base run speed
        writeD(_walkSpd) // base walk speed
        writeD(_swimSpd) // swim run speed
        writeD(_swimSpd) // swim walk speed
        writeD(_runSpd)
        writeD(_walkSpd)
        writeD(if (_activeChar.isFlying) _runSpd else 0) // fly run speed
        writeD(if (_activeChar.isFlying) _walkSpd else 0) // fly walk speed
        writeF(_activeChar.stat.movementSpeedMultiplier.toDouble()) // run speed multiplier
        writeF(_activeChar.stat.attackSpeedMultiplier.toDouble()) // attack speed multiplier

        if (_activeChar.mountType != 0) {
            writeF(NpcData.getInstance().getTemplate(_activeChar.mountNpcId).collisionRadius)
            writeF(NpcData.getInstance().getTemplate(_activeChar.mountNpcId).collisionHeight)
        } else {
            writeF(_activeChar.collisionRadius)
            writeF(_activeChar.collisionHeight)
        }

        writeD(_activeChar.appearance.hairStyle.toInt())
        writeD(_activeChar.appearance.hairColor.toInt())
        writeD(_activeChar.appearance.face.toInt())

        if (gmSeeInvis)
            writeS("Invisible")
        else
            writeS(_activeChar.title)

        writeD(_activeChar.clanId)
        writeD(_activeChar.clanCrestId)
        writeD(_activeChar.allyId)
        writeD(_activeChar.allyCrestId)

        writeD(0)

        writeC(if (_activeChar.isSitting) 0 else 1) // standing = 1 sitting = 0
        writeC(if (_activeChar.isRunning) 1 else 0) // running = 1 walking = 0
        writeC(if (_activeChar.isInCombat) 1 else 0)
        writeC(if (_activeChar.isAlikeDead) 1 else 0)

        if (gmSeeInvis)
            writeC(0)
        else
            writeC(if (_activeChar.appearance.invisible) 1 else 0)

        writeC(_activeChar.mountType)
        writeC(_activeChar.storeType.id)

        writeH(_activeChar.cubics.size)
        for (id in _activeChar.cubics.keys)
            writeH(id)

        writeC(if (_activeChar.isInPartyMatchRoom) 1 else 0)

        if (gmSeeInvis)
            writeD(_activeChar.abnormalEffect or AbnormalEffect.STEALTH.mask)
        else
            writeD(_activeChar.abnormalEffect)

        writeC(_activeChar.recomLeft)
        writeH(_activeChar.recomHave)
        writeD(_activeChar.classId.id)

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

        writeD(0x00) // isRunning() as in UserInfo?

        writeD(_activeChar.pledgeClass)
        writeD(_activeChar.pledgeType)

        writeD(_activeChar.appearance.titleColor)

        if (_activeChar.isCursedWeaponEquipped)
            writeD(CursedWeaponManager.getInstance().getCurrentStage(_activeChar.cursedWeaponEquippedId) - 1)
        else
            writeD(0x00)
    }
}