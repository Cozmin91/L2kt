package com.l2kt.gsregistering

import com.l2kt.Config
import com.l2kt.L2DatabaseFactory
import com.l2kt.gameserver.LoginServerThread
import com.l2kt.loginserver.GameServerManager
import com.l2kt.loginserver.model.GameServerInfo
import java.math.BigInteger
import java.sql.SQLException
import java.util.*

object GameServerRegister {

    @JvmStatic
    fun main(args: Array<String>) {
        Config.loadGameServerRegistration()
        var choice: String
        Scanner(System.`in`).use { _scn ->
            println()
            println()
            println("                        L2kt gameserver registering")
            println("                        ____________________________")
            println()
            println("OPTIONS : a number : register a server ID, if available and existing on list.")
            println("          list : get a list of IDs. A '*' means the id is already used.")
            println("          clean : unregister a specified gameserver.")
            println("          cleanall : unregister all gameservers.")
            println("          exit : exit the program.")

            while (true) {
                println()
                print("Your choice? ")
                choice = _scn.next()

                if (choice.equals("list", ignoreCase = true)) {
                    println()
                    for ((key, value) in GameServerManager.getInstance().serverNames)
                        println(
                            key.toString() + ": " + value + " " + if (GameServerManager.getInstance().registeredGameServers.containsKey(
                                    key
                                )
                            ) "*" else ""
                        )
                } else if (choice.equals("clean", ignoreCase = true)) {
                    println()

                    if (GameServerManager.getInstance().serverNames.isEmpty())
                        println("No server names available, be sure 'serverNames.xml' is in the LoginServer directory.")
                    else {
                        println("UNREGISTER a specific server. Here's the current list :")
                        for (entry in GameServerManager.getInstance().registeredGameServers.values)
                            println(entry.id.toString() + ": " + GameServerManager.getInstance().serverNames[entry.id])

                        println()
                        print("Your choice? ")

                        choice = _scn.next()
                        try {
                            val id = Integer.parseInt(choice!!)

                            if (!GameServerManager.getInstance().registeredGameServers.containsKey(id))
                                println("This server id isn't used.")
                            else {
                                try {
                                    L2DatabaseFactory.connection.use { con ->
                                        val statement =
                                            con.prepareStatement("DELETE FROM gameservers WHERE server_id=?")
                                        statement.setInt(1, id)
                                        statement.executeUpdate()
                                        statement.close()
                                    }
                                } catch (e: SQLException) {
                                    println("SQL error while cleaning registered server: $e")
                                }

                                GameServerManager.getInstance().registeredGameServers.remove(id)

                                println("You successfully dropped gameserver #$id.")
                            }
                        } catch (nfe: NumberFormatException) {
                            println("Type a valid server id.")
                        }

                    }
                } else if (choice.equals("cleanall", ignoreCase = true)) {
                    println()
                    print("UNREGISTER ALL servers. Are you sure? (y/n) ")

                    choice = _scn.next()

                    if (choice == "y") {
                        try {
                            L2DatabaseFactory.connection.use { con ->
                                val statement = con.prepareStatement("DELETE FROM gameservers")
                                statement.executeUpdate()
                                statement.close()
                            }
                        } catch (e: SQLException) {
                            println("SQL error while cleaning registered servers: $e")
                        }

                        GameServerManager.getInstance().registeredGameServers.clear()

                        println("You successfully dropped all registered gameservers.")
                    } else
                        println("'cleanall' processus has been aborted.")
                } else if (choice.equals("exit", ignoreCase = true))
                    System.exit(0)
                else {
                    try {
                        println()

                        if (GameServerManager.getInstance().serverNames.isEmpty())
                            println("No server names available, be sure 'serverNames.xml' is in the LoginServer directory.")
                        else {
                            val id = Integer.parseInt(choice)

                            when {
                                GameServerManager.getInstance().serverNames[id] == null -> println("No name for server id: $id.")
                                GameServerManager.getInstance().registeredGameServers.containsKey(id) -> println("This server id is already used.")
                                else -> {
                                    val hexId = LoginServerThread.generateHex(16)

                                    GameServerManager.getInstance().registeredGameServers[id] = GameServerInfo(id, hexId)
                                    GameServerManager.getInstance().registerServerOnDB(hexId, id, "")
                                    Config.saveHexid(id, BigInteger(hexId).toString(16), "hexid(server $id).txt")

                                    println("Server registered under 'hexid(server $id).txt'.")
                                    println("Put this file in /config gameserver folder and rename it 'hexid.txt'.")
                                }
                            }
                        }
                    } catch (nfe: NumberFormatException) {
                        println("Type a number or list|clean|cleanall commands.")
                    }

                }
            }
        }
    }
}