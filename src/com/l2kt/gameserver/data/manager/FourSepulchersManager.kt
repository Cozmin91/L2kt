package com.l2kt.gameserver.data.manager

import com.l2kt.Config
import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.commons.logging.CLogger
import com.l2kt.commons.math.MathUtil
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.SpawnTable
import com.l2kt.gameserver.data.xml.DoorData
import com.l2kt.gameserver.data.xml.NpcData
import com.l2kt.gameserver.model.L2Spawn
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.actor.instance.SepulcherNpc
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.model.location.SpawnLocation
import com.l2kt.gameserver.model.zone.type.BossZone
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage
import java.util.*

object FourSepulchersManager {

    val LOGGER = CLogger(FourSepulchersManager::class.java.name)

    private const val QUEST_ID = "Q620_FourGoblets"

    private const val ENTRANCE_PASS = 7075
    private const val USED_PASS = 7261
    private const val CHAPEL_KEY = 7260
    private const val ANTIQUE_BROOCH = 7262

    private val shadowSpawnLoc = HashMap<Int, Map<Int, SpawnLocation>>()
    private val archonSpawned = HashMap<Int, Boolean>()
    private val hallInUse = HashMap<Int, Boolean>()
    private val startHallSpawns = HashMap<Int, Location>()
    val hallGateKeepers = HashMap<Int, Int>()
    private val keyBoxNpc = HashMap<Int, Int>()
    private val victim = HashMap<Int, Int>()
    private val executionerSpawns = HashMap<Int, L2Spawn>()
    private val keyBoxSpawns = HashMap<Int, L2Spawn>()
    private val mysteriousBoxSpawns = HashMap<Int, L2Spawn>()
    private val shadowSpawns = HashMap<Int, L2Spawn>()
    private val dukeFinalMobs = HashMap<Int, List<L2Spawn>>()
    private val dukeMobs = HashMap<Int, List<Npc>>()
    private val emperorsGraveNpcs = HashMap<Int, List<L2Spawn>>()
    private val magicalMonsters = HashMap<Int, List<L2Spawn>>()
    private val physicalMonsters = HashMap<Int, List<L2Spawn>>()
    private val viscountMobs = HashMap<Int, List<Npc>>()

    private val managers = ArrayList<L2Spawn>()
    private val allMobs = ArrayList<Npc>()

    private var state = State.ENTRY

    val isEntryTime: Boolean
        get() = state == State.ENTRY

    val isAttackTime: Boolean
        get() = state == State.ATTACK

    enum class State {
        ENTRY,
        ATTACK,
        END
    }

    init {
        initFixedInfo()
        loadMysteriousBox()
        initKeyBoxSpawns()

        loadSpawnsByType(1)
        loadSpawnsByType(2)

        initShadowSpawns()
        initExecutionerSpawns()

        loadSpawnsByType(5)
        loadSpawnsByType(6)

        spawnManagers()

        launchCycle()
    }

