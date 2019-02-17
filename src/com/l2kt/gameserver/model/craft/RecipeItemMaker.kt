package com.l2kt.gameserver.model.craft

import com.l2kt.Config
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.model.holder.IntIntHolder
import com.l2kt.gameserver.model.item.Recipe
import com.l2kt.gameserver.network.SystemMessageId
import com.l2kt.gameserver.network.serverpackets.*
import java.util.logging.Logger

/**
 * The core of craft system, which allow [Player] to exchange materials for a final product. Numerous checks are made (integrity checks, such as item existence, mana left, adena cost).<br></br>
 * <br></br>
 * Required mats / final product infos are controlled by a [Recipe].
 */
class RecipeItemMaker(
    protected val _player: Player // "crafter"
    , protected val _recipe: Recipe, protected val _target: Player // "customer"
) : Runnable {

    var _isValid: Boolean = false
    protected var _skillId: Int = 0
    protected var _skillLevel: Int = 0
    protected var _manaRequired: Double = 0.toDouble()
    protected var _price: Int = 0

    init {
        run{
            _isValid = false
            _skillId = if (_recipe.isDwarven) L2Skill.SKILL_CREATE_DWARVEN else L2Skill.SKILL_CREATE_COMMON
            _skillLevel = _player.getSkillLevel(_skillId)

            _manaRequired = _recipe.mpCost.toDouble()

            _player.isCrafting = true

            if (_player.isAlikeDead || _target!!.isAlikeDead) {
                _player.sendPacket(ActionFailed.STATIC_PACKET)
                abort()
                return@run
            }

            if (_player.isProcessingTransaction || _target!!.isProcessingTransaction) {
                _target!!.sendPacket(ActionFailed.STATIC_PACKET)
                abort()
                return@run
            }

            // Validate skill level.
            if (_recipe.level > _skillLevel) {
                _player.sendPacket(ActionFailed.STATIC_PACKET)
                abort()
                return@run
            }

            // Check if that customer can afford to pay for creation services. Also check manufacturer integrity.
            if (_player != _target) {
                for (temp in _player.createList.list) {
                    // Find recipe for item we want manufactured.
                    if (temp.id == _recipe.id) {
                        _price = temp.value
                        if (_target.adena < _price) {
                            _target.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA)
                            abort()
                            return@run
                        }
                        break
                    }
                }
            }

            // Check if inventory got all required materials.
            if (!listItems(false)) {
                abort()
                return@run
            }

            // Initial mana check requires MP as written on recipe.
            if (_player.currentMp < _manaRequired) {
                _target.sendPacket(SystemMessageId.NOT_ENOUGH_MP)
                abort()
                return@run
            }

            updateMakeInfo(true)
            updateStatus()

            _player.isCrafting = false
            _isValid = true
        }
    }

    override fun run() {
        if (!Config.IS_CRAFTING_ENABLED) {
            _target!!.sendMessage("Item creation is currently disabled.")
            abort()
            return
        }

        if (_player == null || _target == null) {
            abort()
            return
        }

        if (!_player.isOnline || !_target.isOnline) {
            abort()
            return
        }

        _player.reduceCurrentMp(_manaRequired)

        // First take adena for manufacture ; customer must pay for services.
        if (_target != _player && _price > 0) {
            val adenaTransfer = _target.transferItem(
                "PayManufacture",
                _target.inventory!!.adenaInstance!!.objectId,
                _price,
                _player.inventory,
                _player
            )
            if (adenaTransfer == null) {
                _target.sendPacket(SystemMessageId.YOU_NOT_ENOUGH_ADENA)
                abort()
                return
            }
        }

        // Inventory check failed.
        if (!listItems(true)) {
            abort()
            return
        }

        // Success ; we reward the player and update the craft window.
        if (Rnd[100] < _recipe.successRate) {
            rewardPlayer()
            updateMakeInfo(true)
        } else {
            if (_target != _player) {
                _player.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.CREATION_OF_S2_FOR_S1_AT_S3_ADENA_FAILED).addCharName(
                        _target
                    ).addItemName(_recipe.product.id).addItemNumber(_price)
                )
                _target.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.S1_FAILED_TO_CREATE_S2_FOR_S3_ADENA).addCharName(
                        _player
                    ).addItemName(_recipe.product.id).addItemNumber(_price)
                )
            } else
                _target.sendPacket(SystemMessageId.ITEM_MIXING_FAILED)

            updateMakeInfo(false)
        }// Fail ; we only send messages and update craft window.

        // Update load and mana bar of craft window.
        updateStatus()

        _player.isCrafting = false
        _target.sendPacket(ItemList(_target, false))
    }

    /**
     * Send to the [Player] customer [RecipeItemMakeInfo] (self crafting) or [RecipeShopItemInfo] (private workshop) packet.
     * @param success : The result under a boolean, used by packet.
     */
    private fun updateMakeInfo(success: Boolean) {
        if (_target == _player)
            _target!!.sendPacket(RecipeItemMakeInfo(_recipe.id, _target, if (success) 1 else 0))
        else
            _target!!.sendPacket(RecipeShopItemInfo(_player!!, _recipe.id))
    }

    /**
     * Update [Player] customer MP and load status.
     */
    private fun updateStatus() {
        val su = StatusUpdate(_target!!)
        su.addAttribute(StatusUpdate.CUR_MP, _target.currentMp.toInt())
        su.addAttribute(StatusUpdate.CUR_LOAD, _target.currentLoad)
        _target.sendPacket(su)
    }

    /**
     * List all required materials.
     * @param remove : If true we also delete items from customer inventory.
     * @return true if the [Player] customer got every item (with correct amount) on inventory.
     */
    private fun listItems(remove: Boolean): Boolean {
        val inv = _target!!.inventory

        var gotAllMats = true
        for (material in _recipe.materials) {
            val quantity = material.value
            if (quantity > 0) {
                val item = inv!!.getItemByItemId(material.id)
                if (item == null || item.count < quantity) {
                    _target.sendPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.MISSING_S2_S1_TO_CREATE).addItemName(
                            material.id
                        ).addItemNumber(if (item == null) quantity else quantity - item.count)
                    )
                    gotAllMats = false
                }
            }
        }

        if (!gotAllMats)
            return false

        if (remove) {
            for (material in _recipe.materials) {
                inv!!.destroyItemByItemId("Manufacture", material.id, material.value, _target, _player)

                if (material.value > 1)
                    _target.sendPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.S2_S1_DISAPPEARED).addItemName(
                            material.id
                        ).addItemNumber(material.value)
                    )
                else
                    _target.sendPacket(
                        SystemMessage.getSystemMessage(SystemMessageId.S1_DISAPPEARED).addItemName(
                            material.id
                        )
                    )
            }
        }
        return true
    }

    /**
     * Abort the crafting mode for the [Player].
     */
    private fun abort() {
        updateMakeInfo(false)
        _player!!.isCrafting = false
    }

    /**
     * Reward a [Player] with the result of a craft (retained into a [IntIntHolder]).
     */
    private fun rewardPlayer() {
        val itemId = _recipe.product.id
        val itemCount = _recipe.product.value

        _target!!.inventory!!.addItem("Manufacture", itemId, itemCount, _target, _player)

        // inform customer of earned item
        if (_target != _player) {
            // inform manufacturer of earned profit
            if (itemCount == 1) {
                _player!!.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.S2_CREATED_FOR_S1_FOR_S3_ADENA).addString(
                        _target.name
                    ).addItemName(itemId).addItemNumber(_price)
                )
                _target.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.S1_CREATED_S2_FOR_S3_ADENA).addString(
                        _player.name
                    ).addItemName(itemId).addItemNumber(_price)
                )
            } else {
                _player!!.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.S2_S3_S_CREATED_FOR_S1_FOR_S4_ADENA).addString(
                        _target.name
                    ).addNumber(itemCount).addItemName(itemId).addItemNumber(_price)
                )
                _target.sendPacket(
                    SystemMessage.getSystemMessage(SystemMessageId.S1_CREATED_S2_S3_S_FOR_S4_ADENA).addString(
                        _player.name
                    ).addNumber(itemCount).addItemName(itemId).addItemNumber(_price)
                )
            }
        }

        if (itemCount > 1)
            _target.sendPacket(
                SystemMessage.getSystemMessage(SystemMessageId.EARNED_S2_S1_S).addItemName(itemId).addNumber(
                    itemCount
                )
            )
        else
            _target.sendPacket(SystemMessage.getSystemMessage(SystemMessageId.EARNED_ITEM_S1).addItemName(itemId))

        updateMakeInfo(true) // success
    }

    companion object {
        protected val LOG = Logger.getLogger(RecipeItemMaker::class.java.name)
    }
}