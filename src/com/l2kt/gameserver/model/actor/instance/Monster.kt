package com.l2kt.gameserver.model.actor.instance

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.MinionList
import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature

import com.l2kt.gameserver.model.actor.template.NpcTemplate

/**
 * A monster extends [Attackable] class.<br></br>
 * <br></br>
 * It is an attackable [Creature], with the capability to hold minions/master.
 */
open class Monster(objectId: Int, template: NpcTemplate) : Attackable(objectId, template) {
    private var _master: Monster? = null
    private var _minionList: MinionList? = null

    val minionList: MinionList
        get() {
            if (_minionList == null)
                _minionList = MinionList(this)

            return _minionList!!
        }

    override fun isAutoAttackable(attacker: Creature): Boolean {
        // FIXME: to test to allow monsters hit others monsters
        return if (attacker is Monster) false else true

    }

    override fun isAggressive(): Boolean {
        return template.aggroRange > 0
    }

    override fun onSpawn() {
        // Generate minions and spawn them (initial call and regular minions respawn are handled in the same method).
        if (!template.minionData.isEmpty())
            minionList.spawnMinions()

        super.onSpawn()
    }

    override fun onTeleported() {
        super.onTeleported()

        if (hasMinions())
            minionList.onMasterTeleported()
    }

    override fun deleteMe() {
        if (hasMinions())
            minionList.onMasterDeletion()
        else if (_master != null)
            _master!!.minionList.onMinionDeletion(this)

        super.deleteMe()
    }

    override fun getMaster(): Monster? {
        return _master
    }

    fun setMaster(master: Monster?){
        _master = master
    }

    fun hasMinions(): Boolean {
        return _minionList != null
    }

    /**
     * Teleport this [Monster] to its master.
     */
    fun teleToMaster() {
        if (_master == null)
            return

        // Init the position of the Minion and add it in the world as a visible object
        val offset = (100.0 + collisionRadius + _master!!.collisionRadius).toInt()
        val minRadius = (_master!!.collisionRadius + 30).toInt()

        var newX = Rnd[minRadius * 2, offset * 2] // x
        var newY = Rnd[newX, offset * 2] // distance
        newY = Math.sqrt((newY * newY - newX * newX).toDouble()).toInt() // y

        if (newX > offset + minRadius)
            newX = _master!!.x + newX - offset
        else
            newX = _master!!.x - newX + minRadius

        if (newY > offset + minRadius)
            newY = _master!!.y + newY - offset
        else
            newY = _master!!.y - newY + minRadius

        teleToLocation(newX, newY, _master!!.z, 0)
    }
}