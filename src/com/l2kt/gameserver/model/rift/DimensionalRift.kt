package com.l2kt.gameserver.model.rift

import com.l2kt.Config
import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.manager.DimensionalRiftManager
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.group.Party
import com.l2kt.gameserver.network.serverpackets.Earthquake
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Future

/**
 * The main core of Dimension Rift system, which is part of Seven Signs.<br></br>
 * <br></br>
 * In order to participate, a [Player] has to pick the quest from Dimensional Gate Keeper. Killing monsters in Seven Signs reward him with Dimensional Fragments, which can be used to enter the Rifts.<br></br>
 * <br></br>
 * A [Party] of at least 2 members is required. There are 6 different Rifts based on level range, accessible by the given NPC.<br></br>
 * <br></br>
 * Once Dimensional Fragments are consumed, the Party is moved into the first [DimensionalRiftRoom]. Monsters spawn after 10sec. Party leader got the possibility to use a free-token to change of room, but only once. The Party automatically jumps of room in an interval between 480 and 600sec.
 * An earthquake effect happens 7sec before the teleport.<br></br>
 * <br></br>
 * In the Rift, one of the cell is inhabited by Anakazel, a Raid Boss. The cell can only be reached through regular teleport ; this cell can't be the first one, nor a free-token one.<br></br>
 * <br></br>
 * The Dimensional Rift collapses after 4 jumps, or if the party wiped out. Players using teleport (SoE, unstuck,...) are moved back to Dimensional Waiting Room. If the amount of Players inside the Rift is reduced to 1, then the Rift collapses. Any Party edit (add/remove party member) also produces
 * the Rift to collapse.
 */
class DimensionalRift(protected var _party: Party?, room: DimensionalRiftRoom) {
    protected val _completedRooms: MutableSet<Byte> = ConcurrentHashMap.newKeySet()
    protected val _revivedInWaitingRoom: MutableSet<Player> = ConcurrentHashMap.newKeySet()
    /**
     * @return the current [DimensionalRiftRoom].
     */
    var currentRoom: DimensionalRiftRoom? = null
        protected set

    private var _teleporterTimerTask: Future<*>? = null
    private var _spawnTimerTask: Future<*>? = null
    private var _earthQuakeTask: Future<*>? = null

    protected var _currentJumps: Byte = 0
    private var _hasJumped = false

    init {
        currentRoom = room

        room.isPartyInside = true

        _party?.dimensionalRift = this

        for (member in _party?.members ?: emptyList())
            member.teleToLocation(room.teleportLoc)

        prepareNextRoom()
    }

    /**
     * @param object : The [WorldObject] to test.
     * @return true if the WorldObject is inside this [DimensionalRift] current [DimensionalRiftRoom].
     */
    fun isInCurrentRoomZone(`object`: WorldObject): Boolean {
        return currentRoom != null && currentRoom!!.checkIfInZone(`object`.x, `object`.y, `object`.z)
    }

    /**
     * @param party : The [Party] to test.
     * @return a [List] consisting of [Player]s who are still inside a Rift.
     */
    protected fun getAvailablePlayers(party: Party?): List<Player> {
        return if (party == null) emptyList() else party.members.filter { p -> !_revivedInWaitingRoom.contains(p) }

    }

    /**
     * Prepare the next room.
     *
     *  * End all running tasks.
     *  * Generate spawn, earthquake and next teleport tasks.
     *
     */
    protected fun prepareNextRoom() {
        if (_spawnTimerTask != null) {
            _spawnTimerTask!!.cancel(false)
            _spawnTimerTask = null
        }

        if (_teleporterTimerTask != null) {
            _teleporterTimerTask!!.cancel(false)
            _teleporterTimerTask = null
        }

        if (_earthQuakeTask != null) {
            _earthQuakeTask!!.cancel(false)
            _earthQuakeTask = null
        }

        _spawnTimerTask = ThreadPool.schedule(Runnable{ currentRoom!!.spawn() }, Config.RIFT_SPAWN_DELAY.toLong())

        var jumpTime = (Rnd[Config.RIFT_AUTO_JUMPS_TIME_MIN, Config.RIFT_AUTO_JUMPS_TIME_MAX] * 1000).toLong()
        if (currentRoom!!.isBossRoom)
            jumpTime *= Config.RIFT_BOSS_ROOM_TIME_MUTIPLY.toLong()

        _earthQuakeTask = ThreadPool.schedule(Runnable{
            for (member in getAvailablePlayers(_party))
                member.sendPacket(Earthquake(member.x, member.y, member.z, 65, 9))
        }, jumpTime - 7000)

        _teleporterTimerTask = ThreadPool.schedule(Runnable{
            currentRoom!!.unspawn()

            if (_currentJumps < Config.RIFT_MAX_JUMPS && !_party!!.wipedOut()) {
                _currentJumps++

                chooseRoomAndTeleportPlayers(currentRoom!!.type, getAvailablePlayers(_party), true)

                prepareNextRoom()
            } else
                killRift()
        }, jumpTime)
    }

