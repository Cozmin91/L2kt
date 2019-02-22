package com.l2kt.gameserver.model.actor.instance

import com.l2kt.Config
import com.l2kt.commons.lang.StringUtil
import com.l2kt.gameserver.data.manager.CastleManorManager
import com.l2kt.gameserver.data.sql.ClanTable
import com.l2kt.gameserver.instancemanager.SevenSigns
import com.l2kt.gameserver.instancemanager.SevenSigns.CabalType
import com.l2kt.gameserver.instancemanager.SevenSigns.SealType
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.model.pledge.Clan
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.*
import java.util.*
import kotlin.math.roundToInt

/**
 * An instance type extending [Merchant], used for castle chamberlains.<br></br>
 * <br></br>
 * It handles following actions :
 *
 *  * Tax rate control
 *  * Regional manor system control
 *  * Castle treasure control
 *  * Siege time modifier
 *  * Items production
 *  * Doors management && Doors/walls upgrades
 *  * Traps management && upgrades
 *
 */
open class CastleChamberlain(objectId: Int, template: NpcTemplate) : Merchant(objectId, template) {

    private var _preHour = 6

    override fun onBypassFeedback(player: Player, command: String) {
        val cond = validateCondition(player)
        if (cond < COND_OWNER) {
            val html = NpcHtmlMessage(objectId)
            html.setFile(if (cond == COND_BUSY_BECAUSE_OF_SIEGE) "data/html/chamberlain/busy.htm" else "data/html/chamberlain/noprivs.htm")
            player.sendPacket(html)
            return
        }

        val st = StringTokenizer(command, " ")
        val actualCommand = st.nextToken() // Get actual command

        var `val` = ""
        if (st.hasMoreTokens())
            `val` = st.nextToken()

        if (actualCommand.equals("banish_foreigner", ignoreCase = true)) {
            if (!validatePrivileges(player, Clan.CP_CS_DISMISS))
                return

            // Move non-clan members off castle area, and send html
            castle!!.banishForeigners()
            sendFileMessage(player, "data/html/chamberlain/banishafter.htm")
        } else if (actualCommand.equals("banish_foreigner_show", ignoreCase = true)) {
            if (!validatePrivileges(player, Clan.CP_CS_DISMISS))
                return

            sendFileMessage(player, "data/html/chamberlain/banishfore.htm")
        } else if (actualCommand.equals("manage_functions", ignoreCase = true)) {
            if (!validatePrivileges(player, Clan.CP_CS_SET_FUNCTIONS))
                return

            sendFileMessage(player, "data/html/chamberlain/manage.htm")
        } else if (actualCommand.equals("products", ignoreCase = true)) {
            if (!validatePrivileges(player, Clan.CP_CS_USE_FUNCTIONS))
                return

            sendFileMessage(player, "data/html/chamberlain/products.htm")
        } else if (actualCommand.equals("list_siege_clans", ignoreCase = true)) {
            if (!validatePrivileges(player, Clan.CP_CS_MANAGE_SIEGE))
                return

            player.sendPacket(SiegeInfo(castle!!))
        } else if (actualCommand.equals("receive_report", ignoreCase = true)) {
            if (cond == COND_CLAN_MEMBER)
                sendFileMessage(player, "data/html/chamberlain/noprivs.htm")
            else {
                val clan = ClanTable.getClan(castle!!.ownerId)

                val html = NpcHtmlMessage(objectId)
                html.setFile("data/html/chamberlain/report.htm")
                html.replace("%objectId%", objectId)
                html.replace("%clanname%", clan!!.name)
                html.replace("%clanleadername%", clan.leaderName)
                html.replace("%castlename%", castle!!.name)
                html.replace("%ss_event%", SevenSigns.currentPeriod.periodTypeName)

                when (SevenSigns.getSealOwner(SealType.AVARICE)) {
                    SevenSigns.CabalType.NORMAL -> html.replace("%ss_avarice%", "Not in Possession")

                    SevenSigns.CabalType.DAWN -> html.replace("%ss_avarice%", "Lords of Dawn")

                    SevenSigns.CabalType.DUSK -> html.replace("%ss_avarice%", "Revolutionaries of Dusk")
                }

                when (SevenSigns.getSealOwner(SealType.GNOSIS)) {
                    SevenSigns.CabalType.NORMAL -> html.replace("%ss_gnosis%", "Not in Possession")

                    SevenSigns.CabalType.DAWN -> html.replace("%ss_gnosis%", "Lords of Dawn")

                    SevenSigns.CabalType.DUSK -> html.replace("%ss_gnosis%", "Revolutionaries of Dusk")
                }

                when (SevenSigns.getSealOwner(SealType.STRIFE)) {
                    SevenSigns.CabalType.NORMAL -> html.replace("%ss_strife%", "Not in Possession")

                    SevenSigns.CabalType.DAWN -> html.replace("%ss_strife%", "Lords of Dawn")

                    SevenSigns.CabalType.DUSK -> html.replace("%ss_strife%", "Revolutionaries of Dusk")
                }
                player.sendPacket(html)
            }
        } else if (actualCommand.equals("items", ignoreCase = true)) {
            if (!validatePrivileges(player, Clan.CP_CS_USE_FUNCTIONS))
                return

            if (`val`.isEmpty())
                return

            showBuyWindow(player, Integer.parseInt(`val` + "1"))
        } else if (actualCommand.equals("manage_siege_defender", ignoreCase = true)) {
            if (!validatePrivileges(player, Clan.CP_CS_MANAGE_SIEGE))
                return

            player.sendPacket(SiegeInfo(castle!!))
        } else if (actualCommand.equals("manage_vault", ignoreCase = true)) {
            if (!validatePrivileges(player, Clan.CP_CS_TAXES))
                return

            var filename = "data/html/chamberlain/vault.htm"
            var amount = 0

            if (`val`.equals("deposit", ignoreCase = true)) {
                try {
                    amount = Integer.parseInt(st.nextToken())
                } catch (e: NoSuchElementException) {
                }

                if (amount > 0 && castle!!.treasury + amount < Integer.MAX_VALUE) {
                    if (player.reduceAdena("Castle", amount, this, true))
                        castle!!.addToTreasuryNoTax(amount.toLong())
                }
            } else if (`val`.equals("withdraw", ignoreCase = true)) {
                try {
                    amount = Integer.parseInt(st.nextToken())
                } catch (e: NoSuchElementException) {
                }

                if (amount > 0) {
                    if (castle!!.treasury < amount)
                        filename = "data/html/chamberlain/vault-no.htm"
                    else {
                        if (castle!!.addToTreasuryNoTax((-1 * amount).toLong()))
                            player.addAdena("Castle", amount, this, true)
                    }
                }
            }
            val html = NpcHtmlMessage(objectId)
            html.setFile(filename)
            html.replace("%objectId%", objectId)
            html.replace("%tax_income%", StringUtil.formatNumber(castle!!.treasury))
            html.replace("%withdraw_amount%", StringUtil.formatNumber(amount.toLong()))
            player.sendPacket(html)
        } else if (actualCommand.equals("operate_door", ignoreCase = true))
        // door control
        {
            if (!validatePrivileges(player, Clan.CP_CS_OPEN_DOOR))
                return

            if (`val`.isEmpty()) {
                val html = NpcHtmlMessage(objectId)
                html.setFile("data/html/chamberlain/$npcId-d.htm")
                html.replace("%objectId%", objectId)
                player.sendPacket(html)
                return
            }

            val open = Integer.parseInt(`val`) == 1
            while (st.hasMoreTokens())
                castle!!.openCloseDoor(player, Integer.parseInt(st.nextToken()), open)

            val html = NpcHtmlMessage(objectId)
            html.setFile(if (open) "data/html/chamberlain/doors-open.htm" else "data/html/chamberlain/doors-close.htm")
            html.replace("%objectId%", objectId)
            player.sendPacket(html)

        } else if (actualCommand.equals("tax_set", ignoreCase = true))
        // tax rates control
        {
            val html = NpcHtmlMessage(objectId)

            if (!validatePrivileges(player, Clan.CP_CS_TAXES))
                html.setFile("data/html/chamberlain/tax.htm")
            else {
                if (!`val`.isEmpty())
                    castle!!.setTaxPercent(player, Integer.parseInt(`val`))

                html.setFile("data/html/chamberlain/tax-adjust.htm")
            }

            html.replace("%objectId%", objectId)
            html.replace("%tax%", castle!!.taxPercent)
            player.sendPacket(html)
        } else if (actualCommand.equals("manor", ignoreCase = true)) {
            if (!validatePrivileges(player, Clan.CP_CS_MANOR_ADMIN))
                return

            var filename = ""
            if (!Config.ALLOW_MANOR)
                filename = "data/html/npcdefault.htm"
            else {
                val cmd = Integer.parseInt(`val`)
                when (cmd) {
                    0 -> filename = "data/html/chamberlain/manor/manor.htm"

                    // TODO: correct in html's to 1
                    4 -> filename = "data/html/chamberlain/manor/manor_help00" + st.nextToken() + ".htm"

                    else -> filename = "data/html/chamberlain/no.htm"
                }
            }

            if (filename.length != 0) {
                val html = NpcHtmlMessage(objectId)
                html.setFile(filename)
                html.replace("%objectId%", objectId)
                player.sendPacket(html)
            }
        } else if (command.startsWith("manor_menu_select")) {
            if (!validatePrivileges(player, Clan.CP_CS_MANOR_ADMIN))
                return

            val manor = CastleManorManager
            if (manor.isUnderMaintenance) {
                player.sendPacket(ActionFailed.STATIC_PACKET)
                player.sendPacket(SystemMessageId.THE_MANOR_SYSTEM_IS_CURRENTLY_UNDER_MAINTENANCE)
                return
            }

            val params = command.substring(command.indexOf("?") + 1)
            val str = StringTokenizer(params, "&")

            val ask =
                Integer.parseInt(str.nextToken().split("=").dropLastWhile { it.isEmpty() }.toTypedArray()[1])
            val state =
                Integer.parseInt(str.nextToken().split("=").dropLastWhile { it.isEmpty() }.toTypedArray()[1])
            val time = str.nextToken().split("=").dropLastWhile { it.isEmpty() }.toTypedArray()[1] == "1"

            val castleId = if (state == -1) castle!!.castleId else state

            when (ask) {
                3 // Current seeds (Manor info)
                -> player.sendPacket(ExShowSeedInfo(castleId, time, true))

                4 // Current crops (Manor info)
                -> player.sendPacket(ExShowCropInfo(castleId, time, true))

                5 // Basic info (Manor info)
                -> player.sendPacket(ExShowManorDefaultInfo(true))

                7 // Edit seed setup
                -> if (manor.isManorApproved)
                    player.sendPacket(SystemMessageId.A_MANOR_CANNOT_BE_SET_UP_BETWEEN_6_AM_AND_8_PM)
                else
                    player.sendPacket(ExShowSeedSetting(castleId))

                8 // Edit crop setup
                -> if (manor.isManorApproved)
                    player.sendPacket(SystemMessageId.A_MANOR_CANNOT_BE_SET_UP_BETWEEN_6_AM_AND_8_PM)
                else
                    player.sendPacket(ExShowCropSetting(castleId))
            }
        } else if (actualCommand.equals("siege_change", ignoreCase = true))
        // set siege time
        {
            if (!validatePrivileges(player, Clan.CP_CS_MANAGE_SIEGE))
                return

            if (castle!!.siege.siegeRegistrationEndDate < Calendar.getInstance().timeInMillis)
                sendFileMessage(player, "data/html/chamberlain/siegetime1.htm")
            else if (castle!!.siege.isTimeRegistrationOver)
                sendFileMessage(player, "data/html/chamberlain/siegetime2.htm")
            else
                sendFileMessage(player, "data/html/chamberlain/siegetime3.htm")
        } else if (actualCommand.equals("siege_time_set", ignoreCase = true))
        // set preDay
        {
            when (Integer.parseInt(`val`)) {
                1 -> _preHour = Integer.parseInt(st.nextToken())

                else -> {
                }
            }

            if (_preHour != 6) {
                castle!!.siegeDate!!.set(Calendar.HOUR_OF_DAY, _preHour + 12)

                // now store the changed time and finished next Siege Time registration
                castle!!.siege.endTimeRegistration(false)
                sendFileMessage(player, "data/html/chamberlain/siegetime8.htm")
                return
            }

            sendFileMessage(player, "data/html/chamberlain/siegetime6.htm")
        } else if (actualCommand == "give_crown") {
            val html = NpcHtmlMessage(objectId)

            if (cond == COND_OWNER) {
                if (player.inventory!!.getItemByItemId(6841) == null) {
                    player.addItem("Castle Crown", 6841, 1, player, true)

                    html.setFile("data/html/chamberlain/gavecrown.htm")
                    html.replace("%CharName%", player.name)
                    html.replace("%FeudName%", castle!!.name)
                } else
                    html.setFile("data/html/chamberlain/hascrown.htm")
            } else
                html.setFile("data/html/chamberlain/noprivs.htm")

            player.sendPacket(html)
        } else if (actualCommand == "manor_certificate") {
            if (!validatePrivileges(player, Clan.CP_CS_USE_FUNCTIONS))
                return

            val html = NpcHtmlMessage(objectId)

            // Player is registered as dusk, or we aren't in the good side of competition.
            if (SevenSigns.isSealValidationPeriod) {
                if (SevenSigns.getPlayerCabal(player.objectId) === CabalType.DUSK)
                    html.setFile("data/html/chamberlain/not-dawn-or-event.htm")
                else if (castle!!.leftCertificates == 0)
                    html.setFile("data/html/chamberlain/not-enough-ticket.htm")
                else {
                    html.setFile("data/html/chamberlain/sell-dawn-ticket.htm")
                    html.replace("%left%", castle!!.leftCertificates)
                    html.replace("%bundle%", CERTIFICATES_BUNDLE)
                    html.replace("%price%", CERTIFICATES_PRICE)
                }// We already reached the tickets limit.
            } else
                html.setFile("data/html/chamberlain/not-dawn-or-event.htm")

            html.replace("%objectId%", objectId)
            player.sendPacket(html)
        } else if (actualCommand == "validate_certificate") {
            if (!validatePrivileges(player, Clan.CP_CS_USE_FUNCTIONS))
                return

            val html = NpcHtmlMessage(objectId)

            // Player is registered as dusk, or we aren't in the good side of competition.
            if (SevenSigns.isSealValidationPeriod) {
                if (SevenSigns.getPlayerCabal(player.objectId) === CabalType.DUSK)
                    html.setFile("data/html/chamberlain/not-dawn-or-event.htm")
                else if (castle!!.leftCertificates == 0)
                    html.setFile("data/html/chamberlain/not-enough-ticket.htm")
                else if (player.reduceAdena("Certificate", CERTIFICATES_BUNDLE * CERTIFICATES_PRICE, this, true)) {
                    // We add certificates.
                    player.addItem("Certificate", 6388, CERTIFICATES_BUNDLE, this, true)

                    // We update that castle certificates count.
                    castle!!.setLeftCertificates(castle!!.leftCertificates - 10, true)

                    html.setFile("data/html/chamberlain/sell-dawn-ticket.htm")
                    html.replace("%left%", castle!!.leftCertificates)
                    html.replace("%bundle%", CERTIFICATES_BUNDLE)
                    html.replace("%price%", CERTIFICATES_PRICE)
                } else
                    html.setFile("data/html/chamberlain/not-enough-adena.htm")// We already reached the tickets limit.
            } else
                html.setFile("data/html/chamberlain/not-dawn-or-event.htm")

            html.replace("%objectId%", objectId)
            player.sendPacket(html)
        } else if (actualCommand.equals("castle_devices", ignoreCase = true)) {
            if (!validatePrivileges(player, Clan.CP_CS_SET_FUNCTIONS))
                return

            sendFileMessage(player, "data/html/chamberlain/devices.htm")
        } else if (actualCommand.equals("doors_update", ignoreCase = true)) {
            if (!validatePrivileges(player, Clan.CP_CS_SET_FUNCTIONS))
                return

            val html = NpcHtmlMessage(objectId)
            if (`val`.isEmpty())
                html.setFile("data/html/chamberlain/$npcId-gu.htm")
            else {
                html.setFile("data/html/chamberlain/doors-update.htm")
                html.replace("%id%", `val`)
                html.replace("%type%", st.nextToken())
            }
            html.replace("%objectId%", objectId)
            player.sendPacket(html)
        } else if (actualCommand.equals("doors_choose_upgrade", ignoreCase = true)) {
            if (!validatePrivileges(player, Clan.CP_CS_SET_FUNCTIONS))
                return

            val id = `val`
            val type = st.nextToken()
            val level = st.nextToken()

            val html = NpcHtmlMessage(objectId)
            html.setFile("data/html/chamberlain/doors-confirm.htm")
            html.replace("%objectId%", objectId)
            html.replace("%id%", id)
            html.replace("%level%", level)
            html.replace("%type%", type)
            html.replace("%price%", getDoorCost(Integer.parseInt(type), Integer.parseInt(level)))
            player.sendPacket(html)
        } else if (actualCommand.equals("doors_confirm_upgrade", ignoreCase = true)) {
            if (!validatePrivileges(player, Clan.CP_CS_SET_FUNCTIONS))
                return

            val type = Integer.parseInt(st.nextToken())
            val level = Integer.parseInt(st.nextToken())
            val price = getDoorCost(type, level)

            if (price == 0)
                return

            val id = Integer.parseInt(`val`)
            val door = castle?.getDoor(id) ?: return

            val currentHpRatio = door.stat.upgradeHpRatio

            val html = NpcHtmlMessage(objectId)

            if (currentHpRatio >= level) {
                html.setFile("data/html/chamberlain/doors-already-updated.htm")
                html.replace("%level%", currentHpRatio * 100)
            } else if (!player.reduceAdena("doors_upgrade", price, player, true))
                html.setFile("data/html/chamberlain/not-enough-adena.htm")
            else {
                castle!!.upgradeDoor(id, level, true)

                html.setFile("data/html/chamberlain/doors-success.htm")
            }
            html.replace("%objectId%", objectId)
            player.sendPacket(html)
        } else if (actualCommand.equals("traps_update", ignoreCase = true)) {
            if (!validatePrivileges(player, Clan.CP_CS_SET_FUNCTIONS))
                return

            val html = NpcHtmlMessage(objectId)
            if (`val`.isEmpty())
                html.setFile("data/html/chamberlain/$npcId-tu.htm")
            else {
                html.setFile(
                    "data/html/chamberlain/traps-update" + (if (castle?.name.equals(
                            "aden",
                            ignoreCase = true
                        )
                    ) "1" else "") + ".htm"
                )
                html.replace("%trapIndex%", `val`)
            }
            html.replace("%objectId%", objectId)
            player.sendPacket(html)
        } else if (actualCommand.equals("traps_choose_upgrade", ignoreCase = true)) {
            if (!validatePrivileges(player, Clan.CP_CS_SET_FUNCTIONS))
                return

            val trapIndex = `val`
            val level = st.nextToken()

            val html = NpcHtmlMessage(objectId)
            html.setFile("data/html/chamberlain/traps-confirm.htm")
            html.replace("%objectId%", objectId)
            html.replace("%trapIndex%", trapIndex)
            html.replace("%level%", level)
            html.replace("%price%", getTrapCost(Integer.parseInt(level)))
            player.sendPacket(html)
        } else if (actualCommand.equals("traps_confirm_upgrade", ignoreCase = true)) {
            if (!validatePrivileges(player, Clan.CP_CS_SET_FUNCTIONS))
                return

            val level = Integer.parseInt(st.nextToken())
            val price = getTrapCost(level)

            if (price == 0)
                return

            val trapIndex = Integer.parseInt(`val`)
            val currentLevel = castle!!.getTrapUpgradeLevel(trapIndex)

            val html = NpcHtmlMessage(objectId)

            if (currentLevel >= level) {
                html.setFile("data/html/chamberlain/traps-already-updated.htm")
                html.replace("%level%", currentLevel)
            } else if (!player.reduceAdena("traps_upgrade", price, player, true))
                html.setFile("data/html/chamberlain/not-enough-adena.htm")
            else {
                castle!!.setTrapUpgrade(trapIndex, level, true)

                html.setFile("data/html/chamberlain/traps-success.htm")
            }
            html.replace("%objectId%", objectId)
            player.sendPacket(html)
        } else
            super.onBypassFeedback(player, command)
    }

