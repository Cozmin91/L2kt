package com.l2kt.gameserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.logging.LogManager;

import com.l2kt.Config;
import com.l2kt.L2DatabaseFactory;
import com.l2kt.commons.concurrent.ThreadPool;
import com.l2kt.commons.lang.StringUtil;
import com.l2kt.commons.logging.CLogger;
import com.l2kt.commons.mmocore.SelectorConfig;
import com.l2kt.commons.mmocore.SelectorThread;
import com.l2kt.commons.util.SysUtil;
import com.l2kt.util.DeadLockDetector;
import com.l2kt.util.IPv4Filter;

import com.l2kt.gameserver.communitybbs.Manager.ForumsBBSManager;
import com.l2kt.gameserver.data.ItemTable;
import com.l2kt.gameserver.data.SkillTable;
import com.l2kt.gameserver.data.SpawnTable;
import com.l2kt.gameserver.data.cache.CrestCache;
import com.l2kt.gameserver.data.cache.HtmCache;
import com.l2kt.gameserver.data.manager.BoatManager;
import com.l2kt.gameserver.data.manager.BufferManager;
import com.l2kt.gameserver.data.manager.BuyListManager;
import com.l2kt.gameserver.data.manager.CastleManager;
import com.l2kt.gameserver.data.manager.CastleManorManager;
import com.l2kt.gameserver.data.manager.CoupleManager;
import com.l2kt.gameserver.data.manager.CursedWeaponManager;
import com.l2kt.gameserver.data.manager.DerbyTrackManager;
import com.l2kt.gameserver.data.manager.DimensionalRiftManager;
import com.l2kt.gameserver.data.manager.FishingChampionshipManager;
import com.l2kt.gameserver.data.manager.FourSepulchersManager;
import com.l2kt.gameserver.data.manager.LotteryManager;
import com.l2kt.gameserver.data.manager.MovieMakerManager;
import com.l2kt.gameserver.data.manager.PetitionManager;
import com.l2kt.gameserver.data.manager.RaidPointManager;
import com.l2kt.gameserver.data.manager.ZoneManager;
import com.l2kt.gameserver.data.sql.BookmarkTable;
import com.l2kt.gameserver.data.sql.ClanTable;
import com.l2kt.gameserver.data.sql.PlayerInfoTable;
import com.l2kt.gameserver.data.sql.ServerMemoTable;
import com.l2kt.gameserver.data.xml.AdminData;
import com.l2kt.gameserver.data.xml.AnnouncementData;
import com.l2kt.gameserver.data.xml.ArmorSetData;
import com.l2kt.gameserver.data.xml.AugmentationData;
import com.l2kt.gameserver.data.xml.DoorData;
import com.l2kt.gameserver.data.xml.FishData;
import com.l2kt.gameserver.data.xml.HennaData;
import com.l2kt.gameserver.data.xml.HerbDropData;
import com.l2kt.gameserver.data.xml.MapRegionData;
import com.l2kt.gameserver.data.xml.MultisellData;
import com.l2kt.gameserver.data.xml.NewbieBuffData;
import com.l2kt.gameserver.data.xml.NpcData;
import com.l2kt.gameserver.data.xml.PlayerData;
import com.l2kt.gameserver.data.xml.RecipeData;
import com.l2kt.gameserver.data.xml.ScriptData;
import com.l2kt.gameserver.data.xml.SkillTreeData;
import com.l2kt.gameserver.data.xml.SoulCrystalData;
import com.l2kt.gameserver.data.xml.SpellbookData;
import com.l2kt.gameserver.data.xml.StaticObjectData;
import com.l2kt.gameserver.data.xml.SummonItemData;
import com.l2kt.gameserver.data.xml.TeleportLocationData;
import com.l2kt.gameserver.data.xml.WalkerRouteData;
import com.l2kt.gameserver.geoengine.GeoEngine;
import com.l2kt.gameserver.handler.AdminCommandHandler;
import com.l2kt.gameserver.handler.ChatHandler;
import com.l2kt.gameserver.handler.ItemHandler;
import com.l2kt.gameserver.handler.SkillHandler;
import com.l2kt.gameserver.handler.UserCommandHandler;
import com.l2kt.gameserver.idfactory.IdFactory;
import com.l2kt.gameserver.instancemanager.AuctionManager;
import com.l2kt.gameserver.instancemanager.AutoSpawnManager;
import com.l2kt.gameserver.instancemanager.ClanHallManager;
import com.l2kt.gameserver.instancemanager.DayNightSpawnManager;
import com.l2kt.gameserver.instancemanager.GrandBossManager;
import com.l2kt.gameserver.instancemanager.RaidBossSpawnManager;
import com.l2kt.gameserver.instancemanager.SevenSigns;
import com.l2kt.gameserver.instancemanager.SevenSignsFestival;
import com.l2kt.gameserver.model.World;
import com.l2kt.gameserver.model.boat.BoatGiranTalking;
import com.l2kt.gameserver.model.boat.BoatGludinRune;
import com.l2kt.gameserver.model.boat.BoatInnadrilTour;
import com.l2kt.gameserver.model.boat.BoatRunePrimeval;
import com.l2kt.gameserver.model.boat.BoatTalkingGludin;
import com.l2kt.gameserver.model.entity.Hero;
import com.l2kt.gameserver.model.olympiad.Olympiad;
import com.l2kt.gameserver.model.olympiad.OlympiadGameManager;
import com.l2kt.gameserver.model.partymatching.PartyMatchRoomList;
import com.l2kt.gameserver.model.partymatching.PartyMatchWaitingList;
import com.l2kt.gameserver.network.L2GameClient;
import com.l2kt.gameserver.network.L2GamePacketHandler;
import com.l2kt.gameserver.taskmanager.AttackStanceTaskManager;
import com.l2kt.gameserver.taskmanager.DecayTaskManager;
import com.l2kt.gameserver.taskmanager.GameTimeTaskManager;
import com.l2kt.gameserver.taskmanager.ItemsOnGroundTaskManager;
import com.l2kt.gameserver.taskmanager.MovementTaskManager;
import com.l2kt.gameserver.taskmanager.PvpFlagTaskManager;
import com.l2kt.gameserver.taskmanager.RandomAnimationTaskManager;
import com.l2kt.gameserver.taskmanager.ShadowItemTaskManager;
import com.l2kt.gameserver.taskmanager.WaterTaskManager;
import com.l2kt.gameserver.xmlfactory.XMLDocumentFactory;

