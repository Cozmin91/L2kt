package com.l2kt.gameserver.model.olympiad

import com.l2kt.Config
import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.model.zone.type.OlympiadStadiumZone
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ExOlympiadUserInfo
import com.l2kt.gameserver.network.serverpackets.L2GameServerPacket
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import java.sql.SQLException
import java.util.logging.Level

/**
 * @author GodKratos, Pere, DS
 */
abstract class OlympiadGameNormal protected constructor(id: Int, opponents: Array<Participant>) :
    AbstractOlympiadGame(id) {
    protected var _damageP1 = 0
    protected var _damageP2 = 0

    protected var _playerOne: Participant? = null
    protected var _playerTwo: Participant? = null

    override val playerNames: Array<String>
        get() = arrayOf(_playerOne!!.name, _playerTwo!!.name)

    init {

        _playerOne = opponents[0]
        _playerTwo = opponents[1]

        _playerOne!!.player!!.olympiadGameId = id
        _playerTwo!!.player!!.olympiadGameId = id
    }

    override fun containsParticipant(playerId: Int): Boolean {
        return _playerOne!!.objectId == playerId || _playerTwo!!.objectId == playerId
    }

    override
            /**
             * Sends olympiad info to the new spectator.
             */
    fun sendOlympiadInfo(player: Creature) {
        player.sendPacket(ExOlympiadUserInfo(_playerOne!!.player!!))
        _playerOne!!.player!!.updateEffectIcons()
        player.sendPacket(ExOlympiadUserInfo(_playerTwo!!.player!!))
        _playerTwo!!.player!!.updateEffectIcons()
    }

    override
            /**
             * Broadcasts olympiad info to participants and spectators on battle start.
             */
    fun broadcastOlympiadInfo(stadium: OlympiadStadiumZone) {
        stadium.broadcastPacket(ExOlympiadUserInfo(_playerOne!!.player!!))
        _playerOne!!.player!!.updateEffectIcons()
        stadium.broadcastPacket(ExOlympiadUserInfo(_playerTwo!!.player!!))
        _playerTwo!!.player!!.updateEffectIcons()
    }

    override
            /**
             * Broadcasts packet to participants only.
             */
    fun broadcastPacket(packet: L2GameServerPacket) {
        _playerOne!!.updatePlayer()
        if (_playerOne!!.player != null)
            _playerOne!!.player!!.sendPacket(packet)

        _playerTwo!!.updatePlayer()
        if (_playerTwo!!.player != null)
            _playerTwo!!.player!!.sendPacket(packet)
    }

    override fun portPlayersToArena(spawns: List<Location>): Boolean {
        var result = true
        try {
            result = result and portPlayerToArena(_playerOne!!, spawns[0], stadiumId)
            result = result and portPlayerToArena(_playerTwo!!, spawns[1], stadiumId)
        } catch (e: Exception) {
            return false
        }

        return result
    }

    override fun removals() {
        if (isAborted)
            return

        removals(_playerOne!!.player, true)
        removals(_playerTwo!!.player, true)
    }

    override fun buffPlayers() {
        if (isAborted)
            return

        buffPlayer(_playerOne!!.player!!)
        buffPlayer(_playerTwo!!.player!!)
    }

    override fun healPlayers() {
        if (isAborted)
            return

        healPlayer(_playerOne!!.player!!)
        healPlayer(_playerTwo!!.player!!)
    }

    override fun makeCompetitionStart(): Boolean {
        if (!super.makeCompetitionStart())
            return false

        if (_playerOne!!.player == null || _playerTwo!!.player == null)
            return false

        _playerOne!!.player!!.isOlympiadStart = true
        _playerTwo!!.player!!.isOlympiadStart = true
        return true
    }

    override fun cleanEffects() {
        if (_playerOne!!.player != null && !_playerOne!!.defaulted && !_playerOne!!.disconnected && _playerOne!!.player!!.olympiadGameId == stadiumId)
            cleanEffects(_playerOne!!.player!!)

        if (_playerTwo!!.player != null && !_playerTwo!!.defaulted && !_playerTwo!!.disconnected && _playerTwo!!.player!!.olympiadGameId == stadiumId)
            cleanEffects(_playerTwo!!.player!!)
    }

    override fun portPlayersBack() {
        if (_playerOne!!.player != null && !_playerOne!!.defaulted && !_playerOne!!.disconnected)
            portPlayerBack(_playerOne!!.player)
        if (_playerTwo!!.player != null && !_playerTwo!!.defaulted && !_playerTwo!!.disconnected)
            portPlayerBack(_playerTwo!!.player)
    }

    override fun playersStatusBack() {
        if (_playerOne!!.player != null && !_playerOne!!.defaulted && !_playerOne!!.disconnected && _playerOne!!.player!!.olympiadGameId == stadiumId)
            playerStatusBack(_playerOne!!.player!!)

        if (_playerTwo!!.player != null && !_playerTwo!!.defaulted && !_playerTwo!!.disconnected && _playerTwo!!.player!!.olympiadGameId == stadiumId)
            playerStatusBack(_playerTwo!!.player!!)
    }

    override fun clearPlayers() {
        _playerOne!!.player = null
        _playerOne = null
        _playerTwo!!.player = null
        _playerTwo = null
    }

    override fun handleDisconnect(player: Player) {
        if (player.objectId == _playerOne!!.objectId)
            _playerOne!!.disconnected = true
        else if (player.objectId == _playerTwo!!.objectId)
            _playerTwo!!.disconnected = true
    }

    override fun checkBattleStatus(): Boolean {
        if (isAborted)
            return false

        if (_playerOne!!.player == null || _playerOne!!.disconnected)
            return false

        return !(_playerTwo!!.player == null || _playerTwo!!.disconnected)

    }

    override fun haveWinner(): Boolean {
        if (!checkBattleStatus())
            return true

        var playerOneLost = true
        try {
            if (_playerOne!!.player!!.olympiadGameId == stadiumId)
                playerOneLost = _playerOne!!.player!!.isDead
        } catch (e: Exception) {
            playerOneLost = true
        }

        var playerTwoLost = true
        try {
            if (_playerTwo!!.player!!.olympiadGameId == stadiumId)
                playerTwoLost = _playerTwo!!.player!!.isDead
        } catch (e: Exception) {
            playerTwoLost = true
        }

        return playerOneLost || playerTwoLost
    }

    override fun validateWinner(stadium: OlympiadStadiumZone) {
        if (isAborted)
            return

        val _pOneCrash = _playerOne!!.player == null || _playerOne!!.disconnected
        val _pTwoCrash = _playerTwo!!.player == null || _playerTwo!!.disconnected

        val playerOnePoints = _playerOne!!.stats!!.getInteger(POINTS)
        val playerTwoPoints = _playerTwo!!.stats!!.getInteger(POINTS)

        var pointDiff = Math.min(playerOnePoints, playerTwoPoints) / divider
        if (pointDiff <= 0)
            pointDiff = 1
        else if (pointDiff > Config.ALT_OLY_MAX_POINTS)
            pointDiff = Config.ALT_OLY_MAX_POINTS

        val points: Int

        // Check for if a player defaulted before battle started
        if (_playerOne!!.defaulted || _playerTwo!!.defaulted) {
            try {
                checkPlayerDefeated(playerOnePoints, _playerOne!!)
                checkPlayerDefeated(playerTwoPoints, _playerTwo!!)
                return
            } catch (e: Exception) {
                _log.log(Level.WARNING, "Exception on validateWinner(): " + e.message, e)
                return
            }

        }

        // Create results for players if a player crashed
        if (_pOneCrash || _pTwoCrash) {
            try {
                if (_pTwoCrash && !_pOneCrash) {
                    stadium.broadcastPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_WON_THE_GAME).addString(
                            _playerOne!!.name
                        )
                    )

                    _playerOne!!.updateStat(COMP_WON, 1)
                    addPointsToParticipant(_playerOne!!, pointDiff)

                    _playerTwo!!.updateStat(COMP_LOST, 1)
                    removePointsFromParticipant(_playerTwo!!, pointDiff)

                    rewardParticipant(_playerOne!!.player, reward)
                } else if (_pOneCrash && !_pTwoCrash) {
                    stadium.broadcastPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_WON_THE_GAME).addString(
                            _playerTwo!!.name
                        )
                    )

                    _playerTwo!!.updateStat(COMP_WON, 1)
                    addPointsToParticipant(_playerTwo!!, pointDiff)

                    _playerOne!!.updateStat(COMP_LOST, 1)
                    removePointsFromParticipant(_playerOne!!, pointDiff)

                    rewardParticipant(_playerTwo!!.player, reward)
                } else if (_pOneCrash && _pTwoCrash) {
                    stadium.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_ENDED_IN_A_TIE))

                    _playerOne!!.updateStat(COMP_LOST, 1)
                    removePointsFromParticipant(_playerOne!!, pointDiff)

                    _playerTwo!!.updateStat(COMP_LOST, 1)
                    removePointsFromParticipant(_playerTwo!!, pointDiff)
                }

                _playerOne!!.updateStat(COMP_DONE, 1)
                _playerTwo!!.updateStat(COMP_DONE, 1)

                return
            } catch (e: Exception) {
                _log.log(Level.WARNING, "Exception on validateWinner(): " + e.message, e)
                return
            }

        }

        try {
            // Calculate Fight time
            val _fightTime = System.currentTimeMillis() - _startTime

            var playerOneHp = 0.0
            if (_playerOne!!.player != null && !_playerOne!!.player!!.isDead) {
                playerOneHp = _playerOne!!.player!!.currentHp + _playerOne!!.player!!.currentCp
                if (playerOneHp < 0.5)
                    playerOneHp = 0.0
            }

            var playerTwoHp = 0.0
            if (_playerTwo!!.player != null && !_playerTwo!!.player!!.isDead) {
                playerTwoHp = _playerTwo!!.player!!.currentHp + _playerTwo!!.player!!.currentCp
                if (playerTwoHp < 0.5)
                    playerTwoHp = 0.0
            }

            // if players crashed, search if they've relogged
            _playerOne!!.updatePlayer()
            _playerTwo!!.updatePlayer()

            if ((_playerOne!!.player == null || !_playerOne!!.player!!.isOnline) && (_playerTwo!!.player == null || !_playerTwo!!.player!!.isOnline)) {
                _playerOne!!.updateStat(COMP_DRAWN, 1)
                _playerTwo!!.updateStat(COMP_DRAWN, 1)
                stadium.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_ENDED_IN_A_TIE))
            } else if (_playerTwo!!.player == null || !_playerTwo!!.player!!.isOnline || playerTwoHp == 0.0 && playerOneHp != 0.0 || _damageP1 > _damageP2 && playerTwoHp != 0.0 && playerOneHp != 0.0) {
                stadium.broadcastPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_WON_THE_GAME).addString(
                        _playerOne!!.name
                    )
                )

                _playerOne!!.updateStat(COMP_WON, 1)
                _playerTwo!!.updateStat(COMP_LOST, 1)

                addPointsToParticipant(_playerOne!!, pointDiff)
                removePointsFromParticipant(_playerTwo!!, pointDiff)

                // Save Fight Result
                saveResults(_playerOne!!, _playerTwo!!, 1, _startTime, _fightTime, type)
                rewardParticipant(_playerOne!!.player, reward)
            } else if (_playerOne!!.player == null || !_playerOne!!.player!!.isOnline || playerOneHp == 0.0 && playerTwoHp != 0.0 || _damageP2 > _damageP1 && playerOneHp != 0.0 && playerTwoHp != 0.0) {
                stadium.broadcastPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_WON_THE_GAME).addString(
                        _playerTwo!!.name
                    )
                )

                _playerTwo!!.updateStat(COMP_WON, 1)
                _playerOne!!.updateStat(COMP_LOST, 1)

                addPointsToParticipant(_playerTwo!!, pointDiff)
                removePointsFromParticipant(_playerOne!!, pointDiff)

                // Save Fight Result
                saveResults(_playerOne!!, _playerTwo!!, 2, _startTime, _fightTime, type)
                rewardParticipant(_playerTwo!!.player, reward)
            } else {
                // Save Fight Result
                saveResults(_playerOne!!, _playerTwo!!, 0, _startTime, _fightTime, type)

                stadium.broadcastPacket(SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_ENDED_IN_A_TIE))

                removePointsFromParticipant(
                    _playerOne!!,
                    Math.min(playerOnePoints / divider, Config.ALT_OLY_MAX_POINTS)
                )
                removePointsFromParticipant(
                    _playerTwo!!,
                    Math.min(playerTwoPoints / divider, Config.ALT_OLY_MAX_POINTS)
                )
            }

            _playerOne!!.updateStat(COMP_DONE, 1)
            _playerTwo!!.updateStat(COMP_DONE, 1)
        } catch (e: Exception) {
            _log.log(Level.WARNING, "Exception on validateWinner(): " + e.message, e)
        }

    }

    private fun checkPlayerDefeated(playerOnePoints: Int, playerOne: Participant) {
        val points: Int
        if (playerOne.defaulted) {
            try {
                points = Math.min(playerOnePoints / 3, Config.ALT_OLY_MAX_POINTS)
                removePointsFromParticipant(playerOne, points)
            } catch (e: Exception) {
                _log.log(Level.WARNING, "Exception on validateWinner(): " + e.message, e)
            }

        }
    }

    override fun addDamage(player: Player, damage: Int) {
        if (_playerOne!!.player == null || _playerTwo!!.player == null)
            return
        if (player == _playerOne!!.player)
            _damageP1 += damage
        else if (player == _playerTwo!!.player)
            _damageP2 += damage
    }

    public override fun checkDefaulted(): Boolean {
        var reason: SystemMessage?
        _playerOne!!.updatePlayer()
        _playerTwo!!.updatePlayer()

        reason = checkDefaulted(_playerOne!!.player)
        if (reason != null) {
            _playerOne!!.defaulted = true
            if (_playerTwo!!.player != null)
                _playerTwo!!.player!!.sendPacket(reason)
        }

        reason = checkDefaulted(_playerTwo!!.player)
        if (reason != null) {
            _playerTwo!!.defaulted = true
            if (_playerOne!!.player != null)
                _playerOne!!.player!!.sendPacket(reason)
        }

        return _playerOne!!.defaulted || _playerTwo!!.defaulted
    }

    override fun resetDamage() {
        _damageP1 = 0
        _damageP2 = 0
    }

    protected fun saveResults(
        one: Participant,
        two: Participant,
        _winner: Int,
        _startTime: Long,
        _fightTime: Long,
        type: CompetitionType
    ) {
        try {
            L2DatabaseFactory.connection.use { con ->
                val statement =
                    con.prepareStatement("INSERT INTO olympiad_fights (charOneId, charTwoId, charOneClass, charTwoClass, winner, start, time, classed) values(?,?,?,?,?,?,?,?)")
                statement.setInt(1, one.objectId)
                statement.setInt(2, two.objectId)
                statement.setInt(3, one.baseClass)
                statement.setInt(4, two.baseClass)
                statement.setInt(5, _winner)
                statement.setLong(6, _startTime)
                statement.setLong(7, _fightTime)
                statement.setInt(8, if (type === CompetitionType.CLASSED) 1 else 0)
                statement.execute()
                statement.close()
            }
        } catch (e: SQLException) {
            if (_log.isLoggable(Level.SEVERE))
                _log.log(
                    Level.SEVERE,
                    "SQL exception while saving olympiad fight.",
                    e
                )
        }

    }

    companion object {
        fun createListOfParticipants(list: MutableList<Int>?): Array<Participant>? {
            if (list == null || list.isEmpty() || list.size < 2)
                return null

            var playerOneObjectId = 0
            var playerOne: Player? = null
            var playerTwo: Player? = null

            while (list.size > 1) {
                playerOneObjectId = list.removeAt(Rnd[list.size])
                playerOne = World.getPlayer(playerOneObjectId)
                if (playerOne == null || !playerOne.isOnline)
                    continue

                playerTwo = World.getPlayer(list.removeAt(Rnd[list.size]))
                if (playerTwo == null || !playerTwo.isOnline) {
                    list.add(playerOneObjectId)
                    continue
                }

                val result = mutableListOf<Participant>()
                result.add(0, Participant(playerOne, 1))
                result.add(1, Participant(playerTwo, 2))

                return result.toTypedArray()
            }
            return null
        }
    }
}