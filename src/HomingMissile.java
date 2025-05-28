import java.awt.*;
import java.util.List;
import java.util.Queue;
import java.util.LinkedList;

public class HomingMissile extends Bullet {
    private static final float SPEED = 3.5f;
    private static final int HOMING_DELAY = 300; // 5 seconds at 60fps
    private static final float TURN_RATE = 0.05f;

    private final List<Tank> players;
    private Tank target;
    private boolean isHoming = false;
    private int framesAlive = 0;

    public HomingMissile(float x, float y, float angle, Maze maze,
                         List<Tank> players, float sizeMultiplier) {
        super(x, y, angle, maze, SPEED, 2000, sizeMultiplier, null);
        this.players = players;

        // Initialize movement direction
        this.dx = (float) Math.cos(angle) * SPEED;
        this.dy = (float) Math.sin(angle) * SPEED;
    }

    @Override
    public void update(Maze maze) {
        framesAlive++;

        if (framesAlive > HOMING_DELAY) {
            isHoming = true;
            target = findNearestTarget();
        }

        if (isHoming && target != null && !target.isDestroyed()) {
            float currentAngle = (float) Math.atan2(dy, dx);
            float desiredAngle = (float) Math.atan2(target.getY() - y, target.getX() - x);
            float angleDiff = (float) Math.atan2(Math.sin(desiredAngle - currentAngle),
                    Math.cos(desiredAngle - currentAngle));
            float newAngle = currentAngle + angleDiff * TURN_RATE;

            dx = (float) Math.cos(newAngle) * SPEED;
            dy = (float) Math.sin(newAngle) * SPEED;
        }

        super.update(maze);
    }

    private int getPathDistance(Maze maze, int startRow, int startCol, int targetRow, int targetCol) {
        int rows = maze.getRows();
        int cols = maze.getCols();
        boolean[][] visited = new boolean[rows][cols];
        Queue<int[]> queue = new LinkedList<>();
        queue.offer(new int[]{startRow, startCol, 0});
        visited[startRow][startCol] = true;

        int[][] directions = {{-1, 0}, {0, 1}, {1, 0}, {0, -1}};

        while (!queue.isEmpty()) {
            int[] current = queue.poll();
            int r = current[0];
            int c = current[1];
            int dist = current[2];

            if (r == targetRow && c == targetCol) {
                return dist;
            }

            for (int i = 0; i < 4; i++) {
                int nr = r + directions[i][0];
                int nc = c + directions[i][1];

                if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && !visited[nr][nc]) {
                    if (!maze.getWalls()[r][c][i]) {
                        visited[nr][nc] = true;
                        queue.offer(new int[]{nr, nc, dist + 1});
                    }
                }
            }
        }

        return Integer.MAX_VALUE;
    }

    private Tank findNearestTarget() {
        Tank nearest = null;
        int shortestPath = Integer.MAX_VALUE;

        for (Tank t : players) {
            if (t.isDestroyed()) continue;

            int startRow = (int) (y / GamePanel.CELL_SIZE);
            int startCol = (int) (x / GamePanel.CELL_SIZE);
            int targetRow = (int) (t.getY() / GamePanel.CELL_SIZE);
            int targetCol = (int) (t.getX() / GamePanel.CELL_SIZE);

            int pathLen = getPathDistance(maze, startRow, startCol, targetRow, targetCol);

            if (pathLen < shortestPath) {
                shortestPath = pathLen;
                nearest = t;
            }
        }

        return nearest;
    }

    @Override
    public void draw(Graphics g) {
        if (isHoming) {
            g.setColor(Color.RED);
        } else {
            g.setColor(Color.YELLOW);
        }
        g.fillOval((int) x - 5, (int) y - 5, 10, 10);

        float currentAngle = (float) Math.atan2(dy, dx);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setColor(isHoming ? Color.ORANGE : Color.BLACK);
        g2d.setStroke(new BasicStroke(2f));
        g2d.rotate(currentAngle, x, y);
        g2d.drawLine((int) x, (int) y, (int) x + 10, (int) y);
        g2d.dispose();
    }
}
