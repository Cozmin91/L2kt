package com.l2kt.gameserver.data.manager

import com.l2kt.commons.lang.StringUtil
import com.l2kt.gameserver.data.xml.AdminData
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.petition.Petition
import com.l2kt.gameserver.model.petition.PetitionState
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.clientpackets.Say2
import com.l2kt.gameserver.network.serverpackets.CreatureSay
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import java.text.SimpleDateFormat
import java.util.concurrent.ConcurrentHashMap

/**
 * Store all existing [Petition]s, being pending or completed.
 */
object PetitionManager {
    private val _pendingPetitions = ConcurrentHashMap<Int, Petition>()
    val completedPetitions: Map<Int, Petition> = ConcurrentHashMap()

    val pendingPetitions: Map<Int, Petition>
        get() = _pendingPetitions

    val isPetitionInProcess: Boolean
        get() {
            for (petition in _pendingPetitions.values) {
                if (petition.state == PetitionState.IN_PROCESS)
                    return true
            }
            return false
        }

    fun getPlayerTotalPetitionCount(player: Player?): Int {
        if (player == null)
            return 0

        var petitionCount = 0

        for (petition in _pendingPetitions.values) {
            if (petition.petitioner != null && petition.petitioner.objectId == player.objectId)
                petitionCount++
        }

        for (petition in completedPetitions.values) {
            if (petition.petitioner != null && petition.petitioner.objectId == player.objectId)
                petitionCount++
        }

        return petitionCount
    }

    fun isPetitionInProcess(id: Int): Boolean {
        val petition = _pendingPetitions[id]
        return petition != null && petition.state == PetitionState.IN_PROCESS
    }

    fun isPlayerInConsultation(player: Player?): Boolean {
        if (player == null)
            return false

        for (petition in _pendingPetitions.values) {
            if (petition.state != PetitionState.IN_PROCESS)
                continue

            if (petition.petitioner != null && petition.petitioner.objectId == player.objectId || petition.responder != null && petition.responder.objectId == player.objectId)
                return true
        }
        return false
    }

    fun isPlayerPetitionPending(player: Player?): Boolean {
        if (player == null)
            return false

        for (petition in _pendingPetitions.values) {
            if (petition.petitioner != null && petition.petitioner.objectId == player.objectId)
                return true
        }
        return false
    }

    fun rejectPetition(player: Player, id: Int): Boolean {
        val petition = _pendingPetitions[id]
        if (petition == null || petition.responder != null)
            return false

        petition.responder = player
        return petition.endPetitionConsultation(PetitionState.RESPONDER_REJECT)
    }

    fun sendActivePetitionMessage(player: Player, messageText: String): Boolean {
        val cs: CreatureSay

        for (petition in _pendingPetitions.values) {
            if (petition.petitioner != null && petition.petitioner.objectId == player.objectId) {
                cs = CreatureSay(player.objectId, Say2.PETITION_PLAYER, player.name, messageText)
                petition.addLogMessage(cs)

                petition.sendResponderPacket(cs)
                petition.sendPetitionerPacket(cs)
                return true
            }

            if (petition.responder != null && petition.responder.objectId == player.objectId) {
                cs = CreatureSay(player.objectId, Say2.PETITION_GM, player.name, messageText)
                petition.addLogMessage(cs)

                petition.sendResponderPacket(cs)
                petition.sendPetitionerPacket(cs)
                return true
            }
        }

        return false
    }

    fun sendPendingPetitionList(player: Player) {
        val sdf = SimpleDateFormat("dd-MM-yyyy")
        val sb =
            StringBuilder("<html><body><center><font color=\"LEVEL\">Current Petitions</font><br><table width=\"300\">")

        if (_pendingPetitions.size == 0)
            sb.append("<tr><td colspan=\"4\">There are no currently pending petitions.</td></tr>")
        else
            sb.append("<tr><td></td><td><font color=\"999999\">Petitioner</font></td><td><font color=\"999999\">Petition Type</font></td><td><font color=\"999999\">Submitted</font></td></tr>")

        for (petition in _pendingPetitions.values) {
            sb.append("<tr><td>")

            if (petition.state != PetitionState.IN_PROCESS)
                StringUtil.append(
                    sb,
                    "<button value=\"View\" action=\"bypass -h admin_view_petition ",
                    petition.id,
                    "\" width=\"40\" height=\"15\" back=\"sek.cbui94\" fore=\"sek.cbui92\">"
                )
            else
                sb.append("<font color=\"999999\">In Process</font>")

            StringUtil.append(
                sb,
                "</td><td>",
                petition.petitioner.name,
                "</td><td>",
                petition.typeAsString,
                "</td><td>",
                sdf.format(petition.submitTime),
                "</td></tr>"
            )
        }

        sb.append("</table><br><button value=\"Refresh\" action=\"bypass -h admin_view_petitions\" width=\"50\" " + "height=\"15\" back=\"sek.cbui94\" fore=\"sek.cbui92\"><br><button value=\"Back\" action=\"bypass -h admin_admin\" " + "width=\"40\" height=\"15\" back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>")

        val html = NpcHtmlMessage(0)
        html.setHtml(sb.toString())
        player.sendPacket(html)
    }

