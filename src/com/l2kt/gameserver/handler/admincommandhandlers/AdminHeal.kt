package com.l2kt.gameserver.handler.admincommandhandlers

import com.l2kt.commons.lang.StringUtil
import com.l2kt.gameserver.handler.IAdminCommandHandler
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.SystemMessageId
import java.util.*

/**
 * This class handles following admin commands: - heal = restores HP/MP/CP on target, name or radius
 */
class AdminHeal : IAdminCommandHandler {

    override fun useAdminCommand(command: String, player: Player): Boolean {
        if (command.startsWith("admin_heal")) {
            var `object`: WorldObject? = player.target

            val st = StringTokenizer(command, " ")
            st.nextToken()

            if (st.hasMoreTokens()) {
                val nameOrRadius = st.nextToken()

                val target = World.getPlayer(nameOrRadius)
                if (target != null)
                    `object` = target
                else if (StringUtil.isDigit(nameOrRadius)) {
                    val radius = Integer.parseInt(nameOrRadius)
                    for (creature in player.getKnownTypeInRadius(Creature::class.java, radius)) {
                        creature.setCurrentHpMp(creature.maxHp.toDouble(), creature.maxMp.toDouble())
                        if (creature is Player)
                            creature.setCurrentCp(creature.getMaxCp().toDouble())
                    }
                    player.sendMessage("You instant healed all characters within $radius unit radius.")
                    return true
                }
            }

            if (`object` == null)
                `object` = player

            if (`object` is Creature) {
                val creature = `object` as Creature?
                creature!!.setCurrentHpMp(creature.maxHp.toDouble(), creature.maxMp.toDouble())

                if (creature is Player)
                    creature.currentCp = creature.maxCp.toDouble()

                player.sendMessage("You instant healed " + creature.name + ".")
            } else
                player.sendPacket(SystemMessageId.INCORRECT_TARGET)
        }
        return true
    }

    override val adminCommandList: Array<String> get() = ADMIN_COMMANDS

    companion object {
        private val ADMIN_COMMANDS = arrayOf("admin_heal")
    }
}