package com.l2kt.gameserver.network.clientpackets

import com.l2kt.commons.math.MathUtil
import com.l2kt.commons.random.Rnd
import com.l2kt.commons.util.ArraysUtil
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.ai.type.SummonAI
import com.l2kt.gameserver.model.actor.instance.*
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.network.serverpackets.NpcSay

class RequestActionUse : L2GameClientPacket() {

    private var _actionId: Int = 0
    private var _ctrlPressed: Boolean = false
    private var _shiftPressed: Boolean = false

    override fun readImpl() {
        _actionId = readD()
        _ctrlPressed = readD() == 1
        _shiftPressed = readC() == 1
    }

    override fun runImpl() {
        val player = client.activeChar ?: return

        // Dont do anything if player is dead, or use fakedeath using another action than sit.
        if (player.isFakeDeath && _actionId != 0 || player.isDead || player.isOutOfControl) {
            player.sendPacket(ActionFailed.STATIC_PACKET)
            return
        }

        val pet = player.pet
        val target = player.target

        when (_actionId) {
            0 -> player.tryToSitOrStand(target, player.isSitting)

            1 -> {
                // Player is mounted, do not allow to change movement type.
                if (player.isMounted)
                    return

                if (player.isRunning)
                    player.setWalking()
                else
                    player.setRunning()
            }

            10 // Private Store - Sell
            -> player.tryOpenPrivateSellStore(false)

            28 // Private Store - Buy
            -> player.tryOpenPrivateBuyStore()

            15, 21 // Change Movement Mode (pet follow/stop)
            -> {
                if (pet == null)
                    return

                // You can't order anymore your pet to stop if distance is superior to 2000.
                if (pet.followStatus && MathUtil.calculateDistance(player, pet, true) > 2000)
                    return

                if (pet.isOutOfControl) {
                    player.sendPacket(SystemMessageId.PET_REFUSING_ORDER)
                    return
                }

                (pet.ai as SummonAI).notifyFollowStatusChange()
            }

            16, 22 // Attack (pet attack)
            -> {
                if (target !is Creature || pet == null || pet === target || player == target)
                    return

                // Sin eater, Big Boom, Wyvern can't attack with attack button.
                if (ArraysUtil.contains(PASSIVE_SUMMONS, pet.npcId))
                    return

                if (pet.isOutOfControl) {
                    player.sendPacket(SystemMessageId.PET_REFUSING_ORDER)
                    return
                }

                if (pet.isAttackingDisabled) {
                    if (pet.attackEndTime <= System.currentTimeMillis())
                        return

                    pet.ai.setIntention(CtrlIntention.ATTACK, target)
                }

                if (pet is Pet && pet.level - player.level > 20) {
                    player.sendPacket(SystemMessageId.PET_TOO_HIGH_TO_CONTROL)
                    return
                }

                if (player.isInOlympiadMode && !player.isOlympiadStart)
                    return

                pet.target = target

                // Summons can attack NPCs even when the owner cannot.
                if (!target.isAutoAttackable(player) && !_ctrlPressed && target !is Folk) {
                    pet.followStatus = false
                    pet.ai.setIntention(CtrlIntention.FOLLOW, target)
                    player.sendPacket(SystemMessageId.INCORRECT_TARGET)
                    return
                }

                if (target is Door) {
                    if (target.isAutoAttackable(player) && pet.npcId != SiegeSummon.SWOOP_CANNON_ID)
                        pet.ai.setIntention(CtrlIntention.ATTACK, target)
                } else if (pet.npcId != SiegeSummon.SIEGE_GOLEM_ID) {
                    if (Creature.isInsidePeaceZone(pet, target)) {
                        pet.followStatus = false
                        pet.ai.setIntention(CtrlIntention.FOLLOW, target)
                    } else
                        pet.ai.setIntention(CtrlIntention.ATTACK, target)
                }// siege golem AI doesn't support attacking other than doors at the moment
            }

            17, 23 // Stop (pet - cancel action)
            -> {
                if (pet == null)
                    return

                if (pet.isOutOfControl) {
                    player.sendPacket(SystemMessageId.PET_REFUSING_ORDER)
                    return
                }

                pet.ai.setIntention(CtrlIntention.ACTIVE, null)
            }

            19 // Returns pet to control item
            -> {
                if (pet == null || pet !is Pet)
                    return

                if (pet.isDead)
                    player.sendPacket(SystemMessageId.DEAD_PET_CANNOT_BE_RETURNED)
                else if (pet.isOutOfControl)
                    player.sendPacket(SystemMessageId.PET_REFUSING_ORDER)
                else if (pet.isAttackingNow || pet.isInCombat)
                    player.sendPacket(SystemMessageId.PET_CANNOT_SENT_BACK_DURING_BATTLE)
                else if (pet.checkUnsummonState())
                    player.sendPacket(SystemMessageId.YOU_CANNOT_RESTORE_HUNGRY_PETS)
                else
                    pet.unSummon(player)
            }

            38 // pet mount/dismount
            -> player.mountPlayer(pet)

            32 // Wild Hog Cannon - Mode Change
            -> {
            }

            36 // Soulless - Toxic Smoke
            -> useSkill(4259, target)

            37 // Dwarven Manufacture
            -> player.tryOpenWorkshop(true)

            39 // Soulless - Parasite Burst
            -> useSkill(4138, target)

            41 // Wild Hog Cannon - Attack
            -> {
                if (target !is Door) {
                    player.sendPacket(SystemMessageId.INCORRECT_TARGET)
                    return
                }

                useSkill(4230, target)
            }

            42 // Kai the Cat - Self Damage Shield
            -> useSkill(4378, player)

            43 // Unicorn Merrow - Hydro Screw
            -> useSkill(4137, target)

            44 // Big Boom - Boom Attack
            -> useSkill(4139, target)

            45 // Unicorn Boxer - Master Recharge
            -> useSkill(4025, player)

            46 // Mew the Cat - Mega Storm Strike
            -> useSkill(4261, target)

            47 // Silhouette - Steal Blood
            -> useSkill(4260, target)

            48 // Mechanic Golem - Mech. Cannon
            -> useSkill(4068, target)

            51 // General Manufacture
            -> player.tryOpenWorkshop(false)

            52 // Unsummon a servitor
            -> {
                if (pet == null || pet !is Servitor)
                    return

                if (pet.isDead)
                    player.sendPacket(SystemMessageId.DEAD_PET_CANNOT_BE_RETURNED)
                else if (pet.isOutOfControl)
                    player.sendPacket(SystemMessageId.PET_REFUSING_ORDER)
                else if (pet.isAttackingNow || pet.isInCombat)
                    player.sendPacket(SystemMessageId.PET_CANNOT_SENT_BACK_DURING_BATTLE)
                else
                    pet.unSummon(player)
            }

            53 // move to target
                , 54 // move to target hatch/strider
            -> {
                if (target == null || pet == null || pet === target)
                    return

                if (pet.isOutOfControl) {
                    player.sendPacket(SystemMessageId.PET_REFUSING_ORDER)
                    return
                }

                pet.followStatus = false
                pet.ai.setIntention(CtrlIntention.MOVE_TO, Location(target.x, target.y, target.z))
            }

            61 // Private Store Package Sell
            -> player.tryOpenPrivateSellStore(true)

            1000 // Siege Golem - Siege Hammer
            -> {
                if (target !is Door) {
                    player.sendPacket(SystemMessageId.INCORRECT_TARGET)
                    return
                }

                useSkill(4079, target)
            }

            1001 // Sin Eater - Ultimate Bombastic Buster
            -> if (useSkill(4139, pet) && pet!!.npcId == SIN_EATER_ID && Rnd[100] < 10)
                pet.broadcastPacket(
                    NpcSay(
                        pet.objectId,
                        Say2.ALL,
                        pet.npcId,
                        SIN_EATER_ACTIONS_STRINGS[Rnd[SIN_EATER_ACTIONS_STRINGS.size]]
                    )
                )

            1003 // Wind Hatchling/Strider - Wild Stun
            -> useSkill(4710, target)

            1004 // Wind Hatchling/Strider - Wild Defense
            -> useSkill(4711, player)

            1005 // Star Hatchling/Strider - Bright Burst
            -> useSkill(4712, target)

            1006 // Star Hatchling/Strider - Bright Heal
            -> useSkill(4713, player)

            1007 // Cat Queen - Blessing of Queen
            -> useSkill(4699, player)

            1008 // Cat Queen - Gift of Queen
            -> useSkill(4700, player)

            1009 // Cat Queen - Cure of Queen
            -> useSkill(4701, target)

            1010 // Unicorn Seraphim - Blessing of Seraphim
            -> useSkill(4702, player)

            1011 // Unicorn Seraphim - Gift of Seraphim
            -> useSkill(4703, player)

            1012 // Unicorn Seraphim - Cure of Seraphim
            -> useSkill(4704, target)

            1013 // Nightshade - Curse of Shade
            -> useSkill(4705, target)

            1014 // Nightshade - Mass Curse of Shade
            -> useSkill(4706, player)

            1015 // Nightshade - Shade Sacrifice
            -> useSkill(4707, target)

            1016 // Cursed Man - Cursed Blow
            -> useSkill(4709, target)

            1017 // Cursed Man - Cursed Strike/Stun
            -> useSkill(4708, target)

            1031 // Feline King - Slash
            -> useSkill(5135, target)

            1032 // Feline King - Spinning Slash
            -> useSkill(5136, target)

            1033 // Feline King - Grip of the Cat
            -> useSkill(5137, target)

            1034 // Magnus the Unicorn - Whiplash
            -> useSkill(5138, target)

            1035 // Magnus the Unicorn - Tridal Wave
            -> useSkill(5139, target)

            1036 // Spectral Lord - Corpse Kaboom
            -> useSkill(5142, target)

            1037 // Spectral Lord - Dicing Death
            -> useSkill(5141, target)

            1038 // Spectral Lord - Force Curse
            -> useSkill(5140, target)

            1039 // Swoop Cannon - Cannon Fodder
            -> {
                if (target is Door) {
                    player.sendPacket(SystemMessageId.INCORRECT_TARGET)
                    return
                }

                useSkill(5110, target)
            }

            1040 // Swoop Cannon - Big Bang
            -> {
                if (target is Door) {
                    player.sendPacket(SystemMessageId.INCORRECT_TARGET)
                    return
                }

                useSkill(5111, target)
            }

            else -> L2GameClientPacket.LOGGER.warn(
                "Unhandled action type {} detected for {}.",
                _actionId,
                player.name
            )
        }// useSkill(4230);
    }

