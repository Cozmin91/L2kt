package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.scripting.quests.SagasScripts.SagasSuperClass

class Q081_SagaOfTheGhostHunter : SagasSuperClass(81, "Saga of the Ghost Hunter") {
    init {

        NPC = intArrayOf(31603, 31624, 31286, 31615, 31617, 31646, 31649, 31653, 31654, 31655, 31656, 31616)

        Items = intArrayOf(7080, 7518, 7081, 7496, 7279, 7310, 7341, 7372, 7403, 7434, 7104, 0)

        Mob = intArrayOf(27301, 27230, 27304)

        classid = 108
        prevclass = 0x24

        X = intArrayOf(164650, 47391, 47429)

        Y = intArrayOf(-74121, -56929, -56923)

        Z = intArrayOf(-2871, -2370, -2383)

        registerNPCs()
    }
}