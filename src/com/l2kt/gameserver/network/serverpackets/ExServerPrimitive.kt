package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.model.location.Location
import java.awt.Color
import java.util.*

/**
 * A packet used to draw points and lines on client.<br></br>
 * **Note:** Names in points and lines are bugged they will appear even when not looking at them.
 */
class ExServerPrimitive
/**
 * @param name A unique name this will be used to replace lines if second packet is sent
 * @param x the x coordinate usually middle of drawing area
 * @param y the y coordinate usually middle of drawing area
 * @param z the z coordinate usually middle of drawing area
 */
@JvmOverloads constructor(
    private val _name: String,
    private var _x: Int = 0,
    private var _y: Int = 0,
    private var _z: Int = 0
) : L2GameServerPacket() {
    private val _points = ArrayList<Point>()
    private val _lines = ArrayList<Line>()

    /**
     * @param name A unique name this will be used to replace lines if second packet is sent
     * @param location the Location to take coordinates usually middle of drawing area
     */
    constructor(name: String, location: Location) : this(name, location.x, location.y, location.z) {}

    /**
     * Set XYZ before changing packet each time. Useful for broadcasting purposes instead of building the object anew each time.
     * @param x the x coordinate usually middle of drawing area
     * @param y the y coordinate usually middle of drawing area
     * @param z the z coordinate usually middle of drawing area
     */
    fun setXYZ(x: Int, y: Int, z: Int) {
        _x = x
        _y = y
        _z = z
    }

    /**
     * Set XYZ before sending packet each time. Useful for broadcasting purposes instead of building the object anew each time.
     * @param location the Location to take coordinates usually middle of drawing area
     */
    fun setXYZ(location: Location) {
        setXYZ(location.x, location.y, location.z)
    }

    /**
     * Adds a point to be displayed on client.
     * @param name the name that will be displayed over the point
     * @param color the color
     * @param isNameColored if `true` name will be colored as well.
     * @param x the x coordinate for this point
     * @param y the y coordinate for this point
     * @param z the z coordinate for this point
     */
    fun addPoint(name: String, color: Int, isNameColored: Boolean, x: Int, y: Int, z: Int) {
        _points.add(Point(name, color, isNameColored, x, y, z))
    }

    /**
     * Adds a point to be displayed on client.
     * @param name the name that will be displayed over the point
     * @param color the color
     * @param isNameColored if `true` name will be colored as well.
     * @param location the Location to take coordinates for this point
     */
    fun addPoint(name: String, color: Int, isNameColored: Boolean, location: Location) {
        addPoint(name, color, isNameColored, location.x, location.y, location.z)
    }

    /**
     * Adds a point to be displayed on client.
     * @param color the color
     * @param x the x coordinate for this point
     * @param y the y coordinate for this point
     * @param z the z coordinate for this point
     */
    fun addPoint(color: Int, x: Int, y: Int, z: Int) {
        addPoint("", color, false, x, y, z)
    }

    /**
     * Adds a point to be displayed on client.
     * @param color the color
     * @param location the Location to take coordinates for this point
     */
    fun addPoint(color: Int, location: Location) {
        addPoint("", color, false, location)
    }

    /**
     * Adds a point to be displayed on client.
     * @param name the name that will be displayed over the point
     * @param color the color
     * @param isNameColored if `true` name will be colored as well.
     * @param x the x coordinate for this point
     * @param y the y coordinate for this point
     * @param z the z coordinate for this point
     */
    fun addPoint(name: String, color: Color, isNameColored: Boolean, x: Int, y: Int, z: Int) {
        addPoint(name, color.rgb, isNameColored, x, y, z)
    }

    /**
     * Adds a point to be displayed on client.
     * @param name the name that will be displayed over the point
     * @param color the color
     * @param isNameColored if `true` name will be colored as well.
     * @param location the Location to take coordinates for this point
     */
    fun addPoint(name: String, color: Color, isNameColored: Boolean, location: Location) {
        addPoint(name, color.rgb, isNameColored, location)
    }

    /**
     * Adds a point to be displayed on client.
     * @param color the color
     * @param x the x coordinate for this point
     * @param y the y coordinate for this point
     * @param z the z coordinate for this point
     */
    fun addPoint(color: Color, x: Int, y: Int, z: Int) {
        addPoint("", color, false, x, y, z)
    }

    /**
     * Adds a point to be displayed on client.
     * @param color the color
     * @param location the Location to take coordinates for this point
     */
    fun addPoint(color: Color, location: Location) {
        addPoint("", color, false, location)
    }

    /**
     * Adds a line to be displayed on client
     * @param name the name that will be displayed over the middle of line
     * @param color the color
     * @param isNameColored if `true` name will be colored as well.
     * @param x the x coordinate for this line start point
     * @param y the y coordinate for this line start point
     * @param z the z coordinate for this line start point
     * @param x2 the x coordinate for this line end point
     * @param y2 the y coordinate for this line end point
     * @param z2 the z coordinate for this line end point
     */
    fun addLine(name: String, color: Int, isNameColored: Boolean, x: Int, y: Int, z: Int, x2: Int, y2: Int, z2: Int) {
        _lines.add(Line(name, color, isNameColored, x, y, z, x2, y2, z2))
    }

    /**
     * Adds a line to be displayed on client
     * @param name the name that will be displayed over the middle of line
     * @param color the color
     * @param isNameColored if `true` name will be colored as well.
     * @param location the Location to take coordinates for this line start point
     * @param x2 the x coordinate for this line end point
     * @param y2 the y coordinate for this line end point
     * @param z2 the z coordinate for this line end point
     */
    fun addLine(name: String, color: Int, isNameColored: Boolean, location: Location, x2: Int, y2: Int, z2: Int) {
        addLine(name, color, isNameColored, location.x, location.y, location.z, x2, y2, z2)
    }

    /**
     * Adds a line to be displayed on client
     * @param name the name that will be displayed over the middle of line
     * @param color the color
     * @param isNameColored if `true` name will be colored as well.
     * @param x the x coordinate for this line start point
     * @param y the y coordinate for this line start point
     * @param z the z coordinate for this line start point
     * @param location the Location to take coordinates for this line end point
     */
    fun addLine(name: String, color: Int, isNameColored: Boolean, x: Int, y: Int, z: Int, location: Location) {
        addLine(name, color, isNameColored, x, y, z, location.x, location.y, location.z)
    }

    /**
     * Adds a line to be displayed on client
     * @param name the name that will be displayed over the middle of line
     * @param color the color
     * @param isNameColored if `true` name will be colored as well.
     * @param location the Location to take coordinates for this line start point
     * @param location2 the Location to take coordinates for this line end point
     */
    fun addLine(name: String, color: Int, isNameColored: Boolean, location: Location, location2: Location) {
        addLine(name, color, isNameColored, location, location2.x, location2.y, location2.z)
    }

    /**
     * Adds a line to be displayed on client
     * @param color the color
     * @param x the x coordinate for this line start point
     * @param y the y coordinate for this line start point
     * @param z the z coordinate for this line start point
     * @param x2 the x coordinate for this line end point
     * @param y2 the y coordinate for this line end point
     * @param z2 the z coordinate for this line end point
     */
    fun addLine(color: Int, x: Int, y: Int, z: Int, x2: Int, y2: Int, z2: Int) {
        addLine("", color, false, x, y, z, x2, y2, z2)
    }

    /**
     * Adds a line to be displayed on client
     * @param color the color
     * @param location the Location to take coordinates for this line start point
     * @param x2 the x coordinate for this line end point
     * @param y2 the y coordinate for this line end point
     * @param z2 the z coordinate for this line end point
     */
    fun addLine(color: Int, location: Location, x2: Int, y2: Int, z2: Int) {
        addLine("", color, false, location, x2, y2, z2)
    }

    /**
     * Adds a line to be displayed on client
     * @param color the color
     * @param x the x coordinate for this line start point
     * @param y the y coordinate for this line start point
     * @param z the z coordinate for this line start point
     * @param location the Location to take coordinates for this line end point
     */
    fun addLine(color: Int, x: Int, y: Int, z: Int, location: Location) {
        addLine("", color, false, x, y, z, location)
    }

    /**
     * Adds a line to be displayed on client
     * @param color the color
     * @param location the Location to take coordinates for this line start point
     * @param location2 the Location to take coordinates for this line end point
     */
    fun addLine(color: Int, location: Location, location2: Location) {
        addLine("", color, false, location, location2)
    }

    /**
     * Adds a line to be displayed on client
     * @param name the name that will be displayed over the middle of line
     * @param color the color
     * @param isNameColored if `true` name will be colored as well.
     * @param x the x coordinate for this line start point
     * @param y the y coordinate for this line start point
     * @param z the z coordinate for this line start point
     * @param x2 the x coordinate for this line end point
     * @param y2 the y coordinate for this line end point
     * @param z2 the z coordinate for this line end point
     */
    fun addLine(name: String, color: Color, isNameColored: Boolean, x: Int, y: Int, z: Int, x2: Int, y2: Int, z2: Int) {
        addLine(name, color.rgb, isNameColored, x, y, z, x2, y2, z2)
    }

    /**
     * Adds a line to be displayed on client
     * @param name the name that will be displayed over the middle of line
     * @param color the color
     * @param isNameColored if `true` name will be colored as well.
     * @param location the Location to take coordinates for this line start point
     * @param x2 the x coordinate for this line end point
     * @param y2 the y coordinate for this line end point
     * @param z2 the z coordinate for this line end point
     */
    fun addLine(name: String, color: Color, isNameColored: Boolean, location: Location, x2: Int, y2: Int, z2: Int) {
        addLine(name, color.rgb, isNameColored, location, x2, y2, z2)
    }

    /**
     * Adds a line to be displayed on client
     * @param name the name that will be displayed over the middle of line
     * @param color the color
     * @param isNameColored if `true` name will be colored as well.
     * @param x the x coordinate for this line start point
     * @param y the y coordinate for this line start point
     * @param z the z coordinate for this line start point
     * @param location the Location to take coordinates for this line end point
     */
    fun addLine(name: String, color: Color, isNameColored: Boolean, x: Int, y: Int, z: Int, location: Location) {
        addLine(name, color.rgb, isNameColored, x, y, z, location)
    }

    /**
     * Adds a line to be displayed on client
     * @param name the name that will be displayed over the middle of line
     * @param color the color
     * @param isNameColored if `true` name will be colored as well.
     * @param location the Location to take coordinates for this line start point
     * @param location2 the Location to take coordinates for this line end point
     */
    fun addLine(name: String, color: Color, isNameColored: Boolean, location: Location, location2: Location) {
        addLine(name, color.rgb, isNameColored, location, location2)
    }

    /**
     * Adds a line to be displayed on client
     * @param color the color
     * @param x the x coordinate for this line start point
     * @param y the y coordinate for this line start point
     * @param z the z coordinate for this line start point
     * @param x2 the x coordinate for this line end point
     * @param y2 the y coordinate for this line end point
     * @param z2 the z coordinate for this line end point
     */
    fun addLine(color: Color, x: Int, y: Int, z: Int, x2: Int, y2: Int, z2: Int) {
        addLine("", color, false, x, y, z, x2, y2, z2)
    }

    /**
     * Adds a line to be displayed on client
     * @param color the color
     * @param location the Location to take coordinates for this line start point
     * @param x2 the x coordinate for this line end point
     * @param y2 the y coordinate for this line end point
     * @param z2 the z coordinate for this line end point
     */
    fun addLine(color: Color, location: Location, x2: Int, y2: Int, z2: Int) {
        addLine("", color, false, location, x2, y2, z2)
    }

    /**
     * Adds a line to be displayed on client
     * @param color the color
     * @param x the x coordinate for this line start point
     * @param y the y coordinate for this line start point
     * @param z the z coordinate for this line start point
     * @param location the Location to take coordinates for this line end point
     */
    fun addLine(color: Color, x: Int, y: Int, z: Int, location: Location) {
        addLine("", color, false, x, y, z, location)
    }

    /**
     * Adds a line to be displayed on client
     * @param color the color
     * @param location the ILocational to take coordinates for this line start point
     * @param location2 the ILocational to take coordinates for this line end point
     */
    fun addLine(color: Color, location: Location, location2: Location) {
        addLine("", color, false, location, location2)
    }

    override fun writeImpl() {
        writeC(0xFE)
        writeH(0x24)
        writeS(_name)
        writeD(_x)
        writeD(_y)
        writeD(_z)
        writeD(Integer.MAX_VALUE) // has to do something with display range and angle
        writeD(Integer.MAX_VALUE) // has to do something with display range and angle

        writeD(_points.size + _lines.size)

        for (point in _points) {
            writeC(1) // Its the type in this case Point
            writeS(point.name)
            val color = point.color
            writeD(color shr 16 and 0xFF) // R
            writeD(color shr 8 and 0xFF) // G
            writeD(color and 0xFF) // B
            writeD(if (point.isNameColored) 1 else 0)
            writeD(point.x)
            writeD(point.y)
            writeD(point.z)
        }

        for (line in _lines) {
            writeC(2) // Its the type in this case Line
            writeS(line.name)
            val color = line.color
            writeD(color shr 16 and 0xFF) // R
            writeD(color shr 8 and 0xFF) // G
            writeD(color and 0xFF) // B
            writeD(if (line.isNameColored) 1 else 0)
            writeD(line.x)
            writeD(line.y)
            writeD(line.z)
            writeD(line.x2)
            writeD(line.y2)
            writeD(line.z2)
        }
    }

    private open class Point(
        /**
         * @return the name
         */
        val name: String,
        /**
         * @return the color
         */
        val color: Int,
        /**
         * @return the isNameColored
         */
        val isNameColored: Boolean,
        /**
         * @return the x
         */
        val x: Int,
        /**
         * @return the y
         */
        val y: Int,
        /**
         * @return the z
         */
        val z: Int
    )

    private class Line(
        name: String, color: Int, isNameColored: Boolean, x: Int, y: Int, z: Int,
        /**
         * @return the x2
         */
        val x2: Int,
        /**
         * @return the y2
         */
        val y2: Int,
        /**
         * @return the z2
         */
        val z2: Int
    ) : Point(name, color, isNameColored, x, y, z)
}
/**
 * @param name A unique name this will be used to replace lines if second packet is sent
 */