    fun initFixedInfo() {
        var temp: MutableMap<Int, SpawnLocation> = HashMap()
        temp[25339] = SpawnLocation(191231, -85574, -7216, 33380)
        temp[25349] = SpawnLocation(189534, -88969, -7216, 32768)
        temp[25346] = SpawnLocation(173195, -76560, -7215, 49277)
        temp[25342] = SpawnLocation(175591, -72744, -7215, 49317)
        shadowSpawnLoc[0] = temp

        temp = HashMap()
        temp[25342] = SpawnLocation(191231, -85574, -7216, 33380)
        temp[25339] = SpawnLocation(189534, -88969, -7216, 32768)
        temp[25349] = SpawnLocation(173195, -76560, -7215, 49277)
        temp[25346] = SpawnLocation(175591, -72744, -7215, 49317)
        shadowSpawnLoc[1] = temp

        temp = HashMap()
        temp[25346] = SpawnLocation(191231, -85574, -7216, 33380)
        temp[25342] = SpawnLocation(189534, -88969, -7216, 32768)
        temp[25339] = SpawnLocation(173195, -76560, -7215, 49277)
        temp[25349] = SpawnLocation(175591, -72744, -7215, 49317)
        shadowSpawnLoc[2] = temp

        temp = HashMap()
        temp[25349] = SpawnLocation(191231, -85574, -7216, 33380)
        temp[25346] = SpawnLocation(189534, -88969, -7216, 32768)
        temp[25342] = SpawnLocation(173195, -76560, -7215, 49277)
        temp[25339] = SpawnLocation(175591, -72744, -7215, 49317)
        shadowSpawnLoc[3] = temp

        startHallSpawns[31921] = Location(181632, -85587, -7218)
        startHallSpawns[31922] = Location(179963, -88978, -7218)
        startHallSpawns[31923] = Location(173217, -86132, -7218)
        startHallSpawns[31924] = Location(175608, -82296, -7218)

        hallInUse[31921] = false
        hallInUse[31922] = false
        hallInUse[31923] = false
        hallInUse[31924] = false

        hallGateKeepers[31925] = 25150012
        hallGateKeepers[31926] = 25150013
        hallGateKeepers[31927] = 25150014
        hallGateKeepers[31928] = 25150015
        hallGateKeepers[31929] = 25150016
        hallGateKeepers[31930] = 25150002
        hallGateKeepers[31931] = 25150003
        hallGateKeepers[31932] = 25150004
        hallGateKeepers[31933] = 25150005
        hallGateKeepers[31934] = 25150006
        hallGateKeepers[31935] = 25150032
        hallGateKeepers[31936] = 25150033
        hallGateKeepers[31937] = 25150034
        hallGateKeepers[31938] = 25150035
        hallGateKeepers[31939] = 25150036
        hallGateKeepers[31940] = 25150022
        hallGateKeepers[31941] = 25150023
        hallGateKeepers[31942] = 25150024
        hallGateKeepers[31943] = 25150025
        hallGateKeepers[31944] = 25150026

        keyBoxNpc[18120] = 31455
        keyBoxNpc[18121] = 31455
        keyBoxNpc[18122] = 31455
        keyBoxNpc[18123] = 31455
        keyBoxNpc[18124] = 31456
        keyBoxNpc[18125] = 31456
        keyBoxNpc[18126] = 31456
        keyBoxNpc[18127] = 31456
        keyBoxNpc[18128] = 31457
        keyBoxNpc[18129] = 31457
        keyBoxNpc[18130] = 31457
        keyBoxNpc[18131] = 31457
        keyBoxNpc[18149] = 31458
        keyBoxNpc[18150] = 31459
        keyBoxNpc[18151] = 31459
        keyBoxNpc[18152] = 31459
        keyBoxNpc[18153] = 31459
        keyBoxNpc[18154] = 31460
        keyBoxNpc[18155] = 31460
        keyBoxNpc[18156] = 31460
        keyBoxNpc[18157] = 31460
        keyBoxNpc[18158] = 31461
        keyBoxNpc[18159] = 31461
        keyBoxNpc[18160] = 31461
        keyBoxNpc[18161] = 31461
        keyBoxNpc[18162] = 31462
        keyBoxNpc[18163] = 31462
        keyBoxNpc[18164] = 31462
        keyBoxNpc[18165] = 31462
        keyBoxNpc[18183] = 31463
        keyBoxNpc[18184] = 31464
        keyBoxNpc[18212] = 31465
        keyBoxNpc[18213] = 31465
        keyBoxNpc[18214] = 31465
        keyBoxNpc[18215] = 31465
        keyBoxNpc[18216] = 31466
        keyBoxNpc[18217] = 31466
        keyBoxNpc[18218] = 31466
        keyBoxNpc[18219] = 31466

        victim[18150] = 18158
        victim[18151] = 18159
        victim[18152] = 18160
        victim[18153] = 18161
        victim[18154] = 18162
        victim[18155] = 18163
        victim[18156] = 18164
        victim[18157] = 18165
    }

