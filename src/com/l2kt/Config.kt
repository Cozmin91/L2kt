package com.l2kt

import com.l2kt.commons.config.ExProperties
import com.l2kt.commons.logging.CLogger
import com.l2kt.commons.math.MathUtil
import com.l2kt.gameserver.model.holder.IntIntHolder

import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.math.BigInteger
import java.util.*

/**
 * This class contains global server configuration.<br></br>
 * It has static final fields initialized from configuration files.<br></br>
 * @author mkizub
 */
object Config {
    private val LOGGER = CLogger(Config::class.java.name)

    const val CLANS_FILE = "./config/clans.properties"
    const val EVENTS_FILE = "./config/events.properties"
    const val GEOENGINE_FILE = "./config/geoengine.properties"
    const val HEXID_FILE = "./config/hexid.txt"
    const val LOGIN_CONFIGURATION_FILE = "./config/loginserver.properties"
    const val NPCS_FILE = "./config/npcs.properties"
    const val PLAYERS_FILE = "./config/players.properties"
    const val SERVER_FILE = "./config/server.properties"
    const val SIEGE_FILE = "./config/siege.properties"

    // --------------------------------------------------
    // Clans settings
    // --------------------------------------------------

    /** Clans  */
    var ALT_CLAN_JOIN_DAYS: Int = 0
    var ALT_CLAN_CREATE_DAYS: Int = 0
    var ALT_CLAN_DISSOLVE_DAYS: Int = 0
    var ALT_ALLY_JOIN_DAYS_WHEN_LEAVED: Int = 0
    var ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED: Int = 0
    var ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED: Int = 0
    var ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED: Int = 0
    var ALT_MAX_NUM_OF_CLANS_IN_ALLY: Int = 0
    var ALT_CLAN_MEMBERS_FOR_WAR: Int = 0
    var ALT_CLAN_WAR_PENALTY_WHEN_ENDED: Int = 0
    var ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH: Boolean = false

    /** Manor  */
    var ALT_MANOR_REFRESH_TIME: Int = 0
    var ALT_MANOR_REFRESH_MIN: Int = 0
    var ALT_MANOR_APPROVE_TIME: Int = 0
    var ALT_MANOR_APPROVE_MIN: Int = 0
    var ALT_MANOR_MAINTENANCE_MIN: Int = 0
    var ALT_MANOR_SAVE_PERIOD_RATE: Int = 0

    /** Clan Hall function  */
    var CH_TELE_FEE_RATIO: Long = 0
    var CH_TELE1_FEE: Int = 0
    var CH_TELE2_FEE: Int = 0
    var CH_ITEM_FEE_RATIO: Long = 0
    var CH_ITEM1_FEE: Int = 0
    var CH_ITEM2_FEE: Int = 0
    var CH_ITEM3_FEE: Int = 0
    var CH_MPREG_FEE_RATIO: Long = 0
    var CH_MPREG1_FEE: Int = 0
    var CH_MPREG2_FEE: Int = 0
    var CH_MPREG3_FEE: Int = 0
    var CH_MPREG4_FEE: Int = 0
    var CH_MPREG5_FEE: Int = 0
    var CH_HPREG_FEE_RATIO: Long = 0
    var CH_HPREG1_FEE: Int = 0
    var CH_HPREG2_FEE: Int = 0
    var CH_HPREG3_FEE: Int = 0
    var CH_HPREG4_FEE: Int = 0
    var CH_HPREG5_FEE: Int = 0
    var CH_HPREG6_FEE: Int = 0
    var CH_HPREG7_FEE: Int = 0
    var CH_HPREG8_FEE: Int = 0
    var CH_HPREG9_FEE: Int = 0
    var CH_HPREG10_FEE: Int = 0
    var CH_HPREG11_FEE: Int = 0
    var CH_HPREG12_FEE: Int = 0
    var CH_HPREG13_FEE: Int = 0
    var CH_EXPREG_FEE_RATIO: Long = 0
    var CH_EXPREG1_FEE: Int = 0
    var CH_EXPREG2_FEE: Int = 0
    var CH_EXPREG3_FEE: Int = 0
    var CH_EXPREG4_FEE: Int = 0
    var CH_EXPREG5_FEE: Int = 0
    var CH_EXPREG6_FEE: Int = 0
    var CH_EXPREG7_FEE: Int = 0
    var CH_SUPPORT_FEE_RATIO: Long = 0
    var CH_SUPPORT1_FEE: Int = 0
    var CH_SUPPORT2_FEE: Int = 0
    var CH_SUPPORT3_FEE: Int = 0
    var CH_SUPPORT4_FEE: Int = 0
    var CH_SUPPORT5_FEE: Int = 0
    var CH_SUPPORT6_FEE: Int = 0
    var CH_SUPPORT7_FEE: Int = 0
    var CH_SUPPORT8_FEE: Int = 0
    var CH_CURTAIN_FEE_RATIO: Long = 0
    var CH_CURTAIN1_FEE: Int = 0
    var CH_CURTAIN2_FEE: Int = 0
    var CH_FRONT_FEE_RATIO: Long = 0
    var CH_FRONT1_FEE: Int = 0
    var CH_FRONT2_FEE: Int = 0

    // --------------------------------------------------
    // Events settings
    // --------------------------------------------------

    /** Olympiad  */
    var ALT_OLY_START_TIME: Int = 0
    var ALT_OLY_MIN: Int = 0
    var ALT_OLY_CPERIOD: Long = 0
    var ALT_OLY_BATTLE: Long = 0
    var ALT_OLY_WPERIOD: Long = 0
    var ALT_OLY_VPERIOD: Long = 0
    var ALT_OLY_WAIT_TIME: Int = 0
    var ALT_OLY_WAIT_BATTLE: Int = 0
    var ALT_OLY_WAIT_END: Int = 0
    var ALT_OLY_START_POINTS: Int = 0
    var ALT_OLY_WEEKLY_POINTS: Int = 0
    var ALT_OLY_MIN_MATCHES: Int = 0
    var ALT_OLY_CLASSED: Int = 0
    var ALT_OLY_NONCLASSED: Int = 0
    lateinit var ALT_OLY_CLASSED_REWARD: Array<IntArray>
    lateinit var ALT_OLY_NONCLASSED_REWARD: Array<IntArray>
    var ALT_OLY_GP_PER_POINT: Int = 0
    var ALT_OLY_HERO_POINTS: Int = 0
    var ALT_OLY_RANK1_POINTS: Int = 0
    var ALT_OLY_RANK2_POINTS: Int = 0
    var ALT_OLY_RANK3_POINTS: Int = 0
    var ALT_OLY_RANK4_POINTS: Int = 0
    var ALT_OLY_RANK5_POINTS: Int = 0
    var ALT_OLY_MAX_POINTS: Int = 0
    var ALT_OLY_DIVIDER_CLASSED: Int = 0
    var ALT_OLY_DIVIDER_NON_CLASSED: Int = 0
    var ALT_OLY_ANNOUNCE_GAMES: Boolean = false

    /** SevenSigns Festival  */
    var ALT_GAME_CASTLE_DAWN: Boolean = false
    var ALT_GAME_CASTLE_DUSK: Boolean = false
    var ALT_FESTIVAL_MIN_PLAYER: Int = 0
    var ALT_MAXIMUM_PLAYER_CONTRIB: Int = 0
    var ALT_FESTIVAL_MANAGER_START: Long = 0
    var ALT_FESTIVAL_LENGTH: Long = 0
    var ALT_FESTIVAL_CYCLE_LENGTH: Long = 0
    var ALT_FESTIVAL_FIRST_SPAWN: Long = 0
    var ALT_FESTIVAL_FIRST_SWARM: Long = 0
    var ALT_FESTIVAL_SECOND_SPAWN: Long = 0
    var ALT_FESTIVAL_SECOND_SWARM: Long = 0
    var ALT_FESTIVAL_CHEST_SPAWN: Long = 0

    /** Four Sepulchers  */
    var FS_TIME_ENTRY: Int = 0
    var FS_TIME_END: Int = 0
    var FS_PARTY_MEMBER_COUNT: Int = 0

    /** dimensional rift  */
    var RIFT_MIN_PARTY_SIZE: Int = 0
    var RIFT_SPAWN_DELAY: Int = 0
    var RIFT_MAX_JUMPS: Int = 0
    var RIFT_AUTO_JUMPS_TIME_MIN: Int = 0
    var RIFT_AUTO_JUMPS_TIME_MAX: Int = 0
    var RIFT_ENTER_COST_RECRUIT: Int = 0
    var RIFT_ENTER_COST_SOLDIER: Int = 0
    var RIFT_ENTER_COST_OFFICER: Int = 0
    var RIFT_ENTER_COST_CAPTAIN: Int = 0
    var RIFT_ENTER_COST_COMMANDER: Int = 0
    var RIFT_ENTER_COST_HERO: Int = 0
    var RIFT_BOSS_ROOM_TIME_MUTIPLY: Double = 0.toDouble()

    /** Wedding system  */
    var ALLOW_WEDDING: Boolean = false
    var WEDDING_PRICE: Int = 0
    var WEDDING_SAMESEX: Boolean = false
    var WEDDING_FORMALWEAR: Boolean = false

    /** Lottery  */
    var ALT_LOTTERY_PRIZE: Int = 0
    var ALT_LOTTERY_TICKET_PRICE: Int = 0
    var ALT_LOTTERY_5_NUMBER_RATE: Double = 0.toDouble()
    var ALT_LOTTERY_4_NUMBER_RATE: Double = 0.toDouble()
    var ALT_LOTTERY_3_NUMBER_RATE: Double = 0.toDouble()
    var ALT_LOTTERY_2_AND_1_NUMBER_PRIZE: Int = 0

    /** Fishing tournament  */
    var ALT_FISH_CHAMPIONSHIP_ENABLED: Boolean = false
    var ALT_FISH_CHAMPIONSHIP_REWARD_ITEM: Int = 0
    var ALT_FISH_CHAMPIONSHIP_REWARD_1: Int = 0
    var ALT_FISH_CHAMPIONSHIP_REWARD_2: Int = 0
    var ALT_FISH_CHAMPIONSHIP_REWARD_3: Int = 0
    var ALT_FISH_CHAMPIONSHIP_REWARD_4: Int = 0
    var ALT_FISH_CHAMPIONSHIP_REWARD_5: Int = 0

    // --------------------------------------------------
    // GeoEngine
    // --------------------------------------------------

    /** Geodata  */
    var GEODATA_PATH: String = ""

    /** Path checking  */
    var PART_OF_CHARACTER_HEIGHT: Int = 0
    var MAX_OBSTACLE_HEIGHT: Int = 0

