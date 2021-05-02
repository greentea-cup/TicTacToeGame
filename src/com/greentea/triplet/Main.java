package com.greentea.triplet;

import java.util.InputMismatchException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Set player 1: ");
        String player1 = scanner.next();
        System.out.print("Set player 2: ");
        String player2 = scanner.next();
        String winner;
        while (true) {
            int width, height, strike, maxStrike, x, y;

            System.out.print("Set board width: ");
            width = scanner.nextInt();
            System.out.print("Set board height: ");
            height = scanner.nextInt();
            maxStrike = Math.min(width, height);
            System.out.print("Set strike length: ");
            strike = scanner.nextInt();

            if (width < 1) width = 3;
            if (height < 1) height = 3;
            if (strike > maxStrike) strike = maxStrike;

            Board board = Board.newBoard().size(width, height).strikeLength(strike).players(player1, player2).build();
            while (!board.isFinished()) {
                System.out.println(board);
                System.out.println(board.getCurrentPlayer() + " turn");
                try {
                    do {
                        System.out.print("X[1..width]: ");
                        x = scanner.nextInt() - 1;
                        System.out.print("Y[1..height]: ");
                        y = scanner.nextInt() - 1;
                    } while (!board.isCellPlaceable(x, y));
                    board.tryPlaceCell(x, y);
                } catch (InputMismatchException e) {
                    System.out.println("You missed up with keys. End.");
                    return;
                }
            }
            System.out.println(board);
            winner = board.hasWinner() ? "Winner is " + board.tryGetWinner() : "Draw";
            System.out.printf("Finished. %s within %s turns%n", winner, board.getTurnCount());
        }
    }
}