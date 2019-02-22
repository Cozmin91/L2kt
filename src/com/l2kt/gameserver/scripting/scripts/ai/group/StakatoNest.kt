package com.l2kt.gameserver.scripting.scripts.ai.group

import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.Attackable
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Monster
import com.l2kt.gameserver.network.serverpackets.MagicSkillUse
import com.l2kt.gameserver.scripting.scripts.ai.L2AttackableAIScript

/**
 * This AI handles following behaviors :
 *
 *  * Cannibalistic Stakato Leader : try to eat a Follower, if any around, at low HPs.
 *  * Female Spiked Stakato : when Male dies, summons 3 Spiked Stakato Guards.
 *  * Male Spiked Stakato : when Female dies, transforms in stronger form.
 *  * Spiked Stakato Baby : when Spiked Stakato Nurse dies, her baby summons 3 Spiked Stakato Captains.
 *  * Spiked Stakato Nurse : when Spiked Stakato Baby dies, transforms in stronger form.
 *
 * As NCSoft implemented it on postIL, but skills exist since IL, I decided to implemented that script to "honor" the idea (which is kinda funny).
 */
class StakatoNest : L2AttackableAIScript("ai/group") {

    override fun registerNpcs() {
        addAttackId(CANNIBALISTIC_STAKATO_LEADER_1, CANNIBALISTIC_STAKATO_LEADER_2)
        addKillId(MALE_SPIKED_STAKATO_1, FEMALE_SPIKED_STAKATO, SPIKED_STAKATO_NURSE_1, SPIKED_STAKATO_BABY)
    }

    override fun onAttack(npc: Npc, attacker: Creature, damage: Int, skill: L2Skill?): String? {
        if (npc.currentHp / npc.maxHp < 0.3 && Rnd[100] < 5) {
            for (follower in npc.getKnownTypeInRadius(Monster::class.java, 400)) {
                if (follower.npcId == STAKATO_FOLLOWER && !follower.isDead) {
                    npc.setIsCastingNow(true)
                    npc.broadcastPacket(
                        MagicSkillUse(
                            npc,
                            follower,
                            if (npc.npcId == CANNIBALISTIC_STAKATO_LEADER_2) 4072 else 4073,
                            1,
                            3000,
                            0
                        )
                    )
                    ThreadPool.schedule(Runnable{
                        if (npc.isDead)
                            return@Runnable

                        if (follower.isDead) {
                            npc.setIsCastingNow(false)
                            return@Runnable
                        }

                        npc.currentHp = npc.currentHp + follower.currentHp / 2
                        follower.doDie(follower)
                        npc.setIsCastingNow(false)
                    }, 3000L)
                    break
                }
            }
        }
        return super.onAttack(npc, attacker, damage, skill)
    }

    override fun onKill(npc: Npc, killer: Creature?): String? {
        when (npc.npcId) {
            MALE_SPIKED_STAKATO_1 -> for (angryFemale in npc.getKnownTypeInRadius(Monster::class.java, 400)) {
                if (angryFemale.npcId == FEMALE_SPIKED_STAKATO && !angryFemale.isDead) {
                    for (i in 0..2) {
                        val guard = addSpawn(SPIKED_STAKATO_GUARD, angryFemale, true, 0, false)
                        attack(guard as Attackable, killer)
                    }
                }
            }

            FEMALE_SPIKED_STAKATO -> for (morphingMale in npc.getKnownTypeInRadius(Monster::class.java, 400)) {
                if (morphingMale.npcId == MALE_SPIKED_STAKATO_1 && !morphingMale.isDead) {
                    val newForm = addSpawn(MALE_SPIKED_STAKATO_2, morphingMale, true, 0, false)
                    attack(newForm as Attackable, killer)

                    morphingMale.deleteMe()
                }
            }

            SPIKED_STAKATO_NURSE_1 -> for (baby in npc.getKnownTypeInRadius(Monster::class.java, 400)) {
                if (baby.npcId == SPIKED_STAKATO_BABY && !baby.isDead) {
                    for (i in 0..2) {
                        val captain = addSpawn(SPIKED_STAKATO_CAPTAIN, baby, true, 0, false)
                        attack(captain as Attackable, killer)
                    }
                }
            }

            SPIKED_STAKATO_BABY -> for (morphingNurse in npc.getKnownTypeInRadius(Monster::class.java, 400)) {
                if (morphingNurse.npcId == SPIKED_STAKATO_NURSE_1 && !morphingNurse.isDead) {
                    val newForm = addSpawn(SPIKED_STAKATO_NURSE_2, morphingNurse, true, 0, false)
                    attack(newForm as Attackable, killer)

                    morphingNurse.deleteMe()
                }
            }
        }
        return super.onKill(npc, killer)
    }

    companion object {
        private const val SPIKED_STAKATO_GUARD = 22107
        private const val FEMALE_SPIKED_STAKATO = 22108
        private const val MALE_SPIKED_STAKATO_1 = 22109
        private const val MALE_SPIKED_STAKATO_2 = 22110

        private const val STAKATO_FOLLOWER = 22112
        private const val CANNIBALISTIC_STAKATO_LEADER_1 = 22113
        private const val CANNIBALISTIC_STAKATO_LEADER_2 = 22114

        private const val SPIKED_STAKATO_CAPTAIN = 22117
        private const val SPIKED_STAKATO_NURSE_1 = 22118
        private const val SPIKED_STAKATO_NURSE_2 = 22119
        private const val SPIKED_STAKATO_BABY = 22120
    }
}