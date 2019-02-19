package com.l2kt.gameserver.model.olympiad

import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.data.xml.MapRegionData
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.instance.Pet
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.group.Party
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.model.zone.type.OlympiadStadiumZone
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ExOlympiadMode
import com.l2kt.gameserver.network.serverpackets.InventoryUpdate
import com.l2kt.gameserver.network.serverpackets.L2GameServerPacket
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import java.util.logging.Level
import java.util.logging.Logger

abstract class AbstractOlympiadGame protected constructor(val stadiumId: Int) {

    protected var _startTime: Long = 0
    var isAborted = false
        protected set

    protected val _log = Logger.getLogger(AbstractOlympiadGame::class.java.name)

    protected val POINTS = "olympiad_points"
    protected val COMP_DONE = "competitions_done"
    protected val COMP_WON = "competitions_won"
    protected val COMP_LOST = "competitions_lost"
    protected val COMP_DRAWN = "competitions_drawn"

    abstract val type: CompetitionType

    abstract val playerNames: Array<String>

    protected abstract val divider: Int

    protected abstract val reward: Array<IntArray>

    open fun makeCompetitionStart(): Boolean {
        _startTime = System.currentTimeMillis()
        return !isAborted
    }

    protected fun addPointsToParticipant(par: Participant, points: Int) {
        par.updateStat(POINTS, points)
        val sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_GAINED_S2_OLYMPIAD_POINTS)
        sm.addString(par.name)
        sm.addNumber(points)
        broadcastPacket(sm)
    }

    protected fun removePointsFromParticipant(par: Participant, points: Int) {
        par.updateStat(POINTS, -points)
        val sm = SystemMessage.getSystemMessage(SystemMessageId.S1_HAS_LOST_S2_OLYMPIAD_POINTS)
        sm.addString(par.name)
        sm.addNumber(points)
        broadcastPacket(sm)
    }

    abstract fun containsParticipant(playerId: Int): Boolean

    abstract fun sendOlympiadInfo(player: Creature)

    abstract fun broadcastOlympiadInfo(stadium: OlympiadStadiumZone)

    abstract fun broadcastPacket(packet: L2GameServerPacket)

    abstract fun checkDefaulted(): Boolean

    abstract fun removals()

    abstract fun buffPlayers()

    abstract fun healPlayers()

    abstract fun portPlayersToArena(spawns: List<Location>): Boolean

    abstract fun cleanEffects()

    abstract fun portPlayersBack()

    abstract fun playersStatusBack()

    abstract fun clearPlayers()

    abstract fun handleDisconnect(player: Player)

    abstract fun resetDamage()

    abstract fun addDamage(player: Player, damage: Int)

    abstract fun checkBattleStatus(): Boolean

    abstract fun haveWinner(): Boolean

    abstract fun validateWinner(stadium: OlympiadStadiumZone)

    /**
     * Return null if player passed all checks or broadcast the reason to opponent.
     * @param player to check.
     * @return null or reason.
     */
    protected fun checkDefaulted(player: Player?): SystemMessage? {
        if (player == null || !player.isOnline)
            return SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_ENDS_THE_GAME)

        if (player.client == null || player.client!!.isDetached)
            return SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_ENDS_THE_GAME)

        // safety precautions
        if (player.isInObserverMode)
            return SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME)

        if (player.isDead()) {
            player.sendPacket(SystemMessageId.CANNOT_PARTICIPATE_OLYMPIAD_WHILE_DEAD)
            return SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME)
        }

        if (player.isSubClassActive) {
            player.sendPacket(SystemMessageId.SINCE_YOU_HAVE_CHANGED_YOUR_CLASS_INTO_A_SUB_JOB_YOU_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD)
            return SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME)
        }

        if (player.isCursedWeaponEquipped) {
            player.sendPacket(
                SystemMessage.getSystemMessage(SystemMessageId.CANNOT_JOIN_OLYMPIAD_POSSESSING_S1).addItemName(
                    player.cursedWeaponEquippedId
                )
            )
            return SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME)
        }

        if (player.inventoryLimit * 0.8 <= player.inventory!!.size) {
            player.sendPacket(SystemMessageId.SINCE_80_PERCENT_OR_MORE_OF_YOUR_INVENTORY_SLOTS_ARE_FULL_YOU_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD)
            return SystemMessage.getSystemMessage(SystemMessageId.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME)
        }

        return null
    }

    protected fun portPlayerToArena(par: Participant, loc: Location, id: Int): Boolean {
        val player = par.player
        if (player == null || !player.isOnline)
            return false

        try {
            player.savedLocation.set(player.position)

            player.target = null

            player.olympiadGameId = id
            player.setOlympiadMode(true)
            player.isOlympiadStart = false
            player.olympiadSide = par.side
            player.teleToLocation(loc, 0)
            player.sendPacket(ExOlympiadMode(par.side))
        } catch (e: Exception) {
            _log.log(Level.WARNING, e.message, e)
            return false
        }

        return true
    }

    protected fun removals(player: Player?, removeParty: Boolean) {
        try {
            if (player == null)
                return

            // Remove Buffs
            player.stopAllEffectsExceptThoseThatLastThroughDeath()

            // Remove Clan Skills
            if (player.clan != null) {
                for (skill in player.clan!!.clanSkills.values)
                    player.removeSkill(skill.id, false)
            }

            // Abort casting if player casting
            player.abortAttack()
            player.abortCast()

            // Force the character to be visible
            player.appearance.setVisible()

            // Remove Hero Skills
            if (player.isHero) {
                for (skill in SkillTable.heroSkills.filterNotNull())
                    player.removeSkill(skill.id, false)
            }

            // Heal Player fully
            player.currentCp = player.maxCp.toDouble()
            player.currentHp = player.maxHp.toDouble()
            player.currentMp = player.maxMp.toDouble()

            // Dismount player, if mounted.
            if (player.isMounted)
                player.dismount()
            else {
                val summon = player.pet

                // Unsummon pets directly.
                if (summon is Pet)
                    summon.unSummon(player)
                else if (summon != null) {
                    summon.stopAllEffectsExceptThoseThatLastThroughDeath()
                    summon.abortAttack()
                    summon.abortCast()
                }// Remove servitor buffs and cancel animations.
            }// Test summon existence, if any.

            // stop any cubic that has been given by other player.
            player.stopCubicsByOthers()

            // Remove player from his party
            if (removeParty) {
                val party = player.party
                party?.removePartyMember(player, Party.MessageType.EXPELLED)
            }

            player.checkItemRestriction()

            // Remove shot automation
            player.disableAutoShotsAll()

            // Discharge any active shots
            val item = player.activeWeaponInstance
            item?.unChargeAllShots()

            player.sendSkillList()
        } catch (e: Exception) {
            _log.log(Level.WARNING, e.message, e)
        }

    }

    /**
     * Buff the player. WW2 for fighter/mage + haste 1 if fighter.
     * @param player : the happy benefactor.
     */
    protected fun buffPlayer(player: Player) {
        var skill = SkillTable.getInfo(1204, 2) // Windwalk 2
        if (skill != null) {
            skill.getEffects(player, player)
            player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(1204))
        }

        if (!player.isMageClass) {
            skill = SkillTable.getInfo(1086, 1) // Haste 1
            if (skill != null) {
                skill.getEffects(player, player)
                player.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT).addSkillName(
                        1086
                    )
                )
            }
        }
    }

    /**
     * Heal the player.
     * @param player : the happy benefactor.
     */
    protected fun healPlayer(player: Player) {
        player.currentCp = player.maxCp.toDouble()
        player.currentHp = player.maxHp.toDouble()
        player.currentMp = player.maxMp.toDouble()
    }

    protected fun cleanEffects(player: Player) {
        try {
            // prevent players kill each other
            player.isOlympiadStart = false
            player.target = null
            player.abortAttack()
            player.abortCast()
            player.ai.setIntention(CtrlIntention.IDLE)

            if (player.isDead())
                player.setIsDead(false)

            val summon = player.pet
            if (summon != null && !summon.isDead()) {
                summon.target = null
                summon.abortAttack()
                summon.abortCast()
                summon.ai.setIntention(CtrlIntention.IDLE)
            }

            player.currentCp = player.maxCp.toDouble()
            player.currentHp = player.maxHp.toDouble()
            player.currentMp = player.maxMp.toDouble()
            player.status.startHpMpRegeneration()
        } catch (e: Exception) {
            _log.log(Level.WARNING, e.message, e)
        }

    }

    protected fun playerStatusBack(player: Player) {
        try {
            player.setOlympiadMode(false)
            player.isOlympiadStart = false
            player.olympiadSide = -1
            player.olympiadGameId = -1
            player.sendPacket(ExOlympiadMode(0))

            player.stopAllEffectsExceptThoseThatLastThroughDeath()
            player.clearCharges()

            val summon = player.pet
            if (summon != null && !summon.isDead())
                summon.stopAllEffectsExceptThoseThatLastThroughDeath()

            // Add Clan Skills
            if (player.clan != null) {
                player.clan!!.addSkillEffects(player)

                // heal again after adding clan skills
                player.currentCp = player.maxCp.toDouble()
                player.currentHp = player.maxHp.toDouble()
                player.currentMp = player.maxMp.toDouble()
            }

            // Add Hero Skills
            if (player.isHero) {
                for (skill in SkillTable.heroSkills)
                    player.addSkill(skill, false)
            }
            player.sendSkillList()
        } catch (e: Exception) {
            _log.log(Level.WARNING, e.message, e)
        }

    }

    protected fun portPlayerBack(player: Player?) {
        if (player == null)
            return

        var loc: Location? = player.savedLocation
        if (loc == Location.DUMMY_LOC)
            return

        val town = MapRegionData.getTown(loc!!.x, loc.y, loc.z)
        if (town != null)
            loc = town.randomLoc

        player.teleToLocation(loc, 0)
        player.savedLocation.clean()
    }

    fun rewardParticipant(player: Player?, reward: Array<IntArray>?) {
        if (player == null || !player.isOnline || reward == null)
            return

        try {
            val iu = InventoryUpdate()
            for (it in reward) {
                if (it == null || it.size != 2)
                    continue

                val item = player.inventory!!.addItem("Olympiad", it[0], it[1], player, null) ?: continue

                iu.addModifiedItem(item)
                player.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(it[0]).addNumber(
                        it[1]
                    )
                )
            }
            player.sendPacket(iu)
        } catch (e: Exception) {
            _log.log(Level.WARNING, e.message, e)
        }

    }
}