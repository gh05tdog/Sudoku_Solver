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
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import javax.swing.*;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.AbstractDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the start menu of the game. This is where you can choose difficulty, size, load game, show leader board etc.
 * It is also where you can choose the game rules.
 */
public class StartMenu {

    private static final String ERROR = "Error";
    private static final Logger logger = LoggerFactory.getLogger(StartMenu.class);
    private static final String FONT = "SansSerif";
    private final StartMenuWindowManager startMenuWindowManager;
    private final JToggleButton startButton = new JToggleButton("Start Game");

    private boolean isLoadGameDialogOpen = false;
    private final JButton gameRuleButton = new JButton("Game Rules");

    private final CustomBoardPanel twoByTwo = new CustomBoardPanel();
    private final CustomBoardPanel threeByThree = new CustomBoardPanel();
    private final CustomBoardPanel fourByFour = new CustomBoardPanel();
    private final CustomBoardPanel customBoardPanel = new CustomBoardPanel();
    private final JButton createGameButton = new JButton("Create Game");
    private final JButton joinGameButton = new JButton("Join Game");
    private final JButton importButton = new JButton("Import Sudoku");
    private final JButton leaderboardButton = new JButton("Show Leaderboard");
    private final JTextField inputNField = new JTextField("N", 1);
    private final JTextField inputKField = new JTextField("K", 1);
    private final CustomComponentGroup sizeGroup = new CustomComponentGroup();
    private final int[][] boardConfigs = {{2, 2}, {3, 3}, {4, 4}, {3, 3}};
    CustomBoardPanel[] boardPanels = {twoByTwo, threeByThree, fourByFour, customBoardPanel};

    private static final Color darkbackgroundColor = new Color(64, 64, 64);
    private static final Color lightaccentColor = new Color(237, 224, 186);
    private static Color accentColor = Config.getDarkMode() ? lightaccentColor : Color.BLACK;
    private static Color backgroundColor = Config.getDarkMode() ? darkbackgroundColor : Color.WHITE;
    private final JComboBox<String> difficultyDropdown =
            new JComboBox<>(new String[] {"Easy", "Medium", "Hard", "Extreme"});
    GameRulePopup gameRules;

    private final JButton loadGameButton = new JButton("Load Game");

    public StartMenu(StartMenuWindowManager startMenuWindowManager) {
        this.startMenuWindowManager = startMenuWindowManager;
        setBackgroundColor(Config.getDarkMode() ? darkbackgroundColor : Color.WHITE);
        setAccentColor(Config.getDarkMode() ? lightaccentColor : Color.BLACK);
        startMenuWindowManager.display();

        if (!GraphicsEnvironment.isHeadless()) {
            gameRules = new GameRulePopup(this);
            initializeGameRulePopup();
        }
    }

    private static void setAccentColor(Color color) {
        accentColor = color;
    }

    private static void setBackgroundColor(Color color) {
        backgroundColor = color;
    }

    // Initializes the window-manager and game engine with the given parameters for board size
    public void startGame() throws Board.BoardNotCreatable {
        logConfigInfo();

        int n = Config.getN();
        int k = Config.getK();
        int cellSize = Config.getCellSize();
        WindowManager windowManager =
                new WindowManager(startMenuWindowManager.getFrame(), 1000, 850);
        try {
            GameEngine gameEngine = new GameEngine(windowManager, n, k, cellSize);
            windowManager.updateBoard();
            windowManager.display();
            gameEngine.start();
            windowManager.display();

        } catch (Board.BoardNotCreatable boardNotCreatable) {
            throw new Board.BoardNotCreatable("This board is not possible to create");
        }
    }

    // The custom board panel is the panel that shows the size, with the dynamically drawn board
    public void updateCustomBoardPanel(int n, int k) {
        if ((k * n) <= (n * n)) {
            customBoardPanel.updateBoard(n, k); // Update the board based on n and k
        } else {
            customBoardPanel.setBackground(Config.getDarkMode() ? backgroundColor : Color.WHITE);
        }
    }

