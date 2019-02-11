package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.communitybbs.CommunityBoard

/**
 * Format SSSSSS
 * @author -Wooden-
 */
class RequestBBSwrite : L2GameClientPacket() {
    private var _url: String? = null
    private var _arg1: String? = null
    private var _arg2: String? = null
    private var _arg3: String? = null
    private var _arg4: String? = null
    private var _arg5: String? = null

    override fun readImpl() {
        _url = readS()
        _arg1 = readS()
        _arg2 = readS()
        _arg3 = readS()
        _arg4 = readS()
        _arg5 = readS()
    }

    override fun runImpl() {
        CommunityBoard.getInstance().handleWriteCommands(client, _url, _arg1, _arg2, _arg3, _arg4, _arg5)
    }
}