package com.l2kt.gameserver.model.actor

import java.util.ArrayList
import java.util.Collections
import java.util.HashMap
import java.util.concurrent.ConcurrentHashMap

import com.l2kt.Config
import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.commons.math.MathUtil
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.manager.CursedWeaponManager
import com.l2kt.gameserver.data.xml.HerbDropData
import com.l2kt.gameserver.model.item.DropCategory
import com.l2kt.gameserver.model.item.DropData
import com.l2kt.gameserver.model.item.instance.ItemInstance

import com.l2kt.gameserver.model.AbsorbInfo
import com.l2kt.gameserver.model.AggroInfo
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.RewardInfo
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.ai.type.AttackableAI
import com.l2kt.gameserver.model.actor.ai.type.CreatureAI
import com.l2kt.gameserver.model.actor.ai.type.SiegeGuardAI
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.actor.instance.Servitor
import com.l2kt.gameserver.model.actor.status.AttackableStatus
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.model.actor.template.NpcTemplate.SkillType
import com.l2kt.gameserver.model.group.CommandChannel
import com.l2kt.gameserver.model.group.Party
import com.l2kt.gameserver.model.holder.IntIntHolder
import com.l2kt.gameserver.model.manor.Seed
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import com.l2kt.gameserver.scripting.EventType
import com.l2kt.gameserver.scripting.Quest

/**
 * This class manages all NPCs that can be attacked. It inherits from [Npc].
 */
open class Attackable(objectId: Int, template: NpcTemplate) : Npc(objectId, template) {
    private val _attackByList = ConcurrentHashMap.newKeySet<Creature>()

    val aggroList: MutableMap<Creature, AggroInfo> = ConcurrentHashMap()
    private val _absorbersList = ConcurrentHashMap<Int, AbsorbInfo>()

    private val _sweepItems = ArrayList<IntIntHolder>()
    private val _harvestItems = ArrayList<IntIntHolder>()

    private var _isRaid: Boolean = false
    private var _isMinion: Boolean = false

    var isReturningToSpawnPoint: Boolean = false
    private var _seeThroughSilentMove: Boolean = false

    var seed: Seed? = null
        private set
    var seederId: Int = 0
        private set

    /**
     * @return True if the L2Attackable was hit by an over-hit enabled skill.
     */
    var isOverhit: Boolean = false
        private set
    /**
     * @return the amount of damage done on the L2Attackable using an over-hit enabled skill.
     */
    var overhitDamage: Double = 0.toDouble()
        private set
    /**
     * @return the Creature who hit on the L2Attackable using an over-hit enabled skill.
     */
    var overhitAttacker: Creature? = null
        private set

    var firstCommandChannelAttacked: CommandChannel? = null
    var commandChannelTimer: CommandChannelTimer? = null
        protected set
    var commandChannelLastAttack: Long = 0

    /**
     * @return the most hated Creature of the L2Attackable _aggroList.
     */
    open// Go through the aggroList of the L2Attackable
    val mostHated: Creature?
        get() {
            if (aggroList.isEmpty() || isAlikeDead)
                return null

            var mostHated: Creature? = null
            var maxHate = 0
            for (ai in aggroList.values) {
                if (ai.checkHate(this) > maxHate) {
                    mostHated = ai.attacker
                    maxHate = ai.hate
                }
            }
            return mostHated
        }

    /**
     * @return the list of hated Creature. It also make checks, setting hate to 0 following conditions.
     */
    val hateList: List<Creature>
        get() {
            if (aggroList.isEmpty() || isAlikeDead)
                return emptyList()

            val result = ArrayList<Creature>()
            for (ai in aggroList.values) {
                ai.checkHate(this)
                result.add(ai.attacker)
            }
            return result
        }

    open val driftRange: Int
        get() = Config.MAX_DRIFT_RANGE

    val attackByList: MutableSet<Creature>
        get() = _attackByList

    /**
     * @return the active weapon of this L2Attackable (= null).
     */
    val activeWeapon: ItemInstance?
        get() = null

    /**
     * @return true if a Dwarf used Spoil on the L2Attackable.
     */
    val isSpoiled: Boolean
        get() = !_sweepItems.isEmpty()

    /**
     * @return list containing all ItemHolder that can be spoiled.
     */
    val sweepItems: List<IntIntHolder>
        get() = _sweepItems

    /**
     * @return list containing all ItemHolder that can be harvested.
     */
    val harvestItems: List<IntIntHolder>
        get() = _harvestItems

    val isSeeded: Boolean
        get() = seed != null

    /**
     * @return leader of this minion or null.
     */
    open fun getMaster() : Attackable? {
        return null
    }

    open val isGuard: Boolean
        get() = false

    override fun initCharStatus() {
        status = AttackableStatus(this)
    }

    override fun getStatus(): AttackableStatus {
        return super.getStatus() as AttackableStatus
    }

    override fun getAI(): CreatureAI {
        return _ai ?: synchronized(this) {
            if (_ai == null)
                _ai = AttackableAI(this)

            return _ai
        }
    }

    /**
     * Reduce the current HP of the L2Attackable.
     * @param damage The HP decrease value
     * @param attacker The Creature who attacks
     */
    override fun reduceCurrentHp(damage: Double, attacker: Creature?, skill: L2Skill?) {
        reduceCurrentHp(damage, attacker, true, false, skill)
    }

