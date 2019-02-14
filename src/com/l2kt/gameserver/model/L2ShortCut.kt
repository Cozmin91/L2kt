package com.l2kt.gameserver.model

data class L2ShortCut(val slot: Int, val page: Int, val type: Int, val id: Int, val level: Int, val characterType: Int) {
    var sharedReuseGroup = -1

    companion object {
        const val TYPE_ITEM = 1
        const val TYPE_SKILL = 2
        const val TYPE_ACTION = 3
        const val TYPE_MACRO = 4
        const val TYPE_RECIPE = 5
    }
}