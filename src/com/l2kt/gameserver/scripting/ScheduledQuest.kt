package com.l2kt.gameserver.scripting

import java.util.*

abstract class ScheduledQuest(questId: Int, descr: String) : Quest(questId, descr) {

    private var _type: Schedule? = null
    private var _start: Calendar? = null
    private var _end: Calendar? = null
    /**
     * Return true, when a [ScheduledQuest] is started.
     * @return boolean : True, when started.
     */
    var isStarted: Boolean = false
        private set

    /**
     * Returns time of next action of the script.
     * @return long : Time in milliseconds.
     */
    val timeNext: Long
        get() {
            if (_type == null)
                return 0

            return if (isStarted) _end!!.timeInMillis else _start!!.timeInMillis
        }

    /**
     * Period of the script.
     */
    enum class Schedule private constructor(val period: Int) {
        HOURLY(Calendar.HOUR),
        DAILY(Calendar.DAY_OF_YEAR),
        WEEKLY(Calendar.WEEK_OF_YEAR),
        MONTHLY_DAY(Calendar.MONTH),
        MONTHLY_WEEK(Calendar.MONTH),
        YEARLY_DAY(Calendar.YEAR),
        YEARLY_WEEK(Calendar.YEAR)
    }

    /**
     * Set up schedule system for the script. Returns true, when successfully done.
     * @param type : Type of the schedule.
     * @param start : Start information.
     * @param end : End information.
     * @return boolean : True, when successfully loaded schedule system.
     */
    fun setSchedule(type: String, start: String, end: String): Boolean {
        try {
            _type = Schedule.valueOf(type)
            _start = parseTimeStamp(start)
            _end = parseTimeStamp(end)
            isStarted = false

            val st = _start!!.timeInMillis
            val now = System.currentTimeMillis()
            if (_end == null || _end!!.timeInMillis == st) {
                // start and end events are at same time, consider as one-event script
                _end = null

                // schedule next start
                if (st < now)
                    _start!!.add(_type!!.period, 1)
            } else {
                // normal schedule, both events are in same period
                val en = _end!!.timeInMillis
                if (st < en) {
                    // last schedule had passed, schedule next start
                    if (en < now)
                        _start!!.add(_type!!.period, 1)
                    else if (st < now)
                        isStarted = true
                    else
                        _end!!.add(
                            _type!!.period,
                            -1
                        )// last schedule has not started yet, shift end by 1 period backwards (is updated in notifyAndSchedule() when starting schedule)
                    // last schedule is running, start script
                } else {
                    // last schedule is running, schedule next end and start script
                    if (st < now) {
                        _end!!.add(_type!!.period, 1)
                        isStarted = true
                    } else if (now < en) {
                        _start!!.add(_type!!.period, -1)
                        isStarted = true
                    }// last schedule is running, shift start by 1 period backwards (is updated in notifyAndSchedule() when starting schedule) and start script
                    // last schedule has not started yet, do nothing
                }// reverse schedule, each event is in different period (e.g. different day for DAILY - start = 23:00, end = 01:00)
            }

            // initialize script and return
            return init()
        } catch (e: Exception) {
            Quest.LOGGER.error("Error loading schedule data for {}.", e, toString())

            _type = null
            _start = null
            _end = null
            isStarted = false
            return false
        }

    }

