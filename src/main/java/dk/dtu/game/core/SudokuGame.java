/* (C)2024 */
package dk.dtu.game.core;

import dk.dtu.engine.core.StartMenuWindowManager;
import dk.dtu.engine.core.WindowManager;
import dk.dtu.engine.graphics.SudokuBoardCanvas;
import dk.dtu.engine.graphics.numberHub;
import dk.dtu.engine.input.KeyboardListener;
import dk.dtu.engine.input.MouseActionListener;
import dk.dtu.engine.utility.Timer;
import dk.dtu.game.solver.solverAlgorithm;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.*;
import javax.swing.*;

public class SudokuGame {
    public final Board gameboard;
    private final WindowManager windowManager;
    public final Stack<Move> moveList = new Stack<>();
    private final ArrayList<Integer> arrayMovelist = new ArrayList<>();
    private final ArrayList<Move> hintList = new ArrayList<>();
    int gridSize; // or 9 for a standard Sudoku
    int cellSize; // Adjust based on your window size and desired grid size
    public int placeableNumber = 0;
    MouseActionListener mouseActionListener = new MouseActionListener(this);
    KeyboardListener keyboardListener = new KeyboardListener(this);
    public SudokuBoardCanvas board;
    public numberHub numbers;
    public Timer timer;
    public boolean gameIsStarted = false;

    private JButton startButton,
            undoButton,
            hintButton,
            restartButton,
            solveButton,
            newGameButton,
            eraseButton,
            goBackButton;
    private final JToggleButton noteButton = new JToggleButton("Note Mode", false);

    public SudokuGame(WindowManager windowManager, int n, int k, int cellSize) throws Exception {
        this.windowManager = windowManager;
        gameboard = new Board(n, k);
        this.gridSize = n * k;
        this.cellSize = cellSize;
    }

    public void onSudokuBoardClicked(int x, int y) {
        int row = y / (board.getWidth() / gridSize); // Adjust for variable cell size
        int column = x / (board.getHeight() / gridSize); // Adjust for variable cell size
        board.setMarkedCell(row, column);
        board.setChosenNumber(gameboard.getNumber(row, column));

        makeMove(row, column, placeableNumber);

        if (column >= 0 && column < gridSize && row >= 0 && row < gridSize) {
            int cellIndex = row * gridSize + column; // Calculate the cell index
            System.out.println(
                    "Cell " + cellIndex + " clicked. Row: " + (row) + ", Column: " + (column));
            board.removeNumber(row, column);
            board.highlightCell(row, column, true);
            System.out.println("highlighted cell: " + Arrays.toString(board.getMarkedCell()));
            checkCompletionAndOfferNewGame();
        } else {
            System.out.println("Click outside the Sudoku board or on another component");
        }
    }

    public void typeNumberWithKeyboard(KeyEvent e) {
        char keyChar = e.getKeyChar();
        if (Character.isDigit(keyChar)) {
            System.out.println("Key pressed: " + keyChar);
            int number = keyChar - '0'; // Convert character to integer
            int[] markedCell = board.getMarkedCell();
            int row = markedCell[0];
            int col = markedCell[1];
            makeMove(row, col, number);
        }

        checkCompletionAndOfferNewGame();
    }

    public void checkCellsForNotes(int row, int col, int number, String mode) {
        // Check the row and column
        for (int i = 0; i < gridSize; i++) {
            if (board.getNotesInCell(row, i).contains(number)) {
                if (mode.equals("show")) board.removeFromHideList(row, i, number);
                else board.addToHideList(row, i, number);
            }
            if (board.getNotesInCell(i, col).contains(number)) {
                if (mode.equals("show")) board.removeFromHideList(i, col, number);
                else board.addToHideList(i, col, number);
            }
        }
        // Check the 3x3 subsquare
        int startRow = row - row % 3;
        int startCol = col - col % 3;
        for (int i = startRow; i < startRow + 3; i++) {
            for (int j = startCol; j < startCol + 3; j++) {
                if (board.getNotesInCell(i, j).contains(number)) {
                    if (mode.equals("show")) board.removeFromHideList(i, j, number);
                    else board.addToHideList(i, j, number);
                }
            }
        }
    }