    /**
     * Reduce the current HP of the L2Attackable, update its _aggroList and launch the doDie Task if necessary.
     * @param attacker The Creature who attacks
     * @param awake The awake state (If True : stop sleeping)
     */
    override fun reduceCurrentHp(damage: Double, attacker: Creature?, awake: Boolean, isDOT: Boolean, skill: L2Skill?) {
        if (isRaidBoss && attacker != null) {
            val party = attacker.party
            if (party != null) {
                val cc = party.commandChannel
                if (cc != null && cc.meetRaidWarCondition(this)) {
                    if (firstCommandChannelAttacked == null)
                    // looting right isn't set
                    {
                        synchronized(this) {
                            if (firstCommandChannelAttacked == null) {
                                firstCommandChannelAttacked = attacker.party!!.commandChannel
                                if (firstCommandChannelAttacked != null) {
                                    commandChannelTimer = CommandChannelTimer(this)
                                    commandChannelLastAttack = System.currentTimeMillis()
                                    ThreadPool.schedule(commandChannelTimer!!, 10000) // check for last attack
                                }
                            }
                        }
                    } else if (attacker.party!!.commandChannel == firstCommandChannelAttacked)
                    // is in same channel
                        commandChannelLastAttack = System.currentTimeMillis() // update last attack time
                }
            }
        }

        // Add damage and hate to the attacker AggroInfo of the L2Attackable _aggroList
        addDamage(attacker, damage.toInt(), skill)

        // Reduce the current HP of the L2Attackable and launch the doDie Task if necessary
        super.reduceCurrentHp(damage, attacker, awake, isDOT, skill)
    }

    /**
     * Kill the L2Attackable (the corpse disappeared after 7 seconds), distribute rewards (EXP, SP, Drops...) and notify Quest Engine.
     *
     *  * Distribute Exp and SP rewards to Player (including Summon owner) that hit the L2Attackable and to their Party members
     *  * Notify the Quest Engine of the L2Attackable death if necessary
     *  * Kill the L2Npc (the corpse disappeared after 7 seconds)
     *
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T GIVE rewards to L2PetInstance</B></FONT>
     * @param killer The Creature that has killed the L2Attackable
     */
    override fun doDie(killer: Creature?): Boolean {
        // Kill the L2Npc (the corpse disappeared after 7 seconds)
        if (!super.doDie(killer))
            return false

        val scripts = template.getEventQuests(EventType.ON_KILL)
        if (scripts != null)
            for (quest in scripts)
                ThreadPool.schedule(Runnable{ quest.notifyKill(this, killer) }, 3000)

        _attackByList.clear()

        return true
    }

