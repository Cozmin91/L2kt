package com.l2kt.gameserver.data.xml

import com.l2kt.commons.data.xml.IXmlReader
import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.data.xml.SkillTreeData.forEach
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.holder.skillnode.ClanSkillNode
import com.l2kt.gameserver.model.holder.skillnode.EnchantSkillNode
import com.l2kt.gameserver.model.holder.skillnode.FishingSkillNode
import org.w3c.dom.Document
import java.nio.file.Path
import java.util.*

/**
 * This class loads and stores datatypes extending SkillNode, such as [FishingSkillNode], [EnchantSkillNode] and [ClanSkillNode].
 */
object SkillTreeData : IXmlReader {
    private val _fishingSkills = LinkedList<FishingSkillNode>()
    private val _clanSkills = LinkedList<ClanSkillNode>()
    private val _enchantSkills = LinkedList<EnchantSkillNode>()

    init {
        load()
    }

    override fun load() {
        parseFile("./data/xml/skillstrees")
        IXmlReader.LOGGER.info("Loaded {} fishing skills.", _fishingSkills.size)
        IXmlReader.LOGGER.info("Loaded {} clan skills.", _clanSkills.size)
        IXmlReader.LOGGER.info("Loaded {} enchant skills.", _enchantSkills.size)
    }

    override fun parseDocument(doc: Document, path: Path) {
        forEach(doc, "list") { listNode ->
            forEach(
                listNode,
                "clanSkill"
            ) { clanSkillNode -> _clanSkills.add(ClanSkillNode(parseAttributes(clanSkillNode))) }
            forEach(listNode, "fishingSkill") { fishingSkillNode ->
                _fishingSkills.add(
                    FishingSkillNode(
                        parseAttributes(
                            fishingSkillNode
                        )
                    )
                )
            }
            forEach(listNode, "enchantSkill") { enchantSkillNode ->
                _enchantSkills.add(
                    EnchantSkillNode(
                        parseAttributes(
                            enchantSkillNode
                        )
                    )
                )
            }
        }
    }

    /**
     * @param player : The player to check.
     * @return the [List] of available [FishingSkillNode] for the associated [Player].
     */
    fun getFishingSkillsFor(player: Player): List<FishingSkillNode> {
        val result = ArrayList<FishingSkillNode>()

        _fishingSkills
            .filter { s -> s.minLvl <= player.level && (!s.isDwarven || player.hasDwarvenCraft() && s.isDwarven) }
            .forEach { s ->
                if (player.getSkillLevel(s.id) == s.value - 1)
                    result.add(s)
            }

        return result
    }

    /**
     * @param player : The player to check.
     * @param skillId : The skill id to check.
     * @param skillLevel : The skill level to check.
     * @return the [FishingSkillNode] for the associated [Player].
     */
    fun getFishingSkillFor(player: Player, skillId: Int, skillLevel: Int): FishingSkillNode? {
        // We first retrieve skill. If it doesn't exist for this id and level, return null.
        val fsn =
            _fishingSkills.firstOrNull { s -> s.id == skillId && s.value == skillLevel && (!s.isDwarven || player.hasDwarvenCraft() && s.isDwarven) }
                ?: return null

        // Integrity check ; we check if minimum template skill node is ok for player level.
        if (fsn.minLvl > player.level)
            return null

        // We find current known player skill level, if any. If the level is respected, we return the skill.
        return if (player.getSkillLevel(skillId) == fsn.value - 1) fsn else null

    }

    /**
     * @param player : The player to check.
     * @return the required level for next [FishingSkillNode] for the associated [Player].
     */
    fun getRequiredLevelForNextFishingSkill(player: Player): Int {
        return _fishingSkills.stream()
            .filter { s -> s.minLvl > player.level && (!s.isDwarven || player.hasDwarvenCraft() && s.isDwarven) }
            .min { s1, s2 -> Integer.compare(s1.minLvl, s2.minLvl) }.map { s -> s.minLvl }.orElse(0)
    }

    /**
     * @param player : The player to check.
     * @return the [List] of available [ClanSkillNode] for the associated [Player].
     */
    fun getClanSkillsFor(player: Player): List<ClanSkillNode> {
        // Clan check integrity.
        val clan = player.clan ?: return emptyList()

        val result = ArrayList<ClanSkillNode>()

        _clanSkills.stream().filter { s -> s.minLvl <= clan.level }.forEach { s ->
            val clanSkill = clan.clanSkills[s.id]
            if (clanSkill == null && s.value == 1 || clanSkill != null && clanSkill.level == s.value - 1)
                result.add(s)
        }

        return result
    }

    /**
     * @param player : The player to check.
     * @param skillId : The skill id to check.
     * @param skillLevel : The skill level to check.
     * @return the [ClanSkillNode] for the associated [Player].
     */
    fun getClanSkillFor(player: Player, skillId: Int, skillLevel: Int): ClanSkillNode? {
        val clan = player.clan ?: return null

        val csn = _clanSkills.firstOrNull { s -> s.id == skillId && s.value == skillLevel }
            ?: return null

        if (csn.minLvl > clan.level)
            return null

        // We find current known clan skill level, if any. If the level is respected, we return the skill.
        val clanSkill = clan.clanSkills[skillId]
        return if (clanSkill == null && csn.value == 1 || clanSkill != null && clanSkill.level == csn.value - 1) csn else null

    }

    /**
     * @param player : The player to check.
     * @return the [List] of available [EnchantSkillNode] for the associated [Player].
     */
    fun getEnchantSkillsFor(player: Player): List<EnchantSkillNode> {
        val result = ArrayList<EnchantSkillNode>()

        for (esn in _enchantSkills) {
            val skill = player.getSkill(esn.id)
            if (skill != null && (skill.level == SkillTable.getMaxLevel(skill.id) && (esn.value == 101 || esn.value == 141) || skill.level == esn.value - 1))
                result.add(esn)
        }
        return result
    }

    /**
     * @param player : The player to check.
     * @param skillId : The skill id to check.
     * @param skillLevel : The skill level to check.
     * @return the [EnchantSkillNode] for the associated [Player].
     */
    fun getEnchantSkillFor(player: Player, skillId: Int, skillLevel: Int): EnchantSkillNode? {
        // We first retrieve skill. If it doesn't exist for this id and level, return null.
        val esn =
            _enchantSkills.firstOrNull { s -> s.id == skillId && s.value == skillLevel } ?: return null

        // We now test player current skill level.
        val currentSkillLevel = player.getSkillLevel(skillId)
        return if (currentSkillLevel == SkillTable.getMaxLevel(skillId) && (skillLevel == 101 || skillLevel == 141) || currentSkillLevel == skillLevel - 1) esn else null

    }
}