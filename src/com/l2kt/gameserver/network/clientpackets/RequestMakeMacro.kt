package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.model.L2Macro
import com.l2kt.gameserver.network.SystemMessageId

class RequestMakeMacro : L2GameClientPacket() {

    private var _macro: L2Macro? = null
    private var _commandsLenght = 0

    override fun readImpl() {
        val id = readD()
        val name = readS()
        val desc = readS()
        val acronym = readS()
        val icon = readC()
        var count = readC()

        if (count > MAX_MACRO_LENGTH)
            count = MAX_MACRO_LENGTH

        val commands = arrayOfNulls<L2Macro.L2MacroCmd>(count)

        for (i in 0 until count) {
            val entry = readC()
            val type = readC() // 1 = skill, 3 = action, 4 = shortcut
            val d1 = readD() // skill or page number for shortcuts
            val d2 = readC()
            val command = readS()

            _commandsLenght += command.length
            commands[i] = L2Macro.L2MacroCmd(entry, type, d1, d2, command)
        }
        _macro = L2Macro(id, icon, name, desc, acronym, commands)
    }

    override fun runImpl() {
        val player = client.activeChar ?: return

        // Invalid macro. Refer to the Help file for instructions.
        if (_commandsLenght > 255) {
            player.sendPacket(SystemMessageId.INVALID_MACRO)
            return
        }

        // You may create up to 24 macros.
        if (player.macroses.allMacroses.size > 24) {
            player.sendPacket(SystemMessageId.YOU_MAY_CREATE_UP_TO_24_MACROS)
            return
        }

        // Enter the name of the macro.
        if (_macro!!.name.isEmpty()) {
            player.sendPacket(SystemMessageId.ENTER_THE_MACRO_NAME)
            return
        }

        // Macro descriptions may contain up to 32 characters.
        if (_macro!!.descr.length > 32) {
            player.sendPacket(SystemMessageId.MACRO_DESCRIPTION_MAX_32_CHARS)
            return
        }

        player.registerMacro(_macro)
    }

    companion object {
        private val MAX_MACRO_LENGTH = 12
    }
}