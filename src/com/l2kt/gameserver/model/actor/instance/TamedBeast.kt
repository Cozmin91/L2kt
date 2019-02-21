package com.l2kt.gameserver.model.actor.instance

import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.model.actor.template.NpcTemplate.SkillType
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.network.serverpackets.NpcSay
import com.l2kt.gameserver.network.serverpackets.SocialAction
import com.l2kt.gameserver.templates.skills.L2SkillType
import java.util.concurrent.Future

/**
 * A tamed beast behaves a lot like a pet and has an owner. Some points :
 *
 *  * feeding another beast to level 4 will vanish your actual tamed beast.
 *  * running out of spices will vanish your actual tamed beast. There's a 1min food check timer.
 *  * running out of the Beast Farm perimeter will vanish your tamed beast.
 *  * no need to force attack it, it's a normal monster.
 *
 * This class handles the running tasks (such as skills use and feed) of the mob.
 */
class TamedBeast(
    objectId: Int,
    template: NpcTemplate,
    protected var _owner: Player?,
    protected var _foodId: Int,
    loc: Location
) : FeedableBeast(objectId, template) {

    private var _aiTask: Future<*>? = null

    init {

        disableCoreAI(true)
        currentHp = maxHp.toDouble()
        currentMp = maxMp.toDouble()
        title = _owner?.name ?: ""
        _owner?.trainedBeast = this

        // Generate AI task.
        _aiTask = ThreadPool.scheduleAtFixedRate(AiTask(), TASK_INTERVAL.toLong(), TASK_INTERVAL.toLong())

        spawnMe(loc)
    }

    override fun doDie(killer: Creature): Boolean {
        if (!super.doDie(killer))
            return false

        // Stop AI task.
        if (_aiTask != null) {
            _aiTask!!.cancel(true)
            _aiTask = null
        }

        // Clean up actual trained beast.
        if (_owner != null)
            _owner!!.trainedBeast = null

        return true
    }

    override fun deleteMe() {
        // Stop AI task.
        if (_aiTask != null) {
            _aiTask!!.cancel(true)
            _aiTask = null
        }

        stopHpMpRegeneration()
        ai.stopFollow()

        // Clean up actual trained beast.
        if (_owner != null)
            _owner!!.trainedBeast = null

        super.deleteMe()
    }

    /**
     * Notification triggered by the owner when the owner is attacked.<br></br>
     * Tamed mobs will heal/recharge or debuff the enemy according to their skills.
     * @param attacker
     */
    fun onOwnerGotAttacked(attacker: Creature) {
        // Check if the owner is no longer around. If so, despawn.
        if (_owner == null || !_owner!!.isOnline) {
            deleteMe()
            return
        }

        // If the owner is dead or if the tamed beast is currently casting a spell,do nothing.
        if (_owner!!.isDead || isCastingNow)
            return

        val proba = Rnd[3]

        // Heal, 33% luck.
        if (proba == 0) {
            // Happen only when owner's HPs < 50%
            val HPRatio = _owner!!.currentHp.toFloat() / _owner!!.maxHp
            if (HPRatio < 0.5) {
                for (skill in template.getSkills(SkillType.HEAL)) {
                    when (skill.skillType) {
                        L2SkillType.HEAL, L2SkillType.HOT, L2SkillType.BALANCE_LIFE, L2SkillType.HEAL_PERCENT, L2SkillType.HEAL_STATIC -> {
                            sitCastAndFollow(skill, _owner!!)
                            return
                        }
                    }
                }
            }
        } else if (proba == 1) {
            for (skill in template.getSkills(SkillType.DEBUFF)) {
                // if the skill is a debuff, check if the attacker has it already
                if (attacker.getFirstEffect(skill) == null) {
                    sitCastAndFollow(skill, attacker)
                    return
                }
            }
        } else if (proba == 2) {
            // Happen only when owner's MPs < 50%
            val MPRatio = _owner!!.currentMp.toFloat() / _owner!!.maxMp
            if (MPRatio < 0.5) {
                for (skill in template.getSkills(SkillType.HEAL)) {
                    when (skill.skillType) {
                        L2SkillType.MANARECHARGE, L2SkillType.MANAHEAL_PERCENT -> {
                            sitCastAndFollow(skill, _owner!!)
                            return
                        }
                    }
                }
            }
        }// Recharge, 33% luck.
        // Debuff, 33% luck.
    }

    /**
     * Prepare and cast a skill:
     *
     *  * First, prepare the beast for casting, by abandoning other actions.
     *  * Next, call doCast in order to cast the spell.
     *  * Finally, return to auto-following the owner.
     *
     * @param skill The skill to cast.
     * @param target The benefactor of the skill.
     */
    protected fun sitCastAndFollow(skill: L2Skill?, target: Creature) {
        stopMove(null)
        ai.setIntention(CtrlIntention.IDLE)

        setTarget(target)
        doCast(skill)
        ai.setIntention(CtrlIntention.FOLLOW, _owner)
    }

    private inner class AiTask : Runnable {
        private var _step: Int = 0

        override fun run() {
            val owner = _owner

            // Check if the owner is no longer around. If so, despawn.
            if (owner == null || !owner.isOnline) {
                deleteMe()
                return
            }

            // Happens every 60s.
            if (++_step > 12) {
                // Verify first if the tamed beast is still in the good range. If not, delete it.
                if (!isInsideRadius(52335, -83086, MAX_DISTANCE_FROM_HOME, true)) {
                    deleteMe()
                    return
                }

                // Destroy the food from owner's inventory ; if none is found, delete the pet.
                if (!owner.destroyItemByItemId("BeastMob", _foodId, 1, this@TamedBeast, true)) {
                    deleteMe()
                    return
                }

                broadcastPacket(SocialAction(this@TamedBeast, 2))
                broadcastPacket(NpcSay(objectId, 0, npcId, Rnd[FOOD_CHAT]!!))

                _step = 0
            }

            // If the owner is dead or if the tamed beast is currently casting a spell,do nothing.
            if (owner.isDead || isCastingNow)
                return

            var totalBuffsOnOwner = 0
            var i = 0
            var buffToGive: L2Skill? = null

            val skills = template.getSkills(SkillType.BUFF)
            val rand = Rnd[skills.size]

            // Retrieve the random buff, and check how much tamed beast buffs the player has.
            for (skill in skills) {
                if (i == rand)
                    buffToGive = skill

                i++

                if (owner.getFirstEffect(skill) != null)
                    totalBuffsOnOwner++
            }

            // If the owner has less than 2 buffs, cast the chosen buff.
            if (totalBuffsOnOwner < 2 && owner.getFirstEffect(buffToGive) == null)
                sitCastAndFollow(buffToGive, owner)
            else
                ai.setIntention(CtrlIntention.FOLLOW, owner)
        }
    }

    companion object {
        private const val MAX_DISTANCE_FROM_HOME = 13000
        private const val TASK_INTERVAL = 5000

        // Messages used every minute by the tamed beast when he automatically eats food.
        protected val FOOD_CHAT = arrayOf(
            "Refills! Yeah!",
            "I am such a gluttonous beast, it is embarrassing! Ha ha.",
            "Your cooperative feeling has been getting better and better.",
            "I will help you!",
            "The weather is really good. Wanna go for a picnic?",
            "I really like you! This is tasty...",
            "If you do not have to leave this place, then I can help you.",
            "What can I help you with?",
            "I am not here only for food!",
            "Yam, yam, yam, yam, yam!"
        )
    }
}