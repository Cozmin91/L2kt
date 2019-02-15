package com.l2kt.gameserver.communitybbs.Manager

import com.l2kt.commons.lang.StringUtil
import com.l2kt.gameserver.data.cache.HtmCache
import com.l2kt.gameserver.data.sql.ClanTable
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import java.util.*

object ClanBBSManager : BaseBBSManager() {

    override fun parseCmd(command: String, player: Player) {
        if (command.equals("_bbsclan", ignoreCase = true)) {
            if (player.clan == null)
                sendClanList(player, 1)
            else
                sendClanDetails(player, player.clan.clanId)
        } else if (command.startsWith("_bbsclan")) {
            val st = StringTokenizer(command, ";")
            st.nextToken()

            val clanCommand = st.nextToken()
            if (clanCommand.equals("clan", ignoreCase = true))
                sendClanList(player, Integer.parseInt(st.nextToken()))
            else if (clanCommand.equals("home", ignoreCase = true))
                sendClanDetails(player, Integer.parseInt(st.nextToken()))
            else if (clanCommand.equals("mail", ignoreCase = true))
                sendClanMail(player, Integer.parseInt(st.nextToken()))
            else if (clanCommand.equals("management", ignoreCase = true))
                sendClanManagement(player, Integer.parseInt(st.nextToken()))
            else if (clanCommand.equals("notice", ignoreCase = true)) {
                if (st.hasMoreTokens()) {
                    val noticeCommand = st.nextToken()
                    if (!noticeCommand.isEmpty() && player.clan != null)
                        player.clan.setNoticeEnabledAndStore(java.lang.Boolean.parseBoolean(noticeCommand))
                }
                sendClanNotice(player, player.clanId)
            }
        } else
            super.parseCmd(command, player)
    }

    override fun parseWrite(ar1: String, ar2: String, ar3: String, ar4: String, ar5: String, player: Player) {
        if (ar1.equals("intro", ignoreCase = true)) {
            if (Integer.valueOf(ar2) != player.clanId)
                return

            val clan = ClanTable.getClan(player.clanId) ?: return

            clan.setIntroduction(ar3, true)
            sendClanManagement(player, Integer.valueOf(ar2))
        } else if (ar1 == "notice") {
            val clan = player.clan
            if (clan != null) {
                clan.setNoticeAndStore(ar4)
                sendClanNotice(player, player.clanId)
            }
        } else if (ar1.equals("mail", ignoreCase = true)) {
            if (Integer.valueOf(ar2) != player.clanId)
                return

            val clan = ClanTable.getClan(player.clanId) ?: return

            // Retrieve clans members, and store them under a String.
            val members = StringBuilder()

            for (member in clan.members) {
                if (members.length > 0)
                    members.append(";")

                members.append(member.name)
            }
            MailBBSManager.sendMail(members.toString(), ar4, ar5, player)
            sendClanDetails(player, player.clanId)
        } else
            super.parseWrite(ar1, ar2, ar3, ar4, ar5, player)
    }

    override val folder: String get() = "clan/"


    private fun sendClanMail(player: Player, clanId: Int) {
        val clan = ClanTable.getClan(clanId) ?: return

        if (player.clanId != clanId || !player.isClanLeader) {
            player.sendPacket(SystemMessageId.ONLY_THE_CLAN_LEADER_IS_ENABLED)
            sendClanList(player, 1)
            return
        }

        var content = HtmCache.getHtm(BaseBBSManager.CB_PATH + "clan/clanhome-mail.htm")
        content = content.replace("%clanid%", Integer.toString(clanId))
        content = content.replace("%clanName%", clan.name)
        BaseBBSManager.separateAndSend(content, player)
    }

    private fun sendClanManagement(player: Player, clanId: Int) {
        val clan = ClanTable.getClan(clanId) ?: return

        if (player.clanId != clanId || !player.isClanLeader) {
            player.sendPacket(SystemMessageId.ONLY_THE_CLAN_LEADER_IS_ENABLED)
            sendClanList(player, 1)
            return
        }

        var content = HtmCache.getHtm(BaseBBSManager.CB_PATH + "clan/clanhome-management.htm")
        content = content!!.replace("%clanid%", Integer.toString(clan.clanId))
        BaseBBSManager.send1001(content, player)
        BaseBBSManager.send1002(player, clan.introduction, "", "")
    }

