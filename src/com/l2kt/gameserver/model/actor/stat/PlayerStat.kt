package com.l2kt.gameserver.model.actor.stat

import com.l2kt.Config
import com.l2kt.commons.math.MathUtil
import com.l2kt.gameserver.data.manager.ZoneManager
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.RewardInfo
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Pet
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.actor.template.PetTemplate
import com.l2kt.gameserver.model.actor.template.PlayerTemplate
import com.l2kt.gameserver.model.base.Experience
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.model.zone.type.SwampZone
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.*
import com.l2kt.gameserver.skills.Stats

class PlayerStat(activeChar: Player) : PlayableStat(activeChar) {
    private var _oldMaxHp: Int = 0 // stats watch
    private var _oldMaxMp: Int = 0 // stats watch
    private var _oldMaxCp: Int = 0 // stats watch

    override var exp: Long
        get() = if (activeChar!!.isSubClassActive) activeChar!!.subClasses[activeChar!!.classIndex]!!.exp else super.exp
        set(value) = if (activeChar!!.isSubClassActive)
            activeChar!!.subClasses[activeChar!!.classIndex]!!.exp = value
        else
            super.exp = value

    override var level: Byte
        get() = if (activeChar!!.isSubClassActive) activeChar!!.subClasses[activeChar!!.classIndex]!!.level else super.level
        set(value) {
            var value = value
            if (value > Experience.MAX_LEVEL - 1)
                value = (Experience.MAX_LEVEL - 1).toByte()

            if (activeChar!!.isSubClassActive)
                activeChar!!.subClasses[activeChar!!.classIndex]!!.level = value
            else
                super.level = value
        }

    override// Get the Max CP (base+modifier) of the player
    // Launch a regen task if the new Max CP is higher than the old one
    // trigger start of regeneration
    val maxCp: Int
        get() {
            val `val` = calcStat(
                Stats.MAX_CP,
                (activeChar!!.template as PlayerTemplate).getBaseCpMax(activeChar!!.level),
                null,
                null
            ).toInt()
            if (`val` != _oldMaxCp) {
                _oldMaxCp = `val`
                if (activeChar!!.status.currentCp != `val`.toDouble())
                    activeChar!!.status.currentCp = activeChar!!.status.currentCp
            }
            return `val`
        }

    override// Get the Max HP (base+modifier) of the player
    // Launch a regen task if the new Max HP is higher than the old one
    // trigger start of regeneration
    val maxHp: Int
        get() {
            val `val` = super.maxHp
            if (`val` != _oldMaxHp) {
                _oldMaxHp = `val`
                if (activeChar!!.status.currentHp != `val`.toDouble())
                    activeChar!!.status.currentHp = activeChar!!.status.currentHp
            }

            return `val`
        }

    override// Get the Max MP (base+modifier) of the player
    // Launch a regen task if the new Max MP is higher than the old one
    // trigger start of regeneration
    val maxMp: Int
        get() {
            val `val` = super.maxMp

            if (`val` != _oldMaxMp) {
                _oldMaxMp = `val`
                if (activeChar!!.status.currentMp != `val`.toDouble())
                    activeChar!!.status.currentMp = activeChar!!.status.currentMp
            }

            return `val`
        }

    override var sp: Int
        get() = if (activeChar!!.isSubClassActive) activeChar!!.subClasses[activeChar!!.classIndex]!!.sp else super.sp
        set(value) {
            if (activeChar!!.isSubClassActive)
                activeChar!!.subClasses[activeChar!!.classIndex]!!.sp = value
            else
                super.sp = value

            val su = StatusUpdate(activeChar!!)
            su.addAttribute(StatusUpdate.SP, sp)
            activeChar!!.sendPacket(su)
        }

    override val baseRunSpeed: Int
        get() {
            if (activeChar!!.isMounted) {
                var base =
                    if (activeChar!!.isFlying) activeChar!!.petDataEntry!!.mountFlySpeed else activeChar!!.petDataEntry!!.mountBaseSpeed

                if (activeChar!!.level < activeChar!!.mountLevel)
                    base /= 2

                if (activeChar!!.checkFoodState(activeChar!!.petTemplate!!.hungryLimit))
                    base /= 2

                return base
            }

            return super.baseRunSpeed
        }

    val baseSwimSpeed: Int
        get() {
            if (activeChar!!.isMounted) {
                var base = activeChar!!.petDataEntry!!.mountSwimSpeed

                if (activeChar!!.level < activeChar!!.mountLevel)
                    base /= 2

                if (activeChar!!.checkFoodState(activeChar!!.petTemplate!!.hungryLimit))
                    base /= 2

                return base
            }

            return (activeChar!!.template as PlayerTemplate).baseSwimSpeed
        }

