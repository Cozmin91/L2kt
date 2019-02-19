package com.l2kt.gameserver.scripting

import com.l2kt.Config
import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.commons.logging.CLogger
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.ItemTable
import com.l2kt.gameserver.data.cache.HtmCache
import com.l2kt.gameserver.data.manager.CastleManager
import com.l2kt.gameserver.data.manager.ZoneManager
import com.l2kt.gameserver.data.xml.NpcData
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.L2Spawn
import com.l2kt.gameserver.model.WorldObject
import com.l2kt.gameserver.model.actor.Creature
import com.l2kt.gameserver.model.actor.Npc
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.entity.Siege
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.model.location.SpawnLocation
import com.l2kt.gameserver.model.zone.ZoneType
import com.l2kt.gameserver.network.serverpackets.ActionFailed
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage
import com.l2kt.gameserver.scripting.scripts.ai.L2AttackableAIScript
import com.l2kt.gameserver.taskmanager.GameTimeTaskManager
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

open class Quest
/**
 * (Constructor)Add values to class variables and put the quest in HashMaps.
 * @param questId : int pointing out the ID of the quest
 * @param descr : String for the description of the quest
 */
    (
    /**
     * Return ID of the quest.
     * @return int
     */
    val questId: Int,
    /**
     * Return description of the quest.
     * @return String
     */
    val descr: String
) {

    private val _eventTimers = ConcurrentHashMap<Int, MutableList<QuestTimer>>()
    var onEnterWorld: Boolean = false
    private var _itemsIds: IntArray? = null

    /**
     * Returns the name of the script.
     * @return
     */
    val name: String
        get() = javaClass.simpleName

    /**
     * Return type of the quest.
     * @return boolean : True for (live) quest, False for script, AI, etc.
     */
    val isRealQuest: Boolean
        get() = questId > 0

    init {
        DF_REWARD_35[1] = 61
        DF_REWARD_35[4] = 45
        DF_REWARD_35[7] = 128
        DF_REWARD_35[11] = 168
        DF_REWARD_35[15] = 49
        DF_REWARD_35[19] = 61
        DF_REWARD_35[22] = 128
        DF_REWARD_35[26] = 168
        DF_REWARD_35[29] = 49
        DF_REWARD_35[32] = 61
        DF_REWARD_35[35] = 128
        DF_REWARD_35[39] = 168
        DF_REWARD_35[42] = 49
        DF_REWARD_35[45] = 61
        DF_REWARD_35[47] = 61
        DF_REWARD_35[50] = 49
        DF_REWARD_35[54] = 85
        DF_REWARD_35[56] = 85
    }

    init {
        DF_REWARD_37[0] = 96
        DF_REWARD_37[1] = 102
        DF_REWARD_37[2] = 98
        DF_REWARD_37[3] = 109
        DF_REWARD_37[4] = 50
    }

    init {
        DF_REWARD_39[1] = 72
        DF_REWARD_39[4] = 104
        DF_REWARD_39[7] = 96
        DF_REWARD_39[11] = 122
        DF_REWARD_39[15] = 60
        DF_REWARD_39[19] = 72
        DF_REWARD_39[22] = 96
        DF_REWARD_39[26] = 122
        DF_REWARD_39[29] = 45
        DF_REWARD_39[32] = 104
        DF_REWARD_39[35] = 96
        DF_REWARD_39[39] = 122
        DF_REWARD_39[42] = 60
        DF_REWARD_39[45] = 64
        DF_REWARD_39[47] = 72
        DF_REWARD_39[50] = 92
        DF_REWARD_39[54] = 82
        DF_REWARD_39[56] = 23
    }

    override fun toString(): String {
        return questId.toString() + " " + descr
    }

    /**
     * Return registered quest items.
     * @return int[]
     */
    fun getItemsIds(): IntArray? {
        return _itemsIds
    }

    /**
     * Registers all items that have to be destroyed in case player abort the quest or finish it.
     * @param itemIds
     */
    fun setItemsIds(vararg itemIds: Int) {
        _itemsIds = itemIds
    }

    /**
     * Add a new QuestState to the database and return it.
     * @param player
     * @return QuestState : QuestState created
     */
    fun newQuestState(player: Player): QuestState {
        return QuestState(player, this, STATE_CREATED)
    }

    /**
     * Auxiliary function for party quests. Checks the player's condition. Player member must be within Config.PARTY_RANGE distance from the npc. If npc is null, distance condition is ignored.
     * @param player : the instance of a player whose party is to be searched
     * @param npc : the instance of a L2Npc to compare distance
     * @param var : a tuple specifying a quest condition that must be satisfied for a party member to be considered.
     * @param value : a tuple specifying a quest condition that must be satisfied for a party member to be considered.
     * @return QuestState : The QuestState of that player.
     */
    fun checkPlayerCondition(player: Player?, npc: Npc?, `var`: String, value: String): QuestState? {
        // No valid player or npc instance is passed, there is nothing to check.
        if (player == null || npc == null)
            return null

        // Check player's quest conditions.
        val st = player.getQuestState(name) ?: return null

        // Condition exists? Condition has correct value?
        if (st[`var`] == null || !value.equals(st[`var`]!!, ignoreCase = true))
            return null

        // Player is in range?
        return if (!player.isInsideRadius(npc, Config.PARTY_RANGE, true, false)) null else st

    }

    /**
     * Auxiliary function for party quests. Note: This function is only here because of how commonly it may be used by quest developers. For any variations on this function, the quest script can always handle things on its own
     * @param player : the instance of a player whose party is to be searched
     * @param npc : the instance of a L2Npc to compare distance
     * @param var : a tuple specifying a quest condition that must be satisfied for a party member to be considered.
     * @param value : a tuple specifying a quest condition that must be satisfied for a party member to be considered.
     * @return List<Player> : List of party members that matches the specified condition, empty list if none matches. If the var is null, empty list is returned (i.e. no condition is applied). The party member must be within Config.PARTY_RANGE distance from the npc. If npc is null, distance
     * condition is ignored.
    </Player> */
    fun getPartyMembers(player: Player?, npc: Npc, `var`: String, value: String): List<QuestState> {
        if (player == null)
            return emptyList()

        val party = player.party
        if (party == null) {
            val st = checkPlayerCondition(player, npc, `var`, value)
            return if (st != null) Arrays.asList(st) else emptyList()
        }

        val list = ArrayList<QuestState>()
        for (member in party.members) {
            val st = checkPlayerCondition(member, npc, `var`, value)
            if (st != null)
                list.add(st)
        }
        return list
    }

    /**
     * Auxiliary function for party quests. Note: This function is only here because of how commonly it may be used by quest developers. For any variations on this function, the quest script can always handle things on its own
     * @param player : the instance of a player whose party is to be searched
     * @param npc : the instance of a L2Npc to compare distance
     * @param var : a tuple specifying a quest condition that must be satisfied for a party member to be considered.
     * @param value : a tuple specifying a quest condition that must be satisfied for a party member to be considered.
     * @return Player : Player for a random party member that matches the specified condition, or null if no match. If the var is null, null is returned (i.e. no condition is applied). The party member must be within 1500 distance from the npc. If npc is null, distance condition is ignored.
     */
    fun getRandomPartyMember(player: Player?, npc: Npc, `var`: String, value: String): QuestState? {
        // No valid player instance is passed, there is nothing to check.
        return if (player == null) null else Rnd[getPartyMembers(player, npc, `var`, value)]

        // Return random candidate.
    }

    /**
     * Auxiliary function for party quests. Note: This function is only here because of how commonly it may be used by quest developers. For any variations on this function, the quest script can always handle things on its own.
     * @param player : the instance of a player whose party is to be searched
     * @param npc : the instance of a L2Npc to compare distance
     * @param value : the value of the "cond" variable that must be matched
     * @return Player : Player for a random party member that matches the specified condition, or null if no match.
     */
    fun getRandomPartyMember(player: Player, npc: Npc, value: String): QuestState? {
        return getRandomPartyMember(player, npc, "cond", value)
    }

    /**
     * Auxiliary function for party quests. Checks the player's condition. Player member must be within Config.PARTY_RANGE distance from the npc. If npc is null, distance condition is ignored.
     * @param player : the instance of a player whose party is to be searched
     * @param npc : the instance of a L2Npc to compare distance
     * @param state : the state in which the party member's QuestState must be in order to be considered.
     * @return QuestState : The QuestState of that player.
     */
    fun checkPlayerState(player: Player?, npc: Npc?, state: Byte): QuestState? {
        // No valid player or npc instance is passed, there is nothing to check.
        if (player == null || npc == null)
            return null

        // Check player's quest conditions.
        val st = player.getQuestState(name) ?: return null

        // State correct?
        if (st.state != state)
            return null

        // Player is in range?
        return if (!player.isInsideRadius(npc, Config.PARTY_RANGE, true, false)) null else st

    }

    /**
     * Auxiliary function for party quests. Note: This function is only here because of how commonly it may be used by quest developers. For any variations on this function, the quest script can always handle things on its own.
     * @param player : the instance of a player whose party is to be searched
     * @param npc : the instance of a L2Npc to compare distance
     * @param state : the state in which the party member's QuestState must be in order to be considered.
     * @return List<Player> : List of party members that matches the specified quest state, empty list if none matches. The party member must be within Config.PARTY_RANGE distance from the npc. If npc is null, distance condition is ignored.
    </Player> */
    fun getPartyMembersState(player: Player?, npc: Npc, state: Byte): List<QuestState> {
        if (player == null)
            return emptyList()

        val party = player.party
        if (party == null) {
            val st = checkPlayerState(player, npc, state)
            return if (st != null) Arrays.asList(st) else emptyList()
        }

        val list = ArrayList<QuestState>()
        for (member in party.members) {
            val st = checkPlayerState(member, npc, state)
            if (st != null)
                list.add(st)
        }
        return list
    }

    /**
     * Auxiliary function for party quests. Note: This function is only here because of how commonly it may be used by quest developers. For any variations on this function, the quest script can always handle things on its own.
     * @param player : the instance of a player whose party is to be searched
     * @param npc : the instance of a monster to compare distance
     * @param state : the state in which the party member's QuestState must be in order to be considered.
     * @return Player: Player for a random party member that matches the specified condition, or null if no match. If the var is null, any random party member is returned (i.e. no condition is applied).
     */
    fun getRandomPartyMemberState(player: Player?, npc: Npc, state: Byte): QuestState? {
        // No valid player instance is passed, there is nothing to check.
        return if (player == null) null else Rnd[getPartyMembersState(player, npc, state)]

        // Return random candidate.
    }

    /**
     * Retrieves the clan leader quest state.
     * @param player : the player to test
     * @param npc : the npc to test distance
     * @return the QuestState of the leader, or null if not found
     */
    fun getClanLeaderQuestState(player: Player?, npc: Npc): QuestState? {
        // No valid player instance is passed, there is nothing to check.
        if (player == null)
            return null

        // If player is the leader, retrieves directly the qS and bypass others checks
        if (player.isClanLeader && player.isInsideRadius(npc, Config.PARTY_RANGE, true, false))
            return player.getQuestState(name)

        // Verify if the player got a clan
        val clan = player.clan ?: return null

        // Verify if the leader is online
        val leader = clan.leader?.playerInstance ?: return null

        // Verify if the player is on the radius of the leader. If true, send leader's quest state.
        return if (leader.isInsideRadius(npc, Config.PARTY_RANGE, true, false)) leader.getQuestState(name) else null

    }

    /**
     * Add a timer to the quest, if it doesn't exist already. If the timer is repeatable, it will auto-fire automatically, at a fixed rate, until explicitly canceled.
     * @param name name of the timer (also passed back as "event" in onAdvEvent)
     * @param time time in ms for when to fire the timer
     * @param npc npc associated with this timer (can be null)
     * @param player player associated with this timer (can be null)
     * @param repeating indicates if the timer is repeatable or one-time.
     */
    fun startQuestTimer(name: String, time: Long, npc: Npc?, player: Player?, repeating: Boolean) {
        // Get quest timers for this timer type.
        var timers: MutableList<QuestTimer>? = _eventTimers[name.hashCode()]
        if (timers == null) {
            // None timer exists, create new list.
            timers = CopyOnWriteArrayList()

            // Add new timer to the list.
            timers.add(QuestTimer(this, name, npc, player, time, repeating))

            // Add timer list to the map.
            _eventTimers[name.hashCode()] = timers
        } else {
            // Check, if specific timer already exists.
            for (timer in timers) {
                // If so, return.
                if (timer.equals(this, name, npc, player))
                    return
            }

            // Add new timer to the list.
            timers.add(QuestTimer(this, name, npc, player, time, repeating))
        }
    }

    fun getQuestTimer(name: String, npc: Npc?, player: Player?): QuestTimer? {
        // Get quest timers for this timer type.
        val timers = _eventTimers[name.hashCode()]

        // Timer list does not exists or is empty, return.
        if (timers == null || timers.isEmpty())
            return null

        // Check, if specific timer exists.
        for (timer in timers) {
            // If so, return him.
            if (timer.equals(this, name, npc, player))
                return timer
        }
        return null
    }

    fun cancelQuestTimer(name: String, npc: Npc?, player: Player?) {
        // If specified timer exists, cancel him.
        val timer = getQuestTimer(name, npc, player)
        timer?.cancel()
    }

    fun cancelQuestTimers(name: String) {
        // Get quest timers for this timer type.
        val timers = _eventTimers[name.hashCode()]

        // Timer list does not exists or is empty, return.
        if (timers == null || timers.isEmpty())
            return

        // Cancel all quest timers.
        for (timer in timers) {
            timer?.cancel()
        }
    }

    // Note, keep it default. It is used withing QuestTimer, when it terminates.
    /**
     * Removes QuestTimer from timer list, when it terminates.
     * @param timer : QuestTimer, which is beeing terminated.
     */
    internal fun removeQuestTimer(timer: QuestTimer?) {
        // Timer does not exist, return.
        if (timer == null)
            return

        // Get quest timers for this timer type.
        val timers = _eventTimers[timer.toString().hashCode()]

        // Timer list does not exists or is empty, return.
        if (timers == null || timers.isEmpty())
            return

        // Remove timer from the list.
        timers.remove(timer)
    }

    /**
     * Add a temporary (quest) spawn on the location of a character.
     * @param npcId the NPC template to spawn.
     * @param cha the position where to spawn it.
     * @param randomOffset
     * @param despawnDelay
     * @param isSummonSpawn if true, spawn with animation (if any exists).
     * @return instance of the newly spawned npc with summon animation.
     */
    fun addSpawn(npcId: Int, cha: Creature, randomOffset: Boolean, despawnDelay: Long, isSummonSpawn: Boolean): Npc? {
        return addSpawn(npcId, cha.x, cha.y, cha.z, cha.heading, randomOffset, despawnDelay, isSummonSpawn)
    }

    /**
     * Add a temporary (quest) spawn on the Location object.
     * @param npcId the NPC template to spawn.
     * @param loc the position where to spawn it.
     * @param randomOffset
     * @param despawnDelay
     * @param isSummonSpawn if true, spawn with animation (if any exists).
     * @return instance of the newly spawned npc with summon animation.
     */
    fun addSpawn(
        npcId: Int,
        loc: SpawnLocation,
        randomOffset: Boolean,
        despawnDelay: Long,
        isSummonSpawn: Boolean
    ): Npc? {
        return addSpawn(npcId, loc.x, loc.y, loc.z, loc.heading, randomOffset, despawnDelay, isSummonSpawn)
    }

    /**
     * Add a temporary (quest) spawn on the location of a character.
     * @param npcId the NPC template to spawn.
     * @param x
     * @param y
     * @param z
     * @param heading
     * @param randomOffset
     * @param despawnDelay
     * @param isSummonSpawn if true, spawn with animation (if any exists).
     * @return instance of the newly spawned npc with summon animation.
     */
    fun addSpawn(
        npcId: Int,
        x: Int,
        y: Int,
        z: Int,
        heading: Int,
        randomOffset: Boolean,
        despawnDelay: Long,
        isSummonSpawn: Boolean
    ): Npc? {
        var x = x
        var y = y
        try {
            val template = NpcData.getTemplate(npcId) ?: return null

            if (randomOffset) {
                x += Rnd[-100, 100]
                y += Rnd[-100, 100]
            }

            val spawn = L2Spawn(template)
            spawn.setLoc(x, y, z + 20, heading)
            spawn.setRespawnState(false)

            val npc = spawn.doSpawn(isSummonSpawn)
            if (despawnDelay > 0)
                npc!!.scheduleDespawn(despawnDelay)

            return npc
        } catch (e: Exception) {
            LOGGER.error("Couldn't spawn npcId {} for {}.", npcId, toString())
            return null
        }

    }

    /**
     * Show a message to player.<BR></BR>
     * <BR></BR>
     * <U><I>Concept : </I></U><BR></BR>
     * 3 cases are managed according to the value of the parameter "res" :
     * <UL>
     * <LI><U>"res" ends with string ".html" :</U> an HTML is opened in order to be shown in a dialog box</LI>
     * <LI><U>"res" starts with "<html>" :</html></U> the message hold in "res" is shown in a dialog box</LI>
     * <LI><U>otherwise :</U> the message held in "res" is shown in chat box</LI>
    </UL> *
     * @param npc : which launches the dialog, null in case of random scripts
     * @param creature : the creature to test.
     * @param result : String pointing out the message to show at the player
     */
    protected fun showResult(npc: Npc?, creature: Creature?, result: String?) {
        if (creature == null)
            return

        val player = creature.actingPlayer
        if (player == null || result == null || result.isEmpty())
            return

        if (result.endsWith(".htm") || result.endsWith(".html")) {
            val npcReply = NpcHtmlMessage(npc?.npcId ?: 0)
            if (isRealQuest)
                npcReply.setFile("./data/html/scripts/quests/$name/$result")
            else
                npcReply.setFile("./data/html/scripts/$descr/$name/$result")

            if (npc != null)
                npcReply.replace("%objectId%", npc.objectId)

            player.sendPacket(npcReply)
            player.sendPacket(ActionFailed.STATIC_PACKET)
        } else if (result.startsWith("<html>")) {
            val npcReply = NpcHtmlMessage(npc?.npcId ?: 0)
            npcReply.setHtml(result)

            if (npc != null)
                npcReply.replace("%objectId%", npc.objectId)

            player.sendPacket(npcReply)
            player.sendPacket(ActionFailed.STATIC_PACKET)
        } else
            player.sendMessage(result)
    }

    /**
     * Returns String representation of given quest html.
     * @param fileName : the filename to send.
     * @return String : message sent to client.
     */
    fun getHtmlText(fileName: String): String {
        return if (isRealQuest) HtmCache.getHtmForce("./data/html/scripts/quests/$name/$fileName") else HtmCache.getHtmForce(
            "./data/html/scripts/$descr/$name/$fileName"
        )

    }

    /**
     * Add this quest to the list of quests that the passed mob will respond to for the specified Event type.
     * @param npcId : id of the NPC to register
     * @param eventType : type of event being registered
     */
    fun addEventId(npcId: Int, eventType: EventType) {
        val t = NpcData.getTemplate(npcId)
        t?.addQuestEvent(eventType, this)
    }

    /**
     * Add this script to the list of script that the passed mob will respond to for the specified Event type.
     * @param npcId : id of the NPC to register
     * @param eventTypes : types of events being registered
     */
    fun addEventIds(npcId: Int, vararg eventTypes: EventType) {
        val t = NpcData.getTemplate(npcId)
        if (t != null)
            for (eventType in eventTypes)
                t.addQuestEvent(eventType, this)
    }

    /**
     * Register monsters on particular event types.
     * @param npcIds An array of mobs.
     * @param eventTypes Types of event to register mobs on.
     */
    fun addEventIds(npcIds: IntArray, vararg eventTypes: EventType) {
        for (id in npcIds)
            addEventIds(id, *eventTypes)
    }

    /**
     * Register monsters on particular event types.
     * @param npcIds An array of mobs.
     * @param eventTypes Types of event to register mobs on.
     */
    fun addEventIds(npcIds: Iterable<Int>, vararg eventTypes: EventType) {
        for (id in npcIds)
            addEventIds(id, *eventTypes)
    }

    /**
     * Add the quest to the NPC's startQuest
     * @param npcIds A serie of ids.
     */
    fun addStartNpc(vararg npcIds: Int) {
        for (npcId in npcIds)
            addEventId(npcId, EventType.QUEST_START)
    }

    /**
     * Add this quest to the list of quests that the passed mob will respond to for Attack Events.
     * @param npcIds A serie of ids.
     */
    fun addAttackId(vararg npcIds: Int) {
        for (npcId in npcIds)
            addEventId(npcId, EventType.ON_ATTACK)
    }

    /**
     * Quest event listener for NPC under attack.
     * @param npc : Attacked npc instance.
     * @param attacker : The Creature who attacks the Npc.
     * @param damage : The amount of given damage.
     * @param skill : The skill used to attack the Npc (can be null).
     */
    fun notifyAttack(npc: Npc, attacker: Creature, damage: Int, skill: L2Skill?) {
        var res: String? = null
        try {
            res = onAttack(npc, attacker, damage, skill)
        } catch (e: Exception) {
            LOGGER.warn(toString(), e)
            return
        }

        showResult(npc, attacker, res)
    }

    open fun onAttack(npc: Npc, attacker: Creature, damage: Int, skill: L2Skill?): String? {
        return null
    }

    /**
     * Add this quest to the list of quests that the passed mob will respond to for AttackAct Events.
     * @param npcIds A serie of ids.
     */
    fun addAttackActId(vararg npcIds: Int) {
        for (npcId in npcIds)
            addEventId(npcId, EventType.ON_ATTACK_ACT)
    }

    /**
     * Quest event notifycator for player being attacked by NPC.
     * @param npc Npc providing attack.
     * @param victim Attacked npc player.
     */
    fun notifyAttackAct(npc: Npc, victim: Player) {
        var res: String? = null
        try {
            res = onAttackAct(npc, victim)
        } catch (e: Exception) {
            LOGGER.warn(toString(), e)
            return
        }

        showResult(npc, victim, res)
    }

    open fun onAttackAct(npc: Npc, victim: Player): String? {
        return null
    }

    /**
     * Add this quest to the list of quests that the passed npc will respond to for Character See Events.
     * @param npcIds : A serie of ids.
     */
    fun addAggroRangeEnterId(vararg npcIds: Int) {
        for (npcId in npcIds)
            addEventId(npcId, EventType.ON_AGGRO)
    }

    private inner class OnAggroEnter(private val _npc: Npc, private val _pc: Player, private val _isPet: Boolean) :
        Runnable {

        override fun run() {
            var res: String? = null
            try {
                res = onAggro(_npc, _pc, _isPet)
            } catch (e: Exception) {
                LOGGER.warn(toString(), e)
                return
            }

            showResult(_npc, _pc, res)

        }
    }

    fun notifyAggro(npc: Npc, player: Player, isPet: Boolean) {
        ThreadPool.execute(OnAggroEnter(npc, player, isPet))
    }

    open fun onAggro(npc: Npc, player: Player?, isPet: Boolean): String? {
        return null
    }

    fun notifyDeath(killer: Creature, player: Player) {
        var res: String? = null
        try {
            res = onDeath(killer, player)
        } catch (e: Exception) {
            LOGGER.warn(toString(), e)
            return
        }

        showResult(if (killer is Npc) killer else null, player, res)
    }

    open fun onDeath(killer: Creature, player: Player): String? {
        return onAdvEvent("", if (killer is Npc) killer else null, player)
    }

    fun notifyEvent(event: String, npc: Npc?, player: Player?) {
        var res: String? = null
        try {
            res = onAdvEvent(event, npc, player)
        } catch (e: Exception) {
            LOGGER.warn(toString(), e)
            return
        }

        showResult(npc, player, res)
    }

    open fun onAdvEvent(event: String, npc: Npc?, player: Player?): String? {
        // if not overridden by a subclass, then default to the returned value of the simpler (and older) onEvent override
        // if the player has a state, use it as parameter in the next call, else return null
        if (player != null) {
            val qs = player.getQuestState(name)
            if (qs != null)
                return onEvent(event, qs)
        }
        return null
    }

    fun onEvent(event: String, qs: QuestState): String? {
        return null
    }

    fun notifyEnterWorld(player: Player) {
        var res: String? = null
        try {
            res = onEnterWorld(player)
        } catch (e: Exception) {
            LOGGER.warn(toString(), e)
            return
        }

        showResult(null, player, res)
    }

    fun onEnterWorld(player: Player): String? {
        return null
    }

    /**
     * Add this quest to the list of quests that triggers, when player enters specified zones.
     * @param zoneIds : A serie of zone ids.
     */
    fun addEnterZoneId(vararg zoneIds: Int) {
        for (zoneId in zoneIds) {
            val zone = ZoneManager.getZoneById(zoneId)
            zone?.addQuestEvent(EventType.ON_ENTER_ZONE, this)
        }
    }

    fun notifyEnterZone(character: Creature, zone: ZoneType) {
        var res: String? = null
        try {
            res = onEnterZone(character, zone)
        } catch (e: Exception) {
            LOGGER.warn(toString(), e)
            return
        }

        showResult(null, character, res)
    }

    open fun onEnterZone(character: Creature, zone: ZoneType): String? {
        return null
    }

    /**
     * Add this quest to the list of quests that triggers, when player leaves specified zones.
     * @param zoneIds : A serie of zone ids.
     */
    fun addExitZoneId(vararg zoneIds: Int) {
        for (zoneId in zoneIds) {
            val zone = ZoneManager.getZoneById(zoneId)
            zone?.addQuestEvent(EventType.ON_EXIT_ZONE, this)
        }
    }

    fun notifyExitZone(character: Creature, zone: ZoneType) {
        var res: String? = null
        try {
            res = onExitZone(character, zone)
        } catch (e: Exception) {
            LOGGER.warn(toString(), e)
            return
        }

        showResult(null, character, res)
    }

    open fun onExitZone(character: Creature, zone: ZoneType): String? {
        return null
    }

    /**
     * Add this quest to the list of quests that the passed npc will respond to for Faction Call Events.
     * @param npcIds : A serie of ids.
     */
    fun addFactionCallId(vararg npcIds: Int) {
        for (npcId in npcIds)
            addEventId(npcId, EventType.ON_FACTION_CALL)
    }

    fun notifyFactionCall(npc: Npc?, caller: Npc?, attacker: Player?, isPet: Boolean) {
        var res: String? = null
        try {
            res = onFactionCall(npc, caller, attacker, isPet)
        } catch (e: Exception) {
            LOGGER.warn(toString(), e)
            return
        }

        showResult(npc, attacker, res)
    }

    open fun onFactionCall(npc: Npc?, caller: Npc?, attacker: Player?, isPet: Boolean): String? {
        return null
    }

    /**
     * Add the quest to the NPC's first-talk (default action dialog)
     * @param npcIds A serie of ids.
     */
    fun addFirstTalkId(vararg npcIds: Int) {
        for (npcId in npcIds)
            addEventId(npcId, EventType.ON_FIRST_TALK)
    }

    fun notifyFirstTalk(npc: Npc, player: Player) {
        var res: String? = null
        try {
            res = onFirstTalk(npc, player)
        } catch (e: Exception) {
            LOGGER.warn(toString(), e)
            return
        }

        // if the quest returns text to display, display it.
        if (res != null && res.isNotEmpty())
            showResult(npc, player, res)
        else
            player.sendPacket(ActionFailed.STATIC_PACKET)
    }

    open fun onFirstTalk(npc: Npc, player: Player): String? {
        return null
    }

    /**
     * Add the quest to an array of items templates.
     * @param itemIds A serie of ids.
     */
    fun addItemUse(vararg itemIds: Int) {
        for (itemId in itemIds) {
            val t = ItemTable.getTemplate(itemId)
            t?.addQuestEvent(this)
        }
    }

    fun notifyItemUse(item: ItemInstance, player: Player, target: WorldObject?) {
        var res: String? = null
        try {
            res = onItemUse(item, player, target)
        } catch (e: Exception) {
            LOGGER.warn(toString(), e)
            return
        }

        showResult(null, player, res)
    }

    open fun onItemUse(item: ItemInstance, player: Player, target: WorldObject?): String? {
        return null
    }

    /**
     * Add this quest to the list of quests that the passed mob will respond to for Kill Events.
     * @param killIds A serie of ids.
     */
    fun addKillId(vararg killIds: Int) {
        for (killId in killIds)
            addEventId(killId, EventType.ON_KILL)
    }

    fun notifyKill(npc: Npc, killer: Creature) {
        var res: String? = null
        try {
            res = onKill(npc, killer)
        } catch (e: Exception) {
            LOGGER.warn(toString(), e)
            return
        }

        showResult(npc, killer, res)
    }

    open fun onKill(npc: Npc, killer: Creature): String? {
        return null
    }

    /**
     * Add this quest to the list of quests that the passed npc will respond to for Spawn Events.
     * @param npcIds : A serie of ids.
     */
    fun addSpawnId(vararg npcIds: Int) {
        for (npcId in npcIds)
            addEventId(npcId, EventType.ON_SPAWN)
    }

    fun notifySpawn(npc: Npc) {
        try {
            onSpawn(npc)
        } catch (e: Exception) {
            LOGGER.error(toString(), e)
        }

    }

    open fun onSpawn(npc: Npc): String? {
        return null
    }

    /**
     * Add this quest to the list of quests that the passed npc will respond to for Decay Events.
     * @param npcIds : A serie of ids.
     */
    fun addDecayId(vararg npcIds: Int) {
        for (npcId in npcIds)
            addEventId(npcId, EventType.ON_DECAY)
    }

    fun notifyDecay(npc: Npc) {
        try {
            onDecay(npc)
        } catch (e: Exception) {
            LOGGER.error(toString(), e)
        }

    }

    fun onDecay(npc: Npc): String? {
        return null
    }

    /**
     * Add this quest to the list of quests that the passed npc will respond to for Skill-See Events.
     * @param npcIds : A serie of ids.
     */
    fun addSkillSeeId(vararg npcIds: Int) {
        for (npcId in npcIds)
            addEventId(npcId, EventType.ON_SKILL_SEE)
    }

    inner class OnSkillSee(
        private val _npc: Npc,
        private val _caster: Player,
        private val _skill: L2Skill,
        private val _targets: Array<WorldObject>,
        private val _isPet: Boolean
    ) : Runnable {

        override fun run() {
            var res: String? = null
            try {
                res = onSkillSee(_npc, _caster, _skill, _targets, _isPet)
            } catch (e: Exception) {
                LOGGER.warn(toString(), e)
                return
            }

            showResult(_npc, _caster, res)

        }
    }

    fun notifySkillSee(npc: Npc, caster: Player, skill: L2Skill, targets: Array<WorldObject>, isPet: Boolean) {
        ThreadPool.execute(OnSkillSee(npc, caster, skill, targets, isPet))
    }

    open fun onSkillSee(
        npc: Npc,
        caster: Player?,
        skill: L2Skill?,
        targets: Array<WorldObject>,
        isPet: Boolean
    ): String? {
        return null
    }

    /**
     * Add this quest to the list of quests that the passed npc will respond to any skill being used by other npcs or players.
     * @param npcIds : A serie of ids.
     */
    fun addSpellFinishedId(vararg npcIds: Int) {
        for (npcId in npcIds)
            addEventId(npcId, EventType.ON_SPELL_FINISHED)
    }

    fun notifySpellFinished(npc: Npc, player: Player?, skill: L2Skill?) {
        var res: String? = null
        try {
            res = onSpellFinished(npc, player, skill)
        } catch (e: Exception) {
            LOGGER.warn(toString(), e)
            return
        }

        showResult(npc, player, res)
    }

    open fun onSpellFinished(npc: Npc, player: Player?, skill: L2Skill?): String? {
        return null
    }

    /**
     * Add this quest to the list of quests that the passed npc will respond to for Talk Events.
     * @param talkIds : A serie of ids.
     */
    fun addTalkId(vararg talkIds: Int) {
        for (talkId in talkIds)
            addEventId(talkId, EventType.ON_TALK)
    }

    fun notifyTalk(npc: Npc, player: Player) {
        var res: String? = null
        try {
            res = onTalk(npc, player)
        } catch (e: Exception) {
            LOGGER.warn(toString(), e)
            return
        }

        player.lastQuestNpcObject = npc.objectId
        showResult(npc, player, res)
    }

    open fun onTalk(npc: Npc, player: Player): String? {
        return null
    }

    fun addSiegeNotify(castleId: Int): Siege {
        val siege = CastleManager.getCastleById(castleId)!!.siege
        siege!!.addQuestEvent(this)
        return siege
    }

    open fun onSiegeEvent() {}

    /**
     * Add this quest to the list of quests which interact with game time system.
     */
    fun addGameTimeNotify() {
        GameTimeTaskManager.addQuestEvent(this)
    }

    open fun onGameTime() {}

    override fun equals(o: Any?): Boolean {
        // core AIs are available only in one instance (in the list of event of NpcTemplate)
        if (o is L2AttackableAIScript && this is L2AttackableAIScript)
            return true

        if (o is Quest) {
            val q = o as Quest?
            return if (questId > 0 && questId == q!!.questId) name == q.name else javaClass.name == q!!.javaClass.name

            // Scripts may have same names, while being in different sub-package
        }

        return false
    }

    /**
     * @param player : The player instance to check.
     * @return true if the given player got an online clan member sponsor in a 1500 radius range.
     */
    fun getSponsor(player: Player): Boolean {
        // Player hasn't a sponsor.
        val sponsorId = player.sponsor
        if (sponsorId == 0)
            return false

        // Player hasn't a clan.
        val clan = player.clan ?: return false

        // Retrieve sponsor clan member object.
        val member = clan.getClanMember(sponsorId)
        if (member != null && member.isOnline) {
            // The sponsor is online, retrieve player instance and check distance.
            val sponsor = member.playerInstance
            if (sponsor != null && player.isInsideRadius(sponsor, 1500, true, false))
                return true
        }

        return false
    }

    /**
     * @param player : The player instance to check.
     * @return the apprentice of the given player. He must be online, and in a 1500 radius range.
     */
    fun getApprentice(player: Player): Player? {
        // Player hasn't an apprentice.
        val apprenticeId = player.apprentice
        if (apprenticeId == 0)
            return null

        // Player hasn't a clan.
        val clan = player.clan ?: return null

        // Retrieve apprentice clan member object.
        val member = clan.getClanMember(apprenticeId)
        if (member != null && member.isOnline) {
            // The apprentice is online, retrieve player instance and check distance.
            val academic = member.playerInstance
            if (academic != null && player.isInsideRadius(academic, 1500, true, false))
                return academic
        }

        return null
    }

    companion object {
        @JvmField val LOGGER = CLogger(Quest::class.java.name)

        /**
         * @return default html page "You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements."
         */
        const val noQuestMsg =
            "<html><body>You are either not on a quest that involves this NPC, or you don't meet this NPC's minimum quest requirements.</body></html>"
        /**
         * @return default html page "This quest has already been completed."
         */
        const val alreadyCompletedMsg = "<html><body>This quest has already been completed.</body></html>"
        /**
         * @return default html page "You have already accepted the maximum number of quests. No more than 25 quests may be undertaken simultaneously. For quest information, enter Alt+U."
         */
        const val tooMuchQuestsMsg =
            "<html><body>You have already accepted the maximum number of quests. No more than 25 quests may be undertaken simultaneously.<br>For quest information, enter Alt+U.</body></html>"

        const val STATE_CREATED: Byte = 0
        const val STATE_STARTED: Byte = 1
        const val STATE_COMPLETED: Byte = 2

        // Dimensional Diamond Rewards by Class for 2nd class transfer quest (35)
        @JvmField val DF_REWARD_35: MutableMap<Int, Int> = HashMap()

        // Dimensional Diamond Rewards by Race for 2nd class transfer quest (37)
        @JvmField val DF_REWARD_37: MutableMap<Int, Int> = HashMap()

        // Dimensional Diamond Rewards by Class for 2nd class transfer quest (39)
        @JvmField val DF_REWARD_39: MutableMap<Int, Int> = HashMap()
    }
}