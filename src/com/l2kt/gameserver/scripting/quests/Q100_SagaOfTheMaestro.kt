package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.scripting.quests.SagasScripts.SagasSuperClass

class Q100_SagaOfTheMaestro : SagasSuperClass(100, "Saga of the Maestro") {
    init {

        NPC = intArrayOf(31592, 31273, 31597, 31597, 31596, 31646, 31648, 31653, 31654, 31655, 31656, 31597)

        Items = intArrayOf(7080, 7607, 7081, 7515, 7298, 7329, 7360, 7391, 7422, 7453, 7108, 0)

        Mob = intArrayOf(27260, 27249, 27308)

        classid = 118
        prevclass = 0x39

        X = intArrayOf(164650, 47429, 47391)

        Y = intArrayOf(-74121, -56923, -56929)

        Z = intArrayOf(-2871, -2383, -2370)

        registerNPCs()
    }
}