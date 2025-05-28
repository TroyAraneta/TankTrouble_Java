import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ExplosionEffect {
    private static final int PARTICLE_COUNT = 80;
    private static final int DURATION_MS = 1000; // 1 second duration
    private static final Color[] COLORS = {
            new Color(255, 100, 0, 220),  // Orange
            new Color(255, 200, 0, 200),   // Yellow
            new Color(255, 50, 0, 240)     // Red
    };

    private final List<Particle> particles = new ArrayList<>();
    private final float x, y;
    private final long startTime;
    private final float size;
    private final Random random = new Random();
    private float currentScale = 0f;
    private float currentAlpha = 1f;

    public ExplosionEffect(float x, float y, float size) {
        this.x = x;
        this.y = y;
        this.size = size * 1.8f; // Make explosion larger than tank
        this.startTime = System.currentTimeMillis();
        createParticles();
    }

    private void createParticles() {
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            float angle = random.nextFloat() * (float)Math.PI * 2;
            float speed = 0.8f + random.nextFloat() * 4f;
            float maxDistance = size * (0.5f + random.nextFloat() * 0.7f);
            float particleSize = 4 + random.nextFloat() * 8;
            Color color = COLORS[random.nextInt(COLORS.length)];

            particles.add(new Particle(angle, speed, maxDistance, particleSize, color));
        }
    }

    public void update() {
        long elapsed = System.currentTimeMillis() - startTime;
        float progress = elapsed / (float)DURATION_MS;

        // Animate scale and fade
        currentScale = progress * 2f; // Grows to 2x size
        currentAlpha = 1f - progress; // Fades out

        // Update particles
        for (Particle p : particles) {
            p.distance = p.maxDistance * progress;
            p.size = p.originalSize * (1f - progress * 0.5f);
        }
    }

    public void draw(Graphics g) {
        if (currentAlpha <= 0) return;

        Graphics2D g2d = (Graphics2D)g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw expanding shockwave
        float waveSize = size * currentScale;
        g2d.setColor(new Color(255, 200, 100, (int)(100 * currentAlpha)));
        g2d.setStroke(new BasicStroke(4f));
        g2d.drawOval((int)(x - waveSize/2), (int)(y - waveSize/2),
                (int)waveSize, (int)waveSize);

        // Draw particles
        for (Particle p : particles) {
            float px = x + (float)Math.cos(p.angle) * p.distance;
            float py = y + (float)Math.sin(p.angle) * p.distance;

            g2d.setColor(new Color(
                    p.color.getRed(),
                    p.color.getGreen(),
                    p.color.getBlue(),
                    (int)(p.color.getAlpha() * currentAlpha)
            ));
            g2d.fillOval((int)(px - p.size/2), (int)(py - p.size/2),
                    (int)p.size, (int)p.size);
        }

        // Draw core explosion
        float coreSize = size * (1f - currentScale * 0.3f);
        GradientPaint gradient = new GradientPaint(
                x - coreSize/2, y - coreSize/2, new Color(255, 255, 0, (int)(150 * currentAlpha)),
                x + coreSize/2, y + coreSize/2, new Color(255, 50, 0, (int)(50 * currentAlpha))
        );
        g2d.setPaint(gradient);
        g2d.fillOval((int)(x - coreSize/2), (int)(y - coreSize/2),
                (int)coreSize, (int)coreSize);

        g2d.dispose();
    }

    public boolean isActive() {
        return System.currentTimeMillis() - startTime < DURATION_MS;
    }

    private static class Particle {
        final float angle;
        final float speed;
        final float maxDistance;
        float distance;
        final float originalSize;
        float size;
        final Color color;

        Particle(float angle, float speed, float maxDistance, float size, Color color) {
            this.angle = angle;
            this.speed = speed;
            this.maxDistance = maxDistance;
            this.originalSize = size;
            this.size = size;
            this.color = color;
            this.distance = 0;
        }
    }
}