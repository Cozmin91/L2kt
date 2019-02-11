package com.l2kt.gameserver.templates.skills

import java.lang.reflect.Constructor

import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.templates.StatsSet
import com.l2kt.gameserver.skills.l2skills.L2SkillAppearance
import com.l2kt.gameserver.skills.l2skills.L2SkillChargeDmg
import com.l2kt.gameserver.skills.l2skills.L2SkillCreateItem
import com.l2kt.gameserver.skills.l2skills.L2SkillDefault
import com.l2kt.gameserver.skills.l2skills.L2SkillDrain
import com.l2kt.gameserver.skills.l2skills.L2SkillSeed
import com.l2kt.gameserver.skills.l2skills.L2SkillSiegeFlag
import com.l2kt.gameserver.skills.l2skills.L2SkillSignet
import com.l2kt.gameserver.skills.l2skills.L2SkillSignetCasttime
import com.l2kt.gameserver.skills.l2skills.L2SkillSpawn
import com.l2kt.gameserver.skills.l2skills.L2SkillSummon
import com.l2kt.gameserver.skills.l2skills.L2SkillTeleport

/**
 * @author nBd
 */
enum class L2SkillType {
    // Damage
    PDAM,
    FATAL,
    MDAM,
    CPDAMPERCENT,
    MANADAM,
    DOT,
    MDOT,
    DRAIN_SOUL,
    DRAIN(L2SkillDrain::class.java),
    DEATHLINK,
    BLOW,
    SIGNET(L2SkillSignet::class.java),
    SIGNET_CASTTIME(L2SkillSignetCasttime::class.java),
    SEED(L2SkillSeed::class.java),

    // Disablers
    BLEED,
    POISON,
    STUN,
    ROOT,
    CONFUSION,
    FEAR,
    SLEEP,
    MUTE,
    PARALYZE,
    WEAKNESS,

    // hp, mp, cp
    HEAL,
    MANAHEAL,
    COMBATPOINTHEAL,
    HOT,
    MPHOT,
    CPHOT,
    BALANCE_LIFE,
    HEAL_STATIC,
    MANARECHARGE,
    HEAL_PERCENT,
    MANAHEAL_PERCENT,

    GIVE_SP,

    // Aggro
    AGGDAMAGE,
    AGGREDUCE,
    AGGREMOVE,
    AGGREDUCE_CHAR,
    AGGDEBUFF,

    // Fishing
    FISHING,
    PUMPING,
    REELING,

    // MISC
    UNLOCK,
    UNLOCK_SPECIAL,
    DELUXE_KEY_UNLOCK,
    ENCHANT_ARMOR,
    ENCHANT_WEAPON,
    SOULSHOT,
    SPIRITSHOT,
    SIEGEFLAG(L2SkillSiegeFlag::class.java),
    TAKECASTLE,
    WEAPON_SA,
    SOW,
    HARVEST,
    GET_PLAYER,
    DUMMY,
    INSTANT_JUMP,

    // Creation
    COMMON_CRAFT,
    DWARVEN_CRAFT,
    CREATE_ITEM(L2SkillCreateItem::class.java),
    EXTRACTABLE,
    EXTRACTABLE_FISH,

    // Summons
    SUMMON(L2SkillSummon::class.java),
    FEED_PET,
    DEATHLINK_PET,
    STRSIEGEASSAULT,
    ERASE,
    BETRAY,
    SPAWN(L2SkillSpawn::class.java),

    // Cancel
    CANCEL,
    MAGE_BANE,
    WARRIOR_BANE,

    NEGATE,
    CANCEL_DEBUFF,

    BUFF,
    DEBUFF,
    PASSIVE,
    CONT,

    RESURRECT,
    CHARGEDAM(L2SkillChargeDmg::class.java),
    MHOT,
    DETECT_WEAKNESS,
    LUCK,
    RECALL(L2SkillTeleport::class.java),
    TELEPORT(L2SkillTeleport::class.java),
    SUMMON_FRIEND,
    REFLECT,
    SPOIL,
    SWEEP,
    FAKE_DEATH,
    UNBLEED,
    UNPOISON,
    UNDEAD_DEFENSE,
    BEAST_FEED,
    FUSION,

    CHANGE_APPEARANCE(L2SkillAppearance::class.java),

    COREDONE,

    NOTDONE;

    private val _class: Class<out L2Skill>

    fun makeSkill(set: StatsSet): L2Skill {
        try {
            val c = _class.getConstructor(StatsSet::class.java)
            return c.newInstance(set)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }

    constructor() {
        _class = L2SkillDefault::class.java
    }

    constructor(classType: Class<out L2Skill>) {
        _class = classType
    }
}