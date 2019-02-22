package com.l2kt.gameserver.model.actor

import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.commons.math.MathUtil
import com.l2kt.gameserver.data.xml.MapRegionData
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.ai.type.BoatAI
import com.l2kt.gameserver.model.actor.ai.type.CreatureAI
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.actor.stat.BoatStat
import com.l2kt.gameserver.model.actor.template.CreatureTemplate
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.model.item.kind.Weapon
import com.l2kt.gameserver.model.location.BoatLocation
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.model.location.SpawnLocation
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.*
import com.l2kt.gameserver.taskmanager.MovementTaskManager

import java.util.ArrayList

class Boat(objectId: Int, template: CreatureTemplate) : Creature(objectId, template) {
    private var _engine: Runnable? = null

    protected val _passengers: MutableList<Player> = ArrayList()

    var dockId: Int = 0
        protected set
    protected var _currentPath: Array<BoatLocation>? = null
    protected var _runState: Int = 0

    val isInDock: Boolean
        get() = dockId > 0

    val isEmpty: Boolean
        get() = _passengers.isEmpty()

    val passengers: List<Player>
        get() = _passengers

    init {

        ai = BoatAI(this)
    }

    override fun isFlying(): Boolean {
        return true
    }

    fun canBeControlled(): Boolean {
        return _engine == null
    }

    fun registerEngine(r: Runnable) {
        _engine = r
    }

    fun runEngine(delay: Int) {
        if (_engine != null)
            ThreadPool.schedule(_engine!!, delay.toLong())
    }

    fun executePath(path: Array<BoatLocation>) {
        _runState = 0
        _currentPath = path

        if (_currentPath != null && _currentPath!!.size > 0) {
            val point = _currentPath!![0]
            if (point.moveSpeed > 0)
                stat.setMoveSpeed(point.moveSpeed)
            if (point.rotationSpeed > 0)
                stat.rotationSpeed = point.rotationSpeed

            ai.setIntention(CtrlIntention.MOVE_TO, point)
            return
        }
        ai.setIntention(CtrlIntention.ACTIVE)
    }

    override fun moveToNextRoutePoint(): Boolean {
        _move = null

        if (_currentPath != null) {
            _runState++
            if (_runState < _currentPath!!.size) {
                val point = _currentPath!![_runState]
                if (!isMovementDisabled) {
                    if (point.moveSpeed == 0) {
                        teleToLocation(point, 0)
                        _currentPath = null
                    } else {
                        if (point.moveSpeed > 0)
                            stat.setMoveSpeed(point.moveSpeed)
                        if (point.rotationSpeed > 0)
                            stat.rotationSpeed = point.rotationSpeed

                        val m = Creature.MoveData()
                        m.disregardingGeodata = false
                        m.onGeodataPathIndex = -1
                        m._xDestination = point.x
                        m._yDestination = point.y
                        m._zDestination = point.z
                        m._heading = 0

                        val dx = (point.x - x).toDouble()
                        val dy = (point.y - y).toDouble()
                        val distance = Math.sqrt(dx * dx + dy * dy)
                        if (distance > 1)
                        // vertical movement heading check
                            heading = MathUtil.calculateHeadingFrom(x, y, point.x, point.y)

                        m._moveStartTime = System.currentTimeMillis()
                        _move = m

                        MovementTaskManager.add(this)
                        broadcastPacket(VehicleDeparture(this))
                        return true
                    }
                }
            } else
                _currentPath = null
        }

        runEngine(10)
        return false
    }

    override fun getStat(): BoatStat {
        return super.getStat() as BoatStat
    }

    override fun initCharStat() {
        stat = BoatStat(this)
    }

    fun setInDock(d: Int) {
        dockId = d
    }

    fun oustPlayers() {
        for (player in _passengers)
            oustPlayer(player, false, Location.DUMMY_LOC)

        _passengers.clear()
    }

