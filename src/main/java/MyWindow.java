import java.awt.*;
import java.awt.image.BufferStrategy;

public class MyWindow extends Canvas implements Runnable {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final String TITLE = "Sudoku Solver";

    public MyWindow() {
        // Set the size of the canvas
        setPreferredSize(new Dimension(WIDTH, HEIGHT));

        // Initialize the frame
        Frame frame = new Frame(TITLE);
        frame.add(this);
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                System.exit(0);
            }
        });
    }

    @Override
    public void run() {
        // Main loop for your GUI application
        while (true) {
            render();
            try {
                Thread.sleep(16); // Roughly 60 FPS
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void render() {
        BufferStrategy bs = this.getBufferStrategy();
        if (bs == null) {
            this.createBufferStrategy(100);
            return;
        }
        Graphics g = bs.getDrawGraphics();

        // Clear the screen
        g.clearRect(0, 0, WIDTH, HEIGHT);


        g.dispose();
        bs.show();
    }

    public static void main(String[] args) {
        MyWindow window = new MyWindow();
        window.setBackground(Color.black);
        new Thread(window).start();
    }
}