    private fun sendClanNotice(player: Player, clanId: Int) {
        val clan = ClanTable.getClan(clanId)
        if (clan == null || player.clanId != clanId)
            return

        if (clan.level < 2) {
            player.sendPacket(SystemMessageId.NO_CB_IN_MY_CLAN)
            sendClanList(player, 1)
            return
        }

        var content = HtmCache.getHtm(BaseBBSManager.CB_PATH + "clan/clanhome-notice.htm")
        content = content.replace("%clanid%", Integer.toString(clan.clanId))
        content = content.replace("%enabled%", "[" + clan.isNoticeEnabled.toString() + "]")
        content = content.replace("%flag%", (!clan.isNoticeEnabled).toString())
        BaseBBSManager.send1001(content, player)
        BaseBBSManager.send1002(player, clan.notice, "", "")
    }

    private fun sendClanList(player: Player, index: Int) {
        var index = index
        var content = HtmCache.getHtm(BaseBBSManager.CB_PATH + "clan/clanlist.htm")

        // Player got a clan, show the associated header.
        val sb = StringBuilder()

        val clan = player.clan
        if (clan != null)
            StringUtil.append(
                sb,
                "<table width=610 bgcolor=A7A19A><tr><td width=5></td><td width=605><a action=\"bypass _bbsclan;home;",
                clan.clanId,
                "\">[GO TO MY CLAN]</a></td></tr></table>"
            )

        content = content.replace("%homebar%", sb.toString())

        if (index < 1)
            index = 1

        // Cleanup sb.
        sb.setLength(0)

        // List of clans.
        var i = 0
        for (cl in ClanTable.clans) {
            if (i > (index + 1) * 7)
                break

            if (i++ >= (index - 1) * 7)
                StringUtil.append(
                    sb,
                    "<table width=610><tr><td width=5></td><td width=150 align=center><a action=\"bypass _bbsclan;home;",
                    cl.clanId,
                    "\">",
                    cl.name,
                    "</a></td><td width=150 align=center>",
                    cl.leaderName,
                    "</td><td width=100 align=center>",
                    cl.level,
                    "</td><td width=200 align=center>",
                    cl.membersCount,
                    "</td><td width=5></td></tr></table><br1><img src=\"L2UI.Squaregray\" width=605 height=1><br1>"
                )
        }
        sb.append("<table><tr>")

        if (index == 1)
            sb.append("<td><button action=\"\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16></td>")
        else
            StringUtil.append(
                sb,
                "<td><button action=\"_bbsclan;clan;",
                index - 1,
                "\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16 ></td>"
            )

        var pageNumber = ClanTable.clans.size / 8
        if (pageNumber * 8 != ClanTable.clans.size)
            pageNumber++

        i = 1
        while (i <= pageNumber) {
            if (i == index)
                StringUtil.append(sb, "<td> ", i, " </td>")
            else
                StringUtil.append(sb, "<td><a action=\"bypass _bbsclan;clan;", i, "\"> ", i, " </a></td>")
            i++
        }

        if (index == pageNumber)
            sb.append("<td><button action=\"\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16></td>")
        else
            StringUtil.append(
                sb,
                "<td><button action=\"bypass _bbsclan;clan;",
                index + 1,
                "\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16 ></td>"
            )

        sb.append("</tr></table>")

        content = content.replace("%clanlist%", sb.toString())
        BaseBBSManager.separateAndSend(content, player)
    }

    private fun sendClanDetails(player: Player, clanId: Int) {
        val clan = ClanTable.getClan(clanId) ?: return

        if (clan.level < 2) {
            player.sendPacket(SystemMessageId.NO_CB_IN_MY_CLAN)
            sendClanList(player, 1)
            return
        }

        // Load different HTM following player case, 3 possibilites : randomer, member, clan leader.
        var content: String?
        when {
            player.clanId != clanId -> content = HtmCache.getHtm(BaseBBSManager.CB_PATH + "clan/clanhome.htm")
            player.isClanLeader -> content = HtmCache.getHtm(BaseBBSManager.CB_PATH + "clan/clanhome-leader.htm")
            else -> content = HtmCache.getHtm(BaseBBSManager.CB_PATH + "clan/clanhome-member.htm")
        }

        content = content.replace("%clanid%", Integer.toString(clan.clanId))
        content = content.replace("%clanIntro%", clan.introduction)
        content = content.replace("%clanName%", clan.name)
        content = content.replace("%clanLvL%", Integer.toString(clan.level))
        content = content.replace("%clanMembers%", Integer.toString(clan.membersCount))
        content = content.replace("%clanLeader%", clan.leaderName)
        content = content.replace("%allyName%", if (clan.allyId > 0) clan.allyName else "")
        BaseBBSManager.separateAndSend(content, player)
    }
}