package net.cosyfluf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {

    private GamePanel gamePanel;

    private GraphicsDevice graphicsDevice;
    private Dimension originalSize;
    private Point originalLocation;
    private boolean isFullscreen = false;

    private int lastPlayedLevelIndex = 0;

    public MainFrame() {
        setTitle("2D Hill Climb (JBox2D & Java)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        List<LevelInfo> levels = new ArrayList<>();
        levels.add(new LevelInfo("Easy Hills", 12345L, 0.005, 150.0, 10.0, 0.1));
        levels.add(new LevelInfo("Bumpy Ride", 67890L, 0.008, 180.0, 25.0, 0.05));
        levels.add(new LevelInfo("Mountain Pass", 11223L, 0.003, 220.0, 15.0, 0.08));
        levels.add(new LevelInfo("Crazy Terrain", 98765L, 0.012, 120.0, 40.0, 0.03));
        levels.add(new LevelInfo("Deep Valley", 54321L, 0.006, 250.0, 18.0, 0.07));
        levels.add(new LevelInfo("Rocky Road", 13579L, 0.015, 100.0, 50.0, 0.02));

        gamePanel = new GamePanel(this, levels);
        add(gamePanel);
        pack();
        setLocationRelativeTo(null);

        originalSize = getSize();
        originalLocation = getLocation();

        setVisible(true);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (isFullscreen && e.getKeyCode() == KeyEvent.VK_M) {
                    System.out.println("Minimizing game...");
                    setExtendedState(JFrame.ICONIFIED);
                }
            }
        });
        setFocusable(true);
        requestFocusInWindow();

    }

    public int getLastPlayedLevelIndex() {
        return lastPlayedLevelIndex;
    }

    public void setLastPlayedLevelIndex(int index) {
        this.lastPlayedLevelIndex = index;
    }

    public boolean isFullscreen() {
        return isFullscreen;
    }

    public void showMainMenu() {

        if (isFullscreen) {
            setVisible(false);
            graphicsDevice.setFullScreenWindow(null);

            setSize(originalSize);
            setLocation(originalLocation);

            setVisible(true);
            isFullscreen = false;
        }
        gamePanel.showMainMenu();
        gamePanel.requestFocusInWindow();
    }

    public void exitFullscreenAndContinueGame() {
        if (isFullscreen) {
            setVisible(false);
            graphicsDevice.setFullScreenWindow(null);

            setSize(originalSize);
            setLocation(originalLocation);

            setVisible(true);
            isFullscreen = false;
        }
        gamePanel.requestFocusInWindow();
        gamePanel.startGameTimer();
    }

    public void showGameOverScreen() {

        if (isFullscreen) {
            setVisible(false);
            graphicsDevice.setFullScreenWindow(null);
            setSize(originalSize);
            setLocation(originalLocation);
            setVisible(true);
            isFullscreen = false;
        }
        gamePanel.showGameOverScreen();
        gamePanel.requestFocusInWindow();
    }

    public void startGame(int levelIndex) {

        if (graphicsDevice.isFullScreenSupported() && !isFullscreen) {
            originalSize = getSize();
            originalLocation = getLocation();

            setVisible(false);
            graphicsDevice.setFullScreenWindow(this);

            setVisible(true);
            isFullscreen = true;

        } else if (!graphicsDevice.isFullScreenSupported()) {
            System.out.println("Vollbildmodus wird auf diesem Gerät nicht unterstützt. Maximiertes Fenster wird stattdessen verwendet.");
            setExtendedState(JFrame.MAXIMIZED_BOTH);
            setVisible(true);
            isFullscreen = true;
        }

        gamePanel.startGame(levelIndex);
        gamePanel.requestFocusInWindow();
        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame());
    }
}