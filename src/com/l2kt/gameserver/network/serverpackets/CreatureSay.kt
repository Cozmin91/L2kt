package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.network.SystemMessageId
import java.util.*

class CreatureSay : L2GameServerPacket {
    private val _objectId: Int
    private val _textType: Int
    private var _charName: String = ""
    private var _charId = 0
    private var _text: String = ""
    private var _npcString = -1
    private var _parameters: MutableList<String> = mutableListOf()

    constructor(objectId: Int, messageType: Int, charName: String, text: String) {
        _objectId = objectId
        _textType = messageType
        _charName = charName
        _text = text
    }

    constructor(objectId: Int, messageType: Int, charId: Int, sysString: SystemMessageId) {
        _objectId = objectId
        _textType = messageType
        _charId = charId
        _npcString = sysString.id
    }

    fun addStringParameter(text: String) {
        _parameters.add(text)
    }

    override fun writeImpl() {
        writeC(0x4a)
        writeD(_objectId)
        writeD(_textType)
        if (!_charName.isEmpty())
            writeS(_charName)
        else
            writeD(_charId)
        writeD(_npcString) // High Five NPCString ID
        if (!_text.isEmpty())
            writeS(_text)
        else {
            for (s in _parameters)
                writeS(s)
        }
    }
}