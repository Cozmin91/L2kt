package com.l2kt.gameserver.network.serverpackets

/**
 * @author glory to Setekh for IL opcode ;p
 */
class ExShowScreenMessage : L2GameServerPacket {

    private val _type: Int
    private val _sysMessageId: Int
    private val _hide: Boolean
    private val _unk2: Int
    private val _unk3: Int
    private val _fade: Boolean
    private val _size: Int
    private val _position: Int
    private val _effect: Boolean
    private val _text: String
    private val _time: Int

    enum class SMPOS {
        DUMMY,
        TOP_LEFT,
        TOP_CENTER,
        TOP_RIGHT,
        MIDDLE_LEFT,
        MIDDLE_CENTER,
        MIDDLE_RIGHT,
        BOTTOM_CENTER,
        BOTTOM_RIGHT
    }

    constructor(text: String, time: Int) {
        _type = 1
        _sysMessageId = -1
        _hide = false
        _unk2 = 0
        _unk3 = 0
        _fade = false
        _position = 0x02
        _text = text
        _time = time
        _size = 0
        _effect = false
    }

    constructor(text: String, time: Int, pos: SMPOS, effect: Boolean) : this(text, time, pos.ordinal, effect) {}

    constructor(text: String, time: Int, pos: Int, effect: Boolean) {
        _type = 1
        _sysMessageId = -1
        _hide = false
        _unk2 = 0
        _unk3 = 0
        _fade = false
        _position = pos
        _text = text
        _time = time
        _size = 0
        _effect = effect
    }

    constructor(
        type: Int,
        messageId: Int,
        position: Int,
        hide: Boolean,
        size: Int,
        unk2: Int,
        unk3: Int,
        showEffect: Boolean,
        time: Int,
        fade: Boolean,
        text: String
    ) {
        _type = type
        _sysMessageId = messageId
        _hide = hide
        _unk2 = unk2
        _unk3 = unk3
        _fade = fade
        _position = position
        _text = text
        _time = time
        _size = size
        _effect = showEffect
    }

    override fun writeImpl() {
        writeC(0xfe)
        writeH(0x38)
        writeD(_type) // 0 - system messages, 1 - your defined text
        writeD(_sysMessageId) // system message id (_type must be 0 otherwise no effect)
        writeD(_position) // message position
        writeD(if (_hide) 1 else 0) // hide
        writeD(_size) // font size 0 - normal, 1 - small
        writeD(_unk2) // ?
        writeD(_unk3) // ?
        writeD(if (_effect) 1 else 0) // upper effect (0 - disabled, 1 enabled) - _position must be 2 (center) otherwise no effect
        writeD(_time) // time
        writeD(if (_fade) 1 else 0) // fade effect (0 - disabled, 1 enabled)
        writeS(_text) // your text (_type must be 1, otherwise no effect)
    }
}