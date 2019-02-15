package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.scripting.quests.SagasScripts.SagasSuperClass

class Q086_SagaOfTheHierophant : SagasSuperClass(86, "Saga of the Hierophant") {
    init {

        NPC = intArrayOf(30191, 31626, 31588, 31280, 31591, 31646, 31648, 31652, 31654, 31655, 31659, 31280)

        Items = intArrayOf(7080, 7523, 7081, 7501, 7284, 7315, 7346, 7377, 7408, 7439, 7089, 0)

        Mob = intArrayOf(27269, 27235, 27275)

        classid = 98
        prevclass = 0x11

        X = intArrayOf(161719, 124355, 124376)

        Y = intArrayOf(-92823, 82155, 82127)

        Z = intArrayOf(-1893, -2803, -2796)

        registerNPCs()
    }
}