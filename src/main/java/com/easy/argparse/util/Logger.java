package com.easy.argparse.util;

import java.util.Date;

public class Logger {
    private static final String LOG_LEVEL_KEY = "argParse.logLevel";
    
    private final MyLogger logger;
    
    public static Logger getLogger(Class<?> callingClass) {
        return new Logger(new MyLogger(callingClass));
    }
    
    private Logger(MyLogger logger) {
        this.logger = logger;
    }
    
    public void trace(String format, Object... args){
        logger.trace(format, args);
    }
    
    public void debug(String format, Object... args){
        logger.debug(format, args);
    }
    
    public void info(String format, Object... args){
        logger.info(format, args);
    }
    
    public void warn(String format, Object... args){
        logger.warn(format, args);
    }
    
    public void error(String format, Object... args){
        logger.error(format, args);
    }
    
    private enum LogLevel{
        trace,
        debug,
        info,
        warn,
        error
    }
    
    private static class MyLogger{
        private final String callingClassName;
        private LogLevel logLevel;
        
        MyLogger(Class<?> callingClass) {
            this.callingClassName = callingClass.getSimpleName();
            try{
                logLevel = LogLevel.valueOf(System.getProperty(LOG_LEVEL_KEY, "info"));
            }catch(Exception e){
                logLevel = LogLevel.info;
            }
        }
        
        void trace(String format, Object... args){
            if(logLevel.ordinal() <= LogLevel.trace.ordinal()){
                showMessage(LogLevel.trace, format, args);
            }
        }
        
        void debug(String format, Object... args){
            if(logLevel.ordinal() <= LogLevel.debug.ordinal()){
                showMessage(LogLevel.debug, format, args);
            }
        }
        
        void info(String format, Object... args){
            if(logLevel.ordinal() <= LogLevel.info.ordinal()){
                showMessage(LogLevel.info, format, args);
            }
        }
        
        void warn(String format, Object... args){
            if(logLevel.ordinal() <= LogLevel.warn.ordinal()){
                showMessage(LogLevel.warn, format, args);
            }
        }
        
        void error(String format, Object... args){
            if(logLevel.ordinal() <= LogLevel.error.ordinal()){
                showMessage(LogLevel.error, format, args);
            }
        }
        
        void showMessage(LogLevel logLevel, String format, Object... args){
            System.out.println("[" + logLevel + "|" + new Date() + "|" + callingClassName + "]>> " + String.format(
                    getFormatSpaceHolderReplaced(format), args));
        }
        
        String getFormatSpaceHolderReplaced(String format){
            return format.replaceAll("\\{\\}", "%s");
        }
    }
}