package com.l2kt.gameserver.communitybbs.Manager

import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.lang.StringUtil
import com.l2kt.gameserver.data.cache.HtmCache
import com.l2kt.gameserver.data.sql.PlayerInfoTable
import com.l2kt.gameserver.model.BlockList
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ExMailArrived
import com.l2kt.gameserver.network.serverpackets.PlaySound
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object MailBBSManager : BaseBBSManager() {

    private val _mails = ConcurrentHashMap<Int, Set<Mail>>()

    private var _lastid = 0

    private val newMailId: Int
        @Synchronized get() = ++_lastid

    enum class MailType private constructor(val description: String, val bypass: String) {
        INBOX("Inbox", "<a action=\"bypass _bbsmail\">Inbox</a>"),
        SENTBOX("Sent Box", "<a action=\"bypass _bbsmail;sentbox\">Sent Box</a>"),
        ARCHIVE("Mail Archive", "<a action=\"bypass _bbsmail;archive\">Mail Archive</a>"),
        TEMPARCHIVE("Temporary Mail Archive", "<a action=\"bypass _bbsmail;temp_archive\">Temporary Mail Archive</a>");


        companion object {

            val VALUES = values()
        }
    }

    class Mail {
        internal var charId: Int = 0
        internal var mailId: Int = 0
        internal var senderId: Int = 0
        internal var location: MailType? = null
        internal var recipientNames: String? = null
        internal var subject: String? = null
        internal var message: String? = null
        internal var sentDate: Timestamp? = null
        internal var sentDateString: String? = null
        internal var unread: Boolean = false
    }

    init {
        initId()
    }

    override fun parseCmd(command: String, player: Player) {
        if (command == "_bbsmail" || command == "_maillist_0_1_0_")
            showMailList(player, 1, MailType.INBOX)
        else if (command.startsWith("_bbsmail")) {
            val st = StringTokenizer(command, ";")
            st.nextToken()

            val action = st.nextToken()

            if (action == "inbox" || action == "sentbox" || action == "archive" || action == "temparchive") {
                val page = if (st.hasMoreTokens()) Integer.parseInt(st.nextToken()) else 1
                val sType = if (st.hasMoreTokens()) st.nextToken() else ""
                val search = if (st.hasMoreTokens()) st.nextToken() else ""

                showMailList(
                    player,
                    page,
                    MailType.valueOf(action.toUpperCase()),
                    sType,
                    search
                )
            } else if (action == "crea")
                showWriteView(player)
            else {
                // Retrieve the mail based on its id (written as part of the command). If invalid, return to last known forum.
                val mail = getMail(player, if (st.hasMoreTokens()) Integer.parseInt(st.nextToken()) else -1)
                if (mail == null) {
                    showLastForum(player)
                    return
                }

                if (action == "view") {
                    showMailView(player, mail)
                    if (mail.unread)
                        setMailToRead(player, mail.mailId)
                } else if (action == "reply")
                    showWriteView(player, mail)
                else if (action == "del") {
                    deleteMail(player, mail.mailId)
                    showLastForum(player)
                } else if (action == "store") {
                    setMailLocation(player, mail.mailId, MailType.ARCHIVE)
                    showMailList(player, 1, MailType.ARCHIVE)
                }
            }
        } else
            super.parseCmd(command, player)
    }

    override fun parseWrite(ar1: String, ar2: String, ar3: String, ar4: String, ar5: String, player: Player) {
        if (ar1 == "Send") {
            sendMail(ar3, ar4, ar5, player)
            showMailList(player, 1, MailType.SENTBOX)
        } else if (ar1.startsWith("Search")) {
            val st = StringTokenizer(ar1, ";")
            st.nextToken()

            showMailList(
                player,
                1,
                MailType.valueOf(st.nextToken().toUpperCase()),
                ar4,
                ar5
            )
        } else
            super.parseWrite(ar1, ar2, ar3, ar4, ar5, player)
    }

    private fun initId() {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(SELECT_LAST_ID).use { ps ->
                    ps.executeQuery().use { rs ->
                        while (rs.next()) {
                            if (rs.getInt(1) > _lastid)
                                _lastid = rs.getInt(1)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            BaseBBSManager.LOGGER.error("Couldn't find the last mail id.", e)
        }

    }

    private fun getPlayerMails(objectId: Int): MutableSet<Mail> {
        var mails: MutableSet<Mail>? = _mails[objectId] as MutableSet<Mail>?
        if (mails == null) {
            mails = ConcurrentHashMap.newKeySet()

            try {
                L2DatabaseFactory.connection.use { con ->
                    con.prepareStatement(SELECT_CHAR_MAILS).use { ps ->
                        ps.setInt(1, objectId)

                        ps.executeQuery().use { rs ->
                            while (rs.next()) {
                                val mail = Mail()
                                mail.charId = rs.getInt("charId")
                                mail.mailId = rs.getInt("letterId")
                                mail.senderId = rs.getInt("senderId")
                                mail.location =
                                        MailType.valueOf(rs.getString("location").toUpperCase())
                                mail.recipientNames = rs.getString("recipientNames")
                                mail.subject = rs.getString("subject")
                                mail.message = rs.getString("message")
                                mail.sentDate = rs.getTimestamp("sentDate")
                                mail.sentDateString = SimpleDateFormat("yyyy-MM-dd HH:mm").format(mail.sentDate)
                                mail.unread = rs.getInt("unread") != 0
                                mails!!.add(mail)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                BaseBBSManager.LOGGER.error("Couldn't load mail for player id : {}.", e, objectId)
            }

            _mails[objectId] = mails
        }
        return mails ?: mutableSetOf()
    }

    private fun getMail(player: Player, mailId: Int): Mail? {
        return getPlayerMails(player.objectId).stream().filter { l -> l.mailId == mailId }.findFirst().orElse(null)
    }

    fun checkUnreadMail(player: Player): Int {
        return getPlayerMails(player.objectId).stream().filter { l -> l.unread }.count().toInt()
    }

    private fun showMailList(player: Player, page: Int, type: MailType, sType: String = "", search: String = "") {
        var page = page
        val mails: MutableSet<Mail>
        if (sType != "" && search != "") {
            mails = ConcurrentHashMap.newKeySet()

            val byTitle = sType.equals("title", ignoreCase = true)

            for (mail in getPlayerMails(player.objectId)) {
                if (byTitle && mail.subject!!.toLowerCase().contains(search.toLowerCase()))
                    mails.add(mail)
                else if (!byTitle) {
                    val writer = getPlayerName(mail.senderId)
                    if (writer.toLowerCase().contains(search.toLowerCase()))
                        mails.add(mail)
                }
            }
        } else
            mails = getPlayerMails(player.objectId)

        val countMails = getMailCount(player.objectId, type, sType, search)
        val maxpage = getPagesCount(countMails)

        if (page > maxpage)
            page = maxpage
        if (page < 1)
            page = 1

        player.mailPosition = page
        var index = 0
        var minIndex = 0
        var maxIndex = 0
        maxIndex = if (page == 1) page * 9 else page * 10 - 1
        minIndex = maxIndex - 9

        var content = HtmCache.getHtm(BaseBBSManager.CB_PATH + "mail/mail.htm")
        content = content!!.replace("%inbox%", Integer.toString(getMailCount(player.objectId, MailType.INBOX, "", "")))
        content =
                content.replace("%sentbox%", Integer.toString(getMailCount(player.objectId, MailType.SENTBOX, "", "")))
        content =
                content.replace("%archive%", Integer.toString(getMailCount(player.objectId, MailType.ARCHIVE, "", "")))
        content = content.replace(
            "%temparchive%",
            Integer.toString(getMailCount(player.objectId, MailType.TEMPARCHIVE, "", ""))
        )
        content = content.replace("%type%", type.description)
        content = content.replace("%htype%", type.toString().toLowerCase())

        val sb = StringBuilder()
        for (mail in mails) {
            if (mail.location == type) {
                if (index < minIndex) {
                    index++
                    continue
                }

                if (index > maxIndex)
                    break

                StringUtil.append(
                    sb,
                    "<table width=610><tr><td width=5></td><td width=150>",
                    getPlayerName(mail.senderId),
                    "</td><td width=300><a action=\"bypass _bbsmail;view;",
                    mail.mailId,
                    "\">"
                )

                if (mail.unread)
                    sb.append("<font color=\"LEVEL\">")

                sb.append(abbreviate(mail.subject!!, 51))

                if (mail.unread)
                    sb.append("</font>")

                StringUtil.append(
                    sb,
                    "</a></td><td width=150>",
                    mail.sentDateString ?: "",
                    "</td><td width=5></td></tr></table><img src=\"L2UI.Squaregray\" width=610 height=1>"
                )
                index++
            }
        }
        content = content.replace("%maillist%", sb.toString())

        // CLeanup sb.
        sb.setLength(0)

        val fullSearch = if (sType != "" && search != "") ";$sType;$search" else ""

        StringUtil.append(
            sb,
            "<td><table><tr><td></td></tr><tr><td><button action=\"bypass _bbsmail;",
            type,
            ";",
            if (page == 1) page else page - 1,
            fullSearch,
            "\" back=\"l2ui_ch3.prev1_down\" fore=\"l2ui_ch3.prev1\" width=16 height=16></td></tr></table></td>"
        )

        var i = 0
        if (maxpage > 21) {
            if (page <= 11) {
                i = 1
                while (i <= 10 + page) {
                    if (i == page)
                        StringUtil.append(sb, "<td> ", i, " </td>")
                    else
                        StringUtil.append(
                            sb,
                            "<td><a action=\"bypass _bbsmail;",
                            type,
                            ";",
                            i,
                            fullSearch,
                            "\"> ",
                            i,
                            " </a></td>"
                        )
                    i++
                }
            } else if (page > 11 && maxpage - page > 10) {
                i = page - 10
                while (i <= page - 1) {
                    if (i == page) {
                        i++
                        continue
                    }

                    StringUtil.append(
                        sb,
                        "<td><a action=\"bypass _bbsmail;",
                        type,
                        ";",
                        i,
                        fullSearch,
                        "\"> ",
                        i,
                        " </a></td>"
                    )
                    i++
                }
                i = page
                while (i <= page + 10) {
                    if (i == page)
                        StringUtil.append(sb, "<td> ", i, " </td>")
                    else
                        StringUtil.append(
                            sb,
                            "<td><a action=\"bypass _bbsmail;",
                            type,
                            ";",
                            i,
                            fullSearch,
                            "\"> ",
                            i,
                            " </a></td>"
                        )
                    i++
                }
            } else if (maxpage - page <= 10) {
                i = page - 10
                while (i <= maxpage) {
                    if (i == page)
                        StringUtil.append(sb, "<td> ", i, " </td>")
                    else
                        StringUtil.append(
                            sb,
                            "<td><a action=\"bypass _bbsmail;",
                            type,
                            ";",
                            i,
                            fullSearch,
                            "\"> ",
                            i,
                            " </a></td>"
                        )
                    i++
                }
            }
        } else {
            i = 1
            while (i <= maxpage) {
                if (i == page)
                    StringUtil.append(sb, "<td> ", i, " </td>")
                else
                    StringUtil.append(
                        sb,
                        "<td><a action=\"bypass _bbsmail;",
                        type,
                        ";",
                        i,
                        fullSearch,
                        "\"> ",
                        i,
                        " </a></td>"
                    )
                i++
            }
        }
        StringUtil.append(
            sb,
            "<td><table><tr><td></td></tr><tr><td><button action=\"bypass _bbsmail;",
            type,
            ";",
            if (page == maxpage) page else page + 1,
            fullSearch,
            "\" back=\"l2ui_ch3.next1_down\" fore=\"l2ui_ch3.next1\" width=16 height=16 ></td></tr></table></td>"
        )

        content = content.replace("%maillistlength%", sb.toString())

        BaseBBSManager.separateAndSend(content, player)
    }

    private fun showMailView(player: Player, mail: Mail?) {
        if (mail == null) {
            showMailList(player, 1, MailType.INBOX)
            return
        }

        var content = HtmCache.getHtm(BaseBBSManager.CB_PATH + "mail/mail-show.htm")

        val link = mail.location!!.bypass + "&nbsp;&gt;&nbsp;" + mail.subject
        content = content!!.replace("%maillink%", link)

        content = content.replace("%writer%", getPlayerName(mail.senderId))
        content = content.replace("%sentDate%", mail.sentDateString!!)
        content = content.replace("%receiver%", mail.recipientNames!!)
        content = content.replace("%delDate%", "Unknown")
        content = content.replace(
            "%title%",
            mail.subject!!.replace("<", "&lt;").replace(">", "&gt;").replace(
                "\"",
                "&quot;"
            )
        )
        content = content.replace(
            "%mes%",
            mail.message!!.replace("\r\n", "<br>").replace("<", "&lt;").replace(
                ">",
                "&gt;"
            ).replace("\"", "&quot;")
        )
        content = content.replace("%letterId%", mail.mailId.toString() + "")

        BaseBBSManager.separateAndSend(content, player)
    }

    fun sendMail(recipients: String, subject: String?, message: String, player: Player) {
        var subject = subject
        var message = message
        // Current time.
        val currentDate = Calendar.getInstance().timeInMillis

        // Get the current time - 1 day under timestamp format.
        val ts = Timestamp(currentDate - 86400000L)

        // Check sender mails based on previous timestamp. If more than 10 mails have been found for today, then cancel the use.
        if (getPlayerMails(player.objectId).stream().filter { l -> l.sentDate!!.after(ts) && l.location == MailType.SENTBOX }.count() >= 10) {
            player.sendPacket(SystemMessageId.NO_MORE_MESSAGES_TODAY)
            return
        }

        // Format recipient names. If more than 5 are found, cancel the mail.
        val recipientNames =
            recipients.trim { it <= ' ' }.split(";").dropLastWhile { it.isEmpty() }.toTypedArray()
        if (recipientNames.size > 5 && !player.isGM) {
            player.sendPacket(SystemMessageId.ONLY_FIVE_RECIPIENTS)
            return
        }

        // Edit subject, if none.
        if (subject == null || subject.isEmpty())
            subject = "(no subject)"

        // Edit message.
        message = message.replace("\n", "<br1>")

        try {
            L2DatabaseFactory.connection.use { con ->
                // Get the current time under timestamp format.
                val time = Timestamp(currentDate)

                val ps = con.prepareStatement(INSERT_NEW_MAIL)
                ps.setInt(3, player.objectId)
                ps.setString(4, "inbox")
                ps.setString(5, recipients)
                ps.setString(6, abbreviate(subject, 128))
                ps.setString(7, message)
                ps.setTimestamp(8, time)
                ps.setInt(9, 1)

                for (recipientName in recipientNames) {
                    // Recipient is an invalid player, or is the sender.
                    val recipientId = PlayerInfoTable.getPlayerObjectId(recipientName)
                    if (recipientId <= 0 || recipientId == player.objectId) {
                        player.sendPacket(SystemMessageId.INCORRECT_TARGET)
                        continue
                    }

                    val recipientPlayer = World.getInstance().getPlayer(recipientId)

                    if (!player.isGM) {
                        // Sender is a regular player, while recipient is a GM.
                        if (isGM(recipientId)) {
                            player.sendPacket(
                                SystemMessage.getSystemMessage(SystemMessageId.CANNOT_MAIL_GM_S1).addString(
                                    recipientName
                                )
                            )
                            continue
                        }

                        // The recipient is on block mode.
                        if (isBlocked(player, recipientId)) {
                            player.sendPacket(
                                SystemMessage.getSystemMessage(SystemMessageId.S1_BLOCKED_YOU_CANNOT_MAIL).addString(
                                    recipientName
                                )
                            )
                            continue
                        }

                        // The recipient box is already full.
                        if (isInboxFull(recipientId)) {
                            player.sendPacket(SystemMessageId.MESSAGE_NOT_SENT)
                            recipientPlayer?.sendPacket(SystemMessageId.MAILBOX_FULL)

                            continue
                        }
                    }

                    val id = newMailId

                    ps.setInt(1, recipientId)
                    ps.setInt(2, id)
                    ps.addBatch()

                    val mail = Mail()
                    mail.charId = recipientId
                    mail.mailId = id
                    mail.senderId = player.objectId
                    mail.location = MailType.INBOX
                    mail.recipientNames = recipients
                    mail.subject = abbreviate(subject, 128)
                    mail.message = message
                    mail.sentDate = time
                    mail.sentDateString = SimpleDateFormat("yyyy-MM-dd HH:mm").format(mail.sentDate)
                    mail.unread = true

                    getPlayerMails(recipientId).add(mail)

                    if (recipientPlayer != null) {
                        recipientPlayer.sendPacket(SystemMessageId.NEW_MAIL)
                        recipientPlayer.sendPacket(PlaySound("systemmsg_e.1233"))
                        recipientPlayer.sendPacket(ExMailArrived.STATIC_PACKET)
                    }
                }

                // Create a copy into player's sent box, if at least one recipient has been reached.
                val result = ps.executeBatch()
                if (result.size > 0) {
                    val id = newMailId

                    ps.setInt(1, player.objectId)
                    ps.setInt(2, id)
                    ps.setString(4, "sentbox")
                    ps.setInt(9, 0)
                    ps.execute()

                    val mail = Mail()
                    mail.charId = player.objectId
                    mail.mailId = id
                    mail.senderId = player.objectId
                    mail.location = MailType.SENTBOX
                    mail.recipientNames = recipients
                    mail.subject = abbreviate(subject, 128)
                    mail.message = message
                    mail.sentDate = time
                    mail.sentDateString = SimpleDateFormat("yyyy-MM-dd HH:mm").format(mail.sentDate)
                    mail.unread = false

                    getPlayerMails(player.objectId).add(mail)

                    player.sendPacket(SystemMessageId.SENT_MAIL)
                }
                ps.close()
            }
        } catch (e: Exception) {
            BaseBBSManager.LOGGER.error("Couldn't send mail for {}.", e, player.name)
        }

    }

    private fun getMailCount(objectId: Int, location: MailType, type: String, search: String): Int {
        var count = 0
        if (type != "" && search != "") {
            val byTitle = type.equals("title", ignoreCase = true)
            for (mail in getPlayerMails(objectId)) {
                if (mail.location != location)
                    continue

                if (byTitle && mail.subject!!.toLowerCase().contains(search.toLowerCase()))
                    count++
                else if (!byTitle) {
                    val writer = getPlayerName(mail.senderId)
                    if (writer.toLowerCase().contains(search.toLowerCase()))
                        count++
                }
            }
        } else {
            for (mail in getPlayerMails(objectId)) {
                if (mail.location == location)
                    count++
            }
        }
        return count
    }

    private fun deleteMail(player: Player, mailId: Int) {
        for (mail in getPlayerMails(player.objectId)) {
            if (mail.mailId == mailId) {
                getPlayerMails(player.objectId).remove(mail)
                break
            }
        }

        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(DELETE_MAIL).use { ps ->
                    ps.setInt(1, mailId)
                    ps.execute()
                }
            }
        } catch (e: Exception) {
            BaseBBSManager.LOGGER.error("Couldn't delete mail #{}.", e, mailId)
        }

    }

    private fun setMailToRead(player: Player, mailId: Int) {
        getMail(player, mailId)!!.unread = false

        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(MARK_MAIL_READ).use { ps ->
                    ps.setInt(1, mailId)
                    ps.execute()
                }
            }
        } catch (e: Exception) {
            BaseBBSManager.LOGGER.error("Couldn't set read status for mail #{}.", e, mailId)
        }

    }

    private fun setMailLocation(player: Player, mailId: Int, location: MailType) {
        getMail(player, mailId)!!.location = location

        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(SET_MAIL_LOC).use { ps ->
                    ps.setString(1, location.toString().toLowerCase())
                    ps.setInt(2, mailId)
                    ps.execute()
                }
            }
        } catch (e: Exception) {
            BaseBBSManager.LOGGER.error("Couldn't set mail #{} location.", e, mailId)
        }

    }

    private fun isInboxFull(objectId: Int): Boolean {
        return getMailCount(objectId, MailType.INBOX, "", "") >= 100
    }

    private fun showLastForum(player: Player) {
        val page = player.mailPosition % 1000
        val type = player.mailPosition / 1000

        showMailList(player, page, MailType.VALUES[type])
    }

    private const val SELECT_CHAR_MAILS = "SELECT * FROM character_mail WHERE charId = ? ORDER BY letterId ASC"
    private const val INSERT_NEW_MAIL =
        "INSERT INTO character_mail (charId, letterId, senderId, location, recipientNames, subject, message, sentDate, unread) VALUES (?,?,?,?,?,?,?,?,?)"
    private const val DELETE_MAIL = "DELETE FROM character_mail WHERE letterId = ?"
    private const val MARK_MAIL_READ = "UPDATE character_mail SET unread = 0 WHERE letterId = ?"
    private const val SET_MAIL_LOC = "UPDATE character_mail SET location = ? WHERE letterId = ?"
    private const val SELECT_LAST_ID = "SELECT letterId FROM character_mail ORDER BY letterId DESC LIMIT 1"
    private const val GET_GM_STATUS = "SELECT accesslevel FROM characters WHERE obj_Id = ?"

    private fun abbreviate(s: String, maxWidth: Int): String {
        return if (s.length > maxWidth) s.substring(0, maxWidth) else s
    }

    private fun showWriteView(player: Player) {
        val content = HtmCache.getHtm(BaseBBSManager.CB_PATH + "mail/mail-write.htm")
        BaseBBSManager.separateAndSend(content, player)
    }

    private fun showWriteView(player: Player, mail: Mail) {
        var content = HtmCache.getHtm(BaseBBSManager.CB_PATH + "mail/mail-reply.htm")

        val link =
            mail.location!!.bypass + "&nbsp;&gt;&nbsp;<a action=\"bypass _bbsmail;view;" + mail.mailId + "\">" + mail.subject + "</a>&nbsp;&gt;&nbsp;"
        content = content!!.replace("%maillink%", link)

        content = content.replace("%recipients%", if (mail.senderId == player.objectId) mail.recipientNames ?: "" else getPlayerName(mail.senderId))
        content = content.replace("%letterId%", mail.mailId.toString() + "")
        BaseBBSManager.send1001(content, player)
        BaseBBSManager.send1002(player, " ", "Re: " + mail.subject!!, "0")
    }

    private fun isBlocked(player: Player, objectId: Int): Boolean {
        for (playerToTest in World.getInstance().players) {
            if (playerToTest.objectId == objectId) {
                return BlockList.isInBlockList(playerToTest, player)
            }
        }
        return false
    }

    private fun getPlayerName(objectId: Int): String {
        val name = PlayerInfoTable.getPlayerName(objectId)
        return name ?: "Unknown"
    }

    private fun isGM(objectId: Int): Boolean {
        var isGM = false

        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(GET_GM_STATUS).use { ps ->
                    ps.setInt(1, objectId)

                    ps.executeQuery().use { rs ->
                        if (rs.next())
                            isGM = rs.getInt(1) > 0
                    }
                }
            }
        } catch (e: Exception) {
            BaseBBSManager.LOGGER.error("Couldn't verify GM access for {}.", e, objectId)
        }

        return isGM
    }

    private fun getPagesCount(mailCount: Int): Int {
        if (mailCount < 1)
            return 1

        return if (mailCount % 10 == 0) mailCount / 10 else mailCount / 10 + 1

    }

}