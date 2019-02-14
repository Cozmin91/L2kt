package com.l2kt.gameserver.model.pledge

import com.l2kt.L2DatabaseFactory
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.base.Sex
import java.sql.ResultSet
import java.sql.SQLException

class ClanMember {
    val clan: Clan
    private var _objectId: Int = 0
    private var _name: String
    private var _title: String
    private var _powerGrade: Int = 0
    private var _level: Int = 0
    private var _classId: Int = 0
    private var _sex: Sex
    private var _raceOrdinal: Int = 0
    private var _player: Player? = null
    private var _pledgeType: Int = 0
    private var _apprentice: Int = 0
    private var _sponsor: Int = 0

    var playerInstance: Player?
        get() = _player
        set(player) {
            if (player == null && _player != null) {
                _name = _player!!.name
                _level = _player!!.level
                _classId = _player!!.classId.id
                _objectId = _player!!.objectId
                _powerGrade = _player!!.powerGrade
                _pledgeType = _player!!.pledgeType
                _title = _player!!.title
                _apprentice = _player!!.apprentice
                _sponsor = _player!!.sponsor
                _sex = _player!!.appearance.sex
                _raceOrdinal = _player!!.race.ordinal
            }

            if (player != null) {
                if (clan.level > 3 && player.isClanLeader)
                    player.addSiegeSkills()

                if (clan.reputationScore >= 0) {
                    for (sk in clan.clanSkills.values) {
                        if (sk.minPledgeClass <= player.pledgeClass)
                            player.addSkill(sk, false)
                    }
                }
            }
            _player = player
        }

    val isOnline: Boolean
        get() = _player != null && _player!!.client != null && !_player!!.client.isDetached

    val classId: Int
        get() = if (_player != null) _player!!.classId.id else _classId

    val level: Int
        get() = if (_player != null) _player!!.level else _level

    val name: String
        get() = if (_player != null) _player!!.name else _name

    val objectId: Int
        get() = if (_player != null) _player!!.objectId else _objectId

    val title: String
        get() = if (_player != null) _player!!.title else _title

    var pledgeType: Int
        get() = if (_player != null) _player!!.pledgeType else _pledgeType
        set(pledgeType) {
            _pledgeType = pledgeType

            if (_player != null)
                _player!!.pledgeType = pledgeType
            else
                updatePledgeType()
        }

    var powerGrade: Int
        get() = if (_player != null) _player!!.powerGrade else _powerGrade
        set(powerGrade) {
            _powerGrade = powerGrade

            if (_player != null)
                _player!!.powerGrade = powerGrade
            else
                updatePowerGrade()
        }

    val raceOrdinal: Int
        get() = if (_player != null) _player!!.race.ordinal else _raceOrdinal

    val sex: Sex
        get() = if (_player != null) _player!!.appearance.sex else _sex

    val sponsor: Int
        get() = if (_player != null) _player!!.sponsor else _sponsor

    val apprentice: Int
        get() = if (_player != null) _player!!.apprentice else _apprentice

    val apprenticeOrSponsorName: String?
        get() {
            if (_player != null) {
                _apprentice = _player!!.apprentice
                _sponsor = _player!!.sponsor
            }

            if (_apprentice != 0) {
                val apprentice = clan.getClanMember(_apprentice)
                return if (apprentice != null) apprentice.name else "Error"

            }

            if (_sponsor != 0) {
                val sponsor = clan.getClanMember(_sponsor)
                return if (sponsor != null) sponsor.name else "Error"

            }
            return ""
        }

    /**
     * Used to restore a clan member from the database.
     * @param clan the clan where the clan member belongs.
     * @param clanMember the clan member result set
     * @throws SQLException if the columnLabel is not valid or a database error occurs
     */
    @Throws(SQLException::class)
    constructor(clan: Clan?, clanMember: ResultSet) {
        if (clan == null)
            throw IllegalArgumentException("Cannot create a clan member with a null clan.")

        this.clan = clan
        _name = clanMember.getString("char_name")
        _level = clanMember.getInt("level")
        _classId = clanMember.getInt("classid")
        _objectId = clanMember.getInt("obj_Id")
        _pledgeType = clanMember.getInt("subpledge")
        _title = clanMember.getString("title")
        _powerGrade = clanMember.getInt("power_grade")
        _apprentice = clanMember.getInt("apprentice")
        _sponsor = clanMember.getInt("sponsor")
        _sex = Sex.values()[clanMember.getInt("sex")]
        _raceOrdinal = clanMember.getInt("race")
    }

