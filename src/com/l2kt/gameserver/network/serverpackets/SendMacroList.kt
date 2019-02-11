package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.L2Macro

/**
 * packet type id 0xe7 sample e7 d // unknown change of Macro edit,add,delete c // unknown c //count of Macros c // unknown d // id S // macro name S // desc S // acronym c // icon c // count c // entry c // type d // skill id c // shortcut id S // command name format: cdhcdSSScc (ccdcS)
 */
class SendMacroList(private val _rev: Int, private val _count: Int, private val _macro: L2Macro?) :
    L2GameServerPacket() {

    override fun writeImpl() {
        writeC(0xE7)

        writeD(_rev) // macro change revision (changes after each macro edition)
        writeC(0) // unknown
        writeC(_count) // count of Macros
        writeC(if (_macro != null) 1 else 0) // unknown

        if (_macro != null) {
            writeD(_macro.id) // Macro ID
            writeS(_macro.name) // Macro Name
            writeS(_macro.descr) // Desc
            writeS(_macro.acronym) // acronym
            writeC(_macro.icon) // icon

            writeC(_macro.commands.size) // count

            for (i in _macro.commands.indices) {
                val cmd = _macro.commands[i]
                writeC(i + 1) // i of count
                writeC(cmd.type) // type 1 = skill, 3 = action, 4 = shortcut
                writeD(cmd.d1) // skill id
                writeC(cmd.d2) // shortcut id
                writeS(cmd.cmd) // command name
            }
        }
    }
}