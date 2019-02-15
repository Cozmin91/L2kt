package com.l2kt.gameserver.model.zone.type

import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.zone.CastleZoneType
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.EtcStatusUpdate
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.skills.Stats

import java.util.concurrent.Future

/**
 * A zone extending [CastleZoneType], which fires a task on the first character entrance, notably used by castle damage traps.<br></br>
 * <br></br>
 * This task decreases HPs using a reuse delay and can affect specific class types. The zone is considered a danger zone.
 */
class DamageZone(id: Int) : CastleZoneType(id) {
    private var _task: Future<*>? = null

    private var _hpDamage = 200
    private var _initialDelay = 1000
    private var _reuseDelay = 5000

    override fun setParameter(name: String, value: String) {
        when {
            name == "hpDamage" -> _hpDamage = value.toInt()
            name.equals("initialDelay", ignoreCase = true) -> _initialDelay = value.toInt()
            name.equals("reuseDelay", ignoreCase = true) -> _reuseDelay = value.toInt()
            else -> super.setParameter(name, value)
        }
    }

    override fun isAffected(character: Creature): Boolean {
        return character is Playable
    }

    override fun onEnter(character: Creature) {
        if (_task == null && _hpDamage > 0) {
            // Castle traps are active only during siege, or if they're activated.
            if (castle != null && (!isEnabled || !castle!!.siege.isInProgress))
                return

            synchronized(this) {
                if (_task == null) {
                    _task = ThreadPool.scheduleAtFixedRate(Runnable{
                        if (_characters.isEmpty() || _hpDamage <= 0 || castle != null && (!isEnabled || !castle!!.siege.isInProgress)) {
                            stopTask()
                            return@Runnable
                        }

                        // Effect all people inside the zone.
                        for (temp in _characters.values) {
                            if (!temp.isDead)
                                temp.reduceCurrentHp(
                                    _hpDamage * (1 + temp.calcStat(
                                        Stats.DAMAGE_ZONE_VULN,
                                        0.0,
                                        null,
                                        null
                                    ) / 100), null, null
                                )
                        }
                    }, _initialDelay.toLong(), _reuseDelay.toLong())

                    // Message for castle traps.
                    if (castle != null)
                        castle!!.siege.announceToPlayers(
                            SystemMessage.getSystemMessage(SystemMessageId.A_TRAP_DEVICE_HAS_BEEN_TRIPPED),
                            false
                        )
                }
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

    private fun stopTask() {
        if (_task != null) {
            _task!!.cancel(false)
            _task = null
        }
    }
}