    // Main initializer calling all the lesser initializers
    public void initialize() {
        // Initialize the start menu with all the buttons
        addSizePanelButtons();
        addButtonPanelButtons();
        addInputPanelButtons();
        addImportButton();
        addLeaderboardButton();
        updateCustomBoardPanel(2, 2);
        addNetworkGameButtons();
        addLoadGameButton();

        threeByThree.updateBackgroundColor(Color.GRAY);
        Config.setK(3);
        Config.setN(3);
        Config.setCellSize(550 / (Config.getK() * Config.getN()));

        addChangeListenerToField(inputNField);
        addChangeListenerToField(inputKField);
    }

    private void addLoadGameButton() {
        loadGameButton.setBounds(5, 165, 190, 40);
        loadGameButton.setBackground(backgroundColor);
        loadGameButton.setForeground(accentColor);
        loadGameButton.setBorder(new LineBorder(accentColor));
        loadGameButton.setFocusPainted(false);
        loadGameButton.addActionListener(this::onLoadGame);
        startMenuWindowManager.addComponent(
                loadGameButton, startMenuWindowManager.getButtonPanel());
    }

    private void addNetworkGameButtons() {
        createGameButton.setBounds(5, 280, 190, 40);
        createGameButton.setBackground(Config.getDarkMode() ? backgroundColor : Color.WHITE);

        createGameButton.setFocusPainted(false);
        createGameButton.addActionListener(this::onCreateGame);

        joinGameButton.setBounds(5, 325, 190, 40);
        joinGameButton.setBackground(Config.getDarkMode() ? backgroundColor : Color.WHITE);
        joinGameButton.setFocusPainted(false);
        joinGameButton.addActionListener(this::onJoinGame);

        createGameButton.setBorder(new LineBorder(accentColor));
        createGameButton.setForeground(accentColor);
        joinGameButton.setBorder(new LineBorder(accentColor));
        joinGameButton.setForeground(accentColor);

        startMenuWindowManager.addComponent(
                createGameButton, startMenuWindowManager.getButtonPanel());
        startMenuWindowManager.addComponent(
                joinGameButton, startMenuWindowManager.getButtonPanel());

        gameRuleButton.setBounds(5, 440, 190, 40);
        gameRuleButton.setBackground(backgroundColor);
        gameRuleButton.setFocusPainted(false);
        gameRuleButton.setBorder(new LineBorder(accentColor));
        gameRuleButton.setForeground(accentColor);
        gameRuleButton.addActionListener(e -> gameRules.setVisible(true));
        startMenuWindowManager.addComponent(
                gameRuleButton, startMenuWindowManager.getButtonPanel());
    }

    private void initializeGameRulePopup() {
        gameRules.addJSwitchBox("Enable lives", Config.getEnableLives(), Config::setEnableLives);
        gameRules.addJSwitchBox("Enable timer", Config.getEnableTimer(), Config::setEnableTimer);
        gameRules.addJSwitchBox(
                "Enable easy mode", Config.getEnableEasyMode(), Config::setEnableEasyMode);
        gameRules.addJSwitchBox("Dark Mode", Config.getDarkMode(), Config::setDarkMode);
        gameRules.addJSwitchBox(
                "Killer Sudoku Mode",
                Config.getEnableKillerSudoku(),
                Config::setEnableKillerSudoku);
    }

    // Method to handle the logic for loading the game
    private void onLoadGame(ActionEvent e) {
        if (isLoadGameDialogOpen) {
            return;
        }

        List<SavedGame.SavedGameData> savedGames =
                SavedGame.loadSavedGames("jdbc:sqlite:sudoku.db");
        if (savedGames.isEmpty()) {
            JOptionPane.showMessageDialog(
                    null,
                    "No saved games available.",
                    "Error conserving saving",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        isLoadGameDialogOpen = true;

        // Apply the current theme settings to the dialog
        JDialog loadGameDialog = new JDialog();
        loadGameDialog.setTitle("Load Game");
        loadGameDialog.setSize(750, 300);
        loadGameDialog.setLayout(new BorderLayout());
        loadGameDialog.setBackground(backgroundColor);
        loadGameDialog.getContentPane().setBackground(backgroundColor);

        // Create the list of saved games
        JList<SavedGame.SavedGameData> gameList = new JList<>(new Vector<>(savedGames));
        gameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        gameList.setBackground(backgroundColor);
        gameList.setForeground(accentColor);
        gameList.setSelectionBackground(accentColor.darker());
        gameList.setSelectionForeground(backgroundColor);

        // Add a scroll pane to the list
        JScrollPane scrollPane = new JScrollPane(gameList);
        scrollPane.setBackground(backgroundColor);
        scrollPane.setForeground(accentColor);
        scrollPane.setBorder(new LineBorder(accentColor));

        // Create a button panel with a load button
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(backgroundColor);

        JButton loadButton = getjButton(gameList, loadGameDialog);

        loadGameDialog.addWindowListener(
                new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent we) {
                        isLoadGameDialogOpen = false;
                    }
                });

        buttonPanel.add(loadButton);
        loadGameDialog.add(scrollPane, BorderLayout.CENTER);
        loadGameDialog.add(buttonPanel, BorderLayout.SOUTH);
        loadGameDialog.setLocationRelativeTo(null);
        loadGameDialog.setVisible(true);
    }

