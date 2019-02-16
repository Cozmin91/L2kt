package com.l2kt.gameserver.network.clientpackets

import com.l2kt.Config
import com.l2kt.L2DatabaseFactory
import com.l2kt.gameserver.communitybbs.Manager.MailBBSManager
import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.data.manager.CastleManager
import com.l2kt.gameserver.data.manager.CoupleManager
import com.l2kt.gameserver.data.manager.DimensionalRiftManager
import com.l2kt.gameserver.data.manager.PetitionManager
import com.l2kt.gameserver.data.xml.AdminData
import com.l2kt.gameserver.data.xml.AnnouncementData
import com.l2kt.gameserver.data.xml.MapRegionData
import com.l2kt.gameserver.data.xml.ScriptData
import com.l2kt.gameserver.instancemanager.ClanHallManager
import com.l2kt.gameserver.instancemanager.SevenSigns
import com.l2kt.gameserver.instancemanager.SevenSigns.CabalType
import com.l2kt.gameserver.instancemanager.SevenSigns.SealType
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.model.entity.Siege
import com.l2kt.gameserver.model.olympiad.Olympiad
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.*
import com.l2kt.gameserver.scripting.QuestState
import com.l2kt.gameserver.taskmanager.GameTimeTaskManager

class EnterWorld : L2GameClientPacket() {

    override fun readImpl() {}

