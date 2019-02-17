package com.l2kt.gameserver.handler.admincommandhandlers

import com.l2kt.commons.lang.StringUtil
import com.l2kt.gameserver.data.SpawnTable
import com.l2kt.gameserver.data.manager.FenceManager
import com.l2kt.gameserver.data.xml.AdminData
import com.l2kt.gameserver.data.xml.NpcData
import com.l2kt.gameserver.extensions.toAllOnlinePlayers
import com.l2kt.gameserver.handler.IAdminCommandHandler
import com.l2kt.gameserver.instancemanager.DayNightSpawnManager
import com.l2kt.gameserver.instancemanager.RaidBossSpawnManager
import com.l2kt.gameserver.instancemanager.SevenSigns
import com.l2kt.gameserver.model.L2Spawn
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Fence
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage
import com.l2kt.gameserver.network.serverpackets.SystemMessage
import java.util.*
import java.util.regex.Pattern

/**
 * This class handles following admin commands:<br></br>
 * - show_spawns = shows menu<br></br>
 * - spawn_index lvl = shows menu for monsters with respective level<br></br>
 * - spawn id = spawns monster id on target
 */
class AdminSpawn : IAdminCommandHandler {

    override fun useAdminCommand(command: String, activeChar: Player): Boolean {
        if (command.startsWith("admin_list_spawns")) {
            var npcId = 0

            try {
                val params = command.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val pattern = Pattern.compile("[0-9]*")
                val regexp = pattern.matcher(params[1])

                if (regexp.matches())
                    npcId = Integer.parseInt(params[1])
                else {
                    params[1] = params[1].replace('_', ' ')
                    npcId = NpcData.getTemplateByName(params[1])!!.npcId
                }
            } catch (e: Exception) {
                // If the parameter wasn't ok, then take the current target.
                val target = activeChar.target
                if (target is Npc)
                    npcId = target.npcId
            }

            // Load static Htm.
            val html = NpcHtmlMessage(0)
            html.setFile("data/html/admin/listspawns.htm")

            // Generate data.
            val sb = StringBuilder()

            var index = 0
            var x: Int
            var y: Int
            var z: Int
            var name = ""

            for (spawn in SpawnTable.spawnTable) {
                if (npcId == spawn.npcId) {
                    index++
                    name = spawn.template!!.name

                    val _npc = spawn.npc
                    if (_npc != null) {
                        x = _npc.x
                        y = _npc.y
                        z = _npc.z
                    } else {
                        x = spawn.locX
                        y = spawn.locY
                        z = spawn.locZ
                    }
                    StringUtil.append(
                        sb,
                        "<tr><td><a action=\"bypass -h admin_move_to ",
                        x,
                        " ",
                        y,
                        " ",
                        z,
                        "\">",
                        index,
                        " - (",
                        x,
                        " ",
                        y,
                        " ",
                        z,
                        ")",
                        "</a></td></tr>"
                    )
                }
            }

            if (index == 0) {
                html.replace("%npcid%", "?")
                html.replace("%list%", "<tr><td>The parameter you entered as npcId is invalid.</td></tr>")
            } else {
                html.replace("%npcid%", "$name ($npcId)")
                html.replace("%list%", sb.toString())
            }

            activeChar.sendPacket(html)
        } else if (command == "admin_show_spawns")
            AdminHelpPage.showHelpPage(activeChar, "spawns.htm")
        else if (command.startsWith("admin_spawn_index")) {
            val st = StringTokenizer(command, " ")
            try {
                st.nextToken()
                val level = Integer.parseInt(st.nextToken())
                var from = 0
                try {
                    from = Integer.parseInt(st.nextToken())
                } catch (nsee: NoSuchElementException) {
                }

                showMonsters(activeChar, level, from)
            } catch (e: Exception) {
                AdminHelpPage.showHelpPage(activeChar, "spawns.htm")
            }

        } else if (command == "admin_show_npcs")
            AdminHelpPage.showHelpPage(activeChar, "npcs.htm")
        else if (command.startsWith("admin_npc_index")) {
            val st = StringTokenizer(command, " ")
            try {
                st.nextToken()
                val letter = st.nextToken()
                var from = 0
                try {
                    from = Integer.parseInt(st.nextToken())
                } catch (nsee: NoSuchElementException) {
                }

                showNpcs(activeChar, letter, from)
            } catch (e: Exception) {
                AdminHelpPage.showHelpPage(activeChar, "npcs.htm")
            }

        } else if (command.startsWith("admin_unspawnall")) {
            SystemMessage.getSystemMessage(SystemMessageId.NPC_SERVER_NOT_OPERATING).toAllOnlinePlayers()
            RaidBossSpawnManager.cleanUp()
            DayNightSpawnManager.cleanUp()
            World.deleteVisibleNpcSpawns()
            AdminData.broadcastMessageToGMs("NPCs' unspawn is now complete.")
        } else if (command.startsWith("admin_spawnday"))
            DayNightSpawnManager.spawnDayCreatures()
        else if (command.startsWith("admin_spawnnight"))
            DayNightSpawnManager.spawnNightCreatures()
        else if (command.startsWith("admin_respawnall") || command.startsWith("admin_spawn_reload")) {
            // make sure all spawns are deleted
            RaidBossSpawnManager.cleanUp()
            DayNightSpawnManager.cleanUp()
            World.deleteVisibleNpcSpawns()
            // now respawn all
            NpcData.reload()
            SpawnTable.reloadAll()
            RaidBossSpawnManager.reloadBosses()
            SevenSigns.spawnSevenSignsNPC()
            AdminData.broadcastMessageToGMs("NPCs' respawn is now complete.")
        } else if (command.startsWith("admin_spawnfence")) {
            val st = StringTokenizer(command, " ")
            try {
                st.nextToken()
                val type = Integer.parseInt(st.nextToken())
                val sizeX = Integer.parseInt(st.nextToken()) / 100 * 100
                val sizeY = Integer.parseInt(st.nextToken()) / 100 * 100
                var height = 1
                if (st.hasMoreTokens())
                    height = Math.min(Integer.parseInt(st.nextToken()), 3)

                FenceManager.addFence(activeChar.x, activeChar.y, activeChar.z, type, sizeX, sizeY, height)

                listFences(activeChar)
            } catch (e: Exception) {
                activeChar.sendMessage("Usage: //spawnfence <type> <width> <length> [height]")
            }

        } else if (command.startsWith("admin_deletefence")) {
            val st = StringTokenizer(command, " ")
            st.nextToken()
            try {
                val `object` = World.getObject(Integer.parseInt(st.nextToken()))
                if (`object` is Fence) {
                    FenceManager.removeFence((`object` as Fence?)!!)

                    if (st.hasMoreTokens())
                        listFences(activeChar)
                } else
                    activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET)
            } catch (e: Exception) {
                activeChar.sendMessage("Usage: //deletefence <objectId>")
            }

        } else if (command.startsWith("admin_listfence"))
            listFences(activeChar)
        else if (command.startsWith("admin_spawn")) {
            val st = StringTokenizer(command, " ")
            try {
                val cmd = st.nextToken()
                val id = st.nextToken()
                val respawnTime = if (st.hasMoreTokens()) Integer.parseInt(st.nextToken()) else 60

                if (cmd.equals("admin_spawn_once", ignoreCase = true))
                    spawn(activeChar, id, respawnTime, false)
                else
                    spawn(activeChar, id, respawnTime, true)
            } catch (e: Exception) {
                AdminHelpPage.showHelpPage(activeChar, "spawns.htm")
            }

        }
        return true
    }

    override val adminCommandList: Array<String> get() = ADMIN_COMMANDS

    companion object {
        private val ADMIN_COMMANDS = arrayOf(
            "admin_list_spawns",
            "admin_show_spawns",
            "admin_spawn",
            "admin_spawn_index",
            "admin_unspawnall",
            "admin_respawnall",
            "admin_spawn_reload",
            "admin_npc_index",
            "admin_spawn_once",
            "admin_show_npcs",
            "admin_spawnnight",
            "admin_spawnday",
            "admin_spawnfence",
            "admin_deletefence",
            "admin_listfence"
        )

        private fun spawn(activeChar: Player, monsterId: String, respawnTime: Int, permanent: Boolean) {
            var monsterId = monsterId
            var target: WorldObject? = activeChar.target
            if (target == null)
                target = activeChar

            val template: NpcTemplate?

            if (monsterId.matches("[0-9]*".toRegex()))
            // First parameter was an ID number
                template = NpcData.getTemplate(Integer.parseInt(monsterId))
            else
            // First parameter wasn't just numbers, so go by name not ID
            {
                monsterId = monsterId.replace('_', ' ')
                template = NpcData.getTemplateByName(monsterId)
            }

            try {
                val spawn = L2Spawn(template)
                spawn.setLoc(target.x, target.y, target.z, activeChar.heading)
                spawn.respawnDelay = respawnTime

                if (RaidBossSpawnManager.getValidTemplate(spawn.npcId) != null) {
                    if (RaidBossSpawnManager.isDefined(spawn.npcId)) {
                        activeChar.sendMessage("You cannot spawn another instance of " + template!!.name + ".")
                        return
                    }

                    spawn.respawnMinDelay = 43200
                    spawn.respawnMaxDelay = 129600
                    RaidBossSpawnManager.addNewSpawn(spawn, 0, 0.0, 0.0, permanent)
                } else {
                    SpawnTable.addNewSpawn(spawn, permanent)
                    spawn.doSpawn(false)
                    if (permanent)
                        spawn.setRespawnState(true)
                }

                if (!permanent)
                    spawn.setRespawnState(false)

                activeChar.sendMessage("Spawned " + template!!.name + ".")

            } catch (e: Exception) {
                activeChar.sendPacket(SystemMessageId.APPLICANT_INFORMATION_INCORRECT)
            }

        }

        private fun showMonsters(activeChar: Player, level: Int, from: Int) {
            val mobs = NpcData.getTemplates { t -> t.isType("Monster") && t.getLevel().toInt() == level }
            val sb = StringBuilder(200 + mobs.size * 100)

            StringUtil.append(
                sb,
                "<html><title>Spawn Monster:</title><body><p> Level : ",
                level,
                "<br>Total Npc's : ",
                mobs.size,
                "<br>"
            )

            var i = from
            var j = 0
            while (i < mobs.size && j < 50) {
                StringUtil.append(
                    sb,
                    "<a action=\"bypass -h admin_spawn ",
                    mobs[i].npcId,
                    "\">",
                    mobs[i].name,
                    "</a><br1>"
                )
                i++
                j++
            }

            if (i == mobs.size)
                sb.append("<br><center><button value=\"Back\" action=\"bypass -h admin_show_spawns\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>")
            else
                StringUtil.append(
                    sb,
                    "<br><center><button value=\"Next\" action=\"bypass -h admin_spawn_index ",
                    level,
                    " ",
                    i,
                    "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><button value=\"Back\" action=\"bypass -h admin_show_spawns\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>"
                )

            val html = NpcHtmlMessage(0)
            html.setHtml(sb.toString())
            activeChar.sendPacket(html)
        }

        private fun showNpcs(activeChar: Player, starting: String, from: Int) {
            val mobs = NpcData.getTemplates({ t -> t.isType("Folk") && t.getName().startsWith(starting) })
            val sb = StringBuilder(200 + mobs.size * 100)

            StringUtil.append(
                sb,
                "<html><title>Spawn Monster:</title><body><p> There are ",
                mobs.size,
                " Npcs whose name starts with ",
                starting,
                ":<br>"
            )

            var i = from
            var j = 0
            while (i < mobs.size && j < 50) {
                StringUtil.append(
                    sb,
                    "<a action=\"bypass -h admin_spawn ",
                    mobs[i].npcId,
                    "\">",
                    mobs[i].name,
                    "</a><br1>"
                )
                i++
                j++
            }

            if (i == mobs.size)
                sb.append("<br><center><button value=\"Back\" action=\"bypass -h admin_show_npcs\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>")
            else
                StringUtil.append(
                    sb,
                    "<br><center><button value=\"Next\" action=\"bypass -h admin_npc_index ",
                    starting,
                    " ",
                    i,
                    "\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"><button value=\"Back\" action=\"bypass -h admin_show_npcs\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui92\"></center></body></html>"
                )

            val html = NpcHtmlMessage(0)
            html.setHtml(sb.toString())
            activeChar.sendPacket(html)
        }

        private fun listFences(activeChar: Player) {
            val fences = FenceManager.fences
            val sb = StringBuilder()

            sb.append("<html><body>Total Fences: " + fences.size + "<br><br>")
            for (fence in fences)
                sb.append("<a action=\"bypass -h admin_deletefence " + fence.objectId + " 1\">Fence: " + fence.objectId + " [" + fence.x + " " + fence.y + " " + fence.z + "]</a><br>")
            sb.append("</body></html>")

            val html = NpcHtmlMessage(0)
            html.setHtml(sb.toString())
            activeChar.sendPacket(html)
        }
    }
}