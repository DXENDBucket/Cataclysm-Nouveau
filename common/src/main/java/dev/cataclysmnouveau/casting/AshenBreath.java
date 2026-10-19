package dev.cataclysmnouveau.casting;

/** Synced lifetime identifies managed breath on both sides; server session exclusively advances it. */
public interface AshenBreath {
    int confluxRevenant$lifetime();
    void confluxRevenant$lifetime(int ticks);
    void confluxRevenant$advance(boolean value);
}
