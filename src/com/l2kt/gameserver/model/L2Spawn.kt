package com.l2kt.gameserver.model

import com.l2kt.Config
import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.geoengine.GeoEngine
import com.l2kt.gameserver.idfactory.IdFactory
import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Monster
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.model.location.SpawnLocation

import java.lang.reflect.Constructor
import java.util.logging.Logger

/**
 * This class manages the spawn and respawn a [Npc].<br></br>
 * <br></br>
 * L2Npc can be spawned to already defined [SpawnLocation]. If not defined, [Npc] is not spawned.
 */
/**
 * Constructor of L2Spawn.<BR></BR>
 * <BR></BR>
 * <B><U> Concept</U> :</B><BR></BR>
 * <BR></BR>
 * Each L2Spawn owns generic and static properties (ex : RewardExp, RewardSP, AggroRange...). All of those properties are stored in a different L2NpcTemplate for each type of L2Spawn. Each template is loaded once in the server cache memory (reduce memory use). When a new instance of L2Spawn is
 * created, server just create a link between the instance and the template. This link is stored in <B>_template</B><BR></BR>
 * <BR></BR>
 * Each L2Npc is linked to a L2Spawn that manages its spawn and respawn (delay, location...). This link is stored in <B>_spawn</B> of the L2Npc<BR></BR>
 * <BR></BR>
 * <B><U> Actions</U> :</B><BR></BR>
 * <BR></BR>
 *  * Set the _template of the L2Spawn
 *  * Calculate the implementationName used to generate the generic constructor of L2Npc managed by this L2Spawn
 *  * Create the generic constructor of L2Npc managed by this L2Spawn<BR></BR>
 * <BR></BR>
 * @param template : [NpcTemplate] the template of [Npc] to be spawned.
 * @throws SecurityException
 * @throws ClassNotFoundException
 * @throws NoSuchMethodException
 */
