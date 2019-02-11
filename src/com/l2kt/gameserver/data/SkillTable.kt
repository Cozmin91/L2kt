package com.l2kt.gameserver.data

import java.io.File
import java.util.HashMap
import java.util.logging.Logger

import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.skills.DocumentSkill

object SkillTable {

    private val _log = Logger.getLogger(SkillTable::class.java.name)

    private val _skills = HashMap<Int, L2Skill>()
    private val _skillMaxLevel = HashMap<Int, Int>()

    val heroSkills = arrayOfNulls<L2Skill>(5)
    private val _heroSkillsId = intArrayOf(395, 396, 1374, 1375, 1376)

    val nobleSkills = arrayOfNulls<L2Skill>(8)
    private val _nobleSkillsId = intArrayOf(325, 326, 327, 1323, 1324, 1325, 1326, 1327)

    /**
     * Provides the skill hash
     * @param skill The L2Skill to be hashed
     * @return SkillTable.getSkillHashCode(skill.getId(), skill.getLevel())
     */
    fun getSkillHashCode(skill: L2Skill): Int {
        return getSkillHashCode(skill.id, skill.level)
    }

    /**
     * Centralized method for easier change of the hashing sys
     * @param skillId The Skill Id
     * @param skillLevel The Skill Level
     * @return The Skill hash number
     */
    fun getSkillHashCode(skillId: Int, skillLevel: Int): Int {
        return skillId * 256 + skillLevel
    }

    fun isHeroSkill(skillid: Int): Boolean {
        for (id in _heroSkillsId)
            if (id == skillid)
                return true

        return false
    }

    init {
        load()
    }

    private fun load() {
        val dir = File("./data/xml/skills")

        for (file in dir.listFiles()!!) {
            val doc = DocumentSkill(file)
            doc.parse()

            for (skill in doc.skills)
                _skills[getSkillHashCode(skill)] = skill
        }

        _log.info("SkillTable: Loaded " + _skills.size + " skills.")

        // Stores max level of skills in a map for future uses.
        for (skill in _skills.values) {
            // Only non-enchanted skills
            val skillLvl = skill.level
            if (skillLvl < 99) {
                val skillId = skill.id
                val maxLvl = getMaxLevel(skillId)

                if (skillLvl > maxLvl)
                    _skillMaxLevel[skillId] = skillLvl
            }
        }

        // Loading FrequentSkill enumeration values
        for (sk in FrequentSkill.values())
            sk.skill = getInfo(sk._id, sk._level)

        for (i in _heroSkillsId.indices)
            heroSkills[i] = getInfo(_heroSkillsId[i], 1)

        for (i in nobleSkills.indices)
            nobleSkills[i] = getInfo(_nobleSkillsId[i], 1)
    }

    fun reload() {
        _skills.clear()
        _skillMaxLevel.clear()

        load()
    }

    fun getInfo(skillId: Int, level: Int): L2Skill {
        return _skills[getSkillHashCode(skillId, level)]!!
    }

    fun getMaxLevel(skillId: Int): Int {
        val maxLevel = _skillMaxLevel[skillId]
        return maxLevel ?: 0
    }

    /**
     * @param addNoble if true, will add also Advanced headquarters.
     * @return an array with siege skills.
     */
    fun getSiegeSkills(addNoble: Boolean): Array<L2Skill> {
        val temp = mutableListOf<L2Skill>()

        temp.add(0, _skills[SkillTable.getSkillHashCode(246, 1)]!!)
        temp.add(1, _skills[SkillTable.getSkillHashCode(247, 1)]!!)

        if (addNoble)
            temp.add(2, _skills[SkillTable.getSkillHashCode(326, 1)]!!)

        return temp.toTypedArray()
    }

    /**
     * Enum to hold some important references to frequently used (hardcoded) skills in core
     * @author DrHouse
     */
    enum class FrequentSkill constructor(val _id: Int, val _level: Int) {
        LUCKY(194, 1),
        BLESSING_OF_PROTECTION(5182, 1),

        SEAL_OF_RULER(246, 1),
        BUILD_HEADQUARTERS(247, 1),
        STRIDER_SIEGE_ASSAULT(325, 1),

        DWARVEN_CRAFT(1321, 1),
        COMMON_CRAFT(1322, 1),

        FIREWORK(5965, 1),
        LARGE_FIREWORK(2025, 1),
        SPECIAL_TREE_RECOVERY_BONUS(2139, 1),

        ANTHARAS_JUMP(4106, 1),
        ANTHARAS_TAIL(4107, 1),
        ANTHARAS_FEAR(4108, 1),
        ANTHARAS_DEBUFF(4109, 1),
        ANTHARAS_MOUTH(4110, 1),
        ANTHARAS_BREATH(4111, 1),
        ANTHARAS_NORMAL_ATTACK(4112, 1),
        ANTHARAS_NORMAL_ATTACK_EX(4113, 1),
        ANTHARAS_SHORT_FEAR(5092, 1),
        ANTHARAS_METEOR(5093, 1),

        QUEEN_ANT_BRANDISH(4017, 1),
        QUEEN_ANT_STRIKE(4018, 1),
        QUEEN_ANT_SPRINKLE(4019, 1),
        NURSE_HEAL_1(4020, 1),
        NURSE_HEAL_2(4024, 1),

        ZAKEN_TELE(4216, 1),
        ZAKEN_MASS_TELE(4217, 1),
        ZAKEN_DRAIN(4218, 1),
        ZAKEN_HOLD(4219, 1),
        ZAKEN_DUAL_ATTACK(4220, 1),
        ZAKEN_MASS_DUAL_ATTACK(4221, 1),
        ZAKEN_SELF_TELE(4222, 1),
        ZAKEN_NIGHT_TO_DAY(4223, 1),
        ZAKEN_DAY_TO_NIGHT(4224, 1),
        ZAKEN_REGEN_NIGHT(4227, 1),
        ZAKEN_REGEN_DAY(4242, 1),

        RAID_CURSE(4215, 1),
        RAID_CURSE2(4515, 1),
        RAID_ANTI_STRIDER_SLOW(4258, 1),

        WYVERN_BREATH(4289, 1),
        ARENA_CP_RECOVERY(4380, 1),
        VARKA_KETRA_PETRIFICATION(4578, 1),
        FAKE_PETRIFICATION(4616, 1),

        THE_VICTOR_OF_WAR(5074, 1),
        THE_VANQUISHED_OF_WAR(5075, 1);

        var skill: L2Skill? = null
    }
}