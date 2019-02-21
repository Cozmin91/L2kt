package com.l2kt.gameserver.model.actor.instance

import com.l2kt.Config
import com.l2kt.gameserver.data.manager.FishingChampionshipManager
import com.l2kt.gameserver.data.xml.SkillTreeData
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.AcquireSkillList
import com.l2kt.gameserver.network.serverpackets.AcquireSkillList.AcquireSkillType
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage
import com.l2kt.gameserver.network.serverpackets.SystemMessage

/**
 * An instance type extending [Merchant], used for fishing event.
 */
class Fisherman(objectId: Int, template: NpcTemplate) : Merchant(objectId, template) {

    override fun getHtmlPath(npcId: Int, `val`: Int): String {
        var filename = ""

        if (`val` == 0)
            filename = "" + npcId
        else
            filename = "$npcId-$`val`"

        return "data/html/fisherman/$filename.htm"
    }

    override fun onBypassFeedback(player: Player, command: String) {
        // Generic PK check. Send back the HTM if found and cancel current action.
        if (!Config.KARMA_PLAYER_CAN_SHOP && player.karma > 0 && showPkDenyChatWindow(player, "fisherman"))
            return

        if (command.startsWith("FishSkillList"))
            showFishSkillList(player)
        else if (command.startsWith("FishingChampionship")) {
            if (!Config.ALT_FISH_CHAMPIONSHIP_ENABLED) {
                val html = NpcHtmlMessage(objectId)
                html.setFile("data/html/fisherman/championship/no_fish_event001.htm")
                player.sendPacket(html)
                return
            }
            FishingChampionshipManager.showChampScreen(player, objectId)
        } else if (command.startsWith("FishingReward")) {
            if (!Config.ALT_FISH_CHAMPIONSHIP_ENABLED) {
                val html = NpcHtmlMessage(objectId)
                html.setFile("data/html/fisherman/championship/no_fish_event001.htm")
                player.sendPacket(html)
                return
            }

            if (!FishingChampionshipManager.isWinner(player.name)) {
                val html = NpcHtmlMessage(objectId)
                html.setFile("data/html/fisherman/championship/no_fish_event_reward001.htm")
                player.sendPacket(html)
                return
            }
            FishingChampionshipManager.getReward(player)
        } else
            super.onBypassFeedback(player, command)
    }

    override fun showChatWindow(player: Player, `val`: Int) {
        // Generic PK check. Send back the HTM if found and cancel current action.
        if (!Config.KARMA_PLAYER_CAN_SHOP && player.karma > 0 && showPkDenyChatWindow(player, "fisherman"))
            return

        showChatWindow(player, getHtmlPath(npcId, `val`))
    }

    companion object {

        fun showFishSkillList(player: Player) {
            val skills = SkillTreeData.getFishingSkillsFor(player)
            if (skills.isEmpty()) {
                val minlevel = SkillTreeData.getRequiredLevelForNextFishingSkill(player)
                if (minlevel > 0)
                    player.sendPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.DO_NOT_HAVE_FURTHER_SKILLS_TO_LEARN_S1).addNumber(
                            minlevel
                        )
                    )
                else
                    player.sendPacket(SystemMessageId.NO_MORE_SKILLS_TO_LEARN)
            } else
                player.sendPacket(AcquireSkillList(AcquireSkillType.FISHING, skills))

            player.sendPacket(ActionFailed.STATIC_PACKET)
        }
    }
}