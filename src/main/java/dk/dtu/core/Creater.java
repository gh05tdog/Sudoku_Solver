package dk.dtu.core;

import java.util.*;
import java.util.stream.IntStream;

public class Creater {

    public static void fillBoard(Board board){

        //Total number of squaresList
        ArrayList<Integer> squares = new ArrayList<>(IntStream.rangeClosed(0, board.totalNoOfSquares()).boxed().toList());
        ArrayList<Integer> num = new ArrayList<>(IntStream.rangeClosed(1, board.getDimensions()).boxed().toList());
        //Shuffle the list
        Collections.shuffle(squares);
        Collections.shuffle(num);

        System.out.println(Arrays.toString(num.toArray()));
        System.out.println(Arrays.toString(squares.toArray()));


        // Try to place a random number at the first place in the square
        // Calculate row and column
        int x = squares.getFirst() / (board.getDimensions()); // Row index
        int y = squares.getFirst() %  (board.getDimensions()); // Column index

        System.out.println("x:" + x+ ", y:" +y);

        if(board.validPlace(x,y, num.getFirst())){
            board.setNumber(x,y, num.getFirst());
            //Backtrace to check if it is solvable:
            //if(Solvable) {
                squares.removeFirst();
                Collections.shuffle(num);
                board.printBoard();
           // }
        }
    }
}

