import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class MainMenu extends JPanel {
    private final JPanel container;
    private final CardLayout cardLayout;
    private final GamePanel gamePanel;
    private BufferedImage backgroundImage;

    public MainMenu(JPanel container, CardLayout cardLayout, GamePanel gamePanel) {
        this.container = container;
        this.cardLayout = cardLayout;
        this.gamePanel = gamePanel;

        setBackground(new Color(239, 239, 231));
        setLayout(new BorderLayout());

        // Load and resize background image
        try {
            BufferedImage original = ImageIO.read(getClass().getResource("/images/BGMenu.jpg"));
            int newWidth = 600; // Resize as needed
            int newHeight = (int) (original.getHeight() * (newWidth / (double) original.getWidth()));
            backgroundImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = backgroundImage.createGraphics();
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2d.drawImage(original, 0, 0, newWidth, newHeight, null);
            g2d.dispose();
        } catch (IOException e) {
            e.printStackTrace();
        }

        createMenu();
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

    private void createMenu() {
        setLayout(new BorderLayout());

        // Custom title
        TitleLabel title = new TitleLabel("TANK TROUBLE");
        title.setBorder(BorderFactory.createEmptyBorder(40, 0, 20, 0));

        // Button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 50, 0));

        JButton playButton = createMenuButton("PLAY");
        JButton controlsButton = createMenuButton("CONTROLS");
        JButton exitButton = createMenuButton("EXIT");

        playButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        controlsButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        exitButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        playButton.addActionListener(e -> cardLayout.show(container, "select"));
        controlsButton.addActionListener(e -> cardLayout.show(container, "controls"));
        exitButton.addActionListener(e -> System.exit(0));

        buttonPanel.add(Box.createVerticalGlue());
        buttonPanel.add(playButton);
        buttonPanel.add(Box.createVerticalStrut(15));
        buttonPanel.add(controlsButton);
        buttonPanel.add(Box.createVerticalStrut(15));
        buttonPanel.add(exitButton);

        add(title, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 20));
        button.setForeground(Color.BLACK);
        button.setBackground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
        button.setMaximumSize(new Dimension(160, 50));
        return button;
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

            // Simulate black stroke by drawing text multiple times around the center
            g2.setColor(Color.BLACK);
            int strokeSize = 2;
            for (int dx = -strokeSize; dx <= strokeSize; dx++) {
                for (int dy = -strokeSize; dy <= strokeSize; dy++) {
                    if (dx != 0 || dy != 0) {
                        g2.drawString(text, x + dx, y + dy);
                    }
                }
            }

            // Draw the main white text on top
            g2.setColor(Color.WHITE);
            g2.drawString(text, x, y);

            g2.dispose();
        }
    }

}