    override fun runImpl() {
        val player = client.activeChar
        if (player == null) {
            client.closeNow()
            return
        }

        val objectId = player.objectId

        if (player.isGM) {
            if (Config.GM_STARTUP_INVULNERABLE && AdminData.hasAccess(
                    "admin_setinvul",
                    player.accessLevel
                )
            )
                player.setIsMortal(false)

            if (Config.GM_STARTUP_INVISIBLE && AdminData.hasAccess("admin_hide", player.accessLevel))
                player.appearance.setInvisible()

            if (Config.GM_STARTUP_SILENCE && AdminData.hasAccess("admin_silence", player.accessLevel))
                player.isInRefusalMode = true

            if (Config.GM_STARTUP_AUTO_LIST && AdminData.hasAccess("admin_gmlist", player.accessLevel))
                AdminData.addGm(player, false)
            else
                AdminData.addGm(player, true)
        }

        // Set dead status if applies
        if (player.currentHp < 0.5 && player.isMortal)
            player.setIsDead(true)

        // Clan checks.
        val clan = player.clan
        if (clan != null) {
            player.sendPacket(PledgeSkillList(clan))

            // Refresh player instance.
            clan.getClanMember(objectId)?.playerInstance = player

            val msg = SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_LOGGED_IN).addCharName(player)
            val update = PledgeShowMemberListUpdate(player)

            // Send packets to others members.
            for (member in clan.onlineMembers) {
                if (member == player)
                    continue

                member.sendPacket(msg)
                member.sendPacket(update)
            }

            // Send a login notification to sponsor or apprentice, if logged.
            if (player.sponsor != 0) {
                val sponsor = World.getPlayer(player.sponsor)
                sponsor?.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.YOUR_APPRENTICE_S1_HAS_LOGGED_IN).addCharName(
                        player
                    )
                )
            } else if (player.apprentice != 0) {
                val apprentice = World.getPlayer(player.apprentice)
                apprentice?.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.YOUR_SPONSOR_S1_HAS_LOGGED_IN).addCharName(
                        player
                    )
                )
            }

            // Add message at connexion if clanHall not paid.
            val clanHall = ClanHallManager.getInstance().getClanHallByOwner(clan)
            if (clanHall != null && !clanHall.paid)
                player.sendPacket(SystemMessageId.PAYMENT_FOR_YOUR_CLAN_HALL_HAS_NOT_BEEN_MADE_PLEASE_MAKE_PAYMENT_TO_YOUR_CLAN_WAREHOUSE_BY_S1_TOMORROW)

            for (castle in CastleManager.castles) {
                val siege = castle.siege
                if (!siege.isInProgress)
                    continue

                val type = siege.getSide(clan)
                if (type == Siege.SiegeSide.ATTACKER)
                    player.siegeState = 1.toByte()
                else if (type == Siege.SiegeSide.DEFENDER || type == Siege.SiegeSide.OWNER)
                    player.siegeState = 2.toByte()
            }

            player.sendPacket(PledgeShowMemberListAll(clan, 0))

            for (sp in clan.allSubPledges)
                player.sendPacket(PledgeShowMemberListAll(clan, sp.id))

            player.sendPacket(UserInfo(player))
            player.sendPacket(PledgeStatusChanged(clan))
        }

        // Updating Seal of Strife Buff/Debuff
        if (SevenSigns.isSealValidationPeriod && SevenSigns.getSealOwner(SealType.STRIFE) != CabalType.NORMAL) {
            val cabal = SevenSigns.getPlayerCabal(objectId)
            if (cabal != CabalType.NORMAL) {
                if (cabal == SevenSigns.getSealOwner(SealType.STRIFE))
                    player.addSkill(SkillTable.FrequentSkill.THE_VICTOR_OF_WAR.skill, false)
                else
                    player.addSkill(SkillTable.FrequentSkill.THE_VANQUISHED_OF_WAR.skill, false)
            }
        } else {
            player.removeSkill(SkillTable.FrequentSkill.THE_VICTOR_OF_WAR.skill!!.id, false)
            player.removeSkill(SkillTable.FrequentSkill.THE_VANQUISHED_OF_WAR.skill!!.id, false)
        }

        if (Config.PLAYER_SPAWN_PROTECTION > 0)
            player.setSpawnProtection(true)

        player.spawnMe()

        // Engage and notify partner.
        if (Config.ALLOW_WEDDING) {
            for ((key, couple) in CoupleManager.couples) {
                if (couple.id == objectId || couple.value == objectId) {
                    player.coupleId = key
                    break
                }
            }
        }

        // Announcements, welcome & Seven signs period messages
        player.sendPacket(SystemMessageId.WELCOME_TO_LINEAGE)
        player.sendPacket(SevenSigns.currentPeriod.messageId)
        AnnouncementData.showAnnouncements(player, false)

        // if player is DE, check for shadow sense skill at night
        if (player.race == ClassRace.DARK_ELF && player.hasSkill(L2Skill.SKILL_SHADOW_SENSE))
            player.sendPacket(
                SystemMessage.getSystemMessage(if (GameTimeTaskManager.isNight) SystemMessageId.NIGHT_S1_EFFECT_APPLIES else SystemMessageId.DAY_S1_EFFECT_DISAPPEARS).addSkillName(
                    L2Skill.SKILL_SHADOW_SENSE
                )
            )

        player.macroses.sendUpdate()
        player.sendPacket(UserInfo(player))
        player.sendPacket(HennaInfo(player))
        player.sendPacket(FriendList(player))
        // activeChar.queryGameGuard();
        player.sendPacket(ItemList(player, false))
        player.sendPacket(ShortCutInit(player))
        player.sendPacket(ExStorageMaxCount(player))

        // no broadcast needed since the player will already spawn dead to others
        if (player.isAlikeDead)
            player.sendPacket(Die(player))

        player.updateEffectIcons()
        player.sendPacket(EtcStatusUpdate(player))
        player.sendSkillList()

        // Load quests.
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(LOAD_PLAYER_QUESTS).use { ps ->
                    ps.setInt(1, objectId)

                    ps.executeQuery().use { rs ->
                        while (rs.next()) {
                            val questName = rs.getString("name")

                            // Test quest existence.
                            val quest = ScriptData.getQuest(questName)
                            if (quest == null) {
                                L2GameClientPacket.LOGGER.warn(
                                    "Unknown quest {} for player {}.",
                                    questName,
                                    player.name
                                )
                                continue
                            }

                            // Each quest get a single state ; create one QuestState per found <state> variable.
                            val `var` = rs.getString("var")
                            if (`var` == "<state>") {
                                QuestState(player, quest, rs.getByte("value"))

                                // Notify quest for enterworld event, if quest allows it.
                                if (quest.onEnterWorld)
                                    quest.notifyEnterWorld(player)
                            } else {
                                val qs = player.getQuestState(questName)
                                if (qs == null) {
                                    L2GameClientPacket.LOGGER.warn(
                                        "Unknown quest state {} for player {}.",
                                        questName,
                                        player.name
                                    )
                                    continue
                                }

                                qs.setInternal(`var`, rs.getString("value"))
                            }// Feed an existing quest state.
                        }
                    }
                }
            }
        } catch (e: Exception) {
            L2GameClientPacket.LOGGER.error("Couldn't load quests for player {}.", e, player.name)
        }

        player.sendPacket(QuestList(player))

        // Unread mails make a popup appears.
        if (Config.ENABLE_COMMUNITY_BOARD && MailBBSManager.checkUnreadMail(player) > 0) {
            player.sendPacket(SystemMessageId.NEW_MAIL)
            player.sendPacket(PlaySound("systemmsg_e.1233"))
            player.sendPacket(ExMailArrived.STATIC_PACKET)
        }

        // Clan notice, if active.
        if (Config.ENABLE_COMMUNITY_BOARD && clan != null && clan.isNoticeEnabled) {
            val html = NpcHtmlMessage(0)
            html.setFile("data/html/clan_notice.htm")
            html.replace("%clan_name%", clan.name ?: "")
            html.replace(
                "%notice_text%",
                clan.notice.replace("\r\n", "<br>").replace(
                    "action",
                    ""
                ).replace("bypass", "")
            )
            sendPacket(html)
        } else if (Config.SERVER_NEWS) {
            val html = NpcHtmlMessage(0)
            html.setFile("data/html/servnews.htm")
            sendPacket(html)
        }

        PetitionManager.checkPetitionMessages(player)

        player.onPlayerEnter()

        sendPacket(SkillCoolTime(player))

        // If player logs back in a stadium, port him in nearest town.
        if (Olympiad.playerInStadia(player))
            player.teleToLocation(MapRegionData.TeleportType.TOWN)

        if (DimensionalRiftManager.checkIfInRiftZone(player.x, player.y, player.z, false))
            DimensionalRiftManager.teleportToWaitingRoom(player)

        if (player.clanJoinExpiryTime > System.currentTimeMillis())
            player.sendPacket(SystemMessageId.CLAN_MEMBERSHIP_TERMINATED)

        // Attacker or spectator logging into a siege zone will be ported at town.
        if (!player.isGM && (!player.isInSiege || player.siegeState < 2) && player.isInsideZone(ZoneId.SIEGE))
            player.teleToLocation(MapRegionData.TeleportType.TOWN)

        player.sendPacket(ActionFailed.STATIC_PACKET)
    }

    override fun triggersOnActionRequest(): Boolean {
        return false
    }

    companion object {
        private const val LOAD_PLAYER_QUESTS = "SELECT name,var,value FROM character_quests WHERE charId=?"
    }
}