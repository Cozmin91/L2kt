package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.scripting.quests.SagasScripts.SagasSuperClass

class Q074_SagaOfTheDreadnought : SagasSuperClass(74, "Saga of the Dreadnought") {
    init {

        NPC = intArrayOf(30850, 31624, 31298, 31276, 31595, 31646, 31648, 31650, 31654, 31655, 31657, 31522)

        Items = intArrayOf(7080, 7538, 7081, 7489, 7272, 7303, 7334, 7365, 7396, 7427, 7097, 6480)

        Mob = intArrayOf(27290, 27223, 27282)

        classid = 89
        prevclass = 0x03

        X = intArrayOf(191046, 46087, 46066)

        Y = intArrayOf(-40640, -36372, -36396)

        Z = intArrayOf(-3042, -1685, -1685)

        registerNPCs()
    }
}