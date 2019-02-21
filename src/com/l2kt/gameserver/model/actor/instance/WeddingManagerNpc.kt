package com.l2kt.gameserver.model.actor.instance

import com.l2kt.Config
import com.l2kt.commons.lang.StringUtil
import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.data.manager.CastleManager
import com.l2kt.gameserver.data.manager.CoupleManager
import com.l2kt.gameserver.extensions.announceToOnlinePlayers
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.model.itemcontainer.Inventory
import com.l2kt.gameserver.network.serverpackets.*
import java.util.*

class WeddingManagerNpc(objectId: Int, template: NpcTemplate) : Folk(objectId, template) {

    override fun onAction(player: Player) {
        // Set the target of the player
        if (player.target !== this)
            player.target = this
        else {
            // Calculate the distance between the Player and the L2Npc
            if (!canInteract(player))
                player.ai.setIntention(CtrlIntention.INTERACT, this)
            else {
                // Stop moving if we're already in interact range.
                if (player.isMoving || player.isInCombat)
                    player.ai.setIntention(CtrlIntention.IDLE)

                // Rotate the player to face the instance
                player.sendPacket(MoveToPawn(player, this, Npc.INTERACTION_DISTANCE))

                // Send ActionFailed to the player in order to avoid he stucks
                player.sendPacket(ActionFailed.STATIC_PACKET)

                // Shouldn't be able to see wedding content if the mod isn't activated on configs
                if (!Config.ALLOW_WEDDING)
                    sendHtmlMessage(player, "data/html/mods/wedding/disabled.htm")
                else {
                    // Married people got access to another menu
                    if (player.coupleId > 0)
                        sendHtmlMessage(player, "data/html/mods/wedding/start2.htm")
                    else if (player.isUnderMarryRequest)
                        sendHtmlMessage(player, "data/html/mods/wedding/waitforpartner.htm")
                    else
                        sendHtmlMessage(player, "data/html/mods/wedding/start.htm")// And normal players go here :)
                    // "Under marriage acceptance" people go to this one
                }
            }
        }
    }

    override fun onBypassFeedback(player: Player, command: String) {
        if (command.startsWith("AskWedding")) {
            val st = StringTokenizer(command)
            st.nextToken()

            if (st.hasMoreTokens()) {
                val partner = World.getPlayer(st.nextToken())
                if (partner == null) {
                    sendHtmlMessage(player, "data/html/mods/wedding/notfound.htm")
                    return
                }

                // check conditions
                if (!weddingConditions(player, partner))
                    return

                // block the wedding manager until an answer is given.
                player.isUnderMarryRequest = true
                partner.isUnderMarryRequest = true

                // memorize the requesterId for future use, and send a popup to the target
                partner.setRequesterId(player.objectId)
                partner.sendPacket(ConfirmDlg(1983).addString(player.name + " asked you to marry. Do you want to start a new relationship ?"))
            } else
                sendHtmlMessage(player, "data/html/mods/wedding/notfound.htm")
        } else if (command.startsWith("Divorce"))
            CoupleManager.deleteCouple(player.coupleId)
        else if (command.startsWith("GoToLove")) {
            // Find the partner using the couple id.
            val partnerId = CoupleManager.getPartnerId(player.coupleId, player.objectId)
            if (partnerId == 0) {
                player.sendMessage("Your partner can't be found.")
                return
            }

            val partner = World.getPlayer(partnerId)
            if (partner == null) {
                player.sendMessage("Your partner is not online.")
                return
            }

            // Simple checks to avoid exploits
            if (partner.isInJail || partner.isInOlympiadMode || partner.isInDuel || partner.isFestivalParticipant || partner.isInParty && partner.party!!.isInDimensionalRift || partner.isInObserverMode) {
                player.sendMessage("Due to the current partner's status, the teleportation failed.")
                return
            }

            if (partner.clan != null && CastleManager.getCastleByOwner(partner.clan) != null && CastleManager.getCastleByOwner(
                    partner.clan
                )!!.siege.isInProgress
            ) {
                player.sendMessage("As your partner is in siege, you can't go to him/her.")
                return
            }

            // If all checks are successfully passed, teleport the player to the partner
            player.teleToLocation(partner.x, partner.y, partner.z, 20)
        }
    }