    public void makeMove(int row, int col, int number) {
        if (row >= 0 && col >= 0) {
            if (noteButton.isSelected()
                    && gameboard.getInitialNumber(row, col) == 0
                    && (gameboard.getNumber(row, col) == 0)
                    && number != 0) {
                makeNote(row, col, number);
            }
            if (gameboard.validPlace(row, col, number)
                    && gameboard.getInitialNumber(row, col) == 0
                    && !noteButton.isSelected()
                    && number != 0) {
                board.setHiddenProperty(row, col, true);
                checkCellsForNotes(row, col, number, "hide");
                int previousNumber = gameboard.getNumber(row, col);
                board.setCellNumber(row, col, number);
                board.setChosenNumber(number);
                gameboard.setNumber(row, col, number);
                Move move = new Move(row, col, number, previousNumber);
                arrayMovelist.add(move.getNumber());
                moveList.push(move); // Log the move for undo functionality
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
            if (gameboard.getInitialNumber(row, col) == 0) {
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
            int row = move.getRow();
            int col = move.getColumn();
            board.setHiddenProperty(row, col, false);
            checkCellsForNotes(row, col, move.getNumber(), "show");
            int prevNumber = move.getPreviousNumber();
            gameboard.setNumber(row, col, prevNumber);
            board.setCellNumber(row, col, prevNumber);
            arrayMovelist.removeLast();
            System.out.println(Arrays.toString(arrayMovelist.toArray()));
        }
    }

    public void createBoard(int n, int k, int cellSize) {
        board = new SudokuBoardCanvas(n, k, cellSize);
        board.setLocation(50, 50);
        board.setFocusable(true);

        board.addMouseListener(mouseActionListener);

        board.addKeyListener(keyboardListener);

        gameboard.setInitialBoard(deepCopyBoard(gameboard.getBoard()));

        numbers = new numberHub(n * k, cellSize);
        numbers.setLocation(50, 50);
        numbers.setFocusable(true);

        numbers.addMouseListener(mouseActionListener);
        numbers.addKeyListener(keyboardListener);

        timer = new Timer();
        timer.setFocusable(true);
    }

    public void initialize(int n, int k, int cellSize) {
        createBoard(n, k, cellSize);
        displayButtons();
        windowManager.drawBoard(board);
        windowManager.setupNumberAndTimerPanel(timer, numbers);
        windowManager.layoutComponents(timer, numbers);
    }

    public void newGame() throws Exception {
        gameboard.clear();
        hintList.clear();
        arrayMovelist.clear();
        moveList.clear();
        timer.stop();
        timer.reset();
        dk.dtu.game.solver.solverAlgorithm.createSudoku(gameboard);
        gameboard.setInitialBoard(deepCopyBoard(gameboard.getBoard()));
        gameIsStarted = true;
        gameboard.printBoard();
        fillHintList();
        System.out.println(hintList.size());
        timer.start();
    }

    private JButton createButton(String text, int width, int height) {
        JButton button = new JButton(text);
        button.setMaximumSize(new Dimension(width, height));
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
        int[][] solutionBoard = solverAlgorithm.getSolutionBoard(gameboard.getBoard());

        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                if (gameboard.getInitialNumber(row, col)
                        == 0) { // Assuming initialBoard is the puzzle with empty spaces
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
                    return false;
                }
                System.out.println(gameboard.getNumber(row, col));
            }
        }
        return true;
    }

    public void provideHint() {
        if (!hintList.isEmpty()) {
            Random random = new Random();
            int hintIndex = random.nextInt(hintList.size());
            Move hintMove = hintList.get(hintIndex);
            hintList.remove(hintIndex);

            int row = hintMove.getRow();
            int col = hintMove.getColumn();
            int number = hintMove.getNumber();

            checkCellsForNotes(row, col, number, "hide");
            gameboard.setNumber(row, col, number);
            board.setCellNumber(row, col, number);
            gameboard.setInitialNumber(row, col, number);

            // Visualize the hint
            board.visualizeCell(row, col, Color.blue);
            checkCompletionAndOfferNewGame();
        } else {
            System.out.println("No more hints available.");
        }
    }

