package com.l2kt.gameserver.model.actor.instance

import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.data.xml.MultisellData
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage
import java.util.*

/**
 * This instance leads behaviors of Golden Ram mofos, where shown htm is different according to your quest condition. Abercrombie shows you multisells, Selina shows you Buffs list, when Pierce shows you "Quest" link.<br></br>
 * <br></br>
 * Kahman shows you only different htm. He's enthusiastic lazy-ass.
 */
class GoldenRamMercenary(objectId: Int, template: NpcTemplate) : Folk(objectId, template) {

    override fun showChatWindow(player: Player, `val`: Int) {
        val npcId = npcId
        var filename = "data/html/default/$npcId.htm"

        val st = player.getQuestState(qn)
        if (st != null) {
            val cond = st.getInt("cond")

            when (npcId) {
                31553, 31554 ->
                    // Captain Pierce && Kahman ; different behavior if you got at least one badge.
                    if (cond >= 2)
                        filename = "data/html/default/$npcId-1.htm"

                31555, 31556 ->
                    // Abercrombie and Selina
                    if (cond == 2)
                        filename = "data/html/default/$npcId-1.htm"
                    else if (cond == 3)
                        filename = "data/html/default/$npcId-2.htm"
            }
        }

        val html = NpcHtmlMessage(objectId)
        html.setFile(filename)
        html.replace("%objectId%", objectId)
        player.sendPacket(html)
        player.sendPacket(ActionFailed.STATIC_PACKET)
    }

    override fun onBypassFeedback(player: Player, command: String) {
        val qs = player.getQuestState(qn)
        val st = StringTokenizer(command, " ")
        val actualCommand = st.nextToken() // Get actual command

        if (actualCommand.contains("buff")) {
            if (qs != null && qs.getInt("cond") == 3) {
                // Search the next token, which is a number between 0 and 7.
                val buffData = data[Integer.valueOf(st.nextToken())]

                val coins = buffData[2]
                var `val` = 3

                if (qs.getQuestItemsCount(GOLDEN_RAM) >= coins) {
                    qs.takeItems(GOLDEN_RAM, coins)
                    target = player
                    doCast(SkillTable.getInfo(buffData[0], buffData[1]))
                    `val` = 4
                }

                val html = NpcHtmlMessage(objectId)
                html.setFile("data/html/default/31556-$`val`.htm")
                player.sendPacket(html)
                return
            }
        } else if (command.startsWith("gmultisell")) {
            if (qs != null && qs.getInt("cond") == 3)
                MultisellData.separateAndSend(command.substring(10).trim { it <= ' ' }, player, this, false)
        } else
            super.onBypassFeedback(player, command)
    }

    companion object {
        private val qn = "Q628_HuntOfTheGoldenRamMercenaryForce"

        private val data = arrayOf(
            intArrayOf(4404, 2, 2),
            intArrayOf(4405, 2, 2),
            intArrayOf(4393, 3, 3),
            intArrayOf(4400, 2, 3),
            intArrayOf(4397, 1, 3),
            intArrayOf(4399, 2, 3),
            intArrayOf(4401, 1, 6),
            intArrayOf(4402, 2, 6)
        )

        private val GOLDEN_RAM = 7251
    }
}