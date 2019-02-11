package com.l2kt.gameserver.skills.effects

import com.l2kt.gameserver.geoengine.GeoEngine
import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.network.serverpackets.FlyToLocation
import com.l2kt.gameserver.network.serverpackets.FlyToLocation.FlyType
import com.l2kt.gameserver.network.serverpackets.ValidateLocation
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.templates.skills.L2EffectFlag
import com.l2kt.gameserver.templates.skills.L2EffectType

class EffectThrowUp(env: Env, template: EffectTemplate) : L2Effect(env, template) {
    private var _x: Int = 0
    private var _y: Int = 0
    private var _z: Int = 0

    override fun getEffectType(): L2EffectType {
        return L2EffectType.THROW_UP
    }

    override fun onStart(): Boolean {
        val curX = effected.x
        val curY = effected.y
        val curZ = effected.z

        val dx = (effector.x - curX).toDouble()
        val dy = (effector.y - curY).toDouble()
        val dz = (effector.z - curZ).toDouble()

        val distance = Math.sqrt(dx * dx + dy * dy)
        if (distance < 1 || distance > 2000)
            return false

        var offset = Math.min(distance.toInt() + skill.flyRadius, 1400)
        val cos: Double
        val sin: Double

        // TODO: handle Z axis movement better
        offset += Math.abs(dz).toInt()
        if (offset < 5)
            offset = 5

        sin = dy / distance
        cos = dx / distance

        _x = effector.x - (offset * cos).toInt()
        _y = effector.y - (offset * sin).toInt()
        _z = effected.z

        val destiny = GeoEngine.getInstance().canMoveToTargetLoc(effected.x, effected.y, effected.z, _x, _y, _z)
        _x = destiny.x
        _y = destiny.y

        effected.startStunning()
        effected.broadcastPacket(FlyToLocation(effected, _x, _y, _z, FlyType.THROW_UP))
        return true
    }

    override fun onActionTime(): Boolean {
        return false
    }

    override fun onExit() {
        effected.stopStunning(false)
        effected.setXYZ(_x, _y, _z)
        effected.broadcastPacket(ValidateLocation(effected))
    }

    override fun getEffectFlags(): Int {
        return L2EffectFlag.STUNNED.mask
    }
}