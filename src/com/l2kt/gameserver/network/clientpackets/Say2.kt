package com.l2kt.gameserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.gameserver.handler.ChatHandler
import com.l2kt.gameserver.network.SystemMessageId
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger

class Say2 : L2GameClientPacket() {

    private var _text: String = ""
    private var _type: Int = 0
    private var _target: String = ""

    override fun readImpl() {
        _text = readS()
        _type = readD()
        _target = if (_type == TELL) readS() else ""
    }

    override fun runImpl() {
        val player = client.activeChar ?: return

        if (_type < 0 || _type >= CHAT_NAMES.size)
            return

        if (_text.isEmpty() || _text.length > 100)
            return

        if (Config.L2WALKER_PROTECTION && _type == TELL && checkBot(_text))
            return

        if (!player.isGM && (_type == ANNOUNCEMENT || _type == CRITICAL_ANNOUNCE))
            return

        if (player.isChatBanned || player.isInJail && !player.isGM) {
            player.sendPacket(SystemMessageId.CHATTING_PROHIBITED)
            return
        }

        if (_type == PETITION_PLAYER && player.isGM)
            _type = PETITION_GM

        if (Config.LOG_CHAT) {
            val record = LogRecord(Level.INFO, _text)
            record.loggerName = "chat"

            if (_type == TELL)
                record.parameters = arrayOf<Any>(CHAT_NAMES[_type], "[" + player.name + " to " + _target + "]")
            else
                record.parameters = arrayOf<Any>(CHAT_NAMES[_type], "[" + player.name + "]")

            CHAT_LOG.log(record)
        }

        _text = _text.replace("\\\\n".toRegex(), "")

        val handler = ChatHandler.getInstance().getHandler(_type)
        if (handler == null) {
            L2GameClientPacket.LOGGER.warn(
                "{} tried to use unregistred chathandler type: {}.",
                player.name,
                _type
            )
            return
        }

        handler.handleChat(_type, player, _target, _text)
    }

    override fun triggersOnActionRequest(): Boolean {
        return false
    }

    companion object {
        private val CHAT_LOG = Logger.getLogger("chat")

        const val ALL = 0
        const val SHOUT = 1 // !
        const val TELL = 2
        const val PARTY = 3 // #
        const val CLAN = 4 // @
        const val GM = 5
        const val PETITION_PLAYER = 6
        const val PETITION_GM = 7
        const val TRADE = 8 // +
        const val ALLIANCE = 9 // $
        const val ANNOUNCEMENT = 10
        const val BOAT = 11
        const val L2FRIEND = 12
        const val MSNCHAT = 13
        const val PARTYMATCH_ROOM = 14
        const val PARTYROOM_COMMANDER = 15 // (Yellow)
        const val PARTYROOM_ALL = 16 // (Red)
        const val HERO_VOICE = 17
        const val CRITICAL_ANNOUNCE = 18

        private val CHAT_NAMES = arrayOf(
            "ALL",
            "SHOUT",
            "TELL",
            "PARTY",
            "CLAN",
            "GM",
            "PETITION_PLAYER",
            "PETITION_GM",
            "TRADE",
            "ALLIANCE",
            "ANNOUNCEMENT", // 10
            "BOAT",
            "WILLCRASHCLIENT:)",
            "FAKEALL?",
            "PARTYMATCH_ROOM",
            "PARTYROOM_COMMANDER",
            "PARTYROOM_ALL",
            "HERO_VOICE",
            "CRITICAL_ANNOUNCEMENT"
        )

        private val WALKER_COMMAND_LIST = arrayOf(
            "USESKILL",
            "USEITEM",
            "BUYITEM",
            "SELLITEM",
            "SAVEITEM",
            "LOADITEM",
            "MSG",
            "DELAY",
            "LABEL",
            "JMP",
            "CALL",
            "RETURN",
            "MOVETO",
            "NPCSEL",
            "NPCDLG",
            "DLGSEL",
            "CHARSTATUS",
            "POSOUTRANGE",
            "POSINRANGE",
            "GOHOME",
            "SAY",
            "EXIT",
            "PAUSE",
            "STRINDLG",
            "STRNOTINDLG",
            "CHANGEWAITTYPE",
            "FORCEATTACK",
            "ISMEMBER",
            "REQUESTJOINPARTY",
            "REQUESTOUTPARTY",
            "QUITPARTY",
            "MEMBERSTATUS",
            "CHARBUFFS",
            "ITEMCOUNT",
            "FOLLOWTELEPORT"
        )

        private fun checkBot(text: String): Boolean {
            for (botCommand in WALKER_COMMAND_LIST) {
                if (text.startsWith(botCommand))
                    return true
            }
            return false
        }
    }
}