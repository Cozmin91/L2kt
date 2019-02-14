package com.l2kt.gameserver.model.soulcrystal

import com.l2kt.gameserver.templates.StatsSet

/**
 * This class stores Soul Crystal leveling infos related to items, notably:
 *
 *  * The current level on the hierarchy tree of items ;
 *  * The initial itemId from where we start ;
 *  * The succeeded itemId rewarded if absorb was successful ;
 *  * The broken itemId rewarded if absorb failed.
 *
 */
class SoulCrystal(set: StatsSet) {
    val level: Int = set.getInteger("level")
    val initialItemId: Int = set.getInteger("initial")
    val stagedItemId: Int = set.getInteger("staged")
    val brokenItemId: Int = set.getInteger("broken")
}