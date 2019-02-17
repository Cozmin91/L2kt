package com.l2kt.gameserver.model.base

/**
 * This class defines all races that a player can choose.
 */
enum class ClassRace constructor(
    /**
     * @return the breath multiplier.
     */
    val breathMultiplier: Double
) {
    HUMAN(1.0),
    ELF(1.5),
    DARK_ELF(1.5),
    ORC(0.9),
    DWARF(0.8)
}