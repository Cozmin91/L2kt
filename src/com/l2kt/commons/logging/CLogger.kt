package com.l2kt.commons.logging

import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger

import com.l2kt.commons.lang.StringReplacer

/**
 * Wraps the regular [Logger] to handle slf4j features, notably {} replacement.<br></br>
 * <br></br>
 * The current values should be used :
 *
 *  * debug (Level.FINE): debug purposes (end replacement for Config.DEBUG).
 *  * info (Level.INFO) : send regular informations to the console.
 *  * warn (Level.WARNING): report failed integrity checks.
 *  * error (Level.SEVERE): report an issue involving data loss / leading to unexpected server behavior.
 *
 */
class CLogger(name: String) {
    private val _logger: Logger = Logger.getLogger(name)

    private fun log0(level: Level, caller: StackTraceElement?, message: Any, exception: Throwable?) {
        var caller = caller
        if (!_logger.isLoggable(level))
            return

        if (caller == null)
            caller = Throwable().stackTrace[2]

        _logger.logp(level, caller!!.className, caller.methodName, message.toString(), exception)
    }

    private fun log0(level: Level, caller: StackTraceElement?, message: Any, exception: Throwable?, vararg args: Any) {
        var caller = caller
        if (!_logger.isLoggable(level))
            return

        if (caller == null)
            caller = Throwable().stackTrace[2]

        _logger.logp(level, caller!!.className, caller.methodName, format(message.toString(), *args), exception)
    }

    fun log(record: LogRecord) {
        _logger.log(record)
    }

    /**
     * Logs a message with Level.FINE.
     * @param message : The object to log.
     */
    fun debug(message: Any) {
        log0(Level.FINE, null, message, null)
    }

    /**
     * Logs a message with Level.FINE.
     * @param message : The object to log.
     * @param args : The passed arguments, used to format the message.
     */
    fun debug(message: Any, vararg args: Any) {
        log0(Level.FINE, null, message, null, *args)
    }

    /**
     * Logs a message with Level.FINE.
     * @param message : The object to log.
     * @param exception : Log the caught exception.
     */
    fun debug(message: Any, exception: Throwable) {
        log0(Level.FINE, null, message, exception)
    }

    /**
     * Logs a message with Level.FINE.
     * @param message : The object to log.
     * @param exception : Log the caught exception.
     * @param args : The passed arguments, used to format the message.
     */
    fun debug(message: Any, exception: Throwable, vararg args: Any) {
        log0(Level.FINE, null, message, exception, *args)
    }

    /**
     * Logs a message with Level.INFO.
     * @param message : The object to log.
     */
    fun info(message: Any) {
        log0(Level.INFO, null, message, null)
    }

    /**
     * Logs a message with Level.INFO.
     * @param message : The object to log.
     * @param args : The passed arguments, used to format the message.
     */
    fun info(message: Any, vararg args: Any) {
        log0(Level.INFO, null, message, null, *args)
    }

    /**
     * Logs a message with Level.INFO.
     * @param message : The object to log.
     * @param exception : Log the caught exception.
     */
    fun info(message: Any, exception: Throwable) {
        log0(Level.INFO, null, message, exception)
    }

    /**
     * Logs a message with Level.INFO.
     * @param message : The object to log.
     * @param exception : Log the caught exception.
     * @param args : The passed arguments, used to format the message.
     */
    fun info(message: Any, exception: Throwable, vararg args: Any) {
        log0(Level.INFO, null, message, exception, *args)
    }

    /**
     * Logs a message with Level.WARNING.
     * @param message : The object to log.
     */
    fun warn(message: Any) {
        log0(Level.WARNING, null, message, null)
    }

    /**
     * Logs a message with Level.WARNING.
     * @param message : The object to log.
     * @param args : The passed arguments, used to format the message.
     */
    fun warn(message: Any, vararg args: Any) {
        log0(Level.WARNING, null, message, null, *args)
    }

    /**
     * Logs a message with Level.WARNING.
     * @param message : The object to log.
     * @param exception : Log the caught exception.
     */
    fun warn(message: Any, exception: Throwable) {
        log0(Level.WARNING, null, message, exception)
    }

    /**
     * Logs a message with Level.WARNING.
     * @param message : The object to log.
     * @param exception : Log the caught exception.
     * @param args : The passed arguments, used to format the message.
     */
    fun warn(message: Any, exception: Throwable, vararg args: Any) {
        log0(Level.WARNING, null, message, exception, *args)
    }

    /**
     * Logs a message with Level.SEVERE.
     * @param message : The object to log.
     */
    fun error(message: Any) {
        log0(Level.SEVERE, null, message, null)
    }

    /**
     * Logs a message with Level.SEVERE.
     * @param message : The object to log.
     * @param args : The passed arguments, used to format the message.
     */
    fun error(message: Any, vararg args: Any) {
        log0(Level.SEVERE, null, message, null, *args)
    }

    /**
     * Logs a message with Level.SEVERE.
     * @param message : The object to log.
     * @param exception : Log the caught exception.
     */
    fun error(message: Any, exception: Throwable) {
        log0(Level.SEVERE, null, message, exception)
    }

    /**
     * Logs a message with Level.SEVERE.
     * @param message : The object to log.
     * @param exception : Log the caught exception.
     * @param args : The passed arguments, used to format the message.
     */
    fun error(message: Any, exception: Throwable, vararg args: Any) {
        log0(Level.SEVERE, null, message, exception, *args)
    }

    /**
     * Format the message, allowing to use {} as parameter. Avoid to generate String concatenation.
     * @param message : the Object (String) message to format.
     * @param args : the arguments to pass.
     * @return a formatted String.
     */
    private fun format(message: String, vararg args: Any): String {
        if (args.isEmpty())
            return message

        val sr = StringReplacer(message)
        sr.replaceAll(*args)
        return sr.toString()
    }
}