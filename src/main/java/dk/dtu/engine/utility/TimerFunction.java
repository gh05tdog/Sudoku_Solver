package dk.dtu.engine.utility;

import javax.swing.*;
import java.awt.*;
import java.util.TimerTask;
import java.util.Timer;

public class TimerFunction extends JPanel {
    private final JLabel timeLabel;
    private long startTime;
    private transient Timer timer;

    public TimerFunction() {
        setLayout(new FlowLayout(FlowLayout.CENTER));
        setPreferredSize(new Dimension(180, 80));
        timeLabel = new JLabel("00:00:00", SwingConstants.CENTER);
        timeLabel.setFont(new Font("SansSerif", Font.BOLD, 42));
        setBackground(Color.WHITE);
        add(timeLabel);
    }

    public void start() {
        startTime = System.currentTimeMillis();
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                updateTimer();
            }
        }, 0, 1000);
    }

    public void stop() {
        if (timer != null) {
            timer.cancel();
        }
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
}
