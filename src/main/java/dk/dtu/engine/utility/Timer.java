package dk.dtu.engine.utility;

import javax.swing.*;
import java.awt.*;

public class Timer extends JPanel implements Runnable {
    private long startTime;
    private boolean running = false;
    private JLabel timeLabel;

    public Timer() {
        setLayout(new FlowLayout(FlowLayout.CENTER));
        setPreferredSize(new Dimension(180, 80));
        timeLabel = new JLabel("00:00:00", JLabel.CENTER);
        timeLabel.setFont(new Font("SansSerif", Font.BOLD, 42));
        setBackground(Color.WHITE);
        add(timeLabel);
    }

    public void start() {
        startTime = System.currentTimeMillis();
        running = true;
        Thread timerThread = new Thread(this);
        timerThread.start();
    }

    public void stop() {
        running = false;
    }

    public void reset() {
        startTime = System.currentTimeMillis();
    }

    @Override
    public void run() {
        while (running) {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - startTime;
            int hours = (int) (elapsedTime / 3600000);
            int minutes = (int) (elapsedTime % 3600000) / 60000;
            int seconds = (int) (elapsedTime % 60000) / 1000;
            String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            timeLabel.setText(timeString);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public String getTimeString() {
        return timeLabel.getText();
    }
}
