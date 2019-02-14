package com.l2kt.gameserver.data.xml

import com.l2kt.commons.data.xml.IXmlReader
import com.l2kt.gameserver.data.ItemTable
import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.data.xml.NpcData.forEach
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.MinionData
import com.l2kt.gameserver.model.PetDataEntry
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.model.actor.template.PetTemplate
import com.l2kt.gameserver.model.item.DropCategory
import com.l2kt.gameserver.model.item.DropData
import com.l2kt.gameserver.templates.StatsSet
import org.w3c.dom.Document
import java.nio.file.Path
import java.util.*
import java.util.function.Predicate
import kotlin.streams.toList

/**
 * Loads and stores [NpcTemplate]s.
 */
object NpcData : IXmlReader {
    private val _npcs = HashMap<Int, NpcTemplate>()

    val allNpcs: Collection<NpcTemplate>
        get() = _npcs.values

    init {
        load()
    }

    override fun load() {
        parseFile("./data/xml/npcs")
        IXmlReader.LOGGER.info("Loaded {} NPC templates.", _npcs.size)
    }

    override fun parseDocument(doc: Document, path: Path) {
        forEach(doc, "list") { listNode ->
            forEach(listNode, "npc") { npcNode ->
                val attrs = npcNode.attributes
                val npcId = parseInteger(attrs, "id")!!
                val templateId =
                    if (attrs.getNamedItem("idTemplate") == null) npcId else parseInteger(attrs, "idTemplate")
                val set = StatsSet()
                set.set("id", npcId)
                set.set("idTemplate", templateId)
                set.set("name", parseString(attrs, "name"))
                set.set("title", parseString(attrs, "title"))

                forEach(npcNode, "set") { setNode ->
                    val setAttrs = setNode.attributes
                    set.set(parseString(setAttrs, "name"), parseString(setAttrs, "val"))
                }
                forEach(npcNode, "ai") { aiNode ->
                    val aiAttrs = aiNode.attributes
                    set.set("aiType", parseString(aiAttrs, "type"))
                    set.set("ssCount", parseInteger(aiAttrs, "ssCount"))
                    set.set("ssRate", parseInteger(aiAttrs, "ssRate"))
                    set.set("spsCount", parseInteger(aiAttrs, "spsCount"))
                    set.set("spsRate", parseInteger(aiAttrs, "spsRate"))
                    set.set("aggro", try{parseInteger(aiAttrs, "aggro")}catch (test: IllegalStateException){null /* TODO Fix this shit */})

                    if (aiAttrs.getNamedItem("clan") != null) {
                        set.set(
                            "clan",
                            parseString(
                                aiAttrs,
                                "clan"
                            ).split(";").dropLastWhile { it.isEmpty() }.toTypedArray()
                        )
                        set.set("clanRange", parseInteger(aiAttrs, "clanRange"))
                        if (aiAttrs.getNamedItem("ignoredIds") != null)
                            set.set("ignoredIds", parseString(aiAttrs, "ignoredIds"))
                    }
                    set.set("canMove", parseBoolean(aiAttrs, "canMove"))
                    set.set("seedable", parseBoolean(aiAttrs, "seedable"))
                }
                forEach(npcNode, "drops") { dropsNode ->
                    val type = set.getString("type")
                    val isRaid =
                        type.equals("RaidBoss", ignoreCase = true) || type.equals("GrandBoss", ignoreCase = true)
                    val drops = ArrayList<DropCategory>()
                    forEach(dropsNode, "category") { categoryNode ->
                        val categoryAttrs = categoryNode.attributes
                        val category = DropCategory(parseInteger(categoryAttrs, "id")!!)
                        forEach(categoryNode, "drop") innerForEach@ { dropNode ->
                            val dropAttrs = dropNode.attributes
                            val data = DropData()
                            data.itemId = parseInteger(dropAttrs, "itemid")!!
                            data.minDrop = parseInteger(dropAttrs, "min")!!
                            data.maxDrop = parseInteger(dropAttrs, "max")!!
                            data.chance = parseInteger(dropAttrs, "chance")!!
                            if (ItemTable.getTemplate(data.itemId) == null) {
                                IXmlReader.LOGGER.warn("Droplist data for undefined itemId: {}.", data.itemId)
                                return@innerForEach
                            }
                            category.addDropData(data, isRaid)
                        }
                        drops.add(category)
                    }
                    set.set("drops", drops)
                }
                forEach(npcNode, "minions") { minionsNode ->
                    val minions = ArrayList<MinionData>()
                    forEach(minionsNode, "minion") { minionNode ->
                        val minionAttrs = minionNode.attributes
                        val data = MinionData()
                        data.minionId = parseInteger(minionAttrs, "id")
                        data.setAmountMin(parseInteger(minionAttrs, "min")!!)
                        data.setAmountMax(parseInteger(minionAttrs, "max")!!)
                        minions.add(data)
                    }
                    set.set("minions", minions)
                }
                forEach(npcNode, "petdata") { petdataNode ->
                    val petdataAttrs = petdataNode.attributes
                    set.set("mustUsePetTemplate", true)
                    set.set("food1", parseInteger(petdataAttrs, "food1"))
                    set.set("food2", parseInteger(petdataAttrs, "food2"))
                    set.set("autoFeedLimit", parseDouble(petdataAttrs, "autoFeedLimit"))
                    set.set("hungryLimit", parseDouble(petdataAttrs, "hungryLimit"))
                    set.set("unsummonLimit", parseDouble(petdataAttrs, "unsummonLimit"))

                    val entries = HashMap<Int, PetDataEntry>()
                    forEach(petdataNode, "stat") { statNode ->
                        val petSet = parseAttributes(statNode)
                        entries[petSet.getInteger("level")] = PetDataEntry(petSet)
                    }
                    set.set("petData", entries)
                }
                forEach(npcNode, "skills") { skillsNode ->
                    val skills = ArrayList<L2Skill>()
                    forEach(skillsNode, "skill") innerForEach@{ skillNode ->
                        val skillAttrs = skillNode.attributes
                        val skillId = parseInteger(skillAttrs, "id")!!
                        val level = parseInteger(skillAttrs, "level")!!
                        if (skillId == L2Skill.SKILL_NPC_RACE) {
                            set.set("raceId", level)
                            return@innerForEach
                        }

                        val skill = SkillTable.getInfo(skillId, level) ?: return@innerForEach

                        skills.add(skill)
                    }
                    set.set("skills", skills)
                }
                forEach(npcNode, "teachTo") { teachToNode ->
                    set.set(
                        "teachTo",
                        parseString(teachToNode.attributes, "classes")
                    )
                }

                _npcs[npcId] = if (set.getBool("mustUsePetTemplate", false)) PetTemplate(set) else NpcTemplate(set)
            }
        }
    }

    fun reload() {
        _npcs.clear()

        load()
    }

    fun getTemplate(id: Int): NpcTemplate? {
        return _npcs[id]
    }

    /**
     * @param name : The name of the NPC to search.
     * @return the [NpcTemplate] for a given name.
     */
    fun getTemplateByName(name: String): NpcTemplate? {
        return _npcs.values.firstOrNull { t -> t.name.equals(name, ignoreCase = true) }
    }

    /**
     * Gets all [NpcTemplate]s matching the filter.
     * @param filter : The Predicate filter used as a filter.
     * @return a NpcTemplate list matching the given filter.
     */
    fun getTemplates(filter: Predicate<NpcTemplate>): List<NpcTemplate> {
        return _npcs.values.stream().filter(filter).toList()
    }
}