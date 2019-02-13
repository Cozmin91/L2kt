package com.l2kt.gameserver.communitybbs

import com.l2kt.Config
import com.l2kt.gameserver.communitybbs.Manager.*
import com.l2kt.gameserver.network.L2GameClient
import com.l2kt.gameserver.network.SystemMessageId

object CommunityBoard {

    fun handleCommands(client: L2GameClient, command: String) {
        val player = client.activeChar ?: return

        if (!Config.ENABLE_COMMUNITY_BOARD) {
            player.sendPacket(SystemMessageId.CB_OFFLINE)
            return
        }

        when {
            command.startsWith("_bbshome") -> TopBBSManager.parseCmd(command, player)
            command.startsWith("_bbsloc") -> RegionBBSManager.parseCmd(command, player)
            command.startsWith("_bbsclan") -> ClanBBSManager.parseCmd(command, player)
            command.startsWith("_bbsmemo") -> TopicBBSManager.parseCmd(command, player)
            command.startsWith("_bbsmail") || command == "_maillist_0_1_0_" -> MailBBSManager.parseCmd(command, player)
            command.startsWith("_friend") || command.startsWith("_block") -> FriendsBBSManager.parseCmd(command, player)
            command.startsWith("_bbstopics") -> TopicBBSManager.parseCmd(command, player)
            command.startsWith("_bbsposts") -> PostBBSManager.parseCmd(command, player)
            else -> BaseBBSManager.separateAndSend(
                "<html><body><br><br><center>The command: $command isn't implemented.</center></body></html>",
                player
            )
        }
    }

    fun handleWriteCommands(
        client: L2GameClient,
        url: String,
        arg1: String,
        arg2: String,
        arg3: String,
        arg4: String,
        arg5: String
    ) {
        val player = client.activeChar ?: return

        if (!Config.ENABLE_COMMUNITY_BOARD) {
            player.sendPacket(SystemMessageId.CB_OFFLINE)
            return
        }

        when (url) {
            "Topic" -> TopicBBSManager.parseWrite(arg1, arg2, arg3, arg4, arg5, player)
            "Post" -> PostBBSManager.parseWrite(arg1, arg2, arg3, arg4, arg5, player)
            "_bbsloc" -> RegionBBSManager.parseWrite(arg1, arg2, arg3, arg4, arg5, player)
            "_bbsclan" -> ClanBBSManager.parseWrite(arg1, arg2, arg3, arg4, arg5, player)
            "Mail" -> MailBBSManager.parseWrite(arg1, arg2, arg3, arg4, arg5, player)
            "_friend" -> FriendsBBSManager.parseWrite(arg1, arg2, arg3, arg4, arg5, player)
            else -> BaseBBSManager.separateAndSend(
                "<html><body><br><br><center>The command: $url isn't implemented.</center></body></html>",
                player
            )
        }
    }
}