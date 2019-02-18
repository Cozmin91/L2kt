package com.l2kt.gameserver.handler.admincommandhandlers

import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.lang.StringUtil
import com.l2kt.commons.math.MathUtil
import com.l2kt.gameserver.data.manager.CastleManager
import com.l2kt.gameserver.data.sql.PlayerInfoTable
import com.l2kt.gameserver.data.xml.NpcData
import com.l2kt.gameserver.data.xml.PlayerData
import com.l2kt.gameserver.handler.IAdminCommandHandler
import com.l2kt.gameserver.instancemanager.ClanHallManager
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.Summon
import com.l2kt.gameserver.model.actor.instance.Pet
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.actor.stat.PetStat
import com.l2kt.gameserver.model.actor.template.PlayerTemplate
import com.l2kt.gameserver.model.base.ClassId
import com.l2kt.gameserver.model.base.Sex
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.AbstractNpcInfo.NpcInfo
import com.l2kt.gameserver.network.serverpackets.GMViewItemList
import com.l2kt.gameserver.network.serverpackets.HennaInfo
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage
import java.util.*

class AdminEditChar : IAdminCommandHandler {

    override fun useAdminCommand(command: String, activeChar: Player): Boolean {
        if (command.startsWith("admin_changelvl")) {
            try {
                val st = StringTokenizer(command, " ")
                st.nextToken()

                val paramCount = st.countTokens()
                if (paramCount == 1) {
                    val lvl = Integer.parseInt(st.nextToken())
                    if (activeChar.target is Player)
                        onLineChange(activeChar, activeChar.target as Player, lvl)
                    else
                        activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET)
                } else if (paramCount == 2) {
                    val name = st.nextToken()
                    val lvl = Integer.parseInt(st.nextToken())

                    val player = World.getPlayer(name)
                    if (player != null)
                        onLineChange(activeChar, player, lvl)
                    else {
                        try {
                            L2DatabaseFactory.connection.use { con ->
                                con.prepareStatement("UPDATE characters SET accesslevel=? WHERE char_name=?")
                                    .use { ps ->
                                        ps.setInt(1, lvl)
                                        ps.setString(2, name)
                                        ps.execute()

                                        val count = ps.updateCount
                                        if (count == 0)
                                            activeChar.sendMessage("Player can't be found or access level unaltered.")
                                        else
                                            activeChar.sendMessage("Player's access level is now set to $lvl")
                                    }
                            }
                        } catch (e: Exception) {
                        }

                    }
                }
            } catch (e: Exception) {
                activeChar.sendMessage("Usage: //changelvl <target_new_level> | <player_name> <new_level>")
            }

        } else if (command == "admin_current_player")
            showCharacterInfo(activeChar, null)
        else if (command.startsWith("admin_character_info")) {
            try {
                val target = World.getPlayer(command.substring(21))
                if (target != null)
                    showCharacterInfo(activeChar, target)
                else
                    activeChar.sendPacket(SystemMessageId.CHARACTER_DOES_NOT_EXIST)
            } catch (e: Exception) {
                activeChar.sendMessage("Usage: //character_info <player_name>")
            }

        } else if (command.startsWith("admin_show_characters")) {
            try {
                listCharacters(activeChar, Integer.parseInt(command.substring(22)))
            } catch (e: Exception) {
                activeChar.sendMessage("Usage: //show_characters <page_number>")
            }

        } else if (command.startsWith("admin_find_character")) {
            try {
                findCharacter(activeChar, command.substring(21))
            } catch (e: Exception) {
                activeChar.sendMessage("Usage: //find_character <character_name>")
                listCharacters(activeChar, 1)
            }

        } else if (command.startsWith("admin_find_ip")) {
            try {
                findCharactersPerIp(activeChar, command.substring(14))
            } catch (e: Exception) {
                activeChar.sendMessage("Usage: //find_ip <www.xxx.yyy.zzz>")
                listCharacters(activeChar, 1)
            }

        } else if (command.startsWith("admin_find_account")) {
            try {
                findCharactersPerAccount(activeChar, command.substring(19))
            } catch (e: Exception) {
                activeChar.sendMessage("Usage: //find_account <player_name>")
                listCharacters(activeChar, 1)
            }

        } else if (command.startsWith("admin_find_dualbox")) {
            var multibox = 2
            try {
                multibox = Integer.parseInt(command.substring(19))
                if (multibox < 1) {
                    activeChar.sendMessage("Usage: //find_dualbox [number > 0]")
                    return false
                }
            } catch (e: Exception) {
            }

            findDualbox(activeChar, multibox)
        } else if (command == "admin_edit_character")
            editCharacter(activeChar)
        else if (command.startsWith("admin_setkarma")) {
            try {
                setTargetKarma(activeChar, Integer.parseInt(command.substring(15)))
            } catch (e: Exception) {
                activeChar.sendMessage("Usage: //setkarma <new_karma_value>")
            }

        } else if (command.startsWith("admin_rec")) {
            try {
                val target = activeChar.target
                var player: Player? = null

                if (target is Player)
                    player = target
                else
                    return false

                player.recomHave = Integer.parseInt(command.substring(10))
                player.sendMessage("You have been recommended by a GM.")
                player.broadcastUserInfo()
            } catch (e: Exception) {
                activeChar.sendMessage("Usage: //rec number")
            }

        } else if (command.startsWith("admin_setclass")) {
            try {
                val target = activeChar.target
                var player: Player? = null

                if (target is Player)
                    player = target
                else
                    return false

                var valid = false

                val classidval = Integer.parseInt(command.substring(15))
                for (classid in ClassId.VALUES)
                    if (classidval == classid.id)
                        valid = true

                if (valid && player.classId.id != classidval) {
                    player.setClassId(classidval)
                    if (!player.isSubClassActive)
                        player.baseClass = classidval

                    val newclass = (player.template as PlayerTemplate).className

                    player.refreshOverloaded()
                    player.store()
                    player.sendPacket(HennaInfo(player))
                    player.broadcastUserInfo()

                    // Messages
                    if (player != activeChar)
                        player.sendMessage("A GM changed your class to $newclass.")
                    activeChar.sendMessage(player.name + " is now a " + newclass + ".")
                } else
                    activeChar.sendMessage("Usage: //setclass <valid classid>")
            } catch (e: Exception) {
                AdminHelpPage.showHelpPage(activeChar, "charclasses.htm")
            }

        } else if (command.startsWith("admin_settitle")) {
            try {
                val target = activeChar.target
                val newTitle = command.substring(15)

                if (target is Player) {

                    target.title = newTitle
                    target.sendMessage("Your title has been changed by a GM.")
                    target.broadcastTitleInfo()
                } else if (target is Npc) {

                    target.title = newTitle
                    target.broadcastPacket(NpcInfo(target, null))
                } else
                    activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET)
            } catch (e: Exception) {
                activeChar.sendMessage("Usage: //settitle title")
            }

        } else if (command.startsWith("admin_setname")) {
            try {
                val target = activeChar.target
                val newName = command.substring(14)

                if (target is Player) {
                    // Invalid pattern.
                    if (!StringUtil.isValidString(newName, "^[A-Za-z0-9]{3,16}$")) {
                        activeChar.sendPacket(SystemMessageId.INCORRECT_NAME_TRY_AGAIN)
                        return false
                    }

                    // Name is a npc name.
                    if (NpcData.getTemplateByName(newName) != null) {
                        activeChar.sendPacket(SystemMessageId.INCORRECT_NAME_TRY_AGAIN)
                        return false
                    }

                    // Name already exists.
                    if (PlayerInfoTable.getPlayerObjectId(newName) > 0) {
                        activeChar.sendPacket(SystemMessageId.INCORRECT_NAME_TRY_AGAIN)
                        return false
                    }

                    target.name = newName
                    PlayerInfoTable.updatePlayerData(target, false)
                    target.sendMessage("Your name has been changed by a GM.")
                    target.broadcastUserInfo()
                    target.store()
                } else if (target is Npc) {

                    target.name = newName
                    target.broadcastPacket(NpcInfo(target, null))
                } else
                    activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET)
            } catch (e: Exception) {
                activeChar.sendMessage("Usage: //setname name")
            }

        } else if (command.startsWith("admin_setsex")) {
            val target = activeChar.target
            var player: Player? = null

            if (target is Player)
                player = target
            else
                return false

            var sex = Sex.MALE
            try {
                val st = StringTokenizer(command, " ")
                st.nextToken()

                sex = Sex.valueOf(st.nextToken().toUpperCase())
            } catch (e: Exception) {
            }

            if (sex !== player.appearance.sex) {
                player.appearance.sex = sex
                player.sendMessage("Your gender has been changed to " + sex.toString() + " by a GM.")
                player.broadcastUserInfo()

                player.decayMe()
                player.spawnMe()
            } else
                activeChar.sendMessage("The character sex is already defined as " + sex.toString() + ".")
        } else if (command.startsWith("admin_setcolor")) {
            try {
                val target = activeChar.target
                var player: Player? = null

                if (target is Player)
                    player = target
                else
                    return false

                player.appearance.nameColor = Integer.decode("0x" + command.substring(15))!!
                player.sendMessage("Your name color has been changed by a GM.")
                player.broadcastUserInfo()
            } catch (e: Exception) {
                activeChar.sendMessage("You need to specify a valid new color.")
            }

        } else if (command.startsWith("admin_settcolor")) {
            try {
                val target = activeChar.target
                var player: Player? = null

                if (target is Player)
                    player = target
                else
                    return false

                player.appearance.titleColor = Integer.decode("0x" + command.substring(16))!!
                player.sendMessage("Your title color has been changed by a GM.")
                player.broadcastUserInfo()
            } catch (e: Exception) {
                activeChar.sendMessage("You need to specify a valid new color.")
            }

        } else if (command.startsWith("admin_summon_info")) {
            val target = activeChar.target
            if (target is Summon)
                gatherSummonInfo(target, activeChar)
            else if (target is Player) {
                val pet = target.pet
                if (pet != null) {
                    gatherSummonInfo(pet, activeChar)
                    activeChar.target = pet
                } else
                    activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET)
            } else
                activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET)// Allow to target a player to find his pet - target the pet then.
        } else if (command.startsWith("admin_unsummon")) {
            val target = activeChar.target
            if (target is Summon)
                target.unSummon(target.owner)
            else
                activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET)
        } else if (command.startsWith("admin_summon_setlvl")) {
            val target = activeChar.target
            if (target is Pet) {
                try {
                    val level = Integer.parseInt(command.substring(20))

                    val oldExp = target.stat.exp
                    val newExp = (target.stat as PetStat).getExpForLevel(level)

                    if (oldExp > newExp)
                        (target.stat as PetStat).removeExp(oldExp - newExp)
                    else if (oldExp < newExp)
                        (target.stat as PetStat).addExp(newExp - oldExp)
                } catch (e: Exception) {
                }

            } else
                activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET)
        } else if (command.startsWith("admin_show_pet_inv")) {
            var target: WorldObject?
            try {
                target = World.getPet(Integer.parseInt(command.substring(19)))
            } catch (e: Exception) {
                target = activeChar.target
            }

            if (target is Pet)
                activeChar.sendPacket(GMViewItemList((target as Pet?)!!))
            else
                activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET)

        } else if (command.startsWith("admin_fullfood")) {
            val target = activeChar.target
            if (target is Pet) {
                target.currentFed = target.petData.maxMeal
            } else
                activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET)
        } else if (command.startsWith("admin_party_info")) {
            var target: WorldObject?
            try {
                target = World.getPlayer(command.substring(17))
                if (target == null)
                    target = activeChar.target
            } catch (e: Exception) {
                target = activeChar.target
            }

            if (target !is Player) {
                activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET)
                return false
            }

            val player = target as Player?

            val party = player?.party
            if (party == null) {
                activeChar.sendMessage(player?.name + " isn't in a party.")
                return false
            }

            val sb = StringBuilder(400)
            for (member in party.members) {
                if (!party.isLeader(member))
                    StringUtil.append(
                        sb,
                        "<tr><td width=150><a action=\"bypass -h admin_character_info ",
                        member.name,
                        "\">",
                        member.name,
                        " (",
                        member.level,
                        ")</a></td><td width=120 align=right>",
                        member.classId.toString(),
                        "</td></tr>"
                    )
                else
                    StringUtil.append(
                        sb,
                        "<tr><td width=150><a action=\"bypass -h admin_character_info ",
                        member.name,
                        "\"><font color=\"LEVEL\">",
                        member.name,
                        " (",
                        member.level,
                        ")</font></a></td><td width=120 align=right>",
                        member.classId.toString(),
                        "</td></tr>"
                    )
            }

            val html = NpcHtmlMessage(0)
            html.setFile("data/html/admin/partyinfo.htm")
            html.replace("%party%", sb.toString())
            activeChar.sendPacket(html)
        } else if (command.startsWith("admin_clan_info")) {
            try {
                val player = World.getPlayer(command.substring(16))
                if (player == null) {
                    activeChar.sendPacket(SystemMessageId.TARGET_CANT_FOUND)
                    return false
                }

                val clan = player.clan
                if (clan == null) {
                    activeChar.sendMessage("This player isn't in a clan.")
                    return false
                }

                val html = NpcHtmlMessage(0)
                html.setFile("data/html/admin/claninfo.htm")
                html.replace("%clan_name%", clan.name)
                html.replace("%clan_leader%", clan.leaderName)
                html.replace("%clan_level%", clan.level)
                html.replace(
                    "%clan_has_castle%",
                    if (clan.hasCastle()) CastleManager.getCastleById(clan.castleId)!!.name else "No"
                )
                html.replace(
                    "%clan_has_clanhall%",
                    if (clan.hasHideout()) ClanHallManager.getClanHallById(clan.hideoutId)!!.name else "No"
                )
                html.replace("%clan_points%", clan.reputationScore)
                html.replace("%clan_players_count%", clan.membersCount)
                html.replace("%clan_ally%", if (clan.allyId > 0) clan.allyName!! else "Not in ally")
                activeChar.sendPacket(html)
            } catch (e: Exception) {
                activeChar.sendPacket(SystemMessageId.TARGET_CANT_FOUND)
            }

        } else if (command.startsWith("admin_remove_clan_penalty")) {
            try {
                val st = StringTokenizer(command, " ")
                if (st.countTokens() != 3) {
                    activeChar.sendMessage("Usage: //remove_clan_penalty join|create charname")
                    return false
                }

                st.nextToken()

                val changeCreateExpiryTime = st.nextToken().equals("create", ignoreCase = true)
                val playerName = st.nextToken()

                val player = World.getPlayer(playerName)
                if (player == null) {
                    L2DatabaseFactory.connection.use { con ->
                        val ps =
                            con.prepareStatement("UPDATE characters SET " + (if (changeCreateExpiryTime) "clan_create_expiry_time" else "clan_join_expiry_time") + " WHERE char_name=? LIMIT 1")

                        ps.setString(1, playerName)
                        ps.execute()
                        ps.close()
                    }
                } else {
                    // removing penalty
                    if (changeCreateExpiryTime)
                        player.clanCreateExpiryTime = 0
                    else
                        player.clanJoinExpiryTime = 0
                }
                activeChar.sendMessage("Clan penalty is successfully removed for $playerName.")
            } catch (e: Exception) {
                activeChar.sendMessage("Couldn't remove clan penalty.")
            }

        }
        return true
    }

    override val adminCommandList: Array<String> get() = ADMIN_COMMANDS

    companion object {
        private val PAGE_LIMIT = 20

        private val ADMIN_COMMANDS = arrayOf(
            "admin_changelvl", // edit player access level
            "admin_edit_character",
            "admin_current_player",
            "admin_setkarma", // sets karma of target char to any amount. //setkarma <karma>
            "admin_character_info", // given a player name, displays an information window
            "admin_show_characters", // list of characters
            "admin_find_character", // find a player by his name or a part of it (case-insensitive)
            "admin_find_ip", // find all the player connections from a given IPv4 number
            "admin_find_account", // list all the characters from an account (useful for GMs w/o DB access)
            "admin_find_dualbox", // list all IPs with more than 1 char logged in (dualbox)
            "admin_rec", // gives recommendation points
            "admin_settitle", // changes char's title
            "admin_setname", // changes char's name
            "admin_setsex", // changes char's sex
            "admin_setcolor", // change char name's color
            "admin_settcolor", // change char title's color
            "admin_setclass", // changes char's classId

            "admin_summon_info", // displays an information window about target summon
            "admin_unsummon", // unsummon target's pet/summon
            "admin_summon_setlvl", // set the pet's level
            "admin_show_pet_inv", // show pet's inventory
            "admin_fullfood", // fulfills a pet's food bar

            "admin_party_info", // find party infos of targeted character, if any
            "admin_clan_info", // find clan infos of the character, if any
            "admin_remove_clan_penalty" // removes clan penalties
        )

        private fun onLineChange(activeChar: Player, player: Player, lvl: Int) {
            player.setAccessLevel(lvl)

            if (lvl >= 0)
                player.sendMessage("Your access level has been changed to $lvl.")
            else
                player.logout(false)

            activeChar.sendMessage(player.name + "'s access level is now set to " + lvl + ".")
        }

        private fun listCharacters(activeChar: Player, page: Int) {
            var players: List<Player> = ArrayList(World.players)

            val max = MathUtil.countPagesNumber(players.size, PAGE_LIMIT)

            players = players.subList((page - 1) * PAGE_LIMIT, Math.min(page * PAGE_LIMIT, players.size))

            val html = NpcHtmlMessage(0)
            html.setFile("data/html/admin/charlist.htm")

            val sb = StringBuilder(players.size * 200)

            // First use of sb.
            for (x in 0 until max) {
                val pagenr = x + 1
                if (page == pagenr)
                    StringUtil.append(sb, pagenr, "&nbsp;")
                else
                    StringUtil.append(
                        sb,
                        "<a action=\"bypass -h admin_show_characters ",
                        pagenr,
                        "\">",
                        pagenr,
                        "</a>&nbsp;"
                    )
            }
            html.replace("%pages%", sb.toString())

            // Cleanup current sb.
            sb.setLength(0)

            // Second use of sb, add player info into new table row.
            for (player in players)
                StringUtil.append(
                    sb,
                    "<tr><td width=80><a action=\"bypass -h admin_character_info ",
                    player.name,
                    "\">",
                    player.name,
                    "</a></td><td width=110>",
                    (player.template as PlayerTemplate).className,
                    "</td><td width=40>",
                    player.level,
                    "</td></tr>"
                )

            html.replace("%players%", sb.toString())
            activeChar.sendPacket(html)
        }

        fun showCharacterInfo(activeChar: Player, player: Player?) {
            var player = player
            if (player == null) {
                val target = activeChar.target as? Player ?: return

                player = target
            } else
                activeChar.target = player

            gatherCharacterInfo(activeChar, player, "charinfo.htm")
        }

        /**
         * Gather character informations.
         * @param activeChar The player who requested that action.
         * @param player The target to gather informations from.
         * @param filename The name of the HTM to send.
         */
        private fun gatherCharacterInfo(activeChar: Player, player: Player, filename: String) {
            val clientInfo = player.client.toString()
            val account = clientInfo.substring(clientInfo.indexOf("Account: ") + 9, clientInfo.indexOf(" - IP: "))
            val ip = clientInfo.substring(clientInfo.indexOf(" - IP: ") + 7, clientInfo.lastIndexOf("]"))

            val html = NpcHtmlMessage(0)
            html.setFile("data/html/admin/$filename")
            html.replace("%name%", player.name)
            html.replace("%level%", player.level)
            html.replace(
                "%clan%",
                if (player.clan != null) "<a action=\"bypass -h admin_clan_info " + player.name + "\">" + player.clan!!.name + "</a>" else "none"
            )
            html.replace("%xp%", player.exp)
            html.replace("%sp%", player.sp)
            html.replace("%class%", (player.template as PlayerTemplate).className)
            html.replace("%ordinal%", player.classId.ordinal)
            html.replace("%classid%", player.classId.toString())
            html.replace("%baseclass%", PlayerData.getClassNameById(player.baseClass))
            html.replace("%x%", player.x)
            html.replace("%y%", player.y)
            html.replace("%z%", player.z)
            html.replace("%currenthp%", player.currentHp.toInt())
            html.replace("%maxhp%", player.maxHp)
            html.replace("%karma%", player.karma)
            html.replace("%currentmp%", player.currentMp.toInt())
            html.replace("%maxmp%", player.maxMp)
            html.replace("%pvpflag%", player.pvpFlag.toInt())
            html.replace("%currentcp%", player.currentCp.toInt())
            html.replace("%maxcp%", player.maxCp)
            html.replace("%pvpkills%", player.pvpKills)
            html.replace("%pkkills%", player.pkKills)
            html.replace("%currentload%", player.currentLoad)
            html.replace("%maxload%", player.maxLoad)
            html.replace(
                "%percent%",
                MathUtil.roundTo(player.currentLoad.toFloat() / player.maxLoad.toFloat() * 100, 2).toDouble()
            )
            html.replace("%patk%", player.getPAtk(null))
            html.replace("%matk%", player.getMAtk(null, null))
            html.replace("%pdef%", player.getPDef(null))
            html.replace("%mdef%", player.getMDef(null, null))
            html.replace("%accuracy%", player.accuracy)
            html.replace("%evasion%", player.getEvasionRate(null))
            html.replace("%critical%", player.getCriticalHit(null, null))
            html.replace("%runspeed%", player.moveSpeed)
            html.replace("%patkspd%", player.pAtkSpd)
            html.replace("%matkspd%", player.mAtkSpd)
            html.replace("%account%", account)
            html.replace("%ip%", ip)
            html.replace("%ai%", player.ai.desire.intention.name)
            activeChar.sendPacket(html)
        }

        private fun setTargetKarma(activeChar: Player, newKarma: Int) {
            val target = activeChar.target as? Player ?: return

            if (newKarma >= 0) {
                val oldKarma = target.karma

                target.karma = newKarma
                activeChar.sendMessage("You changed " + target.name + "'s karma from " + oldKarma + " to " + newKarma + ".")
            } else
                activeChar.sendMessage("The karma value must be greater or equal to 0.")
        }

        private fun editCharacter(activeChar: Player) {
            val target = activeChar.target as? Player ?: return

            gatherCharacterInfo(activeChar, target, "charedit.htm")
        }

        /**
         * Find the character based on his name, and send back the result to activeChar.
         * @param activeChar The player to send back results.
         * @param characterToFind The name to search.
         */
        private fun findCharacter(activeChar: Player, characterToFind: String) {
            var charactersFound = 0

            val html = NpcHtmlMessage(0)
            html.setFile("data/html/admin/charfind.htm")

            val sb = StringBuilder()

            // First use of sb, add player info into new Table row
            for (player in World.players) {
                val name = player.name
                if (name.toLowerCase().contains(characterToFind.toLowerCase())) {
                    charactersFound++
                    StringUtil.append(
                        sb,
                        "<tr><td width=80><a action=\"bypass -h admin_character_info ",
                        name,
                        "\">",
                        name,
                        "</a></td><td width=110>",
                        (player.template as PlayerTemplate).className,
                        "</td><td width=40>",
                        player.level,
                        "</td></tr>"
                    )
                }

                if (charactersFound > 20)
                    break
            }
            html.replace("%results%", sb.toString())

            // Cleanup sb.
            sb.setLength(0)

            // Second use of sb.
            if (charactersFound == 0)
                sb.append("s. Please try again.")
            else if (charactersFound > 20) {
                html.replace("%number%", " more than 20.")
                sb.append("s.<br>Please refine your search to see all of the results.")
            } else if (charactersFound == 1)
                sb.append(".")
            else
                sb.append("s.")

            html.replace("%number%", charactersFound)
            html.replace("%end%", sb.toString())
            activeChar.sendPacket(html)
        }

        /**
         * @param activeChar
         * @param IpAdress
         * @throws IllegalArgumentException
         */
        @Throws(IllegalArgumentException::class)
        private fun findCharactersPerIp(activeChar: Player, IpAdress: String) {
            var findDisconnected = false

            if (IpAdress == "disconnected")
                findDisconnected = true
            else {
                if (!IpAdress.matches("^(?:(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2(?:[0-4][0-9]|5[0-5]))\\.){3}(?:[0-9]|[1-9][0-9]|1[0-9][0-9]|2(?:[0-4][0-9]|5[0-5]))$".toRegex()))
                    throw IllegalArgumentException("Malformed IPv4 number")
            }

            var charactersFound = 0
            var ip = "0.0.0.0"

            val html = NpcHtmlMessage(0)
            html.setFile("data/html/admin/ipfind.htm")

            val sb = StringBuilder(1000)
            for (player in World.players) {
                val client = player.client!!
                if (client.isDetached) {
                    if (!findDisconnected)
                        continue
                } else {
                    if (findDisconnected)
                        continue

                    ip = client!!.connection.inetAddress.hostAddress
                    if (ip != IpAdress)
                        continue
                }

                val name = player.name
                charactersFound++
                StringUtil.append(
                    sb,
                    "<tr><td width=80><a action=\"bypass -h admin_character_info ",
                    name,
                    "\">",
                    name,
                    "</a></td><td width=110>",
                    (player.template as PlayerTemplate).className,
                    "</td><td width=40>",
                    player.level,
                    "</td></tr>"
                )

                if (charactersFound > 20)
                    break
            }
            html.replace("%results%", sb.toString())

            val replyMSG2: String

            if (charactersFound == 0)
                replyMSG2 = "."
            else if (charactersFound > 20) {
                html.replace("%number%", " more than 20.")
                replyMSG2 = "s."
            } else if (charactersFound == 1)
                replyMSG2 = "."
            else
                replyMSG2 = "s."

            html.replace("%ip%", IpAdress)
            html.replace("%number%", charactersFound)
            html.replace("%end%", replyMSG2)
            activeChar.sendPacket(html)
        }

        /**
         * Returns accountinfo.htm with
         * @param activeChar
         * @param characterName
         */
        private fun findCharactersPerAccount(activeChar: Player, characterName: String) {
            val player = World.getPlayer(characterName)
            if (player == null) {
                activeChar.sendPacket(SystemMessageId.TARGET_CANT_FOUND)
                return
            }

            val html = NpcHtmlMessage(0)
            html.setFile("data/html/admin/accountinfo.htm")
            html.replace("%characters%", player.accountChars.values.joinToString("<br1>"))
            html.replace("%account%", player.accountName)
            html.replace("%player%", characterName)
            activeChar.sendPacket(html)
        }

        /**
         * @param activeChar
         * @param multibox
         */
        private fun findDualbox(activeChar: Player, multibox: Int) {
            val ipMap = HashMap<String, MutableList<Player>>()

            var ip = "0.0.0.0"

            val dualboxIPs = HashMap<String, Int>()

            for (player in World.players) {
                val client = player.client
                if (client == null || client.isDetached)
                    continue

                ip = client.connection.inetAddress.hostAddress
                if (ipMap[ip] == null)
                    ipMap[ip] = ArrayList()

                ipMap[ip]!!.add(player)

                if (ipMap[ip]!!.size >= multibox) {
                    var count: Int? = dualboxIPs[ip]
                    if (count == null)
                        dualboxIPs[ip] = multibox
                    else
                        dualboxIPs[ip] = count++!!
                }
            }

            val keys = ArrayList(dualboxIPs.keys)
            keys.sortWith(Comparator { left, right -> dualboxIPs[left]!!.compareTo(dualboxIPs[right]!!) })
            keys.reverse()

            val sb = StringBuilder()
            for (dualboxIP in keys)
                StringUtil.append(
                    sb,
                    "<a action=\"bypass -h admin_find_ip ",
                    dualboxIP,
                    "\">",
                    dualboxIP,
                    " (",
                    dualboxIPs[dualboxIP]!!,
                    ")</a><br1>"
                )

            val html = NpcHtmlMessage(0)
            html.setFile("data/html/admin/dualbox.htm")
            html.replace("%multibox%", multibox)
            html.replace("%results%", sb.toString())
            html.replace("%strict%", "")
            activeChar.sendPacket(html)
        }

        private fun gatherSummonInfo(target: Summon, activeChar: Player) {
            val name = target.name
            val owner = target.actingPlayer!!.name

            val html = NpcHtmlMessage(0)
            html.setFile("data/html/admin/petinfo.htm")
            html.replace("%name%", name ?: "N/A")
            html.replace("%level%", target.level)
            html.replace("%exp%", target.stat.exp)
            html.replace("%owner%", " <a action=\"bypass -h admin_character_info $owner\">$owner</a>")
            html.replace("%class%", target.javaClass.simpleName)
            html.replace("%ai%", if (target.hasAI()) target.ai.desire.intention.name else "NULL")
            html.replace("%hp%", target.status.currentHp.toInt().toString() + "/" + target.stat.maxHp)
            html.replace("%mp%", target.status.currentMp.toInt().toString() + "/" + target.stat.maxMp)
            html.replace("%karma%", target.karma)
            html.replace("%undead%", if (target.isUndead) "yes" else "no")

            if (target is Pet) {

                html.replace(
                    "%inv%",
                    " <a action=\"bypass admin_show_pet_inv " + target.actingPlayer!!.objectId + "\">view</a>"
                )
                html.replace("%food%", target.currentFed.toString() + "/" + target.petData.maxMeal)
                html.replace("%load%", target.inventory!!.totalWeight.toString() + "/" + target.maxLoad)
            } else {
                html.replace("%inv%", "none")
                html.replace("%food%", "N/A")
                html.replace("%load%", "N/A")
            }
            activeChar.sendPacket(html)
        }
    }
}