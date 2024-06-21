/* (C)2024 */
package dk.dtu.game.core;

import dk.dtu.engine.core.StartMenuWindowManager;
import dk.dtu.engine.core.WindowManager;
import dk.dtu.engine.graphics.Cage;
import dk.dtu.engine.graphics.NumberHub;
import dk.dtu.engine.graphics.SudokuBoardCanvas;
import dk.dtu.engine.input.KeyboardListener;
import dk.dtu.engine.input.MouseActionListener;
import dk.dtu.engine.utility.CustomProgressBar;
import dk.dtu.engine.utility.SavedGame;
import dk.dtu.engine.utility.TimerFunction;
import dk.dtu.engine.utility.UpdateLeaderboard;
import dk.dtu.game.core.solver.SolverAlgorithm;
import dk.dtu.game.core.solver.algorithmx.AlgorithmXSolver;
import dk.dtu.game.core.solver.heuristicsolver.HeuristicSolver;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.PrintWriter;
import java.security.SecureRandom;
import java.util.*;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The SudokuGame class is responsible for managing all the actions taken on the main game window. It handles making moves, undoing moves,
 * providing hints, etc. It also manages the game board and the number hub. It serves as a connection between the board and the sudoku-board canvas,
 * whilst also adding buttons and panels etc. to the window-manager. It is the core logic of
 */
public class SudokuGame {
    public final Board gameboard;
    public record Move(int row, int column, int number, int previousNumber) {}
    public final Deque<Move> moveList = new ArrayDeque<>();
    public final List<Move> wrongMoveList = new ArrayList<>();
    private final Logger logger = LoggerFactory.getLogger(SudokuGame.class);
    private final WindowManager windowManager;
    private final ArrayList<Move> hintList = new ArrayList<>();
    private final int nSize;
    private final int kSize;
    private final JToggleButton noteButton = new JToggleButton("Note Mode", false);
    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
    private final JButton saveGameButton;
    int gridSize;
    int cellSize;
    private int placeableNumber = 0;
    private int nextCageId = 1;
    private static final Random rand = new Random();


    MouseActionListener mouseActionListener = new MouseActionListener(this);
    KeyboardListener keyboardListener = new KeyboardListener(this);
    Random random = new SecureRandom();
    private SudokuBoardCanvas board;
    private NumberHub numbers;
    private TimerFunction timer;
    private boolean gameIsStarted = false;
    private JButton undoButton;
    private JButton hintButton;
    private JButton restartButton;
    private JButton newGameButton;
    private JButton eraseButton;
    private JButton solveButton;
    JButton goBackButton = createButton("Go Back", 30);

    private PrintWriter networkOut;
    private boolean isCustomBoard = false;
    private boolean isNetworkGame = false;
    private final CustomProgressBar opponentProgressBar;
    private final CustomProgressBar playerProgressBar;

    private static final Color darkbackgroundColor = new Color(64, 64, 64);
    private static Color accentColor = new Color(237, 224, 186);
    private static final Color lightaccentcolor = new Color(237, 224, 186);
    private static final Color initialColor = new Color(159, 148, 102);
    private static Color backgroundColor;
    private static final String NEW_GAME = "new game";
    private static final String SAVE_GAME = "save game";

    private Boolean usedSolveButton = false;

    public SudokuGame(WindowManager windowManager, int n, int k, int cellSize)
            throws Board.BoardNotCreatable {
        this.windowManager = windowManager;
        setBackgroundColor(Config.getDarkMode() ? darkbackgroundColor : Color.WHITE);
        setAccentColor(Config.getDarkMode() ? lightaccentcolor : Color.BLACK);

        try {
            gameboard = new Board(n, k);
        } catch (Board.BoardNotCreatable e) {
            throw new Board.BoardNotCreatable("This board is not possible to create");
        }

        this.nSize = n;
        this.kSize = k;
        this.gridSize = n * k;
        this.cellSize = cellSize;

        new Thread(this::processNetworkMessages).start();

        saveGameButton = createButton(SAVE_GAME, 30);
        saveGameButton.setBackground(backgroundColor);
        saveGameButton.setForeground(accentColor);
        saveGameButton.setBorder(
                BorderFactory.createCompoundBorder(
                        new LineBorder(accentColor, 1), new EmptyBorder(5, 5, 5, 5)));
        saveGameButton.addActionListener(
                e -> {
                    onSaveGame();
                    board.requestFocusInWindow();
                });

        Color textColor = Config.getDarkMode() ? Color.BLACK : Color.BLUE;

        // Initialize the opponent progress bar
        opponentProgressBar = new CustomProgressBar(0, 100);
        opponentProgressBar.setStringPainted(true);
        opponentProgressBar.setString("Opponent's Progress");
        opponentProgressBar.setForeground(accentColor);
        opponentProgressBar.setBackground(backgroundColor);
        opponentProgressBar.setBorder(new LineBorder(accentColor, 2));
        opponentProgressBar.setTextColor(textColor);

        // Initialize the player progress bar
        playerProgressBar = new CustomProgressBar(0, 100);
        playerProgressBar.setStringPainted(true);
        playerProgressBar.setString("Your Progress");
        playerProgressBar.setForeground(accentColor);
        playerProgressBar.setBackground(backgroundColor);
        playerProgressBar.setBorder(new LineBorder(accentColor, 2));
        playerProgressBar.setTextColor(textColor);
    }

