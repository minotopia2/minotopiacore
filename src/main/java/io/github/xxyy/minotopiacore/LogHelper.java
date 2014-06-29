package io.github.xxyy.minotopiacore;

import io.github.xxyy.common.log.XYCFormatter;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;


public final class LogHelper {
    private LogHelper() {

    }

    private static final Logger mainLogger = Logger.getLogger("MTC");
    private static final Logger cmdLogger = Logger.getLogger("MTC.CMD");
    private static final Logger badCmdLogger = Logger.getLogger("MTC.BCMD");
    private static final Logger chatLogger = Logger.getLogger("MTC.CHAT");
    private static final Logger clanChatLogger = Logger.getLogger("MTC.CCHAT");
    private static final Logger privChatLogger = Logger.getLogger("MTC.PCHAT");
    private static final Logger banLogger = Logger.getLogger("MTC.BANS");
    private static final Logger warnLogger = Logger.getLogger("MTC.WARNS");
    private static final Logger fullLogger = Logger.getLogger("MTC.FULLS");
    public static void flushAll(){
        LogHelper.flush(LogHelper.mainLogger);
        LogHelper.flush(LogHelper.cmdLogger);
        LogHelper.flush(LogHelper.badCmdLogger);
        LogHelper.flush(LogHelper.chatLogger);
        LogHelper.flush(LogHelper.clanChatLogger);
        LogHelper.flush(LogHelper.privChatLogger);
        LogHelper.flush(LogHelper.banLogger);
        LogHelper.flush(LogHelper.warnLogger);
        LogHelper.flush(LogHelper.fullLogger);
    }

    public static Logger getBadCmdLogger() {
        return LogHelper.badCmdLogger;
    }

    public static Logger getBanLogger() {
        return LogHelper.banLogger;
    }

    public static Logger getChatLogger() {
        return LogHelper.chatLogger;
    }

    public static Logger getClanChatLogger() {
        return LogHelper.clanChatLogger;
    }

    public static Logger getCmdLogger() {
        return LogHelper.cmdLogger;
    }

    public static Logger getFullLogger() {
        return LogHelper.fullLogger;
    }

    public static Logger getMainLogger() {
        return LogHelper.mainLogger;
    }

    public static Logger getPrivChatLogger() {
        return LogHelper.privChatLogger;
    }

    public static Logger getWarnLogger() {
        return LogHelper.warnLogger;
    }
    
    public static void initLogs(){
        new File(MTC.instance().getDataFolder()+"/logs/").mkdirs();
        if(ConfigHelper.isMainLoggerEnabled()){
            try {
                FileHandler hdlr = new FileHandler(MTC.instance().getDataFolder()+"/logs/main.log",10000000, 5, true);
                hdlr.setLevel(Level.FINEST);
                hdlr.setFormatter(new XYCFormatter(MTC.instance(), "Main Log.", true));
                hdlr.setEncoding("UTF-8");
                FileHandler hdlr2 = new FileHandler(MTC.instance().getDataFolder()+"/logs/error.log",10000000, 5, true);
                hdlr2.setLevel(Level.WARNING);
                hdlr2.setFormatter(new XYCFormatter(MTC.instance(), "Main Log.", true));
                hdlr2.setEncoding("UTF-8");
//                LogHelper.mainLogger.removeHandler(LogHelper.mainLogger.getHandlers()[0]);
                LogHelper.mainLogger.addHandler(hdlr);
                LogHelper.mainLogger.addHandler(hdlr2);
                LogHelper.mainLogger.setLevel(Level.FINEST);
            } catch (SecurityException | IOException e) {
                e.printStackTrace();
                System.out.println(">>MTC exception when tryin to initialize cmd loggerz.");
            }
        }
        if(MTC.instance().getConfig().getBoolean("enable.log.cmds",false)){
            LogHelper.tryInitLogger(LogHelper.cmdLogger, MTC.instance().getDataFolder()+"/logs/commands.log","CmdLogger",true);
            LogHelper.tryInitLogger(LogHelper.badCmdLogger,MTC.instance().getDataFolder()+"/logs/badcmds.log","BadCmdLogger",true);
        }
        if(MTC.instance().getConfig().getBoolean("enable.log.chat",true)){
            LogHelper.tryInitLogger(LogHelper.chatLogger,MTC.instance().getDataFolder()+"/logs/chat.log","ChatLogger",true);
            LogHelper.tryInitLogger(LogHelper.clanChatLogger,MTC.instance().getDataFolder()+"/logs/clanchat.log","ClanChatLogger",true);
            LogHelper.tryInitLogger(LogHelper.privChatLogger,MTC.instance().getDataFolder()+"/logs/privatechats.log","PrivChatLogger",true);
        }
        if(MTC.instance().getConfig().getBoolean("enable.log.bansNwarns",true)){
            LogHelper.tryInitLogger(LogHelper.warnLogger,MTC.instance().getDataFolder()+"/logs/warns.log","WarnLogger",true);
            LogHelper.tryInitLogger(LogHelper.banLogger,MTC.instance().getDataFolder()+"/logs/bans.log","BanLogger",true);
        }
        if(ConfigHelper.isFullLogEnabled()){
            LogHelper.tryInitLogger(LogHelper.fullLogger, MTC.instance().getDataFolder()+"/logs/fulls.log","FullLogger",true);
        }
        LogHelper.mainLogger.setUseParentHandlers(false);
        LogHelper.cmdLogger.setUseParentHandlers(false);
        LogHelper.badCmdLogger.setUseParentHandlers(false);
        LogHelper.chatLogger.setUseParentHandlers(false);
        LogHelper.clanChatLogger.setUseParentHandlers(false);
        LogHelper.privChatLogger.setUseParentHandlers(false);
        LogHelper.banLogger.setUseParentHandlers(false);
        LogHelper.warnLogger.setUseParentHandlers(false);
        LogHelper.fullLogger.setUseParentHandlers(false);
    }
    
    private static void flush(Logger lgr){
        for(Handler hdlr : lgr.getHandlers()){
            hdlr.flush();
        }
    }
    
    private static void initLogger(Logger lgr, String fileName, String loggerName, boolean setParent) throws Exception{
        FileHandler hdlr = new FileHandler(fileName);
        hdlr.setLevel(Level.FINEST);
        hdlr.setFormatter(new XYCFormatter(MTC.instance(), loggerName, false));
        hdlr.setEncoding("UTF-8");
//        lgr.removeHandler(lgr.getHandlers()[0]);
        lgr.addHandler(hdlr);
        lgr.setLevel(Level.FINEST);
        if(setParent) {
            lgr.setParent(LogHelper.mainLogger);
        }
    }
    
    private static void tryInitLogger(Logger lgr, String fileName, String loggerName, boolean setParent){
        try{
            LogHelper.initLogger(lgr,fileName, loggerName, setParent);
        }catch(Exception e){
            e.printStackTrace();
            System.out.println(">>MTC exception when tryin to initialize "+loggerName+".");
        }
    }
}
