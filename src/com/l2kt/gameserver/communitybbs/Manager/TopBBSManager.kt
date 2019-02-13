package com.l2kt.gameserver.communitybbs.Manager

import com.l2kt.gameserver.model.actor.instance.Player
import java.util.*

object TopBBSManager : BaseBBSManager() {

    override fun parseCmd(command: String, player: Player) {
        if (command == "_bbshome")
            loadStaticHtm("index.htm", player)
        else if (command.startsWith("_bbshome;")) {
            val st = StringTokenizer(command, ";")
            st.nextToken()

            loadStaticHtm(st.nextToken(), player)
        } else
            super.parseCmd(command, player)
    }

    override val folder: String get() = "top/"
}