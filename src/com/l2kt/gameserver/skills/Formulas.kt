package com.l2kt.gameserver.skills

import com.l2kt.Config
import com.l2kt.commons.logging.CLogger
import com.l2kt.commons.math.MathUtil
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.manager.CastleManager
import com.l2kt.gameserver.data.manager.ZoneManager
import com.l2kt.gameserver.instancemanager.ClanHallManager
import com.l2kt.gameserver.instancemanager.SevenSigns.CabalType
import com.l2kt.gameserver.instancemanager.SevenSignsFestival
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.Playable
import com.l2kt.gameserver.model.actor.instance.Cubic
import com.l2kt.gameserver.model.actor.instance.Door
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.actor.template.NpcTemplate.Race.*
import com.l2kt.gameserver.model.entity.ClanHall
import com.l2kt.gameserver.model.entity.Siege
import com.l2kt.gameserver.model.item.kind.Armor
import com.l2kt.gameserver.model.item.type.WeaponType.*
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.model.zone.type.MotherTreeZone
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.skills.effects.EffectTemplate
import com.l2kt.gameserver.taskmanager.GameTimeTaskManager
import com.l2kt.gameserver.templates.skills.L2SkillType

object Formulas {
    internal val LOGGER = CLogger(Formulas::class.java.name)

    private const val HP_REGENERATE_PERIOD = 3000 // 3 secs

    const val SHIELD_DEFENSE_FAILED: Byte = 0 // no shield defense
    const val SHIELD_DEFENSE_SUCCEED: Byte = 1 // normal shield defense
    const val SHIELD_DEFENSE_PERFECT_BLOCK: Byte = 2 // perfect block

    const val SKILL_REFLECT_FAILED: Byte = 0 // no reflect
    const val SKILL_REFLECT_SUCCEED: Byte = 1 // normal reflect, some damage reflected some other not
    const val SKILL_REFLECT_VENGEANCE: Byte = 2 // 100% of the damage affect both

    private const val MELEE_ATTACK_RANGE: Byte = 40

    const val MAX_STAT_VALUE = 100

    private val STR_COMPUTE = doubleArrayOf(1.036, 34.845)
    private val INT_COMPUTE = doubleArrayOf(1.020, 31.375)
    private val DEX_COMPUTE = doubleArrayOf(1.009, 19.360)
    private val WIT_COMPUTE = doubleArrayOf(1.050, 20.000)
    private val CON_COMPUTE = doubleArrayOf(1.030, 27.632)
    private val MEN_COMPUTE = doubleArrayOf(1.010, -0.060)

    val WIT_BONUS = DoubleArray(MAX_STAT_VALUE)
    val MEN_BONUS = DoubleArray(MAX_STAT_VALUE)
    val INT_BONUS = DoubleArray(MAX_STAT_VALUE)
    val STR_BONUS = DoubleArray(MAX_STAT_VALUE)
    val DEX_BONUS = DoubleArray(MAX_STAT_VALUE)
    val CON_BONUS = DoubleArray(MAX_STAT_VALUE)

    val BASE_EVASION_ACCURACY = DoubleArray(MAX_STAT_VALUE)

    internal val SQRT_MEN_BONUS = DoubleArray(MAX_STAT_VALUE)
    internal val SQRT_CON_BONUS = DoubleArray(MAX_STAT_VALUE)

    private val karmaMods = doubleArrayOf(
        0.0,
        0.772184315,
        2.069377971,
        2.315085083,
        2.467800843,
        2.514219611,
        2.510075822,
        2.532083418,
        2.473028945,
        2.377178509,
        2.285526643,
        2.654635163,
        2.963434737,
        3.266100461,
        3.561112664,
        3.847320291,
        4.123878064,
        4.390194136,
        4.645886341,
        4.890745518,
        5.124704707,
        6.97914069,
        7.270620642,
        7.548951721,
        7.81438302,
        8.067235867,
        8.307889422,
        8.536768399,
        8.754332624,
        8.961068152,
        9.157479758,
        11.41901707,
        11.64989746,
        11.87007991,
        12.08015809,
        12.28072687,
        12.47237891,
        12.65570177,
        12.83127553,
        12.99967093,
        13.16144786,
        15.6563607,
        15.84513182,
        16.02782135,
        16.20501182,
        16.37727218,
        16.54515749,
        16.70920885,
        16.86995336,
        17.02790439,
        17.18356182,
        19.85792061,
        20.04235517,
        20.22556446,
        20.40806338,
        20.59035551,
        20.77293378,
        20.95628115,
        21.1408714,
        21.3271699,
        21.51563446,
        24.3895427,
        24.61486587,
        24.84389213,
        25.07711247,
        25.31501442,
        25.55808296,
        25.80680152,
        26.06165297,
        26.32312062,
        26.59168923,
        26.86784604,
        27.15208178,
        27.44489172,
        27.74677676,
        28.05824444,
        28.37981005,
        28.71199773,
        29.05534154,
        29.41038662,
        29.77769028
    )

    init {
        for (i in STR_BONUS.indices)
            STR_BONUS[i] = Math.floor(Math.pow(STR_COMPUTE[0], i - STR_COMPUTE[1]) * 100 + .5) / 100
        for (i in INT_BONUS.indices)
            INT_BONUS[i] = Math.floor(Math.pow(INT_COMPUTE[0], i - INT_COMPUTE[1]) * 100 + .5) / 100
        for (i in DEX_BONUS.indices)
            DEX_BONUS[i] = Math.floor(Math.pow(DEX_COMPUTE[0], i - DEX_COMPUTE[1]) * 100 + .5) / 100
        for (i in WIT_BONUS.indices)
            WIT_BONUS[i] = Math.floor(Math.pow(WIT_COMPUTE[0], i - WIT_COMPUTE[1]) * 100 + .5) / 100
        for (i in CON_BONUS.indices)
            CON_BONUS[i] = Math.floor(Math.pow(CON_COMPUTE[0], i - CON_COMPUTE[1]) * 100 + .5) / 100
        for (i in MEN_BONUS.indices)
            MEN_BONUS[i] = Math.floor(Math.pow(MEN_COMPUTE[0], i - MEN_COMPUTE[1]) * 100 + .5) / 100

        for (i in BASE_EVASION_ACCURACY.indices)
            BASE_EVASION_ACCURACY[i] = Math.sqrt(i.toDouble()) * 6

        // Precompute square root values
        for (i in SQRT_CON_BONUS.indices)
            SQRT_CON_BONUS[i] = Math.sqrt(CON_BONUS[i])
        for (i in SQRT_MEN_BONUS.indices)
            SQRT_MEN_BONUS[i] = Math.sqrt(MEN_BONUS[i])
    }

