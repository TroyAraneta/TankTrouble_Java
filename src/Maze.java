import java.awt.*;
import java.util.*;
import java.util.List;

public class Maze {
    private final int rows, cols;
    private final boolean[][] visited;
    private final boolean[][][] walls;
    private final Random rand = new Random();

    public Maze(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        visited = new boolean[rows][cols];
        walls = new boolean[rows][cols][4]; // top, right, bottom, left
    }

    public void generate() {
        for (boolean[] row : visited) Arrays.fill(row, false);
        for (boolean[][] cell : walls)
            for (boolean[] wall : cell)
                Arrays.fill(wall, true);
        generateMaze(0, 0);
        addLoops(40);
    }

    private void generateMaze(int r, int c) {
        visited[r][c] = true;
        Integer[] dirs = {0, 1, 2, 3};
        Collections.shuffle(Arrays.asList(dirs));
        for (int dir : dirs) {
            int nr = r, nc = c;
            switch (dir) {
                case 0 -> nr--;
                case 1 -> nc++;
                case 2 -> nr++;
                case 3 -> nc--;
            }
            if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && !visited[nr][nc]) {
                walls[r][c][dir] = false;
                walls[nr][nc][(dir + 2) % 4] = false;
                generateMaze(nr, nc);
            }
        }
    }

    private void addLoops(int count) {
        for (int i = 0; i < count; i++) {
            int r = rand.nextInt(rows), c = rand.nextInt(cols);
            List<Integer> dirs = new ArrayList<>(Arrays.asList(0, 1, 2, 3));
            Collections.shuffle(dirs);
            for (int dir : dirs) {
                int nr = r, nc = c;
                switch (dir) {
                    case 0 -> nr--;
                    case 1 -> nc++;
                    case 2 -> nr++;
                    case 3 -> nc--;
                }
                if (nr >= 0 && nr < rows && nc >= 0 && nc < cols && walls[r][c][dir]) {
                    walls[r][c][dir] = false;
                    walls[nr][nc][(dir + 2) % 4] = false;
                    break;
                }
            }
        }
    }

    public boolean isAreaFree(float x, float y, int width, int height) {
        int left = (int)(x) / GamePanel.CELL_SIZE;
        int right = (int)(x + width - 1) / GamePanel.CELL_SIZE;
        int top = (int)(y) / GamePanel.CELL_SIZE;
        int bottom = (int)(y + height - 1) / GamePanel.CELL_SIZE;

        for (int r = top; r <= bottom; r++) {
            for (int c = left; c <= right; c++) {
                if (r < 0 || r >= rows || c < 0 || c >= cols) return false;

                float offsetX = x - c * GamePanel.CELL_SIZE;
                float offsetY = y - r * GamePanel.CELL_SIZE;

                if (walls[r][c][0] && offsetY < 4) return false; // top wall
                if (walls[r][c][1] && offsetX > GamePanel.CELL_SIZE - 4) return false; // right wall
                if (walls[r][c][2] && offsetY > GamePanel.CELL_SIZE - 4) return false; // bottom wall
                if (walls[r][c][3] && offsetX < 4) return false; // left wall
            }
        }

        return true;
    }

    public void draw(Graphics g) {
        g.setColor(Color.BLACK);
        Graphics2D g2 = (Graphics2D) g;
        g2.setStroke(new BasicStroke(4));

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                int x = c * GamePanel.CELL_SIZE;
                int y = r * GamePanel.CELL_SIZE;
                if (walls[r][c][0]) g2.drawLine(x, y, x + GamePanel.CELL_SIZE, y);
                if (walls[r][c][1]) g2.drawLine(x + GamePanel.CELL_SIZE, y, x + GamePanel.CELL_SIZE, y + GamePanel.CELL_SIZE);
                if (walls[r][c][2]) g2.drawLine(x + GamePanel.CELL_SIZE, y + GamePanel.CELL_SIZE, x, y + GamePanel.CELL_SIZE);
                if (walls[r][c][3]) g2.drawLine(x, y + GamePanel.CELL_SIZE, x, y);
            }
        }
    }

    public boolean[][][] getWalls() {
        return walls;
    }
    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

}
