package com.l2kt.accountmanager

import com.l2kt.Config
import com.l2kt.L2DatabaseFactory
import java.security.MessageDigest
import java.sql.SQLException
import java.util.*

object SQLAccountManager {

    @JvmStatic
    fun main(args: Array<String>) {
        var username = ""
        var password = ""
        var level = ""
        var mode = ""

        Config.loadAccountManager()

        Scanner(System.`in`).use {
            while (true) {
                println("Please choose an option:")
                println()
                println("1 - Create new account or update existing one (change pass and access level)")
                println("2 - Change access level")
                println("3 - Delete existing account")
                println("4 - List accounts and access levels")
                println("5 - Exit")

                while (!(mode == "1" || mode == "2" || mode == "3" || mode == "4" || mode == "5")) {
                    print("Your choice: ")
                    mode = it.next()
                }

                if (mode == "1" || mode == "2" || mode == "3") {
                    while (username.trim().isEmpty()) {
                        print("Username: ")
                        username = it.next().toLowerCase()
                    }

                    if (mode == "1") {
                        while (password.trim().isEmpty()) {
                            print("Password: ")
                            password = it.next()
                        }
                    }

                    if (mode == "1" || mode == "2") {
                        while (level.trim().isEmpty()) {
                            print("Access level: ")
                            level = it.next()
                        }
                    }
                }

                when(mode){
                    "1" -> addOrUpdateAccount(username.trim(), password.trim(), level.trim())
                    "2" -> changeAccountLevel(username.trim(), level.trim())
                    "3" -> {
                        print("WARNING: This will not delete the gameserver data (characters, items, etc..)")
                        print(" it will only delete the account login server data.")
                        println()
                        print("Do you really want to delete this account? Y/N: ")

                        val yesno = it.next()
                        if (yesno != null && yesno.equals("Y", ignoreCase = true)) {
                            deleteAccount(username.trim())
                        } else {
                            println("Deletion cancelled.")
                        }
                    }
                    "4" -> {
                        mode = ""
                        println()
                        println("Please choose a listing mode:")
                        println()
                        println("1 - Banned accounts only (accessLevel < 0)")
                        println("2 - GM/privileged accounts (accessLevel > 0")
                        println("3 - Regular accounts only (accessLevel = 0)")
                        println("4 - List all")

                        while (!(mode == "1" || mode == "2" || mode == "3" || mode == "4")) {
                            print("Your choice: ")
                            mode = it.next()
                        }
                        println()
                        printAccInfo(mode)
                    }
                    "5" -> System.exit(0)
                }

                username = ""
                password = ""
                level = ""
                mode = ""
                println()
            }
        }
    }

    private fun printAccInfo(m: String) {
        var count = 0
        var q = "SELECT login, access_level FROM accounts "
        when (m) {
            "1" -> q += "WHERE access_level < 0"
            "2" -> q += "WHERE access_level > 0"
            "3" -> q += "WHERE access_level = 0"
        }
        q = "$q ORDER BY login ASC"

        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement(q).use { ps ->
                    ps.executeQuery().use {
                        while (it.next()) {
                            println(it.getString("login") + " -> " + it.getInt("access_level"))
                            count++
                        }

                        println("Displayed accounts: $count")
                    }
                }
            }
        } catch (e: SQLException) {
            println("There was error while displaying accounts:")
            println(e.message)
        }

    }

    private fun addOrUpdateAccount(account: String, password: String, level: String) {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement("REPLACE accounts(login, password, access_level) VALUES (?, ?, ?)").use {
                    val newPassword = MessageDigest.getInstance("SHA").digest(password.toByteArray(charset("UTF-8")))

                    it.setString(1, account)
                    it.setString(2, Base64.getEncoder().encodeToString(newPassword))
                    it.setString(3, level)
                    if (it.executeUpdate() > 0) {
                        println("Account $account has been created or updated")
                    } else {
                        println("Account $account doesn't exist")
                    }
                }
            }
        } catch (e: Exception) {
            println("There was error while adding/updating account:")
            println(e.message)
        }

    }

    private fun changeAccountLevel(account: String, level: String) {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement("UPDATE accounts SET access_level = ? WHERE login = ?").use {
                    it.setString(1, level)
                    it.setString(2, account)
                    if (it.executeUpdate() > 0) {
                        println("Account $account has been updated")
                    } else {
                        println("Account $account doesn't exist")
                    }
                }
            }
        } catch (e: SQLException) {
            println("There was error while updating account:")
            println(e.message)
        }

    }

    private fun deleteAccount(account: String) {
        try {
            L2DatabaseFactory.connection.use { con ->
                con.prepareStatement("DELETE FROM accounts WHERE login = ?").use {
                    it.setString(1, account)
                    if (it.executeUpdate() > 0) {
                        println("Account $account has been deleted")
                    } else {
                        println("Account $account doesn't exist")
                    }
                }
            }
        } catch (e: SQLException) {
            println("There was error while deleting account:")
            println(e.message)
        }

    }
}