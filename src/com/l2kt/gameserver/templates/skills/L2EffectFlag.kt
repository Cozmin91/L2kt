package com.l2kt.gameserver.templates.skills

enum class L2EffectFlag {
    NONE,
    CHARM_OF_COURAGE,
    CHARM_OF_LUCK,
    PHOENIX_BLESSING,
    NOBLESS_BLESSING,
    SILENT_MOVE,
    PROTECTION_BLESSING,
    RELAXING,
    FEAR,
    CONFUSED,
    MUTED,
    PHYSICAL_MUTED,
    ROOTED,
    SLEEP,
    STUNNED,
    BETRAYED,
    MEDITATING,
    PARALYZED;

    val mask: Int
        get() = 1 shl ordinal
}