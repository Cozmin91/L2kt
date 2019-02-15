package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.scripting.quests.SagasScripts.SagasSuperClass

class Q099_SagaOfTheFortuneSeeker : SagasSuperClass(99, "Saga of the Fortune Seeker") {
    init {

        NPC = intArrayOf(31594, 31623, 31600, 31600, 31601, 31646, 31649, 31650, 31654, 31655, 31657, 31600)

        Items = intArrayOf(7080, 7608, 7081, 7514, 7297, 7328, 7359, 7390, 7421, 7452, 7109, 0)

        Mob = intArrayOf(27259, 27248, 27309)

        classid = 117
        prevclass = 0x37

        X = intArrayOf(191046, 46066, 46087)

        Y = intArrayOf(-40640, -36396, -36372)

        Z = intArrayOf(-3042, -1685, -1685)

        registerNPCs()
    }
}