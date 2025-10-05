package net.cosyfluf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class MainFrame extends JFrame {

    private GamePanel gamePanel; // Nur noch ein GamePanel

    private GraphicsDevice graphicsDevice;
    private Dimension originalSize;     // Speichert die Größe des Fensters im Fenstermodus
    private Point originalLocation;     // Speichert die Position des Fensters im Fenstermodus
    private boolean isFullscreen = false; // Zustand des Vollbildmodus

    private int lastPlayedLevelIndex = 0; // Speichert das zuletzt gespielte Level

    public MainFrame() {
        setTitle("2D Hill Climb (JBox2D & Java)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        // Level definieren (Liste wird an GamePanel übergeben)
        List<LevelInfo> levels = new ArrayList<>();
        levels.add(new LevelInfo("Easy Hills", 12345L, 0.005, 150.0, 10.0, 0.1));
        levels.add(new LevelInfo("Bumpy Ride", 67890L, 0.008, 180.0, 25.0, 0.05));
        levels.add(new LevelInfo("Mountain Pass", 11223L, 0.003, 220.0, 15.0, 0.08));
        levels.add(new LevelInfo("Crazy Terrain", 98765L, 0.012, 120.0, 40.0, 0.03));
        levels.add(new LevelInfo("Deep Valley", 54321L, 0.006, 250.0, 18.0, 0.07));
        levels.add(new LevelInfo("Rocky Road", 13579L, 0.015, 100.0, 50.0, 0.02));


        gamePanel = new GamePanel(this, levels); // GamePanel direkt erstellen und Levels übergeben
        add(gamePanel); // GamePanel direkt zum Frame hinzufügen
        pack();
        setLocationRelativeTo(null);

        originalSize = getSize();
        originalLocation = getLocation();

        setVisible(true);

        // KeyListener am JFrame selbst für Minimierung im Vollbildmodus
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (isFullscreen && e.getKeyCode() == KeyEvent.VK_M) {
                    System.out.println("Minimizing game...");
                    setExtendedState(JFrame.ICONIFIED);
                }
            }
        });
        setFocusable(true); // Sicherstellen, dass der JFrame Key-Events empfangen kann
        requestFocusInWindow(); // Dem Frame den Fokus geben

        // Der Zustand des GamePanel wird in dessen Konstruktor auf MAIN_MENU gesetzt
    }

    public int getLastPlayedLevelIndex() {
        return lastPlayedLevelIndex;
    }

    public void setLastPlayedLevelIndex(int index) { // Setter für das zuletzt gespielte Level
        this.lastPlayedLevelIndex = index;
    }

    public boolean isFullscreen() {
        return isFullscreen;
    }

    // Diese Methoden delegieren jetzt an das GamePanel und verwalten den Vollbildmodus
    public void showMainMenu() {
        // Vollbildmodus beenden, wenn wir ins Menü zurückkehren
        if (isFullscreen) {
            setVisible(false);
            graphicsDevice.setFullScreenWindow(null);

            setSize(originalSize);
            setLocation(originalLocation);

            setVisible(true);
            isFullscreen = false;
        }
        gamePanel.showMainMenu();
        gamePanel.requestFocusInWindow(); // Fokus auf GamePanel wiederherstellen
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
        // Vollbildmodus beenden, wenn wir zum Game Over Screen kommen
        if (isFullscreen) {
            setVisible(false);
            graphicsDevice.setFullScreenWindow(null);
            setSize(originalSize);
            setLocation(originalLocation);
            setVisible(true);
            isFullscreen = false;
        }
        gamePanel.showGameOverScreen();
        gamePanel.requestFocusInWindow(); // Fokus auf GamePanel wiederherstellen
    }


    public void startGame(int levelIndex) {
        // Vollbildmodus starten, wenn nicht bereits aktiv
        if (graphicsDevice.isFullScreenSupported() && !isFullscreen) { // Prüfen, ob nicht bereits Vollbild
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

        gamePanel.startGame(levelIndex); // Startet das Spiel im GamePanel
        gamePanel.requestFocusInWindow();
        revalidate();
        repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainFrame());
    }
}