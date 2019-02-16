package com.l2kt.gameserver.model.pledge

import com.l2kt.Config
import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.lang.StringUtil
import com.l2kt.commons.logging.CLogger
import com.l2kt.commons.math.MathUtil
import com.l2kt.gameserver.communitybbs.BB.Forum
import com.l2kt.gameserver.communitybbs.Manager.ForumsBBSManager
import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.data.cache.CrestCache
import com.l2kt.gameserver.data.manager.CastleManager
import com.l2kt.gameserver.data.sql.ClanTable
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.entity.Siege
import com.l2kt.gameserver.model.itemcontainer.ClanWarehouse
import com.l2kt.gameserver.model.itemcontainer.ItemContainer
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Clan system is one of the major features of the game. Clans unite players and let them influence the world of Lineage 2.<br></br>
 * <br></br>
 * A clan is made up of a clan leader (commonly known as a lord) and a number of [ClanMember]s.
 */
class Clan {

    private val _members = ConcurrentHashMap<Int, ClanMember>()
    private val _warPenaltyExpiryTime = ConcurrentHashMap<Int, Long>()
    private val _skills = ConcurrentHashMap<Int, L2Skill>()
    private val _priviledges = ConcurrentHashMap<Int, Int>()
    private val _subPledges = ConcurrentHashMap<Int, SubPledge>()

    private val _atWarWith = ConcurrentHashMap.newKeySet<Int>()
    private val _atWarAttackers = ConcurrentHashMap.newKeySet<Int>()

    val warehouse: ItemContainer = ClanWarehouse(this)

    var name: String = ""
    var clanId: Int = 0
    var leader: ClanMember? = null
        set(leader) {
            field = leader ?: return
            _members[leader.objectId] = leader
        }
    var newLeaderId: Int = 0
        private set
    var allyName: String? = null
    var allyId: Int = 0
    /**
     * Sets the [Clan] level and updates the clan forum if it's needed.
     * @param level : The clan level to set.
     */
    var level: Int = 0
        set(level) {
            field = level

            if (Config.ENABLE_COMMUNITY_BOARD && this.level >= 2 && _forum == null) {
                val forum = ForumsBBSManager.getForumByName("ClanRoot")
                if (forum != null) {
                    _forum = forum.getChildByName(name!!)
                    if (_forum == null)
                        _forum =
                                ForumsBBSManager.createNewForum(name!!, forum, Forum.CLAN, Forum.CLANMEMBERONLY, clanId)
                }
            }
        }
    var castleId: Int = 0
        private set
    var hideoutId: Int = 0
        private set
    var crestId: Int = 0
    var crestLargeId: Int = 0
    var allyCrestId: Int = 0
    var auctionBiddedAt: Int = 0
        set(id) {
            field = id

            try {
                L2DatabaseFactory.connection.use { con ->
                    con.prepareStatement(UPDATE_AUCTION).use { ps ->
                        ps.setInt(1, id)
                        ps.setInt(2, clanId)
                        ps.executeUpdate()
                    }
                }
            } catch (e: Exception) {
                LOGGER.error("Error while updating clan auction.", e)
            }

        }
    var allyPenaltyExpiryTime: Long = 0
        private set
    var allyPenaltyType: Int = 0
        private set
    var charPenaltyExpiryTime: Long = 0
    var dissolvingExpiryTime: Long = 0

    private var _forum: Forum? = null

    /**
     * Launch behaviors following how big or low is the actual reputation.<br></br>
     * **This method DOESN'T update the database.**
     * @param value : The total amount to set to _reputationScore.
     */
    // Don't add any CRPs to clans with low level.
    // That check is used to see if it needs a refresh.
    // Store the online members (used in 2 positions, can't merge)
    // Refresh clan windows of all clan members, and reward/remove skills.
    // Points reputation update for all.
    // Save the amount on the database.
    var reputationScore: Int = 0
        private set(value) {
            if (level < 5)
                return
            val needRefresh = reputationScore > 0 && value <= 0 || value > 0 && reputationScore <= 0
            val members = onlineMembers

            field = MathUtil.limit(value, -100000000, 100000000)
            if (needRefresh) {
                val skills = clanSkills.values

                if (reputationScore <= 0) {
                    for (member in members) {
                        member.sendPacket(SystemMessageId.REPUTATION_POINTS_0_OR_LOWER_CLAN_SKILLS_DEACTIVATED)

                        for (sk in skills)
                            member.removeSkill(sk.id, false)

                        member.sendSkillList()
                    }
                } else {
                    for (member in members) {
                        member.sendPacket(SystemMessageId.CLAN_SKILLS_WILL_BE_ACTIVATED_SINCE_REPUTATION_IS_0_OR_HIGHER)

                        for (sk in skills) {
                            if (sk.minPledgeClass <= member.pledgeClass)
                                member.addSkill(sk, false)
                        }

                        member.sendSkillList()
                    }
                }
            }
            val infoRefresh = PledgeShowInfoUpdate(this)
            for (member in members)
                member.sendPacket(infoRefresh)
            try {
                L2DatabaseFactory.connection.use { con ->
                    con.prepareStatement(UPDATE_CRP).use { ps ->
                        ps.setInt(1, reputationScore)
                        ps.setInt(2, clanId)
                        ps.executeUpdate()
                    }
                }
            } catch (e: Exception) {
                LOGGER.error("Error while updating clan reputation points.", e)
            }

        }
    var rank: Int = 0

    private var _notice: String? = null
    var isNoticeEnabled: Boolean = false

    private var _introduction: String? = null

    var siegeKills: Int = 0
    var siegeDeaths: Int = 0

    // If we set flag as null and clan flag is currently existing, delete it.
    var flag: Npc? = null
        set(flag) {
            if (flag == null && this.flag != null)
                this.flag!!.deleteMe()

            field = flag
        }

    val leaderId: Int
        get() = leader!!.objectId

    val leaderName: String
        get() = if (leader == null) "" else leader!!.name

    val members: Collection<ClanMember>
        get() = _members.values

    val membersCount: Int
        get() = _members.size

    val onlineMembers: Array<Player>
        get() {
            val list = ArrayList<Player>()
            for (temp in _members.values) {
                if (temp.isOnline)
                    temp.playerInstance?.let { list.add(it) }
            }
            return list.toTypedArray()
        }

    /**
     * @return the online clan member count.
     */
    val onlineMembersCount: Int
        get() = _members.values.stream().filter { m -> m.isOnline }.count().toInt()

    var notice: String
        get() = _notice ?: ""
        set(notice) {
            _notice = notice
        }

    val introduction: String
        get() = _introduction ?: ""

    /**
     * @return the [Map] with all known [Clan] [L2Skill]s.
     */
    val clanSkills: Map<Int, L2Skill>
        get() = _skills

