package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.scripting.quests.SagasScripts.SagasSuperClass

class Q094_SagaOfTheSoultaker : SagasSuperClass(94, "Saga of the Soultaker") {
    init {

        NPC = intArrayOf(30832, 31623, 31279, 31279, 31645, 31646, 31648, 31650, 31654, 31655, 31657, 31279)

        Items = intArrayOf(7080, 7533, 7081, 7509, 7292, 7323, 7354, 7385, 7416, 7447, 7085, 0)

        Mob = intArrayOf(27257, 27243, 27265)

        classid = 95
        prevclass = 0x0d

        X = intArrayOf(191046, 46066, 46087)

        Y = intArrayOf(-40640, -36396, -36372)

        Z = intArrayOf(-3042, -1685, -1685)

        registerNPCs()
    }
}