    /**
     * Distribute Exp and SP rewards to Player (including Summon owner) that hit the L2Attackable and to their Party members.
     *
     *  * Get the Player owner of the Servitor (if necessary) and L2Party in progress
     *  * Calculate the Experience and SP rewards in function of the level difference
     *  * Add Exp and SP rewards to Player (including Summon penalty) and to Party members in the known area of the last attacker
     *
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T GIVE rewards to L2PetInstance</B></FONT>
     * @param lastAttacker The Creature that has killed the L2Attackable
     */
    override fun calculateRewards(lastAttacker: Creature) {
        if (aggroList.isEmpty())
            return

        // Creates an empty list of rewards.
        val rewards = ConcurrentHashMap<Creature, RewardInfo>()

        var maxDealer: Player? = null
        var maxDamage = 0
        var totalDamage: Long = 0

        // Go through the _aggroList of the L2Attackable.
        for (info in aggroList.values) {
            if (info.attacker !is Playable)
                continue

            // Get the Creature corresponding to this attacker.
            val attacker = info.attacker

            // Get damages done by this attacker.
            val damage = info.damage
            if (damage <= 1)
                continue

            // Check if attacker isn't too far from this.
            if (!MathUtil.checkIfInRange(Config.PARTY_RANGE, this, attacker, true))
                continue

            val attackerPlayer = attacker.actingPlayer

            totalDamage += damage.toLong()

            // Calculate real damages (Summoners should get own damage plus summon's damage).
            var reward: RewardInfo? = rewards[attacker]
            if (reward == null) {
                reward = RewardInfo(attacker)
                rewards[attacker] = reward
            }
            reward.addDamage(damage)

            if (attacker is Summon) {
                reward = rewards[attackerPlayer as Creature]
                if (reward == null) {
                    reward = RewardInfo(attackerPlayer)
                    rewards[attackerPlayer] = reward
                }
                reward.addDamage(damage)
            }

            if (reward.damage > maxDamage) {
                maxDealer = attackerPlayer
                maxDamage = reward.damage
            }
        }

        // Command channel restriction ; if such thing exists, the main contributor is the channel leader, no matter the participation of the channel, and no matter the damage done by other participants.
        if (isRaidBoss && firstCommandChannelAttacked != null)
            maxDealer = firstCommandChannelAttacked!!.leader

        // Manage Base, Quests and Sweep drops of the L2Attackable.
        doItemDrop(template, if (maxDealer != null && maxDealer.isOnline) maxDealer else lastAttacker)

        for (reward in rewards.values) {
            if (reward.attacker is Summon)
                continue

            // Attacker to be rewarded.
            val attacker = reward.attacker.actingPlayer

            // Total amount of damage done.
            val damage = reward.damage

            // Get party.
            val attackerParty = attacker!!.party

            // Penalty applied to the attacker's XP
            val penalty = if (attacker.hasServitor()) (attacker.pet as Servitor).expPenalty else 0f

            // If there's NO party in progress.
            if (attackerParty == null) {
                // Calculate Exp and SP rewards.
                if (!attacker.isDead && attacker.getKnownType(Attackable::class.java).contains(this)) {
                    // Calculate the difference of level between this attacker and the L2Attackable.
                    val levelDiff = attacker.level - level

                    val expSp = calculateExpAndSp(levelDiff, damage, totalDamage)
                    var exp = expSp[0].toLong()
                    var sp = expSp[1]

                    if (isChampion) {
                        exp *= Config.CHAMPION_REWARDS.toLong()
                        sp *= Config.CHAMPION_REWARDS
                    }

                    exp *= (1 - penalty).toLong()

                    if (isOverhit && overhitAttacker != null && overhitAttacker!!.actingPlayer != null && attacker == overhitAttacker!!.actingPlayer) {
                        attacker.sendPacket(SystemMessageId.OVER_HIT)
                        exp += calculateOverhitExp(exp)
                    }

                    // Set new karma.
                    attacker.updateKarmaLoss(exp)

                    // Distribute the Exp and SP between the Player and its L2Summon.
                    attacker.addExpAndSp(exp, sp, rewards)
                }
            } else {
                var partyDmg = 0
                var partyMul = 1f
                var partyLvl = 0

                // Get all Creature that can be rewarded in the party.
                val rewardedMembers = ArrayList<Player>()

                // Go through all Player in the party.
                val groupMembers =
                    if (attackerParty.isInCommandChannel) attackerParty.commandChannel!!.members else attackerParty.members

                val playersWithPets = HashMap<Creature, RewardInfo>()

                for (partyPlayer in groupMembers) {
                    if (partyPlayer == null || partyPlayer.isDead)
                        continue

                    // Get the RewardInfo of this Player from L2Attackable rewards
                    val reward2 = rewards[partyPlayer]

                    // If the Player is in the L2Attackable rewards add its damages to party damages
                    if (reward2 != null) {
                        if (MathUtil.checkIfInRange(Config.PARTY_RANGE, this, partyPlayer, true)) {
                            partyDmg += reward2.damage // Add Player damages to party damages
                            rewardedMembers.add(partyPlayer)

                            if (partyPlayer.level > partyLvl)
                                partyLvl =
                                    if (attackerParty.isInCommandChannel) attackerParty.commandChannel!!.level else partyPlayer.level
                        }
                        rewards.remove(partyPlayer) // Remove the Player from the L2Attackable rewards

                        playersWithPets[partyPlayer] = reward2
                        if (partyPlayer.hasPet() && rewards.containsKey(partyPlayer.pet))
                            playersWithPets[partyPlayer.pet] = rewards[partyPlayer.pet]!!
                    } else {
                        if (MathUtil.checkIfInRange(Config.PARTY_RANGE, this, partyPlayer, true)) {
                            rewardedMembers.add(partyPlayer)
                            if (partyPlayer.level > partyLvl)
                                partyLvl =
                                    if (attackerParty.isInCommandChannel) attackerParty.commandChannel!!.level else partyPlayer.level
                        }
                    }// Add Player of the party (that have attacked or not) to members that can be rewarded and in range of the monster.
                }

                // If the party didn't killed this L2Attackable alone
                if (partyDmg < totalDamage)
                    partyMul = partyDmg.toFloat() / totalDamage

                // Calculate the level difference between Party and L2Attackable
                val levelDiff = partyLvl - level

                // Calculate Exp and SP rewards
                val expSp = calculateExpAndSp(levelDiff, partyDmg, totalDamage)
                var exp = expSp[0].toLong()
                var sp = expSp[1]

                if (isChampion) {
                    exp *= Config.CHAMPION_REWARDS.toLong()
                    sp *= Config.CHAMPION_REWARDS
                }

                exp *= partyMul.toLong()
                sp *= partyMul.toInt()

                // Check for an over-hit enabled strike
                // (When in party, the over-hit exp bonus is given to the whole party and splitted proportionally through the party members)
                if (isOverhit && overhitAttacker != null && overhitAttacker!!.actingPlayer != null && attacker == overhitAttacker!!.actingPlayer) {
                    attacker.sendPacket(SystemMessageId.OVER_HIT)
                    exp += calculateOverhitExp(exp)
                }

                // Distribute Experience and SP rewards to Player Party members in the known area of the last attacker
                if (partyDmg > 0)
                    attackerParty.distributeXpAndSp(exp, sp, rewardedMembers, partyLvl, playersWithPets)
            }// Share with party members.
        }
    }

    override fun onSpawn() {
        super.onSpawn()

        // Clear mob spoil/seed state
        spoilerId = 0

        // Clear all aggro char from list
        aggroList.clear()

        // Clear Harvester Reward List
        _harvestItems.clear()

        // Clear mod Seeded stat
        seed = null
        seederId = 0

        // Clear overhit value
        overhitEnabled(false)

        _sweepItems.clear()
        resetAbsorberList()

        setWalking()

        // check the region where this mob is, do not activate the AI if region is inactive.
        if (!isInActiveRegion) {
            if (hasAI())
                ai.stopAITask()
        }
    }

    /**
     * Check if the server allows Random Animation.<BR></BR>
     * <BR></BR>
     * This is located here because L2Monster and L2FriendlyMob both extend this class. The other non-pc instances extend either L2Npc or L2MonsterInstance.
     */
    override fun hasRandomAnimation(): Boolean {
        return Config.MAX_MONSTER_ANIMATION > 0 && !isRaidRelated
    }

    override fun isMob(): Boolean {
        return true // This means we use MAX_MONSTER_ANIMATION instead of MAX_NPC_ANIMATION
    }

    override fun isRaidBoss(): Boolean {
        return _isRaid && !_isMinion
    }