    // Helper method for the load game popup
    private JButton getjButton(JList<SavedGame.SavedGameData> gameList, JDialog loadGameDialog) {
        JButton loadButton = new JButton("Load");
        loadButton.setBackground(backgroundColor);
        loadButton.setForeground(accentColor);
        loadButton.setBorder(new LineBorder(accentColor));
        loadButton.addActionListener(
                event -> {
                    SavedGame.SavedGameData selectedGame = gameList.getSelectedValue();
                    if (selectedGame != null) {
                        int[][] initialBoard = deserializeBoard(selectedGame.getInitialBoard());
                        int[][] currentBoard = deserializeBoard(selectedGame.getCurrentBoard());
                        int time = selectedGame.getTime();
                        int[] usedLifeLines = selectedGame.getUsedLifeLines();
                        boolean lifeEnabled = selectedGame.isLifeEnabled();
                        int n = selectedGame.getNSize();
                        int k = selectedGame.getKSize();
                        int[][] serializedCages = deserializeBoard(selectedGame.getCages());
                        boolean isKillerSudoku = selectedGame.isKillerSudoku();
                        String notes = selectedGame.getNotes();
                        String difficulty = selectedGame.getDifficulty();

                        startGameWithSavedData(
                                initialBoard,
                                currentBoard,
                                time,
                                usedLifeLines,
                                lifeEnabled,
                                n,
                                k,
                                serializedCages,
                                isKillerSudoku,
                                notes,
                                difficulty);
                        loadGameDialog.dispose();
                        isLoadGameDialogOpen = false;
                    }
                });
        return loadButton;
    }

    // Takes a board representation as a string from the database and makes it into a 2D int array,
    // so it can be used by the backend
    private int[][] deserializeBoard(String boardString) {
        String[] rows = boardString.split(";");
        int size = rows.length;
        int[][] board = new int[size][size];

        for (int i = 0; i < size; i++) {
            String[] cells = rows[i].split(",");
            for (int j = 0; j < cells.length; j++) {
                board[i][j] = Integer.parseInt(cells[j]);
            }
        }

        return board;
    }

    // Custom start game method to start with a loaded game
    private void startGameWithSavedData(
            int[][] initialBoard,
            int[][] currentBoard,
            int time,
            int[] usedLifeLines,
            boolean lifeEnabled,
            int n,
            int k,
            int[][] cages,
            boolean isKillerSudoku,
            String notes,
            String difficulty) {
        Config.setCellSize(550 / (k * n));
        Config.setK(k);
        Config.setN(n);
        logger.info("Used life lines: {}", usedLifeLines);
        int usedLives = usedLifeLines[0];
        Config.setNumberOfLives(usedLifeLines[1]);
        Config.setEnableLives(lifeEnabled);
        Config.setDifficulty(difficulty);
        System.out.println("loaded game: " + Config.getDifficulty());
        logConfigInfo();

        WindowManager windowManager =
                new WindowManager(startMenuWindowManager.getFrame(), 1000, 1000);
        try {
            if (lifeEnabled) {
                Config.setEnableLives(true);
            }
            GameEngine gameEngine =
                    new GameEngine(
                            windowManager, Config.getN(), Config.getK(), Config.getCellSize());
            windowManager.display();
            windowManager.updateBoard();
            gameEngine.startCustomSaved(
                    initialBoard,
                    currentBoard,
                    time,
                    usedLives,
                    n,
                    k,
                    cages,
                    isKillerSudoku,
                    notes);
        } catch (Board.BoardNotCreatable boardNotCreatable) {
            logBoardNotCreatable();
        }
    }