    /** Path finding  */
    var PATHFIND_BUFFERS: String = ""
    var BASE_WEIGHT: Int = 0
    var DIAGONAL_WEIGHT: Int = 0
    var HEURISTIC_WEIGHT: Int = 0
    var OBSTACLE_MULTIPLIER: Int = 0
    var MAX_ITERATIONS: Int = 0
    var DEBUG_PATH: Boolean = false
    var DEBUG_GEO_NODE: Boolean = false

    // --------------------------------------------------
    // HexID
    // --------------------------------------------------

    var SERVER_ID: Int = 0
    lateinit var HEX_ID: ByteArray

    // --------------------------------------------------
    // Loginserver
    // --------------------------------------------------

    lateinit var LOGIN_BIND_ADDRESS: String
    var PORT_LOGIN: Int = 0

    var LOGIN_TRY_BEFORE_BAN: Int = 0
    var LOGIN_BLOCK_AFTER_BAN: Int = 0
    var ACCEPT_NEW_GAMESERVER: Boolean = false

    var SHOW_LICENCE: Boolean = false

    var AUTO_CREATE_ACCOUNTS: Boolean = false

    var LOG_LOGIN_CONTROLLER: Boolean = false

    var FLOOD_PROTECTION: Boolean = false
    var FAST_CONNECTION_LIMIT: Int = 0
    var NORMAL_CONNECTION_TIME: Int = 0
    var FAST_CONNECTION_TIME: Int = 0
    var MAX_CONNECTION_PER_IP: Int = 0

    // --------------------------------------------------
    // NPCs / Monsters
    // --------------------------------------------------

    /** Champion Mod  */
    var CHAMPION_FREQUENCY: Int = 0
    var CHAMP_MIN_LVL: Int = 0
    var CHAMP_MAX_LVL: Int = 0
    var CHAMPION_HP: Int = 0
    var CHAMPION_REWARDS: Int = 0
    var CHAMPION_ADENAS_REWARDS: Int = 0
    var CHAMPION_HP_REGEN: Double = 0.toDouble()
    var CHAMPION_ATK: Double = 0.toDouble()
    var CHAMPION_SPD_ATK: Double = 0.toDouble()
    var CHAMPION_REWARD: Int = 0
    var CHAMPION_REWARD_ID: Int = 0
    var CHAMPION_REWARD_QTY: Int = 0

    /** Buffer  */
    var BUFFER_MAX_SCHEMES: Int = 0
    var BUFFER_STATIC_BUFF_COST: Int = 0

    /** Misc  */
    var ALLOW_CLASS_MASTERS: Boolean = false
    lateinit var CLASS_MASTER_SETTINGS: ClassMasterSettings
    var ALLOW_ENTIRE_TREE: Boolean = false
    var ANNOUNCE_MAMMON_SPAWN: Boolean = false
    var ALT_MOB_AGRO_IN_PEACEZONE: Boolean = false
    var SHOW_NPC_LVL: Boolean = false
    var SHOW_NPC_CREST: Boolean = false
    var SHOW_SUMMON_CREST: Boolean = false

    /** Wyvern Manager  */
    var WYVERN_ALLOW_UPGRADER: Boolean = false
    var WYVERN_REQUIRED_LEVEL: Int = 0
    var WYVERN_REQUIRED_CRYSTALS: Int = 0

    /** Raid Boss  */
    var RAID_HP_REGEN_MULTIPLIER: Double = 0.toDouble()
    var RAID_MP_REGEN_MULTIPLIER: Double = 0.toDouble()
    var RAID_DEFENCE_MULTIPLIER: Double = 0.toDouble()
    var RAID_MINION_RESPAWN_TIMER: Int = 0

    var RAID_DISABLE_CURSE: Boolean = false

    /** Grand Boss  */
    var SPAWN_INTERVAL_AQ: Int = 0
    var RANDOM_SPAWN_TIME_AQ: Int = 0

    var SPAWN_INTERVAL_ANTHARAS: Int = 0
    var RANDOM_SPAWN_TIME_ANTHARAS: Int = 0
    var WAIT_TIME_ANTHARAS: Int = 0

    var SPAWN_INTERVAL_BAIUM: Int = 0
    var RANDOM_SPAWN_TIME_BAIUM: Int = 0

    var SPAWN_INTERVAL_CORE: Int = 0
    var RANDOM_SPAWN_TIME_CORE: Int = 0

    var SPAWN_INTERVAL_FRINTEZZA: Int = 0
    var RANDOM_SPAWN_TIME_FRINTEZZA: Int = 0
    var WAIT_TIME_FRINTEZZA: Int = 0

    var SPAWN_INTERVAL_ORFEN: Int = 0
    var RANDOM_SPAWN_TIME_ORFEN: Int = 0

    var SPAWN_INTERVAL_SAILREN: Int = 0
    var RANDOM_SPAWN_TIME_SAILREN: Int = 0
    var WAIT_TIME_SAILREN: Int = 0

    var SPAWN_INTERVAL_VALAKAS: Int = 0
    var RANDOM_SPAWN_TIME_VALAKAS: Int = 0
    var WAIT_TIME_VALAKAS: Int = 0

    var SPAWN_INTERVAL_ZAKEN: Int = 0
    var RANDOM_SPAWN_TIME_ZAKEN: Int = 0

    /** AI  */
    var GUARD_ATTACK_AGGRO_MOB: Boolean = false
    var MAX_DRIFT_RANGE: Int = 0
    var MIN_NPC_ANIMATION: Int = 0
    var MAX_NPC_ANIMATION: Int = 0
    var MIN_MONSTER_ANIMATION: Int = 0
    var MAX_MONSTER_ANIMATION: Int = 0

    // --------------------------------------------------
    // Players
    // --------------------------------------------------

    /** Misc  */
    var EFFECT_CANCELING: Boolean = false
    var HP_REGEN_MULTIPLIER: Double = 0.toDouble()
    var MP_REGEN_MULTIPLIER: Double = 0.toDouble()
    var CP_REGEN_MULTIPLIER: Double = 0.toDouble()
    var PLAYER_SPAWN_PROTECTION: Int = 0
    var PLAYER_FAKEDEATH_UP_PROTECTION: Int = 0
    var RESPAWN_RESTORE_HP: Double = 0.toDouble()
    var MAX_PVTSTORE_SLOTS_DWARF: Int = 0
    var MAX_PVTSTORE_SLOTS_OTHER: Int = 0
    var DEEPBLUE_DROP_RULES: Boolean = false
    var ALT_GAME_DELEVEL: Boolean = false
    var DEATH_PENALTY_CHANCE: Int = 0

    /** Inventory & WH  */
    var INVENTORY_MAXIMUM_NO_DWARF: Int = 0
    var INVENTORY_MAXIMUM_DWARF: Int = 0
    var INVENTORY_MAXIMUM_QUEST_ITEMS: Int = 0
    var INVENTORY_MAXIMUM_PET: Int = 0
    var MAX_ITEM_IN_PACKET: Int = 0
    var ALT_WEIGHT_LIMIT: Double = 0.toDouble()
    var WAREHOUSE_SLOTS_NO_DWARF: Int = 0
    var WAREHOUSE_SLOTS_DWARF: Int = 0
    var WAREHOUSE_SLOTS_CLAN: Int = 0
    var FREIGHT_SLOTS: Int = 0
    var ALT_GAME_FREIGHTS: Boolean = false
    var ALT_GAME_FREIGHT_PRICE: Int = 0

    /** Enchant  */
    var ENCHANT_CHANCE_WEAPON_MAGIC: Double = 0.toDouble()
    var ENCHANT_CHANCE_WEAPON_MAGIC_15PLUS: Double = 0.toDouble()
    var ENCHANT_CHANCE_WEAPON_NONMAGIC: Double = 0.toDouble()
    var ENCHANT_CHANCE_WEAPON_NONMAGIC_15PLUS: Double = 0.toDouble()
    var ENCHANT_CHANCE_ARMOR: Double = 0.toDouble()
    var ENCHANT_MAX_WEAPON: Int = 0
    var ENCHANT_MAX_ARMOR: Int = 0
    var ENCHANT_SAFE_MAX: Int = 0
    var ENCHANT_SAFE_MAX_FULL: Int = 0

    /** Augmentations  */
    var AUGMENTATION_NG_SKILL_CHANCE: Int = 0
    var AUGMENTATION_NG_GLOW_CHANCE: Int = 0
    var AUGMENTATION_MID_SKILL_CHANCE: Int = 0
    var AUGMENTATION_MID_GLOW_CHANCE: Int = 0
    var AUGMENTATION_HIGH_SKILL_CHANCE: Int = 0
    var AUGMENTATION_HIGH_GLOW_CHANCE: Int = 0
    var AUGMENTATION_TOP_SKILL_CHANCE: Int = 0
    var AUGMENTATION_TOP_GLOW_CHANCE: Int = 0
    var AUGMENTATION_BASESTAT_CHANCE: Int = 0

    /** Karma & PvP  */
    var KARMA_PLAYER_CAN_BE_KILLED_IN_PZ: Boolean = false
    var KARMA_PLAYER_CAN_SHOP: Boolean = false
    var KARMA_PLAYER_CAN_USE_GK: Boolean = false
    var KARMA_PLAYER_CAN_TELEPORT: Boolean = false
    var KARMA_PLAYER_CAN_TRADE: Boolean = false
    var KARMA_PLAYER_CAN_USE_WH: Boolean = false

    var KARMA_DROP_GM: Boolean = false
    var KARMA_AWARD_PK_KILL: Boolean = false
    var KARMA_PK_LIMIT: Int = 0

    var KARMA_NONDROPPABLE_PET_ITEMS: String = ""
    var KARMA_NONDROPPABLE_ITEMS: String = ""
    var KARMA_LIST_NONDROPPABLE_PET_ITEMS: IntArray = intArrayOf()
    var KARMA_LIST_NONDROPPABLE_ITEMS: IntArray = intArrayOf()

    var PVP_NORMAL_TIME: Int = 0
    var PVP_PVP_TIME: Int = 0

    /** Party  */
    var PARTY_XP_CUTOFF_METHOD: String = ""
    var PARTY_XP_CUTOFF_LEVEL: Int = 0
    var PARTY_XP_CUTOFF_PERCENT: Double = 0.toDouble()
    var PARTY_RANGE: Int = 0

