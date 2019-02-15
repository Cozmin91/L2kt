package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.scripting.quests.SagasScripts.SagasSuperClass

class Q082_SagaOfTheSagittarius : SagasSuperClass(82, "Saga of the Sagittarius") {
    init {

        NPC = intArrayOf(30702, 31627, 31604, 31640, 31633, 31646, 31647, 31650, 31654, 31655, 31657, 31641)

        Items = intArrayOf(7080, 7519, 7081, 7497, 7280, 7311, 7342, 7373, 7404, 7435, 7105, 0)

        Mob = intArrayOf(27296, 27231, 27305)

        classid = 92
        prevclass = 0x09

        X = intArrayOf(191046, 46066, 46066)

        Y = intArrayOf(-40640, -36396, -36396)

        Z = intArrayOf(-3042, -1685, -1685)

        registerNPCs()
    }
}