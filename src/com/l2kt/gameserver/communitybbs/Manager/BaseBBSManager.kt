package com.l2kt.gameserver.communitybbs.Manager

import com.l2kt.commons.logging.CLogger
import com.l2kt.gameserver.data.cache.HtmCache
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.serverpackets.ShowBoard
import java.util.*

abstract class BaseBBSManager {

    /**
     * That method is overidden in every board type. It allows to switch of folders following the board.
     * @return the folder.
     */
    open val folder: String
        get() = ""

    open fun parseCmd(command: String, player: Player) {
        separateAndSend(
            "<html><body><br><br><center>The command: $command isn't implemented.</center></body></html>",
            player
        )
    }

    open fun parseWrite(ar1: String, ar2: String, ar3: String, ar4: String, ar5: String, player: Player) {
        separateAndSend(
            "<html><body><br><br><center>The command: $ar1 isn't implemented.</center></body></html>",
            player
        )
    }

    /**
     * Loads an HTM located in the default CB path.
     * @param file : the file to load.
     * @param player : the requester.
     */
    fun loadStaticHtm(file: String, player: Player) {
        separateAndSend(HtmCache.getInstance().getHtm(CB_PATH + folder + file), player)
    }

    companion object {
        val LOGGER = CLogger(BaseBBSManager::class.java.name)

        const val CB_PATH = "data/html/CommunityBoard/"

        fun separateAndSend(html: String?, player: Player?) {
            if (html == null || player == null)
                return

            when {
                html.length < 4090 -> {
                    player.sendPacket(ShowBoard(html, "101"))
                    player.sendPacket(ShowBoard.STATIC_SHOWBOARD_102)
                    player.sendPacket(ShowBoard.STATIC_SHOWBOARD_103)
                }
                html.length < 8180 -> {
                    player.sendPacket(ShowBoard(html.substring(0, 4090), "101"))
                    player.sendPacket(ShowBoard(html.substring(4090, html.length), "102"))
                    player.sendPacket(ShowBoard.STATIC_SHOWBOARD_103)
                }
                html.length < 12270 -> {
                    player.sendPacket(ShowBoard(html.substring(0, 4090), "101"))
                    player.sendPacket(ShowBoard(html.substring(4090, 8180), "102"))
                    player.sendPacket(ShowBoard(html.substring(8180, html.length), "103"))
                }
            }
        }

        fun send1001(html: String, player: Player) {
            if (html.length < 8180)
                player.sendPacket(ShowBoard(html, "1001"))
        }

        @JvmOverloads
        fun send1002(player: Player, string: String = " ", string2: String = " ", string3: String = "0") {
            val params = ArrayList<String>()
            params.add("0")
            params.add("0")
            params.add("0")
            params.add("0")
            params.add("0")
            params.add("0")
            params.add(player.name)
            params.add(Integer.toString(player.objectId))
            params.add(player.accountName)
            params.add("9")
            params.add(string2)
            params.add(string2)
            params.add(string)
            params.add(string3)
            params.add(string3)
            params.add("0")
            params.add("0")

            player.sendPacket(ShowBoard(params))
        }
    }
}