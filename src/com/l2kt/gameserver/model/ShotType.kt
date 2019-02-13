package com.l2kt.gameserver.model

/**
 * @author UnAfraid
 */
enum class ShotType {
    SOULSHOT,
    SPIRITSHOT,
    BLESSED_SPIRITSHOT,
    FISH_SOULSHOT;

    val mask: Int = 1 shl ordinal

}