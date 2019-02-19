package com.l2kt.gameserver.model.actor.appearance

import com.l2kt.gameserver.model.base.Sex

class PcAppearance(face: Byte, hColor: Byte, hStyle: Byte, var sex: Sex) {
    var face: Byte = face
        private set
    var hairColor: Byte = hColor
        private set
    var hairStyle: Byte = hStyle
        private set
    var invisible = false
        private set
    var nameColor = 0xFFFFFF
    var titleColor = 0xFFFF77

    fun setFace(value: Int) {
        face = value.toByte()
    }

    fun setHairColor(value: Int) {
        hairColor = value.toByte()
    }

    fun setHairStyle(value: Int) {
        hairStyle = value.toByte()
    }

    fun setInvisible() {
        invisible = true
    }

    fun setVisible() {
        invisible = false
    }

    fun setNameColor(red: Int, green: Int, blue: Int) {
        nameColor = (red and 0xFF) + (green and 0xFF shl 8) + (blue and 0xFF shl 16)
    }

    fun setTitleColor(red: Int, green: Int, blue: Int) {
        titleColor = (red and 0xFF) + (green and 0xFF shl 8) + (blue and 0xFF shl 16)
    }
}