    // Handles what happens when creating an online game, gets the ip address from the host and
    // starts the server along with a game thread
    private void onCreateGame(ActionEvent e) {
        Config.setDifficulty("Easy");
        joinGameButton.setEnabled(false);
        createGameButton.setEnabled(false);

        // Start the server in a new thread
        new Thread(
                        () -> {
                            GameServer server = new GameServer();
                            server.start();
                        })
                .start();

        try {
            // Get the local IP address of the server
            String localIpAddress = InetAddress.getLocalHost().getHostAddress();
            logger.info("Server started on IP: {}", localIpAddress);

            // Connect the client to the newly started server
            connectClient(localIpAddress);

        } catch (UnknownHostException ex) {
            logger.error("Failed to determine the server's IP address: {}", ex.getMessage());
            JOptionPane.showMessageDialog(
                    null,
                    "Failed to determine the server's IP address.",
                    ERROR,
                    JOptionPane.ERROR_MESSAGE);
            joinGameButton.setEnabled(true);
            createGameButton.setEnabled(true);
        }
    }

    /**
     * Handles what happens when joining a game. It is either possible to search for IPS on the current network or manually typing an ip address.
     * It then makes a game client with a new window-manager and then starts the game
     */
    private void onJoinGame(ActionEvent e) {
        joinGameButton.setEnabled(false);
        createGameButton.setEnabled(false);

        // Prompt user for an IP address
        String serverAddress =
                showInputDialog(null);

        // Check if the dialog was cancelled
        if (serverAddress == null) {
            joinGameButton.setEnabled(true);
            createGameButton.setEnabled(true);
            return;
        }

        // Create a GameClient to discover servers
        GameClient client =
                new GameClient(new WindowManager(startMenuWindowManager.getFrame(), 1000, 1000));

        if (serverAddress.isEmpty()) {
            client.discoverServers();

            List<String> discoveredServers = client.getDiscoveredServers();

            boolean connected = false;
            for (String address : discoveredServers) {
                logger.info("Attempting to connect to server at IP: {}", address);
                if (client.testGameConnection(address)) {
                    logger.info("Connected to server at IP: {}", address);
                    connectClient(address);
                    connected = true;
                    break;
                }
            }
            if (!connected) {
                JOptionPane.showMessageDialog(
                        null,
                        "Failed to connect to any server. Please check the server addresses and try"
                                + " again.",
                        ERROR,
                        JOptionPane.ERROR_MESSAGE);

                joinGameButton.setEnabled(true);
                createGameButton.setEnabled(true);
            }
        } else {
            logger.info("Attempting to directly to IP: {}", serverAddress);
            if (client.testGameConnection(serverAddress)) {
                logger.info("Connected to server at IP: {}", serverAddress);
                connectClient(serverAddress);
            } else {
                JOptionPane.showMessageDialog(
                        null,
                        "Failed to connect to the specified server. Please check the server address"
                                + " and try again.",
                        ERROR,
                        JOptionPane.ERROR_MESSAGE);

                joinGameButton.setEnabled(true);
                createGameButton.setEnabled(true);
            }
        }
    }

    private String showInputDialog(Frame parent) {
        final JDialog dialog = new JDialog(parent, "Join Game", true);
        final JTextField textField = new JTextField(20);
        final JButton okButton = new JButton("OK");
        final boolean[] okPressed = {false};

        dialog.setLayout(new BorderLayout());
        dialog.add(new JLabel("Enter server IP address (leave empty to search):"), BorderLayout.NORTH);
        dialog.add(textField, BorderLayout.CENTER);

        JPanel panel = new JPanel();
        panel.add(okButton);
        dialog.add(panel, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(parent);

        okButton.addActionListener(
                e -> {
                    okPressed[0] = true;
                    dialog.dispose();
                });

        dialog.addWindowListener(
                new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        dialog.dispose();
                    }
                });

        dialog.setVisible(true);

