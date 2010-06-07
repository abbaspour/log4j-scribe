package com.magfa.sms.scribe_log4j;

import org.apache.log4j.AsyncAppender;

import java.io.IOException;

/**
 * Note: This is not ready yet!
 */
public class AsyncScribeAppender extends AsyncAppender {

    private String hostname;
    private String scribeHost;
    private int scribePort;
    private String scribeCategory;
    private String accessLogPath;

    public String getAccessLogPath() {
        return accessLogPath;
    }

    public void setAccessLogPath(String accessLogPath) {
        this.accessLogPath = accessLogPath;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public String getScribeHost() {
        return scribeHost;
    }

    public void setScribeHost(String scribeHost) {
        this.scribeHost = scribeHost;
    }

    public int getScribePort() {
        return scribePort;
    }

    public void setScribePort(int scribePort) {
        this.scribePort = scribePort;
    }

    public String getScribeCategory() {
        return scribeCategory;
    }

    public void setScribeCategory(String scribeCategory) {
        this.scribeCategory = scribeCategory;
    }

    @Override
    public void activateOptions() {
        super.activateOptions();
        synchronized (this) {
            ScribeAppender scribeAppender = null;
            try {
                scribeAppender = new ScribeAppender(getLayout(), getScribeHost(), getScribePort(), getScribeCategory(), getAccessLogPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
            addAppender(scribeAppender);
        }
    }

    @Override
    public boolean requiresLayout() {
        return true;
    }

}