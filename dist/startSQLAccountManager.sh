#!/bin/sh
java -Djava.util.logging.config.file=config/console.cfg -cp ./libs/*:l2kt.jar:mysql-connector-java-5.1.26.jar SQLAccountManager
