package com.l2kt.commons.lang

/**
 * This class is used as a counter to replaceAll. It avoids to generate Strings and is roughly 50% faster than string method format().
 * @author xblx
 */
class StringReplacer(source: String) {

    private val _sb: StringBuilder = StringBuilder(source)

    /**
     * Replace all delimiters '{}' by the String representation of any objects. Important things to note:
     *
     *  * If there isn't enough parameters, then the leftover isn't processed.
     *  * If there is too much parameters, the loop breaks when it doesn't find anything to replace.
     *  * If the object is null, then it sends "null".
     *
     * @param args : The objects to pass.
     */
    fun replaceAll(vararg args: Any) {
        var index: Int
        var newIndex = 0

        for (obj in args) {
            index = _sb.indexOf(DELIM_STR, newIndex)
            if (index == -1)
                break

            newIndex = index + 2
            _sb.replace(index, newIndex, obj.toString())
        }
    }

    override fun toString(): String {
        return _sb.toString()
    }

    companion object {
        private const val DELIM_STR = "{}"
    }
}