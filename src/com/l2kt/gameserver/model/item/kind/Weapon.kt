package com.l2kt.gameserver.model.item.kind

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.handler.SkillHandler
import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.holder.IntIntHolder
import com.l2kt.gameserver.model.item.type.WeaponType
import com.l2kt.gameserver.scripting.EventType
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.skills.Formulas
import com.l2kt.gameserver.skills.conditions.Condition
import com.l2kt.gameserver.skills.conditions.ConditionGameChance
import com.l2kt.gameserver.templates.StatsSet
import com.l2kt.gameserver.templates.skills.L2SkillType
import java.util.*

/**
 * This class is dedicated to the management of weapons.
 */
class Weapon
/**
 * Constructor for Weapon.<BR></BR>
 * <BR></BR>
 * <U><I>Variables filled :</I></U>
 * <UL>
 * <LI>_soulShotCount & _spiritShotCount</LI>
 * <LI>_pDam & _mDam & _rndDam</LI>
 * <LI>_critical</LI>
 * <LI>_hitModifier</LI>
 * <LI>_avoidModifier</LI>
 * <LI>_shieldDes & _shieldDefRate</LI>
 * <LI>_atkSpeed & _AtkReuse</LI>
 * <LI>_mpConsume</LI>
 * <LI>_isMagical</LI>
</UL> *
 * @param set : StatsSet designating the set of couples (key,value) caracterizing the armor
 * @see Item constructor
 */
    (set: StatsSet) : Item(set) {
    /**
     * @return the type of weapon.
     */
    override val itemType: WeaponType?
    /**
     * @return the random damage inflicted by the weapon
     */
    val randomDamage: Int
    /**
     * @return the quantity of SoulShot used.
     */
    val soulShotCount: Int
    /**
     * @return the quatity of SpiritShot used.
     */
    val spiritShotCount: Int
    private val _mpConsume: Int
    private val _mpConsumeReduceRate: Int
    private val _mpConsumeReduceValue: Int
    /**
     * @return true or false if weapon is considered as a mage weapon.
     */
    val isMagical: Boolean

    private var _enchant4Skill: IntIntHolder? = null // skill that activates when item is enchanted +4 (for duals)

    // Attached skills for Special Abilities
    private var _skillsOnCast: IntIntHolder? = null
    private var _skillsOnCastCondition: Condition? = null
    private var _skillsOnCrit: IntIntHolder? = null
    private var _skillsOnCritCondition: Condition? = null

    /**
     * @return the Reuse Delay of the Weapon.
     */
    val reuseDelay: Int

    /**
     * @return the reduced quantity of SoultShot used.
     */
    val reducedSoulShot: Int
    /**
     * @return the chance to use Reduced SoultShot.
     */
    val reducedSoulShotChance: Int

    /**
     * @return the ID of the Etc item after applying the mask.
     */
    override val itemMask: Int
        get() = itemType!!.mask()

    /**
     * @return the MP consumption of the weapon.
     */
    val mpConsume: Int
        get() = if (_mpConsumeReduceRate > 0 && Rnd[100] < _mpConsumeReduceRate) _mpConsumeReduceValue else _mpConsume

    /**
     * @return The skill player obtains when he equiped weapon +4 or more (for duals SA)
     */
    val enchant4Skill: L2Skill?
        get() = if (_enchant4Skill == null) null else _enchant4Skill!!.skill

    init {
        itemType = WeaponType.valueOf(set.getString("weapon_type", "none")!!.toUpperCase())
        type1 = Item.TYPE1_WEAPON_RING_EARRING_NECKLACE
        type2 = Item.TYPE2_WEAPON
        soulShotCount = set.getInteger("soulshots", 0)
        spiritShotCount = set.getInteger("spiritshots", 0)
        randomDamage = set.getInteger("random_damage", 0)
        _mpConsume = set.getInteger("mp_consume", 0)
        val reduce = set.getString("mp_consume_reduce", "0,0")!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
        _mpConsumeReduceRate = Integer.parseInt(reduce[0])
        _mpConsumeReduceValue = Integer.parseInt(reduce[1])
        reuseDelay = set.getInteger("reuse_delay", 0)
        isMagical = set.getBool("is_magical", false)

        val reduced_soulshots =
            set.getString("reduced_soulshot", "")!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        reducedSoulShotChance = if (reduced_soulshots.size == 2) Integer.parseInt(reduced_soulshots[0]) else 0
        reducedSoulShot = if (reduced_soulshots.size == 2) Integer.parseInt(reduced_soulshots[1]) else 0

        var skill = set.getString("enchant4_skill", null)
        if (skill != null) {
            val info = skill.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            if (info != null && info.size == 2) {
                var id = 0
                var level = 0
                try {
                    id = Integer.parseInt(info[0])
                    level = Integer.parseInt(info[1])
                } catch (nfe: Exception) {
                    // Incorrect syntax, dont add new skill
                    _log.info("> Couldnt parse " + skill + " in weapon enchant skills! item " + toString())
                }

                if (id > 0 && level > 0)
                    _enchant4Skill = IntIntHolder(id, level)
            }
        }

        skill = set.getString("oncast_skill", null)
        if (skill != null) {
            val info = skill.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val infochance = set.getString("oncast_chance", null)
            if (info != null && info.size == 2) {
                var id = 0
                var level = 0
                var chance = 0
                try {
                    id = Integer.parseInt(info[0])
                    level = Integer.parseInt(info[1])
                    if (infochance != null)
                        chance = Integer.parseInt(infochance)
                } catch (nfe: Exception) {
                    // Incorrect syntax, dont add new skill
                    _log.info("> Couldnt parse " + skill + " in weapon oncast skills! item " + toString())
                }

                if (id > 0 && level > 0 && chance > 0) {
                    _skillsOnCast = IntIntHolder(id, level)
                    if (infochance != null)
                        _skillsOnCastCondition = ConditionGameChance(chance)
                }
            }
        }

        skill = set.getString("oncrit_skill", null)
        if (skill != null) {
            val info = skill.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            val infochance = set.getString("oncrit_chance", null)
            if (info != null && info.size == 2) {
                var id = 0
                var level = 0
                var chance = 0
                try {
                    id = Integer.parseInt(info[0])
                    level = Integer.parseInt(info[1])
                    if (infochance != null)
                        chance = Integer.parseInt(infochance)
                } catch (nfe: Exception) {
                    // Incorrect syntax, dont add new skill
                    _log.info("> Couldnt parse " + skill + " in weapon oncrit skills! item " + toString())
                }

                if (id > 0 && level > 0 && chance > 0) {
                    _skillsOnCrit = IntIntHolder(id, level)
                    if (infochance != null)
                        _skillsOnCritCondition = ConditionGameChance(chance)
                }
            }
        }
    }

    /**
     * @param caster : Creature pointing out the caster
     * @param target : Creature pointing out the target
     * @param crit : boolean tells whether the hit was critical
     * @return An array of L2Effect of skills associated with the item to be triggered onHit.
     */
    fun getSkillEffects(caster: Creature, target: Creature, crit: Boolean): List<L2Effect> {
        if (_skillsOnCrit == null || !crit)
            return emptyList()

        val effects = ArrayList<L2Effect>()

        if (_skillsOnCritCondition != null) {
            val env = Env()
            env.character = caster
            env.target = target
            env.skill = _skillsOnCrit!!.skill

            if (!_skillsOnCritCondition!!.test(env))
                return emptyList()
        }

        val shld = Formulas.calcShldUse(caster, target, _skillsOnCrit!!.skill)
        if (!Formulas.calcSkillSuccess(caster, target, _skillsOnCrit!!.skill!!, shld, false))
            return emptyList()

        if (target.getFirstEffect(_skillsOnCrit!!.skill!!.id) != null)
            target.getFirstEffect(_skillsOnCrit!!.skill!!.id).exit()

        for (e in _skillsOnCrit!!.skill!!.getEffects(caster, target, Env(shld, false, false, false)))
            effects.add(e)

        return effects
    }

    /**
     * @param caster : Creature pointing out the caster
     * @param target : Creature pointing out the target
     * @param trigger : L2Skill pointing out the skill triggering this action
     * @return An array of L2Effect associated with the item to be triggered onCast.
     */
    fun getSkillEffects(caster: Creature, target: Creature, trigger: L2Skill): List<L2Effect> {
        if (_skillsOnCast == null)
            return emptyList()

        // Trigger only same type of skill.
        if (trigger.isOffensive != _skillsOnCast!!.skill!!.isOffensive)
            return emptyList()

        // No buffing with toggle or not magic skills.
        if ((trigger.isToggle || !trigger.isMagic) && _skillsOnCast!!.skill!!.skillType === L2SkillType.BUFF)
            return emptyList()

        if (_skillsOnCastCondition != null) {
            val env = Env()
            env.character = caster
            env.target = target
            env.skill = _skillsOnCast!!.skill

            if (!_skillsOnCastCondition!!.test(env))
                return emptyList()
        }

        val shld = Formulas.calcShldUse(caster, target, _skillsOnCast!!.skill)
        if (_skillsOnCast!!.skill!!.isOffensive && !Formulas.calcSkillSuccess(
                caster,
                target,
                _skillsOnCast!!.skill!!,
                shld,
                false
            )
        )
            return emptyList()

        // Get the skill handler corresponding to the skill type
        val handler = SkillHandler.getHandler(_skillsOnCast!!.skill!!.skillType)

        val targets = arrayOf(target as WorldObject)

        // Launch the magic skill and calculate its effects
        if (handler != null)
            handler.useSkill(caster, _skillsOnCast!!.skill!!, targets)
        else
            _skillsOnCast!!.skill!!.useSkill(caster, targets)

        // notify quests of a skill use
        if (caster is Player) {
            // Mobs in range 1000 see spell
            for (npcMob in caster.getKnownTypeInRadius(Npc::class.java, 1000)) {
                val scripts = npcMob.template.getEventQuests(EventType.ON_SKILL_SEE)
                if (scripts != null)
                    for (quest in scripts)
                        quest.notifySkillSee(npcMob, caster, _skillsOnCast!!.skill!!, targets, false)
            }
        }
        return emptyList()
    }
}