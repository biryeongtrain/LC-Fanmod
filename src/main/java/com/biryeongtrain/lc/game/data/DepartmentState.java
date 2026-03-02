package com.biryeongtrain.lc.game.data;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class DepartmentState {
    private final DepartmentType type;
    private boolean opened;
    private int expansionLevel;
    @Nullable
    private UUID teamLeader;

    public DepartmentState(DepartmentType type) {
        this.type = type;
        this.opened = false;
        this.expansionLevel = 0;
        this.teamLeader = null;
    }

    public DepartmentType type() {
        return this.type;
    }

    public boolean isOpened() {
        return this.opened;
    }

    public int expansionLevel() {
        return this.expansionLevel;
    }

    @Nullable
    public UUID teamLeader() {
        return this.teamLeader;
    }

    public void open(@Nullable UUID leader) {
        this.opened = true;
        this.expansionLevel = 1;
        this.teamLeader = leader;
    }

    public void expand() {
        if (!this.opened) {
            this.opened = true;
            this.expansionLevel = 1;
            return;
        }
        this.expansionLevel += 1;
    }

    public void setTeamLeader(@Nullable UUID leader) {
        this.teamLeader = leader;
    }
}
