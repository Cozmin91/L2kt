package com.l2kt.gameserver.model

import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.model.zone.ZoneType
import com.l2kt.gameserver.model.zone.type.DerbyTrackZone
import com.l2kt.gameserver.model.zone.type.PeaceZone
import com.l2kt.gameserver.model.zone.type.TownZone
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

class WorldRegion(private val _tileX: Int, private val _tileY: Int) {
    private val _objects = ConcurrentHashMap<Int, WorldObject>()

    val surroundingRegions = mutableListOf<WorldRegion>()
    val zones = mutableListOf<ZoneType>()

    /**
     * This function turns this region's AI on or off.
     * @param value : if true, activate hp/mp regen and random animation. If false, clean aggro/attack list, set objects on IDLE and drop their AI tasks.
     */
    // Set target to null and cancel Attack or Cast
    // Stop movement
    // Stop all active skills effects in progress on the Creature
    // stop the ai tasks
    var isActive: Boolean = false
        set(value) {
            if (isActive == value)
                return

            field = value

            if (!value) {
                for (o in _objects.values) {
                    if (o is Attackable) {
                        o.target = null
                        o.stopMove(null)
                        o.stopAllEffects()

                        o.aggroList.clear()
                        o.attackByList.clear()
                        if (o.hasAI()) {
                            o.ai.setIntention(CtrlIntention.IDLE)
                            o.ai.stopAITask()
                        }
                    }
                }
            } else {
                for (o in _objects.values) {
                    if (o is Attackable)
                        o.status.startHpMpRegeneration()
                    else if (o is Npc)
                        o.startRandomAnimationTimer()
                }
            }
        }
    private val _playersCount = AtomicInteger()

    val objects: Collection<WorldObject>
        get() = _objects.values

    val playersCount: Int
        get() = _playersCount.get()

    /**
     * Check if neighbors (including self) aren't inhabited.
     * @return true if the above condition is met.
     */
    val isEmptyNeighborhood: Boolean
        get() {
            for (neighbor in surroundingRegions) {
                if (neighbor.playersCount != 0)
                    return false
            }
            return true
        }

    override fun toString(): String {
        return "WorldRegion " + _tileX + "_" + _tileY + ", _active=" + isActive + ", _playersCount=" + _playersCount.get() + "]"
    }

    fun addSurroundingRegion(region: WorldRegion) {
        surroundingRegions.add(region)
    }

    fun addZone(zone: ZoneType) {
        zones.add(zone)
    }

    fun removeZone(zone: ZoneType) {
        zones.remove(zone)
    }

    fun revalidateZones(character: Creature) {
        // Do NOT update the world region while the character is still in the process of teleporting
        if (character.isTeleporting)
            return

        zones.forEach { z -> z.revalidateInZone(character) }
    }

    fun removeFromZones(character: Creature) {
        zones.forEach { z -> z.removeCharacter(character) }
    }

    fun containsZone(zoneId: Int): Boolean {
        return zones.filter { x->x.id == zoneId }.any()
    }

    fun checkEffectRangeInsidePeaceZone(skill: L2Skill, loc: Location): Boolean {
        val range = skill.effectRange
        val up = loc.y + range
        val down = loc.y - range
        val left = loc.x + range
        val right = loc.x - range

        for (e in zones) {
            if (e is TownZone && e.isPeaceZone || e is DerbyTrackZone || e is PeaceZone) {
                if (e.isInsideZone(loc.x, up, loc.z))
                    return false

                if (e.isInsideZone(loc.x, down, loc.z))
                    return false

                if (e.isInsideZone(left, loc.y, loc.z))
                    return false

                if (e.isInsideZone(right, loc.y, loc.z))
                    return false

                if (e.isInsideZone(loc.x, loc.y, loc.z))
                    return false
            }
        }
        return true
    }

    /**
     * Put the given object into WorldRegion objects map. If it's a player, increment the counter (used for region activation/desactivation).
     * @param object : The object to register into this region.
     */
    fun addVisibleObject(`object`: WorldObject) {

        _objects[`object`.objectId] = `object`

        if (`object` is Player)
            _playersCount.incrementAndGet()
    }

    /**
     * Remove the given object from WorldRegion objects map. If it's a player, decrement the counter (used for region activation/desactivation).
     * @param object : The object to remove from this region.
     */
    fun removeVisibleObject(`object`: WorldObject) {

        _objects.remove(`object`.objectId)

        if (`object` is Player)
            _playersCount.decrementAndGet()
    }
}