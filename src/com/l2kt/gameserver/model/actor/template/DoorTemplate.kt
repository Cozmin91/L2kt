package com.l2kt.gameserver.model.actor.template

import com.l2kt.gameserver.templates.StatsSet

/**
 * @author Hasha
 */
class DoorTemplate(stats: StatsSet) : CreatureTemplate(stats) {

    val name: String = stats.getString("name")
    val id: Int = stats.getInteger("id")
    val type: DoorType = stats.getEnum("type", DoorType::class.java)
    val level: Int = stats.getInteger("level")

    // coordinates can be part of template, since we spawn 1 instance of door at fixed position
    val posX: Int = stats.getInteger("posX")
    val posY: Int = stats.getInteger("posY")
    val posZ: Int = stats.getInteger("posZ")

    // geodata description of the door
    val geoX: Int = stats.getInteger("geoX")
    val geoY: Int = stats.getInteger("geoY")
    val geoZ: Int = stats.getInteger("geoZ")
    val geoData: Array<ByteArray> = stats.getObject("geoData", Array<ByteArray>::class.java) ?: emptyArray()

    val castle: Int = stats.getInteger("castle", 0)
    val triggerId: Int = stats.getInteger("triggeredId", 0)
    val isOpened: Boolean = stats.getBool("opened", false)

    val openType: OpenType = stats.getEnum("openType", OpenType::class.java, OpenType.NPC) ?: OpenType.NPC
    val openTime: Int = stats.getInteger("openTime", 0)
    val randomTime: Int = stats.getInteger("randomTime", 0)
    val closeTime: Int = stats.getInteger("closeTime", 0)

    enum class DoorType {
        DOOR,
        WALL
    }

    enum class OpenType {
        CLICK,
        TIME,
        SKILL,
        NPC
    }
}