@echo off
title aCis account manager console
@java -Djava.util.logging.config.file=config/console.cfg -cp ./libs/*; SQLAccountManager
@pause
