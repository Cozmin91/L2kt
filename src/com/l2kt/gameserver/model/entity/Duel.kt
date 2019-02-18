package com.l2kt.gameserver.model.entity

import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.gameserver.data.manager.DuelManager
import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.*
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.Future

class Duel(
    /**
     * @return the player that requested the duel.
     */
    val playerA: Player,
    /**
     * @return the player that was challenged.
     */
    val playerB: Player,
    /**
     * @return true if the duel was a party duel, false otherwise.
     */
    val isPartyDuel: Boolean,
    /**
     * @return the duel id.
     */
    val id: Int
) {
    private val _duelEndTime: Calendar = Calendar.getInstance()
    private val _playerConditions = CopyOnWriteArrayList<PlayerCondition>()

    private var _surrenderRequest: Int = 0

    protected var _startTask: Future<*>? = null
    protected var _checkTask: Future<*>? = null
    protected var _countdown = 5

    /**
     * @return the remaining time.
     */
    val remainingTime: Int
        get() = (_duelEndTime.timeInMillis - Calendar.getInstance().timeInMillis).toInt()

    enum class DuelState {
        NO_DUEL,
        ON_COUNTDOWN,
        DUELLING,
        DEAD,
        WINNER,
        INTERRUPTED
    }

    protected enum class DuelResult {
        CONTINUE,
        TEAM_1_WIN,
        TEAM_2_WIN,
        TEAM_1_SURRENDER,
        TEAM_2_SURRENDER,
        CANCELED,
        TIMEOUT
    }

    init {

        _duelEndTime.add(Calendar.SECOND, 120)

        if (this.isPartyDuel) {
            _countdown = 35

            // Inform players that they will be ported shortly.
            val sm =
                SystemMessage.getSystemMessage(SystemMessageId.IN_A_MOMENT_YOU_WILL_BE_TRANSPORTED_TO_THE_SITE_WHERE_THE_DUEL_WILL_TAKE_PLACE)
            broadcastToTeam1(sm)
            broadcastToTeam2(sm)

            for (partyPlayer in this.playerA.party!!.members)
                partyPlayer.setInDuel(id)

            for (partyPlayer in this.playerB.party!!.members)
                partyPlayer.setInDuel(id)
        } else {
            // Set states.
            this.playerA.setInDuel(id)
            this.playerB.setInDuel(id)
        }

        savePlayerConditions()

        // Start task, used for countdowns and startDuel method call. Can be shutdowned if the check task commands it.
        _startTask = ThreadPool.scheduleAtFixedRate(StartTask(), 1000, 1000)

        // Check task, used to verify if duel is disturbed.
        _checkTask = ThreadPool.scheduleAtFixedRate(CheckTask(), 1000, 1000)
    }

    /**
     * This class hold important player informations, which will be restored on duel end.
     */
    private class PlayerCondition(val player: Player?, partyDuel: Boolean) {

        private var _hp: Double = 0.0
        private var _mp: Double = 0.0
        private var _cp: Double = 0.0

        private var _x: Int = 0
        private var _y: Int = 0
        private var _z: Int = 0

        private var _debuffs: MutableList<L2Effect>? = null

        init {
            if (player != null){
                _hp = this.player.currentHp
                _mp = this.player.currentMp
                _cp = this.player.currentCp

                if (partyDuel) {
                    _x = this.player.x
                    _y = this.player.y
                    _z = this.player.z
                }
            }
        }

        fun restoreCondition(abnormalEnd: Boolean) {
            teleportBack()

            if (abnormalEnd)
                return

            player?.currentHp = _hp
            player?.currentMp = _mp
            player?.currentCp = _cp

            if (_debuffs != null) {
                for (skill in _debuffs!!)
                    skill?.exit()
            }
        }

        fun registerDebuff(debuff: L2Effect) {
            if (_debuffs == null)
                _debuffs = CopyOnWriteArrayList()

            _debuffs!!.add(debuff)
        }

        fun teleportBack() {
            if (_x != 0 && _y != 0)
                player?.teleToLocation(_x, _y, _z, 0)
        }
    }

    /**
     * This task makes the countdown, both for party and 1vs1 cases.
     *
     *  * For 1vs1, the timer begins to 5 (messages then start duel process).
     *  * For party duel, the timer begins to 35 (2sec break, teleport parties, 3sec break, messages then start duel process).
     *
     * The task is running until countdown reaches -1 (0 being startDuel).
     */
    private inner class StartTask : Runnable {

        override fun run() {
            // Schedule anew, until time reaches 0.
            if (_countdown < 0) {
                _startTask!!.cancel(true)
                _startTask = null
            }

            when (_countdown) {
                33 -> teleportPlayers(-83760, -238825, -3331)

                30, 20, 15, 10, 5, 4, 3, 2, 1 -> {
                    val sm = SystemMessage.getSystemMessage(SystemMessageId.THE_DUEL_WILL_BEGIN_IN_S1_SECONDS).addNumber(_countdown)
                    broadcastToTeam1(sm)
                    broadcastToTeam2(sm)
                }

                0 -> {
                    val sm = SystemMessage.getSystemMessage(SystemMessageId.LET_THE_DUEL_BEGIN)
                    broadcastToTeam1(sm)
                    broadcastToTeam2(sm)

                    startDuel()
                }
            }

            // Decrease timer.
            _countdown--
        }
    }

    /**
     * This task listens the different ways to disturb the duel. Two cases are possible :
     *
     *  * DuelResult is under CONTINUE state, nothing happens. The task will continue to run every second.
     *  * DuelResult is anything except CONTINUE, then the duel ends. Animations are played on any duel end cases, except CANCELED.
     *
     */
    private inner class CheckTask : Runnable {

        override fun run() {
            val status = checkEndDuelCondition()

            if (status != DuelResult.CONTINUE) {
                // Abort the start task if it was currently running. Interrupt it, even if it was on a loop.
                if (_startTask != null) {
                    _startTask!!.cancel(true)
                    _startTask = null
                }

                // Abort the check task. Let this last loop alive.
                if (_checkTask != null) {
                    _checkTask!!.cancel(false)
                    _checkTask = null
                }

                stopFighting()

                if (status != DuelResult.CANCELED)
                    playAnimations()

                endDuel(status)
            }
        }
    }

    /**
     * Stops all players from attacking. Used for duel timeout / interrupt.
     */
    protected fun stopFighting() {
        if (isPartyDuel) {
            for (partyPlayer in playerA.party!!.members) {
                partyPlayer.abortCast()
                partyPlayer.ai.setIntention(CtrlIntention.ACTIVE)
                partyPlayer.target = null
                partyPlayer.sendPacket(ActionFailed.STATIC_PACKET)
            }

            for (partyPlayer in playerB.party!!.members) {
                partyPlayer.abortCast()
                partyPlayer.ai.setIntention(CtrlIntention.ACTIVE)
                partyPlayer.target = null
                partyPlayer.sendPacket(ActionFailed.STATIC_PACKET)
            }
        } else {
            playerA.abortCast()
            playerB.abortCast()
            playerA.ai.setIntention(CtrlIntention.ACTIVE)
            playerA.target = null
            playerB.ai.setIntention(CtrlIntention.ACTIVE)
            playerB.target = null
            playerA.sendPacket(ActionFailed.STATIC_PACKET)
            playerB.sendPacket(ActionFailed.STATIC_PACKET)
        }
    }

    /**
     * Starts the duel.<br></br>
     * Save players conditions, cancel active trade, set the team color and all duel start packets.<br></br>
     * Handle the duel task, which checks if the duel ends in one way or another.
     */
    protected fun startDuel() {
        if (isPartyDuel) {
            for (partyPlayer in playerA.party!!.members) {
                partyPlayer.cancelActiveTrade()
                partyPlayer.duelState = DuelState.DUELLING
                partyPlayer.team = 1
                partyPlayer.broadcastUserInfo()

                val summon = partyPlayer.pet
                summon?.updateAbnormalEffect()

                broadcastToTeam2(ExDuelUpdateUserInfo(partyPlayer))
            }

            for (partyPlayer in playerB.party!!.members) {
                partyPlayer.cancelActiveTrade()
                partyPlayer.duelState = DuelState.DUELLING
                partyPlayer.team = 2
                partyPlayer.broadcastUserInfo()

                val summon = partyPlayer.pet
                summon?.updateAbnormalEffect()

                broadcastToTeam1(ExDuelUpdateUserInfo(partyPlayer))
            }

            // Send duel Start packets.
            val ready = ExDuelReady(1)
            val start = ExDuelStart(1)

            broadcastToTeam1(ready)
            broadcastToTeam2(ready)
            broadcastToTeam1(start)
            broadcastToTeam2(start)
        } else {
            // Set states.
            playerA.duelState = DuelState.DUELLING
            playerA.team = 1
            playerB.duelState = DuelState.DUELLING
            playerB.team = 2

            // Send duel Start packets.
            val ready = ExDuelReady(0)
            val start = ExDuelStart(0)

            broadcastToTeam1(ready)
            broadcastToTeam2(ready)
            broadcastToTeam1(start)
            broadcastToTeam2(start)

            broadcastToTeam1(ExDuelUpdateUserInfo(playerB))
            broadcastToTeam2(ExDuelUpdateUserInfo(playerA))

            playerA.broadcastUserInfo()

            var summon = playerA.pet
            summon?.updateAbnormalEffect()

            playerB.broadcastUserInfo()

            summon = playerB.pet
            summon?.updateAbnormalEffect()
        }

        // Play sound.
        broadcastToTeam1(B04_S01)
        broadcastToTeam2(B04_S01)
    }

    /**
     * Save the current player condition: hp, mp, cp, location
     */
    private fun savePlayerConditions() {
        if (isPartyDuel) {
            for (partyPlayer in playerA.party!!.members)
                _playerConditions.add(PlayerCondition(partyPlayer, isPartyDuel))

            for (partyPlayer in playerB.party!!.members)
                _playerConditions.add(PlayerCondition(partyPlayer, isPartyDuel))
        } else {
            _playerConditions.add(PlayerCondition(playerA, isPartyDuel))
            _playerConditions.add(PlayerCondition(playerB, isPartyDuel))
        }
    }

    /**
     * Restore player conditions.
     * @param abnormalEnd : true if the duel was canceled.
     */
    private fun restorePlayerConditions(abnormalEnd: Boolean) {
        if (isPartyDuel) {
            for (partyPlayer in playerA.party!!.members) {
                partyPlayer.setInDuel(0)
                partyPlayer.team = 0
                partyPlayer.broadcastUserInfo()

                val summon = partyPlayer.pet
                summon?.updateAbnormalEffect()
            }

            for (partyPlayer in playerB.party!!.members) {
                partyPlayer.setInDuel(0)
                partyPlayer.team = 0
                partyPlayer.broadcastUserInfo()

                val summon = partyPlayer.pet
                summon?.updateAbnormalEffect()
            }
        } else {
            playerA.setInDuel(0)
            playerA.team = 0
            playerA.broadcastUserInfo()

            var summon = playerA.pet
            summon?.updateAbnormalEffect()

            playerB.setInDuel(0)
            playerB.team = 0
            playerB.broadcastUserInfo()

            summon = playerB.pet
            summon?.updateAbnormalEffect()
        }

        // Restore player conditions, but only for party duel (no matter the end) && 1vs1 which ends normally.
        if (!isPartyDuel && !abnormalEnd || isPartyDuel) {
            for (cond in _playerConditions)
                cond.restoreCondition(abnormalEnd)
        }
    }

    /**
     * Teleport all players to the given coordinates. Used by party duel only.
     * @param x
     * @param y
     * @param z
     */
    protected fun teleportPlayers(x: Int, y: Int, z: Int) {
        // TODO: adjust the values if needed... or implement something better (especially using more then 1 arena)
        if (!isPartyDuel)
            return

        var offset = 0

        for (partyPlayer in playerA.party!!.members) {
            partyPlayer.teleToLocation(x + offset - 180, y - 150, z, 0)
            offset += 40
        }

        offset = 0
        for (partyPlayer in playerB.party!!.members) {
            partyPlayer.teleToLocation(x + offset - 180, y + 150, z, 0)
            offset += 40
        }
    }

    /**
     * Broadcast a packet to the challenger team.
     * @param packet : The packet to send.
     */
    fun broadcastToTeam1(packet: L2GameServerPacket) {
        if (isPartyDuel && playerA.party != null) {
            for (partyPlayer in playerA.party!!.members)
                partyPlayer.sendPacket(packet)
        } else
            playerA.sendPacket(packet)
    }

    /**
     * Broadcast a packet to the challenged team.
     * @param packet : The packet to send.
     */
    fun broadcastToTeam2(packet: L2GameServerPacket) {
        if (isPartyDuel && playerB.party != null) {
            for (partyPlayer in playerB.party!!.members)
                partyPlayer.sendPacket(packet)
        } else
            playerB.sendPacket(packet)
    }

    /**
     * Playback the bow animation for loosers, victory pose for winners.<br></br>
     * The method works even if other side is null or offline.
     */
    protected fun playAnimations() {
        if (playerA.isOnline) {
            if (playerA.duelState == DuelState.WINNER) {
                if (isPartyDuel && playerA.party != null) {
                    for (partyPlayer in playerA.party!!.members)
                        partyPlayer.broadcastPacket(SocialAction(partyPlayer, 3))
                } else
                    playerA.broadcastPacket(SocialAction(playerA, 3))
            } else if (playerA.duelState == DuelState.DEAD) {
                if (isPartyDuel && playerA.party != null) {
                    for (partyPlayer in playerA.party!!.members)
                        partyPlayer.broadcastPacket(SocialAction(partyPlayer, 7))
                } else
                    playerA.broadcastPacket(SocialAction(playerA, 7))
            }
        }

        if (playerB.isOnline) {
            if (playerB.duelState == DuelState.WINNER) {
                if (isPartyDuel && playerB.party != null) {
                    for (partyPlayer in playerB.party!!.members)
                        partyPlayer.broadcastPacket(SocialAction(partyPlayer, 3))
                } else
                    playerB.broadcastPacket(SocialAction(playerB, 3))
            } else if (playerB.duelState == DuelState.DEAD) {
                if (isPartyDuel && playerB.party != null) {
                    for (partyPlayer in playerB.party!!.members)
                        partyPlayer.broadcastPacket(SocialAction(partyPlayer, 7))
                } else
                    playerB.broadcastPacket(SocialAction(playerB, 7))
            }
        }
    }

    /**
     * This method ends a duel, sending messages to each team, end duel packet, cleaning player conditions and then removing duel from manager.
     * @param result : The duel result.
     */
    protected fun endDuel(result: DuelResult) {
        var sm: SystemMessage? = null
        when (result) {
            Duel.DuelResult.TEAM_2_SURRENDER -> {
                sm =
                    SystemMessage.getSystemMessage(if (isPartyDuel) SystemMessageId.SINCE_S1_PARTY_WITHDREW_FROM_THE_DUEL_S2_PARTY_HAS_WON else SystemMessageId.SINCE_S1_WITHDREW_FROM_THE_DUEL_S2_HAS_WON)
                        .addString(playerB.name).addString(playerA.name)
                broadcastToTeam1(sm)
                broadcastToTeam2(sm)
                sm =
                    SystemMessage.getSystemMessage(if (isPartyDuel) SystemMessageId.S1_PARTY_HAS_WON_THE_DUEL else SystemMessageId.S1_HAS_WON_THE_DUEL)
                        .addString(playerA.name)
            }
            Duel.DuelResult.TEAM_1_WIN -> sm =
                SystemMessage.getSystemMessage(if (isPartyDuel) SystemMessageId.S1_PARTY_HAS_WON_THE_DUEL else SystemMessageId.S1_HAS_WON_THE_DUEL)
                    .addString(playerA.name)

            Duel.DuelResult.TEAM_1_SURRENDER -> {
                sm =
                    SystemMessage.getSystemMessage(if (isPartyDuel) SystemMessageId.SINCE_S1_PARTY_WITHDREW_FROM_THE_DUEL_S2_PARTY_HAS_WON else SystemMessageId.SINCE_S1_WITHDREW_FROM_THE_DUEL_S2_HAS_WON)
                        .addString(playerA.name).addString(playerB.name)
                broadcastToTeam1(sm)
                broadcastToTeam2(sm)
                sm =
                    SystemMessage.getSystemMessage(if (isPartyDuel) SystemMessageId.S1_PARTY_HAS_WON_THE_DUEL else SystemMessageId.S1_HAS_WON_THE_DUEL)
                        .addString(playerB.name)
            }
            Duel.DuelResult.TEAM_2_WIN -> sm =
                SystemMessage.getSystemMessage(if (isPartyDuel) SystemMessageId.S1_PARTY_HAS_WON_THE_DUEL else SystemMessageId.S1_HAS_WON_THE_DUEL)
                    .addString(playerB.name)

            Duel.DuelResult.CANCELED, Duel.DuelResult.TIMEOUT -> sm =
                SystemMessage.getSystemMessage(SystemMessageId.THE_DUEL_HAS_ENDED_IN_A_TIE)
        }

        sm?.let { broadcastToTeam1(it) }
        sm?.let { broadcastToTeam2(it) }

        restorePlayerConditions(result == DuelResult.CANCELED)

        // Send end duel packet.
        val duelEnd = ExDuelEnd(if (isPartyDuel) 1 else 0)

        broadcastToTeam1(duelEnd)
        broadcastToTeam2(duelEnd)

        // Cleanup.
        _playerConditions.clear()
        DuelManager.removeDuel(id)
    }

    /**
     * This method checks all possible scenari which can disturb a duel, and return the appropriate status.
     * @return DuelResult : The duel status.
     */
    protected fun checkEndDuelCondition(): DuelResult {
        // Both players are offline.
        if (!playerA.isOnline && !playerB.isOnline)
            return DuelResult.CANCELED

        // Player A is offline.
        if (!playerA.isOnline) {
            onPlayerDefeat(playerA)
            return DuelResult.TEAM_1_SURRENDER
        }

        // Player B is offline.
        if (!playerB.isOnline) {
            onPlayerDefeat(playerB)
            return DuelResult.TEAM_2_SURRENDER
        }

        // Duel surrender request.
        if (_surrenderRequest != 0)
            return if (_surrenderRequest == 1) DuelResult.TEAM_1_SURRENDER else DuelResult.TEAM_2_SURRENDER

        // Duel timed out.
        if (remainingTime <= 0)
            return DuelResult.TIMEOUT

        // One of the players is declared winner.
        if (playerA.duelState == DuelState.WINNER)
            return DuelResult.TEAM_1_WIN

        if (playerB.duelState == DuelState.WINNER)
            return DuelResult.TEAM_2_WIN

        if (!isPartyDuel) {
            // Duel was interrupted e.g.: player was attacked by mobs / other players
            if (playerA.duelState == DuelState.INTERRUPTED || playerB.duelState == DuelState.INTERRUPTED)
                return DuelResult.CANCELED

            // Players are too far apart.
            if (!playerA.isInsideRadius(playerB, 2000, false, false))
                return DuelResult.CANCELED

            // One of the players is engaged in PvP.
            if (playerA.pvpFlag.toInt() != 0 || playerB.pvpFlag.toInt() != 0)
                return DuelResult.CANCELED

            // One of the players is in a Siege, Peace or PvP zone.
            if (playerA.isInsideZone(ZoneId.PEACE) || playerB.isInsideZone(ZoneId.PEACE) || playerA.isInsideZone(ZoneId.SIEGE) || playerB.isInsideZone(
                    ZoneId.SIEGE
                ) || playerA.isInsideZone(ZoneId.PVP) || playerB.isInsideZone(ZoneId.PVP)
            )
                return DuelResult.CANCELED
        } else {
            if (playerA.party != null) {
                for (partyMember in playerA.party!!.members) {
                    // Duel was interrupted e.g.: player was attacked by mobs / other players
                    if (partyMember.duelState == DuelState.INTERRUPTED)
                        return DuelResult.CANCELED

                    // Players are too far apart.
                    if (!partyMember.isInsideRadius(playerB, 2000, false, false))
                        return DuelResult.CANCELED

                    // One of the players is engaged in PvP.
                    if (partyMember.pvpFlag.toInt() != 0)
                        return DuelResult.CANCELED

                    // One of the players is in a Siege, Peace or PvP zone.
                    if (partyMember.isInsideZone(ZoneId.PEACE) || partyMember.isInsideZone(ZoneId.PEACE) || partyMember.isInsideZone(
                            ZoneId.SIEGE
                        )
                    )
                        return DuelResult.CANCELED
                }
            }

            if (playerB.party != null) {
                for (partyMember in playerB.party!!.members) {
                    // Duel was interrupted e.g.: player was attacked by mobs / other players
                    if (partyMember.duelState == DuelState.INTERRUPTED)
                        return DuelResult.CANCELED

                    // Players are too far apart.
                    if (!partyMember.isInsideRadius(playerA, 2000, false, false))
                        return DuelResult.CANCELED

                    // One of the players is engaged in PvP.
                    if (partyMember.pvpFlag.toInt() != 0)
                        return DuelResult.CANCELED

                    // One of the players is in a Siege, Peace or PvP zone.
                    if (partyMember.isInsideZone(ZoneId.PEACE) || partyMember.isInsideZone(ZoneId.PEACE) || partyMember.isInsideZone(
                            ZoneId.SIEGE
                        )
                    )
                        return DuelResult.CANCELED
                }
            }
        }

        return DuelResult.CONTINUE
    }

    /**
     * Register a surrender request. It updates DuelState of players.
     * @param player : The player who surrenders.
     */
    fun doSurrender(player: Player) {
        // A surrender request is already under process, return.
        if (_surrenderRequest != 0)
            return

        // TODO: Can every party member cancel a party duel? or only the party leaders?
        if (isPartyDuel) {
            if (playerA.party!!.containsPlayer(player)) {
                _surrenderRequest = 1

                for (partyPlayer in playerA.party!!.members)
                    partyPlayer.duelState = DuelState.DEAD

                for (partyPlayer in playerB.party!!.members)
                    partyPlayer.duelState = DuelState.WINNER
            } else if (playerB.party!!.containsPlayer(player)) {
                _surrenderRequest = 2

                for (partyPlayer in playerB.party!!.members)
                    partyPlayer.duelState = DuelState.DEAD

                for (partyPlayer in playerA.party!!.members)
                    partyPlayer.duelState = DuelState.WINNER
            }
        } else {
            if (player == playerA) {
                _surrenderRequest = 1

                playerA.duelState = DuelState.DEAD
                playerB.duelState = DuelState.WINNER
            } else if (player == playerB) {
                _surrenderRequest = 2

                playerB.duelState = DuelState.DEAD
                playerA.duelState = DuelState.WINNER
            }
        }
    }

    /**
     * This method is called whenever a player was defeated in a duel. It updates DuelState of players.
     * @param player : The defeated player.
     */
    fun onPlayerDefeat(player: Player) {
        // Set player as defeated.
        player.duelState = DuelState.DEAD

        if (isPartyDuel) {
            var teamDefeated = true
            for (partyPlayer in player.party!!.members) {
                if (partyPlayer.duelState == DuelState.DUELLING) {
                    teamDefeated = false
                    break
                }
            }

            if (teamDefeated) {
                var winner = playerA
                if (playerA.party!!.containsPlayer(player))
                    winner = playerB

                for (partyPlayer in winner.party!!.members)
                    partyPlayer.duelState = DuelState.WINNER
            }
        } else {
            if (playerA == player)
                playerB.duelState = DuelState.WINNER
            else
                playerA.duelState = DuelState.WINNER
        }
    }

    /**
     * This method is called when a player join/leave a party during a Duel, and enforce Duel cancellation.
     */
    fun onPartyEdit() {
        if (!isPartyDuel)
            return

        // Teleport back players, setting their duelId to 0.
        for (cond in _playerConditions) {
            cond.teleportBack()
            cond.player?.setInDuel(0)
        }

        // Cancel the duel properly.
        endDuel(DuelResult.CANCELED)
    }

    /**
     * This method is called to register an effect.
     * @param player : The player condition to affect.
     * @param effect : The effect to register.
     */
    fun onBuff(player: Player, effect: L2Effect) {
        for (cond in _playerConditions) {
            if (cond.player == player) {
                cond.registerDebuff(effect)
                return
            }
        }
    }

    companion object {
        private val B04_S01 = PlaySound(1, "B04_S01")
    }
}