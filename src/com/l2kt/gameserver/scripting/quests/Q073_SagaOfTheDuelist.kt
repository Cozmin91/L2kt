package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.scripting.quests.SagasScripts.SagasSuperClass

class Q073_SagaOfTheDuelist : SagasSuperClass(73, "Saga of the Duelist") {
    init {

        NPC = intArrayOf(30849, 31624, 31226, 31331, 31639, 31646, 31647, 31653, 31654, 31655, 31656, 31277)

        Items = intArrayOf(7080, 7537, 7081, 7488, 7271, 7302, 7333, 7364, 7395, 7426, 7096, 7546)

        Mob = intArrayOf(27289, 27222, 27281)

        classid = 88
        prevclass = 0x02

        X = intArrayOf(164650, 47429, 47391)

        Y = intArrayOf(-74121, -56923, -56929)

        Z = intArrayOf(-2871, -2383, -2370)

        registerNPCs()
    }
}