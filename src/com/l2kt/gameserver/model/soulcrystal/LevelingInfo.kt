package com.l2kt.gameserver.model.soulcrystal

import com.l2kt.gameserver.templates.StatsSet

/**
 * This class stores Soul Crystal leveling infos related to NPCs, notably:
 *
 *  * AbsorbCrystalType which can be LAST_HIT, FULL_PARTY or PARTY_ONE_RANDOM ;
 *  * If the item cast on monster is required or not ;
 *  * The chance of break (base 1000) ;
 *  * The chance of success (base 1000) ;
 *  * The list of allowed crystals levels.
 *
 */
class LevelingInfo(set: StatsSet) {

    val absorbCrystalType: AbsorbCrystalType = set.getEnum("absorbType", AbsorbCrystalType::class.java)
    val isSkillRequired: Boolean = set.getBool("skill")
    val chanceStage: Int = set.getInteger("chanceStage")
    val chanceBreak: Int = set.getInteger("chanceBreak")
    val levelList: IntArray = set.getIntegerArray("levelList")

    enum class AbsorbCrystalType {
        LAST_HIT,
        FULL_PARTY,
        PARTY_ONE_RANDOM
    }
}