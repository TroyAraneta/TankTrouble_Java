import java.awt.*;
import java.util.Random;
import java.util.Set;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Arrays;

public class Tank {
    // Core tank properties
    private float x, y;
    private float angle = 0f;  // radians
    private BufferedImage sprite;
    private boolean destroyed = false;
    private int score;

    // Movement and physics
    private final float speed = 2.5f;
    private final float rotationSpeed = 0.05f;
    public static final int SIZE = 20;
    private static final int HITBOX_SIZE = 20;

    // Bullet management
    private int maxBullets = 5;
    private int activeBullets = 0;
    private int activeNormalBullets = 0;
    private int activePowerUpBullets = 0;
    private final LinkedList<Long> shotTimestamps = new LinkedList<>();

    // Power-up states
    private float bulletSize = 1.0f;
    private boolean miniBulletsActive = false;
    private int miniBulletsFired = 0;
    private static final int MAX_MINI_BULLETS = 3;
    private int homingMissiles = 0;
    private boolean hasBlock = false;

    // Effects
    private ShieldEffect shieldEffect;

    // Utilities
    private static final Random rand = new Random();

    public Tank(Color color) {
        try {
            if (color.equals(Color.GREEN)) {
                sprite = ImageIO.read(getClass().getResource("/images/tank1.png"));
            } else if (color.equals(Color.RED)) {
                sprite = ImageIO.read(getClass().getResource("/images/tank2.png"));
            } else if (color.equals(Color.ORANGE)) {
                sprite = ImageIO.read(getClass().getResource("/images/tank3.png"));
            } else {
                sprite = ImageIO.read(getClass().getResource("/images/tank4.png"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void incrementScore() {
        score++;
    }

    public int getScore() {
        return score;
    }

    public void resetScore() {
        score = 0;
    }

    public boolean hasBlock() {
        return hasBlock;
    }

    public void giveBlock() {
        hasBlock = true;
        System.out.println("Block granted");
    }

    public void removeBlock() {
        hasBlock = false;
    }

    public void destroy() {
        if (hasBlock) {
            hasBlock = false;
            shieldEffect = new ShieldEffect(x, y, SIZE * 2);
            System.out.println("Block absorbed the hit!");
            return;  // Early return prevents actual destruction
        }
        destroyed = true;
    }

    public void spawn(Maze maze) {
        reset();
        int attempts = 0;
        do {
            x = rand.nextInt(maze.getCols()) * GamePanel.CELL_SIZE + GamePanel.CELL_SIZE / 2f;
            y = rand.nextInt(maze.getRows()) * GamePanel.CELL_SIZE + GamePanel.CELL_SIZE / 2f;
            attempts++;
        } while (!maze.isAreaFree(x - HITBOX_SIZE / 2f, y - HITBOX_SIZE / 2f, HITBOX_SIZE, HITBOX_SIZE)
                && attempts < 100);
        angle = 0f;
    }

    public void update(Set<Integer> keys, Maze maze, int forward, int backward, int left, int right) {
        if (destroyed) return;

        if (keys.contains(left)) angle -= rotationSpeed;
        if (keys.contains(right)) angle += rotationSpeed;

        float dx = 0, dy = 0;
        if (keys.contains(forward)) {
            dx = (float) Math.cos(angle) * speed;
            dy = (float) Math.sin(angle) * speed;
        } else if (keys.contains(backward)) {
            dx = -(float) Math.cos(angle) * speed;
            dy = -(float) Math.sin(angle) * speed;
        }

        float nextX = x + dx;
        float nextY = y + dy;

        if (maze.isAreaFree(nextX - HITBOX_SIZE / 2, nextY - HITBOX_SIZE / 2, HITBOX_SIZE, HITBOX_SIZE)) {
            x = nextX;
            y = nextY;
        }

        if (shieldEffect != null && !shieldEffect.isActive()) {
            shieldEffect = null;
        }
    }

    public void draw(Graphics g) {
        if (destroyed || sprite == null) return;

        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        g2.rotate(angle);

        int scaledWidth = 25;
        int scaledHeight = 15;

        g2.drawImage(sprite, -scaledWidth / 2, -scaledHeight / 2, scaledWidth, scaledHeight, null);
        g2.dispose();

        if (shieldEffect != null) {
            shieldEffect.draw(g);
        }
    }

    public boolean isDestroyed() {
        return destroyed;
    }

    public boolean canFire() {
        long now = System.currentTimeMillis();
        while (!shotTimestamps.isEmpty() && now - shotTimestamps.peekFirst() > 3000) {
            shotTimestamps.pollFirst();
        }
        return shotTimestamps.size() < 2;
    }

    public void recordShot() {
        shotTimestamps.add(System.currentTimeMillis());
    }

    public Bullet fire(Maze maze, GamePanel gamePanel) {
        if (destroyed || !canFire()) return null;

        // Check if we're firing normal bullets
        if (!miniBulletsActive && bulletSize <= 1.0f && homingMissiles <= 0) {
            if (activeNormalBullets >= maxBullets) return null;
        }

        float bulletSpeed = Bullet.SPEED;
        int bulletLifetime = 600;
        float bulletRadius = 3f * bulletSize;
        float step = 1.0f;
        float maxDistance = Tank.SIZE / 2f + bulletRadius;
        float dx = (float) Math.cos(angle) * step;
        float dy = (float) Math.sin(angle) * step;
        float testX = x;
        float testY = y;

        for (float dist = 0; dist <= maxDistance; dist += step) {
            if (!maze.isAreaFree(testX - bulletRadius, testY - bulletRadius,
                    (int) (bulletRadius * 2), (int) (bulletRadius * 2))) {
                gamePanel.tankHitWall(this);
                return null;
            }
            testX += dx;
            testY += dy;
        }

        recordShot();

        if (miniBulletsActive) {
            float spread = 0.2f;
            float speedMultiplier = 1f;
            int customLifetime = 900;

            Bullet b1 = new Bullet(testX, testY, angle - spread, maze,
                    bulletSpeed * speedMultiplier, customLifetime,
                    bulletSize, gamePanel, this);

            Bullet b2 = new Bullet(testX, testY, angle, maze,  // center bullet
                    bulletSpeed * speedMultiplier / 2, customLifetime,
                    bulletSize, gamePanel, this);

            Bullet b3 = new Bullet(testX, testY, angle + spread, maze,
                    bulletSpeed * speedMultiplier, customLifetime,
                    bulletSize, gamePanel, this);

            gamePanel.addBullet(b1);
            gamePanel.addBullet(b2);
            gamePanel.addBullet(b3);

            activePowerUpBullets += 3;  // Count as power-up bullets
            miniBulletsFired += 3;

            if (miniBulletsFired >= MAX_MINI_BULLETS) {
                deactivateMiniBullets();
            }

            return b2;
        }

        if (hasHomingMissiles()) {
            Bullet missile = fireHomingMissile(maze, null, gamePanel);
            if (missile != null) {
                activePowerUpBullets++;  // Count as power-up bullet
                return missile;
            }
        }

        if (bulletSize > 1.0f) {  // Big bullet
            Bullet bullet = new Bullet(testX, testY, angle, maze, bulletSpeed,
                    bulletLifetime, bulletSize, gamePanel, this);
            activePowerUpBullets++;  // Count as power-up bullet

            // Reset bullet size back to normal after firing once
            bulletSize = 1.0f;

            return bullet;
        }


        // Normal bullet
        Bullet bullet = new Bullet(testX, testY, angle, maze, bulletSpeed,
                bulletLifetime, bulletSize, gamePanel, this);
        activeNormalBullets++;
        return bullet;
    }

    public void bulletDeactivated(Bullet bullet) {
        if (bullet.getSize() > 1.0f || bullet instanceof HomingMissile ||
                (bullet.getSize() < 1.0f && miniBulletsActive)) {
            if (activePowerUpBullets > 0) {
                activePowerUpBullets--;
            }
        } else {
            if (activeNormalBullets > 0) {
                activeNormalBullets--;
            }
        }
    }

    public void activateMiniBullets() {
        miniBulletsActive = true;
        miniBulletsFired = 0;
        maxBullets = 6;
        bulletSize = 0.5f;
    }

    public void deactivateMiniBullets() {
        miniBulletsActive = false;
        miniBulletsFired = 0;
        maxBullets = 5;
        bulletSize = 1.0f;
    }

    public void addHomingMissile() {
        homingMissiles++;
    }

    public boolean hasHomingMissiles() {
        return homingMissiles > 0;
    }

    public void removeHomingMissiles() {
        homingMissiles = 0;
    }

    public Bullet fireHomingMissile(Maze maze, Tank[] allPlayers, GamePanel gamePanel) {
        if (destroyed || !canFire() || activeBullets >= maxBullets || homingMissiles <= 0) return null;

        homingMissiles--;
        recordShot();
        activeBullets++;

        // Initial wall collision check (same as regular bullets)
        float bulletRadius = 3f * 1.0f; // Normal size for missiles
        float step = 1.0f;
        float maxDistance = Tank.SIZE / 2f + bulletRadius;
        float dx = (float) Math.cos(angle) * step;
        float dy = (float) Math.sin(angle) * step;
        float testX = x;
        float testY = y;

        for (float dist = 0; dist <= maxDistance; dist += step) {
            if (!maze.isAreaFree(testX - bulletRadius, testY - bulletRadius,
                    (int) (bulletRadius * 2), (int) (bulletRadius * 2))) {
                gamePanel.tankHitWall(this);
                return null;
            }
            testX += dx;
            testY += dy;
        }

        return new HomingMissile(
                testX, // Use the tested position
                testY,
                angle,
                maze,
                Arrays.asList(allPlayers),
                1.0f) {

            @Override
            public void update(Maze maze) {
                super.update(maze);
                if (!maze.isAreaFree(getX(), getY(), 6, 6)) {
                    gamePanel.scheduleWallDeathReset();
                    this.deactivate();
                }
            }
            @Override
            public void deactivate() {
                super.deactivate();
            }
        };
    }

    public void reset() {
        destroyed = false;
        activeNormalBullets = 0;
        activePowerUpBullets = 0;
        activeBullets = 0;
        miniBulletsActive = false;
        miniBulletsFired = 0;
        bulletSize = 1.0f;
        removeHomingMissiles();
        shotTimestamps.clear();
    }

    public void setBulletSize(float size) {
        this.bulletSize = size;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }
}