    val warPenalty: Map<Int, Long>
        get() = _warPenaltyExpiryTime

    val isAtWar: Boolean
        get() = !_atWarWith.isEmpty()

    val warList: Set<Int>
        get() = _atWarWith

    val attackerList: Set<Int>
        get() = _atWarAttackers

    /**
     * Retrieve all subPledges.
     * @return an array containing all subpledge objects.
     */
    val allSubPledges: Array<SubPledge>
        get() = _subPledges.values.toTypedArray()

    val priviledges: Map<Int, Int>
        get() = _priviledges

    /**
     * Verify if the clan is registered to any siege.
     * @return true if the clan is registered or owner of a castle
     */
    val isRegisteredOnSiege: Boolean
        get() {
            for (castle in CastleManager.castles)
                if (castle.siege.checkSides(this))
                    return true

            return false
        }

    /**
     * Constructor used to restore an existing [Clan]. Infos are fetched using database.
     * @param clanId : The clanId informations to restore.
     * @param leaderId : The clan leader objectId.
     */
    constructor(clanId: Int, leaderId: Int) {
        this.clanId = clanId

        for (i in 1..9)
            _priviledges[i] = CP_NOTHING

        try {
            L2DatabaseFactory.connection.use { con ->
                // Load members.
                var ps = con.prepareStatement(LOAD_MEMBERS)
                ps.setInt(1, this.clanId)

                var rs = ps.executeQuery()
                while (rs.next()) {
                    val member = ClanMember(this, rs)
                    if (member.objectId == leaderId)
                        leader = member
                    else
                        _members[member.objectId] = member

                    member.setApprenticeAndSponsor(rs.getInt("apprentice"), rs.getInt("sponsor"))
                }

                rs.close()
                ps.close()

                // Load subpledges.
                ps = con.prepareStatement(LOAD_SUBPLEDGES)
                ps.setInt(1, this.clanId)

                rs = ps.executeQuery()
                while (rs.next()) {
                    val id = rs.getInt("sub_pledge_id")
                    _subPledges[id] = SubPledge(id, rs.getString("name"), rs.getInt("leader_id"))
                }

                rs.close()
                ps.close()

                // Load priviledges.
                ps = con.prepareStatement(LOAD_PRIVILEDGES)
                ps.setInt(1, this.clanId)

                rs = ps.executeQuery()
                while (rs.next())
                    _priviledges[rs.getInt("rank")] = rs.getInt("privs")

                rs.close()
                ps.close()

                // Load clan skills.
                ps = con.prepareStatement(LOAD_SKILLS)
                ps.setInt(1, this.clanId)

                rs = ps.executeQuery()
                while (rs.next()) {
                    val id = rs.getInt("skill_id")
                    val level = rs.getInt("skill_level")

                    val skill = SkillTable.getInfo(id, level) ?: continue

                    _skills[id] = skill
                }

                rs.close()
                ps.close()
            }
        } catch (e: Exception) {
            LOGGER.error("Error while restoring clan.", e)
        }

        if (crestId != 0 && CrestCache.getCrest(CrestCache.CrestType.PLEDGE, crestId) == null) {
            LOGGER.warn("Removing non-existent crest for clan {}, crestId: {}.", toString(), crestId)
            changeClanCrest(0)
        }

        if (crestLargeId != 0 && CrestCache.getCrest(CrestCache.CrestType.PLEDGE_LARGE, crestLargeId) == null) {
            LOGGER.warn("Removing non-existent large crest for clan {}, crestLargeId: {}.", toString(), crestLargeId)
            changeLargeCrest(0)
        }

        if (allyCrestId != 0 && CrestCache.getCrest(CrestCache.CrestType.ALLY, allyCrestId) == null) {
            LOGGER.warn("Removing non-existent ally crest for clan {}, allyCrestId: {}.", toString(), allyCrestId)
            changeAllyCrest(0, true)
        }

        warehouse.restore()
    }

    /**
     * A constructor called to create a new [Clan]. It feeds all priviledges ranks with CP_NOTHING.
     * @param clanId : A valid id to use.
     * @param clanName : A valid clan name.
     */
    constructor(clanId: Int, clanName: String) {
        this.clanId = clanId
        name = clanName

        for (i in 1..9)
            _priviledges[i] = CP_NOTHING
    }

    fun setNewLeader(member: ClanMember) {
        val newLeader = member.playerInstance
        val exMember = leader
        val exLeader = exMember!!.playerInstance

        if (exLeader != null) {
            if (exLeader.isFlying)
                exLeader.dismount()

            if (level >= Config.MINIMUM_CLAN_LEVEL)
                exLeader.removeSiegeSkills()

            exLeader.clanPrivileges = CP_NOTHING
            exLeader.broadcastUserInfo()

        } else {
            try {
                L2DatabaseFactory.connection.use { con ->
                    con.prepareStatement(RESET_MEMBER_PRIVS).use { ps ->
                        ps.setInt(1, leaderId)
                        ps.executeUpdate()
                    }
                }
            } catch (e: Exception) {
                LOGGER.error("Couldn't update clan privs for old clan leader.", e)
            }

        }

        leader = member
        if (newLeaderId != 0)
            setNewLeaderId(0, true)

        updateClanInDB()

        if (exLeader != null) {
            exLeader.pledgeClass = ClanMember.calculatePledgeClass(exLeader)
            exLeader.broadcastUserInfo()
            exLeader.checkItemRestriction()
        }

        if (newLeader != null) {
            newLeader.pledgeClass = ClanMember.calculatePledgeClass(newLeader)
            newLeader.clanPrivileges = CP_ALL

            if (level >= Config.MINIMUM_CLAN_LEVEL)
                newLeader.addSiegeSkills()

            newLeader.broadcastUserInfo()
        } else {
            try {
                L2DatabaseFactory.connection.use { con ->
                    con.prepareStatement(RESET_MEMBER_PRIVS).use { ps ->
                        ps.setInt(1, leaderId)
                        ps.executeUpdate()
                    }
                }
            } catch (e: Exception) {
                LOGGER.error("Couldn't update clan privs for new clan leader.", e)
            }

        }

        broadcastClanStatus()
        broadcastToOnlineMembers(
            SystemMessage.getSystemMessage(SystemMessageId.CLAN_LEADER_PRIVILEGES_HAVE_BEEN_TRANSFERRED_TO_S1).addString(
                member.name
            )
        )
    }

    fun setNewLeaderId(objectId: Int, storeInDb: Boolean) {
        newLeaderId = objectId

        if (storeInDb)
            updateClanInDB()
    }

    fun hasCastle(): Boolean {
        return castleId > 0
    }

    fun setCastle(castle: Int) {
        castleId = castle
    }

    fun hasHideout(): Boolean {
        return hideoutId > 0
    }

    fun setHideout(hideout: Int) {
        hideoutId = hideout
    }

