package com.l2kt.gameserver

import com.l2kt.Config
import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.commons.lang.StringUtil
import com.l2kt.commons.lang.StringUtil.printSection
import com.l2kt.commons.logging.CLogger
import com.l2kt.commons.mmocore.SelectorConfig
import com.l2kt.commons.mmocore.SelectorThread
import com.l2kt.commons.util.SysUtil
import com.l2kt.gameserver.communitybbs.Manager.ForumsBBSManager
import com.l2kt.gameserver.data.ItemTable
import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.data.SpawnTable
import com.l2kt.gameserver.data.cache.CrestCache
import com.l2kt.gameserver.data.cache.HtmCache
import com.l2kt.gameserver.data.manager.*
import com.l2kt.gameserver.data.sql.BookmarkTable
import com.l2kt.gameserver.data.sql.ClanTable
import com.l2kt.gameserver.data.sql.PlayerInfoTable
import com.l2kt.gameserver.data.sql.ServerMemoTable
import com.l2kt.gameserver.data.xml.*
import com.l2kt.gameserver.geoengine.GeoEngine
import com.l2kt.gameserver.handler.*
import com.l2kt.gameserver.idfactory.IdFactory
import com.l2kt.gameserver.instancemanager.*
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.model.boat.*
import com.l2kt.gameserver.model.entity.Hero
import com.l2kt.gameserver.model.olympiad.Olympiad
import com.l2kt.gameserver.model.olympiad.OlympiadGameManager
import com.l2kt.gameserver.model.partymatching.PartyMatchRoomList
import com.l2kt.gameserver.model.partymatching.PartyMatchWaitingList
import com.l2kt.gameserver.network.L2GameClient
import com.l2kt.gameserver.network.L2GamePacketHandler
import com.l2kt.gameserver.taskmanager.*
import com.l2kt.gameserver.xmlfactory.XMLDocumentFactory
import com.l2kt.util.DeadLockDetector
import com.l2kt.util.IPv4Filter
import java.io.File
import java.io.FileInputStream
import java.net.InetAddress
import java.util.logging.LogManager

object GameServer {

    private val LOGGER = CLogger(GameServer::class.simpleName!!)
    val selectorThread: SelectorThread<L2GameClient>

    @JvmStatic
    fun main(args: Array<String>) {
        GameServer
    }

