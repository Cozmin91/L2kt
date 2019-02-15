package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.scripting.quests.SagasScripts.SagasSuperClass

class Q097_SagaOfTheShillienTemplar : SagasSuperClass(97, "Saga of the Shillien Templar") {
    init {

        NPC = intArrayOf(31580, 31623, 31285, 31285, 31610, 31646, 31648, 31652, 31654, 31655, 31659, 31285)

        Items = intArrayOf(7080, 7526, 7081, 7512, 7295, 7326, 7357, 7388, 7419, 7450, 7091, 0)

        Mob = intArrayOf(27271, 27246, 27273)

        classid = 106
        prevclass = 0x21

        X = intArrayOf(161719, 124355, 124376)

        Y = intArrayOf(-92823, 82155, 82127)

        Z = intArrayOf(-1893, -2803, -2796)

        registerNPCs()
    }
}