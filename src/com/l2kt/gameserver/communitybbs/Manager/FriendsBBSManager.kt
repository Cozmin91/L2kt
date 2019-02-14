package com.l2kt.gameserver.communitybbs.Manager

import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.lang.StringUtil
import com.l2kt.gameserver.data.cache.HtmCache
import com.l2kt.gameserver.data.sql.PlayerInfoTable
import com.l2kt.gameserver.model.BlockList
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.FriendList
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import java.util.*

object FriendsBBSManager : BaseBBSManager() {

    override fun parseCmd(command: String, player: Player) {
        if (command.startsWith("_friendlist"))
            showFriendsList(player, false)
        else if (command.startsWith("_blocklist"))
            showBlockList(player, false)
        else if (command.startsWith("_friend")) {
            val st = StringTokenizer(command, ";")
            st.nextToken()
            val action = st.nextToken()

            if (action == "select") {
                player.selectFriend(if (st.hasMoreTokens()) Integer.valueOf(st.nextToken()) else 0)
                showFriendsList(player, false)
            } else if (action == "deselect") {
                player.deselectFriend(if (st.hasMoreTokens()) Integer.valueOf(st.nextToken()) else 0)
                showFriendsList(player, false)
            } else if (action == "delall") {
                try {
                    L2DatabaseFactory.connection.use { con ->
                        con.prepareStatement(DELETE_ALL_FRIENDS).use { ps ->
                            ps.setInt(1, player.objectId)
                            ps.setInt(2, player.objectId)
                            ps.execute()
                        }
                    }
                } catch (e: Exception) {
                    BaseBBSManager.LOGGER.error("Couldn't delete friends.", e)
                }

                for (friendId in player.friendList) {
                    // Update friend's friendlist.
                    val friend = World.getInstance().getPlayer(friendId)
                    if (friend != null) {
                        friend.friendList.remove(Integer.valueOf(player.objectId))
                        friend.selectedFriendList.remove(Integer.valueOf(player.objectId))

                        friend.sendPacket(FriendList(friend))
                    }
                }

                player.friendList.clear()
                player.selectedFriendList.clear()
                showFriendsList(player, false)

                player.sendMessage("You have cleared your friends list.")
                player.sendPacket(FriendList(player))
            } else if (action == "delconfirm")
                showFriendsList(player, true)
            else if (action == "del") {
                try {
                    L2DatabaseFactory.connection.use { con ->
                        con.prepareStatement(DELETE_FRIEND).use { ps ->
                            ps.setInt(1, player.objectId)
                            ps.setInt(4, player.objectId)

                            for (friendId in player.selectedFriendList) {
                                ps.setInt(2, friendId)
                                ps.setInt(3, friendId)
                                ps.addBatch()

                                // Update friend's friendlist.
                                val friend = World.getInstance().getPlayer(friendId)
                                if (friend != null) {
                                    friend.friendList.remove(Integer.valueOf(player.objectId))
                                    friend.sendPacket(FriendList(friend))
                                }

                                // The friend is deleted from your friendlist.
                                player.friendList.remove(Integer.valueOf(friendId))
                                player.sendPacket(
                                    SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_DELETED_FROM_YOUR_FRIENDS_LIST).addString(
                                        PlayerInfoTable.getPlayerName(friendId)!!
                                    )
                                )
                            }
                            ps.executeBatch()
                        }
                    }
                } catch (e: Exception) {
                    BaseBBSManager.LOGGER.error("Couldn't delete friend.", e)
                }

                player.selectedFriendList.clear()
                showFriendsList(player, false)

                player.sendPacket(FriendList(player)) // update friendList *heavy method*
            } else if (action == "mail") {
                if (!player.selectedFriendList.isEmpty())
                    showMailWrite(player)
            }
        } else if (command.startsWith("_block")) {
            val st = StringTokenizer(command, ";")
            st.nextToken()
            val action = st.nextToken()

            if (action == "select") {
                player.selectBlock(if (st.hasMoreTokens()) Integer.valueOf(st.nextToken()) else 0)
                showBlockList(player, false)
            } else if (action == "deselect") {
                player.deselectBlock(if (st.hasMoreTokens()) Integer.valueOf(st.nextToken()) else 0)
                showBlockList(player, false)
            } else if (action == "delall") {
                val list = ArrayList<Int>()
                list.addAll(player.blockList.blockList!!)

                for (blockId in list)
                    BlockList.removeFromBlockList(player, blockId)

                player.selectedBlocksList.clear()
                showBlockList(player, false)
            } else if (action == "delconfirm")
                showBlockList(player, true)
            else if (action == "del") {
                for (blockId in player.selectedBlocksList)
                    BlockList.removeFromBlockList(player, blockId!!)

                player.selectedBlocksList.clear()
                showBlockList(player, false)
            }
        } else
            super.parseCmd(command, player)
    }

    override fun parseWrite(ar1: String, ar2: String, ar3: String, ar4: String, ar5: String, player: Player) {
        if (ar1.equals("mail", ignoreCase = true)) {
            MailBBSManager.sendMail(ar2, ar4, ar5, player)
            showFriendsList(player, false)
        } else
            super.parseWrite(ar1, ar2, ar3, ar4, ar5, player)
    }

    override val folder: String get() = "friend/"

    private const val FRIENDLIST_DELETE_BUTTON =
        "<br>\n<table><tr><td width=10></td><td>Are you sure you want to delete all friends from your Friends List?</td><td width=20></td><td><button value=\"OK\" action=\"bypass _friend;delall\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td></tr></table>"
    private const val BLOCKLIST_DELETE_BUTTON =
        "<br>\n<table><tr><td width=10></td><td>Are you sure you want to delete all players from your Block List?</td><td width=20></td><td><button value=\"OK\" action=\"bypass _block;delall\" back=\"l2ui_ch3.smallbutton2_down\" width=65 height=20 fore=\"l2ui_ch3.smallbutton2\"></td></tr></table>"

    private const val DELETE_ALL_FRIENDS = "DELETE FROM character_friends WHERE char_id = ? OR friend_id = ?"
    private const val DELETE_FRIEND =
        "DELETE FROM character_friends WHERE (char_id = ? AND friend_id = ?) OR (char_id = ? AND friend_id = ?)"

    private fun showFriendsList(player: Player, delMsg: Boolean) {
        var content: String? =
            HtmCache.getHtm(BaseBBSManager.CB_PATH + "friend/friend-list.htm") ?: return

        // Retrieve player's friendlist and selected
        val list = player.friendList
        val selectedList = player.selectedFriendList

        val sb = StringBuilder()

        // Friendlist
        for (id in list) {
            if (selectedList.contains(id))
                continue

            val friendName = PlayerInfoTable.getPlayerName(id!!) ?: continue

            val friend = World.getInstance().getPlayer(id)
            StringUtil.append(
                sb,
                "<a action=\"bypass _friend;select;",
                id,
                "\">[Select]</a>&nbsp;",
                friendName,
                " ",
                if (friend != null && friend.isOnline) "(on)" else "(off)",
                "<br1>"
            )
        }
        content = content!!.replace("%friendslist%", sb.toString())

        // Cleanup sb.
        sb.setLength(0)

        // Selected friendlist
        for (id in selectedList) {
            val friendName = PlayerInfoTable.getPlayerName(id!!) ?: continue

            val friend = World.getInstance().getPlayer(id)
            StringUtil.append(
                sb,
                "<a action=\"bypass _friend;deselect;",
                id,
                "\">[Deselect]</a>&nbsp;",
                friendName,
                " ",
                if (friend != null && friend.isOnline) "(on)" else "(off)",
                "<br1>"
            )
        }
        content = content.replace("%selectedFriendsList%", sb.toString())

        // Delete button.
        content = content.replace("%deleteMSG%", if (delMsg) FRIENDLIST_DELETE_BUTTON else "")

        BaseBBSManager.separateAndSend(content, player)
    }

    private fun showBlockList(player: Player, delMsg: Boolean) {
        var content: String? =
            HtmCache.getHtm(BaseBBSManager.CB_PATH + "friend/friend-blocklist.htm") ?: return

        // Retrieve player's blocklist and selected
        val list = player.blockList.blockList
        val selectedList = player.selectedBlocksList

        val sb = StringBuilder()

        // Blocklist
        for (id in list!!) {
            if (selectedList.contains(id))
                continue

            val blockName = PlayerInfoTable.getPlayerName(id) ?: continue

            val block = World.getInstance().getPlayer(id)
            StringUtil.append(
                sb,
                "<a action=\"bypass _block;select;",
                id,
                "\">[Select]</a>&nbsp;",
                blockName,
                " ",
                if (block != null && block.isOnline) "(on)" else "(off)",
                "<br1>"
            )
        }
        content = content!!.replace("%blocklist%", sb.toString())

        // Cleanup sb.
        sb.setLength(0)

        // Selected Blocklist
        for (id in selectedList) {
            val blockName = PlayerInfoTable.getPlayerName(id!!) ?: continue

            val block = World.getInstance().getPlayer(id)
            StringUtil.append(
                sb,
                "<a action=\"bypass _block;deselect;",
                id,
                "\">[Deselect]</a>&nbsp;",
                blockName,
                " ",
                if (block != null && block.isOnline) "(on)" else "(off)",
                "<br1>"
            )
        }
        content = content.replace("%selectedBlocksList%", sb.toString())

        // Delete button.
        content = content.replace("%deleteMSG%", if (delMsg) BLOCKLIST_DELETE_BUTTON else "")

        BaseBBSManager.separateAndSend(content, player)
    }

    fun showMailWrite(player: Player) {
        var content: String? =
            HtmCache.getHtm(BaseBBSManager.CB_PATH + "friend/friend-mail.htm") ?: return

        val sb = StringBuilder()
        for (id in player.selectedFriendList) {
            val friendName = PlayerInfoTable.getPlayerName(id) ?: continue

            if (sb.isNotEmpty())
                sb.append(";")

            sb.append(friendName)
        }

        content = content!!.replace("%list%", sb.toString())

        BaseBBSManager.separateAndSend(content, player)
    }
}