package com.l2kt.gameserver.handler

import com.l2kt.gameserver.handler.itemhandlers.*
import com.l2kt.gameserver.model.item.kind.EtcItem
import java.util.*

object ItemHandler {
    private val _entries = HashMap<Int, IItemHandler>()

    init {
        registerHandler(BeastSoulShot())
        registerHandler(BeastSpice())
        registerHandler(BeastSpiritShot())
        registerHandler(BlessedSpiritShot())
        registerHandler(Book())
        registerHandler(Calculator())
        registerHandler(Elixir())
        registerHandler(EnchantScrolls())
        registerHandler(FishShots())
        registerHandler(Harvester())
        registerHandler(ItemSkills())
        registerHandler(Keys())
        registerHandler(Maps())
        registerHandler(MercTicket())
        registerHandler(PaganKeys())
        registerHandler(PetFood())
        registerHandler(Recipes())
        registerHandler(RollingDice())
        registerHandler(ScrollOfResurrection())
        registerHandler(SeedHandler())
        registerHandler(SevenSignsRecord())
        registerHandler(SoulShots())
        registerHandler(SpecialXMas())
        registerHandler(SoulCrystals())
        registerHandler(SpiritShot())
        registerHandler(SummonItems())
    }

    private fun registerHandler(handler: IItemHandler) {
        _entries[handler.javaClass.simpleName.intern().hashCode()] = handler
    }

    fun getHandler(item: EtcItem?): IItemHandler? {
        return if (item == null || item.handlerName == null) null else _entries[item.handlerName.hashCode()]

    }

    fun size(): Int {
        return _entries.size
    }
}