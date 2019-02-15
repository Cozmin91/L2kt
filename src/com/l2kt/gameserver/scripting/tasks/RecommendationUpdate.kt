package com.l2kt.gameserver.scripting.tasks

import com.l2kt.L2DatabaseFactory
import com.l2kt.gameserver.model.World
import com.l2kt.gameserver.network.serverpackets.UserInfo
import com.l2kt.gameserver.scripting.Quest
import com.l2kt.gameserver.scripting.ScheduledQuest

class RecommendationUpdate : ScheduledQuest(-1, "tasks") {

    public override fun onStart() {
        // Refresh online characters stats.
        for (player in World.getInstance().players) {
            player.recomChars.clear()

            val level = player.level
            when {
                level < 20 -> {
                    player.recomLeft = 3
                    player.editRecomHave(-1)
                }
                level < 40 -> {
                    player.recomLeft = 6
                    player.editRecomHave(-2)
                }
                else -> {
                    player.recomLeft = 9
                    player.editRecomHave(-3)
                }
            }

            player.sendPacket(UserInfo(player))
        }

        // Refresh database side.
        try {
            L2DatabaseFactory.connection.use { con ->
                // Delete all characters listed on character_recommends table.
                var ps = con.prepareStatement(DELETE_CHAR_RECOMS)
                ps.execute()
                ps.close()

                // Initialize the update statement.
                val ps2 = con.prepareStatement(UPDATE_ALL_RECOMS)

                // Select needed informations of all characters.
                ps = con.prepareStatement(SELECT_ALL_RECOMS)

                val rset = ps.executeQuery()
                while (rset.next()) {
                    val level = rset.getInt("level")
                    if (level < 20) {
                        ps2.setInt(1, 3)
                        ps2.setInt(2, Math.max(0, rset.getInt("rec_have") - 1))
                    } else if (level < 40) {
                        ps2.setInt(1, 6)
                        ps2.setInt(2, Math.max(0, rset.getInt("rec_have") - 2))
                    } else {
                        ps2.setInt(1, 9)
                        ps2.setInt(2, Math.max(0, rset.getInt("rec_have") - 3))
                    }
                    ps2.setInt(3, rset.getInt("obj_Id"))
                    ps2.addBatch()
                }
                rset.close()
                ps.close()

                ps2.executeBatch()
                ps2.close()
            }
        } catch (e: Exception) {
            Quest.LOGGER.error("Couldn't clear players recommendations.", e)
        }

    }

    public override fun onEnd() {}

    companion object {
        private const val DELETE_CHAR_RECOMS = "TRUNCATE TABLE character_recommends"
        private const val SELECT_ALL_RECOMS = "SELECT obj_Id, level, rec_have FROM characters"
        private const val UPDATE_ALL_RECOMS = "UPDATE characters SET rec_left=?, rec_have=? WHERE obj_Id=?"
    }
}