    /** GMs & Admin Stuff  */
    var DEFAULT_ACCESS_LEVEL: Int = 0
    var GM_HERO_AURA: Boolean = false
    var GM_STARTUP_INVULNERABLE: Boolean = false
    var GM_STARTUP_INVISIBLE: Boolean = false
    var GM_STARTUP_SILENCE: Boolean = false
    var GM_STARTUP_AUTO_LIST: Boolean = false

    /** petitions  */
    var PETITIONING_ALLOWED: Boolean = false
    var MAX_PETITIONS_PER_PLAYER: Int = 0
    var MAX_PETITIONS_PENDING: Int = 0

    /** Crafting  */
    var IS_CRAFTING_ENABLED: Boolean = false
    var DWARF_RECIPE_LIMIT: Int = 0
    var COMMON_RECIPE_LIMIT: Int = 0
    var ALT_BLACKSMITH_USE_RECIPES: Boolean = false

    /** Skills & Classes  */
    var AUTO_LEARN_SKILLS: Boolean = false
    var MAGIC_FAILURES: Boolean = false
    var PERFECT_SHIELD_BLOCK_RATE: Int = 0
    var LIFE_CRYSTAL_NEEDED: Boolean = false
    var SP_BOOK_NEEDED: Boolean = false
    var ES_SP_BOOK_NEEDED: Boolean = false
    var DIVINE_SP_BOOK_NEEDED: Boolean = false
    var SUBCLASS_WITHOUT_QUESTS: Boolean = false

    /** Buffs  */
    var STORE_SKILL_COOLTIME: Boolean = false
    var MAX_BUFFS_AMOUNT: Int = 0

    // --------------------------------------------------
    // Sieges
    // --------------------------------------------------

    var SIEGE_LENGTH: Int = 0
    var MINIMUM_CLAN_LEVEL: Int = 0
    var MAX_ATTACKERS_NUMBER: Int = 0
    var MAX_DEFENDERS_NUMBER: Int = 0
    var ATTACKERS_RESPAWN_DELAY: Int = 0

    // --------------------------------------------------
    // Server
    // --------------------------------------------------

    var GAMESERVER_HOSTNAME: String = ""
    var PORT_GAME: Int = 0
    var HOSTNAME: String = ""
    var GAME_SERVER_LOGIN_PORT: Int = 0
    var GAME_SERVER_LOGIN_HOST: String = ""
    var REQUEST_ID: Int = 0
    var ACCEPT_ALTERNATE_ID: Boolean = false

    /** Access to database  */
    var DATABASE_URL: String = ""
    var DATABASE_LOGIN: String = ""
    var DATABASE_PASSWORD: String = ""
    var DATABASE_MAX_CONNECTIONS: Int = 0

    /** serverList & Test  */
    var SERVER_LIST_BRACKET: Boolean = false
    var SERVER_LIST_CLOCK: Boolean = false
    var SERVER_LIST_AGE: Int = 0
    var SERVER_LIST_TESTSERVER: Boolean = false
    var SERVER_LIST_PVPSERVER: Boolean = false
    var SERVER_GMONLY: Boolean = false

    /** clients related  */
    var DELETE_DAYS: Int = 0
    var MAXIMUM_ONLINE_USERS: Int = 0

    /** Auto-loot  */
    var AUTO_LOOT: Boolean = false
    var AUTO_LOOT_HERBS: Boolean = false
    var AUTO_LOOT_RAID: Boolean = false

    /** Items Management  */
    var ALLOW_DISCARDITEM: Boolean = false
    var MULTIPLE_ITEM_DROP: Boolean = false
    var HERB_AUTO_DESTROY_TIME: Int = 0
    var ITEM_AUTO_DESTROY_TIME: Int = 0
    var EQUIPABLE_ITEM_AUTO_DESTROY_TIME: Int = 0
    var SPECIAL_ITEM_DESTROY_TIME: MutableMap<Int, Int> = mutableMapOf()
    var PLAYER_DROPPED_ITEM_MULTIPLIER: Int = 0

    /** Rate control  */
    var RATE_XP: Double = 0.toDouble()
    var RATE_SP: Double = 0.toDouble()
    var RATE_PARTY_XP: Double = 0.toDouble()
    var RATE_PARTY_SP: Double = 0.toDouble()
    var RATE_DROP_ADENA: Double = 0.toDouble()
    var RATE_DROP_ITEMS: Double = 0.toDouble()
    var RATE_DROP_ITEMS_BY_RAID: Double = 0.toDouble()
    var RATE_DROP_SPOIL: Double = 0.toDouble()
    var RATE_DROP_MANOR: Int = 0

    var RATE_QUEST_DROP: Double = 0.toDouble()
    var RATE_QUEST_REWARD: Double = 0.toDouble()
    var RATE_QUEST_REWARD_XP: Double = 0.toDouble()
    var RATE_QUEST_REWARD_SP: Double = 0.toDouble()
    var RATE_QUEST_REWARD_ADENA: Double = 0.toDouble()

    var RATE_KARMA_EXP_LOST: Double = 0.toDouble()
    var RATE_SIEGE_GUARDS_PRICE: Double = 0.toDouble()

    var PLAYER_DROP_LIMIT: Int = 0
    var PLAYER_RATE_DROP: Int = 0
    var PLAYER_RATE_DROP_ITEM: Int = 0
    var PLAYER_RATE_DROP_EQUIP: Int = 0
    var PLAYER_RATE_DROP_EQUIP_WEAPON: Int = 0

    var KARMA_DROP_LIMIT: Int = 0
    var KARMA_RATE_DROP: Int = 0
    var KARMA_RATE_DROP_ITEM: Int = 0
    var KARMA_RATE_DROP_EQUIP: Int = 0
    var KARMA_RATE_DROP_EQUIP_WEAPON: Int = 0

    var PET_XP_RATE: Double = 0.toDouble()
    var PET_FOOD_RATE: Int = 0
    var SINEATER_XP_RATE: Double = 0.toDouble()

    var RATE_DROP_COMMON_HERBS: Double = 0.toDouble()
    var RATE_DROP_HP_HERBS: Double = 0.toDouble()
    var RATE_DROP_MP_HERBS: Double = 0.toDouble()
    var RATE_DROP_SPECIAL_HERBS: Double = 0.toDouble()

    /** Allow types  */
    var ALLOW_FREIGHT: Boolean = false
    var ALLOW_WAREHOUSE: Boolean = false
    var ALLOW_WEAR: Boolean = false
    var WEAR_DELAY: Int = 0
    var WEAR_PRICE: Int = 0
    var ALLOW_LOTTERY: Boolean = false
    var ALLOW_WATER: Boolean = false
    var ALLOW_BOAT: Boolean = false
    var ALLOW_CURSED_WEAPONS: Boolean = false
    var ALLOW_MANOR: Boolean = false
    var ENABLE_FALLING_DAMAGE: Boolean = false

    /** Debug & Dev  */
    var ALT_DEV_NO_SPAWNS: Boolean = false
    var DEVELOPER: Boolean = false
    var PACKET_HANDLER_DEBUG: Boolean = false
    var DEBUG_MOVEMENT: Int = 0

    /** Deadlock Detector  */
    var DEADLOCK_DETECTOR: Boolean = false
    var DEADLOCK_CHECK_INTERVAL: Int = 0
    var RESTART_ON_DEADLOCK: Boolean = false

    /** Logs  */
    var LOG_CHAT: Boolean = false
    var LOG_ITEMS: Boolean = false
    var GMAUDIT: Boolean = false

    /** Community Board  */
    var ENABLE_COMMUNITY_BOARD: Boolean = false
    var BBS_DEFAULT: String = ""

    /** Flood Protectors  */
    var ROLL_DICE_TIME: Int = 0
    var HERO_VOICE_TIME: Int = 0
    var SUBCLASS_TIME: Int = 0
    var DROP_ITEM_TIME: Int = 0
    var SERVER_BYPASS_TIME: Int = 0
    var MULTISELL_TIME: Int = 0
    var MANUFACTURE_TIME: Int = 0
    var MANOR_TIME: Int = 0
    var SENDMAIL_TIME: Int = 0
    var CHARACTER_SELECT_TIME: Int = 0
    var GLOBAL_CHAT_TIME: Int = 0
    var TRADE_CHAT_TIME: Int = 0
    var SOCIAL_TIME: Int = 0

    /** ThreadPool  */
    var SCHEDULED_THREAD_POOL_COUNT: Int = 0
    var THREADS_PER_SCHEDULED_THREAD_POOL: Int = 0
    var INSTANT_THREAD_POOL_COUNT: Int = 0
    var THREADS_PER_INSTANT_THREAD_POOL: Int = 0

    /** Misc  */
    var L2WALKER_PROTECTION: Boolean = false
    var SERVER_NEWS: Boolean = false
    var ZONE_TOWN: Int = 0
    var DISABLE_TUTORIAL: Boolean = false

    // --------------------------------------------------
    // Those "hidden" settings haven't configs to avoid admins to fuck their server
    // You still can experiment changing values here. But don't say I didn't warn you.
    // --------------------------------------------------

    /** Reserve Host on LoginServerThread  */
    var RESERVE_HOST_ON_LOGIN = false // default false

    /** MMO settings  */
    var MMO_SELECTOR_SLEEP_TIME = 20 // default 20
    var MMO_MAX_SEND_PER_PASS = 80 // default 80
    var MMO_MAX_READ_PER_PASS = 80 // default 80
    var MMO_HELPER_BUFFER_COUNT = 20 // default 20

    /** Client Packets Queue settings  */
    var CLIENT_PACKET_QUEUE_SIZE = MMO_MAX_READ_PER_PASS + 2 // default MMO_MAX_READ_PER_PASS + 2
    var CLIENT_PACKET_QUEUE_MAX_BURST_SIZE = MMO_MAX_READ_PER_PASS + 1 // default MMO_MAX_READ_PER_PASS + 1
    var CLIENT_PACKET_QUEUE_MAX_PACKETS_PER_SECOND = 160 // default 160
    var CLIENT_PACKET_QUEUE_MEASURE_INTERVAL = 5 // default 5
    var CLIENT_PACKET_QUEUE_MAX_AVERAGE_PACKETS_PER_SECOND = 80 // default 80
    var CLIENT_PACKET_QUEUE_MAX_FLOODS_PER_MIN = 2 // default 2
    var CLIENT_PACKET_QUEUE_MAX_OVERFLOWS_PER_MIN = 1 // default 1
    var CLIENT_PACKET_QUEUE_MAX_UNDERFLOWS_PER_MIN = 1 // default 1
    var CLIENT_PACKET_QUEUE_MAX_UNKNOWN_PER_MIN = 5 // default 5

    // --------------------------------------------------

