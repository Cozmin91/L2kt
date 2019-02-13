package com.l2kt.gameserver.model

data class L2Macro(
    var id: Int,
    val icon: Int,
    val name: String,
    val descr: String,
    val acronym: String,
    val commands: List<L2MacroCmd>
) {

    data class L2MacroCmd(
        val entry: Int, val type: Int, val d1: Int // skill_id or page for shortcuts
        , val d2: Int // shortcut
        , val cmd: String
    )

    companion object {
        const val CMD_TYPE_SKILL = 1
        const val CMD_TYPE_ACTION = 3
        const val CMD_TYPE_SHORTCUT = 4
    }
}