package com.l2kt.gameserver.skills.effects

import com.l2kt.commons.math.MathUtil
import com.l2kt.gameserver.geoengine.GeoEngine
import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.network.serverpackets.FlyToLocation
import com.l2kt.gameserver.network.serverpackets.FlyToLocation.FlyType
import com.l2kt.gameserver.network.serverpackets.ValidateLocation
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.templates.skills.L2EffectType

/**
 * This class handles warp effects, disappear and quickly turn up in a near location. If geodata enabled and an object is between initial and final point, flight is stopped just before colliding with object. Flight course and radius are set as skill properties (flyCourse and flyRadius):
 *  * Fly Radius means the distance between starting point and final point, it must be an integer.
 *  * Fly Course means the movement direction: imagine a compass above player's head, making north player's heading. So if fly course is 180, player will go backwards (good for blink, e.g.). By the way, if flyCourse = 360 or 0, player will be moved in in front of him. <br></br>
 * <br></br>
 * If target is effector, put in XML self = "1". This will make _actor = getEffector(). This, combined with target type, allows more complex actions like flying target's backwards or player's backwards.<br></br>
 * <br></br>
 * @author House
 */
class EffectWarp(env: Env, template: EffectTemplate) : L2Effect(env, template) {
    private var x: Int = 0
    private var y: Int = 0
    private var z: Int = 0
    private var _actor: Creature? = null

    override fun getEffectType(): L2EffectType {
        return L2EffectType.WARP
    }

    override fun onStart(): Boolean {
        _actor = if (isSelfEffect) effector else effected

        if (_actor!!.isMovementDisabled)
            return false

        val _radius = skill.flyRadius

        val angle = MathUtil.convertHeadingToDegree(_actor!!.heading)
        val radian = Math.toRadians(angle)
        val course = Math.toRadians(skill.flyCourse.toDouble())

        val x1 = (Math.cos(Math.PI + radian + course) * _radius).toInt()
        val y1 = (Math.sin(Math.PI + radian + course) * _radius).toInt()

        x = _actor!!.x + x1
        y = _actor!!.y + y1
        z = _actor!!.z

        val destiny = GeoEngine.canMoveToTargetLoc(_actor!!.x, _actor!!.y, _actor!!.z, x, y, z)
        x = destiny.x
        y = destiny.y
        z = destiny.z

        // TODO: check if this AI intention is retail-like. This stops player's previous movement
        _actor!!.ai.setIntention(CtrlIntention.IDLE)

        _actor!!.broadcastPacket(FlyToLocation(_actor!!, x, y, z, FlyType.DUMMY))
        _actor!!.abortAttack()
        _actor!!.abortCast()

        _actor!!.setXYZ(x, y, z)
        _actor!!.broadcastPacket(ValidateLocation(_actor!!))

        return true
    }

    override fun onActionTime(): Boolean {
        return false
    }
}