    /**
     * Set this object as part of raid (it can be either a boss or a minion).<br></br>
     * <br></br>
     * This state affects behaviors such as auto loot configs, Command Channel acquisition, or even Config related to raid bosses.<br></br>
     * <br></br>
     * A raid boss can't be lethal-ed, and a raid curse occurs if the level difference is too high.
     * @param isRaid : if true, this object will be set as a raid.
     */
    fun setRaid(isRaid: Boolean) {
        _isRaid = isRaid
    }

    override fun isRaidRelated(): Boolean {
        return _isRaid
    }

    /**
     * Set this [Attackable] as a minion instance.
     * @param isRaidMinion : If true, this instance is considered a raid minion.
     */
    fun setMinion(isRaidMinion: Boolean) {
        _isRaid = isRaidMinion
        _isMinion = true
    }

    override fun isMinion(): Boolean {
        return _isMinion
    }

    fun addAttackerToAttackByList(player: Creature?) {
        if (player == null || player === this)
            return

        _attackByList.add(player)
    }

    /**
     * Add damage and hate to the attacker AggroInfo of the L2Attackable _aggroList.
     * @param attacker The Creature that gave damages to this L2Attackable
     * @param damage The number of damages given by the attacker Creature
     * @param skill The skill used to make damage.
     */
    fun addDamage(attacker: Creature?, damage: Int, skill: L2Skill?) {
        if (attacker == null || isDead)
            return

        val scripts = template.getEventQuests(EventType.ON_ATTACK)
        if (scripts != null)
            for (quest in scripts)
                quest.notifyAttack(this, attacker, damage, skill)
    }

    /**
     * Add damage and hate to the attacker AggroInfo of the L2Attackable _aggroList.
     * @param attacker The Creature that gave damages to this L2Attackable
     * @param damage The number of damages given by the attacker Creature
     * @param aggro The hate (=damage) given by the attacker Creature
     */
    open fun addDamageHate(attacker: Creature?, damage: Int, aggro: Int) {
        var aggro = aggro
        if (attacker == null)
            return

        // Get or create the AggroInfo of the attacker.
        val ai = aggroList.computeIfAbsent(
            attacker
        ) { AggroInfo(it) }
        ai.addDamage(damage)
        ai.addHate(aggro)

        if (aggro == 0) {
            val targetPlayer = attacker.actingPlayer
            if (targetPlayer != null) {
                val scripts = template.getEventQuests(EventType.ON_AGGRO)
                if (scripts != null)
                    for (quest in scripts)
                        quest.notifyAggro(this, targetPlayer, attacker is Summon)
            } else {
                aggro = 1
                ai.addHate(1)
            }
        } else {
            // Set the intention to the L2Attackable to ACTIVE
            if (aggro > 0 && getAI().desire.intention === CtrlIntention.IDLE)
                getAI().setIntention(CtrlIntention.ACTIVE)
        }
    }

    /**
     * Reduce hate for the target. If the target is null, decrease the hate for the whole aggrolist.
     * @param target The target to check.
     * @param amount The amount to remove.
     */
    fun reduceHate(target: Creature?, amount: Int) {
        var amount = amount
        if (ai is SiegeGuardAI) {
            stopHating(target)
            setTarget(null)
            ai.setIntention(CtrlIntention.IDLE)
            return
        }

        if (target == null)
        // whole aggrolist
        {
            val mostHated = mostHated

            // If not most hated target is found, makes AI passive for a moment more
            if (mostHated == null) {
                (ai as AttackableAI).setGlobalAggro(-25)
                return
            }

            for (ai in aggroList.values)
                ai.addHate(-amount)

            amount = getHating(mostHated)

            if (amount <= 0) {
                (ai as AttackableAI).setGlobalAggro(-25)
                aggroList.clear()
                ai.setIntention(CtrlIntention.ACTIVE)
                setWalking()
            }
            return
        }

        val ai = aggroList[target] ?: return

        ai.addHate(-amount)

        if (ai.hate <= 0) {
            if (mostHated == null) {
                (getAI() as AttackableAI).setGlobalAggro(-25)
                aggroList.clear()
                getAI().setIntention(CtrlIntention.ACTIVE)
                setWalking()
            }
        }
    }

    /**
     * Clears _aggroList hate of the Creature without removing from the list.
     * @param target The target to clean from that L2Attackable _aggroList.
     */
    fun stopHating(target: Creature?) {
        if (target == null)
            return

        val ai = aggroList[target]
        ai?.stopHate()
    }

    fun cleanAllHate() {
        for (ai in aggroList.values)
            ai.stopHate()
    }

    /**
     * @param target The Creature whose hate level must be returned
     * @return the hate level of the L2Attackable against this Creature contained in _aggroList.
     */
    fun getHating(target: Creature?): Int {
        if (aggroList.isEmpty() || target == null)
            return 0

        val ai = aggroList[target] ?: return 0

        if (ai.attacker is Player && ai.attacker.appearance.invisible) {
            // Remove Object Should Use This Method and Can be Blocked While Interating
            aggroList.remove(target)
            return 0
        }

        if (!ai.attacker.isVisible) {
            aggroList.remove(target)
            return 0
        }

        if (ai.attacker.isAlikeDead) {
            ai.stopHate()
            return 0
        }
        return ai.hate
    }