    override fun showChatWindow(player: Player) {
        player.sendPacket(ActionFailed.STATIC_PACKET)
        var filename = "data/html/chamberlain/no.htm"

        val condition = validateCondition(player)
        if (condition > COND_ALL_FALSE) {
            if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
                filename = "data/html/chamberlain/busy.htm"
            else if (condition >= COND_OWNER)
                filename = "data/html/chamberlain/chamberlain.htm"
        }

        val html = NpcHtmlMessage(objectId)
        html.setFile(filename)
        html.replace("%objectId%", objectId)
        player.sendPacket(html)
    }

    protected fun validateCondition(player: Player): Int {
        if (castle != null && player.clan != null) {
            if (castle!!.siege.isInProgress)
                return COND_BUSY_BECAUSE_OF_SIEGE

            if (castle!!.ownerId == player.clanId) {
                return if (player.isClanLeader) COND_OWNER else COND_CLAN_MEMBER

            }
        }
        return COND_ALL_FALSE
    }

    private fun validatePrivileges(player: Player, privilege: Int): Boolean {
        if (player.clanPrivileges and privilege != privilege) {
            val html = NpcHtmlMessage(objectId)
            html.setFile("data/html/chamberlain/noprivs.htm")
            player.sendPacket(html)
            return false
        }
        return true
    }

