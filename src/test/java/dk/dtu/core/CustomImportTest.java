/* (C)2024 */
package dk.dtu.core;

import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import dk.dtu.engine.core.StartMenuWindowManager;
import dk.dtu.game.core.Board;
import dk.dtu.game.core.StartMenu;
import dk.dtu.game.core.solver.bruteforce.BruteForceAlgorithm;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import javax.swing.*;
import org.junit.jupiter.api.Test;

class CustomImportTest {

    @Test
    void testCustomBoardValid() throws IOException, Board.BoardNotCreatable {
        List<String> lines =
                Arrays.asList(
                        "3;3",
                        ".;1;.;3;.;.;8;.;.",
                        "5;.;9;6;.;.;7;.;.",
                        "7;.;4;.;9;5;.;2;.",
                        "4;.;.;.;.;.;1;.;.",
                        ".;2;8;.;7;1;.;6;3",
                        ".;.;.;2;.;4;9;5;.",
                        "6;.;3;.;.;9;.;.;7",
                        ".;.;.;4;2;.;5;1;6",
                        ".;5;2;.;8;.;.;4;.");

        JFrame mockedFrame = mock(JFrame.class);

        StartMenuWindowManager startMenuWindowManager =
                new StartMenuWindowManager(mockedFrame, 1000, 1000);
        StartMenu startMenu = new StartMenu(startMenuWindowManager);
        startMenu.initialize();

        assertTrue(BruteForceAlgorithm.isValidSudoku(startMenu.importSudokuFromFile(lines)));
    }

    @Test
    void testCustomBoardInvalid() throws IOException {
        List<String> lines =
                Arrays.asList(
                        "3;3",
                        "5;1;.;3;.;.;8;.;.",
                        "5;.;9;6;.;.;7;.;.",
                        "7;.;4;.;9;5;.;2;.",
                        "4;.;.;.;.;.;1;.;.",
                        ".;2;8;.;7;1;.;6;3",
                        ".;.;.;2;.;4;9;5;.",
                        "6;.;3;.;.;9;.;.;7",
                        ".;.;.;4;2;.;5;1;6",
                        ".;5;2;.;8;.;.;4;.");

        JFrame mockedFrame = mock(JFrame.class);

        StartMenuWindowManager startMenuWindowManager =
                new StartMenuWindowManager(mockedFrame, 1000, 1000);
        StartMenu startMenu = new StartMenu(startMenuWindowManager);
        try {
            BruteForceAlgorithm.isValidSudoku(startMenu.importSudokuFromFile(lines));
        } catch (Board.BoardNotCreatable e) {
            assertThrowsExactly(
                    Board.BoardNotCreatable.class,
                    () -> {
                        throw new Board.BoardNotCreatable("This board is not possible to create");
                    });
        }
    }
}