    /**
     * Calculates quantity of items for specific drop acording to current situation.
     * @param drop The L2DropData count is being calculated for
     * @param levelModifier level modifier in %'s (will be subtracted from drop chance)
     * @param isSweep if true, use spoil drop chance.
     * @return the ItemHolder.
     */
    private fun calculateRewardItem(drop: DropData, levelModifier: Int, isSweep: Boolean): IntIntHolder? {
        // Get default drop chance
        var dropChance = drop.chance.toDouble()

        if (Config.DEEPBLUE_DROP_RULES) {
            var deepBlueDrop = 1
            if (levelModifier > 0) {
                // We should multiply by the server's drop rate, so we always get a low chance of drop for deep blue mobs.
                // NOTE: This is valid only for adena drops! Others drops will still obey server's rate
                deepBlueDrop = 3
                if (drop.itemId == 57) {
                    deepBlueDrop *= if (isRaidBoss) Config.RATE_DROP_ITEMS_BY_RAID.toInt() else Config.RATE_DROP_ITEMS.toInt()
                    if (deepBlueDrop == 0)
                    // avoid div by 0
                        deepBlueDrop = 1
                }
            }

            // Check if we should apply our maths so deep blue mobs will not drop that easy
            dropChance = ((drop.chance - drop.chance * levelModifier / 100) / deepBlueDrop).toDouble()
        }

        // Applies Drop rates
        if (drop.itemId == 57)
            dropChance *= Config.RATE_DROP_ADENA
        else if (isSweep)
            dropChance *= Config.RATE_DROP_SPOIL
        else
            dropChance *= if (isRaidBoss) Config.RATE_DROP_ITEMS_BY_RAID else Config.RATE_DROP_ITEMS

        if (isChampion)
            dropChance *= Config.CHAMPION_REWARDS.toDouble()

        // Set our limits for chance of drop
        if (dropChance < 1)
            dropChance = 1.0

        // Get min and max Item quantity that can be dropped in one time
        val minCount = drop.minDrop
        val maxCount = drop.maxDrop

        // Get the item quantity dropped
        var itemCount = 0

        // Check if the Item must be dropped
        val random = Rnd[DropData.MAX_CHANCE]
        while (random < dropChance) {
            // Get the item quantity dropped
            if (minCount < maxCount)
                itemCount += Rnd[minCount, maxCount]
            else if (minCount == maxCount)
                itemCount += minCount
            else
                itemCount++

            // Prepare for next iteration if dropChance > L2DropData.MAX_CHANCE
            dropChance -= DropData.MAX_CHANCE.toDouble()
        }

        if (isChampion)
            if (drop.itemId == 57 || drop.itemId >= 6360 && drop.itemId <= 6362)
                itemCount *= Config.CHAMPION_ADENAS_REWARDS

        return if (itemCount > 0) IntIntHolder(drop.itemId, itemCount) else null

    }

    /**
     * Calculates quantity of items for specific drop CATEGORY according to current situation <br></br>
     * Only a max of ONE item from a category is allowed to be dropped.
     * @param categoryDrops The category to make checks on.
     * @param levelModifier level modifier in %'s (will be subtracted from drop chance)
     * @return the ItemHolder.
     */
    private fun calculateCategorizedRewardItem(categoryDrops: DropCategory?, levelModifier: Int): IntIntHolder? {
        if (categoryDrops == null)
            return null

        // Get default drop chance for the category (that's the sum of chances for all items in the category)
        // keep track of the base category chance as it'll be used later, if an item is drop from the category.
        // for everything else, use the total "categoryDropChance"
        val basecategoryDropChance = categoryDrops.categoryChance
        var categoryDropChance = basecategoryDropChance

        if (Config.DEEPBLUE_DROP_RULES) {
            val deepBlueDrop = if (levelModifier > 0) 3 else 1

            // Check if we should apply our maths so deep blue mobs will not drop that easy
            categoryDropChance = (categoryDropChance - categoryDropChance * levelModifier / 100) / deepBlueDrop
        }

        // Applies Drop rates
        categoryDropChance *= (if (isRaidBoss) Config.RATE_DROP_ITEMS_BY_RAID else Config.RATE_DROP_ITEMS).toInt()

        if (isChampion)
            categoryDropChance *= Config.CHAMPION_REWARDS

        // Set our limits for chance of drop
        if (categoryDropChance < 1)
            categoryDropChance = 1

        // Check if an Item from this category must be dropped
        if (Rnd[DropData.MAX_CHANCE] < categoryDropChance) {
            val drop = categoryDrops.dropOne(isRaidBoss) ?: return null

            // Now decide the quantity to drop based on the rates and penalties. To get this value
            // simply divide the modified categoryDropChance by the base category chance. This
            // results in a chance that will dictate the drops amounts: for each amount over 100
            // that it is, it will give another chance to add to the min/max quantities.
            //
            // For example, If the final chance is 120%, then the item should drop between
            // its min and max one time, and then have 20% chance to drop again. If the final
            // chance is 330%, it will similarly give 3 times the min and max, and have a 30%
            // chance to give a 4th time.
            // At least 1 item will be dropped for sure. So the chance will be adjusted to 100%
            // if smaller.

            var dropChance = drop.chance.toDouble()
            if (drop.itemId == 57)
                dropChance *= Config.RATE_DROP_ADENA
            else
                dropChance *= if (isRaidBoss) Config.RATE_DROP_ITEMS_BY_RAID else Config.RATE_DROP_ITEMS

            if (isChampion)
                dropChance *= Config.CHAMPION_REWARDS.toDouble()

            if (dropChance < DropData.MAX_CHANCE)
                dropChance = DropData.MAX_CHANCE.toDouble()

            // Get min and max Item quantity that can be dropped in one time
            val min = drop.minDrop
            val max = drop.maxDrop

            // Get the item quantity dropped
            var itemCount = 0

            // Check if the Item must be dropped
            val random = Rnd[DropData.MAX_CHANCE]
            while (random < dropChance) {
                // Get the item quantity dropped
                if (min < max)
                    itemCount += Rnd[min, max]
                else if (min == max)
                    itemCount += min
                else
                    itemCount++

                // Prepare for next iteration if dropChance > L2DropData.MAX_CHANCE
                dropChance -= DropData.MAX_CHANCE.toDouble()
            }

            if (isChampion)
                if (drop.itemId == 57 || drop.itemId >= 6360 && drop.itemId <= 6362)
                    itemCount *= Config.CHAMPION_ADENAS_REWARDS

            if (itemCount > 0)
                return IntIntHolder(drop.itemId, itemCount)
        }
        return null
    }

