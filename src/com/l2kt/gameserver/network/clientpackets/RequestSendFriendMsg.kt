package com.l2kt.gameserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.L2FriendSay
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger

/**
 * Recieve Private (Friend) Message - 0xCC Format: c SS S: Message S: Receiving Player
 * @author Tempy
 */
class RequestSendFriendMsg : L2GameClientPacket() {

    private var _message: String = ""
    private var _reciever: String = ""

    override fun readImpl() {
        _message = readS()
        _reciever = readS()
    }

    override fun runImpl() {
        if (_message.isEmpty() || _message.length > 300)
            return

        val activeChar = client.activeChar ?: return

        val targetPlayer = World.getInstance().getPlayer(_reciever)
        if (targetPlayer == null || !targetPlayer.friendList.contains(activeChar.objectId)) {
            activeChar.sendPacket(SystemMessageId.TARGET_IS_NOT_FOUND_IN_THE_GAME)
            return
        }

        if (Config.LOG_CHAT) {
            val record = LogRecord(Level.INFO, _message)
            record.loggerName = "chat"
            record.parameters = arrayOf<Any>("PRIV_MSG", "[" + activeChar.name + " to " + _reciever + "]")

            CHAT_LOG.log(record)
        }

        targetPlayer.sendPacket(L2FriendSay(activeChar.name, _reciever, _message))
    }

    companion object {
        private val CHAT_LOG = Logger.getLogger("chat")
    }
}