    init {
        File("./log").mkdir()
        File("./log/chat").mkdir()
        File("./log/console").mkdir()
        File("./log/error").mkdir()
        File("./log/gmaudit").mkdir()
        File("./log/item").mkdir()
        File("./data/crests").mkdirs()

        FileInputStream(File("config/logging.properties")).use {
            LogManager.getLogManager().readConfiguration(it)
        }

        printSection("L2kt")

        Config.loadGameServer()

        XMLDocumentFactory
        L2DatabaseFactory
        ThreadPool.init()

        printSection("IdFactory")
        IdFactory.getInstance()

        StringUtil.printSection("World")
        World.getInstance()
        MapRegionData
        AnnouncementData
        ServerMemoTable

        StringUtil.printSection("Skills")
        SkillTable
        SkillTreeData

        StringUtil.printSection("Items")
        ItemTable
        SummonItemData
        HennaData
        BuyListManager
        MultisellData
        RecipeData
        ArmorSetData
        FishData
        SpellbookData
        SoulCrystalData
        AugmentationData
        CursedWeaponManager

        StringUtil.printSection("Admins")
        AdminData
        BookmarkTable
        MovieMakerManager
        PetitionManager

        StringUtil.printSection("Characters")
        PlayerData
        PlayerInfoTable
        NewbieBuffData
        TeleportLocationData
        HtmCache
        PartyMatchWaitingList.getInstance()
        PartyMatchRoomList.getInstance()
        RaidPointManager

        printSection("Community server")
        if (Config.ENABLE_COMMUNITY_BOARD)
            ForumsBBSManager.initRoot()
        else
            LOGGER.info("Community server is disabled.")

        printSection("Clans")
        CrestCache
        ClanTable
        AuctionManager
        ClanHallManager.getInstance()

        printSection("Geodata & Pathfinding")
        GeoEngine.getInstance()

        printSection("Zones")
        ZoneManager.getInstance()

        printSection("Castles")
        CastleManager

        printSection("Task Managers")
        AttackStanceTaskManager
        DecayTaskManager
        GameTimeTaskManager
        ItemsOnGroundTaskManager
        MovementTaskManager
        PvpFlagTaskManager
        RandomAnimationTaskManager
        ShadowItemTaskManager
        WaterTaskManager

        printSection("Seven Signs")
        SevenSigns.getInstance().spawnSevenSignsNPC()
        SevenSignsFestival.getInstance()

        printSection("Manor Manager")
        CastleManorManager

        printSection("NPCs")
        BufferManager
        HerbDropData
        NpcData
        WalkerRouteData
        DoorData.spawn()
        StaticObjectData
        SpawnTable
        RaidBossSpawnManager.getInstance()
        GrandBossManager.getInstance()
        DayNightSpawnManager.getInstance()
        DimensionalRiftManager

        printSection("Olympiads & Heroes")
        OlympiadGameManager.getInstance()
        Olympiad.getInstance()
        Hero.getInstance()

        printSection("Four Sepulchers")
        FourSepulchersManager.getInstance()

        printSection("Quests & Scripts")
        ScriptData

        if (Config.ALLOW_BOAT) {
            BoatManager
            BoatGiranTalking.load()
            BoatGludinRune.load()
            BoatInnadrilTour.load()
            BoatRunePrimeval.load()
            BoatTalkingGludin.load()
        }

        printSection("Events")
        DerbyTrackManager
        LotteryManager.getInstance()

        if (Config.ALLOW_WEDDING)
            CoupleManager

        if (Config.ALT_FISH_CHAMPIONSHIP_ENABLED)
            FishingChampionshipManager.getInstance()

        printSection("Handlers")
        LOGGER.info("AutoSpawnHandler: Loaded {} handlers.", AutoSpawnManager.getInstance().size())
        LOGGER.info("Loaded {} admin command handlers.", AdminCommandHandler.getInstance().size())
        LOGGER.info("Loaded {} chat handlers.", ChatHandler.getInstance().size())
        LOGGER.info("Loaded {} item handlers.", ItemHandler.getInstance().size())
        LOGGER.info("Loaded {} skill handlers.", SkillHandler.getInstance().size())
        LOGGER.info("Loaded {} user command handlers.", UserCommandHandler.getInstance().size())

        printSection("System")
        Runtime.getRuntime().addShutdownHook(Shutdown.instance)
        ForumsBBSManager

        if (Config.DEADLOCK_DETECTOR) {
            LOGGER.info("Deadlock detector is enabled. Timer: {}s.", Config.DEADLOCK_CHECK_INTERVAL)

            val deadDetectThread = DeadLockDetector()
            deadDetectThread.isDaemon = true
            deadDetectThread.start()
        } else
            LOGGER.info("Deadlock detector is disabled.")

        System.gc()

        LOGGER.info("Gameserver has started, used memory: {} / {} Mo.", SysUtil.getUsedMemory(), SysUtil.getMaxMemory())
        LOGGER.info("Maximum allowed players: {}.", Config.MAXIMUM_ONLINE_USERS)

        printSection("Login")
        LoginServerThread.start()

        val sc = SelectorConfig()
        sc.MAX_READ_PER_PASS = Config.MMO_MAX_READ_PER_PASS
        sc.MAX_SEND_PER_PASS = Config.MMO_MAX_SEND_PER_PASS
        sc.SLEEP_TIME = Config.MMO_SELECTOR_SLEEP_TIME
        sc.HELPER_BUFFER_COUNT = Config.MMO_HELPER_BUFFER_COUNT

        val handler = L2GamePacketHandler()
        selectorThread = SelectorThread(sc, handler, handler, handler, IPv4Filter())

        var bindAddress: InetAddress? = null
        if (Config.GAMESERVER_HOSTNAME != "*") {
            try {
                bindAddress = InetAddress.getByName(Config.GAMESERVER_HOSTNAME)
            } catch (e: Exception) {
                LOGGER.error("The GameServer bind address is invalid, using all available IPs.", e)
            }

        }

        try {
            selectorThread.openServerSocket(bindAddress, Config.PORT_GAME)
        } catch (e: Exception) {
            LOGGER.error("Failed to open server socket.", e)
            System.exit(1)
        }

        selectorThread.start()
    }
}