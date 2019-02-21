package com.l2kt.gameserver.model.actor.instance

import com.l2kt.Config
import com.l2kt.gameserver.data.manager.ZoneManager
import com.l2kt.gameserver.instancemanager.SevenSigns
import com.l2kt.gameserver.instancemanager.SevenSigns.CabalType
import com.l2kt.gameserver.instancemanager.SevenSignsFestival
import com.l2kt.gameserver.instancemanager.SevenSignsFestival.FestivalType
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.model.group.Party.MessageType
import com.l2kt.gameserver.model.zone.type.PeaceZone
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import java.util.*

class FestivalGuide(objectId: Int, template: NpcTemplate) : Folk(objectId, template) {
    protected lateinit var _festivalType: FestivalType
    protected lateinit var _festivalOracle: CabalType

    protected var _blueStonesNeeded: Int = 0
    protected var _greenStonesNeeded: Int = 0
    protected var _redStonesNeeded: Int = 0

    private val statsTable: String
        get() {
            val sb = StringBuilder()
            for (i in 0..4) {
                val dawnScore = SevenSignsFestival.getHighestScore(CabalType.DAWN, i)
                val duskScore = SevenSignsFestival.getHighestScore(CabalType.DUSK, i)

                var winningCabal = "Children of Dusk"
                if (dawnScore > duskScore)
                    winningCabal = "Children of Dawn"
                else if (dawnScore == duskScore)
                    winningCabal = "None"

                sb.append("<tr><td width=\"100\" align=\"center\">" + FestivalType.VALUES[i].festivalTypeName + "</td><td align=\"center\" width=\"35\">" + duskScore + "</td><td align=\"center\" width=\"35\">" + dawnScore + "</td><td align=\"center\" width=\"130\">" + winningCabal + "</td></tr>")
            }

            return sb.toString()
        }

    private val bonusTable: String
        get() {
            val sb = StringBuilder()
            for (i in 0..4)
                sb.append(
                    "<tr><td align=\"center\" width=\"150\">" + FestivalType.VALUES[i].festivalTypeName + "</td><td align=\"center\" width=\"150\">" + SevenSignsFestival.getAccumulatedBonus(
                        i
                    ) + "</td></tr>"
                )

            return sb.toString()
        }

    init {

        when (npcId) {
            31127, 31132 -> {
                _festivalType = FestivalType.MAX_31
                _festivalOracle = CabalType.DAWN
                _blueStonesNeeded = 900
                _greenStonesNeeded = 540
                _redStonesNeeded = 270
            }

            31128, 31133 -> {
                _festivalType = FestivalType.MAX_42
                _festivalOracle = CabalType.DAWN
                _blueStonesNeeded = 1500
                _greenStonesNeeded = 900
                _redStonesNeeded = 450
            }

            31129, 31134 -> {
                _festivalType = FestivalType.MAX_53
                _festivalOracle = CabalType.DAWN
                _blueStonesNeeded = 3000
                _greenStonesNeeded = 1800
                _redStonesNeeded = 900
            }

            31130, 31135 -> {
                _festivalType = FestivalType.MAX_64
                _festivalOracle = CabalType.DAWN
                _blueStonesNeeded = 4500
                _greenStonesNeeded = 2700
                _redStonesNeeded = 1350
            }

            31131, 31136 -> {
                _festivalType = FestivalType.MAX_NONE
                _festivalOracle = CabalType.DAWN
                _blueStonesNeeded = 6000
                _greenStonesNeeded = 3600
                _redStonesNeeded = 1800
            }

            31137, 31142 -> {
                _festivalType = FestivalType.MAX_31
                _festivalOracle = CabalType.DUSK
                _blueStonesNeeded = 900
                _greenStonesNeeded = 540
                _redStonesNeeded = 270
            }

            31138, 31143 -> {
                _festivalType = FestivalType.MAX_42
                _festivalOracle = CabalType.DUSK
                _blueStonesNeeded = 1500
                _greenStonesNeeded = 900
                _redStonesNeeded = 450
            }

            31139, 31144 -> {
                _festivalType = FestivalType.MAX_53
                _festivalOracle = CabalType.DUSK
                _blueStonesNeeded = 3000
                _greenStonesNeeded = 1800
                _redStonesNeeded = 900
            }

            31140, 31145 -> {
                _festivalType = FestivalType.MAX_64
                _festivalOracle = CabalType.DUSK
                _blueStonesNeeded = 4500
                _greenStonesNeeded = 2700
                _redStonesNeeded = 1350
            }

            31141, 31146 -> {
                _festivalType = FestivalType.MAX_NONE
                _festivalOracle = CabalType.DUSK
                _blueStonesNeeded = 6000
                _greenStonesNeeded = 3600
                _redStonesNeeded = 1800
            }
        }
    }