    /**
     * @param lastAttacker The Player that has killed the L2Attackable
     * @return the level modifier for drop
     */
    private fun calculateLevelModifierForDrop(lastAttacker: Player): Int {
        if (Config.DEEPBLUE_DROP_RULES) {
            var highestLevel = lastAttacker.level

            // Check to prevent very high level player to nearly kill mob and let low level player do the last hit.
            for (atkChar in _attackByList)
                if (atkChar.level > highestLevel)
                    highestLevel = atkChar.level

            // According to official data (Prima), deep blue mobs are 9 or more levels below players
            if (highestLevel - 9 >= level)
                return (highestLevel - (level + 8)) * 9
        }
        return 0
    }

    private fun calculateCategorizedHerbItem(categoryDrops: DropCategory?, levelModifier: Int): IntIntHolder? {
        if (categoryDrops == null)
            return null

        var categoryDropChance = categoryDrops.categoryChance

        // Applies Drop rates
        when (categoryDrops.categoryType) {
            1 -> categoryDropChance *= Config.RATE_DROP_HP_HERBS.toInt()
            2 -> categoryDropChance *= Config.RATE_DROP_MP_HERBS.toInt()
            3 -> categoryDropChance *= Config.RATE_DROP_SPECIAL_HERBS.toInt()
            else -> categoryDropChance *= Config.RATE_DROP_COMMON_HERBS.toInt()
        }

        // Drop chance is affected by deep blue drop rule.
        if (Config.DEEPBLUE_DROP_RULES) {
            val deepBlueDrop = if (levelModifier > 0) 3 else 1

            // Check if we should apply our maths so deep blue mobs will not drop that easy
            categoryDropChance = (categoryDropChance - categoryDropChance * levelModifier / 100) / deepBlueDrop
        }

        // Check if an Item from this category must be dropped
        if (Rnd[DropData.MAX_CHANCE] < Math.max(1, categoryDropChance)) {
            val drop = categoryDrops.dropOne(false) ?: return null

            /*
			 * Now decide the quantity to drop based on the rates and penalties. To get this value, simply divide the modified categoryDropChance by the base category chance. This results in a chance that will dictate the drops amounts : for each amount over 100 that it is, it will give another
			 * chance to add to the min/max quantities. For example, if the final chance is 120%, then the item should drop between its min and max one time, and then have 20% chance to drop again. If the final chance is 330%, it will similarly give 3 times the min and max, and have a 30% chance to
			 * give a 4th time. At least 1 item will be dropped for sure. So the chance will be adjusted to 100% if smaller.
			 */
            var dropChance = drop.chance.toDouble()

            when (categoryDrops.categoryType) {
                1 -> dropChance *= Config.RATE_DROP_HP_HERBS
                2 -> dropChance *= Config.RATE_DROP_MP_HERBS
                3 -> dropChance *= Config.RATE_DROP_SPECIAL_HERBS
                else -> dropChance *= Config.RATE_DROP_COMMON_HERBS
            }

            if (dropChance < DropData.MAX_CHANCE)
                dropChance = DropData.MAX_CHANCE.toDouble()

            // Get min and max Item quantity that can be dropped in one time
            val min = drop.minDrop
            val max = drop.maxDrop

            // Get the item quantity dropped
            var itemCount = 0

            // Check if the Item must be dropped
            val random = Rnd[DropData.MAX_CHANCE]
            while (random < dropChance) {
                // Get the item quantity dropped
                if (min < max)
                    itemCount += Rnd[min, max]
                else if (min == max)
                    itemCount += min
                else
                    itemCount++

                // Prepare for next iteration if dropChance > L2DropData.MAX_CHANCE
                dropChance -= DropData.MAX_CHANCE.toDouble()
            }

            if (itemCount > 0)
                return IntIntHolder(drop.itemId, itemCount)
        }
        return null
    }