    fun setAllyPenaltyExpiryTime(expiryTime: Long, penaltyType: Int) {
        allyPenaltyExpiryTime = expiryTime
        allyPenaltyType = penaltyType
    }

    /**
     * @param objectId : The required clan member objectId.
     * @return the [ClanMember] for a given objectId.
     */
    fun getClanMember(objectId: Int): ClanMember? {
        return _members[objectId]
    }

    /**
     * @param name : The required clan member name.
     * @return the [ClanMember] for a given name.
     */
    fun getClanMember(name: String): ClanMember? {
        return _members.values.stream().filter { m -> m.name == name }.findFirst().orElse(null)
    }

    /**
     * @param objectId : The member objectId.
     * @return true if the [ClanMember] objectId given as parameter is in the _members list.
     */
    fun isMember(objectId: Int): Boolean {
        return _members.containsKey(objectId)
    }

    /**
     * Add a [ClanMember] to this [Clan], based on a [Player].
     *
     *  * Generate the ClanMember object, link it to the Player object.
     *  * Calculate and set the player pledge class.
     *  * Update the siege flag.
     *  * Send packets
     *
     * @param player : The player to add on clan.
     */
    fun addClanMember(player: Player) {
        val member = ClanMember(this, player)
        _members[member.objectId] = member
        member.playerInstance = player
        player.clan = this
        player.pledgeClass = ClanMember.calculatePledgeClass(player)

        // Update siege flag, if needed.
        for (castle in CastleManager.castles) {
            val siege = castle.siege
            if (!siege.isInProgress)
                continue

            if (siege.checkSide(this, Siege.SiegeSide.ATTACKER))
                player.siegeState = 1.toByte()
            else if (siege.checkSides(this, Siege.SiegeSide.DEFENDER, Siege.SiegeSide.OWNER))
                player.siegeState = 2.toByte()
        }

        player.sendPacket(PledgeShowMemberListUpdate(player))
        player.sendPacket(UserInfo(player))
    }

    /**
     * Remove a [ClanMember] from this [Clan].
     * @param objectId : The objectId of the member that will be removed.
     * @param clanJoinExpiryTime : The time penalty to join a clan.
     */
    fun removeClanMember(objectId: Int, clanJoinExpiryTime: Long) {
        val exMember = _members.remove(objectId) ?: return

        // Sub-unit leader withdraws, position becomes vacant and leader should appoint new via NPC.
        val subPledgeId = getLeaderSubPledge(objectId)
        if (subPledgeId != 0) {
            val pledge = getSubPledge(subPledgeId)
            if (pledge != null) {
                pledge.leaderId = 0

                updateSubPledgeInDB(pledge)
            }
        }

        // Remove apprentice, if any.
        if (exMember.apprentice != 0) {
            val apprentice = getClanMember(exMember.apprentice)
            if (apprentice != null) {
                if (apprentice.playerInstance != null)
                    apprentice.playerInstance!!.sponsor = 0
                else
                    apprentice.setApprenticeAndSponsor(0, 0)

                apprentice.saveApprenticeAndSponsor(0, 0)
            }
        }

        // Remove sponsor, if any.
        if (exMember.sponsor != 0) {
            val sponsor = getClanMember(exMember.sponsor)
            if (sponsor != null) {
                if (sponsor.playerInstance != null)
                    sponsor.playerInstance!!.apprentice = 0
                else
                    sponsor.setApprenticeAndSponsor(0, 0)

                sponsor.saveApprenticeAndSponsor(0, 0)
            }
        }
        exMember.saveApprenticeAndSponsor(0, 0)

        // Unequip castle related items.
        if (hasCastle())
            CastleManager.getCastleById(castleId)!!.checkItemsForMember(exMember)

        if (exMember.isOnline) {
            val player = exMember.playerInstance

            // Clean title only for non nobles.
            if (!player!!.isNoble)
                player.title = ""

            // Setup active warehouse to null.
            if (player.activeWarehouse != null)
                player.activeWarehouse = null

            player.apprentice = 0
            player.sponsor = 0
            player.siegeState = 0.toByte()

            if (player.isClanLeader) {
                player.removeSiegeSkills()
                player.clanCreateExpiryTime = System.currentTimeMillis() + Config.ALT_CLAN_CREATE_DAYS * 86400000L
            }

            // Remove clan skills.
            for (skill in _skills.values)
                player.removeSkill(skill.id, false)

            player.sendSkillList()
            player.clan = null

            // Players leaving from clan academy have no penalty.
            if (exMember.pledgeType != SUBUNIT_ACADEMY)
                player.clanJoinExpiryTime = clanJoinExpiryTime

            player.pledgeClass = ClanMember.calculatePledgeClass(player)
            player.broadcastUserInfo()

            // Disable clan tab.
            player.sendPacket(PledgeShowMemberListDeleteAll.STATIC_PACKET)
        } else {
            try {
                L2DatabaseFactory.connection.use { con ->
                    var ps = con.prepareStatement(REMOVE_MEMBER)
                    ps.setString(1, "")
                    ps.setLong(2, clanJoinExpiryTime)
                    ps.setLong(
                        3,
                        if (leader!!.objectId == objectId) System.currentTimeMillis() + Config.ALT_CLAN_CREATE_DAYS * 86400000L else 0
                    )
                    ps.setInt(4, exMember.objectId)
                    ps.executeUpdate()
                    ps.close()

                    ps = con.prepareStatement(REMOVE_APPRENTICE)
                    ps.setInt(1, exMember.objectId)
                    ps.executeUpdate()
                    ps.close()

                    ps = con.prepareStatement(REMOVE_SPONSOR)
                    ps.setInt(1, exMember.objectId)
                    ps.executeUpdate()
                    ps.close()
                }
            } catch (e: Exception) {
                LOGGER.error("Error while removing clan member.", e)
            }

        }
    }

    fun getSubPledgeMembersCount(pledgeType: Int): Int {
        return _members.values.stream().filter { m -> m.pledgeType == pledgeType }.count().toInt()
    }

    fun getSubPledgeLeaderName(pledgeType: Int): String {
        if (pledgeType == 0)
            return leader!!.name

        val subPledge = _subPledges[pledgeType]
        val leaderId = subPledge?.leaderId

        if (subPledge?.id == SUBUNIT_ACADEMY || leaderId == 0)
            return ""

        if (!_members.containsKey(leaderId)) {
            LOGGER.warn("SubPledge leader {} is missing from clan: {}.", leaderId!!, toString())
            return ""
        }

        return _members[leaderId]!!.name
    }

