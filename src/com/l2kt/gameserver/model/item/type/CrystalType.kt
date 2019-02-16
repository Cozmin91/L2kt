package com.l2kt.gameserver.model.item.type

/**
 * Crystal Type enumerated.
 * @author Adry_85
 */
enum class CrystalType constructor(
    /**
     * Gets the crystal type ID.
     * @return the crystal type ID
     */
    val id: Int,
    /**
     * Gets the item ID of the crystal.
     * @return the item ID of the crystal
     */
    val crystalId: Int, val crystalEnchantBonusArmor: Int, val crystalEnchantBonusWeapon: Int
) {
    NONE(0, 0, 0, 0),
    D(1, 1458, 11, 90),
    C(2, 1459, 6, 45),
    B(3, 1460, 11, 67),
    A(4, 1461, 19, 144),
    S(5, 1462, 25, 250);

    fun isGreater(crystalType: CrystalType): Boolean {
        return id > crystalType.id
    }

    fun isLesser(crystalType: CrystalType): Boolean {
        return id < crystalType.id
    }
}