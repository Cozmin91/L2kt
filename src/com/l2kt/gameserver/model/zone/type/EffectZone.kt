package com.l2kt.gameserver.model.zone.type

import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.holder.IntIntHolder
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.model.zone.ZoneType
import com.l2kt.gameserver.network.serverpackets.EtcStatusUpdate
import java.util.*
import java.util.concurrent.Future

/**
 * A zone extending [ZoneType], which fires a task on the first character entrance.<br></br>
 * <br></br>
 * This task launches skill effects on all characters within this zone, and can affect specific class types. It can also be activated or desactivated. The zone is considered a danger zone.
 */
class EffectZone(id: Int) : ZoneType(id) {
    private val _skills = ArrayList<IntIntHolder>(5)

    private var _chance = 100
    private var _initialDelay = 0
    private var _reuseDelay = 30000

    private var _isEnabled = true

    private var _task: Future<*>? = null
    private var _target = "Playable"

    override fun setParameter(name: String, value: String) {
        if (name == "chance")
            _chance = Integer.parseInt(value)
        else if (name == "initialDelay")
            _initialDelay = Integer.parseInt(value)
        else if (name == "reuseDelay")
            _reuseDelay = Integer.parseInt(value)
        else if (name == "defaultStatus")
            _isEnabled = java.lang.Boolean.parseBoolean(value)
        else if (name == "skill") {
            val skills = value.split(";").dropLastWhile { it.isEmpty() }.toTypedArray()
            for (skill in skills) {
                val skillSplit = skill.split("-").dropLastWhile { it.isEmpty() }.toTypedArray()
                if (skillSplit.size != 2)
                    ZoneType.LOGGER.warn("Invalid skill format {} for {}.", skill, toString())
                else {
                    try {
                        _skills.add(IntIntHolder(Integer.parseInt(skillSplit[0]), Integer.parseInt(skillSplit[1])))
                    } catch (nfe: NumberFormatException) {
                        ZoneType.LOGGER.warn("Invalid skill format {} for {}.", skill, toString())
                    }

                }
            }
        } else if (name == "targetType")
            _target = value
        else
            super.setParameter(name, value)
    }

    override fun isAffected(character: Creature): Boolean {
        try {
            if (!Class.forName("com.l2kt.gameserver.model.actor.$_target").isInstance(character))
                return false
        } catch (e: ClassNotFoundException) {
            ZoneType.LOGGER.error("Error for {} on invalid target type {}.", e, toString(), _target)
        }

        return true
    }

    override fun onEnter(character: Creature) {
        if (_task == null) {
            synchronized(this) {
                if (_task == null)
                    _task = ThreadPool.scheduleAtFixedRate(Runnable{
                        if (!_isEnabled)
                            return@Runnable

                        if (_characters.isEmpty()) {
                            _task!!.cancel(true)
                            _task = null

                            return@Runnable
                        }

                        for (temp in _characters.values) {
                            if (temp.isDead || Rnd[100] >= _chance)
                                continue

                            for (entry in _skills) {
                                val skill = entry.skill
                                if (skill != null && skill.checkCondition(temp, temp, false) && temp.getFirstEffect(
                                        entry.id
                                    ) == null
                                )
                                    skill.getEffects(temp, temp)
                            }
                        }
                    }, _initialDelay.toLong(), _reuseDelay.toLong())
            }
        }

        if (character is Player) {
            character.setInsideZone(ZoneId.DANGER_AREA, true)
            character.sendPacket(EtcStatusUpdate(character))
        }
    }

    override fun onExit(character: Creature) {
        if (character is Player) {
            character.setInsideZone(ZoneId.DANGER_AREA, false)

            if (!character.isInsideZone(ZoneId.DANGER_AREA))
                character.sendPacket(EtcStatusUpdate(character))
        }
    }

    /**
     * Edit this zone activation state.
     * @param state : The new state to set.
     */
    fun editStatus(state: Boolean) {
        _isEnabled = state
    }
}