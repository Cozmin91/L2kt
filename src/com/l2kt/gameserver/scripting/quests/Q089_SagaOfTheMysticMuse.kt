package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.scripting.quests.SagasScripts.SagasSuperClass

class Q089_SagaOfTheMysticMuse : SagasSuperClass(89, "Saga of the Mystic Muse") {
    init {

        NPC = intArrayOf(30174, 31627, 31283, 31283, 31643, 31646, 31648, 31651, 31654, 31655, 31658, 31283)

        Items = intArrayOf(7080, 7530, 7081, 7504, 7287, 7318, 7349, 7380, 7411, 7442, 7083, 0)

        Mob = intArrayOf(27251, 27238, 27255)

        classid = 103
        prevclass = 0x1b

        X = intArrayOf(119518, 181227, 181215)

        Y = intArrayOf(-28658, 36703, 36676)

        Z = intArrayOf(-3811, -4816, -4812)

        registerNPCs()
    }
}