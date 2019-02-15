package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.scripting.quests.SagasScripts.SagasSuperClass

class Q087_SagaOfEvasSaint : SagasSuperClass(87, "Saga of Eva's Saint") {
    init {

        NPC = intArrayOf(30191, 31626, 31588, 31280, 31620, 31646, 31649, 31653, 31654, 31655, 31657, 31280)

        Items = intArrayOf(7080, 7524, 7081, 7502, 7285, 7316, 7347, 7378, 7409, 7440, 7088, 0)

        Mob = intArrayOf(27266, 27236, 27276)

        classid = 105
        prevclass = 0x1e

        X = intArrayOf(164650, 46087, 46066)

        Y = intArrayOf(-74121, -36372, -36396)

        Z = intArrayOf(-2871, -1685, -1685)

        registerNPCs()
    }
}