package com.l2kt.gameserver.model.actor.instance

import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.manager.FourSepulchersManager
import com.l2kt.gameserver.data.xml.DoorData
import com.l2kt.gameserver.geoengine.GeoEngine
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.network.clientpackets.Say2
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.network.serverpackets.CreatureSay
import com.l2kt.gameserver.network.serverpackets.MoveToPawn
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage
import com.l2kt.gameserver.scripting.EventType
import com.l2kt.gameserver.scripting.Quest
import java.util.*

class SepulcherNpc(objectId: Int, template: NpcTemplate) : Folk(objectId, template) {

    override fun onAction(player: Player) {
        // Set the target of the player
        if (player.target !== this)
            player.target = this
        else {
            // Check if the player is attackable (without a forced attack)
            if (isAutoAttackable(player))
                player.ai.setIntention(CtrlIntention.ATTACK, this)
            else if (!isAutoAttackable(player)) {
                // Calculate the distance between the Player and this instance.
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

                    if (hasRandomAnimation())
                        onRandomAnimation(Rnd[8])

                    doAction(player)
                }
            }
        }
    }

    override fun onActionShift(player: Player) {
        // Check if the Player is a GM ; send him NPC infos if true.
        if (player.isGM)
            sendNpcInfos(player)

        if (player.target !== this)
            player.target = this
        else {
            if (isAutoAttackable(player)) {
                if (player.isInsideRadius(this, player.physicalAttackRange, false, false) && GeoEngine.canSeeTarget(
                        player,
                        this
                    )
                )
                    player.ai.setIntention(CtrlIntention.ATTACK, this)
                else
                    player.sendPacket(ActionFailed.STATIC_PACKET)
            } else if (canInteract(player)) {
                // Rotate the player to face the instance
                player.sendPacket(MoveToPawn(player, this, Npc.INTERACTION_DISTANCE))

                // Send ActionFailed to the player in order to avoid he stucks
                player.sendPacket(ActionFailed.STATIC_PACKET)

                if (hasRandomAnimation())
                    onRandomAnimation(Rnd[8])

                doAction(player)
            } else
                player.sendPacket(ActionFailed.STATIC_PACKET)
        }
    }

    private fun doAction(player: Player) {
        var player = player
        if (isDead) {
            player.sendPacket(ActionFailed.STATIC_PACKET)
            return
        }

        when (npcId) {
            31468, 31469, 31470, 31471, 31472, 31473, 31474, 31475, 31476, 31477, 31478, 31479, 31480, 31481, 31482, 31483, 31484, 31485, 31486, 31487 -> {
                // Time limit is reached. You can't open anymore Mysterious boxes after the 49th minute.
                if (Calendar.getInstance().get(Calendar.MINUTE) >= 50) {
                    broadcastNpcSay("You can start at the scheduled time.")
                    return
                }
                FourSepulchersManager.spawnMonster(npcId)
                deleteMe()
            }

            31455, 31456, 31457, 31458, 31459, 31460, 31461, 31462, 31463, 31464, 31465, 31466, 31467 -> {
                if (player.isInParty && !player.party!!.isLeader(player))
                    player = player.party!!.leader

                player.addItem("Quest", HALLS_KEY, 1, player, true)

                deleteMe()
            }

            else -> {
                var scripts: List<Quest>? = template.getEventQuests(EventType.QUEST_START)
                if (scripts != null && !scripts.isEmpty())
                    player.lastQuestNpcObject = objectId

                scripts = template.getEventQuests(EventType.ON_FIRST_TALK)
                if (scripts.size == 1)
                    scripts[0].notifyFirstTalk(this, player)
                else
                    showChatWindow(player)
            }
        }
        player.sendPacket(ActionFailed.STATIC_PACKET)
    }

    override fun getHtmlPath(npcId: Int, `val`: Int): String {
        var filename = ""
        if (`val` == 0)
            filename = "" + npcId
        else
            filename = "$npcId-$`val`"

        return "$HTML_FILE_PATH$filename.htm"
    }

    override fun onBypassFeedback(player: Player, command: String) {
        if (command.startsWith("open_gate")) {
            val hallsKey = player.inventory!!.getItemByItemId(HALLS_KEY)
            if (hallsKey == null)
                showHtmlFile(player, "Gatekeeper-no.htm")
            else if (FourSepulchersManager.isAttackTime) {
                when (npcId) {
                    31929, 31934, 31939, 31944 -> {
                        FourSepulchersManager.spawnShadow(npcId)
                        run {
                            openNextDoor(npcId)

                            val party = player.party
                            if (party != null) {
                                for (member in player.party!!.members) {
                                    val key = member.inventory!!.getItemByItemId(HALLS_KEY)
                                    if (key != null)
                                        member.destroyItemByItemId("Quest", HALLS_KEY, key.count, member, true)
                                }
                            } else
                                player.destroyItemByItemId("Quest", HALLS_KEY, hallsKey.count, player, true)
                        }
                    }
                    else -> {
                        openNextDoor(npcId)
                        val party = player.party
                        if (party != null) {
                            for (member in player.party!!.members) {
                                val key = member.inventory!!.getItemByItemId(HALLS_KEY)
                                if (key != null)
                                    member.destroyItemByItemId("Quest", HALLS_KEY, key.count, member, true)
                            }
                        } else
                            player.destroyItemByItemId("Quest", HALLS_KEY, hallsKey.count, player, true)
                    }
                }
            }
        } else
            super.onBypassFeedback(player, command)
    }

    fun openNextDoor(npcId: Int) {
        val doorId = FourSepulchersManager.hallGateKeepers[npcId] ?: return
        val door = DoorData.getDoor(doorId)  ?: return

        // Open the door.
        door!!.openMe()

        // Schedule the automatic door close.
        ThreadPool.schedule(Runnable{ door.closeMe() }, 10000)

        // Spawn the next mysterious box.
        FourSepulchersManager.spawnMysteriousBox(npcId)

        sayInShout("The monsters have spawned!")
    }

    fun sayInShout(msg: String?) {
        if (msg == null || msg.isEmpty())
            return

        val sm = CreatureSay(objectId, Say2.SHOUT, name, msg)
        for (player in getKnownType(Player::class.java))
            player.sendPacket(sm)
    }

    fun showHtmlFile(player: Player, file: String) {
        val html = NpcHtmlMessage(objectId)
        html.setFile("data/html/sepulchers/$file")
        html.replace("%npcname%", name)
        player.sendPacket(html)
    }

    companion object {
        private const val HTML_FILE_PATH = "data/html/sepulchers/"
        private const val HALLS_KEY = 7260
    }
}