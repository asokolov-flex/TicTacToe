package com.xowin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL;

public class SoundManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(SoundManager.class);

    private static void playSound(String soundFile) {
        try {
            URL url = Thread.currentThread().getContextClassLoader().getResource(soundFile);

            if (url != null) {
                AudioInputStream audioIn = AudioSystem.getAudioInputStream(url);
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                clip.start();
            }
        } catch (Exception e) {
            LOGGER.error("Failed to play sound '{}': {}", soundFile, e.getMessage());
        }
    }

    public static void playCollisionSound() {
        playSound("collision.wav");
    }

    public static void playScoreSound() {
        playSound("count.wav");
    }

    public static void playLoseSound() {
        playSound("lose.wav");
    }
}
