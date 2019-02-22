package com.l2kt.gameserver.model.actor

import com.l2kt.Config
import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.commons.lang.StringUtil
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.ItemTable
import com.l2kt.gameserver.data.SkillTable
import com.l2kt.gameserver.data.cache.HtmCache
import com.l2kt.gameserver.data.manager.DimensionalRiftManager
import com.l2kt.gameserver.data.manager.LotteryManager
import com.l2kt.gameserver.data.sql.ClanTable
import com.l2kt.gameserver.data.xml.MultisellData
import com.l2kt.gameserver.data.xml.NewbieBuffData
import com.l2kt.gameserver.data.xml.ScriptData
import com.l2kt.gameserver.extensions.*
import com.l2kt.gameserver.geoengine.GeoEngine
import com.l2kt.gameserver.idfactory.IdFactory
import com.l2kt.gameserver.model.*
import com.l2kt.gameserver.model.actor.ai.CtrlIntention
import com.l2kt.gameserver.model.actor.instance.Merchant
import com.l2kt.gameserver.model.actor.instance.Monster
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.actor.stat.NpcStat
import com.l2kt.gameserver.model.actor.status.NpcStatus
import com.l2kt.gameserver.model.actor.template.NpcTemplate
import com.l2kt.gameserver.model.actor.template.NpcTemplate.AIType
import com.l2kt.gameserver.model.actor.template.NpcTemplate.Race
import com.l2kt.gameserver.model.actor.template.NpcTemplate.SkillType
import com.l2kt.gameserver.model.entity.Castle
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.model.item.kind.Item
import com.l2kt.gameserver.model.item.kind.Weapon
import com.l2kt.gameserver.model.pledge.Clan
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.clientpackets.Say2
import com.l2kt.gameserver.network.serverpackets.AbstractNpcInfo.NpcInfo
import com.l2kt.gameserver.network.serverpackets.*
import com.l2kt.gameserver.scripting.EventType
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.QuestState
import com.l2kt.gameserver.taskmanager.DecayTaskManager
import com.l2kt.gameserver.taskmanager.RandomAnimationTaskManager
import com.l2kt.gameserver.templates.skills.L2SkillType

import java.text.DateFormat
import java.util.ArrayList
import java.util.Arrays

/**
 * An instance type extending [Creature], which represents a Non Playable Character (or NPC) in the world.
 */
