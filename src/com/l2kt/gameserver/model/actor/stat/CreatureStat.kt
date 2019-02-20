package com.l2kt.gameserver.model.actor.stat

import com.l2kt.Config
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.skills.Stats

open class CreatureStat(open val activeChar: Creature) {

    open var exp: Long = 0
    open var sp = 0
    open var level: Byte = 1

    /**
     * @return the STR of the Creature (base+modifier).
     */
    val str: Int
        get() = calcStat(Stats.STAT_STR, activeChar!!.template.baseSTR.toDouble(), null, null).toInt()

    /**
     * @return the DEX of the Creature (base+modifier).
     */
    val dex: Int
        get() = calcStat(Stats.STAT_DEX, activeChar!!.template.baseDEX.toDouble(), null, null).toInt()

    /**
     * @return the CON of the Creature (base+modifier).
     */
    val con: Int
        get() = calcStat(Stats.STAT_CON, activeChar!!.template.baseCON.toDouble(), null, null).toInt()

    /**
     * @return the INT of the Creature (base+modifier).
     */
    val int: Int
        get() = calcStat(Stats.STAT_INT, activeChar!!.template.baseINT.toDouble(), null, null).toInt()

    /**
     * @return the MEN of the Creature (base+modifier).
     */
    val men: Int
        get() = calcStat(Stats.STAT_MEN, activeChar!!.template.baseMEN.toDouble(), null, null).toInt()

    /**
     * @return the WIT of the Creature (base+modifier).
     */
    val wit: Int
        get() = calcStat(Stats.STAT_WIT, activeChar!!.template.baseWIT.toDouble(), null, null).toInt()

    /**
     * @return the Accuracy (base+modifier) of the Creature in function of the Weapon Expertise Penalty.
     */
    open val accuracy: Int
        get() = calcStat(Stats.ACCURACY_COMBAT, 0.0, null, null).toInt()

    open val maxHp: Int
        get() = calcStat(Stats.MAX_HP, activeChar!!.template.getBaseHpMax(activeChar!!.level), null, null).toInt()

    open val maxCp: Int
        get() = 0

    open val maxMp: Int
        get() = calcStat(Stats.MAX_MP, activeChar!!.template.getBaseMpMax(activeChar!!.level), null, null).toInt()

    /**
     * @return the MAtk Speed (base+modifier) of the Creature in function of the Armour Expertise Penalty.
     */
    open val mAtkSpd: Int
        get() = calcStat(
            Stats.MAGIC_ATTACK_SPEED,
            333.0 * if (activeChar!!.isChampion) Config.CHAMPION_SPD_ATK else 1.0,
            null,
            null
        ).toInt()

    /**
     * @return the PAtk Speed (base+modifier) of the Creature in function of the Armour Expertise Penalty.
     */
    open val pAtkSpd: Int
        get() = calcStat(
            Stats.POWER_ATTACK_SPEED,
            activeChar!!.template.basePAtkSpd * if (activeChar!!.isChampion) Config.CHAMPION_SPD_ATK else 1.0,
            null,
            null
        ).toInt()

    /**
     * @return the Physical Attack range (base+modifier) of the Creature.
     */
    open val physicalAttackRange: Int
        get() = activeChar!!.attackType.range

    /**
     * @return the ShieldDef rate (base+modifier) of the Creature.
     */
    val shldDef: Int
        get() = calcStat(Stats.SHIELD_DEFENCE, 0.0, null, null).toInt()

    /**
     * Returns base running speed, given by owner template.<br></br>
     * Player is affected by mount type.
     * @return int : Base running speed.
     */
    open val baseRunSpeed: Int
        get() = activeChar!!.template.baseRunSpeed

    /**
     * Returns base walking speed, given by owner template.<br></br>
     * Player is affected by mount type.
     * @return int : Base walking speed.
     */
    val baseWalkSpeed: Int
        get() = activeChar!!.template.baseWalkSpeed

    /**
     * Returns base movement speed, given by owner template and owner movement status.<br></br>
     * Player is affected by mount type and by being in L2WaterZone.
     * @return int : Base walking speed.
     */
    protected val baseMoveSpeed: Int
        get() = if (activeChar!!.isRunning) baseRunSpeed else baseWalkSpeed

