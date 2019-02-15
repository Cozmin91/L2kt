package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.scripting.quests.SagasScripts.SagasSuperClass

class Q072_SagaOfTheSwordMuse : SagasSuperClass(72, "Saga of the Sword Muse") {
    init {

        NPC = intArrayOf(30853, 31624, 31583, 31537, 31618, 31646, 31649, 31652, 31654, 31655, 31659, 31281)

        Items = intArrayOf(7080, 7536, 7081, 7487, 7270, 7301, 7332, 7363, 7394, 7425, 7095, 6482)

        Mob = intArrayOf(27288, 27221, 27280)

        classid = 100
        prevclass = 0x15

        X = intArrayOf(161719, 124355, 124376)

        Y = intArrayOf(-92823, 82155, 82127)

        Z = intArrayOf(-1893, -2803, -2796)

        registerNPCs()
    }
}