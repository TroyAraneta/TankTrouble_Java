import java.awt.*;

public class Bullet {
    // Constants
    protected static final float SPEED = 3f;
    private static final float RADIUS = 3f;
    private static final int MAX_LIFETIME = 600;
    private static final int BASE_SIZE = 7;

    // Core bullet properties
    protected float x, y;          // Position
    protected float dx, dy;        // Movement vector
    protected int lifetime;        // Frames alive
    protected boolean active;      // Active state
    private int maxLifetime = MAX_LIFETIME;
    private float sizeMultiplier = 1.0f;

    // References
    public final Maze maze;
    protected final GamePanel gamePanel;
    protected final Tank owner;    // Null if environment bullet

    public Bullet(float x, float y, float angle, Maze maze, float speed, int customLifetime,
                  float sizeMultiplier, GamePanel gamePanel) {
        this(x, y, angle, maze, speed, customLifetime, sizeMultiplier, gamePanel, null);
    }

    public Bullet(float x, float y, float angle, Maze maze, float speed, int customLifetime,
                  float sizeMultiplier, GamePanel gamePanel, Tank owner) {
        this.x = x;
        this.y = y;
        this.dx = (float) Math.cos(angle) * speed;
        this.dy = (float) Math.sin(angle) * speed;
        this.maze = maze;
        this.gamePanel = gamePanel;
        this.lifetime = 0;
        this.active = true;
        this.maxLifetime = customLifetime;
        this.sizeMultiplier = sizeMultiplier;
        this.owner = owner;
    }

    public void draw(Graphics g) {
        int drawSize = (int) (BASE_SIZE * sizeMultiplier);
        g.setColor(Color.DARK_GRAY);
        g.fillOval((int) x - drawSize / 2, (int) y - drawSize / 2, drawSize, drawSize);
    }

    public boolean isActive() {
        return active;
    }

    public float getSize() {
        return sizeMultiplier;
    }

    public boolean checkCollisionWithTank(float tankX, float tankY, float radius) {
        if (!active) return false; // only collide if active
        float combinedRadius = radius + (BASE_SIZE * sizeMultiplier) / 2f;
        return Math.hypot(tankX - x, tankY - y) < combinedRadius;
    }

    public void update(Maze maze) {
        if (!active) return;

        float bulletRadius = RADIUS * sizeMultiplier;  // Scale collision radius with size

        float newX = x + dx;
        float newY = y + dy;
        boolean bounced = false;

        int steps = (int) (Math.max(Math.abs(dx), Math.abs(dy))) + 1;
        for (int i = 0; i <= steps && !bounced; i++) {
            float t = i / (float) steps;
            float testX = x + t * dx;
            float testY = y + t * dy;

            int r = (int) testY / GamePanel.CELL_SIZE;
            int c = (int) testX / GamePanel.CELL_SIZE;

            if (r < 0 || r >= GamePanel.ROWS || c < 0 || c >= GamePanel.COLS) {
                deactivate();
                return;
            }

            boolean[][][] walls = maze.getWalls();
            float cellX = c * GamePanel.CELL_SIZE;
            float cellY = r * GamePanel.CELL_SIZE;

            if (walls[r][c][0] && testY - bulletRadius < cellY) { // Top Wall
                newY = cellY + bulletRadius;
                dy = -dy;
                bounced = true;
            } else if (walls[r][c][2] && testY + bulletRadius > cellY + GamePanel.CELL_SIZE) { // Bottom wall
                newY = cellY + GamePanel.CELL_SIZE - bulletRadius;
                dy = -dy;
                bounced = true;
            }

            if (walls[r][c][3] && testX - bulletRadius < cellX) { // Left wall
                newX = cellX + bulletRadius;
                dx = -dx;
                bounced = true;
            } else if (walls[r][c][1] && testX + bulletRadius > cellX + GamePanel.CELL_SIZE) { // Right wall
                newX = cellX + GamePanel.CELL_SIZE - bulletRadius;
                dx = -dx;
                bounced = true;
            }
        }

        x = newX;
        y = newY;
        lifetime++;

        if (lifetime > maxLifetime) {
            deactivate();
        }
    }

    protected void deactivate() {
        if (active) {
            active = false;
            dx = 0;
            dy = 0;
            if (owner != null) {
                owner.bulletDeactivated(this);
            }
        }
    }
}