    /**
     * @param pledgeType : The id of the pledge type.
     * @return the maximum number of members allowed for a given pledgeType.
     */
    fun getMaxNrOfMembers(pledgeType: Int): Int {
        when (pledgeType) {
            0 -> when (level) {
                0 -> return 10

                1 -> return 15

                2 -> return 20

                3 -> return 30

                else -> return 40
            }

            SUBUNIT_ACADEMY, SUBUNIT_ROYAL1, SUBUNIT_ROYAL2 -> return 20

            SUBUNIT_KNIGHT1, SUBUNIT_KNIGHT2, SUBUNIT_KNIGHT3, SUBUNIT_KNIGHT4 -> return 10
        }
        return 0
    }

    /**
     * Update all [Clan] informations.
     */
    fun updateClanInDB() {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(UPDATE_CLAN).use { ps ->
                    ps.setInt(1, leader!!.objectId)
                    ps.setInt(2, newLeaderId)
                    ps.setInt(3, allyId)
                    ps.setString(4, allyName)
                    ps.setInt(5, reputationScore)
                    ps.setLong(6, allyPenaltyExpiryTime)
                    ps.setInt(7, allyPenaltyType)
                    ps.setLong(8, charPenaltyExpiryTime)
                    ps.setLong(9, dissolvingExpiryTime)
                    ps.setInt(10, clanId)
                    ps.executeUpdate()
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Error while updating clan.", e)
        }

    }

    /**
     * Store informations of a newly created [Clan].
     */
    fun store() {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(STORE_CLAN).use { ps ->
                    ps.setInt(1, clanId)
                    ps.setString(2, name)
                    ps.setInt(3, level)
                    ps.setInt(4, castleId)
                    ps.setInt(5, allyId)
                    ps.setString(6, allyName)
                    ps.setInt(7, leader!!.objectId)
                    ps.setInt(8, newLeaderId)
                    ps.setInt(9, crestId)
                    ps.setInt(10, crestLargeId)
                    ps.setInt(11, allyCrestId)
                    ps.executeUpdate()
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Error while storing clan.", e)
        }

    }

    private fun storeNotice(notice: String?, enabled: Boolean) {
        var notice = notice
        if (notice == null)
            notice = ""

        if (notice.length > MAX_NOTICE_LENGTH)
            notice = notice.substring(0, MAX_NOTICE_LENGTH - 1)

        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(UPDATE_NOTICE).use { ps ->
                    ps.setBoolean(1, enabled)
                    ps.setString(2, notice)
                    ps.setInt(3, clanId)
                    ps.executeUpdate()
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Error while storing notice.", e)
        }

        _notice = notice
        isNoticeEnabled = enabled
    }

    fun setNoticeEnabledAndStore(enabled: Boolean) {
        storeNotice(_notice, enabled)
    }

    fun setNoticeAndStore(notice: String) {
        storeNotice(notice, isNoticeEnabled)
    }

    fun setIntroduction(intro: String?, saveOnDb: Boolean) {
        var intro = intro
        if (saveOnDb) {
            if (intro == null)
                intro = ""

            if (intro.length > MAX_INTRODUCTION_LENGTH)
                intro = intro.substring(0, MAX_INTRODUCTION_LENGTH - 1)

            try {
                L2DatabaseFactory.connection.use { con ->
                    con.prepareStatement(UPDATE_INTRODUCTION).use { ps ->
                        ps.setString(1, intro)
                        ps.setInt(2, clanId)
                        ps.executeUpdate()
                    }
                }
            } catch (e: Exception) {
                LOGGER.error("Error while storing introduction.", e)
            }

        }

        _introduction = intro
    }

    /**
     * Add a new [L2Skill] to the [Clan].
     * @param newSkill : The skill to add.
     */
    fun addNewSkill(newSkill: L2Skill?) {
        if (newSkill == null)
            return

        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(ADD_OR_UPDATE_SKILL).use { ps ->
                    ps.setInt(1, clanId)
                    ps.setInt(2, newSkill.id)
                    ps.setInt(3, newSkill.level)
                    ps.executeUpdate()
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Error while storing a clan skill.", e)
            return
        }

        // Replace or add the skill.
        _skills[newSkill.id] = newSkill

        val pledgeListAdd = PledgeSkillListAdd(newSkill.id, newSkill.level)
        val pledgeList = PledgeSkillList(this)
        val sm = SystemMessage.getSystemMessage(SystemMessageId.CLAN_SKILL_S1_ADDED).addSkillName(newSkill.id)

        for (temp in onlineMembers) {
            if (newSkill.minPledgeClass <= temp.pledgeClass) {
                temp.addSkill(newSkill, false)
                temp.sendPacket(pledgeListAdd)
                temp.sendSkillList()
            }
            temp.sendPacket(pledgeList)
            temp.sendPacket(sm)
        }
    }

    fun addSkillEffects(player: Player?) {
        if (player == null || reputationScore <= 0 || player.isInOlympiadMode)
            return

        for (skill in _skills.values) {
            if (skill.minPledgeClass <= player.pledgeClass)
                player.addSkill(skill, false)
        }
    }

    fun broadcastToOnlineAllyMembers(packet: L2GameServerPacket) {
        for (clan in ClanTable.getClanAllies(allyId))
            clan.broadcastToOnlineMembers(packet)
    }

    fun broadcastToOnlineMembers(vararg packets: L2GameServerPacket) {
        for (member in _members.values) {
            if (member != null && member.isOnline) {
                for (packet in packets)
                    member.playerInstance!!.sendPacket(packet)
            }
        }
    }

    fun broadcastToOtherOnlineMembers(packet: L2GameServerPacket, player: Player) {
        for (member in _members.values) {
            if (member != null && member.isOnline && member.playerInstance != player)
                member.playerInstance!!.sendPacket(packet)
        }
    }

    override fun toString(): String {
        return "$name[$clanId]"
    }

    fun isAtWarWith(id: Int): Boolean {
        return _atWarWith.contains(id)
    }

    fun isAtWarAttacker(id: Int): Boolean {
        return _atWarAttackers.contains(id)
    }

    fun setEnemyClan(clanId: Int) {
        _atWarWith.add(clanId)
    }

    fun setAttackerClan(clanId: Int) {
        _atWarAttackers.add(clanId)
    }

    fun deleteEnemyClan(clanId: Int) {
        _atWarWith.remove(Integer.valueOf(clanId))
    }

    fun deleteAttackerClan(clanId: Int) {
        _atWarAttackers.remove(Integer.valueOf(clanId))
    }

    fun addWarPenaltyTime(clanId: Int, expiryTime: Long) {
        _warPenaltyExpiryTime[clanId] = expiryTime
    }

    fun hasWarPenaltyWith(clanId: Int): Boolean {
        return if (!_warPenaltyExpiryTime.containsKey(clanId)) false else _warPenaltyExpiryTime[clanId]!! > System.currentTimeMillis()

    }

