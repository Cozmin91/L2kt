package com.l2kt.gameserver.scripting.scripts.ai

import com.l2kt.Config
import com.l2kt.commons.math.MathUtil
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.data.xml.NpcData
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.ai.CtrlEvent
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.instance.Monster
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.actor.instance.RiftInvader
import com.l2kt.gameserver.network.serverpackets.MagicSkillUse
import com.l2kt.gameserver.scripting.EventType
import com.l2kt.gameserver.scripting.Quest
import java.util.*

open class L2AttackableAIScript : Quest {
    constructor() : super(-1, "ai") {

        registerNpcs()
    }

    constructor(name: String) : super(-1, name) {

        registerNpcs()
    }

    protected open fun registerNpcs() {
        // register all mobs here...
        for (template in NpcData.allNpcs) {
            try {
                if (Attackable::class.java.isAssignableFrom(Class.forName("com.l2kt.gameserver.model.actor.instance." + template.type))) {
                    template.addQuestEvent(EventType.ON_ATTACK, this)
                    template.addQuestEvent(EventType.ON_KILL, this)
                    template.addQuestEvent(EventType.ON_SPAWN, this)
                    template.addQuestEvent(EventType.ON_SKILL_SEE, this)
                    template.addQuestEvent(EventType.ON_FACTION_CALL, this)
                    template.addQuestEvent(EventType.ON_AGGRO, this)
                }
            } catch (e: ClassNotFoundException) {
                Quest.LOGGER.error("An unknown template type {} has been found on {}.", e, template.type, toString())
            }

        }
    }