    @Throws(Exception::class)
    private fun parseTimeStamp(value: String?): Calendar? {
        if (value == null)
            return null

        val calendar = Calendar.getInstance()
        val timeStamp: Array<String>

        when (_type) {
            ScheduledQuest.Schedule.HOURLY -> {
                // HOURLY, "20:10", "50:00"
                timeStamp = value.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                calendar.set(Calendar.MINUTE, Integer.valueOf(timeStamp[0]))
                calendar.set(Calendar.SECOND, Integer.valueOf(timeStamp[1]))
                calendar.set(Calendar.MILLISECOND, 0)
                return calendar
            }

            ScheduledQuest.Schedule.DAILY ->
                // DAILY, "16:20:10", "17:20:00"
                timeStamp = value.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            ScheduledQuest.Schedule.WEEKLY -> {
                // WEEKLY, "MON 6:20:10", "FRI 17:20:00"
                val params = value.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                timeStamp = params[1].split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                calendar.set(Calendar.DAY_OF_WEEK, getDayOfWeek(params[0]))
            }

            ScheduledQuest.Schedule.MONTHLY_DAY -> {
                // MONTHLY_DAY, "1 6:20:10", "2 17:20:00"
                val params = value.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                timeStamp = params[1].split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                calendar.set(Calendar.DAY_OF_MONTH, Integer.valueOf(params[0]))
            }

            ScheduledQuest.Schedule.MONTHLY_WEEK -> {
                // MONTHLY_WEEK, "MON-1 6:20:10", "FRI-2 17:20:00"
                val params = value.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val date = params[0].split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                timeStamp = params[1].split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                calendar.set(Calendar.DAY_OF_WEEK, getDayOfWeek(date[0]))
                calendar.set(Calendar.WEEK_OF_MONTH, Integer.valueOf(date[1]))
            }

            ScheduledQuest.Schedule.YEARLY_DAY -> {
                // YEARLY_DAY, "23-02 6:20:10", "25-03 17:20:00"
                val params = value.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val date = params[0].split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                timeStamp = params[1].split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                calendar.set(Calendar.DAY_OF_MONTH, Integer.valueOf(date[0]))
                calendar.set(Calendar.MONTH, Integer.valueOf(date[1]) - 1)
            }

            ScheduledQuest.Schedule.YEARLY_WEEK -> {
                // YEARLY_WEEK, "MON-1 6:20:10", "FRI-2 17:20:00"
                val params = value.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val date = params[0].split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                timeStamp = params[1].split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                calendar.set(Calendar.DAY_OF_WEEK, getDayOfWeek(date[0]))
                calendar.set(Calendar.WEEK_OF_YEAR, Integer.valueOf(date[1]))
            }

            else -> return null
        }

        // set hour, minute and second
        calendar.set(Calendar.HOUR_OF_DAY, Integer.valueOf(timeStamp[0]))
        calendar.set(Calendar.MINUTE, Integer.valueOf(timeStamp[1]))
        calendar.set(Calendar.SECOND, Integer.valueOf(timeStamp[2]))
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar
    }

    /**
     * Notify and schedule next action of the script.
     */
    fun notifyAndSchedule() {
        if (_type == null)
            return

        // notify one-action script
        if (_end == null) {
            // notify start
            try {
                onStart()
            } catch (e: Exception) {
                Quest.LOGGER.error("Error starting {}.", e, toString())
            }

            // schedule next start
            _start!!.add(_type!!.period, 1)
            print(_start!!)
            return
        }

        // notify two-action script
        if (isStarted) {
            // notify end
            try {
                onEnd()
                isStarted = false
            } catch (e: Exception) {
                Quest.LOGGER.error("Error ending {}.", e, toString())
            }

            // schedule start
            _start!!.add(_type!!.period, 1)
            print(_start!!)
        } else {
            // notify start
            try {
                onStart()
                isStarted = true
            } catch (e: Exception) {
                Quest.LOGGER.error("Error starting {}.", e, toString())
            }

            // schedule end
            _end!!.add(_type!!.period, 1)
            print(_end!!)
        }
    }

    /**
     * Initializes a script and returns information about script to be scheduled or not. Set internal values, parameters, etc...
     * @return boolean : True, when script was initialized and can be scheduled.
     */
    protected fun init(): Boolean {
        // the script was initialized as started, run start event
        if (isStarted)
            onStart()

        return true
    }

    /**
     * Starts a script. Handles spawns, announcements, loads variables, etc...
     */
    protected abstract fun onStart()

    /**
     * Ends a script. Handles spawns, announcements, saves variables, etc...
     */
    protected abstract fun onEnd()

    /**
     * Convert text representation of day [Calendar] day.
     * @param day : String representation of day.
     * @return int : [Calendar] representation of day.
     * @throws Exception : Throws [Exception], when can't convert day.
     */
    @Throws(Exception::class)
    private fun getDayOfWeek(day: String): Int {
        return if (day == "MON")
            Calendar.MONDAY
        else if (day == "TUE")
            Calendar.TUESDAY
        else if (day == "WED")
            Calendar.WEDNESDAY
        else if (day == "THU")
            Calendar.THURSDAY
        else if (day == "FRI")
            Calendar.FRIDAY
        else if (day == "SAT")
            Calendar.SATURDAY
        else if (day == "SUN")
            Calendar.SUNDAY
        else
            throw Exception()
    }

    private fun print(c: Calendar) {
        Quest.LOGGER.debug(
            "{}: {} = {}.",
            toString(),
            if (c === _start) "Next start" else "Next end",
            String.format(
                "%d.%d.%d %d:%02d:%02d",
                c.get(Calendar.DAY_OF_MONTH),
                c.get(Calendar.MONTH) + 1,
                c.get(Calendar.YEAR),
                c.get(Calendar.HOUR_OF_DAY),
                c.get(Calendar.MINUTE),
                c.get(Calendar.SECOND)
            )
        )
    }
}