    private fun loadMysteriousBox() {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement("SELECT id, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM spawnlist_4s WHERE spawntype = 0 ORDER BY id")
                    .use { ps ->
                        ps.executeQuery().use { rs ->
                            while (rs.next()) {
                                val template = NpcData.getTemplate(rs.getInt("npc_templateid"))
                                if (template == null) {
                                    LOGGER.warn("Data missing in NPC table for ID: {}.", rs.getInt("npc_templateid"))
                                    continue
                                }

                                val spawn = L2Spawn(template)
                                spawn.setLoc(
                                    rs.getInt("locx"),
                                    rs.getInt("locy"),
                                    rs.getInt("locz"),
                                    rs.getInt("heading")
                                )
                                spawn.respawnDelay = rs.getInt("respawn_delay")

                                SpawnTable.addNewSpawn(spawn, false)
                                mysteriousBoxSpawns[rs.getInt("key_npc_id")] = spawn
                            }
                        }
                    }
            }
        } catch (e: Exception) {
            LOGGER.error("Failed to initialize a spawn.", e)
        }

        LOGGER.info("Loaded {} Mysterious-Box.", mysteriousBoxSpawns.size)
    }

    private fun initKeyBoxSpawns() {
        for ((key, value) in keyBoxNpc) {
            try {
                val template = NpcData.getTemplate(value)
                if (template == null) {
                    LOGGER.warn("Data missing in NPC table for ID: {}.", value)
                    continue
                }

                val spawn = L2Spawn(template)
                SpawnTable.addNewSpawn(spawn, false)
                keyBoxSpawns[key] = spawn
            } catch (e: Exception) {
                LOGGER.error("Failed to initialize a spawn.", e)
            }

        }
        LOGGER.info("Loaded {} Key-Box.", keyBoxNpc.size)
    }

    private fun loadSpawnsByType(type: Int) {
        var loaded = 0

        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement("SELECT Distinct key_npc_id FROM spawnlist_4s WHERE spawntype = ? ORDER BY key_npc_id")
                    .use { ps ->
                        ps.setInt(1, type)

                        ps.executeQuery().use { rs ->
                            con.prepareStatement("SELECT id, npc_templateid, locx, locy, locz, heading, respawn_delay, key_npc_id FROM spawnlist_4s WHERE key_npc_id = ? AND spawntype = ? ORDER BY id")
                                .use { ps2 ->
                                    while (rs.next()) {
                                        val keyNpcId = rs.getInt("key_npc_id")

                                        // Feed the second statement.
                                        ps2.setInt(1, keyNpcId)
                                        ps2.setInt(2, type)

                                        ps2.executeQuery().use innerUse@ { rs2 ->
                                            // Clear parameters of the second statement.
                                            ps2.clearParameters()

                                            // Generate a new List, which will be stored as value on the Map.
                                            val spawns = ArrayList<L2Spawn>()

                                            while (rs2.next()) {
                                                val template = NpcData.getTemplate(rs2.getInt("npc_templateid"))
                                                if (template == null) {
                                                    LOGGER.warn(
                                                        "Data missing in NPC table for ID: {}.",
                                                        rs2.getInt("npc_templateid")
                                                    )
                                                    continue
                                                }

                                                val spawn = L2Spawn(template)
                                                spawn.setLoc(
                                                    rs2.getInt("locx"),
                                                    rs2.getInt("locy"),
                                                    rs2.getInt("locz"),
                                                    rs2.getInt("heading")
                                                )
                                                spawn.respawnDelay = rs2.getInt("respawn_delay")

                                                SpawnTable.addNewSpawn(spawn, false)
                                                spawns.add(spawn)

                                                loaded++
                                            }

                                            if (type == 1)
                                                physicalMonsters[keyNpcId] = spawns
                                            else if (type == 2)
                                                magicalMonsters[keyNpcId] = spawns
                                            else if (type == 5) {
                                                dukeFinalMobs[keyNpcId] = spawns
                                                archonSpawned[keyNpcId] = false
                                            } else if (type == 6)
                                                emperorsGraveNpcs[keyNpcId] = spawns
                                            else return@innerUse
                                        }
                                    }
                                }
                        }
                    }
            }
        } catch (e: Exception) {
            LOGGER.error("Failed to initialize a spawn.", e)
        }

        if (type == 1)
            LOGGER.info("Loaded {} physical type monsters.", loaded)
        else if (type == 2)
            LOGGER.info("Loaded {} magical type monsters.", loaded)
        else if (type == 5)
            LOGGER.info("Loaded {} Duke's Hall Gatekeepers.", loaded)
        else if (type == 6)
            LOGGER.info("Loaded {} Emperor's Grave monsters.", loaded)
    }

    private fun initShadowSpawns() {
        val gateKeeper = intArrayOf(31929, 31934, 31939, 31944)

        // Generate new locations for the 4 shadows.
        val newLoc = shadowSpawnLoc[Rnd[4]]!!

        // Used to store current index.
        var index = 0

        // Generate spawns and refresh locations.
        for ((key, value) in newLoc) {
            val template = NpcData.getTemplate(key)
            if (template == null) {
                LOGGER.warn("Data missing in NPC table for ID: {}.", key)
                continue
            }

            try {
                val spawn = L2Spawn(template)
                spawn.loc = value

                SpawnTable.addNewSpawn(spawn, false)

                shadowSpawns[gateKeeper[index]] = spawn

                index++
            } catch (e: Exception) {
                LOGGER.error("Failed to initialize a spawn.", e)
            }

        }
        LOGGER.info("Loaded {} Shadows of Halisha.", shadowSpawns.size)
    }

    private fun initExecutionerSpawns() {
        for ((key, value) in victim) {
            try {
                val template = NpcData.getTemplate(value)
                if (template == null) {
                    LOGGER.warn("Data missing in NPC table for ID: {}.", value)
                    continue
                }

                val spawn = L2Spawn(template)

                SpawnTable.addNewSpawn(spawn, false)
                executionerSpawns[key] = spawn
            } catch (e: Exception) {
                LOGGER.error("Failed to initialize a spawn.", e)
            }

        }
    }

    private fun spawnManagers() {
        for (i in 31921..31924) {
            val template = NpcData.getTemplate(i) ?: continue

            try {
                val spawn = L2Spawn(template)
                spawn.respawnDelay = 60

                when (i) {
                    31921 // conquerors
                    -> spawn.setLoc(181061, -85595, -7200, -32584)

                    31922 // emperors
                    -> spawn.setLoc(179292, -88981, -7200, -33272)

                    31923 // sages
                    -> spawn.setLoc(173202, -87004, -7200, -16248)

                    31924 // judges
                    -> spawn.setLoc(175606, -82853, -7200, -16248)
                }

                SpawnTable.addNewSpawn(spawn, false)
                spawn.doSpawn(false)
                spawn.setRespawnState(true)

                managers.add(spawn)
            } catch (e: Exception) {
                LOGGER.error("Failed to spawn managers.", e)
            }

        }
        LOGGER.info("Loaded {} managers.", managers.size)
    }

    private fun launchCycle() {
        // Get time in future (one minute cleaned of seconds/milliseconds).
        val cal = Calendar.getInstance()
        cal.add(Calendar.MINUTE, 1)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        // Calculate the time needed to reach that minute, then fire an event every minute.
        ThreadPool.scheduleAtFixedRate(Cycle(), cal.timeInMillis - System.currentTimeMillis(), 60000)
    }

    @Synchronized
    fun tryEntry(npc: Npc, player: Player) {
        val npcId = npc.npcId

        when (npcId) {
            31921, 31922, 31923, 31924 -> {
            }

            else -> return
        }

        if (hallInUse[npcId] == true) {
            showHtmlFile(player, npcId.toString() + "-FULL.htm", npc, null)
            return
        }

        val party = player.party
        if (party == null || party.membersCount < Config.FS_PARTY_MEMBER_COUNT) {
            showHtmlFile(player, npcId.toString() + "-SP.htm", npc, null)
            return
        }

        if (!party.isLeader(player)) {
            showHtmlFile(player, npcId.toString() + "-NL.htm", npc, null)
            return
        }

        for (member in party.members) {
            val qs = member.getQuestState(QUEST_ID)
            if (qs == null || !qs.isStarted && !qs.isCompleted) {
                showHtmlFile(player, npcId.toString() + "-NS.htm", npc, member)
                return
            }

            if (member.inventory!!.getItemByItemId(ENTRANCE_PASS) == null) {
                showHtmlFile(player, npcId.toString() + "-SE.htm", npc, member)
                return
            }

            if (member.weightPenalty > 2) {
                member.sendPacket(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT)
                return
            }
        }

        if (!isEntryTime) {
            showHtmlFile(player, npcId.toString() + "-NE.htm", npc, null)
            return
        }

        showHtmlFile(player, npcId.toString() + "-OK.htm", npc, null)

        val loc = startHallSpawns[npcId]!!

        // For every party player who isn't dead and who is near current player.
        for (member in party.members.filter { m ->
            !m.isDead && MathUtil.checkIfInRange(
                700,
                player,
                m,
                true
            )
        }) {
            // Allow zone timer.
            ZoneManager.getZone(
                loc.x,
                loc.y,
                loc.z,
                BossZone::class.java
            )?.allowPlayerEntry(member, 30)

            // Teleport the player.
            member.teleToLocation(loc, 80)

            // Delete entrance pass.
            member.destroyItemByItemId("Quest", ENTRANCE_PASS, 1, member, true)

            // Add Used Pass if Antique Brooch wasn't possessed.
            if (member.inventory!!.getItemByItemId(ANTIQUE_BROOCH) == null)
                member.addItem("Quest", USED_PASS, 1, member, true)

            // Delete all instances of Chapel Key.
            val key = member.inventory!!.getItemByItemId(CHAPEL_KEY)
            if (key != null)
                member.destroyItemByItemId("Quest", CHAPEL_KEY, key.count, member, true)
        }

        // Set the hall as being used.
        hallInUse[npcId] = true
    }

    fun spawnMysteriousBox(npcId: Int) {
        if (!isAttackTime)
            return

        val spawn = mysteriousBoxSpawns[npcId]
        if (spawn != null) {
            allMobs.add(spawn.doSpawn(false))
            spawn.setRespawnState(false)
        }
    }

    fun spawnMonster(npcId: Int) {
        if (!isAttackTime)
            return

        val monsterList = if (Rnd.nextBoolean()) physicalMonsters[npcId] else magicalMonsters[npcId]
        val mobs = ArrayList<Npc>()

        var spawnKeyBoxMob = false
        var spawnedKeyBoxMob = false

        for (spawn in monsterList!!) {
            if (spawnedKeyBoxMob)
                spawnKeyBoxMob = false
            else {
                when (npcId) {
                    31469, 31474, 31479, 31484 -> if (Rnd[48] == 0)
                        spawnKeyBoxMob = true

                    else -> spawnKeyBoxMob = false
                }
            }

            var mob: Npc? = null

            if (spawnKeyBoxMob) {
                try {
                    val template = NpcData.getTemplate(18149)
                    if (template == null) {
                        LOGGER.warn("Data missing in NPC table for ID: 18149.")
                        continue
                    }

                    val keyBoxMobSpawn = L2Spawn(template)
                    keyBoxMobSpawn.loc = spawn.getLoc()
                    keyBoxMobSpawn.respawnDelay = 3600

                    SpawnTable.addNewSpawn(keyBoxMobSpawn, false)
                    mob = keyBoxMobSpawn.doSpawn(false)
                    keyBoxMobSpawn.setRespawnState(false)
                } catch (e: Exception) {
                    LOGGER.error("Failed to initialize a spawn.", e)
                }

                spawnedKeyBoxMob = true
            } else {
                mob = spawn.doSpawn(false)
                spawn.setRespawnState(false)
            }

            if (mob != null) {
                mob.scriptValue = npcId
                when (npcId) {
                    31469, 31474, 31479, 31484, 31472, 31477, 31482, 31487 -> mobs.add(mob)
                }
                allMobs.add(mob)
            }
        }

        when (npcId) {
            31469, 31474, 31479, 31484 -> viscountMobs[npcId] = mobs

            31472, 31477, 31482, 31487 -> dukeMobs[npcId] = mobs
        }
    }

    @Synchronized
    fun testViscountMobsAnnihilation(npcId: Int) {
        val mobs = viscountMobs[npcId] ?: return

        for (mob in mobs) {
            if (!mob.isDead)
                return
        }

        spawnMonster(npcId)
    }

    @Synchronized
    fun testDukeMobsAnnihilation(npcId: Int) {
        val mobs = dukeMobs[npcId] ?: return

        for (mob in mobs) {
            if (!mob.isDead)
                return
        }

        spawnArchonOfHalisha(npcId)
    }

    fun spawnKeyBox(npc: Npc) {
        if (!isAttackTime)
            return

        val spawn = keyBoxSpawns[npc.npcId]
        if (spawn != null) {
            spawn.loc = npc.position
            spawn.respawnDelay = 3600

            allMobs.add(spawn.doSpawn(false))
            spawn.setRespawnState(false)
        }
    }

    fun spawnExecutionerOfHalisha(npc: Npc) {
        if (!isAttackTime)
            return

        val spawn = executionerSpawns[npc.npcId]
        if (spawn != null) {
            spawn.loc = npc.position
            spawn.respawnDelay = 3600

            allMobs.add(spawn.doSpawn(false))
            spawn.setRespawnState(false)
        }
    }

    fun spawnArchonOfHalisha(npcId: Int) {
        if (!isAttackTime)
            return

        if (archonSpawned[npcId] == true)
            return

        val monsterList = dukeFinalMobs[npcId]
        if (monsterList != null) {
            for (spawn in monsterList) {
                val mob = spawn.doSpawn(false)
                spawn.setRespawnState(false)

                if (mob != null) {
                    mob.scriptValue = npcId
                    allMobs.add(mob)
                }
            }
            archonSpawned[npcId] = true
        }
    }

    fun spawnEmperorsGraveNpc(npcId: Int) {
        if (!isAttackTime)
            return

        val monsterList = emperorsGraveNpcs[npcId]
        if (monsterList != null) {
            for (spawn in monsterList) {
                allMobs.add(spawn.doSpawn(false))
                spawn.setRespawnState(false)
            }
        }
    }

    /**
     * Spawn a Shadow of Halisha based on current Manager npcId.
     * @param npcId : the manager npcId.
     */
    fun spawnShadow(npcId: Int) {
        if (!isAttackTime)
            return

        val spawn = shadowSpawns[npcId]
        if (spawn != null) {
            val mob = spawn.doSpawn(false)
            spawn.setRespawnState(false)

            if (mob != null) {
                mob.scriptValue = npcId
                allMobs.add(mob)
            }
        }
    }

    fun showHtmlFile(player: Player, file: String, npc: Npc, member: Player?) {
        val html = NpcHtmlMessage(npc.objectId)
        html.setFile("data/html/sepulchers/$file")
        if (member != null)
            html.replace("%member%", member.name)

        player.sendPacket(html)
    }

    /**
     * Make each manager shout, informing Players they can now teleport in.
     */
    fun onEntryEvent() {
        // Managers shout. Entrance is allowed.
        val msg1 = "You may now enter the Sepulcher."
        val msg2 = "If you place your hand on the stone statue in front of each sepulcher, you will be able to enter."
        for (temp in managers) {
            (temp.npc as SepulcherNpc).sayInShout(msg1)
            (temp.npc as SepulcherNpc).sayInShout(msg2)
        }
    }

    /**
     * Prepare all Mysterious Boxes and refresh Shadows of Halisha spawn points.
     */
    fun onAttackEvent() {
        // Generate new locations for the 4 shadows.
        val newLoc = shadowSpawnLoc[Rnd[4]]

        // Refresh locations for all spawns.
        for (spawn in shadowSpawns.values) {
            val spawnLoc = newLoc?.get(spawn.npcId)
            if (spawnLoc != null)
                spawn.loc = spawnLoc
        }

        // Spawn all Mysterious Boxes.
        spawnMysteriousBox(31921)
        spawnMysteriousBox(31922)
        spawnMysteriousBox(31923)
        spawnMysteriousBox(31924)
    }

    /**
     * Reset all variables, teleport Players out of the zones, delete all Monsters, close all Doors.
     */
    fun onEndEvent() {
        // Managers shout. Game is ended.
        for (temp in managers) {
            // Hall isn't used right now, so its manager will not shout.
            if (hallInUse[temp.npcId] == false)
                continue

            (temp.npc as SepulcherNpc).sayInShout("Game over. The teleport will appear momentarily.")
        }

        // Teleport out all players, and destroy
        for (loc in startHallSpawns.values) {
            val players =
                ZoneManager.getZone(loc.x, loc.y, loc.z, BossZone::class.java)!!.oustAllPlayers()
            for (player in players) {
                // Delete all instances of Chapel Key.
                val key = player.inventory!!.getItemByItemId(CHAPEL_KEY)
                if (key != null)
                    player.destroyItemByItemId("Quest", CHAPEL_KEY, key.count, player, false)
            }
        }

        // Delete all monsters.
        for (mob in allMobs) {
            if (mob.spawn != null)
                mob.spawn.setRespawnState(false)

            mob.deleteMe()
        }
        allMobs.clear()

        // Close all doors.
        for (doorId in hallGateKeepers.values) {
            val door = DoorData.getDoor(doorId)
            door?.closeMe()
        }

        // Reset maps.
        for(pair in hallInUse){
            hallInUse[pair.key] = false
        }

        for(pair in archonSpawned){
            archonSpawned[pair.key] = false
        }
    }

    /**
     * Make each manager shout during ATTACK State, every 5 minutes. No shout on first minute.
     * @param currentMinute : the current minute.
     */
    fun managersShout(currentMinute: Int) {
        if (state == State.ATTACK && currentMinute != 0) {
            val modulo = currentMinute % 5
            if (modulo == 0) {
                val msg = currentMinute.toString() + " minute(s) have passed."

                for (temp in managers) {
                    // Hall isn't used right now, so its manager will not shout.
                    if (hallInUse[temp.npcId] == false)
                        continue

                    (temp.npc as SepulcherNpc).sayInShout(msg)
                }
            }
        }
    }

    private class Cycle : Runnable {
        override fun run() {
            val currentMinute = Calendar.getInstance().get(Calendar.MINUTE)

            // Calculate the new State.
            var newState = State.ATTACK
            if (currentMinute >= Config.FS_TIME_ENTRY)
                newState = State.ENTRY
            else if (currentMinute >= Config.FS_TIME_END)
                newState = State.END

            // A new State has been found. Fire the good event.
            if (newState != state) {
                state = newState

                when (state) {
                    FourSepulchersManager.State.ENTRY -> onEntryEvent()

                    FourSepulchersManager.State.ATTACK -> onAttackEvent()

                    FourSepulchersManager.State.END -> onEndEvent()
                }
                LOGGER.info("A new Four Sepulchers event has been announced ({}).", state)
            }

            // Managers shout during ATTACK state, every 5min, if the hall is under use.
            managersShout(currentMinute)
        }
    }
}