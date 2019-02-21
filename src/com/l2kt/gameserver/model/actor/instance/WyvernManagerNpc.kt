package com.l2kt.gameserver.model.actor.instance

import com.l2kt.Config
import com.l2kt.gameserver.instancemanager.SevenSigns
import com.l2kt.gameserver.instancemanager.SevenSigns.CabalType
import com.l2kt.gameserver.instancemanager.SevenSigns.SealType
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage

/**
 * This instance leads the behavior of Wyvern Managers.<br></br>
 * Those NPCs allow Castle Lords to mount a wyvern in return for B Crystals.<br></br>
 * Three configs exist so far :<br></br>
 *
 *  * WYVERN_ALLOW_UPGRADER : spawn instances of Wyvern Manager through the world, or no;
 *  * WYVERN_REQUIRED_LEVEL : the strider's required level;
 *  * WYVERN_REQUIRED_CRYSTALS : the B-crystals' required amount;
 *
 */
class WyvernManagerNpc(objectId: Int, template: NpcTemplate) : CastleChamberlain(objectId, template) {

    override fun onBypassFeedback(player: Player, command: String) {
        if (player.currentFolk == null || player.currentFolk.objectId != objectId)
            return

        if (command.startsWith("RideWyvern")) {
            var `val` = "2"
            if (player.isClanLeader) {
                // Verify if Dusk own Seal of Strife (if true, CLs can't mount wyvern).
                if (SevenSigns.getSealOwner(SealType.STRIFE) === CabalType.DUSK)
                    `val` = "3"
                else if (player.isMounted && (player.mountNpcId == 12526 || player.mountNpcId == 12527 || player.mountNpcId == 12528)) {
                    // Check for strider level
                    if (player.mountLevel < Config.WYVERN_REQUIRED_LEVEL)
                        `val` = "6"
                    else if (player.destroyItemByItemId(
                            "Wyvern",
                            1460,
                            Config.WYVERN_REQUIRED_CRYSTALS,
                            player,
                            true
                        )
                    ) {
                        player.dismount()
                        if (player.mount(12621, 0))
                            `val` = "4"
                    } else
                        `val` = "5"// Check for items consumption
                } else {
                    player.sendPacket(SystemMessageId.YOU_MAY_ONLY_RIDE_WYVERN_WHILE_RIDING_STRIDER)
                    `val` = "1"
                }// If player is mounted on a strider
            }

            sendHtm(player, `val`)
        } else if (command.startsWith("Chat")) {
            var `val` = "1" // Default send you to error HTM.
            try {
                `val` = command.substring(5)
            } catch (ioobe: IndexOutOfBoundsException) {
            }

            sendHtm(player, `val`)
        } else
            super.onBypassFeedback(player, command)
    }

    override fun showChatWindow(player: Player) {
        var `val` = "0a" // Default value : player's clan doesn't own castle.

        val condition = validateCondition(player)
        if (condition > COND_ALL_FALSE) {
            if (condition == COND_OWNER)
            // Clan owns castle && player is CL ; send the good HTM.
            {
                if (player.isFlying)
                // Already mounted on Wyvern
                    `val` = "4"
                else
                    `val` = "0" // Initial screen
            } else if (condition == COND_CLAN_MEMBER)
            // Good clan, but player isn't a CL.
                `val` = "2"
        }
        sendHtm(player, `val`)
    }

    private fun sendHtm(player: Player, `val`: String) {
        val html = NpcHtmlMessage(objectId)
        html.setFile("data/html/wyvernmanager/wyvernmanager-$`val`.htm")
        html.replace("%objectId%", objectId)
        html.replace("%npcname%", name)
        html.replace("%wyvern_level%", Config.WYVERN_REQUIRED_LEVEL)
        html.replace("%needed_crystals%", Config.WYVERN_REQUIRED_CRYSTALS)
        player.sendPacket(html)

        player.sendPacket(ActionFailed.STATIC_PACKET)
    }
}