package com.l2kt.gameserver.model.zone

import com.l2kt.commons.logging.CLogger
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.network.serverpackets.L2GameServerPacket
import com.l2kt.gameserver.scripting.EventType
import com.l2kt.gameserver.scripting.Quest
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * An abstract base class for any zone type, which holds [Creature]s affected by this zone, linked [Quest]s and the associated [ZoneForm].<br></br>
 * <br></br>
 * Zones can be retrieved by id, but since most use dynamic IDs, you must set individual zone id yourself if you want the system works correctly (otherwise id can be different if you add or remove zone types or zones).
 */
abstract class ZoneType protected constructor(val id: Int) {
    protected val _characters: MutableMap<Int, Creature> = ConcurrentHashMap()

    private var _questEvents: MutableMap<EventType, MutableList<Quest>> = mutableMapOf()
    var zone: ZoneForm? = null
        set(zone) {
            if (this.zone != null)
                throw IllegalStateException("Zone already set")

            field = zone
        }

    val characters: Collection<Creature>
        get() = _characters.values

    protected abstract fun onEnter(character: Creature)

    protected abstract fun onExit(character: Creature)

    override fun toString(): String {
        return javaClass.simpleName + "[" + id + "]"
    }

    /**
     * @param x : The X position to test.
     * @param y : The Y position to test.
     * @return true if the given coordinates are within zone's plane. We use getHighZ() as Z reference.
     */
    fun isInsideZone(x: Int, y: Int): Boolean {
        return zone!!.isInsideZone(x, y, zone!!.highZ)
    }

    /**
     * @param x : The X position to test.
     * @param y : The Y position to test.
     * @param z : The Z position to test.
     * @return true if the given coordinates are within the zone.
     */
    fun isInsideZone(x: Int, y: Int, z: Int): Boolean {
        return zone!!.isInsideZone(x, y, z)
    }

    /**
     * @param object : Use object's X/Y positions.
     * @return true if the [WorldObject] is inside the zone.
     */
    fun isInsideZone(`object`: WorldObject): Boolean {
        return isInsideZone(`object`.x, `object`.y, `object`.z)
    }

    fun getDistanceToZone(x: Int, y: Int): Double {
        return zone!!.getDistanceToZone(x, y)
    }

    fun getDistanceToZone(`object`: WorldObject): Double {
        return zone!!.getDistanceToZone(`object`.x, `object`.y)
    }

    fun visualizeZone(z: Int) {
        zone!!.visualizeZone(id, z)
    }

    /**
     * Update a [Creature] zone state.<br></br>
     * <br></br>
     * If the Creature is inside the zone, but not yet part of _characters [Map] :
     *
     *  * Fire [Quest.notifyEnterZone].
     *  * Add the Creature to the Map.
     *  * Fire zone onEnter() event.
     *
     * If the Creature isn't inside the zone, and was part of _characters Map, we run [.removeCharacter].
     * @param character : The affected Creature.
     */
    fun revalidateInZone(character: Creature) {
        // If the character can't be affected by this zone, return.
        if (!isAffected(character))
            return

        // If the character is inside the zone.
        if (isInsideZone(character)) {
            // We test if the character was part of the zone.
            if (!_characters.containsKey(character.objectId)) {
                // Notify to scripts.
                val quests = getQuestByEvent(EventType.ON_ENTER_ZONE)
                for (quest in quests)
                    quest.notifyEnterZone(character, this)

                // Register player.
                _characters[character.objectId] = character

                // Notify Zone implementation.
                onEnter(character)
            }
        } else
            removeCharacter(character)
    }

    /**
     * Remove a [Creature] from this zone.
     *
     *  * Fire [Quest.notifyExitZone].
     *  * Remove the Creature from the [Map].
     *  * Fire zone onExit() event.
     *
     * @param character : The Creature to remove.
     */
    fun removeCharacter(character: Creature) {
        // We test and remove the character if he was part of the zone.
        if (_characters.remove(character.objectId) != null) {
            // Notify to scripts.
            val quests = getQuestByEvent(EventType.ON_EXIT_ZONE)
            for (quest in quests)
                quest.notifyExitZone(character, this)
            // Notify Zone implementation.
            onExit(character)
        }
    }

    /**
     * @param character : The Creature to test.
     * @return true if the [Creature] is in the zone _characters [Map].
     */
    fun isCharacterInZone(character: Creature): Boolean {
        return _characters.containsKey(character.objectId)
    }

    /**
     * @param <A> : The generic type.
     * @param type : The instance type to filter.
     * @return a [List] of filtered type [Creature]s within this zone. Generate a temporary List.
    </A> */
    fun <A> getKnownTypeInside(type: Class<A>): List<A> {
        val result = ArrayList<A>()

        for (obj in _characters.values) {
            if (type.isAssignableFrom(obj.javaClass))
                result.add(obj as A)
        }
        return result
    }

    /**
     * Add a [Quest] on _questEvents [Map]. Generate both Map and [List] if not existing (lazy initialization).<br></br>
     * <br></br>
     * If already existing, we remove and add it back.
     * @param type : The EventType to test.
     * @param quest : The Quest to add.
     */
    fun addQuestEvent(type: EventType, quest: Quest) {
        var eventList = _questEvents[type] ?: mutableListOf()
        if (eventList.isEmpty()) {
            eventList.add(quest)

            _questEvents[type] = eventList
        } else {
            eventList.remove(quest)
            eventList.add(quest)
        }
    }

    /**
     * @param type : The EventType to test.
     * @return the [List] of available [Quest]s associated to this zone for a given [EventType].
     */
    fun getQuestByEvent(type: EventType): List<Quest> {
        return if (_questEvents.isEmpty()) mutableListOf() else _questEvents[type] ?: mutableListOf()
    }

    /**
     * Broadcast a [L2GameServerPacket] to all [Player]s inside the zone.
     * @param packet : The packet to use.
     */
    fun broadcastPacket(packet: L2GameServerPacket) {
        for (character in _characters.values) {
            if (character is Player)
                character.sendPacket(packet)
        }
    }

    /**
     * Setup new parameters for this zone. By default, we return a warning (which mean this parameter isn't used on child zone).
     * @param name : The parameter name.
     * @param value : The parameter value.
     */
    open fun setParameter(name: String, value: String) {
        LOGGER.warn("Unknown name/values couple {}, {} for {}.", name, value, toString())
    }

    /**
     * @param character : The Creature to test.
     * @return true if the given [Creature] is affected by this zone. Overriden in children classes.
     */
    protected open fun isAffected(character: Creature): Boolean {
        return true
    }

    /**
     * Teleport all [Player]s located in this [ZoneType] to specific coords x/y/z.
     * @param x : The X parameter used as teleport location.
     * @param y : The Y parameter used as teleport location.
     * @param z : The Z parameter used as teleport location.
     */
    fun movePlayersTo(x: Int, y: Int, z: Int) {
        if (_characters.isEmpty())
            return

        for (player in getKnownTypeInside(Player::class.java)) {
            if (player.isOnline)
                player.teleToLocation(x, y, z, 0)
        }
    }

    /**
     * Teleport all [Player]s located in this [ZoneType] to a specific [Location].
     * @see .movePlayersTo
     * @param loc : The Location used as coords.
     */
    fun movePlayersTo(loc: Location) {
        movePlayersTo(loc.x, loc.y, loc.z)
    }

    companion object {
        val LOGGER = CLogger(ZoneType::class.java.name)
    }
}