package com.l2kt.gameserver.model.actor.instance

import com.l2kt.Config
import com.l2kt.gameserver.instancemanager.SevenSigns
import com.l2kt.gameserver.instancemanager.SevenSigns.CabalType
import com.l2kt.gameserver.instancemanager.SevenSigns.SealType
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.model.itemcontainer.PcInventory
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import java.util.*

open class SignsPriest(objectId: Int, template: NpcTemplate) : Folk(objectId, template) {

    override fun onBypassFeedback(player: Player, command: String) {
        if (player.currentFolk == null || player.currentFolk.objectId != objectId)
            return

        if (command.startsWith("SevenSignsDesc")) {
            showChatWindow(player, Integer.parseInt(command.substring(15)), null, true)
        } else if (command.startsWith("SevenSigns")) {
            val path: String

            var cabal = CabalType.NORMAL
            var stoneType = 0

            val ancientAdenaAmount = player.ancientAdena.toLong()

            var `val` = Integer.parseInt(command.substring(11, 12).trim { it <= ' ' })

            if (command.length > 12)
            // SevenSigns x[x] x [x..x]
                `val` = Integer.parseInt(command.substring(11, 13).trim { it <= ' ' })

            if (command.length > 13) {
                try {
                    cabal = CabalType.VALUES[Integer.parseInt(command.substring(14, 15).trim { it <= ' ' })]
                } catch (e: Exception) {
                    try {
                        cabal = CabalType.VALUES[Integer.parseInt(command.substring(13, 14).trim { it <= ' ' })]
                    } catch (e2: Exception) {
                        try {
                            val st = StringTokenizer(command.trim { it <= ' ' })
                            st.nextToken()
                            cabal = CabalType.VALUES[Integer.parseInt(st.nextToken())]
                        } catch (e3: Exception) {
                            WorldObject.LOGGER.warn(
                                "Failed to retrieve cabal from bypass command. NpcId: {}, command: {}.",
                                npcId,
                                command
                            )
                        }

                    }

                }

            }

            when (`val`) {
                2 // Purchase Record of the Seven Signs
                -> run {
                    if (!player.inventory!!.validateCapacity(1)) {
                        player.sendPacket(SystemMessageId.SLOTS_FULL)
                        return@run
                    }

                    if (!player.reduceAdena("SevenSigns", SevenSigns.RECORD_SEVEN_SIGNS_COST, this, true))
                        return@run

                    player.addItem("SevenSigns", SevenSigns.RECORD_SEVEN_SIGNS_ID, 1, player, true)

                    if (this is DawnPriest)
                        showChatWindow(player, `val`, "dawn", false)
                    else
                        showChatWindow(player, `val`, "dusk", false)
                }

                33 // "I want to participate" request
                -> run {
                    val oldCabal = SevenSigns.getPlayerCabal(player.objectId)

                    if (oldCabal !== CabalType.NORMAL) {
                        if (this is DawnPriest)
                            showChatWindow(player, `val`, "dawn_member", false)
                        else
                            showChatWindow(player, `val`, "dusk_member", false)
                        return
                    } else if (player.classId.level() == 0) {
                        if (this is DawnPriest)
                            showChatWindow(player, `val`, "dawn_firstclass", false)
                        else
                            showChatWindow(player, `val`, "dusk_firstclass", false)
                        return
                    } else if (cabal === CabalType.DUSK && Config.ALT_GAME_CASTLE_DUSK)
                    // dusk
                    {
                        // castle owners cannot participate with dusk side
                        if (player.clan != null && player.clan.hasCastle()) {
                            showChatWindow(player, SevenSigns.SEVEN_SIGNS_HTML_PATH + "signs_33_dusk_no.htm")
                            return@run
                        }
                    } else if (cabal === CabalType.DAWN && Config.ALT_GAME_CASTLE_DAWN)
                    // dawn
                    {
                        // clans without castle need to pay participation fee
                        if (player.clan == null || !player.clan.hasCastle()) {
                            showChatWindow(player, SevenSigns.SEVEN_SIGNS_HTML_PATH + "signs_33_dawn_fee.htm")
                            return@run
                        }
                    }

                    if (this is DawnPriest)
                        showChatWindow(player, `val`, "dawn", false)
                    else
                        showChatWindow(player, `val`, "dusk", false)
                }

                34 // Pay the participation fee request
                -> {
                    val adena = player.inventory!!.getItemByItemId(PcInventory.ADENA_ID) // adena
                    val certif = player.inventory!!.getItemByItemId(6388) // Lord of the Manor's Certificate of Approval
                    var fee = true

                    if (player.classId.level() < 2 || adena != null && adena.count >= SevenSigns.ADENA_JOIN_DAWN_COST || certif != null && certif.count >= 1)
                        fee = false

                    if (fee)
                        showChatWindow(player, SevenSigns.SEVEN_SIGNS_HTML_PATH + "signs_33_dawn_no.htm")
                    else
                        showChatWindow(player, SevenSigns.SEVEN_SIGNS_HTML_PATH + "signs_33_dawn.htm")
                }

                3 // Join Cabal Intro 1
                    , 8 // Festival of Darkness Intro - SevenSigns x [0]1
                -> showChatWindow(player, `val`, cabal.shortName, false)

                4 // Join a Cabal - SevenSigns 4 [0]1 x
                -> {
                    val newSeal = SealType.VALUES[Integer.parseInt(command.substring(15))]

                    if (player.classId.level() >= 2) {
                        if (cabal === CabalType.DUSK && Config.ALT_GAME_CASTLE_DUSK) {
                            if (player.clan != null && player.clan.hasCastle())
                            // even if in htmls is said that ally can have castle too, but its not
                            {
                                showChatWindow(player, SevenSigns.SEVEN_SIGNS_HTML_PATH + "signs_33_dusk_no.htm")
                                return
                            }
                        }
                        /*
						 * If the player is trying to join the Lords of Dawn, check if they are carrying a Lord's certificate. If not then try to take the required amount of adena instead.
						 */
                        if (Config.ALT_GAME_CASTLE_DAWN && cabal === CabalType.DAWN) {
                            var allowJoinDawn = false

                            if (player.clan != null && player.clan.hasCastle())
                            // castle owner don't need to pay anything
                                allowJoinDawn = true
                            else if (player.destroyItemByItemId(
                                    "SevenSigns",
                                    SevenSigns.CERTIFICATE_OF_APPROVAL_ID,
                                    1,
                                    this,
                                    true
                                )
                            )
                                allowJoinDawn = true
                            else if (player.reduceAdena("SevenSigns", SevenSigns.ADENA_JOIN_DAWN_COST, this, true))
                                allowJoinDawn = true

                            if (!allowJoinDawn) {
                                showChatWindow(player, SevenSigns.SEVEN_SIGNS_HTML_PATH + "signs_33_dawn_fee.htm")
                                return
                            }
                        }
                    }
                    SevenSigns.setPlayerInfo(player.objectId, cabal, newSeal)

                    if (cabal === CabalType.DAWN)
                        player.sendPacket(SystemMessageId.SEVENSIGNS_PARTECIPATION_DAWN) // Joined Dawn
                    else
                        player.sendPacket(SystemMessageId.SEVENSIGNS_PARTECIPATION_DUSK) // Joined Dusk

                    // Show a confirmation message to the user, indicating which seal they chose.
                    when (newSeal) {
                        SevenSigns.SealType.AVARICE -> player.sendPacket(SystemMessageId.FIGHT_FOR_AVARICE)

                        SevenSigns.SealType.GNOSIS -> player.sendPacket(SystemMessageId.FIGHT_FOR_GNOSIS)

                        SevenSigns.SealType.STRIFE -> player.sendPacket(SystemMessageId.FIGHT_FOR_STRIFE)
                    }

                    showChatWindow(player, 4, cabal.shortName, false)
                }

                5 -> if (this is DawnPriest) {
                    if (SevenSigns.getPlayerCabal(player.objectId) === CabalType.NORMAL)
                        showChatWindow(player, `val`, "dawn_no", false)
                    else
                        showChatWindow(player, `val`, "dawn", false)
                } else {
                    if (SevenSigns.getPlayerCabal(player.objectId) === CabalType.NORMAL)
                        showChatWindow(player, `val`, "dusk_no", false)
                    else
                        showChatWindow(player, `val`, "dusk", false)
                }

                21 -> run {
                    val contribStoneId = Integer.parseInt(command.substring(14, 18))

                    val contribBlueStones = player.inventory!!.getItemByItemId(SevenSigns.SEAL_STONE_BLUE_ID)
                    val contribGreenStones = player.inventory!!.getItemByItemId(SevenSigns.SEAL_STONE_GREEN_ID)
                    val contribRedStones = player.inventory!!.getItemByItemId(SevenSigns.SEAL_STONE_RED_ID)

                    val contribBlueStoneCount = contribBlueStones?.count ?: 0
                    val contribGreenStoneCount = contribGreenStones?.count ?: 0
                    val contribRedStoneCount = contribRedStones?.count ?: 0

                    var score = SevenSigns.getPlayerContribScore(player.objectId)
                    var contributionCount = 0

                    var contribStonesFound = false

                    var redContrib = 0
                    var greenContrib = 0
                    var blueContrib = 0

                    try {
                        contributionCount = Integer.parseInt(command.substring(19).trim { it <= ' ' })
                    } catch (NumberFormatException: Exception) {
                        if (this is DawnPriest)
                            showChatWindow(player, 6, "dawn_failure", false)
                        else
                            showChatWindow(player, 6, "dusk_failure", false)
                        return@run
                    }

                    when (contribStoneId) {
                        SevenSigns.SEAL_STONE_BLUE_ID -> {
                            blueContrib = (Config.ALT_MAXIMUM_PLAYER_CONTRIB - score) / SevenSigns.SEAL_STONE_BLUE_VALUE
                            if (blueContrib > contribBlueStoneCount)
                                blueContrib = contributionCount
                        }

                        SevenSigns.SEAL_STONE_GREEN_ID -> {
                            greenContrib =
                                (Config.ALT_MAXIMUM_PLAYER_CONTRIB - score) / SevenSigns.SEAL_STONE_GREEN_VALUE
                            if (greenContrib > contribGreenStoneCount)
                                greenContrib = contributionCount
                        }

                        SevenSigns.SEAL_STONE_RED_ID -> {
                            redContrib = (Config.ALT_MAXIMUM_PLAYER_CONTRIB - score) / SevenSigns.SEAL_STONE_RED_VALUE
                            if (redContrib > contribRedStoneCount)
                                redContrib = contributionCount
                        }
                    }

                    if (redContrib > 0)
                        contribStonesFound = contribStonesFound or player.destroyItemByItemId(
                            "SevenSigns",
                            SevenSigns.SEAL_STONE_RED_ID,
                            redContrib,
                            this,
                            true
                        )

                    if (greenContrib > 0)
                        contribStonesFound = contribStonesFound or player.destroyItemByItemId(
                            "SevenSigns",
                            SevenSigns.SEAL_STONE_GREEN_ID,
                            greenContrib,
                            this,
                            true
                        )

                    if (blueContrib > 0)
                        contribStonesFound = contribStonesFound or player.destroyItemByItemId(
                            "SevenSigns",
                            SevenSigns.SEAL_STONE_BLUE_ID,
                            blueContrib,
                            this,
                            true
                        )

                    if (!contribStonesFound) {
                        if (this is DawnPriest)
                            showChatWindow(player, 6, "dawn_low_stones", false)
                        else
                            showChatWindow(player, 6, "dusk_low_stones", false)
                    } else {
                        score = SevenSigns.addPlayerStoneContrib(player.objectId, blueContrib, greenContrib, redContrib)
                        player.sendPacket(
                            SystemMessage.getSystemMessage(SystemMessageId.CONTRIB_SCORE_INCREASED_S1).addItemNumber(
                                score
                            )
                        )

                        if (this is DawnPriest)
                            showChatWindow(player, 6, "dawn", false)
                        else
                            showChatWindow(player, 6, "dusk", false)
                    }
                }

                6 // Contribute Seal Stones - SevenSigns 6 x
                -> {
                    stoneType = Integer.parseInt(command.substring(13))

                    val blueStones = player.inventory!!.getItemByItemId(SevenSigns.SEAL_STONE_BLUE_ID)
                    val greenStones = player.inventory!!.getItemByItemId(SevenSigns.SEAL_STONE_GREEN_ID)
                    val redStones = player.inventory!!.getItemByItemId(SevenSigns.SEAL_STONE_RED_ID)

                    val blueStoneCount = blueStones?.count ?: 0
                    val greenStoneCount = greenStones?.count ?: 0
                    val redStoneCount = redStones?.count ?: 0

                    var contribScore = SevenSigns.getPlayerContribScore(player.objectId)
                    var stonesFound = false

                    if (contribScore == Config.ALT_MAXIMUM_PLAYER_CONTRIB) {
                        player.sendPacket(SystemMessageId.CONTRIB_SCORE_EXCEEDED)
                    } else {
                        var redContribCount = 0
                        var greenContribCount = 0
                        var blueContribCount = 0

                        var contribStoneColor: String? = null
                        var stoneColorContr: String? = null

                        var stoneCountContr = 0
                        var stoneIdContr = 0

                        when (stoneType) {
                            1 -> {
                                contribStoneColor = "Blue"
                                stoneColorContr = "blue"
                                stoneIdContr = SevenSigns.SEAL_STONE_BLUE_ID
                                stoneCountContr = blueStoneCount
                            }

                            2 -> {
                                contribStoneColor = "Green"
                                stoneColorContr = "green"
                                stoneIdContr = SevenSigns.SEAL_STONE_GREEN_ID
                                stoneCountContr = greenStoneCount
                            }

                            3 -> {
                                contribStoneColor = "Red"
                                stoneColorContr = "red"
                                stoneIdContr = SevenSigns.SEAL_STONE_RED_ID
                                stoneCountContr = redStoneCount
                            }

                            4 -> {
                                var tempContribScore = contribScore
                                redContribCount =
                                    (Config.ALT_MAXIMUM_PLAYER_CONTRIB - tempContribScore) / SevenSigns.SEAL_STONE_RED_VALUE
                                if (redContribCount > redStoneCount)
                                    redContribCount = redStoneCount

                                tempContribScore += redContribCount * SevenSigns.SEAL_STONE_RED_VALUE
                                greenContribCount =
                                    (Config.ALT_MAXIMUM_PLAYER_CONTRIB - tempContribScore) / SevenSigns.SEAL_STONE_GREEN_VALUE
                                if (greenContribCount > greenStoneCount)
                                    greenContribCount = greenStoneCount

                                tempContribScore += greenContribCount * SevenSigns.SEAL_STONE_GREEN_VALUE
                                blueContribCount =
                                    (Config.ALT_MAXIMUM_PLAYER_CONTRIB - tempContribScore) / SevenSigns.SEAL_STONE_BLUE_VALUE
                                if (blueContribCount > blueStoneCount)
                                    blueContribCount = blueStoneCount

                                if (redContribCount > 0)
                                    stonesFound = stonesFound or player.destroyItemByItemId(
                                        "SevenSigns",
                                        SevenSigns.SEAL_STONE_RED_ID,
                                        redContribCount,
                                        this,
                                        true
                                    )

                                if (greenContribCount > 0)
                                    stonesFound = stonesFound or player.destroyItemByItemId(
                                        "SevenSigns",
                                        SevenSigns.SEAL_STONE_GREEN_ID,
                                        greenContribCount,
                                        this,
                                        true
                                    )

                                if (blueContribCount > 0)
                                    stonesFound = stonesFound or player.destroyItemByItemId(
                                        "SevenSigns",
                                        SevenSigns.SEAL_STONE_BLUE_ID,
                                        blueContribCount,
                                        this,
                                        true
                                    )

                                if (!stonesFound) {
                                    if (this is DawnPriest)
                                        showChatWindow(player, `val`, "dawn_no_stones", false)
                                    else
                                        showChatWindow(player, `val`, "dusk_no_stones", false)
                                } else {
                                    contribScore = SevenSigns.addPlayerStoneContrib(
                                        player.objectId,
                                        blueContribCount,
                                        greenContribCount,
                                        redContribCount
                                    )
                                    player.sendPacket(
                                        SystemMessage.getSystemMessage(SystemMessageId.CONTRIB_SCORE_INCREASED_S1).addItemNumber(
                                            contribScore
                                        )
                                    )

                                    if (this is DawnPriest)
                                        showChatWindow(player, 6, "dawn", false)
                                    else
                                        showChatWindow(player, 6, "dusk", false)
                                }
                                return
                            }
                        }

                        if (this is DawnPriest)
                            path = SevenSigns.SEVEN_SIGNS_HTML_PATH + "signs_6_dawn_contribute.htm"
                        else
                            path = SevenSigns.SEVEN_SIGNS_HTML_PATH + "signs_6_dusk_contribute.htm"

                        val html = NpcHtmlMessage(objectId)
                        html.setFile(path)
                        html.replace("%contribStoneColor%", contribStoneColor!!)
                        html.replace("%stoneColor%", stoneColorContr!!)
                        html.replace("%stoneCount%", stoneCountContr)
                        html.replace("%stoneItemId%", stoneIdContr)
                        html.replace("%objectId%", objectId)
                        player.sendPacket(html)
                    }
                }

                7 // Exchange Ancient Adena for Adena - SevenSigns 7 xxxxxxx
                -> run {
                    var ancientAdenaConvert = 0

                    try {
                        ancientAdenaConvert = Integer.parseInt(command.substring(13).trim { it <= ' ' })
                    } catch (e: NumberFormatException) {
                        showChatWindow(player, SevenSigns.SEVEN_SIGNS_HTML_PATH + "blkmrkt_3.htm")
                        return@run
                    } catch (e: StringIndexOutOfBoundsException) {
                        showChatWindow(player, SevenSigns.SEVEN_SIGNS_HTML_PATH + "blkmrkt_3.htm")
                        return@run
                    }

                    if (ancientAdenaConvert < 1) {
                        showChatWindow(player, SevenSigns.SEVEN_SIGNS_HTML_PATH + "blkmrkt_3.htm")
                        return@run
                    }

                    if (ancientAdenaAmount < ancientAdenaConvert) {
                        showChatWindow(player, SevenSigns.SEVEN_SIGNS_HTML_PATH + "blkmrkt_4.htm")
                        return@run
                    }

                    player.reduceAncientAdena("SevenSigns", ancientAdenaConvert, this, true)
                    player.addAdena("SevenSigns", ancientAdenaConvert, this, true)

                    showChatWindow(player, SevenSigns.SEVEN_SIGNS_HTML_PATH + "blkmrkt_5.htm")
                }

                9 // Receive Contribution Rewards
                -> run{
                    if (SevenSigns.isSealValidationPeriod && SevenSigns.getPlayerCabal(player.objectId) === SevenSigns.cabalHighestScore) {
                        val ancientAdenaReward = SevenSigns.getAncientAdenaReward(player.objectId)

                        if (ancientAdenaReward < 3) {
                            if (this is DawnPriest)
                                showChatWindow(player, 9, "dawn_b", false)
                            else
                                showChatWindow(player, 9, "dusk_b", false)
                            return@run
                        }

                        player.addAncientAdena("SevenSigns", ancientAdenaReward, this, true)

                        if (this is DawnPriest)
                            showChatWindow(player, 9, "dawn_a", false)
                        else
                            showChatWindow(player, 9, "dusk_a", false)
                    }
                }

                11 // Teleport to Hunting Grounds
                -> run {
                    try {
                        val portInfo = command.substring(14).trim { it <= ' ' }
                        val st = StringTokenizer(portInfo)

                        val x = Integer.parseInt(st.nextToken())
                        val y = Integer.parseInt(st.nextToken())
                        val z = Integer.parseInt(st.nextToken())

                        val ancientAdenaCost = Integer.parseInt(st.nextToken())

                        if (ancientAdenaCost > 0) {
                            if (!player.reduceAncientAdena("SevenSigns", ancientAdenaCost, this, true))
                                return@run
                        }

                        player.teleToLocation(x, y, z, 0)
                    } catch (e: Exception) {
                        WorldObject.LOGGER.error("An error occurred while teleporting a player.", e)
                    }
                }

                16 -> if (this is DawnPriest)
                    showChatWindow(player, `val`, "dawn", false)
                else
                    showChatWindow(player, `val`, "dusk", false)

                17 // Exchange Seal Stones for Ancient Adena (Type Choice) - SevenSigns 17 x
                -> {
                    stoneType = Integer.parseInt(command.substring(14))

                    var stoneId = 0
                    var stoneCount = 0
                    var stoneValue = 0

                    var stoneColor: String? = null

                    when (stoneType) {
                        1 -> {
                            stoneColor = "blue"
                            stoneId = SevenSigns.SEAL_STONE_BLUE_ID
                            stoneValue = SevenSigns.SEAL_STONE_BLUE_VALUE
                        }

                        2 -> {
                            stoneColor = "green"
                            stoneId = SevenSigns.SEAL_STONE_GREEN_ID
                            stoneValue = SevenSigns.SEAL_STONE_GREEN_VALUE
                        }

                        3 -> {
                            stoneColor = "red"
                            stoneId = SevenSigns.SEAL_STONE_RED_ID
                            stoneValue = SevenSigns.SEAL_STONE_RED_VALUE
                        }

                        4 -> {
                            val blueStonesAll = player.inventory!!.getItemByItemId(SevenSigns.SEAL_STONE_BLUE_ID)
                            val greenStonesAll = player.inventory!!.getItemByItemId(SevenSigns.SEAL_STONE_GREEN_ID)
                            val redStonesAll = player.inventory!!.getItemByItemId(SevenSigns.SEAL_STONE_RED_ID)

                            val blueStoneCountAll = blueStonesAll?.count ?: 0
                            val greenStoneCountAll = greenStonesAll?.count ?: 0
                            val redStoneCountAll = redStonesAll?.count ?: 0
                            var ancientAdenaRewardAll = 0

                            ancientAdenaRewardAll =
                                SevenSigns.calcScore(blueStoneCountAll, greenStoneCountAll, redStoneCountAll)

                            if (ancientAdenaRewardAll == 0) {
                                if (this is DawnPriest)
                                    showChatWindow(player, 18, "dawn_no_stones", false)
                                else
                                    showChatWindow(player, 18, "dusk_no_stones", false)
                                return
                            }

                            if (blueStoneCountAll > 0)
                                player.destroyItemByItemId(
                                    "SevenSigns",
                                    SevenSigns.SEAL_STONE_BLUE_ID,
                                    blueStoneCountAll,
                                    this,
                                    true
                                )
                            if (greenStoneCountAll > 0)
                                player.destroyItemByItemId(
                                    "SevenSigns",
                                    SevenSigns.SEAL_STONE_GREEN_ID,
                                    greenStoneCountAll,
                                    this,
                                    true
                                )
                            if (redStoneCountAll > 0)
                                player.destroyItemByItemId(
                                    "SevenSigns",
                                    SevenSigns.SEAL_STONE_RED_ID,
                                    redStoneCountAll,
                                    this,
                                    true
                                )

                            player.addAncientAdena("SevenSigns", ancientAdenaRewardAll, this, true)

                            if (this is DawnPriest)
                                showChatWindow(player, 18, "dawn", false)
                            else
                                showChatWindow(player, 18, "dusk", false)
                            return
                        }
                    }

                    val stoneInstance = player.inventory!!.getItemByItemId(stoneId)
                    if (stoneInstance != null)
                        stoneCount = stoneInstance.count

                    if (this is DawnPriest)
                        path = SevenSigns.SEVEN_SIGNS_HTML_PATH + "signs_17_dawn.htm"
                    else
                        path = SevenSigns.SEVEN_SIGNS_HTML_PATH + "signs_17_dusk.htm"

                    var html = NpcHtmlMessage(objectId)
                    html.setFile(path)
                    html.replace("%stoneColor%", stoneColor!!)
                    html.replace("%stoneValue%", stoneValue)
                    html.replace("%stoneCount%", stoneCount)
                    html.replace("%stoneItemId%", stoneId)
                    html.replace("%objectId%", objectId)
                    player.sendPacket(html)
                }

                18 // Exchange Seal Stones for Ancient Adena - SevenSigns 18 xxxx xxxxxx
                -> run {
                    val convertStoneId = Integer.parseInt(command.substring(14, 18))
                    var convertCount = 0

                    try {
                        convertCount = Integer.parseInt(command.substring(19).trim { it <= ' ' })
                    } catch (NumberFormatException: Exception) {
                        if (this is DawnPriest)
                            showChatWindow(player, 18, "dawn_failed", false)
                        else
                            showChatWindow(player, 18, "dusk_failed", false)
                        return@run
                    }

                    val convertItem = player.inventory!!.getItemByItemId(convertStoneId)

                    if (convertItem != null) {
                        var ancientAdenaReward = 0
                        val totalCount = convertItem.count

                        if (convertCount <= totalCount && convertCount > 0) {
                            when (convertStoneId) {
                                SevenSigns.SEAL_STONE_BLUE_ID -> ancientAdenaReward =
                                    SevenSigns.calcScore(convertCount, 0, 0)
                                SevenSigns.SEAL_STONE_GREEN_ID -> ancientAdenaReward =
                                    SevenSigns.calcScore(0, convertCount, 0)
                                SevenSigns.SEAL_STONE_RED_ID -> ancientAdenaReward =
                                    SevenSigns.calcScore(0, 0, convertCount)
                            }

                            if (player.destroyItemByItemId("SevenSigns", convertStoneId, convertCount, this, true)) {
                                player.addAncientAdena("SevenSigns", ancientAdenaReward, this, true)

                                if (this is DawnPriest)
                                    showChatWindow(player, 18, "dawn", false)
                                else
                                    showChatWindow(player, 18, "dusk", false)
                            }
                        } else {
                            if (this is DawnPriest)
                                showChatWindow(player, 18, "dawn_low_stones", false)
                            else
                                showChatWindow(player, 18, "dusk_low_stones", false)
                            return@run
                        }
                    } else {
                        if (this is DawnPriest)
                            showChatWindow(player, 18, "dawn_no_stones", false)
                        else
                            showChatWindow(player, 18, "dusk_no_stones", false)
                        return@run
                    }
                }

                19 // Seal Information (for when joining a cabal)
                -> {
                    val chosenSeal = SealType.VALUES[Integer.parseInt(command.substring(16))]

                    val fileSuffix = chosenSeal.shortName + "_" + cabal.shortName

                    showChatWindow(player, `val`, fileSuffix, false)
                }

                20 // Seal Status (for when joining a cabal)
                -> {
                    val sb = StringBuilder()

                    if (this is DawnPriest)
                        sb.append("<html><body>Priest of Dawn:<br><font color=\"LEVEL\">[ Seal Status ]</font><br>")
                    else
                        sb.append("<html><body>Dusk Priestess:<br><font color=\"LEVEL\">[ Status of the Seals ]</font><br>")

                    for ((seal, sealOwner) in SevenSigns.sealOwners) {

                        if (sealOwner !== CabalType.NORMAL)
                            sb.append("[" + seal.fullName + ": " + sealOwner.fullName + "]<br>")
                        else
                            sb.append("[" + seal.fullName + ": Nothingness]<br>")
                    }

                    sb.append("<a action=\"bypass -h npc_" + objectId + "_Chat 0\">Go back.</a></body></html>")

                    val html = NpcHtmlMessage(objectId)
                    html.setHtml(sb.toString())
                    player.sendPacket(html)
                }

                else -> showChatWindow(player, `val`, null, false)
            }
        } else
            super.onBypassFeedback(player, command)
    }

