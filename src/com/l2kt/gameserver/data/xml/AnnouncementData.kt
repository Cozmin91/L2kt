package com.l2kt.gameserver.data.xml

import com.l2kt.commons.data.xml.IXmlReader
import com.l2kt.commons.lang.StringUtil
import com.l2kt.gameserver.data.cache.HtmCache
import com.l2kt.gameserver.data.xml.AnnouncementData.forEach
import com.l2kt.gameserver.extensions.announceToOnlinePlayers
import com.l2kt.gameserver.model.Announcement
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.clientpackets.Say2
import com.l2kt.gameserver.network.serverpackets.CreatureSay
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage
import org.w3c.dom.Document
import java.io.File
import java.io.FileWriter
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

/**
 * This class loads and stores [Announcement]s, the key being dynamically generated on loading.<br></br>
 * As the storage is a XML, the whole XML needs to be regenerated on Announcement addition/deletion.
 */
object AnnouncementData : IXmlReader {

    private val _announcements = ConcurrentHashMap<Int, Announcement>()
    private const val HEADER =
        "<?xml version='1.0' encoding='utf-8'?> \n<!-- \n@param String message - the message to be announced \n@param Boolean critical - type of announcement (true = critical,false = normal) \n@param Boolean auto - when the announcement will be displayed (true = auto,false = on player login) \n@param Integer initial_delay - time delay for the first announce (used only if auto=true;value in seconds) \n@param Integer delay - time delay for the announces following the first announce (used only if auto=true;value in seconds) \n@param Integer limit - limit of announces (used only if auto=true, 0 = unlimited) \n--> \n"

    init {
        load()
    }

    override fun load() {
        parseFile("./data/xml/announcements.xml")
        IXmlReader.LOGGER.info("Loaded {} announcements.", _announcements.size)
    }

    override fun parseDocument(doc: Document, path: Path) {
        forEach(doc, "list") { listNode ->
            forEach(listNode, "announcement") { announcementNode ->
                val attrs = announcementNode.attributes
                val message = parseString(attrs, "message")
                if (message == null || message.isEmpty()) {
                    IXmlReader.LOGGER.warn("The message is empty on an announcement. Ignoring it.")
                    return@forEach
                }

                val critical = parseBoolean(attrs, "critical", false)!!
                val auto = parseBoolean(attrs, "auto", false)!!
                if (auto) {
                    val initialDelay = parseInteger(attrs, "initial_delay")!!
                    val delay = parseInteger(attrs, "delay")!!
                    val limit = Math.max(parseInteger(attrs, "limit")!!, 0)
                    _announcements[_announcements.size] =
                            Announcement(message, critical, auto, initialDelay, delay, limit)
                } else
                    _announcements[_announcements.size] = Announcement(message, critical)
            }
        }
    }

    fun reload() {
        // Clean first tasks from automatic announcements.
        for (announce in _announcements.values)
            announce.stopTask()

        load()
    }

    /**
     * Send stored [Announcement]s from _announcements Map to a specific [Player].
     * @param player : The Player to send infos.
     * @param autoOrNot : If true, sends only automatic announcements, otherwise send classic ones.
     */
    fun showAnnouncements(player: Player, autoOrNot: Boolean) {
        for (announce in _announcements.values) {
            if (autoOrNot)
                announce.reloadTask()
            else {
                if (announce.isAuto)
                    continue

                player.sendPacket(
                    CreatureSay(
                        0,
                        if (announce.isCritical) Say2.CRITICAL_ANNOUNCE else Say2.ANNOUNCEMENT,
                        player.name,
                        announce.message
                    )
                )
            }
        }
    }

    /**
     * Use [BroadcastExtensionsKt].announceToOnlinePlayers(String, Boolean) in order to send announcement, wrapped into a ioobe try/catch.
     * @param command : The command to handle.
     * @param lengthToTrim : The length to trim, in order to send only the message without the command.
     * @param critical : Is the message critical or not.
     */
    fun handleAnnounce(command: String, lengthToTrim: Int, critical: Boolean) {
        try {
            command.substring(lengthToTrim).announceToOnlinePlayers(critical)
        } catch (e: StringIndexOutOfBoundsException) {
        }

    }

    /**
     * Send a static HTM with dynamic announcements content took from _announcements Map to a [Player].
     * @param player : The Player to send the [NpcHtmlMessage] packet.
     */
    fun listAnnouncements(player: Player) {
        val sb = StringBuilder("<br>")
        if (_announcements.isEmpty())
            sb.append("<tr><td>The XML file doesn't contain any content.</td></tr>")
        else {
            for ((index, announce) in _announcements) {

                StringUtil.append(
                    sb,
                    "<tr><td width=240>#",
                    index,
                    " - ",
                    announce.message,
                    "</td><td></td></tr><tr><td>Critical: ",
                    announce.isCritical,
                    " | Auto: ",
                    announce.isAuto,
                    "</td><td><button value=\"Delete\" action=\"bypass -h admin_announce del ",
                    index,
                    "\" width=65 height=19 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\"></td></tr>"
                )
            }
        }

        val html = NpcHtmlMessage(0)
        html.setHtml(HtmCache.getHtmForce("data/html/admin/announce_list.htm"))
        html.replace("%announces%", sb.toString())
        player.sendPacket(html)
    }

    /**
     * Add an [Announcement] but only if the message isn't empty or null. Regenerate the XML.
     * @param message : The String to announce.
     * @param critical : Is it a critical announcement or not.
     * @param auto : Is it using a specific task or not.
     * @param initialDelay : Initial delay of the task, used only if auto is setted to True.
     * @param delay : Delay of the task, used only if auto is setted to True.
     * @param limit : Maximum amount of loops the task will do before ending.
     * @return true if the announcement has been successfully added, false otherwise.
     */
    fun addAnnouncement(
        message: String,
        critical: Boolean,
        auto: Boolean,
        initialDelay: Int,
        delay: Int,
        limit: Int
    ): Boolean {
        if (message.isEmpty())
            return false

        if (auto)
            _announcements[_announcements.size] = Announcement(message, critical, auto, initialDelay, delay, limit)
        else
            _announcements[_announcements.size] = Announcement(message, critical)

        regenerateXML()
        return true
    }

    /**
     * End the task linked to an [Announcement] and delete it.
     * @param index : The Map index to remove.
     */
    fun delAnnouncement(index: Int) {
        _announcements.remove(index)?.stopTask()
        regenerateXML()
    }

    private fun regenerateXML() {
        val sb = StringBuilder(HEADER)

        sb.append("<list> \n")

        for (announce in _announcements.values)
            StringUtil.append(
                sb,
                "<announcement message=\"",
                announce.message,
                "\" critical=\"",
                announce.isCritical,
                "\" auto=\"",
                announce.isAuto,
                "\" initial_delay=\"",
                announce.initialDelay,
                "\" delay=\"",
                announce.delay,
                "\" limit=\"",
                announce.limit,
                "\" /> \n"
            )

        sb.append("</list>")

        try {
            FileWriter(File("./data/xml/announcements.xml")).use { fw -> fw.write(sb.toString()) }
        } catch (e: Exception) {
            IXmlReader.LOGGER.error("Error regenerating XML.", e)
        }
    }
}