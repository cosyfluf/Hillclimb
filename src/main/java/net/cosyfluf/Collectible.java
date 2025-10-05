package net.cosyfluf;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;

import static net.cosyfluf.PhysicsConstants.PIXELS_PER_METER;

public class Collectible {
    public Body body;

    public enum Type { NITRO_RECHARGE }
    public Type type;

    private final float radiusMeters = 0.5f;

    public Collectible(Body body, Type type) {
        this.body = body;
        this.type = type;
    }

    public float getRadiusMeters() {
        return radiusMeters;
    }

    public void draw(Graphics2D g2d, int screenHeight, double cameraOffsetX) {
        Vec2 pos = body.getPosition();
        float xPixel = pos.x * PIXELS_PER_METER - (float)cameraOffsetX;
        float yPixel = screenHeight - (pos.y * PIXELS_PER_METER);

        g2d.setColor(Color.CYAN.darker());
        float radiusPixels = radiusMeters * PIXELS_PER_METER;
        g2d.fill(new Ellipse2D.Double(xPixel - radiusPixels, yPixel - radiusPixels, radiusPixels * 2, radiusPixels * 2));
        g2d.setColor(Color.WHITE);
        g2d.setStroke(new BasicStroke(1f));
        g2d.draw(new Ellipse2D.Double(xPixel - radiusPixels, yPixel - radiusPixels, radiusPixels * 2, radiusPixels * 2));
    }
}