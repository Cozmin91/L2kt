package com.l2kt.gameserver.model.actor.template

import com.l2kt.gameserver.data.manager.CastleManager
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.MinionData
import com.l2kt.gameserver.model.base.ClassId
import com.l2kt.gameserver.model.entity.Castle
import com.l2kt.gameserver.model.item.DropCategory
import com.l2kt.gameserver.model.item.DropData
import com.l2kt.gameserver.scripting.EventType
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.templates.StatsSet
import com.l2kt.gameserver.templates.skills.L2SkillType
import java.util.*

open class NpcTemplate(set: StatsSet) : CreatureTemplate(set) {

    val npcId: Int = set.getInteger("id")
    val idTemplate: Int
    val type: String
    val name: String
    val isUsingServerSideName: Boolean
    val title: String?
    val isUsingServerSideTitle: Boolean
    private val _cantBeChampionMonster: Boolean
    val level: Byte
    val rewardExp: Int
    val rewardSp: Int
    val rightHand: Int
    val leftHand: Int
    val enchantEffect: Int
    val corpseTime: Int

    val dropHerbGroup: Int
    var race = Race.UNKNOWN
        private set
    val aiType: AIType?

    val ssCount: Int
    val ssRate: Int
    val spsCount: Int
    val spsRate: Int
    val aggroRange: Int

    var clans: Array<String>? = null
        private set
    var clanRange: Int = 0
        private set
    var ignoredIds: IntArray? = null
        private set

    private val _canMove: Boolean
    val isSeedable: Boolean

    private val _categories: MutableList<DropCategory>
    /**
     * @return the list of all Minions that must be spawn with the L2Npc using this L2NpcTemplate.
     */
    val minionData: MutableList<MinionData>
    private var _teachInfo: MutableList<ClassId> = mutableListOf()

    private val _skills = HashMap<SkillType, MutableList<L2Skill>>()
    private val _questEvents = HashMap<EventType, MutableList<Quest>>()

    var castle: Castle? = null
        private set

    /**
     * @return the list of all possible UNCATEGORIZED drops of this L2NpcTemplate.
     */
    val dropData: List<DropCategory>
        get() = _categories

    /**
     * @return the list of all possible item drops of this L2NpcTemplate. (ie full drops and part drops, mats, miscellaneous & UNCATEGORIZED)
     */
    val allDropData: List<DropData>
        get() {
            val list = ArrayList<DropData>()
            for (tmp in _categories)
                list.addAll(tmp.allDrops)

            return list
        }

    val skills: Map<SkillType, List<L2Skill>>
        get() = _skills

    val eventQuests: Map<EventType, List<Quest>>
        get() = _questEvents

    enum class SkillType {
        BUFF,
        DEBUFF,
        HEAL,
        PASSIVE,
        LONG_RANGE,
        SHORT_RANGE,
        SUICIDE
    }

    enum class AIType {
        DEFAULT,
        ARCHER,
        MAGE,
        HEALER,
        CORPSE
    }

    enum class Race {
        UNKNOWN,
        UNDEAD,
        MAGICCREATURE,
        BEAST,
        ANIMAL,
        PLANT,
        HUMANOID,
        SPIRIT,
        ANGEL,
        DEMON,
        DRAGON,
        GIANT,
        BUG,
        FAIRIE,
        HUMAN,
        ELVE,
        DARKELVE,
        ORC,
        DWARVE,
        OTHER,
        NONLIVING,
        SIEGEWEAPON,
        DEFENDINGARMY,
        MERCENARIE;


        companion object {

            val VALUES = values()
        }
    }

    init {

        idTemplate = set.getInteger("idTemplate", npcId)
        type = set.getString("type")
        name = set.getString("name")
        isUsingServerSideName = set.getBool("usingServerSideName", false)
        title = set.getString("title", "")
        isUsingServerSideTitle = set.getBool("usingServerSideTitle", false)
        _cantBeChampionMonster = title!!.equals("Quest Monster", ignoreCase = true) || isType("Chest")
        level = set.getByte("level", 1.toByte())
        rewardExp = set.getInteger("exp", 0)
        rewardSp = set.getInteger("sp", 0)
        rightHand = set.getInteger("rHand", 0)
        leftHand = set.getInteger("lHand", 0)
        enchantEffect = set.getInteger("enchant", 0)
        corpseTime = set.getInteger("corpseTime", 7)
        dropHerbGroup = set.getInteger("dropHerbGroup", 0)

        if (set.containsKey("raceId"))
            setRace(set.getInteger("raceId"))

        aiType = set.getEnum("aiType", AIType::class.java, AIType.DEFAULT)

        ssCount = set.getInteger("ssCount", 0)
        ssRate = set.getInteger("ssRate", 0)
        spsCount = set.getInteger("spsCount", 0)
        spsRate = set.getInteger("spsRate", 0)
        aggroRange = set.getInteger("aggro", 0)

        if (set.containsKey("clan")) {
            clans = set.getStringArray("clan")
            clanRange = set.getInteger("clanRange")

            if (set.containsKey("ignoredIds"))
                ignoredIds = set.getIntegerArray("ignoredIds")
        }

        _canMove = set.getBool("canMove", true)
        isSeedable = set.getBool("seedable", false)

        _categories = set.getList("drops")
        minionData = set.getList("minions")

        if (set.containsKey("teachTo")) {
            val classIds = set.getIntegerArray("teachTo")

            _teachInfo = ArrayList(classIds.size)
            for (classId in classIds)
                _teachInfo!!.add(ClassId.VALUES[classId])
        }

        addSkills(set.getList("skills"))

        // Set the Castle.
        for (castle in CastleManager.castles) {
            if (castle.relatedNpcIds.contains(npcId)) {
                this.castle = castle
                break
            }
        }
    }

