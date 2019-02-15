package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.scripting.quests.SagasScripts.SagasSuperClass

class Q090_SagaOfTheStormScreamer : SagasSuperClass(90, "Saga of the Storm Screamer") {
    init {

        NPC = intArrayOf(30175, 31627, 31287, 31287, 31598, 31646, 31649, 31652, 31654, 31655, 31659, 31287)

        Items = intArrayOf(7080, 7531, 7081, 7505, 7288, 7319, 7350, 7381, 7412, 7443, 7084, 0)

        Mob = intArrayOf(27252, 27239, 27256)

        classid = 110
        prevclass = 0x28

        X = intArrayOf(161719, 124376, 124355)

        Y = intArrayOf(-92823, 82127, 82155)

        Z = intArrayOf(-1893, -2796, -2803)

        registerNPCs()
    }
}