package com.l2kt.gameserver.model.actor.instance

import com.l2kt.Config
import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.commons.math.MathUtil
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.LoginServerThread
import com.l2kt.gameserver.communitybbs.BB.Forum
import com.l2kt.gameserver.communitybbs.Manager.ForumsBBSManager
import com.l2kt.gameserver.data.ItemTable
import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.data.manager.*
import com.l2kt.gameserver.data.sql.ClanTable
import com.l2kt.gameserver.data.sql.PlayerInfoTable
import com.l2kt.gameserver.data.xml.*
import com.l2kt.gameserver.extensions.toKnownPlayers
import com.l2kt.gameserver.geoengine.GeoEngine
import com.l2kt.gameserver.handler.ItemHandler
import com.l2kt.gameserver.handler.admincommandhandlers.AdminEditChar
import com.l2kt.gameserver.instancemanager.SevenSigns
import com.l2kt.gameserver.instancemanager.SevenSigns.CabalType
import com.l2kt.gameserver.instancemanager.SevenSigns.SealType
import com.l2kt.gameserver.instancemanager.SevenSignsFestival
import com.l2kt.gameserver.model.*
import com.l2kt.gameserver.model.actor.*
import com.l2kt.gameserver.model.actor.ai.CtrlEvent
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.ai.NextAction
import com.l2kt.gameserver.model.actor.ai.type.CreatureAI
import com.l2kt.gameserver.model.actor.ai.type.PlayerAI
import com.l2kt.gameserver.model.actor.ai.type.SummonAI
import com.l2kt.gameserver.model.actor.appearance.PcAppearance
import com.l2kt.gameserver.model.actor.stat.CreatureStat
import com.l2kt.gameserver.model.actor.stat.PlayerStat
import com.l2kt.gameserver.model.actor.status.CreatureStatus
import com.l2kt.gameserver.model.actor.status.PlayerStatus
import com.l2kt.gameserver.model.actor.template.CreatureTemplate
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.model.actor.template.PetTemplate
import com.l2kt.gameserver.model.actor.template.PlayerTemplate
import com.l2kt.gameserver.model.base.*
import com.l2kt.gameserver.model.craft.ManufactureList
import com.l2kt.gameserver.model.entity.Castle
import com.l2kt.gameserver.model.entity.Duel.DuelState
import com.l2kt.gameserver.model.entity.Hero
import com.l2kt.gameserver.model.entity.Siege.SiegeSide
import com.l2kt.gameserver.model.group.Party
import com.l2kt.gameserver.model.group.Party.LootRule
import com.l2kt.gameserver.model.group.Party.MessageType
import com.l2kt.gameserver.model.holder.IntIntHolder
import com.l2kt.gameserver.model.holder.SkillUseHolder
import com.l2kt.gameserver.model.holder.Timestamp
import com.l2kt.gameserver.model.holder.skillnode.GeneralSkillNode
import com.l2kt.gameserver.model.item.Henna
import com.l2kt.gameserver.model.item.Recipe
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.model.item.kind.Item
import com.l2kt.gameserver.model.item.kind.Weapon
import com.l2kt.gameserver.model.item.type.ActionType
import com.l2kt.gameserver.model.item.type.ArmorType
import com.l2kt.gameserver.model.item.type.EtcItemType
import com.l2kt.gameserver.model.item.type.WeaponType
import com.l2kt.gameserver.model.itemcontainer.*
import com.l2kt.gameserver.model.itemcontainer.listeners.ItemPassiveSkillsListener
import com.l2kt.gameserver.model.location.Location
import com.l2kt.gameserver.model.location.SpawnLocation
import com.l2kt.gameserver.model.memo.PlayerMemo
import com.l2kt.gameserver.model.multisell.PreparedListContainer
import com.l2kt.gameserver.model.olympiad.OlympiadGameManager
import com.l2kt.gameserver.model.olympiad.OlympiadManager
import com.l2kt.gameserver.model.partymatching.PartyMatchRoomList
import com.l2kt.gameserver.model.partymatching.PartyMatchWaitingList
import com.l2kt.gameserver.model.pledge.Clan
import com.l2kt.gameserver.model.pledge.ClanMember
import com.l2kt.gameserver.model.tradelist.TradeList
import com.l2kt.gameserver.model.zone.ZoneId
import com.l2kt.gameserver.model.zone.type.BossZone
import com.l2kt.gameserver.network.L2GameClient
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.*
import com.l2kt.gameserver.network.serverpackets.SetupGauge.GaugeColor
import com.l2kt.gameserver.scripting.EventType
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import com.l2kt.gameserver.skills.Env
import com.l2kt.gameserver.skills.Formulas
import com.l2kt.gameserver.skills.Stats
import com.l2kt.gameserver.skills.funcs.*
import com.l2kt.gameserver.skills.l2skills.L2SkillSiegeFlag
import com.l2kt.gameserver.skills.l2skills.L2SkillSummon
import com.l2kt.gameserver.taskmanager.*
import com.l2kt.gameserver.templates.skills.L2EffectFlag
import com.l2kt.gameserver.templates.skills.L2EffectType
import com.l2kt.gameserver.templates.skills.L2SkillType
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.ReentrantLock
import java.util.stream.Collectors

/**
 * This class represents a player in the world.<br></br>
 * There is always a client-thread connected to this (except if a player-store is activated upon logout).
 */
class Player : Playable {

    /**
     * @return The client owner of this char.
     */
    var client: L2GameClient? = null
    private val _chars = HashMap<Int, String>()

    private val _accountName: String
    /**
     * @return The _deleteTimer of the Player.
     */
    /**
     * Set the _deleteTimer of the Player.
     * @param deleteTimer Time in ms.
     */
    var deleteTimer: Long = 0

    /**
     * @return True if the Player is online.
     */
    var isOnline: Boolean = false
        private set
    private var _onlineTime: Long = 0
    private var _onlineBeginTime: Long = 0
    var lastAccess: Long = 0
        private set
    var uptime: Long = 0
        get() = System.currentTimeMillis() - field

    var baseClass: Int = 0
    protected var _activeClass: Int = 0
    var classIndex: Int = 0
        protected set

    val subClasses: MutableMap<Int, SubClass> = ConcurrentSkipListMap()
    private val _subclassLock = ReentrantLock()

    val appearance: PcAppearance

