package com.l2kt.gameserver.network.serverpackets

import com.l2kt.commons.lang.StringUtil

class ShowBoard : L2GameServerPacket {

    private val _htmlCode = StringBuilder()

    constructor(htmlCode: String, id: String) {
        StringUtil.append(_htmlCode, id, "\u0008", htmlCode)
    }

    constructor(arg: List<String>) {
        _htmlCode.append("1002\u0008")
        for (str in arg)
            StringUtil.append(_htmlCode, str, " \u0008")
    }

    override fun writeImpl() {
        writeC(0x6e)
        writeC(0x01) // 1 to show, 0 to hide
        writeS(TOP)
        writeS(FAV)
        writeS(REGION)
        writeS(CLAN)
        writeS(MEMO)
        writeS(MAIL)
        writeS(FRIENDS)
        writeS(ADDFAV)
        writeS(_htmlCode.toString())
    }

    companion object {
        val STATIC_SHOWBOARD_102 = ShowBoard("", "102")
        val STATIC_SHOWBOARD_103 = ShowBoard("", "103")

        private const val TOP = "bypass _bbshome"
        private const val FAV = "bypass _bbsgetfav"
        private const val REGION = "bypass _bbsloc"
        private const val CLAN = "bypass _bbsclan"
        private const val MEMO = "bypass _bbsmemo"
        private const val MAIL = "bypass _maillist_0_1_0_"
        private const val FRIENDS = "bypass _friendlist_0_"
        private const val ADDFAV = "bypass bbs_add_fav"
    }
}