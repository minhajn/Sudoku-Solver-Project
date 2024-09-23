/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.for207project;

/**
 *
 * @author mdmin
 */
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class AdvancedSudokuSolver extends JFrame {
    private static final int SIZE = 9;
    private JTextField[][] cells = new JTextField[SIZE][SIZE];
    private int[][] board = new int[SIZE][SIZE];
    private ForkJoinPool pool = new ForkJoinPool();

    public AdvancedSudokuSolver() {
        setTitle("Advanced Sudoku Solver");
        setSize(600, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel gridPanel = new JPanel(new GridLayout(SIZE, SIZE));
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                cells[row][col] = new JTextField();
                cells[row][col].setHorizontalAlignment(JTextField.CENTER);
                cells[row][col].setFont(new Font("Arial", Font.BOLD, 20));
                cells[row][col].setBorder(BorderFactory.createLineBorder(Color.BLACK));
                gridPanel.add(cells[row][col]);
            }
        }

        add(gridPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        JButton solveButton = new JButton("Solve");
        JButton generateButton = new JButton("Generate Puzzle");
        buttonPanel.add(solveButton);
        buttonPanel.add(generateButton);
        add(buttonPanel, BorderLayout.SOUTH);

        solveButton.addActionListener((ActionEvent e) -> {
            readBoardFromUI();
            new Thread(() -> {
                boolean solved = solveWithConstraintPropagation();
                SwingUtilities.invokeLater(() -> {
                    if (solved) {
                        updateUIBoard();
                        JOptionPane.showMessageDialog(null, "Solved!");
                    } else {
                        JOptionPane.showMessageDialog(null, "No solution exists!");
                    }
                });
            }).start();
        });

        generateButton.addActionListener((ActionEvent e) -> {
            generatePuzzleWithDifficulty("medium");
            updateUIBoard();
        });
    }

    private void readBoardFromUI() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                try {
                    int value = Integer.parseInt(cells[row][col].getText());
                    board[row][col] = (value >= 1 && value <= 9) ? value : 0;
                    cells[row][col].setEditable(value == 0);
                } catch (NumberFormatException ex) {
                    board[row][col] = 0;
                }
            }
        }
    }

    private void updateUIBoard() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                if (board[row][col] != 0) {
                    cells[row][col].setText(String.valueOf(board[row][col]));
                    cells[row][col].setEditable(false);
                } else {
                    cells[row][col].setText("");
                    cells[row][col].setEditable(true);
                }
            }
        }
    }

    private boolean solveWithConstraintPropagation() {
        return pool.invoke(new ParallelSudokuSolver(board, 0, 0));
    }

    private class ParallelSudokuSolver extends RecursiveTask<Boolean> {
        private final int[][] localBoard;
        private int row, col;

        public ParallelSudokuSolver(int[][] board, int row, int col) {
            this.localBoard = new int[SIZE][SIZE];
            for (int i = 0; i < SIZE; i++) {
                System.arraycopy(board[i], 0, this.localBoard[i], 0, SIZE);
            }
            this.row = row;
            this.col = col;
        }

        @Override
        protected Boolean compute() {
            while (row < SIZE && localBoard[row][col] != 0) {
                col++;
                if (col == SIZE) {
                    col = 0;
                    row++;
                }
            }

            if (row == SIZE) return true;

            for (int num = 1; num <= SIZE; num++) {
                if (isValid(localBoard, row, col, num)) {
                    localBoard[row][col] = num;
                    ParallelSudokuSolver task = new ParallelSudokuSolver(localBoard, row, col + 1);
                    task.fork();

                    if (task.join()) {
                        updateBoard(localBoard);
                        return true;
                    }

                    localBoard[row][col] = 0;
                }
            }

            return false;
        }

        private boolean isValid(int[][] board, int row, int col, int num) {
            for (int i = 0; i < SIZE; i++) {
                if (board[row][i] == num) {
                    return false;
                }
            }

            for (int i = 0; i < SIZE; i++) {
                if (board[i][col] == num) {
                    return false;
                }
            }

            int startRow = row - row % 3;
            int startCol = col - col % 3;
            for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    if (board[startRow + i][startCol + j] == num) {
                        return false;
                    }
                }
            }

            return true;
        }

        private void updateBoard(int[][] solvedBoard) {
            for (int i = 0; i < SIZE; i++) {
                System.arraycopy(solvedBoard[i], 0, board[i], 0, SIZE);
            }
        }
    }

    private void generatePuzzleWithDifficulty(String difficulty) {
        clearBoard();
        int clues = 0;
        switch (difficulty.toLowerCase()) {
            case "easy" -> clues = 40;
            case "medium" -> clues = 32;
            case "hard" -> clues = 24;
        }
        while (clues > 0) {
            int row = (int) (Math.random() * SIZE);
            int col = (int) (Math.random() * SIZE);
            int num = 1 + (int) (Math.random() * SIZE);
            if (isValid(board, row, col, num) && board[row][col] == 0) {
                board[row][col] = num;
                clues--;
            }
        }
    }

    private boolean isValid(int[][] board, int row, int col, int num) {
        for (int i = 0; i < SIZE; i++) {
            if (board[row][i] == num) {
                return false;
            }
        }

        for (int i = 0; i < SIZE; i++) {
            if (board[i][col] == num) {
                return false;
            }
        }

        int startRow = row - row % 3;
        int startCol = col - col % 3;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[startRow + i][startCol + j] == num) {
                    return false;
                }
            }
        }

        return true;
    }

    private void clearBoard() {
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                board[row][col] = 0;
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AdvancedSudokuSolver solver = new AdvancedSudokuSolver();
            solver.setVisible(true);
        });
    }
}
