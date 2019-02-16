package com.l2kt.gameserver.geoengine.geodata

import com.l2kt.gameserver.model.World

object GeoStructure {
    // cells
    const val CELL_FLAG_E = (1 shl 0).toByte()
    const val CELL_FLAG_W = (1 shl 1).toByte()
    const val CELL_FLAG_S = (1 shl 2).toByte()
    const val CELL_FLAG_N = (1 shl 3).toByte()
    const val CELL_FLAG_SE = (1 shl 4).toByte()
    const val CELL_FLAG_SW = (1 shl 5).toByte()
    const val CELL_FLAG_NE = (1 shl 6).toByte()
    const val CELL_FLAG_NW = (1 shl 7).toByte()
    val CELL_FLAG_S_AND_E = (CELL_FLAG_S.toInt() or CELL_FLAG_E.toInt()).toByte()
    val CELL_FLAG_S_AND_W = (CELL_FLAG_S.toInt() or CELL_FLAG_W.toInt()).toByte()
    val CELL_FLAG_N_AND_E = (CELL_FLAG_N.toInt() or CELL_FLAG_E.toInt()).toByte()
    val CELL_FLAG_N_AND_W = (CELL_FLAG_N.toInt() or CELL_FLAG_W.toInt()).toByte()

    const val CELL_SIZE = 16
    const val CELL_HEIGHT = 8
    val CELL_IGNORE_HEIGHT = CELL_HEIGHT * 6

    // blocks
    const val TYPE_FLAT_L2J_L2OFF: Byte = 0
    const val TYPE_FLAT_L2D = 0xD0.toByte()
    const val TYPE_COMPLEX_L2J: Byte = 1
    const val TYPE_COMPLEX_L2OFF: Byte = 0x40
    const val TYPE_COMPLEX_L2D = 0xD1.toByte()
    const val TYPE_MULTILAYER_L2J: Byte = 2
    // public static final byte TYPE_MULTILAYER_L2OFF = 0x41; // officially not does exist, is anything above complex block (0x41 - 0xFFFF)
    const val TYPE_MULTILAYER_L2D = 0xD2.toByte()

    const val BLOCK_CELLS_X = 8
    const val BLOCK_CELLS_Y = 8
    val BLOCK_CELLS = BLOCK_CELLS_X * BLOCK_CELLS_Y

    // regions
    const val REGION_BLOCKS_X = 256
    const val REGION_BLOCKS_Y = 256
    val REGION_BLOCKS = REGION_BLOCKS_X * REGION_BLOCKS_Y

    val REGION_CELLS_X = REGION_BLOCKS_X * BLOCK_CELLS_X
    val REGION_CELLS_Y = REGION_BLOCKS_Y * BLOCK_CELLS_Y

    // global geodata
    const val GEO_REGIONS_X = World.TILE_X_MAX - World.TILE_X_MIN + 1
    const val GEO_REGIONS_Y = World.TILE_Y_MAX - World.TILE_Y_MIN + 1

    val GEO_BLOCKS_X = GEO_REGIONS_X * REGION_BLOCKS_X
    val GEO_BLOCKS_Y = GEO_REGIONS_Y * REGION_BLOCKS_Y

    val GEO_CELLS_X = GEO_BLOCKS_X * BLOCK_CELLS_X
    val GEO_CELLS_Y = GEO_BLOCKS_Y * BLOCK_CELLS_Y
}