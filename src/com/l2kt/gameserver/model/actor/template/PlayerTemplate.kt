package com.l2kt.gameserver.model.actor.template

import com.l2kt.commons.random.Rnd
import com.l2kt.commons.util.ArraysUtil
import com.l2kt.gameserver.data.ItemTable
import com.l2kt.gameserver.model.base.ClassId
import com.l2kt.gameserver.model.base.ClassRace
import com.l2kt.gameserver.model.base.Sex
import com.l2kt.gameserver.model.holder.skillnode.GeneralSkillNode
import com.l2kt.gameserver.model.item.kind.Weapon
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.templates.StatsSet

/**
 * A datatype extending [CreatureTemplate], used to retain Player template informations such as classId, specific collision values for female, hp/mp/cp tables, etc.<br></br>
 * <br></br>
 * Since each PlayerTemplate is associated to a [ClassId], it is also used as a container for [GeneralSkillNode]s this class can use.<br></br>
 * <br></br>
 * Finally, it holds starter equipment (under an int array of itemId) and initial spawn [Location] for newbie templates.
 */
class PlayerTemplate(set: StatsSet) : CreatureTemplate(set) {
    val classId: ClassId = ClassId.VALUES[set.getInteger("id")]

    val fallHeight: Int = set.getInteger("falling_height", 333)

    val baseSwimSpeed: Int = set.getInteger("swimSpd", 1)

    private val _collisionRadiusFemale: Double = set.getDouble("radiusFemale")
    private val _collisionHeightFemale: Double = set.getDouble("heightFemale")

    private val _spawnLocations: MutableList<Location> = set.getList("spawnLocations")

    val classBaseLevel: Int = set.getInteger("baseLvl")

    private val _hpTable: DoubleArray = set.getDoubleArray("hpTable")
    private val _mpTable: DoubleArray = set.getDoubleArray("mpTable")
    private val _cpTable: DoubleArray = set.getDoubleArray("cpTable")

    /**
     * @return the itemIds of all the starter equipment under an integer array.
     */
    val itemIds: IntArray = set.getIntegerArray("items", ArraysUtil.EMPTY_INT_ARRAY)
    /**
     * @return the [List] of all available [GeneralSkillNode] for this [PlayerTemplate].
     */
    val skills: MutableList<GeneralSkillNode> = set.getList("skills")

    /**
     * @return the [Weapon] used as fists for this [PlayerTemplate].
     */
    val fists: Weapon = ItemTable.getTemplate(set.getInteger("fists")) as Weapon

    val race: ClassRace
        get() = classId.race!!

    val className: String
        get() = classId.toString()

    val randomSpawn: Location
        get() {
            val loc = Rnd[_spawnLocations]
            return loc ?: Location.DUMMY_LOC
        }

    /**
     * @param sex
     * @return : height depends on sex.
     */
    fun getCollisionRadiusBySex(sex: Sex): Double {
        return if (sex === Sex.MALE) collisionRadius else _collisionRadiusFemale
    }

    /**
     * @param sex
     * @return : height depends on sex.
     */
    fun getCollisionHeightBySex(sex: Sex): Double {
        return if (sex === Sex.MALE) collisionHeight else _collisionHeightFemale
    }

    override fun getBaseHpMax(level: Int): Double {
        return _hpTable[level - 1]
    }

    override fun getBaseMpMax(level: Int): Double {
        return _mpTable[level - 1]
    }

    fun getBaseCpMax(level: Int): Double {
        return _cpTable[level - 1]
    }

    /**
     * Find if the skill exists on skill tree.
     * @param id : The skill id to check.
     * @param level : The skill level to check.
     * @return the associated [GeneralSkillNode] if a matching id/level is found on this [PlayerTemplate], or null.
     */
    fun findSkill(id: Int, level: Int): GeneralSkillNode? {
        return skills.stream().filter { s -> s.id == id && s.value == level }.findFirst().orElse(null)
    }
}