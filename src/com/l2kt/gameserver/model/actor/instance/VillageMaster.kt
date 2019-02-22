package com.l2kt.gameserver.model.actor.instance

import com.l2kt.Config
import com.l2kt.commons.lang.StringUtil
import com.l2kt.gameserver.data.manager.CastleManager
import com.l2kt.gameserver.data.sql.ClanTable
import com.l2kt.gameserver.data.xml.PlayerData
import com.l2kt.gameserver.data.xml.SkillTreeData
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.model.base.ClassId
import com.l2kt.gameserver.model.olympiad.OlympiadManager
import com.l2kt.gameserver.model.pledge.Clan
import com.l2kt.gameserver.model.pledge.ClanMember
import com.l2kt.gameserver.network.FloodProtectors
import com.l2kt.gameserver.network.FloodProtectors.Action
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.*
import com.l2kt.gameserver.network.serverpackets.AcquireSkillList.AcquireSkillType

/**
 * The generic villagemaster. Some childs instances depends of it for race/classe restriction.
 */
open class VillageMaster(objectId: Int, template: NpcTemplate) : Folk(objectId, template) {

    override fun getHtmlPath(npcId: Int, `val`: Int): String {
        var filename = ""

        if (`val` == 0)
            filename = "" + npcId
        else
            filename = "$npcId-$`val`"

        return "data/html/villagemaster/$filename.htm"
    }

    override fun onBypassFeedback(player: Player, command: String) {
        val commandStr = command.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val actualCommand = commandStr[0]

        var cmdParams = ""
        var cmdParams2 = ""

        if (commandStr.size >= 2)
            cmdParams = commandStr[1]
        if (commandStr.size >= 3)
            cmdParams2 = commandStr[2]

        if (actualCommand.equals("create_clan", ignoreCase = true)) {
            if (cmdParams.isEmpty())
                return

            ClanTable.createClan(player, cmdParams)
        } else if (actualCommand.equals("create_academy", ignoreCase = true)) {
            if (cmdParams.isEmpty())
                return

            createSubPledge(player, cmdParams, null, Clan.SUBUNIT_ACADEMY, 5)
        } else if (actualCommand.equals("rename_pledge", ignoreCase = true)) {
            if (cmdParams.isEmpty() || cmdParams2.isEmpty())
                return

            if (!player.isClanLeader) {
                player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT)
                return
            }

            val clan = player.clan
            val subPledge = player.clan.getSubPledge(Integer.valueOf(cmdParams))

            if (subPledge == null) {
                player.sendMessage("Pledge doesn't exist.")
                return
            }

            if (!StringUtil.isAlphaNumeric(cmdParams2)) {
                player.sendPacket(SystemMessageId.CLAN_NAME_INVALID)
                return
            }

            if (cmdParams2.length < 2 || cmdParams2.length > 16) {
                player.sendPacket(SystemMessageId.CLAN_NAME_LENGTH_INCORRECT)
                return
            }

            subPledge.name = cmdParams2
            clan.updateSubPledgeInDB(subPledge)
            clan.broadcastToOnlineMembers(PledgeShowMemberListAll(clan, subPledge.id))
            player.sendMessage("Pledge name have been changed to: $cmdParams2")
        } else if (actualCommand.equals("create_royal", ignoreCase = true)) {
            if (cmdParams.isEmpty())
                return

            createSubPledge(player, cmdParams, cmdParams2, Clan.SUBUNIT_ROYAL1, 6)
        } else if (actualCommand.equals("create_knight", ignoreCase = true)) {
            if (cmdParams.isEmpty())
                return

            createSubPledge(player, cmdParams, cmdParams2, Clan.SUBUNIT_KNIGHT1, 7)
        } else if (actualCommand.equals("assign_subpl_leader", ignoreCase = true)) {
            if (cmdParams.isEmpty())
                return

            assignSubPledgeLeader(player, cmdParams, cmdParams2)
        } else if (actualCommand.equals("create_ally", ignoreCase = true)) {
            if (cmdParams.isEmpty())
                return

            if (player.clan == null)
                player.sendPacket(SystemMessageId.ONLY_CLAN_LEADER_CREATE_ALLIANCE)
            else
                player.clan.createAlly(player, cmdParams)
        } else if (actualCommand.equals("dissolve_ally", ignoreCase = true)) {
            player.clan.dissolveAlly(player)
        } else if (actualCommand.equals("dissolve_clan", ignoreCase = true)) {
            if (!player.isClanLeader) {
                player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT)
                return
            }

