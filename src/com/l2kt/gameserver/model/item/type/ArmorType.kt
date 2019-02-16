package com.l2kt.gameserver.model.item.type

enum class ArmorType : ItemType {
    NONE,
    LIGHT,
    HEAVY,
    MAGIC,
    PET,
    SHIELD;

    internal val _mask: Int = 1 shl ordinal + WeaponType.values().size

    /**
     * Returns the ID of the ArmorType after applying a mask.
     * @return int : ID of the ArmorType after mask
     */
    override fun mask(): Int {
        return _mask
    }
}