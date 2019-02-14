package com.l2kt.gameserver.model

import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.xml.NpcData
import com.l2kt.gameserver.idfactory.IdFactory
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Monster
import java.util.concurrent.ConcurrentHashMap

class MinionList(private val _master: Monster) {
    private val _minions = ConcurrentHashMap<Monster, Boolean>()

    /**
     * @return a set of the spawned (alive) minions.
     */
    val spawnedMinions: List<Monster>
        get() = _minions.entries.filter{ m -> m.value }.map{ it.key }

    /**
     * @return a complete view of minions.
     */
    val minions: Map<Monster, Boolean>
        get() = _minions

    /**
     * Manage the spawn of Minions.
     *
     *  * Get the Minion data of all Minions that must be spawn
     *  * For each Minion type, spawn the amount of Minion needed
     *
     */
    fun spawnMinions() {
        // We generate new instances. We can't reuse existing instances, since previous monsters can still exist.
        for (data in _master.template.minionData) {
            // Get the template of the Minion to spawn
            val template = NpcData.getInstance().getTemplate(data.minionId) ?: continue

            for (i in 0 until data.amount) {
                val minion = Monster(IdFactory.getInstance().nextId, template)
                minion.master = _master
                minion.isMinion = _master.isRaidBoss

                initializeNpcInstance(_master, minion)
            }
        }
    }

    /**
     * Called on the master death.
     *
     *  * In case of regular master monster : minions references are deleted, but monsters instances are kept alive (until they gonna be deleted if their interaction move to IDLE, or killed).
     *  * In case of raid bosses : all minions are instantly deleted.
     *
     */
    fun onMasterDie() {
        if (_master.isRaidBoss) {
            // For all minions, delete them.
            for (minion in spawnedMinions)
                minion.deleteMe()
        } else {
            // For all minions, remove leader reference.
            for (minion in _minions.keys)
                minion.master = null
        }

        // Cleanup the entire MinionList.
        _minions.clear()
    }

    /**
     * Called on master deletion.
     */
    fun onMasterDeletion() {
        // For all minions, delete them and remove leader reference.
        for (minion in _minions.keys) {
            minion.master = null
            minion.deleteMe()
        }

        // Cleanup the entire MinionList.
        _minions.clear()
    }

    /**
     * Called on minion deletion, or on master death.
     * @param minion : The minion to make checks on.
     */
    fun onMinionDeletion(minion: Monster) {
        // Keep it to avoid OOME.
        minion.master = null

        // Cleanup the map form this reference.
        _minions.remove(minion)
    }

    /**
     * Called on minion death. Flag the [Monster] from the list of the spawned minions as unspawned.
     * @param minion : The minion to make checks on.
     * @param respawnTime : Respawn the Monster using this timer, but only if master is alive.
     */
    fun onMinionDie(minion: Monster, respawnTime: Int) {
        _minions[minion] = false

        if (minion.isRaidRelated && respawnTime > 0 && !_master.isAlikeDead) {
            ThreadPool.schedule({
                // Master is visible, but minion isn't spawned back (via teleport, for example).
                if (!_master.isAlikeDead && _master.isVisible && !_minions[minion]!!) {
                    minion.refreshID()
                    initializeNpcInstance(_master, minion)
                }
            }, respawnTime.toLong())
        }
    }

    /**
     * Called if master/minion was attacked. Master and all free minions receive aggro against attacker.
     * @param caller : That instance will call for help versus attacker.
     * @param attacker : That instance will receive all aggro.
     */
    fun onAssist(caller: Creature, attacker: Creature?) {
        if (attacker == null)
            return

        // The master is aggroed.
        if (!_master.isAlikeDead && !_master.isInCombat)
            _master.addDamageHate(attacker, 0, 1)

        val callerIsMaster = caller === _master

        // Define the aggro value of minions.
        var aggro = if (callerIsMaster) 10 else 1
        if (_master.isRaidBoss)
            aggro *= 10

        for (minion in spawnedMinions) {
            if (!minion.isDead && (callerIsMaster || !minion.isInCombat))
                minion.addDamageHate(attacker, 0, aggro)
        }
    }

    /**
     * Teleport all minions back to master position.
     */
    fun onMasterTeleported() {
        for (minion in spawnedMinions) {
            if (minion.isDead || minion.isMovementDisabled)
                continue

            minion.teleToMaster()
        }
    }

    protected fun initializeNpcInstance(master: Monster, minion: Monster): Monster {
        _minions[minion] = true

        minion.setIsNoRndWalk(true)
        minion.stopAllEffects()
        minion.setIsDead(false)
        minion.isDecayed = false

        // Set the Minion HP, MP and Heading
        minion.setCurrentHpMp(minion.maxHp.toDouble(), minion.maxMp.toDouble())
        minion.heading = master.heading

        // Init the position of the Minion and add it in the world as a visible object
        val offset = (100.0 + minion.collisionRadius + master.collisionRadius).toInt()
        val minRadius = (master.collisionRadius + 30).toInt()

        var newX = Rnd[minRadius * 2, offset * 2] // x
        var newY = Rnd[newX, offset * 2] // distance
        newY = Math.sqrt((newY * newY - newX * newX).toDouble()).toInt() // y
        if (newX > offset + minRadius)
            newX = master.x + newX - offset
        else
            newX = master.x - newX + minRadius
        if (newY > offset + minRadius)
            newY = master.y + newY - offset
        else
            newY = master.y - newY + minRadius

        minion.spawnMe(newX, newY, master.z)

        return minion
    }
}