    override// get base value, use swimming speed in water
    // apply zone modifier before final calculation
    // apply armor grade penalty before final calculation
    // calculate speed
    val moveSpeed: Float
        get() {
            var baseValue =
                (if (activeChar!!.isInsideZone(ZoneId.WATER)) baseSwimSpeed else baseMoveSpeed).toFloat()
            if (activeChar!!.isInsideZone(ZoneId.SWAMP)) {
                val zone = ZoneManager.getZone(activeChar, SwampZone::class.java)
                if (zone != null)
                    baseValue *= ((100 + zone.moveBonus) / 100.0).toFloat()
            }
            val penalty = activeChar!!.expertiseArmorPenalty
            if (penalty > 0)
                baseValue *= Math.pow(0.84, penalty.toDouble()).toFloat()
            return calcStat(Stats.RUN_SPEED, baseValue.toDouble(), null, null).toFloat()
        }

    override val mAtkSpd: Int
        get() {
            var base = 333.0

            if (activeChar!!.isMounted) {
                if (activeChar!!.checkFoodState(activeChar!!.petTemplate!!.hungryLimit))
                    base /= 2.0
            }

            val penalty = activeChar!!.expertiseArmorPenalty
            if (penalty > 0)
                base *= Math.pow(0.84, penalty.toDouble())

            return calcStat(Stats.MAGIC_ATTACK_SPEED, base, null, null).toInt()
        }

    override val pAtkSpd: Int
        get() {
            if (activeChar!!.isFlying)
                return if (activeChar!!.checkFoodState(activeChar!!.petTemplate!!.hungryLimit)) 150 else 300

            if (activeChar!!.isRiding) {
                var base = activeChar!!.petDataEntry!!.mountAtkSpd

                if (activeChar!!.checkFoodState(activeChar!!.petTemplate!!.hungryLimit))
                    base /= 2

                return calcStat(Stats.POWER_ATTACK_SPEED, base.toDouble(), null, null).toInt()
            }

            return super.pAtkSpd
        }

    override val accuracy: Int
        get() {
            var `val` = super.accuracy

            if (activeChar!!.expertiseWeaponPenalty)
                `val` -= 20

            return `val`
        }

    override val physicalAttackRange: Int //TODO look into this
        get() = calcStat(Stats.POWER_ATTACK_RANGE, activeChar!!.attackType?.range?.toDouble() ?: 0.0, null, null).toInt()

    override fun addExp(value: Long): Boolean {
        // Allowed to gain exp?
        if (!activeChar!!.accessLevel.canGainExp)
            return false

        if (!super.addExp(value))
            return false

        activeChar!!.sendPacket(UserInfo(activeChar!!))
        return true
    }