            val clan = player.clan
            if (clan.allyId != 0) {
                player.sendPacket(SystemMessageId.CANNOT_DISPERSE_THE_CLANS_IN_ALLY)
                return
            }

            if (clan.isAtWar) {
                player.sendPacket(SystemMessageId.CANNOT_DISSOLVE_WHILE_IN_WAR)
                return
            }

            if (clan.hasCastle() || clan.hasHideout()) {
                player.sendPacket(SystemMessageId.CANNOT_DISSOLVE_WHILE_OWNING_CLAN_HALL_OR_CASTLE)
                return
            }

            for (castle in CastleManager.castles) {
                if (castle.siege.checkSides(clan)) {
                    player.sendPacket(if (castle.siege.isInProgress) SystemMessageId.CANNOT_DISSOLVE_WHILE_IN_SIEGE else SystemMessageId.CANNOT_DISSOLVE_CAUSE_CLAN_WILL_PARTICIPATE_IN_CASTLE_SIEGE)
                    return
                }
            }

            if (clan.dissolvingExpiryTime > System.currentTimeMillis()) {
                player.sendPacket(SystemMessageId.DISSOLUTION_IN_PROGRESS)
                return
            }

            if (Config.ALT_CLAN_DISSOLVE_DAYS > 0) {
                clan.dissolvingExpiryTime = System.currentTimeMillis() + Config.ALT_CLAN_DISSOLVE_DAYS * 86400000L
                clan.updateClanInDB()

                ClanTable.scheduleRemoveClan(clan)
            } else
                ClanTable.destroyClan(clan)

