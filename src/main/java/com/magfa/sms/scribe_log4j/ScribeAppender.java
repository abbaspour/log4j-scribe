package com.magfa.sms.scribe_log4j;

import com.magfa.sms.scribe_log4j.gen.LogEntry;
import com.magfa.sms.scribe_log4j.gen.ResultCode;
import com.magfa.sms.scribe_log4j.gen.scribe;
import org.apache.log4j.DailyRollingFileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TFramedTransport;
import org.apache.thrift.transport.TSocket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/*
* A Log4j Appender that writes log entries to a Scribe server.
* By default the Scribe server is expected to run on localhost, port 1463.
*/
@SuppressWarnings({"UnusedDeclaration"})
public class ScribeAppender extends DailyRollingFileAppender {

    public static final String MY_HOST_NAME_PREFIX;

    static {
        try {
            MY_HOST_NAME_PREFIX = InetAddress.getLocalHost().getHostName() + " ";

        } catch (UnknownHostException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static final String DEFAULT_SCRIBE_HOST = "127.0.0.1";
    private static final int DEFAULT_SCRIBE_PORT = 1463;
    private static final int RECONNECT_PERIOD = 1000;

    private String scribeHost;
    private int scribePort = DEFAULT_SCRIBE_PORT;
    private String scribeCategory = "";

    private volatile scribe.Client client;
    private volatile TFramedTransport transport;
    private volatile boolean connected = false;

    private static final String DEFAULT_DATE_PATTERN = "'.'yyyy-MM-dd";

    public ScribeAppender() {
    }

    public ScribeAppender(Layout layout, String scribeHost, int scribePort, String category, String accessLogPath) throws IOException {

        super(layout, accessLogPath, DEFAULT_DATE_PATTERN);

        this.layout = layout;
        this.scribeHost = scribeHost;
        this.scribePort = scribePort;
        this.scribeCategory = category;

        activateOptions();
    }

    public void setScribeHost(String scribeHost) {
        this.scribeHost = scribeHost;
    }

    public String getScribeHost() {
        return scribeHost == null ? DEFAULT_SCRIBE_HOST : scribeHost;
    }

    public void setScribePort(int scribePort) {
        this.scribePort = scribePort;
    }

    public void setScribeCategory(String scribeCategory) {
        this.scribeCategory = scribeCategory;
    }

    /*
    * Activates this Appender by opening
    * a transport to the Scribe server.
    */
    @Override
    public void activateOptions() {
        super.activateOptions();
        connect();

        final Timer reconnectTimer = new Timer("scribe-reconnect", true);
        reconnectTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!connected)
                    connect();
            }
        }, 1000, RECONNECT_PERIOD);
    }

    private void connect() {

        final String address = getScribeHost() + ":" + scribePort;

        System.err.println("trying to (re)connect to scribe server at: " + address);

        try {
            final TSocket sock = new TSocket(new Socket(getScribeHost(), scribePort));
            transport = new TFramedTransport(sock);
            final TBinaryProtocol protocol = new TBinaryProtocol(transport, false, false);
            client = new scribe.Client(protocol, protocol);
            // transport.open(); // This is commented out because it was throwing Exceptions for no good reason.
            connected = true;
            System.out.println("connected to scribe server at: " + address);

        } catch (Throwable t) {
            System.err.println("Exception on trying to connect to scribe server at: " + address + ", reason: " + t.getMessage());
            connected = false;
        }

    }

    /*
    * Appends a log message to Scribe
    */
    @Override
    public void append(LoggingEvent event) {

        if (!connected) {
            super.append(event);
            return;
        }

        final LogEntry entry = new LogEntry(scribeCategory, MY_HOST_NAME_PREFIX + layout.format(event));
        final List<LogEntry> entries;

        if (layout.ignoresThrowable()) {
            final String[] stack = event.getThrowableStrRep();
            if (stack != null) {
                entries = new ArrayList<LogEntry>(stack.length + 1); // entry + stack#
                entries.add(entry);
                for (final String s : stack) {
                    entries.add(new LogEntry(scribeCategory, MY_HOST_NAME_PREFIX + s + Layout.LINE_SEP));
                }
            } else {
                entries = new ArrayList<LogEntry>(1);
                entries.add(entry);
            }
        } else {
            entries = new ArrayList<LogEntry>(1);
            entries.add(entry);
        }


        boolean sendOk = false;

        try {
            sendOk = (client.Log(entries) == ResultCode.OK);
        } catch (Throwable t) {
            System.err.println("Exception in log to scribd. reason: " + t);
            connected = false;
        }

        if (!sendOk) {
            super.append(event);
        }
    }


    @Override
    public void close() {
        super.close();
        if (transport != null) {
            transport.close();
        }
    }

    @Override
    public boolean requiresLayout() {
        return true;
    }


}