    fun broadcastClanStatus() {
        for (member in onlineMembers) {
            member.sendPacket(PledgeShowMemberListDeleteAll.STATIC_PACKET)
            member.sendPacket(PledgeShowMemberListAll(this, 0))

            for ((id) in allSubPledges)
                member.sendPacket(PledgeShowMemberListAll(this, id))

            member.sendPacket(UserInfo(member))
        }
    }

    fun isSubPledgeLeader(objectId: Int): Boolean {
        for ((_, _, leaderId1) in allSubPledges) {
            if (leaderId1 == objectId)
                return true
        }

        return false
    }

    /**
     * Retrieve subPledge by type
     * @param pledgeType
     * @return the subpledge object.
     */
    fun getSubPledge(pledgeType: Int): SubPledge? {
        return _subPledges[pledgeType]
    }

    /**
     * Retrieve subPledge by name
     * @param pledgeName
     * @return the subpledge object.
     */
    fun getSubPledge(pledgeName: String): SubPledge? {
        return _subPledges.values.stream().filter { (_, name1) -> name1.equals(pledgeName, ignoreCase = true) }
            .findFirst().orElse(null)
    }

    fun createSubPledge(player: Player, type: Int, leaderId: Int, subPledgeName: String): SubPledge? {
        val pledgeType = getAvailablePledgeTypes(type)
        if (pledgeType == 0) {
            player.sendPacket(if (type == SUBUNIT_ACADEMY) SystemMessageId.CLAN_HAS_ALREADY_ESTABLISHED_A_CLAN_ACADEMY else SystemMessageId.YOU_DO_NOT_MEET_CRITERIA_IN_ORDER_TO_CREATE_A_MILITARY_UNIT)
            return null
        }

        if (leader!!.objectId == leaderId) {
            player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_CRITERIA_IN_ORDER_TO_CREATE_A_MILITARY_UNIT)
            return null
        }

        if (pledgeType != SUBUNIT_ACADEMY && (reputationScore < 5000 && pledgeType < SUBUNIT_KNIGHT1 || reputationScore < 10000 && pledgeType > SUBUNIT_ROYAL2)) {
            player.sendPacket(SystemMessageId.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW)
            return null
        }

        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(INSERT_SUBPLEDGE).use { ps ->
                    ps.setInt(1, clanId)
                    ps.setInt(2, pledgeType)
                    ps.setString(3, subPledgeName)
                    ps.setInt(4, if (pledgeType != SUBUNIT_ACADEMY) leaderId else 0)
                    ps.executeUpdate()
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Error creating subpledge.", e)
            return null
        }

        val subPledge = SubPledge(pledgeType, subPledgeName, leaderId)
        _subPledges[pledgeType] = subPledge

        if (pledgeType != SUBUNIT_ACADEMY) {
            if (pledgeType < SUBUNIT_KNIGHT1)
            // royal
                takeReputationScore(5000)
            else if (pledgeType > SUBUNIT_ROYAL2)
            // knight
                takeReputationScore(10000)
        }

        broadcastToOnlineMembers(PledgeShowInfoUpdate(this), PledgeReceiveSubPledgeCreated(subPledge, this))

