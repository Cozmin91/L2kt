package com.l2kt.gameserver.data.manager

import com.l2kt.commons.concurrent.ThreadPool
import com.l2kt.commons.lang.StringUtil
import com.l2kt.gameserver.data.manager.MovieMakerManager.Sequence
import com.l2kt.gameserver.model.actor.instance.Player
import com.l2kt.gameserver.network.serverpackets.NpcHtmlMessage
import com.l2kt.gameserver.network.serverpackets.SpecialCamera
import java.util.*

/**
 * Store a serie of [Sequence]s, which set in a particular order form a cinematic using [SpecialCamera] packet.<br></br>
 * <br></br>
 * This system is used to ease the craft of animations by admins.
 */
object MovieMakerManager {
    private val _sequences: MutableMap<Int, Sequence> = HashMap()

    fun mainHtm(player: Player) {
        val html = NpcHtmlMessage(0)

        if (_sequences.isEmpty())
            html.setFile("data/html/admin/movie/main_empty.htm")
        else {
            val sb = StringBuilder()
            for (sequence in _sequences.values)
                StringUtil.append(
                    sb,
                    "<tr><td>",
                    sequence._sequenceId,
                    ": (",
                    sequence._dist,
                    ", ",
                    sequence._yaw,
                    ", ",
                    sequence._pitch,
                    ", ",
                    sequence._time,
                    ", ",
                    sequence._duration,
                    ", ",
                    sequence._turn,
                    ", ",
                    sequence._rise,
                    ", ",
                    sequence._widescreen,
                    ")</td></tr>"
                )

            html.setFile("data/html/admin/movie/main_notempty.htm")
            html.replace("%sequences%", sb.toString())
        }
        player.sendPacket(html)
    }

    fun playSequence(id: Int, player: Player) {
        val sequence = _sequences[id]
        if (sequence == null) {
            player.sendMessage("Wrong sequence id.")
            mainHtm(player)
            return
        }

        player.sendPacket(
            SpecialCamera(
                sequence._objid,
                sequence._dist,
                sequence._yaw,
                sequence._pitch,
                sequence._time,
                sequence._duration,
                sequence._turn,
                sequence._rise,
                sequence._widescreen,
                0
            )
        )
    }

    fun broadcastSequence(id: Int, player: Player) {
        val sequence = _sequences[id]
        if (sequence == null) {
            player.sendMessage("Wrong sequence id.")
            mainHtm(player)
            return
        }

        player.broadcastPacket(
            SpecialCamera(
                sequence._objid,
                sequence._dist,
                sequence._yaw,
                sequence._pitch,
                sequence._time,
                sequence._duration,
                sequence._turn,
                sequence._rise,
                sequence._widescreen,
                0
            )
        )
    }

    fun playSequence(
        player: Player,
        objid: Int,
        dist: Int,
        yaw: Int,
        pitch: Int,
        time: Int,
        duration: Int,
        turn: Int,
        rise: Int,
        screen: Int
    ) {
        player.sendPacket(SpecialCamera(objid, dist, yaw, pitch, time, duration, turn, rise, screen, 0))
    }

    fun addSequence(
        player: Player,
        seqId: Int,
        objid: Int,
        dist: Int,
        yaw: Int,
        pitch: Int,
        time: Int,
        duration: Int,
        turn: Int,
        rise: Int,
        screen: Int
    ) {
        if (_sequences.containsKey(seqId))
            player.sendMessage("This sequence already exists.")
        else {
            val sequence = Sequence()
            sequence._sequenceId = seqId
            sequence._objid = objid
            sequence._dist = dist
            sequence._yaw = yaw
            sequence._pitch = pitch
            sequence._time = time
            sequence._duration = duration
            sequence._turn = turn
            sequence._rise = rise
            sequence._widescreen = screen

            _sequences[seqId] = sequence

        }
        mainHtm(player)
    }

    fun addSequence(player: Player) {
        val html = NpcHtmlMessage(0)
        html.setFile("data/html/admin/movie/add_sequence.htm")
        player.sendPacket(html)
    }

    fun editSequence(id: Int, player: Player) {
        val sequence = _sequences[id]
        if (sequence == null) {
            player.sendMessage("The sequence couldn't be updated.")
            mainHtm(player)
            return
        }

        val html = NpcHtmlMessage(0)
        html.setFile("data/html/admin/movie/edit_sequence.htm")
        html.replace("%sId%", sequence._sequenceId)
        html.replace("%sDist%", sequence._dist)
        html.replace("%sYaw%", sequence._yaw)
        html.replace("%sPitch%", sequence._pitch)
        html.replace("%sTime%", sequence._time)
        html.replace("%sDuration%", sequence._duration)
        html.replace("%sTurn%", sequence._turn)
        html.replace("%sRise%", sequence._rise)
        html.replace("%sWidescreen%", sequence._widescreen)
        player.sendPacket(html)
    }

    fun updateSequence(
        player: Player,
        seqId: Int,
        objid: Int,
        dist: Int,
        yaw: Int,
        pitch: Int,
        time: Int,
        duration: Int,
        turn: Int,
        rise: Int,
        screen: Int
    ) {
        val sequence = _sequences[seqId]
        if (sequence == null)
            player.sendMessage("This sequence doesn't exist.")
        else {
            sequence._objid = objid
            sequence._dist = dist
            sequence._yaw = yaw
            sequence._pitch = pitch
            sequence._time = time
            sequence._duration = duration
            sequence._turn = turn
            sequence._rise = rise
            sequence._widescreen = screen
        }

        mainHtm(player)
    }

    fun deleteSequence(id: Int, player: Player) {
        if (_sequences.remove(id) == null)
            player.sendMessage("This sequence id doesn't exist.")

        mainHtm(player)
    }

    fun playMovie(broadcast: Int, player: Player) {
        if (_sequences.isEmpty()) {
            player.sendMessage("There is nothing to play.")
            mainHtm(player)
            return
        }

        ThreadPool.schedule(Play(1, broadcast, player), 500)
    }

    private class Play(private val _id: Int, private val _broad: Int, private val _player: Player) : Runnable {

        override fun run() {
            val sequence = _sequences[_id]
            if (sequence == null) {
                _player.sendMessage("Movie ended on sequence: " + (_id - 1) + ".")
                mainHtm(_player)
                return
            }

            if (_broad == 1)
                _player.broadcastPacket(
                    SpecialCamera(
                        sequence._objid,
                        sequence._dist,
                        sequence._yaw,
                        sequence._pitch,
                        sequence._time,
                        sequence._duration,
                        sequence._turn,
                        sequence._rise,
                        sequence._widescreen,
                        0
                    )
                )
            else
                _player.sendPacket(
                    SpecialCamera(
                        sequence._objid,
                        sequence._dist,
                        sequence._yaw,
                        sequence._pitch,
                        sequence._time,
                        sequence._duration,
                        sequence._turn,
                        sequence._rise,
                        sequence._widescreen,
                        0
                    )
                )

            ThreadPool.schedule(Play(_id + 1, _broad, _player), (sequence._duration - 100).toLong())
        }
    }

    private class Sequence {
        var _sequenceId: Int = 0
        var _objid: Int = 0
        var _dist: Int = 0
        var _yaw: Int = 0
        var _pitch: Int = 0
        var _time: Int = 0
        var _duration: Int = 0
        var _turn: Int = 0
        var _rise: Int = 0
        var _widescreen: Int = 0
    }
}