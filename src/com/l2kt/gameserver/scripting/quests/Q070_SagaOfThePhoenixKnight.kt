package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.scripting.quests.SagasScripts.SagasSuperClass

class Q070_SagaOfThePhoenixKnight : SagasSuperClass(70, "Saga of the Phoenix Knight") {
    init {

        NPC = intArrayOf(30849, 31624, 31277, 30849, 31631, 31646, 31647, 31650, 31654, 31655, 31657, 31277)

        Items = intArrayOf(7080, 7534, 7081, 7485, 7268, 7299, 7330, 7361, 7392, 7423, 7093, 6482)

        Mob = intArrayOf(27286, 27219, 27278)

        classid = 90
        prevclass = 0x05

        X = intArrayOf(191046, 46087, 46066)

        Y = intArrayOf(-40640, -36372, -36396)

        Z = intArrayOf(-3042, -1685, -1685)

        registerNPCs()
    }
}