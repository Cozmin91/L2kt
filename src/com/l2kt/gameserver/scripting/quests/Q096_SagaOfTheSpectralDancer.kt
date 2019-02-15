package com.l2kt.gameserver.scripting.quests

import com.l2kt.gameserver.scripting.quests.SagasScripts.SagasSuperClass

class Q096_SagaOfTheSpectralDancer : SagasSuperClass(96, "Saga of the Spectral Dancer") {
    init {

        NPC = intArrayOf(31582, 31623, 31284, 31284, 31611, 31646, 31649, 31653, 31654, 31655, 31656, 31284)

        Items = intArrayOf(7080, 7527, 7081, 7511, 7294, 7325, 7356, 7387, 7418, 7449, 7092, 0)

        Mob = intArrayOf(27272, 27245, 27264)

        classid = 107
        prevclass = 0x22

        X = intArrayOf(164650, 47429, 47391)

        Y = intArrayOf(-74121, -56923, -56929)

        Z = intArrayOf(-2871, -2383, -2370)

        registerNPCs()
    }
}