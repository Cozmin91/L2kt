package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.scripting.quests.SagasScripts.SagasSuperClass

class Q076_SagaOfTheGrandKhavatari : SagasSuperClass(76, "Saga of the Grand Khavatari") {
    init {

        NPC = intArrayOf(31339, 31624, 31589, 31290, 31637, 31646, 31647, 31652, 31654, 31655, 31659, 31290)

        Items = intArrayOf(7080, 7539, 7081, 7491, 7274, 7305, 7336, 7367, 7398, 7429, 7099, 0)

        Mob = intArrayOf(27293, 27226, 27284)

        classid = 114
        prevclass = 0x30

        X = intArrayOf(161719, 124355, 124376)

        Y = intArrayOf(-92823, 82155, 82127)

        Z = intArrayOf(-1893, -2803, -2796)

        registerNPCs()
    }
}