    private static void setAccentColor(Color color) {
        accentColor = color;
    }

    private static void setBackgroundColor(Color color) {
        backgroundColor = color;
    }

    // Saves the current game for further playing, it saves all the necessary information to the
    // database
    private void onSaveGame() {
        String name =
                JOptionPane.showInputDialog(
                        null,
                        "Enter a name for your saved game:",
                        SAVE_GAME,
                        JOptionPane.PLAIN_MESSAGE);
        if (name != null && !name.trim().isEmpty()) {
            int[] hearts = getUsedLives();

            SavedGame.saveGame(
                    "jdbc:sqlite:sudoku.db",
                    name,
                    gameboard.getInitialBoard(),
                    gameboard.getGameBoard(),
                    timer.getTimeToInt(),
                    hearts,
                    Config.getEnableLives(),
                    Config.getK(),
                    Config.getN(),
                    board.getCagesIntArray(),
                    Config.getEnableKillerSudoku(),
                    getNotesToString(),
                    Config.getDifficulty());
            JOptionPane.showMessageDialog(
                    null, "Game saved successfully.", SAVE_GAME, JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(
                    null,
                    "Game save canceled. Name cannot be empty.",
                    SAVE_GAME,
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    private String getNotesToString() {
        // Return notes to a string
        return board.getNotes();
    }

    private void processNetworkMessages() {
        while (!isGameOver() && isNetworkGame) {
            try {
                String message = messageQueue.take();
                processNetworkMessage(message);
            } catch (InterruptedException e) {
                logger.error("Error processing network message: {}", e.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }

    public void setNetworkGame(boolean networkGame) {
        isNetworkGame = networkGame;
    }

    public void setNetworkOut(PrintWriter networkOut) {
        this.networkOut = networkOut;
    }

    public void processNetworkMessage(String message) {
        String[] parts = message.split(" ");
        String command = parts[0];
        logger.info("Received message: {}", message);

        switch (command) {
            case "WINNER":
                timer.stop();
                String winner = parts[1];
                SwingUtilities.invokeLater(() -> announceWinner(winner));
                break;

            case "COMPLETED":
                timer.stop();
                String playerName = parts[1];
                SwingUtilities.invokeLater(
                        () ->
                                JOptionPane.showMessageDialog(
                                        null, playerName + " has completed the Sudoku!"));
                break;
            case "PROGRESS":
                int progress1 = Integer.parseInt(parts[1]);
                SwingUtilities.invokeLater(() -> updateOpponentProgress(progress1));
                break;

            default:
                logger.error("Unknown command: {}", command);
        }
    }

    private void updateOpponentProgress(int progress) {
        // Check if my progress is higher than the opponent's progress
        if (progress != calculateProgress()) {
            opponentProgressBar.setValue(progress);
        }
    }

    private void announceWinner(String winner) {
        JOptionPane.showMessageDialog(null, "The winner is: " + winner);
    }

    // This functions determines what happens when you click on a tile on the sudoku board
    public void onSudokuBoardClicked(int x, int y) {
        int row = y / (board.getHeight() / gridSize);
        int column = x / (board.getWidth() / gridSize);
        if (column >= 0 && column < gridSize && row >= 0 && row < gridSize) {
            board.setMarkedCell(row, column);
            board.setChosenNumber(gameboard.getNumber(row, column));

            // Ensure placeableNumber is set correctly and the cell is empty before making a move
            if (placeableNumber != 0
                    && gameboard.getInitialNumber(row, column) == 0
                    && gameboard.getNumber(row, column) == 0) {
                makeMove(row, column, placeableNumber);
            }

            if (Config.getEnableEasyMode()) {
                board.highlightPlaceableCells(gameboard.getNumber(row, column));
            }

            int cellIndex = row * gridSize + column;
            logger.debug("Cell {} clicked. Row: {}, Column: {}", cellIndex, row, column);

            board.highlightCell(row, column, true);
            checkCompletionAndOfferNewGame();
        } else {
            logger.debug("Click outside the Sudoku board or on another component");
        }
    }

    // This lets the player set a number with the keyboard
    public void typeNumberWithKeyboard(KeyEvent e) {
        char keyChar = e.getKeyChar();

        if (Character.isDigit(keyChar)) {
            int number = keyChar - '0'; // Convert character to integer
            int[] markedCell = board.getMarkedCell();
            int row = markedCell[0];
            int col = markedCell[1];

            makeMove(row, col, number);

            if (Config.getEnableEasyMode()) {
                board.highlightPlaceableCells(number);
            }
        }
        updateNumberCount();
        checkCompletionAndOfferNewGame();
    }

    public void checkCellsForNotes(int row, int col, int number, String mode) {
        checkRowAndColumnForNotes(row, col, number, mode);
        checkSubSquareForNotes(row, col, number, mode);
    }

    private void checkRowAndColumnForNotes(int row, int col, int number, String mode) {
        for (int i = 0; i < gridSize; i++) {
            if (board.getNotesInCell(row, i).contains(number)) {
                updateHideList(row, i, number, mode);
            }
            if (board.getNotesInCell(i, col).contains(number)) {
                updateHideList(i, col, number, mode);
            }
        }
    }

    private void checkSubSquareForNotes(int row, int col, int number, String mode) {
        int startRow = row - (row % nSize);
        int startCol = col - (col % nSize);
        for (int i = startRow; i < startRow + nSize; i++) {
            for (int j = startCol; j < startCol + nSize; j++) {
                if (board.getNotesInCell(i, j).contains(number)) {
                    updateHideList(i, j, number, mode);
                }
            }
        }
    }

    // This method updates a cell to determine if the notes in the cell should be hidden or not.
    // They should
    // be hidden when a number is placed in the cell.
    private void updateHideList(int row, int col, int number, String mode) {
        if (mode.equals("show")) {
            board.removeFromHideList(row, col, number);
        } else {
            board.addToHideList(row, col, number);
        }
    }

    private void updateNumberCount() {
        // Loop through the board and add all the numbers to a list,
        // then check if each number is equal to the max needed number,
        // if it is, then update the number display
        List<Integer> numberList = new ArrayList<>();
        for (int row = 0; row < gameboard.getDimensions(); row++) {
            for (int col = 0; col < gameboard.getDimensions(); col++) {
                numberList.add(gameboard.getNumber(row, col));
            }
        }

        for (int i = 1; i <= gameboard.getDimensions(); i++) {
            int count = Collections.frequency(numberList, i);
            getNumbersBoard().updateNumberDisplay(i, count != gameboard.getDimensions());
        }
    }

    // This either makes a note or a move, depending upon the selection of the noteButton
    public void makeMove(int row, int col, int number) {
        if (row >= 0 && col >= 0 && row < gridSize && col < gridSize) {
            if (noteButton.isSelected()
                    && gameboard.getInitialNumber(row, col) == 0
                    && gameboard.getNumber(row, col) == 0
                    && number != 0) {
                makeNote(row, col, number);
            } else {
                if (number != 0) {
                    if (Config.getEnableLives()) {
                        makeMoveWithLives(row, col, number);
                    } else {
                        makeMoveWithoutLives(row, col, number);
                    }
                }
            }
        }
        if (isNetworkGame) {
            sendProgress();
        }
    }

    // Makes a move with lives enabled, meaning you can place everywhere, but will lose lives.
    private void makeMoveWithLives(int row, int col, int number) {
        if (gameboard.getInitialNumber(row, col) == 0
                && !noteButton.isSelected()
                && number != 0
                && gameboard.getNumber(row, col) != number) {

            int previousNumber = gameboard.getNumber(row, col);
            applyMove(row, col, number, previousNumber);

            int[][] solutionB = gameboard.getSolvedBoard();

            if (gameboard.getNumber(row, col) != solutionB[row][col]
                    || (Config.getEnableKillerSudoku()
                    && cageContains(new Point(col, row), number))) {
                windowManager.removeHeart();
                board.setWrongNumber(row, col, number);
                wrongMoveList.add(new Move(row, col, number, previousNumber));
            }
        }
    }



    // This only lets you place a number in a valid place, meaning you cannot place 2 numbers in the
    // same row, col and subgrid.
    // This doesn't mean that you can only place in correct places, so you can still make mistakes.
    private void makeMoveWithoutLives(int row, int col, int number) {
        if (gameboard.getInitialNumber(row, col) == 0
                && !noteButton.isSelected()
                && number != 0
                && gameboard.validPlace(row, col, number)) {

            if (Config.getEnableKillerSudoku()) {
                Point cell = new Point(col, row);
                if (cageContains(cell, number)) {
                    // If the number is already in the cage, do not make the move
                    return;
                } else {
                    // Get the current cage and add the number to it
                    Cage cage = board.getCage(row, col);
                    if (cage != null) {
                        cage.addCurrentNumber(number);
                    }
                }
            }

            int previousNumber = gameboard.getNumber(row, col);
            applyMove(row, col, number, previousNumber);

            int[][] solutionB = gameboard.getSolvedBoard();

            if (gameboard.getNumber(row, col) != solutionB[row][col]) {
                wrongMoveList.add(new Move(row, col, number, previousNumber));
            }
        }
    }

    // The logic behind setting the visual number and setting the backend number
    private void applyMove(int row, int col, int number, int previousNumber) {
        board.setHiddenProperty(row, col, true);
        checkCellsForNotes(row, col, number, "hide");
        board.setCellNumber(row, col, number);
        board.setChosenNumber(number);
        gameboard.setNumber(row, col, number);
        Move move = new Move(row, col, number, previousNumber);
        moveList.push(move); // Log the move for undo functionality
    }

    // Add a note to the cell if all the right conditions are met
    public void makeNote(int row, int col, int number) {
        if (gameIsStarted) {
            if (isNumberInRow(row, number)
                    || isNumberInColumn(col, number)
                    || isNumberInSubGrid(row, col, number)) {
                // Do not place the note if the number is already present in the subgrid, row, or
                // column
                return;
            }
            if (board.getNotesInCell(row, col).contains(number)) {
                board.removeNoteFromCell(row, col, number);
            } else {
                board.addNoteToCell(row, col, number);
            }
        }
    }

    public void eraseNumber() {
        if (board.isACellMarked()) {
            int[] cell = board.getMarkedCell();
            int row = cell[0];
            int col = cell[1];
            int number = gameboard.getNumber(row, col);
            checkCellsForNotes(row, col, number, "show");

            if (gameboard.getInitialNumber(row, col) == 0) {
                if (Config.getEnableKillerSudoku()) {
                    Cage cage = board.getCage(row, col);
                    if (cage != null) {
                        cage.removeCurrentNumber(number);
                    }
                }
                board.setHiddenProperty(row, col, false);
                board.removeNumber(row, col);
                gameboard.setNumber(row, col, 0);
            }
        }
    }

    // This sets the numbers on the visual board.
    public void displayNumbersVisually() {
        for (int row = 0; row < gameboard.getDimensions(); row++) {
            for (int col = 0; col < gameboard.getDimensions(); col++) {
                int number = gameboard.getNumber(row, col);
                board.setCellNumber(row, col, number);
            }
        }
    }

    public void undoMove() {
        if (!moveList.isEmpty()) {
            Move move = moveList.pop();
            if (!wrongMoveList.isEmpty() && !Config.getEnableLives()) {
                wrongMoveList.removeFirst();
            }
            int row = move.row();
            int col = move.column();

            board.setHiddenProperty(row, col, false);
            checkCellsForNotes(row, col, move.number(), "show");
            int prevNumber = move.previousNumber();
            int number = move.number();

            if (Config.getEnableKillerSudoku()) {
                Cage cage = board.getCage(row, col);
                if (cage != null) {
                    cage.removeCurrentNumber(number);
                }
            }
            gameboard.setNumber(row, col, prevNumber);
            board.setCellNumber(row, col, prevNumber);
            logger.debug("Undo move: Row: {}, Column: {}, Number: {}", row, col, prevNumber);
        }
        if (isNetworkGame) {
            sendProgress();
        }
    }

    // An initializer for the visual and backend board, vital for the game to start
    public void createBoard(int n, int k, int cellSize) {
        board = new SudokuBoardCanvas(n, k, cellSize);
        board.setLocation(50, 50);
        board.setFocusable(true);

        board.addMouseListener(mouseActionListener);

        board.addKeyListener(keyboardListener);

        gameboard.setInitialBoard(deepCopyBoard(gameboard.getGameBoard()));

        numbers = new NumberHub(n, 40) {};
        getNumbersBoard().update();
        board.update();

        numbers.setLocation(50, 50);
        numbers.setFocusable(true);

        numbers.addMouseListener(mouseActionListener);
        numbers.addKeyListener(keyboardListener);

        timer = new TimerFunction();
        timer.setFocusable(true);
        timer.update();
    }

    // This method generates all the cages for the killer sudoku game mode and determines their sum
    public void generateKillerSudokuCages() {
        int[][] solvedBoard = gameboard.getSolvedBoard();
        board.clearCages();

        boolean[][] used = new boolean[gridSize][gridSize];

        int minCageSize = 1;
        int maxCageSize = 4;
        double smallCageProbability =
                switch (Config.getDifficulty()) {
                    case "medium" -> 0.9; // 90% chance to create smaller cages
                    case "hard" -> 0.7; // 70% chance to create smaller cages
                    case "extreme" -> 0.5; // 50% chance to create smaller cages
                    default -> 1.0; // 100% chance to create smaller cages for other difficulties
                };

        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                if (!used[row][col]) {
                    List<Point> cageCells = new ArrayList<>();
                    Set<Integer> uniqueNumbers = new HashSet<>();
                    int cageSize = rand.nextInt(maxCageSize - minCageSize + 1) + minCageSize;

                    // Adjust cage size probability
                    if (rand.nextDouble() > smallCageProbability) {
                        cageSize = Math.min(maxCageSize, cageSize + 1);
                    }

                    int sum = 0;

                    Queue<Point> queue = new LinkedList<>();
                    queue.add(new Point(col, row));

                    while (!queue.isEmpty() && cageCells.size() < cageSize) {
                        Point current = queue.poll();
                        int r = current.y;
                        int c = current.x;

                        if (r >= 0 && r < gridSize && c >= 0 && c < gridSize && !used[r][c]) {
                            int num = solvedBoard[r][c];
                            Point cell = new Point(c, r);
                            if (!cageContains(cell, num) && !uniqueNumbers.contains(num)) {
                                cageCells.add(cell);
                                sum += num;
                                uniqueNumbers.add(num);
                                used[r][c] = true;

                                // Add adjacent cells to the queue
                                if (r + 1 < gridSize && !used[r + 1][c])
                                    queue.add(new Point(c, r + 1));
                                if (r - 1 >= 0 && !used[r - 1][c]) queue.add(new Point(c, r - 1));
                                if (c + 1 < gridSize && !used[r][c + 1])
                                    queue.add(new Point(c + 1, r));
                                if (c - 1 >= 0 && !used[r][c - 1]) queue.add(new Point(c - 1, r));
                            }
                        }
                    }

                    if (!cageCells.isEmpty()) {
                        Cage cage = new Cage(cageCells, sum, nextCageId++);
                        board.addCage(cage.getId(), cage);
                    }
                }
            }
        }
    }

    // The main initializer, which is calling all the lesser initializers such as createBoard and
    // displayButtons
    public void initialize(int n, int k, int cellSize) throws Board.BoardNotCreatable {
        createBoard(n, k, cellSize);

        displayButtons();
        windowManager.drawBoard(board);
        windowManager.setupNumberAndTimerPanel(timer, numbers, goBackButton, saveGameButton);
        windowManager.layoutComponents(timer, numbers, goBackButton, saveGameButton);
        startGame();
        if (Config.getEnableKillerSudoku()) {
            generateKillerSudokuCages();
        }
    }

    // This method is used to initialize the game with a custom imported board
    public void initializeCustom(int[][] customBoard) {
        isCustomBoard = true;
        createBoard(Config.getN(), Config.getK(), Config.getCellSize());
        displayButtons();
        windowManager.drawBoard(board);
        windowManager.setupNumberAndTimerPanel(timer, numbers, goBackButton, saveGameButton);
        windowManager.layoutComponents(timer, numbers, goBackButton, saveGameButton);
        gameboard.setInitialBoard(customBoard);
        gameboard.setGameBoard(deepCopyBoard(customBoard));

        if (nSize == kSize) {
            AlgorithmXSolver.solveExistingBoard(gameboard);
        } else {
            HeuristicSolver.createPlayableSudoku(gameboard);
        }

        fillHintList();
        if (Config.getEnableTimer()) {
            timer.start();
        }
        displayNumbersVisually();
        setInitialBoardColor();
        gameIsStarted = true;

        solveButton.setEnabled(true);

        if (isNetworkGame) {
            timer.setVisibility(true);
            timer.start();
            solveButton.setEnabled(false);
            hintButton.setEnabled(false);
            newGameButton.setEnabled(false);
            restartButton.setEnabled(false);
            windowManager.addProgressBar(opponentProgressBar, 2);
            windowManager.addProgressBar(playerProgressBar, 3);
            playerProgressBar.setValue(calculateProgress());
            opponentProgressBar.setValue(calculateProgress());
            sendProgress();
        } else {
            windowManager.setHeart();
        }

        newGameButton.setText("Replay");
        board.requestFocusInWindow();
    }

    // This initializes a custom game, when the game is loaded form the saved games.
    public void initializeCustomSaved(
            int[][] initialBoard,
            int[][] currentBoard,
            int time,
            int usedLifeLines,
            int[][] cages,
            boolean isKillerSudoku,
            String notes) {
        isCustomBoard = true;
        createBoard(Config.getN(), Config.getK(), Config.getCellSize());
        displayButtons();
        windowManager.drawBoard(board);
        windowManager.setupNumberAndTimerPanel(timer, numbers, goBackButton, saveGameButton);
        windowManager.layoutComponents(timer, numbers, goBackButton, saveGameButton);

        gameboard.setInitialBoard(initialBoard);
        gameboard.setGameBoard(deepCopyBoard(currentBoard));

        if (nSize == kSize) {
            AlgorithmXSolver.solveExistingBoard(gameboard);
        } else {
            HeuristicSolver.createPlayableSudoku(gameboard);
        }

        if (isKillerSudoku) {
            board.addCages(cages, gameboard);
            // Calculate the sum of the cages
            for (Cage cage : board.getCages()) {
                cage.calculateSumFromSolution(gameboard.getSolvedBoard());
            }
        }

        gameboard.setGameBoard(deepCopyBoard(currentBoard));

        if (!Objects.equals(notes, "")) {
            board.setNotes(notes);
            applyNotesToCells();
        }

        fillHintList();
        if (time > 0) {
            timer.startWithTime(time);
        }

        if (usedLifeLines >= 0) {
            windowManager.setHeart();
            windowManager.setHearts(usedLifeLines);
        }
        displayNumbersVisually();
        setInitialBoardColor();
        gameIsStarted = true;

        solveButton.setEnabled(true);

        board.requestFocusInWindow();
    }

    // This method calculates the progress of the game, this is used by the online multiplayer.
    public int calculateProgress() {
        int filledCells = 0;
        int totalCells = gridSize * gridSize;

        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                if (gameboard.getNumber(row, col) != 0) {
                    filledCells++;
                }
            }
        }

        return (int) ((filledCells / (double) totalCells) * 100);
    }

    public void applyNotesToCells() {
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                Set<Integer> notes = board.getNotesInCell(row, col);
                int cellNumber = gameboard.getNumber(row, col);
                if (cellNumber != 0) {
                    checkCellsForNotes(row, col, cellNumber, "hide");
                    board.setHiddenProperty(row, col, true);
                }
                for (int note : notes) {
                    if (isNumberInRow(row, note)
                            || isNumberInColumn(col, note)
                            || isNumberInSubGrid(row, col, note)) {
                        updateHideList(row, col, note, "hide");
                    }
                }
            }
        }
    }

    private boolean isNumberInRow(int row, int number) {
        for (int col = 0; col < gridSize; col++) {
            if (gameboard.getNumber(row, col) == number) {
                return true;
            }
        }
        return false;
    }

    private boolean isNumberInColumn(int col, int number) {
        for (int row = 0; row < gridSize; row++) {
            if (gameboard.getNumber(row, col) == number) {
                return true;
            }
        }
        return false;
    }

    private boolean isNumberInSubGrid(int row, int col, int number) {
        int startRow = row - (row % nSize);
        int startCol = col - (col % nSize);
        for (int i = startRow; i < startRow + nSize; i++) {
            for (int j = startCol; j < startCol + nSize; j++) {
                if (gameboard.getNumber(i, j) == number) {
                    return true;
                }
            }
        }
        return false;
    }

    // A multiplayer function to send the progress update to the other player
    public void sendProgress() {
        logger.debug("Sending progress");
        if (networkOut != null) {
            int progress = calculateProgress();
            playerProgressBar.setValue(progress);
            logger.debug("Progress: {}", progress);
            networkOut.println("PROGRESS " + progress);
        }
    }

    // This is what makes a new game, either from the start menu or the new game button inside the
    // game window
    public void newGame() {
        if (!isCustomBoard) {
            gameboard.clear();
            hintList.clear();
            moveList.clear();
            wrongMoveList.clear();
            windowManager.setHeart();
            board.clearUnPlaceableCells();
            board.clearWrongNumbers();
            timer.stop();
            timer.reset();
            board.clearNotes();
            gameboard.clearInitialBoard();
            if (nSize == kSize) {
                AlgorithmXSolver.createXSudoku(gameboard);
            } else {
                HeuristicSolver.createPlayableSudoku(gameboard);
            }
            fillHintList();
        } else {
            gameboard.setGameBoard(
                    deepCopyBoard(gameboard.getInitialBoard()));
            moveList.clear();
            wrongMoveList.clear();
            windowManager.setHeart();
            timer.stop();
            timer.reset();
            board.clearNotes();
        }
        displayNumbersVisually();
        gameIsStarted = true;
        board.requestFocusInWindow();
        solveButton.setEnabled(true);
        if (Config.getEnableTimer()) {
            timer.start();
        }

        if (Config.getEnableKillerSudoku()) {
            generateKillerSudokuCages();
        }
        setInitialBoardColor();

        if (!isCustomBoard) {
            newGameButton.setText(NEW_GAME);
        }
        updateNumberCount();
    }

    private JButton createButton(String text, int height) {
        JButton button = new JButton(text);
        button.setMaximumSize(new Dimension(100, height));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Create a margin around the button
        int topBottomMargin = 5; // Space above and below the button
        int leftRightMargin = 10; // Space to the left and right of the button
        button.setBorder(
                BorderFactory.createEmptyBorder(
                        topBottomMargin, leftRightMargin, topBottomMargin, leftRightMargin));

        return button;
    }

    public void fillHintList() {
        int[][] solutionBoard;
        solutionBoard = gameboard.getSolvedBoard();
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                if (gameboard.getInitialNumber(row, col) == 0) {
                    assert solutionBoard != null;
                    hintList.add(new Move(row, col, solutionBoard[row][col], 0));
                }
            }
        }
    }

    public boolean isSudokuCompleted() {
        for (int row = 0; row < gameboard.getDimensions(); row++) {
            for (int col = 0; col < gameboard.getDimensions(); col++) {
                if (gameboard.getNumber(row, col) == 0) {
                    logger.debug("Sudoku is not completed");
                    return false;
                }
            }
        }
        logger.debug("Sudoku is completed");
        return true;
    }

    public void provideHint() {
        if (!wrongMoveList.isEmpty()) {
            // Update the number count

            Move wrongMove = wrongMoveList.removeFirst();
            int row = wrongMove.row();
            int col = wrongMove.column();
            int number = wrongMove.number();

            gameboard.setNumber(row, col, 0);
            board.setCellNumber(row, col, 0);
            board.visualizeCell(row, col, Color.red);
            board.setHiddenProperty(row, col, false);
            checkCellsForNotes(row, col, number, "show");

        } else if (!hintList.isEmpty()) {
            int hintIndex = random.nextInt(hintList.size());
            Move hintMove = hintList.get(hintIndex);
            hintList.remove(hintIndex);

            int row = hintMove.row();
            int col = hintMove.column();
            int number = hintMove.number();

            checkCellsForNotes(row, col, number, "hide");
            gameboard.setNumber(row, col, number);
            board.setCellNumber(row, col, number);
            gameboard.setInitialNumber(row, col, number);

            // Visualize the hint
            board.visualizeCell(row, col, Color.blue);
            updateNumberCount();
            checkCompletionAndOfferNewGame();

        } else {
            logger.info("No more hints available.");
        }
        updateNumberCount();
        board.revalidate();
        board.repaint();
    }

    // This function is run after every move, making sure to check when the game is over, either by
    // losing all lives or completing the sudoku.

    public void checkCompletionAndOfferNewGame() {
        if (Boolean.FALSE.equals(usedSolveButton)) {
            if (isGameOver()) {
                handleGameOver();
            } else if (isSudokuCompleted() && !testMode()) {
                handleGameCompletion();
            }
        } else {
            // Make a pop-up to tell the user that they have used the solved button
            JOptionPane.showMessageDialog(
                    null,
                    "You have used the solve button, you will not be able to enter the"
                            + " leaderboard");
        }
        usedSolveButton = false;
    }

    private void handleGameOver() {
        timer.stop();
        String message =
                "Game Over! You've run out of hearts.\n\nWould you like to start a new game?";
        promptForNewGame(message, "Game Over");
    }

    private void handleGameCompletion() {
        timer.stop();
        if (networkOut != null) {
            networkOut.println("COMPLETED " + "Player1");
        }
        if (Config.getEnableTimer() || isNetworkGame) {
            promptForLeaderboardEntry();
        }
        String message =
                "Congratulations! You've completed the Sudoku in\n"
                        + timer.getTimeString()
                        + "\n\nWould you like to start a new game?";
        promptForNewGame(message, "Game Completed");
    }

    private void promptForNewGame(String message, String title) {
        Object[] options = {isCustomBoard ? "Replay" : NEW_GAME, "Close"};
        int response =
                JOptionPane.showOptionDialog(
                        null,
                        message,
                        title,
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE,
                        null,
                        options,
                        options[0]);

        if (response == JOptionPane.YES_OPTION) {
            try {
                startGame();
            } catch (Exception e) {
                logger.error("Error creating new game: {}", e.getMessage());
            }
        } else {
            returnToMainMenu();
        }
    }

    // If the game is over and not solved with the solved button, the player will be prompted to
    // enter the leaderboard
    private void promptForLeaderboardEntry() {
        Preferences pref = Preferences.userNodeForPackage(this.getClass());
        String storedUsername = pref.get("username", "");

        String username =
                JOptionPane.showInputDialog(
                        null, "Enter your name for the leaderboard:", storedUsername);
        if (username != null && !username.trim().isEmpty()) {
            pref.put("username", username.trim());
            String difficulty = Config.getDifficulty();
            int time = timer.getTimeToInt();
            UpdateLeaderboard.addScore("jdbc:sqlite:sudoku.db", username, difficulty, time, Config.getN(), Config.getK());
        }
    }

    private void returnToMainMenu() {
        JFrame frame = windowManager.getFrame();
        StartMenuWindowManager startMenu = new StartMenuWindowManager(frame, 1000, 1000);
        StartMenu startMenu1 = new StartMenu(startMenu);
        startMenu1.initialize();
    }

    private boolean isGameOver() {
        return windowManager.checkIfLostGame();
    }

    private boolean testMode() {
        return System.getProperty("testMode") != null;
    }

    // Visually displays the buttons on the screen, and adds action-listeners to each button
    private void displayButtons() {
        restartButton = createButton("Restart", 30);
        solveButton = createButton("Solve", 30);
        newGameButton = createButton(NEW_GAME, 30);
        eraseButton = createButton("Erase", 30);
        undoButton = createButton("Undo", 300);
        hintButton = createButton("Hint", 30);
        goBackButton = createButton("Go Back", 30);

        noteButton.setBackground(backgroundColor);
        noteButton.setForeground(accentColor);
        noteButton.setBorder(
                BorderFactory.createCompoundBorder(
                        new LineBorder(accentColor, 1), new EmptyBorder(5, 5, 5, 5)));

        solveButton.setEnabled(false);

        JButton[] buttons = {
                restartButton,
                solveButton,
                newGameButton,
                eraseButton,
                undoButton,
                hintButton,
                goBackButton
        };

        applyButtonStyles(buttons);

        restartButton.addActionListener(
                e -> {
                    moveList.clear();
                    wrongMoveList.clear();
                    timer.stop();
                    timer.reset();
                    board.clearNotes();
                    gameboard.setGameBoard(deepCopyBoard(gameboard.getInitialBoard()));
                    displayNumbersVisually();
                    setInitialBoardColor();
                    timer.start();
                    gameIsStarted = true;
                    board.requestFocusInWindow();
                    solveButton.setEnabled(true);
                    updateNumberCount();
                });

        solveButton.addActionListener(
                e -> {
                    usedSolveButton = true;
                    board.clearNotes();
                    timer.stop();
                    gameboard.setGameBoard(gameboard.getSolvedBoard());
                    usedSolveButton = true;
                    updateNumberCount();
                    displayNumbersVisually();
                    board.revalidate();
                    board.repaint();
                    if (!GraphicsEnvironment.isHeadless()) {
                        checkCompletionAndOfferNewGame();
                    }
                });

        newGameButton.addActionListener(
                e -> {
                    try {
                        startGame();
                    } catch (Board.BoardNotCreatable ex) {
                        throw new RuntimeException(ex);
                    }
                });

        eraseButton.addActionListener(
                e -> {
                    board.requestFocusInWindow();
                    eraseNumber();
                    updateNumberCount();
                });

        undoButton.addActionListener(
                e -> {
                    board.requestFocusInWindow();
                    undoMove();
                    updateNumberCount();
                });

        hintButton.addActionListener(
                e -> {
                    board.requestFocusInWindow();
                    provideHint();
                });

        noteButton.addActionListener(e -> board.requestFocusInWindow());

        goBackButton.addActionListener(
                e -> {
                    isNetworkGame = false;
                    int response =
                            JOptionPane.showConfirmDialog(
                                    null,
                                    "Are you sure you want to go back to the main menu?",
                                    "Go back to main menu",
                                    JOptionPane.YES_NO_OPTION);
                    if (response == JOptionPane.YES_OPTION) {
                        JFrame frame = windowManager.getFrame();
                        StartMenuWindowManager startMenu =
                                new StartMenuWindowManager(frame, 1000, 1000);
                        StartMenu startMenu1 = new StartMenu(startMenu);
                        startMenu1.initialize();
                    }
                });

        for (JButton button : buttons) {
            windowManager.addComponentToButtonPanel(button);
            windowManager.addComponentToButtonPanel(Box.createRigidArea(new Dimension(10, 10)));
        }
        windowManager.addComponentToButtonPanel(noteButton);
        if (!(nSize == 3) || !(kSize == 3)) {
            noteButton.setEnabled(false);
        }
    }

    // The styling of the buttons visual look
    private void applyButtonStyles(JButton[] buttons) {
        int padding = 5;
        for (JButton button : buttons) {
            button.setBackground(backgroundColor);
            button.setForeground(accentColor);
            button.setBorder(
                    BorderFactory.createCompoundBorder(
                            new LineBorder(accentColor, 1),
                            new EmptyBorder(padding, padding, padding, padding)));
        }
    }

    // A method that separates which numbers were originally there and which the player has set.
    private void setInitialBoardColor() {
        for (int row = 0; row < gameboard.getDimensions(); row++) {
            for (int col = 0; col < gameboard.getDimensions(); col++) {
                if (gameboard.getInitialNumber(row, col) != 0) {
                    board.setCellTextColor(
                            row, col, Config.getDarkMode() ? initialColor : Color.GRAY);
                } else {
                    board.setCellTextColor(
                            row, col, Config.getDarkMode() ? accentColor : Color.BLACK);
                }
            }
        }
    }

    public int[][] deepCopyBoard(int[][] original) {
        return SolverAlgorithm.deepCopyBoard(original);
    }

    public List<Move> getHintList() {
        return hintList;
    }

    public void onNumbersBoardClicked(int x, int y) {
        int chosenNumber = numbers.getNumber(x, y);
        board.setChosenNumber(chosenNumber);
        if (board.isACellMarked()) {
            int[] markedCell = board.getMarkedCell();
            int row = markedCell[0];
            int col = markedCell[1];
            makeMove(row, col, chosenNumber);

            if (Config.getEnableEasyMode()) {
                board.highlightPlaceableCells(chosenNumber);
            }
        }
    }

    public JButton getUndoButton() {
        return undoButton;
    }

    public JButton getHintButton() {
        return hintButton;
    }

    public JButton getNewGameButton() {
        return newGameButton;
    }

    public JButton getEraseButton() {
        return eraseButton;
    }

    public JButton getSolveButton() {
        return solveButton;
    }

    public JButton getRestartButton() {
        return restartButton;
    }

    public JToggleButton getNoteButton() {
        return noteButton;
    }

    public void setPlaceableNumber(int i) {
        placeableNumber = i;
    }

    public void clearBoard() {
        gameboard.clear();
        hintList.clear();
        moveList.clear();
        timer.stop();
        timer.reset();
        gameboard.clearInitialBoard();
    }

    // A killer sudoku game-mode method, which helps easy mode determine if the number can be placed
    public boolean cageContains(Point cell, int num) {
        List<Cage> cages = board.getCages();
        for (Cage cage : cages) {
            if (cage.getCells().contains(cell)) {
                logger.debug("printing numbers in cage {} {} ", cage.getId(), cage.getNumbers());
                return cage.contains(num);
            }
        }
        return false;
    }

    public int[] getUsedLives() {
        return windowManager.getUsedLives();
    }

    public SudokuBoardCanvas getBoard() {
        return board;
    }

    public NumberHub getNumbersBoard() {
        return numbers;
    }

    public void setNumbersBoard(NumberHub numberHub) {
        this.numbers = numberHub;
    }

    public boolean isGameStarted() {
        return gameIsStarted;
    }

    public void setGameIsStarted(boolean b) {
        gameIsStarted = b;
    }

    public void startGame() throws Board.BoardNotCreatable {
        newGame();
        displayNumbersVisually();
        setInitialBoardColor();
        board.requestFocusInWindow();
        gameIsStarted = true;
        board.requestFocusInWindow();
        solveButton.setEnabled(true);
        restartButton.setEnabled(true);
        hintButton.setEnabled(true);
        newGameButton.setEnabled(true);
    }
}
