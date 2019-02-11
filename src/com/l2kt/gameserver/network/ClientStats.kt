package com.l2kt.gameserver.network

import com.l2kt.Config

class ClientStats {
    var processedPackets = 0
    var droppedPackets = 0
    var unknownPackets = 0
    var totalQueueSize = 0
    var maxQueueSize = 0
    var totalBursts = 0
    var maxBurstSize = 0
    var shortFloods = 0
    var longFloods = 0
    var totalQueueOverflows = 0
    var totalUnderflowExceptions = 0

    private val _packetsInSecond: IntArray
    private var _packetCountStartTick: Long = 0
    private var _head: Int = 0
    private var _totalCount = 0

    private var _floodsInMin = 0
    private var _floodStartTick: Long = 0
    private var _unknownPacketsInMin = 0
    private var _unknownPacketStartTick: Long = 0
    private var _overflowsInMin = 0
    private var _overflowStartTick: Long = 0
    private var _underflowReadsInMin = 0
    private var _underflowReadStartTick: Long = 0

    @Volatile
    private var _floodDetected = false
    @Volatile
    private var _queueOverflowDetected = false

    private val BUFFER_SIZE: Int = Config.CLIENT_PACKET_QUEUE_MEASURE_INTERVAL

    init {
        _packetsInSecond = IntArray(BUFFER_SIZE)
        _head = BUFFER_SIZE - 1
    }

    /**
     * @return true if incoming packet need to be dropped.
     */
    fun dropPacket(): Boolean {
        val result = _floodDetected || _queueOverflowDetected
        if (result)
            droppedPackets++
        return result
    }

    /**
     * @param queueSize
     * @return true if flood detected first and ActionFailed packet need to be sent. Later during flood returns true (and send ActionFailed) once per second.
     */
    fun countPacket(queueSize: Int): Boolean {
        processedPackets++
        totalQueueSize += queueSize
        if (maxQueueSize < queueSize)
            maxQueueSize = queueSize
        if (_queueOverflowDetected && queueSize < 2)
            _queueOverflowDetected = false

        return countPacket()
    }

    /**
     * Counts unknown packets.
     * @return true if threshold is reached.
     */
    fun countUnknownPacket(): Boolean {
        unknownPackets++

        val tick = System.currentTimeMillis()
        if (tick - _unknownPacketStartTick > 60000) {
            _unknownPacketStartTick = tick
            _unknownPacketsInMin = 1
            return false
        }

        _unknownPacketsInMin++
        return _unknownPacketsInMin > Config.CLIENT_PACKET_QUEUE_MAX_UNKNOWN_PER_MIN
    }

    /**
     * Counts burst length.
     * @param count - current number of processed packets in burst
     * @return true if execution of the queue need to be aborted.
     */
    fun countBurst(count: Int): Boolean {
        if (count > maxBurstSize)
            maxBurstSize = count

        if (count < Config.CLIENT_PACKET_QUEUE_MAX_BURST_SIZE)
            return false

        totalBursts++
        return true
    }

    /**
     * Counts queue overflows.
     * @return true if threshold is reached.
     */
    fun countQueueOverflow(): Boolean {
        _queueOverflowDetected = true
        totalQueueOverflows++

        val tick = System.currentTimeMillis()
        if (tick - _overflowStartTick > 60000) {
            _overflowStartTick = tick
            _overflowsInMin = 1
            return false
        }

        _overflowsInMin++
        return _overflowsInMin > Config.CLIENT_PACKET_QUEUE_MAX_OVERFLOWS_PER_MIN
    }

    /**
     * Counts underflow exceptions.
     * @return true if threshold is reached.
     */
    fun countUnderflowException(): Boolean {
        totalUnderflowExceptions++

        val tick = System.currentTimeMillis()
        if (tick - _underflowReadStartTick > 60000) {
            _underflowReadStartTick = tick
            _underflowReadsInMin = 1
            return false
        }

        _underflowReadsInMin++
        return _underflowReadsInMin > Config.CLIENT_PACKET_QUEUE_MAX_UNDERFLOWS_PER_MIN
    }

    /**
     * @return true if maximum number of floods per minute is reached.
     */
    fun countFloods(): Boolean {
        return _floodsInMin > Config.CLIENT_PACKET_QUEUE_MAX_FLOODS_PER_MIN
    }

    private fun longFloodDetected(): Boolean {
        return _totalCount / BUFFER_SIZE > Config.CLIENT_PACKET_QUEUE_MAX_AVERAGE_PACKETS_PER_SECOND
    }

    /**
     * @return true if flood detected first and ActionFailed packet need to be sent. Later during flood returns true (and send ActionFailed) once per second.
     */
    @Synchronized
    private fun countPacket(): Boolean {
        _totalCount++
        val tick = System.currentTimeMillis()
        if (tick - _packetCountStartTick > 1000) {
            _packetCountStartTick = tick

            // clear flag if no more flooding during last seconds
            if (_floodDetected && !longFloodDetected() && _packetsInSecond[_head] < Config.CLIENT_PACKET_QUEUE_MAX_PACKETS_PER_SECOND / 2)
                _floodDetected = false

            // wrap head of the buffer around the tail
            if (_head <= 0)
                _head = BUFFER_SIZE
            _head--

            _totalCount -= _packetsInSecond[_head]
            _packetsInSecond[_head] = 1
            return _floodDetected
        }

        val count = ++_packetsInSecond[_head]
        if (!_floodDetected) {
            if (count > Config.CLIENT_PACKET_QUEUE_MAX_PACKETS_PER_SECOND)
                shortFloods++
            else if (longFloodDetected())
                longFloods++
            else
                return false

            _floodDetected = true
            if (tick - _floodStartTick > 60000) {
                _floodStartTick = tick
                _floodsInMin = 1
            } else
                _floodsInMin++

            return true // Return true only in the beginning of the flood
        }

        return false
    }
}