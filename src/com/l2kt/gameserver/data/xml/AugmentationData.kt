package com.l2kt.gameserver.data.xml

import com.l2kt.Config
import com.l2kt.commons.data.xml.IXmlReader
import com.l2kt.commons.random.Rnd
import com.l2kt.gameserver.data.xml.AugmentationData.AugmentationStat
import com.l2kt.gameserver.data.xml.AugmentationData.forEach
import com.l2kt.gameserver.model.L2Augmentation
import com.l2kt.gameserver.model.L2Skill
import com.l2kt.gameserver.model.holder.IntIntHolder
import com.l2kt.gameserver.network.clientpackets.AbstractRefinePacket
import com.l2kt.gameserver.skills.Stats
import org.w3c.dom.Document
import java.nio.file.Path
import java.util.*

/**
 * This class loads and stores :
 *
 *  * [AugmentationStat] under 4 different tables of stats (pDef, etc)
 *  * Augmentation skills based on colors (blue, purple and red)
 *
 * It is also used to generate new [L2Augmentation], based on stored content.
 */
object AugmentationData : IXmlReader {

    private val _augStats = ArrayList<MutableList<AugmentationStat>>(4)

    private val _blueSkills = ArrayList<MutableList<Int>>(10)
    private val _purpleSkills = ArrayList<MutableList<Int>>(10)
    private val _redSkills = ArrayList<MutableList<Int>>(10)

    private val _allSkills = HashMap<Int, IntIntHolder>()

    // stats
    private const val STAT_START = 1
    private const val STAT_END = 14560
    private const val STAT_BLOCKSIZE = 3640
    private const val STAT_SUBBLOCKSIZE = 91
    private const val STAT_NUM = 13

    private val STATS1_MAP = ByteArray(STAT_SUBBLOCKSIZE)
    private val STATS2_MAP = ByteArray(STAT_SUBBLOCKSIZE)

    // skills
    private const val BLUE_START = 14561
    private const val SKILLS_BLOCKSIZE = 178

    // basestats
    private const val BASESTAT_STR = 16341
    private const val BASESTAT_CON = 16342
    private const val BASESTAT_INT = 16343
    private const val BASESTAT_MEN = 16344

    init {
        // Lookup tables structure: STAT1 represent first stat, STAT2 - second.
        // If both values are the same - use solo stat, if different - combined.
        var idx: Byte = 0

        // weapon augmentation block: solo values first
        while (idx < STAT_NUM) {
            // solo stats
            STATS1_MAP[idx.toInt()] = idx
            STATS2_MAP[idx.toInt()] = idx
            idx++
        }

        // combined values next.
        for (i in 0 until STAT_NUM) {
            var j = i + 1
            while (j < STAT_NUM) {
                // combined stats
                STATS1_MAP[idx.toInt()] = i.toByte()
                STATS2_MAP[idx.toInt()] = j.toByte()
                idx++
                j++
            }
        }

        for (i in 0..3)
            _augStats.add(ArrayList())

        for (i in 0..9) {
            _blueSkills.add(ArrayList())
            _purpleSkills.add(ArrayList())
            _redSkills.add(ArrayList())
        }

        load()
    }

    override fun load() {
        parseFile("./data/xml/augmentation")
        IXmlReader.LOGGER.info("Loaded {} sets of augmentation stats.", _augStats.size)

        val blue = _blueSkills.stream().mapToInt { it.size }.sum()
        val purple = _purpleSkills.stream().mapToInt { it.size }.sum()
        val red = _redSkills.stream().mapToInt { it.size }.sum()
        IXmlReader.LOGGER.info("Loaded {} blue, {} purple and {} red Life-Stone skills.", blue, purple, red)
    }

