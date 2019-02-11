package com.l2kt.gameserver.scripting

import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import java.util.concurrent.ScheduledFuture
import java.util.logging.Logger

class QuestTimer(
    protected val _quest: Quest,
    protected val _name: String,
    protected val _npc: Npc,
    protected val _player: Player,
    time: Long,
    protected val _isRepeating: Boolean
) {

    protected var _schedular: ScheduledFuture<*>? = null

    init {

        if (_isRepeating)
            _schedular = ThreadPool.scheduleAtFixedRate(ScheduleTimerTask(), time, time)
        else
            _schedular = ThreadPool.schedule(ScheduleTimerTask(), time)
    }

    override fun toString(): String {
        return _name
    }

    protected inner class ScheduleTimerTask : Runnable {
        override fun run() {
            if (_schedular == null)
                return

            if (!_isRepeating)
                cancel()

            _quest.notifyEvent(_name, _npc, _player)
        }
    }

    fun cancel() {
        if (_schedular != null) {
            _schedular!!.cancel(false)
            _schedular = null
        }

        _quest.removeQuestTimer(this)
    }

    /**
     * public method to compare if this timer matches with the key attributes passed.
     * @param quest : Quest instance to which the timer is attached
     * @param name : Name of the timer
     * @param npc : Npc instance attached to the desired timer (null if no npc attached)
     * @param player : Player instance attached to the desired timer (null if no player attached)
     * @return boolean
     */
    fun equals(quest: Quest?, name: String?, npc: Npc, player: Player): Boolean {
        if (quest == null || quest !== _quest)
            return false

        return if (name == null || name != _name) false else npc === _npc && player == _player

    }

    companion object {
        protected val _log = Logger.getLogger(QuestTimer::class.java.name)
    }
}