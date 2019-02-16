package com.l2kt.gameserver.model.item.type

/**
 * @author mkizub
 */
enum class WeaponType constructor(val range: Int) : ItemType {
    NONE(40),
    SWORD(40),
    BLUNT(40),
    DAGGER(40),
    BOW(500),
    POLE(66),
    ETC(40),
    FIST(40),
    DUAL(40),
    DUALFIST(40),
    BIGSWORD(40),
    FISHINGROD(40),
    BIGBLUNT(40),
    PET(40);

    private val _mask: Int

    init {
        _mask = 1 shl ordinal
    }

    /**
     * Returns the ID of the item after applying the mask.
     * @return int : ID of the item
     */
    override fun mask(): Int {
        return _mask
    }
}