    override fun parseDocument(doc: Document, path: Path) {
        forEach(doc, "list") { listNode ->
            forEach(listNode, "augmentation") { augmentationNode ->
                val set = parseAttributes(augmentationNode)
                val augmentationId = set.getInteger("id")
                val k = (augmentationId - BLUE_START) / SKILLS_BLOCKSIZE

                when (set.getString("type")) {
                    "blue" -> _blueSkills[k].add(augmentationId)

                    "purple" -> _purpleSkills[k].add(augmentationId)

                    "red" -> _redSkills[k].add(augmentationId)
                }
                _allSkills[augmentationId] = IntIntHolder(set.getInteger("skillId"), set.getInteger("skillLevel"))
            }
            forEach(listNode, "set") { setNode ->
                val order = parseInteger(setNode.attributes, "order")!!
                val statList = _augStats[order]
                forEach(setNode, "stat") { statNode ->
                    val statName = parseString(statNode.attributes, "name")
                    val soloValues = ArrayList<Float>()
                    val combinedValues = ArrayList<Float>()
                    forEach(statNode, "table") { tableNode ->
                        val tableName = parseString(tableNode.attributes, "name")
                        val data = StringTokenizer(tableNode.firstChild.nodeValue)
                        if ("#soloValues".equals(tableName, ignoreCase = true))
                            while (data.hasMoreTokens())
                                soloValues.add(java.lang.Float.parseFloat(data.nextToken()))
                        else
                            while (data.hasMoreTokens())
                                combinedValues.add(java.lang.Float.parseFloat(data.nextToken()))
                    }
                    val soloValuesArr = FloatArray(soloValues.size)
                    for (i in soloValuesArr.indices)
                        soloValuesArr[i] = soloValues[i]
                    val combinedValuesArr = FloatArray(combinedValues.size)
                    for (i in combinedValuesArr.indices)
                        combinedValuesArr[i] = combinedValues[i]
                    statList.add(AugmentationStat(Stats.valueOfXml(statName), soloValuesArr, combinedValuesArr))
                }
            }
        }
    }

