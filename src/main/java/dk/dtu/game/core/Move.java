/* (C)2024 */
package dk.dtu.game.core;
/**
 * The Move class represents a move in the game.
 * It contains information about the row, column, number, and previous number of the move.
 */
public record Move(int row, int column, int number, int previousNumber) {

}
