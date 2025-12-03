package com.example.smartair.r3;

import java.util.Date;

import com.example.smartair.model.Child;

public class TechniqueSession {

    private final Child child;
    private final Date timestamp;

    private boolean lipsSealed;
    private boolean slowDeepBreath;
    private boolean breathHeldFor10s;
    private boolean waitedBetweenPuffs;
    private boolean spacerUsedIfNeeded;

    public TechniqueSession(Child child, Date timestamp) {
        this.child = child;
        this.timestamp = timestamp;
    }

    public Child getChild() {
        return child;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public boolean isLipsSealed() {
        return lipsSealed;
    }

    public void setLipsSealed(boolean lipsSealed) {
        this.lipsSealed = lipsSealed;
    }

    public boolean isSlowDeepBreath() {
        return slowDeepBreath;
    }

    public void setSlowDeepBreath(boolean slowDeepBreath) {
        this.slowDeepBreath = slowDeepBreath;
    }

    public boolean isBreathHeldFor10s() {
        return breathHeldFor10s;
    }

    public void setBreathHeldFor10s(boolean breathHeldFor10s) {
        this.breathHeldFor10s = breathHeldFor10s;
    }

    public boolean isWaitedBetweenPuffs() {
        return waitedBetweenPuffs;
    }

    public void setWaitedBetweenPuffs(boolean waitedBetweenPuffs) {
        this.waitedBetweenPuffs = waitedBetweenPuffs;
    }

    public boolean isSpacerUsedIfNeeded() {
        return spacerUsedIfNeeded;
    }

    public void setSpacerUsedIfNeeded(boolean spacerUsedIfNeeded) {
        this.spacerUsedIfNeeded = spacerUsedIfNeeded;
    }


    public boolean isHighQuality() {
        return lipsSealed
                && slowDeepBreath
                && breathHeldFor10s
                && waitedBetweenPuffs;
    }
}
