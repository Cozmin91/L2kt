package com.l2kt.gameserver.model.zone

enum class ZoneId constructor(val id: Int) {
    PVP(0),
    PEACE(1),
    SIEGE(2),
    MOTHER_TREE(3),
    CLAN_HALL(4),
    NO_LANDING(5),
    WATER(6),
    JAIL(7),
    MONSTER_TRACK(8),
    CASTLE(9),
    SWAMP(10),
    NO_SUMMON_FRIEND(11),
    NO_STORE(12),
    TOWN(13),
    HQ(14),
    DANGER_AREA(15),
    CAST_ON_ARTIFACT(16),
    NO_RESTART(17),
    SCRIPT(18),
    BOSS(19);


    companion object {

        val VALUES = values()
    }
}