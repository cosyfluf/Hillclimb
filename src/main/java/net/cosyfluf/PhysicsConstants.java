package net.cosyfluf;

import org.jbox2d.common.Vec2;

public class PhysicsConstants {
    public static final float PIXELS_PER_METER = 30.0f; // 30 Pixel = 1 Meter
    public static final Vec2 GRAVITY = new Vec2(0.0f, -10.0f); // JBox2D Y-up (-10 m/s^2 nach unten)

    public static final float TIME_STEP = 1.0f / 60.0f; // 60 Updates pro Sekunde
    public static final int VELOCITY_ITERATIONS = 6;    // Für Geschwindigkeitslösung
    public static final int POSITION_ITERATIONS = 2;    // Für Positionslösung

    // --- KONSTANTEN FÜR NITRO ---
    public static final float NITRO_FORCE_MULTIPLIER = 2.0f; // Faktor, um den die Motorkraft erhöht wird
    public static final float NITRO_CONSUMPTION_RATE = 2.0f; // Nitro-Verbrauch pro Sekunde (schneller leer)
    public static final float NITRO_MAX_FUEL = 100.0f;       // Maximale Nitro-Menge
    public static final float NITRO_RECHARGE_AMOUNT = 20.0f; // Menge, die ein Collectible auflädt
    public static final float NITRO_COOLDOWN_TIME = 2.0f;    // Cooldown-Zeit in Sekunden nach Nitro-Ende

    // --- KONSTANTEN FÜR GESCHWINDIGKEITSANZEIGE ---
    public static final float METERS_PER_SECOND_TO_KMH_FACTOR = 3.6f; // m/s * 3.6 = km/h

    // --- GAME OVER ZEIT BEI ÜBER KOPF ---
    public static final float UPSIDE_DOWN_GAME_OVER_TIME = 3.0f; // Sekunden, die das Auto über Kopf liegen muss

    // --- NEU: GLOBALE RAD-DIMENSION ---
    public static final float WHEEL_RADIUS = 0.4f;   // Radius des Rades in Metern
}