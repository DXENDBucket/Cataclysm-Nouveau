package dev.cataclysmnouveau.casting;

public interface LeviathanBeam {
    boolean confluxLeviathan$managed();
    void confluxLeviathan$managed(boolean managed);
    default void confluxLeviathan$advance(boolean advance) {}
    default int confluxLeviathan$executor(){return -1;}
    default void confluxLeviathan$executor(int id) {}
}