    fun submitPetition(player: Player, content: String, type: Int): Int {
        // Create a new petition instance and add it to the list of pending petitions.
        val petition = Petition(player, content, type)

        _pendingPetitions[petition.id] = petition

        // Notify all GMs that a new petition has been submitted.
        AdminData.broadcastToGMs(
            CreatureSay(
                player.objectId,
                17,
                "Petition System",
                player.name + " has submitted a new petition."
            )
        )

        return petition.id
    }

    fun viewPetition(player: Player, id: Int) {
        if (!player.isGM)
            return

        val petition = _pendingPetitions[id] ?: return

        val sb = StringBuilder("<html><body>")
        sb.append("<center><br><font color=\"LEVEL\">Petition #" + petition.id + "</font><br1>")
        sb.append("<img src=\"L2UI.SquareGray\" width=\"200\" height=\"1\"></center><br>")
        sb.append("Submit Time: " + SimpleDateFormat("dd-MM-yyyy HH:mm").format(petition.submitTime) + "<br1>")
        sb.append("Petitioner: " + petition.petitioner.name + "<br1>")
        sb.append("Petition Type: " + petition.typeAsString + "<br>" + petition.content + "<br>")
        sb.append("<center><button value=\"Accept\" action=\"bypass -h admin_accept_petition " + petition.id + "\"" + "width=\"50\" height=\"15\" back=\"sek.cbui94\" fore=\"sek.cbui92\"><br1>")
        sb.append("<button value=\"Reject\" action=\"bypass -h admin_reject_petition " + petition.id + "\" " + "width=\"50\" height=\"15\" back=\"sek.cbui94\" fore=\"sek.cbui92\"><br>")
        sb.append("<button value=\"Back\" action=\"bypass -h admin_view_petitions\" width=\"40\" height=\"15\" back=\"sek.cbui94\" " + "fore=\"sek.cbui92\"></center>")
        sb.append("</body></html>")

        val html = NpcHtmlMessage(0)
        html.setHtml(sb.toString())
        player.sendPacket(html)
    }

    fun acceptPetition(player: Player, id: Int): Boolean {
        val petition = _pendingPetitions[id]
        if (petition == null || petition.responder != null)
            return false

        petition.responder = player
        petition.state = PetitionState.IN_PROCESS

        // Petition application accepted. (Send to Petitioner)
        petition.sendPetitionerPacket(SystemMessage.getSystemMessage(SystemMessageId.PETITION_APP_ACCEPTED))

        // Petition application accepted. Reciept No. is <ID>
        petition.sendResponderPacket(
            SystemMessage.getSystemMessage(SystemMessageId.PETITION_ACCEPTED_RECENT_NO_S1).addNumber(
                petition.id
            )
        )

        // Petition consultation with <Player> underway.
        petition.sendResponderPacket(
            SystemMessage.getSystemMessage(SystemMessageId.PETITION_WITH_S1_UNDER_WAY).addCharName(
                petition.petitioner
            )
        )
        return true
    }

    fun cancelActivePetition(player: Player): Boolean {
        for (currPetition in _pendingPetitions.values) {
            if (currPetition.petitioner != null && currPetition.petitioner.objectId == player.objectId)
                return currPetition.endPetitionConsultation(PetitionState.PETITIONER_CANCEL)

            if (currPetition.responder != null && currPetition.responder.objectId == player.objectId)
                return currPetition.endPetitionConsultation(PetitionState.RESPONDER_CANCEL)
        }

        return false
    }

    fun checkPetitionMessages(player: Player?) {
        if (player == null)
            return

        for (currPetition in _pendingPetitions.values) {
            if (currPetition.petitioner != null && currPetition.petitioner.objectId == player.objectId) {
                for (logMessage in currPetition.logMessages)
                    player.sendPacket(logMessage)

                return
            }
        }
    }

    fun endActivePetition(player: Player): Boolean {
        if (!player.isGM)
            return false

        for (currPetition in _pendingPetitions.values) {
            if (currPetition.responder != null && currPetition.responder.objectId == player.objectId)
                return currPetition.endPetitionConsultation(PetitionState.COMPLETED)
        }

        return false
    }
}