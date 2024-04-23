package dk.dtu.game.core;

import dk.dtu.engine.core.StartMenuWindowManager;
import dk.dtu.engine.core.WindowManager;
import dk.dtu.engine.graphics.SudokuBoardCanvas;
import dk.dtu.engine.graphics.numberHub;
import dk.dtu.engine.input.KeyboardListener;
import dk.dtu.engine.input.MouseActionListener;
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
    public boolean gameIsStarted = false;
    private JButton startButton, undoButton, hintButton, restartButton, solveButton, newGameButton, eraseButton;

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

        if (row >= 0 && column >= 0) { // Validate that a cell is indeed highlighted
            if (gameboard.validPlace(row, column, placeableNumber)
                    && gameboard.getInitialNumber(row, column) == 0) {
                int previousNumber = gameboard.getNumber(row, column);
                board.setCellNumber(row, column, placeableNumber);
                gameboard.setNumber(row, column, placeableNumber);
                Move move = moveList.push(new Move(row, column, placeableNumber, previousNumber));
                arrayMovelist.add(move.getNumber());
                System.out.println(Arrays.toString(arrayMovelist.toArray()));
            }
        }

        System.out.println("Row: " + row + ", Column: " + column);

        // Check if the adjusted click is within the bounds of the Sudoku board
        if (column >= 0 && column < gridSize && row >= 0 && row < gridSize) {
            int cellIndex = row * gridSize + column + 1; // Calculate the cell index
            System.out.println(
                    "Cell "
                            + cellIndex
                            + " clicked. Row: "
                            + (row + 1)
                            + ", Column: "
                            + (column + 1));

            board.removeNumber(row, column);
            board.highlightCell(row, column, true);
            System.out.println("highlighted cell: " + Arrays.toString(board.getMarkedCell()));

        } else {
            System.out.println("Click outside the Sudoku board or on another component");
        }
    }

    public void typeNumberWithKeyboard(KeyEvent e) {
        char keyChar = e.getKeyChar();
        if (Character.isDigit(keyChar)) {
            int number = keyChar - '0'; // Convert character to integer

            int[] markedCell = board.getMarkedCell();
            int row = markedCell[0];
            int col = markedCell[1];
            if (row >= 0 && col >= 0) { // Validate that a cell is indeed highlighted
                if (gameboard.validPlace(row, col, number)
                        && gameboard.getInitialNumber(row, col) == 0) {
                    int previousNumber = gameboard.getNumber(row, col);
                    board.setCellNumber(row, col, number);
                    gameboard.setNumber(row, col, number);
                    gameboard.printBoard();
                    Move move = moveList.push(new Move(row, col, number, previousNumber));
                    arrayMovelist.add(move.getNumber());
                    System.out.println(Arrays.toString(arrayMovelist.toArray()));
                }
            }
        }

        checkCompletionAndOfferNewGame();
    }

    public void eraseNumber() {
        if (board.isACellMarked()) {
            int[] cell = board.getMarkedCell();
            int row = cell[0];
            int col = cell[1];
            if (gameboard.getInitialNumber(row, col) == 0) {
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
    }

    public void initialize(int n, int k, int cellSize) {
        createBoard(n, k, cellSize);
        displayButtons();
        windowManager.drawBoard(board);
        windowManager.drawNumbers(numbers);
    }

    public void newGame() throws Exception {
        gameboard.clear();
        hintList.clear();
        dk.dtu.game.solver.solverAlgorithm.createSudoku(gameboard);
        gameboard.setInitialBoard(deepCopyBoard(gameboard.getBoard()));
        gameIsStarted = true;
        gameboard.printBoard();
        fillHintList();
        System.out.println(hintList.size());
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
            Object[] options = {"New Game", "Close"};
            int response =
                    JOptionPane.showOptionDialog(
                            null,
                            "Congratulations! You've completed the Sudoku!\nWould you like to start a new game?",
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

        JButton startButton = createButton("Start", 30);
        JButton restartButton = createButton("Restart", 30);
        JButton solveButton = createButton("Solve", 30);
        JButton newGameButton = createButton("New Game", 30);
        JButton eraseButton = createButton("Erase", 30);
        JButton undoButton = createButton("Undo", 300);
        JButton hintButton = createButton("Hint", 30);
        JButton GoBackButton = createButton("Menu", 30);


        // Go back to the start menu
        GoBackButton.addActionListener(
                e -> {
                    JFrame frame = windowManager.getFrame();
                    frame.getContentPane().removeAll();
                    frame.invalidate();
                    frame.validate();
                    frame.repaint();

                    StartMenuWindowManager startMenuWindowManager = new StartMenuWindowManager(frame, 1000, 700);
                    StartMenu startMenu1 = new StartMenu(startMenuWindowManager);
                    startMenu1.initialize();
                }
        );


        //Set solvebutton to be disabled at the start of the game
        solveButton.setEnabled(false);

        startButton.addActionListener(e -> {
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
        restartButton.addActionListener(e -> {
            //set the numbers to the initial board
            gameIsStarted = false;
            gameboard.setBoard(deepCopyBoard(gameboard.getInitialBoard()));
            board.requestFocusInWindow();
            gameIsStarted = true;
            windowManager.updateBoard();
        });

        solveButton.addActionListener(e -> {
            gameboard.setBoard(Objects.requireNonNull(solverAlgorithm.getSolutionBoard(gameboard.getInitialBoard())));
            checkCompletionAndOfferNewGame();
        });

        newGameButton.addActionListener(e -> {
            gameIsStarted = false;
            try {
                newGame();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
            board.requestFocusInWindow();

            windowManager.updateBoard();
        });

        eraseButton.addActionListener(e -> {
            board.requestFocusInWindow();
            eraseNumber();
        });

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
                    gameboard.setBoard(deepCopyBoard(gameboard.getInitialBoard()));
                    board.requestFocusInWindow();
                    gameIsStarted = true;
                    windowManager.updateBoard();
                });

        solveButton.addActionListener(
                e -> {
                    gameboard.setBoard(
                            Objects.requireNonNull(
                                    solverAlgorithm.getSolutionBoard(gameboard.getInitialBoard())));
                    checkCompletionAndOfferNewGame();
                });

        newGameButton.addActionListener(
                e -> {
                    gameIsStarted = false;
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

        windowManager.addComponentToButtonPanel(startButton);
        windowManager.addComponentToButtonPanel(
                Box.createRigidArea(new Dimension(10, 10))); // 10-pixel vertical spacing
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
        windowManager.addComponentToButtonPanel(GoBackButton);
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

    // Next function is simulating the move typed from the keyboard:
    public void makeMoveTest(int row, int col, int number) {
        int previousNumber = gameboard.getNumber(row, col);
        gameboard.setNumber(row, col, number);
        Move move = new Move(row, col, number, previousNumber);
        arrayMovelist.add(move.getNumber());
        moveList.push(move); // Log the move for undo functionality
    }

    public ArrayList<Move> getHintList() {
        return hintList;
    }

    public void onNumbersBoardClicked(int x, int y) {
        System.out.println("Numbers board clicked at: " + x + ", " + y);
        placeableNumber = numbers.getNumber(x, y);
        System.out.println("Number: " + placeableNumber);

        numbers.highlightNumber(x, y);
        int chosenNumber = numbers.getNumber(x, y);
        board.setChosenNumber(chosenNumber);
    }

    public JButton getUndoButton(){
        return undoButton;
    }
    public JButton getHintButton(){
        return hintButton;
    }
    public JButton getNewGameButton(){
        return newGameButton;
    }
    public JButton getEraseButton(){
        return eraseButton;
    }
    public JButton getStartButton(){
        return startButton;
    }
    public JButton getRestartButton(){
        return restartButton;
    }
}
