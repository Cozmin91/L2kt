package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.scripting.quests.SagasScripts.SagasSuperClass

class Q095_SagaOfTheHellKnight : SagasSuperClass(95, "Saga of the Hell Knight") {
    init {

        NPC = intArrayOf(31582, 31623, 31297, 31297, 31599, 31646, 31647, 31653, 31654, 31655, 31656, 31297)

        Items = intArrayOf(7080, 7532, 7081, 7510, 7293, 7324, 7355, 7386, 7417, 7448, 7086, 0)

        Mob = intArrayOf(27258, 27244, 27263)

        classid = 91
        prevclass = 0x06

        X = intArrayOf(164650, 47391, 47429)

        Y = intArrayOf(-74121, -56929, -56923)

        Z = intArrayOf(-2871, -2370, -2383)

        registerNPCs()
    }
}