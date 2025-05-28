import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class PlayerSelectPanel extends JPanel {
    private int playerCount = 2;
    private JLabel countLabel;
    private BufferedImage backgroundImage;

    public PlayerSelectPanel(CardLayout cardLayout, JPanel container, GamePanel gamePanel) {
        setLayout(new BorderLayout());

        try {
            BufferedImage original = ImageIO.read(getClass().getResource("/images/BGPSelect.png"));
            int newWidth = 600;
            int newHeight = (int) (original.getHeight() * (newWidth / (double) original.getWidth()));
            backgroundImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = backgroundImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(original, 0, 0, newWidth, newHeight, null);
            g2d.dispose();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Title
        TitleLabel title = new TitleLabel("SELECT PLAYERS");
        title.setBorder(BorderFactory.createEmptyBorder(40, 0, 20, 0));
        add(title, BorderLayout.NORTH);

        // Center panel with player count selection
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);

        JPanel countPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        countPanel.setOpaque(false);

        JButton decreaseBtn = createMenuButton("-");
        decreaseBtn.addActionListener(e -> {
            playerCount = Math.max(2, playerCount - 1);
            updateCount();
        });

        countLabel = new JLabel(playerCount + " PLAYERS", SwingConstants.CENTER);
        countLabel.setFont(new Font("ROBOTO", Font.BOLD, 36));
        countLabel.setForeground(Color.GREEN);

        JButton increaseBtn = createMenuButton("+");
        increaseBtn.addActionListener(e -> {
            playerCount = Math.min(4, playerCount + 1);
            updateCount();
        });

        countPanel.add(decreaseBtn);
        countPanel.add(countLabel);
        countPanel.add(increaseBtn);

        centerPanel.add(Box.createVerticalGlue());
        centerPanel.add(countPanel);
        centerPanel.add(Box.createVerticalStrut(30));

        // Buttons (start and back)
        JButton startButton = createMenuButton("START GAME");
        JButton backButton = createMenuButton("BACK TO MENU");

        startButton.addActionListener(e -> {
            gamePanel.setPlayerCount(playerCount);
            gamePanel.resetGame();
            gamePanel.scorePanel.setPlayerCount(playerCount);
            cardLayout.show(container, "game");
        });

        backButton.addActionListener(e -> cardLayout.show(container, "menu"));

        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerPanel.add(startButton);
        centerPanel.add(Box.createVerticalStrut(15));
        centerPanel.add(backButton);
        centerPanel.add(Box.createVerticalGlue());

        add(centerPanel, BorderLayout.CENTER);
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setForeground(Color.BLACK);
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        button.setMaximumSize(new Dimension(200, 50));
        return button;
    }

    private void updateCount() {
        countLabel.setText(playerCount + " PLAYERS");
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

    private static class TitleLabel extends JComponent {
        private final String text;

        public TitleLabel(String text) {
            this.text = text;
            setPreferredSize(new Dimension(600, 100));
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int fontSize = 60;
            Font font = new Font("Arial", Font.BOLD, fontSize);
            g2.setFont(font);

            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(text)) / 2;
            int y = (getHeight() + fm.getAscent()) / 2 - 10;

            g2.setColor(Color.BLACK);
            int stroke = 2;
            for (int dx = -stroke; dx <= stroke; dx++) {
                for (int dy = -stroke; dy <= stroke; dy++) {
                    if (dx != 0 || dy != 0) {
                        g2.drawString(text, x + dx, y + dy);
                    }
                }
            }

            g2.setColor(Color.WHITE);
            g2.drawString(text, x, y);
            g2.dispose();
        }
    }
}
