package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.scripting.quests.SagasScripts.SagasSuperClass

class Q077_SagaOfTheDominator : SagasSuperClass(77, "Saga of the Dominator") {
    init {

        NPC = intArrayOf(31336, 31624, 31371, 31290, 31636, 31646, 31648, 31653, 31654, 31655, 31656, 31290)

        Items = intArrayOf(7080, 7539, 7081, 7492, 7275, 7306, 7337, 7368, 7399, 7430, 7100, 0)

        Mob = intArrayOf(27294, 27226, 27262)

        classid = 115
        prevclass = 0x33

        X = intArrayOf(164650, 47429, 47391)

        Y = intArrayOf(-74121, -56923, -56929)

        Z = intArrayOf(-2871, -2383, -2370)

        registerNPCs()
    }
}