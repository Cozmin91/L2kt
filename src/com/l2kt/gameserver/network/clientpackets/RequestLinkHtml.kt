package com.l2kt.gameserver.network.clientpackets

import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage

/**
 * @author zabbix Lets drink to code!
 */
class RequestLinkHtml : L2GameClientPacket() {
    private var _link: String? = null

    override fun readImpl() {
        _link = readS()
    }

    public override fun runImpl() {
        client.activeChar ?: return

        if (_link!!.contains("..") || !_link!!.contains(".htm"))
            return

        val html = NpcHtmlMessage(0)
        html.setFile(_link!!)
        sendPacket(html)
    }
}