    private fun sendFileMessage(player: Player, htmlMessage: String) {
        val html = NpcHtmlMessage(objectId)
        html.setFile(htmlMessage)
        html.replace("%objectId%", objectId)
        html.replace("%npcId%", npcId)
        html.replace("%time%", castle!!.siegeDate!!.time.toString())
        player.sendPacket(html)
    }

    companion object {
        private const val CERTIFICATES_BUNDLE = 10
        private const val CERTIFICATES_PRICE = 1000

        const val COND_ALL_FALSE = 0
        const val COND_BUSY_BECAUSE_OF_SIEGE = 1
        const val COND_OWNER = 2
        const val COND_CLAN_MEMBER = 3

        /**
         * Retrieve the price of the door, following its type, required level of upgrade and current Seven Signs state.
         * @param type : The type of doors (1: normal gates, 2: metallic gates, 3: walls).
         * @param level : The required level of upgrade (x2, x3 or x5 HPs).
         * @return The price modified by Seal of Strife state (-20% if Dawn is winning, x3 if Dusk is winning).
         */
        private fun getDoorCost(type: Int, level: Int): Int {
            var price = 0

            when (type) {
                1 -> when (level) {
                    2 -> price = 300000
                    3 -> price = 400000
                    5 -> price = 500000
                }

                2 -> when (level) {
                    2 -> price = 750000
                    3 -> price = 900000
                    5 -> price = 1000000
                }

                3 -> when (level) {
                    2 -> price = 1600000
                    3 -> price = 1800000
                    5 -> price = 2000000
                }
            }

            when (SevenSigns.getSealOwner(SealType.STRIFE)) {
                SevenSigns.CabalType.DUSK -> price *= 3

                SevenSigns.CabalType.DAWN -> price = (price * 0.8).roundToInt()
            }

            return price
        }

        /**
         * Retrieve the price of traps, following its level.
         * @param level : The required level of upgrade.
         * @return The price modified by Seal of Strife state (-20% if Dawn is winning, x3 if Dusk is winning).
         */
        private fun getTrapCost(level: Int): Int {
            var price = 0

            when (level) {
                1 -> price = 3000000

                2 -> price = 4000000

                3 -> price = 5000000

                4 -> price = 6000000
            }

            when (SevenSigns.getSealOwner(SealType.STRIFE)) {
                SevenSigns.CabalType.DUSK -> price *= 3

                SevenSigns.CabalType.DAWN -> price = (price * 0.8).roundToInt()
            }

            return price
        }
    }
}