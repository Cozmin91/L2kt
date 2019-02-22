package com.l2kt.gameserver.model.actor.instance

import com.l2kt.gameserver.instancemanager.SevenSigns
import com.l2kt.gameserver.instancemanager.SevenSigns.CabalType
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage
import com.l2kt.gameserver.templates.skills.L2EffectType

/**
 * @author Kerberos | ZaKaX
 */
class CastleMagician(objectId: Int, template: NpcTemplate) : Folk(objectId, template) {

    override fun showChatWindow(player: Player, `val`: Int) {
        player.sendPacket(ActionFailed.STATIC_PACKET)
        var filename = "data/html/castlemagician/magician-no.htm"

        val condition = validateCondition(player)
        if (condition > COND_ALL_FALSE) {
            if (condition == COND_BUSY_BECAUSE_OF_SIEGE)
                filename = "data/html/castlemagician/magician-busy.htm" // Busy because of siege
            else if (condition == COND_OWNER)
            // Clan owns castle
            {
                if (`val` == 0)
                    filename = "data/html/castlemagician/magician.htm"
                else
                    filename = "data/html/castlemagician/magician-$`val`.htm"
            }
        }

        val html = NpcHtmlMessage(objectId)
        html.setFile(filename)
        html.replace("%objectId%", objectId)
        player.sendPacket(html)
    }

    override fun onBypassFeedback(player: Player, command: String) {
        if (command.startsWith("Chat")) {
            var `val` = 0
            try {
                `val` = Integer.parseInt(command.substring(5))
            } catch (ioobe: IndexOutOfBoundsException) {
            } catch (nfe: NumberFormatException) {
            }

            showChatWindow(player, `val`)
            return
        } else if (command == "gotoleader") {
            if (player.clan != null) {
                val clanLeader = player.clan.leader!!.playerInstance ?: return

                if (clanLeader.getFirstEffect(L2EffectType.CLAN_GATE) != null) {
                    if (!validateGateCondition(clanLeader, player))
                        return

                    player.teleToLocation(clanLeader.x, clanLeader.y, clanLeader.z, 0)
                    return
                }
                val filename = "data/html/castlemagician/magician-nogate.htm"
                showChatWindow(player, filename)
            }
            return
        } else
            super.onBypassFeedback(player, command)
    }

    protected fun validateCondition(player: Player): Int {
        if (castle != null && player.clan != null) {
            if (castle!!.siegeZone!!.isActive)
                return COND_BUSY_BECAUSE_OF_SIEGE

            if (castle!!.ownerId == player.clanId)
                return COND_OWNER
        }
        return COND_ALL_FALSE
    }

    companion object {
        protected const val COND_ALL_FALSE = 0
        protected const val COND_BUSY_BECAUSE_OF_SIEGE = 1
        protected const val COND_OWNER = 2

        private fun validateGateCondition(clanLeader: Player, player: Player): Boolean {
            if (clanLeader.isAlikeDead || clanLeader.isInStoreMode || clanLeader.isRooted || clanLeader.isInCombat || clanLeader.isInOlympiadMode || clanLeader.isFestivalParticipant || clanLeader.isInObserverMode || clanLeader.isInsideZone(
                    ZoneId.NO_SUMMON_FRIEND
                )
            ) {
                player.sendMessage("Couldn't teleport to clan leader. The requirements was not meet.")
                return false
            }

            if (player.isIn7sDungeon) {
                val targetCabal = SevenSigns.getPlayerCabal(clanLeader.objectId)
                if (SevenSigns.isSealValidationPeriod) {
                    if (targetCabal !== SevenSigns.cabalHighestScore) {
                        player.sendMessage("Couldn't teleport to clan leader. The requirements was not meet.")
                        return false
                    }
                } else {
                    if (targetCabal === CabalType.NORMAL) {
                        player.sendMessage("Couldn't teleport to clan leader. The requirements was not meet.")
                        return false
                    }
                }
            }

            return true
        }
    }
}