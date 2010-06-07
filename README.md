Scribe Appender for Log4j
=========================

See src/test/resources/log4j.properties for sample usage.

This appender _extends_ Log4j standard *DailyRollingFileAppender* and writes back to file whenever connection to scribe server is lost.
Scriber Appender tries to reconnect to server if the connection is lost.

Please note that in this version only sync appender works. Will enable async appender in close future.

Sample Usage (inside _log4j.properties_):

    log4j.rootCategory=ALL, scribe

    log4j.appender.scribe=com.magfa.sms.scribe_log4j.ScribeAppender
    log4j.appender.scribe.Threshold=ALL
    log4j.appender.scribe.scribeHost=logsrvhost
    log4j.appender.scribe.scribePort=1463
    log4j.appender.scribe.scribeCategory=system
    log4j.appender.scribe.layout=org.apache.log4j.PatternLayout
    log4j.appender.scribe.layout.ConversionPattern=%d{yy/MM/dd HH:mm:ss} %p [%c] %m%n
    log4j.appender.scribe.File=/tmp/scribe-test-backup.log
    log4j.appender.scribe.DatePattern='.'yyyy-MM-dd

