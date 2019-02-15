package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.scripting.quests.SagasScripts.SagasSuperClass

class Q092_SagaOfTheElementalMaster : SagasSuperClass(92, "Saga of the Elemental Master") {
    init {

        NPC = intArrayOf(30174, 31281, 31614, 31614, 31629, 31646, 31648, 31652, 31654, 31655, 31659, 31614)

        Items = intArrayOf(7080, 7605, 7081, 7507, 7290, 7321, 7352, 7383, 7414, 7445, 7111, 0)

        Mob = intArrayOf(27314, 27241, 27311)

        classid = 104
        prevclass = 0x1c

        X = intArrayOf(161719, 124376, 124355)

        Y = intArrayOf(-92823, 82127, 82155)

        Z = intArrayOf(-1893, -2796, -2803)

        registerNPCs()
    }
}