import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ControlSettingsPanel extends JPanel {
    private final CardLayout cardLayout;
    private final JPanel container;
    private final GamePanel gamePanel;
    private final ScorePanel scorePanel;
    private int[][] currentControls;
    private BufferedImage backgroundImage;
    private JTextField[][] keyFields = new JTextField[4][5];


    public ControlSettingsPanel(CardLayout cardLayout, JPanel container, GamePanel gamePanel, ScorePanel scorePanel) {
        this.cardLayout = cardLayout;
        this.container = container;
        this.gamePanel = gamePanel;
        this.scorePanel = scorePanel;
        this.currentControls = deepCopyControls(gamePanel.getControls());

        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(600, 500));

        // Load background image with transparency support
        try {
            backgroundImage = ImageIO.read(getClass().getResource("/images/BGControls.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Title label
        TitleLabel titleLabel = new TitleLabel("CONTROL SETTINGS");
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Grid panel for 4 player settings in 2x2 layout
        JPanel gridPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        gridPanel.setOpaque(false);
        gridPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        String[] controlNames = {"Up", "Down", "Left", "Right", "Fire"};
        String[] playerNames = {"Player 1", "Player 2", "Player 3", "Player 4"};

        for (int player = 0; player < 4; player++) {
            JPanel playerPanel = new JPanel();
            playerPanel.setLayout(new BoxLayout(playerPanel, BoxLayout.Y_AXIS));
            playerPanel.setOpaque(true);
            playerPanel.setBackground(new Color(230, 230, 230, 200)); // slight transparency on panel bg
            playerPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.GRAY, 1),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)));

            JLabel playerLabel = new JLabel(playerNames[player], SwingConstants.CENTER);
            playerLabel.setFont(new Font("Arial", Font.BOLD, 16));
            playerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            playerPanel.add(playerLabel);

            JPanel controlsGrid = new JPanel(new GridLayout(5, 2, 5, 5));
            controlsGrid.setOpaque(false);

            for (int i = 0; i < 5; i++) {
                JLabel actionLabel = new JLabel(controlNames[i] + ":", SwingConstants.RIGHT);
                actionLabel.setFont(new Font("Arial", Font.PLAIN, 14));
                controlsGrid.add(actionLabel);

                JTextField keyField = new JTextField(KeyEvent.getKeyText(currentControls[player][i]));
                keyFields[player][i] = keyField;
                keyField.setEditable(false);
                keyField.setHorizontalAlignment(JTextField.CENTER);
                keyField.setFont(new Font("Arial", Font.BOLD, 14));

                // Make text fields transparent
                keyField.setOpaque(false);
                keyField.setBackground(new Color(0, 0, 0, 0));
                keyField.setForeground(Color.BLACK);
                keyField.setBorder(BorderFactory.createLineBorder(Color.GRAY));

                final int playerIndex = player;
                final int controlIndex = i;
                keyField.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyPressed(KeyEvent e) {
                        int keyCode = e.getKeyCode();
                        keyField.setText(KeyEvent.getKeyText(keyCode));
                        currentControls[playerIndex][controlIndex] = keyCode;
                        gamePanel.updateControls(currentControls);

                        String[] updatedControls = new String[4];
                        for (int p = 0; p < 4; p++) {
                            updatedControls[p] = getControlStringForPlayer(p);
                        }
                        scorePanel.updateControls(updatedControls);
                    }
                });

                controlsGrid.add(keyField);
            }

            playerPanel.add(Box.createVerticalStrut(10));
            playerPanel.add(controlsGrid);
            gridPanel.add(playerPanel);
        }

        add(gridPanel, BorderLayout.CENTER);

        JButton backButton = new JButton("BACK TO MENU");
        backButton.setPreferredSize(new Dimension(180, 35));
        backButton.setFont(new Font("Arial", Font.BOLD, 14));
        backButton.setBackground(Color.DARK_GRAY);
        backButton.setForeground(Color.WHITE);
        backButton.setFocusPainted(false);
        backButton.addActionListener(e -> {
            gamePanel.updateControls(currentControls);
            cardLayout.show(container, "menu");
        });

        JButton resetButton = new JButton("RESET TO DEFAULT");
        resetButton.setPreferredSize(new Dimension(180, 35));
        resetButton.setFont(new Font("Arial", Font.BOLD, 14));
        resetButton.setBackground(Color.DARK_GRAY);
        resetButton.setForeground(Color.WHITE);
        resetButton.setFocusPainted(false);
        resetButton.addActionListener(e -> resetToDefaultControls());


        JPanel bottomPanel = new JPanel();
        bottomPanel.setOpaque(false);
        bottomPanel.add(resetButton);
        bottomPanel.add(Box.createHorizontalStrut(20)); // spacing
        bottomPanel.add(backButton);

        bottomPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        add(bottomPanel, BorderLayout.SOUTH);

        // Initialize ScorePanel controls display
        String[] initialControls = new String[4];
        for (int p = 0; p < 4; p++) {
            initialControls[p] = getControlStringForPlayer(p);
        }
        scorePanel.updateControls(initialControls);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            int x = (getWidth() - backgroundImage.getWidth()) / 2;
            int y = (getHeight() - backgroundImage.getHeight()) / 2;
            g.drawImage(backgroundImage, x, y, this);
        }
    }

    private String getControlStringForPlayer(int playerIndex) {
        int[] keys = currentControls[playerIndex];
        String up = KeyEvent.getKeyText(keys[0]);
        String down = KeyEvent.getKeyText(keys[1]);
        String left = KeyEvent.getKeyText(keys[2]);
        String right = KeyEvent.getKeyText(keys[3]);
        String fire = KeyEvent.getKeyText(keys[4]);
        return String.format("%s %s %s %s + %s", up, down, left, right, fire);
    }

    private int[][] deepCopyControls(int[][] original) {
        int[][] copy = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = original[i].clone();
        }
        return copy;
    }

    private void resetToDefaultControls() {
        currentControls = deepCopyControls(getDefaultControls());

        // Update each field visually
        for (int p = 0; p < 4; p++) {
            for (int i = 0; i < 5; i++) {
                keyFields[p][i].setText(KeyEvent.getKeyText(currentControls[p][i]));
            }
        }

        // Update the game and score panels
        gamePanel.updateControls(currentControls);

        String[] updatedControls = new String[4];
        for (int p = 0; p < 4; p++) {
            updatedControls[p] = getControlStringForPlayer(p);
        }
        scorePanel.updateControls(updatedControls);
    }


    private int[][] getDefaultControls() {
        return new int[][] {
                {KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_SPACE},
                {KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_ENTER},
                {KeyEvent.VK_T, KeyEvent.VK_G, KeyEvent.VK_F, KeyEvent.VK_H, KeyEvent.VK_Y},
                {KeyEvent.VK_I, KeyEvent.VK_K, KeyEvent.VK_J, KeyEvent.VK_L, KeyEvent.VK_O}
        };
    }


    // Inner class or separate file for stroke title
    private static class TitleLabel extends JComponent {
        private final String text;

        public TitleLabel(String text) {
            this.text = text;
            setPreferredSize(new Dimension(600, 80));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int fontSize = 36;
            Font font = new Font("Arial", Font.BOLD, fontSize);
            g2.setFont(font);

            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(text)) / 2;
            int y = (getHeight() + fm.getAscent()) / 2 - 10;

            // Draw black stroke
            g2.setColor(Color.BLACK);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (dx != 0 || dy != 0) {
                        g2.drawString(text, x + dx, y + dy);
                    }
                }
            }

            // Draw white text
            g2.setColor(Color.WHITE);
            g2.drawString(text, x, y);

            g2.dispose();
        }
    }
}
