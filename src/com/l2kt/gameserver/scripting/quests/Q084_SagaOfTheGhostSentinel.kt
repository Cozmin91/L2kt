package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.scripting.quests.SagasScripts.SagasSuperClass

class Q084_SagaOfTheGhostSentinel : SagasSuperClass(84, "Saga of the Ghost Sentinel") {
    init {

        NPC = intArrayOf(30702, 31587, 31604, 31640, 31635, 31646, 31649, 31652, 31654, 31655, 31659, 31641)

        Items = intArrayOf(7080, 7521, 7081, 7499, 7282, 7313, 7344, 7375, 7406, 7437, 7107, 0)

        Mob = intArrayOf(27298, 27233, 27307)

        classid = 109
        prevclass = 0x25

        X = intArrayOf(161719, 124376, 124376)

        Y = intArrayOf(-92823, 82127, 82127)

        Z = intArrayOf(-1893, -2796, -2796)

        registerNPCs()
    }
}