    override fun showChatWindow(player: Player, `val`: Int) {
        val npcId = template.npcId
        var filename = SevenSigns.SEVEN_SIGNS_HTML_PATH

        val playerCabal = SevenSigns.getPlayerCabal(player.objectId)
        val winningCabal = SevenSigns.cabalHighestScore

        when (npcId) {
            31092 // Black Marketeer of Mammon
            -> filename += "blkmrkt_1.htm"

            31113 // Merchant of Mammon
            -> {
                val sealAvariceOwner = SevenSigns.getSealOwner(SealType.AVARICE)
                when (winningCabal) {
                    SevenSigns.CabalType.DAWN -> if (playerCabal !== winningCabal || playerCabal !== sealAvariceOwner) {
                        player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DAWN)
                        player.sendPacket(ActionFailed.STATIC_PACKET)
                        return
                    }

                    SevenSigns.CabalType.DUSK -> if (playerCabal !== winningCabal || playerCabal !== sealAvariceOwner) {
                        player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DUSK)
                        player.sendPacket(ActionFailed.STATIC_PACKET)
                        return
                    }

                    else -> {
                        player.sendPacket(SystemMessageId.QUEST_EVENT_PERIOD)
                        return
                    }
                }
                filename += "mammmerch_1.htm"
            }

