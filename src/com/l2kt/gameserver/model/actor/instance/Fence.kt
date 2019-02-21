package com.l2kt.gameserver.model.actor.instance

import com.l2kt.gameserver.geoengine.geodata.IGeoObject
import com.l2kt.gameserver.idfactory.IdFactory
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.network.serverpackets.ExColosseumFenceInfo

/**
 * @author Hasha
 */
class Fence(// fence description from world point of view
    val type: Int, val sizeX: Int, val sizeY: Int, height: Int, // fence description from geodata point of view
    override val geoX: Int, override val geoY: Int, override val geoZ: Int, override val objectGeoData: Array<ByteArray>
) : WorldObject(IdFactory.getInstance().nextId), IGeoObject {
    override val height: Int

    // 2 dummy object to spawn fences with 2 and 3 layers easily
    // TODO: I know it is shitcoded, but didn't figure out any better solution
    private val _object2: L2DummyFence?
    private val _object3: L2DummyFence?

    init {
        this.height = height * FENCE_HEIGHT

        _object2 = if (height > 1) L2DummyFence(this) else null
        _object3 = if (height > 2) L2DummyFence(this) else null
    }

    override fun onSpawn() {
        // spawn me
        super.onSpawn()

        // spawn dummy fences
        _object2?.spawnMe(position)
        _object3?.spawnMe(position)
    }

    override fun decayMe() {
        // despawn dummy fences
        _object2?.decayMe()
        _object3?.decayMe()

        // despawn me
        super.decayMe()
    }

    override fun isAutoAttackable(attacker: Creature): Boolean {
        return false
    }

    override fun sendInfo(activeChar: Player) {
        activeChar.sendPacket(ExColosseumFenceInfo(objectId, this))
    }

    /**
     * Dummy fence class in order to spawn/delete multi-layer fences correctly.
     * @author Hasha
     */
    protected inner class L2DummyFence(private val _fence: Fence) : WorldObject(IdFactory.getInstance().nextId) {

        override fun isAutoAttackable(attacker: Creature): Boolean {
            return false
        }

        override fun sendInfo(activeChar: Player) {
            activeChar.sendPacket(ExColosseumFenceInfo(objectId, _fence))
        }
    }

    companion object {
        private const val FENCE_HEIGHT = 24
    }
}