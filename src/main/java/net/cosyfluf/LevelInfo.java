package net.cosyfluf;

public class LevelInfo {
    public final String name;
    public final long seed; // Seed für die Zufallsgenerierung des Terrains
    public final double terrainSmoothness;
    public final double terrainAmplitude;
    public final double terrainNoiseFactor; // Steuert die "Zackigkeit" des Terrains
    public final double initialCarXFactor; // z.B. 0.1 für 10% der Breite

    public LevelInfo(String name, long seed, double terrainSmoothness, double terrainAmplitude, double terrainNoiseFactor, double initialCarXFactor) {
        this.name = name;
        this.seed = seed;
        this.terrainSmoothness = terrainSmoothness;
        this.terrainAmplitude = terrainAmplitude;
        this.terrainNoiseFactor = terrainNoiseFactor;
        this.initialCarXFactor = initialCarXFactor;
    }
}