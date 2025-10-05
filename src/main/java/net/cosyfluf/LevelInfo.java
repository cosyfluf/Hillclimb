package net.cosyfluf;

public class LevelInfo {
    public final String name;
    public final long seed;
    public final double terrainSmoothness;
    public final double terrainAmplitude;
    public final double terrainNoiseFactor;
    public final double initialCarXFactor;

    public LevelInfo(String name, long seed, double terrainSmoothness, double terrainAmplitude, double terrainNoiseFactor, double initialCarXFactor) {
        this.name = name;
        this.seed = seed;
        this.terrainSmoothness = terrainSmoothness;
        this.terrainAmplitude = terrainAmplitude;
        this.terrainNoiseFactor = terrainNoiseFactor;
        this.initialCarXFactor = initialCarXFactor;
    }
}