    /**
     * Add Experience and SP rewards to the Player, remove its Karma (if necessary) and Launch increase level task.
     *
     *  * Remove Karma when the player kills L2MonsterInstance
     *  * Send StatusUpdate to the Player
     *  * Send a Server->Client System Message to the Player
     *  * If the Player increases its level, send SocialAction (broadcast)
     *  * If the Player increases its level, manage the increase level task (Max MP, Max MP, Recommandation, Expertise and beginner skills...)
     *  * If the Player increases its level, send UserInfo to the Player
     *
     * @param addToExp The Experience value to add
     * @param addToSp The SP value to add
     */
    override fun addExpAndSp(addToExp: Long, addToSp: Int): Boolean {
        if (!super.addExpAndSp(addToExp, addToSp))
            return false

        val sm: SystemMessage

        if (addToExp == 0L && addToSp > 0)
            sm = SystemMessage.getSystemMessage(SystemMessageId.ACQUIRED_S1_SP).addNumber(addToSp)
        else if (addToExp > 0 && addToSp == 0)
            sm = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1_EXPERIENCE).addNumber(addToExp.toInt())
        else
            sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_EARNED_S1_EXP_AND_S2_SP).addNumber(addToExp.toInt())
                .addNumber(addToSp)

        activeChar!!.sendPacket(sm)

        return true
    }

    /**
     * Add Experience and SP rewards to the Player, remove its Karma (if necessary) and Launch increase level task.
     *
     *  * Remove Karma when the player kills L2MonsterInstance
     *  * Send StatusUpdate to the Player
     *  * Send a Server->Client System Message to the Player
     *  * If the Player increases its level, send SocialAction (broadcast)
     *  * If the Player increases its level, manage the increase level task (Max MP, Max MP, Recommandation, Expertise and beginner skills...)
     *  * If the Player increases its level, send UserInfo to the Player
     *
     * @param addToExp The Experience value to add
     * @param addToSp The SP value to add
     * @param rewards The list of players and summons, who done damage
     * @return
     */
    fun addExpAndSp(addToExp: Long, addToSp: Int, rewards: Map<Creature, RewardInfo>): Boolean {
        var addToExp = addToExp
        var addToSp = addToSp
        // GM check concerning canGainExp().
        if (!activeChar!!.accessLevel.canGainExp)
            return false

        // If this player has a pet, give the xp to the pet now (if any).
        if (activeChar!!.hasPet()) {
            val pet = activeChar!!.pet as Pet
            if (pet.stat.exp <= (pet.template as PetTemplate).getPetDataEntry(81)!!.maxExp + 10000 && !pet.isDead()) {
                if (MathUtil.checkIfInShortRadius(Config.PARTY_RANGE, pet, activeChar, true)) {
                    var ratio = pet.petData.expType
                    var petExp: Long = 0
                    var petSp = 0
                    if (ratio == -1) {
                        val r = rewards[pet]
                        val reward = rewards[activeChar!!]
                        if (r != null && reward != null) {
                            val damageDoneByPet = r.damage.toDouble() / reward.damage
                            petExp = (addToExp * damageDoneByPet).toLong()
                            petSp = (addToSp * damageDoneByPet).toInt()
                        }
                    } else {
                        // now adjust the max ratio to avoid the owner earning negative exp/sp
                        if (ratio > 100)
                            ratio = 100

                        petExp = Math.round(addToExp * (1 - ratio / 100.0))
                        petSp = Math.round(addToSp * (1 - ratio / 100.0)).toInt()
                    }

                    addToExp -= petExp
                    addToSp -= petSp
                    pet.addExpAndSp(petExp, petSp)
                }
            }
        }
        return addExpAndSp(addToExp, addToSp)
    }

    override fun removeExpAndSp(removeExp: Long, removeSp: Int): Boolean {
        return removeExpAndSp(removeExp, removeSp, true)
    }

    fun removeExpAndSp(removeExp: Long, removeSp: Int, sendMessage: Boolean): Boolean {
        val oldLevel = level.toInt()

        if (!super.removeExpAndSp(removeExp, removeSp))
            return false

        // Send messages.
        if (sendMessage) {
            if (removeExp > 0)
                activeChar!!.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.EXP_DECREASED_BY_S1).addNumber(
                        removeExp.toInt()
                    )
                )

            if (removeSp > 0)
                activeChar!!.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.SP_DECREASED_S1).addNumber(
                        removeSp
                    )
                )

            if (level < oldLevel)
                activeChar!!.broadcastStatusUpdate()
        }
        return true
    }

    override fun addLevel(value: Byte): Boolean {
        if (level + value > Experience.MAX_LEVEL - 1)
            return false

        val levelIncreased = super.addLevel(value)

        if (levelIncreased) {
            if (!Config.DISABLE_TUTORIAL) {
                val qs = activeChar!!.getQuestState("Tutorial")
                qs?.quest?.notifyEvent("CE40", null, activeChar)
            }

            activeChar!!.currentCp = maxCp.toDouble()
            activeChar!!.broadcastPacket(SocialAction(activeChar!!, 15))
            activeChar!!.sendPacket(SystemMessageId.YOU_INCREASED_YOUR_LEVEL)
        }

        // Refresh player skills (autoGet skills or all available skills if Config.AUTO_LEARN_SKILLS is activated).
        activeChar!!.giveSkills()

        val clan = activeChar!!.clan
        if (clan != null) {
            val member = clan.getClanMember(activeChar!!.objectId)
            member?.refreshLevel()

            clan.broadcastToOnlineMembers(PledgeShowMemberListUpdate(activeChar!!))
        }

        // Recalculate the party level
        val party = activeChar!!.party
        party?.recalculateLevel()

        // Update the overloaded status of the player
        activeChar!!.refreshOverloaded()
        // Update the expertise status of the player
        activeChar!!.refreshExpertisePenalty()
        // Send UserInfo to the player
        activeChar!!.sendPacket(UserInfo(activeChar!!))

        return levelIncreased
    }

    override fun getExpForLevel(level: Int): Long {
        return Experience.LEVEL[level]
    }
    
    override val activeChar: Player? get() = super.activeChar as Player?

    override fun getMAtk(target: Creature?, skill: L2Skill?): Int {
        if (activeChar!!.isMounted) {
            var base = activeChar!!.petDataEntry!!.mountMAtk

            if (activeChar!!.level < activeChar!!.mountLevel)
                base /= 2.0

            return calcStat(Stats.MAGIC_ATTACK, base, null, null).toInt()
        }

        return super.getMAtk(target, skill)
    }

    override fun getPAtk(target: Creature?): Int {
        if (activeChar!!.isMounted) {
            var base = activeChar!!.petDataEntry!!.mountPAtk

            if (activeChar!!.level < activeChar!!.mountLevel)
                base /= 2.0

            return calcStat(Stats.POWER_ATTACK, base, null, null).toInt()
        }

        return super.getPAtk(target)
    }

    override fun getEvasionRate(target: Creature?): Int {
        var `val` = super.getEvasionRate(target)

        val penalty = activeChar!!.expertiseArmorPenalty
        if (penalty > 0)
            `val` -= 2 * penalty

        return `val`
    }
}