    fun generateRandomAugmentation(lifeStoneLevel: Int, lifeStoneGrade: Int): L2Augmentation {
        var lifeStoneLevel = lifeStoneLevel
        // Note that stat12 stands for stat 1 AND 2 (same for stat34 ;p )
        // this is because a value can contain up to 2 stat modifications
        // (there are two short values packed in one integer value, meaning 4 stat modifications at max)
        // for more info take a look at getAugStatsById(...)

        // Note: lifeStoneGrade: (0 means low grade, 3 top grade)
        // First: determine whether we will add a skill/baseStatModifier or not
        // because this determine which color could be the result
        var stat12 = 0
        var stat34 = 0
        var generateSkill = false
        var generateGlow = false

        // lifestonelevel is used for stat Id and skill level, but here the max level is 9
        lifeStoneLevel = Math.min(lifeStoneLevel, 9)

        when (lifeStoneGrade) {
            AbstractRefinePacket.GRADE_NONE -> {
                if (Rnd[1, 100] <= Config.AUGMENTATION_NG_SKILL_CHANCE)
                    generateSkill = true
                if (Rnd[1, 100] <= Config.AUGMENTATION_NG_GLOW_CHANCE)
                    generateGlow = true
            }
            AbstractRefinePacket.GRADE_MID -> {
                if (Rnd[1, 100] <= Config.AUGMENTATION_MID_SKILL_CHANCE)
                    generateSkill = true
                if (Rnd[1, 100] <= Config.AUGMENTATION_MID_GLOW_CHANCE)
                    generateGlow = true
            }
            AbstractRefinePacket.GRADE_HIGH -> {
                if (Rnd[1, 100] <= Config.AUGMENTATION_HIGH_SKILL_CHANCE)
                    generateSkill = true
                if (Rnd[1, 100] <= Config.AUGMENTATION_HIGH_GLOW_CHANCE)
                    generateGlow = true
            }
            AbstractRefinePacket.GRADE_TOP -> {
                if (Rnd[1, 100] <= Config.AUGMENTATION_TOP_SKILL_CHANCE)
                    generateSkill = true
                if (Rnd[1, 100] <= Config.AUGMENTATION_TOP_GLOW_CHANCE)
                    generateGlow = true
            }
        }

        if (!generateSkill && Rnd[1, 100] <= Config.AUGMENTATION_BASESTAT_CHANCE)
            stat34 = Rnd[BASESTAT_STR, BASESTAT_MEN]

        // Second: decide which grade the augmentation result is going to have:
        // 0:yellow, 1:blue, 2:purple, 3:red
        // The chances used here are most likely custom,
        // whats known is: you cant have yellow with skill(or baseStatModifier)
        // noGrade stone can not have glow, mid only with skill, high has a chance(custom), top allways glow
        var resultColor = Rnd[0, 100]
        if (stat34 == 0 && !generateSkill) {
            if (resultColor <= 15 * lifeStoneGrade + 40)
                resultColor = 1
            else
                resultColor = 0
        } else {
            if (resultColor <= 10 * lifeStoneGrade + 5 || stat34 != 0)
                resultColor = 3
            else if (resultColor <= 10 * lifeStoneGrade + 10)
                resultColor = 1
            else
                resultColor = 2
        }

        // generate a skill if neccessary
        var skill: L2Skill? = null
        if (generateSkill) {
            when (resultColor) {
                1 // blue skill
                -> stat34 = _blueSkills[lifeStoneLevel][Rnd[0, _blueSkills[lifeStoneLevel].size - 1]]
                2 // purple skill
                -> stat34 = _purpleSkills[lifeStoneLevel][Rnd[0, _purpleSkills[lifeStoneLevel].size - 1]]
                3 // red skill
                -> stat34 = _redSkills[lifeStoneLevel][Rnd[0, _redSkills[lifeStoneLevel].size - 1]]
            }
            skill = _allSkills[stat34]?.skill
        }

        // Third: Calculate the subblock offset for the choosen color,
        // and the level of the lifeStone
        // from large number of retail augmentations:
        // no skill part
        // Id for stat12:
        // A:1-910 B:911-1820 C:1821-2730 D:2731-3640 E:3641-4550 F:4551-5460 G:5461-6370 H:6371-7280
        // Id for stat34(this defines the color):
        // I:7281-8190(yellow) K:8191-9100(blue) L:10921-11830(yellow) M:11831-12740(blue)
        // you can combine I-K with A-D and L-M with E-H
        // using C-D or G-H Id you will get a glow effect
        // there seems no correlation in which grade use which Id except for the glowing restriction
        // skill part
        // Id for stat12:
        // same for no skill part
        // A same as E, B same as F, C same as G, D same as H
        // A - no glow, no grade LS
        // B - weak glow, mid grade LS?
        // C - glow, high grade LS?
        // D - strong glow, top grade LS?

        // is neither a skill nor basestat used for stat34? then generate a normal stat
        var offset: Int
        if (stat34 == 0) {
            val temp = Rnd[2, 3]
            val colorOffset = resultColor * (10 * STAT_SUBBLOCKSIZE) + temp * STAT_BLOCKSIZE + 1
            offset = lifeStoneLevel * STAT_SUBBLOCKSIZE + colorOffset

            stat34 = Rnd[offset, offset + STAT_SUBBLOCKSIZE - 1]
            if (generateGlow && lifeStoneGrade >= 2)
                offset = lifeStoneLevel * STAT_SUBBLOCKSIZE + (temp - 2) * STAT_BLOCKSIZE + lifeStoneGrade *
                        (10 * STAT_SUBBLOCKSIZE) + 1
            else
                offset = lifeStoneLevel * STAT_SUBBLOCKSIZE + (temp - 2) * STAT_BLOCKSIZE + Rnd[0, 1] *
                        (10 * STAT_SUBBLOCKSIZE) + 1
        } else {
            offset = if (!generateGlow)
                lifeStoneLevel * STAT_SUBBLOCKSIZE + Rnd[0, 1] * STAT_BLOCKSIZE + 1
            else
                lifeStoneLevel * STAT_SUBBLOCKSIZE + Rnd[0, 1] *
                        STAT_BLOCKSIZE + (lifeStoneGrade + resultColor) / 2 * (10 * STAT_SUBBLOCKSIZE) + 1
        }
        stat12 = Rnd[offset, offset + STAT_SUBBLOCKSIZE - 1]

        return L2Augmentation((stat34 shl 16) + stat12, skill)
    }

