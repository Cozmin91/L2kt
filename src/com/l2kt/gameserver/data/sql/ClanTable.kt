package com.l2kt.gameserver.data.sql

import com.l2kt.Config
import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.commons.lang.StringUtil
import com.l2kt.commons.logging.CLogger
import com.l2kt.gameserver.data.manager.CastleManager
import com.l2kt.gameserver.idfactory.IdFactory
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.pledge.Clan
import com.l2kt.gameserver.model.pledge.ClanMember
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.PledgeShowInfoUpdate
import com.l2kt.gameserver.network.serverpackets.PledgeShowMemberListAll
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.network.serverpackets.UserInfo
import java.sql.PreparedStatement
import java.util.concurrent.ConcurrentHashMap

object ClanTable {

    private val _clans = ConcurrentHashMap<Int, Clan>()
    private val LOGGER = CLogger(ClanTable::class.java.name)

    val clans: Collection<Clan>
        get() = _clans.values

    init {
        // Load all clans.
        try {
            L2DatabaseFactory.connection.use { con ->
                val ps = con.prepareStatement("SELECT * FROM clan_data")
                val rs = ps.executeQuery()

                while (rs.next()) {
                    val clanId = rs.getInt("clan_id")

                    val clan = Clan(clanId, rs.getInt("leader_id"))
                    _clans[clanId] = clan

                    clan.name = rs.getString("clan_name")
                    clan.level = rs.getInt("clan_level")
                    clan.setCastle(rs.getInt("hasCastle"))
                    clan.allyId = rs.getInt("ally_id")
                    clan.allyName = rs.getString("ally_name")

                    // If ally expire time has been reached while server was off, keep it to 0.
                    val allyExpireTime = rs.getLong("ally_penalty_expiry_time")
                    if (allyExpireTime > System.currentTimeMillis())
                        clan.setAllyPenaltyExpiryTime(allyExpireTime, rs.getInt("ally_penalty_type"))

                    // If character expire time has been reached while server was off, keep it to 0.
                    val charExpireTime = rs.getLong("char_penalty_expiry_time")
                    if (charExpireTime + Config.ALT_CLAN_JOIN_DAYS * 86400000L > System.currentTimeMillis())
                        clan.charPenaltyExpiryTime = charExpireTime

                    clan.dissolvingExpiryTime = rs.getLong("dissolving_expiry_time")

                    clan.crestId = rs.getInt("crest_id")
                    clan.crestLargeId = rs.getInt("crest_large_id")
                    clan.allyCrestId = rs.getInt("ally_crest_id")

                    clan.addReputationScore(rs.getInt("reputation_score"))
                    clan.auctionBiddedAt = rs.getInt("auction_bid_at")
                    clan.setNewLeaderId(rs.getInt("new_leader_id"), false)

                    if (clan.dissolvingExpiryTime != 0L)
                        scheduleRemoveClan(clan)

                    clan.isNoticeEnabled = rs.getBoolean("enabled")
                    clan.notice = rs.getString("notice")

                    clan.setIntroduction(rs.getString("introduction"), false)
                }
                rs.close()
                ps.close()
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't restore clans.", e)
        }

        LOGGER.info("Loaded {} clans.", _clans.size)

        // Check for non-existing alliances.
        allianceCheck()

        // Restore clan wars.
        restoreWars()

        // Refresh clans ladder.
        refreshClansLadder(false)
    }

    /**
     * @param clanId : The id of the clan to retrieve.
     * @return the clan object based on id.
     */
    fun getClan(clanId: Int): Clan? {
        return _clans[clanId]
    }

    fun getClanByName(clanName: String): Clan? {
        for (clan in _clans.values) {
            if (clan.name.equals(clanName, ignoreCase = true))
                return clan
        }
        return null
    }

    /**
     * Creates a new clan and store clan info to database
     * @param player The player who requested the clan creation.
     * @param clanName The name of the clan player wants.
     * @return null if checks fail, or L2Clan
     */
    fun createClan(player: Player?, clanName: String): Clan? {
        if (player == null)
            return null

        if (player.level < 10) {
            player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_CRITERIA_IN_ORDER_TO_CREATE_A_CLAN)
            return null
        }

        if (player.clanId != 0) {
            player.sendPacket(SystemMessageId.FAILED_TO_CREATE_CLAN)
            return null
        }

        if (System.currentTimeMillis() < player.clanCreateExpiryTime) {
            player.sendPacket(SystemMessageId.YOU_MUST_WAIT_XX_DAYS_BEFORE_CREATING_A_NEW_CLAN)
            return null
        }

        if (!StringUtil.isAlphaNumeric(clanName)) {
            player.sendPacket(SystemMessageId.CLAN_NAME_INVALID)
            return null
        }

        if (clanName.length < 2 || clanName.length > 16) {
            player.sendPacket(SystemMessageId.CLAN_NAME_LENGTH_INCORRECT)
            return null
        }

        if (getClanByName(clanName) != null) {
            // clan name is already taken
            player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_ALREADY_EXISTS).addString(clanName))
            return null
        }

        val clan = Clan(IdFactory.getInstance().nextId, clanName)
        val leader = ClanMember(clan, player)
        clan.leader = leader
        leader.playerInstance = player
        clan.store()
        player.clan = clan
        player.pledgeClass = ClanMember.calculatePledgeClass(player)
        player.clanPrivileges = Clan.CP_ALL

        _clans[clan.clanId] = clan

        player.sendPacket(PledgeShowMemberListAll(clan, 0))
        player.sendPacket(UserInfo(player))
        player.sendPacket(SystemMessageId.CLAN_CREATED)
        return clan
    }

    /**
     * Instantly delete related clan. Run different clanId related queries, remove clan from _clans, inform clan members, delete clans from pending sieges.
     * @param clan : The clan to delete.
     */
    fun destroyClan(clan: Clan) {
        if (!_clans.containsKey(clan.clanId))
            return

        clan.broadcastToOnlineMembers(SystemMessage.getSystemMessage(SystemMessageId.CLAN_HAS_DISPERSED))

        // Drop the clan from all sieges. The related mySQL query is handled below.
        for (castle in CastleManager.castles)
            castle.siege.registeredClans.keys.removeIf { c -> c.clanId == clan.clanId }

        // Delete all clan wars.
        for (clanId in clan.attackerList) {
            val attackerClan = _clans[clanId]

            attackerClan?.deleteAttackerClan(clan.clanId)
            attackerClan?.deleteEnemyClan(clan.clanId)
        }

        // Drop all items from clan warehouse.
        clan.warehouse.destroyAllItems(
            "ClanRemove",
            if (clan.leader == null) null else clan.leader?.playerInstance,
            null
        )

        for (member in clan.members)
            clan.removeClanMember(member.objectId, 0)

        // Numerous mySQL queries.
        try {
            L2DatabaseFactory.connection.use { con ->
                var ps = con.prepareStatement("DELETE FROM clan_data WHERE clan_id=?")
                ps.setInt(1, clan.clanId)
                ps.execute()
                ps.close()

                ps = con.prepareStatement("DELETE FROM clan_privs WHERE clan_id=?")
                ps.setInt(1, clan.clanId)
                ps.execute()
                ps.close()

                ps = con.prepareStatement("DELETE FROM clan_skills WHERE clan_id=?")
                ps.setInt(1, clan.clanId)
                ps.execute()
                ps.close()

                ps = con.prepareStatement("DELETE FROM clan_subpledges WHERE clan_id=?")
                ps.setInt(1, clan.clanId)
                ps.execute()
                ps.close()

                ps = con.prepareStatement("DELETE FROM clan_wars WHERE clan1=? OR clan2=?")
                ps.setInt(1, clan.clanId)
                ps.setInt(2, clan.clanId)
                ps.execute()
                ps.close()

                ps = con.prepareStatement("DELETE FROM siege_clans WHERE clan_id=?")
                ps.setInt(1, clan.clanId)
                ps.execute()
                ps.close()

                if (clan.castleId != 0) {
                    ps = con.prepareStatement("UPDATE castle SET taxPercent = 0 WHERE id = ?")
                    ps.setInt(1, clan.castleId)
                    ps.execute()
                    ps.close()
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't delete clan.", e)
        }

        // Release clan id.
        IdFactory.getInstance().releaseId(clan.clanId)

        // Remove the clan from the map.
        _clans.remove(clan.clanId)
    }

    fun scheduleRemoveClan(clan: Clan?) {
        if (clan == null)
            return

        ThreadPool.schedule(Runnable{
            if (clan.dissolvingExpiryTime != 0L)
                destroyClan(clan)
        }, Math.max(clan.dissolvingExpiryTime - System.currentTimeMillis(), 60000))
    }

    fun isAllyExists(allyName: String): Boolean {
        for (clan in _clans.values) {
            if (clan.allyName != null && clan.allyName.equals(allyName, ignoreCase = true))
                return true
        }
        return false
    }

    fun storeClansWars(clanId1: Int, clanId2: Int) {
        val clan1 = _clans[clanId1]!!
        val clan2 = _clans[clanId2]!!

        clan1.setEnemyClan(clanId2)
        clan1.broadcastToOnlineMembers(
            PledgeShowInfoUpdate(clan1),
            SystemMessage.getSystemMessage(SystemMessageId.CLAN_WAR_DECLARED_AGAINST_S1_IF_KILLED_LOSE_LOW_EXP).addString(
                clan2.name
            )
        )

        clan2.setAttackerClan(clanId1)
        clan2.broadcastToOnlineMembers(
            PledgeShowInfoUpdate(clan2),
            SystemMessage.getSystemMessage(SystemMessageId.CLAN_S1_DECLARED_WAR).addString(clan1.name)
        )

        try {
            L2DatabaseFactory.connection.use { con ->
                val ps = con.prepareStatement("REPLACE INTO clan_wars (clan1, clan2) VALUES(?,?)")
                ps.setInt(1, clanId1)
                ps.setInt(2, clanId2)
                ps.execute()
                ps.close()
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't store clans wars.", e)
        }

    }

    fun deleteClansWars(clanId1: Int, clanId2: Int) {
        val clan1 = _clans[clanId1]!!
        val clan2 = _clans[clanId2]!!

        clan1.deleteEnemyClan(clanId2)
        clan1.broadcastToOnlineMembers(
            PledgeShowInfoUpdate(clan1),
            SystemMessage.getSystemMessage(SystemMessageId.WAR_AGAINST_S1_HAS_STOPPED).addString(clan2.name!!)
        )

        clan2.deleteAttackerClan(clanId1)
        clan2.broadcastToOnlineMembers(
            PledgeShowInfoUpdate(clan2),
            SystemMessage.getSystemMessage(SystemMessageId.CLAN_S1_HAS_DECIDED_TO_STOP).addString(clan1.name!!)
        )

        try {
            L2DatabaseFactory.connection.use { con ->
                val ps: PreparedStatement

                if (Config.ALT_CLAN_WAR_PENALTY_WHEN_ENDED > 0) {
                    val penaltyExpiryTime =
                        System.currentTimeMillis() + Config.ALT_CLAN_WAR_PENALTY_WHEN_ENDED * 86400000L

                    clan1.addWarPenaltyTime(clanId2, penaltyExpiryTime)

                    ps = con.prepareStatement("UPDATE clan_wars SET expiry_time=? WHERE clan1=? AND clan2=?")
                    ps.setLong(1, penaltyExpiryTime)
                    ps.setInt(2, clanId1)
                    ps.setInt(3, clanId2)
                } else {
                    ps = con.prepareStatement("DELETE FROM clan_wars WHERE clan1=? AND clan2=?")
                    ps.setInt(1, clanId1)
                    ps.setInt(2, clanId2)
                }
                ps.execute()
                ps.close()
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't delete clans wars.", e)
        }

    }

    fun checkSurrender(clan1: Clan, clan2: Clan) {
        var count = 0
        for (player in clan1.members) {
            if (player != null && player.playerInstance!!.wantsPeace())
                count++
        }

        if (count == clan1.membersCount - 1) {
            clan1.deleteEnemyClan(clan2.clanId)
            clan2.deleteEnemyClan(clan1.clanId)
            deleteClansWars(clan1.clanId, clan2.clanId)
        }
    }

    /**
     * Restore wars, checking penalties.
     */
    private fun restoreWars() {
        try {
            L2DatabaseFactory.connection.use { con ->
                // Delete deprecated wars (server was offline).
                var ps = con.prepareStatement("DELETE FROM clan_wars WHERE expiry_time > 0 AND expiry_time <= ?")
                ps.setLong(1, System.currentTimeMillis())
                ps.execute()
                ps.close()

                // Load all wars.
                ps = con.prepareStatement("SELECT * FROM clan_wars")
                val rs = ps.executeQuery()
                while (rs.next()) {
                    val clan1 = rs.getInt("clan1")
                    val clan2 = rs.getInt("clan2")
                    val expiryTime = rs.getLong("expiry_time")

                    // Expiry timer is found, add a penalty. Otherwise, add the regular war.
                    if (expiryTime > 0)
                        _clans[clan1]?.addWarPenaltyTime(clan2, expiryTime)
                    else {
                        _clans[clan1]?.setEnemyClan(clan2)
                        _clans[clan2]?.setAttackerClan(clan1)
                    }
                }
                rs.close()
                ps.close()
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't restore clans wars.", e)
        }

    }

    /**
     * Check for nonexistent alliances
     */
    private fun allianceCheck() {
        for (clan in _clans.values) {
            val allyId = clan.allyId
            if (allyId != 0 && clan.clanId != allyId) {
                if (!_clans.containsKey(allyId)) {
                    clan.allyId = 0
                    clan.allyName = null
                    clan.changeAllyCrest(0, true)
                    clan.updateClanInDB()
                }
            }
        }
    }

    fun getClanAllies(allianceId: Int): List<Clan> {
        return if (allianceId == 0) emptyList() else _clans.values.filter { c -> c.allyId == allianceId }

    }

    /**
     * Refresh clans ladder, picking up the 99 first best clans, and allocating their ranks accordingly.
     * @param cleanupRank if true, cleanup ranks. Used for the task, useless for startup.
     */
    fun refreshClansLadder(cleanupRank: Boolean) {
        // Cleanup ranks. Needed, as one clan can go off the list.
        if (cleanupRank) {
            for (clan in _clans.values)
                if (clan.rank != 0)
                    clan.rank = 0
        }

        // Retrieve the 99 best clans, allocate their ranks.
        try {
            L2DatabaseFactory.connection.use { con ->
                val ps = con.prepareStatement("SELECT clan_id FROM clan_data ORDER BY reputation_score DESC LIMIT 99")
                val rs = ps.executeQuery()

                var rank = 1

                while (rs.next()) {
                    val clan = _clans[rs.getInt("clan_id")]
                    if (clan != null && clan.reputationScore > 0)
                        clan.rank = rank++
                }
                rs.close()
                ps.close()
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't refresh clans ladder.", e)
        }

    }
}