    /**
     * Initialize [ExProperties] from specified configuration file.
     * @param filename : File name to be loaded.
     * @return ExProperties : Initialized [ExProperties].
     */
    fun initProperties(filename: String): ExProperties {
        val result = ExProperties()

        try {
            result.load(File(filename))
        } catch (e: Exception) {
            LOGGER.error("An error occured loading '{}' config.", e, filename)
        }

        return result
    }

    /**
     * itemId1,itemNumber1;itemId2,itemNumber2... to the int[n][2] = [itemId1][itemNumber1],[itemId2][itemNumber2]...
     * @param line
     * @return an array consisting of parsed items.
     */
    private fun parseItemsList(line: String): Array<IntArray> {
        val propertySplit = line.split(";").dropLastWhile { it.isEmpty() }.toTypedArray()
        if (propertySplit.isEmpty())
            return emptyArray()

        var i = 0
        var valueSplit: Array<String>
        val result = arrayOfNulls<IntArray>(propertySplit.size)
        for (value in propertySplit) {
            valueSplit = value.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (valueSplit.size != 2) {
                LOGGER.warn("Error parsing entry '{}', it should be itemId,itemNumber.", valueSplit[0])
                return emptyArray()
            }

            result[i] = IntArray(2)
            try {
                result[i]!![0] = Integer.parseInt(valueSplit[0])
                result[i]!![1] = Integer.parseInt(valueSplit[1])
            } catch (e: Exception) {
                LOGGER.error("Error parsing entry '{}', one of the value isn't a number.", valueSplit[0])
                return emptyArray()
            }

            i++
        }
        return result.filterNotNull().toTypedArray()
    }

    /**
     * Loads clan and clan hall settings.
     */
    private fun loadClans() {
        val clans = initProperties(CLANS_FILE)
        ALT_CLAN_JOIN_DAYS = clans.getProperty("DaysBeforeJoinAClan", 5)
        ALT_CLAN_CREATE_DAYS = clans.getProperty("DaysBeforeCreateAClan", 10)
        ALT_MAX_NUM_OF_CLANS_IN_ALLY = clans.getProperty("AltMaxNumOfClansInAlly", 3)
        ALT_CLAN_MEMBERS_FOR_WAR = clans.getProperty("AltClanMembersForWar", 15)
        ALT_CLAN_WAR_PENALTY_WHEN_ENDED = clans.getProperty("AltClanWarPenaltyWhenEnded", 5)
        ALT_CLAN_DISSOLVE_DAYS = clans.getProperty("DaysToPassToDissolveAClan", 7)
        ALT_ALLY_JOIN_DAYS_WHEN_LEAVED = clans.getProperty("DaysBeforeJoinAllyWhenLeaved", 1)
        ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED = clans.getProperty("DaysBeforeJoinAllyWhenDismissed", 1)
        ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED = clans.getProperty("DaysBeforeAcceptNewClanWhenDismissed", 1)
        ALT_CREATE_ALLY_DAYS_WHEN_DISSOLVED = clans.getProperty("DaysBeforeCreateNewAllyWhenDissolved", 10)
        ALT_MEMBERS_CAN_WITHDRAW_FROM_CLANWH = clans.getProperty("AltMembersCanWithdrawFromClanWH", false)

        ALT_MANOR_REFRESH_TIME = clans.getProperty("AltManorRefreshTime", 20)
        ALT_MANOR_REFRESH_MIN = clans.getProperty("AltManorRefreshMin", 0)
        ALT_MANOR_APPROVE_TIME = clans.getProperty("AltManorApproveTime", 6)
        ALT_MANOR_APPROVE_MIN = clans.getProperty("AltManorApproveMin", 0)
        ALT_MANOR_MAINTENANCE_MIN = clans.getProperty("AltManorMaintenanceMin", 6)
        ALT_MANOR_SAVE_PERIOD_RATE = clans.getProperty("AltManorSavePeriodRate", 2) * 3600000

        CH_TELE_FEE_RATIO = clans.getProperty("ClanHallTeleportFunctionFeeRatio", 86400000).toLong()
        CH_TELE1_FEE = clans.getProperty("ClanHallTeleportFunctionFeeLvl1", 7000)
        CH_TELE2_FEE = clans.getProperty("ClanHallTeleportFunctionFeeLvl2", 14000)
        CH_SUPPORT_FEE_RATIO = clans.getProperty("ClanHallSupportFunctionFeeRatio", 86400000).toLong()
        CH_SUPPORT1_FEE = clans.getProperty("ClanHallSupportFeeLvl1", 17500)
        CH_SUPPORT2_FEE = clans.getProperty("ClanHallSupportFeeLvl2", 35000)
        CH_SUPPORT3_FEE = clans.getProperty("ClanHallSupportFeeLvl3", 49000)
        CH_SUPPORT4_FEE = clans.getProperty("ClanHallSupportFeeLvl4", 77000)
        CH_SUPPORT5_FEE = clans.getProperty("ClanHallSupportFeeLvl5", 147000)
        CH_SUPPORT6_FEE = clans.getProperty("ClanHallSupportFeeLvl6", 252000)
        CH_SUPPORT7_FEE = clans.getProperty("ClanHallSupportFeeLvl7", 259000)
        CH_SUPPORT8_FEE = clans.getProperty("ClanHallSupportFeeLvl8", 364000)
        CH_MPREG_FEE_RATIO = clans.getProperty("ClanHallMpRegenerationFunctionFeeRatio", 86400000).toLong()
        CH_MPREG1_FEE = clans.getProperty("ClanHallMpRegenerationFeeLvl1", 14000)
        CH_MPREG2_FEE = clans.getProperty("ClanHallMpRegenerationFeeLvl2", 26250)
        CH_MPREG3_FEE = clans.getProperty("ClanHallMpRegenerationFeeLvl3", 45500)
        CH_MPREG4_FEE = clans.getProperty("ClanHallMpRegenerationFeeLvl4", 96250)
        CH_MPREG5_FEE = clans.getProperty("ClanHallMpRegenerationFeeLvl5", 140000)
        CH_HPREG_FEE_RATIO = clans.getProperty("ClanHallHpRegenerationFunctionFeeRatio", 86400000).toLong()
        CH_HPREG1_FEE = clans.getProperty("ClanHallHpRegenerationFeeLvl1", 4900)
        CH_HPREG2_FEE = clans.getProperty("ClanHallHpRegenerationFeeLvl2", 5600)
        CH_HPREG3_FEE = clans.getProperty("ClanHallHpRegenerationFeeLvl3", 7000)
        CH_HPREG4_FEE = clans.getProperty("ClanHallHpRegenerationFeeLvl4", 8166)
        CH_HPREG5_FEE = clans.getProperty("ClanHallHpRegenerationFeeLvl5", 10500)
        CH_HPREG6_FEE = clans.getProperty("ClanHallHpRegenerationFeeLvl6", 12250)
        CH_HPREG7_FEE = clans.getProperty("ClanHallHpRegenerationFeeLvl7", 14000)
        CH_HPREG8_FEE = clans.getProperty("ClanHallHpRegenerationFeeLvl8", 15750)
        CH_HPREG9_FEE = clans.getProperty("ClanHallHpRegenerationFeeLvl9", 17500)
        CH_HPREG10_FEE = clans.getProperty("ClanHallHpRegenerationFeeLvl10", 22750)
        CH_HPREG11_FEE = clans.getProperty("ClanHallHpRegenerationFeeLvl11", 26250)
        CH_HPREG12_FEE = clans.getProperty("ClanHallHpRegenerationFeeLvl12", 29750)
        CH_HPREG13_FEE = clans.getProperty("ClanHallHpRegenerationFeeLvl13", 36166)
        CH_EXPREG_FEE_RATIO = clans.getProperty("ClanHallExpRegenerationFunctionFeeRatio", 86400000).toLong()
        CH_EXPREG1_FEE = clans.getProperty("ClanHallExpRegenerationFeeLvl1", 21000)
        CH_EXPREG2_FEE = clans.getProperty("ClanHallExpRegenerationFeeLvl2", 42000)
        CH_EXPREG3_FEE = clans.getProperty("ClanHallExpRegenerationFeeLvl3", 63000)
        CH_EXPREG4_FEE = clans.getProperty("ClanHallExpRegenerationFeeLvl4", 105000)
        CH_EXPREG5_FEE = clans.getProperty("ClanHallExpRegenerationFeeLvl5", 147000)
        CH_EXPREG6_FEE = clans.getProperty("ClanHallExpRegenerationFeeLvl6", 163331)
        CH_EXPREG7_FEE = clans.getProperty("ClanHallExpRegenerationFeeLvl7", 210000)
        CH_ITEM_FEE_RATIO = clans.getProperty("ClanHallItemCreationFunctionFeeRatio", 86400000).toLong()
        CH_ITEM1_FEE = clans.getProperty("ClanHallItemCreationFunctionFeeLvl1", 210000)
        CH_ITEM2_FEE = clans.getProperty("ClanHallItemCreationFunctionFeeLvl2", 490000)
        CH_ITEM3_FEE = clans.getProperty("ClanHallItemCreationFunctionFeeLvl3", 980000)
        CH_CURTAIN_FEE_RATIO = clans.getProperty("ClanHallCurtainFunctionFeeRatio", 86400000).toLong()
        CH_CURTAIN1_FEE = clans.getProperty("ClanHallCurtainFunctionFeeLvl1", 2002)
        CH_CURTAIN2_FEE = clans.getProperty("ClanHallCurtainFunctionFeeLvl2", 2625)
        CH_FRONT_FEE_RATIO = clans.getProperty("ClanHallFrontPlatformFunctionFeeRatio", 86400000).toLong()
        CH_FRONT1_FEE = clans.getProperty("ClanHallFrontPlatformFunctionFeeLvl1", 3031)
        CH_FRONT2_FEE = clans.getProperty("ClanHallFrontPlatformFunctionFeeLvl2", 9331)
    }