    /**
     * Returns the stat and basestat boni for a given augmentation id
     * @param augmentationId
     * @return
     */
    fun getAugStatsById(augmentationId: Int): List<AugStat> {
        val temp = ArrayList<AugStat>()
        // An augmentation id contains 2 short vaues so we gotta seperate them here
        // both values contain a number from 1-16380, the first 14560 values are stats
        // the 14560 stats are divided into 4 blocks each holding 3640 values
        // each block contains 40 subblocks holding 91 stat values
        // the first 13 values are so called Solo-stats and they have the highest stat increase possible
        // after the 13 Solo-stats come 78 combined stats (thats every possible combination of the 13 solo stats)
        // the first 12 combined stats (14-26) is the stat 1 combined with stat 2-13
        // the next 11 combined stats then are stat 2 combined with stat 3-13 and so on...
        // to get the idea have a look @ optiondata_client-e.dat - thats where the data came from :)
        val stats = IntArray(2)
        stats[0] = 0x0000FFFF and augmentationId
        stats[1] = augmentationId shr 16

        for (i in 0..1) {
            // weapon augmentation - stats
            if (stats[i] >= STAT_START && stats[i] <= STAT_END) {
                val base = stats[i] - STAT_START
                val color = base / STAT_BLOCKSIZE // 4 color blocks
                val subblock = base % STAT_BLOCKSIZE // offset in color block
                val level = subblock / STAT_SUBBLOCKSIZE // stat level (sub-block number)
                val stat = subblock % STAT_SUBBLOCKSIZE // offset in sub-block - stat

                val stat1 = STATS1_MAP[stat]
                val stat2 = STATS2_MAP[stat]

                if (stat1 == stat2)
                // solo stat
                {
                    val `as` = _augStats[color][stat1.toInt()]
                    temp.add(AugStat(`as`.stat, `as`.getSingleStatValue(level)))
                } else
                // combined stat
                {
                    var `as` = _augStats[color][stat1.toInt()]
                    temp.add(AugStat(`as`.stat, `as`.getCombinedStatValue(level)))

                    `as` = _augStats[color][stat2.toInt()]
                    temp.add(AugStat(`as`.stat, `as`.getCombinedStatValue(level)))
                }
            } else if (stats[i] >= BASESTAT_STR && stats[i] <= BASESTAT_MEN) {
                when (stats[i]) {
                    BASESTAT_STR -> temp.add(AugStat(Stats.STAT_STR, 1.0f))
                    BASESTAT_CON -> temp.add(AugStat(Stats.STAT_CON, 1.0f))
                    BASESTAT_INT -> temp.add(AugStat(Stats.STAT_INT, 1.0f))
                    BASESTAT_MEN -> temp.add(AugStat(Stats.STAT_MEN, 1.0f))
                }
            }// its a base stat
        }
        return temp
    }

    class AugStat(val stat: Stats, val value: Float)

    class AugmentationStat(
        val stat: Stats,
        private val _singleValues: FloatArray,
        private val _combinedValues: FloatArray
    ) {
        val singleStatSize: Int
        val combinedStatSize: Int

        init {
            singleStatSize = _singleValues.size
            combinedStatSize = _combinedValues.size
        }

        fun getSingleStatValue(i: Int): Float {
            return if (i >= singleStatSize || i < 0) _singleValues[singleStatSize - 1] else _singleValues[i]

        }

        fun getCombinedStatValue(i: Int): Float {
            return if (i >= combinedStatSize || i < 0) _combinedValues[combinedStatSize - 1] else _combinedValues[i]

        }
    }
}