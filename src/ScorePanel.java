import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ScorePanel extends JPanel {
    private final List<JLabel> scoreLabels = new ArrayList<>();
    private final List<JLabel> controlLabels = new ArrayList<>();
    private int playerCount = 2;
    private final JPanel scoresPanel;
    private final JPanel controlsPanel;
    private final JPanel powerUpsPanel;
    private final JButton menuButton;
    private final Color darkOrange = new Color(255, 140, 0);

    private static class PowerUpInfo {
        String name;
        String description;
        Color color;

        PowerUpInfo(String name, String description, Color color) {
            this.name = name;
            this.description = description;
            this.color = color;
        }
    }

    // Power-ups info with spawn chances
    private final PowerUpInfo[] powerUps = {
            new PowerUpInfo("BIG BULLETS", "Bigger bullets. (Chance: 40%)", Color.ORANGE),
            new PowerUpInfo("MINI BULLETS", "Smaller bullets that spread. (Chance: 35%)", Color.CYAN),
            new PowerUpInfo("HOMING MISSILE", "Missiles that track enemies. (Chance: 20%)", Color.MAGENTA),
            new PowerUpInfo("BLOCK", "Blocks damage once. (Chance: 5%)", Color.GREEN)
    };

    public ScorePanel(int cellSize, int rows, CardLayout cardLayout, JPanel container) {
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(280, rows * cellSize));
        setBackground(new Color(30, 30, 30));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setOpaque(false);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        mainPanel.add(createTitleLabel());
        mainPanel.add(Box.createVerticalStrut(15));

        scoresPanel = new JPanel();
        scoresPanel.setLayout(new BoxLayout(scoresPanel, BoxLayout.Y_AXIS));
        scoresPanel.setOpaque(false);
        createScoreEntries();
        mainPanel.add(scoresPanel);
        mainPanel.add(Box.createVerticalStrut(15));

        mainPanel.add(createControlsTitle());
        mainPanel.add(Box.createVerticalStrut(8));

        controlsPanel = new JPanel();
        controlsPanel.setLayout(new BoxLayout(controlsPanel, BoxLayout.Y_AXIS));
        controlsPanel.setOpaque(false);
        createControlEntries();  // Start with default empty strings, will update later
        mainPanel.add(controlsPanel);
        mainPanel.add(Box.createVerticalStrut(15));

        mainPanel.add(createPowerUpsTitle());
        mainPanel.add(Box.createVerticalStrut(8));

        powerUpsPanel = new JPanel();
        powerUpsPanel.setLayout(new BoxLayout(powerUpsPanel, BoxLayout.Y_AXIS));
        powerUpsPanel.setOpaque(false);
        createPowerUpEntries();
        mainPanel.add(powerUpsPanel);
        mainPanel.add(Box.createVerticalStrut(15));

        menuButton = createMenuButton(cardLayout, container);
        mainPanel.add(menuButton);

        add(mainPanel, BorderLayout.CENTER);
        setPlayerCount(2);
    }

    private JLabel createTitleLabel() {
        JLabel title = new JLabel("SCORE BOARD");
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setMaximumSize(new Dimension(250, 30));
        return title;
    }

    private JLabel createControlsTitle() {
        JLabel title = new JLabel("CONTROLS");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setMaximumSize(new Dimension(250, 25));
        return title;
    }

    private JLabel createPowerUpsTitle() {
        JLabel title = new JLabel("POWER-UPS");
        title.setFont(new Font("Arial", Font.BOLD, 16));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setMaximumSize(new Dimension(250, 25));
        return title;
    }

    private JButton createMenuButton(CardLayout cardLayout, JPanel container) {
        JButton button = new JButton("MAIN MENU");
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(70, 70, 70));
        button.setBorder(BorderFactory.createLineBorder(Color.RED, 2));
        button.setFocusPainted(false);
        button.addActionListener(e -> cardLayout.show(container, "menu"));
        button.setMaximumSize(new Dimension(200, 36));
        return button;
    }

    private void createScoreEntries() {
        Color[] colors = {Color.GREEN, Color.RED, darkOrange, Color.YELLOW};
        for (int i = 0; i < 4; i++) {
            JPanel playerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
            playerPanel.setOpaque(false);
            playerPanel.setMaximumSize(new Dimension(260, 24));

            JLabel dot = new JLabel("●");
            dot.setForeground(colors[i]);
            dot.setFont(new Font("Arial", Font.BOLD, 14));

            JLabel scoreLabel = new JLabel("Player " + (i + 1) + ": 0");
            scoreLabel.setForeground(Color.WHITE);
            scoreLabel.setFont(new Font("Arial", Font.PLAIN, 14));
            scoreLabel.setMaximumSize(new Dimension(220, 20));
            scoreLabels.add(scoreLabel);

            playerPanel.add(dot);
            playerPanel.add(scoreLabel);
            scoresPanel.add(playerPanel);
        }
    }

    private void createControlEntries() {
        Color[] colors = {Color.GREEN, Color.RED, darkOrange, Color.YELLOW};
        for (int i = 0; i < 4; i++) {
            JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
            controlPanel.setOpaque(false);
            controlPanel.setMaximumSize(new Dimension(260, 22));

            JLabel playerLabel = new JLabel("P" + (i + 1) + ":");
            playerLabel.setForeground(colors[i]);
            playerLabel.setFont(new Font("Arial", Font.BOLD, 12));
            playerLabel.setPreferredSize(new Dimension(25, 18));

            JLabel controlLabel = new JLabel("");  // Empty for now, will be updated externally
            controlLabel.setForeground(Color.WHITE);
            controlLabel.setFont(new Font("Arial", Font.PLAIN, 12));
            controlLabel.setMaximumSize(new Dimension(220, 18));
            controlLabels.add(controlLabel);

            controlPanel.add(playerLabel);
            controlPanel.add(controlLabel);
            controlsPanel.add(controlPanel);
        }
    }

    private void createPowerUpEntries() {
        for (PowerUpInfo p : powerUps) {
            JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2)); // Match Scores
            panel.setOpaque(false);

            JLabel nameLabel = new JLabel(p.name + ": ");
            nameLabel.setForeground(p.color);
            nameLabel.setFont(new Font("Arial", Font.BOLD, 14));

            JLabel descLabel = new JLabel(p.description); // No HTML
            descLabel.setForeground(Color.LIGHT_GRAY);
            descLabel.setFont(new Font("Arial", Font.ITALIC, 11));

            panel.add(nameLabel);
            panel.add(descLabel);
            powerUpsPanel.add(panel);
        }
    }

    public void setPlayerCount(int count) {
        this.playerCount = Math.min(4, Math.max(2, count));
        updateVisiblePlayers();
    }

    private void updateVisiblePlayers() {
        for (int i = 0; i < 4; i++) {
            scoresPanel.getComponent(i).setVisible(i < playerCount);
            controlsPanel.getComponent(i).setVisible(i < playerCount);
        }
        revalidate();
        repaint();
    }

    public void setScores(int[] scores) {
        for (int i = 0; i < scores.length && i < playerCount; i++) {
            scoreLabels.get(i).setText("Player " + (i + 1) + ": " + scores[i]);
        }
    }

    public void setScores(int p1, int p2) {
        scoreLabels.get(0).setText("Player 1: " + p1);
        scoreLabels.get(1).setText("Player 2: " + p2);
    }

    // New method to update control labels dynamically
    public void updateControls(String[] newControls) {
        for (int i = 0; i < controlLabels.size() && i < newControls.length; i++) {
            controlLabels.get(i).setText(newControls[i]);
        }
    }
}
