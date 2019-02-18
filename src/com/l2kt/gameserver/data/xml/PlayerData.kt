package com.l2kt.gameserver.data.xml

import com.l2kt.commons.data.xml.IXmlReader
import com.l2kt.gameserver.data.xml.PlayerData.forEach
import com.l2kt.gameserver.model.actor.template.PlayerTemplate
import com.l2kt.gameserver.model.base.ClassId
import com.l2kt.gameserver.model.holder.skillnode.GeneralSkillNode
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.templates.StatsSet
import org.w3c.dom.Document
import java.nio.file.Path
import java.util.*

/**
 * This class loads and stores [PlayerTemplate]s. It also feed their skill trees.
 */
object PlayerData : IXmlReader {
    private val _templates = HashMap<Int, PlayerTemplate>()

    init {
        load()
    }

    override fun load() {
        parseFile("./data/xml/classes")
        IXmlReader.LOGGER.info("Loaded {} player classes templates.", _templates.size)

        // We add parent skills, if existing.
        for (template in _templates.values) {
            val parentClassId = template.classId.parent
            if (parentClassId != null)
                template.skills.addAll(_templates[parentClassId.id]!!.skills)
        }
    }

    override fun parseDocument(doc: Document, path: Path) {
        forEach(doc, "list") { listNode ->
            forEach(listNode, "class") { classNode ->
                val set = StatsSet()
                forEach(classNode, "set") { setNode -> set.putAll(parseAttributes(setNode)) }
                forEach(classNode, "skills") { skillsNode ->
                    val skills = ArrayList<GeneralSkillNode>()
                    forEach(
                        skillsNode,
                        "skill"
                    ) { skillNode -> skills.add(GeneralSkillNode(parseAttributes(skillNode))) }
                    set.set("skills", skills)
                }
                forEach(classNode, "spawns") { spawnsNode ->
                    val locs = ArrayList<Location>()
                    forEach(spawnsNode, "spawn") { spawnNode -> locs.add(Location(parseAttributes(spawnNode))) }
                    set.set("spawnLocations", locs)
                }
                _templates[set.getInteger("id")] = PlayerTemplate(set)
            }
        }
    }

    fun getTemplate(classId: ClassId): PlayerTemplate? {
        return _templates[classId.id]
    }

    fun getTemplate(classId: Int): PlayerTemplate? {
        return _templates[classId]
    }

    fun getClassNameById(classId: Int): String {
        val template = _templates[classId]
        return if (template != null) template.className else "Invalid class"
    }
}