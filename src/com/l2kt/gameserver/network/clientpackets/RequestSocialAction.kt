package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.network.FloodProtectors
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SocialAction

class RequestSocialAction : L2GameClientPacket() {
    private var _actionId: Int = 0

    override fun readImpl() {
        _actionId = readD()
    }

    override fun runImpl() {
        if (!FloodProtectors.performAction(client, FloodProtectors.Action.SOCIAL))
            return

        val activeChar = client.activeChar ?: return

        if (activeChar.isFishing) {
            activeChar.sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_3)
            return
        }

        if (_actionId < 2 || _actionId > 13)
            return

        if (activeChar.isInStoreMode || activeChar.activeRequester != null || activeChar.isAlikeDead || activeChar.ai.desire.intention != CtrlIntention.IDLE)
            return

        activeChar.broadcastPacket(SocialAction(activeChar, _actionId))
    }
}