    /**
     * Creates a clan member from a player instance.
     * @param clan the clan where the player belongs
     * @param player the player from which the clan member will be created
     */
    constructor(clan: Clan?, player: Player) {
        if (clan == null)
            throw IllegalArgumentException("Cannot create a clan member with a null clan.")

        _player = player
        this.clan = clan
        _name = player.name
        _level = player.level
        _classId = player.classId.id
        _objectId = player.objectId
        _pledgeType = player.pledgeType
        _powerGrade = player.powerGrade
        _title = player.title
        _sponsor = 0
        _apprentice = 0
        _sex = player.appearance.sex
        _raceOrdinal = player.race.ordinal
    }

    fun refreshLevel() {
        if (_player != null)
            _level = _player!!.level
    }

    fun updatePledgeType() {
        L2DatabaseFactory.connection.use { con ->
            val statement = con.prepareStatement("UPDATE characters SET subpledge=? WHERE obj_id=?")
            statement.setInt(1, _pledgeType)
            statement.setInt(2, objectId)
            statement.execute()
            statement.close()
        }
    }

    /**
     * Update the characters table of the database with power grade.
     */
    fun updatePowerGrade() {
        L2DatabaseFactory.connection.use { con ->
            val statement = con.prepareStatement("UPDATE characters SET power_grade=? WHERE obj_id=?")
            statement.setInt(1, _powerGrade)
            statement.setInt(2, objectId)
            statement.execute()
            statement.close()
        }
    }

    fun setApprenticeAndSponsor(apprenticeId: Int, sponsorId: Int) {
        _apprentice = apprenticeId
        _sponsor = sponsorId
    }

    fun saveApprenticeAndSponsor(apprentice: Int, sponsor: Int) {
        try {
            L2DatabaseFactory.connection.use { con ->
                val statement = con.prepareStatement("UPDATE characters SET apprentice=?,sponsor=? WHERE obj_Id=?")
                statement.setInt(1, apprentice)
                statement.setInt(2, sponsor)
                statement.setInt(3, objectId)
                statement.execute()
                statement.close()
            }
        } catch (e: SQLException) {
        }

    }

    companion object {

        /**
         * Calculate player's pledge class.
         * @param player The player to test.
         * @return the pledge class, under an int.
         */
        fun calculatePledgeClass(player: Player): Int {
            var pledgeClass = 0

            val clan = player.clan
            if (clan != null) {
                when (clan.level) {
                    4 -> if (player.isClanLeader)
                        pledgeClass = 3

                    5 -> if (player.isClanLeader)
                        pledgeClass = 4
                    else
                        pledgeClass = 2

                    6 -> when (player.pledgeType) {
                        -1 -> pledgeClass = 1

                        100, 200 -> pledgeClass = 2

                        0 -> if (player.isClanLeader)
                            pledgeClass = 5
                        else
                            when (clan.getLeaderSubPledge(player.objectId)) {
                                100, 200 -> pledgeClass = 4

                                -1 -> pledgeClass = 3
                                else -> pledgeClass = 3
                            }
                    }

                    7 -> when (player.pledgeType) {
                        -1 -> pledgeClass = 1

                        100, 200 -> pledgeClass = 3

                        1001, 1002, 2001, 2002 -> pledgeClass = 2

                        0 -> if (player.isClanLeader)
                            pledgeClass = 7
                        else
                            when (clan.getLeaderSubPledge(player.objectId)) {
                                100, 200 -> pledgeClass = 6

                                1001, 1002, 2001, 2002 -> pledgeClass = 5

                                -1 -> pledgeClass = 4
                                else -> pledgeClass = 4
                            }
                    }

                    8 -> when (player.pledgeType) {
                        -1 -> pledgeClass = 1

                        100, 200 -> pledgeClass = 4

                        1001, 1002, 2001, 2002 -> pledgeClass = 3

                        0 -> if (player.isClanLeader)
                            pledgeClass = 8
                        else
                            when (clan.getLeaderSubPledge(player.objectId)) {
                                100, 200 -> pledgeClass = 7

                                1001, 1002, 2001, 2002 -> pledgeClass = 6

                                -1 -> pledgeClass = 5
                                else -> pledgeClass = 5
                            }
                    }

                    else -> pledgeClass = 1
                }
            }

            if (player.isHero && pledgeClass < 8)
                pledgeClass = 8
            else if (player.isNoble && pledgeClass < 5)
                pledgeClass = 5

            return pledgeClass
        }
    }
}