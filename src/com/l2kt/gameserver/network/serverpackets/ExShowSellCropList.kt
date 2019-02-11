package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.data.manager.CastleManorManager
import com.l2kt.gameserver.model.item.instance.ItemInstance
import com.l2kt.gameserver.model.itemcontainer.PcInventory
import com.l2kt.gameserver.model.manor.CropProcure
import java.util.*

class ExShowSellCropList(inventory: PcInventory, manorId: Int) : L2GameServerPacket() {
    private var _manorId = 1
    private val _cropsItems: MutableMap<Int, ItemInstance>
    private val _castleCrops: MutableMap<Int, CropProcure>

    init {
        _manorId = manorId
        _castleCrops = HashMap()
        _cropsItems = HashMap()

        for (cropId in CastleManorManager.getInstance().cropIds) {
            val item = inventory.getItemByItemId(cropId)
            if (item != null)
                _cropsItems[cropId] = item
        }

        for (crop in CastleManorManager.getInstance().getCropProcure(_manorId, false)) {
            if (_cropsItems.containsKey(crop.id) && crop.amount > 0)
                _castleCrops[crop.id] = crop
        }
    }

    public override fun writeImpl() {
        writeC(0xFE)
        writeH(0x21)

        writeD(_manorId)
        writeD(_cropsItems.size)

        for (item in _cropsItems.values) {
            val seed = CastleManorManager.getInstance().getSeedByCrop(item.itemId)

            writeD(item.objectId)
            writeD(item.itemId)
            writeD(seed!!.level)
            writeC(1)
            writeD(seed.getReward(1))
            writeC(1)
            writeD(seed.getReward(2))

            if (_castleCrops.containsKey(item.itemId)) {
                val crop = _castleCrops[item.itemId]!!
                writeD(_manorId)
                writeD(crop.amount)
                writeD(crop.price)
                writeC(crop.reward)
            } else {
                writeD(-0x1)
                writeD(0)
                writeD(0)
                writeC(0)
            }
            writeD(item.count)
        }
    }
}