    /**
     * Set the exp of the Player before a death
     * @param exp
     */
    var expBeforeDeath: Long = 0
    /**
     * Return the Karma of the Player.
     */
    /**
     * Set the Karma of the Player and send StatusUpdate (broadcast).
     * @param karma A value.
     */
    // send message with new karma value
    override var karma: Int = 0
        set(karma) {
            var karma = karma
            if (karma < 0)
                karma = 0

            if (this.karma > 0 && karma == 0) {
                sendPacket(UserInfo(this))
                broadcastRelationsChanges()
            }
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOUR_KARMA_HAS_BEEN_CHANGED_TO_S1).addNumber(karma))

            field = karma
            broadcastKarma()
        }
    /**
     * @return PvP Kills of the Player (number of player killed during a PvP).
     */
    /**
     * Set PvP Kills of the Player (number of player killed during a PvP).
     * @param pvpKills A value.
     */
    var pvpKills: Int = 0
    /**
     * @return the PK counter of the Player.
     */
    /**
     * Set the PK counter of the Player.
     * @param pkKills A number.
     */
    var pkKills: Int = 0
    override var pvpFlag: Byte = 0
        private set
    /**
     * @return the siege state of the Player.
     */
    /**
     * Set the siege state of the Player.
     * @param siegeState 1 = attacker, 2 = defender, 0 = not involved
     */
    var siegeState: Byte = 0
    var weightPenalty: Int = 0
        private set

    private var _lastCompassZone: Int = 0 // the last compass zone update send to the client

    var isIn7sDungeon: Boolean = false

    /**
     * @return punishment level of player
     */
    var punishLevel = PunishLevel.NONE
        private set
    var punishTimer: Long = 0
    private var _punishTask: ScheduledFuture<*>? = null

    var isInOlympiadMode: Boolean = false
        private set
    var isOlympiadStart: Boolean = false
    var olympiadGameId = -1
    var olympiadSide = -1

    var duelState = DuelState.NO_DUEL
    var duelId: Int = 0
        private set
    private var _noDuelReason = SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL

    var boat: Boat? = null
        set(v) {
            if (v == null && boat != null)
                boat!!.removePassenger(this)

            field = v
        }
    val boatPosition = SpawnLocation(0, 0, 0, 0)

    private var _canFeed: Boolean = false
    var petTemplate: PetTemplate? = null
        protected set
    var petDataEntry: PetDataEntry? = null
        private set
    private var _controlItemId: Int = 0
    var currentFeed: Int = 0
        set(num) {
            field = Math.min(num, petDataEntry!!.maxMeal)

            sendPacket(
                SetupGauge(
                    GaugeColor.GREEN,
                    currentFeed * 10000 / feedConsume,
                    petDataEntry!!.maxMeal * 10000 / feedConsume
                )
            )
        }
    protected var _mountFeedTask: Future<*>? = null
    private var _dismountTask: ScheduledFuture<*>? = null

    /**
     * @return the type of Pet mounted (0 : none, 1 : Strider, 2 : Wyvern).
     */
    var mountType: Int = 0
        private set
    var mountNpcId: Int = 0
        private set
    var mountLevel: Int = 0
        private set
    var mountObjectId: Int = 0

    protected var _throneId: Int = 0

    var teleMode: Int = 0
    var isCrystallizing: Boolean = false
    var isCrafting: Boolean = false

    private val _dwarvenRecipeBook = HashMap<Int, Recipe>()
    private val _commonRecipeBook = HashMap<Int, Recipe>()

    /**
     * @return True if the Player is sitting.
     */
    /**
     * Set _isSitting to given value.
     * @param state A boolean.
     */
    var isSitting: Boolean = false

    val savedLocation = Location(0, 0, 0)

    private var _recomHave: Int = 0
    private var _recomLeft: Int = 0
    private val _recomChars = ArrayList<Int>()

    /**
     * Return the PcInventory Inventory of the Player contained in _inventory.
     */
    override val inventory: PcInventory? = PcInventory(this)
    private var _warehouse: PcWarehouse? = null
    private var _freight: PcFreight? = null
    private val _depositedFreight = ArrayList<PcFreight>()

    /**
     * @return The Store type of the Player.
     */
    /**
     * Set the Store type of the Player.
     * @param type : 0 = none, 1 = sell, 2 = sellmanage, 3 = buy, 4 = buymanage, 5 = manufacture.
     */
    var storeType = StoreType.NONE

    /**
     * @return The active TradeList.
     */
    /**
     * Set the TradeList to be used in next activity.
     * @param tradeList The TradeList to be used.
     */
    var activeTradeList: TradeList? = null
    /**
     * @return The active Warehouse.
     */
    /**
     * Select the Warehouse to be used in next activity.
     * @param warehouse An active warehouse.
     */
    var activeWarehouse: ItemContainer? = null
    /**
     * @return The _createList object of the Player.
     */
    /**
     * Set the _createList object of the Player.
     * @param list
     */
    var createList: ManufactureList? = null
    private var _sellList: TradeList? = null
    private var _buyList: TradeList? = null

    var multiSell: PreparedListContainer? = null

    var isNoble: Boolean = false
        private set
    var isHero: Boolean = false
        set(hero) {
            if (hero && baseClass == _activeClass) {
                for (skill in SkillTable.heroSkills)
                    addSkill(skill, false)
            } else {
                for (skill in SkillTable.heroSkills)
                    removeSkill(skill!!.getId(), false)
            }
            field = hero

            sendSkillList()
        }

    override var isAlikeDead: Boolean
        get() = if (super.isAlikeDead) true else isFakeDeath
        set(value: Boolean) {
            super.isAlikeDead = value
        }

    /**
     * @return the current [Folk] of the [Player].
     */
    /**
     * Remember the current [Folk] of the [Player], used notably for integrity check.
     * @param folk : The Folk to remember.
     */
    var currentFolk: Folk? = null

    /**
     * @return the Id for the last talked quest NPC.
     */
    var lastQuestNpcObject: Int = 0

    private val _quests = ArrayList<QuestState>()
    private val _notifyQuestOfDeathList = ArrayList<QuestState>()

    /**
     * @return player memos.
     */
    val memos = PlayerMemo(objectId)

    private val _shortCuts = ShortCuts(this)

    /**
     * @return all L2Macro of the Player.
     */
    val macroses = MacroList(this)

    private val _henna = arrayOfNulls<Henna>(3)
    var hennaStatSTR: Int = 0
        private set
    var hennaStatINT: Int = 0
        private set
    var hennaStatDEX: Int = 0
        private set
    var hennaStatMEN: Int = 0
        private set
    var hennaStatWIT: Int = 0
        private set
    var hennaStatCON: Int = 0
        private set

    /**
     * Return the L2Summon of the Player or null.
     */
    /**
     * Set the L2Summon of the Player.
     * @param summon The Object.
     */
    override var pet: Summon? = null
    /**
     * @return the L2TamedBeast of the Player or null.
     */
    /**
     * Set the L2TamedBeast of the Player.
     * @param tamedBeast The Object.
     */
    var trainedBeast: TamedBeast? = null

    // TODO: This needs to be better integrated and saved/loaded
    val radar: L2Radar

    var partyRoom: Int = 0

    /**
     * @return The Clan Identifier of the Player.
     */
    var clanId: Int = 0
        private set
    /**
     * @return The _clan object of the Player.
     */
    /**
     * Set the _clan object, _clanId, _clanLeader Flag and title of the Player.
     * @param clan The Clan object which is used to feed Player values.
     */
    // char has been kicked from clan
    var clan: Clan? = null
        set(clan) {
            this.clan = clan
            title = ""

            if (clan == null) {
                clanId = 0
                clanPrivileges = 0
                pledgeType = 0
                powerGrade = 0
                lvlJoinedAcademy = 0
                apprentice = 0
                sponsor = 0
                return
            }

            if (!clan.isMember(objectId)) {
                this.clan = null
                return
            }

            clanId = clan.clanId
        }
    var apprentice: Int = 0
    var sponsor: Int = 0
    var clanJoinExpiryTime: Long = 0
    var clanCreateExpiryTime: Long = 0
    var powerGrade: Int = 0
    var clanPrivileges: Int = 0
    var pledgeClass: Int = 0
    var pledgeType: Int = 0
    var lvlJoinedAcademy: Int = 0

    private var _wantsPeace: Boolean = false

    var deathPenaltyBuffLevel: Int = 0

    private val _charges = AtomicInteger()
    private var _chargeTask: ScheduledFuture<*>? = null

    var currentSkillWorldPosition: Location? = null

    /**
     * @return the _accessLevel of the Player.
     */
    lateinit var accessLevel: AccessLevel
        private set

    var isInRefusalMode: Boolean = false
        set(mode) {
            field = mode
            sendPacket(EtcStatusUpdate(this))
        } // message refusal mode
    var tradeRefusal: Boolean = false // Trade refusal
    var exchangeRefusal: Boolean = false // Exchange refusal

    /**
     * Return the _party object of the Player.
     */
    /**
     * Set the _party object of the Player (without joining it).
     * @param party The object.
     */
    override var party: Party? = null
    var lootRule: LootRule? = null

    /**
     * @return the Player requester of a transaction (ex : FriendInvite, JoinAlly, JoinParty...).
     */
    /**
     * Set the Player requester of a transaction (ex : FriendInvite, JoinAlly, JoinParty...).
     * @param requester
     */
    var activeRequester: Player? = null
        get() {
            if (field != null && field!!.isRequestExpired && activeTradeList == null)
                activeRequester = null

            return field
        }
    private var _requestExpireTime: Long = 0
    /**
     * @return the current [Request].
     */
    val request = Request(this)

    private var _protectTask: ScheduledFuture<*>? = null

    private var _recentFakeDeathEndTime: Long = 0
    var isFakeDeath: Boolean = false

    var expertiseArmorPenalty: Int = 0
        private set
    var expertiseWeaponPenalty: Boolean = false
        private set

    var activeEnchantItem: ItemInstance? = null

    /**
     * @return True if the Inventory is disabled.
     */
    var isInventoryDisabled: Boolean = false
        protected set

    protected var _cubics: MutableMap<Int, Cubic> = ConcurrentSkipListMap()

    protected var _activeSoulShots: MutableSet<Int> = ConcurrentHashMap.newKeySet(1)

    private val _loto = IntArray(5)
    private val _race = IntArray(2)

    val blockList = BlockList(this)

    var team: Int = 0

    /**
     * [-5,-1] varka, 0 neutral, [1,5] ketra
     * @return the side faction.
     */
    var allianceWithVarkaKetra: Int =
        0 // lvl of alliance with ketra orcs or varka silenos, used in quests and aggro checks [-5,-1] varka, 0 neutral, [1,5] ketra

    val fishingStance = FishingStance(this)

    private val _validBypass = ArrayList<String>()
    private val _validBypass2 = ArrayList<String>()

    private var _forumMemo: Forum? = null

    var isInSiege: Boolean = false

    private val _skills = ConcurrentSkipListMap<Int, L2Skill>()

    /**
     * @return the current player skill in use.
     */
    val currentSkill: SkillUseHolder = SkillUseHolder()
    /**
     * @return the current pet skill in use.
     */
    val currentPetSkill: SkillUseHolder = SkillUseHolder()
    /**
     * @return the current queued skill in use.
     */
    val queuedSkill = SkillUseHolder()

    var cursedWeaponEquippedId: Int = 0

    private var _reviveRequested: Int = 0
    private var _revivePower = .0
    var isRevivingPet: Boolean = false
        private set

    private var _cpUpdateIncCheck = .0
    private var _cpUpdateDecCheck = .0
    private var _cpUpdateInterval = .0
    private var _mpUpdateIncCheck = .0
    private var _mpUpdateDecCheck = .0
    private var _mpUpdateInterval = .0

    var clientX: Int = 0
    var clientY: Int = 0
    var clientZ: Int = 0
    var clientHeading: Int = 0

    /**
     * @return the mailPosition.
     */
    /**
     * @param mailPosition The mailPosition to set.
     */
    var mailPosition: Int = 0
    @Volatile
    private var _fallingTimestamp: Long = 0

    private var _shortBuffTask: ScheduledFuture<*>? = null
    var shortBuffTaskSkillId: Int = 0

    var coupleId: Int = 0
    var isUnderMarryRequest: Boolean = false
    private var _requesterId: Int = 0

    private val _friendList = ArrayList<Int>() // Related to CB.
    private val _selectedFriendList = mutableListOf<Int>() // Related to CB.
    private val _selectedBlocksList = ArrayList<Int>() // Related to CB.

    private var _summonTargetRequest: Player? = null
    private var _summonSkillRequest: L2Skill? = null

    private var _requestedGate: Door? = null

    override var stat: CreatureStat
        get() = super.stat as PlayerStat
        set(value: CreatureStat) {
            super.stat = value
        }

    override var status: CreatureStatus
        get() = super.status as PlayerStatus
        set(value: CreatureStatus) {
            super.status = value
        }

    /**
     * @return the base L2PcTemplate link to the Player.
     */
    val baseTemplate: PlayerTemplate
        get() = PlayerData.getTemplate(baseClass)!!

    /** Return the L2PcTemplate link to the Player.  */
    override var template: CreatureTemplate
        get() = super.template as PlayerTemplate
        set(value: CreatureTemplate) {
            super.template = value
        }

    /**
     * Return the AI of the Player (create it if necessary).
     */
    override var ai: CreatureAI = PlayerAI(this)
        get() = _ai ?: synchronized(this) {
            if (_ai == null)
                _ai = PlayerAI(this)

            return _ai!!
        }

    /** Return the Level of the Player.  */
    override val level: Int
        get() = stat.level.toInt()

    /**
     * A newbie is a player reaching level 6. He isn't considered newbie at lvl 25.<br></br>
     * Since IL newbie isn't anymore the first character of an account reaching that state, but any.
     * @return True if newbie.
     */
    val isNewbie: Boolean
        get() = classId.level() <= 1 && level >= 6 && level <= 25

    val isInStoreMode: Boolean
        get() = storeType != StoreType.NONE

    /**
     * @return a table containing all Common RecipeList of the Player.
     */
    val commonRecipeBook: Collection<Recipe>
        get() = _commonRecipeBook.values

    /**
     * @return a table containing all Dwarf RecipeList of the Player.
     */
    val dwarvenRecipeBook: Collection<Recipe>
        get() = _dwarvenRecipeBook.values

    /**
     * @return A list of QuestStates which registered for notify of death of this Player.
     */
    val notifyQuestOfDeath: List<QuestState>
        get() = _notifyQuestOfDeathList

    /**
     * @return A table containing all L2ShortCut of the Player.
     */
    val allShortCuts: Array<L2ShortCut>
        get() = _shortCuts.allShortCuts

    /**
     * @return The current weight of the Player.
     */
    val currentLoad: Int
        get() = inventory!!.totalWeight

    /**
     * @return The number of recommandation obtained by the Player.
     */
    /**
     * Set the number of recommandations obtained by the Player (Max : 255).
     * @param value Number of recommandations obtained.
     */
    var recomHave: Int
        get() = _recomHave
        set(value) {
            _recomHave = MathUtil.limit(value, 0, 255)
        }

    /**
     * @return The number of recommandation that the Player can give.
     */
    /**
     * Set the number of givable recommandations by the [Player] (Max : 9).
     * @param value : The number of recommendations a player can give.
     */
    var recomLeft: Int
        get() = _recomLeft
        set(value) {
            _recomLeft = MathUtil.limit(value, 0, 9)
        }

    val recomChars: MutableList<Int>
        get() = _recomChars

    /**
     * Weight Limit = (CON Modifier*69000)*Skills
     * @return The max weight that the Player can load.
     */
    val maxLoad: Int
        get() {
            if (con < 1)
                return 31000

            if (con > 59)
                return 176000

            val baseLoad = Math.pow(1.029993928, con.toDouble()) * 30495.627366
            return calcStat(Stats.MAX_LOAD, baseLoad * Config.ALT_WEIGHT_LIMIT, this, null).toInt()
        }

    /**
     * @return The ClassId object of the Player contained in L2PcTemplate.
     */
    val classId: ClassId
        get() = (template as PlayerTemplate).classId

    /**
     * @return the Experience of the Player.
     */
    val exp: Long
        get() = stat.exp

    /**
     * @return The Race object of the Player.
     */
    val race: ClassRace
        get() = if (isSubClassActive) baseTemplate.race!! else (template as PlayerTemplate).race!!

    /**
     * @return the SP amount of the Player.
     */
    val sp: Int
        get() = stat.sp

    /**
     * @return The Clan Crest Identifier of the Player or 0.
     */
    val clanCrestId: Int
        get() = if (clan != null) clan!!.crestId else 0

    /**
     * @return The Clan CrestLarge Identifier or 0
     */
    val clanCrestLargeId: Int
        get() = if (clan != null) clan!!.crestLargeId else 0

    /**
     * @return The PcWarehouse object of the Player.
     */
    val warehouse: PcWarehouse
        get() {
            if (_warehouse == null) {
                _warehouse = PcWarehouse(this)
                _warehouse!!.restore()
            }
            return _warehouse!!
        }

    /**
     * @return The PcFreight object of the Player.
     */
    val freight: PcFreight
        get() {
            if (_freight == null) {
                _freight = PcFreight(this)
                _freight!!.restore()
            }
            return _freight!!
        }

    /**
     * @return The Adena amount of the Player.
     */
    val adena: Int
        get() = inventory!!.adena

    /**
     * @return The Ancient Adena amount of the Player.
     */
    val ancientAdena: Int
        get() = inventory!!.ancientAdena

    val isSpawnProtected: Boolean
        get() = _protectTask != null

    val isRecentFakeDeath: Boolean
        get() = _recentFakeDeathEndTime > System.currentTimeMillis()


    val accountName: String
        get() = client!!.accountName!!

    val accountChars: MutableMap<Int, String>
        get() = _chars

    /**
     * @return the Alliance Identifier of the Player.
     */
    val allyId: Int
        get() = if (clan == null) 0 else clan!!.allyId

    val allyCrestId: Int
        get() {
            if (clanId == 0)
                return 0

            return if (clan!!.allyId == 0) 0 else clan!!.allyCrestId

        }

    // Check if the new target is visible.
    // Can't target and attack festival monsters if not participant
    // Can't target and attack rift invaders if not in the same room
    // Get the current target
    // no target change
    // Remove the Player from the _statusListener of the old target if it was a Creature
    // Verify if it's a static object.
    // Add the Player to the _statusListener of the new target if it's a Creature
    // Validate location of the new target.
    // Show the client his new target.
    // Send max/current hp.
    // Target the new WorldObject
    override var target: WorldObject?
        get() = super.target
        set(newTarget) {
            var newTarget = newTarget
            if (newTarget != null && !newTarget.isVisible && !(newTarget is Player && isInParty && party!!.containsPlayer(
                    newTarget
                ))
            )
                newTarget = null
            if (newTarget is FestivalMonster && !isFestivalParticipant)
                newTarget = null
            else if (newTarget != null && isInParty && party!!.isInDimensionalRift && !party!!.dimensionalRift!!.isInCurrentRoomZone(
                    newTarget
                )
            )
                newTarget = null
            val oldTarget = target

            if (oldTarget != null) {
                if (oldTarget == newTarget)
                    return
                if (oldTarget is Creature)
                    oldTarget.removeStatusListener(this)
            }
            if (newTarget is StaticObject) {
                sendPacket(MyTargetSelected(newTarget.objectId, 0))
                sendPacket(StaticObjectInfo((newTarget as StaticObject?)!!))
            } else if (newTarget is Creature) {
                val target = newTarget as Creature?
                if (newTarget.objectId != objectId)
                    sendPacket(ValidateLocation(target!!))
                sendPacket(
                    MyTargetSelected(
                        target!!.objectId,
                        if (target.isAutoAttackable(this) || target is Summon) level - target.level else 0
                    )
                )

                target.addStatusListener(this)
                val su = StatusUpdate(target)
                su.addAttribute(StatusUpdate.MAX_HP, target.maxHp)
                su.addAttribute(StatusUpdate.CUR_HP, target.currentHp.toInt())
                sendPacket(su)

                this.toKnownPlayers(TargetSelected(objectId, newTarget.objectId, x, y, z))
            }

            if (newTarget is Folk)
                currentFolk = newTarget
            else if (newTarget == null) {
                sendPacket(ActionFailed.STATIC_PACKET)

                if (target != null) {
                    broadcastPacket(TargetUnselected(this))
                    currentFolk = null
                }
            }
            super.target = newTarget
        }

    /**
     * Return the active weapon instance (always equipped in the right hand).
     */
    override val activeWeaponInstance: ItemInstance?
        get() = inventory!!.getPaperdollItem(Inventory.PAPERDOLL_RHAND)

    /**
     * Return the active weapon item (always equipped in the right hand).
     */
    override val activeWeaponItem: Weapon?
        get() {
            val weapon = inventory!!.getPaperdollItem(Inventory.PAPERDOLL_RHAND)
            return if (weapon == null) (template as PlayerTemplate).fists else weapon.item as Weapon
        }

    /**
     * @return the type of attack, depending of the worn weapon.
     */
    override val attackType: WeaponType?
        get() = activeWeaponItem?.itemType

    /**
     * Return the secondary weapon instance (always equipped in the left hand).
     */
    override val secondaryWeaponInstance: ItemInstance?
        get() = inventory!!.getPaperdollItem(Inventory.PAPERDOLL_LHAND)

    /**
     * Return the secondary L2Item item (always equiped in the left hand).
     */
    override val secondaryWeaponItem: Item?
        get() {
            val item = inventory!!.getPaperdollItem(Inventory.PAPERDOLL_LHAND)
            return item?.item

        }

    val isInPartyMatchRoom: Boolean
        get() = partyRoom > 0

    /**
     * @return True if a request is in progress.
     */
    val isProcessingRequest: Boolean
        get() = activeRequester != null || _requestExpireTime > System.currentTimeMillis()

    /**
     * @return True if a transaction <B>(trade OR request)</B> is in progress.
     */
    val isProcessingTransaction: Boolean
        get() = activeRequester != null || activeTradeList != null || _requestExpireTime > System.currentTimeMillis()

    /**
     * @return true if last request is expired.
     */
    val isRequestExpired: Boolean
        get() = _requestExpireTime <= System.currentTimeMillis()

    /**
     * @return The _sellList object of the Player.
     */
    val sellList: TradeList
        get() {
            if (_sellList == null)
                _sellList = TradeList(this)

            return _sellList!!
        }

    /**
     * @return the _buyList object of the Player.
     */
    val buyList: TradeList
        get() {
            if (_buyList == null)
                _buyList = TradeList(this)

            return _buyList!!
        }

    /**
     * @return a [List] of all available autoGet [GeneralSkillNode]s **of maximal level** for this [Player].
     */
    val availableAutoGetSkills: List<GeneralSkillNode>
        get() {
            val result = ArrayList<GeneralSkillNode>()

            (template as PlayerTemplate).skills.filter{s -> s.minLvl <= level && s.cost == 0}
                .associateBy {it.id to Collectors.maxBy(COMPARE_SKILLS_BY_LVL) }
                .forEach { i, s ->
                    if (getSkillLevel(i.first) < s.value)
                        result.add(s)
                }
            return result
        }

    /**
     * @return a [List] of available [GeneralSkillNode]s (only general) for this [Player].
     */
    val availableSkills: List<GeneralSkillNode>
        get() {
            val result = ArrayList<GeneralSkillNode>()

            (template as PlayerTemplate).skills.stream().filter { s -> s.minLvl <= level && s.cost != 0 }.forEach { s ->
                if (getSkillLevel(s.id) == s.value - 1)
                    result.add(s)
            }
            return result
        }

    /**
     * @return a [List] of all available [GeneralSkillNode]s (being general or autoGet) **of maximal level** for this [Player].
     */
    val allAvailableSkills: List<GeneralSkillNode>
        get() {
            val result = ArrayList<GeneralSkillNode>()

            (template as PlayerTemplate).skills.filter { s -> s.minLvl <= level }
                .associateBy {it.id to Collectors.maxBy(COMPARE_SKILLS_BY_LVL) }
                .forEach { i, s ->
                    if (getSkillLevel(i.first) < s.value)
                        result.add(s)
                }

            return result
        }

    /**
     * Retrieve next lowest level skill to learn, based on current player level and skill sp cost.
     * @return the required level for next [GeneralSkillNode] to learn for this [Player].
     */
    val requiredLevelForNextSkill: Int
        get() = (template as PlayerTemplate).skills.stream().filter { s -> s.minLvl > level && s.cost != 0 }.min(COMPARE_SKILLS_BY_MIN_LVL).map { s -> s.minLvl }.orElse(
            0
        )

    /**
     * @return True if the Player is the leader of its clan.
     */
    val isClanLeader: Boolean
        get() = clan != null && objectId == clan!!.leaderId

    protected val feedConsume: Int
        get() = if (isAttackingNow) petDataEntry!!.mountMealInBattle else petDataEntry!!.mountMealInNormal

    /**
     * Return True if the Player is invulnerable.
     */
    override var isInvul: Boolean
        get() = super.isInvul || isSpawnProtected
        set(value: Boolean) {
            super.isInvul = value
        }

    /**
     * Return True if the Player has a Party in progress.
     */
    override val isInParty: Boolean
        get() = party != null

    /**
     * Return True if the Player is a GM.
     */
    override val isGM: Boolean
        get() = accessLevel!!.isGm

    val memo: Forum?
        get() {
            if (_forumMemo == null) {
                val forum = ForumsBBSManager.getForumByName("MemoRoot")
                if (forum != null) {
                    _forumMemo = forum.getChildByName(_accountName)
                    if (_forumMemo == null)
                        _forumMemo =
                            ForumsBBSManager.createNewForum(_accountName, forum, Forum.MEMO, Forum.OWNERONLY, objectId)
                }
            }
            return _forumMemo
        }

    /**
     * @return an int interpretation of online status.
     */
    val isOnlineInt: Int
        get() = if (isOnline && client != null) if (client!!.isDetached) 2 else 1 else 0

    /**
     * @return the number of [Henna] empty slots of this [Player].
     */
    val hennaEmptySlots: Int
        get() {
            var totalSlots = 0
            if (classId.level() == 1)
                totalSlots = 2
            else
                totalSlots = 3

            for (i in 0..2) {
                if (_henna[i] != null)
                    totalSlots--
            }

            return if (totalSlots <= 0) 0 else totalSlots

        }

    /**
     * @return True if the Player is a Mage (based on class templates).
     */
    val isMageClass: Boolean
        get() = classId.type !== ClassType.FIGHTER

    val isMounted: Boolean
        get() = mountType > 0

    override val isSeated: Boolean
        get() = _throneId > 0

    override val isRiding: Boolean
        get() = mountType == 1

    override val isFlying: Boolean
        get() = mountType == 2

    val cubics: MutableMap<Int, Cubic>
        get() = _cubics

    /**
     * @return the modifier corresponding to the Enchant Effect of the Active Weapon (Min : 127).
     */
    val enchantEffect: Int
        get() {
            val wpn = activeWeaponInstance
            return if (wpn == null) 0 else Math.min(127, wpn.enchantLevel)
        }

    /**
     * @return True if Player is a participant in the Festival of Darkness.
     */
    val isFestivalParticipant: Boolean
        get() = SevenSignsFestival.isParticipant(this)

    val autoSoulShot: Set<Int>
        get() = _activeSoulShots

    val isInObserverMode: Boolean
        get() = !isInOlympiadMode && savedLocation != Location.DUMMY_LOC

    val isInDuel: Boolean
        get() = duelId > 0

    /**
     * This returns a SystemMessage stating why the player is not available for duelling.
     * @return S1_CANNOT_DUEL... message
     */
    // Prepare the message with the good reason.
    // Reinitialize the reason.
    // Send stored reason.
    val noDuelReason: SystemMessage
        get() {
            val sm = SystemMessage.getSystemMessage(_noDuelReason).addCharName(this)
            _noDuelReason = SystemMessageId.THERE_IS_NO_OPPONENT_TO_RECEIVE_YOUR_CHALLENGE_FOR_A_DUEL
            return sm
        }

    val isAcademyMember: Boolean
        get() = lvlJoinedAcademy > 0

    val isFishing: Boolean
        get() = fishingStance.isUnderFishCombat || fishingStance.isLookingForFish

    val isAlliedWithVarka: Boolean
        get() = allianceWithVarkaKetra < 0

    val isAlliedWithKetra: Boolean
        get() = allianceWithVarkaKetra > 0

    val isSubClassActive: Boolean
        get() = classIndex > 0

    val isLocked: Boolean
        get() = _subclassLock.isLocked

    val isReviveRequested: Boolean
        get() = _reviveRequested == 1

    /**
     * @return Returns the inBoat.
     */
    val isInBoat: Boolean
        get() = boat != null

    val inventoryLimit: Int
        get() = (if (race === ClassRace.DWARF) Config.INVENTORY_MAXIMUM_DWARF else Config.INVENTORY_MAXIMUM_NO_DWARF) + stat.calcStat(
            Stats.INV_LIM,
            0.0,
            null,
            null
        ).toInt()

    val wareHouseLimit: Int
        get() = (if (race === ClassRace.DWARF) Config.WAREHOUSE_SLOTS_DWARF else Config.WAREHOUSE_SLOTS_NO_DWARF) + stat.calcStat(
            Stats.WH_LIM,
            0.0,
            null,
            null
        ).toInt()

    val privateSellStoreLimit: Int
        get() = (if (race === ClassRace.DWARF) Config.MAX_PVTSTORE_SLOTS_DWARF else Config.MAX_PVTSTORE_SLOTS_OTHER) + stat.calcStat(
            Stats.P_SELL_LIM,
            0.0,
            null,
            null
        ).toInt()

    val privateBuyStoreLimit: Int
        get() = (if (race === ClassRace.DWARF) Config.MAX_PVTSTORE_SLOTS_DWARF else Config.MAX_PVTSTORE_SLOTS_OTHER) + stat.calcStat(
            Stats.P_BUY_LIM,
            0.0,
            null,
            null
        ).toInt()

    val freightLimit: Int
        get() = Config.FREIGHT_SLOTS + stat.calcStat(Stats.FREIGHT_LIM, 0.0, null, null).toInt()

    val dwarfRecipeLimit: Int
        get() = Config.DWARF_RECIPE_LIMIT + stat.calcStat(Stats.REC_D_LIM, 0.0, null, null).toInt()

    val commonRecipeLimit: Int
        get() = Config.COMMON_RECIPE_LIMIT + stat.calcStat(Stats.REC_C_LIM, 0.0, null, null).toInt()

    /**
     * This method is overidden on Player, L2Summon and L2Npc.
     * @return the skills list of this Creature.
     */
    override val skills: MutableMap<Int, L2Skill>
        get() = _skills

    /**
     * @return True if player is jailed
     */
    val isInJail: Boolean
        get() = punishLevel == PunishLevel.JAIL

    /**
     * @return True if player is chat banned
     */
    val isChatBanned: Boolean
        get() = punishLevel == PunishLevel.CHAT

    val isCursedWeaponEquipped: Boolean
        get() = cursedWeaponEquippedId != 0

    private val _reuseTimeStamps = ConcurrentHashMap<Int, Timestamp>()

    val reuseTimeStamps: Collection<Timestamp>
        get() = _reuseTimeStamps.values

    val reuseTimeStamp: MutableMap<Int, Timestamp>
        get() = _reuseTimeStamps

    override val actingPlayer: Player?
        get() = this

    /**
     * @return the number of charges this Player got.
     */
    val charges: Int
        get() = _charges.get()

    val isAllowedToEnchantSkills: Boolean
        get() {
            if (isLocked)
                return false

            if (AttackStanceTaskManager.isInAttackStance(this))
                return false

            if (isCastingNow || isCastingSimultaneouslyNow)
                return false

            return if (isInBoat) false else true

        }

    val friendList: MutableList<Int>
        get() = _friendList

    val selectedFriendList: MutableList<Int>
        get() = _selectedFriendList

    val selectedBlocksList: MutableList<Int>
        get() = _selectedBlocksList

    override val collisionRadius: Double
        get() = baseTemplate.getCollisionRadiusBySex(appearance.sex)

    override val collisionHeight: Double
        get() = baseTemplate.getCollisionHeightBySex(appearance.sex)

    enum class StoreType private constructor(val id: Int) {
        NONE(0),
        SELL(1),
        SELL_MANAGE(2),
        BUY(3),
        BUY_MANAGE(4),
        MANUFACTURE(5),
        PACKAGE_SELL(8)
    }

    enum class PunishLevel private constructor(private val punValue: Int, private val punString: String) {
        NONE(0, ""),
        CHAT(1, "chat banned"),
        JAIL(2, "jailed"),
        CHAR(3, "banned"),
        ACC(4, "banned");

        fun value(): Int {
            return punValue
        }

        fun string(): String {
            return punString
        }
    }

    /**
     * Constructor of Player (use Creature constructor).
     *
     *  * Call the Creature constructor to create an empty _skills slot and copy basic Calculator set to this Player
     *  * Set the name of the Player
     *
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method SET the level of the Player to 1</B></FONT>
     * @param objectId Identifier of the object to initialized
     * @param template The L2PcTemplate to apply to the Player
     * @param accountName The name of the account including this Player
     * @param app The PcAppearance of the Player
     */
    private constructor(objectId: Int, template: PlayerTemplate, accountName: String, app: PcAppearance) : super(
        objectId,
        template
    ) {

        initCharStatusUpdateValues()

        _accountName = accountName
        appearance = app

        // Create an AI
        _ai = PlayerAI(this)

        // Create a L2Radar object
        radar = L2Radar(this)

        // Retrieve from the database all items of this Player and add them to _inventory
        inventory!!.restore()
        warehouse
        freight
    }

    private constructor(objectId: Int) : super(objectId, null!!) {

        initCharStatusUpdateValues()
    }

    override fun addFuncsToNewCharacter() {
        // Add Creature functionalities.
        super.addFuncsToNewCharacter()

        addStatFunc(FuncMaxCpMul)

        addStatFunc(FuncHennaSTR)
        addStatFunc(FuncHennaDEX)
        addStatFunc(FuncHennaINT)
        addStatFunc(FuncHennaMEN)
        addStatFunc(FuncHennaCON)
        addStatFunc(FuncHennaWIT)
    }

    override fun initCharStatusUpdateValues() {
        super.initCharStatusUpdateValues()

        _cpUpdateInterval = maxCp / 352.0
        _cpUpdateIncCheck = maxCp.toDouble()
        _cpUpdateDecCheck = maxCp - _cpUpdateInterval
        _mpUpdateInterval = maxMp / 352.0
        _mpUpdateIncCheck = maxMp.toDouble()
        _mpUpdateDecCheck = maxMp - _mpUpdateInterval
    }

    override fun initCharStat() {
        stat = PlayerStat(this)
    }

    override fun initCharStatus() {
        status = PlayerStatus(this)
    }

    fun setTemplate(newclass: ClassId) {
        super.template = PlayerData.getTemplate(newclass)!!
    }

    fun setBaseClass(classId: ClassId) {
        baseClass = classId.ordinal
    }

    /**
     * Add a new L2RecipList to the table _commonrecipebook containing all RecipeList of the Player.
     * @param recipe The RecipeList to add to the _recipebook
     */
    fun registerCommonRecipeList(recipe: Recipe) {
        _commonRecipeBook[recipe.id] = recipe
    }

    /**
     * Add a new L2RecipList to the table _recipebook containing all RecipeList of the Player.
     * @param recipe The RecipeList to add to the _recipebook
     */
    fun registerDwarvenRecipeList(recipe: Recipe) {
        _dwarvenRecipeBook[recipe.id] = recipe
    }

    /**
     * @param recipeId The Identifier of the RecipeList to check in the player's recipe books
     * @return **TRUE** if player has the recipe on Common or Dwarven Recipe book else returns **FALSE**
     */
    fun hasRecipeList(recipeId: Int): Boolean {
        return _dwarvenRecipeBook.containsKey(recipeId) || _commonRecipeBook.containsKey(recipeId)
    }

    /**
     * Tries to remove a [Recipe] from this [Player]. Delete the associated [L2ShortCut], if existing.
     * @param recipeId : The id of the Recipe to remove.
     */
    fun unregisterRecipeList(recipeId: Int) {
        if (_dwarvenRecipeBook.containsKey(recipeId))
            _dwarvenRecipeBook.remove(recipeId)
        else if (_commonRecipeBook.containsKey(recipeId))
            _commonRecipeBook.remove(recipeId)

        for ((slot, page, type, id) in allShortCuts) {
            if (id == recipeId && type == L2ShortCut.TYPE_RECIPE)
                deleteShortCut(slot, page)
        }
    }

    /**
     * @param name The name of the quest.
     * @return The QuestState object corresponding to the quest name.
     */
    fun getQuestState(name: String): QuestState? {
        for (qs in _quests) {
            if (name == qs.quest.name)
                return qs
        }
        return null
    }

    /**
     * Add a QuestState to the table _quest containing all quests began by the Player.
     * @param qs The QuestState to add to _quest.
     */
    fun setQuestState(qs: QuestState) {
        _quests.add(qs)
    }

    /**
     * Remove a QuestState from the table _quest containing all quests began by the Player.
     * @param qs : The QuestState to be removed from _quest.
     */
    fun delQuestState(qs: QuestState) {
        _quests.remove(qs)
    }

    /**
     * @param completed : If true, include completed quests to the list.
     * @return list of started and eventually completed quests of the player.
     */
    fun getAllQuests(completed: Boolean): List<Quest> {
        val quests = ArrayList<Quest>()

        for (qs in _quests) {
            if (completed && qs.isCreated || !completed && !qs.isStarted)
                continue

            val quest = qs.quest
            if (!quest.isRealQuest)
                continue

            quests.add(quest)
        }

        return quests
    }

    fun processQuestEvent(questName: String, event: String) {
        val quest = ScriptData.getQuest(questName) ?: return

        val qs = getQuestState(questName) ?: return

        val `object` = World.getObject(lastQuestNpcObject)
        if (`object` !is Npc || !isInsideRadius(`object`, Npc.INTERACTION_DISTANCE, false, false))
            return

        val npc = `object` as Npc?

        val scripts = (npc!!.template as NpcTemplate).getEventQuests(EventType.ON_TALK)
        for (script in scripts) {
            if (script != quest)
                continue

            quest.notifyEvent(event, npc, this)
            break
        }
    }

    /**
     * Add QuestState instance that is to be notified of Player's death.
     * @param qs The QuestState that subscribe to this event
     */
    fun addNotifyQuestOfDeath(qs: QuestState?) {
        if (qs == null)
            return

        if (!_notifyQuestOfDeathList.contains(qs))
            _notifyQuestOfDeathList.add(qs)
    }

    /**
     * Remove QuestState instance that is to be notified of Player's death.
     * @param qs The QuestState that subscribe to this event
     */
    fun removeNotifyQuestOfDeath(qs: QuestState?) {
        if (qs == null)
            return

        _notifyQuestOfDeathList.remove(qs)
    }

    /**
     * @param slot The slot in wich the shortCuts is equipped
     * @param page The page of shortCuts containing the slot
     * @return The L2ShortCut of the Player corresponding to the position (page-slot).
     */
    fun getShortCut(slot: Int, page: Int): L2ShortCut? {
        return _shortCuts.getShortCut(slot, page)
    }

    /**
     * Add a L2shortCut to the Player _shortCuts
     * @param shortcut The shortcut to add.
     */
    fun registerShortCut(shortcut: L2ShortCut) {
        _shortCuts.registerShortCut(shortcut)
    }

    /**
     * Delete the L2ShortCut corresponding to the position (page-slot) from the Player _shortCuts.
     * @param slot
     * @param page
     */
    fun deleteShortCut(slot: Int, page: Int) {
        _shortCuts.deleteShortCut(slot, page)
    }

    /**
     * Add a L2Macro to the Player _macroses.
     * @param macro The Macro object to add.
     */
    fun registerMacro(macro: L2Macro) {
        macroses.registerMacro(macro)
    }

    /**
     * Delete the L2Macro corresponding to the Identifier from the Player _macroses.
     * @param id
     */
    fun deleteMacro(id: Int) {
        macroses.deleteMacro(id)
    }

    /**
     * Set the PvP Flag of the Player.
     * @param pvpFlag 0 or 1.
     */
    fun setPvpFlag(pvpFlag: Int) {
        this.pvpFlag = pvpFlag.toByte()
    }

    fun updatePvPFlag(value: Int) {
        if (pvpFlag.toInt() == value)
            return

        setPvpFlag(value)
        sendPacket(UserInfo(this))

        if (pet != null)
            sendPacket(RelationChanged(pet!!, getRelation(this), false))

        broadcastRelationsChanges()
    }

    fun getRelation(target: Player): Int {
        var result = 0

        // karma and pvp may not be required
        if (pvpFlag.toInt() != 0)
            result = result or RelationChanged.RELATION_PVP_FLAG
        if (karma > 0)
            result = result or RelationChanged.RELATION_HAS_KARMA

        if (isClanLeader)
            result = result or RelationChanged.RELATION_LEADER

        if (siegeState.toInt() != 0) {
            result = result or RelationChanged.RELATION_INSIEGE
            if (siegeState != target.siegeState)
                result = result or RelationChanged.RELATION_ENEMY
            else
                result = result or RelationChanged.RELATION_ALLY
            if (siegeState.toInt() == 1)
                result = result or RelationChanged.RELATION_ATTACKER
        }

        if (clan != null && target.clan != null) {
            if (target.pledgeType != Clan.SUBUNIT_ACADEMY && pledgeType != Clan.SUBUNIT_ACADEMY && target.clan!!.isAtWarWith(
                    clan!!.clanId
                )
            ) {
                result = result or RelationChanged.RELATION_1SIDED_WAR
                if (clan!!.isAtWarWith(target.clan!!.clanId))
                    result = result or RelationChanged.RELATION_MUTUAL_WAR
            }
        }
        return result
    }

    override fun revalidateZone(force: Boolean) {
        super.revalidateZone(force)

        if (Config.ALLOW_WATER) {
            if (isInsideZone(ZoneId.WATER))
                WaterTaskManager.add(this)
            else
                WaterTaskManager.remove(this)
        }

        if (isInsideZone(ZoneId.SIEGE)) {
            if (_lastCompassZone == ExSetCompassZoneCode.SIEGEWARZONE2)
                return

            _lastCompassZone = ExSetCompassZoneCode.SIEGEWARZONE2
            sendPacket(ExSetCompassZoneCode(ExSetCompassZoneCode.SIEGEWARZONE2))
        } else if (isInsideZone(ZoneId.PVP)) {
            if (_lastCompassZone == ExSetCompassZoneCode.PVPZONE)
                return

            _lastCompassZone = ExSetCompassZoneCode.PVPZONE
            sendPacket(ExSetCompassZoneCode(ExSetCompassZoneCode.PVPZONE))
        } else if (isIn7sDungeon) {
            if (_lastCompassZone == ExSetCompassZoneCode.SEVENSIGNSZONE)
                return

            _lastCompassZone = ExSetCompassZoneCode.SEVENSIGNSZONE
            sendPacket(ExSetCompassZoneCode(ExSetCompassZoneCode.SEVENSIGNSZONE))
        } else if (isInsideZone(ZoneId.PEACE)) {
            if (_lastCompassZone == ExSetCompassZoneCode.PEACEZONE)
                return

            _lastCompassZone = ExSetCompassZoneCode.PEACEZONE
            sendPacket(ExSetCompassZoneCode(ExSetCompassZoneCode.PEACEZONE))
        } else {
            if (_lastCompassZone == ExSetCompassZoneCode.GENERALZONE)
                return

            if (_lastCompassZone == ExSetCompassZoneCode.SIEGEWARZONE2)
                updatePvPStatus()

            _lastCompassZone = ExSetCompassZoneCode.GENERALZONE
            sendPacket(ExSetCompassZoneCode(ExSetCompassZoneCode.GENERALZONE))
        }
    }

    /**
     * Edit the number of recommandation obtained by the Player (Max : 255).
     * @param value : The value to add or remove.
     */
    fun editRecomHave(value: Int) {
        _recomHave = MathUtil.limit(_recomHave + value, 0, 255)
    }

    /**
     * Increment the number of recommandation that the Player can give.
     */
    protected fun decRecomLeft() {
        if (_recomLeft > 0)
            _recomLeft--
    }

    fun giveRecom(target: Player) {
        target.editRecomHave(1)
        decRecomLeft()

        _recomChars.add(target.objectId)

        try {
            L2DatabaseFactory.connection.use { con ->
                var ps = con.prepareStatement(ADD_CHAR_RECOM)
                ps.setInt(1, objectId)
                ps.setInt(2, target.objectId)
                ps.execute()
                ps.close()

                ps = con.prepareStatement(UPDATE_TARGET_RECOM_HAVE)
                ps.setInt(1, target.recomHave)
                ps.setInt(2, target.objectId)
                ps.execute()
                ps.close()

                ps = con.prepareStatement(UPDATE_CHAR_RECOM_LEFT)
                ps.setInt(1, recomLeft)
                ps.setInt(2, objectId)
                ps.execute()
                ps.close()
            }
        } catch (e: Exception) {
            WorldObject.LOGGER.error("Couldn't update player recommendations.", e)
        }

    }

    fun canRecom(target: Player): Boolean {
        return !_recomChars.contains(target.objectId)
    }

    /**
     * Update the overloaded status of the Player.
     */
    fun refreshOverloaded() {
        val maxLoad = maxLoad
        if (maxLoad > 0) {
            val weightproc = currentLoad * 1000 / maxLoad
            val newWeightPenalty: Int

            if (weightproc < 500)
                newWeightPenalty = 0
            else if (weightproc < 666)
                newWeightPenalty = 1
            else if (weightproc < 800)
                newWeightPenalty = 2
            else if (weightproc < 1000)
                newWeightPenalty = 3
            else
                newWeightPenalty = 4

            if (weightPenalty != newWeightPenalty) {
                weightPenalty = newWeightPenalty

                if (newWeightPenalty > 0) {
                    addSkill(SkillTable.getInfo(4270, newWeightPenalty), false)
                    isOverloaded = currentLoad > maxLoad
                } else {
                    removeSkill(4270, false)
                    isOverloaded = false
                }

                sendPacket(UserInfo(this))
                sendPacket(EtcStatusUpdate(this))
                broadcastCharInfo()
            }
        }
    }

    /**
     * Refresh expertise level ; weapon got one rank, when armor got 4 ranks.<br></br>
     */
    fun refreshExpertisePenalty() {
        val expertiseLevel = getSkillLevel(L2Skill.SKILL_EXPERTISE)

        var armorPenalty = 0
        var weaponPenalty = false

        for (item in inventory!!.paperdollItems) {
            if (item.itemType !== EtcItemType.ARROW && item.item.crystalType!!.id > expertiseLevel) {
                if (item.isWeapon)
                    weaponPenalty = true
                else
                    armorPenalty += if (item.item.bodyPart == Item.SLOT_FULL_ARMOR) 2 else 1
            }
        }

        armorPenalty = Math.min(armorPenalty, 4)

        // Found a different state than previous ; update it.
        if (expertiseWeaponPenalty != weaponPenalty || expertiseArmorPenalty != armorPenalty) {
            expertiseWeaponPenalty = weaponPenalty
            expertiseArmorPenalty = armorPenalty

            // Passive skill "Grade Penalty" is either granted or dropped.
            if (expertiseWeaponPenalty || expertiseArmorPenalty > 0)
                addSkill(SkillTable.getInfo(4267, 1), false)
            else
                removeSkill(4267, false)

            sendSkillList()
            sendPacket(EtcStatusUpdate(this))

            val weapon = inventory.getPaperdollItem(Inventory.PAPERDOLL_RHAND)
            if (weapon != null) {
                if (expertiseWeaponPenalty)
                    ItemPassiveSkillsListener.onUnequip(0, weapon, this)
                else
                    ItemPassiveSkillsListener.onEquip(0, weapon, this)
            }
        }
    }

    /**
     * Equip or unequip the item.
     * <UL>
     * <LI>If item is equipped, shots are applied if automation is on.</LI>
     * <LI>If item is unequipped, shots are discharged.</LI>
    </UL> *
     * @param item The item to charge/discharge.
     * @param abortAttack If true, the current attack will be aborted in order to equip the item.
     */
    fun useEquippableItem(item: ItemInstance, abortAttack: Boolean) {
        var items: Array<ItemInstance>? = null
        val isEquipped = item.isEquipped
        val oldInvLimit = inventoryLimit

        if (item.item is Weapon)
            item.unChargeAllShots()

        if (isEquipped) {
            if (item.enchantLevel > 0)
                sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED).addNumber(item.enchantLevel).addItemName(
                        item
                    )
                )
            else
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED).addItemName(item))

            items = inventory!!.unEquipItemInBodySlotAndRecord(item)
        } else {
            items = inventory!!.equipItemAndRecord(item)

            if (item.isEquipped) {
                if (item.enchantLevel > 0)
                    sendPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.S1_S2_EQUIPPED).addNumber(item.enchantLevel).addItemName(
                            item
                        )
                    )
                else
                    sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_EQUIPPED).addItemName(item))

                if (item.item.bodyPart and Item.SLOT_ALLWEAPON != 0)
                    rechargeShots(true, true)
            } else
                sendPacket(SystemMessageId.CANNOT_EQUIP_ITEM_DUE_TO_BAD_CONDITION)
        }
        refreshExpertisePenalty()
        broadcastUserInfo()

        val iu = InventoryUpdate()
        iu.addItems(Arrays.asList(*items))
        sendPacket(iu)

        if (abortAttack)
            abortAttack()

        if (inventoryLimit != oldInvLimit)
            sendPacket(ExStorageMaxCount(this))
    }

    /**
     * Set the template of the Player.
     * @param Id The Identifier of the L2PcTemplate to set to the Player
     */
    fun setClassId(Id: Int) {
        if (!_subclassLock.tryLock())
            return

        try {
            if (lvlJoinedAcademy != 0 && clan != null && ClassId.VALUES[Id].level() == 2) {
                if (lvlJoinedAcademy <= 16)
                    clan!!.addReputationScore(400)
                else if (lvlJoinedAcademy >= 39)
                    clan!!.addReputationScore(170)
                else
                    clan!!.addReputationScore(400 - (lvlJoinedAcademy - 16) * 10)

                lvlJoinedAcademy = 0

                // Oust pledge member from the academy, because he has finished his 2nd class transfer.
                clan!!.broadcastToOnlineMembers(
                    PledgeShowMemberListDelete(name),
                    SystemMessage.getSystemMessage(SystemMessageId.CLAN_MEMBER_S1_EXPELLED).addString(name)
                )
                clan!!.removeClanMember(objectId, 0)
                sendPacket(SystemMessageId.ACADEMY_MEMBERSHIP_TERMINATED)

                // receive graduation gift : academy circlet
                addItem("Gift", 8181, 1, this, true)
            }

            if (isSubClassActive)
                subClasses[classIndex]!!.classId = Id

            broadcastPacket(MagicSkillUse(this, this, 5103, 1, 1000, 0))
            setClassTemplate(Id)

            if (classId.level() == 3)
                sendPacket(SystemMessageId.THIRD_CLASS_TRANSFER)
            else
                sendPacket(SystemMessageId.CLASS_TRANSFER)

            // Update class icon in party and clan
            if (party != null)
                party!!.broadcastPacket(PartySmallWindowUpdate(this))

            if (clan != null)
                clan!!.broadcastToOnlineMembers(PledgeShowMemberListUpdate(this))

            if (Config.AUTO_LEARN_SKILLS)
                rewardSkills()
        } finally {
            _subclassLock.unlock()
        }
    }

    /**
     * @param castleId The castle to check.
     * @return True if this Player is a clan leader in ownership of the passed castle.
     */
    fun isCastleLord(castleId: Int): Boolean {
        val clan = clan
        if (clan != null && clan.leader!!.playerInstance == this) {
            val castle = CastleManager.getCastleByOwner(clan)
            return castle != null && castle.castleId == castleId
        }
        return false
    }

    fun setOnlineTime(time: Long) {
        _onlineTime = time
        _onlineBeginTime = System.currentTimeMillis()
    }

    /**
     * Delete a ShortCut of the Player _shortCuts.
     * @param objectId The shortcut id.
     */
    fun removeItemFromShortCut(objectId: Int) {
        _shortCuts.deleteShortCutByObjectId(objectId)
    }

    @JvmOverloads
    fun sitDown(checkCast: Boolean = true) {
        if (checkCast && isCastingNow)
            return

        if (!isSitting && !isAttackingDisabled && !isOutOfControl && !isImmobilized) {
            breakAttack()
            isSitting = true
            broadcastPacket(ChangeWaitType(this, ChangeWaitType.WT_SITTING))

            // Schedule a sit down task to wait for the animation to finish
            ai!!.setIntention(CtrlIntention.REST)

            ThreadPool.schedule(Runnable{ isParalyzed = false }, 2500)
            isParalyzed = true
        }
    }

    /**
     * Stand up the Player, set the AI Intention to IDLE and send ChangeWaitType packet (broadcast)
     */
    fun standUp() {
        if (isSitting && !isInStoreMode && !isAlikeDead && !isParalyzed) {
            if (_effects.isAffected(L2EffectFlag.RELAXING))
                stopEffects(L2EffectType.RELAXING)

            broadcastPacket(ChangeWaitType(this, ChangeWaitType.WT_STANDING))

            // Schedule a stand up task to wait for the animation to finish
            ThreadPool.schedule(Runnable{
                isSitting = false
                isParalyzed = false
                ai!!.setIntention(CtrlIntention.IDLE)
            }, 2500)
            isParalyzed = true
        }
    }

    /**
     * Stands up and close any opened shop window, if any.
     */
    fun forceStandUp() {
        // Cancels any shop types.
        if (isInStoreMode) {
            storeType = StoreType.NONE
            broadcastUserInfo()
        }

        // Stand up.
        standUp()
    }

    /**
     * Used to sit or stand. If not possible, queue the action.
     * @param target The target, used for thrones types.
     * @param sittingState The sitting state, inheritated from packet or player status.
     */
    fun tryToSitOrStand(target: WorldObject?, sittingState: Boolean) {
        if (isFakeDeath) {
            stopFakeDeath(true)
            return
        }

        val isThrone = target is StaticObject && target.type == 1

        // Player wants to sit on a throne but is out of radius, move to the throne delaying the sit action.
        if (isThrone && !sittingState && !isInsideRadius(target!!, Npc.INTERACTION_DISTANCE, false, false)) {
            ai!!.setIntention(CtrlIntention.MOVE_TO, Location(target.x, target.y, target.z))

            val nextAction = NextAction(CtrlEvent.EVT_ARRIVED, CtrlIntention.MOVE_TO, Runnable{
                if (mountType != 0)
                    return@Runnable

                sitDown()

                if (!(target as StaticObject).isBusy) {
                    _throneId = target.objectId

                    target.isBusy = true
                    broadcastPacket(ChairSit(objectId, target.staticObjectId))
                }
            })

            // Binding next action to AI.
            ai!!.setNextAction(nextAction)
            return
        }

        // Player isn't moving, sit directly.
        if (!isMoving) {
            if (mountType != 0)
                return

            if (sittingState) {
                if (_throneId != 0) {
                    val `object` = World.getObject(_throneId)
                    if (`object` is StaticObject)
                        `object`.isBusy = false

                    _throneId = 0
                }

                standUp()
            } else {
                sitDown()

                if (isThrone && !(target as StaticObject).isBusy && isInsideRadius(
                        target,
                        Npc.INTERACTION_DISTANCE,
                        false,
                        false
                    )
                ) {
                    _throneId = target.objectId

                    target.isBusy = true
                    broadcastPacket(ChairSit(objectId, target.staticObjectId))
                }
            }
        } else {
            val nextAction = NextAction(CtrlEvent.EVT_ARRIVED, CtrlIntention.MOVE_TO, Runnable{
                if (mountType != 0)
                    return@Runnable

                if (sittingState) {
                    if (_throneId != 0) {
                        val `object` = World.getObject(_throneId)
                        if (`object` is StaticObject)
                            `object`.isBusy = false

                        _throneId = 0
                    }

                    standUp()
                } else {
                    sitDown()

                    if (isThrone && !(target as StaticObject).isBusy && isInsideRadius(
                            target,
                            Npc.INTERACTION_DISTANCE,
                            false,
                            false
                        )
                    ) {
                        _throneId = target.objectId

                        target.isBusy = true
                        broadcastPacket(ChairSit(objectId, target.staticObjectId))
                    }
                }
            })

            // Binding next action to AI.
            ai!!.setNextAction(nextAction)
        }// Player is moving, wait the current action is done, then sit.
    }

    /**
     * Free memory used by Warehouse
     */
    fun clearWarehouse() {
        if (_warehouse != null)
            _warehouse!!.deleteMe()

        _warehouse = null
    }

    /**
     * Free memory used by Freight
     */
    fun clearFreight() {
        if (_freight != null)
            _freight!!.deleteMe()

        _freight = null
    }

    /**
     * @param objectId The id of the owner.
     * @return deposited PcFreight object for the objectId or create new if not existing.
     */
    fun getDepositedFreight(objectId: Int): PcFreight {
        for (freight in _depositedFreight) {
            if (freight != null && freight.ownerId == objectId)
                return freight
        }

        val freight = PcFreight(null)
        freight.doQuickRestore(objectId)
        _depositedFreight.add(freight)
        return freight
    }

    /**
     * Clear memory used by deposited freight
     */
    fun clearDepositedFreight() {
        for (freight in _depositedFreight) {
            freight?.deleteMe()
        }
        _depositedFreight.clear()
    }

    /**
     * Add adena to Inventory of the Player and send InventoryUpdate packet to the Player.
     * @param process String Identifier of process triggering this action
     * @param count int Quantity of adena to be added
     * @param reference WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @param sendMessage boolean Specifies whether to send message to Client about this action
     */
    fun addAdena(process: String, count: Int, reference: WorldObject?, sendMessage: Boolean) {
        if (sendMessage)
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1_ADENA).addNumber(count))

        if (count > 0) {
            inventory!!.addAdena(process, count, this, reference)

            val iu = InventoryUpdate()
            iu.addItem(inventory.adenaInstance)
            sendPacket(iu)
        }
    }

    /**
     * Reduce adena in Inventory of the Player and send InventoryUpdate packet to the Player.
     * @param process String Identifier of process triggering this action
     * @param count int Quantity of adena to be reduced
     * @param reference WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @param sendMessage boolean Specifies whether to send message to Client about this action
     * @return boolean informing if the action was successfull
     */
    fun reduceAdena(process: String, count: Int, reference: WorldObject?, sendMessage: Boolean): Boolean {
        if (count > adena) {
            if (sendMessage)
                sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA)

            return false
        }

        if (count > 0) {
            val adenaItem = inventory!!.adenaInstance
            if (!inventory.reduceAdena(process, count, this, reference))
                return false

            // Send update packet
            val iu = InventoryUpdate()
            iu.addItem(adenaItem)
            sendPacket(iu)

            if (sendMessage)
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED_ADENA).addNumber(count))
        }
        return true
    }

    /**
     * Add ancient adena to Inventory of the Player and send InventoryUpdate packet to the Player.
     * @param process String Identifier of process triggering this action
     * @param count int Quantity of ancient adena to be added
     * @param reference WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @param sendMessage boolean Specifies whether to send message to Client about this action
     */
    fun addAncientAdena(process: String, count: Int, reference: WorldObject, sendMessage: Boolean) {
        if (sendMessage)
            sendPacket(
                SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(PcInventory.ANCIENT_ADENA_ID).addNumber(
                    count
                )
            )

        if (count > 0) {
            inventory!!.addAncientAdena(process, count, this, reference)

            val iu = InventoryUpdate()
            iu.addItem(inventory.ancientAdenaInstance)
            sendPacket(iu)
        }
    }

    /**
     * Reduce ancient adena in Inventory of the Player and send InventoryUpdate packet to the Player.
     * @param process String Identifier of process triggering this action
     * @param count int Quantity of ancient adena to be reduced
     * @param reference WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @param sendMessage boolean Specifies whether to send message to Client about this action
     * @return boolean informing if the action was successfull
     */
    fun reduceAncientAdena(process: String, count: Int, reference: WorldObject, sendMessage: Boolean): Boolean {
        if (count > ancientAdena) {
            if (sendMessage)
                sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA)

            return false
        }

        if (count > 0) {
            val ancientAdenaItem = inventory!!.ancientAdenaInstance
            if (!inventory.reduceAncientAdena(process, count, this, reference))
                return false

            val iu = InventoryUpdate()
            iu.addItem(ancientAdenaItem)
            sendPacket(iu)

            if (sendMessage) {
                if (count > 1)
                    sendPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(PcInventory.ANCIENT_ADENA_ID).addItemNumber(
                            count
                        )
                    )
                else
                    sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED).addItemName(PcInventory.ANCIENT_ADENA_ID))
            }
        }
        return true
    }

    /**
     * Adds item to inventory and send InventoryUpdate packet to the Player.
     * @param process String Identifier of process triggering this action
     * @param item ItemInstance to be added
     * @param reference WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @param sendMessage boolean Specifies whether to send message to Client about this action
     */
    fun addItem(process: String, item: ItemInstance, reference: WorldObject?, sendMessage: Boolean) {
        if (item.count > 0) {
            // Sends message to client if requested
            if (sendMessage) {
                if (item.count > 1)
                    sendPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S2_S1).addItemName(item).addNumber(
                            item.count
                        )
                    )
                else if (item.enchantLevel > 0)
                    sendPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_A_S1_S2).addNumber(item.enchantLevel).addItemName(
                            item
                        )
                    )
                else
                    sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1).addItemName(item))
            }

            // Add the item to inventory
            val newitem = inventory!!.addItem(process, item, this, reference)

            // Send inventory update packet
            val playerIU = InventoryUpdate()
            playerIU.addItem(newitem)
            sendPacket(playerIU)

            // Update current load as well
            val su = StatusUpdate(this)
            su.addAttribute(StatusUpdate.CUR_LOAD, currentLoad)
            sendPacket(su)

            // Cursed Weapon
            if (CursedWeaponManager.isCursed(newitem!!.itemId))
                CursedWeaponManager.activate(this, newitem)
            else if (item.item.itemType === EtcItemType.ARROW && attackType === WeaponType.BOW && inventory.getPaperdollItem(
                    Inventory.PAPERDOLL_LHAND
                ) == null
            )
                checkAndEquipArrows()// If you pickup arrows and a bow is equipped, try to equip them if no arrows is currently equipped.
        }
    }

    /**
     * Adds item to Inventory and send InventoryUpdate packet to the Player.
     * @param process String Identifier of process triggering this action
     * @param itemId int Item Identifier of the item to be added
     * @param count int Quantity of items to be added
     * @param reference WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @param sendMessage boolean Specifies whether to send message to Client about this action
     * @return The created ItemInstance.
     */
    fun addItem(process: String, itemId: Int, count: Int, reference: WorldObject?, sendMessage: Boolean): ItemInstance? {
        if (count > 0) {
            // Retrieve the template of the item.
            val item = ItemTable.getTemplate(itemId) ?: return null

            // Sends message to client if requested.
            if (sendMessage && (!isCastingNow && item.itemType === EtcItemType.HERB || item.itemType !== EtcItemType.HERB)) {
                if (count > 1) {
                    if (process.equals("Sweep", ignoreCase = true) || process.equals("Quest", ignoreCase = true))
                        sendPacket(
                            SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(itemId).addItemNumber(
                                count
                            )
                        )
                    else
                        sendPacket(
                            SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S2_S1).addItemName(
                                itemId
                            ).addItemNumber(count)
                        )
                } else {
                    if (process.equals("Sweep", ignoreCase = true) || process.equals("Quest", ignoreCase = true))
                        sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1).addItemName(itemId))
                    else
                        sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_PICKED_UP_S1).addItemName(itemId))
                }
            }

            // If the item is herb type, dont add it to inventory.
            if (item.itemType === EtcItemType.HERB) {
                val herb = ItemInstance(0, itemId)

                val handler = ItemHandler.getHandler(herb.etcItem)
                handler?.useItem(this, herb, false)
            } else {
                // Add the item to inventory
                val createdItem = inventory!!.addItem(process, itemId, count, this, reference)

                // Cursed Weapon
                if (CursedWeaponManager.isCursed(createdItem!!.itemId))
                    CursedWeaponManager.activate(this, createdItem)
                else if (item.itemType === EtcItemType.ARROW && attackType === WeaponType.BOW && inventory.getPaperdollItem(
                        Inventory.PAPERDOLL_LHAND
                    ) == null
                )
                    checkAndEquipArrows()// If you pickup arrows and a bow is equipped, try to equip them if no arrows is currently equipped.

                return createdItem
            }
        }
        return null
    }

    /**
     * Destroy item from inventory and send InventoryUpdate packet to the Player.
     * @param process String Identifier of process triggering this action
     * @param item ItemInstance to be destroyed
     * @param reference WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @param sendMessage boolean Specifies whether to send message to Client about this action
     * @return boolean informing if the action was successfull
     */
    fun destroyItem(process: String, item: ItemInstance, reference: WorldObject?, sendMessage: Boolean): Boolean {
        return destroyItem(process, item, item.count, reference, sendMessage)
    }

    /**
     * Destroy item from inventory and send InventoryUpdate packet to the Player.
     * @param process String Identifier of process triggering this action
     * @param item ItemInstance to be destroyed
     * @param count int Quantity of ancient adena to be reduced
     * @param reference WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @param sendMessage boolean Specifies whether to send message to Client about this action
     * @return boolean informing if the action was successfull
     */
    fun destroyItem(
        process: String?,
        item: ItemInstance?,
        count: Int,
        reference: WorldObject?,
        sendMessage: Boolean
    ): Boolean {
        var item = item
        item = inventory!!.destroyItem(process, item!!, count, this, reference)
        if (item == null) {
            if (sendMessage)
                sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS)

            return false
        }

        // Send inventory update packet
        val iu = InventoryUpdate()
        if (item.count == 0)
            iu.addRemovedItem(item)
        else
            iu.addModifiedItem(item)
        sendPacket(iu)

        // Update current load as well
        val su = StatusUpdate(this)
        su.addAttribute(StatusUpdate.CUR_LOAD, currentLoad)
        sendPacket(su)

        // Sends message to client if requested
        if (sendMessage) {
            if (count > 1)
                sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(item).addItemNumber(
                        count
                    )
                )
            else
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED).addItemName(item))
        }
        return true
    }

    /**
     * Destroys item from inventory and send InventoryUpdate packet to the Player.
     * @param process String Identifier of process triggering this action
     * @param objectId int Item Instance identifier of the item to be destroyed
     * @param count int Quantity of items to be destroyed
     * @param reference WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @param sendMessage boolean Specifies whether to send message to Client about this action
     * @return boolean informing if the action was successfull
     */
    override fun destroyItem(
        process: String,
        objectId: Int,
        count: Int,
        reference: WorldObject?,
        sendMessage: Boolean
    ): Boolean {
        val item = inventory!!.getItemByObjectId(objectId)
        if (item == null) {
            if (sendMessage)
                sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS)

            return false
        }

        return destroyItem(process, item, count, reference, sendMessage)
    }

    /**
     * Destroys shots from inventory without logging and only occasional saving to database. Sends InventoryUpdate packet to the Player.
     * @param process String Identifier of process triggering this action
     * @param objectId int Item Instance identifier of the item to be destroyed
     * @param count int Quantity of items to be destroyed
     * @param reference WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @param sendMessage boolean Specifies whether to send message to Client about this action
     * @return boolean informing if the action was successfull
     */
    fun destroyItemWithoutTrace(
        process: String,
        objectId: Int,
        count: Int,
        reference: WorldObject?,
        sendMessage: Boolean
    ): Boolean {
        val item = inventory!!.getItemByObjectId(objectId)

        if (item == null || item.count < count) {
            if (sendMessage)
                sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS)

            return false
        }

        return destroyItem(null, item, count, reference, sendMessage)
    }

    /**
     * Destroy item from inventory by using its <B>itemId</B> and send InventoryUpdate packet to the Player.
     * @param process String Identifier of process triggering this action
     * @param itemId int Item identifier of the item to be destroyed
     * @param count int Quantity of items to be destroyed
     * @param reference WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @param sendMessage boolean Specifies whether to send message to Client about this action
     * @return boolean informing if the action was successfull
     */
    override fun destroyItemByItemId(
        process: String,
        itemId: Int,
        count: Int,
        reference: WorldObject?,
        sendMessage: Boolean
    ): Boolean {
        if (itemId == 57)
            return reduceAdena(process, count, reference, sendMessage)

        val item = inventory!!.getItemByItemId(itemId)

        if (item == null || item.count < count || inventory.destroyItemByItemId(
                process,
                itemId,
                count,
                this,
                reference
            ) == null
        ) {
            if (sendMessage)
                sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS)

            return false
        }

        // Send inventory update packet
        val playerIU = InventoryUpdate()
        playerIU.addItem(item)
        sendPacket(playerIU)

        // Update current load as well
        val su = StatusUpdate(this)
        su.addAttribute(StatusUpdate.CUR_LOAD, currentLoad)
        sendPacket(su)

        // Sends message to client if requested
        if (sendMessage) {
            if (count > 1)
                sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(itemId).addItemNumber(
                        count
                    )
                )
            else
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED).addItemName(itemId))
        }
        return true
    }

    /**
     * Transfers item to another ItemContainer and send InventoryUpdate packet to the Player.
     * @param process String Identifier of process triggering this action
     * @param objectId int Item Identifier of the item to be transfered
     * @param count int Quantity of items to be transfered
     * @param target Inventory the Inventory target.
     * @param reference WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @return ItemInstance corresponding to the new item or the updated item in inventory
     */
    fun transferItem(
        process: String,
        objectId: Int,
        count: Int,
        target: Inventory?,
        reference: WorldObject?
    ): ItemInstance? {
        val oldItem = checkItemManipulation(objectId, count) ?: return null

        val newItem = inventory!!.transferItem(process, objectId, count, target, this, reference) ?: return null

        // Send inventory update packet
        val playerIU = InventoryUpdate()

        if (oldItem.count > 0 && oldItem != newItem)
            playerIU.addModifiedItem(oldItem)
        else
            playerIU.addRemovedItem(oldItem)

        sendPacket(playerIU)

        // Update current load as well
        var playerSU = StatusUpdate(this)
        playerSU.addAttribute(StatusUpdate.CUR_LOAD, currentLoad)
        sendPacket(playerSU)

        // Send target update packet
        if (target is PcInventory) {
            val targetPlayer = target.owner

            val playerIU2 = InventoryUpdate()
            if (newItem.count > count)
                playerIU2.addModifiedItem(newItem)
            else
                playerIU2.addNewItem(newItem)
            targetPlayer.sendPacket(playerIU2)

            // Update current load as well
            playerSU = StatusUpdate(targetPlayer)
            playerSU.addAttribute(StatusUpdate.CUR_LOAD, targetPlayer.currentLoad)
            targetPlayer.sendPacket(playerSU)
        } else if (target is PetInventory) {
            val petIU = PetInventoryUpdate()
            if (newItem.count > count)
                petIU.addModifiedItem(newItem)
            else
                petIU.addNewItem(newItem)
            target.owner.owner.sendPacket(petIU)
        }
        return newItem
    }

    /**
     * Drop item from inventory and send InventoryUpdate packet to the Player.
     * @param process String Identifier of process triggering this action
     * @param item ItemInstance to be dropped
     * @param reference WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @param sendMessage boolean Specifies whether to send message to Client about this action
     * @return boolean informing if the action was successfull
     */
    fun dropItem(process: String, item: ItemInstance?, reference: WorldObject?, sendMessage: Boolean): Boolean {
        var item = item
        item = inventory!!.dropItem(process, item, this, reference)

        if (item == null) {
            if (sendMessage)
                sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS)

            return false
        }

        item.dropMe(this, x + Rnd[-25, 25], y + Rnd[-25, 25], z + 20)

        // Send inventory update packet
        val playerIU = InventoryUpdate()
        playerIU.addItem(item)
        sendPacket(playerIU)

        // Update current load as well
        val su = StatusUpdate(this)
        su.addAttribute(StatusUpdate.CUR_LOAD, currentLoad)
        sendPacket(su)

        // Sends message to client if requested
        if (sendMessage)
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_DROPPED_S1).addItemName(item))

        return true
    }

    /**
     * Drop item from inventory by using its <B>objectID</B> and send InventoryUpdate packet to the Player.
     * @param process String Identifier of process triggering this action
     * @param objectId int Item Instance identifier of the item to be dropped
     * @param count int Quantity of items to be dropped
     * @param x int coordinate for drop X
     * @param y int coordinate for drop Y
     * @param z int coordinate for drop Z
     * @param reference WorldObject Object referencing current action like NPC selling item or previous item in transformation
     * @param sendMessage boolean Specifies whether to send message to Client about this action
     * @return ItemInstance corresponding to the new item or the updated item in inventory
     */
    fun dropItem(
        process: String,
        objectId: Int,
        count: Int,
        x: Int,
        y: Int,
        z: Int,
        reference: WorldObject?,
        sendMessage: Boolean
    ): ItemInstance? {
        val invItem = inventory!!.getItemByObjectId(objectId)
        val item = inventory.dropItem(process, objectId, count, this, reference)

        if (item == null) {
            if (sendMessage)
                sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS)

            return null
        }

        item.dropMe(this, x, y, z)

        // Send inventory update packet
        val playerIU = InventoryUpdate()
        playerIU.addItem(invItem)
        sendPacket(playerIU)

        // Update current load as well
        val su = StatusUpdate(this)
        su.addAttribute(StatusUpdate.CUR_LOAD, currentLoad)
        sendPacket(su)

        // Sends message to client if requested
        if (sendMessage)
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_DROPPED_S1).addItemName(item))

        return item
    }

    fun checkItemManipulation(objectId: Int, count: Int): ItemInstance? {
        if (World.getObject(objectId) == null)
            return null

        val item = inventory!!.getItemByObjectId(objectId)

        if (item == null || item.ownerId != objectId)
            return null

        if (count < 1 || count > 1 && !item.isStackable)
            return null

        if (count > item.count)
            return null

        // Pet is summoned and not the item that summoned the pet AND not the buggle from strider you're mounting
        if (pet != null && pet!!.controlItemId == objectId || mountObjectId == objectId)
            return null

        if (activeEnchantItem != null && activeEnchantItem!!.objectId == objectId)
            return null

        // We cannot put a Weapon with Augmention in WH while casting (Possible Exploit)
        return if (item.isAugmented && (isCastingNow || isCastingSimultaneouslyNow)) null else item

    }

    /**
     * Launch a task corresponding to Config time.
     * @param protect boolean Drop timer or activate it.
     */
    fun setSpawnProtection(protect: Boolean) {
        if (protect) {
            if (_protectTask == null)
                _protectTask = ThreadPool.schedule(Runnable{
                    setSpawnProtection(false)
                    sendMessage("The spawn protection has ended.")
                }, (Config.PLAYER_SPAWN_PROTECTION * 1000).toLong())
        } else {
            _protectTask!!.cancel(true)
            _protectTask = null
        }
        broadcastUserInfo()
    }

    /**
     * Set protection from agro mobs when getting up from fake death, according settings.
     */
    fun setRecentFakeDeath() {
        _recentFakeDeathEndTime = System.currentTimeMillis() + Config.PLAYER_FAKEDEATH_UP_PROTECTION * 1000
    }

    fun clearRecentFakeDeath() {
        _recentFakeDeathEndTime = 0
    }

    /**
     * Close the active connection with the [L2GameClient] linked to this [Player].
     * @param closeClient : If true, the client is entirely closed. Otherwise, the client is sent back to login.
     */
    fun logout(closeClient: Boolean) {
        val client = this.client ?: return

        if (client.isDetached)
            client.cleanMe(true)
        else if (!client.connection.isClosed)
            client.close(if (closeClient) LeaveWorld.STATIC_PACKET else ServerClose.STATIC_PACKET)
    }

    /**
     * @see Creature.enableSkill
     */
    override fun enableSkill(skill: L2Skill?) {
        super.enableSkill(skill)
        _reuseTimeStamps.remove(skill!!.reuseHashCode)
    }

    override fun checkDoCastConditions(skill: L2Skill?): Boolean {
        if (!super.checkDoCastConditions(skill))
            return false

        // Can't summon multiple servitors.
        if (skill!!.skillType === L2SkillType.SUMMON) {
            if (!(skill as L2SkillSummon).isCubic && (pet != null || isMounted)) {
                sendPacket(SystemMessageId.SUMMON_ONLY_ONE)
                return false
            }
        } else if (skill!!.skillType === L2SkillType.RESURRECT) {
            val siege = CastleManager.getActiveSiege(this)
            if (siege != null) {
                if (clan == null) {
                    sendPacket(SystemMessageId.CANNOT_BE_RESURRECTED_DURING_SIEGE)
                    return false
                }

                val side = siege.getSide(clan!!)
                if (side === SiegeSide.DEFENDER || side === SiegeSide.OWNER) {
                    if (siege.controlTowerCount == 0) {
                        sendPacket(SystemMessageId.TOWER_DESTROYED_NO_RESURRECTION)
                        return false
                    }
                } else if (side === SiegeSide.ATTACKER) {
                    if (clan!!.flag == null) {
                        sendPacket(SystemMessageId.NO_RESURRECTION_WITHOUT_BASE_CAMP)
                        return false
                    }
                } else {
                    sendPacket(SystemMessageId.CANNOT_BE_RESURRECTED_DURING_SIEGE)
                    return false
                }
            }
        } else if (skill!!.skillType === L2SkillType.SIGNET || skill!!.skillType === L2SkillType.SIGNET_CASTTIME) {
            val region = region ?: return false

            if (!region.checkEffectRangeInsidePeaceZone(
                    skill!!,
                    if (skill.targetType == L2Skill.SkillTargetType.TARGET_GROUND) currentSkillWorldPosition!! else position
                )
            ) {
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill))
                return false
            }
        }// Can't casting signets on peace zone.
        // Can't use ressurect skills on siege if you are defender and control towers is not alive, if you are attacker and flag isn't spawned or if you aren't part of that siege.

        // Can't use Hero and resurrect skills during Olympiad
        if (isInOlympiadMode && (skill!!.isHeroSkill || skill.skillType === L2SkillType.RESURRECT)) {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.THIS_SKILL_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT))
            return false
        }

        // Check if the spell uses charges
        if (skill!!.maxCharges == 0 && charges < skill.numCharges) {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill))
            return false
        }

        return true
    }

    override fun onAction(player: Player) {
        // Set the target of the player
        if (player.target !== this)
            player.target = this
        else {
            // Check if this Player has a Private Store
            if (isInStoreMode) {
                player.ai!!.setIntention(CtrlIntention.INTERACT, this)
                return
            }

            // Check if this Player is autoAttackable
            if (isAutoAttackable(player)) {
                // Player with lvl < 21 can't attack a cursed weapon holder and a cursed weapon holder can't attack players with lvl < 21
                if (isCursedWeaponEquipped && player.level < 21 || player.isCursedWeaponEquipped && level < 21) {
                    player.sendPacket(ActionFailed.STATIC_PACKET)
                    return
                }

                if (GeoEngine.canSeeTarget(player, this)) {
                    player.ai!!.setIntention(CtrlIntention.ATTACK, this)
                    player.onActionRequest()
                }
            } else {
                // avoids to stuck when clicking two or more times
                player.sendPacket(ActionFailed.STATIC_PACKET)

                if (player != this && GeoEngine.canSeeTarget(player, this))
                    player.ai!!.setIntention(CtrlIntention.FOLLOW, this)
            }
        }
    }

    override fun onActionShift(player: Player) {
        if (player.isGM)
            AdminEditChar.showCharacterInfo(player, this)

        super.onActionShift(player)
    }

    /**
     * @param barPixels
     * @return true if cp update should be done, false if not
     */
    private fun needCpUpdate(barPixels: Int): Boolean {
        val currentCp = currentCp

        if (currentCp <= 1.0 || maxCp < barPixels)
            return true

        if (currentCp <= _cpUpdateDecCheck || currentCp >= _cpUpdateIncCheck) {
            if (currentCp == maxCp.toDouble()) {
                _cpUpdateIncCheck = currentCp + 1
                _cpUpdateDecCheck = currentCp - _cpUpdateInterval
            } else {
                val doubleMulti = currentCp / _cpUpdateInterval
                var intMulti = doubleMulti.toInt()

                _cpUpdateDecCheck = _cpUpdateInterval * if (doubleMulti < intMulti) intMulti-- else intMulti
                _cpUpdateIncCheck = _cpUpdateDecCheck + _cpUpdateInterval
            }

            return true
        }

        return false
    }

    /**
     * @param barPixels
     * @return true if mp update should be done, false if not
     */
    private fun needMpUpdate(barPixels: Int): Boolean {
        val currentMp = currentMp

        if (currentMp <= 1.0 || maxMp < barPixels)
            return true

        if (currentMp <= _mpUpdateDecCheck || currentMp >= _mpUpdateIncCheck) {
            if (currentMp == maxMp.toDouble()) {
                _mpUpdateIncCheck = currentMp + 1
                _mpUpdateDecCheck = currentMp - _mpUpdateInterval
            } else {
                val doubleMulti = currentMp / _mpUpdateInterval
                var intMulti = doubleMulti.toInt()

                _mpUpdateDecCheck = _mpUpdateInterval * if (doubleMulti < intMulti) intMulti-- else intMulti
                _mpUpdateIncCheck = _mpUpdateDecCheck + _mpUpdateInterval
            }

            return true
        }

        return false
    }

    /**
     * Send packet StatusUpdate with current HP,MP and CP to the Player and only current HP, MP and Level to all other Player of the Party.
     *
     *  * Send StatusUpdate with current HP, MP and CP to this Player
     *  * Send PartySmallWindowUpdate with current HP, MP and Level to all other Player of the Party
     *
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : This method DOESN'T SEND current HP and MP to all Player of the _statusListener</B></FONT>
     */
    override fun broadcastStatusUpdate() {
        // Send StatusUpdate with current HP, MP and CP to this Player
        val su = StatusUpdate(this)
        su.addAttribute(StatusUpdate.CUR_HP, currentHp.toInt())
        su.addAttribute(StatusUpdate.CUR_MP, currentMp.toInt())
        su.addAttribute(StatusUpdate.CUR_CP, currentCp.toInt())
        su.addAttribute(StatusUpdate.MAX_CP, maxCp)
        sendPacket(su)

        val needCpUpdate = needCpUpdate(352)
        val needHpUpdate = needHpUpdate(352)

        // Check if a party is in progress and party window update is needed.
        if (party != null && (needCpUpdate || needHpUpdate || needMpUpdate(352)))
            party!!.broadcastToPartyMembers(this, PartySmallWindowUpdate(this))

        if (isInOlympiadMode && isOlympiadStart && (needCpUpdate || needHpUpdate)) {
            val game = OlympiadGameManager.getOlympiadTask(olympiadGameId)
            if (game != null && game.isBattleStarted)
                game.zone.broadcastStatusUpdate(this)
        }

        // In duel, MP updated only with CP or HP
        if (isInDuel && (needCpUpdate || needHpUpdate)) {
            val update = ExDuelUpdateUserInfo(this)
            DuelManager.broadcastToOppositeTeam(this, update)
        }
    }

    /**
     * Broadcast informations from a user to himself and his knownlist.<BR></BR>
     * If player is morphed, it sends informations from the template the player is using.
     *
     *  * Send a UserInfo packet (public and private data) to this Player.
     *  * Send a CharInfo packet (public data only) to Player's knownlist.
     *
     */
    fun broadcastUserInfo() {
        sendPacket(UserInfo(this))

        if (polyType === WorldObject.PolyType.NPC)
            this.toKnownPlayers(AbstractNpcInfo.PcMorphInfo(this, polyTemplate!!))
        else
            broadcastCharInfo()
    }

    fun broadcastCharInfo() {
        for (player in getKnownType(Player::class.java)) {
            player.sendPacket(CharInfo(this))

            val relation = getRelation(player)
            val isAutoAttackable = isAutoAttackable(player)

            player.sendPacket(RelationChanged(this, relation, isAutoAttackable))
            if (pet != null)
                player.sendPacket(RelationChanged(pet!!, relation, isAutoAttackable))
        }
    }

    /**
     * Broadcast player title information.
     */
    fun broadcastTitleInfo() {
        sendPacket(UserInfo(this))
        broadcastPacket(TitleUpdate(this))
    }

    /**
     * Send a packet to the Player.
     */
    override fun sendPacket(packet: L2GameServerPacket) {
        if (client != null)
            client!!.sendPacket(packet)
    }

    /**
     * Send SystemMessage packet.
     * @param id SystemMessageId
     */
    override fun sendPacket(id: SystemMessageId) {
        sendPacket(SystemMessage.getSystemMessage(id))
    }

    /**
     * Manage Interact Task with another Player.<BR></BR>
     * Turn the character in front of the target.<BR></BR>
     * In case of private stores, send the related packet.
     * @param target The Creature targeted
     */
    fun doInteract(target: Creature?) {
        if (target is Player) {
            val temp = target as Player?
            sendPacket(MoveToPawn(this, temp!!, Npc.INTERACTION_DISTANCE))

            when (temp.storeType) {
                Player.StoreType.SELL, Player.StoreType.PACKAGE_SELL -> sendPacket(PrivateStoreListSell(this, temp))

                Player.StoreType.BUY -> sendPacket(PrivateStoreListBuy(this, temp))

                Player.StoreType.MANUFACTURE -> sendPacket(RecipeShopSellList(this, temp))
            }
        } else {
            // _interactTarget=null should never happen but one never knows ^^;
            target?.onAction(this)
        }
    }

    /**
     * Manage AutoLoot Task.
     *
     *  * Send a System Message to the Player : YOU_PICKED_UP_S1_ADENA or YOU_PICKED_UP_S1_S2
     *  * Add the Item to the Player inventory
     *  * Send InventoryUpdate to this Player with NewItem (use a new slot) or ModifiedItem (increase amount)
     *  * Send StatusUpdate to this Player with current weight
     *
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : If a Party is in progress, distribute Items between party members</B></FONT>
     * @param target The reference Object.
     * @param item The dropped ItemHolder.
     */
    fun doAutoLoot(target: Attackable, item: IntIntHolder) {
        if (isInParty)
            party!!.distributeItem(this, item, false, target)
        else if (item.id == 57)
            addAdena("Loot", item.value, target, true)
        else
            addItem("Loot", item.id, item.value, target, true)
    }

    /**
     * Manage Pickup Task.
     *
     *  * Send StopMove to this Player
     *  * Remove the ItemInstance from the world and send GetItem packets
     *  * Send a System Message to the Player : YOU_PICKED_UP_S1_ADENA or YOU_PICKED_UP_S1_S2
     *  * Add the Item to the Player inventory
     *  * Send InventoryUpdate to this Player with NewItem (use a new slot) or ModifiedItem (increase amount)
     *  * Send StatusUpdate to this Player with current weight
     *
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : If a Party is in progress, distribute Items between party members</B></FONT>
     * @param object The ItemInstance to pick up
     */
    override fun doPickupItem(`object`: WorldObject) {
        if (isAlikeDead || isFakeDeath)
            return

        // Set the AI Intention to IDLE
        ai!!.setIntention(CtrlIntention.IDLE)

        // Check if the WorldObject to pick up is a ItemInstance
        if (`object` !is ItemInstance)
            return

// Send ActionFailed to this Player
        sendPacket(ActionFailed.STATIC_PACKET)
        sendPacket(StopMove(this))

        synchronized(`object`) {
            if (!`object`.isVisible)
                return

            if (isInStoreMode)
                return

            if (!inventory!!.validateWeight(`object`.count * `object`.item.weight)) {
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.WEIGHT_LIMIT_EXCEEDED))
                return
            }

            if ((isInParty && party!!.lootRule === LootRule.ITEM_LOOTER || !isInParty) && !inventory.validateCapacity(
                    `object`
                )
            ) {
                sendPacket(SystemMessageId.SLOTS_FULL)
                return
            }

            if (activeTradeList != null) {
                sendPacket(SystemMessageId.CANNOT_PICKUP_OR_USE_ITEM_WHILE_TRADING)
                return
            }

            if (`object`.ownerId != 0 && !isLooterOrInLooterParty(`object`.ownerId)) {
                if (`object`.itemId == 57)
                    sendPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1_ADENA).addNumber(
                            `object`.count
                        )
                    )
                else if (`object`.count > 1)
                    sendPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S2_S1_S).addItemName(
                            `object`
                        ).addNumber(`object`.count)
                    )
                else
                    sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FAILED_TO_PICKUP_S1).addItemName(`object`))

                return
            }

            if (`object`.hasDropProtection())
                `object`.removeDropProtection()

            // Remove the ItemInstance from the world and send GetItem packets
            `object`.pickupMe(this)

            // item must be removed from ItemsOnGroundManager if is active
            ItemsOnGroundTaskManager.remove(`object`)
        }

        // Auto use herbs - pick up
        if (`object`.itemType === EtcItemType.HERB) {
            val handler = ItemHandler.getHandler(`object`.etcItem)
            handler?.useItem(this, `object`, false)

            `object`.destroyMe("Consume", this, null)
        } else if (CursedWeaponManager.isCursed(`object`.itemId)) {
            addItem("Pickup", `object`, null, true)
        } else {
            // if item is instance of L2ArmorType or WeaponType broadcast an "Attention" system message
            if (`object`.itemType is ArmorType || `object`.itemType is WeaponType) {
                val msg: SystemMessage
                if (`object`.enchantLevel > 0)
                    msg = SystemMessage.getSystemMessage(SystemMessageId.ATTENTION_S1_PICKED_UP_S2_S3).addString(name)
                        .addNumber(`object`.enchantLevel).addItemName(`object`.itemId)
                else
                    msg = SystemMessage.getSystemMessage(SystemMessageId.ATTENTION_S1_PICKED_UP_S2).addString(name)
                        .addItemName(`object`.itemId)

                broadcastPacket(msg, 1400)
            }

            // Check if a Party is in progress
            if (isInParty)
                party!!.distributeItem(this, `object`)
            else if (`object`.itemId == 57 && inventory!!.adenaInstance != null) {
                addAdena("Pickup", `object`.count, null, true)
                `object`.destroyMe("Pickup", this, null)
            } else
                addItem("Pickup", `object`, null, true)// Target is regular item
            // Target is adena
        }// Cursed Weapons are not distributed

        // Schedule a paralyzed task to wait for the animation to finish
        ThreadPool.schedule(Runnable{ isParalyzed = false }, (700 / stat.movementSpeedMultiplier).toInt().toLong())
        isParalyzed = true
    }

    override fun doAttack(target: Creature?) {
        super.doAttack(target)
        clearRecentFakeDeath()
    }

    override fun doCast(skill: L2Skill?) {
        super.doCast(skill)
        clearRecentFakeDeath()
    }

    fun canOpenPrivateStore(): Boolean {
        if (activeTradeList != null)
            cancelActiveTrade()

        return !isAlikeDead && !isInOlympiadMode && !isMounted && !isInsideZone(ZoneId.NO_STORE) && !isCastingNow
    }

    fun tryOpenPrivateBuyStore() {
        if (canOpenPrivateStore()) {
            if (storeType == StoreType.BUY || storeType == StoreType.BUY_MANAGE)
                storeType = StoreType.NONE

            if (storeType == StoreType.NONE) {
                standUp()

                storeType = StoreType.BUY_MANAGE
                sendPacket(PrivateStoreManageListBuy(this))
            }
        } else {
            if (isInsideZone(ZoneId.NO_STORE))
                sendPacket(SystemMessageId.NO_PRIVATE_STORE_HERE)

            sendPacket(ActionFailed.STATIC_PACKET)
        }
    }

    fun tryOpenPrivateSellStore(isPackageSale: Boolean) {
        if (canOpenPrivateStore()) {
            if (storeType == StoreType.SELL || storeType == StoreType.SELL_MANAGE || storeType == StoreType.PACKAGE_SELL)
                storeType = StoreType.NONE

            if (storeType == StoreType.NONE) {
                standUp()

                storeType = StoreType.SELL_MANAGE
                sendPacket(PrivateStoreManageListSell(this, isPackageSale))
            }
        } else {
            if (isInsideZone(ZoneId.NO_STORE))
                sendPacket(SystemMessageId.NO_PRIVATE_STORE_HERE)

            sendPacket(ActionFailed.STATIC_PACKET)
        }
    }

    fun tryOpenWorkshop(isDwarven: Boolean) {
        if (canOpenPrivateStore()) {
            if (isInStoreMode)
                storeType = StoreType.NONE

            if (storeType == StoreType.NONE) {
                standUp()

                if (createList == null)
                    createList = ManufactureList()

                sendPacket(RecipeShopManageList(this, isDwarven))
            }
        } else {
            if (isInsideZone(ZoneId.NO_STORE))
                sendPacket(SystemMessageId.NO_PRIVATE_WORKSHOP_HERE)

            sendPacket(ActionFailed.STATIC_PACKET)
        }
    }

    /**
     * @param type : The ArmorType to check. It supports NONE, SHIELD, MAGIC, LIGHT and HEAVY.
     * @return true if the given ArmorType is used by the chest of the player, false otherwise.
     */
    fun isWearingArmorType(type: ArmorType): Boolean {
        // Retrieve either the shield or the chest, following ArmorType to check.
        val armor =
            inventory!!.getPaperdollItem(if (type === ArmorType.SHIELD) Inventory.PAPERDOLL_LHAND else Inventory.PAPERDOLL_CHEST)
                ?: return type === ArmorType.NONE
// Return true if not equipped and the check was based on NONE ArmorType.

        // Test if the equipped item is an armor, then finally compare both ArmorType.
        return armor.itemType is ArmorType && armor.itemType === type
    }

    fun setRequesterId(requesterId: Int) {
        _requesterId = requesterId
    }

    fun engageAnswer(answer: Int) {
        if (!isUnderMarryRequest || _requesterId == 0)
            return

        val requester = World.getPlayer(_requesterId)
        if (requester != null) {
            if (answer == 1) {
                // Create the couple
                CoupleManager.addCouple(requester, this)

                // Then "finish the job"
                WeddingManagerNpc.justMarried(requester, this)
            } else {
                isUnderMarryRequest = false
                sendMessage("You declined your partner's marriage request.")

                requester.isUnderMarryRequest = false
                requester.sendMessage("Your partner declined your marriage request.")
            }
        }
    }

    /**
     * Kill the Creature, Apply Death Penalty, Manage gain/loss Karma and Item Drop.
     *
     *  * Reduce the Experience of the Player in function of the calculated Death Penalty
     *  * If necessary, unsummon the Pet of the killed Player
     *  * Manage Karma gain for attacker and Karam loss for the killed Player
     *  * If the killed Player has Karma, manage Drop Item
     *  * Kill the Player
     *
     * @param killer The Creature who attacks
     */
    override fun doDie(killer: Creature?): Boolean {
        // Kill the Player
        if (!super.doDie(killer))
            return false

        if (isMounted)
            stopFeed()

        synchronized(this) {
            if (isFakeDeath)
                stopFakeDeath(true)
        }

        if (killer != null) {
            val pk = killer.actingPlayer

            // Clear resurrect xp calculation
            expBeforeDeath = 0

            if (isCursedWeaponEquipped)
                CursedWeaponManager.drop(cursedWeaponEquippedId, killer)
            else {
                if (pk == null || !pk.isCursedWeaponEquipped) {
                    onDieDropItem(killer) // Check if any item should be dropped

                    // if the area isn't an arena
                    if (!isInArena) {
                        // if both victim and attacker got clans & aren't academicians
                        if (pk != null && pk.clan != null && clan != null && !isAcademyMember && !pk.isAcademyMember) {
                            // if clans got mutual war, then use the reputation calcul
                            if (clan!!.isAtWarWith(pk.clanId) && pk.clan!!.isAtWarWith(clan!!.clanId)) {
                                // when your reputation score is 0 or below, the other clan cannot acquire any reputation points
                                if (clan!!.reputationScore > 0)
                                    pk.clan!!.addReputationScore(1)
                                // when the opposing sides reputation score is 0 or below, your clans reputation score doesn't decrease
                                if (pk.clan!!.reputationScore > 0)
                                    clan!!.takeReputationScore(1)
                            }
                        }
                    }

                    // Reduce player's xp and karma.
                    if (Config.ALT_GAME_DELEVEL && (!hasSkill(L2Skill.SKILL_LUCKY) || stat.level > 9))
                        deathPenalty(
                            pk != null && clan != null && pk.clan != null && (clan!!.isAtWarWith(pk.clanId) || pk.clan!!.isAtWarWith(
                                clanId
                            )), pk != null, killer is SiegeGuard
                        )
                }
            }
        }

        // Unsummon Cubics
        if (!_cubics.isEmpty()) {
            for (cubic in _cubics.values) {
                cubic.stopAction()
                cubic.cancelDisappear()
            }

            _cubics.clear()
        }

        if (fusionSkill != null)
            abortCast()

        for (character in getKnownType(Creature::class.java))
            if (character.fusionSkill != null && character.fusionSkill!!.target === this)
                character.abortCast()

        // calculate death penalty buff
        calculateDeathPenaltyBuffLevel(killer)

        WaterTaskManager.remove(this)

        if (isPhoenixBlessed || isAffected(L2EffectFlag.CHARM_OF_COURAGE) && isInSiege)
            reviveRequest(this, null, false)

        // Icons update in order to get retained buffs list
        updateEffectIcons()

        return true
    }

    private fun onDieDropItem(killer: Creature?) {
        if (killer == null)
            return

        val pk = killer.actingPlayer
        if (karma <= 0 && pk != null && pk.clan != null && clan != null && pk.clan!!.isAtWarWith(clanId))
            return

        if ((!isInsideZone(ZoneId.PVP) || pk == null) && (!isGM || Config.KARMA_DROP_GM)) {
            val isKillerNpc = killer is Npc
            val pkLimit = Config.KARMA_PK_LIMIT

            var dropEquip = 0
            var dropEquipWeapon = 0
            var dropItem = 0
            var dropLimit = 0
            var dropPercent = 0

            if (karma > 0 && pkKills >= pkLimit) {
                dropPercent = Config.KARMA_RATE_DROP
                dropEquip = Config.KARMA_RATE_DROP_EQUIP
                dropEquipWeapon = Config.KARMA_RATE_DROP_EQUIP_WEAPON
                dropItem = Config.KARMA_RATE_DROP_ITEM
                dropLimit = Config.KARMA_DROP_LIMIT
            } else if (isKillerNpc && level > 4 && !isFestivalParticipant) {
                dropPercent = Config.PLAYER_RATE_DROP
                dropEquip = Config.PLAYER_RATE_DROP_EQUIP
                dropEquipWeapon = Config.PLAYER_RATE_DROP_EQUIP_WEAPON
                dropItem = Config.PLAYER_RATE_DROP_ITEM
                dropLimit = Config.PLAYER_DROP_LIMIT
            }

            if (dropPercent > 0 && Rnd[100] < dropPercent) {
                var dropCount = 0
                var itemDropPercent = 0

                for (itemDrop in inventory!!.items) {
                    // Don't drop those following things
                    if (!itemDrop.isDropable || itemDrop.isShadowItem || itemDrop.itemId == 57 || itemDrop.item.type2 == Item.TYPE2_QUEST || pet != null && pet!!.controlItemId == itemDrop.itemId || Arrays.binarySearch(
                            Config.KARMA_LIST_NONDROPPABLE_ITEMS,
                            itemDrop.itemId
                        ) >= 0 || Arrays.binarySearch(Config.KARMA_LIST_NONDROPPABLE_PET_ITEMS, itemDrop.itemId) >= 0
                    )
                        continue

                    if (itemDrop.isEquipped) {
                        // Set proper chance according to Item type of equipped Item
                        itemDropPercent = if (itemDrop.item.type2 == Item.TYPE2_WEAPON) dropEquipWeapon else dropEquip
                        inventory.unEquipItemInSlot(itemDrop.locationSlot)
                    } else
                        itemDropPercent = dropItem // Item in inventory

                    // NOTE: Each time an item is dropped, the chance of another item being dropped gets lesser (dropCount * 2)
                    if (Rnd[100] < itemDropPercent) {
                        dropItem("DieDrop", itemDrop, killer, true)

                        if (++dropCount >= dropLimit)
                            break
                    }
                }
            }
        }
    }

    fun updateKarmaLoss(exp: Long) {
        if (!isCursedWeaponEquipped && karma > 0) {
            val karmaLost = Formulas.calculateKarmaLost(level, exp)
            if (karmaLost > 0)
                karma = karma - karmaLost
        }
    }

    /**
     * This method is used to update PvP counter, or PK counter / add Karma if necessary.<br></br>
     * It also updates clan kills/deaths counters on siege.
     * @param target The L2Playable victim.
     */
    fun onKillUpdatePvPKarma(target: Playable?) {
        if (target == null)
            return

        val targetPlayer = target.actingPlayer
        if (targetPlayer == null || targetPlayer == this)
            return

        // Don't rank up the CW if it was a summon.
        if (isCursedWeaponEquipped && target is Player) {
            CursedWeaponManager.increaseKills(cursedWeaponEquippedId)
            return
        }

        // If in duel and you kill (only can kill l2summon), do nothing
        if (isInDuel && targetPlayer.isInDuel)
            return

        // If in pvp zone, do nothing.
        if (isInsideZone(ZoneId.PVP) && targetPlayer.isInsideZone(ZoneId.PVP)) {
            // Until the zone was a siege zone. Check also if victim was a player. Randomers aren't counted.
            if (target is Player && siegeState > 0 && targetPlayer.siegeState > 0 && siegeState != targetPlayer.siegeState) {
                // Now check clan relations.
                val killerClan = clan
                if (killerClan != null)
                    killerClan.siegeKills = killerClan.siegeKills + 1

                val targetClan = targetPlayer.clan
                if (targetClan != null)
                    targetClan.siegeDeaths = targetClan.siegeDeaths + 1
            }
            return
        }

        // Check if it's pvp (cases : regular, wars, victim is PKer)
        if (checkIfPvP(target) || targetPlayer.clan != null && clan != null && clan!!.isAtWarWith(targetPlayer.clanId) && targetPlayer.clan!!.isAtWarWith(
                clanId
            ) && targetPlayer.pledgeType != Clan.SUBUNIT_ACADEMY && pledgeType != Clan.SUBUNIT_ACADEMY || targetPlayer.karma > 0 && Config.KARMA_AWARD_PK_KILL
        ) {
            if (target is Player) {
                // Add PvP point to attacker.
                pvpKills = pvpKills + 1

                // Send UserInfo packet to attacker with its Karma and PK Counter
                sendPacket(UserInfo(this))
            }
        } else if (targetPlayer.karma == 0 && targetPlayer.pvpFlag.toInt() == 0) {
            // PK Points are increased only if you kill a player.
            if (target is Player)
                pkKills = pkKills + 1

            // Calculate new karma.
            karma = karma + Formulas.calculateKarmaGain(pkKills, target is Summon)

            // Send UserInfo packet to attacker with its Karma and PK Counter
            sendPacket(UserInfo(this))
        }// Otherwise, killer is considered as a PKer.
    }

    fun updatePvPStatus() {
        if (isInsideZone(ZoneId.PVP))
            return

        PvpFlagTaskManager.add(this, Config.PVP_NORMAL_TIME.toLong())

        if (pvpFlag.toInt() == 0)
            updatePvPFlag(1)
    }

    fun updatePvPStatus(target: Creature) {
        val player = target.actingPlayer ?: return

        if (isInDuel && player.duelId == duelId)
            return

        if ((!isInsideZone(ZoneId.PVP) || !target.isInsideZone(ZoneId.PVP)) && player.karma == 0) {
            PvpFlagTaskManager.add(
                this,
                (if (checkIfPvP(player)) Config.PVP_PVP_TIME else Config.PVP_NORMAL_TIME).toLong()
            )

            if (pvpFlag.toInt() == 0)
                updatePvPFlag(1)
        }
    }

    /**
     * Restore the experience this Player has lost and sends StatusUpdate packet.
     * @param restorePercent The specified % of restored experience.
     */
    fun restoreExp(restorePercent: Double) {
        if (expBeforeDeath > 0) {
            (stat as PlayerStat).addExp(Math.round((expBeforeDeath - exp) * restorePercent / 100).toInt().toLong())
            expBeforeDeath = 0
        }
    }

    /**
     * Reduce the Experience (and level if necessary) of the Player in function of the calculated Death Penalty.
     *
     *  * Calculate the Experience loss
     *  * Set the value of _expBeforeDeath
     *  * Set the new Experience value of the Player and Decrease its level if necessary
     *  * Send StatusUpdate packet with its new Experience
     *
     * @param atWar If true, use clan war penalty system instead of regular system.
     * @param killedByPlayable Used to see if victim loses XP or not.
     * @param killedBySiegeNpc Used to see if victim loses XP or not.
     */
    fun deathPenalty(atWar: Boolean, killedByPlayable: Boolean, killedBySiegeNpc: Boolean) {
        // No xp loss inside pvp zone unless
        // - it's a siege zone and you're NOT participating
        // - you're killed by a non-pc whose not belong to the siege
        if (isInsideZone(ZoneId.PVP)) {
            // No xp loss for siege participants inside siege zone.
            if (isInsideZone(ZoneId.SIEGE)) {
                if (isInSiege && (killedByPlayable || killedBySiegeNpc))
                    return
            } else if (killedByPlayable)
                return // No xp loss for arenas participants killed by playable.
        }

        // Get the level of the Player
        val lvl = level

        // The death steal you some Exp
        var percentLost = 7.0
        if (level >= 76)
            percentLost = 2.0
        else if (level >= 40)
            percentLost = 4.0

        if (karma > 0)
            percentLost *= Config.RATE_KARMA_EXP_LOST

        if (isFestivalParticipant || atWar || isInsideZone(ZoneId.SIEGE))
            percentLost /= 4.0

        // Calculate the Experience loss
        var lostExp: Long = 0

        if (lvl < Experience.MAX_LEVEL)
            lostExp = Math.round(((stat as PlayerStat).getExpForLevel(lvl + 1) - (stat as PlayerStat).getExpForLevel(lvl)) * percentLost / 100)
        else
            lostExp =
                Math.round(((stat as PlayerStat).getExpForLevel(Experience.MAX_LEVEL.toInt()) - (stat as PlayerStat).getExpForLevel(Experience.MAX_LEVEL - 1)) * percentLost / 100)

        // Get the Experience before applying penalty
        expBeforeDeath = exp

        // Set new karma
        updateKarmaLoss(lostExp)

        // Set the new Experience value of the Player
        (stat as PlayerStat).addExp(-lostExp)
    }

    /**
     * Remove the player from both waiting list and any potential room.
     */
    fun removeMeFromPartyMatch() {
        PartyMatchWaitingList.removePlayer(this)
        if (partyRoom != 0) {
            val room = PartyMatchRoomList.getRoom(partyRoom)
            room?.deleteMember(this)
        }
    }

    /**
     * @return `true` if the player has a pet, `false` otherwise
     */
    fun hasPet(): Boolean {
        return pet is Pet
    }

    /**
     * @return `true` if the player has a summon, `false` otherwise
     */
    fun hasServitor(): Boolean {
        return pet is Servitor
    }

    /**
     * Set the _requestExpireTime of that Player, and set his partner as the active requester.
     * @param partner The partner to make checks on.
     */
    fun onTransactionRequest(partner: Player) {
        _requestExpireTime = System.currentTimeMillis() + REQUEST_TIMEOUT * 1000
        partner.activeRequester = this
    }

    /**
     * Select the Warehouse to be used in next activity.
     */
    fun onTransactionResponse() {
        _requestExpireTime = 0
    }

    fun onTradeStart(partner: Player) {
        activeTradeList = TradeList(this)
        activeTradeList!!.partner = partner

        sendPacket(SystemMessage.getSystemMessage(SystemMessageId.BEGIN_TRADE_WITH_S1).addString(partner.name))
        sendPacket(TradeStart(this))
    }

    fun onTradeConfirm(partner: Player) {
        sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CONFIRMED_TRADE).addString(partner.name))

        partner.sendPacket(TradePressOwnOk.STATIC_PACKET)
        sendPacket(TradePressOtherOk.STATIC_PACKET)
    }

    fun onTradeCancel(partner: Player) {
        if (activeTradeList == null)
            return

        activeTradeList!!.lock()
        activeTradeList = null

        sendPacket(SendTradeDone(0))
        sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CANCELED_TRADE).addString(partner.name))
    }

    fun onTradeFinish(successfull: Boolean) {
        activeTradeList = null
        sendPacket(SendTradeDone(1))
        if (successfull)
            sendPacket(SystemMessageId.TRADE_SUCCESSFUL)
    }

    fun startTrade(partner: Player) {
        onTradeStart(partner)
        partner.onTradeStart(this)
    }

    fun cancelActiveTrade() {
        if (activeTradeList == null)
            return

        val partner = activeTradeList!!.partner
        partner?.onTradeCancel(this)

        onTradeCancel(this)
    }

    /**
     * @return true if the [Player] can use dwarven recipes.
     */
    fun hasDwarvenCraft(): Boolean {
        return hasSkill(L2Skill.SKILL_CREATE_DWARVEN)
    }

    /**
     * @return true if the [Player] can use common recipes.
     */
    fun hasCommonCraft(): Boolean {
        return hasSkill(L2Skill.SKILL_CREATE_COMMON)
    }

    /**
     * Method used by regular leveling system.<br></br>
     * Reward the [Player] with autoGet skills only, or if Config.AUTO_LEARN_SKILLS is activated, with all available skills.
     */
    fun giveSkills() {
        if (Config.AUTO_LEARN_SKILLS)
            rewardSkills()
        else {
            // We reward all autoGet skills to this player, but don't store any on database.
            for (skill in availableAutoGetSkills)
                addSkill(skill.skill, false)

            // Remove the Lucky skill if level superior to 10.
            if (level >= 10 && hasSkill(L2Skill.SKILL_LUCKY))
                removeSkill(L2Skill.SKILL_LUCKY, false)

            // Remove invalid skills.
            removeInvalidSkills()

            sendSkillList()
        }
    }

    /**
     * Method used by admin commands, Config.AUTO_LEARN_SKILLS or class master.<br></br>
     * Reward the [Player] with all available skills, being autoGet or general skills.
     */
    fun rewardSkills() {
        // We reward all skills to the players, but don't store autoGet skills on the database.
        for (skill in allAvailableSkills)
            addSkill(skill.skill, skill.cost != 0)

        // Remove the Lucky skill if level superior to 10.
        if (level >= 10 && hasSkill(L2Skill.SKILL_LUCKY))
            removeSkill(L2Skill.SKILL_LUCKY, false)

        // Remove invalid skills.
        removeInvalidSkills()

        sendSkillList()
    }

    /**
     * Delete all invalid [L2Skill]s for this [Player].<br></br>
     * <br></br>
     * A skill is considered invalid when the level of obtention of the skill is superior to 9 compared to player level (expertise skill obtention level is compared to player level without any penalty).<br></br>
     * <br></br>
     * It is then either deleted, or level is refreshed.
     */
    private fun removeInvalidSkills() {
        if (skills.isEmpty())
            return

        // Retrieve the player template skills, based on actual level (+9 for regular skills, unchanged for expertise).
        val availableSkills =
            (template as PlayerTemplate).skills.filter { s -> s.minLvl <= level + if (s.id == L2Skill.SKILL_EXPERTISE) 0 else 9 }
                .associateBy {it.id to Collectors.maxBy(COMPARE_SKILLS_BY_LVL) }
                .map{ it.key.first to it.value }.toMap()

        for (skill in skills.values) {
            // Bother only with skills existing on template (spare temporary skills, items skills, etc).
            if ((template as PlayerTemplate).skills.stream().filter { s -> s.id == skill.id }.count() == 0L)
                continue

            // The known skill doesn't exist on available skills ; we drop existing skill.
            val tempSkill = availableSkills[skill.id]
            if (tempSkill == null) {
                removeSkill(skill.id, true)
                continue
            }

            // Retrieve the skill and max level for enchant scenario.
            val availableSkill = tempSkill
            val maxLevel = SkillTable.getMaxLevel(skill.id)

            // Case of enchanted skills.
            if (skill.level > maxLevel) {
                // Player level is inferior to 76, or available skill is a good candidate.
                if ((level < 76 || availableSkill.value < maxLevel) && skill.level > availableSkill.value)
                    addSkill(availableSkill.skill, true)
            } else if (skill.level > availableSkill.value)
                addSkill(
                    availableSkill.skill,
                    true
                )// We check if current known skill level is bigger than available skill level. If it's true, we override current skill with available skill.
        }
    }

    /**
     * Regive all skills which aren't saved to database, like Noble, Hero, Clan Skills.<br></br>
     * **Do not call this on enterworld or char load.**.
     */
    private fun regiveTemporarySkills() {
        // Add noble skills if noble.
        if (isNoble)
            setNoble(true, false)

        // Add Hero skills if hero.
        if (isHero)
            isHero = true

        // Add clan skills.
        if (clan != null) {
            clan!!.addSkillEffects(this)

            if (clan!!.level >= Config.MINIMUM_CLAN_LEVEL && isClanLeader)
                addSiegeSkills()
        }

        // Reload passive skills from armors / jewels / weapons
        inventory!!.reloadEquippedItems()

        // Add Death Penalty Buff Level
        restoreDeathPenaltyBuffLevel()
    }

    fun addSiegeSkills() {
        for (sk in SkillTable.getSiegeSkills(isNoble))
            addSkill(sk, false)
    }

    fun removeSiegeSkills() {
        for (sk in SkillTable.getSiegeSkills(isNoble))
            removeSkill(sk.id, false)
    }

    /**
     * Reduce the number of arrows owned by the Player and send InventoryUpdate or ItemList (to unequip if the last arrow was consummed).
     */
    override fun reduceArrowCount() // TODO: replace with a simple player.destroyItem...
    {
        val arrows = inventory!!.getPaperdollItem(Inventory.PAPERDOLL_LHAND) ?: return

        val iu = InventoryUpdate()

        if (arrows.count > 1) {
            synchronized(arrows) {
                arrows.changeCount(null, -1, this, null)
                arrows.lastChange = ItemInstance.ItemState.MODIFIED

                iu.addModifiedItem(arrows)

                // could do also without saving, but let's save approx 1 of 10
                if (Rnd[10] < 1)
                    arrows.updateDatabase()

                inventory.refreshWeight()
            }
        } else {
            iu.addRemovedItem(arrows)

            // Destroy entire item and save to database
            inventory.destroyItem("Consume", arrows, this, null)
        }
        sendPacket(iu)
    }

    /**
     * Check if the arrow item exists on inventory and is already slotted ; if not, equip it.
     */
    override fun checkAndEquipArrows(): Boolean {
        // Retrieve arrows instance on player inventory.
        val arrows = inventory!!.findArrowForBow(activeWeaponItem) ?: return false

        // Arrows are already equiped, don't bother.
        if (arrows.location === ItemInstance.ItemLocation.PAPERDOLL)
            return true

        // Equip arrows in left hand.
        inventory.setPaperdollItem(Inventory.PAPERDOLL_LHAND, arrows)

        // Send ItemList to this player to update left hand equipement
        sendPacket(ItemList(this, false))

        return true
    }

    /**
     * Disarm the player's weapon and shield.
     * @return true if successful, false otherwise.
     */
    fun disarmWeapons(): Boolean {
        // Don't allow disarming a cursed weapon
        if (isCursedWeaponEquipped)
            return false

        // Unequip the weapon
        val wpn = inventory!!.getPaperdollItem(Inventory.PAPERDOLL_RHAND)
        if (wpn != null) {
            val unequipped = inventory.unEquipItemInBodySlotAndRecord(wpn)
            val iu = InventoryUpdate()
            for (itm in unequipped)
                iu.addModifiedItem(itm)
            sendPacket(iu)

            abortAttack()
            broadcastUserInfo()

            // this can be 0 if the user pressed the right mousebutton twice very fast
            if (unequipped.size > 0) {
                val sm: SystemMessage
                if (unequipped[0].enchantLevel > 0)
                    sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED)
                        .addNumber(unequipped[0].enchantLevel).addItemName(unequipped[0])
                else
                    sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED).addItemName(unequipped[0])

                sendPacket(sm)
            }
        }

        // Unequip the shield
        val sld = inventory.getPaperdollItem(Inventory.PAPERDOLL_LHAND)
        if (sld != null) {
            val unequipped = inventory.unEquipItemInBodySlotAndRecord(sld)
            val iu = InventoryUpdate()
            for (itm in unequipped)
                iu.addModifiedItem(itm)
            sendPacket(iu)

            abortAttack()
            broadcastUserInfo()

            // this can be 0 if the user pressed the right mousebutton twice very fast
            if (unequipped.size > 0) {
                val sm: SystemMessage
                if (unequipped[0].enchantLevel > 0)
                    sm = SystemMessage.getSystemMessage(SystemMessageId.EQUIPMENT_S1_S2_REMOVED)
                        .addNumber(unequipped[0].enchantLevel).addItemName(unequipped[0])
                else
                    sm = SystemMessage.getSystemMessage(SystemMessageId.S1_DISARMED).addItemName(unequipped[0])

                sendPacket(sm)
            }
        }
        return true
    }

    fun mount(pet: Summon): Boolean {
        if (!disarmWeapons())
            return false

        setRunning()
        stopAllToggles()

        val mount = Ride(objectId, Ride.ACTION_MOUNT, pet.getTemplate().npcId)
        setMount(pet.npcId, pet.level, mount.mountType)

        petTemplate = pet.getTemplate() as PetTemplate
        petDataEntry = petTemplate!!.getPetDataEntry(pet.level)
        mountObjectId = pet.controlItemId

        startFeed(pet.npcId)
        broadcastPacket(mount)

        // Notify self and others about speed change
        broadcastUserInfo()

        pet.unSummon(this)
        return true
    }

    fun mount(npcId: Int, controlItemId: Int): Boolean {
        if (!disarmWeapons())
            return false

        setRunning()
        stopAllToggles()

        val mount = Ride(objectId, Ride.ACTION_MOUNT, npcId)
        if (setMount(npcId, level, mount.mountType)) {
            petTemplate = NpcData.getTemplate(npcId) as PetTemplate?
            petDataEntry = petTemplate!!.getPetDataEntry(level)
            mountObjectId = controlItemId

            broadcastPacket(mount)

            // Notify self and others about speed change
            broadcastUserInfo()

            startFeed(npcId)

            return true
        }
        return false
    }

    fun mountPlayer(summon: Summon?): Boolean {
        if (summon is Pet && summon.isMountable() && !isMounted && !isBetrayed) {
            if (isDead())
            // A strider cannot be ridden when dead.
            {
                sendPacket(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_DEAD)
                return false
            }

            if (summon.isDead())
            // A dead strider cannot be ridden.
            {
                sendPacket(SystemMessageId.DEAD_STRIDER_CANT_BE_RIDDEN)
                return false
            }

            if (summon.isInCombat || summon.isRooted)
            // A strider in battle cannot be ridden.
            {
                sendPacket(SystemMessageId.STRIDER_IN_BATLLE_CANT_BE_RIDDEN)
                return false
            }

            if (isInCombat)
            // A strider cannot be ridden while in battle
            {
                sendPacket(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE)
                return false
            }

            if (isSitting)
            // A strider can be ridden only when standing
            {
                sendPacket(SystemMessageId.STRIDER_CAN_BE_RIDDEN_ONLY_WHILE_STANDING)
                return false
            }

            if (isFishing)
            // You can't mount, dismount, break and drop items while fishing
            {
                sendPacket(SystemMessageId.CANNOT_DO_WHILE_FISHING_2)
                return false
            }

            if (isCursedWeaponEquipped)
            // You can't mount, dismount, break and drop items while weilding a cursed weapon
            {
                sendPacket(SystemMessageId.STRIDER_CANT_BE_RIDDEN_WHILE_IN_BATTLE)
                return false
            }

            if (!MathUtil.checkIfInRange(200, this, summon, true)) {
                sendPacket(SystemMessageId.TOO_FAR_AWAY_FROM_STRIDER_TO_MOUNT)
                return false
            }

            if (summon.checkHungryState()) {
                sendPacket(SystemMessageId.HUNGRY_STRIDER_NOT_MOUNT)
                return false
            }

            if (!summon.isDead() && !isMounted)
                mount(summon)
        } else if (isMounted) {
            if (mountType == 2 && isInsideZone(ZoneId.NO_LANDING)) {
                sendPacket(SystemMessageId.NO_DISMOUNT_HERE)
                return false
            }

            if (checkFoodState(petTemplate!!.hungryLimit)) {
                sendPacket(SystemMessageId.HUNGRY_STRIDER_NOT_MOUNT)
                return false
            }

            dismount()
        }
        return true
    }

    fun dismount(): Boolean {
        sendPacket(SetupGauge(GaugeColor.GREEN, 0))

        val petId = mountNpcId
        if (setMount(0, 0, 0)) {
            stopFeed()

            broadcastPacket(Ride(objectId, Ride.ACTION_DISMOUNT, 0))

            petTemplate = null
            petDataEntry = null
            mountObjectId = 0

            storePetFood(petId)

            // Notify self and others about speed change
            broadcastUserInfo()
            return true
        }
        return false
    }

    fun storePetFood(petId: Int) {
        if (_controlItemId != 0 && petId != 0) {
            try {
                L2DatabaseFactory.connection.use { con ->
                    con.prepareStatement("UPDATE pets SET fed=? WHERE item_obj_id = ?").use { ps ->
                        ps.setInt(1, currentFeed)
                        ps.setInt(2, _controlItemId)
                        ps.executeUpdate()

                        _controlItemId = 0
                    }
                }
            } catch (e: Exception) {
                WorldObject.LOGGER.error("Couldn't store pet food data for {}.", e, _controlItemId)
            }

        }
    }

    protected inner class FeedTask : Runnable {
        override fun run() {
            if (!isMounted) {
                stopFeed()
                return
            }

            // Eat or return to pet control item.
            if (currentFeed > feedConsume)
                currentFeed = currentFeed - feedConsume
            else {
                currentFeed = 0
                stopFeed()
                dismount()
                sendPacket(SystemMessageId.OUT_OF_FEED_MOUNT_CANCELED)
                return
            }

            var food = inventory!!.getItemByItemId(petTemplate!!.food1)
            if (food == null)
                food = inventory.getItemByItemId(petTemplate!!.food2)

            if (food != null && checkFoodState(petTemplate!!.autoFeedLimit)) {
                val handler = ItemHandler.getHandler(food.etcItem)
                if (handler != null) {
                    handler.useItem(this@Player, food, false)
                    sendPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.PET_TOOK_S1_BECAUSE_HE_WAS_HUNGRY).addItemName(
                            food
                        )
                    )
                }
            }
        }
    }

    @Synchronized
    protected fun startFeed(npcId: Int) {
        _canFeed = npcId > 0
        if (!isMounted)
            return

        if (pet != null) {
            currentFeed = (pet as Pet).currentFed
            _controlItemId = pet!!.controlItemId
            sendPacket(
                SetupGauge(
                    GaugeColor.GREEN,
                    currentFeed * 10000 / feedConsume,
                    petDataEntry!!.maxMeal * 10000 / feedConsume
                )
            )
            if (!isDead())
                _mountFeedTask = ThreadPool.scheduleAtFixedRate(FeedTask(), 10000, 10000)
        } else if (_canFeed) {
            currentFeed = petDataEntry!!.maxMeal
            sendPacket(
                SetupGauge(
                    GaugeColor.GREEN,
                    currentFeed * 10000 / feedConsume,
                    petDataEntry!!.maxMeal * 10000 / feedConsume
                )
            )
            if (!isDead())
                _mountFeedTask = ThreadPool.scheduleAtFixedRate(FeedTask(), 10000, 10000)
        }
    }

    @Synchronized
    protected fun stopFeed() {
        if (_mountFeedTask != null) {
            _mountFeedTask!!.cancel(false)
            _mountFeedTask = null
        }
    }

    /**
     * @param state : The state to check (can be autofeed, hungry or unsummon).
     * @return true if the limit is reached, false otherwise or if there is no need to feed.
     */
    fun checkFoodState(state: Double): Boolean {
        return if (_canFeed) currentFeed < petDataEntry!!.maxMeal * state else false
    }

    /**
     * Set the [AccessLevel] of this [Player].
     *
     *  * If invalid, set the default user access level 0.
     *  * If superior to 0, it means it's a special access.
     *
     * @param level : The level to set.
     */
    fun setAccessLevel(level: Int) {
        // Retrieve the AccessLevel. Even if not existing, it returns user level.
        var accessLevel = AdminData.getAccessLevel(level)
        if (accessLevel == null) {
            WorldObject.LOGGER.warn(
                "An invalid access level {} has been granted for {}, therefore it has been reset.",
                level,
                toString()
            )
            accessLevel = AdminData.getAccessLevel(0)
        }

        this.accessLevel = accessLevel!!

        if (level > 0) {
            // For level lower or equal to user, we don't apply AccessLevel name as title.
            title = accessLevel!!.name

            // We log master access.
            if (level == AdminData.masterAccessLevel)
                WorldObject.LOGGER.info("{} has logged in with Master access level.", name)
        }

        // We refresh GMList if the access level is GM.
        if (accessLevel!!.isGm) {
            // A little hack to avoid Enterworld config to be replaced.
            if (!AdminData.isRegisteredAsGM(this))
                AdminData.addGm(this, false)
        } else
            AdminData.deleteGm(this)

        appearance.nameColor = accessLevel.nameColor
        appearance.titleColor = accessLevel.titleColor
        broadcastUserInfo()

        PlayerInfoTable.updatePlayerData(this, true)
    }

    fun setAccountAccesslevel(level: Int) {
        LoginServerThread.sendAccessLevel(accountName!!, level)
    }

    /**
     * Update Stats of the Player client side by sending UserInfo/StatusUpdate to this Player and CharInfo/StatusUpdate to all Player in its _KnownPlayers (broadcast).
     * @param broadcastType
     */
    fun updateAndBroadcastStatus(broadcastType: Int) {
        refreshOverloaded()
        refreshExpertisePenalty()

        if (broadcastType == 1)
            sendPacket(UserInfo(this))
        else if (broadcastType == 2)
            broadcastUserInfo()
    }

    /**
     * Send StatusUpdate packet with Karma to the Player and all Player to inform (broadcast).
     */
    fun broadcastKarma() {
        val su = StatusUpdate(this)
        su.addAttribute(StatusUpdate.KARMA, karma)
        sendPacket(su)

        if (pet != null)
            sendPacket(RelationChanged(pet!!, getRelation(this), false))

        broadcastRelationsChanges()
    }

    /**
     * Set the online Flag to True or False and update the characters table of the database with online status and lastAccess (called when login and logout).
     * @param isOnline
     * @param updateInDb
     */
    fun setOnlineStatus(isOnline: Boolean, updateInDb: Boolean) {
        if (this.isOnline != isOnline)
            this.isOnline = isOnline

        // Update the characters table of the database with online status and lastAccess (called when login and logout)
        if (updateInDb)
            updateOnlineStatus()
    }

    /**
     * Update the characters table of the database with online status and lastAccess of this Player (called when login and logout).
     */
    fun updateOnlineStatus() {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement("UPDATE characters SET online=?, lastAccess=? WHERE obj_id=?").use { ps ->
                    ps.setInt(1, isOnlineInt)
                    ps.setLong(2, System.currentTimeMillis())
                    ps.setInt(3, objectId)
                    ps.execute()
                }
            }
        } catch (e: Exception) {
            WorldObject.LOGGER.error("Couldn't set player online status.", e)
        }

    }

    /**
     * Restores secondary data for the Player, based on the current class index.
     */
    private fun restoreCharData() {
        // Retrieve from the database all skills of this Player and add them to _skills.
        restoreSkills()

        // Retrieve from the database all macroses of this Player and add them to _macroses.
        macroses.restore()

        // Retrieve from the database all shortCuts of this Player and add them to _shortCuts.
        _shortCuts.restore()

        // Retrieve from the database all henna of this Player and add them to _henna.
        restoreHenna()

        // Retrieve from the database all recom data of this Player and add to _recomChars.
        restoreRecom()

        // Retrieve from the database the recipe book of this Player.
        if (!isSubClassActive)
            restoreRecipeBook()
    }

    /**
     * Store [Recipe] book data for this [Player], if he isn't on an active subclass.
     */
    private fun storeRecipeBook() {
        // If the player is on a sub-class don't even attempt to store a recipe book.
        if (isSubClassActive)
            return

        try {
            L2DatabaseFactory.connection.use { con ->
                var ps = con.prepareStatement("DELETE FROM character_recipebook WHERE charId=?")
                ps.setInt(1, objectId)
                ps.execute()
                ps.close()

                ps = con.prepareStatement("INSERT INTO character_recipebook (charId, recipeId) values(?,?)")

                for (recipe in commonRecipeBook) {
                    ps.setInt(1, objectId)
                    ps.setInt(2, recipe.id)
                    ps.addBatch()
                }

                for (recipe in dwarvenRecipeBook) {
                    ps.setInt(1, objectId)
                    ps.setInt(2, recipe.id)
                    ps.addBatch()
                }

                ps.executeBatch()
                ps.close()
            }
        } catch (e: Exception) {
            WorldObject.LOGGER.error("Couldn't store recipe book data.", e)
        }

    }

    /**
     * Restore [Recipe] book data for this [Player].
     */
    private fun restoreRecipeBook() {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement("SELECT recipeId FROM character_recipebook WHERE charId=?").use { ps ->
                    ps.setInt(1, objectId)

                    ps.executeQuery().use { rs ->
                        while (rs.next()) {
                            val recipe = RecipeData.getRecipeList(rs.getInt("recipeId"))
                            if (recipe!!.isDwarven)
                                registerDwarvenRecipeList(recipe)
                            else
                                registerCommonRecipeList(recipe)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            WorldObject.LOGGER.error("Couldn't restore recipe book data.", e)
        }

    }

    /**
     * Update Player stats in the characters table of the database.
     * @param storeActiveEffects
     */
    @Synchronized
    fun store(storeActiveEffects: Boolean) {
        // update client coords, if these look like true
        if (isInsideRadius(clientX, clientY, 1000, true))
            setXYZ(clientX, clientY, clientZ)

        storeCharBase()
        storeCharSub()
        storeEffect(storeActiveEffects)
        storeRecipeBook()

        memos.storeMe()
    }

    fun store() {
        store(true)
    }

    private fun storeCharBase() {
        // Get the exp, level, and sp of base class to store in base table
        val currentClassIndex = classIndex

        classIndex = 0

        val exp = stat.exp
        val level = stat.level.toInt()
        val sp = stat.sp

        classIndex = currentClassIndex

        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(UPDATE_CHARACTER).use { ps ->
                    ps.setInt(1, level)
                    ps.setInt(2, maxHp)
                    ps.setDouble(3, currentHp)
                    ps.setInt(4, maxCp)
                    ps.setDouble(5, currentCp)
                    ps.setInt(6, maxMp)
                    ps.setDouble(7, currentMp)
                    ps.setInt(8, appearance.face.toInt())
                    ps.setInt(9, appearance.hairStyle.toInt())
                    ps.setInt(10, appearance.hairColor.toInt())
                    ps.setInt(11, appearance.sex.ordinal)
                    ps.setInt(12, heading)

                    if (!isInObserverMode) {
                        ps.setInt(13, x)
                        ps.setInt(14, y)
                        ps.setInt(15, z)
                    } else {
                        ps.setInt(13, savedLocation.x)
                        ps.setInt(14, savedLocation.y)
                        ps.setInt(15, savedLocation.z)
                    }

                    ps.setLong(16, exp)
                    ps.setLong(17, expBeforeDeath)
                    ps.setInt(18, sp)
                    ps.setInt(19, karma)
                    ps.setInt(20, pvpKills)
                    ps.setInt(21, pkKills)
                    ps.setInt(22, clanId)
                    ps.setInt(23, race!!.ordinal)
                    ps.setInt(24, classId.id)
                    ps.setLong(25, deleteTimer)
                    ps.setString(26, title)
                    ps.setInt(27, accessLevel!!.level)
                    ps.setInt(28, isOnlineInt)
                    ps.setInt(29, if (isIn7sDungeon) 1 else 0)
                    ps.setInt(30, clanPrivileges)
                    ps.setInt(31, if (wantsPeace()) 1 else 0)
                    ps.setInt(32, baseClass)

                    var totalOnlineTime = _onlineTime
                    if (_onlineBeginTime > 0)
                        totalOnlineTime += (System.currentTimeMillis() - _onlineBeginTime) / 1000

                    ps.setLong(33, totalOnlineTime)
                    ps.setInt(34, punishLevel.value())
                    ps.setLong(35, punishTimer)
                    ps.setInt(36, if (isNoble) 1 else 0)
                    ps.setLong(37, powerGrade.toLong())
                    ps.setInt(38, pledgeType)
                    ps.setInt(39, lvlJoinedAcademy)
                    ps.setLong(40, apprentice.toLong())
                    ps.setLong(41, sponsor.toLong())
                    ps.setInt(42, allianceWithVarkaKetra)
                    ps.setLong(43, clanJoinExpiryTime)
                    ps.setLong(44, clanCreateExpiryTime)
                    ps.setString(45, name)
                    ps.setLong(46, deathPenaltyBuffLevel.toLong())
                    ps.setInt(47, objectId)

                    ps.execute()
                }
            }
        } catch (e: Exception) {
            WorldObject.LOGGER.error("Couldn't store player base data.", e)
        }

    }

    private fun storeCharSub() {
        if (subClasses.isEmpty())
            return

        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(UPDATE_CHAR_SUBCLASS).use { ps ->
                    for (subClass in subClasses.values) {
                        ps.setLong(1, subClass.exp)
                        ps.setInt(2, subClass.sp)
                        ps.setInt(3, subClass.level.toInt())
                        ps.setInt(4, subClass.classId)
                        ps.setInt(5, objectId)
                        ps.setInt(6, subClass.classIndex)
                        ps.addBatch()
                    }
                    ps.executeBatch()
                }
            }
        } catch (e: Exception) {
            WorldObject.LOGGER.error("Couldn't store subclass data.", e)
        }

    }

    private fun storeEffect(storeEffects: Boolean) {
        if (!Config.STORE_SKILL_COOLTIME)
            return

        try {
            L2DatabaseFactory.connection.use { con ->
                // Delete all current stored effects for char to avoid dupe
                con.prepareStatement(DELETE_SKILL_SAVE).use { ps ->
                    ps.setInt(1, objectId)
                    ps.setInt(2, classIndex)
                    ps.executeUpdate()
                }

                var buff_index = 0
                val storedSkills = ArrayList<Int>()

                con.prepareStatement(ADD_SKILL_SAVE).use { ps ->
                    // Store all effect data along with calulated remaining reuse delays for matching skills. 'restore_type'= 0.
                    if (storeEffects) {
                        for (effect in allEffects) {
                            if (effect == null)
                                continue

                            if (effect.effectType == L2EffectType.HEAL_OVER_TIME || effect.effectType == L2EffectType.COMBAT_POINT_HEAL_OVER_TIME) continue

                            val skill = effect.skill
                            if (storedSkills.contains(skill.reuseHashCode))
                                continue

                            storedSkills.add(skill.reuseHashCode)
                            if (!effect.isHerbEffect && effect.inUse && !skill.isToggle) {
                                ps.setInt(1, objectId)
                                ps.setInt(2, skill.id)
                                ps.setInt(3, skill.level)
                                ps.setInt(4, effect.count)
                                ps.setInt(5, effect.time)

                                val t = _reuseTimeStamps[skill.reuseHashCode]
                                if (t != null && t.hasNotPassed()) {
                                    ps.setLong(6, t.reuse)
                                    ps.setDouble(7, t.stamp.toDouble())
                                } else {
                                    ps.setLong(6, 0)
                                    ps.setDouble(7, 0.0)
                                }

                                ps.setInt(8, 0)
                                ps.setInt(9, classIndex)
                                ps.setInt(10, ++buff_index)
                                ps.addBatch() // Add SQL
                            }
                        }
                    }

                    // Store the reuse delays of remaining skills which lost effect but still under reuse delay. 'restore_type' 1.
                    for ((hash, t) in _reuseTimeStamps) {
                        if (storedSkills.contains(hash))
                            continue

                        if (t != null && t.hasNotPassed()) {
                            storedSkills.add(hash)

                            ps.setInt(1, objectId)
                            ps.setInt(2, t.id)
                            ps.setInt(3, t.value)
                            ps.setInt(4, -1)
                            ps.setInt(5, -1)
                            ps.setLong(6, t.reuse)
                            ps.setDouble(7, t.stamp.toDouble())
                            ps.setInt(8, 1)
                            ps.setInt(9, classIndex)
                            ps.setInt(10, ++buff_index)
                            ps.addBatch() // Add SQL
                        }
                    }

                    ps.executeBatch() // Execute SQLs
                }
            }
        } catch (e: Exception) {
            WorldObject.LOGGER.error("Couldn't store player effects.", e)
        }

    }

    /**
     * Add a [L2Skill] and its Func objects to the calculator set of the [Player].<BR></BR>
     *
     *  * Replace or add oldSkill by newSkill (only if oldSkill is different than newSkill)
     *  * If an old skill has been replaced, remove all its Func objects of Creature calculator set
     *  * Add Func objects of newSkill to the calculator set of the Creature
     *
     * @param newSkill : The skill to add.
     * @param store : If true, we save the skill on database.
     * @return true if the skill has been successfully added.
     */
    fun addSkill(newSkill: L2Skill?, store: Boolean): Boolean {
        // New skill is null, abort.
        if (newSkill == null)
            return false

        // Search the old skill. We test if it's different than the new one. If yes, we abort the operation.
        val oldSkill = skills[newSkill.id]
        if (oldSkill != null && oldSkill == newSkill)
            return false

        // The 2 skills were different (or old wasn't existing). We can refresh the map.
        skills[newSkill.id] = newSkill

        // If an old skill has been replaced, remove all its Func objects
        if (oldSkill != null) {
            // if skill came with another one, we should delete the other one too.
            if (oldSkill.triggerAnotherSkill())
                removeSkill(oldSkill.triggeredId, false)

            removeStatsByOwner(oldSkill)
        }

        // Add Func objects of newSkill to the calculator set of the Creature
        addStatFuncs(newSkill.getStatFuncs(this))

        // Test and delete chance skill if found.
        if (oldSkill != null && chanceSkills != null)
            removeChanceSkill(oldSkill.id)

        // If new skill got a chance, trigger it.
        if (newSkill.isChance)
            addChanceTrigger(newSkill)

        // Add or update the skill in the database.
        if (store)
            storeSkill(newSkill, -1)

        return true
    }

    /**
     * Remove a [L2Skill] from this [Player]. If parameter store is true, we also remove it from database and update shortcuts.
     * @param skillId : The skill identifier to remove.
     * @param store : If true, we delete the skill from database.
     * @param removeEffect : If true, we remove the associated effect if existing.
     * @return the L2Skill removed or null if it couldn't be removed.
     */
    @JvmOverloads
    fun removeSkill(skillId: Int, store: Boolean, removeEffect: Boolean = true): L2Skill? {
        // Remove the skill from the Creature _skills
        val oldSkill = skills.remove(skillId) ?: return null

        // this is just a fail-safe againts buggers and gm dummies...
        if (oldSkill.triggerAnotherSkill() && oldSkill.triggeredId > 0)
            removeSkill(oldSkill.triggeredId, false)

        // Stop casting if this skill is used right now
        if (lastSkillCast != null && isCastingNow && skillId == lastSkillCast!!.id)
            abortCast()

        if (lastSimultaneousSkillCast != null && isCastingSimultaneouslyNow && skillId == lastSimultaneousSkillCast!!.id)
            abortCast()

        // Remove all its Func objects from the Creature calculator set
        if (removeEffect) {
            removeStatsByOwner(oldSkill)
            stopSkillEffects(skillId)
        }

        if (oldSkill.isChance && chanceSkills != null)
            removeChanceSkill(skillId)

        if (store) {
            try {
                L2DatabaseFactory.connection.use { con ->
                    con.prepareStatement(DELETE_SKILL_FROM_CHAR).use { ps ->
                        ps.setInt(1, skillId)
                        ps.setInt(2, objectId)
                        ps.setInt(3, classIndex)
                        ps.execute()
                    }
                }
            } catch (e: Exception) {
                WorldObject.LOGGER.error("Couldn't delete player skill.", e)
            }

            // Don't busy with shortcuts if skill was a passive skill.
            if (!oldSkill.isPassive) {
                for (sc in allShortCuts) {
                    if (sc != null && sc.id == skillId && sc.type == L2ShortCut.TYPE_SKILL)
                        deleteShortCut(sc.slot, sc.page)
                }
            }
        }
        return oldSkill
    }

    /**
     * Insert or update a [Player] skill in the database.<br></br>
     * If newClassIndex > -1, the skill will be stored with that class index, not the current one.
     * @param skill : The skill to add or update (if updated, only the level is refreshed).
     * @param classIndex : The current class index to set, or current if none is found.
     */
    private fun storeSkill(skill: L2Skill?, classIndex: Int) {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(ADD_OR_UPDATE_SKILL).use { ps ->
                    ps.setInt(1, objectId)
                    ps.setInt(2, skill!!.id)
                    ps.setInt(3, skill.level)
                    ps.setInt(4, if (classIndex > -1) classIndex else this.classIndex)
                    ps.executeUpdate()
                }
            }
        } catch (e: Exception) {
            WorldObject.LOGGER.error("Couldn't store player skill.", e)
        }

    }

    /**
     * Restore all skills from database for this [Player] and feed getSkills().
     */
    private fun restoreSkills() {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(RESTORE_SKILLS_FOR_CHAR).use { ps ->
                    ps.setInt(1, objectId)
                    ps.setInt(2, classIndex)

                    val rs = ps.executeQuery()
                    while (rs.next())
                        addSkill(SkillTable.getInfo(rs.getInt("skill_id"), rs.getInt("skill_level")), false)

                    rs.close()
                }
            }
        } catch (e: Exception) {
            WorldObject.LOGGER.error("Couldn't restore player skills.", e)
        }

    }

    /**
     * Retrieve from the database all skill effects of this Player and add them to the player.
     */
    fun restoreEffects() {
        try {
            L2DatabaseFactory.connection.use { con ->
                var statement = con.prepareStatement(RESTORE_SKILL_SAVE)
                statement.setInt(1, objectId)
                statement.setInt(2, classIndex)
                val rset = statement.executeQuery()

                while (rset.next()) {
                    val effectCount = rset.getInt("effect_count")
                    val effectCurTime = rset.getInt("effect_cur_time")
                    val reuseDelay = rset.getLong("reuse_delay")
                    val systime = rset.getLong("systime")
                    val restoreType = rset.getInt("restore_type")

                    val skill = SkillTable.getInfo(rset.getInt("skill_id"), rset.getInt("skill_level")) ?: continue

                    val remainingTime = systime - System.currentTimeMillis()
                    if (remainingTime > 10) {
                        disableSkill(skill, remainingTime)
                        addTimeStamp(skill, reuseDelay, systime)
                    }

                    /**
                     * Restore Type 1 The remaning skills lost effect upon logout but were still under a high reuse delay.
                     */
                    if (restoreType > 0)
                        continue

                    /**
                     * Restore Type 0 These skills were still in effect on the character upon logout. Some of which were self casted and might still have a long reuse delay which also is restored.
                     */
                    if (skill.hasEffects()) {
                        val env = Env()
                        env.character = this
                        env.target = this
                        env.skill = skill

                        for (et in skill.effectTemplates) {
                            val ef = et.getEffect(env)
                            if (ef != null) {
                                ef.count = effectCount
                                ef.setFirstTime(effectCurTime)
                                ef.scheduleEffect()
                            }
                        }
                    }
                }

                rset.close()
                statement.close()

                statement = con.prepareStatement(DELETE_SKILL_SAVE)
                statement.setInt(1, objectId)
                statement.setInt(2, classIndex)
                statement.executeUpdate()
                statement.close()
            }
        } catch (e: Exception) {
            WorldObject.LOGGER.error("Couldn't restore effects.", e)
        }

    }

    /**
     * Retrieve from the database all Henna of this Player, add them to _henna and calculate stats of the Player.
     */
    private fun restoreHenna() {
        // Initialize the array.
        for (i in 0..2)
            _henna[i] = null

        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(RESTORE_CHAR_HENNAS).use { ps ->
                    ps.setInt(1, objectId)
                    ps.setInt(2, classIndex)

                    ps.executeQuery().use { rs ->
                        while (rs.next()) {
                            val slot = rs.getInt("slot")
                            if (slot < 1 || slot > 3)
                                continue

                            val symbolId = rs.getInt("symbol_id")
                            if (symbolId != 0) {
                                val henna = HennaData.getHenna(symbolId)
                                if (henna != null)
                                    _henna[slot - 1] = henna
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            WorldObject.LOGGER.error("Couldn't restore henna.", e)
        }

        // Calculate Henna modifiers of this Player
        recalcHennaStats()
    }

    /**
     * Retrieve from the database all Recommendation data of this Player, add to _recomChars and calculate stats of the Player.
     */
    private fun restoreRecom() {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(RESTORE_CHAR_RECOMS).use { ps ->
                    ps.setInt(1, objectId)

                    ps.executeQuery().use { rset ->
                        while (rset.next())
                            _recomChars.add(rset.getInt("target_id"))
                    }
                }
            }
        } catch (e: Exception) {
            WorldObject.LOGGER.error("Couldn't restore recommendations.", e)
        }

    }

    /**
     * Remove an [Henna] of this [Player], save it to the database and send packets to refresh client.
     * @param slot : The slot number to make checks on.
     * @return true if successful.
     */
    fun removeHenna(slot: Int): Boolean {
        var slot = slot
        if (slot < 1 || slot > 3)
            return false

        slot--

        if (_henna[slot] == null)
            return false

        val henna = _henna[slot]
        _henna[slot] = null

        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(DELETE_CHAR_HENNA).use { ps ->
                    ps.setInt(1, objectId)
                    ps.setInt(2, slot + 1)
                    ps.setInt(3, classIndex)
                    ps.execute()
                }
            }
        } catch (e: Exception) {
            WorldObject.LOGGER.error("Couldn't remove henna.", e)
        }

        // Calculate Henna modifiers of this Player
        recalcHennaStats()

        // Send HennaInfo packet to this Player
        sendPacket(HennaInfo(this))

        // Send UserInfo packet to this Player
        sendPacket(UserInfo(this))

        reduceAdena("Henna", henna!!.price / 5, this, false)

        // Add the recovered dyes to the player's inventory and notify them.
        addItem("Henna", henna.dyeId, Henna.requiredDyeAmount / 2, this, true)
        sendPacket(SystemMessageId.SYMBOL_DELETED)
        return true
    }

    /**
     * Add a [Henna] to this [Player], save it to the database and send packets to refresh client.
     * @param henna : The Henna template to add.
     */
    fun addHenna(henna: Henna) {
        for (i in 0..2) {
            if (_henna[i] == null) {
                _henna[i] = henna

                // Calculate Henna modifiers of this Player
                recalcHennaStats()

                try {
                    L2DatabaseFactory.connection.use { con ->
                        con.prepareStatement(ADD_CHAR_HENNA).use { ps ->
                            ps.setInt(1, objectId)
                            ps.setInt(2, henna.symbolId)
                            ps.setInt(3, i + 1)
                            ps.setInt(4, classIndex)
                            ps.execute()
                        }
                    }
                } catch (e: Exception) {
                    WorldObject.LOGGER.error("Couldn't save henna.", e)
                }

                sendPacket(HennaInfo(this))
                sendPacket(UserInfo(this))
                sendPacket(SystemMessageId.SYMBOL_ADDED)
                return
            }
        }
    }

    /**
     * Recalculate [Henna] modifiers of this [Player].
     */
    private fun recalcHennaStats() {
        hennaStatINT = 0
        hennaStatSTR = 0
        hennaStatCON = 0
        hennaStatMEN = 0
        hennaStatWIT = 0
        hennaStatDEX = 0

        for (i in 0..2) {
            if (_henna[i] == null)
                continue

            hennaStatINT += _henna[i]!!.int
            hennaStatSTR += _henna[i]!!.str
            hennaStatMEN += _henna[i]!!.men
            hennaStatCON += _henna[i]!!.con
            hennaStatWIT += _henna[i]!!.wit
            hennaStatDEX += _henna[i]!!.dex
        }

        if (hennaStatINT > 5)
            hennaStatINT = 5

        if (hennaStatSTR > 5)
            hennaStatSTR = 5

        if (hennaStatMEN > 5)
            hennaStatMEN = 5

        if (hennaStatCON > 5)
            hennaStatCON = 5

        if (hennaStatWIT > 5)
            hennaStatWIT = 5

        if (hennaStatDEX > 5)
            hennaStatDEX = 5
    }

    /**
     * @param slot A slot to check.
     * @return the [Henna] of this [Player] corresponding to the selected slot.
     */
    fun getHenna(slot: Int): Henna? {
        return if (slot < 1 || slot > 3) null else _henna[slot - 1]

    }

    /**
     * Return True if the Player is autoAttackable.
     *
     *  * Check if the attacker isn't the Player Pet
     *  * Check if the attacker is L2MonsterInstance
     *  * If the attacker is a Player, check if it is not in the same party
     *  * Check if the Player has Karma
     *  * If the attacker is a Player, check if it is not in the same siege clan (Attacker, Defender)
     *
     */
    override fun isAutoAttackable(attacker: Creature): Boolean {
        // Check if the attacker isn't the Player Pet
        if (attacker === this || attacker === pet)
            return false

        // Check if the attacker is a L2MonsterInstance
        if (attacker is Monster)
            return true

        // Check if the attacker is not in the same party
        if (party != null && party!!.containsPlayer(attacker))
            return false

        // Check if the attacker is a L2Playable
        if (attacker is Playable) {
            if (isInsideZone(ZoneId.PEACE))
                return false

            // Get Player
            val cha = attacker.actingPlayer

            // Check if the attacker is in olympiad and olympiad start
            if (attacker is Player && cha!!.isInOlympiadMode) {
                return if (isInOlympiadMode && isOlympiadStart && cha.olympiadGameId == olympiadGameId) true else false

            }

            // is AutoAttackable if both players are in the same duel and the duel is still going on
            if (duelState === DuelState.DUELLING && duelId == cha!!.duelId)
                return true

            if (clan != null) {
                val siege = CastleManager.getActiveSiege(this)
                if (siege != null) {
                    // Check if a siege is in progress and if attacker and the Player aren't in the Defender clan
                    if (siege.checkSides(cha!!.clan, SiegeSide.DEFENDER, SiegeSide.OWNER) && siege.checkSides(
                            clan,
                            SiegeSide.DEFENDER,
                            SiegeSide.OWNER
                        )
                    )
                        return false

                    // Check if a siege is in progress and if attacker and the Player aren't in the Attacker clan
                    if (siege.checkSide(cha.clan, SiegeSide.ATTACKER) && siege.checkSide(clan, SiegeSide.ATTACKER))
                        return false
                }

                // Check if clan is at war
                if (clan!!.isAtWarWith(cha!!.clanId) && !wantsPeace() && !cha.wantsPeace() && !isAcademyMember)
                    return true
            }

            // Check if the Player is in an arena.
            if (isInArena && attacker.isInArena)
                return true

            // Check if the attacker is not in the same ally.
            if (allyId != 0 && allyId == cha!!.allyId)
                return false

            // Check if the attacker is not in the same clan.
            if (clan != null && clan!!.isMember(cha!!.objectId))
                return false

            // Now check again if the Player is in pvp zone (as arenas check was made before, it ends with sieges).
            if (isInsideZone(ZoneId.PVP) && attacker.isInsideZone(ZoneId.PVP))
                return true
        } else if (attacker is SiegeGuard) {
            if (clan != null) {
                val siege = CastleManager.getActiveSiege(this)
                return siege != null && siege.checkSide(clan, SiegeSide.ATTACKER)
            }
        }

        // Check if the Player has Karma
        return if (karma > 0 || pvpFlag > 0) true else false

    }

    /**
     * Check if the active L2Skill can be casted.
     *
     *  * Check if the skill isn't toggle and is offensive
     *  * Check if the target is in the skill cast range
     *  * Check if the skill is Spoil type and if the target isn't already spoiled
     *  * Check if the caster owns enought consummed Item, enough HP and MP to cast the skill
     *  * Check if the caster isn't sitting
     *  * Check if all skills are enabled and this skill is enabled
     *  * Check if the caster own the weapon needed
     *  * Check if the skill is active
     *  * Check if all casting conditions are completed
     *  * Notify the AI with CAST and target
     *
     * @param skill The L2Skill to use
     * @param forceUse used to force ATTACK on players
     * @param dontMove used to prevent movement, if not in range
     */
    override fun useMagic(skill: L2Skill, forceUse: Boolean, dontMove: Boolean): Boolean {
        // Check if the skill is active
        if (skill.isPassive) {
            sendPacket(ActionFailed.STATIC_PACKET)
            return false
        }

        // Check if this skill is enabled (ex : reuse time)
        if (isSkillDisabled(skill)) {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_PREPARED_FOR_REUSE).addSkillName(skill))
            return false
        }

        // Cancels the use of skills when player uses a cursed weapon or is flying.
        if (isCursedWeaponEquipped && !skill.isDemonicSkill // If CW, allow ONLY demonic skills.

            || mountType == 1 && !skill.isStriderSkill // If mounted, allow ONLY Strider skills.

            || mountType == 2 && !skill.isFlyingSkill
        )
        // If flying, allow ONLY Wyvern skills.
        {
            sendPacket(ActionFailed.STATIC_PACKET)
            return false
        }

        // Players wearing Formal Wear cannot use skills.
        val formal = inventory!!.getPaperdollItem(Inventory.PAPERDOLL_CHEST)
        if (formal != null && formal.item.bodyPart == Item.SLOT_ALLDRESS) {
            sendPacket(SystemMessageId.CANNOT_USE_ITEMS_SKILLS_WITH_FORMALWEAR)
            sendPacket(ActionFailed.STATIC_PACKET)
            return false
        }

        // ************************************* Check Casting in Progress *******************************************

        // If a skill is currently being used, queue this one if this is not the same
        if (isCastingNow) {
            // Check if new skill different from current skill in progress ; queue it in the player _queuedSkill
            if (currentSkill!!.skill != null && skill.id != currentSkill.skillId)
                setQueuedSkill(skill, forceUse, dontMove)

            sendPacket(ActionFailed.STATIC_PACKET)
            return false
        }

        isCastingNow = true

        // Set the player _currentSkill.
        setCurrentSkill(skill, forceUse, dontMove)

        // Wipe queued skill.
        if (queuedSkill.skill != null)
            setQueuedSkill(null, false, false)

        if (!checkUseMagicConditions(skill, forceUse, dontMove)) {
            isCastingNow = false
            return false
        }

        // Check if the target is correct and Notify the AI with CAST and target
        var target: WorldObject? = null

        when (skill.targetType) {
            L2Skill.SkillTargetType.TARGET_AURA, L2Skill.SkillTargetType.TARGET_FRONT_AURA, L2Skill.SkillTargetType.TARGET_BEHIND_AURA, L2Skill.SkillTargetType.TARGET_GROUND, L2Skill.SkillTargetType.TARGET_SELF, L2Skill.SkillTargetType.TARGET_CORPSE_ALLY, L2Skill.SkillTargetType.TARGET_AURA_UNDEAD -> target =
                this

            else // Get the first target of the list
            -> target = skill.getFirstOfTargetList(this)
        }

        // Notify the AI with CAST and target
        ai!!.setIntention(CtrlIntention.CAST, skill, target)
        return true
    }

    private fun checkUseMagicConditions(skill: L2Skill, forceUse: Boolean, dontMove: Boolean): Boolean {
        // ************************************* Check Player State *******************************************

        // Check if the player is dead or out of control.
        if (isDead() || isOutOfControl) {
            sendPacket(ActionFailed.STATIC_PACKET)
            return false
        }

        val sklType = skill.skillType

        if (isFishing && sklType !== L2SkillType.PUMPING && sklType !== L2SkillType.REELING && sklType !== L2SkillType.FISHING) {
            // Only fishing skills are available
            sendPacket(SystemMessageId.ONLY_FISHING_SKILLS_NOW)
            return false
        }

        if (isInObserverMode) {
            sendPacket(SystemMessageId.OBSERVERS_CANNOT_PARTICIPATE)
            abortCast()
            sendPacket(ActionFailed.STATIC_PACKET)
            return false
        }

        // Check if the caster is sitted.
        if (isSitting) {
            // Send a System Message to the caster
            sendPacket(SystemMessageId.CANT_MOVE_SITTING)

            // Send ActionFailed to the Player
            sendPacket(ActionFailed.STATIC_PACKET)
            return false
        }

        // Check if the skill type is TOGGLE
        if (skill.isToggle) {
            // Get effects of the skill
            val effect = getFirstEffect(skill.id)

            if (effect != null) {
                // If the toggle is different of FakeDeath, you can de-activate it clicking on it.
                if (skill.id != 60)
                    effect.exit()

                // Send ActionFailed to the Player
                sendPacket(ActionFailed.STATIC_PACKET)
                return false
            }
        }

        // Check if the player uses "Fake Death" skill
        if (isFakeDeath) {
            // Send ActionFailed to the Player
            sendPacket(ActionFailed.STATIC_PACKET)
            return false
        }

        // ************************************* Check Target *******************************************
        // Create and set a WorldObject containing the target of the skill
        var target: WorldObject? = null
        val sklTargetType = skill.targetType
        val worldPosition = currentSkillWorldPosition

        if (sklTargetType == L2Skill.SkillTargetType.TARGET_GROUND && worldPosition == null) {
            sendPacket(ActionFailed.STATIC_PACKET)
            return false
        }

        when (sklTargetType) {
            // Target the player if skill type is AURA, PARTY, CLAN or SELF
            L2Skill.SkillTargetType.TARGET_AURA, L2Skill.SkillTargetType.TARGET_FRONT_AURA, L2Skill.SkillTargetType.TARGET_BEHIND_AURA, L2Skill.SkillTargetType.TARGET_AURA_UNDEAD, L2Skill.SkillTargetType.TARGET_PARTY, L2Skill.SkillTargetType.TARGET_ALLY, L2Skill.SkillTargetType.TARGET_CLAN, L2Skill.SkillTargetType.TARGET_GROUND, L2Skill.SkillTargetType.TARGET_SELF, L2Skill.SkillTargetType.TARGET_CORPSE_ALLY, L2Skill.SkillTargetType.TARGET_AREA_SUMMON -> target =
                this
            L2Skill.SkillTargetType.TARGET_PET, L2Skill.SkillTargetType.TARGET_SUMMON -> target = pet
            else -> target = target
        }

        // Check the validity of the target
        if (target == null) {
            sendPacket(ActionFailed.STATIC_PACKET)
            return false
        }

        if (target is Door) {
            if (!target.isAutoAttackable(this) // Siege doors only hittable during siege
                || target.isUnlockable && skill.skillType !== L2SkillType.UNLOCK
            )
            // unlockable doors
            {
                sendPacket(SystemMessageId.INCORRECT_TARGET)
                sendPacket(ActionFailed.STATIC_PACKET)
                return false
            }
        }

        // Are the target and the player in the same duel?
        if (isInDuel) {
            if (target is Playable) {
                // Get Player
                val cha = target.actingPlayer
                if (cha!!.duelId != duelId) {
                    sendPacket(SystemMessageId.INCORRECT_TARGET)
                    sendPacket(ActionFailed.STATIC_PACKET)
                    return false
                }
            }
        }

        // ************************************* Check skill availability *******************************************

        // Siege summon checks. Both checks send a message to the player if it return false.
        if (skill.isSiegeSummonSkill) {
            val siege = CastleManager.getActiveSiege(this)
            if (siege == null || !siege.checkSide(
                    clan,
                    SiegeSide.ATTACKER
                ) || isInSiege && isInsideZone(ZoneId.CASTLE)
            ) {
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.NOT_CALL_PET_FROM_THIS_LOCATION))
                return false
            }

            if (SevenSigns.isSealValidationPeriod && SevenSigns.getSealOwner(SealType.STRIFE) === CabalType.DAWN && SevenSigns.getPlayerCabal(
                    objectId
                ) === CabalType.DUSK
            ) {
                sendPacket(SystemMessageId.SEAL_OF_STRIFE_FORBIDS_SUMMONING)
                return false
            }
        }

        // ************************************* Check casting conditions *******************************************

        // Check if all casting conditions are completed
        if (!skill.checkCondition(this, target, false)) {
            // Send ActionFailed to the Player
            sendPacket(ActionFailed.STATIC_PACKET)
            return false
        }

        // ************************************* Check Skill Type *******************************************

        // Check if this is offensive magic skill
        if (skill.isOffensive) {
            if (Creature.Companion.isInsidePeaceZone(this, target)) {
                // If Creature or target is in a peace zone, send a system message TARGET_IN_PEACEZONE ActionFailed
                sendPacket(SystemMessageId.TARGET_IN_PEACEZONE)
                sendPacket(ActionFailed.STATIC_PACKET)
                return false
            }

            if (isInOlympiadMode && !isOlympiadStart) {
                // if Player is in Olympia and the match isn't already start, send ActionFailed
                sendPacket(ActionFailed.STATIC_PACKET)
                return false
            }

            // Check if the target is attackable
            if (!target.isAttackable && !accessLevel!!.allowPeaceAttack) {
                // If target is not attackable, send ActionFailed
                sendPacket(ActionFailed.STATIC_PACKET)
                return false
            }

            // Check if a Forced ATTACK is in progress on non-attackable target
            if (!target.isAutoAttackable(this) && !forceUse) {
                when (sklTargetType) {
                    L2Skill.SkillTargetType.TARGET_AURA, L2Skill.SkillTargetType.TARGET_FRONT_AURA, L2Skill.SkillTargetType.TARGET_BEHIND_AURA, L2Skill.SkillTargetType.TARGET_AURA_UNDEAD, L2Skill.SkillTargetType.TARGET_CLAN, L2Skill.SkillTargetType.TARGET_ALLY, L2Skill.SkillTargetType.TARGET_PARTY, L2Skill.SkillTargetType.TARGET_SELF, L2Skill.SkillTargetType.TARGET_GROUND, L2Skill.SkillTargetType.TARGET_CORPSE_ALLY, L2Skill.SkillTargetType.TARGET_AREA_SUMMON -> {
                    }
                    else // Send ActionFailed to the Player
                    -> {
                        sendPacket(ActionFailed.STATIC_PACKET)
                        return false
                    }
                }
            }

            // Check if the target is in the skill cast range
            if (dontMove) {
                // Calculate the distance between the Player and the target
                if (sklTargetType == L2Skill.SkillTargetType.TARGET_GROUND) {
                    if (!isInsideRadius(
                            worldPosition!!.x,
                            worldPosition.y,
                            worldPosition.z,
                            (skill.castRange + collisionRadius).toInt(),
                            false,
                            false
                        )
                    ) {
                        // Send a System Message to the caster
                        sendPacket(SystemMessageId.TARGET_TOO_FAR)

                        // Send ActionFailed to the Player
                        sendPacket(ActionFailed.STATIC_PACKET)
                        return false
                    }
                } else if (skill.castRange > 0 && !isInsideRadius(
                        target,
                        (skill.castRange + collisionRadius).toInt(),
                        false,
                        false
                    )
                ) {
                    // Send a System Message to the caster
                    sendPacket(SystemMessageId.TARGET_TOO_FAR)

                    // Send ActionFailed to the Player
                    sendPacket(ActionFailed.STATIC_PACKET)
                    return false
                }
            }
        }

        // Check if the skill is defensive
        if (!skill.isOffensive && target is Monster && !forceUse) {
            // check if the target is a monster and if force attack is set.. if not then we don't want to cast.
            when (sklTargetType) {
                L2Skill.SkillTargetType.TARGET_PET, L2Skill.SkillTargetType.TARGET_SUMMON, L2Skill.SkillTargetType.TARGET_AURA, L2Skill.SkillTargetType.TARGET_FRONT_AURA, L2Skill.SkillTargetType.TARGET_BEHIND_AURA, L2Skill.SkillTargetType.TARGET_AURA_UNDEAD, L2Skill.SkillTargetType.TARGET_CLAN, L2Skill.SkillTargetType.TARGET_SELF, L2Skill.SkillTargetType.TARGET_CORPSE_ALLY, L2Skill.SkillTargetType.TARGET_PARTY, L2Skill.SkillTargetType.TARGET_ALLY, L2Skill.SkillTargetType.TARGET_CORPSE_MOB, L2Skill.SkillTargetType.TARGET_AREA_CORPSE_MOB, L2Skill.SkillTargetType.TARGET_GROUND -> {
                }
                else -> {
                    when (sklType) {
                        L2SkillType.BEAST_FEED, L2SkillType.DELUXE_KEY_UNLOCK, L2SkillType.UNLOCK -> {
                        }
                        else -> {
                            sendPacket(ActionFailed.STATIC_PACKET)
                            return false
                        }
                    }
                }
            }
        }

        // Check if the skill is Spoil type and if the target isn't already spoiled
        if (sklType === L2SkillType.SPOIL) {
            if (target !is Monster) {
                // Send a System Message to the Player
                sendPacket(SystemMessageId.INCORRECT_TARGET)

                // Send ActionFailed to the Player
                sendPacket(ActionFailed.STATIC_PACKET)
                return false
            }
        }

        // Check if the skill is Sweep type and if conditions not apply
        if (sklType === L2SkillType.SWEEP && target is Attackable) {
            if (target.isDead()) {
                val spoilerId = target.spoilerId
                if (spoilerId == 0) {
                    // Send a System Message to the Player
                    sendPacket(SystemMessageId.SWEEPER_FAILED_TARGET_NOT_SPOILED)

                    // Send ActionFailed to the Player
                    sendPacket(ActionFailed.STATIC_PACKET)
                    return false
                }

                if (!isLooterOrInLooterParty(spoilerId)) {
                    // Send a System Message to the Player
                    sendPacket(SystemMessageId.SWEEP_NOT_ALLOWED)

                    // Send ActionFailed to the Player
                    sendPacket(ActionFailed.STATIC_PACKET)
                    return false
                }
            }
        }

        // Check if the skill is Drain Soul (Soul Crystals) and if the target is a MOB
        if (sklType === L2SkillType.DRAIN_SOUL) {
            if (target !is Monster) {
                // Send a System Message to the Player
                sendPacket(SystemMessageId.INCORRECT_TARGET)

                // Send ActionFailed to the Player
                sendPacket(ActionFailed.STATIC_PACKET)
                return false
            }
        }

        // Check if this is a Pvp skill and target isn't a non-flagged/non-karma player
        when (sklTargetType) {
            L2Skill.SkillTargetType.TARGET_PARTY, L2Skill.SkillTargetType.TARGET_ALLY // For such skills, checkPvpSkill() is called from L2Skill.getTargetList()
                , L2Skill.SkillTargetType.TARGET_CLAN // For such skills, checkPvpSkill() is called from L2Skill.getTargetList()
                , L2Skill.SkillTargetType.TARGET_AURA, L2Skill.SkillTargetType.TARGET_FRONT_AURA, L2Skill.SkillTargetType.TARGET_BEHIND_AURA, L2Skill.SkillTargetType.TARGET_AURA_UNDEAD, L2Skill.SkillTargetType.TARGET_GROUND, L2Skill.SkillTargetType.TARGET_SELF, L2Skill.SkillTargetType.TARGET_CORPSE_ALLY, L2Skill.SkillTargetType.TARGET_AREA_SUMMON -> {
            }
            else -> if (!checkPvpSkill(target, skill) && !accessLevel!!.allowPeaceAttack) {
                // Send a System Message to the Player
                sendPacket(SystemMessageId.TARGET_IS_INCORRECT)

                // Send ActionFailed to the Player
                sendPacket(ActionFailed.STATIC_PACKET)
                return false
            }
        }

        if (sklTargetType == L2Skill.SkillTargetType.TARGET_HOLY && !checkIfOkToCastSealOfRule(
                CastleManager.getCastle(
                    this
                ), false, skill, target
            ) || sklType === L2SkillType.SIEGEFLAG && !L2SkillSiegeFlag.checkIfOkToPlaceFlag(
                this,
                false
            ) || sklType === L2SkillType.STRSIEGEASSAULT && !checkIfOkToUseStriderSiegeAssault(skill) || sklType === L2SkillType.SUMMON_FRIEND && !(checkSummonerStatus() && checkSummonTargetStatus(
                target
            ))
        ) {
            sendPacket(ActionFailed.STATIC_PACKET)
            abortCast()
            return false
        }

        // GeoData Los Check here
        if (skill.castRange > 0) {
            if (sklTargetType == L2Skill.SkillTargetType.TARGET_GROUND) {
                if (!GeoEngine.canSeeTarget(this, worldPosition!!)) {
                    sendPacket(SystemMessageId.CANT_SEE_TARGET)
                    sendPacket(ActionFailed.STATIC_PACKET)
                    return false
                }
            } else if (!GeoEngine.canSeeTarget(this, target)) {
                sendPacket(SystemMessageId.CANT_SEE_TARGET)
                sendPacket(ActionFailed.STATIC_PACKET)
                return false
            }
        }
        // finally, after passing all conditions
        return true
    }

    fun checkIfOkToUseStriderSiegeAssault(skill: L2Skill): Boolean {
        val siege = CastleManager.getActiveSiege(this)

        val sm: SystemMessage

        if (!isRiding)
            sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill)
        else if (target !is Door)
            sm = SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET)
        else if (siege == null || !siege.checkSide(clan, SiegeSide.ATTACKER))
            sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill)
        else
            return true

        sendPacket(sm)
        return false
    }

    fun checkIfOkToCastSealOfRule(
        castle: Castle?,
        isCheckOnly: Boolean,
        skill: L2Skill,
        target: WorldObject?
    ): Boolean {
        val sm: SystemMessage

        if (castle == null || castle.castleId <= 0)
            sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill)
        else if (!castle.isGoodArtifact(target!!))
            sm = SystemMessage.getSystemMessage(SystemMessageId.INCORRECT_TARGET)
        else if (!castle.siege.isInProgress)
            sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill)
        else if (!MathUtil.checkIfInRange(200, this, target, true))
            sm = SystemMessage.getSystemMessage(SystemMessageId.DIST_TOO_FAR_CASTING_STOPPED)
        else if (!isInsideZone(ZoneId.CAST_ON_ARTIFACT))
            sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill)
        else if (!castle.siege.checkSide(clan, SiegeSide.ATTACKER))
            sm = SystemMessage.getSystemMessage(SystemMessageId.S1_CANNOT_BE_USED).addSkillName(skill)
        else {
            if (!isCheckOnly) {
                sm = SystemMessage.getSystemMessage(SystemMessageId.OPPONENT_STARTED_ENGRAVING)
                castle.siege.announceToPlayers(sm, false)
            }
            return true
        }
        sendPacket(sm)
        return false
    }

    /**
     * @param objectId : The looter object to make checks on.
     * @return true if the active player is the looter or in the same party or command channel than looter objectId.
     */
    fun isLooterOrInLooterParty(objectId: Int): Boolean {
        if (objectId == objectId)
            return true

        val looter = World.getPlayer(objectId) ?: return false

        if (party == null)
            return false

        val channel = party!!.commandChannel
        return channel?.containsPlayer(looter) ?: party!!.containsPlayer(looter)
    }

    /**
     * Check if the requested casting is a Pc->Pc skill cast and if it's a valid pvp condition
     * @param target WorldObject instance containing the target
     * @param skill L2Skill instance with the skill being casted
     * @return `false` if the skill is a pvpSkill and target is not a valid pvp target, `true` otherwise.
     */
    fun checkPvpSkill(target: WorldObject?, skill: L2Skill?): Boolean {
        if (skill == null || target == null)
            return false

        if (target !is Playable)
            return true

        if (skill.isDebuff || skill.isOffensive) {
            val targetPlayer = target.actingPlayer
            if (targetPlayer == null || this == target)
                return false

            // Peace Zone
            if (target.isInsideZone(ZoneId.PEACE))
                return false

            // Duel
            if (isInDuel && targetPlayer.isInDuel && duelId == targetPlayer.duelId)
                return true

            val isCtrlPressed =
                currentSkill != null && currentSkill.isCtrlPressed || currentPetSkill != null && currentPetSkill.isCtrlPressed

            // Party
            if (isInParty && targetPlayer.isInParty) {
                // Same Party
                if (party!!.leader == targetPlayer.party!!.leader) {
                    return if (skill.effectRange > 0 && isCtrlPressed && target === target && skill.isDamage) true else false

                } else if (party!!.commandChannel != null && party!!.commandChannel!!.containsPlayer(targetPlayer)) {
                    return if (skill.effectRange > 0 && isCtrlPressed && target === target && skill.isDamage) true else false

                }
            }

            // You can debuff anyone except party members while in an arena...
            if (isInsideZone(ZoneId.PVP) && targetPlayer.isInsideZone(ZoneId.PVP))
                return true

            // Olympiad
            if (isInOlympiadMode && targetPlayer.isInOlympiadMode && olympiadGameId == targetPlayer.olympiadGameId)
                return true

            val aClan = clan
            val tClan = targetPlayer.clan

            if (aClan != null && tClan != null) {
                if (aClan.isAtWarWith(tClan.clanId) && tClan.isAtWarWith(aClan.clanId)) {
                    // Check if skill can do dmg
                    return if (skill.effectRange > 0 && isCtrlPressed && target === target && skill.isAOE) true else isCtrlPressed

                } else if (clanId == targetPlayer.clanId || allyId > 0 && allyId == targetPlayer.allyId) {
                    // Check if skill can do dmg
                    return if (skill.effectRange > 0 && isCtrlPressed && target === target && skill.isDamage) true else false

                }
            }

            // On retail, it is impossible to debuff a "peaceful" player.
            if (targetPlayer.pvpFlag.toInt() == 0 && targetPlayer.karma == 0) {
                // Check if skill can do dmg
                return if (skill.effectRange > 0 && isCtrlPressed && target === target && skill.isDamage) true else false

            }

            return if (targetPlayer.pvpFlag > 0 || targetPlayer.karma > 0) true else false

        }
        return true
    }

    /**
     * This method allows to :
     *
     *  * change isRiding/isFlying flags
     *  * gift player with Wyvern Breath skill if mount is a wyvern
     *  * send the skillList (faded icons update)
     *
     * @param npcId the npcId of the mount
     * @param npcLevel The level of the mount
     * @param mountType 0, 1 or 2 (dismount, strider or wyvern).
     * @return always true.
     */
    fun setMount(npcId: Int, npcLevel: Int, mountType: Int): Boolean {
        when (mountType) {
            0 // Dismounted
            -> if (isFlying)
                removeSkill(SkillTable.FrequentSkill.WYVERN_BREATH.skill!!.id, false)

            2 // Flying Wyvern
            -> addSkill(SkillTable.FrequentSkill.WYVERN_BREATH.skill, false)
        }

        mountNpcId = npcId
        this.mountType = mountType
        mountLevel = npcLevel

        sendSkillList() // Update faded icons && eventual added skills.
        return true
    }

    override fun stopAllEffects() {
        super.stopAllEffects()
        updateAndBroadcastStatus(2)
    }

    override fun stopAllEffectsExceptThoseThatLastThroughDeath() {
        super.stopAllEffectsExceptThoseThatLastThroughDeath()
        updateAndBroadcastStatus(2)
    }

    /**
     * Stop all toggle-type effects
     */
    fun stopAllToggles() {
        _effects.stopAllToggles()
    }

    fun stopCubics() {
        if (cubics != null) {
            var removed = false
            for (cubic in cubics!!.values) {
                cubic.stopAction()
                delCubic(cubic.id)
                removed = true
            }
            if (removed)
                broadcastUserInfo()
        }
    }

    fun stopCubicsByOthers() {
        if (cubics != null) {
            var removed = false
            for (cubic in cubics!!.values) {
                if (cubic.givenByOther()) {
                    cubic.stopAction()
                    delCubic(cubic.id)
                    removed = true
                }
            }
            if (removed)
                broadcastUserInfo()
        }
    }

    /**
     * Send UserInfo to this Player and CharInfo to all Player in its _KnownPlayers.<BR></BR>
     *
     *  * Send UserInfo to this Player (Public and Private Data)
     *  * Send CharInfo to all Player in _KnownPlayers of the Player (Public data only)
     *
     * <FONT COLOR=#FF0000><B> <U>Caution</U> : DON'T SEND UserInfo packet to other players instead of CharInfo packet. Indeed, UserInfo packet contains PRIVATE DATA as MaxHP, STR, DEX...</B></FONT><BR></BR>
     * <BR></BR>
     */
    override fun updateAbnormalEffect() {
        broadcastUserInfo()
    }

    /**
     * Disable the Inventory and create a new task to enable it after 1.5s.
     */
    fun tempInventoryDisable() {
        isInventoryDisabled = true

        ThreadPool.schedule(Runnable{ isInventoryDisabled = false }, 1500)
    }

    /**
     * Add a L2CubicInstance to the Player _cubics.
     * @param id
     * @param level
     * @param matk
     * @param activationtime
     * @param activationchance
     * @param totalLifetime
     * @param givenByOther
     */
    fun addCubic(
        id: Int,
        level: Int,
        matk: Double,
        activationtime: Int,
        activationchance: Int,
        totalLifetime: Int,
        givenByOther: Boolean
    ) {
        _cubics[id] =
            Cubic(this, id, level, matk.toInt(), activationtime, activationchance, totalLifetime, givenByOther)
    }

    /**
     * Remove a L2CubicInstance from the Player _cubics.
     * @param id
     */
    fun delCubic(id: Int) {
        _cubics.remove(id)
    }

    /**
     * @param id
     * @return the L2CubicInstance corresponding to the Identifier of the Player _cubics.
     */
    fun getCubic(id: Int): Cubic? {
        return _cubics[id]
    }

    override fun toString(): String {
        return "$name ($objectId)"
    }

    fun addAutoSoulShot(itemId: Int) {
        _activeSoulShots.add(itemId)
    }

    fun removeAutoSoulShot(itemId: Int): Boolean {
        return _activeSoulShots.remove(itemId)
    }

    override fun isChargedShot(type: ShotType): Boolean {
        val weapon = activeWeaponInstance
        return weapon != null && weapon.isChargedShot(type)
    }

    override fun setChargedShot(type: ShotType, charged: Boolean) {
        val weapon = activeWeaponInstance
        weapon?.setChargedShot(type, charged)
    }

    override fun rechargeShots(physical: Boolean, magic: Boolean) {
        if (_activeSoulShots.isEmpty())
            return

        for (itemId in _activeSoulShots) {
            val item = inventory!!.getItemByItemId(itemId)
            if (item != null) {
                if (magic && item.item.defaultAction === ActionType.spiritshot) {
                    val handler = ItemHandler.getHandler(item.etcItem)
                    handler?.useItem(this, item, false)
                }

                if (physical && item.item.defaultAction === ActionType.soulshot) {
                    val handler = ItemHandler.getHandler(item.etcItem)
                    handler?.useItem(this, item, false)
                }
            } else
                removeAutoSoulShot(itemId)
        }
    }

    /**
     * Cancel autoshot use for shot itemId
     * @param itemId int id to disable
     * @return true if canceled.
     */
    fun disableAutoShot(itemId: Int): Boolean {
        if (_activeSoulShots.contains(itemId)) {
            removeAutoSoulShot(itemId)
            sendPacket(ExAutoSoulShot(itemId, 0))
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED).addItemName(itemId))
            return true
        }

        return false
    }

    /**
     * Cancel all autoshots for player
     */
    fun disableAutoShotsAll() {
        for (itemId in _activeSoulShots) {
            sendPacket(ExAutoSoulShot(itemId, 0))
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.AUTO_USE_OF_S1_CANCELLED).addItemName(itemId))
        }
        _activeSoulShots.clear()
    }

    override fun sendMessage(message: String) {
        sendPacket(SystemMessage.sendString(message))
    }

    /**
     * Unsummon all types of summons : pets, cubics, normal summons and trained beasts.
     */
    fun dropAllSummons() {
        // Delete summons and pets
        if (pet != null)
            pet!!.unSummon(this)

        // Delete trained beasts
        if (trainedBeast != null)
            trainedBeast!!.deleteMe()

        // Delete any form of cubics
        stopCubics()
    }

    fun enterObserverMode(x: Int, y: Int, z: Int) {
        dropAllSummons()

        if (party != null)
            party!!.removePartyMember(this, MessageType.EXPELLED)

        standUp()

        savedLocation.set(position)

        target = null
        isInvul = true
        appearance.setInvisible()
        isParalyzed = true
        startParalyze()

        teleToLocation(x, y, z, 0)
        sendPacket(ObservationMode(x, y, z))
    }

    fun enterOlympiadObserverMode(id: Int) {
        val task = OlympiadGameManager.getOlympiadTask(id) ?: return

        dropAllSummons()

        if (party != null)
            party!!.removePartyMember(this, MessageType.EXPELLED)

        olympiadGameId = id

        standUp()

        // Don't override saved location if we jump from stadium to stadium.
        if (!isInObserverMode)
            savedLocation.set(position)

        target = null
        isInvul = true
        appearance.setInvisible()

        teleToLocation(task.zone.locs[2], 0)
        sendPacket(ExOlympiadMode(3))
    }

    fun leaveObserverMode() {
        if (hasAI())
            ai!!.setIntention(CtrlIntention.IDLE)

        target = null
        appearance.setVisible()
        isInvul = false
        isParalyzed = false
        stopParalyze(false)

        sendPacket(ObservationReturn(savedLocation))
        teleToLocation(savedLocation, 0)

        // Clear the location.
        savedLocation.clean()
    }

    fun leaveOlympiadObserverMode() {
        if (olympiadGameId == -1)
            return

        olympiadGameId = -1

        if (hasAI())
            ai!!.setIntention(CtrlIntention.IDLE)

        target = null
        appearance.setVisible()
        isInvul = false

        sendPacket(ExOlympiadMode(0))
        teleToLocation(savedLocation, 0)

        // Clear the location.
        savedLocation.clean()
    }

    fun getLoto(i: Int): Int {
        return _loto[i]
    }

    fun setLoto(i: Int, `val`: Int) {
        _loto[i] = `val`
    }

    fun getRace(i: Int): Int {
        return _race[i]
    }

    fun setRace(i: Int, `val`: Int) {
        _race[i] = `val`
    }

    fun setOlympiadMode(b: Boolean) {
        isInOlympiadMode = b
    }

    /**
     * Sets up the duel state using a non 0 duelId.
     * @param duelId 0=not in a duel
     */
    fun setInDuel(duelId: Int) {
        if (duelId > 0) {
            duelState = DuelState.ON_COUNTDOWN
            this.duelId = duelId
        } else {
            if (duelState === DuelState.DEAD) {
                enableAllSkills()
                status.startHpMpRegeneration()
            }
            duelState = DuelState.NO_DUEL
            this.duelId = 0
        }
    }

    /**
     * Checks if this player might join / start a duel. To get the reason use getNoDuelReason() after calling this function.
     * @return true if the player might join/start a duel.
     */
    fun canDuel(): Boolean {
        if (isInCombat || punishLevel == PunishLevel.JAIL)
            _noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_ENGAGED_IN_BATTLE
        else if (isDead() || isAlikeDead || currentHp < maxHp / 2 || currentMp < maxMp / 2)
            _noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_HP_OR_MP_IS_BELOW_50_PERCENT
        else if (isInDuel)
            _noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_ALREADY_ENGAGED_IN_A_DUEL
        else if (isInOlympiadMode)
            _noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_PARTICIPATING_IN_THE_OLYMPIAD
        else if (isCursedWeaponEquipped || karma != 0)
            _noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_IN_A_CHAOTIC_STATE
        else if (isInStoreMode)
            _noDuelReason =
                SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_ENGAGED_IN_A_PRIVATE_STORE_OR_MANUFACTURE
        else if (isMounted || isInBoat)
            _noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_RIDING_A_BOAT_WYVERN_OR_STRIDER
        else if (isFishing)
            _noDuelReason = SystemMessageId.S1_CANNOT_DUEL_BECAUSE_S1_IS_CURRENTLY_FISHING
        else if (isInsideZone(ZoneId.PVP) || isInsideZone(ZoneId.PEACE) || isInsideZone(ZoneId.SIEGE))
            _noDuelReason =
                SystemMessageId.S1_CANNOT_MAKE_A_CHALLANGE_TO_A_DUEL_BECAUSE_S1_IS_CURRENTLY_IN_A_DUEL_PROHIBITED_AREA
        else
            return true

        return false
    }

    /**
     * Set Noblesse Status, and reward with nobles' skills.
     * @param val Add skills if setted to true, else remove skills.
     * @param store Store the status directly in the db if setted to true.
     */
    fun setNoble(`val`: Boolean, store: Boolean) {
        if (`val`)
            for (skill in SkillTable.nobleSkills)
                addSkill(skill, false)
        else
            for (skill in SkillTable.nobleSkills)
                removeSkill(skill!!.getId(), false)

        isNoble = `val`

        sendSkillList()

        if (store) {
            try {
                L2DatabaseFactory.connection.use { con ->
                    con.prepareStatement(UPDATE_NOBLESS).use { ps ->
                        ps.setBoolean(1, `val`)
                        ps.setInt(2, objectId)
                        ps.executeUpdate()
                    }
                }
            } catch (e: Exception) {
                WorldObject.LOGGER.error("Couldn't update nobless status for {}.", e, name)
            }

        }
    }

    fun setWantsPeace(wantsPeace: Boolean) {
        _wantsPeace = wantsPeace
    }

    fun wantsPeace(): Boolean {
        return _wantsPeace
    }

    fun sendSkillList() {
        val formal = inventory!!.getPaperdollItem(Inventory.PAPERDOLL_CHEST)
        val isWearingFormalWear = formal != null && formal.item.bodyPart == Item.SLOT_ALLDRESS
        val sl = SkillList()

        for (s in skills.values) {
            if (isWearingFormalWear)
                sl.addSkill(s.id, s.level, s.isPassive, true)
            else {
                var isDisabled = false
                if (clan != null)
                    isDisabled = s.isClanSkill && clan!!.reputationScore < 0

                if (isCursedWeaponEquipped)
                // Only Demonic skills are available
                    isDisabled = !s.isDemonicSkill
                else if (isMounted)
                // else if, because only ONE state is possible
                {
                    if (mountType == 1)
                    // Only Strider skills are available
                        isDisabled = !s.isStriderSkill
                    else if (mountType == 2)
                    // Only Wyvern skills are available
                        isDisabled = !s.isFlyingSkill
                }
                sl.addSkill(s.id, s.level, s.isPassive, isDisabled)
            }
        }
        sendPacket(sl)
    }

    /**
     * 1. Add the specified class ID as a subclass (up to the maximum number of **three**) for this character.<BR></BR>
     * 2. This method no longer changes the active _classIndex of the player. This is only done by the calling of setActiveClass() method as that should be the only way to do so.
     * @param classId
     * @param classIndex
     * @return boolean subclassAdded
     */
    fun addSubClass(classId: Int, classIndex: Int): Boolean {
        if (!_subclassLock.tryLock())
            return false

        try {
            if (subClasses.size == 3 || classIndex == 0 || subClasses.containsKey(classIndex))
                return false

            val subclass = SubClass(classId, classIndex)

            try {
                L2DatabaseFactory.connection.use { con ->
                    con.prepareStatement(ADD_CHAR_SUBCLASS).use { ps ->
                        ps.setInt(1, objectId)
                        ps.setInt(2, subclass.classId)
                        ps.setLong(3, subclass.exp)
                        ps.setInt(4, subclass.sp)
                        ps.setInt(5, subclass.level.toInt())
                        ps.setInt(6, subclass.classIndex)
                        ps.execute()
                    }
                }
            } catch (e: Exception) {
                WorldObject.LOGGER.error("Couldn't add subclass for {}.", e, name)
                return false
            }

            subClasses[subclass.classIndex] = subclass

            PlayerData.getTemplate(classId).skills.filter { s -> s.minLvl <= 40 }
                .associateBy {it.id to Collectors.maxBy(COMPARE_SKILLS_BY_LVL) }
                .forEach{ i, s -> storeSkill(s.skill, classIndex) }

            return true
        } finally {
            _subclassLock.unlock()
        }
    }

    /**
     * 1. Completely erase all existance of the subClass linked to the classIndex.<BR></BR>
     * 2. Send over the newClassId to addSubClass()to create a new instance on this classIndex.<BR></BR>
     * 3. Upon Exception, revert the player to their BaseClass to avoid further problems.<BR></BR>
     * @param classIndex
     * @param newClassId
     * @return boolean subclassAdded
     */
    fun modifySubClass(classIndex: Int, newClassId: Int): Boolean {
        if (!_subclassLock.tryLock())
            return false

        try {
            try {
                L2DatabaseFactory.connection.use { con ->
                    // Remove all henna info stored for this sub-class.
                    var ps = con.prepareStatement(DELETE_CHAR_HENNAS)
                    ps.setInt(1, objectId)
                    ps.setInt(2, classIndex)
                    ps.execute()
                    ps.close()

                    // Remove all shortcuts info stored for this sub-class.
                    ps = con.prepareStatement(DELETE_CHAR_SHORTCUTS)
                    ps.setInt(1, objectId)
                    ps.setInt(2, classIndex)
                    ps.execute()
                    ps.close()

                    // Remove all effects info stored for this sub-class.
                    ps = con.prepareStatement(DELETE_SKILL_SAVE)
                    ps.setInt(1, objectId)
                    ps.setInt(2, classIndex)
                    ps.execute()
                    ps.close()

                    // Remove all skill info stored for this sub-class.
                    ps = con.prepareStatement(DELETE_CHAR_SKILLS)
                    ps.setInt(1, objectId)
                    ps.setInt(2, classIndex)
                    ps.execute()
                    ps.close()

                    // Remove all basic info stored about this sub-class.
                    ps = con.prepareStatement(DELETE_CHAR_SUBCLASS)
                    ps.setInt(1, objectId)
                    ps.setInt(2, classIndex)
                    ps.execute()
                    ps.close()
                }
            } catch (e: Exception) {
                WorldObject.LOGGER.error("Couldn't modify subclass for {} to class index {}.", e, name, classIndex)

                // This must be done in order to maintain data consistency.
                subClasses.remove(classIndex)
                return false
            }

            subClasses.remove(classIndex)
        } finally {
            _subclassLock.unlock()
        }

        return addSubClass(newClassId, classIndex)
    }

    fun getActiveClass(): Int {
        return _activeClass
    }

    private fun setClassTemplate(classId: Int) {
        _activeClass = classId

        // Set the template of the Player
        template = PlayerData.getTemplate(classId)
    }

    /**
     * Changes the character's class based on the given class index. <BR></BR>
     * <BR></BR>
     * An index of zero specifies the character's original (base) class, while indexes 1-3 specifies the character's sub-classes respectively.
     * @param classIndex
     * @return true if successful.
     */
    fun setActiveClass(classIndex: Int): Boolean {
        if (!_subclassLock.tryLock())
            return false

        try {
            // Remove active item skills before saving char to database because next time when choosing this class, worn items can be different
            for (item in inventory!!.augmentedItems) {
                if (item != null && item.isEquipped)
                    item.getAugmentation()!!.removeBonus(this)
            }

            // abort any kind of cast.
            abortCast()

            // Stop casting for any player that may be casting a force buff on this l2pcinstance.
            for (character in getKnownType(Creature::class.java))
                if (character.fusionSkill != null && character.fusionSkill!!.target === this)
                    character.abortCast()

            store()
            _reuseTimeStamps.clear()

            // clear charges
            _charges.set(0)
            stopChargeTask()

            if (classIndex == 0)
                setClassTemplate(baseClass)
            else {
                try {
                    setClassTemplate(subClasses[classIndex]!!.classId)
                } catch (e: Exception) {
                    WorldObject.LOGGER.error("Could not switch {}'s subclass to class index {}.", e, name, classIndex)
                    return false
                }

            }
            this.classIndex = classIndex

            if (party != null)
                party!!.recalculateLevel()

            if (pet is Servitor)
                pet!!.unSummon(this)

            for (skill in skills.values)
                removeSkill(skill.id, false)

            stopAllEffectsExceptThoseThatLastThroughDeath()
            stopCubics()

            if (isSubClassActive) {
                _dwarvenRecipeBook.clear()
                _commonRecipeBook.clear()
            } else
                restoreRecipeBook()

            restoreSkills()
            giveSkills()
            regiveTemporarySkills()

            // Prevents some issues when changing between subclases that shares skills
            disabledSkills.clear()

            restoreEffects()
            updateEffectIcons()
            sendPacket(EtcStatusUpdate(this))

            // If player has quest "Repent Your Sins", remove it
            val st = getQuestState("Q422_RepentYourSins")
            st?.exitQuest(true)

            for (i in 0..2)
                _henna[i] = null

            restoreHenna()
            sendPacket(HennaInfo(this))

            if (currentHp > maxHp)
                currentHp = maxHp.toDouble()
            if (currentMp > maxMp)
                currentMp = maxMp.toDouble()
            if (currentCp > maxCp)
                currentCp = maxCp.toDouble()

            refreshOverloaded()
            refreshExpertisePenalty()
            broadcastUserInfo()

            // Clear resurrect xp calculation
            expBeforeDeath = 0

            // Remove shot automation
            disableAutoShotsAll()

            // Discharge any active shots
            val item = activeWeaponInstance
            item?.unChargeAllShots()

            _shortCuts.restore()
            sendPacket(ShortCutInit(this))

            broadcastPacket(SocialAction(this, 15))
            sendPacket(SkillCoolTime(this))
            return true
        } finally {
            _subclassLock.unlock()
        }
    }

    fun onPlayerEnter() {
        if (isCursedWeaponEquipped)
            CursedWeaponManager.getCursedWeapon(cursedWeaponEquippedId)!!.cursedOnLogin()

        // Add to the GameTimeTask to keep inform about activity time.
        GameTimeTaskManager.add(this)

        // Teleport player if the Seven Signs period isn't the good one, or if the player isn't in a cabal.
        if (isIn7sDungeon && !isGM) {
            if (SevenSigns.isSealValidationPeriod || SevenSigns.isCompResultsPeriod) {
                if (SevenSigns.getPlayerCabal(objectId) !== SevenSigns.cabalHighestScore) {
                    teleToLocation(MapRegionData.TeleportType.TOWN)
                    isIn7sDungeon = false
                }
            } else if (SevenSigns.getPlayerCabal(objectId) === CabalType.NORMAL) {
                teleToLocation(MapRegionData.TeleportType.TOWN)
                isIn7sDungeon = false
            }
        }

        // Jail task
        updatePunishState()

        if (isGM) {
            if (isInvul)
                sendMessage("Entering world in Invulnerable mode.")
            if (appearance.invisible)
                sendMessage("Entering world in Invisible mode.")
            if (isInRefusalMode)
                sendMessage("Entering world in Message Refusal mode.")
        }

        revalidateZone(true)
        notifyFriends(true)
    }

    override fun doRevive() {
        super.doRevive()

        stopEffects(L2EffectType.CHARMOFCOURAGE)
        sendPacket(EtcStatusUpdate(this))

        _reviveRequested = 0
        _revivePower = 0.0

        if (isMounted)
            startFeed(mountNpcId)

        // Schedule a paralyzed task to wait for the animation to finish
        ThreadPool.schedule(Runnable{ isParalyzed = false }, (2000 / stat.movementSpeedMultiplier).toInt().toLong())
        isParalyzed = true
    }

    override fun doRevive(revivePower: Double) {
        // Restore the player's lost experience, depending on the % return of the skill used (based on its power).
        restoreExp(revivePower)
        doRevive()
    }

    fun reviveRequest(Reviver: Player, skill: L2Skill?, Pet: Boolean) {
        if (_reviveRequested == 1) {
            // Resurrection has already been proposed.
            if (isRevivingPet == Pet)
                Reviver.sendPacket(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED)
            else {
                if (Pet)
                // A pet cannot be resurrected while it's owner is in the process of resurrecting.
                    Reviver.sendPacket(SystemMessageId.CANNOT_RES_PET2)
                else
                // While a pet is attempting to resurrect, it cannot help in resurrecting its master.
                    Reviver.sendPacket(SystemMessageId.MASTER_CANNOT_RES)
            }
            return
        }

        if (Pet && pet != null && pet!!.isDead() || !Pet && isDead()) {
            _reviveRequested = 1

            if (isPhoenixBlessed)
                _revivePower = 100.0
            else if (isAffected(L2EffectFlag.CHARM_OF_COURAGE))
                _revivePower = 0.0
            else
                _revivePower = Formulas.calculateSkillResurrectRestorePercent(skill!!.power, Reviver)

            isRevivingPet = Pet

            if (isAffected(L2EffectFlag.CHARM_OF_COURAGE)) {
                sendPacket(ConfirmDlg(SystemMessageId.DO_YOU_WANT_TO_BE_RESTORED).addTime(60000))
                return
            }

            sendPacket(ConfirmDlg(SystemMessageId.RESSURECTION_REQUEST_BY_S1).addCharName(Reviver))
        }
    }

    fun reviveAnswer(answer: Int) {
        if (_reviveRequested != 1 || !isDead() && !isRevivingPet || isRevivingPet && pet != null && !pet!!.isDead())
            return

        if (answer == 0 && isPhoenixBlessed)
            stopPhoenixBlessing(null)
        else if (answer == 1) {
            if (!isRevivingPet) {
                if (_revivePower != 0.0)
                    doRevive(_revivePower)
                else
                    doRevive()
            } else if (pet != null) {
                if (_revivePower != 0.0)
                    pet!!.doRevive(_revivePower)
                else
                    pet!!.doRevive()
            }
        }
        _reviveRequested = 0
        _revivePower = 0.0
    }

    fun removeReviving() {
        _reviveRequested = 0
        _revivePower = 0.0
    }

    fun onActionRequest() {
        if (isSpawnProtected) {
            sendMessage("As you acted, you are no longer under spawn protection.")
            setSpawnProtection(false)
        }
    }

    override fun onTeleported() {
        super.onTeleported()

        if (Config.PLAYER_SPAWN_PROTECTION > 0)
            setSpawnProtection(true)

        // Stop toggles upon teleport.
        if (!isGM)
            stopAllToggles()

        // Modify the position of the tamed beast if necessary
        if (trainedBeast != null) {
            trainedBeast!!.getAI().stopFollow()
            trainedBeast!!.teleToLocation(position, 0)
            trainedBeast!!.getAI().startFollow(this)
        }

        // Modify the position of the pet if necessary
        val pet = pet
        if (pet != null) {
            pet.followStatus = false
            pet.teleToLocation(position, 0)
            (pet.getAI() as SummonAI).setStartFollowController(true)
            pet.followStatus = true
        }
    }

    override fun addExpAndSp(addToExp: Long, addToSp: Int) {
        (stat as PlayerStat).addExpAndSp(addToExp, addToSp)
    }

    fun addExpAndSp(addToExp: Long, addToSp: Int, rewards: Map<Creature, RewardInfo>) {
        (stat as PlayerStat).addExpAndSp(addToExp, addToSp, rewards)
    }

    fun removeExpAndSp(removeExp: Long, removeSp: Int) {
        (stat as PlayerStat).removeExpAndSp(removeExp, removeSp)
    }

    override fun reduceCurrentHp(value: Double, attacker: Creature?, awake: Boolean, isDOT: Boolean, skill: L2Skill?) {
        if (skill != null)
            (status as PlayerStatus).reduceHp(value, attacker, awake, isDOT, skill.isToggle, skill.dmgDirectlyToHP)
        else
            (status as PlayerStatus).reduceHp(value, attacker, awake, isDOT, false, false)

        // notify the tamed beast of attacks
        if (trainedBeast != null)
            trainedBeast!!.onOwnerGotAttacked(attacker)
    }

    @Synchronized
    fun addBypass(bypass: String?) {
        if (bypass == null)
            return

        _validBypass.add(bypass)
    }

    @Synchronized
    fun addBypass2(bypass: String?) {
        if (bypass == null)
            return

        _validBypass2.add(bypass)
    }

    @Synchronized
    fun validateBypass(cmd: String): Boolean {
        for (bp in _validBypass) {
            if (bp == null)
                continue

            if (bp == cmd)
                return true
        }

        for (bp in _validBypass2) {
            if (bp == null)
                continue

            if (cmd.startsWith(bp))
                return true
        }

        return false
    }

    /**
     * Test cases (player drop, trade item) where the item shouldn't be able to manipulate.
     * @param objectId : The item objectId.
     * @return true if it the item can be manipulated, false ovtherwise.
     */
    fun validateItemManipulation(objectId: Int): ItemInstance? {
        val item = inventory!!.getItemByObjectId(objectId)

        // You don't own the item, or item is null.
        if (item == null || item.ownerId != objectId)
            return null

        // Pet whom item you try to manipulate is summoned/mounted.
        if (pet != null && pet!!.controlItemId == objectId || mountObjectId == objectId)
            return null

        // Item is under enchant process.
        if (activeEnchantItem != null && activeEnchantItem!!.objectId == objectId)
            return null

        // Can't trade a cursed weapon.
        return if (CursedWeaponManager.isCursed(item.itemId)) null else item

    }

    @Synchronized
    fun clearBypass() {
        _validBypass.clear()
        _validBypass2.clear()
    }

    /**
     * Manage the delete task of a Player (Leave Party, Unsummon pet, Save its inventory in the database, Remove it from the world...).
     *
     *  * If the Player is in observer mode, set its position to its position before entering in observer mode
     *  * Set the online Flag to True or False and update the characters table of the database with online status and lastAccess
     *  * Stop the HP/MP/CP Regeneration task
     *  * Cancel Crafting, Attak or Cast
     *  * Remove the Player from the world
     *  * Stop Party and Unsummon Pet
     *  * Update database with items in its inventory and remove them from the world
     *  * Remove the object from region
     *  * Close the connection with the client
     *
     */
    override fun deleteMe() {
        cleanup()
        store()
        super.deleteMe()
    }

    @Synchronized
    private fun cleanup() {
        try {
            // Put the online status to false
            setOnlineStatus(false, true)

            // abort cast & attack and remove the target. Cancels movement aswell.
            abortAttack()
            abortCast()
            stopMove(null)
            target = null

            removeMeFromPartyMatch()

            if (isFlying)
                removeSkill(SkillTable.FrequentSkill.WYVERN_BREATH.skill!!.id, false)

            // Dismount the player.
            if (isMounted)
                dismount()
            else if (pet != null)
                pet!!.unSummon(this)// If the Player has a summon, unsummon it.

            // Stop all scheduled tasks.
            stopHpMpRegeneration()
            stopPunishTask(true)
            stopChargeTask()

            // Stop all timers associated to that Player.
            WaterTaskManager.remove(this)
            AttackStanceTaskManager.remove(this)
            PvpFlagTaskManager.remove(this)
            GameTimeTaskManager.remove(this)
            ShadowItemTaskManager.remove(this)

            // Cancel the cast of eventual fusion skill users on this target.
            for (character in getKnownType(Creature::class.java))
                if (character.fusionSkill != null && character.fusionSkill!!.target === this)
                    character.abortCast()

            // Stop signets & toggles effects.
            for (effect in allEffects) {
                if (effect.skill.isToggle) {
                    effect.exit()
                    continue
                }

                when (effect.effectType) {
                    L2EffectType.SIGNET_GROUND, L2EffectType.SIGNET_EFFECT -> effect.exit()
                }
            }

            // Remove the Player from the world
            decayMe()

            // If a party is in progress, leave it
            if (party != null)
                party!!.removePartyMember(this, MessageType.DISCONNECTED)

            // Handle removal from olympiad game
            if (OlympiadManager.isRegistered(this) || olympiadGameId != -1)
                OlympiadManager.removeDisconnectedCompetitor(this)

            // set the status for pledge member list to OFFLINE
            if (clan != null) {
                val clanMember = clan!!.getClanMember(objectId)
                if (clanMember != null)
                    clanMember.playerInstance = null
            }

            // deals with sudden exit in the middle of transaction
            if (activeRequester != null) {
                activeRequester = null
                cancelActiveTrade()
            }

            // If the Player is a GM, remove it from the GM List
            if (isGM)
                AdminData.deleteGm(this)

            // Check if the Player is in observer mode to set its position to its position before entering in observer mode
            if (isInObserverMode)
                setXYZInvisible(savedLocation)

            // Oust player from boat
            if (boat != null)
                boat!!.oustPlayer(this, true, Location.DUMMY_LOC)

            // Update inventory and remove them from the world
            inventory!!.deleteMe()

            // Update warehouse and remove them from the world
            clearWarehouse()

            // Update freight and remove them from the world
            clearFreight()
            clearDepositedFreight()

            if (isCursedWeaponEquipped)
                CursedWeaponManager.getCursedWeapon(cursedWeaponEquippedId)!!.player = null

            if (clanId > 0)
                clan!!.broadcastToOtherOnlineMembers(PledgeShowMemberListUpdate(this), this)

            if (isSeated) {
                val `object` = World.getObject(_throneId)
                if (`object` is StaticObject)
                    `object`.isBusy = false
            }

            World.removePlayer(this) // force remove in case of crash during teleport

            // friends & blocklist update
            notifyFriends(false)
            blockList.playerLogout()
        } catch (e: Exception) {
            WorldObject.LOGGER.error("Couldn't disconnect correctly the player.", e)
        }

    }

    /**
     * Update the _currentSkill holder.
     * @param skill : The skill to update for (or null)
     * @param ctrlPressed : The boolean information regarding ctrl key.
     * @param shiftPressed : The boolean information regarding shift key.
     */
    fun setCurrentSkill(skill: L2Skill?, ctrlPressed: Boolean, shiftPressed: Boolean) {
        currentSkill.skill = skill
        currentSkill.isCtrlPressed = ctrlPressed
        currentSkill.isShiftPressed = shiftPressed
    }

    /**
     * Update the _currentPetSkill holder.
     * @param skill : The skill to update for (or null)
     * @param ctrlPressed : The boolean information regarding ctrl key.
     * @param shiftPressed : The boolean information regarding shift key.
     */
    fun setCurrentPetSkill(skill: L2Skill?, ctrlPressed: Boolean, shiftPressed: Boolean) {
        currentPetSkill.skill = skill
        currentPetSkill.isCtrlPressed = ctrlPressed
        currentPetSkill.isShiftPressed = shiftPressed
    }

    /**
     * Update the _queuedSkill holder.
     * @param skill : The skill to update for (or null)
     * @param ctrlPressed : The boolean information regarding ctrl key.
     * @param shiftPressed : The boolean information regarding shift key.
     */
    fun setQueuedSkill(skill: L2Skill?, ctrlPressed: Boolean, shiftPressed: Boolean) {
        queuedSkill.skill = skill
        queuedSkill.isCtrlPressed = ctrlPressed
        queuedSkill.isShiftPressed = shiftPressed
    }

    fun setPunishLevel(state: Int) {
        when (state) {
            0 -> punishLevel = PunishLevel.NONE
            1 -> punishLevel = PunishLevel.CHAT
            2 -> punishLevel = PunishLevel.JAIL
            3 -> punishLevel = PunishLevel.CHAR
            4 -> punishLevel = PunishLevel.ACC
        }
    }

    /**
     * Sets punish level for player based on delay
     * @param state
     * @param delayInMinutes -- 0 for infinite
     */
    fun setPunishLevel(state: PunishLevel, delayInMinutes: Int) {
        val delayInMilliseconds = delayInMinutes * 60000L
        when (state) {
            Player.PunishLevel.NONE // Remove Punishments
            -> {
                when (punishLevel) {
                    Player.PunishLevel.CHAT -> {
                        punishLevel = state
                        stopPunishTask(true)
                        sendPacket(EtcStatusUpdate(this))
                        sendMessage("Chatting is now available.")
                        sendPacket(PlaySound("systemmsg_e.345"))
                    }
                    Player.PunishLevel.JAIL -> {
                        punishLevel = state

                        // Open a Html message to inform the player
                        val html = NpcHtmlMessage(0)
                        html.setFile("data/html/jail_out.htm")
                        sendPacket(html)

                        stopPunishTask(true)
                        teleToLocation(17836, 170178, -3507, 20) // Floran village
                    }
                }
            }
            Player.PunishLevel.CHAT // Chat ban
            -> {
                run{
                    // not allow player to escape jail using chat ban
                    if (punishLevel == PunishLevel.JAIL)
                        return@run

                    punishLevel = state
                    punishTimer = 0
                    sendPacket(EtcStatusUpdate(this))

                    // Remove the task if any
                    stopPunishTask(false)

                    if (delayInMinutes > 0) {
                        punishTimer = delayInMilliseconds

                        // start the countdown
                        _punishTask = ThreadPool.schedule(Runnable{ setPunishLevel(PunishLevel.NONE, 0) }, punishTimer)
                        sendMessage("Chatting has been suspended for $delayInMinutes minute(s).")
                    } else
                        sendMessage("Chatting has been suspended.")

                    // Send same sound packet in both "delay" cases.
                    sendPacket(PlaySound("systemmsg_e.346"))
                }
            }
            Player.PunishLevel.JAIL // Jail Player
            -> {
                punishLevel = state
                punishTimer = 0

                // Remove the task if any
                stopPunishTask(false)

                if (delayInMinutes > 0) {
                    punishTimer = delayInMilliseconds

                    // start the countdown
                    _punishTask = ThreadPool.schedule(Runnable{ setPunishLevel(PunishLevel.NONE, 0) }, punishTimer)
                    sendMessage("You are jailed for $delayInMinutes minutes.")
                }

                if (OlympiadManager.isRegisteredInComp(this))
                    OlympiadManager.removeDisconnectedCompetitor(this)

                // Open a Html message to inform the player
                val html = NpcHtmlMessage(0)
                html.setFile("data/html/jail_in.htm")
                sendPacket(html)

                isIn7sDungeon = false
                teleToLocation(-114356, -249645, -2984, 0) // Jail
            }
            Player.PunishLevel.CHAR // Ban Character
            -> {
                setAccessLevel(-1)
                logout(false)
            }
            Player.PunishLevel.ACC // Ban Account
            -> {
                setAccountAccesslevel(-100)
                logout(false)
            }
            else -> {
                punishLevel = state
            }
        }

        // store in database
        storeCharBase()
    }

    private fun updatePunishState() {
        if (punishLevel != PunishLevel.NONE) {
            // If punish timer exists, restart punishtask.
            if (punishTimer > 0) {
                _punishTask = ThreadPool.schedule(Runnable{ setPunishLevel(PunishLevel.NONE, 0) }, punishTimer)
                sendMessage("You are still " + punishLevel.string() + " for " + Math.round(punishTimer / 60000f) + " minutes.")
            }

            if (punishLevel == PunishLevel.JAIL) {
                // If player escaped, put him back in jail
                if (!isInsideZone(ZoneId.JAIL))
                    teleToLocation(-114356, -249645, -2984, 20)
            }
        }
    }

    fun stopPunishTask(save: Boolean) {
        if (_punishTask != null) {
            if (save) {
                var delay = _punishTask!!.getDelay(TimeUnit.MILLISECONDS)
                if (delay < 0)
                    delay = 0
                punishTimer = delay
            }
            _punishTask!!.cancel(false)
            _punishTask = null
        }
    }

    fun shortBuffStatusUpdate(magicId: Int, level: Int, time: Int) {
        if (_shortBuffTask != null) {
            _shortBuffTask!!.cancel(false)
            _shortBuffTask = null
        }

        _shortBuffTask = ThreadPool.schedule(Runnable{
            sendPacket(ShortBuffStatusUpdate(0, 0, 0))
            shortBuffTaskSkillId = 0
        }, (time * 1000).toLong())
        shortBuffTaskSkillId = magicId

        sendPacket(ShortBuffStatusUpdate(magicId, level, time))
    }

    fun calculateDeathPenaltyBuffLevel(killer: Creature?) {
        if (deathPenaltyBuffLevel >= 15)
        // maximum level reached
            return

        if ((karma > 0 || Rnd[1, 100] <= Config.DEATH_PENALTY_CHANCE) && killer !is Player && !isGM && !(charmOfLuck && (killer == null || killer.isRaidRelated)) && !isPhoenixBlessed && !(isInsideZone(
                ZoneId.PVP
            ) || isInsideZone(ZoneId.SIEGE))
        ) {
            if (deathPenaltyBuffLevel != 0)
                removeSkill(5076, false)

            deathPenaltyBuffLevel++

            addSkill(SkillTable.getInfo(5076, deathPenaltyBuffLevel), false)
            sendPacket(EtcStatusUpdate(this))
            sendPacket(
                SystemMessage.getSystemMessage(SystemMessageId.DEATH_PENALTY_LEVEL_S1_ADDED).addNumber(
                    deathPenaltyBuffLevel
                )
            )
        }
    }

    fun reduceDeathPenaltyBuffLevel() {
        if (deathPenaltyBuffLevel <= 0)
            return

        removeSkill(5076, false)

        deathPenaltyBuffLevel--

        if (deathPenaltyBuffLevel > 0) {
            addSkill(SkillTable.getInfo(5076, deathPenaltyBuffLevel), false)
            sendPacket(
                SystemMessage.getSystemMessage(SystemMessageId.DEATH_PENALTY_LEVEL_S1_ADDED).addNumber(
                    deathPenaltyBuffLevel
                )
            )
        } else
            sendPacket(SystemMessageId.DEATH_PENALTY_LIFTED)

        sendPacket(EtcStatusUpdate(this))
    }

    fun restoreDeathPenaltyBuffLevel() {
        if (deathPenaltyBuffLevel > 0)
            addSkill(SkillTable.getInfo(5076, deathPenaltyBuffLevel), false)
    }

    /**
     * Index according to skill id the current timestamp of use.
     * @param skill
     * @param reuse delay
     */
    override fun addTimeStamp(skill: L2Skill, reuse: Long) {
        _reuseTimeStamps[skill.reuseHashCode] = Timestamp(skill, reuse)
    }

    /**
     * Index according to skill this TimeStamp instance for restoration purposes only.
     * @param skill
     * @param reuse
     * @param systime
     */
    fun addTimeStamp(skill: L2Skill, reuse: Long, systime: Long) {
        _reuseTimeStamps[skill.reuseHashCode] = Timestamp(skill, reuse, systime)
    }

    override fun sendDamageMessage(target: Creature, damage: Int, mcrit: Boolean, pcrit: Boolean, miss: Boolean) {
        // Check if hit is missed
        if (miss) {
            sendPacket(SystemMessageId.MISSED_TARGET)
            return
        }

        // Check if hit is critical
        if (pcrit)
            sendPacket(SystemMessageId.CRITICAL_HIT)
        if (mcrit)
            sendPacket(SystemMessageId.CRITICAL_HIT_MAGIC)

        if (target.isInvul) {
            if (target.isParalyzed)
                sendPacket(SystemMessageId.OPPONENT_PETRIFIED)
            else
                sendPacket(SystemMessageId.ATTACK_WAS_BLOCKED)
        } else
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.YOU_DID_S1_DMG).addNumber(damage))

        if (isInOlympiadMode && target is Player && target.isInOlympiadMode && target.olympiadGameId == olympiadGameId)
            OlympiadGameManager.notifyCompetitorDamage(this, damage)
    }

    fun checkItemRestriction() {
        for (equippedItem in inventory!!.paperdollItems) {
            if (equippedItem.item.checkCondition(this, this, false))
                continue

            useEquippableItem(equippedItem, equippedItem.isWeapon)
        }
    }

    /**
     * A method used to test player entrance on no landing zone.<br></br>
     * <br></br>
     * If a player is mounted on a Wyvern, it launches a dismount task after 5 seconds, and a warning message.
     */
    fun enterOnNoLandingZone() {
        if (mountType == 2) {
            if (_dismountTask == null)
                _dismountTask = ThreadPool.schedule(Runnable{ dismount() }, 5000)

            sendPacket(SystemMessageId.AREA_CANNOT_BE_ENTERED_WHILE_MOUNTED_WYVERN)
        }
    }

    /**
     * A method used to test player leave on no landing zone.<br></br>
     * <br></br>
     * If a player is mounted on a Wyvern, it cancels the dismount task, if existing.
     */
    fun exitOnNoLandingZone() {
        if (mountType == 2 && _dismountTask != null) {
            _dismountTask!!.cancel(true)
            _dismountTask = null
        }
    }

    /**
     * Remove player from BossZones (used on char logout/exit)
     */
    fun removeFromBossZone() {
        for (zone in ZoneManager.getAllZones(BossZone::class.java))
            zone.removePlayer(this)
    }

    fun increaseCharges(count: Int, max: Int) {
        if (_charges.get() >= max) {
            sendPacket(SystemMessageId.FORCE_MAXLEVEL_REACHED)
            return
        }

        restartChargeTask()

        if (_charges.addAndGet(count) >= max) {
            _charges.set(max)
            sendPacket(SystemMessageId.FORCE_MAXLEVEL_REACHED)
        } else
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FORCE_INCREASED_TO_S1).addNumber(_charges.get()))

        sendPacket(EtcStatusUpdate(this))
    }

    fun decreaseCharges(count: Int): Boolean {
        if (_charges.get() < count)
            return false

        if (_charges.addAndGet(-count) == 0)
            stopChargeTask()
        else
            restartChargeTask()

        sendPacket(EtcStatusUpdate(this))
        return true
    }

    fun clearCharges() {
        _charges.set(0)
        sendPacket(EtcStatusUpdate(this))
    }

    /**
     * Starts/Restarts the ChargeTask to Clear Charges after 10 Mins.
     */
    private fun restartChargeTask() {
        if (_chargeTask != null) {
            _chargeTask!!.cancel(false)
            _chargeTask = null
        }

        _chargeTask = ThreadPool.schedule(Runnable { this.clearCharges() }, 600000)
    }

    /**
     * Stops the Charges Clearing Task.
     */
    fun stopChargeTask() {
        if (_chargeTask != null) {
            _chargeTask!!.cancel(false)
            _chargeTask = null
        }
    }

    /**
     * Signets check used to valid who is affected when he entered in the aoe effect.
     * @param cha The target to make checks on.
     * @return true if player can attack the target.
     */
    fun canAttackCharacter(cha: Creature): Boolean {
        if (cha is Attackable)
            return true

        if (cha is Playable) {
            if (cha.isInArena)
                return true

            val target = cha.actingPlayer

            if (isInDuel && target!!.isInDuel && target.duelId == duelId)
                return true

            if (isInParty && target!!.isInParty) {
                if (party === target.party)
                    return false

                if ((party!!.commandChannel != null || target.party!!.commandChannel != null) && party!!.commandChannel === target.party!!.commandChannel)
                    return false
            }

            if (clan != null && target!!.clan != null) {
                if (clanId == target.clanId)
                    return false

                if ((allyId > 0 || target.allyId > 0) && allyId == target.allyId)
                    return false

                if (clan!!.isAtWarWith(target.clanId))
                    return true
            } else {
                if (target!!.pvpFlag.toInt() == 0 && target.karma == 0)
                    return false
            }
        }
        return true
    }

    /**
     * Teleport the current [Player] to the destination of another player.<br></br>
     * <br></br>
     * Check if summoning is allowed, and consume items if [L2Skill] got such constraints.
     * @param player : The player to teleport on.
     * @param skill : The skill used to find item consumption informations.
     */
    fun teleportToFriend(player: Player?, skill: L2Skill?) {
        if (player == null || skill == null)
            return

        if (!player.checkSummonerStatus() || !player.checkSummonTargetStatus(this))
            return

        val itemConsumeId = skill.targetConsumeId
        val itemConsumeCount = skill.targetConsume

        if (itemConsumeId != 0 && itemConsumeCount != 0) {
            if (inventory!!.getInventoryItemCount(itemConsumeId, -1) < itemConsumeCount) {
                sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_REQUIRED_FOR_SUMMONING).addItemName(skill.targetConsumeId))
                return
            }

            destroyItemByItemId("Consume", itemConsumeId, itemConsumeCount, this, true)
        }
        teleToLocation(player.x, player.y, player.z, 20)
    }

    /**
     * Test if the current [Player] can summon. Send back messages if he can't.
     * @return true if the player can summon, false otherwise.
     */
    fun checkSummonerStatus(): Boolean {
        if (isMounted)
            return false

        if (isInOlympiadMode || isInObserverMode || isInsideZone(ZoneId.NO_SUMMON_FRIEND)) {
            sendPacket(SystemMessageId.YOU_MAY_NOT_SUMMON_FROM_YOUR_CURRENT_LOCATION)
            return false
        }
        return true
    }

    /**
     * Test if the [WorldObject] can be summoned. Send back messages if he can't.
     * @param target : The target to test.
     * @return true if the given target can be summoned, false otherwise.
     */
    fun checkSummonTargetStatus(target: WorldObject?): Boolean {
        if (target !is Player)
            return false

        val player = target as Player?

        if (player!!.isAlikeDead) {
            sendPacket(
                SystemMessage.getSystemMessage(SystemMessageId.S1_IS_DEAD_AT_THE_MOMENT_AND_CANNOT_BE_SUMMONED).addCharName(
                    player
                )
            )
            return false
        }

        if (player.isInStoreMode) {
            sendPacket(
                SystemMessage.getSystemMessage(SystemMessageId.S1_CURRENTLY_TRADING_OR_OPERATING_PRIVATE_STORE_AND_CANNOT_BE_SUMMONED).addCharName(
                    player
                )
            )
            return false
        }

        if (player.isRooted || player.isInCombat) {
            sendPacket(
                SystemMessage.getSystemMessage(SystemMessageId.S1_IS_ENGAGED_IN_COMBAT_AND_CANNOT_BE_SUMMONED).addCharName(
                    player
                )
            )
            return false
        }

        if (player.isInOlympiadMode) {
            sendPacket(SystemMessageId.YOU_CANNOT_SUMMON_PLAYERS_WHO_ARE_IN_OLYMPIAD)
            return false
        }

        if (player.isFestivalParticipant || player.isMounted) {
            sendPacket(SystemMessageId.YOUR_TARGET_IS_IN_AN_AREA_WHICH_BLOCKS_SUMMONING)
            return false
        }

        if (player.isInObserverMode || player.isInsideZone(ZoneId.NO_SUMMON_FRIEND)) {
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_IN_SUMMON_BLOCKING_AREA).addCharName(player))
            return false
        }

        return true
    }

    /**
     * @param z
     * @return true if character falling now On the start of fall return false for correct coord sync !
     */
    fun isFalling(z: Int): Boolean {
        if (isDead() || isFlying || isInsideZone(ZoneId.WATER))
            return false

        if (System.currentTimeMillis() < _fallingTimestamp)
            return true

        val deltaZ = z - z
        if (deltaZ <= baseTemplate.fallHeight)
            return false

        val damage = Formulas.calcFallDam(this, deltaZ).toInt()
        if (damage > 0) {
            reduceCurrentHp(Math.min(damage.toDouble(), currentHp - 1), null!!, false, true, null)
            sendPacket(SystemMessage.getSystemMessage(SystemMessageId.FALL_DAMAGE_S1).addNumber(damage))
        }

        setFalling()

        return false
    }

    /**
     * Set falling timestamp
     */
    fun setFalling() {
        _fallingTimestamp = System.currentTimeMillis() + FALLING_VALIDATION_DELAY
    }

    fun selectFriend(friendId: Int?) {
        if (!_selectedFriendList.contains(friendId))
            _selectedFriendList.add(friendId!!)
    }

    fun deselectFriend(friendId: Int?) {
        if (_selectedFriendList.contains(friendId))
            _selectedFriendList.remove(friendId!!)
    }

    private fun restoreFriendList() {
        _friendList.clear()

        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement("SELECT friend_id FROM character_friends WHERE char_id = ? AND relation = 0")
                    .use { ps ->
                        ps.setInt(1, objectId)

                        ps.executeQuery().use { rset ->
                            while (rset.next()) {
                                val friendId = rset.getInt("friend_id")
                                if (friendId == objectId)
                                    continue

                                _friendList.add(friendId)
                            }
                        }
                    }
            }
        } catch (e: Exception) {
            WorldObject.LOGGER.error("Couldn't restore {}'s friendlist.", e, name)
        }

    }

    private fun notifyFriends(login: Boolean) {
        for (id in _friendList) {
            val friend = World.getPlayer(id)
            if (friend != null) {
                friend.sendPacket(FriendList(friend))

                if (login)
                    friend.sendPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.FRIEND_S1_HAS_LOGGED_IN).addCharName(
                            this
                        )
                    )
            }
        }
    }

    fun selectBlock(friendId: Int?) {
        if (!_selectedBlocksList.contains(friendId))
            _selectedBlocksList.add(friendId!!)
    }

    fun deselectBlock(friendId: Int?) {
        if (_selectedBlocksList.contains(friendId))
            _selectedBlocksList.remove(friendId!!)
    }

    override fun broadcastRelationsChanges() {
        for (player in getKnownType(Player::class.java)) {
            val relation = getRelation(player)
            val isAutoAttackable = isAutoAttackable(player)

            player.sendPacket(RelationChanged(this, relation, isAutoAttackable))
            if (pet != null)
                player.sendPacket(RelationChanged(pet!!, relation, isAutoAttackable))
        }
    }

    override fun sendInfo(activeChar: Player) {
        if (isInBoat)
            position.set(boat!!.position)

        if (polyType === WorldObject.PolyType.NPC)
            activeChar.sendPacket(AbstractNpcInfo.PcMorphInfo(this, polyTemplate!!))
        else {
            activeChar.sendPacket(CharInfo(this))

            if (isSeated) {
                val `object` = World.getObject(_throneId)
                if (`object` is StaticObject)
                    activeChar.sendPacket(ChairSit(objectId, `object`.staticObjectId))
            }
        }

        var relation = getRelation(activeChar)
        var isAutoAttackable = isAutoAttackable(activeChar)

        activeChar.sendPacket(RelationChanged(this, relation, isAutoAttackable))
        if (pet != null)
            activeChar.sendPacket(RelationChanged(pet!!, relation, isAutoAttackable))

        relation = activeChar.getRelation(this)
        isAutoAttackable = activeChar.isAutoAttackable(this)

        sendPacket(RelationChanged(activeChar, relation, isAutoAttackable))
        if (activeChar.pet != null)
            sendPacket(RelationChanged(activeChar.pet!!, relation, isAutoAttackable))

        if (isInBoat)
            activeChar.sendPacket(GetOnVehicle(objectId, boat!!.objectId, boatPosition))

        when (storeType) {
            Player.StoreType.SELL, Player.StoreType.PACKAGE_SELL -> activeChar.sendPacket(PrivateStoreMsgSell(this))

            Player.StoreType.BUY -> activeChar.sendPacket(PrivateStoreMsgBuy(this))

            Player.StoreType.MANUFACTURE -> activeChar.sendPacket(RecipeShopMsg(this))
        }
    }

    fun teleportRequest(requester: Player?, skill: L2Skill?): Boolean {
        if (_summonTargetRequest != null && requester != null)
            return false

        _summonTargetRequest = requester
        _summonSkillRequest = skill
        return true
    }

    fun teleportAnswer(answer: Int, requesterId: Int) {
        if (_summonTargetRequest == null)
            return

        if (answer == 1 && _summonTargetRequest!!.objectId == requesterId)
            teleportToFriend(_summonTargetRequest, _summonSkillRequest)

        _summonTargetRequest = null
        _summonSkillRequest = null
    }

    fun activateGate(answer: Int, type: Int) {
        if (_requestedGate == null)
            return

        if (answer == 1 && target === _requestedGate) {
            if (type == 1)
                _requestedGate!!.openMe()
            else if (type == 0)
                _requestedGate!!.closeMe()
        }

        _requestedGate = null
    }

    fun setRequestedGate(door: Door) {
        _requestedGate = door
    }

    override fun polymorph(type: WorldObject.PolyType, npcId: Int): Boolean {
        if (super.polymorph(type, npcId)) {
            sendPacket(UserInfo(this))
            return true
        }
        return false
    }

    override fun unpolymorph() {
        super.unpolymorph()
        sendPacket(UserInfo(this))
    }

    override fun addKnownObject(`object`: WorldObject) {
        sendInfoFrom(`object`)
    }

    override fun removeKnownObject(`object`: WorldObject) {
        super.removeKnownObject(`object`)

        // send Server-Client Packet DeleteObject to the Player
        sendPacket(DeleteObject(`object`, `object` is Player && `object`.isSeated))
    }

    fun refreshInfos() {
        for (`object` in getKnownType(WorldObject::class.java)) {
            if (`object` is Player && `object`.isInObserverMode)
                continue

            sendInfoFrom(`object`)
        }
    }

    /**
     * teleToLocation method without Dimensional Rift check.
     * @param loc : The Location to teleport.
     */
    fun teleToLocation(loc: Location) {
        super.teleToLocation(loc, 0)
    }

    override fun teleToLocation(loc: Location?, randomOffset: Int) {
        var loc = loc
        if (DimensionalRiftManager.checkIfInRiftZone(x, y, z, true)) {
            sendMessage("You have been sent to the waiting room.")

            if (isInParty && party!!.isInDimensionalRift)
                party!!.dimensionalRift!!.usedTeleport(this)

            loc = DimensionalRiftManager.getRoom(0.toByte(), 0.toByte())!!.teleportLoc
        }
        super.teleToLocation(loc, randomOffset)
    }

    private fun sendInfoFrom(`object`: WorldObject) {
        if (`object`.polyType === WorldObject.PolyType.ITEM)
            sendPacket(SpawnItem(`object`))
        else {
            // send object info to player
            `object`.sendInfo(this)

            if (`object` is Creature) {
                // Update the state of the Creature object client side by sending Server->Client packet MoveToPawn/MoveToLocation and AutoAttackStart to the Player
                if (`object`.hasAI())
                    `object`.ai.describeStateToPlayer(this)
            }
        }
    }

    companion object {

        private val RESTORE_SKILLS_FOR_CHAR =
            "SELECT skill_id,skill_level FROM character_skills WHERE char_obj_id=? AND class_index=?"
        private val ADD_OR_UPDATE_SKILL =
            "INSERT INTO character_skills (char_obj_id,skill_id,skill_level,class_index) VALUES (?,?,?,?) ON DUPLICATE KEY UPDATE skill_level=VALUES(skill_level)"
        private val DELETE_SKILL_FROM_CHAR =
            "DELETE FROM character_skills WHERE skill_id=? AND char_obj_id=? AND class_index=?"
        private val DELETE_CHAR_SKILLS = "DELETE FROM character_skills WHERE char_obj_id=? AND class_index=?"

        private val ADD_SKILL_SAVE =
            "INSERT INTO character_skills_save (char_obj_id,skill_id,skill_level,effect_count,effect_cur_time,reuse_delay,systime,restore_type,class_index,buff_index) VALUES (?,?,?,?,?,?,?,?,?,?)"
        private val RESTORE_SKILL_SAVE =
            "SELECT skill_id,skill_level,effect_count,effect_cur_time, reuse_delay, systime, restore_type FROM character_skills_save WHERE char_obj_id=? AND class_index=? ORDER BY buff_index ASC"
        private val DELETE_SKILL_SAVE = "DELETE FROM character_skills_save WHERE char_obj_id=? AND class_index=?"

        private val INSERT_CHARACTER =
            "INSERT INTO characters (account_name,obj_Id,char_name,level,maxHp,curHp,maxCp,curCp,maxMp,curMp,face,hairStyle,hairColor,sex,exp,sp,karma,pvpkills,pkkills,clanid,race,classid,deletetime,cancraft,title,accesslevel,online,isin7sdungeon,clan_privs,wantspeace,base_class,nobless,power_grade) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"
        private val UPDATE_CHARACTER =
            "UPDATE characters SET level=?,maxHp=?,curHp=?,maxCp=?,curCp=?,maxMp=?,curMp=?,face=?,hairStyle=?,hairColor=?,sex=?,heading=?,x=?,y=?,z=?,exp=?,expBeforeDeath=?,sp=?,karma=?,pvpkills=?,pkkills=?,clanid=?,race=?,classid=?,deletetime=?,title=?,accesslevel=?,online=?,isin7sdungeon=?,clan_privs=?,wantspeace=?,base_class=?,onlinetime=?,punish_level=?,punish_timer=?,nobless=?,power_grade=?,subpledge=?,lvl_joined_academy=?,apprentice=?,sponsor=?,varka_ketra_ally=?,clan_join_expiry_time=?,clan_create_expiry_time=?,char_name=?,death_penalty_level=? WHERE obj_id=?"
        private val RESTORE_CHARACTER = "SELECT * FROM characters WHERE obj_id=?"

        private val RESTORE_CHAR_SUBCLASSES =
            "SELECT class_id,exp,sp,level,class_index FROM character_subclasses WHERE char_obj_id=? ORDER BY class_index ASC"
        private val ADD_CHAR_SUBCLASS =
            "INSERT INTO character_subclasses (char_obj_id,class_id,exp,sp,level,class_index) VALUES (?,?,?,?,?,?)"
        private val UPDATE_CHAR_SUBCLASS =
            "UPDATE character_subclasses SET exp=?,sp=?,level=?,class_id=? WHERE char_obj_id=? AND class_index =?"
        private val DELETE_CHAR_SUBCLASS = "DELETE FROM character_subclasses WHERE char_obj_id=? AND class_index=?"

        private val RESTORE_CHAR_HENNAS =
            "SELECT slot,symbol_id FROM character_hennas WHERE char_obj_id=? AND class_index=?"
        private val ADD_CHAR_HENNA =
            "INSERT INTO character_hennas (char_obj_id,symbol_id,slot,class_index) VALUES (?,?,?,?)"
        private val DELETE_CHAR_HENNA = "DELETE FROM character_hennas WHERE char_obj_id=? AND slot=? AND class_index=?"
        private val DELETE_CHAR_HENNAS = "DELETE FROM character_hennas WHERE char_obj_id=? AND class_index=?"
        private val DELETE_CHAR_SHORTCUTS = "DELETE FROM character_shortcuts WHERE char_obj_id=? AND class_index=?"

        private val RESTORE_CHAR_RECOMS = "SELECT char_id,target_id FROM character_recommends WHERE char_id=?"
        private val ADD_CHAR_RECOM = "INSERT INTO character_recommends (char_id,target_id) VALUES (?,?)"
        private val UPDATE_TARGET_RECOM_HAVE = "UPDATE characters SET rec_have=? WHERE obj_Id=?"
        private val UPDATE_CHAR_RECOM_LEFT = "UPDATE characters SET rec_left=? WHERE obj_Id=?"

        private val UPDATE_NOBLESS = "UPDATE characters SET nobless=? WHERE obj_Id=?"

        val REQUEST_TIMEOUT = 15

        private val COMPARE_SKILLS_BY_MIN_LVL = Comparator.comparing<GeneralSkillNode, Int> { it.minLvl }
        private val COMPARE_SKILLS_BY_LVL = Comparator.comparing<GeneralSkillNode, Int> { it.value }

        private val FALLING_VALIDATION_DELAY = 10000

        /**
         * Create a new Player and add it in the characters table of the database.
         *
         *  * Create a new Player with an account name
         *  * Set the name, the Hair Style, the Hair Color and the Face type of the Player
         *  * Add the player in the characters table of the database
         *
         * @param objectId Identifier of the object to initialized
         * @param template The L2PcTemplate to apply to the Player
         * @param accountName The name of the Player
         * @param name The name of the Player
         * @param hairStyle The hair style Identifier of the Player
         * @param hairColor The hair color Identifier of the Player
         * @param face The face type Identifier of the Player
         * @param sex The sex type Identifier of the Player
         * @return The Player added to the database or null
         */
        fun create(
            objectId: Int,
            template: PlayerTemplate,
            accountName: String,
            name: String,
            hairStyle: Byte,
            hairColor: Byte,
            face: Byte,
            sex: Sex
        ): Player? {
            // Create a new Player with an account name
            val app = PcAppearance(face, hairColor, hairStyle, sex)
            val player = Player(objectId, template, accountName, app)

            // Set the name of the Player
            player.name = name

            // Set access level
            player.setAccessLevel(Config.DEFAULT_ACCESS_LEVEL)

            // Cache few informations into CharNameTable.
            PlayerInfoTable.addPlayer(objectId, accountName, name, player.accessLevel!!.level)

            // Set the base class ID to that of the actual class ID.
            player.setBaseClass(player.classId)

            // Add the player in the characters table of the database
            try {
                L2DatabaseFactory.connection.use { con ->
                    con.prepareStatement(INSERT_CHARACTER).use { ps ->
                        ps.setString(1, accountName)
                        ps.setInt(2, player.objectId)
                        ps.setString(3, player.name)
                        ps.setInt(4, player.level)
                        ps.setInt(5, player.maxHp)
                        ps.setDouble(6, player.currentHp)
                        ps.setInt(7, player.maxCp)
                        ps.setDouble(8, player.currentCp)
                        ps.setInt(9, player.maxMp)
                        ps.setDouble(10, player.currentMp)
                        ps.setInt(11, player.appearance.face.toInt())
                        ps.setInt(12, player.appearance.hairStyle.toInt())
                        ps.setInt(13, player.appearance.hairColor.toInt())
                        ps.setInt(14, player.appearance.sex.ordinal)
                        ps.setLong(15, player.exp)
                        ps.setInt(16, player.sp)
                        ps.setInt(17, player.karma)
                        ps.setInt(18, player.pvpKills)
                        ps.setInt(19, player.pkKills)
                        ps.setInt(20, player.clanId)
                        ps.setInt(21, player.race!!.ordinal)
                        ps.setInt(22, player.classId.id)
                        ps.setLong(23, player.deleteTimer)
                        ps.setInt(24, if (player.hasDwarvenCraft()) 1 else 0)
                        ps.setString(25, player.title)
                        ps.setInt(26, player.accessLevel!!.level)
                        ps.setInt(27, player.isOnlineInt)
                        ps.setInt(28, if (player.isIn7sDungeon) 1 else 0)
                        ps.setInt(29, player.clanPrivileges)
                        ps.setInt(30, if (player.wantsPeace()) 1 else 0)
                        ps.setInt(31, player.baseClass)
                        ps.setInt(32, if (player.isNoble) 1 else 0)
                        ps.setLong(33, 0)
                        ps.executeUpdate()
                    }
                }
            } catch (e: Exception) {
                WorldObject.LOGGER.error("Couldn't create player {} for {} account.", e, name, accountName)
                return null
            }

            return player
        }

        /**
         * Retrieve a Player from the characters table of the database.
         *
         *  * Retrieve the Player from the characters table of the database
         *  * Set the x,y,z position of the Player and make it invisible
         *  * Update the overloaded status of the Player
         *
         * @param objectId Identifier of the object to initialized
         * @return The Player loaded from the database
         */
        fun restore(objectId: Int): Player? {
            var player: Player? = null

            try {
                L2DatabaseFactory.connection.use { con ->
                    val statement = con.prepareStatement(RESTORE_CHARACTER)
                    statement.setInt(1, objectId)

                    val rset = statement.executeQuery()
                    while (rset.next()) {
                        val activeClassId = rset.getInt("classid")
                        val template = PlayerData.getTemplate(activeClassId)!!
                        val app = PcAppearance(
                            rset.getByte("face"),
                            rset.getByte("hairColor"),
                            rset.getByte("hairStyle"),
                            Sex.values()[rset.getInt("sex")]
                        )

                        player = Player(objectId, template, rset.getString("account_name"), app)
                        player!!.name = rset.getString("char_name")
                        player!!.lastAccess = rset.getLong("lastAccess")

                        player!!.stat.exp = rset.getLong("exp")
                        player!!.stat.level = rset.getByte("level")
                        player!!.stat.sp = rset.getInt("sp")

                        player!!.expBeforeDeath = rset.getLong("expBeforeDeath")
                        player!!.setWantsPeace(rset.getInt("wantspeace") == 1)
                        player!!.heading = rset.getInt("heading")
                        player!!.karma = rset.getInt("karma")
                        player!!.pvpKills = rset.getInt("pvpkills")
                        player!!.pkKills = rset.getInt("pkkills")
                        player!!.setOnlineTime(rset.getLong("onlinetime"))
                        player!!.setNoble(rset.getInt("nobless") == 1, false)

                        player!!.clanJoinExpiryTime = rset.getLong("clan_join_expiry_time")
                        if (player!!.clanJoinExpiryTime < System.currentTimeMillis())
                            player!!.clanJoinExpiryTime = 0

                        player!!.clanCreateExpiryTime = rset.getLong("clan_create_expiry_time")
                        if (player!!.clanCreateExpiryTime < System.currentTimeMillis())
                            player!!.clanCreateExpiryTime = 0

                        player!!.powerGrade = rset.getInt("power_grade")
                        player!!.pledgeType = rset.getInt("subpledge")

                        val clanId = rset.getInt("clanid")
                        if (clanId > 0)
                            player!!.clan = ClanTable.getClan(clanId)

                        if (player!!.clan != null) {
                            if (player!!.clan!!.leaderId != player!!.objectId) {
                                if (player!!.powerGrade == 0)
                                    player!!.powerGrade = 5

                                player!!.clanPrivileges = player!!.clan!!.getPriviledgesByRank(player!!.powerGrade)
                            } else {
                                player!!.clanPrivileges = Clan.CP_ALL
                                player!!.powerGrade = 1
                            }
                        } else
                            player!!.clanPrivileges = Clan.CP_NOTHING

                        player!!.deleteTimer = rset.getLong("deletetime")
                        player!!.title = rset.getString("title")
                        player!!.setAccessLevel(rset.getInt("accesslevel"))
                        player!!.uptime = System.currentTimeMillis()
                        player!!.recomHave = rset.getInt("rec_have")
                        player!!.recomLeft = rset.getInt("rec_left")

                        player!!.classIndex = 0
                        try {
                            player!!.baseClass = rset.getInt("base_class")
                        } catch (e: Exception) {
                            player!!.baseClass = activeClassId
                        }

                        // Restore Subclass Data (cannot be done earlier in function)
                        if (restoreSubClassData(player!!) && activeClassId != player!!.baseClass) {
                            for (subClass in player!!.subClasses.values)
                                if (subClass.classId == activeClassId)
                                    player!!.classIndex = subClass.classIndex
                        }

                        // Subclass in use but doesn't exist in DB - a possible subclass cheat has been attempted. Switching to base class.
                        if (player!!.classIndex == 0 && activeClassId != player!!.baseClass)
                            player!!.setClassId(player!!.baseClass)
                        else
                            player!!._activeClass = activeClassId

                        player!!.apprentice = rset.getInt("apprentice")
                        player!!.sponsor = rset.getInt("sponsor")
                        player!!.lvlJoinedAcademy = rset.getInt("lvl_joined_academy")
                        player!!.isIn7sDungeon = rset.getInt("isin7sdungeon") == 1
                        player!!.setPunishLevel(rset.getInt("punish_level"))
                        player!!.punishTimer =
                            if (player!!.punishLevel == PunishLevel.NONE) 0 else rset.getLong("punish_timer")

                        CursedWeaponManager.checkPlayer(player)

                        player!!.allianceWithVarkaKetra = rset.getInt("varka_ketra_ally")

                        player!!.deathPenaltyBuffLevel = rset.getInt("death_penalty_level")

                        // Set the x,y,z position of the Player and make it invisible
                        player!!.position[rset.getInt("x"), rset.getInt("y")] = rset.getInt("z")

                        // Set Hero status if it applies
                        if (Hero.isActiveHero(objectId))
                            player!!.isHero = true

                        // Set pledge class rank.
                        player!!.pledgeClass = ClanMember.calculatePledgeClass(player!!)

                        // Retrieve from the database all secondary data of this Player and reward expertise/lucky skills if necessary.
                        // Note that Clan, Noblesse and Hero skills are given separately and not here.
                        player!!.restoreCharData()
                        player!!.giveSkills()

                        // buff and status icons
                        if (Config.STORE_SKILL_COOLTIME)
                            player!!.restoreEffects()

                        // Restore current CP, HP and MP values
                        val currentHp = rset.getDouble("curHp")

                        player!!.currentCp = rset.getDouble("curCp")
                        player!!.currentHp = currentHp
                        player!!.currentMp = rset.getDouble("curMp")

                        if (currentHp < 0.5) {
                            player!!.setIsDead(true)
                            player!!.stopHpMpRegeneration()
                        }

                        // Restore pet if it exists in the world.
                        val pet = World.getPet(player!!.objectId)
                        if (pet != null) {
                            player!!.pet = pet
                            pet.owner = player
                        }

                        player!!.refreshOverloaded()
                        player!!.refreshExpertisePenalty()

                        player!!.restoreFriendList()

                        // Retrieve the name and ID of the other characters assigned to this account.
                        val stmt =
                            con.prepareStatement("SELECT obj_Id, char_name FROM characters WHERE account_name=? AND obj_Id<>?")
                        stmt.setString(1, player!!._accountName)
                        stmt.setInt(2, objectId)
                        val chars = stmt.executeQuery()

                        while (chars.next())
                            player!!.accountChars[chars.getInt("obj_Id")] = chars.getString("char_name")

                        chars.close()
                        stmt.close()
                        break
                    }

                    rset.close()
                    statement.close()
                }
            } catch (e: Exception) {
                WorldObject.LOGGER.error("Couldn't restore player data.", e)
            }

            return player
        }

        /**
         * Restores sub-class data for the Player, used to check the current class index for the character.
         * @param player The player to make checks on.
         * @return true if successful.
         */
        private fun restoreSubClassData(player: Player): Boolean {
            try {
                L2DatabaseFactory.connection.use { con ->
                    con.prepareStatement(RESTORE_CHAR_SUBCLASSES).use { ps ->
                        ps.setInt(1, player.objectId)

                        ps.executeQuery().use { rs ->
                            while (rs.next()) {
                                val subClass = SubClass(
                                    rs.getInt("class_id"),
                                    rs.getInt("class_index"),
                                    rs.getLong("exp"),
                                    rs.getInt("sp"),
                                    rs.getByte("level")
                                )

                                // Enforce the correct indexing of _subClasses against their class indexes.
                                player.subClasses[subClass.classIndex] = subClass
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                WorldObject.LOGGER.error("Couldn't restore subclasses for {}.", e, player.name)
                return false
            }

            return true
        }

        val questInventoryLimit: Int
            get() = Config.INVENTORY_MAXIMUM_QUEST_ITEMS
    }
}
/**
 * Sit down the Player, set the AI Intention to REST and send ChangeWaitType packet (broadcast)
 */
/**
 * Remove a [L2Skill] from this [Player]. If parameter store is true, we also remove it from database and update shortcuts.
 * @param skillId : The skill identifier to remove.
 * @param store : If true, we delete the skill from database.
 * @return the L2Skill removed or null if it couldn't be removed.
 */