class L2Spawn @Throws(SecurityException::class, ClassNotFoundException::class, NoSuchMethodException::class)
constructor(// the link on the NpcTemplate object containing generic and static properties of this spawn (ex : RewardExp, RewardSP, AggroRange...)
    /**
     * Return the template of NPC.
     * @return [NpcTemplate] : Template of NPC.
     */
    val template: NpcTemplate?
) : Runnable {

    // the generic constructor of L2Npc managed by this L2Spawn
    private lateinit var _constructor: Constructor<*>

    // the instance if L2Npc
    /**
     * Return the instance of NPC.
     * @return [Npc] : Instance of NPC.
     */
    var npc: Npc? = null
        private set

    // spawn location
    /**
     * Returns the [Location] of the spawn point.
     * @return Location : location of spawn point.
     */
    /**
     * Sets the [SpawnLocation] of the spawn point.
     * @param loc : Location.
     */
    var loc: SpawnLocation? = null

    // respawn information
    /**
     * Returns the respawn delay of the spawn. Respawn delay represents average respawn time of the NPC.
     * @return int : Respawn delay of the spawn.
     */
    /**
     * Set the respawn delay. Respawn delay represents average respawn time of the NPC. It can't be inferior to 0, it is automatically modified to 1 second.
     * @param delay : Respawn delay in seconds.
     */
    var respawnDelay: Int = 0
        set(delay) {
            field = Math.max(1, delay)
        }
    /**
     * Returns the respawn delay of the spawn. Respawn delay represents average respawn time of the NPC.
     * @return int : Respawn delay of the spawn.
     */
    /**
     * Set the respawn random delay. Respawn random delay represents random period of the respawn. It can't be inferior to respawn delay.
     * @param random : Random respawn delay in seconds.
     */
    var respawnRandom: Int = 0
        set(random) {
            field = Math.min(respawnDelay, random)
        }
    private var _respawnEnabled: Boolean = false

    /**
     * @return the minimum RaidBoss spawn delay.
     */
    /**
     * Set the minimum respawn delay.
     * @param date
     */
    var respawnMinDelay: Int = 0
    /**
     * @return the maximum RaidBoss spawn delay.
     */
    /**
     * Set Maximum respawn delay.
     * @param date
     */
    var respawnMaxDelay: Int = 0

    /**
     * Returns the ID of NPC.
     * @return int : ID of NPC.
     */
    val npcId: Int
        get() = template!!.npcId

    /**
     * Returns the X coordinate of the spawn point.
     * @return int : X coordinate of spawn point.
     */
    val locX: Int
        get() = loc!!.x

    /**
     * Returns the Y coordinate of the spawn point.
     * @return int : Y coordinate of spawn point.
     */
    val locY: Int
        get() = loc!!.y

    /**
     * Returns the Z coordinate of the spawn point.
     * @return int : Z coordinate of spawn point.
     */
    val locZ: Int
        get() = loc!!.z

    /**
     * Returns the heading of the spawn point.
     * @return int : Heading of spawn point.
     */
    val heading: Int
        get() = loc!!.heading

    /**
     * Returns the respawn time of the spawn. Respawn time is respawn delay +- random respawn delay.
     * @return int : Respawn time of the spawn.
     */
    val respawnTime: Int
        get() {
            var respawnTime = respawnDelay

            if (respawnRandom > 0)
                respawnTime += Rnd[-respawnRandom, respawnRandom]

            return respawnTime
        }

    init {
        if (this.template != null)
        {
            // Create the generic constructor of L2Npc managed by this L2Spawn
            val parameters = arrayOf<Class<*>>(
                Int::class.java,
                Class.forName("com.l2kt.gameserver.model.actor.template.NpcTemplate")
            )
            _constructor = Class.forName("com.l2kt.gameserver.model.actor.instance." + this.template!!.type)
                .getConstructor(*parameters)
        }

    }// Set the _template of the L2Spawn

    /**
     * Sets the [Location] of the spawn point by separate coordinates.
     * @param locX : X coordinate.
     * @param locY : Y coordinate.
     * @param locZ : Z coordinate.
     * @param heading : Heading.
     */
    fun setLoc(locX: Int, locY: Int, locZ: Int, heading: Int) {
        loc = SpawnLocation(locX, locY, locZ, heading)
    }

    /**
     * Enables or disable respawn state of NPC.
     * @param state
     */
    fun setRespawnState(state: Boolean) {
        _respawnEnabled = state
    }

    /**
     * Create the [Npc], add it to the world and launch its onSpawn() action.<BR></BR>
     * <BR></BR>
     * <B><U> Concept</U> :</B><BR></BR>
     * <BR></BR>
     * L2Npc can be spawned to already defined [SpawnLocation]. If not defined, [Npc] is not spawned.<BR></BR>
     * <BR></BR>
     * <B><U> Actions sequence for each spawn</U> : </B><BR></BR>
     *
     *  * Get [Npc] initialize parameters and generate its object ID
     *  * Call the constructor of the [Npc]
     *  * Link the [Npc] to this [L2Spawn]
     *  * Make [SpawnLocation] check, when exists spawn process continues
     *  * Reset [Npc] parameters - for re-spawning of existing [Npc]
     *  * Calculate position using [SpawnLocation] and geodata
     *  * Set the HP and MP of the [Npc] to the max
     *  * Set the position and heading of the [Npc] (random heading is calculated, if not defined : value -1)
     *  * Spawn [Npc] to the world
     *
     * @param isSummonSpawn When true, summon magic circle will appear.
     * @return the newly created instance.
     */
    fun doSpawn(isSummonSpawn: Boolean): Npc? {
        try {
            // Check if the L2Spawn is not a Pet.
            if (template!!.isType("Pet"))
                return null

            // Get L2Npc Init parameters and its generate an Identifier
            val parameters = arrayOf(IdFactory.getInstance().nextId, template)

            // Call the constructor of the L2Npc (can be a L2ArtefactInstance, L2FriendlyMobInstance, L2GuardInstance, L2MonsterInstance, L2SiegeGuardInstance, L2BoxInstance, L2FeedableBeastInstance, L2TamedBeastInstance, L2NpcInstance)
            val tmp = _constructor.newInstance(*parameters)

            if (isSummonSpawn && tmp is Creature)
                tmp.isShowSummonAnimation = isSummonSpawn

            // Check if the Instance is a L2Npc
            if (tmp !is Npc)
                return null

            // create final instance of L2Npc
            npc = tmp

            // assign L2Spawn to L2Npc
            npc!!.spawn = this

            // initialize L2Npc and spawn it
            initializeAndSpawn()

            return npc
        } catch (e: Exception) {
            _log.warning("L2Spawn: Error during spawn, NPC id=" + template!!.npcId)
            return null
        }

    }

    /**
     * Create a respawn task to be launched after the fixed + random delay. Respawn is only possible when respawn enabled.
     */
    fun doRespawn() {
        // Check if respawn is possible to prevent multiple respawning caused by lag
        if (_respawnEnabled) {
            // Calculate the random time, if any.
            val respawnTime = respawnTime * 1000

            // Schedule respawn of the NPC
            ThreadPool.schedule(this, respawnTime.toLong())
        }
    }

    /**
     * Respawns NPC.
     */
    override fun run() {
        if (_respawnEnabled) {
            npc!!.refreshID()

            initializeAndSpawn()
        }
    }

    /**
     * Initializes the [Npc] based on data in this L2Spawn and spawn [Npc] into the world.
     */
    private fun initializeAndSpawn() {
        // If location does not exist, there's a problem.
        if (loc == null) {
            _log.warning("L2Spawn : the following npcID: " + template!!.npcId + " misses location informations.")
            return
        }

        // reset effects and status
        npc!!.stopAllEffects()
        npc!!.setIsDead(false)

        // reset decay info
        npc!!.isDecayed = false

        // reset script value
        npc!!.scriptValue = 0

        // The L2Npc is spawned at the exact position (Lox, Locy, Locz)
        val locx = loc!!.x
        val locy = loc!!.y
        var locz = GeoEngine.getHeight(locx, locy, loc!!.z).toInt()

        // FIXME temporarily fix: when the spawn Z and geo Z differs more than 200, use spawn Z coord
        if (Math.abs(locz - loc!!.z) > 200)
            locz = loc!!.z

        // Set the HP and MP of the L2Npc to the max
        npc!!.setCurrentHpMp(npc!!.maxHp.toDouble(), npc!!.maxMp.toDouble())

        // when champion mod is enabled, try to make NPC a champion
        if (Config.CHAMPION_FREQUENCY > 0) {
            // It can't be a Raid, a Raid minion nor a minion. Quest mobs and chests are disabled too.
            if (npc is Monster && !template!!.cantBeChampion() && npc!!.level >= Config.CHAMP_MIN_LVL && npc!!.level <= Config.CHAMP_MAX_LVL && !npc!!.isRaidRelated && !npc!!.isMinion)
                (npc as Attackable).isChampion = Rnd[100] < Config.CHAMPION_FREQUENCY
        }

        // set heading (random heading if not defined)
        npc!!.heading = if (loc!!.heading < 0) Rnd[65536] else loc!!.heading

        // spawn NPC on new coordinates
        npc!!.spawnMe(locx, locy, locz)
    }

    override fun toString(): String {
        return "L2Spawn [id=" + template!!.npcId + ", loc=" + loc!!.toString() + "]"
    }

    companion object {
        private val _log = Logger.getLogger(L2Spawn::class.java.name)
    }
}