open class Npc
/**
 * Constructor of L2Npc (use Creature constructor).<BR></BR>
 * <BR></BR>
 * <B><U> Actions</U> :</B><BR></BR>
 * <BR></BR>
 *  * Call the Creature constructor to set the _template of the Creature (copy skills from template to object and link _calculators to NPC_STD_CALCULATOR)
 *  * Set the name of the Creature
 *  * Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it<BR></BR>
 * <BR></BR>
 * @param objectId Identifier of the object to initialized
 * @param template The L2NpcTemplate to apply to the NPC
 */
    (objectId: Int, template: NpcTemplate) : Creature(objectId, template) {

    /**
     * @return the L2Spawn object that manage this L2Npc.
     */
    /**
     * Set the spawn of the L2Npc.<BR></BR>
     * <BR></BR>
     * @param spawn The L2Spawn that manage the L2Npc
     */
    var spawn: L2Spawn? = null

    @Volatile
    var isDecayed = false

    var spoilerId = 0

    private var _lastSocialBroadcast: Long = 0

    /**
     * @return the Identifier of the item in the left hand of this L2Npc contained in the L2NpcTemplate.
     */
    var leftHandItem: Int = 0
        private set
    /**
     * @return the Identifier of the item in the right hand of this L2Npc contained in the L2NpcTemplate.
     */
    var rightHandItem: Int = 0
        private set
    var enchantEffect: Int = 0
        private set

    private var _currentCollisionHeight: Double = 0.toDouble() // used for npc grow effect skills
    private var _currentCollisionRadius: Double = 0.toDouble() // used for npc grow effect skills

    private var _currentSsCount = 0
    private var _currentSpsCount = 0
    private var _shotsMask = 0

    var scriptValue = 0

    /**
     * @return the L2Castle this L2Npc belongs to.
     */
    var castle: Castle? = null

    /**
     * @return the generic Identifier of this L2Npc contained in the L2NpcTemplate.
     */
    val npcId: Int
        get() = template.npcId

    override val isAttackable: Boolean
        get() = true

    /**
     * @return True if the L2Npc is agressive (ex : L2MonsterInstance in function of aggroRange).
     */
    open val isAggressive: Boolean
        get() = false

    /**
     * Overidden in L2CastleWarehouse, L2ClanHallManager and L2Warehouse.
     * @return true if this L2Npc instance can be warehouse manager.
     */
    open val isWarehouse: Boolean
        get() = false

    /**
     * @return the Exp Reward of this L2Npc contained in the L2NpcTemplate (modified by RATE_XP).
     */
    val expReward: Int
        get() = (template.rewardExp * Config.RATE_XP).toInt()

    /**
     * @return the SP Reward of this L2Npc contained in the L2NpcTemplate (modified by RATE_SP).
     */
    val spReward: Int
        get() = (template.rewardSp * Config.RATE_SP).toInt()

    /**
     * Used for animation timers, overridden in L2Attackable.
     * @return true if L2Attackable, false otherwise.
     */
    open val isMob: Boolean
        get() = false

    /**
     * Broadcast a SocialAction packet.
     * @param id the animation id.
     */
    fun onRandomAnimation(id: Int) {
        val now = System.currentTimeMillis()
        if (now - _lastSocialBroadcast > SOCIAL_INTERVAL) {
            _lastSocialBroadcast = now
            broadcastPacket(SocialAction(this, id))
        }
    }

    /**
     * Create a RandomAnimation Task that will be launched after the calculated delay.
     */
    fun startRandomAnimationTimer() {
        if (!hasRandomAnimation())
            return

        val timer =
            if (isMob) Rnd[Config.MIN_MONSTER_ANIMATION, Config.MAX_MONSTER_ANIMATION] else Rnd[Config.MIN_NPC_ANIMATION, Config.MAX_NPC_ANIMATION]
        RandomAnimationTaskManager.add(this, timer)
    }

    /**
     * @return true if the server allows Random Animation, false if not or the AItype is a corpse.
     */
    open fun hasRandomAnimation(): Boolean {
        return Config.MAX_NPC_ANIMATION > 0 && template.aiType != AIType.CORPSE
    }

    init {

        for (skill in template.getSkills(SkillType.PASSIVE))
            addStatFuncs(skill.getStatFuncs(this))

        initCharStatusUpdateValues()

        // initialize the "current" equipment
        leftHandItem = template.leftHand
        rightHandItem = template.rightHand
        enchantEffect = template.enchantEffect

        // initialize the "current" collisions
        _currentCollisionHeight = template.collisionHeight
        _currentCollisionRadius = template.collisionRadius

        // Set the name of the Creature
        name = template.name
        title = template.title

        castle = template.castle
    }

    override fun initCharStat() {
        stat = NpcStat(this)
    }

    override fun getStat(): NpcStat {
        return super.getStat() as NpcStat
    }

    override fun initCharStatus() {
        status = NpcStatus(this)
    }

    override fun getStatus(): NpcStatus {
        return super.getStatus() as NpcStatus
    }

    override fun getTemplate(): NpcTemplate {
        return super.getTemplate() as NpcTemplate
    }

    override fun getLevel(): Int {
        return template.level.toInt()
    }

    /**
     * Return True if this L2Npc is undead in function of the L2NpcTemplate.
     */
    override fun isUndead(): Boolean {
        return template.race === Race.UNDEAD
    }

    /**
     * Broadcast a NpcInfo packet with state of abnormal effect.
     */
    override fun updateAbnormalEffect() {
        for (player in getKnownType(Player::class.java)) {
            if (moveSpeed == 0)
                player.sendPacket(ServerObjectInfo(this, player))
            else
                player.sendPacket(NpcInfo(this, player))
        }
    }

    override fun setTitle(value: String?) {
        if (value == null)
            _title = ""
        else
            _title = value
    }

    override fun isAutoAttackable(attacker: Creature): Boolean {
        return false
    }

    override fun onAction(player: Player) {
        // Set the target of the player
        if (player.target !== this)
            player.target = this
        else {
            // Check if the player is attackable (without a forced attack).
            if (isAutoAttackable(player))
                player.ai.setIntention(CtrlIntention.ATTACK, this)
            else {
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

                    var scripts: List<Quest>? = template.getEventQuests(EventType.QUEST_START)
                    if (scripts != null && !scripts.isEmpty())
                        player.lastQuestNpcObject = objectId

                    scripts = template.getEventQuests(EventType.ON_FIRST_TALK)
                    if (scripts != null && scripts.size == 1)
                        scripts[0].notifyFirstTalk(this, player)
                    else
                        showChatWindow(player)
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

                var scripts: List<Quest>? = template.getEventQuests(EventType.QUEST_START)
                if (scripts != null && !scripts.isEmpty())
                    player.lastQuestNpcObject = objectId

                scripts = template.getEventQuests(EventType.ON_FIRST_TALK)
                if (scripts != null && scripts.size == 1)
                    scripts[0].notifyFirstTalk(this, player)
                else
                    showChatWindow(player)
            } else
                player.sendPacket(ActionFailed.STATIC_PACKET)
        }
    }

    /**
     * Open a quest or chat window on client with the text of the L2Npc in function of the command.
     * @param player The player to test
     * @param command The command string received from client
     */
    open fun onBypassFeedback(player: Player, command: String) {
        if (command.equals("TerritoryStatus", ignoreCase = true)) {
            val html = NpcHtmlMessage(objectId)

            if (castle!!.ownerId > 0) {
                html.setFile("data/html/territorystatus.htm")
                val clan = ClanTable.getClan(castle!!.ownerId)
                html.replace("%clanname%", clan!!.name)
                html.replace("%clanleadername%", clan.leaderName)
            } else
                html.setFile("data/html/territorynoclan.htm")

            html.replace("%castlename%", castle!!.name)
            html.replace("%taxpercent%", castle!!.taxPercent)
            html.replace("%objectId%", objectId)

            if (castle!!.castleId > 6)
                html.replace("%territory%", "The Kingdom of Elmore")
            else
                html.replace("%territory%", "The Kingdom of Aden")

            player.sendPacket(html)
        } else if (command.startsWith("Quest")) {
            var quest = ""
            try {
                quest = command.substring(5).trim { it <= ' ' }
            } catch (ioobe: IndexOutOfBoundsException) {
            }

            if (quest.isEmpty())
                showQuestWindowGeneral(player, this)
            else
                showQuestWindowSingle(player, this, ScriptData.getQuest(quest))
        } else if (command.startsWith("Chat")) {
            var `val` = 0
            try {
                `val` = Integer.parseInt(command.substring(5))
            } catch (ioobe: IndexOutOfBoundsException) {
            } catch (nfe: NumberFormatException) {
            }

            showChatWindow(player, `val`)
        } else if (command.startsWith("Link")) {
            val path = command.substring(5).trim { it <= ' ' }
            if (path.indexOf("..") != -1)
                return

            val html = NpcHtmlMessage(objectId)
            html.setFile("data/html/$path")
            html.replace("%objectId%", objectId)
            player.sendPacket(html)
        } else if (command.startsWith("Loto")) {
            var `val` = 0
            try {
                `val` = Integer.parseInt(command.substring(5))
            } catch (ioobe: IndexOutOfBoundsException) {
            } catch (nfe: NumberFormatException) {
            }

            if (`val` == 0) {
                // new loto ticket
                for (i in 0..4)
                    player.setLoto(i, 0)
            }
            showLotoWindow(player, `val`)
        } else if (command.startsWith("CPRecovery")) {
            makeCPRecovery(player)
        } else if (command.startsWith("SupportMagic")) {
            makeSupportMagic(player)
        } else if (command.startsWith("multisell")) {
            MultisellData.separateAndSend(command.substring(9).trim { it <= ' ' }, player, this, false)
        } else if (command.startsWith("exc_multisell")) {
            MultisellData.separateAndSend(command.substring(13).trim { it <= ' ' }, player, this, true)
        } else if (command.startsWith("Augment")) {
            val cmdChoice = Integer.parseInt(command.substring(8, 9).trim { it <= ' ' })
            when (cmdChoice) {
                1 -> {
                    player.sendPacket(SystemMessageId.SELECT_THE_ITEM_TO_BE_AUGMENTED)
                    player.sendPacket(ExShowVariationMakeWindow.STATIC_PACKET)
                }
                2 -> {
                    player.sendPacket(SystemMessageId.SELECT_THE_ITEM_FROM_WHICH_YOU_WISH_TO_REMOVE_AUGMENTATION)
                    player.sendPacket(ExShowVariationCancelWindow.STATIC_PACKET)
                }
            }
        } else if (command.startsWith("EnterRift")) {
            try {
                val b1 = java.lang.Byte.parseByte(command.substring(10)) // Selected Area: Recruit, Soldier etc
                DimensionalRiftManager.start(player, b1, this)
            } catch (e: Exception) {
            }

        }
    }

    override fun getActiveWeaponInstance(): ItemInstance? {
        return null
    }

    override fun getActiveWeaponItem(): Weapon? {
        // Get the weapon identifier equipped in the right hand of the L2Npc
        val weaponId = template.rightHand
        if (weaponId < 1)
            return null

        // Get the weapon item equipped in the right hand of the L2Npc
        val item = ItemTable.getTemplate(weaponId)
        return if (item !is Weapon) null else item

    }

    override fun getSecondaryWeaponInstance(): ItemInstance? {
        return null
    }

    override fun getSecondaryWeaponItem(): Item? {
        // Get the weapon identifier equipped in the right hand of the L2Npc
        val itemId = template.leftHand
        return if (itemId < 1) null else ItemTable.getTemplate(itemId)

        // Return the item equipped in the left hand of the L2Npc
    }

    /**
     * Generate the complete path to retrieve a htm, based on npcId.
     *
     *  * if the file exists on the server (page number = 0) : <B>data/html/default/12006.htm</B> (npcId-page number)
     *  * if the file exists on the server (page number > 0) : <B>data/html/default/12006-1.htm</B> (npcId-page number)
     *  * if the file doesn't exist on the server : <B>data/html/npcdefault.htm</B> (message : "I have nothing to say to you")
     *
     * @param npcId : The id of the Npc whose text must be displayed.
     * @param val : The number of the page to display.
     * @return the pathfile of the selected HTML file in function of the npcId and of the page number.
     */
    open fun getHtmlPath(npcId: Int, `val`: Int): String {
        val filename: String

        if (`val` == 0)
            filename = "data/html/default/$npcId.htm"
        else
            filename = "data/html/default/$npcId-$`val`.htm"

        return if (HtmCache.isLoadable(filename)) filename else "data/html/npcdefault.htm"

    }

    /**
     * Make the NPC speaks to his current knownlist.
     * @param message The String message to send.
     */
    fun broadcastNpcSay(message: String) {
        broadcastPacket(NpcSay(objectId, Say2.ALL, npcId, message))
    }

    /**
     * Open a Loto window on client with the text of the L2Npc.
     * @param player : The player that talk with the L2Npc.
     * @param val : The number of the page of the L2Npc to display.
     */
    // 0 - first buy lottery ticket window
    // 1-20 - buttons
    // 21 - second buy lottery ticket window
    // 22 - selected ticket with 5 numbers
    // 23 - current lottery jackpot
    // 24 - Previous winning numbers/Prize claim
    // >24 - check lottery ticket by item object id
    fun showLotoWindow(player: Player, `val`: Int) {
        val npcId = template.npcId

        val html = NpcHtmlMessage(objectId)

        if (`val` == 0)
        // 0 - first buy lottery ticket window
        {
            html.setFile(getHtmlPath(npcId, 1))
        } else if (`val` >= 1 && `val` <= 21)
        // 1-20 - buttons, 21 - second buy lottery ticket window
        {
            if (!LotteryManager.isStarted) {
                // tickets can't be sold
                player.sendPacket(SystemMessageId.NO_LOTTERY_TICKETS_CURRENT_SOLD)
                return
            }
            if (!LotteryManager.isSellableTickets) {
                // tickets can't be sold
                player.sendPacket(SystemMessageId.NO_LOTTERY_TICKETS_AVAILABLE)
                return
            }

            html.setFile(getHtmlPath(npcId, 5))

            var count = 0
            var found = 0
            // counting buttons and unsetting button if found
            for (i in 0..4) {
                if (player.getLoto(i) == `val`) {
                    // unsetting button
                    player.setLoto(i, 0)
                    found = 1
                } else if (player.getLoto(i) > 0) {
                    count++
                }
            }

            // if not rearched limit 5 and not unseted value
            if (count < 5 && found == 0 && `val` <= 20)
                for (i in 0..4)
                    if (player.getLoto(i) == 0) {
                        player.setLoto(i, `val`)
                        break
                    }

            // setting pusshed buttons
            count = 0
            for (i in 0..4)
                if (player.getLoto(i) > 0) {
                    count++
                    var button = player.getLoto(i).toString()
                    if (player.getLoto(i) < 10)
                        button = "0$button"
                    val search = "fore=\"L2UI.lottoNum" + button + "\" back=\"L2UI.lottoNum" + button + "a_check\""
                    val replace = "fore=\"L2UI.lottoNum" + button + "a_check\" back=\"L2UI.lottoNum" + button + "\""
                    html.replace(search, replace)
                }

            if (count == 5) {
                val search = "0\">Return"
                val replace = "22\">The winner selected the numbers above."
                html.replace(search, replace)
            }
        } else if (`val` == 22)
        // 22 - selected ticket with 5 numbers
        {
            if (!LotteryManager.isStarted) {
                // tickets can't be sold
                player.sendPacket(SystemMessageId.NO_LOTTERY_TICKETS_CURRENT_SOLD)
                return
            }
            if (!LotteryManager.isSellableTickets) {
                // tickets can't be sold
                player.sendPacket(SystemMessageId.NO_LOTTERY_TICKETS_AVAILABLE)
                return
            }

            val price = Config.ALT_LOTTERY_TICKET_PRICE
            val lotonumber = LotteryManager.id
            var enchant = 0
            var type2 = 0

            for (i in 0..4) {
                if (player.getLoto(i) == 0)
                    return

                if (player.getLoto(i) < 17)
                    enchant += Math.pow(2.0, (player.getLoto(i) - 1).toDouble()).toInt()
                else
                    type2 += Math.pow(2.0, (player.getLoto(i) - 17).toDouble()).toInt()
            }

            if (!player.reduceAdena("Loto", price, this, true))
                return

            LotteryManager.increasePrize(price)

            val item = ItemInstance(IdFactory.getInstance().nextId, 4442)
            item.count = 1
            item.customType1 = lotonumber
            item.enchantLevel = enchant
            item.customType2 = type2

            player.addItem("Loto", item, player, false)
            player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1).addItemName(4442))

            html.setFile(getHtmlPath(npcId, 3))
        } else if (`val` == 23)
        // 23 - current lottery jackpot
        {
            html.setFile(getHtmlPath(npcId, 3))
        } else if (`val` == 24)
        // 24 - Previous winning numbers/Prize claim
        {
            val lotoNumber = LotteryManager.id

            val sb = StringBuilder()
            for (item in player.inventory!!.items) {

                if (item.itemId == 4442 && item.customType1 < lotoNumber) {
                    StringUtil.append(
                        sb,
                        "<a action=\"bypass -h npc_%objectId%_Loto ",
                        item.objectId,
                        "\">",
                        item.customType1,
                        " Event Number "
                    )

                    val numbers = LotteryManager.decodeNumbers(item.enchantLevel, item.customType2)
                    for (i in 0..4)
                        StringUtil.append(sb, numbers[i], " ")

                    val check = LotteryManager.checkTicket(item)
                    if (check[0] > 0) {
                        when (check[0]) {
                            1 -> sb.append("- 1st Prize")
                            2 -> sb.append("- 2nd Prize")
                            3 -> sb.append("- 3th Prize")
                            4 -> sb.append("- 4th Prize")
                        }
                        StringUtil.append(sb, " ", check[1], "a.")
                    }
                    sb.append("</a><br>")
                }
            }

            if (sb.length == 0)
                sb.append("There is no winning lottery ticket...<br>")

            html.setFile(getHtmlPath(npcId, 4))
            html.replace("%result%", sb.toString())
        } else if (`val` == 25)
        // 25 - lottery instructions
        {
            html.setFile(getHtmlPath(npcId, 2))
            html.replace("%prize5%", Config.ALT_LOTTERY_5_NUMBER_RATE * 100)
            html.replace("%prize4%", Config.ALT_LOTTERY_4_NUMBER_RATE * 100)
            html.replace("%prize3%", Config.ALT_LOTTERY_3_NUMBER_RATE * 100)
            html.replace("%prize2%", Config.ALT_LOTTERY_2_AND_1_NUMBER_PRIZE)
        } else if (`val` > 25)
        // >25 - check lottery ticket by item object id
        {
            val item = player.inventory!!.getItemByObjectId(`val`)
            if (item == null || item.itemId != 4442 || item.customType1 >= LotteryManager.id)
                return

            if (player.destroyItem("Loto", item, this, true)) {
                val adena = LotteryManager.checkTicket(item)[1]
                if (adena > 0)
                    player.addAdena("Loto", adena, this, true)
            }
            return
        }
        html.replace("%objectId%", objectId)
        html.replace("%race%", LotteryManager.id)
        html.replace("%adena%", LotteryManager.prize)
        html.replace("%ticket_price%", Config.ALT_LOTTERY_TICKET_PRICE)
        html.replace("%enddate%", DateFormat.getDateInstance().format(LotteryManager.endDate))
        player.sendPacket(html)

        // Send a Server->Client ActionFailed to the Player in order to avoid that the client wait another packet
        player.sendPacket(ActionFailed.STATIC_PACKET)
    }

    fun makeCPRecovery(player: Player) {
        if (npcId != 31225 && npcId != 31226)
            return

        if (player.isCursedWeaponEquipped) {
            player.sendMessage("Go away, you're not welcome here.")
            return
        }

        // Consume 100 adenas
        if (player.reduceAdena("RestoreCP", 100, player.currentFolk, true)) {
            target = player
            doCast(SkillTable.FrequentSkill.ARENA_CP_RECOVERY.skill)
            player.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.S1_CP_WILL_BE_RESTORED).addCharName(player))
        }
    }

    /**
     * Add Newbie buffs to Player according to its level.
     * @param player : The player that talk with the Npc.
     */
    fun makeSupportMagic(player: Player?) {
        if (player == null)
            return

        // Prevent a cursed weapon wielder of being buffed.
        if (player.isCursedWeaponEquipped)
            return

        val playerLevel = player.level
        var lowestLevel = 0
        var higestLevel = 0

        // Select the player.
        target = player

        // Calculate the min and max level between which the player must be to obtain buff.
        if (player.isMageClass) {
            lowestLevel = NewbieBuffData.magicLowestLevel
            higestLevel = NewbieBuffData.magicHighestLevel
        } else {
            lowestLevel = NewbieBuffData.physicLowestLevel
            higestLevel = NewbieBuffData.physicHighestLevel
        }

        // If the player is too high level, display a message and return.
        if (playerLevel > higestLevel || !player.isNewbie) {
            val html = NpcHtmlMessage(objectId)
            html.setHtml("<html><body>Newbie Guide:<br>Only a <font color=\"LEVEL\">novice character of level $higestLevel or less</font> can receive my support magic.<br>Your novice character is the first one that you created and raised in this world.</body></html>")
            html.replace("%objectId%", objectId)
            player.sendPacket(html)
            return
        }

        // If the player is too low level, display a message and return.
        if (playerLevel < lowestLevel) {
            val html = NpcHtmlMessage(objectId)
            html.setHtml("<html><body>Come back here when you have reached level $lowestLevel. I will give you support magic then.</body></html>")
            html.replace("%objectId%", objectId)
            player.sendPacket(html)
            return
        }

        // Go through the NewbieBuffData list and cast skills.
        for (buff in NewbieBuffData.buffs) {
            if (buff.isMagicClassBuff == player.isMageClass && playerLevel >= buff.lowerLevel && playerLevel <= buff.upperLevel) {
                val skill = SkillTable.getInfo(buff.skillId, buff.skillLevel)
                if (skill!!.skillType === L2SkillType.SUMMON)
                    player.doCast(skill)
                else
                    doCast(skill)
            }
        }
    }

    /**
     * Research the pk chat window htm related to this [Npc], based on a String folder and npcId.<br></br>
     * Send the content to the [Player] passed as parameter.
     * @param player : The player to send the HTM.
     * @param type : The folder to search on.
     * @return true if such HTM exists.
     */
    protected fun showPkDenyChatWindow(player: Player, type: String): Boolean {
        val content = HtmCache.getHtm("data/html/$type/$npcId-pk.htm")
        if (content != null) {
            val html = NpcHtmlMessage(objectId)
            html.setHtml(content)
            player.sendPacket(html)

            player.sendPacket(ActionFailed.STATIC_PACKET)
            return true
        }
        return false
    }

    /**
     * Open a chat window on client with the text of the L2Npc.
     * @param player : The player that talk with the L2Npc.
     */
    open fun showChatWindow(player: Player) {
        showChatWindow(player, 0)
    }

    /**
     * Open a chat window on client with the text specified by [.getHtmlPath] and val parameter.
     * @param player : The player that talk with the Npc.
     * @param val : The current htm page to show.
     */
    open fun showChatWindow(player: Player, `val`: Int) {
        showChatWindow(player, getHtmlPath(npcId, `val`))
    }

    /**
     * Open a chat window on client with the text specified by the given file name and path.
     * @param player : The player that talk with the Npc.
     * @param filename : The filename that contains the text to send.
     */
    fun showChatWindow(player: Player, filename: String) {
        val html = NpcHtmlMessage(objectId)
        html.setFile(filename)
        html.replace("%objectId%", objectId)
        player.sendPacket(html)

        player.sendPacket(ActionFailed.STATIC_PACKET)
    }

    /**
     * Kill the L2Npc (the corpse disappeared after 7 seconds).<BR></BR>
     * <BR></BR>
     * <B><U> Actions</U> :</B><BR></BR>
     * <BR></BR>
     *  * Create a DecayTask to remove the corpse of the L2Npc after 7 seconds
     *  * Set target to null and cancel Attack or Cast
     *  * Stop movement
     *  * Stop HP/MP/CP Regeneration task
     *  * Stop all active skills effects in progress on the Creature
     *  * Send the Server->Client packet StatusUpdate with current HP and MP to all other Player to inform
     *  * Notify Creature AI<BR></BR>
     * <BR></BR>
     * <B><U> Overriden in </U> :</B><BR></BR>
     * <BR></BR>
     *  * L2Attackable<BR></BR>
     * <BR></BR>
     * @param killer The Creature who killed it
     */
    override fun doDie(killer: Creature?): Boolean {
        if (!super.doDie(killer))
            return false

        leftHandItem = template.leftHand
        rightHandItem = template.rightHand
        enchantEffect = template.enchantEffect
        _currentCollisionHeight = template.collisionHeight
        _currentCollisionRadius = template.collisionRadius
        DecayTaskManager.add(this, template.corpseTime)
        return true
    }

    override fun onSpawn() {
        super.onSpawn()

        // initialize ss/sps counts.
        _currentSsCount = template.ssCount
        _currentSpsCount = template.spsCount

        val scripts = template.getEventQuests(EventType.ON_SPAWN)
        if (scripts != null)
            for (quest in scripts)
                quest.notifySpawn(this)
    }

    override fun onDecay() {
        if (isDecayed)
            return

        isDecayed = true

        val scripts = template.getEventQuests(EventType.ON_DECAY)
        if (scripts != null)
            for (quest in scripts)
                quest.notifyDecay(this)

        // Remove the L2Npc from the world when the decay task is launched.
        super.onDecay()

        // Respawn it, if possible.
        if (spawn != null)
            spawn!!.doRespawn()
    }

    override fun deleteMe() {
        // Decay
        onDecay()

        super.deleteMe()
    }

    override fun toString(): String {
        return name
    }

    fun endDecayTask() {
        if (!isDecayed) {
            DecayTaskManager.cancel(this)
            onDecay()
        }
    }

    fun setLHandId(newWeaponId: Int) {
        leftHandItem = newWeaponId
    }

    fun setRHandId(newWeaponId: Int) {
        rightHandItem = newWeaponId
    }

    fun setEnchant(enchant: Int) {
        enchantEffect = enchant
    }

    fun setCollisionHeight(height: Double) {
        _currentCollisionHeight = height
    }

    override fun getCollisionHeight(): Double {
        return _currentCollisionHeight
    }

    fun setCollisionRadius(radius: Double) {
        _currentCollisionRadius = radius
    }

    override fun getCollisionRadius(): Double {
        return _currentCollisionRadius
    }

    fun isScriptValue(`val`: Int): Boolean {
        return scriptValue == `val`
    }

    fun scheduleDespawn(delay: Long): Npc {
        ThreadPool.schedule(DespawnTask(), delay)
        return this
    }

    protected inner class DespawnTask : Runnable {
        override fun run() {
            if (!isDecayed)
                deleteMe()
        }
    }

    override fun notifyQuestEventSkillFinished(skill: L2Skill, target: WorldObject?) {
        val scripts = template.getEventQuests(EventType.ON_SPELL_FINISHED)
        if (scripts != null) {
            val player = target?.actingPlayer

            for (quest in scripts)
                quest.notifySpellFinished(this, player, skill)
        }
    }

    override fun isMovementDisabled(): Boolean {
        return super.isMovementDisabled() || !template.canMove() || template.aiType == AIType.CORPSE
    }

    override fun isCoreAIDisabled(): Boolean {
        return super.isCoreAIDisabled() || template.aiType == AIType.CORPSE
    }

    override fun sendInfo(activeChar: Player) {
        if (moveSpeed == 0)
            activeChar.sendPacket(ServerObjectInfo(this, activeChar))
        else
            activeChar.sendPacket(NpcInfo(this, activeChar))
    }

    override fun isChargedShot(type: ShotType): Boolean {
        return _shotsMask and type.mask == type.mask
    }

    override fun setChargedShot(type: ShotType, charged: Boolean) {
        if (charged)
            _shotsMask = _shotsMask or type.mask
        else
            _shotsMask = _shotsMask and type.mask.inv()
    }

    override fun rechargeShots(physical: Boolean, magic: Boolean) {
        if (physical) {
            if (_currentSsCount <= 0)
                return

            if (Rnd[100] > template.ssRate)
                return

            _currentSsCount--
            this.toSelfAndKnownPlayersInRadius(MagicSkillUse(this, this, 2154, 1, 0, 0), 600)
            setChargedShot(ShotType.SOULSHOT, true)
        }

        if (magic) {
            if (_currentSpsCount <= 0)
                return

            if (Rnd[100] > template.spsRate)
                return

            _currentSpsCount--
            this.toSelfAndKnownPlayersInRadius(MagicSkillUse(this, this, 2061, 1, 0, 0), 600)
            setChargedShot(ShotType.SPIRITSHOT, true)
        }
    }

    override fun getSkillLevel(skillId: Int): Int {
        for (list in template.skills.values) {
            for (skill in list)
                if (skill.id == skillId)
                    return skill.level
        }
        return 0
    }

    override fun getSkill(skillId: Int): L2Skill? {
        for (list in template.skills.values) {
            for (skill in list)
                if (skill.id == skillId)
                    return skill
        }
        return null
    }

    protected fun sendNpcInfos(player: Player) {
        val html = NpcHtmlMessage(objectId)
        html.setFile("data/html/admin/npcinfo.htm")
        html.replace("%class%", javaClass.simpleName)
        html.replace("%id%", template.npcId)
        html.replace("%lvl%", template.level.toInt())
        html.replace("%name%", name)
        html.replace("%race%", template.race.toString())
        html.replace("%tmplid%", template.idTemplate)
        html.replace("%script%", scriptValue)
        html.replace("%castle%", if (castle != null) castle!!.name else "none")
        html.replace("%aggro%", template.aggroRange)
        html.replace("%corpse%", StringUtil.getTimeStamp(template.corpseTime))
        html.replace("%enchant%", template.enchantEffect)
        html.replace("%hp%", currentHp.toInt())
        html.replace("%hpmax%", maxHp)
        html.replace("%mp%", currentMp.toInt())
        html.replace("%mpmax%", maxMp)
        html.replace("%patk%", getPAtk(null))
        html.replace("%matk%", getMAtk(null, null))
        html.replace("%pdef%", getPDef(null))
        html.replace("%mdef%", getMDef(null, null))
        html.replace("%accu%", accuracy)
        html.replace("%evas%", getEvasionRate(null))
        html.replace("%crit%", getCriticalHit(null, null))
        html.replace("%rspd%", moveSpeed)
        html.replace("%aspd%", pAtkSpd)
        html.replace("%cspd%", mAtkSpd)
        html.replace("%str%", str)
        html.replace("%dex%", dex)
        html.replace("%con%", con)
        html.replace("%int%", int)
        html.replace("%wit%", wit)
        html.replace("%men%", men)
        html.replace("%loc%", "$x $y $z")
        html.replace("%dist%", Math.sqrt(player.getDistanceSq(this)).toInt())
        html.replace("%ele_fire%", getDefenseElementValue(2.toByte()))
        html.replace("%ele_water%", getDefenseElementValue(3.toByte()))
        html.replace("%ele_wind%", getDefenseElementValue(1.toByte()))
        html.replace("%ele_earth%", getDefenseElementValue(4.toByte()))
        html.replace("%ele_holy%", getDefenseElementValue(5.toByte()))
        html.replace("%ele_dark%", getDefenseElementValue(6.toByte()))

        if (spawn != null) {
            html.replace("%spawn%", spawn!!.loc!!.toString())
            html.replace("%loc2d%", Math.sqrt(getPlanDistanceSq(spawn!!.locX, spawn!!.locY)).toInt())
            html.replace("%loc3d%", Math.sqrt(getDistanceSq(spawn!!.locX, spawn!!.locY, spawn!!.locZ)).toInt())
            html.replace("%resp%", StringUtil.getTimeStamp(spawn!!.respawnDelay))
            html.replace("%rand_resp%", StringUtil.getTimeStamp(spawn!!.respawnRandom))
        } else {
            html.replace("%spawn%", "<font color=FF0000>null</font>")
            html.replace("%loc2d%", "<font color=FF0000>--</font>")
            html.replace("%loc3d%", "<font color=FF0000>--</font>")
            html.replace("%resp%", "<font color=FF0000>--</font>")
            html.replace("%rand_resp%", "<font color=FF0000>--</font>")
        }

        if (hasAI()) {
            html.replace(
                "%ai_intention%",
                "<font color=\"LEVEL\">Intention</font><table width=\"100%\"><tr><td><font color=\"LEVEL\">Intention:</font></td><td>" + ai.desire.intention.name + "</td></tr>"
            )
            html.replace(
                "%ai%",
                "<tr><td><font color=\"LEVEL\">AI:</font></td><td>" + ai.javaClass.simpleName + "</td></tr></table><br>"
            )
        } else {
            html.replace("%ai_intention%", "")
            html.replace("%ai%", "")
        }

        html.replace("%ai_type%", template.aiType!!.name)
        html.replace(
            "%ai_clan%",
            if (template.clans != null) "<tr><td width=100><font color=\"LEVEL\">Clan:</font></td><td align=right width=170>" + Arrays.toString(
                template.clans
            ) + " " + template.clanRange + "</td></tr>" + (if (template.ignoredIds != null) "<tr><td width=100><font color=\"LEVEL\">Ignored ids:</font></td><td align=right width=170>" + Arrays.toString(
                template.ignoredIds
            ) + "</td></tr>" else "") else ""
        )
        html.replace("%ai_move%", template.canMove().toString())
        html.replace("%ai_seed%", template.isSeedable.toString())
        html.replace(
            "%ai_ssinfo%",
            _currentSsCount.toString() + "[" + template.ssCount + "] - " + template.ssRate + "%"
        )
        html.replace(
            "%ai_spsinfo%",
            _currentSpsCount.toString() + "[" + template.spsCount + "] - " + template.spsRate + "%"
        )
        html.replace(
            "%shop%",
            if (this is Merchant) "<button value=\"Shop\" action=\"bypass -h admin_show_shop $npcId\" width=65 height=19 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\">" else ""
        )
        html.replace(
            "%minion%",
            if (this is Monster && (this.getMaster() != null || this.hasMinions())) "<button value=\"Minions\" action=\"bypass -h admin_show_minion\" width=65 height=19 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\">" else ""
        )
        player.sendPacket(html)
    }

    companion object {
        val INTERACTION_DISTANCE = 150
        private val SOCIAL_INTERVAL = 12000

        /**
         * Collect quests in progress and possible quests and show proper quest window.
         * @param player : The player that talk with the L2Npc.
         * @param npc : The L2Npc instance.
         */
        fun showQuestWindowGeneral(player: Player, npc: Npc) {
            // The container used to retrieve quests.
            val quests = ArrayList<Quest>()

            var scripts: List<Quest>? = npc.template.getEventQuests(EventType.ON_TALK)
            if (scripts != null) {
                for (quest in scripts) {
                    if (!quest.isRealQuest || quests.contains(quest))
                        continue

                    val qs = player.getQuestState(quest.name)
                    if (qs == null || qs.isCreated)
                        continue

                    quests.add(quest)
                }
            }

            scripts = npc.template.getEventQuests(EventType.QUEST_START)
            for (quest in scripts) {
                if (!quest.isRealQuest || quests.contains(quest))
                    continue

                quests.add(quest)
            }

            if (quests.isEmpty())
                showQuestWindowSingle(player, npc, null)
            else if (quests.size == 1)
                showQuestWindowSingle(player, npc, quests[0])
            else
                showQuestWindowChoose(player, npc, quests)
        }

        /**
         * Open a quest window on client with the text of the L2Npc. Create the QuestState if not existing.
         * @param player : the player that talk with the L2Npc.
         * @param npc : the L2Npc instance.
         * @param quest : the quest HTMLs to show.
         */
        fun showQuestWindowSingle(player: Player, npc: Npc, quest: Quest?) {
            if (quest == null) {
                val html = NpcHtmlMessage(npc.objectId)
                html.setHtml(Quest.noQuestMsg)
                player.sendPacket(html)

                player.sendPacket(ActionFailed.STATIC_PACKET)
                return
            }

            if (quest.isRealQuest && (player.weightPenalty > 2 || player.inventoryLimit * 0.8 <= player.inventory!!.size)) {
                player.sendPacket(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT)
                return
            }

            var qs = player.getQuestState(quest.name)
            if (qs == null) {
                if (quest.isRealQuest && player.getAllQuests(false).size >= 25) {
                    val html = NpcHtmlMessage(npc.objectId)
                    html.setHtml(Quest.tooMuchQuestsMsg)
                    player.sendPacket(html)

                    player.sendPacket(ActionFailed.STATIC_PACKET)
                    return
                }

                val scripts = npc.template.getEventQuests(EventType.QUEST_START)
                if (scripts.contains(quest))
                    qs = quest.newQuestState(player)
            }

            if (qs != null)
                quest.notifyTalk(npc, qs.player)
        }

        /**
         * Shows the list of available quest of the L2Npc.
         * @param player : The player that talk with the L2Npc.
         * @param npc : The L2Npc instance.
         * @param quests : The list containing quests of the L2Npc.
         */
        fun showQuestWindowChoose(player: Player, npc: Npc, quests: List<Quest>) {
            val sb = StringBuilder("<html><body>")

            for (q in quests) {
                StringUtil.append(sb, "<a action=\"bypass -h npc_%objectId%_Quest ", q.name, "\">[", q.descr)

                val qs = player.getQuestState(q.name)
                if (qs != null && qs.isStarted)
                    sb.append(" (In Progress)]</a><br>")
                else if (qs != null && qs.isCompleted)
                    sb.append(" (Done)]</a><br>")
                else
                    sb.append("]</a><br>")
            }

            sb.append("</body></html>")

            val html = NpcHtmlMessage(npc.objectId)
            html.setHtml(sb.toString())
            html.replace("%objectId%", npc.objectId)
            player.sendPacket(html)

            player.sendPacket(ActionFailed.STATIC_PACKET)
        }
    }
}