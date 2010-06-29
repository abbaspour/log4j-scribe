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

Building Scribe for Java/Log4J in Ubuntu Linux (10.04 Lucid)
============================================================

Reference [Compiling, installing and test-running Scribe](http://agiletesting.blogspot.com/2009/10/compiling-installing-and-test-running.html)

Note: Ubuntu 10.04 uses Boost 1.40 so there is no need to build the latest version of it. Simply use aptitude to install it and other required packages as bellow:

Required Packages
-----------------

	sudo aptitude install build-essential autoconf libboost-dev g++ make flex bison libtool mono-gmcs libevent-dev libboost-filesystem-dev libboost-system-dev python-dev git-core sun-java6-jdk


Build and Install Thrift
------------------------

	git clone git://git.thrift-rpc.org/thrift.git
	cd thrift
	./bootstrap.sh
	./configure
	make	
	sudo make install

Build and Install fb303 Library
-------------------------------
	cd contrib/fb303
	./bootstrap.sh
	make
	sudo make install		

Python modules:

	cd ../../lib/py
	sudo python setup.py install
	cd ../../contrib/fb303/py
	sudo python setup.py install

Check it:

	python -c 'import thrift' ; python -c 'import fb303'	

If no warning is shown, you are well done up to here.

Build and Install Scribe
------------------------
First get the latest version from github project [here](http://github.com/facebook/scribe) and extract.

	cd scribe
	./bootstrap.sh
	make
	sudo make install

And python modules:

	cd lib/py
	sudo python setup.py install

And check:

	sudo ldconfig
	python -c 'import scribe'

Scribe init.d Script
--------------------

	#!/bin/sh
	#
	# scribed - this script starts and stops the scribed daemon
	#
	# chkconfig:   - 84 16
	# description:  Scribe is a server for aggregating log data \
	#               streamed in real time from a large number of \
	#               servers.
	# processname: scribed
	# config:      /usr/local/scribe/scribe.conf
	# pidfile:     /var/run/scribed.pid
	
	# Source function library
	. /lib/lsb/init-functions
	
	run="/usr/local/bin/scribed"
	user=[choose-it]
	pid_dir="/var/run"
	
	start() {
	        log_daemon_msg "Starting Scribe"
	        if start-stop-daemon -v -b -m --oknodo -c $user --start --quiet --pidfile "$pid_dir/scribe.pid" --exec /usr/bin/env -- $run; then
	            log_end_msg 0
	        else
	            log_end_msg 1
	        fi
	}
	
	stop() {
	        log_daemon_msg "Stopping Scribe"
	        if start-stop-daemon --stop --quiet --oknodo --pidfile "$pid_dir/scribe.pid"; then
	            log_end_msg 0
	            rm -f "$pid_dir/scribe.pid"
	        else
	            log_end_msg 1
	        fi
	}
	
	status() {
	   $run_ctrl status $port
	}
	
	restart() {
	   stop
	   start
	}
	
	case "$1" in
	   start|stop|restart|status)
	       $1
	       ;;
	   *)
	       echo $"Usage: $0 {start|stop|status|restart|reload}"
	       exit 2
	esac

