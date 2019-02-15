package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.scripting.quests.SagasScripts.SagasSuperClass

class Q071_SagaOfEvasTemplar : SagasSuperClass(71, "Saga of Eva's Templar") {
    init {

        NPC = intArrayOf(30852, 31624, 31278, 30852, 31638, 31646, 31648, 31651, 31654, 31655, 31658, 31281)

        Items = intArrayOf(7080, 7535, 7081, 7486, 7269, 7300, 7331, 7362, 7393, 7424, 7094, 6482)

        Mob = intArrayOf(27287, 27220, 27279)

        classid = 99
        prevclass = 0x14

        X = intArrayOf(119518, 181215, 181227)

        Y = intArrayOf(-28658, 36676, 36703)

        Z = intArrayOf(-3811, -4812, -4816)

        registerNPCs()
    }
}