public class GameServer
{
	private static final CLogger LOGGER = new CLogger(GameServer.class.getName());
	
	private final SelectorThread<L2GameClient> _selectorThread;
	
	private static GameServer _gameServer;
	
	public static void main(String[] args) throws Exception
	{
		_gameServer = new GameServer();
	}
	
	public GameServer() throws Exception
	{
		// Create log folder
		new File("./log").mkdir();
		new File("./log/chat").mkdir();
		new File("./log/console").mkdir();
		new File("./log/error").mkdir();
		new File("./log/gmaudit").mkdir();
		new File("./log/item").mkdir();
		new File("./data/crests").mkdirs();
		
		// Create input stream for log file -- or store file data into memory
		try (InputStream is = new FileInputStream(new File("config/logging.properties")))
		{
			LogManager.getLogManager().readConfiguration(is);
		}
		
		StringUtil.printSection("L2kt");
		
		// Initialize config
		Config.loadGameServer();
		
		// Factories
		XMLDocumentFactory.getInstance();
		L2DatabaseFactory.getInstance();
		ThreadPool.init();
		
		StringUtil.printSection("IdFactory");
		IdFactory.getInstance();
		
		StringUtil.printSection("World");
		World.getInstance();
		MapRegionData.getInstance();
		AnnouncementData.getInstance();
		ServerMemoTable.getInstance();
		
		StringUtil.printSection("Skills");
		SkillTable.getInstance();
		SkillTreeData.getInstance();
		
		StringUtil.printSection("Items");
		ItemTable.getInstance();
		SummonItemData.getInstance();
		HennaData.getInstance();
		BuyListManager.getInstance();
		MultisellData.getInstance();
		RecipeData.getInstance();
		ArmorSetData.getInstance();
		FishData.getInstance();
		SpellbookData.getInstance();
		SoulCrystalData.getInstance();
		AugmentationData.getInstance();
		CursedWeaponManager.getInstance();
		
		StringUtil.printSection("Admins");
		AdminData.getInstance();
		BookmarkTable.getInstance();
		MovieMakerManager.getInstance();
		PetitionManager.getInstance();
		
		StringUtil.printSection("Characters");
		PlayerData.getInstance();
		PlayerInfoTable.getInstance();
		NewbieBuffData.getInstance();
		TeleportLocationData.getInstance();
		HtmCache.getInstance();
		PartyMatchWaitingList.getInstance();
		PartyMatchRoomList.getInstance();
		RaidPointManager.getInstance();
		
		StringUtil.printSection("Community server");
		if (Config.ENABLE_COMMUNITY_BOARD) // Forums has to be loaded before clan data
			ForumsBBSManager.getInstance().initRoot();
		else
			LOGGER.info("Community server is disabled.");
		
		StringUtil.printSection("Clans");
		CrestCache.getInstance();
		ClanTable.getInstance();
		AuctionManager.getInstance();
		ClanHallManager.getInstance();
		
		StringUtil.printSection("Geodata & Pathfinding");
		GeoEngine.getInstance();
		
		StringUtil.printSection("Zones");
		ZoneManager.getInstance();
		
		StringUtil.printSection("Castles");
		CastleManager.getInstance();
		
		StringUtil.printSection("Task Managers");
		AttackStanceTaskManager.getInstance();
		DecayTaskManager.getInstance();
		GameTimeTaskManager.getInstance();
		ItemsOnGroundTaskManager.getInstance();
		MovementTaskManager.getInstance();
		PvpFlagTaskManager.getInstance();
		RandomAnimationTaskManager.getInstance();
		ShadowItemTaskManager.getInstance();
		WaterTaskManager.getInstance();
		
		StringUtil.printSection("Seven Signs");
		SevenSigns.getInstance().spawnSevenSignsNPC();
		SevenSignsFestival.getInstance();
		
		StringUtil.printSection("Manor Manager");
		CastleManorManager.getInstance();
		
		StringUtil.printSection("NPCs");
		BufferManager.getInstance();
		HerbDropData.getInstance();
		NpcData.getInstance();
		WalkerRouteData.getInstance();
		DoorData.getInstance().spawn();
		StaticObjectData.getInstance();
		SpawnTable.getInstance();
		RaidBossSpawnManager.getInstance();
		GrandBossManager.getInstance();
		DayNightSpawnManager.getInstance();
		DimensionalRiftManager.getInstance();
		
		StringUtil.printSection("Olympiads & Heroes");
		OlympiadGameManager.getInstance();
		Olympiad.getInstance();
		Hero.getInstance();
		
		StringUtil.printSection("Four Sepulchers");
		FourSepulchersManager.getInstance();
		
		StringUtil.printSection("Quests & Scripts");
		ScriptData.getInstance();
		
		if (Config.ALLOW_BOAT)
		{
			BoatManager.getInstance();
			BoatGiranTalking.load();
			BoatGludinRune.load();
			BoatInnadrilTour.load();
			BoatRunePrimeval.load();
			BoatTalkingGludin.load();
		}
		
		StringUtil.printSection("Events");
		DerbyTrackManager.getInstance();
		LotteryManager.getInstance();
		
		if (Config.ALLOW_WEDDING)
			CoupleManager.getInstance();
		
		if (Config.ALT_FISH_CHAMPIONSHIP_ENABLED)
			FishingChampionshipManager.getInstance();
		
		StringUtil.printSection("Handlers");
		LOGGER.info("AutoSpawnHandler: Loaded {} handlers.", AutoSpawnManager.getInstance().size());
		LOGGER.info("Loaded {} admin command handlers.", AdminCommandHandler.getInstance().size());
		LOGGER.info("Loaded {} chat handlers.", ChatHandler.getInstance().size());
		LOGGER.info("Loaded {} item handlers.", ItemHandler.getInstance().size());
		LOGGER.info("Loaded {} skill handlers.", SkillHandler.getInstance().size());
		LOGGER.info("Loaded {} user command handlers.", UserCommandHandler.getInstance().size());
		
		StringUtil.printSection("System");
		Runtime.getRuntime().addShutdownHook(Shutdown.getInstance());
		ForumsBBSManager.getInstance();
		
		if (Config.DEADLOCK_DETECTOR)
		{
			LOGGER.info("Deadlock detector is enabled. Timer: {}s.", Config.DEADLOCK_CHECK_INTERVAL);
			
			final DeadLockDetector deadDetectThread = new DeadLockDetector();
			deadDetectThread.setDaemon(true);
			deadDetectThread.start();
		}
		else
			LOGGER.info("Deadlock detector is disabled.");
		
		System.gc();
		
		LOGGER.info("Gameserver has started, used memory: {} / {} Mo.", SysUtil.getUsedMemory(), SysUtil.getMaxMemory());
		LOGGER.info("Maximum allowed players: {}.", Config.MAXIMUM_ONLINE_USERS);
		
		StringUtil.printSection("Login");
		LoginServerThread.getInstance().start();
		
		final SelectorConfig sc = new SelectorConfig();
		sc.MAX_READ_PER_PASS = Config.MMO_MAX_READ_PER_PASS;
		sc.MAX_SEND_PER_PASS = Config.MMO_MAX_SEND_PER_PASS;
		sc.SLEEP_TIME = Config.MMO_SELECTOR_SLEEP_TIME;
		sc.HELPER_BUFFER_COUNT = Config.MMO_HELPER_BUFFER_COUNT;
		
		final L2GamePacketHandler handler = new L2GamePacketHandler();
		_selectorThread = new SelectorThread<>(sc, handler, handler, handler, new IPv4Filter());
		
		InetAddress bindAddress = null;
		if (!Config.GAMESERVER_HOSTNAME.equals("*"))
		{
			try
			{
				bindAddress = InetAddress.getByName(Config.GAMESERVER_HOSTNAME);
			}
			catch (Exception e)
			{
				LOGGER.error("The GameServer bind address is invalid, using all available IPs.", e);
			}
		}
		
		try
		{
			_selectorThread.openServerSocket(bindAddress, Config.PORT_GAME);
		}
		catch (Exception e)
		{
			LOGGER.error("Failed to open server socket.", e);
			System.exit(1);
		}
		_selectorThread.start();
	}
	
	public static GameServer getInstance()
	{
		return _gameServer;
	}
	
	public SelectorThread<L2GameClient> getSelectorThread()
	{
		return _selectorThread;
	}
}