package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.scripting.quests.SagasScripts.SagasSuperClass

class Q088_SagaOfTheArchmage : SagasSuperClass(88, "Saga of the Archmage") {
    init {

        NPC = intArrayOf(30176, 31627, 31282, 31282, 31590, 31646, 31647, 31650, 31654, 31655, 31657, 31282)

        Items = intArrayOf(7080, 7529, 7081, 7503, 7286, 7317, 7348, 7379, 7410, 7441, 7082, 0)

        Mob = intArrayOf(27250, 27237, 27254)

        classid = 94
        prevclass = 0x0c

        X = intArrayOf(191046, 46066, 46087)

        Y = intArrayOf(-40640, -36396, -36372)

        Z = intArrayOf(-3042, -1685, -1685)

        registerNPCs()
    }
}