            // The clan leader should take the XP penalty of a full death.
            player.deathPenalty(false, false, false)
        } else if (actualCommand.equals("change_clan_leader", ignoreCase = true)) {
            if (cmdParams.isEmpty())
                return

            if (!player.isClanLeader) {
                player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT)
                return
            }

            if (player.name.equals(cmdParams, ignoreCase = true))
                return

            val clan = player.clan
            val member = clan.getClanMember(cmdParams)

            if (member == null) {
                player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DOES_NOT_EXIST).addString(cmdParams))
                return
            }

            if (!member.isOnline) {
                player.sendPacket(SystemMessageId.INVITED_USER_NOT_ONLINE)
                return
            }

            if (member.pledgeType != 0) {
                player.sendMessage("Selected member cannot be found in main clan.")
                return
            }

            val html = NpcHtmlMessage(objectId)
            if (clan.newLeaderId == 0) {
                clan.setNewLeaderId(member.objectId, true)
                html.setFile("data/html/scripts/village_master/Clan/9000-07-success.htm")
            } else
                html.setFile("data/html/scripts/village_master/Clan/9000-07-in-progress.htm")

            player.sendPacket(html)
        } else if (actualCommand.equals("cancel_clan_leader_change", ignoreCase = true)) {
            if (!player.isClanLeader) {
                player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT)
                return
            }

            val clan = player.clan
            val html = NpcHtmlMessage(objectId)
            if (clan.newLeaderId != 0) {
                clan.setNewLeaderId(0, true)
                html.setFile("data/html/scripts/village_master/Clan/9000-08-success.htm")
            } else
                html.setFile("data/html/scripts/village_master/Clan/9000-08-no.htm")

            player.sendPacket(html)
        } else if (actualCommand.equals("recover_clan", ignoreCase = true)) {
            if (!player.isClanLeader) {
                player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT)
                return
            }

            val clan = player.clan
            clan.dissolvingExpiryTime = 0
            clan.updateClanInDB()
        } else if (actualCommand.equals("increase_clan_level", ignoreCase = true)) {
            if (player.clan.levelUpClan(player))
                player.broadcastPacket(MagicSkillUse(player, player, 5103, 1, 0, 0))
        } else if (actualCommand.equals("learn_clan_skills", ignoreCase = true)) {
            showPledgeSkillList(player)
        } else if (command.startsWith("Subclass")) {
            // Subclasses may not be changed while a skill is in use.
            if (player.isCastingNow || player.isAllSkillsDisabled) {
                player.sendPacket(SystemMessageId.SUBCLASS_NO_CHANGE_OR_CREATE_WHILE_SKILL_IN_USE)
                return
            }

            // Affecting subclasses (add/del/change) if registered in Olympiads makes you ineligible to compete.
            if (OlympiadManager.isRegisteredInComp(player))
                OlympiadManager.unRegisterNoble(player)

            val html = NpcHtmlMessage(objectId)

            var cmdChoice = 0
            var paramOne = 0
            var paramTwo = 0

            try {
                cmdChoice = Integer.parseInt(command.substring(9, 10).trim { it <= ' ' })

                var endIndex = command.indexOf(' ', 11)
                if (endIndex == -1)
                    endIndex = command.length

                paramOne = Integer.parseInt(command.substring(11, endIndex).trim { it <= ' ' })
                if (command.length > endIndex)
                    paramTwo = Integer.parseInt(command.substring(endIndex).trim { it <= ' ' })
            } catch (NumberFormatException: Exception) {
            }

            when (cmdChoice) {
                0 // Subclass change menu
                -> html.setFile("data/html/villagemaster/SubClass.htm")

                1 // Add Subclass - Initial
                -> run {
                    // Subclasses may not be added while a summon is active.
                    if (player.pet != null) {
                        player.sendPacket(SystemMessageId.CANT_SUBCLASS_WITH_SUMMONED_SERVITOR)
                        return
                    }

                    // Subclasses may not be added while you are over your weight limit.
                    if (player.inventoryLimit * 0.8 <= player.inventory!!.size || player.weightPenalty > 2) {
                        player.sendPacket(SystemMessageId.NOT_SUBCLASS_WHILE_OVERWEIGHT)
                        return
                    }

                    // Avoid giving player an option to add a new sub class, if they have three already.
                    if (player.subClasses.size >= 3) {
                        html.setFile("data/html/villagemaster/SubClass_Fail.htm")
                        return@run
                    }

                    val subsAvailable = getAvailableSubClasses(player)
                    if (subsAvailable == null || subsAvailable.isEmpty()) {
                        player.sendMessage("There are no sub classes available at this time.")
                        return
                    }

                    val sb = StringBuilder(300)
                    for (subClass in subsAvailable)
                        StringUtil.append(
                            sb,
                            "<a action=\"bypass -h npc_%objectId%_Subclass 4 ",
                            subClass.id,
                            "\" msg=\"1268;",
                            subClass,
                            "\">",
                            subClass,
                            "</a><br>"
                        )

                    html.setFile("data/html/villagemaster/SubClass_Add.htm")
                    html.replace("%list%", sb.toString())
                }

                2 // Change Class - Initial
                -> {
                    // Subclasses may not be changed while a summon is active.
                    if (player.pet != null) {
                        player.sendPacket(SystemMessageId.CANT_SUBCLASS_WITH_SUMMONED_SERVITOR)
                        return
                    }

                    // Subclasses may not be changed while a you are over your weight limit.
                    if (player.inventoryLimit * 0.8 <= player.inventory!!.size || player.weightPenalty > 2) {
                        player.sendPacket(SystemMessageId.NOT_SUBCLASS_WHILE_OVERWEIGHT)
                        return
                    }

                    if (player.subClasses.isEmpty())
                        html.setFile("data/html/villagemaster/SubClass_ChangeNo.htm")
                    else {
                        val sb = StringBuilder(300)

                        if (checkVillageMaster(player.baseClass))
                            StringUtil.append(
                                sb,
                                "<a action=\"bypass -h npc_%objectId%_Subclass 5 0\">",
                                PlayerData.getClassNameById(player.baseClass),
                                "</a><br>"
                            )

                        for (subclass in player.subClasses.values) {
                            if (checkVillageMaster(subclass.classDefinition))
                                StringUtil.append(
                                    sb,
                                    "<a action=\"bypass -h npc_%objectId%_Subclass 5 ",
                                    subclass.classIndex,
                                    "\">",
                                    subclass.classDefinition,
                                    "</a><br>"
                                )
                        }

                        if (sb.length > 0) {
                            html.setFile("data/html/villagemaster/SubClass_Change.htm")
                            html.replace("%list%", sb.toString())
                        } else
                            html.setFile("data/html/villagemaster/SubClass_ChangeNotFound.htm")
                    }
                }

                3 // Change/Cancel Subclass - Initial
                -> run {
                    if (player.subClasses.isEmpty()) {
                        html.setFile("data/html/villagemaster/SubClass_ModifyEmpty.htm")
                        return@run
                    }

                    html.setFile("data/html/villagemaster/SubClass_Modify.htm")
                    if (player.subClasses.containsKey(1))
                        html.replace("%sub1%", PlayerData.getClassNameById(player.subClasses[1]!!.classId))
                    else
                        html.replace("<a action=\"bypass -h npc_%objectId%_Subclass 6 1\">%sub1%</a><br>", "")

                    if (player.subClasses.containsKey(2))
                        html.replace("%sub2%", PlayerData.getClassNameById(player.subClasses[2]!!.classId))
                    else
                        html.replace("<a action=\"bypass -h npc_%objectId%_Subclass 6 2\">%sub2%</a><br>", "")

                    if (player.subClasses.containsKey(3))
                        html.replace("%sub3%", PlayerData.getClassNameById(player.subClasses[3]!!.classId))
                    else
                        html.replace("<a action=\"bypass -h npc_%objectId%_Subclass 6 3\">%sub3%</a><br>", "")
                }

                4 // Add Subclass - Action (Subclass 4 x[x])
                -> {
                    if (!FloodProtectors.performAction(player.client, Action.SUBCLASS))
                        return

                    var allowAddition = true

                    if (player.subClasses.size >= 3)
                        allowAddition = false

                    if (player.level < 75)
                        allowAddition = false

                    if (allowAddition) {
                        for (subclass in player.subClasses.values) {
                            if (subclass.level < 75) {
                                allowAddition = false
                                break
                            }
                        }
                    }

                    // Verify if the character has completed the Mimir's Elixir (Path to Subclass) and Fate's Whisper (A Grade Weapon) quests by checking for instances of their unique reward items.
                    if (allowAddition && !Config.SUBCLASS_WITHOUT_QUESTS)
                        allowAddition = checkQuests(player)

                    // If they both exist, remove both unique items and continue with adding the subclass.
                    if (allowAddition && isValidNewSubClass(player, paramOne)) {
                        if (!player.addSubClass(paramOne, player.subClasses.size + 1))
                            return

                        player.activeClass = player.subClasses.size

                        html.setFile("data/html/villagemaster/SubClass_AddOk.htm")
                        player.sendPacket(SystemMessageId.ADD_NEW_SUBCLASS) // Subclass added.
                    } else
                        html.setFile("data/html/villagemaster/SubClass_Fail.htm")
                }

                5 // Change Class - Action
                -> run {
                    if (!FloodProtectors.performAction(player.client, Action.SUBCLASS))
                        return

                    if (player.classIndex == paramOne) {
                        html.setFile("data/html/villagemaster/SubClass_Current.htm")
                        return@run
                    }

                    if (paramOne == 0) {
                        if (!checkVillageMaster(player.baseClass))
                            return
                    } else {
                        try {
                            if (!checkVillageMaster(player.subClasses[paramOne]!!.classDefinition))
                                return
                        } catch (e: NullPointerException) {
                            return
                        }

                    }

                    player.activeClass = paramOne

                    player.sendPacket(SystemMessageId.SUBCLASS_TRANSFER_COMPLETED) // Transfer completed.
                    return
                }

                6 // Change/Cancel Subclass - Choice
                -> {
                    // validity check
                    if (paramOne < 1 || paramOne > 3)
                        return

                    val subsAvailable = getAvailableSubClasses(player)

                    // another validity check
                    if (subsAvailable == null || subsAvailable.isEmpty()) {
                        player.sendMessage("There are no sub classes available at this time.")
                        return
                    }

                    val sb = StringBuilder(300)
                    for (subClass in subsAvailable)
                        StringUtil.append(
                            sb,
                            "<a action=\"bypass -h npc_%objectId%_Subclass 7 ",
                            paramOne,
                            " ",
                            subClass.id,
                            "\" msg=\"1445;",
                            "\">",
                            subClass,
                            "</a><br>"
                        )

                    when (paramOne) {
                        1 -> html.setFile("data/html/villagemaster/SubClass_ModifyChoice1.htm")

                        2 -> html.setFile("data/html/villagemaster/SubClass_ModifyChoice2.htm")

                        3 -> html.setFile("data/html/villagemaster/SubClass_ModifyChoice3.htm")

                        else -> html.setFile("data/html/villagemaster/SubClass_ModifyChoice.htm")
                    }
                    html.replace("%list%", sb.toString())
                }

                7 // Change Subclass - Action
                -> {
                    if (!FloodProtectors.performAction(player.client, Action.SUBCLASS))
                        return

                    if (!isValidNewSubClass(player, paramTwo))
                        return

                    if (player.modifySubClass(paramOne, paramTwo)) {
                        player.abortCast()
                        player.stopAllEffectsExceptThoseThatLastThroughDeath() // all effects from old subclass stopped!
                        player.stopCubics()
                        player.activeClass = paramOne

                        html.setFile("data/html/villagemaster/SubClass_ModifyOk.htm")
                        player.sendPacket(SystemMessageId.ADD_NEW_SUBCLASS) // Subclass added.
                    } else {
                        player.activeClass = 0 // Also updates _classIndex plus switching _classid to baseclass.

                        player.sendMessage("The sub class could not be added, you have been reverted to your base class.")
                        return
                    }
                }
            }

            html.replace("%objectId%", objectId)
            player.sendPacket(html)
        } else {
            // this class dont know any other commands, let forward the command to the parent class
            super.onBypassFeedback(player, command)
        }
    }

    protected fun checkQuests(player: Player): Boolean {
        // Noble players can add subbclasses without quests
        if (player.isNoble)
            return true

        var qs = player.getQuestState("Q234_FatesWhisper")
        if (qs == null || !qs.isCompleted)
            return false

        qs = player.getQuestState("Q235_MimirsElixir")
        return !(qs == null || !qs.isCompleted)

    }

    private fun getAvailableSubClasses(player: Player): Set<ClassId>? {
        val availSubs = ClassId.getAvailableSubclasses(player)

        if (availSubs != null && !availSubs.isEmpty()) {
            val availSub = availSubs.iterator()
            while (availSub.hasNext()) {
                val classId = availSub.next()

                // check for the village master
                if (!checkVillageMaster(classId)) {
                    availSub.remove()
                    continue
                }

                // scan for already used subclasses
                for (subclass in player.subClasses.values) {
                    if (subclass.classDefinition.equalsOrChildOf(classId)) {
                        availSub.remove()
                        break
                    }
                }
            }
        }

        return availSubs
    }

    /**
     * Check subclass classId for validity (villagemaster race/type is not contains in previous subclasses, but in allowed subclasses). Base class not added into allowed subclasses.
     * @param player : The player to check.
     * @param classId : The class id to check.
     * @return true if the [Player] can pick this subclass id.
     */
    private fun isValidNewSubClass(player: Player, classId: Int): Boolean {
        if (!checkVillageMaster(classId))
            return false

        val cid = ClassId.VALUES[classId]
        for (subclass in player.subClasses.values) {
            if (subclass.classDefinition.equalsOrChildOf(cid))
                return false
        }

        val availSubs = ClassId.getAvailableSubclasses(player)
        if (availSubs == null || availSubs.isEmpty())
            return false

        var found = false
        for (pclass in availSubs) {
            if (pclass.id == classId) {
                found = true
                break
            }
        }

        return found
    }

    protected open fun checkVillageMasterRace(pclass: ClassId?): Boolean {
        return true
    }

    protected open fun checkVillageMasterTeachType(pclass: ClassId?): Boolean {
        return true
    }

    fun checkVillageMaster(classId: Int): Boolean {
        return checkVillageMaster(ClassId.VALUES[classId])
    }

    fun checkVillageMaster(pclass: ClassId): Boolean {
        return checkVillageMasterRace(pclass) && checkVillageMasterTeachType(pclass)
    }

    companion object {

        private fun createSubPledge(
            player: Player,
            clanName: String,
            leaderName: String?,
            pledgeType: Int,
            minClanLvl: Int
        ) {
            if (!player.isClanLeader) {
                player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT)
                return
            }

            val clan = player.clan
            if (clan.level < minClanLvl) {
                if (pledgeType == Clan.SUBUNIT_ACADEMY)
                    player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_CRITERIA_IN_ORDER_TO_CREATE_A_CLAN_ACADEMY)
                else
                    player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_CRITERIA_IN_ORDER_TO_CREATE_A_MILITARY_UNIT)

                return
            }

            if (!StringUtil.isAlphaNumeric(clanName)) {
                player.sendPacket(SystemMessageId.CLAN_NAME_INVALID)
                return
            }

            if (clanName.length < 2 || clanName.length > 16) {
                player.sendPacket(SystemMessageId.CLAN_NAME_LENGTH_INCORRECT)
                return
            }

            for (tempClan in ClanTable.clans) {
                if (tempClan.getSubPledge(clanName) != null) {
                    if (pledgeType == Clan.SUBUNIT_ACADEMY)
                        player.sendPacket(
                            SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_EXISTS).addString(
                                clanName
                            )
                        )
                    else
                        player.sendPacket(SystemMessageId.ANOTHER_MILITARY_UNIT_IS_ALREADY_USING_THAT_NAME)

                    return
                }
            }

            if (pledgeType != Clan.SUBUNIT_ACADEMY) {
                if (clan.getClanMember(leaderName!!) == null || clan.getClanMember(leaderName)!!.pledgeType != 0) {
                    if (pledgeType >= Clan.SUBUNIT_KNIGHT1)
                        player.sendPacket(SystemMessageId.CAPTAIN_OF_ORDER_OF_KNIGHTS_CANNOT_BE_APPOINTED)
                    else if (pledgeType >= Clan.SUBUNIT_ROYAL1)
                        player.sendPacket(SystemMessageId.CAPTAIN_OF_ROYAL_GUARD_CANNOT_BE_APPOINTED)

                    return
                }
            }

            val leaderId = if (pledgeType != Clan.SUBUNIT_ACADEMY) clan.getClanMember(leaderName!!)!!.objectId else 0
            if (clan.createSubPledge(player, pledgeType, leaderId, clanName) == null)
                return

            val sm: SystemMessage
            if (pledgeType == Clan.SUBUNIT_ACADEMY) {
                sm = SystemMessage.getSystemMessage(SystemMessageId.THE_S1S_CLAN_ACADEMY_HAS_BEEN_CREATED)
                sm.addString(player.clan.name)
            } else if (pledgeType >= Clan.SUBUNIT_KNIGHT1) {
                sm = SystemMessage.getSystemMessage(SystemMessageId.THE_KNIGHTS_OF_S1_HAVE_BEEN_CREATED)
                sm.addString(player.clan.name)
            } else if (pledgeType >= Clan.SUBUNIT_ROYAL1) {
                sm = SystemMessage.getSystemMessage(SystemMessageId.THE_ROYAL_GUARD_OF_S1_HAVE_BEEN_CREATED)
                sm.addString(player.clan.name)
            } else
                sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_CREATED)
            player.sendPacket(sm)

            if (pledgeType != Clan.SUBUNIT_ACADEMY) {
                val leaderSubPledge = clan.getClanMember(leaderName!!)
                val leaderPlayer = leaderSubPledge!!.playerInstance
                if (leaderPlayer != null) {
                    leaderPlayer.pledgeClass = ClanMember.calculatePledgeClass(leaderPlayer)
                    leaderPlayer.sendPacket(UserInfo(leaderPlayer))
                }
            }
        }

        private fun assignSubPledgeLeader(player: Player, clanName: String, leaderName: String) {
            if (!player.isClanLeader) {
                player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT)
                return
            }

            if (leaderName.length > 16) {
                player.sendPacket(SystemMessageId.NAMING_CHARNAME_UP_TO_16CHARS)
                return
            }

            if (player.name == leaderName) {
                player.sendPacket(SystemMessageId.CAPTAIN_OF_ROYAL_GUARD_CANNOT_BE_APPOINTED)
                return
            }

            val clan = player.clan
            val subPledge = player.clan.getSubPledge(clanName)

            if (null == subPledge || subPledge.id == Clan.SUBUNIT_ACADEMY) {
                player.sendPacket(SystemMessageId.CLAN_NAME_INVALID)
                return
            }

            val leaderSubPledge = clan.getClanMember(leaderName)

            if (leaderSubPledge == null || leaderSubPledge.pledgeType != 0) {
                if (subPledge.id >= Clan.SUBUNIT_KNIGHT1)
                    player.sendPacket(SystemMessageId.CAPTAIN_OF_ORDER_OF_KNIGHTS_CANNOT_BE_APPOINTED)
                else if (subPledge.id >= Clan.SUBUNIT_ROYAL1)
                    player.sendPacket(SystemMessageId.CAPTAIN_OF_ROYAL_GUARD_CANNOT_BE_APPOINTED)

                return
            }

            // Avoid naming sub pledges with the same captain
            if (clan.isSubPledgeLeader(leaderSubPledge.objectId)) {
                if (subPledge.id >= Clan.SUBUNIT_KNIGHT1)
                    player.sendPacket(SystemMessageId.CAPTAIN_OF_ORDER_OF_KNIGHTS_CANNOT_BE_APPOINTED)
                else if (subPledge.id >= Clan.SUBUNIT_ROYAL1)
                    player.sendPacket(SystemMessageId.CAPTAIN_OF_ROYAL_GUARD_CANNOT_BE_APPOINTED)

                return
            }

            subPledge.leaderId = leaderSubPledge.objectId
            clan.updateSubPledgeInDB(subPledge)

            val leaderPlayer = leaderSubPledge.playerInstance
            if (leaderPlayer != null) {
                leaderPlayer.pledgeClass = ClanMember.calculatePledgeClass(leaderPlayer)
                leaderPlayer.sendPacket(UserInfo(leaderPlayer))
            }

            clan.broadcastToOnlineMembers(
                PledgeShowMemberListAll(clan, subPledge.id),
                SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_BEEN_SELECTED_AS_CAPTAIN_OF_S2).addString(
                    leaderName
                ).addString(clanName)
            )
        }

        fun showPledgeSkillList(player: Player) {
            if (!player.isClanLeader) {
                val html = NpcHtmlMessage(0)
                html.setFile("data/html/scripts/village_master/Clan/9000-09-no.htm")
                player.sendPacket(html)
                player.sendPacket(ActionFailed.STATIC_PACKET)
                return
            }

            val skills = SkillTreeData.getClanSkillsFor(player)
            if (skills.isEmpty()) {
                val html = NpcHtmlMessage(0)
                html.setFile("data/html/scripts/village_master/Clan/9000-09-no.htm")
                player.sendPacket(html)
            } else
                player.sendPacket(AcquireSkillList(AcquireSkillType.CLAN, skills))

            player.sendPacket(ActionFailed.STATIC_PACKET)
        }
    }
}