    /**
     * Loads event settings.<br></br>
     * Such as olympiad, seven signs festival, four sepulchures, dimensional rift, weddings, lottery, fishing championship.
     */
    private fun loadEvents() {
        val events = initProperties(EVENTS_FILE)
        ALT_OLY_START_TIME = events.getProperty("AltOlyStartTime", 18)
        ALT_OLY_MIN = events.getProperty("AltOlyMin", 0)
        ALT_OLY_CPERIOD = events.getProperty("AltOlyCPeriod", 21600000).toLong()
        ALT_OLY_BATTLE = events.getProperty("AltOlyBattle", 180000).toLong()
        ALT_OLY_WPERIOD = events.getProperty("AltOlyWPeriod", 604800000).toLong()
        ALT_OLY_VPERIOD = events.getProperty("AltOlyVPeriod", 86400000).toLong()
        ALT_OLY_WAIT_TIME = events.getProperty("AltOlyWaitTime", 30)
        ALT_OLY_WAIT_BATTLE = events.getProperty("AltOlyWaitBattle", 60)
        ALT_OLY_WAIT_END = events.getProperty("AltOlyWaitEnd", 40)
        ALT_OLY_START_POINTS = events.getProperty("AltOlyStartPoints", 18)
        ALT_OLY_WEEKLY_POINTS = events.getProperty("AltOlyWeeklyPoints", 3)
        ALT_OLY_MIN_MATCHES = events.getProperty("AltOlyMinMatchesToBeClassed", 5)
        ALT_OLY_CLASSED = events.getProperty("AltOlyClassedParticipants", 5)
        ALT_OLY_NONCLASSED = events.getProperty("AltOlyNonClassedParticipants", 9)
        ALT_OLY_CLASSED_REWARD = parseItemsList(events.getProperty("AltOlyClassedReward", "6651,50"))
        ALT_OLY_NONCLASSED_REWARD = parseItemsList(events.getProperty("AltOlyNonClassedReward", "6651,30"))
        ALT_OLY_GP_PER_POINT = events.getProperty("AltOlyGPPerPoint", 1000)
        ALT_OLY_HERO_POINTS = events.getProperty("AltOlyHeroPoints", 300)
        ALT_OLY_RANK1_POINTS = events.getProperty("AltOlyRank1Points", 100)
        ALT_OLY_RANK2_POINTS = events.getProperty("AltOlyRank2Points", 75)
        ALT_OLY_RANK3_POINTS = events.getProperty("AltOlyRank3Points", 55)
        ALT_OLY_RANK4_POINTS = events.getProperty("AltOlyRank4Points", 40)
        ALT_OLY_RANK5_POINTS = events.getProperty("AltOlyRank5Points", 30)
        ALT_OLY_MAX_POINTS = events.getProperty("AltOlyMaxPoints", 10)
        ALT_OLY_DIVIDER_CLASSED = events.getProperty("AltOlyDividerClassed", 3)
        ALT_OLY_DIVIDER_NON_CLASSED = events.getProperty("AltOlyDividerNonClassed", 3)
        ALT_OLY_ANNOUNCE_GAMES = events.getProperty("AltOlyAnnounceGames", true)

        ALT_GAME_CASTLE_DAWN = events.getProperty("AltCastleForDawn", true)
        ALT_GAME_CASTLE_DUSK = events.getProperty("AltCastleForDusk", true)
        ALT_FESTIVAL_MIN_PLAYER = MathUtil.limit(events.getProperty("AltFestivalMinPlayer", 5), 2, 9)
        ALT_MAXIMUM_PLAYER_CONTRIB = events.getProperty("AltMaxPlayerContrib", 1000000)
        ALT_FESTIVAL_MANAGER_START = events.getProperty("AltFestivalManagerStart", 120000).toLong()
        ALT_FESTIVAL_LENGTH = events.getProperty("AltFestivalLength", 1080000).toLong()
        ALT_FESTIVAL_CYCLE_LENGTH = events.getProperty("AltFestivalCycleLength", 2280000).toLong()
        ALT_FESTIVAL_FIRST_SPAWN = events.getProperty("AltFestivalFirstSpawn", 120000).toLong()
        ALT_FESTIVAL_FIRST_SWARM = events.getProperty("AltFestivalFirstSwarm", 300000).toLong()
        ALT_FESTIVAL_SECOND_SPAWN = events.getProperty("AltFestivalSecondSpawn", 540000).toLong()
        ALT_FESTIVAL_SECOND_SWARM = events.getProperty("AltFestivalSecondSwarm", 720000).toLong()
        ALT_FESTIVAL_CHEST_SPAWN = events.getProperty("AltFestivalChestSpawn", 900000).toLong()

        FS_TIME_ENTRY = events.getProperty("EntryTime", 55)
        FS_TIME_END = events.getProperty("EndTime", 50)
        FS_PARTY_MEMBER_COUNT = MathUtil.limit(events.getProperty("NeededPartyMembers", 4), 2, 9)

        RIFT_MIN_PARTY_SIZE = events.getProperty("RiftMinPartySize", 2)
        RIFT_MAX_JUMPS = events.getProperty("MaxRiftJumps", 4)
        RIFT_SPAWN_DELAY = events.getProperty("RiftSpawnDelay", 10000)
        RIFT_AUTO_JUMPS_TIME_MIN = events.getProperty("AutoJumpsDelayMin", 480)
        RIFT_AUTO_JUMPS_TIME_MAX = events.getProperty("AutoJumpsDelayMax", 600)
        RIFT_ENTER_COST_RECRUIT = events.getProperty("RecruitCost", 18)
        RIFT_ENTER_COST_SOLDIER = events.getProperty("SoldierCost", 21)
        RIFT_ENTER_COST_OFFICER = events.getProperty("OfficerCost", 24)
        RIFT_ENTER_COST_CAPTAIN = events.getProperty("CaptainCost", 27)
        RIFT_ENTER_COST_COMMANDER = events.getProperty("CommanderCost", 30)
        RIFT_ENTER_COST_HERO = events.getProperty("HeroCost", 33)
        RIFT_BOSS_ROOM_TIME_MUTIPLY = events.getProperty("BossRoomTimeMultiply", 1.0)

        ALLOW_WEDDING = events.getProperty("AllowWedding", false)
        WEDDING_PRICE = events.getProperty("WeddingPrice", 1000000)
        WEDDING_SAMESEX = events.getProperty("WeddingAllowSameSex", false)
        WEDDING_FORMALWEAR = events.getProperty("WeddingFormalWear", true)

        ALT_LOTTERY_PRIZE = events.getProperty("AltLotteryPrize", 50000)
        ALT_LOTTERY_TICKET_PRICE = events.getProperty("AltLotteryTicketPrice", 2000)
        ALT_LOTTERY_5_NUMBER_RATE = events.getProperty("AltLottery5NumberRate", 0.6)
        ALT_LOTTERY_4_NUMBER_RATE = events.getProperty("AltLottery4NumberRate", 0.2)
        ALT_LOTTERY_3_NUMBER_RATE = events.getProperty("AltLottery3NumberRate", 0.2)
        ALT_LOTTERY_2_AND_1_NUMBER_PRIZE = events.getProperty("AltLottery2and1NumberPrize", 200)

        ALT_FISH_CHAMPIONSHIP_ENABLED = events.getProperty("AltFishChampionshipEnabled", true)
        ALT_FISH_CHAMPIONSHIP_REWARD_ITEM = events.getProperty("AltFishChampionshipRewardItemId", 57)
        ALT_FISH_CHAMPIONSHIP_REWARD_1 = events.getProperty("AltFishChampionshipReward1", 800000)
        ALT_FISH_CHAMPIONSHIP_REWARD_2 = events.getProperty("AltFishChampionshipReward2", 500000)
        ALT_FISH_CHAMPIONSHIP_REWARD_3 = events.getProperty("AltFishChampionshipReward3", 300000)
        ALT_FISH_CHAMPIONSHIP_REWARD_4 = events.getProperty("AltFishChampionshipReward4", 200000)
        ALT_FISH_CHAMPIONSHIP_REWARD_5 = events.getProperty("AltFishChampionshipReward5", 100000)
    }

    /**
     * Loads geoengine settings.
     */
    private fun loadGeoengine() {
        val geoengine = initProperties(GEOENGINE_FILE)
        GEODATA_PATH = geoengine.getProperty("GeoDataPath", "./data/geodata/")

        PART_OF_CHARACTER_HEIGHT = geoengine.getProperty("PartOfCharacterHeight", 95)
        MAX_OBSTACLE_HEIGHT = geoengine.getProperty("MaxObstacleHeight", 32)

        PATHFIND_BUFFERS = geoengine.getProperty("PathFindBuffers", "100x6;128x6;192x6;256x4;320x4;384x4;500x2")
        BASE_WEIGHT = geoengine.getProperty("BaseWeight", 10)
        DIAGONAL_WEIGHT = geoengine.getProperty("DiagonalWeight", 14)
        OBSTACLE_MULTIPLIER = geoengine.getProperty("ObstacleMultiplier", 10)
        HEURISTIC_WEIGHT = geoengine.getProperty("HeuristicWeight", 20)
        MAX_ITERATIONS = geoengine.getProperty("MaxIterations", 3500)
        DEBUG_PATH = geoengine.getProperty("DebugPath", false)
        DEBUG_GEO_NODE = geoengine.getProperty("DebugGeoNode", false)
    }

    /**
     * Loads hex ID settings.
     */
    private fun loadHexID() {
        val hexid = initProperties(HEXID_FILE)
        SERVER_ID = Integer.parseInt(hexid.getProperty("ServerID"))
        HEX_ID = BigInteger(hexid.getProperty("HexID"), 16).toByteArray()
    }

    /**
     * Saves hexID file.
     * @param serverId : The ID of server.
     * @param hexId : The hexID of server.
     * @param filename : The file name.
     */
    @JvmOverloads
    fun saveHexid(serverId: Int, hexId: String, filename: String = HEXID_FILE) {
        try {
            val hexSetting = Properties()
            val file = File(filename)
            file.createNewFile()

            val out = FileOutputStream(file)
            hexSetting.setProperty("ServerID", serverId.toString())
            hexSetting.setProperty("HexID", hexId)
            hexSetting.store(out, "the hexID to auth into login")
            out.close()
        } catch (e: Exception) {
            LOGGER.error("Failed to save hex ID to '{}' file.", e, filename)
        }

    }