    /**
     * Returns movement speed multiplier, which is used by client to set correct character/object movement speed.
     * @return float : Movement speed multiplier.
     */
    val movementSpeedMultiplier: Float
        get() = moveSpeed / baseMoveSpeed

    /**
     * Returns attack speed multiplier, which is used by client to set correct character/object attack speed.
     * @return float : Attack speed multiplier.
     */
    val attackSpeedMultiplier: Float
        get() = (1.1 * pAtkSpd / activeChar!!.template.basePAtkSpd).toFloat()

    /**
     * Returns final movement speed, given by owner template, owner status and effects.<br></br>
     * L2Playable is affected by L2SwampZone.<br></br>
     * Player is affected by L2SwampZone and armor grade penalty.
     * @return float : Final movement speed.
     */
    open val moveSpeed: Float
        get() = calcStat(Stats.RUN_SPEED, baseMoveSpeed.toDouble(), null, null).toFloat()

    /**
     * Calculate the new value of the state with modifiers that will be applied on the targeted Creature.<BR></BR>
     * <BR></BR>
     * <B><U> Concept</U> :</B><BR></BR>
     * <BR></BR>
     * A Creature owns a table of Calculators called <B>_calculators</B>. Each Calculator (a calculator per state) own a table of Func object. A Func object is a mathematic function that permit to calculate the modifier of a state (ex : REGENERATE_HP_RATE...) : <BR></BR>
     * <BR></BR>
     * FuncAtkAccuracy -> Math.sqrt(_player.getDEX())*6+_player.getLevel()<BR></BR>
     * <BR></BR>
     * When the calc method of a calculator is activeCharlaunched, each mathematic function is called according to its priority <B>_order</B>. Indeed, Func with lowest priority order is executed firsta and Funcs with the same order are executed in unspecified order. The result of the calculation is stored in
     * the value property of an Env class instance.<BR></BR>
     * <BR></BR>
     * @param stat The stat to calculate the new value with modifiers
     * @param init The initial value of the stat before applying modifiers
     * @param target The L2Charcater whose properties will be used in the calculation (ex : CON, INT...)
     * @param skill The L2Skill whose properties will be used in the calculation (ex : Level...)
     * @return
     */
    fun calcStat(stat: Stats?, init: Double, target: Creature?, skill: L2Skill?): Double {
        if (activeChar == null || stat == null)
            return init

        val id = stat.ordinal

        val c = activeChar!!.calculators[id]
        if (c == null || c.size() == 0)
            return init

        // Create and init an Env object to pass parameters to the Calculator
        val env = Env()
        env.character = activeChar
        env.target = target
        env.skill = skill
        env.value = init

        // Launch the calculation
        c.calc(env)

        // avoid some troubles with negative stats (some stats should never be negative)
        if (env.value <= 0) {
            when (stat) {
                Stats.MAX_HP, Stats.MAX_MP, Stats.MAX_CP, Stats.MAGIC_DEFENCE, Stats.POWER_DEFENCE, Stats.POWER_ATTACK, Stats.MAGIC_ATTACK, Stats.POWER_ATTACK_SPEED, Stats.MAGIC_ATTACK_SPEED, Stats.SHIELD_DEFENCE, Stats.STAT_CON, Stats.STAT_DEX, Stats.STAT_INT, Stats.STAT_MEN, Stats.STAT_STR, Stats.STAT_WIT -> env.value =
                    1.0
            }
        }
        return env.value
    }

    /**
     * @param target
     * @param skill
     * @return the Critical Hit rate (base+modifier) of the Creature.
     */
    fun getCriticalHit(target: Creature?, skill: L2Skill?): Int {
        return Math.min(
            calcStat(
                Stats.CRITICAL_RATE,
                activeChar!!.template.baseCritRate.toDouble(),
                target,
                skill
            ).toInt(), 500
        )
    }

    /**
     * @param target
     * @param skill
     * @return the Magic Critical Hit rate (base+modifier) of the Creature.
     */
    fun getMCriticalHit(target: Creature?, skill: L2Skill?): Int {
        return calcStat(Stats.MCRITICAL_RATE, 8.0, target, skill).toInt()
    }

