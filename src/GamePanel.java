import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.*;

public class GamePanel extends JPanel implements KeyListener, ActionListener {
    public static final int CELL_SIZE = 45;
    public static final int ROWS = 12;
    public static final int COLS = 20;

    private final Set<Integer> keys = new HashSet<>();
    private final List<Bullet> bullets = new ArrayList<>();
    private final javax.swing.Timer timer = new javax.swing.Timer(16, this);
    private List<ExplosionEffect> explosions = new ArrayList<>();
    private long lastPowerUpSpawnTime = System.currentTimeMillis();

    public ScorePanel scorePanel;
    private boolean needsRoundEndCheck = false;
    private List<Tank> destroyedThisFrame = new ArrayList<>();
    private final Maze maze = new Maze(ROWS, COLS);
    private boolean gameOver = false;
    private boolean resetScheduled = false;
    private boolean wallDeathOccurred = false;

    private Tank[] players;
    private int playerCount = 2;
    private final int[][] controls = {
            {KeyEvent.VK_W, KeyEvent.VK_S, KeyEvent.VK_A, KeyEvent.VK_D, KeyEvent.VK_SPACE},
            {KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT, KeyEvent.VK_ENTER},
            {KeyEvent.VK_I, KeyEvent.VK_K, KeyEvent.VK_J, KeyEvent.VK_L, KeyEvent.VK_M},
            {KeyEvent.VK_T, KeyEvent.VK_G, KeyEvent.VK_F, KeyEvent.VK_H, KeyEvent.VK_B}
    };

    private final List<PowerUp> powerUps = new ArrayList<>();
    private final Random random = new Random();

