package com.l2kt.gameserver.model

import com.l2kt.Config
import com.l2kt.commons.logging.CLogger
import com.l2kt.commons.math.MathUtil
import com.l2kt.gameserver.data.ItemTable
import com.l2kt.gameserver.data.xml.NpcData
import com.l2kt.gameserver.idfactory.IdFactory
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.model.location.SpawnLocation
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.taskmanager.DebugMovementTaskManager
import java.util.*

/**
 * Mother class of all interactive objects in the world (PC, NPC, Item...)
 */
abstract class WorldObject(objectId: Int) {

    open var name: String = ""
    var objectId: Int = 0
        private set

    var polyTemplate: NpcTemplate? = null
        private set
    var polyType = PolyType.DEFAULT
        private set
    var polyId: Int = 0
        private set

    val position = SpawnLocation(0, 0, 0, 0)

    var region: WorldRegion? = null
        private set

    /**
     * Update current and surrounding [WorldRegion]s, based on both current region and region setted as parameter.
     * @param newRegion : null to remove the [WorldObject], or the new region.
     */
    // For every old surrounding area NOT SHARED with new surrounding areas.
    // Update all objects.
    // Desactivate the old neighbor region.
    // For every new surrounding area NOT SHARED with old surrounding areas.
    // Update all objects.
    // Activate the new neighbor region.
    open fun setRegion(newRegion: WorldRegion?){
        var oldAreas = emptyList<WorldRegion>()

        if (region != null) {
            region!!.removeVisibleObject(this)
            oldAreas = region!!.surroundingRegions
        }

        var newAreas = emptyList<WorldRegion>()

        if (newRegion != null) {
            newRegion.addVisibleObject(this)
            newAreas = newRegion.surroundingRegions
        }
        for (region in oldAreas) {
            if (!newAreas.contains(region)) {
                for (obj in region.objects) {
                    if (obj === this)
                        continue

                    obj.removeKnownObject(this)
                    removeKnownObject(obj)
                }
                if (this is Player && region.isEmptyNeighborhood)
                    region.isActive = false
            }
        }
        for (region in newAreas) {
            if (!oldAreas.contains(region)) {
                for (obj in region.objects) {
                    if (obj === this)
                        continue

                    obj.addKnownObject(this)
                    addKnownObject(obj)
                }
                if (this is Player)
                    region.isActive = true
            }
        }

        region = newRegion
    }

    private var _isVisible: Boolean = false

    open val isAttackable: Boolean
        get() = false

    /**
     * @return the visibilty state of this [WorldObject].
     */
    var isVisible: Boolean
        get() = region != null && _isVisible
        set(value) {
            _isVisible = value

            if (!_isVisible)
                setRegion(null)
        }

    open val actingPlayer: Player?
        get() = null

    val x: Int
        get() = position.x

    val y: Int
        get() = position.y

    val z: Int
        get() = position.z

    enum class PolyType {
        ITEM,
        NPC,
        DEFAULT
    }

    init {
        this.objectId = objectId
    }

    /**
     * @param attacker : The target to make checks on.
     * @return true if this [WorldObject] is attackable or false if it isn't.
     */
    abstract fun isAutoAttackable(attacker: Creature): Boolean

    override fun toString(): String {
        return javaClass.simpleName + ":" + name + "[" + objectId + "]"
    }

    open fun onAction(player: Player) {
        player.sendPacket(ActionFailed.STATIC_PACKET)
    }

    open fun onActionShift(player: Player) {
        player.sendPacket(ActionFailed.STATIC_PACKET)
    }

    open fun onForcedAttack(player: Player) {
        player.sendPacket(ActionFailed.STATIC_PACKET)
    }

    open fun onSpawn() {}

    /**
     * Remove this [WorldObject] from the world.
     */
    open fun decayMe() {
        setRegion(null)

        World.removeObject(this)
    }

    fun refreshID() {
        World.removeObject(this)
        IdFactory.getInstance().releaseId(objectId)
        objectId = IdFactory.getInstance().nextId
    }

    /**
     * Spawn this [WorldObject] and add it in the world as a visible object.
     */
    fun spawnMe() {
        _isVisible = true

        setRegion(World.getRegion(position))

        World.addObject(this)

        onSpawn()
    }

    /**
     * Initialize the position of this [WorldObject] and add it in the world as a visible object.
     * @param loc : The location used as reference X/Y/Z.
     */
    fun spawnMe(loc: Location) {
        spawnMe(loc.x, loc.y, loc.z)
    }

    /**
     * Initialize the position of this [WorldObject] and add it in the world as a visible object.
     * @param x : The X position to set.
     * @param y : The Y position to set.
     * @param z : The Z position to set.
     */
    fun spawnMe(x: Int, y: Int, z: Int) {
        position.set(
            MathUtil.limit(x, World.WORLD_X_MIN + 100, World.WORLD_X_MAX - 100),
            MathUtil.limit(y, World.WORLD_Y_MIN + 100, World.WORLD_Y_MAX - 100),
            z
        )

        spawnMe()
    }