        return subPledge
    }

    fun getAvailablePledgeTypes(pledgeType: Int): Int {
        var pledgeType = pledgeType
        if (_subPledges[pledgeType] != null) {
            when (pledgeType) {
                SUBUNIT_ACADEMY -> return 0

                SUBUNIT_ROYAL1 -> pledgeType = getAvailablePledgeTypes(SUBUNIT_ROYAL2)

                SUBUNIT_ROYAL2 -> return 0

                SUBUNIT_KNIGHT1 -> pledgeType = getAvailablePledgeTypes(SUBUNIT_KNIGHT2)

                SUBUNIT_KNIGHT2 -> pledgeType = getAvailablePledgeTypes(SUBUNIT_KNIGHT3)

                SUBUNIT_KNIGHT3 -> pledgeType = getAvailablePledgeTypes(SUBUNIT_KNIGHT4)

                SUBUNIT_KNIGHT4 -> return 0
            }
        }
        return pledgeType
    }

    fun updateSubPledgeInDB(pledge: SubPledge) {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(UPDATE_SUBPLEDGE).use { ps ->
                    ps.setInt(1, pledge.leaderId)
                    ps.setString(2, pledge.name)
                    ps.setInt(3, clanId)
                    ps.setInt(4, pledge.id)
                    ps.executeUpdate()
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Error updating subpledge.", e)
        }

    }

    fun getPriviledgesByRank(rank: Int): Int {
        return (_priviledges as java.util.Map<Int, Int>).getOrDefault(rank, CP_NOTHING)
    }

    /**
     * Update ranks priviledges.
     * @param rank : The rank to edit.
     * @param privs : The priviledges to be set.
     */
    fun setPriviledgesForRank(rank: Int, privs: Int) {
        // Avoid to bother with invalid ranks.
        if (!_priviledges.containsKey(rank))
            return

        // Replace the priviledges.
        _priviledges[rank] = privs

        // Refresh online members priviledges.
        for (member in onlineMembers) {
            if (member.powerGrade == rank)
                member.clanPrivileges = privs
        }
        broadcastClanStatus()

        // Update database.
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(ADD_OR_UPDATE_PRIVILEDGE).use { ps ->
                    ps.setInt(1, clanId)
                    ps.setInt(2, rank)
                    ps.setInt(3, privs)
                    ps.executeUpdate()
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Error while storing rank.", e)
        }

    }

    fun getLeaderSubPledge(leaderId: Int): Int {
        var id = 0
        for ((id1, _, leaderId1) in _subPledges.values) {
            if (leaderId1 == 0)
                continue

            if (leaderId1 == leaderId)
                id = id1
        }
        return id
    }

    /**
     * Add the value to the total amount of the clan's reputation score.<br></br>
     * **This method updates the database.**
     * @param value : The value to add to current amount.
     */
    @Synchronized
    fun addReputationScore(value: Int) {
        reputationScore = reputationScore + value
    }

    /**
     * Removes the value to the total amount of the clan's reputation score.<br></br>
     * **This method updates the database.**
     * @param value : The value to remove to current amount.
     */
    @Synchronized
    fun takeReputationScore(value: Int) {
        reputationScore = reputationScore - value
    }

    /**
     * Checks if activeChar and target meet various conditions to join a clan
     * @param activeChar
     * @param target
     * @param pledgeType
     * @return
     */
    fun checkClanJoinCondition(activeChar: Player?, target: Player?, pledgeType: Int): Boolean {
        if (activeChar == null)
            return false

        if (activeChar.clanPrivileges and CP_CL_JOIN_CLAN != CP_CL_JOIN_CLAN) {
            activeChar.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT)
            return false
        }

        if (target == null) {
            activeChar.sendPacket(SystemMessageId.YOU_HAVE_INVITED_THE_WRONG_TARGET)
            return false
        }

        if (activeChar.objectId == target.objectId) {
            activeChar.sendPacket(SystemMessageId.CANNOT_INVITE_YOURSELF)
            return false
        }

        if (charPenaltyExpiryTime > System.currentTimeMillis()) {
            activeChar.sendPacket(SystemMessageId.YOU_MUST_WAIT_BEFORE_ACCEPTING_A_NEW_MEMBER)
            return false
        }

        if (target.clanId != 0) {
            activeChar.sendPacket(
                SystemMessage.getSystemMessage(SystemMessageId.S1_WORKING_WITH_ANOTHER_CLAN).addCharName(
                    target
                )
            )
            return false
        }

        if (target.clanJoinExpiryTime > System.currentTimeMillis()) {
            activeChar.sendPacket(
                SystemMessage.getSystemMessage(SystemMessageId.S1_MUST_WAIT_BEFORE_JOINING_ANOTHER_CLAN).addCharName(
                    target
                )
            )
            return false
        }

        if ((target.level > 40 || target.classId.level() >= 2) && pledgeType == SUBUNIT_ACADEMY) {
            activeChar.sendPacket(
                SystemMessage.getSystemMessage(SystemMessageId.S1_DOESNOT_MEET_REQUIREMENTS_TO_JOIN_ACADEMY).addCharName(
                    target
                )
            )
            activeChar.sendPacket(SystemMessageId.ACADEMY_REQUIREMENTS)
            return false
        }

        if (getSubPledgeMembersCount(pledgeType) >= getMaxNrOfMembers(pledgeType)) {
            if (pledgeType == 0)
                activeChar.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CLAN_IS_FULL).addString(name!!))
            else
                activeChar.sendPacket(SystemMessageId.SUBCLAN_IS_FULL)

            return false
        }
        return true
    }

    fun createAlly(player: Player?, allyName: String) {
        if (player == null)
            return

        if (!player.isClanLeader) {
            player.sendPacket(SystemMessageId.ONLY_CLAN_LEADER_CREATE_ALLIANCE)
            return
        }

        if (allyId != 0) {
            player.sendPacket(SystemMessageId.ALREADY_JOINED_ALLIANCE)
            return
        }

        if (level < 5) {
            player.sendPacket(SystemMessageId.TO_CREATE_AN_ALLY_YOU_CLAN_MUST_BE_LEVEL_5_OR_HIGHER)
            return
        }

        if (allyPenaltyType == PENALTY_TYPE_DISSOLVE_ALLY && allyPenaltyExpiryTime > System.currentTimeMillis()) {
            player.sendPacket(SystemMessageId.CANT_CREATE_ALLIANCE_10_DAYS_DISOLUTION)
            return
        }

        if (dissolvingExpiryTime > System.currentTimeMillis()) {
            player.sendPacket(SystemMessageId.YOU_MAY_NOT_CREATE_ALLY_WHILE_DISSOLVING)
            return
        }

        if (!StringUtil.isAlphaNumeric(allyName)) {
            player.sendPacket(SystemMessageId.INCORRECT_ALLIANCE_NAME)
            return
        }

        if (allyName.length > 16 || allyName.length < 2) {
            player.sendPacket(SystemMessageId.INCORRECT_ALLIANCE_NAME_LENGTH)
            return
        }

        if (ClanTable.isAllyExists(allyName)) {
            player.sendPacket(SystemMessageId.ALLIANCE_ALREADY_EXISTS)
            return
        }

        for (castle in CastleManager.castles) {
            if (castle.siege.isInProgress && castle.siege.checkSides(this)) {
                player.sendPacket(SystemMessageId.NO_ALLY_CREATION_WHILE_SIEGE)
                return
            }
        }

        allyId = clanId
        this.allyName = allyName
        setAllyPenaltyExpiryTime(0, 0)
        updateClanInDB()

        player.sendPacket(UserInfo(player))
    }

    fun dissolveAlly(player: Player) {
        if (allyId == 0) {
            player.sendPacket(SystemMessageId.NO_CURRENT_ALLIANCES)
            return
        }

        if (!player.isClanLeader || clanId != allyId) {
            player.sendPacket(SystemMessageId.FEATURE_ONLY_FOR_ALLIANCE_LEADER)
            return
        }

        // For every clan in alliance, check if it is currently registered on siege.
        for (clan in ClanTable.getClanAllies(allyId)) {
            for (castle in CastleManager.castles) {
                if (castle.siege.isInProgress && castle.siege.checkSides(clan)) {
                    player.sendPacket(SystemMessageId.CANNOT_DISSOLVE_ALLY_WHILE_IN_SIEGE)
                    return
                }
            }
        }

        broadcastToOnlineAllyMembers(SystemMessage.getSystemMessage(SystemMessageId.ALLIANCE_DISOLVED))

        val currentTime = System.currentTimeMillis()
        for (clan in ClanTable.clans) {
            if (clan.allyId == allyId && clan.clanId != clanId) {
                clan.allyId = 0
                clan.allyName = null
                clan.setAllyPenaltyExpiryTime(0, 0)
                clan.updateClanInDB()
            }
        }

        allyId = 0
        allyName = null
        changeAllyCrest(0, false)
        setAllyPenaltyExpiryTime(
            currentTime + Config.ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED * 86400000L,
            PENALTY_TYPE_DISSOLVE_ALLY
        )
        updateClanInDB()

        // The clan leader should take the XP penalty of a full death.
        player.deathPenalty(false, false, false)
    }

    fun levelUpClan(player: Player): Boolean {
        if (!player.isClanLeader) {
            player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT)
            return false
        }

        if (System.currentTimeMillis() < dissolvingExpiryTime) {
            player.sendPacket(SystemMessageId.CANNOT_RISE_LEVEL_WHILE_DISSOLUTION_IN_PROGRESS)
            return false
        }

        var increaseClanLevel = false

        when (level) {
            0 // upgrade to 1
            -> if (player.sp >= 30000 && player.reduceAdena("ClanLvl", 650000, player.target, true)) {
                player.removeExpAndSp(0, 30000)
                increaseClanLevel = true
            }

            1 // upgrade to 2
            -> if (player.sp >= 150000 && player.reduceAdena("ClanLvl", 2500000, player.target, true)) {
                player.removeExpAndSp(0, 150000)
                increaseClanLevel = true
            }

            2// upgrade to 3
            -> if (player.sp >= 500000 && player.destroyItemByItemId("ClanLvl", 1419, 1, player.target, true)) {
                player.removeExpAndSp(0, 500000)
                increaseClanLevel = true
            }

            3 // upgrade to 4
            -> if (player.sp >= 1400000 && player.destroyItemByItemId("ClanLvl", 3874, 1, player.target, true)) {
                player.removeExpAndSp(0, 1400000)
                increaseClanLevel = true
            }

            4 // upgrade to 5
            -> if (player.sp >= 3500000 && player.destroyItemByItemId("ClanLvl", 3870, 1, player.target, true)) {
                player.removeExpAndSp(0, 3500000)
                increaseClanLevel = true
            }

            5 -> if (reputationScore >= 10000 && membersCount >= 30) {
                takeReputationScore(10000)
                player.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP).addNumber(
                        10000
                    )
                )
                increaseClanLevel = true
            }

            6 -> if (reputationScore >= 20000 && membersCount >= 80) {
                takeReputationScore(20000)
                player.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP).addNumber(
                        20000
                    )
                )
                increaseClanLevel = true
            }

            7 -> if (reputationScore >= 40000 && membersCount >= 120) {
                takeReputationScore(40000)
                player.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP).addNumber(
                        40000
                    )
                )
                increaseClanLevel = true
            }
        }

        if (!increaseClanLevel) {
            player.sendPacket(SystemMessageId.FAILED_TO_INCREASE_CLAN_LEVEL)
            return false
        }

        player.sendPacket(ItemList(player, false))

        changeLevel(level + 1)
        return true
    }

    fun changeLevel(level: Int) {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(UPDATE_CLAN_LEVEL).use { ps ->
                    ps.setInt(1, level)
                    ps.setInt(2, clanId)
                    ps.executeUpdate()
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Error while updating clan level.", e)
        }

        this@Clan.level = level

        if (leader!!.isOnline) {
            val leader = this.leader!!.playerInstance
            if (3 < level)
                leader!!.addSiegeSkills()
            else if (4 > level)
                leader!!.removeSiegeSkills()

            if (4 < level)
                leader!!.sendPacket(SystemMessageId.CLAN_CAN_ACCUMULATE_CLAN_REPUTATION_POINTS)
        }

        broadcastToOnlineMembers(
            PledgeShowInfoUpdate(this),
            SystemMessage.getSystemMessage(SystemMessageId.CLAN_LEVEL_INCREASED)
        )
    }

    /**
     * Change the clan crest. Old crest is removed. New crest id is saved to database.
     * @param crestId : The new crest id is set and saved to database.
     */
    fun changeClanCrest(crestId: Int) {
        // Delete previous crest if existing.
        if (this.crestId != 0)
            CrestCache.removeCrest(CrestCache.CrestType.PLEDGE, this.crestId)

        this.crestId = crestId

        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(UPDATE_CLAN_CREST).use { ps ->
                    ps.setInt(1, crestId)
                    ps.setInt(2, clanId)
                    ps.executeUpdate()
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Error while updating clan crest.", e)
        }

        for (member in onlineMembers)
            member.broadcastUserInfo()
    }

    /**
     * Change the ally crest. Old crest is removed. New crest id is saved to database.
     * @param crestId : The new crest id is set and saved to database.
     * @param onlyThisClan : If false, do it for the ally aswell.
     */
    fun changeAllyCrest(crestId: Int, onlyThisClan: Boolean) {
        var query = "UPDATE clan_data SET ally_crest_id = ? WHERE clan_id = ?"
        var allyId = clanId
        if (!onlyThisClan) {
            // Delete previous crest if existing.
            if (allyCrestId != 0)
                CrestCache.removeCrest(CrestCache.CrestType.ALLY, allyCrestId)

            query = "UPDATE clan_data SET ally_crest_id = ? WHERE ally_id = ?"
            allyId = this.allyId
        }

        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(query).use { ps ->
                    ps.setInt(1, crestId)
                    ps.setInt(2, allyId)
                    ps.executeUpdate()
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Error while updating ally crest.", e)
        }

        if (onlyThisClan) {
            allyCrestId = crestId
            for (member in onlineMembers)
                member.broadcastUserInfo()
        } else {
            for (clan in ClanTable.clans) {
                if (clan.allyId == this.allyId) {
                    clan.allyCrestId = crestId
                    for (member in clan.onlineMembers)
                        member.broadcastUserInfo()
                }
            }
        }
    }

    /**
     * Change the large crest. Old crest is removed. New crest id is saved to database.
     * @param crestId : The new crest id is set and saved to database.
     */
    fun changeLargeCrest(crestId: Int) {
        // Delete previous crest if existing.
        if (crestLargeId != 0)
            CrestCache.removeCrest(CrestCache.CrestType.PLEDGE_LARGE, crestLargeId)

        crestLargeId = crestId

        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(UPDATE_LARGE_CREST).use { ps ->
                    ps.setInt(1, crestId)
                    ps.setInt(2, clanId)
                    ps.executeUpdate()
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Error while updating large crest.", e)
        }

        for (member in onlineMembers)
            member.broadcastUserInfo()
    }

    companion object {
        private val LOGGER = CLogger(Clan::class.java.name)

        private const val LOAD_MEMBERS =
            "SELECT char_name,level,classid,obj_Id,title,power_grade,subpledge,apprentice,sponsor,sex,race FROM characters WHERE clanid=?"
        private const val LOAD_SUBPLEDGES = "SELECT sub_pledge_id,name,leader_id FROM clan_subpledges WHERE clan_id=?"
        private const val LOAD_PRIVILEDGES = "SELECT privs,rank FROM clan_privs WHERE clan_id=?"
        private const val LOAD_SKILLS = "SELECT skill_id,skill_level FROM clan_skills WHERE clan_id=?"

        private const val RESET_MEMBER_PRIVS = "UPDATE characters SET clan_privs=0 WHERE obj_Id=?"

        private const val REMOVE_MEMBER =
            "UPDATE characters SET clanid=0, title=?, clan_join_expiry_time=?, clan_create_expiry_time=?, clan_privs=0, wantspeace=0, subpledge=0, lvl_joined_academy=0, apprentice=0, sponsor=0 WHERE obj_Id=?"
        private const val REMOVE_APPRENTICE = "UPDATE characters SET apprentice=0 WHERE apprentice=?"
        private const val REMOVE_SPONSOR = "UPDATE characters SET sponsor=0 WHERE sponsor=?"

        private const val UPDATE_CLAN =
            "UPDATE clan_data SET leader_id=?,new_leader_id=?,ally_id=?,ally_name=?,reputation_score=?,ally_penalty_expiry_time=?,ally_penalty_type=?,char_penalty_expiry_time=?,dissolving_expiry_time=? WHERE clan_id=?"
        private const val STORE_CLAN =
            "INSERT INTO clan_data (clan_id,clan_name,clan_level,hasCastle,ally_id,ally_name,leader_id,new_leader_id,crest_id,crest_large_id,ally_crest_id) VALUES (?,?,?,?,?,?,?,?,?,?,?)"

        private const val UPDATE_NOTICE = "UPDATE clan_data SET enabled=?,notice=? WHERE clan_id=?"
        private const val UPDATE_INTRODUCTION = "UPDATE clan_data SET introduction=? WHERE clan_id=?"

        private const val ADD_OR_UPDATE_SKILL =
            "INSERT INTO clan_skills (clan_id,skill_id,skill_level) VALUES (?,?,?) ON DUPLICATE KEY UPDATE skill_level=VALUES(skill_level)"

        private const val INSERT_SUBPLEDGE =
            "INSERT INTO clan_subpledges (clan_id,sub_pledge_id,name,leader_id) values (?,?,?,?)"
        private const val UPDATE_SUBPLEDGE =
            "UPDATE clan_subpledges SET leader_id=?, name=? WHERE clan_id=? AND sub_pledge_id=?"

        private const val ADD_OR_UPDATE_PRIVILEDGE =
            "INSERT INTO clan_privs (clan_id,rank,privs) VALUES (?,?,?) ON DUPLICATE KEY UPDATE privs=VALUES(privs)"

        private const val UPDATE_CRP = "UPDATE clan_data SET reputation_score=? WHERE clan_id=?"
        private const val UPDATE_AUCTION = "UPDATE clan_data SET auction_bid_at=? WHERE clan_id=?"
        private const val UPDATE_CLAN_LEVEL = "UPDATE clan_data SET clan_level = ? WHERE clan_id = ?"
        private const val UPDATE_CLAN_CREST = "UPDATE clan_data SET crest_id = ? WHERE clan_id = ?"
        private const val UPDATE_LARGE_CREST = "UPDATE clan_data SET crest_large_id = ? WHERE clan_id = ?"

        // Ally Penalty Types
        const val PENALTY_TYPE_CLAN_LEAVED = 1
        const val PENALTY_TYPE_CLAN_DISMISSED = 2
        const val PENALTY_TYPE_DISMISS_CLAN = 3
        const val PENALTY_TYPE_DISSOLVE_ALLY = 4

        // Clan Privileges
        const val CP_NOTHING = 0
        const val CP_CL_JOIN_CLAN = 2
        const val CP_CL_GIVE_TITLE = 4
        const val CP_CL_VIEW_WAREHOUSE = 8
        const val CP_CL_MANAGE_RANKS = 16
        const val CP_CL_PLEDGE_WAR = 32
        const val CP_CL_DISMISS = 64
        const val CP_CL_REGISTER_CREST = 128
        const val CP_CL_MASTER_RIGHTS = 256
        const val CP_CL_MANAGE_LEVELS = 512
        const val CP_CH_OPEN_DOOR = 1024
        const val CP_CH_OTHER_RIGHTS = 2048
        const val CP_CH_AUCTION = 4096
        const val CP_CH_DISMISS = 8192
        const val CP_CH_SET_FUNCTIONS = 16384
        const val CP_CS_OPEN_DOOR = 32768
        const val CP_CS_MANOR_ADMIN = 65536
        const val CP_CS_MANAGE_SIEGE = 131072
        const val CP_CS_USE_FUNCTIONS = 262144
        const val CP_CS_DISMISS = 524288
        const val CP_CS_TAXES = 1048576
        const val CP_CS_MERCENARIES = 2097152
        const val CP_CS_SET_FUNCTIONS = 4194304
        const val CP_ALL = 8388606

        // Sub-unit types
        const val SUBUNIT_ACADEMY = -1
        const val SUBUNIT_ROYAL1 = 100
        const val SUBUNIT_ROYAL2 = 200
        const val SUBUNIT_KNIGHT1 = 1001
        const val SUBUNIT_KNIGHT2 = 1002
        const val SUBUNIT_KNIGHT3 = 2001
        const val SUBUNIT_KNIGHT4 = 2002

        private const val MAX_NOTICE_LENGTH = 8192
        private const val MAX_INTRODUCTION_LENGTH = 300

        /**
         * @param player : The player alliance launching the invitation.
         * @param target : The target clan to recruit.
         * @return true if target's clan meet conditions to join player's alliance.
         */
        fun checkAllyJoinCondition(player: Player?, target: Player?): Boolean {
            if (player == null)
                return false

            if (player.allyId == 0 || !player.isClanLeader || player.clanId != player.allyId) {
                player.sendPacket(SystemMessageId.FEATURE_ONLY_FOR_ALLIANCE_LEADER)
                return false
            }

            val leaderClan = player.clan
            if (leaderClan.allyPenaltyType == PENALTY_TYPE_DISMISS_CLAN && leaderClan.allyPenaltyExpiryTime > System.currentTimeMillis()) {
                player.sendPacket(SystemMessageId.CANT_INVITE_CLAN_WITHIN_1_DAY)
                return false
            }

            if (target == null) {
                player.sendPacket(SystemMessageId.SELECT_USER_TO_INVITE)
                return false
            }

            if (player.objectId == target.objectId) {
                player.sendPacket(SystemMessageId.CANNOT_INVITE_YOURSELF)
                return false
            }

            if (target.clan == null) {
                player.sendPacket(SystemMessageId.TARGET_MUST_BE_IN_CLAN)
                return false
            }

            if (!target.isClanLeader) {
                player.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.S1_IS_NOT_A_CLAN_LEADER).addCharName(
                        target
                    )
                )
                return false
            }

            val targetClan = target.clan
            if (target.allyId != 0) {
                player.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.S1_CLAN_ALREADY_MEMBER_OF_S2_ALLIANCE).addString(
                        targetClan.name!!
                    ).addString(targetClan.allyName!!)
                )
                return false
            }

            if (targetClan.allyPenaltyExpiryTime > System.currentTimeMillis()) {
                if (targetClan.allyPenaltyType == PENALTY_TYPE_CLAN_LEAVED) {
                    player.sendPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.S1_CANT_ENTER_ALLIANCE_WITHIN_1_DAY).addString(
                            targetClan.name!!
                        )
                    )
                    return false
                }

                if (targetClan.allyPenaltyType == PENALTY_TYPE_CLAN_DISMISSED) {
                    player.sendPacket(SystemMessageId.CANT_ENTER_ALLIANCE_WITHIN_1_DAY)
                    return false
                }
            }

            // Check if clans are registered as opponents on the same siege.
            for (castle in CastleManager.castles) {
                if (castle.siege.isOnOppositeSide(leaderClan, targetClan)) {
                    player.sendPacket(SystemMessageId.OPPOSING_CLAN_IS_PARTICIPATING_IN_SIEGE)
                    return false
                }
            }

            if (leaderClan.isAtWarWith(targetClan.clanId)) {
                player.sendPacket(SystemMessageId.MAY_NOT_ALLY_CLAN_BATTLE)
                return false
            }

            if (ClanTable.getClanAllies(player.allyId).size >= Config.ALT_MAX_NUM_OF_CLANS_IN_ALLY) {
                player.sendPacket(SystemMessageId.YOU_HAVE_EXCEEDED_THE_LIMIT)
                return false
            }

            return true
        }
    }
}