    /**
     * @param cha The character to make checks on.
     * @return the period between 2 regenerations task (3s for Creature, 5 min for L2DoorInstance).
     */
    @JvmStatic fun getRegeneratePeriod(cha: Creature): Int {
        return if (cha is Door) HP_REGENERATE_PERIOD * 100 else HP_REGENERATE_PERIOD // 5 mins

// 3s
    }

    /**
     * @param cha The character to make checks on.
     * @return the HP regen rate (base + modifiers).
     */
    @JvmStatic fun calcHpRegen(cha: Creature): Double {
        var init = cha.template.baseHpReg
        var hpRegenMultiplier = if (cha.isRaidRelated) Config.RAID_HP_REGEN_MULTIPLIER else Config.HP_REGEN_MULTIPLIER
        var hpRegenBonus = 0.0

        if (cha.isChampion)
            hpRegenMultiplier *= Config.CHAMPION_HP_REGEN

        if (cha is Player) {

            // Calculate correct baseHpReg value for certain level of PC
            init += if (cha.level > 10) (cha.level - 1) / 10.0 else 0.5

            // SevenSigns Festival modifier
            if (SevenSignsFestival.getInstance().isFestivalInProgress && cha.isFestivalParticipant)
                hpRegenMultiplier *= calcFestivalRegenModifier(cha)
            else if (calcSiegeRegenModifer(cha))
                hpRegenMultiplier *= 1.5

            if (cha.isInsideZone(ZoneId.CLAN_HALL) && cha.clan != null) {
                val clanHallIndex = cha.clan.hideoutId
                if (clanHallIndex > 0) {
                    val clansHall = ClanHallManager.getInstance().getClanHallById(clanHallIndex)
                    if (clansHall != null)
                        if (clansHall.getFunction(ClanHall.FUNC_RESTORE_HP) != null)
                            hpRegenMultiplier *= (1 + clansHall.getFunction(ClanHall.FUNC_RESTORE_HP).lvl / 100).toDouble()
                }
            }

            // Mother Tree effect is calculated at last
            if (cha.isInsideZone(ZoneId.MOTHER_TREE)) {
                val zone = ZoneManager.getZone(cha, MotherTreeZone::class.java)
                val hpBonus = zone?.hpRegenBonus ?: 0
                hpRegenBonus += hpBonus.toDouble()
            }

            // Calculate Movement bonus
            if (cha.isSitting)
                hpRegenMultiplier *= 1.5 // Sitting
            else if (!cha.isMoving)
                hpRegenMultiplier *= 1.1 // Staying
            else if (cha.isRunning)
                hpRegenMultiplier *= 0.7 // Running
        }
        // Add CON bonus
        init *= cha.levelMod * CON_BONUS[cha.con]

        if (init < 1)
            init = 1.0

        return cha.calcStat(Stats.REGENERATE_HP_RATE, init, null, null) * hpRegenMultiplier + hpRegenBonus
    }

    /**
     * @param cha The character to make checks on.
     * @return the MP regen rate (base + modifiers).
     */
    @JvmStatic fun calcMpRegen(cha: Creature): Double {
        var init = cha.template.baseMpReg
        var mpRegenMultiplier = if (cha.isRaidRelated) Config.RAID_MP_REGEN_MULTIPLIER else Config.MP_REGEN_MULTIPLIER
        var mpRegenBonus = 0.0

        if (cha is Player) {

            // Calculate correct baseMpReg value for certain level of PC
            init += 0.3 * ((cha.level - 1) / 10.0)

            // SevenSigns Festival modifier
            if (SevenSignsFestival.getInstance().isFestivalInProgress && cha.isFestivalParticipant)
                mpRegenMultiplier *= calcFestivalRegenModifier(cha)

            // Mother Tree effect is calculated at last
            if (cha.isInsideZone(ZoneId.MOTHER_TREE)) {
                val zone = ZoneManager.getZone(cha, MotherTreeZone::class.java)
                val mpBonus = zone?.mpRegenBonus ?: 0
                mpRegenBonus += mpBonus.toDouble()
            }

            if (cha.isInsideZone(ZoneId.CLAN_HALL) && cha.clan != null) {
                val clanHallIndex = cha.clan.hideoutId
                if (clanHallIndex > 0) {
                    val clansHall = ClanHallManager.getInstance().getClanHallById(clanHallIndex)
                    if (clansHall != null)
                        if (clansHall.getFunction(ClanHall.FUNC_RESTORE_MP) != null)
                            mpRegenMultiplier *= (1 + clansHall.getFunction(ClanHall.FUNC_RESTORE_MP).lvl / 100).toDouble()
                }
            }

            // Calculate Movement bonus
            if (cha.isSitting)
                mpRegenMultiplier *= 1.5 // Sitting
            else if (!cha.isMoving)
                mpRegenMultiplier *= 1.1 // Staying
            else if (cha.isRunning)
                mpRegenMultiplier *= 0.7 // Running
        }
        // Add MEN bonus
        init *= cha.levelMod * MEN_BONUS[cha.men]

        if (init < 1)
            init = 1.0

        return cha.calcStat(Stats.REGENERATE_MP_RATE, init, null, null) * mpRegenMultiplier + mpRegenBonus
    }

    /**
     * @param player The player to make checks on.
     * @return the CP regen rate (base + modifiers).
     */
    @JvmStatic fun calcCpRegen(player: Player): Double {
        // Calculate correct baseHpReg value for certain level of PC
        var init = player.template.baseHpReg + if (player.level > 10) (player.level - 1) / 10.0 else 0.5
        var cpRegenMultiplier = Config.CP_REGEN_MULTIPLIER

        // Calculate Movement bonus
        if (player.isSitting)
            cpRegenMultiplier *= 1.5 // Sitting
        else if (!player.isMoving)
            cpRegenMultiplier *= 1.1 // Staying
        else if (player.isRunning)
            cpRegenMultiplier *= 0.7 // Running

        // Apply CON bonus
        init *= player.levelMod * CON_BONUS[player.con]

        if (init < 1)
            init = 1.0

        return player.calcStat(Stats.REGENERATE_CP_RATE, init, null, null) * cpRegenMultiplier
    }