    /**
     * Checks types, ignore case.
     * @param t the type to check.
     * @return true if the type are the same, false otherwise.
     */
    fun isType(t: String): Boolean {
        return type.equals(t, ignoreCase = true)
    }

    fun cantBeChampion(): Boolean {
        return _cantBeChampionMonster
    }

    fun setRace(raceId: Int) {
        // Race.UNKNOWN is already the default value. No needs to handle it.
        if (raceId < 1 || raceId > 23)
            return

        race = Race.VALUES[raceId]
    }

    fun canMove(): Boolean {
        return _canMove
    }

    /**
     * Add a drop to a given category. If the category does not exist, create it.
     * @param drop
     * @param categoryType
     */
    fun addDropData(drop: DropData, categoryType: Int) {
        val isBossType = isType("RaidBoss") || isType("GrandBoss")

        synchronized(_categories) {
            // Category exists, stores the drop and return.
            for (cat in _categories) {
                if (cat.categoryType == categoryType) {
                    cat.addDropData(drop, isBossType)
                    return
                }
            }

            // Category doesn't exist, create and store it.
            val cat = DropCategory(categoryType)
            cat.addDropData(drop, isBossType)

            _categories.add(cat)
        }
    }

    fun canTeach(classId: ClassId): Boolean {
        return _teachInfo != null && _teachInfo!!.contains(if (classId.level() == 3) classId.parent else classId)
    }

    fun getSkills(type: SkillType): List<L2Skill> {
        return (_skills as java.util.Map<SkillType, List<L2Skill>>).getOrDefault(type, emptyList())
    }

    fun addSkills(skills: List<L2Skill>) {
        for (skill in skills) {
            if (skill.isPassive) {
                addSkill(SkillType.PASSIVE, skill)
                continue
            }

            if (skill.isSuicideAttack) {
                addSkill(SkillType.SUICIDE, skill)
                continue
            }

            if (skill.skillType == L2SkillType.BUFF || skill.skillType == L2SkillType.CONT || skill.skillType == L2SkillType.REFLECT) {
                addSkill(SkillType.BUFF, skill)
                continue
            }
            else if (skill.skillType == L2SkillType.HEAL || skill.skillType == L2SkillType.HOT || skill.skillType == L2SkillType.HEAL_PERCENT || skill.skillType == L2SkillType.HEAL_STATIC || skill.skillType == L2SkillType.BALANCE_LIFE || skill.skillType == L2SkillType.MANARECHARGE || skill.skillType == L2SkillType.MANAHEAL_PERCENT) {
                addSkill(SkillType.HEAL, skill)
                continue
            }
            else if (skill.skillType == L2SkillType.DEBUFF || skill.skillType == L2SkillType.ROOT || skill.skillType == L2SkillType.SLEEP || skill.skillType == L2SkillType.STUN || skill.skillType == L2SkillType.PARALYZE || skill.skillType == L2SkillType.POISON || skill.skillType == L2SkillType.DOT || skill.skillType == L2SkillType.MDOT || skill.skillType == L2SkillType.BLEED || skill.skillType == L2SkillType.MUTE || skill.skillType == L2SkillType.FEAR || skill.skillType == L2SkillType.CANCEL || skill.skillType == L2SkillType.NEGATE || skill.skillType == L2SkillType.WEAKNESS || skill.skillType == L2SkillType.AGGDEBUFF) {
                addSkill(SkillType.DEBUFF, skill)
                continue
            }
            else if (skill.skillType == L2SkillType.PDAM || skill.skillType == L2SkillType.MDAM || skill.skillType == L2SkillType.BLOW || skill.skillType == L2SkillType.DRAIN || skill.skillType == L2SkillType.CHARGEDAM || skill.skillType == L2SkillType.FATAL || skill.skillType == L2SkillType.DEATHLINK || skill.skillType == L2SkillType.MANADAM || skill.skillType == L2SkillType.CPDAMPERCENT || skill.skillType == L2SkillType.GET_PLAYER || skill.skillType == L2SkillType.INSTANT_JUMP || skill.skillType == L2SkillType.AGGDAMAGE) {
                addSkill(if (skill.castRange > 150) SkillType.LONG_RANGE else SkillType.SHORT_RANGE, skill)
                continue
            }
            // _log.warning(skill.getName() + " skill wasn't added due to specific logic."); TODO
        }
    }

    fun addSkill(type: SkillType, skill: L2Skill) {
        var list: MutableList<L2Skill>? = _skills[type]
        if (list == null) {
            list = ArrayList(5)
            list.add(skill)

            _skills[type] = list
        } else
            list.add(skill)
    }

    fun getEventQuests(EventType: EventType): List<Quest> {
        return _questEvents[EventType] ?: emptyList()
    }

    fun addQuestEvent(type: EventType, quest: Quest) {
        var list: MutableList<Quest>? = _questEvents[type]
        if (list == null) {
            list = ArrayList(5)
            list.add(quest)

            _questEvents[type] = list
        } else {
            list.remove(quest)

            if (type.isMultipleRegistrationAllowed || list.isEmpty())
                list.add(quest)
        }
    }
}