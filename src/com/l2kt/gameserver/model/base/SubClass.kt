package com.l2kt.gameserver.model.base

/**
 * Character Sub-Class Definition <BR></BR>
 * Used to store key information about a character's sub-class.
 * @author Tempy
 */
class SubClass {
    var classDefinition: ClassId
        private set
    val classIndex: Int
    private var _exp: Long = 0
    var sp: Int = 0
    private var _level: Byte = 0

    var classId: Int
        get() = classDefinition.id
        set(classId) {
            classDefinition = ClassId.VALUES[classId]
        }

    var exp: Long
        get() = _exp
        set(exp) {
            var newExp = exp
            if (newExp > Experience.LEVEL[Experience.MAX_LEVEL.toInt()])
                newExp = Experience.LEVEL[Experience.MAX_LEVEL.toInt()]

            _exp = newExp
        }

    var level: Byte
        get() = _level
        set(level) {
            var newLvl = level
            if (newLvl > Experience.MAX_LEVEL - 1)
                newLvl = (Experience.MAX_LEVEL - 1).toByte()
            else if (newLvl < 40)
                newLvl = 40

            _level = newLvl
        }

    /**
     * Implicit constructor with all parameters to be set.
     * @param classId : Class ID of the subclass.
     * @param classIndex : Class index of the subclass.
     * @param exp : Exp of the subclass.
     * @param sp : Sp of the subclass.
     * @param level : Level of the subclass.
     */
    constructor(classId: Int, classIndex: Int, exp: Long, sp: Int, level: Byte) {
        classDefinition = ClassId.VALUES[classId]
        this.classIndex = classIndex
        _exp = exp
        this.sp = sp
        _level = level
    }

    /**
     * Implicit constructor with default EXP, SP and level parameters.
     * @param classId : Class ID of the subclass.
     * @param classIndex : Class index of the subclass.
     */
    constructor(classId: Int, classIndex: Int) {
        classDefinition = ClassId.VALUES[classId]
        this.classIndex = classIndex
        _exp = Experience.LEVEL[40]
        sp = 0
        _level = 40
    }
}