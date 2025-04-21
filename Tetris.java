import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;

public class Tetris extends JFrame {
    private static final int GRID_WIDTH = 18; // 16:9 ratio with GRID_HEIGHT = 18
    private static final int GRID_HEIGHT = 20;
    private static final int BLOCK_SIZE = 25;
    private static final int LEFT_PADDING = 19; // 0.5 cm at 96 DPI
    private static final int RIGHT_PADDING = 35;
    private static final int TOP_PADDING = 35;
    private static final int BOTTOM_PADDING = 45;
    private static final int[][][] SHAPES = {
        {{1, 1, 1, 1}}, // I
        {{1, 1}, {1, 1}}, // O
        {{1, 1, 1}, {0, 1, 0}}, // T
        {{1, 1, 1}, {1, 0, 0}}, // L
        {{1, 1, 1}, {0, 0, 1}}, // J
        {{1, 1, 0}, {0, 1, 1}}, // S
        {{0, 1, 1}, {1, 1, 0}} // Z
    };
    
    private final int[][] board = new int[GRID_HEIGHT][GRID_WIDTH];
    private Piece currentPiece;
    private int score = 0;
    private boolean gameOver = false;
    private final Random random = new Random();
    
    private class Piece {
        int x, y;
        int[][] shape;
        
        Piece(int[][] shape) {
            this.shape = shape;
            this.x = GRID_WIDTH / 2 - shape[0].length / 2;
            this.y = 0;
        }
    }
    