    fun oustPlayer(player: Player, removeFromList: Boolean, location: Location) {
        player.boat = null

        if (removeFromList)
            removePassenger(player)

        player.setInsideZone(ZoneId.PEACE, false)
        player.sendPacket(SystemMessageId.EXIT_PEACEFUL_ZONE)

        val loc = if (location == Location.DUMMY_LOC) MapRegionData.getLocationToTeleport(
            this,
            MapRegionData.TeleportType.TOWN
        ) else location
        if (player.isOnline)
            player.teleToLocation(loc!!.x, loc.y, loc.z, 0)
        else
            player.setXYZInvisible(loc!!) // disconnects handling
    }

    fun addPassenger(player: Player?): Boolean {
        if (player == null || _passengers.contains(player))
            return false

        // already in other vehicle
        if (player.boat != null && player.boat !== this)
            return false

        _passengers.add(player)

        player.setInsideZone(ZoneId.PEACE, true)
        player.sendPacket(SystemMessageId.ENTER_PEACEFUL_ZONE)

        return true
    }

    fun removePassenger(player: Player) {
        _passengers.remove(player)
    }

    fun broadcastToPassengers(sm: L2GameServerPacket) {
        for (player in _passengers) {
            player?.sendPacket(sm)
        }
    }

    /**
     * Consume ticket(s) and teleport player from boat if no correct ticket
     * @param itemId Ticket itemId
     * @param count Ticket count
     * @param loc The location to port player in case he can't pay.
     */
    fun payForRide(itemId: Int, count: Int, loc: Location) {
        for (player in getKnownTypeInRadius(Player::class.java, 1000)) {
            if (player.isInBoat && player.boat === this) {
                if (itemId > 0) {
                    if (!player.destroyItemByItemId("Boat", itemId, count, this, false)) {
                        oustPlayer(player, true, loc)
                        player.sendPacket(SystemMessageId.NOT_CORRECT_BOAT_TICKET)
                        continue
                    }

                    if (count > 1)
                        player.sendPacket(
                            SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(
                                itemId
                            ).addItemNumber(count)
                        )
                    else
                        player.sendPacket(
                            SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED).addItemName(
                                itemId
                            )
                        )
                }
                addPassenger(player)
            }
        }
    }

    override fun updatePosition(): Boolean {
        val result = super.updatePosition()

        for (player in _passengers) {
            if (player != null && player.boat === this) {
                player.setXYZ(x, y, z)
                player.revalidateZone(false)
            }
        }
        return result
    }

    override fun teleToLocation(x: Int, y: Int, z: Int, randomOffset: Int) {
        if (isMoving)
            stopMove(null)

        setIsTeleporting(true)

        ai.setIntention(CtrlIntention.ACTIVE)

        for (player in _passengers) {
            player?.teleToLocation(x, y, z, randomOffset)
        }

        decayMe()
        setXYZ(x, y, z)

        onTeleported()
        revalidateZone(true)
    }

    override fun stopMove(loc: SpawnLocation?) {
        _move = null

        if (loc != null) {
            setXYZ(loc.x, loc.y, loc.z)
            heading = loc.heading
            revalidateZone(true)
        }

        broadcastPacket(VehicleStarted(this, 0))
        broadcastPacket(VehicleInfo(this))
    }

    override fun deleteMe() {
        _engine = null

        if (isMoving)
            stopMove(null)

        // Oust all players.
        oustPlayers()

        // Decay the vehicle.
        decayMe()

        super.deleteMe()
    }

    override fun updateAbnormalEffect() {}

    override fun getActiveWeaponInstance(): ItemInstance? {
        return null
    }

    override fun getActiveWeaponItem(): Weapon? {
        return null
    }

    override fun getSecondaryWeaponInstance(): ItemInstance? {
        return null
    }

    override fun getSecondaryWeaponItem(): Weapon? {
        return null
    }

    override fun getLevel(): Int {
        return 0
    }

    override fun isAutoAttackable(attacker: Creature): Boolean {
        return false
    }

    override fun setAI(newAI: CreatureAI) {
        if (_ai == null)
            _ai = newAI
    }

    override fun detachAI() {}

    override fun sendInfo(activeChar: Player) {
        activeChar.sendPacket(VehicleInfo(this))
    }
}