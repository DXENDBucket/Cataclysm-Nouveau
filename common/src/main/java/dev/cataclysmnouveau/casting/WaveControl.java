package dev.cataclysmnouveau.casting;

/** Wave-only access; native contact damage/animation stays in Cataclysm. */
public interface WaveControl {
    void confluxCataclysm$attack(double strength,double x,double z);
    void confluxCataclysm$speed(float multiplier);
}