    public Tetris() {
        setTitle("Tetris");
        setSize(GRID_WIDTH * BLOCK_SIZE + LEFT_PADDING + RIGHT_PADDING, GRID_HEIGHT * BLOCK_SIZE + TOP_PADDING + BOTTOM_PADDING); // 504x570 pixels
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        JPanel gamePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawBoard(g);
                drawPiece(g);
                g.setColor(Color.BLACK);
                g.drawString("Score: " + score, 10 + LEFT_PADDING, 20 + TOP_PADDING); // Adjusted for padding
                if (gameOver) {
                    g.setColor(Color.RED);
                    g.drawString("Game Over", (getWidth() / 2) - 30, (getHeight() / 2)); // Centered with padding
                }
            }
        };
        gamePanel.setPreferredSize(new Dimension(GRID_WIDTH * BLOCK_SIZE, GRID_HEIGHT * BLOCK_SIZE)); // 450x500 pixels
        add(gamePanel);
        
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (!gameOver) {
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_LEFT -> movePiece(-1, 0);
                        case KeyEvent.VK_RIGHT -> movePiece(1, 0);
                        case KeyEvent.VK_DOWN -> movePiece(0, 1);
                        case KeyEvent.VK_UP -> rotatePiece();
                    }
                    gamePanel.repaint();
                }
            }
        });
        
        Timer timer = new Timer(500, e -> {
            if (!gameOver) {
                movePiece(0, 1);
                gamePanel.repaint();
            }
        });
        timer.start();
        
        spawnPiece();
    }
    
    private void drawBoard(Graphics g) {
        for (int y = 0; y < GRID_HEIGHT; y++) {
            for (int x = 0; x < GRID_WIDTH; x++) { // Draw all 18 columns
                if (board[y][x] == 1) {
                    g.setColor(Color.BLUE);
                    g.fillRect(x * BLOCK_SIZE + LEFT_PADDING, y * BLOCK_SIZE + TOP_PADDING, BLOCK_SIZE, BLOCK_SIZE); // Shifted by padding
                    g.setColor(Color.BLACK);
                    g.drawRect(x * BLOCK_SIZE + LEFT_PADDING, y * BLOCK_SIZE + TOP_PADDING, BLOCK_SIZE, BLOCK_SIZE);
                }
            }
        }
        // Draw bottom border line for clarity
        g.setColor(Color.BLACK);
        g.drawLine(LEFT_PADDING, (GRID_HEIGHT * BLOCK_SIZE) + TOP_PADDING, (GRID_WIDTH * BLOCK_SIZE) + LEFT_PADDING, (GRID_HEIGHT * BLOCK_SIZE) + TOP_PADDING);
    }
    
    private void drawPiece(Graphics g) {
        g.setColor(Color.RED);
        for (int y = 0; y < currentPiece.shape.length; y++) {
            for (int x = 0; x < currentPiece.shape[y].length; x++) {
                if (currentPiece.shape[y][x] == 1) {
                    g.fillRect((currentPiece.x + x) * BLOCK_SIZE + LEFT_PADDING, (currentPiece.y + y) * BLOCK_SIZE + TOP_PADDING, BLOCK_SIZE, BLOCK_SIZE);
                    g.setColor(Color.BLACK);
                    g.drawRect((currentPiece.x + x) * BLOCK_SIZE + LEFT_PADDING, (currentPiece.y + y) * BLOCK_SIZE + TOP_PADDING, BLOCK_SIZE, BLOCK_SIZE);
                    g.setColor(Color.RED);
                }
            }
        }
    }
    
    private void spawnPiece() {
        currentPiece = new Piece(SHAPES[random.nextInt(SHAPES.length)]);
        if (collides(currentPiece)) {
            gameOver = true;
        }
    }
    
    private boolean collides(Piece piece) {
        for (int y = 0; y < piece.shape.length; y++) {
            for (int x = 0; x < piece.shape[y].length; x++) {
                if (piece.shape[y][x] == 1) {
                    int boardX = piece.x + x;
                    int boardY = piece.y + y;
                    if (boardX < 0 || boardX >= GRID_WIDTH || boardY >= GRID_HEIGHT || 
                        (boardY >= 0 && boardY < GRID_HEIGHT && board[boardY][boardX] == 1)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    private void movePiece(int dx, int dy) {
        currentPiece.x += dx;
        currentPiece.y += dy;
        // Calculate the rightmost edge of the piece
        int maxX = currentPiece.x + currentPiece.shape[0].length - 1;
        if (collides(currentPiece) || maxX >= GRID_WIDTH) {
            currentPiece.x -= dx;
            currentPiece.y -= dy;
            if (dy > 0) {
                mergePiece();
                clearLines();
                spawnPiece();
            }
        }
    }
    
    private void mergePiece() {
        for (int y = 0; y < currentPiece.shape.length; y++) {
            for (int x = 0; x < currentPiece.shape[y].length; x++) {
                if (currentPiece.shape[y][x] == 1) {
                    board[currentPiece.y + y][currentPiece.x + x] = 1;
                }
            }
        }
    }
    
    private void clearLines() {
        int linesCleared = 0;
        for (int y = GRID_HEIGHT - 1; y >= 0; y--) {
            boolean fullLine = true;
            for (int x = 0; x < GRID_WIDTH; x++) {
                if (board[y][x] == 0) {
                    fullLine = false;
                    break;
                }
            }
            if (fullLine) {
                linesCleared++;
                for (int i = y; i > 0; i--) {
                    board[i] = board[i - 1].clone();
                }
                board[0] = new int[GRID_WIDTH];
                y++;
            }
        }
        score += linesCleared * 100;
    }
    
    private void rotatePiece() {
        int[][] originalShape = currentPiece.shape;
        int height = originalShape.length;
        int width = originalShape[0].length;
        int[][] newShape = new int[width][height]; // Swap dimensions for rotation
        
        // Perform 90-degree clockwise rotation
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                newShape[x][height - 1 - y] = originalShape[y][x];
            }
        }
        
        currentPiece.shape = newShape;
        // Adjust position to prevent out-of-bounds after rotation
        if (currentPiece.x + newShape[0].length > GRID_WIDTH) {
            currentPiece.x = GRID_WIDTH - newShape[0].length;
        }
        if (currentPiece.x < 0) {
            currentPiece.x = 0;
        }
        
        if (collides(currentPiece)) {
            currentPiece.shape = originalShape; // Revert if collision
            currentPiece.x = Math.min(Math.max(currentPiece.x, 0), GRID_WIDTH - originalShape[0].length);
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            Tetris game = new Tetris();
            game.setVisible(true);
        });
    }
}