package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.holder.IntIntHolder
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.model.item.kind.Item
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.network.SystemMessageId

class SystemMessage private constructor(val systemMessageId: SystemMessageId) : L2GameServerPacket() {
    private var _params: Array<SMParam?> = emptyArray()
    private var _paramIndex: Int = 0

    init {
        val paramCount = systemMessageId.paramCount
        _params = if (paramCount != 0) arrayOfNulls(paramCount) else EMPTY_PARAM_ARRAY
    }

    private fun append(param: SMParam) {
        if (_paramIndex >= _params.size) {
            _params = _params.copyOf(_paramIndex + 1)
            systemMessageId.paramCount = _paramIndex + 1

            L2GameServerPacket.Companion.LOGGER.warn(
                "Wrong parameter count '{}' for {}.",
                _paramIndex + 1,
                systemMessageId
            )
        }

        _params[_paramIndex++] = param
    }

    fun addString(text: String): SystemMessage {
        append(SMParam(TYPE_TEXT, text))
        return this
    }

    /**
     * Castlename-e.dat<br></br>
     * 0-9 Castle names<br></br>
     * 21-64 CH names<br></br>
     * 81-89 Territory names<br></br>
     * 101-121 Fortress names<br></br>
     * @param number
     * @return
     */
    fun addFortId(number: Int): SystemMessage {
        append(SMParam(TYPE_CASTLE_NAME, number))
        return this
    }

    fun addNumber(number: Int): SystemMessage {
        append(SMParam(TYPE_NUMBER, number))
        return this
    }

    fun addItemNumber(number: Int): SystemMessage {
        append(SMParam(TYPE_ITEM_NUMBER, number))
        return this
    }

    fun addCharName(cha: Creature): SystemMessage {
        return addString(cha.name)
    }

    fun addItemName(item: ItemInstance): SystemMessage {
        return addItemName(item.item.itemId)
    }

    fun addItemName(item: Item): SystemMessage {
        return addItemName(item.itemId)
    }

    fun addItemName(id: Int): SystemMessage {
        append(SMParam(TYPE_ITEM_NAME, id))
        return this
    }

    fun addZoneName(loc: Location): SystemMessage {
        append(SMParam(TYPE_ZONE_NAME, loc))
        return this
    }

    fun addSkillName(effect: L2Effect): SystemMessage {
        return addSkillName(effect.skill)
    }

    fun addSkillName(skill: L2Skill): SystemMessage {
        return addSkillName(skill.id, skill.level)
    }

    @JvmOverloads
    fun addSkillName(id: Int, lvl: Int = 1): SystemMessage {
        append(SMParam(TYPE_SKILL_NAME, IntIntHolder(id, lvl)))
        return this
    }

    override fun writeImpl() {
        writeC(0x64)

        writeD(systemMessageId.id)
        writeD(_paramIndex)

        var param: SMParam
        for (i in 0 until _paramIndex) {
            param = _params[i]!!
            writeD(param.type)

            when (param.type) {
                TYPE_TEXT.toInt() -> writeS(param.`object` as String)

                TYPE_ITEM_NUMBER.toInt(), TYPE_ITEM_NAME.toInt(), TYPE_CASTLE_NAME.toInt(), TYPE_NUMBER.toInt(), TYPE_NPC_NAME.toInt()-> writeD(param.`object` as Int)

                TYPE_SKILL_NAME.toInt() -> {
                    val info = param.`object` as IntIntHolder
                    writeD(info.id)
                    writeD(info.value)
                }

                TYPE_ZONE_NAME.toInt() -> writeLoc(param.`object` as Location)
            }
        }
    }

    private class SMParam(private val _type: Byte, val `object`: Any) {

        val type: Int
            get() = _type.toInt()
    }

    companion object {
        private val EMPTY_PARAM_ARRAY = arrayOfNulls<SMParam>(0)

        private const val TYPE_ZONE_NAME: Byte = 7
        private const val TYPE_ITEM_NUMBER: Byte = 6
        private const val TYPE_CASTLE_NAME: Byte = 5
        private const val TYPE_SKILL_NAME: Byte = 4
        private const val TYPE_ITEM_NAME: Byte = 3
        private const val TYPE_NPC_NAME: Byte = 2
        private const val TYPE_NUMBER: Byte = 1
        private const val TYPE_TEXT: Byte = 0

        fun sendString(text: String): SystemMessage {
            return SystemMessage.getSystemMessage(SystemMessageId.S1).addString(text)
        }

        fun getSystemMessage(smId: SystemMessageId): SystemMessage {
            var sm: SystemMessage? = smId.staticSystemMessage
            if (sm != null)
                return sm

            sm = SystemMessage(smId)
            if (smId.paramCount == 0)
                smId.staticSystemMessage = sm

            return sm
        }

        /**
         * Use [.getSystemMessage] where possible instead
         * @param id
         * @return the system message associated to the given Id.
         */
        fun getSystemMessage(id: Int): SystemMessage {
            return getSystemMessage(SystemMessageId.getSystemMessageId(id))
        }
    }
}