    /**
     * Loads NPC settings.<br></br>
     * Such as champion monsters, NPC buffer, class master, wyvern, raid bosses and grand bosses, AI.
     */
    private fun loadNpcs() {
        val npcs = initProperties(NPCS_FILE)
        CHAMPION_FREQUENCY = npcs.getProperty("ChampionFrequency", 0)
        CHAMP_MIN_LVL = npcs.getProperty("ChampionMinLevel", 20)
        CHAMP_MAX_LVL = npcs.getProperty("ChampionMaxLevel", 70)
        CHAMPION_HP = npcs.getProperty("ChampionHp", 8)
        CHAMPION_HP_REGEN = npcs.getProperty("ChampionHpRegen", 1.0)
        CHAMPION_REWARDS = npcs.getProperty("ChampionRewards", 8)
        CHAMPION_ADENAS_REWARDS = npcs.getProperty("ChampionAdenasRewards", 1)
        CHAMPION_ATK = npcs.getProperty("ChampionAtk", 1.0)
        CHAMPION_SPD_ATK = npcs.getProperty("ChampionSpdAtk", 1.0)
        CHAMPION_REWARD = npcs.getProperty("ChampionRewardItem", 0)
        CHAMPION_REWARD_ID = npcs.getProperty("ChampionRewardItemID", 6393)
        CHAMPION_REWARD_QTY = npcs.getProperty("ChampionRewardItemQty", 1)

        BUFFER_MAX_SCHEMES = npcs.getProperty("BufferMaxSchemesPerChar", 4)
        BUFFER_STATIC_BUFF_COST = npcs.getProperty("BufferStaticCostPerBuff", -1)

        ALLOW_CLASS_MASTERS = npcs.getProperty("AllowClassMasters", false)
        ALLOW_ENTIRE_TREE = npcs.getProperty("AllowEntireTree", false)
        if (ALLOW_CLASS_MASTERS)
            CLASS_MASTER_SETTINGS = ClassMasterSettings(npcs.getProperty("ConfigClassMaster"))

        ANNOUNCE_MAMMON_SPAWN = npcs.getProperty("AnnounceMammonSpawn", true)
        ALT_MOB_AGRO_IN_PEACEZONE = npcs.getProperty("AltMobAgroInPeaceZone", true)
        SHOW_NPC_LVL = npcs.getProperty("ShowNpcLevel", false)
        SHOW_NPC_CREST = npcs.getProperty("ShowNpcCrest", false)
        SHOW_SUMMON_CREST = npcs.getProperty("ShowSummonCrest", false)

        WYVERN_ALLOW_UPGRADER = npcs.getProperty("AllowWyvernUpgrader", true)
        WYVERN_REQUIRED_LEVEL = npcs.getProperty("RequiredStriderLevel", 55)
        WYVERN_REQUIRED_CRYSTALS = npcs.getProperty("RequiredCrystalsNumber", 10)

        RAID_HP_REGEN_MULTIPLIER = npcs.getProperty("RaidHpRegenMultiplier", 1.0)
        RAID_MP_REGEN_MULTIPLIER = npcs.getProperty("RaidMpRegenMultiplier", 1.0)
        RAID_DEFENCE_MULTIPLIER = npcs.getProperty("RaidDefenceMultiplier", 1.0)
        RAID_MINION_RESPAWN_TIMER = npcs.getProperty("RaidMinionRespawnTime", 300000)

        RAID_DISABLE_CURSE = npcs.getProperty("DisableRaidCurse", false)

        SPAWN_INTERVAL_AQ = npcs.getProperty("AntQueenSpawnInterval", 36)
        RANDOM_SPAWN_TIME_AQ = npcs.getProperty("AntQueenRandomSpawn", 17)

        SPAWN_INTERVAL_ANTHARAS = npcs.getProperty("AntharasSpawnInterval", 264)
        RANDOM_SPAWN_TIME_ANTHARAS = npcs.getProperty("AntharasRandomSpawn", 72)
        WAIT_TIME_ANTHARAS = npcs.getProperty("AntharasWaitTime", 30) * 60000

        SPAWN_INTERVAL_BAIUM = npcs.getProperty("BaiumSpawnInterval", 168)
        RANDOM_SPAWN_TIME_BAIUM = npcs.getProperty("BaiumRandomSpawn", 48)

        SPAWN_INTERVAL_CORE = npcs.getProperty("CoreSpawnInterval", 60)
        RANDOM_SPAWN_TIME_CORE = npcs.getProperty("CoreRandomSpawn", 23)

        SPAWN_INTERVAL_FRINTEZZA = npcs.getProperty("FrintezzaSpawnInterval", 48)
        RANDOM_SPAWN_TIME_FRINTEZZA = npcs.getProperty("FrintezzaRandomSpawn", 8)
        WAIT_TIME_FRINTEZZA = npcs.getProperty("FrintezzaWaitTime", 1) * 60000

        SPAWN_INTERVAL_ORFEN = npcs.getProperty("OrfenSpawnInterval", 48)
        RANDOM_SPAWN_TIME_ORFEN = npcs.getProperty("OrfenRandomSpawn", 20)

        SPAWN_INTERVAL_SAILREN = npcs.getProperty("SailrenSpawnInterval", 36)
        RANDOM_SPAWN_TIME_SAILREN = npcs.getProperty("SailrenRandomSpawn", 24)
        WAIT_TIME_SAILREN = npcs.getProperty("SailrenWaitTime", 5) * 60000

        SPAWN_INTERVAL_VALAKAS = npcs.getProperty("ValakasSpawnInterval", 264)
        RANDOM_SPAWN_TIME_VALAKAS = npcs.getProperty("ValakasRandomSpawn", 72)
        WAIT_TIME_VALAKAS = npcs.getProperty("ValakasWaitTime", 30) * 60000

        SPAWN_INTERVAL_ZAKEN = npcs.getProperty("ZakenSpawnInterval", 60)
        RANDOM_SPAWN_TIME_ZAKEN = npcs.getProperty("ZakenRandomSpawn", 20)

        GUARD_ATTACK_AGGRO_MOB = npcs.getProperty("GuardAttackAggroMob", false)
        MAX_DRIFT_RANGE = npcs.getProperty("MaxDriftRange", 300)
        MIN_NPC_ANIMATION = npcs.getProperty("MinNPCAnimation", 20)
        MAX_NPC_ANIMATION = npcs.getProperty("MaxNPCAnimation", 40)
        MIN_MONSTER_ANIMATION = npcs.getProperty("MinMonsterAnimation", 10)
        MAX_MONSTER_ANIMATION = npcs.getProperty("MaxMonsterAnimation", 40)
    }