    override fun onBypassFeedback(player: Player, command: String) {
        if (command.startsWith("FestivalDesc")) {
            val `val` = Integer.parseInt(command.substring(13))

            showChatWindow(player, `val`, null, true)
        } else if (command.startsWith("Festival")) {
            val festivalIndex = _festivalType.ordinal

            val playerParty = player.party
            val `val` = Integer.parseInt(command.substring(9, 10))

            when (`val`) {
                1 // Become a Participant
                -> {
                    // Check if the festival period is active, if not then don't allow registration.
                    if (SevenSigns.isSealValidationPeriod) {
                        showChatWindow(player, 2, "a", false)
                        return
                    }

                    // Check if a festival is in progress, then don't allow registration yet.
                    if (SevenSignsFestival.isFestivalInitialized) {
                        player.sendMessage("You cannot sign up while a festival is in progress.")
                        return
                    }

                    // Check if the player is in a formed party already.
                    if (playerParty == null) {
                        showChatWindow(player, 2, "b", false)
                        return
                    }

                    // Check if the player is the party leader.
                    if (!playerParty.isLeader(player)) {
                        showChatWindow(player, 2, "c", false)
                        return
                    }

                    // Check to see if the party has at least 5 members.
                    if (playerParty.membersCount < Config.ALT_FESTIVAL_MIN_PLAYER) {
                        showChatWindow(player, 2, "b", false)
                        return
                    }

                    // Check if all the party members are in the required level range.
                    if (playerParty.level > _festivalType.maxLevel) {
                        showChatWindow(player, 2, "d", false)
                        return
                    }

                    // Check to see if the player has already signed up, if they are then update the participant list providing all the required criteria has been met.
                    if (player.isFestivalParticipant) {
                        SevenSignsFestival.setParticipants(_festivalOracle, festivalIndex, playerParty)
                        showChatWindow(player, 2, "f", false)
                        return
                    }

                    showChatWindow(player, 1, null, false)
                }
                2 // Festival 2 xxxx
                -> {
                    val stoneType = Integer.parseInt(command.substring(11))
                    var stonesNeeded = 0

                    when (stoneType) {
                        SevenSigns.SEAL_STONE_BLUE_ID -> stonesNeeded = _blueStonesNeeded
                        SevenSigns.SEAL_STONE_GREEN_ID -> stonesNeeded = _greenStonesNeeded
                        SevenSigns.SEAL_STONE_RED_ID -> stonesNeeded = _redStonesNeeded
                    }

                    if (!player.destroyItemByItemId("SevenSigns", stoneType, stonesNeeded, this, true))
                        return

                    SevenSignsFestival.setParticipants(_festivalOracle, festivalIndex, playerParty)
                    SevenSignsFestival.addAccumulatedBonus(festivalIndex, stoneType, stonesNeeded)

                    showChatWindow(player, 2, "e", false)
                }
                3 // Score Registration
                -> {
                    // Check if the festival period is active, if not then don't register the score.
                    if (SevenSigns.isSealValidationPeriod) {
                        showChatWindow(player, 3, "a", false)
                        return
                    }

                    // Check if a festival is in progress, if it is don't register the score.
                    if (SevenSignsFestival.isFestivalInProgress) {
                        player.sendMessage("You cannot register a score while a festival is in progress.")
                        return
                    }

                    // Check if the player is in a party.
                    if (playerParty == null) {
                        showChatWindow(player, 3, "b", false)
                        return
                    }

                    val prevParticipants = SevenSignsFestival.getPreviousParticipants(_festivalOracle, festivalIndex)

                    // Check if there are any past participants.
                    if (prevParticipants == null || prevParticipants.isEmpty() || !prevParticipants.contains(player.objectId)) {
                        showChatWindow(player, 3, "b", false)
                        return
                    }

                    // Check if this player was the party leader in the festival.
                    if (player.objectId != prevParticipants[0]) {
                        showChatWindow(player, 3, "b", false)
                        return
                    }

                    val bloodOfferings = player.inventory!!.getItemByItemId(SevenSignsFestival.FESTIVAL_OFFERING_ID)

                    // Check if the player collected any blood offerings during the festival.
                    if (bloodOfferings == null) {
                        player.sendMessage("You do not have any blood offerings to contribute.")
                        return
                    }

                    val offeringScore = bloodOfferings.count * SevenSignsFestival.FESTIVAL_OFFERING_VALUE
                    if (!player.destroyItem("SevenSigns", bloodOfferings, this, false))
                        return

                    val isHighestScore =
                        SevenSignsFestival.setFinalScore(player, _festivalOracle, _festivalType, offeringScore)

                    // Send message that the contribution score has increased.
                    player.sendPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.CONTRIB_SCORE_INCREASED_S1).addNumber(
                            offeringScore
                        )
                    )

                    if (isHighestScore)
                        showChatWindow(player, 3, "c", false)
                    else
                        showChatWindow(player, 3, "d", false)
                }
                4 // Current High Scores
                -> {
                    val sb =
                        StringBuilder("<html><body>Festival Guide:<br>These are the top scores of the week, for the ")

                    val dawnData = SevenSignsFestival.getHighestScoreData(CabalType.DAWN, festivalIndex)
                    val duskData = SevenSignsFestival.getHighestScoreData(CabalType.DUSK, festivalIndex)
                    val overallData = SevenSignsFestival.getOverallHighestScoreData(festivalIndex)

                    val dawnScore = dawnData!!.getInteger("score")
                    val duskScore = duskData!!.getInteger("score")

                    sb.append(_festivalType.festivalTypeName).append(" festival.<br>")

                    if (dawnScore > 0)
                        sb.append("Dawn: ").append(calculateDate(dawnData.getString("date"))).append(". Score ").append(
                            dawnScore
                        ).append("<br>").append(dawnData.getString("members")).append("<br>")
                    else
                        sb.append("Dawn: No record exists. Score 0<br>")

                    if (duskScore > 0)
                        sb.append("Dusk: ").append(calculateDate(duskData.getString("date"))).append(". Score ").append(
                            duskScore
                        ).append("<br>").append(duskData.getString("members")).append("<br>")
                    else
                        sb.append("Dusk: No record exists. Score 0<br>")

                    // If no data is returned, assume there is no record, or all scores are 0.
                    if (overallData != null) {
                        var cabalStr = "Children of Dusk"

                        if (overallData.getString("cabal") == "dawn")
                            cabalStr = "Children of Dawn"

                        sb.append("Consecutive top scores: ").append(calculateDate(overallData.getString("date")))
                            .append(". Score ").append(overallData.getInteger("score")).append("<br>Affilated side: ")
                            .append(cabalStr).append("<br>").append(overallData.getString("members")).append("<br>")
                    } else
                        sb.append("Consecutive top scores: No record exists. Score 0<br>")

                    sb.append("<a action=\"bypass -h npc_").append(objectId)
                        .append("_Chat 0\">Go back.</a></body></html>")

                    val html = NpcHtmlMessage(objectId)
                    html.setHtml(sb.toString())
                    player.sendPacket(html)
                }
                8 // Increase the Festival Challenge
                -> run {
                    if (playerParty == null)
                        return

                    if (!SevenSignsFestival.isFestivalInProgress)
                        return

                    if (!playerParty.isLeader(player)) {
                        showChatWindow(player, 8, "a", false)
                        return@run
                    }

                    if (SevenSignsFestival.increaseChallenge(_festivalOracle, festivalIndex))
                        showChatWindow(player, 8, "b", false)
                    else
                        showChatWindow(player, 8, "c", false)
                }
                9 // Leave the Festival
                -> {
                    if (playerParty == null)
                        return

                    /**
                     * If the player is the party leader, remove all participants from the festival (i.e. set the party to null, when updating the participant list) otherwise just remove this player from the "arena", and also remove them from the party.
                     */
                    val isLeader = playerParty.isLeader(player)

                    if (isLeader) {
                        SevenSignsFestival.updateParticipants(player, null)
                    } else {
                        SevenSignsFestival.updateParticipants(player, playerParty)
                        playerParty.removePartyMember(player, MessageType.EXPELLED)
                    }
                }
                0 // Distribute Accumulated Bonus
                -> {
                    if (!SevenSigns.isSealValidationPeriod) {
                        player.sendMessage("Bonuses cannot be paid during the competition period.")
                        return
                    }

                    if (SevenSignsFestival.distribAccumulatedBonus(player) > 0)
                        showChatWindow(player, 0, "a", false)
                    else
                        showChatWindow(player, 0, "b", false)
                }
                else -> showChatWindow(player, `val`, null, false)
            }
        } else {
            // this class dont know any other commands, let forward
            // the command to the parent class
            super.onBypassFeedback(player, command)
        }
    }

    override fun showChatWindow(player: Player, `val`: Int) {
        var filename = SevenSigns.SEVEN_SIGNS_HTML_PATH

        when (template.npcId) {
            31127 //
                , 31128 //
                , 31129 // Dawn Festival Guides
                , 31130 //
                , 31131 //
            -> filename += "festival/dawn_guide.htm"

            31137 //
                , 31138 //
                , 31139 // Dusk Festival Guides
                , 31140 //
                , 31141 //
            -> filename += "festival/dusk_guide.htm"

            31132 //
                , 31133 //
                , 31134 //
                , 31135 //
                , 31136 // Festival Witches
                , 31142 //
                , 31143 //
                , 31144 //
                , 31145 //
                , 31146 //
            -> filename += "festival/festival_witch.htm"
        }

        val html = NpcHtmlMessage(objectId)
        html.setFile(filename)
        html.replace("%objectId%", objectId)
        html.replace("%festivalMins%", SevenSignsFestival.timeToNextFestivalStr)
        player.sendPacket(html)

        // Send ActionFailed to the player in order to avoid he stucks
        player.sendPacket(ActionFailed.STATIC_PACKET)
    }

    private fun showChatWindow(player: Player, `val`: Int, suffix: String?, isDescription: Boolean) {
        val html = NpcHtmlMessage(objectId)
        html.setFile(SevenSigns.SEVEN_SIGNS_HTML_PATH + "festival/" + (if (isDescription) "desc_" else "festival_") + (if (suffix != null) `val`.toString() + suffix else `val`) + ".htm")
        html.replace("%objectId%", objectId)
        html.replace("%festivalType%", _festivalType.festivalTypeName)
        html.replace("%cycleMins%", SevenSignsFestival.minsToNextCycle)
        if (!isDescription && "2b" == `val`.toString() + suffix!!)
            html.replace("%minFestivalPartyMembers%", Config.ALT_FESTIVAL_MIN_PLAYER)

        // Festival's fee
        if (`val` == 1) {
            html.replace("%blueStoneNeeded%", _blueStonesNeeded)
            html.replace("%greenStoneNeeded%", _greenStonesNeeded)
            html.replace("%redStoneNeeded%", _redStonesNeeded)
        } else if (`val` == 5)
            html.replace("%statsTable%", statsTable)
        else if (`val` == 6)
            html.replace("%bonusTable%", bonusTable)// If the stats or bonus table is required, construct them.

        player.sendPacket(html)

        // Send ActionFailed to the player in order to avoid he stucks
        player.sendPacket(ActionFailed.STATIC_PACKET)
    }

    private fun calculateDate(milliFromEpoch: String): String {
        val numMillis = java.lang.Long.valueOf(milliFromEpoch)
        val calCalc = Calendar.getInstance()

        calCalc.timeInMillis = numMillis

        return calCalc.get(Calendar.YEAR).toString() + "/" + calCalc.get(Calendar.MONTH) + "/" + calCalc.get(Calendar.DAY_OF_MONTH)
    }

    override fun onSpawn() {
        super.onSpawn()

        val zone = ZoneManager.getZone(this, PeaceZone::class.java)

        // Festival Witches are spawned inside festival, out of peace zone -> skip them
        if (zone != null)
            SevenSignsFestival.addPeaceZone(zone, _festivalOracle === CabalType.DAWN)
    }
}