/*
###############################################################################
#                                                                             #
#    Copyright 2016, AdeptJ (http://www.adeptj.com)                           #
#                                                                             #
#    Licensed under the Apache License, Version 2.0 (the "License");          #
#    you may not use this file except in compliance with the License.         #
#    You may obtain a copy of the License at                                  #
#                                                                             #
#        http://www.apache.org/licenses/LICENSE-2.0                           #
#                                                                             #
#    Unless required by applicable law or agreed to in writing, software      #
#    distributed under the License is distributed on an "AS IS" BASIS,        #
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. #
#    See the License for the specific language governing permissions and      #
#    limitations under the License.                                           #
#                                                                             #
###############################################################################
*/

package com.adeptj.runtime.tools.logging;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy;
import ch.qos.logback.core.util.FileSize;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * LogbackManager
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public enum LogbackManager {

    INSTANCE;

    private static final String SYS_PROP_LOG_IMMEDIATE_FLUSH = "log.immediate.flush";

    private final List<Appender<ILoggingEvent>> appenderList;

    private volatile LoggerContext loggerContext;

    LogbackManager() {
        this.appenderList = new ArrayList<>();
        this.loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
    }

    public LoggerContext getLoggerContext() {
        return this.loggerContext;
    }

    public LogbackManager addAppender(Appender<ILoggingEvent> appender) {
        this.appenderList.add(appender);
        return this;
    }

    public List<Appender<ILoggingEvent>> getAppenders() {
        return this.appenderList;
    }

    public Appender<ILoggingEvent> getAppender(String name) {
        return this.appenderList.stream()
                .filter(appender -> appender.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public void addLogger(LogbackConfig logbackConfig) {
        logbackConfig.getLoggerNames().forEach(loggerName -> {
            Logger logger = this.loggerContext.getLogger(loggerName);
            logger.setLevel(Level.toLevel(logbackConfig.getLevel()));
            logger.setAdditive(logbackConfig.isAdditivity());
            logger.addAppender(logbackConfig.getAppender());
        });
    }

    public PatternLayoutEncoder getLayoutEncoder(String logPattern) {
        PatternLayoutEncoder layoutEncoder = new PatternLayoutEncoder();
        layoutEncoder.setContext(this.loggerContext);
        layoutEncoder.setPattern(logPattern);
        layoutEncoder.start();
        return layoutEncoder;
    }

    public ConsoleAppender<ILoggingEvent> getConsoleAppender(String name, String logPattern) {
        ConsoleAppender<ILoggingEvent> consoleAppender = new ConsoleAppender<>();
        consoleAppender.setName(name);
        consoleAppender.setContext(this.loggerContext);
        consoleAppender.setEncoder(this.getLayoutEncoder(logPattern));
        consoleAppender.start();
        return consoleAppender;
    }

    public SizeAndTimeBasedRollingPolicy<ILoggingEvent> getRollingPolicy(LogbackConfig logbackConfig) {
        SizeAndTimeBasedRollingPolicy<ILoggingEvent> trigAndRollPolicy = new SizeAndTimeBasedRollingPolicy<>();
        trigAndRollPolicy.setMaxFileSize(FileSize.valueOf(logbackConfig.getLogMaxSize()));
        trigAndRollPolicy.setContext(this.loggerContext);
        trigAndRollPolicy.setFileNamePattern(logbackConfig.getRolloverFile());
        trigAndRollPolicy.setMaxHistory(logbackConfig.getLogMaxHistory());
        return trigAndRollPolicy;
    }

    public RollingFileAppender<ILoggingEvent> getFileAppender(LogbackConfig logbackConfig) {
        RollingFileAppender<ILoggingEvent> fileAppender = new RollingFileAppender<>();
        fileAppender.setName(logbackConfig.getAppenderName());
        fileAppender.setFile(logbackConfig.getLogFile());
        fileAppender.setAppend(true);
        // First check the system property for immediate log flush, if false then use the immediateFlush parameter.
        fileAppender.setImmediateFlush(Boolean.getBoolean(SYS_PROP_LOG_IMMEDIATE_FLUSH));
        if (!fileAppender.isImmediateFlush()) {
            fileAppender.setImmediateFlush(logbackConfig.isImmediateFlush());
        }
        fileAppender.setEncoder(this.getLayoutEncoder(logbackConfig.getPattern()));
        fileAppender.setContext(this.loggerContext);
        return fileAppender;
    }

    public void addAsyncAppender(LogbackConfig logbackConfig) {
        AsyncAppender asyncAppender = new AsyncAppender();
        asyncAppender.setName(logbackConfig.getAsyncAppenderName());
        asyncAppender.setQueueSize(logbackConfig.getAsyncLogQueueSize());
        asyncAppender.setDiscardingThreshold(logbackConfig.getAsyncLogDiscardingThreshold());
        asyncAppender.setContext(this.loggerContext);
        asyncAppender.addAppender(logbackConfig.getAppender());
        asyncAppender.start();
    }
}
