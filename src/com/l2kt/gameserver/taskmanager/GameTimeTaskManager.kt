package com.l2kt.gameserver.taskmanager

import java.util.ArrayList
import java.util.Calendar
import java.util.concurrent.ConcurrentHashMap

import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.scripting.Quest

import com.l2kt.gameserver.instancemanager.DayNightSpawnManager

/**
 * Controls game time, informs spawn manager about day/night spawns and players about daytime change. Informs players about their extended activity in game.
 */
object GameTimeTaskManager : Runnable {

    private val players = ConcurrentHashMap<Player, Int>()

    private var questEvents: MutableList<Quest> = mutableListOf()

    private var time: Int = 0
    private var _isNight: Boolean = false
    private const val MINUTES_PER_DAY = 24 * 60 // 24h * 60m

    private const val HOURS_PER_GAME_DAY = 4 // 4h is 1 game day
    private const val MINUTES_PER_GAME_DAY = HOURS_PER_GAME_DAY * 60 // 240m is 1 game day
    private const val SECONDS_PER_GAME_DAY = MINUTES_PER_GAME_DAY * 60 // 14400s is 1 game day
    private const val MILLISECONDS_PER_GAME_MINUTE = SECONDS_PER_GAME_DAY / MINUTES_PER_DAY * 1000 // 10000ms is 1 game minute

    private const val TAKE_BREAK_HOURS = 2
    private const val TAKE_BREAK_GAME_MINUTES = TAKE_BREAK_HOURS * MINUTES_PER_DAY / HOURS_PER_GAME_DAY // 2h of real time is 720 game minutes

    /**
     * Returns how many game days have left since last server start.
     * @return int : Game day.
     */
    val gameDay: Int
        get() = time / MINUTES_PER_DAY

    /**
     * Returns game time in minute format (0-1439).
     * @return int : Game time.
     */
    val gameTime: Int
        get() = time % MINUTES_PER_DAY

    /**
     * Returns game hour (0-23).
     * @return int : Game hour.
     */
    val gameHour: Int
        get() = time % MINUTES_PER_DAY / 60

    /**
     * Returns game minute (0-59).
     * @return int : Game minute.
     */
    val gameMinute: Int
        get() = time % 60

    /**
     * Returns game time standard format (00:00-23:59).
     * @return String : Game time.
     */
    val gameTimeFormated: String
        get() = String.format("%02d:%02d", gameHour, gameMinute)

    /**
     * Returns game daytime. Night is between 00:00 and 06:00.
     * @return boolean : True, when there is night.
     */
    val isNight: Boolean
        get() = gameTime < 360

    init {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        time = (System.currentTimeMillis() - cal.timeInMillis).toInt() / MILLISECONDS_PER_GAME_MINUTE
        _isNight = isNight

        ThreadPool.scheduleAtFixedRate(
            this,
            MILLISECONDS_PER_GAME_MINUTE.toLong(),
            MILLISECONDS_PER_GAME_MINUTE.toLong()
        )
    }

    override fun run() {
        time++

        for (quest in questEvents)
            quest.onGameTime()

        var skill: L2Skill? = null

        if (dayNightHasChanged()) {
            // Change day/night.
            _isNight = !_isNight

            // Inform day/night spawn manager.
            DayNightSpawnManager.notifyChangeMode()

            // Set Shadow Sense skill to apply/remove effect from players.
            skill = SkillTable.getInfo(L2Skill.SKILL_SHADOW_SENSE, 1)
        }

        if (players.isEmpty())
            return

        // Loop all players.
        for (entry in players.entries) {
            // Get player.
            val player = entry.key

            // Player isn't online, skip.
            if (!player.isOnline)
                continue

            // Shadow Sense skill is set and player has Shadow Sense skill, activate/deactivate its effect.
            if (skill != null && player.hasSkill(L2Skill.SKILL_SHADOW_SENSE)) {
                // Remove and add Shadow Sense to activate/deactivate effect.
                player.removeSkill(L2Skill.SKILL_SHADOW_SENSE, false)
                player.addSkill(skill, false)

                // Inform player about effect change.
                player.sendPacket(
                    SystemMessage.getSystemMessage(if (_isNight) SystemMessageId.NIGHT_S1_EFFECT_APPLIES else SystemMessageId.DAY_S1_EFFECT_DISAPPEARS).addSkillName(
                        L2Skill.SKILL_SHADOW_SENSE
                    )
                )
            }

            // Activity time has passed already.
            if (time >= entry.value) {
                // Inform player about his activity.
                player.sendPacket(SystemMessageId.PLAYING_FOR_LONG_TIME)

                // Update activity time.
                entry.setValue(time + TAKE_BREAK_GAME_MINUTES)
            }
        }
    }

    private fun dayNightHasChanged() = _isNight != isNight

    fun addQuestEvent(quest: Quest) {
        if (questEvents.isEmpty())
            questEvents = ArrayList(3)

        questEvents.add(quest)
    }

    fun add(player: Player) {
        players[player] = time + TAKE_BREAK_GAME_MINUTES
    }

    fun remove(player: Creature) {
        players.remove(player)
    }
}