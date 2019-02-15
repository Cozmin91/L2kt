package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.scripting.quests.SagasScripts.SagasSuperClass

class Q080_SagaOfTheWindRider : SagasSuperClass(80, "Saga of the Wind Rider") {
    init {

        NPC = intArrayOf(31603, 31624, 31284, 31615, 31612, 31646, 31648, 31652, 31654, 31655, 31659, 31616)

        Items = intArrayOf(7080, 7517, 7081, 7495, 7278, 7309, 7340, 7371, 7402, 7433, 7103, 0)

        Mob = intArrayOf(27300, 27229, 27303)

        classid = 101
        prevclass = 0x17

        X = intArrayOf(161719, 124314, 124355)

        Y = intArrayOf(-92823, 82155, 82155)

        Z = intArrayOf(-1893, -2803, -2803)

        registerNPCs()
    }
}