    /**
     * @param target
     * @return the Attack Evasion rate (base+modifier) of the Creature.
     */
    open fun getEvasionRate(target: Creature?): Int {
        return calcStat(Stats.EVASION_RATE, 0.0, target, null).toInt()
    }

    /**
     * @param target The Creature targeted by the skill
     * @param skill The L2Skill used against the target
     * @return the MAtk (base+modifier) of the Creature for a skill used in function of abnormal effects in progress.
     */
    open fun getMAtk(target: Creature?, skill: L2Skill?): Int {
        var attack = activeChar!!.template.baseMAtk * if (activeChar!!.isChampion) Config.CHAMPION_ATK else 1.0

        // Add the power of the skill to the attack effect
        if (skill != null)
            attack += skill.power

        // Calculate modifiers Magic Attack
        return calcStat(Stats.MAGIC_ATTACK, attack, target, skill).toInt()
    }

    /**
     * @param target The Creature targeted by the skill
     * @param skill The L2Skill used against the target
     * @return the MDef (base+modifier) of the Creature against a skill in function of abnormal effects in progress.
     */
    open fun getMDef(target: Creature?, skill: L2Skill?): Int {
        // Calculate modifiers Magic Attack
        return calcStat(
            Stats.MAGIC_DEFENCE,
            activeChar!!.template.baseMDef * if (activeChar!!.isRaidRelated) Config.RAID_DEFENCE_MULTIPLIER else 1.0,
            target,
            skill
        ).toInt()
    }

    /**
     * @param target
     * @return the PAtk (base+modifier) of the Creature.
     */
    open fun getPAtk(target: Creature?): Int {
        return calcStat(
            Stats.POWER_ATTACK,
            activeChar!!.template.basePAtk * if (activeChar!!.isChampion) Config.CHAMPION_ATK else 1.0,
            target,
            null
        ).toInt()
    }

    /**
     * @param target
     * @return the PDef (base+modifier) of the Creature.
     */
    open fun getPDef(target: Creature?): Int {
        return calcStat(
            Stats.POWER_DEFENCE,
            activeChar!!.template.basePDef * if (activeChar!!.isRaidRelated) Config.RAID_DEFENCE_MULTIPLIER else 1.0,
            target,
            null
        ).toInt()
    }

    /**
     * @param target
     * @return the PAtk Modifier against animals.
     */
    fun getPAtkAnimals(target: Creature?): Double {
        return calcStat(Stats.PATK_ANIMALS, 1.0, target, null)
    }

    /**
     * @param target
     * @return the PAtk Modifier against dragons.
     */
    fun getPAtkDragons(target: Creature?): Double {
        return calcStat(Stats.PATK_DRAGONS, 1.0, target, null)
    }

    /**
     * @param target
     * @return the PAtk Modifier against insects.
     */
    fun getPAtkInsects(target: Creature?): Double {
        return calcStat(Stats.PATK_INSECTS, 1.0, target, null)
    }

    /**
     * @param target
     * @return the PAtk Modifier against monsters.
     */
    fun getPAtkMonsters(target: Creature?): Double {
        return calcStat(Stats.PATK_MONSTERS, 1.0, target, null)
    }

    /**
     * @param target
     * @return the PAtk Modifier against plants.
     */
    fun getPAtkPlants(target: Creature?): Double {
        return calcStat(Stats.PATK_PLANTS, 1.0, target, null)
    }

    /**
     * @param target
     * @return the PAtk Modifier against giants.
     */
    fun getPAtkGiants(target: Creature?): Double {
        return calcStat(Stats.PATK_GIANTS, 1.0, target, null)
    }

    /**
     * @param target
     * @return the PAtk Modifier against magic creatures
     */
    fun getPAtkMagicCreatures(target: Creature?): Double {
        return calcStat(Stats.PATK_MCREATURES, 1.0, target, null)
    }

    /**
     * @param target
     * @return the PDef Modifier against animals.
     */
    fun getPDefAnimals(target: Creature?): Double {
        return calcStat(Stats.PDEF_ANIMALS, 1.0, target, null)
    }

    /**
     * @param target
     * @return the PDef Modifier against dragons.
     */
    fun getPDefDragons(target: Creature?): Double {
        return calcStat(Stats.PDEF_DRAGONS, 1.0, target, null)
    }

