package com.l2kt.gameserver.data.xml

import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.commons.data.xml.IXmlReader
import com.l2kt.gameserver.data.xml.ScriptData.forEach
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.ScheduledQuest
import org.w3c.dom.Document
import java.nio.file.Path
import java.util.*

/**
 * This class loads and stores [Quest]s - being regular quests, AI scripts or scheduled scripts.
 */
object ScriptData : IXmlReader, Runnable {

    private val _quests = ArrayList<Quest>()
    private val _scheduled = LinkedList<ScheduledQuest>()
    const val PERIOD = 5 * 60 * 1000 // 5 minutes

    val quests: List<Quest>
        get() = _quests

    init {
        load()
    }

    override fun load() {
        parseFile("./data/xml/scripts.xml")
        IXmlReader.LOGGER.info("Loaded {} regular scripts and {} scheduled scripts.", _quests.size, _scheduled.size)

        ThreadPool.scheduleAtFixedRate(this, 0, PERIOD.toLong())
    }

    override fun parseDocument(doc: Document, p: Path) {
        forEach(doc, "list") { listNode ->
            forEach(listNode, "script") innerForEach@{ scriptNode ->
                val params = scriptNode.attributes
                val path = parseString(params, "path")
                if (path == null) {
                    IXmlReader.LOGGER.warn("One of the script path isn't defined.")
                    return@innerForEach
                }

                try {
                    // Create the script.
                    val instance = Class.forName("com.l2kt.gameserver.scripting.$path").newInstance() as Quest

                    // Add quest, script, AI or any other custom type of script.
                    _quests.add(instance)

                    // The script has been identified as a scheduled script, make proper checks and schedule the launch.
                    if (instance is ScheduledQuest) {
                        // Get schedule parameter, when not exist, script is not scheduled.
                        val type = parseString(params, "schedule") ?: return@innerForEach

                        // Get mandatory start parameter, when not exist, script is not scheduled.
                        val start = parseString(params, "start")
                        if (start == null) {
                            IXmlReader.LOGGER.warn("Missing 'start' parameter for scheduled script '{}'.", path)
                            return@innerForEach
                        }

                        // Get optional end parameter, when not exist, script is one-event type.
                        val end = parseString(params, "end")

                        // Schedule script, when successful, register it.
                        if (instance.setSchedule(type, start, end))
                            _scheduled.add(instance)
                    }
                } catch (e: Exception) {
                    IXmlReader.LOGGER.error("Script '{}' is missing.", e, path)
                }
            }
        }
    }

    override fun run() {
        val next = System.currentTimeMillis() + PERIOD

        for (script in _scheduled) {
            // When next action triggers in closest period, schedule the script action.
            val eta = next - script.timeNext
            if (eta > 0)
                ThreadPool.schedule(Scheduler(script), PERIOD - eta)
        }
    }

    /**
     * Returns the [Quest] by given quest name.
     * @param questName : The name of the quest.
     * @return Quest : Quest to be returned, null if quest does not exist.
     */
    fun getQuest(questName: String): Quest? {
        return _quests.firstOrNull { q -> q.name.equals(questName, ignoreCase = true) }
    }

    /**
     * Returns the [Quest] by given quest id.
     * @param questId : The id of the quest.
     * @return Quest : Quest to be returned, null if quest does not exist.
     */
    fun getQuest(questId: Int): Quest? {
        return _quests.firstOrNull { q -> q.questId == questId }
    }

    private class Scheduler(private val _script: ScheduledQuest) : Runnable {

        override fun run() {
            _script.notifyAndSchedule()

            // In case the next action is triggered before the resolution, schedule the the action again.
            val eta = System.currentTimeMillis() + PERIOD - _script.timeNext
            if (eta > 0)
                ThreadPool.schedule(this, PERIOD - eta)
        }
    }
}