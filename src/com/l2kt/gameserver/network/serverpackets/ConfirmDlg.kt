package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.holder.IntIntHolder
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.model.item.kind.Item
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.network.SystemMessageId
import java.util.*

class ConfirmDlg : L2GameServerPacket {

    private val _messageId: Int
    private val _info = ArrayList<CnfDlgData>()

    private var _time = 0
    private var _requesterId = 0

    constructor(messageId: Int) {
        _messageId = messageId
    }

    constructor(messageId: SystemMessageId) {
        _messageId = messageId.id
    }

    fun addString(text: String): ConfirmDlg {
        _info.add(CnfDlgData(TYPE_TEXT, text))
        return this
    }

    fun addNumber(number: Int): ConfirmDlg {
        _info.add(CnfDlgData(TYPE_NUMBER, number))
        return this
    }

    fun addCharName(cha: Creature): ConfirmDlg {
        return addString(cha.name)
    }

    fun addItemName(item: ItemInstance): ConfirmDlg {
        return addItemName(item.item.itemId)
    }

    fun addItemName(item: Item): ConfirmDlg {
        return addItemName(item.itemId)
    }

    fun addItemName(id: Int): ConfirmDlg {
        _info.add(CnfDlgData(TYPE_ITEM_NAME, id))
        return this
    }

    fun addZoneName(loc: Location): ConfirmDlg {
        _info.add(CnfDlgData(TYPE_ZONE_NAME, loc))
        return this
    }

    fun addSkillName(effect: L2Effect): ConfirmDlg {
        return addSkillName(effect.skill)
    }

    fun addSkillName(skill: L2Skill): ConfirmDlg {
        return addSkillName(skill.id, skill.level)
    }

    @JvmOverloads
    fun addSkillName(id: Int, lvl: Int = 1): ConfirmDlg {
        _info.add(CnfDlgData(TYPE_SKILL_NAME, IntIntHolder(id, lvl)))
        return this
    }

    fun addTime(time: Int): ConfirmDlg {
        _time = time
        return this
    }

    fun addRequesterId(id: Int): ConfirmDlg {
        _requesterId = id
        return this
    }

    override fun writeImpl() {
        writeC(0xed)
        writeD(_messageId)

        if (_info.isEmpty()) {
            writeD(0x00)
            writeD(_time)
            writeD(_requesterId)
        } else {
            writeD(_info.size)

            for (data in _info) {
                writeD(data.type)

                when (data.type) {
                    TYPE_TEXT -> writeS(data.`object` as String)

                    TYPE_NUMBER, TYPE_NPC_NAME, TYPE_ITEM_NAME -> writeD(data.`object` as Int)

                    TYPE_SKILL_NAME -> {
                        val info = data.`object` as IntIntHolder
                        writeD(info.id)
                        writeD(info.value)
                    }

                    TYPE_ZONE_NAME -> writeLoc(data.`object` as Location)
                }
            }
            if (_time != 0)
                writeD(_time)
            if (_requesterId != 0)
                writeD(_requesterId)
        }
    }

    private class CnfDlgData(val type: Int, val `object`: Any)

    companion object {
        private const val TYPE_ZONE_NAME = 7
        private const val TYPE_SKILL_NAME = 4
        private const val TYPE_ITEM_NAME = 3
        private const val TYPE_NPC_NAME = 2
        private const val TYPE_NUMBER = 1
        private const val TYPE_TEXT = 0
    }
}