        return okPressed[0] ? textField.getText() : null;
    }

    // Method to connect the client to the server and starting the thread
    private void connectClient(String serverAddress) {
        new Thread(
                        () -> {
                            WindowManager windowManager =
                                    new WindowManager(
                                            startMenuWindowManager.getFrame(), 1000, 1000);
                            GameClient client = new GameClient(windowManager);
                            try {
                                client.start(serverAddress);
                                SwingUtilities.invokeLater(
                                        () ->
                                                JOptionPane.showMessageDialog(
                                                        null,
                                                        "Connected to server at " + serverAddress,
                                                        "Connected",
                                                        JOptionPane.INFORMATION_MESSAGE));
                            } catch (IOException | Board.BoardNotCreatable ex) {
                                logger.error("Failed to start the game with the server: {}", ex.getMessage());
                                SwingUtilities.invokeLater(
                                        () -> {
                                            JOptionPane.showMessageDialog(
                                                    null,
                                                    "Failed to start the game with the server.",
                                                    ERROR,
                                                    JOptionPane.ERROR_MESSAGE);
                                            joinGameButton.setEnabled(true);
                                            createGameButton.setEnabled(true);
                                        });
                            }
                        })
                .start();
    }

    private void addLeaderboardButton() {
        leaderboardButton.addActionListener(this::onShowLeaderboard);
        leaderboardButton.setBounds(5, 95, 190, 40);
        leaderboardButton.setBackground(backgroundColor);
        leaderboardButton.setFocusPainted(false);

        leaderboardButton.setBorder(new LineBorder(accentColor));
        leaderboardButton.setForeground(accentColor);

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

        JScrollPane leaderboardScrollPane = new JScrollPane(leaderboardTable);

        JDialog leaderboardDialog = new JDialog();
        leaderboardDialog.setTitle("Leaderboard");
        leaderboardDialog.setSize(600, 400);
        leaderboardDialog.setLocationRelativeTo(null);
        leaderboardDialog.add(leaderboardScrollPane);
        leaderboardDialog.setVisible(true);
    }

    // This method adds listeners for the input fiels that make up the custom board, allowing
    // different configurations of game boards
    private void addChangeListenerToField(JTextField field) {
        // This method adds a document listener to the input fields, so that the board & config is
        // updated
        // when the user changes the values
        field.getDocument()
                .addDocumentListener(
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
                                        boardConfigs[3] = new int[] {n, k};
                                    }
                                } catch (NumberFormatException ex) {
                                    // Handle the case where one of the fields is empty or does not
                                    // contain a valid integer
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
            field.setBackground(backgroundColor);
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

            field.setBorder(new LineBorder(accentColor));
            field.setForeground(accentColor);

            startMenuWindowManager.addComponent(field, startMenuWindowManager.getInputPanel());
        }
    }

    private void addButtonPanelButtons() {
        startButton.setBounds(5, 5, 190, 40);
        startButton.setBackground(backgroundColor);
        startButton.setFocusPainted(false);
        startButton.addItemListener(
                e -> {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        startButton.setBackground(Color.GRAY);
                        try {
                            if (!GraphicsEnvironment.isHeadless()) {
                                gameRules.setVisible(false);
                            }
                            startGame();
                        } catch (Board.BoardNotCreatable ex) {
                            logger.error("This board-type is not creatable: {}", ex.getMessage());
                        }
                    } else {
                        startButton.setBackground(Color.WHITE);
                    }
                });

        startButton.setBorder(new LineBorder(accentColor));
        startButton.setForeground(accentColor);

        startMenuWindowManager.addComponent(startButton, startMenuWindowManager.getButtonPanel());

        difficultyDropdown.setBounds(5, 50, 190, 40);
        difficultyDropdown.setBackground(backgroundColor);
        difficultyDropdown.setForeground(accentColor);
        difficultyDropdown.setBorder(new LineBorder(accentColor));
        difficultyDropdown.setSelectedItem("Medium");
        Config.setDifficulty("medium");

        difficultyDropdown.addActionListener(
                e -> {
                    String selectedDifficulty = (String) difficultyDropdown.getSelectedItem();
                    if (selectedDifficulty != null) {
                        Config.setDifficulty(selectedDifficulty.toLowerCase());
                    }
                });

        startMenuWindowManager.addComponent(
                difficultyDropdown, startMenuWindowManager.getButtonPanel());
    }

    private void addImportButton() {
        importButton.addActionListener(this::onImportSudoku);
        importButton.setBounds(5, 210, 190, 40);
        importButton.setBackground(backgroundColor);
        importButton.setFocusPainted(false);

        importButton.setBorder(new LineBorder(accentColor));
        importButton.setForeground(accentColor);

        startMenuWindowManager.addComponent(importButton, startMenuWindowManager.getButtonPanel());
    }

    // Calls the correct start game method when importing a sudoku from a file
    private void onImportSudoku(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a Sudoku Puzzle File");
        int result = fileChooser.showOpenDialog(null);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                List<String> lines = Files.readAllLines(selectedFile.toPath());
                int[][] customBoard = importSudokuFromFile(lines);
                startGameWithBoard(customBoard);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(
                        null,
                        "Failed to load the Sudoku file: " + ex.getMessage(),
                        ERROR,
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // This method takes in a list of strings by reading a file and then it spits out a game board
    // represented as a 2D int array
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

            GameEngine gameEngine =
                    new GameEngine(
                            windowManager, Config.getN(), Config.getK(), Config.getCellSize());
            windowManager.display();
            windowManager.updateBoard();
            gameEngine.startCustom(board);
        } catch (Board.BoardNotCreatable boardNotCreatable) {
            logger.error("Board not creatable: {}", boardNotCreatable.getMessage());
        }
    }

    // Add the custom board panels with different prefixed sizes and the one you can set yourself
    // with N and K values
    private void addSizePanelButtons() {
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
            panel.updateBoard(n, k);

            panel.addMouseListener(mouseAdapter);

            panel.setBounds(xPosition, 5, 150, 150);
            xPosition += 150 + 5;

            startMenuWindowManager.addComponent(
                    panel,
                    startMenuWindowManager.getSizePanel());
            sizeGroup.addComponent(panel);
        }
    }

    // Making sure to dynamically update the colors of the start menu when switching between light
    // and dark mode in the game rule menu
    // Making sure that you do not need to open and close the game between each swap.
    public void updateColors() {
        setBackgroundColor(Config.getDarkMode() ? darkbackgroundColor : Color.WHITE);
        setAccentColor(Config.getDarkMode() ? lightaccentColor : Color.BLACK);
        startMenuWindowManager.setCustomBoardPanels(boardPanels);

        startButton.setBackground(backgroundColor);
        startButton.setForeground(accentColor);
        startButton.setBorder(new LineBorder(accentColor));

        difficultyDropdown.setBackground(backgroundColor);
        difficultyDropdown.setForeground(accentColor);
        difficultyDropdown.setBorder(new LineBorder(accentColor));

        gameRuleButton.setBackground(backgroundColor);
        gameRuleButton.setForeground(accentColor);
        gameRuleButton.setBorder(new LineBorder(accentColor));

        createGameButton.setBackground(backgroundColor);
        createGameButton.setForeground(accentColor);
        createGameButton.setBorder(new LineBorder(accentColor));

        joinGameButton.setBackground(backgroundColor);
        joinGameButton.setForeground(accentColor);
        joinGameButton.setBorder(new LineBorder(accentColor));

        importButton.setBackground(backgroundColor);
        importButton.setForeground(accentColor);
        importButton.setBorder(new LineBorder(accentColor));

        leaderboardButton.setBackground(backgroundColor);
        leaderboardButton.setForeground(accentColor);
        leaderboardButton.setBorder(new LineBorder(accentColor));

        loadGameButton.setBackground(backgroundColor);
        loadGameButton.setForeground(accentColor);
        loadGameButton.setBorder(new LineBorder(accentColor));

        inputNField.setBackground(backgroundColor);
        inputNField.setForeground(accentColor);
        inputNField.setBorder(new LineBorder(accentColor));
        inputKField.setBackground(backgroundColor);
        inputKField.setForeground(accentColor);
        inputKField.setBorder(new LineBorder(accentColor));

        startMenuWindowManager.update();
    }

    private void logConfigInfo() {
        logger.info(
                "startGame: {} {} {} {}",
                Config.getK(),
                Config.getN(),
                Config.getDifficulty(),
                Config.getCellSize());
    }

    private void logBoardNotCreatable() {
        logger.error("This board-type is not creatable");
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

    public CustomBoardPanel getFourByFour() {
        return fourByFour;
    }

    public JComboBox<String> getDifficultyDropdown() {
        return difficultyDropdown;
    }
}