    private fun weddingConditions(requester: Player, partner: Player): Boolean {
        // Check if player target himself
        if (partner.objectId == requester.objectId) {
            sendHtmlMessage(requester, "data/html/mods/wedding/error_wrongtarget.htm")
            return false
        }

        // Sex check
        if (!Config.WEDDING_SAMESEX && partner.appearance.sex === requester.appearance.sex) {
            sendHtmlMessage(requester, "data/html/mods/wedding/error_sex.htm")
            return false
        }

        // Check if player has the target on friendlist
        if (!requester.friendList.contains(partner.objectId)) {
            sendHtmlMessage(requester, "data/html/mods/wedding/error_friendlist.htm")
            return false
        }

        // Target mustn't be already married
        if (partner.coupleId > 0) {
            sendHtmlMessage(requester, "data/html/mods/wedding/error_alreadymarried.htm")
            return false
        }

        // Check for Formal Wear
        if (Config.WEDDING_FORMALWEAR && !wearsFormalWear(requester, partner)) {
            sendHtmlMessage(requester, "data/html/mods/wedding/error_noformal.htm")
            return false
        }

        // Check and reduce wedding price
        if (requester.adena < Config.WEDDING_PRICE || partner.adena < Config.WEDDING_PRICE) {
            sendHtmlMessage(requester, "data/html/mods/wedding/error_adena.htm")
            return false
        }

        return true
    }

    private fun sendHtmlMessage(player: Player, file: String) {
        val html = NpcHtmlMessage(objectId)
        html.setFile(file)
        html.replace("%objectId%", objectId)
        html.replace("%adenasCost%", StringUtil.formatNumber(Config.WEDDING_PRICE.toLong()))
        html.replace("%needOrNot%", if (Config.WEDDING_FORMALWEAR) "will" else "won't")
        player.sendPacket(html)
    }

    companion object {

        /**
         * Are both partners wearing formal wear ? If Formal Wear check is disabled, returns True in any case.<BR></BR>
         * @param p1 Player
         * @param p2 Player
         * @return boolean
         */
        private fun wearsFormalWear(p1: Player, p2: Player): Boolean {
            val fw1 = p1.inventory!!.getPaperdollItem(Inventory.PAPERDOLL_CHEST)
            if (fw1 == null || fw1.itemId != 6408)
                return false

            val fw2 = p2.inventory!!.getPaperdollItem(Inventory.PAPERDOLL_CHEST)
            return !(fw2 == null || fw2.itemId != 6408)

        }

        fun justMarried(requester: Player, partner: Player) {
            // Unlock the wedding manager for both users, and set them as married
            requester.isUnderMarryRequest = false
            partner.isUnderMarryRequest = false

            // reduce adenas amount according to configs
            requester.reduceAdena("Wedding", Config.WEDDING_PRICE, requester.currentFolk, true)
            partner.reduceAdena("Wedding", Config.WEDDING_PRICE, requester.currentFolk, true)

            // Messages to the couple
            requester.sendMessage("Congratulations, you are now married with " + partner.name + " !")
            partner.sendMessage("Congratulations, you are now married with " + requester.name + " !")

            // Wedding march
            requester.broadcastPacket(MagicSkillUse(requester, requester, 2230, 1, 1, 0))
            partner.broadcastPacket(MagicSkillUse(partner, partner, 2230, 1, 1, 0))

            // Fireworks
            requester.doCast(SkillTable.FrequentSkill.LARGE_FIREWORK.skill)
            partner.doCast(SkillTable.FrequentSkill.LARGE_FIREWORK.skill)

            ("Congratulations to " + requester.name + " and " + partner.name + "! They have been married.").announceToOnlinePlayers()
        }
    }
}