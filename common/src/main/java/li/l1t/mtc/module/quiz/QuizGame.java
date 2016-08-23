/*
 * Copyright (c) 2013-2016.
 * This work is protected by international copyright laws and licensed
 * under the license terms which can be found at src/main/resources/LICENSE.txt
 * or alternatively obtained by sending an email to xxyy98+mtclicense@gmail.com.
 */

package li.l1t.mtc.module.quiz;

import li.l1t.mtc.helper.MTCHelper;
import li.l1t.mtc.module.chat.globalmute.GlobalMuteModule;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Manages a quiz game.
 *
 * @author <a href="http://xxyy.github.io/">xxyy</a>
 * @since 14.11.14
 */
public class QuizGame {
    private final QuizModule module;
    private final GlobalMuteModule globalMuteModule;
    private QuizQuestion currentQuestion;
    private boolean wasGlobalMute = false; //Whether global mute was enabled when the quiz was started

    public QuizGame(QuizModule module, GlobalMuteModule globalMuteModule) {
        this.module = module;
        this.globalMuteModule = globalMuteModule;
    }

    public QuizQuestion getCurrentQuestion() {
        return currentQuestion;
    }

    /**
     * @return whether this game has an unanswered question
     */
    public boolean hasQuestion() {
        return getCurrentQuestion() != null;
    }

    public void setQuestion(QuizQuestion question) {
        Validate.isTrue(currentQuestion == null, "Cannot override question!");
        Bukkit.broadcastMessage(MTCHelper.locArgs("XU-qzquestion", "CONSOLE", false, question.getText()));
        currentQuestion = question;
        startGlobalMute();
    }

    private void startGlobalMute() {
        if (!isGlobalMuteSupported()) {
            return;
        }
        wasGlobalMute = globalMuteModule.isGlobalMute();
        if (wasGlobalMute) {
            globalMuteModule.disableGlobaleMute();
        }
    }

    private boolean isGlobalMuteSupported() {
        return globalMuteModule != null;
    }

    public void reset(Player winner) {
        Validate.isTrue(currentQuestion != null, "Cannot reset when not running!");
        module.getPlugin().getServer().broadcastMessage(MTCHelper.locArgs("XU-qzanswer", "CONSOLE", false,
                winner.getName(), getCurrentQuestion().getAnswer()));
        currentQuestion = null;
        resetGlobalMute();
    }

    private void resetGlobalMute() {
        if (isGlobalMuteSupported() && wasGlobalMute) {
            globalMuteModule.enableGlobalMute("WahrFalsch");
        }
    }

    public boolean abort(String reason) {
        Bukkit.broadcastMessage(MTCHelper.locArgs("XU-qzabort", "CONSOLE", false) +
                (reason == null ? "" : " §7Grund: " + reason));
        module.setGame(null);
        resetGlobalMute();
        return true;
    }
}