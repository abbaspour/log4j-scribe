Scribe Appender for Log4j
=========================

***
Usage
-----

This appender _extends_ Log4j standard *DailyRollingFileAppender* and writes back to file whenever connection to scribe server is lost.
Scribe Appender tries to reconnect to server if the connection is lost.

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

***
Scribe Setup
------------
Sample Scribe config format (_scribe.conf_)

    port=1463
    max_msg_per_second=2000000
    check_interval=3
    
    <store>
        category=system*
        type=buffer

        target_write_size=20480
        max_write_interval=1
        buffer_send_rate=2
        retry_interval=30
        retry_interval_range=10

        <primary>
            type=file
            fs_type=std
            file_path=/var/log/gw/system
            base_filename=thisisoverwritten
            max_size=1000000000
            add_newlines=0
            rotate_period=daily
            rotate_hour=0
            rotate_minute=10
        </primary>

        <secondary>
            type=file
            fs_type=std
            file_path=/tmp
            base_filename=thisisoverwritten
            max_size=3000000000
            rotate_period=daily
            rotate_hour=0
            rotate_minute=10
        </secondary>
    </store>