    /**
     * Loads player settings.<br></br>
     * Such as stats, inventory/warehouse, enchant, augmentation, karma, party, admin, petition, skill learn.
     */
    private fun loadPlayers() {
        val players = initProperties(PLAYERS_FILE)
        EFFECT_CANCELING = players.getProperty("CancelLesserEffect", true)
        HP_REGEN_MULTIPLIER = players.getProperty("HpRegenMultiplier", 1.0)
        MP_REGEN_MULTIPLIER = players.getProperty("MpRegenMultiplier", 1.0)
        CP_REGEN_MULTIPLIER = players.getProperty("CpRegenMultiplier", 1.0)
        PLAYER_SPAWN_PROTECTION = players.getProperty("PlayerSpawnProtection", 0)
        PLAYER_FAKEDEATH_UP_PROTECTION = players.getProperty("PlayerFakeDeathUpProtection", 0)
        RESPAWN_RESTORE_HP = players.getProperty("RespawnRestoreHP", 0.7)
        MAX_PVTSTORE_SLOTS_DWARF = players.getProperty("MaxPvtStoreSlotsDwarf", 5)
        MAX_PVTSTORE_SLOTS_OTHER = players.getProperty("MaxPvtStoreSlotsOther", 4)
        DEEPBLUE_DROP_RULES = players.getProperty("UseDeepBlueDropRules", true)
        ALT_GAME_DELEVEL = players.getProperty("Delevel", true)
        DEATH_PENALTY_CHANCE = players.getProperty("DeathPenaltyChance", 20)

        INVENTORY_MAXIMUM_NO_DWARF = players.getProperty("MaximumSlotsForNoDwarf", 80)
        INVENTORY_MAXIMUM_DWARF = players.getProperty("MaximumSlotsForDwarf", 100)
        INVENTORY_MAXIMUM_QUEST_ITEMS = players.getProperty("MaximumSlotsForQuestItems", 100)
        INVENTORY_MAXIMUM_PET = players.getProperty("MaximumSlotsForPet", 12)
        MAX_ITEM_IN_PACKET = Math.max(INVENTORY_MAXIMUM_NO_DWARF, INVENTORY_MAXIMUM_DWARF)
        ALT_WEIGHT_LIMIT = players.getProperty("AltWeightLimit", 1).toDouble()
        WAREHOUSE_SLOTS_NO_DWARF = players.getProperty("MaximumWarehouseSlotsForNoDwarf", 100)
        WAREHOUSE_SLOTS_DWARF = players.getProperty("MaximumWarehouseSlotsForDwarf", 120)
        WAREHOUSE_SLOTS_CLAN = players.getProperty("MaximumWarehouseSlotsForClan", 150)
        FREIGHT_SLOTS = players.getProperty("MaximumFreightSlots", 20)
        ALT_GAME_FREIGHTS = players.getProperty("AltGameFreights", false)
        ALT_GAME_FREIGHT_PRICE = players.getProperty("AltGameFreightPrice", 1000)

        ENCHANT_CHANCE_WEAPON_MAGIC = players.getProperty("EnchantChanceMagicWeapon", 0.4)
        ENCHANT_CHANCE_WEAPON_MAGIC_15PLUS = players.getProperty("EnchantChanceMagicWeapon15Plus", 0.2)
        ENCHANT_CHANCE_WEAPON_NONMAGIC = players.getProperty("EnchantChanceNonMagicWeapon", 0.7)
        ENCHANT_CHANCE_WEAPON_NONMAGIC_15PLUS = players.getProperty("EnchantChanceNonMagicWeapon15Plus", 0.35)
        ENCHANT_CHANCE_ARMOR = players.getProperty("EnchantChanceArmor", 0.66)
        ENCHANT_MAX_WEAPON = players.getProperty("EnchantMaxWeapon", 0)
        ENCHANT_MAX_ARMOR = players.getProperty("EnchantMaxArmor", 0)
        ENCHANT_SAFE_MAX = players.getProperty("EnchantSafeMax", 3)
        ENCHANT_SAFE_MAX_FULL = players.getProperty("EnchantSafeMaxFull", 4)

        AUGMENTATION_NG_SKILL_CHANCE = players.getProperty("AugmentationNGSkillChance", 15)
        AUGMENTATION_NG_GLOW_CHANCE = players.getProperty("AugmentationNGGlowChance", 0)
        AUGMENTATION_MID_SKILL_CHANCE = players.getProperty("AugmentationMidSkillChance", 30)
        AUGMENTATION_MID_GLOW_CHANCE = players.getProperty("AugmentationMidGlowChance", 40)
        AUGMENTATION_HIGH_SKILL_CHANCE = players.getProperty("AugmentationHighSkillChance", 45)
        AUGMENTATION_HIGH_GLOW_CHANCE = players.getProperty("AugmentationHighGlowChance", 70)
        AUGMENTATION_TOP_SKILL_CHANCE = players.getProperty("AugmentationTopSkillChance", 60)
        AUGMENTATION_TOP_GLOW_CHANCE = players.getProperty("AugmentationTopGlowChance", 100)
        AUGMENTATION_BASESTAT_CHANCE = players.getProperty("AugmentationBaseStatChance", 1)

        KARMA_PLAYER_CAN_BE_KILLED_IN_PZ = players.getProperty("KarmaPlayerCanBeKilledInPeaceZone", false)
        KARMA_PLAYER_CAN_SHOP = players.getProperty("KarmaPlayerCanShop", false)
        KARMA_PLAYER_CAN_USE_GK = players.getProperty("KarmaPlayerCanUseGK", false)
        KARMA_PLAYER_CAN_TELEPORT = players.getProperty("KarmaPlayerCanTeleport", true)
        KARMA_PLAYER_CAN_TRADE = players.getProperty("KarmaPlayerCanTrade", true)
        KARMA_PLAYER_CAN_USE_WH = players.getProperty("KarmaPlayerCanUseWareHouse", true)
        KARMA_DROP_GM = players.getProperty("CanGMDropEquipment", false)
        KARMA_AWARD_PK_KILL = players.getProperty("AwardPKKillPVPPoint", true)
        KARMA_PK_LIMIT = players.getProperty("MinimumPKRequiredToDrop", 5)
        KARMA_NONDROPPABLE_PET_ITEMS =
            players.getProperty("ListOfPetItems", "2375,3500,3501,3502,4422,4423,4424,4425,6648,6649,6650")
        KARMA_NONDROPPABLE_ITEMS =
            players.getProperty("ListOfNonDroppableItemsForPK", "1147,425,1146,461,10,2368,7,6,2370,2369")

        var array = KARMA_NONDROPPABLE_PET_ITEMS.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        KARMA_LIST_NONDROPPABLE_PET_ITEMS = IntArray(array.size)

        for (i in array.indices)
            KARMA_LIST_NONDROPPABLE_PET_ITEMS[i] = Integer.parseInt(array[i])

        array = KARMA_NONDROPPABLE_ITEMS.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        KARMA_LIST_NONDROPPABLE_ITEMS = IntArray(array.size)

        for (i in array.indices)
            KARMA_LIST_NONDROPPABLE_ITEMS[i] = Integer.parseInt(array[i])

        // sorting so binarySearch can be used later
        Arrays.sort(KARMA_LIST_NONDROPPABLE_PET_ITEMS)
        Arrays.sort(KARMA_LIST_NONDROPPABLE_ITEMS)

        PVP_NORMAL_TIME = players.getProperty("PvPVsNormalTime", 15000)
        PVP_PVP_TIME = players.getProperty("PvPVsPvPTime", 30000)

        PARTY_XP_CUTOFF_METHOD = players.getProperty("PartyXpCutoffMethod", "level")
        PARTY_XP_CUTOFF_PERCENT = players.getProperty("PartyXpCutoffPercent", 3.0)
        PARTY_XP_CUTOFF_LEVEL = players.getProperty("PartyXpCutoffLevel", 20)
        PARTY_RANGE = players.getProperty("PartyRange", 1500)

        DEFAULT_ACCESS_LEVEL = players.getProperty("DefaultAccessLevel", 0)
        GM_HERO_AURA = players.getProperty("GMHeroAura", false)
        GM_STARTUP_INVULNERABLE = players.getProperty("GMStartupInvulnerable", true)
        GM_STARTUP_INVISIBLE = players.getProperty("GMStartupInvisible", true)
        GM_STARTUP_SILENCE = players.getProperty("GMStartupSilence", true)
        GM_STARTUP_AUTO_LIST = players.getProperty("GMStartupAutoList", true)

        PETITIONING_ALLOWED = players.getProperty("PetitioningAllowed", true)
        MAX_PETITIONS_PER_PLAYER = players.getProperty("MaxPetitionsPerPlayer", 5)
        MAX_PETITIONS_PENDING = players.getProperty("MaxPetitionsPending", 25)

        IS_CRAFTING_ENABLED = players.getProperty("CraftingEnabled", true)
        DWARF_RECIPE_LIMIT = players.getProperty("DwarfRecipeLimit", 50)
        COMMON_RECIPE_LIMIT = players.getProperty("CommonRecipeLimit", 50)
        ALT_BLACKSMITH_USE_RECIPES = players.getProperty("AltBlacksmithUseRecipes", true)

        AUTO_LEARN_SKILLS = players.getProperty("AutoLearnSkills", false)
        MAGIC_FAILURES = players.getProperty("MagicFailures", true)
        PERFECT_SHIELD_BLOCK_RATE = players.getProperty("PerfectShieldBlockRate", 5)
        LIFE_CRYSTAL_NEEDED = players.getProperty("LifeCrystalNeeded", true)
        SP_BOOK_NEEDED = players.getProperty("SpBookNeeded", true)
        ES_SP_BOOK_NEEDED = players.getProperty("EnchantSkillSpBookNeeded", true)
        DIVINE_SP_BOOK_NEEDED = players.getProperty("DivineInspirationSpBookNeeded", true)
        SUBCLASS_WITHOUT_QUESTS = players.getProperty("SubClassWithoutQuests", false)

        MAX_BUFFS_AMOUNT = players.getProperty("MaxBuffsAmount", 20)
        STORE_SKILL_COOLTIME = players.getProperty("StoreSkillCooltime", true)
    }

    /**
     * Loads siege settings.
     */
    private fun loadSieges() {
        val sieges = initProperties(Config.SIEGE_FILE)

        SIEGE_LENGTH = sieges.getProperty("SiegeLength", 120)
        MINIMUM_CLAN_LEVEL = sieges.getProperty("SiegeClanMinLevel", 4)
        MAX_ATTACKERS_NUMBER = sieges.getProperty("AttackerMaxClans", 10)
        MAX_DEFENDERS_NUMBER = sieges.getProperty("DefenderMaxClans", 10)
        ATTACKERS_RESPAWN_DELAY = sieges.getProperty("AttackerRespawn", 10000)
    }