    /**
     * Cast a skill for active pet/servitor.
     * @param skillId The id of the skill to launch.
     * @param target The target is specified as a parameter but can be overwrited or ignored depending on skill type.
     * @return true if you can use the skill, false otherwise.
     */
    private fun useSkill(skillId: Int, target: WorldObject?): Boolean {
        val activeChar = client.activeChar

        // No owner, or owner in shop mode.
        if (activeChar == null || activeChar.isInStoreMode)
            return false

        val activeSummon = activeChar.pet ?: return false

        // Pet which is 20 levels higher than owner.
        if (activeSummon is Pet && activeSummon.level - activeChar.level > 20) {
            activeChar.sendPacket(SystemMessageId.PET_TOO_HIGH_TO_CONTROL)
            return false
        }

        // Out of control pet.
        if (activeSummon.isOutOfControl) {
            activeChar.sendPacket(SystemMessageId.PET_REFUSING_ORDER)
            return false
        }

        // Verify if the launched skill is mastered by the summon.
        val skill = activeSummon.getSkill(skillId) ?: return false

        // Can't launch offensive skills on owner.
        if (skill.isOffensive && activeChar == target)
            return false

        activeSummon.target = target
        return activeSummon.useMagic(skill, _ctrlPressed, _shiftPressed)
    }

    companion object {
        private val PASSIVE_SUMMONS = intArrayOf(
            12564,
            12621,
            14702,
            14703,
            14704,
            14705,
            14706,
            14707,
            14708,
            14709,
            14710,
            14711,
            14712,
            14713,
            14714,
            14715,
            14716,
            14717,
            14718,
            14719,
            14720,
            14721,
            14722,
            14723,
            14724,
            14725,
            14726,
            14727,
            14728,
            14729,
            14730,
            14731,
            14732,
            14733,
            14734,
            14735,
            14736
        )

        private const val SIN_EATER_ID = 12564
        private val SIN_EATER_ACTIONS_STRINGS = arrayOf(
            "special skill? Abuses in this kind of place, can turn blood Knots...!",
            "Hey! Brother! What do you anticipate to me?",
            "shouts ha! Flap! Flap! Response?",
            ", has not hit...!"
        )
    }
}