    /**
     * Manage Base & Quests drops of this [Attackable] using an associated [NpcTemplate] (can be its or another template).<br></br>
     * <br></br>
     * This method is called by [Attackable.calculateRewards].
     * @param npcTemplate : The template used to retrieve drops.
     * @param mainDamageDealer : The Creature that made the most damage.
     */
    open fun doItemDrop(npcTemplate: NpcTemplate, mainDamageDealer: Creature?) {
        if (mainDamageDealer == null)
            return

        // Don't drop anything if the last attacker or owner isn't Player
        val player = mainDamageDealer.actingPlayer ?: return

        // level modifier in %'s (will be subtracted from drop chance)
        val levelModifier = calculateLevelModifierForDrop(player)

        CursedWeaponManager.checkDrop(this, player)

        // now throw all categorized drops and handle spoil.
        for (cat in npcTemplate.dropData) {
            var item: IntIntHolder? = null
            if (cat.isSweep) {
                if (spoilerId != 0) {
                    for (drop in cat.allDrops) {
                        item = calculateRewardItem(drop, levelModifier, true)
                        if (item == null)
                            continue

                        _sweepItems.add(item)
                    }
                }
            } else {
                if (isSeeded) {
                    val drop = cat.dropSeedAllowedDropsOnly() ?: continue

                    item = calculateRewardItem(drop, levelModifier, false)
                } else
                    item = calculateCategorizedRewardItem(cat, levelModifier)

                if (item != null) {
                    // Check if the autoLoot mode is active
                    if (isRaidBoss && Config.AUTO_LOOT_RAID || !isRaidBoss && Config.AUTO_LOOT)
                        player.doAutoLoot(this, item)
                    else
                        dropItem(player, item)

                    // Broadcast message if RaidBoss was defeated
                    if (isRaidBoss)
                        broadcastPacket(
                            SystemMessage.getSystemMessage(SystemMessageId.S1_DIED_DROPPED_S3_S2).addCharName(
                                this
                            ).addItemName(item.id).addNumber(item.value)
                        )
                }
            }
        }

        // Apply special item drop for champions.
        if (isChampion && Config.CHAMPION_REWARD > 0) {
            var dropChance = Config.CHAMPION_REWARD

            // Apply level modifier, if any/wanted.
            if (Config.DEEPBLUE_DROP_RULES) {
                val deepBlueDrop = if (levelModifier > 0) 3 else 1

                // Check if we should apply our maths so deep blue mobs will not drop that easy.
                dropChance = (Config.CHAMPION_REWARD - Config.CHAMPION_REWARD * levelModifier / 100) / deepBlueDrop
            }

            if (Rnd[100] < dropChance) {
                val item = IntIntHolder(Config.CHAMPION_REWARD_ID, Math.max(1, Rnd[1, Config.CHAMPION_REWARD_QTY]))
                if (Config.AUTO_LOOT)
                    player.addItem("ChampionLoot", item.id, item.value, this, true)
                else
                    dropItem(player, item)
            }
        }

        // Herbs.
        if (template.dropHerbGroup > 0) {
            for (cat in HerbDropData.getHerbDroplist(template.dropHerbGroup)) {
                val item = calculateCategorizedHerbItem(cat, levelModifier)
                if (item != null) {
                    if (Config.AUTO_LOOT_HERBS)
                        player.addItem("Loot", item.id, 1, this, true)
                    else {
                        // If multiple similar herbs drop, split them and make a unique drop per item.
                        val count = item.value
                        if (count > 1) {
                            item.value = 1
                            for (i in 0 until count)
                                dropItem(player, item)
                        } else
                            dropItem(player, item)
                    }
                }
            }
        }
    }

    /**
     * Drop reward item.
     * @param mainDamageDealer The player who made highest damage.
     * @param holder The ItemHolder.
     */
    private fun dropItem(mainDamageDealer: Player, holder: IntIntHolder) {
        for (i in 0 until holder.value) {
            // Init the dropped ItemInstance and add it in the world as a visible object at the position where mob was last
            val item = ItemInstance.create(holder.id, holder.value, mainDamageDealer, this)
            item.setDropProtection(mainDamageDealer.objectId, isRaidBoss)
            item.dropMe(this, x + Rnd[-70, 70], y + Rnd[-70, 70], Math.max(z, mainDamageDealer.z) + 20)

            // If stackable, end loop as entire count is included in 1 instance of item.
            if (item.isStackable || !Config.MULTIPLE_ITEM_DROP)
                break
        }
    }

    fun useMagic(skill: L2Skill?) {
        if (skill == null || isAlikeDead)
            return

        if (skill.isPassive)
            return

        if (isCastingNow)
            return

        if (isSkillDisabled(skill))
            return

        if (currentMp < stat.getMpConsume(skill) + stat.getMpInitialConsume(skill))
            return

        if (currentHp <= skill.hpConsume)
            return

        if (skill.isMagic) {
            if (isMuted)
                return
        } else {
            if (isPhysicalMuted)
                return
        }

        val target = skill.getFirstOfTargetList(this) ?: return

        ai.setIntention(CtrlIntention.CAST, skill, target)
    }

    /**
     * @return true if the [Attackable] successfully returned to spawn point. In case of minions, they are simply deleted.
     */
    open fun returnHome(): Boolean {
        // Do nothing if the Attackable is already dead.
        if (isDead)
            return false

        // Minions are simply squeezed if they lose activity.
        if (isMinion && !isRaidRelated) {
            deleteMe()
            return true
        }

        // For regular Attackable, we check if a spawn exists, and if we're far from it (using drift range).
        if (spawn != null && !isInsideRadius(spawn.locX, spawn.locY, driftRange, false)) {
            cleanAllHate()

            isReturningToSpawnPoint = true
            setWalking()
            ai.setIntention(CtrlIntention.MOVE_TO, spawn.loc)
            return true
        }
        return false
    }

    fun canSeeThroughSilentMove(): Boolean {
        return _seeThroughSilentMove
    }

    fun seeThroughSilentMove(`val`: Boolean) {
        _seeThroughSilentMove = `val`
    }

    /**
     * Set the over-hit flag on the L2Attackable.
     * @param status The status of the over-hit flag
     */
    fun overhitEnabled(status: Boolean) {
        isOverhit = status
    }

    /**
     * Set the over-hit values like the attacker who did the strike and the amount of damage done by the skill.
     * @param attacker The Creature who hit on the L2Attackable using the over-hit enabled skill
     * @param damage The ammount of damage done by the over-hit enabled skill on the L2Attackable
     */
    fun setOverhitValues(attacker: Creature?, damage: Double) {
        // Calculate the over-hit damage
        // Ex: mob had 10 HP left, over-hit skill did 50 damage total, over-hit damage is 40
        val overhitDmg = (currentHp - damage) * -1
        if (overhitDmg < 0) {
            // we didn't killed the mob with the over-hit strike. (it wasn't really an over-hit strike)
            // let's just clear all the over-hit related values
            overhitEnabled(false)
            overhitDamage = 0.0
            overhitAttacker = null
            return
        }
        overhitEnabled(true)
        overhitDamage = overhitDmg
        overhitAttacker = attacker
    }

