package com.l2kt.gameserver.network.serverpackets

import com.l2kt.L2DatabaseFactory
import com.l2kt.gameserver.data.sql.ClanTable
import com.l2kt.gameserver.model.CharSelectSlot
import com.l2kt.gameserver.model.itemcontainer.Inventory
import com.l2kt.gameserver.network.L2GameClient
import java.util.*

class CharSelectInfo : L2GameServerPacket {

    val characterSlots: Array<CharSelectSlot>
    private val _loginName: String
    private val _sessionId: Int

    private var _activeId: Int = 0

    constructor(loginName: String, sessionId: Int) {
        characterSlots = loadCharSelectSlots(loginName)
        _sessionId = sessionId
        _loginName = loginName

        _activeId = -1
    }

    constructor(loginName: String, sessionId: Int, activeId: Int) {
        characterSlots = loadCharSelectSlots(loginName)
        _sessionId = sessionId
        _loginName = loginName

        _activeId = activeId
    }

    override fun writeImpl() {
        val size = characterSlots.size

        writeC(0x13)
        writeD(size)

        var lastAccess = 0L

        if (_activeId == -1) {
            for (i in 0 until size)
                if (lastAccess < characterSlots[i].lastAccess) {
                    lastAccess = characterSlots[i].lastAccess
                    _activeId = i
                }
        }

        for (i in 0 until size) {
            val slot = characterSlots[i]

            writeS(slot.name)
            writeD(slot.charId)
            writeS(_loginName)
            writeD(_sessionId)
            writeD(slot.clanId)
            writeD(0x00) // Builder level

            writeD(slot.sex)
            writeD(slot.race)
            writeD(slot.baseClassId)

            writeD(0x01) // active ??

            writeD(slot.x)
            writeD(slot.y)
            writeD(slot.z)

            writeF(slot.currentHp)
            writeF(slot.currentMp)

            writeD(slot.sp)
            writeQ(slot.exp)
            writeD(slot.level)

            writeD(slot.karma)
            writeD(slot.pkKills)
            writeD(slot.pvPKills)

            writeD(0x00)
            writeD(0x00)
            writeD(0x00)
            writeD(0x00)
            writeD(0x00)
            writeD(0x00)
            writeD(0x00)

            writeD(slot.getPaperdollObjectId(Inventory.PAPERDOLL_HAIRALL))
            writeD(slot.getPaperdollObjectId(Inventory.PAPERDOLL_REAR))
            writeD(slot.getPaperdollObjectId(Inventory.PAPERDOLL_LEAR))
            writeD(slot.getPaperdollObjectId(Inventory.PAPERDOLL_NECK))
            writeD(slot.getPaperdollObjectId(Inventory.PAPERDOLL_RFINGER))
            writeD(slot.getPaperdollObjectId(Inventory.PAPERDOLL_LFINGER))
            writeD(slot.getPaperdollObjectId(Inventory.PAPERDOLL_HEAD))
            writeD(slot.getPaperdollObjectId(Inventory.PAPERDOLL_RHAND))
            writeD(slot.getPaperdollObjectId(Inventory.PAPERDOLL_LHAND))
            writeD(slot.getPaperdollObjectId(Inventory.PAPERDOLL_GLOVES))
            writeD(slot.getPaperdollObjectId(Inventory.PAPERDOLL_CHEST))
            writeD(slot.getPaperdollObjectId(Inventory.PAPERDOLL_LEGS))
            writeD(slot.getPaperdollObjectId(Inventory.PAPERDOLL_FEET))
            writeD(slot.getPaperdollObjectId(Inventory.PAPERDOLL_BACK))
            writeD(slot.getPaperdollObjectId(Inventory.PAPERDOLL_RHAND))
            writeD(slot.getPaperdollObjectId(Inventory.PAPERDOLL_HAIR))
            writeD(slot.getPaperdollObjectId(Inventory.PAPERDOLL_FACE))

            writeD(slot.getPaperdollItemId(Inventory.PAPERDOLL_HAIRALL))
            writeD(slot.getPaperdollItemId(Inventory.PAPERDOLL_REAR))
            writeD(slot.getPaperdollItemId(Inventory.PAPERDOLL_LEAR))
            writeD(slot.getPaperdollItemId(Inventory.PAPERDOLL_NECK))
            writeD(slot.getPaperdollItemId(Inventory.PAPERDOLL_RFINGER))
            writeD(slot.getPaperdollItemId(Inventory.PAPERDOLL_LFINGER))
            writeD(slot.getPaperdollItemId(Inventory.PAPERDOLL_HEAD))
            writeD(slot.getPaperdollItemId(Inventory.PAPERDOLL_RHAND))
            writeD(slot.getPaperdollItemId(Inventory.PAPERDOLL_LHAND))
            writeD(slot.getPaperdollItemId(Inventory.PAPERDOLL_GLOVES))
            writeD(slot.getPaperdollItemId(Inventory.PAPERDOLL_CHEST))
            writeD(slot.getPaperdollItemId(Inventory.PAPERDOLL_LEGS))
            writeD(slot.getPaperdollItemId(Inventory.PAPERDOLL_FEET))
            writeD(slot.getPaperdollItemId(Inventory.PAPERDOLL_BACK))
            writeD(slot.getPaperdollItemId(Inventory.PAPERDOLL_RHAND))
            writeD(slot.getPaperdollItemId(Inventory.PAPERDOLL_HAIR))
            writeD(slot.getPaperdollItemId(Inventory.PAPERDOLL_FACE))

            writeD(slot.hairStyle)
            writeD(slot.hairColor)
            writeD(slot.face)

            writeF(slot.maxHp.toDouble())
            writeF(slot.maxMp.toDouble())

            writeD(if (slot.accessLevel > -1) if (slot.deleteTimer > 0) ((slot.deleteTimer - System.currentTimeMillis()) / 1000).toInt() else 0 else -1)
            writeD(slot.classId)
            writeD(if (i == _activeId) 0x01 else 0x00)
            writeC(Math.min(127, slot.enchantEffect))
            writeD(slot.augmentationId)
        }
        client.setCharSelectSlot(characterSlots)
    }

