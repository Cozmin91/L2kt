package com.l2kt.gameserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.gameserver.network.SystemMessageId

/**
 * @author Dezmond_snz Format: cddd
 */
class DlgAnswer : L2GameClientPacket() {
    private var _messageId: Int = 0
    private var _answer: Int = 0
    private var _requesterId: Int = 0

    override fun readImpl() {
        _messageId = readD()
        _answer = readD()
        _requesterId = readD()
    }

    public override fun runImpl() {
        val activeChar = client.activeChar ?: return

        when {
            _messageId == SystemMessageId.RESSURECTION_REQUEST_BY_S1.id || _messageId == SystemMessageId.DO_YOU_WANT_TO_BE_RESTORED.id -> activeChar.reviveAnswer(_answer)
            _messageId == SystemMessageId.S1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT.id -> activeChar.teleportAnswer(_answer, _requesterId)
            _messageId == 1983 && Config.ALLOW_WEDDING -> activeChar.engageAnswer(_answer)
            _messageId == SystemMessageId.WOULD_YOU_LIKE_TO_OPEN_THE_GATE.id -> activeChar.activateGate(_answer, 1)
            _messageId == SystemMessageId.WOULD_YOU_LIKE_TO_CLOSE_THE_GATE.id -> activeChar.activateGate(_answer, 0)
        }
    }
}