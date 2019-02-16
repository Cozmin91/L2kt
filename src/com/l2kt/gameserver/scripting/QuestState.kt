package com.l2kt.gameserver.scripting

import com.l2kt.Config
import com.l2kt.L2DatabaseFactory
import com.l2kt.commons.logging.CLogger
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.cache.HtmCache
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.item.DropData
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.*
import java.util.*

class QuestState
/**
 * Constructor of the QuestState : save the quest in the list of quests of the player.<BR></BR>
 * <BR></BR>
 * <U><I>Actions :</I></U><BR></BR>
 * <LI>Save informations in the object QuestState created (Quest, Player, Completion, State)</LI>
 * <LI>Add the QuestState in the player's list of quests by using setQuestState()</LI>
 * <LI>Add drops gotten by the quest</LI> <BR></BR>
 * @param quest : quest associated with the QuestState
 * @param player : Player pointing out the player
 * @param state : state of the quest
 */
    (
    /**
     * Return the Player
     * @return Player
     */
    val player: Player,
    /**
     * Return the quest
     * @return Quest
     */
    val quest: Quest, private var _state: Byte
) {
    private val _vars = HashMap<String, String>()

    /**
     * Return the state of the quest
     * @return State
     */
    /**
     * Set state of the quest.
     *
     *  * Remove drops from previous state
     *  * Set new state of the quest
     *  * Add drop for new state
     *  * Update information in database
     *  * Send packet QuestList to client
     *
     * @param state
     */
    var state: Byte
        get() = _state
        set(state) {
            if (_state != state) {
                _state = state

                setQuestVarInDb("<state>", _state.toString())

                player.sendPacket(QuestList(player))
            }
        }

    /**
     * Return true if quest just created, false otherwise
     * @return
     */
    val isCreated: Boolean
        get() = _state == Quest.STATE_CREATED

    /**
     * Return true if quest completed, false otherwise
     * @return boolean
     */
    val isCompleted: Boolean
        get() = _state == Quest.STATE_COMPLETED

    /**
     * Return true if quest started, false otherwise
     * @return boolean
     */
    val isStarted: Boolean
        get() = _state == Quest.STATE_STARTED

    init {

        this.player.setQuestState(this)
    }

    /**
     * Destroy element used by quest when quest is exited
     * @param repeatable
     */
    fun exitQuest(repeatable: Boolean) {
        if (!isStarted)
            return

        // Remove quest from player's notifyDeath list.
        player!!.removeNotifyQuestOfDeath(this)

        // Remove/Complete quest.
        if (repeatable) {
            player.delQuestState(this)
            player.sendPacket(QuestList(player))
        } else
            state = Quest.STATE_COMPLETED

        // Remove quest variables.
        _vars.clear()

        // Remove registered quest items.
        val itemIdList = quest.getItemsIds()
        if (itemIdList != null) {
            for (itemId in itemIdList)
                takeItems(itemId, -1)
        }

        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(if (repeatable) QUEST_DELETE else QUEST_COMPLETE).use { ps ->
                    ps.setInt(1, player.objectId)
                    ps.setString(2, quest.name)
                    ps.executeUpdate()
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't delete quest.", e)
        }

    }

    /**
     * Add player to get notification of characters death
     */
    fun addNotifyOfDeath() {
        player?.addNotifyQuestOfDeath(this)
    }

    /**
     * Return value of parameter "val" after adding the couple (var,val) in class variable "vars".<BR></BR>
     * <BR></BR>
     * <U><I>Actions :</I></U><BR></BR>
     * <LI>Initialize class variable "vars" if is null</LI>
     * <LI>Initialize parameter "val" if is null</LI>
     * <LI>Add/Update couple (var,val) in class variable FastMap "vars"</LI>
     * <LI>If the key represented by "var" exists in FastMap "vars", the couple (var,val) is updated in the database. The key is known as existing if the preceding value of the key (given as result of function put()) is not null.<BR></BR>
     * If the key doesn't exist, the couple is added/created in the database</LI>
     * @param var : String indicating the name of the variable for quest
     * @param value : String indicating the value of the variable for quest
     */
    operator fun set(`var`: String?, value: String?) {
        if (`var` == null || `var`.isEmpty() || value == null || value.isEmpty())
            return

        // Returns previous value associated with specified key, or null if there was no mapping for key.
        val old = _vars.put(`var`, value)

        setQuestVarInDb(`var`, value)

        if ("cond" == `var`) {
            try {
                var previousVal = 0
                previousVal = try {
                    Integer.parseInt(old!!)
                } catch (ex: Exception) {
                    0
                }

                setCond(Integer.parseInt(value), previousVal)
            } catch (e: Exception) {
                LOGGER.error(
                    "{}, {} cond [{}] is not an integer. Value stored, but no packet was sent.",
                    e,
                    player.name,
                    quest.name,
                    value
                )
            }

        }
    }

    /**
     * Add parameter used in quests.
     * @param var : String pointing out the name of the variable for quest
     * @param value : String pointing out the value of the variable for quest
     */
    fun setInternal(`var`: String?, value: String?) {
        if (`var` == null || `var`.isEmpty() || value == null || value.isEmpty())
            return

        _vars[`var`] = value
    }

    /**
     * Internally handles the progression of the quest so that it is ready for sending appropriate packets to the client<BR></BR>
     * <BR></BR>
     * <U><I>Actions :</I></U><BR></BR>
     * <LI>Check if the new progress number resets the quest to a previous (smaller) step</LI>
     * <LI>If not, check if quest progress steps have been skipped</LI>
     * <LI>If skipped, prepare the variable completedStateFlags appropriately to be ready for sending to clients</LI>
     * <LI>If no steps were skipped, flags do not need to be prepared...</LI>
     * <LI>If the passed step resets the quest to a previous step, reset such that steps after the parameter are not considered, while skipped steps before the parameter, if any, maintain their info</LI>
     * @param cond : int indicating the step number for the current quest progress (as will be shown to the client)
     * @param old : int indicating the previously noted step For more info on the variable communicating the progress steps to the client, please see
     */
    private fun setCond(cond: Int, old: Int) {
        // if there is no change since last setting, there is nothing to do here
        if (cond == old)
            return

        var completedStateFlags = 0

        // cond 0 and 1 do not need completedStateFlags. Also, if cond > 1, the 1st step must
        // always exist (i.e. it can never be skipped). So if cond is 2, we can still safely
        // assume no steps have been skipped.
        // Finally, more than 31 steps CANNOT be supported in any way with skipping.
        if (cond < 3 || cond > 31)
            unset("__compltdStateFlags")
        else
            completedStateFlags = getInt("__compltdStateFlags")

        // case 1: No steps have been skipped so far...
        if (completedStateFlags == 0) {
            // check if this step also doesn't skip anything. If so, no further work is needed
            // also, in this case, no work is needed if the state is being reset to a smaller value
            // in those cases, skip forward to informing the client about the change...

            // ELSE, if we just now skipped for the first time...prepare the flags!!!
            if (cond > old + 1) {
                // set the most significant bit to 1 (indicates that there exist skipped states)
                // also, ensure that the least significant bit is an 1 (the first step is never skipped, no matter
                // what the cond says)
                completedStateFlags = -0x7fffffff

                // since no flag had been skipped until now, the least significant bits must all
                // be set to 1, up until "old" number of bits.
                completedStateFlags = completedStateFlags or (1 shl old) - 1

                // now, just set the bit corresponding to the passed cond to 1 (current step)
                completedStateFlags = completedStateFlags or (1 shl cond - 1)
                set("__compltdStateFlags", completedStateFlags.toString())
            }
        } else {
            // if this is a push back to a previous step, clear all completion flags ahead
            if (cond < old) {
                completedStateFlags = completedStateFlags and (1 shl cond) -
                        1 // note, this also unsets the flag indicating that there exist skips

                // now, check if this resulted in no steps being skipped any more
                if (completedStateFlags == (1 shl cond) - 1)
                    unset("__compltdStateFlags")
                else {
                    // set the most significant bit back to 1 again, to correctly indicate that this skips states.
                    // also, ensure that the least significant bit is an 1 (the first step is never skipped, no matter
                    // what the cond says)
                    completedStateFlags = completedStateFlags or -0x7fffffff
                    set("__compltdStateFlags", completedStateFlags.toString())
                }
            } else {
                completedStateFlags = completedStateFlags or (1 shl cond - 1)
                set("__compltdStateFlags", completedStateFlags.toString())
            }// if this moves forward, it changes nothing on previously skipped steps...so just mark this
            // state and we are done
        }// case 2: There were exist previously skipped steps

        // send a packet to the client to inform it of the quest progress (step change)
        player!!.sendPacket(QuestList(player))

        if (quest.isRealQuest && cond > 0)
            player.sendPacket(ExShowQuestMark(quest.questId))
    }

    /**
     * Remove the variable of quest from the list of variables for the quest.<BR></BR>
     * <BR></BR>
     * <U><I>Concept : </I></U> Remove the variable of quest represented by "var" from the class variable FastMap "vars" and from the database.
     * @param var : String designating the variable for the quest to be deleted
     */
    fun unset(`var`: String) {
        if (_vars.remove(`var`) != null)
            removeQuestVarInDb(`var`)
    }

    /**
     * Return the value of the variable of quest represented by "var"
     * @param var : name of the variable of quest
     * @return String
     */
    operator fun get(`var`: String): String? {
        return _vars[`var`]
    }

    /**
     * Return the value of the variable of quest represented by "var"
     * @param var : String designating the variable for the quest
     * @return int
     */
    fun getInt(`var`: String): Int {
        val variable = _vars[`var`]
        if (variable == null || variable.isEmpty())
            return 0

        var value = 0
        try {
            value = Integer.parseInt(variable)
        } catch (e: Exception) {
            LOGGER.error("{}: variable {} isn't an integer: {}.", e, player!!.name, `var`, value)
        }

        return value
    }

    /**
     * Set in the database the quest for the player.
     * @param var : String designating the name of the variable for the quest
     * @param value : String designating the value of the variable for the quest
     */
    private fun setQuestVarInDb(`var`: String, value: String) {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(QUEST_SET_VAR).use { ps ->
                    ps.setInt(1, player!!.objectId)
                    ps.setString(2, quest.name)
                    ps.setString(3, `var`)
                    ps.setString(4, value)
                    ps.executeUpdate()
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't insert quest.", e)
        }

    }

    /**
     * Delete a variable of player's quest from the database.
     * @param var : String designating the variable characterizing the quest
     */
    private fun removeQuestVarInDb(`var`: String) {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(QUEST_DEL_VAR).use { ps ->
                    ps.setInt(1, player!!.objectId)
                    ps.setString(2, quest.name)
                    ps.setString(3, `var`)
                    ps.executeUpdate()
                }
            }
        } catch (e: Exception) {
            LOGGER.error("Couldn't delete quest.", e)
        }

    }

    /**
     * Check for an item in player's inventory.
     * @param itemId the ID of the item to check for
     * @return `true` if the item exists in player's inventory, `false` otherwise
     */
    fun hasQuestItems(itemId: Int): Boolean {
        return player!!.inventory!!.getItemByItemId(itemId) != null
    }

    /**
     * Check for multiple items in player's inventory.
     * @param itemIds a list of item IDs to check for
     * @return `true` if all items exist in player's inventory, `false` otherwise
     */
    fun hasQuestItems(vararg itemIds: Int): Boolean {
        val inv = player!!.inventory
        for (itemId in itemIds) {
            if (inv!!.getItemByItemId(itemId) == null)
                return false
        }
        return true
    }

    /**
     * Check if player possesses at least one given item.
     * @param itemIds a list of item IDs to check for
     * @return `true` if at least one item exists in player's inventory, `false` otherwise
     */
    fun hasAtLeastOneQuestItem(vararg itemIds: Int): Boolean {
        return player!!.inventory!!.hasAtLeastOneItem(*itemIds)
    }

    /**
     * @param itemId : ID of the item wanted to be count
     * @return the quantity of one sort of item hold by the player
     */
    fun getQuestItemsCount(itemId: Int): Int {
        var count = 0

        for (item in player!!.inventory!!.items)
            if (item != null && item.itemId == itemId)
                count += item.count

        return count
    }

    /**
     * @param loc A paperdoll slot to check.
     * @return the id of the item in the loc paperdoll slot.
     */
    fun getItemEquipped(loc: Int): Int {
        return player!!.inventory!!.getPaperdollItemId(loc)
    }

    /**
     * Return the level of enchantment on the weapon of the player(Done specifically for weapon SA's)
     * @param itemId : ID of the item to check enchantment
     * @return int
     */
    fun getEnchantLevel(itemId: Int): Int {
        val enchanteditem = player!!.inventory!!.getItemByItemId(itemId) ?: return 0

        return enchanteditem.enchantLevel
    }

    /**
     * Give items to the player's inventory.
     * @param itemId : Identifier of the item.
     * @param itemCount : Quantity of items to add.
     * @param enchantLevel : Enchant level of items to add.
     */
    @JvmOverloads
    fun giveItems(itemId: Int, itemCount: Int, enchantLevel: Int = 0) {
        // Incorrect amount.
        if (itemCount <= 0)
            return

        // Add items to player's inventory.
        val item = player!!.inventory!!.addItem("Quest", itemId, itemCount, player, player) ?: return

        // Set enchant level for the item.
        if (enchantLevel > 0)
            item.enchantLevel = enchantLevel

        // Send message to the client.
        if (itemId == 57) {
            val smsg = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S1_ADENA)
            smsg.addItemNumber(itemCount)
            player.sendPacket(smsg)
        } else {
            if (itemCount > 1) {
                val smsg = SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S)
                smsg.addItemName(itemId)
                smsg.addItemNumber(itemCount)
                player.sendPacket(smsg)
            } else {
                val smsg = SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1)
                smsg.addItemName(itemId)
                player.sendPacket(smsg)
            }
        }

        // Send status update packet.
        val su = StatusUpdate(player)
        su.addAttribute(StatusUpdate.CUR_LOAD, player.currentLoad)
        player.sendPacket(su)
    }

    /**
     * Remove items from the player's inventory.
     * @param itemId : Identifier of the item.
     * @param itemCount : Quantity of items to destroy.
     */
    fun takeItems(itemId: Int, itemCount: Int) {
        var itemCount = itemCount
        // Find item in player's inventory.
        val item = player.inventory!!.getItemByItemId(itemId) ?: return

        // Tests on count value and set correct value if necessary.
        if (itemCount < 0 || itemCount > item.count)
            itemCount = item.count

        // Disarm item, if equipped.
        if (item.isEquipped) {
            val unequiped = player.inventory!!.unEquipItemInBodySlotAndRecord(item)
            val iu = InventoryUpdate()
            for (itm in unequiped)
                iu.addModifiedItem(itm)

            player.sendPacket(iu)
            player.broadcastUserInfo()
        }

        // Destroy the quantity of items wanted.
        player.destroyItemByItemId("Quest", itemId, itemCount, player, true)
    }

    /**
     * Drop items to the player's inventory. Rate is 100%, amount is affected by Config.RATE_QUEST_DROP.
     * @param itemId : Identifier of the item to be dropped.
     * @param count : Quantity of items to be dropped.
     * @param neededCount : Quantity of items needed to complete the task. If set to 0, unlimited amount is collected.
     * @return boolean : Indicating whether item quantity has been reached.
     */
    fun dropItemsAlways(itemId: Int, count: Int, neededCount: Int): Boolean {
        return dropItems(itemId, count, neededCount, DropData.MAX_CHANCE, DROP_FIXED_RATE)
    }

    /**
     * Drop items to the player's inventory.
     * @param itemId : Identifier of the item to be dropped.
     * @param count : Quantity of items to be dropped.
     * @param neededCount : Quantity of items needed to complete the task. If set to 0, unlimited amount is collected.
     * @param dropChance : Item drop rate (100% chance is defined by the L2DropData.MAX_CHANCE = 1.000.000).
     * @param type : Item drop behavior: DROP_DIVMOD (rate and), DROP_FIXED_RATE, DROP_FIXED_COUNT or DROP_FIXED_BOTH
     * @return boolean : Indicating whether item quantity has been reached.
     */
    @JvmOverloads
    fun dropItems(itemId: Int, count: Int, neededCount: Int, dropChance: Int, type: Byte = DROP_DIVMOD): Boolean {
        var dropChance = dropChance
        // Get current amount of item.
        val currentCount = getQuestItemsCount(itemId)

        // Required amount reached already?
        if (neededCount > 0 && currentCount >= neededCount)
            return true

        var amount = 0
        when (type) {
            DROP_DIVMOD -> {
                dropChance *= Config.RATE_QUEST_DROP.toInt()
                amount = count * (dropChance / DropData.MAX_CHANCE)
                if (Rnd[DropData.MAX_CHANCE] < dropChance % DropData.MAX_CHANCE)
                    amount += count
            }

            DROP_FIXED_RATE -> if (Rnd[DropData.MAX_CHANCE] < dropChance)
                amount = (count * Config.RATE_QUEST_DROP).toInt()

            DROP_FIXED_COUNT -> if (Rnd[DropData.MAX_CHANCE] < dropChance * Config.RATE_QUEST_DROP)
                amount = count

            DROP_FIXED_BOTH -> if (Rnd[DropData.MAX_CHANCE] < dropChance)
                amount = count
        }

        var reached = false
        if (amount > 0) {
            // Limit count to reach required amount.
            if (neededCount > 0) {
                reached = currentCount + amount >= neededCount
                amount = if (reached) neededCount - currentCount else amount
            }

            // Inventory slot check.
            if (!player!!.inventory!!.validateCapacityByItemId(itemId))
                return false

            // Give items to the player.
            giveItems(itemId, amount, 0)

            // Play the sound.
            playSound(if (reached) SOUND_MIDDLE else SOUND_ITEMGET)
        }

        return neededCount > 0 && reached
    }

    /**
     * Drop items to the player's inventory.
     * @param rewardsInfos : Infos regarding drops (itemId, count, neededCount, dropChance).
     * @param type : Item drop behavior: DROP_DIVMOD (rate and), DROP_FIXED_RATE, DROP_FIXED_COUNT or DROP_FIXED_BOTH
     * @return boolean : Indicating whether item quantity has been reached.
     */
    @JvmOverloads
    fun dropMultipleItems(rewardsInfos: Array<IntArray>, type: Byte = DROP_DIVMOD): Boolean {
        // Used for the sound.
        var sendSound = false

        // Used for the reached state.
        var reached = true

        // For each reward type, calculate the probability of drop.
        for (info in rewardsInfos) {
            val itemId = info[0]
            val currentCount = getQuestItemsCount(itemId)
            val neededCount = info[2]

            // Required amount reached already?
            if (neededCount > 0 && currentCount >= neededCount)
                continue

            val count = info[1]

            var dropChance = info[3]
            var amount = 0

            when (type) {
                DROP_DIVMOD -> {
                    dropChance *= Config.RATE_QUEST_DROP.toInt()
                    amount = count * (dropChance / DropData.MAX_CHANCE)
                    if (Rnd[DropData.MAX_CHANCE] < dropChance % DropData.MAX_CHANCE)
                        amount += count
                }

                DROP_FIXED_RATE -> if (Rnd[DropData.MAX_CHANCE] < dropChance)
                    amount = (count * Config.RATE_QUEST_DROP).toInt()

                DROP_FIXED_COUNT -> if (Rnd[DropData.MAX_CHANCE] < dropChance * Config.RATE_QUEST_DROP)
                    amount = count

                DROP_FIXED_BOTH -> if (Rnd[DropData.MAX_CHANCE] < dropChance)
                    amount = count
            }

            if (amount > 0) {
                // Limit count to reach required amount.
                if (neededCount > 0)
                    amount = if (currentCount + amount >= neededCount) neededCount - currentCount else amount

                // Inventory slot check.
                if (!player!!.inventory!!.validateCapacityByItemId(itemId))
                    continue

                // Give items to the player.
                giveItems(itemId, amount, 0)

                // Send sound.
                sendSound = true
            }

            // Illimited needed count or current count being inferior to needed count means the state isn't reached.
            if (neededCount <= 0 || currentCount + amount < neededCount)
                reached = false
        }

        // Play the sound.
        if (sendSound)
            playSound(if (reached) SOUND_MIDDLE else SOUND_ITEMGET)

        return reached
    }

    /**
     * Reward player with items. The amount is affected by Config.RATE_QUEST_REWARD or Config.RATE_QUEST_REWARD_ADENA.
     * @param itemId : Identifier of the item.
     * @param itemCount : Quantity of item to reward before applying multiplier.
     */
    fun rewardItems(itemId: Int, itemCount: Int) {
        if (itemId == 57)
            giveItems(itemId, (itemCount * Config.RATE_QUEST_REWARD_ADENA).toInt(), 0)
        else
            giveItems(itemId, (itemCount * Config.RATE_QUEST_REWARD).toInt(), 0)
    }

    /**
     * Reward player with EXP and SP. The amount is affected by Config.RATE_QUEST_REWARD_XP and Config.RATE_QUEST_REWARD_SP
     * @param exp : Experience amount.
     * @param sp : Skill point amount.
     */
    fun rewardExpAndSp(exp: Long, sp: Int) {
        player!!.addExpAndSp((exp * Config.RATE_QUEST_REWARD_XP).toLong(), (sp * Config.RATE_QUEST_REWARD_SP).toInt())
    }

    // TODO: More radar functions need to be added when the radar class is complete.
    // BEGIN STUFF THAT WILL PROBABLY BE CHANGED

    fun addRadar(x: Int, y: Int, z: Int) {
        player!!.radar.addMarker(x, y, z)
    }

    fun removeRadar(x: Int, y: Int, z: Int) {
        player!!.radar.removeMarker(x, y, z)
    }

    fun clearRadar() {
        player!!.radar.removeAllMarkers()
    }

    // END STUFF THAT WILL PROBABLY BE CHANGED

    /**
     * Send a packet in order to play sound at client terminal
     * @param sound
     */
    fun playSound(sound: String) {
        player!!.sendPacket(PlaySound(sound))
    }

    fun showQuestionMark(number: Int) {
        player!!.sendPacket(TutorialShowQuestionMark(number))
    }

    fun playTutorialVoice(voice: String) {
        player!!.sendPacket(PlaySound(2, voice, player))
    }

    fun showTutorialHTML(html: String) {
        player!!.sendPacket(TutorialShowHtml(HtmCache.getHtmForce("data/html/scripts/quests/Tutorial/$html")))
    }

    fun closeTutorialHtml() {
        player!!.sendPacket(TutorialCloseHtml.STATIC_PACKET)
    }

    fun onTutorialClientEvent(number: Int) {
        player!!.sendPacket(TutorialEnableClientEvent(number))
    }

    companion object {
        protected val LOGGER = CLogger(QuestState::class.java.name)

        const val SOUND_ACCEPT = "ItemSound.quest_accept"
        const val SOUND_ITEMGET = "ItemSound.quest_itemget"
        const val SOUND_MIDDLE = "ItemSound.quest_middle"
        const val SOUND_FINISH = "ItemSound.quest_finish"
        const val SOUND_GIVEUP = "ItemSound.quest_giveup"
        const val SOUND_JACKPOT = "ItemSound.quest_jackpot"
        const val SOUND_FANFARE = "ItemSound.quest_fanfare_2"
        const val SOUND_BEFORE_BATTLE = "Itemsound.quest_before_battle"

        private const val QUEST_SET_VAR = "REPLACE INTO character_quests (charId,name,var,value) VALUES (?,?,?,?)"
        private const val QUEST_DEL_VAR = "DELETE FROM character_quests WHERE charId=? AND name=? AND var=?"
        private const val QUEST_DELETE = "DELETE FROM character_quests WHERE charId=? AND name=?"
        private const val QUEST_COMPLETE = "DELETE FROM character_quests WHERE charId=? AND name=? AND var<>'<state>'"

        const val DROP_DIVMOD: Byte = 0
        const val DROP_FIXED_RATE: Byte = 1
        const val DROP_FIXED_COUNT: Byte = 2
        const val DROP_FIXED_BOTH: Byte = 3
    }
}
/**
 * Give items to the player's inventory.
 * @param itemId : Identifier of the item.
 * @param itemCount : Quantity of items to add.
 */
/**
 * Drop items to the player's inventory. Rate and amount is affected by DIVMOD of Config.RATE_QUEST_DROP.
 * @param itemId : Identifier of the item to be dropped.
 * @param count : Quantity of items to be dropped.
 * @param neededCount : Quantity of items needed to complete the task. If set to 0, unlimited amount is collected.
 * @param dropChance : Item drop rate (100% chance is defined by the L2DropData.MAX_CHANCE = 1.000.000).
 * @return boolean : Indicating whether item quantity has been reached.
 */
/**
 * Drop multiple items to the player's inventory. Rate and amount is affected by DIVMOD of Config.RATE_QUEST_DROP.
 * @param rewardsInfos : Infos regarding drops (itemId, count, neededCount, dropChance).
 * @return boolean : Indicating whether item quantity has been reached.
 */
