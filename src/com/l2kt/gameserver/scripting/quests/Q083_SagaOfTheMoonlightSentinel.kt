package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.scripting.quests.SagasScripts.SagasSuperClass

class Q083_SagaOfTheMoonlightSentinel : SagasSuperClass(83, "Saga of the Moonlight Sentinel") {
    init {

        NPC = intArrayOf(30702, 31627, 31604, 31640, 31634, 31646, 31648, 31652, 31654, 31655, 31658, 31641)

        Items = intArrayOf(7080, 7520, 7081, 7498, 7281, 7312, 7343, 7374, 7405, 7436, 7106, 0)

        Mob = intArrayOf(27297, 27232, 27306)

        classid = 102
        prevclass = 0x18

        X = intArrayOf(161719, 181227, 181215)

        Y = intArrayOf(-92823, 36703, 36676)

        Z = intArrayOf(-1893, -4816, -4812)

        registerNPCs()
    }
}