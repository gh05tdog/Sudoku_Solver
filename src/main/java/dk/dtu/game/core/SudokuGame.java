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
    private final Logger logger = LoggerFactory.getLogger(SudokuGame.class);
    public final Board gameboard;
    public final Deque<Move> moveList = new ArrayDeque<>();
    public final List<Move> wrongMoveList = new ArrayList<>();
    private final WindowManager windowManager;
    private final ArrayList<Move> hintList = new ArrayList<>();
    private final int nSize;
    private final int kSize;
    private final JToggleButton noteButton = new JToggleButton("Note Mode", false);
    private final BlockingQueue<String> messageQueue = new LinkedBlockingQueue<>();
    int gridSize;
    int cellSize;
    private int placeableNumber = 0;
    private int nextCageId = 1;

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
    private boolean usedSolveButton = false;
    private PrintWriter networkOut;
    private boolean isCustomBoard = false;
    private boolean isNetworkGame = false;
    private final JProgressBar opponentProgressBar;
    private final JProgressBar playerProgressBar;

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


        new Thread(this::processNetworkMessages).start();

        // Initialize the opponent progress bar
        opponentProgressBar = new JProgressBar(0, 100);
        opponentProgressBar.setStringPainted(true);
        opponentProgressBar.setString("Opponent's Progress");

        playerProgressBar = new JProgressBar(0, 100);
        playerProgressBar.setStringPainted(true);
        playerProgressBar.setString("Your Progress");
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
        System.out.println("Received message: " + message);

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
                int progress = Integer.parseInt(parts[1]);
                SwingUtilities.invokeLater(() -> updateOpponentProgress(progress));
                break;
        }
    }

    private void updateOpponentProgress(int progress) {
        //Check if my progress is higher than the opponent's progress
        if (progress != calculateProgress()) {
            opponentProgressBar.setValue(progress);
        }
    }

    private void announceWinner(String winner) {
        JOptionPane.showMessageDialog(null, "The winner is: " + winner);
    }

    public void onSudokuBoardClicked(int x, int y) {
        int row = y / (board.getHeight() / gridSize);
        int column = x / (board.getWidth() / gridSize);
        if (column >= 0 && column < gridSize && row >= 0 && row < gridSize) {
            board.setMarkedCell(row, column);
            board.setChosenNumber(gameboard.getNumber(row, column));

            // Ensure placeableNumber is set correctly and the cell is empty before making a move
            if (placeableNumber != 0 && gameboard.getInitialNumber(row, column) == 0 && gameboard.getNumber(row, column) == 0) {
                makeMove(row, column, placeableNumber);
            }

            if (Config.getEnableEasyMode()) {
                board.highlightPlaceableCells(gameboard.getNumber(row, column));
            }

            int cellIndex = row * gridSize + column;
            logger.info("Cell {} clicked. Row: {}, Column: {}", cellIndex, row, column);

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

    private void updateNumberCount() {

        //Loop through the board and add all the numbers to a list,
        // then check if each number is equal to the max needed number,
        // if it is, then update the number display
        List<Integer> numbers = new ArrayList<>();
        for (int row = 0; row < gameboard.getDimensions(); row++) {
            for (int col = 0; col < gameboard.getDimensions(); col++) {
                numbers.add(gameboard.getNumber(row, col));
            }
        }


        for (int i = 1; i <= gameboard.getDimensions(); i++) {
            int count = Collections.frequency(numbers, i);
            getNumbersBoard().updateNumberDisplay(i, count != gameboard.getDimensions());
        }

    }

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
        if(isNetworkGame)
        {
            sendProgress();
        }
    }


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

    private void applyMove(int row, int col, int number, int previousNumber) {
        board.setHiddenProperty(row, col, true);
        checkCellsForNotes(row, col, number, "hide");
        board.setCellNumber(row, col, number);
        board.setChosenNumber(number);
        gameboard.setNumber(row, col, number);
        Move move = new Move(row, col, number, previousNumber);
        moveList.push(move); // Log the move for undo functionality
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
            gameboard.setNumber(row, col, prevNumber);
            board.setCellNumber(row, col, prevNumber);
            logger.debug("Undo move: Row: {}, Column: {}, Number: {}", row, col, prevNumber);
        }
        if(isNetworkGame){
            sendProgress();
        }
    }

    public void createBoard(int n, int k, int cellSize) {
        board = new SudokuBoardCanvas(n, k, cellSize);
        board.setLocation(50, 50);
        board.setFocusable(true);

        board.addMouseListener(mouseActionListener);

        board.addKeyListener(keyboardListener);

        gameboard.setInitialBoard(deepCopyBoard(gameboard.getGameBoard()));

        numbers = new NumberHub(n, 40) {
        };

        numbers.setLocation(50, 50);
        numbers.setFocusable(true);

        numbers.addMouseListener(mouseActionListener);
        numbers.addKeyListener(keyboardListener);

        timer = new TimerFunction();
        timer.setFocusable(true);
    }

    public void generateKillerSudokuCages() {
        int[][] solvedBoard = gameboard.getSolvedBoard();
        board.clearCages();
        Random rand = new Random();
        boolean[][] used = new boolean[gridSize][gridSize];

        int minCageSize = 1;
        int maxCageSize = 4;
        double smallCageProbability = switch (Config.getDifficulty()) {
            case "medium" -> 0.9; // 90% chance to create smaller cages
            case "hard" -> 0.7;   // 50% chance to create smaller cages
            case "extreme" -> 0.5; // 50% chance to create smaller cages
            default -> 1.0;       // 100% chance to create smaller cages for other difficulties
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

        adjustInitialNumbersVisibility();
    }

    public void adjustInitialNumbersVisibility() {
        Random rand = new Random();
        int[][] solvedBoard = gameboard.getSolvedBoard();
        hintList.clear();

        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                boolean keepNumber =
                        switch (Config.getDifficulty()) {
                            case "easy" -> rand.nextDouble() < 0.5; // Keep about 50% of the numbers
                            case "medium" -> rand.nextDouble() < 0.3;
                            case "hard" -> rand.nextDouble() < 0.1; // Keep about 30% of the numbers
                            case "extreme" -> false;
                            default -> true; // Remove all numbers
                        };
                if (!keepNumber) {
                    board.removeNumber(row, col);
                    gameboard.setNumber(row, col, 0);
                    gameboard.setInitialNumber(row, col, 0);
                } else {
                    int number = solvedBoard[row][col];
                    board.setCellNumber(row, col, number);
                    gameboard.setInitialNumber(row, col, number);
                    gameboard.setNumber(row, col, number);


                    Cage cage = board.getCageContainingCell(row, col);
                    if (cage != null) {
                        cage.addCurrentNumber(number); // Add the initial number to the cage
                    }
                }
            }
        }
        setInitialBoardColor();
        fillHintList();
    }

    public void initialize(int n, int k, int cellSize) {
        createBoard(n, k, cellSize);

        displayButtons();
        windowManager.drawBoard(board);
        windowManager.setupNumberAndTimerPanel(timer, numbers);
        windowManager.layoutComponents(timer, numbers);
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
        windowManager.setupNumberAndTimerPanel(timer, numbers);
        windowManager.layoutComponents(timer, numbers);
        gameboard.setInitialBoard(customBoard);
        gameboard.setGameBoard(deepCopyBoard(customBoard));

        if (nSize == kSize) {
            AlgorithmXSolver.solveExistingBoard(gameboard);
        } else {
            BruteForceAlgorithm.createSudoku(gameboard);
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
            windowManager.addProgressBar(opponentProgressBar,2);
            windowManager.addProgressBar(playerProgressBar,3);
            calculateProgress();
        } else {
            windowManager.setHeart();
        }


        newGameButton.setText("Replay");
        board.requestFocusInWindow();
    }

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

    public void sendProgress() {
        System.out.println("Sending progress");
        if (networkOut != null) {
            int progress = calculateProgress();
            playerProgressBar.setValue(progress);
            System.out.println("Progress: " + progress);
            networkOut.println("PROGRESS " + progress);
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
            gameboard.setGameBoard(deepCopyBoard(gameboard.getInitialBoard())); // Reset to custom board
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
            newGameButton.setText("New Game");
        }
        updateNumberCount();
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

            checkCompletionAndOfferNewGame();

        } else {
            logger.info("No more hints available.");
        }
        updateNumberCount();
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

                    if (networkOut != null) {
                        networkOut.println("COMPLETED " + "Player1");
                    }
                    if (Config.getEnableTimer() || isNetworkGame) {
                        Preferences pref = Preferences.userNodeForPackage(this.getClass());
                        String storedUsername = pref.get("username", "");


                        // Prompt user for their username
                        String username = JOptionPane.showInputDialog(null,
                                "Enter your name for the leaderboard:", storedUsername);
                        if (username != null && !username.trim().isEmpty()) {
                            // Store the username in preferences
                            pref.put("username", username.trim());

                            // Add the completion details to the leaderboard
                            String difficulty = Config.getDifficulty();
                            int time = timer.getTimeToInt(); // returns time

                            UpdateLeaderboard.addScore(
                                    "jdbc:sqlite:sudoku.db", username, difficulty, time);
                        }
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
\s
                                    Would you like to start a new game?""";
                }
            }

            Object[] options = {"New Game", "Close"};
            if (isCustomBoard) {
                options[0] = "Replay";
            }

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

        solveButton.setEnabled(false);

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
                    board.clearNotes();
                    timer.stop();
                    if (nSize == kSize) {
                        gameboard.setGameBoard(
                                Objects.requireNonNull(AlgorithmXSolver.getSolutionBoard()));
                    } else {
                        gameboard.setGameBoard(BruteForceAlgorithm.getSolvedBoard());
                    }
                    usedSolveButton = true;
                    updateNumberCount();
                    checkCompletionAndOfferNewGame();
                    usedSolveButton = false;

                });

        newGameButton.addActionListener(e -> startGame());

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
                    //Ensure that the game is not a network game
                    isNetworkGame = false;
                    // Make a popup to ask if they want to go back
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
                System.out.println(
                        "printing numbers in cage "
                                + cage.getId()
                                + " "
                                + Arrays.toString(cage.getNumbers()));
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

    public void startGame() {
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