    open fun polymorph(type: PolyType, id: Int): Boolean {
        if (this !is Npc && this !is Player)
            return false

        if (type == PolyType.NPC) {
            val template = NpcData.getTemplate(id) ?: return false

            polyTemplate = template
        } else if (type == PolyType.ITEM) {
            if (ItemTable.getTemplate(id) == null)
                return false
        } else if (type == PolyType.DEFAULT)
            return false

        polyType = type
        polyId = id

        decayMe()
        spawnMe()

        return true
    }

    open fun unpolymorph() {
        polyTemplate = null
        polyType = PolyType.DEFAULT
        polyId = 0

        decayMe()
        spawnMe()
    }

    /**
     * Sends the Server->Client info packet for this [WorldObject].
     * @param player : The packet receiver.
     */
    open fun sendInfo(player: Player) {

    }

    /**
     * Check if this [WorldObject] has charged shot.
     * @param type : The type of the shot to be checked.
     * @return true if the object has charged shot.
     */
    open fun isChargedShot(type: ShotType): Boolean {
        return false
    }

    /**
     * Charging shot into this [WorldObject].
     * @param type : The type of the shot to be (un)charged.
     * @param charged : true if we charge, false if we uncharge.
     */
    open fun setChargedShot(type: ShotType, charged: Boolean) {}

    /**
     * Try to recharge a shot.
     * @param physical : The skill is using Soulshots.
     * @param magical : The skill is using Spiritshots.
     */
    open fun rechargeShots(physical: Boolean, magical: Boolean) {}

    /**
     * Check if this [WorldObject] is in the given [ZoneId].
     * @param zone : The ZoneId to check.
     * @return true if the object is in that ZoneId.
     */
    open fun isInsideZone(zone: ZoneId): Boolean {
        return false
    }

    /**
     * Set the position of this [WorldObject] and if necessary modify its _region.
     * @param x : The X position to set.
     * @param y : The Y position to set.
     * @param z : The Z position to set.
     */
    fun setXYZ(x: Int, y: Int, z: Int) {
        position.set(x, y, z)

        if (Config.DEBUG_MOVEMENT > 0)
            DebugMovementTaskManager.addItem(this, x, y, z)

        if (!isVisible)
            return

        val region = World.getRegion(position)
        if (region != this.region)
            setRegion(region)
    }

    /**
     * Set the position of this [WorldObject] and make it invisible.
     * @param x : The X position to set.
     * @param y : The Y position to set.
     * @param z : The Z position to set.
     */
    fun setXYZInvisible(x: Int, y: Int, z: Int) {
        position.set(
            MathUtil.limit(x, World.WORLD_X_MIN + 100, World.WORLD_X_MAX - 100),
            MathUtil.limit(y, World.WORLD_Y_MIN + 100, World.WORLD_Y_MAX - 100),
            z
        )

        isVisible = false
    }

    fun setXYZInvisible(loc: Location) {
        setXYZInvisible(loc.x, loc.y, loc.z)
    }

    /**
     * Add a [WorldObject] to knownlist.
     * @param object : An object to be added.
     */
    open fun addKnownObject(`object`: WorldObject) {}

    /**
     * Remove a [WorldObject] from knownlist.
     * @param object : An object to be removed.
     */
    open fun removeKnownObject(`object`: WorldObject) {}

    /**
     * Return the knownlist of this [WorldObject] for a given object type.
     * @param <A> : The object type must be an instance of WorldObject.
     * @param type : The class specifying object type.
     * @return List<A> : The knownlist of given object type.
    </A></A> */
    fun <A> getKnownType(type: Class<A>): List<A> {
        val region = this.region ?: return emptyList()

        val result = ArrayList<A>()

        for (reg in region.surroundingRegions) {
            for (obj in reg.objects) {
                if (obj === this || !type.isAssignableFrom(obj.javaClass))
                    continue

                result.add(obj as A)
            }
        }

        return result
    }

    /**
     * Return the knownlist of this [WorldObject] for a given object type within specified radius.
     * @param <A> : The object type must be an instance of WorldObject.
     * @param type : The class specifying object type.
     * @param radius : The radius to check in which object must be located.
     * @return List<A> : The knownlist of given object type.
    </A></A> */
    fun <A> getKnownTypeInRadius(type: Class<A>, radius: Int): List<A> {
        val region = this.region ?: return emptyList()

        val result = ArrayList<A>()

        for (reg in region.surroundingRegions) {
            for (obj in reg.objects) {
                if (obj === this || !type.isAssignableFrom(obj.javaClass) || !MathUtil.checkIfInRange(
                        radius,
                        this,
                        obj,
                        true
                    )
                )
                    continue

                result.add(obj as A)
            }
        }

        return result
    }

    companion object {

        @JvmField val LOGGER = CLogger(WorldObject::class.java.name)
    }
}