    override fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        return null
    }

    override fun onSpellFinished(npc: Npc, player: Player?, skill: L2Skill?): String? {
        return null
    }

    override fun onSkillSee(
        npc: Npc,
        caster: Player?,
        skill: L2Skill?,
        targets: Array<WorldObject>,
        isPet: Boolean
    ): String? {
        if (caster == null)
            return null

        if (npc !is Attackable)
            return null

        var skillAggroPoints = skill?.aggroPoints ?: 0

        if (caster.pet != null) {
            if (targets.size == 1 && targets.contains(caster.pet))
                skillAggroPoints = 0
        }

        if (skillAggroPoints > 0) {
            if (npc.hasAI() && npc.ai.desire.intention == CtrlIntention.ATTACK) {
                val npcTarget = npc.target
                for (skillTarget in targets) {
                    if (npcTarget === skillTarget || npc === skillTarget) {
                        val originalCaster = if (isPet) caster.pet else caster
                        npc.addDamageHate(originalCaster, 0, skillAggroPoints * 150 / (npc.level + 7))
                    }
                }
            }
        }
        return null
    }

    override fun onFactionCall(npc: Npc?, caller: Npc?, attacker: Player?, isPet: Boolean): String? {
        if (attacker == null)
            return null

        if (caller is RiftInvader && attacker.isInParty && attacker.party!!.isInDimensionalRift && !attacker.party!!.dimensionalRift.isInCurrentRoomZone(
                npc
            )
        )
            return null

        val attackable = npc as Attackable
        val originalAttackTarget = if (isPet) attacker.pet else attacker

        // Add the target to the actor _aggroList or update hate if already present
        attackable.addDamageHate(originalAttackTarget, 0, 1)

        // Set the actor AI Intention to ATTACK
        if (attackable.ai.desire.intention != CtrlIntention.ATTACK) {
            // Set the Creature movement type to run and send Server->Client packet ChangeMoveType to all others Player
            attackable.setRunning()

            attackable.ai.setIntention(CtrlIntention.ATTACK, originalAttackTarget)
        }
        return null
    }

    override fun onAggro(npc: Npc, player: Player?, isPet: Boolean): String? {
        if (player == null)
            return null

        (npc as Attackable).addDamageHate(if (isPet) player.pet else player, 0, 1)
        return null
    }

    override fun onSpawn(npc: Npc): String? {
        return null
    }

    override fun onAttack(npc: Npc, attacker: Creature, damage: Int, skill: L2Skill?): String? {
        npc.ai.notifyEvent(CtrlEvent.EVT_ATTACKED, attacker)
        (npc as Attackable).addDamageHate(attacker, damage, damage * 100 / (npc.getLevel() + 7))
        return null
    }

    override fun onKill(npc: Npc, killer: Creature): String? {
        if (npc is Monster) {
            val master = npc.master

            master?.minionList?.onMinionDie(
                npc,
                if (master.isRaidBoss) Config.RAID_MINION_RESPAWN_TIMER else master.spawn.respawnDelay * 1000 / 2
            )

            if (npc.hasMinions())
                npc.minionList.onMasterDie()
        }
        return null
    }

    /**
     * This method selects a random player.<br></br>
     * Player can't be dead and isn't an hidden GM aswell.
     * @param npc to check.
     * @return the random player.
     */
    fun getRandomPlayer(npc: Npc): Player? {
        val result = ArrayList<Player>()

        for (player in npc.getKnownType(Player::class.java)) {
            if (player.isDead)
                continue

            if (player.isGM && player.appearance.invisible)
                continue

            result.add(player)
        }

        return if (result.isEmpty()) null else Rnd[result]
    }

    /**
     * Return the number of players in a defined radius.<br></br>
     * Dead players aren't counted, invisible ones is the boolean parameter.
     * @param range : the radius.
     * @param npc : the object to make the test on.
     * @param invisible : true counts invisible characters.
     * @return the number of targets found.
     */
    fun getPlayersCountInRadius(range: Int, npc: Creature, invisible: Boolean): Int {
        var count = 0
        for (player in npc.getKnownTypeInRadius(Player::class.java, range)) {
            if (player.isDead)
                continue

            if (!invisible && player.appearance.invisible)
                continue

            count++
        }
        return count
    }

    /**
     * Under that barbarian name, return the number of players in front, back and sides of the npc.<br></br>
     * Dead players aren't counted, invisible ones is the boolean parameter.
     * @param range : the radius.
     * @param npc : the object to make the test on.
     * @param invisible : true counts invisible characters.
     * @return an array composed of front, back and side targets number.
     */
    fun getPlayersCountInPositions(range: Int, npc: Creature, invisible: Boolean): IntArray {
        var frontCount = 0
        var backCount = 0
        var sideCount = 0

        for (player in npc.getKnownType(Player::class.java)) {
            if (player.isDead)
                continue

            if (!invisible && player.appearance.invisible)
                continue

            if (!MathUtil.checkIfInRange(range, npc, player, true))
                continue

            when {
                player.isInFrontOf(npc) -> frontCount++
                player.isBehind(npc) -> backCount++
                else -> sideCount++
            }
        }

        return intArrayOf(frontCount, backCount, sideCount)
    }

    /**
     * Set an [Attackable] intention to ATTACK, and attacks the chosen [Creature].
     * @param npc : The Attackable who is attacking the target.
     * @param victim : The Creature to attack.
     * @param aggro : The aggro to add, 999 if not given.
     */
    @JvmOverloads
    protected fun attack(npc: Attackable, victim: Creature?, aggro: Int = 0) {
        npc.setIsRunning(true)
        npc.addDamageHate(victim, 0, if (aggro <= 0) 999 else aggro)
        npc.ai.setIntention(CtrlIntention.ATTACK, victim)
    }

    /**
     * Test and cast curses once a [Creature] attacks a [Npc].<br></br>
     * <br></br>
     * <font color=red>BEWARE : no checks are made based on Playable. You have to add it on the caller method.</font>
     * @param npc : The NPC who casts the skill.
     * @param attacker : The Creature to test.
     * @param npcId : The npcId who calls Anti Strider debuff (only bosses, normally).
     * @return true if the curse must counter the leftover behavior.
     */
    @JvmOverloads
    protected fun testCursesOnAttack(npc: Npc, attacker: Creature, npcId: Int = npc.npcId): Boolean {
        if (Config.RAID_DISABLE_CURSE)
            return false

        // Petrification curse.
        if (attacker.level - npc.level > 8) {
            val curse = SkillTable.FrequentSkill.RAID_CURSE2.skill
            if (attacker.getFirstEffect(curse) == null) {
                npc.broadcastPacket(MagicSkillUse(npc, attacker, curse!!.id, curse.level, 300, 0))
                curse.getEffects(npc, attacker)

                (npc as Attackable).stopHating(attacker)
                return true
            }
        }

        // Antistrider slow curse.
        if (npc.npcId == npcId && attacker is Player && attacker.isMounted) {
            val curse = SkillTable.FrequentSkill.RAID_ANTI_STRIDER_SLOW.skill
            if (attacker.getFirstEffect(curse) == null) {
                npc.broadcastPacket(MagicSkillUse(npc, attacker, curse!!.id, curse.level, 300, 0))
                curse.getEffects(npc, attacker)
            }
        }
        return false
    }

    /**
     * Enforced testCursesOnAttack with third parameter set to -1. We only test RAID_CURSE2, not RAID_ANTI_STRIDER_SLOW.
     * @see .testCursesOnAttack
     * @param npc : The NPC who casts the skill.
     * @param attacker : The Creature to test.
     * @return true if the curse must counter the leftover behavior.
     */
    protected fun testCursesOnAggro(npc: Npc, attacker: Creature): Boolean {
        return testCursesOnAttack(npc, attacker, -1)
    }
}
/**
 * Similar to its mother class, but the Anti Strider Slow debuff is known to be casted by the actual npc.
 * @see .testCursesOnAttack
 * @param npc : The NPC who casts the skill.
 * @param attacker : The Creature to test.
 * @return true if the curse must counter the leftover behavior.
 */