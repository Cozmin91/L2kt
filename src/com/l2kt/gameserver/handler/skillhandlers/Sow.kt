package com.l2kt.gameserver.handler.skillhandlers

import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.handler.ISkillHandler
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.instance.Monster
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.manor.Seed
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.PlaySound
import com.l2kt.gameserver.scripting.QuestState
import com.l2kt.gameserver.templates.skills.L2SkillType

class Sow : ISkillHandler {

    override val skillIds: Array<L2SkillType>
        get() = SKILL_IDS

    override fun useSkill(activeChar: Creature, skill: L2Skill, targets: Array<WorldObject>) {
        if (activeChar !is Player)
            return

        val `object` = targets[0] as? Monster ?: return

        if (`object`.isDead || !`object`.isSeeded || `object`.seederId != activeChar.objectId)
            return

        val seed = `object`.seed ?: return

        // Consuming used seed
        if (!activeChar.destroyItemByItemId("Consume", seed.seedId, 1, `object`, false))
            return

        val smId: SystemMessageId
        if (calcSuccess(activeChar, `object`, seed)) {
            activeChar.sendPacket(PlaySound(QuestState.SOUND_ITEMGET))
            `object`.setSeeded(activeChar.objectId)
            smId = SystemMessageId.THE_SEED_WAS_SUCCESSFULLY_SOWN
        } else
            smId = SystemMessageId.THE_SEED_WAS_NOT_SOWN

        val party = activeChar.party
        if (party == null)
            activeChar.sendPacket(smId)
        else
            party.broadcastMessage(smId)

        `object`.ai.setIntention(CtrlIntention.IDLE)
    }

    companion object {
        private val SKILL_IDS = arrayOf(L2SkillType.SOW)

        private fun calcSuccess(activeChar: Creature, target: Creature, seed: Seed): Boolean {
            val minlevelSeed = seed.level - 5
            val maxlevelSeed = seed.level + 5

            val levelPlayer = activeChar.level // Attacker Level
            val levelTarget = target.level // target Level

            var basicSuccess = if (seed.isAlternative) 20 else 90

            // Seed level
            if (levelTarget < minlevelSeed)
                basicSuccess -= 5 * (minlevelSeed - levelTarget)

            if (levelTarget > maxlevelSeed)
                basicSuccess -= 5 * (levelTarget - maxlevelSeed)

            // 5% decrease in chance if player level is more than +/- 5 levels to _target's_ level
            var diff = levelPlayer - levelTarget
            if (diff < 0)
                diff = -diff

            if (diff > 5)
                basicSuccess -= 5 * (diff - 5)

            // Chance can't be less than 1%
            if (basicSuccess < 1)
                basicSuccess = 1

            return Rnd[99] < basicSuccess
        }
    }
}