    @JvmStatic fun calcFestivalRegenModifier(player: Player): Double {
        val festivalInfo = SevenSignsFestival.getInstance().getFestivalForPlayer(player)
        val festivalId = festivalInfo[1]

        // If the player isn't found in the festival, leave the regen rate as it is.
        if (festivalId < 0)
            return 1.0

        val festivalCenter: IntArray

        // Retrieve the X and Y coords for the center of the festival arena the player is in.
        val oracle = CabalType.VALUES[festivalInfo[0]]
        if (oracle == CabalType.DAWN)
            festivalCenter = SevenSignsFestival.FESTIVAL_DAWN_PLAYER_SPAWNS[festivalId]
        else
            festivalCenter = SevenSignsFestival.FESTIVAL_DUSK_PLAYER_SPAWNS[festivalId]

        // Check the distance between the player and the player spawn point, in the center of the arena.
        val distToCenter = player.getPlanDistanceSq(festivalCenter[0], festivalCenter[1])

        if (Config.DEVELOPER)
            LOGGER.info(
                "calcFestivalRegenModifier() distance: {}, RegenMulti: {}.",
                distToCenter,
                String.format("%1.2f", 1.0 - distToCenter * 0.0005)
            )

        return 1.0 - distToCenter * 0.0005 // Maximum Decreased Regen of ~ -65%;
    }

    /**
     * @param player the player to test on.
     * @return true if the player is near one of his clan HQ (+50% regen boost).
     */
    @JvmStatic fun calcSiegeRegenModifer(player: Player?): Boolean {
        if (player == null)
            return false

        val clan = player.clan ?: return false

        val siege = CastleManager.getActiveSiege(player)
        return if (siege == null || !siege.checkSide(
                clan,
                Siege.SiegeSide.ATTACKER
            )
        ) false else MathUtil.checkIfInRange(
            200,
            player,
            clan.flag,
            true
        )

    }

    /**
     * @param attacker The attacker, from where the blow comes from.
     * @param target The victim of the blow.
     * @param skill The skill used.
     * @param shld True if victim was wearign a shield.
     * @param ss True if ss were activated.
     * @return blow damage based on cAtk
     */
    @JvmStatic fun calcBlowDamage(attacker: Creature, target: Creature, skill: L2Skill, shld: Byte, ss: Boolean): Double {
        var defence = target.getPDef(attacker).toDouble()
        when (shld) {
            SHIELD_DEFENSE_SUCCEED -> defence += target.shldDef.toDouble()

            SHIELD_DEFENSE_PERFECT_BLOCK // perfect block
            -> return 1.0
        }

        val isPvP = attacker is Playable && target is Playable

        var power = skill.power
        var damage = 0.0
        damage += calcValakasAttribute(attacker, target, skill)

        if (ss) {
            damage *= 2.0

            if (skill.ssBoost > 0)
                power *= skill.ssBoost.toDouble()
        }

        damage += power
        damage *= attacker.calcStat(Stats.CRITICAL_DAMAGE, 1.0, target, skill)
        damage *= (attacker.calcStat(Stats.CRITICAL_DAMAGE_POS, 1.0, target, skill) - 1) / 2 + 1
        damage += attacker.calcStat(Stats.CRITICAL_DAMAGE_ADD, 0.0, target, skill) * 6.5
        damage *= target.calcStat(Stats.CRIT_VULN, 1.0, target, skill)

        // get the vulnerability for the instance due to skills (buffs, passives, toggles, etc)
        damage = target.calcStat(Stats.DAGGER_WPN_VULN, damage, target, null)
        damage *= 70.0 / defence

        // Random weapon damage
        damage *= attacker.randomDamageMultiplier

        // Dmg bonusses in PvP fight
        if (isPvP)
            damage *= attacker.calcStat(Stats.PVP_PHYS_SKILL_DMG, 1.0, null, null)

        return if (damage < 1) 1.0 else damage
    }

    /**
     * Calculated damage caused by ATTACK of attacker on target, called separatly for each weapon, if dual-weapon is used.
     * @param attacker player or NPC that makes ATTACK
     * @param target player or NPC, target of ATTACK
     * @param skill skill used.
     * @param shld target was using a shield or not.
     * @param crit if the ATTACK have critical success
     * @param ss if weapon item was charged by soulshot
     * @return damage points
     */
    @JvmStatic
    fun calcPhysDam(
        attacker: Creature,
        target: Creature,
        skill: L2Skill?,
        shld: Byte,
        crit: Boolean,
        ss: Boolean
    ): Double {
        if (attacker is Player) {
            if (attacker.isGM && !attacker.accessLevel.canGiveDamage)
                return 0.0
        }

        var defence = target.getPDef(attacker).toDouble()
        when (shld) {
            SHIELD_DEFENSE_SUCCEED -> defence += target.shldDef.toDouble()

            SHIELD_DEFENSE_PERFECT_BLOCK // perfect block
            -> return 1.0
        }

        val isPvP = attacker is Playable && target is Playable
        var damage = attacker.getPAtk(target).toDouble()

        damage += calcValakasAttribute(attacker, target, skill)

        if (ss)
            damage *= 2.0

        if (skill != null) {
            var skillpower = skill.getPower(attacker)

            val ssBoost = skill.ssBoost
            if (ssBoost > 0 && ss)
                skillpower *= ssBoost.toDouble()

            damage += skillpower
        }

        // defence modifier depending of the attacker weapon
        val weapon = attacker.activeWeaponItem
        var stat: Stats? = null
        if (weapon != null) {
            when (weapon.itemType) {
                BOW -> stat = Stats.BOW_WPN_VULN

                BLUNT -> stat = Stats.BLUNT_WPN_VULN

                BIGSWORD -> stat = Stats.BIGSWORD_WPN_VULN

                BIGBLUNT -> stat = Stats.BIGBLUNT_WPN_VULN

                DAGGER -> stat = Stats.DAGGER_WPN_VULN

                DUAL -> stat = Stats.DUAL_WPN_VULN

                DUALFIST -> stat = Stats.DUALFIST_WPN_VULN

                POLE -> stat = Stats.POLE_WPN_VULN

                SWORD -> stat = Stats.SWORD_WPN_VULN
            }
        }

        if (crit) {
            // Finally retail like formula
            damage = 2.0 * attacker.calcStat(Stats.CRITICAL_DAMAGE, 1.0, target, skill) *
                    attacker.calcStat(Stats.CRITICAL_DAMAGE_POS, 1.0, target, skill) *
                    target.calcStat(Stats.CRIT_VULN, 1.0, target, null) * (70 * damage / defence)
            // Crit dmg add is almost useless in normal hits...
            damage += attacker.calcStat(Stats.CRITICAL_DAMAGE_ADD, 0.0, target, skill) * 70 / defence
        } else
            damage = 70 * damage / defence

        if (stat != null)
            damage = target.calcStat(stat, damage, target, null)

        // Weapon random damage ; invalid for CHARGEDAM skills.
        if (skill == null || skill.effectType !== L2SkillType.CHARGEDAM)
            damage *= attacker.randomDamageMultiplier

        if (target is Npc) {
            val multiplier: Double
            when (target.template.race) {
                BEAST -> {
                    multiplier = 1 + (attacker.getPAtkMonsters(target) - target.getPDefMonsters(target)) / 100
                    damage *= multiplier
                }

                ANIMAL -> {
                    multiplier = 1 + (attacker.getPAtkAnimals(target) - target.getPDefAnimals(target)) / 100
                    damage *= multiplier
                }

                PLANT -> {
                    multiplier = 1 + (attacker.getPAtkPlants(target) - target.getPDefPlants(target)) / 100
                    damage *= multiplier
                }

                DRAGON -> {
                    multiplier = 1 + (attacker.getPAtkDragons(target) - target.getPDefDragons(target)) / 100
                    damage *= multiplier
                }

                BUG -> {
                    multiplier = 1 + (attacker.getPAtkInsects(target) - target.getPDefInsects(target)) / 100
                    damage *= multiplier
                }

                GIANT -> {
                    multiplier = 1 + (attacker.getPAtkGiants(target) - target.getPDefGiants(target)) / 100
                    damage *= multiplier
                }

                MAGICCREATURE -> {
                    multiplier = 1 + (attacker.getPAtkMagicCreatures(target) - target.getPDefMagicCreatures(target)) /
                            100
                    damage *= multiplier
                }
            }
        }

        if (damage > 0 && damage < 1)
            damage = 1.0
        else if (damage < 0)
            damage = 0.0

        // Dmg bonuses in PvP fight
        if (isPvP) {
            if (skill == null)
                damage *= attacker.calcStat(Stats.PVP_PHYSICAL_DMG, 1.0, null, null)
            else
                damage *= attacker.calcStat(Stats.PVP_PHYS_SKILL_DMG, 1.0, null, null)
        }

        // Weapon elemental damages
        damage += calcElemental(attacker, target, null)

        return damage
    }

