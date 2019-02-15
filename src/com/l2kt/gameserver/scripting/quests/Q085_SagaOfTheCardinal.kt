package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.scripting.quests.SagasScripts.SagasSuperClass

class Q085_SagaOfTheCardinal : SagasSuperClass(85, "Saga of the Cardinal") {
    init {

        NPC = intArrayOf(30191, 31626, 31588, 31280, 31644, 31646, 31647, 31651, 31654, 31655, 31658, 31280)

        Items = intArrayOf(7080, 7522, 7081, 7500, 7283, 7314, 7345, 7376, 7407, 7438, 7087, 0)

        Mob = intArrayOf(27267, 27234, 27274)

        classid = 97
        prevclass = 0x10

        X = intArrayOf(119518, 181215, 181227)

        Y = intArrayOf(-28658, 36676, 36703)

        Z = intArrayOf(-3811, -4812, -4816)

        registerNPCs()
    }
}