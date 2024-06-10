/* (C)2024 */
package dk.dtu.game.core;

import dk.dtu.engine.core.StartMenuWindowManager;
import dk.dtu.engine.core.WindowManager;
import dk.dtu.engine.graphics.Cage;
import dk.dtu.engine.graphics.NumberHub;
import dk.dtu.engine.graphics.SudokuBoardCanvas;
import dk.dtu.engine.input.KeyboardListener;
import dk.dtu.engine.input.MouseActionListener;
import dk.dtu.engine.utility.TimerFunction;
import dk.dtu.engine.utility.UpdateLeaderboard;
import dk.dtu.game.core.solver.SolverAlgorithm;
import dk.dtu.game.core.solver.algorithmx.AlgorithmXSolver;
import dk.dtu.game.core.solver.bruteforce.BruteForceAlgorithm;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SudokuGame {
    private static final Logger logger = LoggerFactory.getLogger(SudokuGame.class);
    private final WindowManager windowManager;
    public final Board gameboard;
    public final Deque<Move> moveList = new ArrayDeque<>();
    private final ArrayList<Move> hintList = new ArrayList<>();
    public final List<Move> wrongMoveList = new ArrayList<>();

    private final int nSize;
    private final int kSize;
    int gridSize;
    int cellSize;
    private int placeableNumber = 0;
    private int nextCageId = 1;

    MouseActionListener mouseActionListener = new MouseActionListener(this);
    KeyboardListener keyboardListener = new KeyboardListener(this);

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
    private final JToggleButton noteButton = new JToggleButton("Note Mode", false);

    private boolean usedSolveButton = false;

    Random random = new SecureRandom();

    private PrintWriter networkOut;
    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();

    private boolean isCustomBoard = false;

    private boolean isNetworkGame = false;

    private final Map<Integer, Integer> numberCountMap = new HashMap<>();

    public SudokuGame(WindowManager windowManager, int n, int k, int cellSize)
            throws Board.BoardNotCreatable {
        this.windowManager = windowManager;
        try {
            gameboard = new Board(n, k);
        } catch (Board.BoardNotCreatable e) {
            throw new Board.BoardNotCreatable("This board is not possible to create");
        }
        this.nSize = n;
        this.kSize = k;
        this.gridSize = n * k;
        this.cellSize = cellSize;

        // Used to keep track of the number of times a number is placed on the board
        for (int i = 1; i <= n * k; i++) {
            numberCountMap.put(i, 0);
        }

        new Thread(this::processNetworkMessages).start();
    }

    private void processNetworkMessages() {
        while (true) {
            try {
                String message = messageQueue.take();
                processNetworkMessage(message);
            } catch (InterruptedException e) {
                logger.error("Error processing network message: {}", e.getMessage());
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
        }
    }

    private void announceWinner(String winner) {
        JOptionPane.showMessageDialog(null, "The winner is: " + winner);
    }

    public void onSudokuBoardClicked(int x, int y) {
        int row = y / (board.getWidth() / gridSize);
        int column = x / (board.getHeight() / gridSize);
        board.setMarkedCell(row, column);
        board.setChosenNumber(gameboard.getNumber(row, column));

        makeMove(row, column, placeableNumber);



        if (Config.getEnableEasyMode()) {
            board.highlightPlaceableCells(gameboard.getNumber(row, column));
        }

        if (column >= 0 && column < gridSize && row >= 0 && row < gridSize) {
            int cellIndex = row * gridSize + column; // Calculate the cell index
            logger.info("Cell {} clicked. Row: {}, Column: {}", cellIndex, row, column);
            board.removeNumber(row, column);
            board.highlightCell(row, column, true);
            checkCompletionAndOfferNewGame();
        } else {
            logger.debug("Click outside the Sudoku board or on another component");
        }
    }

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
        checkCompletionAndOfferNewGame();
    }

    public void checkCellsForNotes(int row, int col, int number, String mode) {
        checkRowAndColumnForNotes(row, col, number, mode);
        checkSubSquareForNotes(row, col, number, mode);
    }

    private void updateInitialNumberCounts() {
        // Clear previous counts
        numberCountMap.clear();
        for (int i = 1; i <= nSize * kSize; i++) {
            numberCountMap.put(i, 0);
        }
        // Count numbers in the initial board
        for (int row = 0; row < gameboard.getDimensions(); row++) {
            for (int col = 0; col < gameboard.getDimensions(); col++) {
                int number = gameboard.getInitialNumber(row, col);
                if (number > 0) {
                    updateNumberCount(number, 1);
                }
            }
        }
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
        int subSize = (int) Math.sqrt(gameboard.getDimensions());
        int startRow = row - (row % subSize);
        int startCol = col - (col % subSize);
        for (int i = startRow; i < startRow + subSize; i++) {
            for (int j = startCol; j < startCol + subSize; j++) {
                if (board.getNotesInCell(i, j).contains(number)) {
                    updateHideList(i, j, number, mode);
                }
            }
        }
    }

    private void updateHideList(int row, int col, int number, String mode) {
        if (mode.equals("show")) {
            board.removeFromHideList(row, col, number);
        } else {
            board.addToHideList(row, col, number);
        }
    }

    private void updateNumberCount(int number, int increment) {
        if (number > 0) {
            int newCount = numberCountMap.getOrDefault(number, 0) + increment;
            numberCountMap.put(number, newCount);

            // Get the maximum count for the numbers based on the grid size
            int maxCount = gridSize; // For example, 9 for a 9x9 grid

            // Update the display based on the new count
            if (newCount >= maxCount) {
                getNumbersBoard().updateNumberDisplay(number, false); // Grey out the number
            } else if (newCount >= 0) {
                getNumbersBoard().updateNumberDisplay(number, true); // Ungrey the number
            }
        }
    }


    public void makeMove(int row, int col, int number) {
        if (row >= 0 && col >= 0) {
            if (noteButton.isSelected()
                    && gameboard.getInitialNumber(row, col) == 0
                    && gameboard.getNumber(row, col) == 0
                    && number != 0) {
                makeNote(row, col, number);
            } else {
                if (Config.getEnableLives()) {
                    makeMoveWithLives(row, col, number);
                } else {
                    makeMoveWithoutLives(row, col, number);
                }

                if (number != 0) {
                    updateNumberCount(number, 1);
                }
            }
        }
    }


    private void makeMoveWithLives(int row, int col, int number) {
        if (gameboard.getInitialNumber(row, col) == 0 && !noteButton.isSelected() && number != 0) {
            board.setHiddenProperty(row, col, true);
            checkCellsForNotes(row, col, number, "hide");
            int previousNumber = gameboard.getNumber(row, col);
            board.setCellNumber(row, col, number);
            board.setChosenNumber(number);
            gameboard.setNumber(row, col, number);
            Move move = new Move(row, col, number, previousNumber);
            moveList.push(move); // Log the move for undo functionality

            int[][] solutionB = gameboard.getSolvedBoard();

            if (gameboard.getNumber(row, col) != solutionB[row][col]
                    || (Config.getEnableKillerSudoku() && cageContains(new Point(col, row), number))) {
                windowManager.removeHeart();
                board.setWrongNumber(row, col, number);
                wrongMoveList.add(move);
            }
        }
    }

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

            board.setHiddenProperty(row, col, true);
            checkCellsForNotes(row, col, number, "hide");
            int previousNumber = gameboard.getNumber(row, col);
            board.setCellNumber(row, col, number);
            board.setChosenNumber(number);
            gameboard.setNumber(row, col, number);
            Move move = new Move(row, col, number, previousNumber);
            moveList.push(move); // Log the move for undo functionality

            int[][] solutionB = gameboard.getSolvedBoard();

            if (gameboard.getNumber(row, col) != solutionB[row][col]) {
                wrongMoveList.add(move);
            }
        }
    }


    public void makeNote(int row, int col, int number) {
        if (gameIsStarted) {
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

            if (gameboard.getInitialNumber(row, col) == 0) {
                if (Config.getEnableKillerSudoku()) {
                    Cage cage = board.getCage(row, col);
                    if (cage != null) {
                        cage.removeCurrentNumber(number);
                    }
                }
                updateNumberCount(number, -1);
                board.setHiddenProperty(row, col, false);
                board.removeNumber(row, col);
                gameboard.setNumber(row, col, 0);
            }
        }
    }


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
            updateNumberCount(number, -1);
            updateNumberCount(prevNumber, 1);
            gameboard.setNumber(row, col, prevNumber);
            board.setCellNumber(row, col, prevNumber);
            logger.debug("Undo move: Row: {}, Column: {}, Number: {}", row, col, prevNumber);
        }
    }


    public void createBoard(int n, int k, int cellSize) {
        board = new SudokuBoardCanvas(n, k, cellSize);
        board.setLocation(50, 50);
        board.setFocusable(true);

        board.addMouseListener(mouseActionListener);

        board.addKeyListener(keyboardListener);

        gameboard.setInitialBoard(deepCopyBoard(gameboard.getGameBoard()));

        numbers = new NumberHub(n, 40) {};

        numbers.setLocation(50, 50);
        numbers.setFocusable(true);

        numbers.addMouseListener(mouseActionListener);
        numbers.addKeyListener(keyboardListener);

        timer = new TimerFunction();
        timer.setFocusable(true);
    }

    public void generateRandomCages() {
        int[][] solvedBoard = gameboard.getSolvedBoard();
        board.clearCages();
        Random rand = new Random();
        boolean[][] used = new boolean[gridSize][gridSize];

        int minCageSize = 2;
        int maxCageSize = 4;
        double smallCageProbability = switch (Config.getDifficulty()) {
            case "medium" -> {
                maxCageSize = 4;
                yield 0.8; // 90% chance to create smaller cages
            }
            case "hard" -> {
                maxCageSize = 5;
                yield 0.7; // 80% chance to create smaller cages
            }
            case "extreme" -> {
                maxCageSize = 5;
                yield 0.5; // 60% chance to create smaller cages
            }
            default -> {
                maxCageSize = 4;
                yield 1.0; // 100% chance to create smaller cages
            }
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
                                if (r + 1 < gridSize && !used[r + 1][c]) queue.add(new Point(c, r + 1));
                                if (r - 1 >= 0 && !used[r - 1][c]) queue.add(new Point(c, r - 1));
                                if (c + 1 < gridSize && !used[r][c + 1]) queue.add(new Point(c + 1, r));
                                if (c - 1 >= 0 && !used[r][c - 1]) queue.add(new Point(c - 1, r));
                            }
                        }
                    }

                    if (!cageCells.isEmpty()) {
                        Cage cage = new Cage(cageCells, sum, nextCageId++);
                        for (Point cell : cageCells) {
                            int number = solvedBoard[cell.y][cell.x];
                            cage.addSolutionNumber(number);  // Ensure numbers are added to the cage
                        }
                        board.addCage(cage.getId(), cage);
                    }
                }
            }
        }

        adjustInitialNumbersVisibility(solvedBoard);
    }





    private void adjustInitialNumbersVisibility(int[][] solvedBoard) {
        Random rand = new Random();

        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                boolean keepNumber = switch (Config.getDifficulty()) {
                    case "medium" -> rand.nextDouble() < 0.8;
                    case "hard" -> rand.nextDouble() < 0.5; // Keep about 30% of the numbers
                    case "extreme" -> false;
                    default -> true; // Remove all numbers
                };
                if (!keepNumber) {
                    board.removeNumber(row, col);
                    gameboard.setNumber(row, col, 0);
                } else {
                    board.setCellNumber(row, col, solvedBoard[row][col]);
                }
            }
        }
    }



    public void initialize(int n, int k, int cellSize) {
        createBoard(n, k, cellSize);

        displayButtons();
        windowManager.drawBoard(board);
        windowManager.setupNumberAndTimerPanel(timer, numbers);
        windowManager.layoutComponents(timer, numbers);
        startGame();
        if(Config.getEnableKillerSudoku()){
            generateRandomCages();
        }

        if (Config.getEnableKillerSudoku()) {
            for (int row = 0; row < gridSize; row++) {
                for (int col = 0; col < gridSize; col++) {
                    gameboard.setInitialNumber(row, col, 0);
                }
            }
        }
    }

    // This method is used to initialize the game with a custom imported board
    public void initializeCustom(int[][] customBoard) {
        Config.setEnableLives(false);
        isCustomBoard = true;
        createBoard(Config.getN(), Config.getK(), Config.getCellSize());
        displayButtons();
        windowManager.drawBoard(board);
        windowManager.setupNumberAndTimerPanel(timer, numbers);

        windowManager.layoutComponents(timer, numbers);

        gameboard.setInitialBoard(customBoard);
        gameboard.setGameBoard(deepCopyBoard(customBoard));

        if (nSize == kSize) {
            AlgorithmXSolver.solveExistingBoard(gameboard);

        } else {
            BruteForceAlgorithm.createSudoku(gameboard);
        }

        updateInitialNumberCounts();
        fillHintList();
        if (Config.getEnableTimer()) {
            timer.start();
        }
        displayNumbersVisually();
        setInitialBoardColor();
        gameIsStarted = true;

        solveButton.setEnabled(true);

        if (isNetworkGame) {
            solveButton.setEnabled(false);
            hintButton.setEnabled(false);
            newGameButton.setEnabled(false);
            restartButton.setEnabled(false);
        } else {
            windowManager.setHeart();
        }
        isNetworkGame = false;

        // Set initial numbers to zero if Killer Sudoku is enabled
        if (Config.getEnableKillerSudoku()) {
            for (int row = 0; row < gridSize; row++) {
                for (int col = 0; col < gridSize; col++) {
                    gameboard.setInitialNumber(row, col, 0);
                }
            }
        }
    }


    public void newGame() {

        if (!isCustomBoard) {
            gameboard.clear();
            hintList.clear();
            moveList.clear();
            wrongMoveList.clear();
            windowManager.setHeart();
            board.clearUnplacableCells();
            board.clearWrongNumbers();
            timer.stop();
            timer.reset();
            board.clearNotes();
            gameboard.clearInitialBoard();
            if (nSize == kSize) {
                AlgorithmXSolver.createXSudoku(gameboard);
            } else {
                BruteForceAlgorithm.createSudoku(gameboard);
            }
            fillHintList();
        } else {
            gameboard.setGameBoard(
                    deepCopyBoard(gameboard.getInitialBoard())); // Reset to custom board
            gameboard.clearInitialBoard(); // Clear previous initial state
            gameboard.setInitialBoard(
                    deepCopyBoard(gameboard.getGameBoard())); // Set the current state as initial
            moveList.clear();
            wrongMoveList.clear();
            timer.stop();
            timer.reset();
            board.clearNotes();
        }
        updateInitialNumberCounts();
        displayNumbersVisually();
        setInitialBoardColor();
        gameIsStarted = true;
        board.requestFocusInWindow();
        solveButton.setEnabled(true);
        if (Config.getEnableTimer()) {
            timer.start();
        }
        if(Config.getEnableKillerSudoku()){
            generateRandomCages();
            for(int row = 0; row < gridSize; row++){
                for(int col = 0; col < gridSize; col++){
                    gameboard.setInitialNumber(row, col, 0);
                }
            }
        }
    }

    private JButton createButton(String text, int height) {
        JButton button = new JButton(text);
        button.setMaximumSize(new Dimension(100, height));
        button.setAlignmentX(Component.CENTER_ALIGNMENT); // Align buttons for BoxLayout

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
        if (nSize == kSize) {
            solutionBoard = AlgorithmXSolver.getSolutionBoard();
        } else {
            solutionBoard = BruteForceAlgorithm.getSolvedBoard();
        }
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
            updateNumberCount(number, -1);

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

            // Update the number count
            updateNumberCount(number, 1);

            checkCompletionAndOfferNewGame();
        } else {
            logger.info("No more hints available.");
        }
    }


    public void checkCompletionAndOfferNewGame() {
        boolean completedSuccessfully = isSudokuCompleted() && !testMode();
        boolean isGameOver = isGameOver();

        if (completedSuccessfully || isGameOver) {
            String message = null;
            if (!usedSolveButton) {
                timer.stop();

                if (completedSuccessfully) {
                    // Preferences object to store and retrieve the username
                    Preferences pref = Preferences.userNodeForPackage(this.getClass());
                    String storedUsername = pref.get("username", "");

                    if (networkOut != null) {
                        networkOut.println("COMPLETED " + "Player1");
                    }

                    // Prompt user for their username
                    String username =
                            JOptionPane.showInputDialog(
                                    null, "Enter your name for the leaderboard:", storedUsername);
                    if (username != null && !username.trim().isEmpty()) {
                        // Store the username in preferences
                        pref.put("username", username.trim());

                        // Add the completion details to the leaderboard
                        String difficulty = Config.getDifficulty();
                        int time = timer.getTimeToInt(); // returns time

                        UpdateLeaderboard.addScore(
                                "jdbc:sqlite:sudoku.db", username, difficulty, time);
                    }

                    message =
                            "Congratulations! You've completed the Sudoku in\n"
                                    + timer.getTimeString()
                                    + "\n\n"
                                    + "Would you like to start a new game?";
                } else { // This is the game over scenario
                    message =
                            """
                            Game Over! You've run out of hearts.

                            Would you like to start a new game?""";
                }
            }

            Object[] options = {"New Game", "Close"};
            int response =
                    JOptionPane.showOptionDialog(
                            null,
                            message,
                            completedSuccessfully ? "Game Completed" : "Game Over",
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
            }
        }
    }

    private boolean isGameOver() {
        return windowManager.checkGameOver();
    }

    private boolean testMode() {
        return System.getProperty("testMode") != null;
    }

    private void displayButtons() {

        restartButton = createButton("Restart", 30);
        solveButton = createButton("Solve", 30);
        newGameButton = createButton("New Game", 30);
        eraseButton = createButton("Erase", 30);
        undoButton = createButton("Undo", 300);
        hintButton = createButton("Hint", 30);
        JButton goBackButton = createButton("Go Back", 30);

        // Set the solved button to be disabled at the start of the game
        solveButton.setEnabled(false);

        restartButton.addActionListener(
                e -> {
                    // Reset the game state to the initial board configuration
                    moveList.clear();
                    wrongMoveList.clear();
                    timer.stop();
                    timer.reset();
                    board.clearNotes();

                    // Reinitialize the board with the initial puzzle
                    gameboard.setGameBoard(deepCopyBoard(gameboard.getInitialBoard()));
                    displayNumbersVisually();
                    setInitialBoardColor();

                    // Restart the timer
                    timer.start();

                    gameIsStarted = true;
                    board.requestFocusInWindow();
                    solveButton.setEnabled(true);
                });
        solveButton.addActionListener(
                e -> {
                    board.clearNotes();
                    timer.stop();
                    if (nSize == kSize) {

                        gameboard.setGameBoard(
                                Objects.requireNonNull(AlgorithmXSolver.getSolutionBoard()));
                    } else {
                        gameboard.setGameBoard(BruteForceAlgorithm.getSolvedBoard());
                    }
                    usedSolveButton = true;
                    checkCompletionAndOfferNewGame();
                    usedSolveButton = false;
                });

        newGameButton.addActionListener(e -> startGame());

        eraseButton.addActionListener(
                e -> {
                    board.requestFocusInWindow();
                    eraseNumber();
                });

        undoButton.addActionListener(
                e -> {
                    board.requestFocusInWindow();
                    undoMove();
                });

        hintButton.addActionListener(
                e -> {
                    board.requestFocusInWindow();
                    provideHint();
                });
        noteButton.addActionListener(e -> board.requestFocusInWindow());
        goBackButton.addActionListener(
                e -> {

                    // Make a popup to ask if they want to go back
                    int response =
                            JOptionPane.showConfirmDialog(
                                    null,
                                    "Are you sure you want to go back to the main menu?",
                                    "Go back to main menu",
                                    JOptionPane.YES_NO_OPTION);
                    if (response == JOptionPane.YES_OPTION) {
                        // Get the frame
                        JFrame frame = windowManager.getFrame();
                        StartMenuWindowManager startMenu =
                                new StartMenuWindowManager(frame, 1000, 1000);
                        StartMenu startMenu1 = new StartMenu(startMenu);
                        startMenu1.initialize();
                    }
                });

        windowManager.addComponentToButtonPanel(restartButton);
        windowManager.addComponentToButtonPanel(Box.createRigidArea(new Dimension(10, 10)));
        windowManager.addComponentToButtonPanel(solveButton);
        windowManager.addComponentToButtonPanel(Box.createRigidArea(new Dimension(10, 10)));
        windowManager.addComponentToButtonPanel(newGameButton);
        windowManager.addComponentToButtonPanel(Box.createRigidArea(new Dimension(10, 10)));
        windowManager.addComponentToButtonPanel(eraseButton);
        windowManager.addComponentToButtonPanel(Box.createRigidArea((new Dimension(10, 10))));
        windowManager.addComponentToButtonPanel(undoButton);
        windowManager.addComponentToButtonPanel(Box.createRigidArea((new Dimension(10, 10))));
        windowManager.addComponentToButtonPanel(hintButton);
        windowManager.addComponentToButtonPanel(Box.createRigidArea((new Dimension(10, 10))));
        windowManager.addComponentToButtonPanel(noteButton);

        windowManager.addGoBackButton(goBackButton);
    }

    private void setInitialBoardColor() {
        for (int row = 0; row < gameboard.getDimensions(); row++) {
            for (int col = 0; col < gameboard.getDimensions(); col++) {
                if (gameboard.getInitialNumber(row, col) != 0) {
                    board.setCellTextColor(row, col, Color.GRAY);
                } else {
                    board.setCellTextColor(row, col, Color.BLACK);
                }
            }
        }
    }

    public int[][] deepCopyBoard(int[][] original) {
        return SolverAlgorithm.deepCopyBoard(original);
    }

    public void render() {
        if (gameIsStarted) {
            displayNumbersVisually();
        }
    }

    public void update() {
        render();
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


    public boolean cageContains(Point cell, int num) {
        List<Cage> cages = board.getCages();
        for (Cage cage : cages) {
            if (cage.getCells().contains(cell)) {
                System.out.println("printing numbers in cage " + cage.getId() + " " + Arrays.toString(cage.getNumbers()));
                return cage.contains(num);
            }
        }
        return false;
    }






    public int getLives() {
        return windowManager.getHearts();
    }

    public SudokuBoardCanvas getBoard() {
        return board;
    }

    public void setNumbersBoard(NumberHub numberHub) {
        this.numbers = numberHub;
    }

    public NumberHub getNumbersBoard() {
        return numbers;
    }

    public boolean isGameStarted() {
        return gameIsStarted;
    }

    public void setGameIsStarted(boolean b) {
        gameIsStarted = b;
    }

    public void startGame() {
        newGame();
        displayNumbersVisually();
        setInitialBoardColor();
        gameIsStarted = true;
        board.requestFocusInWindow();
        solveButton.setEnabled(true);
        restartButton.setEnabled(true);
        hintButton.setEnabled(true);
        newGameButton.setEnabled(true);
    }
}