    @JvmStatic fun calcMagicDam(
        attacker: Creature,
        target: Creature,
        skill: L2Skill,
        shld: Byte,
        ss: Boolean,
        bss: Boolean,
        mcrit: Boolean
    ): Double {
        if (attacker is Player) {
            if (attacker.isGM && !attacker.accessLevel.canGiveDamage)
                return 0.0
        }

        var mDef = target.getMDef(attacker, skill).toDouble()
        when (shld) {
            SHIELD_DEFENSE_SUCCEED -> mDef += target.shldDef.toDouble()

            SHIELD_DEFENSE_PERFECT_BLOCK // perfect block
            -> return 1.0
        }

        var mAtk = attacker.getMAtk(target, skill).toDouble()

        if (bss)
            mAtk *= 4.0
        else if (ss)
            mAtk *= 2.0

        var damage = 91 * Math.sqrt(mAtk) / mDef * skill.getPower(attacker)

        // Failure calculation
        if (Config.MAGIC_FAILURES && !calcMagicSuccess(attacker, target, skill)) {
            if (attacker is Player) {
                if (calcMagicSuccess(attacker, target, skill) && target.level - attacker.getLevel() <= 9) {
                    if (skill.skillType === L2SkillType.DRAIN)
                        attacker.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.DRAIN_HALF_SUCCESFUL))
                    else
                        attacker.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.ATTACK_FAILED))

