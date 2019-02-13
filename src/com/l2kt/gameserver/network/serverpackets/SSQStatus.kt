package com.l2kt.gameserver.network.serverpackets

import com.l2kt.gameserver.instancemanager.SevenSigns
import com.l2kt.gameserver.instancemanager.SevenSigns.CabalType
import com.l2kt.gameserver.instancemanager.SevenSignsFestival
import com.l2kt.gameserver.instancemanager.SevenSignsFestival.FestivalType
import com.l2kt.gameserver.network.SystemMessageId

class SSQStatus(private val _objectId: Int, private val _page: Int) : L2GameServerPacket() {

    override fun writeImpl() {
        val winningCabal = SevenSigns.getInstance().cabalHighestScore
        val totalDawnMembers = SevenSigns.getInstance().getTotalMembers(CabalType.DAWN)
        val totalDuskMembers = SevenSigns.getInstance().getTotalMembers(CabalType.DUSK)

        writeC(0xf5)

        writeC(_page)
        writeC(SevenSigns.getInstance().currentPeriod.ordinal)

        var dawnPercent = 0
        var duskPercent = 0

        when (_page) {
            1 -> {
                // [ddd cc dd ddd c ddd c]
                writeD(SevenSigns.getInstance().currentCycle)

                when (SevenSigns.getInstance().currentPeriod) {
                    SevenSigns.PeriodType.RECRUITING -> {
                        writeD(SystemMessageId.INITIAL_PERIOD.id)
                        writeD(SystemMessageId.UNTIL_TODAY_6PM.id)
                    }

                    SevenSigns.PeriodType.COMPETITION -> {
                        writeD(SystemMessageId.QUEST_EVENT_PERIOD.id)
                        writeD(SystemMessageId.UNTIL_MONDAY_6PM.id)
                    }

                    SevenSigns.PeriodType.RESULTS -> {
                        writeD(SystemMessageId.RESULTS_PERIOD.id)
                        writeD(SystemMessageId.UNTIL_TODAY_6PM.id)
                    }

                    SevenSigns.PeriodType.SEAL_VALIDATION -> {
                        writeD(SystemMessageId.VALIDATION_PERIOD.id)
                        writeD(SystemMessageId.UNTIL_MONDAY_6PM.id)
                    }
                }

                writeC(SevenSigns.getInstance().getPlayerCabal(_objectId).ordinal)
                writeC(SevenSigns.getInstance().getPlayerSeal(_objectId).ordinal)

                writeD(SevenSigns.getInstance().getPlayerStoneContrib(_objectId)) // Seal Stones Turned-In
                writeD(SevenSigns.getInstance().getPlayerAdenaCollect(_objectId)) // Ancient Adena to Collect

                val dawnStoneScore = SevenSigns.getInstance().getCurrentStoneScore(CabalType.DAWN)
                val dawnFestivalScore = SevenSigns.getInstance().getCurrentFestivalScore(CabalType.DAWN)

                val duskStoneScore = SevenSigns.getInstance().getCurrentStoneScore(CabalType.DUSK)
                val duskFestivalScore = SevenSigns.getInstance().getCurrentFestivalScore(CabalType.DUSK)

                val totalStoneScore = duskStoneScore + dawnStoneScore

                /*
				 * Scoring seems to be proportionate to a set base value, so base this on the maximum obtainable score from festivals, which is 500.
				 */
                var duskStoneScoreProp = 0
                var dawnStoneScoreProp = 0

                if (totalStoneScore != 0.0) {
                    duskStoneScoreProp = Math.round(duskStoneScore.toFloat() / totalStoneScore.toFloat() * 500)
                    dawnStoneScoreProp = Math.round(dawnStoneScore.toFloat() / totalStoneScore.toFloat() * 500)
                }

                val duskTotalScore = SevenSigns.getInstance().getCurrentScore(CabalType.DUSK)
                val dawnTotalScore = SevenSigns.getInstance().getCurrentScore(CabalType.DAWN)

                val totalOverallScore = duskTotalScore + dawnTotalScore

                if (totalOverallScore != 0) {
                    dawnPercent = Math.round(dawnTotalScore.toFloat() / totalOverallScore.toFloat() * 100)
                    duskPercent = Math.round(duskTotalScore.toFloat() / totalOverallScore.toFloat() * 100)
                }

                /* DUSK */
                writeD(duskStoneScoreProp) // Seal Stone Score
                writeD(duskFestivalScore) // Festival Score
                writeD(duskTotalScore) // Total Score

                writeC(duskPercent) // Dusk %

                /* DAWN */
                writeD(dawnStoneScoreProp) // Seal Stone Score
                writeD(dawnFestivalScore) // Festival Score
                writeD(dawnTotalScore) // Total Score

                writeC(dawnPercent) // Dawn %
            }
            2 -> {
                // c cc hc [cd (dc (S))]
                writeH(1)

                writeC(5) // Total number of festivals

                for (level in FestivalType.VALUES) {
                    val festivalId = level.ordinal

                    writeC(festivalId + 1) // Current client-side festival ID
                    writeD(level.maxScore)

                    val duskScore = SevenSignsFestival.getInstance().getHighestScore(CabalType.DUSK, festivalId)
                    val dawnScore = SevenSignsFestival.getInstance().getHighestScore(CabalType.DAWN, festivalId)

                    // Dusk Score \\
                    writeD(duskScore)

                    var highScoreData = SevenSignsFestival.getInstance().getHighestScoreData(CabalType.DUSK, festivalId)
                    var partyMembers: MutableList<String> =
                        highScoreData.getString("members").split(",").dropLastWhile { it.isEmpty() }.toMutableList()

                    writeC(partyMembers.size)

                    for (partyMember in partyMembers)
                        writeS(partyMember)

                    // Dawn Score \\
                    writeD(dawnScore)

                    highScoreData = SevenSignsFestival.getInstance().getHighestScoreData(CabalType.DAWN, festivalId)
                    partyMembers =
                            highScoreData.getString("members").split(",").dropLastWhile { it.isEmpty() }
                                .toMutableList()

                    writeC(partyMembers.size)

                    for (partyMember in partyMembers)
                        writeS(partyMember)
                }
            }
            3 -> {
                // c cc [ccc (cccc)]
                writeC(10) // Minimum limit for winning cabal to retain their seal
                writeC(35) // Minimum limit for winning cabal to claim a seal
                writeC(3) // Total number of seals

                for ((seal, sealOwner) in SevenSigns.getInstance().sealOwners) {

                    val dawnProportion = SevenSigns.getInstance().getSealProportion(seal, CabalType.DAWN)
                    val duskProportion = SevenSigns.getInstance().getSealProportion(seal, CabalType.DUSK)

                    writeC(seal.ordinal)
                    writeC(sealOwner.ordinal)

                    if (totalDuskMembers == 0) {
                        if (totalDawnMembers == 0) {
                            writeC(0)
                            writeC(0)
                        } else {
                            writeC(0)
                            writeC(Math.round(dawnProportion.toFloat() / totalDawnMembers.toFloat() * 100))
                        }
                    } else {
                        if (totalDawnMembers == 0) {
                            writeC(Math.round(duskProportion.toFloat() / totalDuskMembers.toFloat() * 100))
                            writeC(0)
                        } else {
                            writeC(Math.round(duskProportion.toFloat() / totalDuskMembers.toFloat() * 100))
                            writeC(Math.round(dawnProportion.toFloat() / totalDawnMembers.toFloat() * 100))
                        }
                    }
                }
            }
            4 -> {
                // c cc [cc (cchh)]
                writeC(winningCabal.ordinal) // Overall predicted winner
                writeC(3) // Total number of seals

                for ((seal, sealOwner) in SevenSigns.getInstance().sealOwners) {

                    val dawnProportion = SevenSigns.getInstance().getSealProportion(seal, CabalType.DAWN)
                    val duskProportion = SevenSigns.getInstance().getSealProportion(seal, CabalType.DUSK)

                    dawnPercent = Math.round(dawnProportion / if (totalDawnMembers == 0) 1f else totalDawnMembers.toFloat() * 100)
                    duskPercent = Math.round(duskProportion / if (totalDuskMembers == 0) 1f else totalDuskMembers.toFloat() * 100)

                    writeC(sealOwner.ordinal)

                    when (sealOwner) {
                        SevenSigns.CabalType.NORMAL -> when (winningCabal) {
                            SevenSigns.CabalType.NORMAL -> {
                                writeC(CabalType.NORMAL.ordinal)
                                writeH(SystemMessageId.COMPETITION_TIE_SEAL_NOT_AWARDED.id)
                            }
                            SevenSigns.CabalType.DAWN -> if (dawnPercent >= 35) {
                                writeC(CabalType.DAWN.ordinal)
                                writeH(SystemMessageId.SEAL_NOT_OWNED_35_MORE_VOTED.id)
                            } else {
                                writeC(CabalType.NORMAL.ordinal)
                                writeH(SystemMessageId.SEAL_NOT_OWNED_35_LESS_VOTED.id)
                            }
                            SevenSigns.CabalType.DUSK -> if (duskPercent >= 35) {
                                writeC(CabalType.DUSK.ordinal)
                                writeH(SystemMessageId.SEAL_NOT_OWNED_35_MORE_VOTED.id)
                            } else {
                                writeC(CabalType.NORMAL.ordinal)
                                writeH(SystemMessageId.SEAL_NOT_OWNED_35_LESS_VOTED.id)
                            }
                        }

                        SevenSigns.CabalType.DAWN -> when (winningCabal) {
                            SevenSigns.CabalType.NORMAL -> if (dawnPercent >= 10) {
                                writeC(CabalType.DAWN.ordinal)
                                writeH(SystemMessageId.SEAL_OWNED_10_MORE_VOTED.id)
                            } else {
                                writeC(CabalType.NORMAL.ordinal)
                                writeH(SystemMessageId.COMPETITION_TIE_SEAL_NOT_AWARDED.id)
                            }

                            SevenSigns.CabalType.DAWN -> if (dawnPercent >= 10) {
                                writeC(sealOwner.ordinal)
                                writeH(SystemMessageId.SEAL_OWNED_10_MORE_VOTED.id)
                            } else {
                                writeC(CabalType.NORMAL.ordinal)
                                writeH(SystemMessageId.SEAL_OWNED_10_LESS_VOTED.id)
                            }

                            SevenSigns.CabalType.DUSK -> if (duskPercent >= 35) {
                                writeC(CabalType.DUSK.ordinal)
                                writeH(SystemMessageId.SEAL_NOT_OWNED_35_MORE_VOTED.id)
                            } else if (dawnPercent >= 10) {
                                writeC(CabalType.DAWN.ordinal)
                                writeH(SystemMessageId.SEAL_OWNED_10_MORE_VOTED.id)
                            } else {
                                writeC(CabalType.NORMAL.ordinal)
                                writeH(SystemMessageId.SEAL_OWNED_10_LESS_VOTED.id)
                            }
                        }

                        SevenSigns.CabalType.DUSK -> when (winningCabal) {
                            SevenSigns.CabalType.NORMAL -> if (duskPercent >= 10) {
                                writeC(CabalType.DUSK.ordinal)
                                writeH(SystemMessageId.SEAL_OWNED_10_MORE_VOTED.id)
                            } else {
                                writeC(CabalType.NORMAL.ordinal)
                                writeH(SystemMessageId.COMPETITION_TIE_SEAL_NOT_AWARDED.id)
                            }

                            SevenSigns.CabalType.DAWN -> if (dawnPercent >= 35) {
                                writeC(CabalType.DAWN.ordinal)
                                writeH(SystemMessageId.SEAL_NOT_OWNED_35_MORE_VOTED.id)
                            } else if (duskPercent >= 10) {
                                writeC(sealOwner.ordinal)
                                writeH(SystemMessageId.SEAL_OWNED_10_MORE_VOTED.id)
                            } else {
                                writeC(CabalType.NORMAL.ordinal)
                                writeH(SystemMessageId.SEAL_OWNED_10_LESS_VOTED.id)
                            }

                            SevenSigns.CabalType.DUSK -> if (duskPercent >= 10) {
                                writeC(sealOwner.ordinal)
                                writeH(SystemMessageId.SEAL_OWNED_10_MORE_VOTED.id)
                            } else {
                                writeC(CabalType.NORMAL.ordinal)
                                writeH(SystemMessageId.SEAL_OWNED_10_LESS_VOTED.id)
                            }
                        }
                    }
                }
            }
        }
    }
}