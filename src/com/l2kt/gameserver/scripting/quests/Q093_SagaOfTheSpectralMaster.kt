package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.scripting.quests.SagasScripts.SagasSuperClass

class Q093_SagaOfTheSpectralMaster : SagasSuperClass(93, "Saga of the Spectral Master") {
    init {

        NPC = intArrayOf(30175, 31287, 31613, 30175, 31632, 31646, 31649, 31653, 31654, 31655, 31656, 31613)

        Items = intArrayOf(7080, 7606, 7081, 7508, 7291, 7322, 7353, 7384, 7415, 7446, 7112, 0)

        Mob = intArrayOf(27315, 27242, 27312)

        classid = 111
        prevclass = 0x29

        X = intArrayOf(164650, 47429, 47391)

        Y = intArrayOf(-74121, -56923, -56929)

        Z = intArrayOf(-2871, -2383, -2370)

        registerNPCs()
    }
}