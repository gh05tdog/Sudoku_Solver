/* (C)2024 */
package dk.dtu.game.core;

import dk.dtu.engine.core.GameEngine;
import dk.dtu.engine.core.StartMenuWindowManager;
import dk.dtu.engine.core.WindowManager;
import dk.dtu.engine.graphics.GameRulePopup;
import dk.dtu.engine.utility.*;
import dk.dtu.engine.utility.Leaderboard;
import dk.dtu.engine.utility.Leaderboard.LeaderboardEntry;
import dk.dtu.game.core.solver.bruteforce.BruteForceAlgorithm;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.AbstractDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartMenu {

    private static final Logger logger = LoggerFactory.getLogger(StartMenu.class);
    private static final String FONT = "SansSerif";
    private final StartMenuWindowManager startMenuWindowManager;
    private final JToggleButton startButton = new JToggleButton("Start Game");
    private final JToggleButton easyButton = new JToggleButton("Easy");
    private final JToggleButton mediumButton = new JToggleButton("Medium", true);
    private final JToggleButton hardButton = new JToggleButton("Hard");
    private final JToggleButton extremeButton = new JToggleButton("Extreme");

    private final JButton gameRuleButton = new JButton("Game Rules");

    private final CustomBoardPanel twoByTwo = new CustomBoardPanel();
    private final CustomBoardPanel threeByThree = new CustomBoardPanel();
    private final CustomBoardPanel fourByFour = new CustomBoardPanel();
    private final CustomBoardPanel customBoardPanel = new CustomBoardPanel();
    private final JButton createGameButton = new JButton("Create Game");
    private final JButton joinGameButton = new JButton("Join Game");
    private final JTextField inputNField = new JTextField("N", 1);
    private final JTextField inputKField = new JTextField("K", 1);
    private final ButtonGroup difficultyGroup = new ButtonGroup();
    private final CustomComponentGroup sizeGroup = new CustomComponentGroup();
    private final int[][] boardConfigs = {{2, 2}, {3, 3}, {4, 4}, {3, 3}};

    public StartMenu(StartMenuWindowManager startMenuWindowManager) {
        this.startMenuWindowManager = startMenuWindowManager;
        startMenuWindowManager.display();
    }

    public void startGame() throws Board.BoardNotCreatable {
        logConfigInfo();

        int n = Config.getN();
        int k = Config.getK();
        int cellSize = Config.getCellSize();
        WindowManager windowManager =
                new WindowManager(startMenuWindowManager.getFrame(), 1000, 1000);
        try {
            GameEngine gameEngine = new GameEngine(windowManager, n, k, cellSize);
            windowManager.display();
            gameEngine.start();

        } catch (Board.BoardNotCreatable boardNotCreatable) {
            throw new Board.BoardNotCreatable("This board is not possible to create");
        }
    }

    public void updateCustomBoardPanel(int n, int k) {
        if ((k * n) <= (n * n)) {
            customBoardPanel.updateBoard(n, k); // Update the board based on n and k
        } else {
            customBoardPanel.setBackground(Color.WHITE);
        }
    }

    public void initialize() {
        // Initialize the start menu with all the buttons, and set the default to 3x3 board with
        // medium difficulty
        addSizePanelButtons();
        addDifficultyPanelButtons();
        addButtonPanelButtons();
        addInputPanelButtons();
        addImportButton();
        addLeaderboardButton();
        updateCustomBoardPanel(2, 2);
        addNetworkGameButtons();
        addGameruleButton();

        threeByThree.updateBackgroundColor(Color.GRAY);
        Config.setK(3);
        Config.setN(3);
        Config.setCellSize(550 / (Config.getK() * Config.getN()));

        addChangeListenerToField(inputNField);
        addChangeListenerToField(inputKField);
    }

    private void addNetworkGameButtons() {
        createGameButton.setBounds(5, 400 - 90, 190, 40); // Adjust the size and position as needed
        createGameButton.setBackground(Color.WHITE);
        createGameButton.setFocusPainted(false);
        createGameButton.addActionListener(this::onCreateGame);

        joinGameButton.setBounds(5, 400 - 45, 190, 40); // Adjust the size and position as needed
        joinGameButton.setBackground(Color.WHITE);
        joinGameButton.setFocusPainted(false);
        joinGameButton.addActionListener(this::onJoinGame);

        startMenuWindowManager.addComponent(
                createGameButton, startMenuWindowManager.getButtonPanel());
        startMenuWindowManager.addComponent(
                joinGameButton, startMenuWindowManager.getButtonPanel());
    }

    private void onCreateGame(ActionEvent e) {
        joinGameButton.setEnabled(false);
        createGameButton.setEnabled(false);

        // Start the server in a new thread
        new Thread(() -> {
            GameServer server = new GameServer();
            server.start();
        }).start();

        try {
            // Get the local IP address of the server
            String localIpAddress = InetAddress.getLocalHost().getHostAddress();
            System.out.println("Server started on IP: " + localIpAddress);

            // Connect the client to the newly started server
            connectClient(localIpAddress);

            JOptionPane.showMessageDialog(
                    null,
                    "Server started on IP: " + localIpAddress + ". Waiting for clients to join.",
                    "Server Started",
                    JOptionPane.INFORMATION_MESSAGE);
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    null,
                    "Failed to determine the server's IP address.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            joinGameButton.setEnabled(true);
            createGameButton.setEnabled(true);
        }
    }

    private void onJoinGame(ActionEvent e) {
        joinGameButton.setEnabled(false);
        createGameButton.setEnabled(false);

        // Create a GameClient to discover servers
        GameClient client = new GameClient(new WindowManager(startMenuWindowManager.getFrame(), 1000, 1000));
        client.discoverServers();

        // Wait for a short time to allow server discovery
        try {
            Thread.sleep(1000); // Wait for 1 second
        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }

        List<String> discoveredServers = client.getDiscoveredServers();
        boolean connected = false;
        for (String serverAddress : discoveredServers) {
            System.out.println("Attempting to connect to server at IP: " + serverAddress);
            if (client.testGameConnection(serverAddress)) {
                System.out.println("Connected to server at IP: " + serverAddress);
                connectClient(serverAddress);
                connected = true;
                break;
            }
        }
        if (!connected) {
            JOptionPane.showMessageDialog(
                    null,
                    "Failed to connect to any server. Please check the server addresses and try again.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);

            joinGameButton.setEnabled(true);
            createGameButton.setEnabled(true);
        }

    }

    private void connectClient(String serverAddress) {
        new Thread(() -> {
            WindowManager windowManager = new WindowManager(startMenuWindowManager.getFrame(), 1000, 1000);
            GameClient client = new GameClient(windowManager);
            try {
                client.start(serverAddress);
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(
                            null,
                            "Connected to server at " + serverAddress,
                            "Connected",
                            JOptionPane.INFORMATION_MESSAGE);
                });
            } catch (IOException | Board.BoardNotCreatable ex) {
                ex.printStackTrace();
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(
                            null,
                            "Failed to start the game with the server.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    joinGameButton.setEnabled(true);
                    createGameButton.setEnabled(true);
                });
            }
        }).start();
    }

    private void addLeaderboardButton() {
        JButton leaderboardButton = new JButton("Show Leaderboard");
        leaderboardButton.addActionListener(this::onShowLeaderboard);
        leaderboardButton.setBounds(5, 180, 190, 40); // Adjust the size and position as needed
        leaderboardButton.setBackground(Color.WHITE);
        leaderboardButton.setFocusPainted(false);
        startMenuWindowManager.addComponent(
                leaderboardButton, startMenuWindowManager.getButtonPanel());
    }

    private void onShowLeaderboard(ActionEvent e) {
        // Column names for the leaderboard table
        String[] columnNames = {"Username", "Difficulty", "Time", "Timestamp"};

        // Fetch leaderboard data
        List<LeaderboardEntry> leaderboard = Leaderboard.loadLeaderboard("jdbc:sqlite:sudoku.db");
        List<String[]> rowData = new ArrayList<>();
        for (LeaderboardEntry entry : leaderboard) {
            rowData.add(
                    new String[] {
                        entry.username(),
                        entry.difficulty(),
                        String.format(
                                "%02d:%02d:%02d",
                                entry.time() / 3600, (entry.time() % 3600) / 60, entry.time() % 60),
                        entry.timestamp()
                    });
        }

        // Create a non-editable table model
        DefaultTableModel model =
                new DefaultTableModel(rowData.toArray(new String[0][0]), columnNames) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };

        // Create a JTable with the leaderboard data
        JTable leaderboardTable = new JTable(model);
        leaderboardTable.setFillsViewportHeight(true);
        leaderboardTable.setRowHeight(30);
        leaderboardTable.getTableHeader().setFont(new Font(FONT, Font.BOLD, 14));
        leaderboardTable.setFont(new Font(FONT, Font.PLAIN, 12));
        leaderboardTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Center align the table cell contents
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < leaderboardTable.getColumnCount(); i++) {
            leaderboardTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        // Enable sorting
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        leaderboardTable.setRowSorter(sorter);

        // Create a JScrollPane containing the JTable
        JScrollPane leaderboardScrollPane = new JScrollPane(leaderboardTable);

        // Create a JDialog to display the leaderboard
        JDialog leaderboardDialog = new JDialog();
        leaderboardDialog.setTitle("Leaderboard");
        leaderboardDialog.setSize(600, 400);
        leaderboardDialog.setLocationRelativeTo(null);
        leaderboardDialog.add(leaderboardScrollPane);
        leaderboardDialog.setVisible(true);
    }


    private void addChangeListenerToField(JTextField field) {
        // This method adds a document listener to the input fields, so that the board is updated
        // when the user changes the values
        field.getDocument().addDocumentListener(
                new DocumentListener() {
                    public void changedUpdate(DocumentEvent e) {
                        updateBoard();
                    }

                    public void removeUpdate(DocumentEvent e) {
                        updateBoard();
                    }

                    public void insertUpdate(DocumentEvent e) {
                        updateBoard();
                    }

                    // Method to parse the n and k values and update the custom board panel
                    private void updateBoard() {
                        try {
                            int n = Integer.parseInt(inputNField.getText().trim());
                            int k = Integer.parseInt(inputKField.getText().trim());
                            if (n * k <= n * n) {
                                Config.setN(n);
                                Config.setK(k);
                                updateCustomBoardPanel(n, k);
                                boardConfigs[3] = new int[]{n, k};
                            }
                        } catch (NumberFormatException ex) {
                            // Handle the case where one of the fields is empty or does not contain a valid integer
                            logger.error("Invalid input: {}", ex.getMessage());
                        }
                    }
                });
    }


    private void addInputPanelButtons() {
        // This method adds the N and K fields for the custom board
        Font fieldFont = new Font(FONT, Font.BOLD, 20);

        JTextField[] fields = {inputKField, inputNField};
        String[] initialTexts = {"K", "N"};
        for (int i = 0; i < fields.length; i++) {
            JTextField field = fields[i];
            field.setFont(fieldFont);
            field.setHorizontalAlignment(SwingConstants.CENTER);
            ((AbstractDocument) field.getDocument()).setDocumentFilter(new NumberDocumentFilter());

            String initialText = initialTexts[i];
            field.addFocusListener(
                    new FocusAdapter() {
                        @Override
                        public void focusGained(FocusEvent e) {
                            if (field.getText().trim().equalsIgnoreCase(initialText)) {
                                field.setText("");
                            }
                        }
                    });

            field.setBounds(i == 0 ? 5 : 85, 5, 50, 40);
            startMenuWindowManager.addComponent(field, startMenuWindowManager.getInputPanel());
        }
    }

    private void addGameruleButton() {
        gameRuleButton.setBounds(5, 5, 150, 40); // Set bounds appropriately if needed
        gameRuleButton.setBackground(Color.WHITE);
        gameRuleButton.setFocusPainted(false);
        gameRuleButton.addMouseListener(
                new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent evt) {
                        gameRuleButton.setBackground(Color.LIGHT_GRAY);
                    }

                    public void mouseExited(java.awt.event.MouseEvent evt) {
                        gameRuleButton.setBackground(Color.WHITE);
                    }
                });
        startMenuWindowManager.addComponent(
                gameRuleButton, startMenuWindowManager.getGameRulePanel());

        gameRuleButton.addActionListener(
                e -> {
                    GameRulePopup gameRules = new GameRulePopup();
                    gameRules.setVisible(true);
                    gameRules.addJSwitchBox(
                            "Enable lives", Config.getEnableLives(), Config::setEnableLives);
                    gameRules.addJSwitchBox(
                            "Enable timer", Config.getEnableTimer(), Config::setEnableTimer);
                    gameRules.addJSwitchBox(
                            "Enable easy mode",
                            Config.getEnableEasyMode(),
                            Config::setEnableEasyMode);
                    gameRules.addJSwitchBox(
                            "Killer Sudoku Mode",
                            Config.getEnableKillerSudoku(),
                            Config::setEnableKillerSudoku);
                });
    }

    private void addButtonPanelButtons() {
        // This method adds the start button and in the future different buttons
        startButton.setBounds(5, 5, 190, 40);
        startButton.setBackground(Color.WHITE);
        startButton.setFocusPainted(false);
        startButton.addItemListener(
                e -> {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        startButton.setBackground(Color.GRAY);

                        try {
                            startGame();
                        } catch (Board.BoardNotCreatable ex) {
                            logger.error("This board-type is not creatable: {}", ex.getMessage());
                        }

                    } else {
                        startButton.setBackground(Color.WHITE);
                    }
                });
        startMenuWindowManager.addComponent(startButton, startMenuWindowManager.getButtonPanel());
    }

    private void addImportButton() {
        JButton importButton = new JButton("Import Sudoku");
        importButton.addActionListener(this::onImportSudoku);
        importButton.setBounds(5, 90, 190, 40); // Set bounds appropriately if needed
        importButton.setBackground(Color.WHITE);
        importButton.setFocusPainted(false);
        startMenuWindowManager.addComponent(importButton, startMenuWindowManager.getButtonPanel());
    }

    private void onImportSudoku(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a Sudoku Puzzle File");
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                List<String> lines = Files.readAllLines(selectedFile.toPath());
                int[][] customBoard = importSudokuFromFile(lines);
                startGameWithBoard(customBoard); // Start game with custom board
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        null,
                        "Failed to load the Sudoku file: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public int[][] importSudokuFromFile(List<String> lines)
            throws IOException, Board.BoardNotCreatable {
        if (lines.isEmpty()) {
            throw new IOException("File is empty");
        }

        String[] firstLine = lines.getFirst().split(";");
        if (firstLine.length < 2) {
            throw new IOException("First line format should be 'k;n'");
        }

        int k = Integer.parseInt(firstLine[0].trim());
        Config.setK(k);
        int n = Integer.parseInt(firstLine[1].trim());
        Config.setN(n);

        int[][] board = new int[k * n][k * n];
        if (lines.size() != k * n + 1) {
            throw new IOException(
                    "The number of lines in the file does not match the expected size of k^2 + 1");
        }

        for (int i = 1; i < lines.size(); i++) {
            String[] row = lines.get(i).split(";");
            if (row.length != k * n) {
                throw new IOException(
                        "Row " + i + " does not contain the correct number of elements");
            }
            for (int j = 0; j < row.length; j++) {
                board[i - 1][j] = row[j].equals(".") ? 0 : Integer.parseInt(row[j].trim());
            }
        }

        // Check if the board is valid
        if (BruteForceAlgorithm.isValidSudoku(board)) {
            return board;
        } else {
            throw new Board.BoardNotCreatable("This board is not possible to create");
        }
    }

    private void startGameWithBoard(int[][] board) {
        Config.setCellSize(550 / (Config.getK() * Config.getN()));
        logConfigInfo();

        WindowManager windowManager =
                new WindowManager(startMenuWindowManager.getFrame(), 1000, 1000);
        try {

            GameEngine gameEngine = new GameEngine(
                            windowManager, Config.getN(), Config.getK(), Config.getCellSize());
            windowManager.display();
            gameEngine.startCustom(board); // Pass the custom board to the game engine
        } catch (Board.BoardNotCreatable boardNotCreatable) {
            logger.error("Board not creatable: {}", boardNotCreatable.getMessage());
        }
    }

    private void addSizePanelButtons() {
        // This function adds the small boards for selecting size in game
        CustomBoardPanel[] boardPanels = {twoByTwo, threeByThree, fourByFour, customBoardPanel};

        MouseAdapter mouseAdapter =
                new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent evt) {
                        CustomBoardPanel source = (CustomBoardPanel) evt.getSource();
                        for (int i = 0; i < boardPanels.length; i++) {
                            if (source == boardPanels[i]) {
                                int n = boardConfigs[i][0];
                                int k = boardConfigs[i][1];
                                Config.setN(n);
                                Config.setK(k);
                                Config.setCellSize(550 / (n * k));
                                break;
                            }
                        }
                    }
                };

        int xPosition = 5;
        for (int i = 0; i < boardPanels.length; i++) {
            int n = boardConfigs[i][0];
            int k = boardConfigs[i][1];

            CustomBoardPanel panel = boardPanels[i];
            panel.updateBoard(n, k); // Initialize with the correct board configuration

            panel.addMouseListener(mouseAdapter); // Add the mouse listener

            panel.setBounds(xPosition, 5, 150, 150);
            xPosition += 150 + 5; // Increment for the next panel

            startMenuWindowManager.addComponent(
                    panel,
                    startMenuWindowManager.getSizePanel()); // Add the panel to the size panel
            sizeGroup.addComponent(panel); // Add the panel to the custom component group
        }
    }

    private void addDifficultyPanelButtons() {
        // This method adds the difficulty buttons to the start menu
        Config.setDifficulty("medium");
        difficultyGroup.add(easyButton);
        difficultyGroup.add(mediumButton);
        difficultyGroup.add(hardButton);
        difficultyGroup.add(extremeButton);

        easyButton.setBackground(Color.WHITE);
        mediumButton.setBackground(Color.WHITE);
        hardButton.setBackground(Color.WHITE);
        extremeButton.setBackground(Color.WHITE);
        easyButton.setFocusPainted(false);
        mediumButton.setFocusPainted(false);
        hardButton.setFocusPainted(false);
        extremeButton.setFocusPainted(false);

        easyButton.addItemListener(
                e -> {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        easyButton.setBackground(Color.GRAY);
                        Config.setDifficulty("easy");
                    } else {
                        easyButton.setBackground(Color.WHITE);
                    }
                });
        mediumButton.addItemListener(
                e -> {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        mediumButton.setBackground(Color.GRAY);
                        Config.setDifficulty("medium");
                    } else {
                        mediumButton.setBackground(Color.WHITE);
                    }
                });
        hardButton.addItemListener(
                e -> {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        hardButton.setBackground(Color.GRAY);
                        Config.setDifficulty("hard");
                    } else {
                        hardButton.setBackground(Color.WHITE);
                    }
                });
        extremeButton.addItemListener(
                e -> {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        extremeButton.setBackground(Color.GRAY);
                        Config.setDifficulty("extreme");
                    } else {
                        extremeButton.setBackground(Color.WHITE);
                    }
                });

        easyButton.setBounds(5, 5, twoByTwo.getWidth(), 40);
        mediumButton.setBounds(10 + easyButton.getWidth(), 5, twoByTwo.getWidth(), 40);
        hardButton.setBounds(
                15 + easyButton.getWidth() + mediumButton.getWidth(), 5, twoByTwo.getWidth(), 40);
        extremeButton.setBounds(
                20 + easyButton.getWidth() + mediumButton.getWidth() + hardButton.getWidth(),
                5,
                twoByTwo.getWidth(),
                40);

        startMenuWindowManager.addComponent(
                easyButton, startMenuWindowManager.getDifficultyPanel());
        startMenuWindowManager.addComponent(
                mediumButton, startMenuWindowManager.getDifficultyPanel());
        startMenuWindowManager.addComponent(
                hardButton, startMenuWindowManager.getDifficultyPanel());
        startMenuWindowManager.addComponent(
                extremeButton, startMenuWindowManager.getDifficultyPanel());
    }

    private void logConfigInfo(){
        logger.info(
                "startGame: {} {} {} {}",
                Config.getK(),
                Config.getN(),
                Config.getDifficulty(),
                Config.getCellSize());
    }

    // Getters used for testing the startMenu
    public CustomBoardPanel getCustomBoardPanel() {
        return customBoardPanel;
    }

    public JToggleButton getStartButton() {
        return startButton;
    }

    public JTextField getInputNField() {
        return inputNField;
    }

    public JTextField getInputKField() {
        return inputKField;
    }

    public JToggleButton getMediumButton() {
        return mediumButton;
    }

    public JToggleButton getEasyButton() {
        return easyButton;
    }

    public JToggleButton getExtremeButton() {
        return extremeButton;
    }

    public JToggleButton getHardButton() {
        return hardButton;
    }

    public CustomBoardPanel getFourByFour() {
        return fourByFour;
    }
}