    companion object {
        private val SELECT_INFOS =
            "SELECT obj_Id, char_name, level, maxHp, curHp, maxMp, curMp, face, hairStyle, hairColor, sex, heading, x, y, z, exp, sp, karma, pvpkills, pkkills, clanid, race, classid, deletetime, cancraft, title, accesslevel, online, lastAccess, base_class FROM characters WHERE account_name=?"
        private val SELECT_CURRENT_SUBCLASS =
            "SELECT exp, sp, level FROM character_subclasses WHERE char_obj_id=? && class_id=? ORDER BY char_obj_id"
        private val SELECT_AUGMENTS = "SELECT attributes FROM augmentations WHERE item_id=?"

        private fun loadCharSelectSlots(loginName: String): Array<CharSelectSlot> {
            val list = ArrayList<CharSelectSlot>()

            try {
                L2DatabaseFactory.connection.use { con ->
                    val ps = con.prepareStatement(SELECT_INFOS)
                    ps.setString(1, loginName)

                    val rset = ps.executeQuery()
                    while (rset.next()) {
                        val objectId = rset.getInt("obj_id")
                        val name = rset.getString("char_name")

                        // See if the char must be deleted
                        val deleteTime = rset.getLong("deletetime")
                        if (deleteTime > 0) {
                            if (System.currentTimeMillis() > deleteTime) {
                                val clan = ClanTable.getClan(rset.getInt("clanid"))
                                clan?.removeClanMember(objectId, 0)

                                L2GameClient.deleteCharByObjId(objectId)
                                continue
                            }
                        }

                        val slot = CharSelectSlot(objectId, name)
                        slot.accessLevel = rset.getInt("accesslevel")
                        slot.level = rset.getInt("level")
                        slot.maxHp = rset.getInt("maxhp")
                        slot.currentHp = rset.getDouble("curhp")
                        slot.maxMp = rset.getInt("maxmp")
                        slot.currentMp = rset.getDouble("curmp")
                        slot.karma = rset.getInt("karma")
                        slot.pkKills = rset.getInt("pkkills")
                        slot.pvPKills = rset.getInt("pvpkills")
                        slot.face = rset.getInt("face")
                        slot.hairStyle = rset.getInt("hairstyle")
                        slot.hairColor = rset.getInt("haircolor")
                        slot.sex = rset.getInt("sex")
                        slot.exp = rset.getLong("exp")
                        slot.sp = rset.getInt("sp")
                        slot.clanId = rset.getInt("clanid")
                        slot.race = rset.getInt("race")
                        slot.x = rset.getInt("x")
                        slot.y = rset.getInt("y")
                        slot.z = rset.getInt("z")

                        val baseClassId = rset.getInt("base_class")
                        val activeClassId = rset.getInt("classid")

                        // If the player is currently on a subclass, loads subclass content.
                        if (baseClassId != activeClassId) {
                            val ps2 = con.prepareStatement(SELECT_CURRENT_SUBCLASS)
                            ps2.setInt(1, objectId)
                            ps2.setInt(2, activeClassId)

                            val rset2 = ps2.executeQuery()
                            if (rset2.next()) {
                                slot.exp = rset2.getLong("exp")
                                slot.sp = rset2.getInt("sp")
                                slot.level = rset2.getInt("level")
                            }
                            rset2.close()
                            ps2.close()
                        }

                        slot.classId = activeClassId

                        // Get the augmentation for equipped weapon.
                        val weaponObjId = slot.getPaperdollObjectId(Inventory.PAPERDOLL_RHAND)
                        if (weaponObjId > 0) {
                            val ps3 = con.prepareStatement(SELECT_AUGMENTS)
                            ps3.setInt(1, weaponObjId)

                            val rset3 = ps3.executeQuery()
                            if (rset3.next()) {
                                val augment = rset3.getInt("attributes")
                                slot.augmentationId = if (augment == -1) 0 else augment
                            }
                            rset3.close()
                            ps3.close()
                        }

                        slot.baseClassId = if (baseClassId == 0 && activeClassId > 0) activeClassId else baseClassId
                        slot.deleteTimer = deleteTime
                        slot.lastAccess = rset.getLong("lastAccess")

                        // Finally add the slot to the list.
                        list.add(slot)
                    }
                    rset.close()
                    ps.close()

                    return list.toTypedArray()
                }
            } catch (e: Exception) {
                L2GameServerPacket.LOGGER.error("Couldn't restore player slots for account {}.", e, loginName)
            }

            return emptyArray()
        }
    }
}