    /**
     * Loads gameserver settings.<br></br>
     * IP addresses, database, rates, feature enabled/disabled, misc.
     */
    private fun loadServer() {
        val server = initProperties(SERVER_FILE)

        GAMESERVER_HOSTNAME = server.getProperty("GameserverHostname")
        PORT_GAME = server.getProperty("GameserverPort", 7777)

        HOSTNAME = server.getProperty("Hostname", "*")

        GAME_SERVER_LOGIN_PORT = server.getProperty("LoginPort", 9014)
        GAME_SERVER_LOGIN_HOST = server.getProperty("LoginHost", "127.0.0.1")

        REQUEST_ID = server.getProperty("RequestServerID", 0)
        ACCEPT_ALTERNATE_ID = server.getProperty("AcceptAlternateID", true)

        DATABASE_URL = server.getProperty("URL", "jdbc:mysql://localhost/L2kt")
        DATABASE_LOGIN = server.getProperty("Login", "root")
        DATABASE_PASSWORD = server.getProperty("Password", "")
        DATABASE_MAX_CONNECTIONS = server.getProperty("MaximumDbConnections", 10)

        SERVER_LIST_BRACKET = server.getProperty("ServerListBrackets", false)
        SERVER_LIST_CLOCK = server.getProperty("ServerListClock", false)
        SERVER_GMONLY = server.getProperty("ServerGMOnly", false)
        SERVER_LIST_AGE = server.getProperty("ServerListAgeLimit", 0)
        SERVER_LIST_TESTSERVER = server.getProperty("TestServer", false)
        SERVER_LIST_PVPSERVER = server.getProperty("PvpServer", true)

        DELETE_DAYS = server.getProperty("DeleteCharAfterDays", 7)
        MAXIMUM_ONLINE_USERS = server.getProperty("MaximumOnlineUsers", 100)

        AUTO_LOOT = server.getProperty("AutoLoot", false)
        AUTO_LOOT_HERBS = server.getProperty("AutoLootHerbs", false)
        AUTO_LOOT_RAID = server.getProperty("AutoLootRaid", false)

        ALLOW_DISCARDITEM = server.getProperty("AllowDiscardItem", true)
        MULTIPLE_ITEM_DROP = server.getProperty("MultipleItemDrop", true)
        HERB_AUTO_DESTROY_TIME = server.getProperty("AutoDestroyHerbTime", 15) * 1000
        ITEM_AUTO_DESTROY_TIME = server.getProperty("AutoDestroyItemTime", 600) * 1000
        EQUIPABLE_ITEM_AUTO_DESTROY_TIME = server.getProperty("AutoDestroyEquipableItemTime", 0) * 1000
        SPECIAL_ITEM_DESTROY_TIME = HashMap()
        val data = server.getProperty("AutoDestroySpecialItemTime", emptyArray(), ",")
        if (data != null) {
            for (itemData in data) {
                val item = itemData.split("-".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                SPECIAL_ITEM_DESTROY_TIME[Integer.parseInt(item[0])] = Integer.parseInt(item[1]) * 1000
            }
        }
        PLAYER_DROPPED_ITEM_MULTIPLIER = server.getProperty("PlayerDroppedItemMultiplier", 1)

        RATE_XP = server.getProperty("RateXp", 1.0)
        RATE_SP = server.getProperty("RateSp", 1.0)
        RATE_PARTY_XP = server.getProperty("RatePartyXp", 1.0)
        RATE_PARTY_SP = server.getProperty("RatePartySp", 1.0)
        RATE_DROP_ADENA = server.getProperty("RateDropAdena", 1.0)
        RATE_DROP_ITEMS = server.getProperty("RateDropItems", 1.0)
        RATE_DROP_ITEMS_BY_RAID = server.getProperty("RateRaidDropItems", 1.0)
        RATE_DROP_SPOIL = server.getProperty("RateDropSpoil", 1.0)
        RATE_DROP_MANOR = server.getProperty("RateDropManor", 1)
        RATE_QUEST_DROP = server.getProperty("RateQuestDrop", 1.0)
        RATE_QUEST_REWARD = server.getProperty("RateQuestReward", 1.0)
        RATE_QUEST_REWARD_XP = server.getProperty("RateQuestRewardXP", 1.0)
        RATE_QUEST_REWARD_SP = server.getProperty("RateQuestRewardSP", 1.0)
        RATE_QUEST_REWARD_ADENA = server.getProperty("RateQuestRewardAdena", 1.0)
        RATE_KARMA_EXP_LOST = server.getProperty("RateKarmaExpLost", 1.0)
        RATE_SIEGE_GUARDS_PRICE = server.getProperty("RateSiegeGuardsPrice", 1.0)
        RATE_DROP_COMMON_HERBS = server.getProperty("RateCommonHerbs", 1.0)
        RATE_DROP_HP_HERBS = server.getProperty("RateHpHerbs", 1.0)
        RATE_DROP_MP_HERBS = server.getProperty("RateMpHerbs", 1.0)
        RATE_DROP_SPECIAL_HERBS = server.getProperty("RateSpecialHerbs", 1.0)
        PLAYER_DROP_LIMIT = server.getProperty("PlayerDropLimit", 3)
        PLAYER_RATE_DROP = server.getProperty("PlayerRateDrop", 5)
        PLAYER_RATE_DROP_ITEM = server.getProperty("PlayerRateDropItem", 70)
        PLAYER_RATE_DROP_EQUIP = server.getProperty("PlayerRateDropEquip", 25)
        PLAYER_RATE_DROP_EQUIP_WEAPON = server.getProperty("PlayerRateDropEquipWeapon", 5)
        PET_XP_RATE = server.getProperty("PetXpRate", 1.0)
        PET_FOOD_RATE = server.getProperty("PetFoodRate", 1)
        SINEATER_XP_RATE = server.getProperty("SinEaterXpRate", 1.0)
        KARMA_DROP_LIMIT = server.getProperty("KarmaDropLimit", 10)
        KARMA_RATE_DROP = server.getProperty("KarmaRateDrop", 70)
        KARMA_RATE_DROP_ITEM = server.getProperty("KarmaRateDropItem", 50)
        KARMA_RATE_DROP_EQUIP = server.getProperty("KarmaRateDropEquip", 40)
        KARMA_RATE_DROP_EQUIP_WEAPON = server.getProperty("KarmaRateDropEquipWeapon", 10)

        ALLOW_FREIGHT = server.getProperty("AllowFreight", true)
        ALLOW_WAREHOUSE = server.getProperty("AllowWarehouse", true)
        ALLOW_WEAR = server.getProperty("AllowWear", true)
        WEAR_DELAY = server.getProperty("WearDelay", 5)
        WEAR_PRICE = server.getProperty("WearPrice", 10)
        ALLOW_LOTTERY = server.getProperty("AllowLottery", true)
        ALLOW_WATER = server.getProperty("AllowWater", true)
        ALLOW_MANOR = server.getProperty("AllowManor", true)
        ALLOW_BOAT = server.getProperty("AllowBoat", true)
        ALLOW_CURSED_WEAPONS = server.getProperty("AllowCursedWeapons", true)

        ENABLE_FALLING_DAMAGE = server.getProperty("EnableFallingDamage", true)

        ALT_DEV_NO_SPAWNS = server.getProperty("NoSpawns", false)
        DEVELOPER = server.getProperty("Developer", false)
        PACKET_HANDLER_DEBUG = server.getProperty("PacketHandlerDebug", false)
        DEBUG_MOVEMENT = server.getProperty("DebugMovement", 0) * 1000

        DEADLOCK_DETECTOR = server.getProperty("DeadLockDetector", false)
        DEADLOCK_CHECK_INTERVAL = server.getProperty("DeadLockCheckInterval", 20)
        RESTART_ON_DEADLOCK = server.getProperty("RestartOnDeadlock", false)

        LOG_CHAT = server.getProperty("LogChat", false)
        LOG_ITEMS = server.getProperty("LogItems", false)
        GMAUDIT = server.getProperty("GMAudit", false)

        ENABLE_COMMUNITY_BOARD = server.getProperty("EnableCommunityBoard", false)
        BBS_DEFAULT = server.getProperty("BBSDefault", "_bbshome")

        ROLL_DICE_TIME = server.getProperty("RollDiceTime", 4200)
        HERO_VOICE_TIME = server.getProperty("HeroVoiceTime", 10000)
        SUBCLASS_TIME = server.getProperty("SubclassTime", 2000)
        DROP_ITEM_TIME = server.getProperty("DropItemTime", 1000)
        SERVER_BYPASS_TIME = server.getProperty("ServerBypassTime", 500)
        MULTISELL_TIME = server.getProperty("MultisellTime", 100)
        MANUFACTURE_TIME = server.getProperty("ManufactureTime", 300)
        MANOR_TIME = server.getProperty("ManorTime", 3000)
        SENDMAIL_TIME = server.getProperty("SendMailTime", 10000)
        CHARACTER_SELECT_TIME = server.getProperty("CharacterSelectTime", 3000)
        GLOBAL_CHAT_TIME = server.getProperty("GlobalChatTime", 0)
        TRADE_CHAT_TIME = server.getProperty("TradeChatTime", 0)
        SOCIAL_TIME = server.getProperty("SocialTime", 2000)

        SCHEDULED_THREAD_POOL_COUNT = server.getProperty("ScheduledThreadPoolCount", -1)
        THREADS_PER_SCHEDULED_THREAD_POOL = server.getProperty("ThreadsPerScheduledThreadPool", 4)
        INSTANT_THREAD_POOL_COUNT = server.getProperty("InstantThreadPoolCount", -1)
        THREADS_PER_INSTANT_THREAD_POOL = server.getProperty("ThreadsPerInstantThreadPool", 2)

        L2WALKER_PROTECTION = server.getProperty("L2WalkerProtection", false)
        ZONE_TOWN = server.getProperty("ZoneTown", 0)
        SERVER_NEWS = server.getProperty("ShowServerNews", false)
        DISABLE_TUTORIAL = server.getProperty("DisableTutorial", false)
    }

    /**
     * Loads loginserver settings.<br></br>
     * IP addresses, database, account, misc.
     */
    private fun loadLogin() {
        val server = initProperties(LOGIN_CONFIGURATION_FILE)
        HOSTNAME = server.getProperty("Hostname", "localhost")

        LOGIN_BIND_ADDRESS = server.getProperty("LoginserverHostname", "*")
        PORT_LOGIN = server.getProperty("LoginserverPort", 2106)

        GAME_SERVER_LOGIN_HOST = server.getProperty("LoginHostname", "*")
        GAME_SERVER_LOGIN_PORT = server.getProperty("LoginPort", 9014)

        LOGIN_TRY_BEFORE_BAN = server.getProperty("LoginTryBeforeBan", 3)
        LOGIN_BLOCK_AFTER_BAN = server.getProperty("LoginBlockAfterBan", 600)
        ACCEPT_NEW_GAMESERVER = server.getProperty("AcceptNewGameServer", false)

        SHOW_LICENCE = server.getProperty("ShowLicence", true)

        DATABASE_URL = server.getProperty("URL", "jdbc:mysql://localhost/L2kt")
        DATABASE_LOGIN = server.getProperty("Login", "root")
        DATABASE_PASSWORD = server.getProperty("Password", "")
        DATABASE_MAX_CONNECTIONS = server.getProperty("MaximumDbConnections", 10)

        AUTO_CREATE_ACCOUNTS = server.getProperty("AutoCreateAccounts", true)

        LOG_LOGIN_CONTROLLER = server.getProperty("LogLoginController", false)

        FLOOD_PROTECTION = server.getProperty("EnableFloodProtection", true)
        FAST_CONNECTION_LIMIT = server.getProperty("FastConnectionLimit", 15)
        NORMAL_CONNECTION_TIME = server.getProperty("NormalConnectionTime", 700)
        FAST_CONNECTION_TIME = server.getProperty("FastConnectionTime", 350)
        MAX_CONNECTION_PER_IP = server.getProperty("MaxConnectionPerIP", 50)
    }

    fun loadGameServer() {
        LOGGER.info("Loading gameserver configuration files.")

        // clans settings
        loadClans()

        // events settings
        loadEvents()

        // geoengine settings
        loadGeoengine()

        // hexID
        loadHexID()

        // NPCs/monsters settings
        loadNpcs()

        // players settings
        loadPlayers()

        // siege settings
        loadSieges()

        // server settings
        loadServer()
    }

    fun loadLoginServer() {
        LOGGER.info("Loading loginserver configuration files.")

        // login settings
        loadLogin()
    }

    fun loadAccountManager() {
        LOGGER.info("Loading account manager configuration files.")

        // login settings
        loadLogin()
    }

    fun loadGameServerRegistration() {
        LOGGER.info("Loading gameserver registration configuration files.")

        // login settings
        loadLogin()
    }

    fun loadGeodataConverter() {
        LOGGER.info("Loading geodata converter configuration files.")

        // geoengine settings
        loadGeoengine()
    }

    class ClassMasterSettings(configLine: String?) {
        private val _allowedClassChange: MutableMap<Int, Boolean>?
        private val _claimItems: MutableMap<Int, List<IntIntHolder>>
        private val _rewardItems: MutableMap<Int, List<IntIntHolder>>

        init {
            _allowedClassChange = HashMap(3)
            _claimItems = HashMap(3)
            _rewardItems = HashMap(3)

            if (configLine != null)
                parseConfigLine(configLine.trim { it <= ' ' })
        }

        private fun parseConfigLine(configLine: String) {
            val st = StringTokenizer(configLine, ";")
            while (st.hasMoreTokens()) {
                // Get allowed class change.
                val job = Integer.parseInt(st.nextToken())

                _allowedClassChange!![job] = true

                var items: MutableList<IntIntHolder> = ArrayList()

                // Parse items needed for class change.
                if (st.hasMoreTokens()) {
                    val st2 = StringTokenizer(st.nextToken(), "[],")
                    while (st2.hasMoreTokens()) {
                        val st3 = StringTokenizer(st2.nextToken(), "()")
                        items.add(IntIntHolder(Integer.parseInt(st3.nextToken()), Integer.parseInt(st3.nextToken())))
                    }
                }

                // Feed the map, and clean the list.
                _claimItems[job] = items
                items = ArrayList()

                // Parse gifts after class change.
                if (st.hasMoreTokens()) {
                    val st2 = StringTokenizer(st.nextToken(), "[],")
                    while (st2.hasMoreTokens()) {
                        val st3 = StringTokenizer(st2.nextToken(), "()")
                        items.add(IntIntHolder(Integer.parseInt(st3.nextToken()), Integer.parseInt(st3.nextToken())))
                    }
                }

                _rewardItems[job] = items
            }
        }

        fun isAllowed(job: Int): Boolean {
            if (_allowedClassChange == null)
                return false

            return if (_allowedClassChange.containsKey(job)) _allowedClassChange[job]!! else false

        }

        fun getRewardItems(job: Int): List<IntIntHolder> {
            return _rewardItems[job] ?: emptyList()
        }

        fun getRequiredItems(job: Int): List<IntIntHolder> {
            return _claimItems[job] ?: emptyList()
        }
    }
}
/**
 * Saves hex ID file.
 * @param serverId : The ID of server.
 * @param hexId : The hex ID of server.
 */