    /**
     * Adds an attacker that successfully absorbed the soul of this L2Attackable into the _absorbersList.
     * @param user : The Player who attacked the monster.
     * @param crystal : The ItemInstance which was used to register.
     */
    fun addAbsorber(user: Player, crystal: ItemInstance) {
        // If the Creature attacker isn't already in the _absorbersList of this L2Attackable, add it
        val ai = _absorbersList[user.objectId]
        if (ai == null) {
            // Create absorb info.
            _absorbersList[user.objectId] = AbsorbInfo(crystal.objectId)
        } else {
            // Add absorb info, unless already registered.
            if (!ai.isRegistered)
                ai.itemId = crystal.objectId
        }
    }

    fun registerAbsorber(user: Player) {
        // Get AbsorbInfo for user.
        val ai = _absorbersList[user.objectId] ?: return

        // Check item being used and register player to mob's absorber list.
        if (user.inventory!!.getItemByObjectId(ai.itemId) == null)
            return

        // Register AbsorbInfo.
        if (!ai.isRegistered) {
            ai.setAbsorbedHpPercent((100 * currentHp / maxHp).toInt())
            ai.isRegistered = true
        }
    }

    fun resetAbsorberList() {
        _absorbersList.clear()
    }

    fun getAbsorbInfo(npcObjectId: Int): AbsorbInfo?{
        return _absorbersList[npcObjectId]
    }

    /**
     * Calculate the Experience and SP to distribute to attacker (Player, Servitor or L2Party) of the L2Attackable.
     * @param diff The difference of level between attacker (Player, Servitor or L2Party) and the L2Attackable
     * @param damage The damages given by the attacker (Player, Servitor or L2Party)
     * @param totalDamage The total damage done.
     * @return an array consisting of xp and sp values.
     */
    private fun calculateExpAndSp(diff: Int, damage: Int, totalDamage: Long): IntArray {
        var diff = diff
        if (diff < -5)
            diff = -5

        var xp = expReward.toDouble() * damage / totalDamage
        var sp = spReward.toDouble() * damage / totalDamage

        val hpSkill = getSkill(4408)
        if (hpSkill != null) {
            xp *= hpSkill.power
            sp *= hpSkill.power
        }

        if (diff > 5)
        // formula revised May 07
        {
            val pow = Math.pow(5.toDouble() / 6, (diff - 5).toDouble())
            xp = xp * pow
            sp = sp * pow
        }

        if (xp <= 0) {
            xp = 0.0
            sp = 0.0
        } else if (sp <= 0)
            sp = 0.0

        return intArrayOf(xp.toInt(), sp.toInt())
    }

    fun calculateOverhitExp(normalExp: Long): Long {
        // Get the percentage based on the total of extra (over-hit) damage done relative to the total (maximum) ammount of HP on the L2Attackable
        var overhitPercentage = overhitDamage * 100 / maxHp

        // Over-hit damage percentages are limited to 25% max
        if (overhitPercentage > 25)
            overhitPercentage = 25.0

        // Get the overhit exp bonus according to the above over-hit damage percentage
        // (1/1 basis - 13% of over-hit damage, 13% of extra exp is given, and so on...)
        val overhitExp = overhitPercentage / 100 * normalExp

        // Return the rounded ammount of exp points to be added to the player's normal exp reward
        return Math.round(overhitExp)
    }

    /**
     * Sets state of the mob to seeded.
     * @param objectId : The player object id to check.
     */
    fun setSeeded(objectId: Int) {
        if (seed != null && seederId == objectId) {
            var count = 1

            for (skill in template.getSkills(SkillType.PASSIVE)) {
                when (skill.id) {
                    4303 // Strong type x2
                    -> count *= 2
                    4304 // Strong type x3
                    -> count *= 3
                    4305 // Strong type x4
                    -> count *= 4
                    4306 // Strong type x5
                    -> count *= 5
                    4307 // Strong type x6
                    -> count *= 6
                    4308 // Strong type x7
                    -> count *= 7
                    4309 // Strong type x8
                    -> count *= 8
                    4310 // Strong type x9
                    -> count *= 9
                }
            }

            val diff = level - seed!!.level - 5
            if (diff > 0)
                count += diff

            _harvestItems.add(IntIntHolder(seed!!.cropId, count * Config.RATE_DROP_MANOR))
        }
    }

    /**
     * Sets the seed parameters, but not the seed state.
     * @param seed - the seed.
     * @param objectId - the player objectId who is sowing the seed.
     */
    fun setSeeded(seed: Seed, objectId: Int) {
        if (this.seed == null) {
            this.seed = seed
            seederId = objectId
        }
    }

    class CommandChannelTimer(private val _monster: Attackable) : Runnable {

        override fun run() {
            if (System.currentTimeMillis() - _monster.commandChannelLastAttack > 900000) {
                _monster.commandChannelTimer = null
                _monster.firstCommandChannelAttacked = null
                _monster.commandChannelLastAttack = 0
            } else
                ThreadPool.schedule(this, 10000) // 10sec
        }
    }

    override fun addKnownObject(`object`: WorldObject) {
        if (`object` is Player && ai.desire.intention === CtrlIntention.IDLE)
            ai.setIntention(CtrlIntention.ACTIVE, null)
    }

    override fun removeKnownObject(`object`: WorldObject) {
        super.removeKnownObject(`object`)

        // remove object from agro list
        if (`object` is Creature)
            aggroList.remove(`object`)
    }
}