package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.network.SystemMessageId
import java.util.*

class CreatureSay : L2GameServerPacket {
    private val _objectId: Int
    private val _textType: Int
    private var _charName: String? = null
    private var _charId = 0
    private var _text: String? = null
    private var _npcString = -1
    private var _parameters: MutableList<String>? = null

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
        if (_parameters == null)
            _parameters = ArrayList()

        _parameters!!.add(text)
    }

    override fun writeImpl() {
        writeC(0x4a)
        writeD(_objectId)
        writeD(_textType)
        if (_charName != null)
            writeS(_charName)
        else
            writeD(_charId)
        writeD(_npcString) // High Five NPCString ID
        if (_text != null)
            writeS(_text)
        else {
            if (_parameters != null) {
                for (s in _parameters!!)
                    writeS(s)
            }
        }
    }
}