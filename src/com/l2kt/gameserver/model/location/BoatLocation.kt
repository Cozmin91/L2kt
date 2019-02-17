package com.l2kt.gameserver.model.location

/**
 * A datatype extending [Location] used for boats. It notably holds move speed and rotation speed.
 */
class BoatLocation : Location {
    var moveSpeed: Int = 0
        private set
    var rotationSpeed: Int = 0
        private set

    constructor(x: Int, y: Int, z: Int) : super(x, y, z) {

        moveSpeed = 350
        rotationSpeed = 4000
    }

    constructor(x: Int, y: Int, z: Int, moveSpeed: Int, rotationSpeed: Int) : super(x, y, z) {

        this.moveSpeed = moveSpeed
        this.rotationSpeed = rotationSpeed
    }
}