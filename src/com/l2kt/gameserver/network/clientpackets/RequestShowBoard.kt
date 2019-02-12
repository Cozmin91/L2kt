package com.l2kt.gameserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.gameserver.communitybbs.CommunityBoard

class RequestShowBoard : L2GameClientPacket() {
    private var _unknown: Int = 0

    override fun readImpl() {
        _unknown = readD()
    }

    override fun runImpl() {
        CommunityBoard.getInstance().handleCommands(client, Config.BBS_DEFAULT)
    }
}