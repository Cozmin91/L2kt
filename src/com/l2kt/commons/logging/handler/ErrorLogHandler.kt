package com.l2kt.commons.logging.handler

import java.io.IOException
import java.util.logging.FileHandler

class ErrorLogHandler @Throws(IOException::class, SecurityException::class)
constructor() : FileHandler()