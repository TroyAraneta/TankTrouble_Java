import java.awt.Color;
import java.awt.Graphics2D;

public class PowerUp {
    public static final int SIZE = 15;
    private static final int LIFESPAN = 60000; // 1 minute in milliseconds

    private final GamePanel.PowerUpType type;
    private final int x, y;
    private final long spawnTime;

    public PowerUp(GamePanel.PowerUpType type, int x, int y) {
        this.type = type;
        this.x = x;
        this.y = y;
        this.spawnTime = System.currentTimeMillis();
    }

    public GamePanel.PowerUpType getType() {
        return type;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public boolean isActive() {
        return System.currentTimeMillis() - spawnTime < LIFESPAN;
    }

    public void draw(Graphics2D g) {
        if (!isActive()) return;

        switch (type) {
            case BIG_BULLETS -> g.setColor(Color.ORANGE);
            case MINI_BULLETS -> g.setColor(Color.CYAN);
            case HOMING_MISSILE -> g.setColor(Color.MAGENTA);
            case BLOCK -> g.setColor(Color.GREEN);
        }
        g.fillOval(x - SIZE / 2, y - SIZE / 2, SIZE, SIZE);

        float progress = 1 - (float)(System.currentTimeMillis() - spawnTime) / LIFESPAN;
        g.setColor(new Color(0, 0, 0, 100));
        g.drawArc(x - SIZE/2, y - SIZE/2, SIZE, SIZE, 90, (int)(360 * progress));
    }

    public boolean collidesWith(float px, float py) {
        double dx = x - px;
        double dy = y - py;
        return Math.sqrt(dx * dx + dy * dy) < SIZE / 2.0;
    }
}
