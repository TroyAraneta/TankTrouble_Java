import javax.swing.*;
import java.awt.*;

public class TankTrouble extends JFrame {
    public TankTrouble() {
        setTitle("Tank Trouble");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Create card layout and container
        CardLayout cardLayout = new CardLayout();
        JPanel container = new JPanel(cardLayout);

        // Create score panel with menu button
        ScorePanel scorePanel = new ScorePanel(
                GamePanel.CELL_SIZE,
                GamePanel.ROWS,
                cardLayout,
                container
        );

        // Create game panel with score panel
        GamePanel gamePanel = new GamePanel(scorePanel);

        // Create main game container
        JPanel gameContainer = new JPanel(new BorderLayout());
        gameContainer.add(gamePanel, BorderLayout.CENTER);
        gameContainer.add(scorePanel, BorderLayout.EAST);


        // Create panels - now passing all required arguments
        MainMenu mainMenu = new MainMenu(container, cardLayout, gamePanel);
        ControlSettingsPanel controlSettings = new ControlSettingsPanel(cardLayout, container, gamePanel, scorePanel);
        PlayerSelectPanel playerSelect = new PlayerSelectPanel(cardLayout, container, gamePanel);

        // Add panels to container
        container.add(mainMenu, "menu");
        container.add(playerSelect, "select");
        container.add(gameContainer, "game");

        // Start with menu visible
        cardLayout.show(container, "menu");

        add(container);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        container.add(mainMenu, "menu");
        container.add(controlSettings, "controls");

        cardLayout.show(container, "menu");

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TankTrouble());
    }
}