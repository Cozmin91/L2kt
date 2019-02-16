package com.l2kt.gameserver.model.item

import com.l2kt.gameserver.instancemanager.SevenSigns.CabalType
import com.l2kt.gameserver.templates.StatsSet

class MercenaryTicket(set: StatsSet) {

    val itemId: Int = set.getInteger("itemId")
    val type: TicketType
    val isStationary: Boolean
    val npcId: Int
    val maxAmount: Int
    private val _ssq: MutableList<CabalType>

    enum class TicketType {
        SWORD,
        POLE,
        BOW,
        CLERIC,
        WIZARD,
        TELEPORTER
    }

    init {
        type = set.getEnum("type", TicketType::class.java)
        isStationary = set.getBool("stationary")
        npcId = set.getInteger("npcId")
        maxAmount = set.getInteger("maxAmount")

        val ssq = set.getStringArray("ssq")

        _ssq = mutableListOf()
        for (i in ssq.indices)
            _ssq.add(i, CabalType.valueOf(ssq[i]))
    }

    fun isSsqType(type: CabalType): Boolean {
        return _ssq.contains(type)
    }
}