    public void checkCompletionAndOfferNewGame() {
        if (isSudokuCompleted()) {
            timer.stop();
            Object[] options = {"New Game", "Close"};
            int response =
                    JOptionPane.showOptionDialog(
                            null,
                            "Congratulations! You've completed the Sudoku in\n"
                                    + timer.getTimeString()
                                    + "\n\n"
                                    + "Would you like to start a new game?",
                            "Game Completed",
                            JOptionPane.YES_NO_OPTION,
                            JOptionPane.INFORMATION_MESSAGE,
                            null,
                            options,
                            options[0]);

            if (response == JOptionPane.YES_OPTION) {
                try {
                    newGame();
                } catch (Exception e) {
                    System.out.println("Error creating new game:" + e.getMessage());
                }
            }
        }
    }

    private void displayButtons() {
        startButton = createButton("Start", 100, 30);
        restartButton = createButton("Restart", 100, 30);
        solveButton = createButton("Solve", 100, 30);
        newGameButton = createButton("New Game", 100, 30);
        eraseButton = createButton("Erase", 100, 30);
        undoButton = createButton("Undo", 100, 300);
        hintButton = createButton("Hint", 100, 30);
        goBackButton = createButton("Go Back", 100, 30);

        // Set solvebutton to be disabled at the start of the game
        solveButton.setEnabled(false);

        startButton.addActionListener(
                e -> {
                    System.out.println("Start game!");
                    try {
                        newGame();
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                    displayNumbersVisually();
                    gameIsStarted = true;
                    board.requestFocusInWindow();
                    solveButton.setEnabled(true);
                });
        restartButton.addActionListener(
                e -> {
                    // set the numbers to the initial board
                    gameIsStarted = false;
                    board.clearNotes();
                    timer.stop();
                    gameboard.setBoard(deepCopyBoard(gameboard.getInitialBoard()));
                    board.requestFocusInWindow();
                    gameIsStarted = true;
                    windowManager.updateBoard();
                    timer.reset();
                    timer.start();
                });

        solveButton.addActionListener(
                e -> {
                    board.clearNotes();
                    timer.stop();
                    gameboard.setBoard(
                            Objects.requireNonNull(
                                    solverAlgorithm.getSolutionBoard(gameboard.getInitialBoard())));
                    checkCompletionAndOfferNewGame();
                });

        newGameButton.addActionListener(
                e -> {
                    gameIsStarted = false;
                    board.clearNotes();
                    try {
                        newGame();
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                    board.requestFocusInWindow();

                    windowManager.updateBoard();
                });

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
        noteButton.addActionListener(
                e -> {
                    board.requestFocusInWindow();
                });
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

        windowManager.addComponentToButtonPanel(startButton);
        windowManager.addComponentToButtonPanel(Box.createRigidArea(new Dimension(10, 10)));
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

    public int[][] deepCopyBoard(int[][] original) {
        return solverAlgorithm.deepCopy(original);
    }

    public void render() {
        if (gameIsStarted) {
            displayNumbersVisually();
        }
    }

    public void update() {
        render();
    }

    public ArrayList<Move> getHintList() {
        return hintList;
    }

    public void onNumbersBoardClicked(int x, int y) {
        // System.out.println("Numbers board clicked at: " + x + ", " + y);
        // placeableNumber = numbers.getNumber(x, y);
        // System.out.println("Number: " + placeableNumber);

        // numbers.highlightNumber(x, y);
        int chosenNumber = numbers.getNumber(x, y);
        board.setChosenNumber(chosenNumber);
        int[] markedCell = board.getMarkedCell();
        int row = markedCell[0];
        int col = markedCell[1];
        makeMove(row, col, chosenNumber);
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

    public JButton getStartButton() {
        return startButton;
    }

    public JButton getRestartButton() {
        return restartButton;
    }

    public JToggleButton getNoteButton() {
        return noteButton;
    }
}
