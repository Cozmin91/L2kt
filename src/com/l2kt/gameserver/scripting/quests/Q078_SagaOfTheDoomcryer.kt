package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.scripting.quests.SagasScripts.SagasSuperClass

class Q078_SagaOfTheDoomcryer : SagasSuperClass(78, "Saga of the Doomcryer") {
    init {

        NPC = intArrayOf(31336, 31624, 31589, 31290, 31642, 31646, 31649, 31650, 31654, 31655, 31657, 31290)

        Items = intArrayOf(7080, 7539, 7081, 7493, 7276, 7307, 7338, 7369, 7400, 7431, 7101, 0)

        Mob = intArrayOf(27295, 27227, 27285)

        classid = 116
        prevclass = 0x34

        X = intArrayOf(191046, 46087, 46066)

        Y = intArrayOf(-40640, -36372, -36396)

        Z = intArrayOf(-3042, -1685, -1685)

        registerNPCs()
    }
}