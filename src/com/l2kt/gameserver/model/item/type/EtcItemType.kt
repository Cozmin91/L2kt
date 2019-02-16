package com.l2kt.gameserver.model.item.type

enum class EtcItemType : ItemType {
    NONE,
    ARROW,
    POTION,
    SCRL_ENCHANT_WP,
    SCRL_ENCHANT_AM,
    SCROLL,
    RECIPE,
    MATERIAL,
    PET_COLLAR,
    CASTLE_GUARD,
    LOTTO,
    RACE_TICKET,
    DYE,
    SEED,
    CROP,
    MATURECROP,
    HARVEST,
    SEED2,
    TICKET_OF_LORD,
    LURE,
    BLESS_SCRL_ENCHANT_WP,
    BLESS_SCRL_ENCHANT_AM,
    COUPON,
    ELIXIR,

    // L2J CUSTOM, BACKWARD COMPATIBILITY
    SHOT,
    HERB,
    QUEST;

    /**
     * Returns the ID of the item after applying the mask.
     * @return int : ID of the item
     */
    override fun mask(): Int {
        return 0
    }
}