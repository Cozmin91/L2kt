package com.l2kt.gameserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.commons.lang.StringUtil
import com.l2kt.gameserver.data.sql.PlayerInfoTable
import com.l2kt.gameserver.data.xml.NpcData
import com.l2kt.gameserver.data.xml.PlayerData
import com.l2kt.gameserver.data.xml.ScriptData
import com.l2kt.gameserver.idfactory.IdFactory
import com.l2kt.gameserver.model.L2ShortCut
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.Sex
import com.l2kt.gameserver.model.item.kind.Item
import com.l2kt.gameserver.network.serverpackets.CharCreateFail
import com.l2kt.gameserver.network.serverpackets.CharCreateOk
import com.l2kt.gameserver.network.serverpackets.CharSelectInfo
import com.l2kt.gameserver.scripting.Quest

class CharacterCreate : L2GameClientPacket() {
    private var _name: String = ""
    private var _race: Int = 0
    private var _sex: Byte = 0
    private var _classId: Int = 0
    private var _int: Int = 0
    private var _str: Int = 0
    private var _con: Int = 0
    private var _men: Int = 0
    private var _dex: Int = 0
    private var _wit: Int = 0
    private var _hairStyle: Byte = 0
    private var _hairColor: Byte = 0
    private var _face: Byte = 0

    override fun readImpl() {
        _name = readS()
        _race = readD()
        _sex = readD().toByte()
        _classId = readD()
        _int = readD()
        _str = readD()
        _con = readD()
        _men = readD()
        _dex = readD()
        _wit = readD()
        _hairStyle = readD().toByte()
        _hairColor = readD().toByte()
        _face = readD().toByte()
    }

    override fun runImpl() {
        // Invalid race.
        if (_race > 4 || _race < 0) {
            sendPacket(CharCreateFail.REASON_CREATION_FAILED)
            return
        }

        // Invalid face.
        if (_face > 2 || _face < 0) {
            sendPacket(CharCreateFail.REASON_CREATION_FAILED)
            return
        }

        // Invalid hair style.
        if (_hairStyle < 0 || _sex.toInt() == 0 && _hairStyle > 4 || _sex.toInt() != 0 && _hairStyle > 6) {
            sendPacket(CharCreateFail.REASON_CREATION_FAILED)
            return
        }

        // Invalid hair color.
        if (_hairColor > 3 || _hairColor < 0) {
            sendPacket(CharCreateFail.REASON_CREATION_FAILED)
            return
        }

        // Invalid name length, or name typo.
        if (!StringUtil.isValidString(_name, "^[A-Za-z0-9]{3,16}$")) {
            sendPacket(if (_name.length > 16) CharCreateFail.REASON_16_ENG_CHARS else CharCreateFail.REASON_INCORRECT_NAME)
            return
        }

        // Your name is already taken by a NPC.
        if (NpcData.getInstance().getTemplateByName(_name) != null) {
            sendPacket(CharCreateFail.REASON_INCORRECT_NAME)
            return
        }

        // You already have the maximum amount of characters for this account.
        if (PlayerInfoTable.getInstance().getCharactersInAcc(client.accountName) >= 7) {
            sendPacket(CharCreateFail.REASON_TOO_MANY_CHARACTERS)
            return
        }

        // The name already exists.
        if (PlayerInfoTable.getInstance().getPlayerObjectId(_name) > 0) {
            sendPacket(CharCreateFail.REASON_NAME_ALREADY_EXISTS)
            return
        }

        // The class id related to this template is post-newbie.
        val template = PlayerData.getInstance().getTemplate(_classId)
        if (template == null || template.classBaseLevel > 1) {
            sendPacket(CharCreateFail.REASON_CREATION_FAILED)
            return
        }

        // Create the player Object.
        val player = Player.create(
            IdFactory.getInstance().nextId,
            template,
            client.accountName,
            _name,
            _hairStyle,
            _hairColor,
            _face,
            Sex.values()[_sex.toInt()]
        )
        if (player == null) {
            sendPacket(CharCreateFail.REASON_CREATION_FAILED)
            return
        }

        // Set default values.
        player.currentCp = 0.0
        player.currentHp = player.maxHp.toDouble()
        player.currentMp = player.maxMp.toDouble()

        // send acknowledgement
        sendPacket(CharCreateOk.STATIC_PACKET)

        World.getInstance().addObject(player)

        player.position.set(template.randomSpawn)
        player.title = ""

        // Register shortcuts.
        player.registerShortCut(L2ShortCut(0, 0, 3, 2, -1, 1)) // attack shortcut
        player.registerShortCut(L2ShortCut(3, 0, 3, 5, -1, 1)) // take shortcut
        player.registerShortCut(L2ShortCut(10, 0, 3, 0, -1, 1)) // sit shortcut

        // Equip or add items, based on template.
        for (itemId in template.itemIds) {
            val item = player.inventory!!.addItem("Init", itemId, 1, player, null)
            if (itemId == 5588)
            // tutorial book shortcut
                player.registerShortCut(L2ShortCut(11, 0, 1, item!!.objectId, -1, 1))

            if (item!!.isEquipable) {
                if (player.activeWeaponItem == null || item.item.type2 == Item.TYPE2_WEAPON)
                    player.inventory!!.equipItemAndRecord(item)
            }
        }

        // Add skills.
        for (skill in player.availableAutoGetSkills) {
            if (skill.id == 1001 || skill.id == 1177)
                player.registerShortCut(L2ShortCut(1, 0, 2, skill.id, 1, 1))

            if (skill.id == 1216)
                player.registerShortCut(L2ShortCut(9, 0, 2, skill.id, 1, 1))
        }

        // Tutorial runs here.
        if (!Config.DISABLE_TUTORIAL) {
            if (player.getQuestState("Tutorial") == null) {
                val quest = ScriptData.getInstance().getQuest("Tutorial")
                if (quest != null)
                    quest.newQuestState(player).state = Quest.STATE_STARTED
            }
        }

        player.setOnlineStatus(true, false)
        player.deleteMe()

        val csi = CharSelectInfo(client.accountName!!, client.sessionId!!.playOkID1)
        sendPacket(csi)
        client.setCharSelectSlot(csi.characterSlots)
    }
}