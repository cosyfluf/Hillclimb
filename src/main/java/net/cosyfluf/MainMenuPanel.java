package net.cosyfluf;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class MainMenuPanel extends JPanel {

    private MainFrame mainFrame;
    private List<LevelInfo> levels;

    public MainMenuPanel(MainFrame mainFrame, List<LevelInfo> levels) {
        this.mainFrame = mainFrame;
        this.levels = levels;

        setLayout(new BorderLayout());
        setBackground(new Color(100, 149, 237));

        JLabel titleLabel = new JLabel("Hill Climb Game", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 48));
        titleLabel.setForeground(Color.WHITE);
        add(titleLabel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(levels.size() + 1, 1, 10, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(50, 200, 50, 200));

        for (int i = 0; i < levels.size(); i++) {
            final int levelIndex = i;
            JButton levelButton = new JButton("Level " + (i + 1) + ": " + levels.get(i).name);
            styleButton(levelButton);
            levelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    mainFrame.startGame(levelIndex);
                }
            });
            buttonPanel.add(levelButton);
        }

        JButton exitButton = new JButton("Exit Game");
        styleButton(exitButton);
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        buttonPanel.add(exitButton);

        add(buttonPanel, BorderLayout.CENTER);
    }

    private void styleButton(JButton button) {
        button.setFont(new Font("Arial", Font.BOLD, 24));
        button.setBackground(new Color(60, 179, 113));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        GradientPaint gradient = new GradientPaint(0, 0, new Color(100, 149, 237), 0, getHeight(), new Color(65, 105, 225));
        g2d.setPaint(gradient);
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
}