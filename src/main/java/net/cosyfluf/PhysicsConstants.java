package net.cosyfluf;

import org.jbox2d.common.Vec2;

public class PhysicsConstants {
    public static final float PIXELS_PER_METER = 30.0f;
    public static final Vec2 GRAVITY = new Vec2(0.0f, -10.0f);

    public static final float TIME_STEP = 1.0f / 60.0f;
    public static final int VELOCITY_ITERATIONS = 6;
    public static final int POSITION_ITERATIONS = 2;

    public static final float NITRO_FORCE_MULTIPLIER = 2.0f;
    public static final float NITRO_CONSUMPTION_RATE = 2.0f;
    public static final float NITRO_MAX_FUEL = 100.0f;
    public static final float NITRO_RECHARGE_AMOUNT = 20.0f;
    public static final float NITRO_COOLDOWN_TIME = 2.0f;

    public static final float METERS_PER_SECOND_TO_KMH_FACTOR = 3.6f;

    public static final float UPSIDE_DOWN_GAME_OVER_TIME = 3.0f;

    public static final float WHEEL_RADIUS = 0.4f;
}