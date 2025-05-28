import java.awt.*;
import java.awt.geom.*;

public class ShieldEffect {
    private static final int DURATION = 500; // 0.5 seconds
    private final float x, y;
    private final float size;
    private final long startTime;

    public ShieldEffect(float x, float y, float size) {
        this.x = x;
        this.y = y;
        this.size = size * 1.5f; // Make shield larger than tank
        this.startTime = System.currentTimeMillis();
    }

    public void draw(Graphics g) {
        float progress = (System.currentTimeMillis() - startTime) / (float)DURATION;
        if (progress >= 1) return;

        Graphics2D g2d = (Graphics2D)g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Pulsing shield effect
        float pulse = (float)(Math.sin(progress * Math.PI * 4) * 0.2f + 1f);
        float shieldSize = size * pulse;
        float alpha = 200 * (1 - progress);

        // Yellow shield with gradient
        GradientPaint gradient = new GradientPaint(
                x - shieldSize/2, y, new Color(255, 255, 0, (int)alpha),
                x + shieldSize/2, y, new Color(255, 200, 0, (int)(alpha * 0.7f))
        );

        g2d.setPaint(gradient);
        g2d.setStroke(new BasicStroke(4f));
        g2d.draw(new Ellipse2D.Float(
                x - shieldSize/2, y - shieldSize/2,
                shieldSize, shieldSize
        ));

        g2d.dispose();
    }

    public boolean isActive() {
        return System.currentTimeMillis() - startTime < DURATION;
    }
}