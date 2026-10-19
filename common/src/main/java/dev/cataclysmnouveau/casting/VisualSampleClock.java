package dev.cataclysmnouveau.casting;

/** Render frames and packet refreshes must not multiply tick-based native particles. */
public final class VisualSampleClock {
    public enum Step { SKIP, RESET, ADVANCE }
    private long tick=Long.MIN_VALUE,sampledTick=Long.MIN_VALUE;
    private int state=-1,age=-1;
    public Step sample(long now,int nextState,int nextAge) {
        if(now==tick)return Step.SKIP;
        tick=now;
        if(nextState==state && nextAge==age)return Step.SKIP;
        boolean continuous=now==sampledTick+1 && nextState==state && nextAge==age+1;
        sampledTick=now;
        state=nextState;age=nextAge;
        return continuous?Step.ADVANCE:Step.RESET;
    }
}
