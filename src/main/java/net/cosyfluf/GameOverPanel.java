package net.cosyfluf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class GameOverPanel extends JPanel {

    private MainFrame mainFrame;

    public GameOverPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout());
        setBackground(new Color(50, 50, 50)); // Dunkler Hintergrund

        // Titel
        JLabel titleLabel = new JLabel("GAME OVER", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 64));
        titleLabel.setForeground(Color.RED);
        add(titleLabel, BorderLayout.NORTH);

        // Buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 1, 10, 10)); // Zwei Buttons vertikal
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(100, 200, 100, 200));

        JButton retryButton = new JButton("Try Again");
        styleButton(retryButton, new Color(30, 144, 255)); // DodgerBlue
        retryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainFrame.startGame(mainFrame.getLastPlayedLevelIndex()); // Startet das letzte Level neu
            }
        });
        buttonPanel.add(retryButton);

        JButton mainMenuButton = new JButton("Main Menu");
        styleButton(mainMenuButton, new Color(100, 149, 237)); // CornflowerBlue
        mainMenuButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainFrame.showMainMenu();
            }
        });
        buttonPanel.add(mainMenuButton);

        add(buttonPanel, BorderLayout.CENTER);
    }

    private void styleButton(JButton button, Color bgColor) {
        button.setFont(new Font("Arial", Font.BOLD, 28));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Optional: Einen subtilen Farbverlauf für den Hintergrund zeichnen
        Graphics2D g2d = (Graphics2D) g;
        GradientPaint gradient = new GradientPaint(0, 0, new Color(50, 50, 50), 0, getHeight(), new Color(20, 20, 20));
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
}