    /**
     * @param target
     * @return the PDef Modifier against insects.
     */
    fun getPDefInsects(target: Creature?): Double {
        return calcStat(Stats.PDEF_INSECTS, 1.0, target, null)
    }

    /**
     * @param target
     * @return the PDef Modifier against monsters.
     */
    fun getPDefMonsters(target: Creature?): Double {
        return calcStat(Stats.PDEF_MONSTERS, 1.0, target, null)
    }

    /**
     * @param target
     * @return the PDef Modifier against plants.
     */
    fun getPDefPlants(target: Creature?): Double {
        return calcStat(Stats.PDEF_PLANTS, 1.0, target, null)
    }

    /**
     * @param target
     * @return the PDef Modifier against giants.
     */
    fun getPDefGiants(target: Creature?): Double {
        return calcStat(Stats.PDEF_GIANTS, 1.0, target, null)
    }

    /**
     * @param target
     * @return the PDef Modifier against giants.
     */
    fun getPDefMagicCreatures(target: Creature?): Double {
        return calcStat(Stats.PDEF_MCREATURES, 1.0, target, null)
    }

    /**
     * @param skill
     * @return the mpConsume.
     */
    fun getMpConsume(skill: L2Skill?): Int {
        if (skill == null)
            return 1

        var mpConsume = skill.mpConsume.toDouble()
        if (skill.isDance) {
            if (activeChar != null && activeChar!!.danceCount > 0)
                mpConsume += (activeChar!!.danceCount * skill.nextDanceMpCost).toDouble()
        }

        if (skill.isDance)
            return calcStat(Stats.DANCE_MP_CONSUME_RATE, mpConsume, null, null).toInt()

        return if (skill.isMagic) calcStat(Stats.MAGICAL_MP_CONSUME_RATE, mpConsume, null, null).toInt() else calcStat(
            Stats.PHYSICAL_MP_CONSUME_RATE,
            mpConsume,
            null,
            null
        ).toInt()

    }

    /**
     * @param skill
     * @return the mpInitialConsume.
     */
    fun getMpInitialConsume(skill: L2Skill?): Int {
        if (skill == null)
            return 1

        val mpConsume = skill.mpInitialConsume.toDouble()

        if (skill.isDance)
            return calcStat(Stats.DANCE_MP_CONSUME_RATE, mpConsume, null, null).toInt()

        return if (skill.isMagic) calcStat(Stats.MAGICAL_MP_CONSUME_RATE, mpConsume, null, null).toInt() else calcStat(
            Stats.PHYSICAL_MP_CONSUME_RATE,
            mpConsume,
            null,
            null
        ).toInt()

    }

    fun getAttackElementValue(attackAttribute: Byte): Int {
        when (attackAttribute.toInt()) {
            1 // wind
            -> return calcStat(Stats.WIND_POWER, 0.0, null, null).toInt()
            2 // fire
            -> return calcStat(Stats.FIRE_POWER, 0.0, null, null).toInt()
            3 // water
            -> return calcStat(Stats.WATER_POWER, 0.0, null, null).toInt()
            4 // earth
            -> return calcStat(Stats.EARTH_POWER, 0.0, null, null).toInt()
            5 // holy
            -> return calcStat(Stats.HOLY_POWER, 0.0, null, null).toInt()
            6 // dark
            -> return calcStat(Stats.DARK_POWER, 0.0, null, null).toInt()
            else -> return 0
        }
    }

    fun getDefenseElementValue(defenseAttribute: Byte): Double {
        when (defenseAttribute.toInt()) {
            1 // wind
            -> return calcStat(Stats.WIND_RES, 1.0, null, null)
            2 // fire
            -> return calcStat(Stats.FIRE_RES, 1.0, null, null)
            3 // water
            -> return calcStat(Stats.WATER_RES, 1.0, null, null)
            4 // earth
            -> return calcStat(Stats.EARTH_RES, 1.0, null, null)
            5 // holy
            -> return calcStat(Stats.HOLY_RES, 1.0, null, null)
            6 // dark
            -> return calcStat(Stats.DARK_RES, 1.0, null, null)
            else -> return 1.0
        }
    }
}