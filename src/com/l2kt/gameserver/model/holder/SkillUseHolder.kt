package com.l2kt.gameserver.model.holder

import com.l2kt.gameserver.model.L2Skill

/**
 * Skill casting information (used to queue when several skills are cast in a short time)
 */
class SkillUseHolder {
    var skill: L2Skill? = null
    var isCtrlPressed: Boolean = false
    var isShiftPressed: Boolean = false

    val skillId: Int
        get() = if (skill != null) skill!!.id else -1

    constructor()

    constructor(skill: L2Skill, ctrlPressed: Boolean, shiftPressed: Boolean) {
        this.skill = skill
        isCtrlPressed = ctrlPressed
        isShiftPressed = shiftPressed
    }
}