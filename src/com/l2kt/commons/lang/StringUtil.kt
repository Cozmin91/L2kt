package com.l2kt.commons.lang

import java.text.NumberFormat
import java.util.Arrays
import java.util.Collections
import java.util.Locale
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

import com.l2kt.commons.logging.CLogger

object StringUtil {
    val DIGITS = "0123456789"
    val LOWER_CASE_LETTERS = "abcdefghijklmnopqrstuvwxyz"
    val UPPER_CASE_LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

    val LETTERS = LOWER_CASE_LETTERS + UPPER_CASE_LETTERS
    val LETTERS_AND_DIGITS = LETTERS + DIGITS

    private val LOGGER = CLogger(StringUtil::class.java.name)

    /**
     * Checks each String passed as parameter. If at least one is empty or null, than return false.
     * @param strings : The Strings to test.
     * @return false if at least one String is empty or null.
     */
    fun isEmpty(vararg strings: String): Boolean {
        for (str in strings) {
            if (str.isEmpty())
                return false
        }
        return true
    }

    /**
     * Appends objects to an existing StringBuilder.
     * @param sb : the StringBuilder to edit.
     * @param content : parameters to append.
     */
    fun append(sb: StringBuilder, vararg content: Any) {
        for (obj in content)
            sb.append(obj.toString())
    }

    /**
     * @param text : the String to check.
     * @return true if the String contains only numbers, false otherwise.
     */
    fun isDigit(text: String?): Boolean {
        return text?.matches("[0-9]+".toRegex()) ?: false

    }

    /**
     * @param text : the String to check.
     * @return true if the String contains only numbers and letters, false otherwise.
     */
    fun isAlphaNumeric(text: String?): Boolean {
        if (text == null)
            return false

        for (chars in text.toCharArray()) {
            if (!Character.isLetterOrDigit(chars))
                return false
        }
        return true
    }

    /**
     * @param value : the number to format.
     * @return a number formatted with "," delimiter.
     */
    fun formatNumber(value: Long): String {
        return NumberFormat.getInstance(Locale.ENGLISH).format(value)
    }

    /**
     * @param string : the initial word to scramble.
     * @return an anagram of the given string.
     */
    fun scrambleString(string: String): String {
        val letters = Arrays.asList(*string.split("".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
        letters.shuffle()

        val sb = StringBuilder(string.length)
        for (c in letters)
            sb.append(c)

        return sb.toString()
    }

    /**
     * Verify if the given text matches with the regex pattern.
     * @param text : the text to test.
     * @param regex : the regex pattern to make test with.
     * @return true if matching.
     */
    fun isValidString(text: String, regex: String): Boolean {
        val pattern: Pattern = try {
            Pattern.compile(regex)
        } catch (e: PatternSyntaxException) // case of illegal pattern
        {
            Pattern.compile(".*")
        }

        val regexp = pattern.matcher(text)

        return regexp.matches()
    }

    /**
     * Format a given text to fit with logging "title" criterias, and send it.
     * @param text : the String to format.
     */
    fun printSection(text: String) {
        val sb = StringBuilder(80)
        for (i in 0 until 73 - text.length)
            sb.append("-")

        StringUtil.append(sb, "=[ ", text, " ]")

        LOGGER.info(sb.toString())
    }

    /**
     * Format a time given in seconds into "h m s" String format.
     * @param time : a time given in seconds.
     * @return a "h m s" formated String.
     */
    fun getTimeStamp(time: Int): String {
        var time = time
        val hours = time / 3600
        time %= 3600
        val minutes = time / 60
        time %= 60

        var result = ""
        if (hours > 0)
            result += hours.toString() + "h"
        if (minutes > 0)
            result += " " + minutes + "m"
        if (time > 0 || result.length == 0)
            result += " " + time + "s"

        return result
    }

    /**
     * Format a [String] to delete its extension ("castles.xml" > "castles"), if any.
     * @param fileName : The String to edit, which is a former file name.
     * @return a left-side truncated String to the first "." encountered.
     */
    fun getNameWithoutExtension(fileName: String): String {
        var fileName = fileName
        val pos = fileName.lastIndexOf(".")
        if (pos > 0)
            fileName = fileName.substring(0, pos)

        return fileName
    }
}