    /**
     * The manual teleport, consisting to [Player] using the free token on the teleporter [Npc] to freely change of room. Only one token is normally allowed.
     * @param player : The tested Player.
     * @param npc : The called Npc.
     */
    fun manualTeleport(player: Player, npc: Npc) {
        val party = player.party
        if (party == null || !party.isInDimensionalRift)
            return

        if (!party.isLeader(player)) {
            DimensionalRiftManager.showHtmlFile(player, "data/html/seven_signs/rift/NotPartyLeader.htm", npc)
            return
        }

        if (_currentJumps.toInt() == Config.RIFT_MAX_JUMPS) {
            DimensionalRiftManager.showHtmlFile(player, "data/html/seven_signs/rift/UsedAllJumps.htm", npc)
            return
        }

        if (_hasJumped) {
            DimensionalRiftManager.showHtmlFile(player, "data/html/seven_signs/rift/AlreadyTeleported.htm", npc)
            return
        }
        _hasJumped = true

        currentRoom!!.unspawn()

        chooseRoomAndTeleportPlayers(currentRoom!!.type, _party!!.members, false)

        prepareNextRoom()
    }

    /**
     * Allow a [Player] to manually exit the [DimensionalRift]. He needs to be party leader. If successful, the DimensionalRift is destroyed.
     * @param player : The tested Player.
     * @param npc : The called Npc.
     */
    fun manualExitRift(player: Player, npc: Npc) {
        val party = player.party
        if (party == null || !party.isInDimensionalRift)
            return

        if (!party.isLeader(player)) {
            DimensionalRiftManager.showHtmlFile(player, "data/html/seven_signs/rift/NotPartyLeader.htm", npc)
            return
        }

        killRift()
    }

    /**
     * This method allows to jump from one room to another.
     * @param type : The type of [DimensionalRift] to handle.
     * @param players : The List of [Player]s to teleport.
     * @param canUseBossRoom : if false, Anakazel room can't be choosen (case of manual teleport).
     */
    protected fun chooseRoomAndTeleportPlayers(type: Byte, players: List<Player>, canUseBossRoom: Boolean) {
        // Set the current room id as used.
        _completedRooms.add(currentRoom!!.id)

        // Compute free rooms for the Party.
        val list = DimensionalRiftManager.getFreeRooms(type, canUseBossRoom)
            .filter { r -> !_completedRooms.contains(r.id) }

        // If no rooms are found, simply break the Party Rift.
        if (list.isEmpty()) {
            killRift()
            return
        }

        // List is filled ; return a random Room and set it as filled.
        currentRoom = Rnd[list]
        currentRoom!!.isPartyInside = true

        // Teleport all Players in.
        for (member in players)
            member.teleToLocation(currentRoom!!.teleportLoc)
    }

    /**
     * Cleanup this instance of [DimensionalRift].
     *
     *  * Teleport all [Player]s to waiting room.
     *  * Clean all [List]s.
     *  * Stop all running tasks.
     *  * Unspawn minions and set the room as unused.
     *
     */
    fun killRift() {
        if (_party != null) {
            for (member in getAvailablePlayers(_party))
                DimensionalRiftManager.teleportToWaitingRoom(member)

            _party!!.dimensionalRift = null
            _party = null
        }

        _completedRooms.clear()
        _revivedInWaitingRoom.clear()

        if (_earthQuakeTask != null) {
            _earthQuakeTask!!.cancel(false)
            _earthQuakeTask = null
        }

        if (_teleporterTimerTask != null) {
            _teleporterTimerTask!!.cancel(false)
            _teleporterTimerTask = null
        }

        if (_spawnTimerTask != null) {
            _spawnTimerTask!!.cancel(false)
            _spawnTimerTask = null
        }

        currentRoom!!.unspawn()
        currentRoom = null
    }

    /**
     * On [Player] teleport (SoE, unstuck,...), put the Player on revived [List]. If the minimum required party members amount isn't reached, we kill this [DimensionalRift] instance.
     * @param player : The Player to test.
     */
    fun usedTeleport(player: Player) {
        _revivedInWaitingRoom.add(player)

        if (_party!!.membersCount - _revivedInWaitingRoom.size < Config.RIFT_MIN_PARTY_SIZE)
            killRift()
    }
}