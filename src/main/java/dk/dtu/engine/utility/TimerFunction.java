/* (C)2024 */
package dk.dtu.engine.utility;

import dk.dtu.game.core.Config;
import java.awt.*;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.*;

/**
 * This class is responsible for managing the timer function in the game.
 * It sets up the timer and updates the time every second.
 */
public class TimerFunction extends JPanel {
    private final JLabel timeLabel;
    private long startTime;
    private transient Timer timer;

    private static final Color darkbackgroundColor = new Color(64, 64, 64);
    private static final Color lightaccentColor = new Color(237, 224, 186);
    private static Color accentColor = Config.getDarkMode() ? lightaccentColor : Color.BLACK;
    private static Color backgroundColor = Config.getDarkMode() ? darkbackgroundColor : Color.WHITE;

    public TimerFunction() {
        setLayout(new FlowLayout(FlowLayout.CENTER));
        setPreferredSize(new Dimension(180, 80));
        timeLabel = new JLabel("00:00:00", SwingConstants.CENTER);
        timeLabel.setFont(new Font("SansSerif", Font.BOLD, 42));
        accentColor = Config.getDarkMode() ? accentColor : Color.BLACK;
        backgroundColor = Config.getDarkMode() ? backgroundColor : Color.WHITE;
        timeLabel.setForeground(accentColor);
        setBackground(backgroundColor);
        add(timeLabel);
        repaint();
    }

    public void start() {
        startTime = System.currentTimeMillis();
        timer = new Timer();
        timer.scheduleAtFixedRate(
                new TimerTask() {
                    @Override
                    public void run() {
                        updateTimer();
                    }
                },
                0,
                1000);
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
        }
    }

    public void setVisibility(boolean visible) {
        setVisible(visible);
    }

    public void reset() {
        startTime = System.currentTimeMillis();
    }

    private void updateTimer() {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - startTime;
        int hours = (int) (elapsedTime / 3600000);
        int minutes = (int) (elapsedTime % 3600000) / 60000;
        int seconds = (int) (elapsedTime % 60000) / 1000;
        String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        timeLabel.setText(timeString);
    }

    public String getTimeString() {
        return timeLabel.getText();
    }

    // Convert the timestring to seconds
    public int getTimeToInt() {
        String[] time = timeLabel.getText().split(":");
        int hours = Integer.parseInt(time[0]);
        int minutes = Integer.parseInt(time[1]);
        int seconds = Integer.parseInt(time[2]);
        return hours * 3600 + minutes * 60 + seconds;
    }

    public void setTime(int time) {
        int hours = time / 3600;
        int minutes = (time % 3600) / 60;
        int seconds = time % 60;
        String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        timeLabel.setText(timeString);
    }

    public void startWithTime(int time) {
        startTime = System.currentTimeMillis() - time * 1000L;
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateTimer();
            }
        }, 0, 1000);
    }

    public void update() {
        accentColor = Config.getDarkMode() ? lightaccentColor : Color.BLACK;
        backgroundColor = Config.getDarkMode() ? darkbackgroundColor : Color.WHITE;
        timeLabel.setForeground(accentColor);
        setBackground(backgroundColor);
        repaint();
    }
}