            31126 // Blacksmith of Mammon
            -> {
                val sealGnosisOwner = SevenSigns.getSealOwner(SealType.GNOSIS)
                when (winningCabal) {
                    SevenSigns.CabalType.DAWN -> if (playerCabal !== winningCabal || playerCabal !== sealGnosisOwner) {
                        player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DAWN)
                        player.sendPacket(ActionFailed.STATIC_PACKET)
                        return
                    }

                    SevenSigns.CabalType.DUSK -> if (playerCabal !== winningCabal || playerCabal !== sealGnosisOwner) {
                        player.sendPacket(SystemMessageId.CAN_BE_USED_BY_DUSK)
                        player.sendPacket(ActionFailed.STATIC_PACKET)
                        return
                    }
                }
                filename += "mammblack_1.htm"
            }

            else ->
                // Get the text of the selected HTML file in function of the npcId and of the page number
                filename = getHtmlPath(npcId, `val`)
        }

        val html = NpcHtmlMessage(objectId)
        html.setFile(filename)
        html.replace("%objectId%", objectId)
        player.sendPacket(html)

        // Send a Server->Client ActionFailed to the Player in order to avoid that the client wait another packet
        player.sendPacket(ActionFailed.STATIC_PACKET)
    }

    private fun showChatWindow(player: Player, `val`: Int, suffix: String?, isDescription: Boolean) {
        var filename = SevenSigns.SEVEN_SIGNS_HTML_PATH

        filename += if (isDescription) "desc_$`val`" else "signs_$`val`"
        filename += if (suffix != null) "_$suffix.htm" else ".htm"

        showChatWindow(player, filename)
    }
}