                    damage /= 2.0
                } else {
                    attacker.sendPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(
                            target
                        ).addSkillName(skill)
                    )
                    damage = 1.0
                }
            }

            if (target is Player) {
                if (skill.skillType === L2SkillType.DRAIN)
                    target.sendPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.RESISTED_S1_DRAIN).addCharName(
                            attacker
                        )
                    )
                else
                    target.sendPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.RESISTED_S1_MAGIC).addCharName(
                            attacker
                        )
                    )
            }
        } else if (mcrit)
            damage *= 4.0

        // Pvp bonuses for dmg
        if (attacker is Playable && target is Playable) {
            if (skill.isMagic)
                damage *= attacker.calcStat(Stats.PVP_MAGICAL_DMG, 1.0, null, null)
            else
                damage *= attacker.calcStat(Stats.PVP_PHYS_SKILL_DMG, 1.0, null, null)
        }

        damage *= calcElemental(attacker, target, skill)

        return damage
    }

    @JvmStatic fun calcMagicDam(attacker: Cubic, target: Creature, skill: L2Skill, mcrit: Boolean, shld: Byte): Double {
        var mDef = target.getMDef(attacker.owner, skill).toDouble()
        when (shld) {
            SHIELD_DEFENSE_SUCCEED -> mDef += target.shldDef.toDouble()

            SHIELD_DEFENSE_PERFECT_BLOCK // perfect block
            -> return 1.0
        }

        var damage = 91 / mDef * skill.power
        val owner = attacker.owner

        // Failure calculation
        if (Config.MAGIC_FAILURES && !calcMagicSuccess(owner, target, skill)) {
            if (calcMagicSuccess(owner, target, skill) && target.level - skill.magicLevel <= 9) {
                if (skill.skillType === L2SkillType.DRAIN)
                    owner.sendPacket(SystemMessageId.DRAIN_HALF_SUCCESFUL)
                else
                    owner.sendPacket(SystemMessageId.ATTACK_FAILED)

                damage /= 2.0
            } else {
                owner.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.S1_RESISTED_YOUR_S2).addCharName(target).addSkillName(
                        skill
                    )
                )
                damage = 1.0
            }

            if (target is Player) {
                if (skill.skillType === L2SkillType.DRAIN)
                    target.sendPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.RESISTED_S1_DRAIN).addCharName(
                            owner
                        )
                    )
                else
                    target.sendPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.RESISTED_S1_MAGIC).addCharName(
                            owner
                        )
                    )
            }
        } else if (mcrit)
            damage *= 4.0

        damage *= calcElemental(owner, target, skill)

        return damage
    }

    /**
     * @param rate The value to make check on.
     * @return true in case of critical hit
     */
    @JvmStatic
    fun calcCrit(rate: Double): Boolean {
        return rate > Rnd[1000]
    }

    /**
     * Calcul value of blow success
     * @param attacker The character delaing the blow.
     * @param target The victim.
     * @param chance The base chance of landing a blow.
     * @return true if successful, false otherwise
     */
    @JvmStatic
    fun calcBlow(attacker: Creature, target: Creature, chance: Int): Boolean {
        return attacker.calcStat(Stats.BLOW_RATE, chance * (1.0 + (attacker.dex - 20) / 100), target, null) > Rnd[100]
    }

    /**
     * Calcul value of lethal chance
     * @param attacker The character delaing the blow.
     * @param target The victim.
     * @param baseLethal The base lethal chance of the skill.
     * @param magiclvl
     * @return
     */
    @JvmStatic
    fun calcLethal(attacker: Creature, target: Creature, baseLethal: Int, magiclvl: Int): Double {
        var chance = 0.0
        if (magiclvl > 0) {
            val delta = (magiclvl + attacker.level) / 2 - 1 - target.level

            if (delta >= -3)
                chance = baseLethal * (attacker.level.toDouble() / target.level)
            else if (delta < -3 && delta >= -9)
                chance = (-3 * (baseLethal / delta)).toDouble()
            else
                chance = (baseLethal / 15).toDouble()
        } else
            chance = baseLethal * (attacker.level.toDouble() / target.level)

        chance = 10 * attacker.calcStat(Stats.LETHAL_RATE, chance, target, null)

        if (Config.DEVELOPER)
            LOGGER.info("Current calcLethal: {} / 1000.", chance)

        return chance
    }

    @JvmStatic
    fun calcLethalHit(attacker: Creature, target: Creature, skill: L2Skill) {
        if (target.isRaidRelated || target is Door)
            return

        // If one of following IDs is found, return false (Tyrannosaurus x 3, Headquarters)
        if (target is Npc) {
            when (target.npcId) {
                22215, 22216, 22217, 35062 -> return
            }
        }

        // Second lethal effect (hp to 1 for npc, cp/hp to 1 for player).
        if (skill.lethalChance2 > 0 && Rnd[1000] < calcLethal(
                attacker,
                target,
                skill.lethalChance2,
                skill.magicLevel
            )
        ) {
            if (target is Npc)
                target.reduceCurrentHp(target.getCurrentHp() - 1, attacker, skill)
            else if (target is Player)
            // If is a active player set his HP and CP to 1
            {
                if (!target.isInvul) {
                    if (!(attacker is Player && attacker.isGM && !attacker.accessLevel.canGiveDamage)) {
                        target.currentHp = 1.0
                        target.currentCp = 1.0
                        target.sendPacket(SystemMessageId.LETHAL_STRIKE)
                    }
                }
            }
            attacker.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.LETHAL_STRIKE_SUCCESSFUL))
        } else if (skill.lethalChance1 > 0 && Rnd[1000] < calcLethal(
                attacker,
                target,
                skill.lethalChance1,
                skill.magicLevel
            )
        ) {
            if (target is Npc)
                target.reduceCurrentHp(target.getCurrentHp() / 2, attacker, skill)
            else if (target is Player) {
                if (!target.isInvul) {
                    if (!(attacker is Player && attacker.isGM && !attacker.accessLevel.canGiveDamage)) {
                        target.currentCp = 1.0
                        target.sendPacket(SystemMessageId.LETHAL_STRIKE)
                    }
                }
            }
            attacker.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.LETHAL_STRIKE_SUCCESSFUL))
        }// First lethal effect (hp/2 for npc, cp to 1 for player).
    }

    @JvmStatic
    fun calcMCrit(mRate: Int): Boolean {
        if (Config.DEVELOPER)
            LOGGER.info("Current mCritRate: {} / 1000.", mRate)

        return mRate > Rnd[1000]
    }

    /**
     * Check if casting process is canceled due to hit.
     * @param target The target to make checks on.
     * @param dmg The amount of dealt damages.
     */
    @JvmStatic
    fun calcCastBreak(target: Creature, dmg: Double) {
        // Don't go further for invul characters or raid bosses.
        if (target.isRaidRelated || target.isInvul)
            return

        // Break automatically the skill cast if under attack.
        if (target is Player && target.fusionSkill != null) {
            target.breakCast()
            return
        }

        // Initialization to 15% for magical skills ; don't go further for ppl casting a physical skill
        if (!target.isCastingNow && target.lastSkillCast != null && !target.lastSkillCast.isMagic)
            return

        // Calculate all modifiers for ATTACK_CANCEL ; chance to break is higher with higher dmg, and is affected by target MEN.
        var rate = target.calcStat(
            Stats.ATTACK_CANCEL,
            15 + Math.sqrt(13 * dmg) - (MEN_BONUS[target.men] * 100 - 100),
            null,
            null
        )

        // Adjust the rate to be between 1 and 99
        if (rate > 99)
            rate = 99.0
        else if (rate < 1)
            rate = 1.0

        if (Config.DEVELOPER)
            LOGGER.info("calcCastBreak rate: {}%.", rate.toInt())

        if (Rnd[100] < rate)
            target.breakCast()
    }

    /**
     * Calculate delay (in milliseconds) before next ATTACK.
     * @param attacker
     * @param target
     * @param rate
     * @return delay in ms.
     */
    @JvmStatic
    fun calcPAtkSpd(attacker: Creature, target: Creature, rate: Double): Int {
        return if (rate < 2) 2700 else (470000 / rate).toInt()

    }

    /**
     * Calculate delay (in milliseconds) for skills cast.
     * @param attacker
     * @param skill used to know if skill is magic or no.
     * @param skillTime
     * @return delay in ms.
     */
    @JvmStatic
    fun calcAtkSpd(attacker: Creature, skill: L2Skill, skillTime: Double): Int {
        return if (skill.isMagic) (skillTime * 333 / attacker.mAtkSpd).toInt() else (skillTime * 333 / attacker.pAtkSpd).toInt()

    }

    /**
     * Calculate the hit/miss chance.
     * @param attacker : The attacker to make checks on.
     * @param target : The target to make checks on.
     * @return true if hit is missed, false if it evaded.
     */
    @JvmStatic
    fun calcHitMiss(attacker: Creature, target: Creature): Boolean {
        var chance = (80 + 2 * (attacker.accuracy - target.getEvasionRate(attacker))) * 10

        var modifier = 100.0

        // Get high or low Z bonus.
        if (attacker.z - target.z > 50)
            modifier += 3.0
        else if (attacker.z - target.z < -50)
            modifier -= 3.0

        // Get weather bonus. TODO: rain support (-3%).
        if (GameTimeTaskManager.isNight)
            modifier -= 10.0

        // Get position bonus.
        if (attacker.isBehindTarget)
            modifier += 10.0
        else if (!attacker.isInFrontOfTarget)
            modifier += 5.0

        chance *= (modifier / 100).toInt()

        if (Config.DEVELOPER)
            LOGGER.info("calcHitMiss rate: {}%, modifier : x{}.", chance / 10, +modifier / 100)

        return Math.max(Math.min(chance, 980), 200) < Rnd[1000]
    }

    /**
     * Test the shield use.
     * @param attacker The attacker.
     * @param target The victim ; make check about his shield.
     * @param skill The skill the attacker has used.
     * @return 0 = shield defense doesn't succeed<br></br>
     * 1 = shield defense succeed<br></br>
     * 2 = perfect block
     */
    @JvmStatic
    fun calcShldUse(attacker: Creature, target: Creature, skill: L2Skill?): Byte {
        // Ignore shield skills types bypass the shield use.
        if (skill != null && skill.ignoreShield())
            return 0

        val item = target.secondaryWeaponItem
        if (item == null || item !is Armor)
            return 0

        var shldRate = target.calcStat(Stats.SHIELD_RATE, 0.0, attacker, null) * DEX_BONUS[target.dex]
        if (shldRate == 0.0)
            return 0

        val degreeside = target.calcStat(Stats.SHIELD_DEFENCE_ANGLE, 120.0, null, null).toInt()
        if (degreeside < 360 && !target.isFacing(attacker, degreeside))
            return 0

        var shldSuccess = SHIELD_DEFENSE_FAILED

        // if attacker use bow and target wear shield, shield block rate is multiplied by 1.3 (30%)
        if (attacker.attackType == BOW)
            shldRate *= 1.3

        if (shldRate > 0 && 100 - Config.PERFECT_SHIELD_BLOCK_RATE < Rnd[100])
            shldSuccess = SHIELD_DEFENSE_PERFECT_BLOCK
        else if (shldRate > Rnd[100])
            shldSuccess = SHIELD_DEFENSE_SUCCEED

        if (target is Player) {
            when (shldSuccess) {
                SHIELD_DEFENSE_SUCCEED -> target.sendPacket(SystemMessageId.SHIELD_DEFENCE_SUCCESSFULL)

                SHIELD_DEFENSE_PERFECT_BLOCK -> target.sendPacket(SystemMessageId.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS)
            }
        }

        return shldSuccess
    }

    @JvmStatic
    fun calcMagicAffected(actor: Creature, target: Creature, skill: L2Skill): Boolean {
        val type = skill.skillType
        if (target.isRaidRelated && !calcRaidAffected(type))
            return false

        var defence = 0.0

        if (skill.isActive && skill.isOffensive)
            defence = target.getMDef(actor, skill).toDouble()

        val attack = 2.0 * actor.getMAtk(target, skill).toDouble() * calcSkillVulnerability(actor, target, skill, type)
        var d = (attack - defence) / (attack + defence)

        d += 0.5 * Rnd.nextGaussian()
        return d > 0
    }

    @JvmStatic
    fun calcSkillVulnerability(attacker: Creature, target: Creature, skill: L2Skill, type: L2SkillType?): Double {
        var multiplier = 1.0

        // Get the elemental damages.
        if (skill.element > 0)
            multiplier *= Math.sqrt(calcElemental(attacker, target, skill))

        // Get the skillType to calculate its effect in function of base stats of the target.
        when (type) {
            L2SkillType.BLEED -> multiplier = target.calcStat(Stats.BLEED_VULN, multiplier, target, null)

            L2SkillType.POISON -> multiplier = target.calcStat(Stats.POISON_VULN, multiplier, target, null)

            L2SkillType.STUN -> multiplier = target.calcStat(Stats.STUN_VULN, multiplier, target, null)

            L2SkillType.PARALYZE -> multiplier = target.calcStat(Stats.PARALYZE_VULN, multiplier, target, null)

            L2SkillType.ROOT -> multiplier = target.calcStat(Stats.ROOT_VULN, multiplier, target, null)

            L2SkillType.SLEEP -> multiplier = target.calcStat(Stats.SLEEP_VULN, multiplier, target, null)

            L2SkillType.MUTE, L2SkillType.FEAR, L2SkillType.BETRAY, L2SkillType.AGGDEBUFF, L2SkillType.AGGREDUCE_CHAR, L2SkillType.ERASE, L2SkillType.CONFUSION -> multiplier =
                    target.calcStat(Stats.DERANGEMENT_VULN, multiplier, target, null)

            L2SkillType.DEBUFF, L2SkillType.WEAKNESS -> multiplier =
                    target.calcStat(Stats.DEBUFF_VULN, multiplier, target, null)

            L2SkillType.CANCEL -> multiplier = target.calcStat(Stats.CANCEL_VULN, multiplier, target, null)
        }

        // Return a multiplier (exemple with resist shock : 1 + (-0,4 stun vuln) = 0,6%
        return multiplier
    }

    private fun calcSkillStatModifier(type: L2SkillType, target: Creature): Double {
        var multiplier = 1.0

        when (type) {
            L2SkillType.STUN, L2SkillType.BLEED, L2SkillType.POISON -> multiplier = 2 - SQRT_CON_BONUS[target.stat.con]

            L2SkillType.SLEEP, L2SkillType.DEBUFF, L2SkillType.WEAKNESS, L2SkillType.ERASE, L2SkillType.ROOT, L2SkillType.MUTE, L2SkillType.FEAR, L2SkillType.BETRAY, L2SkillType.CONFUSION, L2SkillType.AGGREDUCE_CHAR, L2SkillType.PARALYZE -> multiplier = 2 -
                    SQRT_MEN_BONUS[target.stat.men]
        }

        return Math.max(0.0, multiplier)
    }

    @JvmStatic
    fun getSTRBonus(activeChar: Creature): Double {
        return STR_BONUS[activeChar.str]
    }

    private fun getLevelModifier(attacker: Creature, target: Creature, skill: L2Skill): Double {
        if (skill.levelDepend == 0)
            return 1.0

        val delta = (if (skill.magicLevel > 0) skill.magicLevel else attacker.level) + skill.levelDepend - target.level
        return 1 + (if (delta < 0) 0.01 else 0.005) * delta
    }

    private fun getMatkModifier(attacker: Creature, target: Creature, skill: L2Skill, bss: Boolean): Double {
        var mAtkModifier = 1.0

        if (skill.isMagic) {
            val mAtk = attacker.getMAtk(target, skill).toDouble()
            var `val` = mAtk
            if (bss)
                `val` = mAtk * 4.0

            mAtkModifier = Math.sqrt(`val`) / target.getMDef(attacker, skill) * 11.0
        }
        return mAtkModifier
    }

    @JvmStatic
    fun calcEffectSuccess(
        attacker: Creature,
        target: Creature,
        effect: EffectTemplate,
        skill: L2Skill,
        shld: Byte,
        bss: Boolean
    ): Boolean {
        if (shld == SHIELD_DEFENSE_PERFECT_BLOCK)
        // perfect block
            return false

        val type = effect.effectType
        val baseChance = effect.effectPower

        if (type == null)
            return Rnd[100] < baseChance

        if (type == L2SkillType.CANCEL)
        // CANCEL type lands always
            return true

        val statModifier = calcSkillStatModifier(type, target)
        val skillModifier = calcSkillVulnerability(attacker, target, skill, type)
        val mAtkModifier = getMatkModifier(attacker, target, skill, bss)
        val lvlModifier = getLevelModifier(attacker, target, skill)
        val rate = Math.max(1.0, Math.min(baseChance * statModifier * skillModifier * mAtkModifier * lvlModifier, 99.0))

        if (Config.DEVELOPER)
            LOGGER.info(
                "calcEffectSuccess(): name:{} eff.type:{} power:{} statMod:{} skillMod:{} mAtkMod:{} lvlMod:{} total:{}%.",
                skill.name,
                type.toString(),
                baseChance,
                String.format("%1.2f", statModifier),
                String.format("%1.2f", skillModifier),
                String.format("%1.2f", mAtkModifier),
                String.format("%1.2f", lvlModifier),
                String.format("%1.2f", rate)
            )

        return Rnd[100] < rate
    }

    @JvmStatic
    fun calcSkillSuccess(attacker: Creature, target: Creature, skill: L2Skill, shld: Byte, bss: Boolean): Boolean {
        if (shld == SHIELD_DEFENSE_PERFECT_BLOCK)
        // perfect block
            return false

        val type = skill.effectType

        if (target.isRaidRelated && !calcRaidAffected(type))
            return false

        val baseChance = skill.effectPower
        if (skill.ignoreResists())
            return Rnd[100] < baseChance

        val statModifier = calcSkillStatModifier(type, target)
        val skillModifier = calcSkillVulnerability(attacker, target, skill, type)
        val mAtkModifier = getMatkModifier(attacker, target, skill, bss)
        val lvlModifier = getLevelModifier(attacker, target, skill)
        val rate = Math.max(1.0, Math.min(baseChance * statModifier * skillModifier * mAtkModifier * lvlModifier, 99.0))

        if (Config.DEVELOPER)
            LOGGER.info(
                "calcSkillSuccess(): name:{} type:{} power:{} statMod:{} skillMod:{} mAtkMod:{} lvlMod:{} total:{}%.",
                skill.name,
                skill.skillType.toString(),
                baseChance,
                String.format("%1.2f", statModifier),
                String.format("%1.2f", skillModifier),
                String.format("%1.2f", mAtkModifier),
                String.format("%1.2f", lvlModifier),
                String.format("%1.2f", rate)
            )

        return Rnd[100] < rate
    }

    @JvmStatic
    fun calcCubicSkillSuccess(attacker: Cubic, target: Creature, skill: L2Skill, shld: Byte, bss: Boolean): Boolean {
        // if target reflect this skill then the effect will fail
        if (calcSkillReflect(target, skill) != SKILL_REFLECT_FAILED)
            return false

        if (shld == SHIELD_DEFENSE_PERFECT_BLOCK)
        // perfect block
            return false

        val type = skill.effectType

        if (target.isRaidRelated && !calcRaidAffected(type))
            return false

        val baseChance = skill.effectPower

        if (skill.ignoreResists())
            return Rnd[100] < baseChance

        var mAtkModifier = 1.0

        // Add Matk/Mdef Bonus
        if (skill.isMagic) {
            val mAtk = attacker.mAtk.toDouble()
            var `val` = mAtk
            if (bss)
                `val` = mAtk * 4.0

            mAtkModifier = Math.sqrt(`val`) / target.getMDef(null, null) * 11.0
        }

        val statModifier = calcSkillStatModifier(type, target)
        val skillModifier = calcSkillVulnerability(attacker.owner, target, skill, type)
        val lvlModifier = getLevelModifier(attacker.owner, target, skill)
        val rate = Math.max(1.0, Math.min(baseChance * statModifier * skillModifier * mAtkModifier * lvlModifier, 99.0))

        if (Config.DEVELOPER)
            LOGGER.info(
                "calcCubicSkillSuccess(): name:{} type:{} power:{} statMod:{} skillMod:{} mAtkMod:{} lvlMod:{} total:{}%.",
                skill.name,
                skill.skillType.toString(),
                baseChance,
                String.format("%1.2f", statModifier),
                String.format("%1.2f", skillModifier),
                String.format("%1.2f", mAtkModifier),
                String.format("%1.2f", lvlModifier),
                String.format("%1.2f", rate)
            )

        return Rnd[100] < rate
    }

    @JvmStatic
    fun calcMagicSuccess(attacker: Creature, target: Creature, skill: L2Skill): Boolean {
        val lvlDifference =
            target.level - ((if (skill.magicLevel > 0) skill.magicLevel else attacker.level) + skill.levelDepend)
        var rate = 100.0

        if (lvlDifference > 0)
            rate = Math.pow(1.166, lvlDifference.toDouble()) * 100

        if (attacker is Player && attacker.expertiseWeaponPenalty)
            rate += 6000.0

        if (Config.DEVELOPER)
            LOGGER.info(
                "calcMagicSuccess(): name:{} lvlDiff:{} fail:{}%.",
                skill.name,
                lvlDifference,
                String.format("%1.2f", rate / 100)
            )

        rate = Math.min(rate, 9900.0)

        return Rnd[10000] > rate
    }

    @JvmStatic
    fun calcManaDam(attacker: Creature, target: Creature, skill: L2Skill, ss: Boolean, bss: Boolean): Double {
        var mAtk = attacker.getMAtk(target, skill).toDouble()
        val mDef = target.getMDef(attacker, skill).toDouble()
        val mp = target.maxMp.toDouble()

        if (bss)
            mAtk *= 4.0
        else if (ss)
            mAtk *= 2.0

        var damage = Math.sqrt(mAtk) * skill.getPower(attacker) * (mp / 97) / mDef
        damage *= calcSkillVulnerability(attacker, target, skill, skill.skillType)
        return damage
    }

    @JvmStatic
    fun calculateSkillResurrectRestorePercent(baseRestorePercent: Double, caster: Creature): Double {
        if (baseRestorePercent == 0.0 || baseRestorePercent == 100.0)
            return baseRestorePercent

        var restorePercent = baseRestorePercent * WIT_BONUS[caster.wit]
        if (restorePercent - baseRestorePercent > 20.0)
            restorePercent += 20.0

        restorePercent = Math.max(restorePercent, baseRestorePercent)
        restorePercent = Math.min(restorePercent, 90.0)

        return restorePercent
    }

    @JvmStatic
    fun calcPhysicalSkillEvasion(target: Creature, skill: L2Skill): Boolean {
        return if (skill.isMagic) false else Rnd[100] < target.calcStat(Stats.P_SKILL_EVASION, 0.0, null, skill)

    }

    @JvmStatic
    fun calcSkillMastery(actor: Creature, sk: L2Skill): Boolean {
        // Pointless check for Creature other than players, as initial value will stay 0.
        if (actor !is Player)
            return false

        if (sk.skillType === L2SkillType.FISHING)
            return false

        var `val` = actor.stat.calcStat(Stats.SKILL_MASTERY, 0.0, null, null)

        if (actor.isMageClass)
            `val` *= INT_BONUS[actor.getINT()]
        else
            `val` *= STR_BONUS[actor.getSTR()]

        return Rnd[100] < `val`
    }

    @JvmStatic
    fun calcValakasAttribute(attacker: Creature, target: Creature, skill: L2Skill?): Double {
        var calcPower = 0.0
        var calcDefen = 0.0

        if (skill != null && skill.attributeName.contains("valakas")) {
            calcPower = attacker.calcStat(Stats.VALAKAS, calcPower, target, skill)
            calcDefen = target.calcStat(Stats.VALAKAS_RES, calcDefen, target, skill)
        } else {
            calcPower = attacker.calcStat(Stats.VALAKAS, calcPower, target, skill)
            if (calcPower > 0) {
                calcPower = attacker.calcStat(Stats.VALAKAS, calcPower, target, skill)
                calcDefen = target.calcStat(Stats.VALAKAS_RES, calcDefen, target, skill)
            }
        }
        return calcPower - calcDefen
    }

    /**
     * Calculate elemental modifier. There are 2 possible cases :
     *
     *  * the check emanates from a skill : the result will be a multiplier, including an amount of attacker element, and the target vuln/prof.
     *  * the check emanates from a weapon : the result is an addition of all elements, lowered/enhanced by the target vuln/prof
     *
     * @param attacker : The attacker used to retrieve elemental attacks.
     * @param target : The victim used to retrieve elemental protections.
     * @param skill : If different of null, it will be considered as a skill resist check.
     * @return A multiplier or a sum of damages.
     */
    @JvmStatic
    fun calcElemental(attacker: Creature, target: Creature, skill: L2Skill?): Double {
        if (skill != null) {
            val element = skill.element
            return if (element > 0) 1 + (attacker.getAttackElementValue(element) / 10.0 / 100.0 - (1 - target.getDefenseElementValue(
                element
            ))) else 1.0

        }

        var elemDamage = 0.0
        for (i in 1..6) {
            val attackerBonus = attacker.getAttackElementValue(i.toByte())
            elemDamage += Math.max(0.0, attackerBonus - attackerBonus * (target.getDefenseElementValue(i.toByte()) / 100.0))
        }
        return elemDamage
    }

    /**
     * Calculate skill reflection according to these three possibilities:
     *
     *  * Reflect failed
     *  * Normal reflect (just effects).
     *  * Vengeance reflect (100% damage reflected but damage is also dealt to actor).
     *
     * @param target : The skill's target.
     * @param skill : The skill to test.
     * @return SKILL_REFLECTED_FAILED, SKILL_REFLECT_SUCCEED or SKILL_REFLECT_VENGEANCE
     */
    @JvmStatic
    fun calcSkillReflect(target: Creature, skill: L2Skill): Byte {
        // Some special skills (like hero debuffs...) or ignoring resistances skills can't be reflected.
        if (skill.ignoreResists() || !skill.canBeReflected())
            return SKILL_REFLECT_FAILED

        // Only magic and melee skills can be reflected.
        if (!skill.isMagic && (skill.castRange == -1 || skill.castRange > MELEE_ATTACK_RANGE))
            return SKILL_REFLECT_FAILED

        var reflect = SKILL_REFLECT_FAILED

        // Check for non-reflected skilltypes, need additional retail check.
        when (skill.skillType) {
            L2SkillType.BUFF, L2SkillType.REFLECT, L2SkillType.HEAL_PERCENT, L2SkillType.MANAHEAL_PERCENT, L2SkillType.HOT, L2SkillType.CPHOT, L2SkillType.MPHOT, L2SkillType.UNDEAD_DEFENSE, L2SkillType.AGGDEBUFF, L2SkillType.CONT -> return SKILL_REFLECT_FAILED

            L2SkillType.PDAM, L2SkillType.BLOW, L2SkillType.MDAM, L2SkillType.DEATHLINK, L2SkillType.CHARGEDAM -> {
                val venganceChance = target.stat.calcStat(
                    if (skill.isMagic) Stats.VENGEANCE_SKILL_MAGIC_DAMAGE else Stats.VENGEANCE_SKILL_PHYSICAL_DAMAGE,
                    0.0,
                    target,
                    skill
                )
                if (venganceChance > Rnd[100])
                    reflect = (reflect.toInt() or SKILL_REFLECT_VENGEANCE.toInt()).toByte()
            }
        }

        val reflectChance = target.calcStat(
            if (skill.isMagic) Stats.REFLECT_SKILL_MAGIC else Stats.REFLECT_SKILL_PHYSIC,
            0.0,
            null,
            skill
        )
        if (Rnd[100] < reflectChance)
            reflect = (reflect.toInt() or SKILL_REFLECT_SUCCEED.toInt()).toByte()

        return reflect
    }

    /**
     * @param actor : The character affected.
     * @param fallHeight : The height the NPC fallen.
     * @return the damage, based on max HPs and falling height.
     */
    @JvmStatic
    fun calcFallDam(actor: Creature, fallHeight: Int): Double {
        return if (!Config.ENABLE_FALLING_DAMAGE || fallHeight < 0) 0.0 else actor.calcStat(
            Stats.FALL,
            (fallHeight * actor.maxHp / 1000).toDouble(),
            null,
            null
        )

    }

    /**
     * @param type : The L2SkillType to test.
     * @return true if the L2SkillType can affect a raid boss, false otherwise.
     */
    @JvmStatic
    fun calcRaidAffected(type: L2SkillType): Boolean {
        when (type) {
            L2SkillType.MANADAM, L2SkillType.MDOT -> return true

            L2SkillType.CONFUSION, L2SkillType.ROOT, L2SkillType.STUN, L2SkillType.MUTE, L2SkillType.FEAR, L2SkillType.DEBUFF, L2SkillType.PARALYZE, L2SkillType.SLEEP, L2SkillType.AGGDEBUFF, L2SkillType.AGGREDUCE_CHAR -> if (Rnd[1000] == 1)
                return true
        }
        return false
    }

    /**
     * Calculates karma lost upon death.
     * @param level : The level of the PKer.
     * @param exp : The amount of xp earned.
     * @return The amount of karma player has lost.
     */
    @JvmStatic
    fun calculateKarmaLost(level: Int, exp: Long): Int {
        return (exp.toDouble() / karmaMods[level] / 15.0).toInt()
    }

    /**
     * Calculates karma gain upon player kill.
     * @param pkCount : The current number of PK kills.
     * @param isSummon : Does the victim is a summon or no (lesser karma gain if true).
     * @return karma points that will be added to the player.
     */
    @JvmStatic
    fun calculateKarmaGain(pkCount: Int, isSummon: Boolean): Int {
        var result = 14400
        if (pkCount < 100)
            result = (((pkCount - 1) * 0.5 + 1) * 60 * 4).toInt()
        else if (pkCount < 180)
            result = (((pkCount + 1) * 0.125 + 37.5) * 60 * 4).toInt()

        if (isSummon)
            result = (pkCount and 3) + result shr 2

        return result
    }
}