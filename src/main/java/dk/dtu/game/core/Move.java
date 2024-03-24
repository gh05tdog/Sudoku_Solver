package dk.dtu.game.core;

public class Move {

    private final int row;
    private final int column;
    private final int number;
    private final int previousNumber;


    public Move(int row, int column, int number, int previousNumber){
        this.row = row;
        this.column = column;
        this.number = number;
        this.previousNumber = previousNumber;
    }


    public int getRow(){
        return row;
    }
    public int getColumn(){
        return column;
    }
    public int getNumber(){
        return number;
    }
    public int getPreviousNumber(){
        return previousNumber;
    }


}