    public GamePanel(ScorePanel scorePanel) {
        this.scorePanel = scorePanel;
        setPreferredSize(new Dimension(COLS * CELL_SIZE, ROWS * CELL_SIZE));
        setFocusable(true);
        addKeyListener(this);

        players = new Tank[4];
        initializePlayers();
        maze.generate();
        spawnPlayers();
        timer.start();

        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                requestFocusInWindow();
            }
        });

        new java.util.Timer().schedule(new TimerTask() {
            public void run() {
                SwingUtilities.invokeLater(() -> spawnRandomPowerUps());
            }
        }, 25000, 25000);
    }

    public void addBullet(Bullet bullet) {
        bullets.add(bullet);
    }

    private void initializePlayers() {
        Color[] colors = {Color.GREEN, Color.RED, Color.ORANGE, Color.YELLOW};
        String[] images = {"/tank1.png", "/tank2.png", "/tank3.png", "/tank4.png"};
        for (int i = 0; i < 4; i++) {
            players[i] = new Tank(colors[i], images[i]);
        }
    }

    public void setPlayerCount(int count) {
        this.playerCount = Math.min(4, Math.max(2, count));
    }

    private void spawnPlayers() {
        for (int i = 0; i < playerCount; i++) {
            boolean spawned = false;
            int attempts = 0;

            while (!spawned && attempts < 100) {
                players[i].spawn(maze);
                spawned = true;

                for (int j = 0; j < i; j++) {
                    if (Math.hypot(players[i].getX() - players[j].getX(), players[i].getY() - players[j].getY()) < 60) {
                        spawned = false;
                        break;
                    }
                }
                attempts++;
            }
        }
    }

    public enum PowerUpType {
        BIG_BULLETS, MINI_BULLETS, HOMING_MISSILE, BLOCK
    }

    private PowerUpType getRandomPowerUpType() {
        int roll = random.nextInt(100);
        if (roll < 40) return PowerUpType.BIG_BULLETS;
        else if (roll < 75) return PowerUpType.MINI_BULLETS;
        else if (roll < 95) return PowerUpType.HOMING_MISSILE;
        else return PowerUpType.BLOCK;
    }

    private void applyPowerUp(PowerUpType type, Tank player) {
        switch (type) {
            case BIG_BULLETS -> {
                player.setBulletSize(2.3f);
            }
            case MINI_BULLETS -> {
                player.setBulletSize(0.5f);
                player.activateMiniBullets();
            }
            case HOMING_MISSILE -> player.addHomingMissile();
            case BLOCK -> player.giveBlock();
        }
    }

    private void spawnRandomPowerUps() {
        if (gameOver) return;

        int powerUpCount = 1 + random.nextInt(3);
        for (int i = 0; i < powerUpCount; i++) {
            PowerUpType type = getRandomPowerUpType();
            Point pos = getRandomValidPosition();
            powerUps.add(new PowerUp(type, pos.x, pos.y));
        }
    }

    private void resetPlayerPowerUps() {
        for (int i = 0; i < playerCount; i++) {
            players[i].setBulletSize(1.0f); // default bullet size
            players[i].deactivateMiniBullets();
            players[i].removeHomingMissiles();
            players[i].removeBlock();
        }
    }

    private Point getRandomValidPosition() {
        int attempts = 0;
        float spawnRadius = PowerUp.SIZE / 2f;

        while (attempts < 100) {
            float x = random.nextInt(maze.getCols()) * GamePanel.CELL_SIZE + GamePanel.CELL_SIZE / 2f;
            float y = random.nextInt(maze.getRows()) * GamePanel.CELL_SIZE + GamePanel.CELL_SIZE / 2f;

            if (maze.isAreaFree(x - spawnRadius, y - spawnRadius, PowerUp.SIZE, PowerUp.SIZE)) {
                return new Point((int)x, (int)y);
            }

            attempts++;
        }

        // Fallback
        return new Point(100, 100);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        maze.draw(g);

        for (int i = 0; i < playerCount; i++) {
            if (!players[i].isDestroyed()) {
                players[i].draw(g);
            }
        }

        g.setColor(Color.DARK_GRAY);
        for (Bullet b : bullets) b.draw(g);

        for (PowerUp pu : powerUps) {
            pu.draw((Graphics2D) g);
        }

        for (ExplosionEffect exp : explosions) {
            exp.draw(g);
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameOver) return;

        powerUps.removeIf(pu -> !pu.isActive());

        for (int i = 0; i < playerCount; i++) {
            if (!players[i].isDestroyed()) {
                players[i].update(keys, maze, controls[i][0], controls[i][1], controls[i][2], controls[i][3]);
            }
        }

        Iterator<Bullet> bulletIter = bullets.iterator();
        while (bulletIter.hasNext()) {
            Bullet b = bulletIter.next();
            b.update(maze);

            if (!b.isActive()) {
                for (Tank player : players) {
                    player.bulletDeactivated(b);
                }
                bulletIter.remove();
                continue;
            }

            for (int i = 0; i < playerCount; i++) {
                if (players[i].isDestroyed()) continue;

                if (b.checkCollisionWithTank(players[i].getX(), players[i].getY(), Tank.SIZE/2f)) {
                    if (players[i].hasBlock()) {
                        players[i].destroy();
                    } else {
                        explosions.add(new ExplosionEffect(players[i].getX(), players[i].getY(), Tank.SIZE));
                        players[i].destroy();
                        destroyedThisFrame.add(players[i]);  // Track destruction
                        needsRoundEndCheck = true;          // Flag for later check
                    }
                    bulletIter.remove();
                    break;
                }
            }
        }

        // Add this AFTER all bullet processing:
        if (needsRoundEndCheck) {
            checkEndOfRoundAfterHit();
            destroyedThisFrame.clear();
            needsRoundEndCheck = false;
        }

        explosions.removeIf(exp -> !exp.isActive());
        for (ExplosionEffect exp : explosions) exp.update();

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastPowerUpSpawnTime > 30000) {
            spawnRandomPowerUps();
            lastPowerUpSpawnTime = currentTime;
        }

        for (int i = 0; i < playerCount; i++) {
            Tank player = players[i];
            if (player.isDestroyed()) continue;

            Iterator<PowerUp> iter = powerUps.iterator();
            while (iter.hasNext()) {
                PowerUp pu = iter.next();
                if (pu.collidesWith(player.getX(), player.getY())) {
                    applyPowerUp(pu.getType(), player);
                    iter.remove();
                }
            }
        }

        repaint();
    }

    private void checkEndOfRoundAfterHit() {
        if (wallDeathOccurred) return;

        int aliveCount = 0;
        Tank lastAlive = null;

        for (int i = 0; i < playerCount; i++) {
            if (!players[i].isDestroyed()) {
                aliveCount++;
                lastAlive = players[i];
            }
        }

        // Only act if we're not already resetting
        if (!resetScheduled) {
            if (aliveCount == 1) {
                scheduleRoundReset(lastAlive);
            }
            else if (aliveCount == 0) {
                // Mutual destruction - no points
                scheduleRoundReset(null);
            }
        }
    }

    private void scheduleRoundReset(Tank scoringTank) {
        if (resetScheduled) return;
        resetScheduled = true;

        new java.util.Timer().schedule(new TimerTask() {
            public void run() {
                SwingUtilities.invokeLater(() -> {
                    gameOver = true;
                    repaint();

                    new java.util.Timer().schedule(new TimerTask() {
                        public void run() {
                            SwingUtilities.invokeLater(() -> {
                                // Only increment score if we have a valid tank to score
                                if (scoringTank != null && !scoringTank.isDestroyed()) {
                                    scoringTank.incrementScore();
                                }

                                // Reset game state
                                bullets.clear();
                                powerUps.clear();
                                resetPlayerPowerUps();
                                maze.generate();
                                spawnPlayers();
                                updateScores();

                                // Reset flags
                                gameOver = false;
                                resetScheduled = false;
                                wallDeathOccurred = false;

                                repaint();
                            });
                        }
                    }, 2000);
                });
            }
        }, 4000);
    }

    private void updateScores() {
        int[] scores = new int[4];
        for (int i = 0; i < playerCount; i++) {
            scores[i] = players[i].getScore();
        }
        scorePanel.setScores(scores);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        keys.add(e.getKeyCode());

        for (int i = 0; i < playerCount; i++) {
            if (!gameOver && e.getKeyCode() == controls[i][4]) {
                if (players[i].canFire()) {
                    Bullet b = players[i].hasHomingMissiles()
                            ? players[i].fireHomingMissile(maze, players, this)
                            : players[i].fire(maze, this);
                    if (b != null) {
                        bullets.add(b);
                        players[i].recordShot();
                    }
                }
            }
        }
    }

    @Override public void keyReleased(KeyEvent e) { keys.remove(e.getKeyCode()); }
    @Override public void keyTyped(KeyEvent e) {}

    public int[][] getControls() {
        return controls;
    }

    public void updateControls(int[][] newControls) {
        Set<Integer> allKeys = new HashSet<>();
        for (int i = 0; i < playerCount; i++) {
            for (int j = 0; j < 5; j++) {
                if (!allKeys.add(newControls[i][j])) {
                    JOptionPane.showMessageDialog(this,
                            "Duplicate key binding detected! Please use unique keys for each control.",
                            "Control Conflict", JOptionPane.WARNING_MESSAGE);
                    return;
                }
            }
        }

        for (int i = 0; i < playerCount; i++) {
            System.arraycopy(newControls[i], 0, controls[i], 0, 5);
        }
    }

    public void resetGame() {
        bullets.clear();
        powerUps.clear();
        resetPlayerPowerUps();
        maze.generate();
        for (int i = 0; i < playerCount; i++) {
            players[i].resetScore();
        }
        spawnPlayers();
        updateScores();
        gameOver = false;
    }

    public void tankHitWall(Tank tank) {
        if (tank.hasBlock()) {
            tank.destroy();
        } else {
            explosions.add(new ExplosionEffect(tank.getX(), tank.getY(), Tank.SIZE));
            tank.destroy();
            scheduleWallDeathReset();
        }
    }

    public void scheduleWallDeathReset() {
        if (resetScheduled) return;
        wallDeathOccurred = true;

        int aliveCount = 0;
        Tank lastAliveTank = null;

        for (int i = 0; i < playerCount; i++) {
            if (!players[i].isDestroyed()) {
                aliveCount++;
                lastAliveTank = players[i];
            }
        }

        // Only award points if exactly one tank is alive
        Tank tankToScore = (aliveCount == 1) ? lastAliveTank : null;
        scheduleRoundReset(tankToScore);
    }
}
