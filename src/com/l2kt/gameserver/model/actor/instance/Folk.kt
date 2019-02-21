package com.l2kt.gameserver.model.actor.instance

import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.data.xml.SkillTreeData
import com.l2kt.gameserver.model.L2Effect
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.*
import com.l2kt.gameserver.network.serverpackets.AcquireSkillList.AcquireSkillType
import com.l2kt.gameserver.skills.effects.EffectBuff
import com.l2kt.gameserver.skills.effects.EffectDebuff

open class Folk(objectId: Int, template: NpcTemplate) : Npc(objectId, template) {
    init {

        setIsMortal(false)
    }

    override fun addEffect(newEffect: L2Effect?) {
        if (newEffect is EffectDebuff || newEffect is EffectBuff)
            super.addEffect(newEffect)
        else newEffect?.stopEffectTask()
    }

    /**
     * This method displays SkillList to the player.
     * @param player The player who requested the method.
     */
    fun showSkillList(player: Player) {
        if (!template.canTeach(player.classId)) {
            val html = NpcHtmlMessage(objectId)
            html.setFile("data/html/trainer/" + template.npcId + "-noskills.htm")
            player.sendPacket(html)
            return
        }

        val skills = player.availableSkills
        if (skills.isEmpty()) {
            val minlevel = player.requiredLevelForNextSkill
            if (minlevel > 0)
                player.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN_S1).addNumber(
                        minlevel
                    )
                )
            else
                player.sendPacket(SystemMessageId.NO_MORE_SKILLS_TO_LEARN)
        } else
            player.sendPacket(AcquireSkillList(AcquireSkillType.USUAL, skills))

        player.sendPacket(ActionFailed.STATIC_PACKET)
    }

    /**
     * This method displays EnchantSkillList to the player.
     * @param player The player who requested the method.
     */
    fun showEnchantSkillList(player: Player) {
        if (!template.canTeach(player.classId)) {
            val html = NpcHtmlMessage(objectId)
            html.setFile("data/html/trainer/" + template.npcId + "-noskills.htm")
            player.sendPacket(html)
            return
        }

        if (player.classId.level() < 3) {
            val html = NpcHtmlMessage(objectId)
            html.setHtml("<html><body> You must have 3rd class change quest completed.</body></html>")
            player.sendPacket(html)
            return
        }

        val skills = SkillTreeData.getEnchantSkillsFor(player)
        if (skills.isEmpty()) {
            player.sendPacket(SystemMessageId.THERE_IS_NO_SKILL_THAT_ENABLES_ENCHANT)

            if (player.level < 74)
                player.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN_S1).addNumber(
                        74
                    )
                )
            else
                player.sendPacket(SystemMessageId.NO_MORE_SKILLS_TO_LEARN)
        } else
            player.sendPacket(ExEnchantSkillList(skills))

        player.sendPacket(ActionFailed.STATIC_PACKET)
    }

    fun giveBlessingSupport(player: Player?) {
        if (player == null)
            return

        // Select the player
        target = player

        // If the player is too high level, display a message and return
        if (player.level > 39 || player.classId.level() >= 2) {
            val html = NpcHtmlMessage(objectId)
            html.setHtml("<html><body>Newbie Guide:<br>I'm sorry, but you are not eligible to receive the protection blessing.<br1>It can only be bestowed on <font color=\"LEVEL\">characters below level 39 who have not made a seccond transfer.</font></body></html>")
            html.replace("%objectId%", objectId)
            player.sendPacket(html)
            return
        }
        doCast(SkillTable.FrequentSkill.BLESSING_OF_PROTECTION.skill)
    }

    override fun onBypassFeedback(player: Player, command: String) {
        if (command.startsWith("SkillList"))
            showSkillList(player)
        else if (command.startsWith("EnchantSkillList"))
            showEnchantSkillList(player)
        else if (command.startsWith("GiveBlessing"))
            giveBlessingSupport(player)
        else
            super.onBypassFeedback(player, command)
    }
}