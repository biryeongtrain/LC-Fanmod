package com.biryeongtrain.lc.player;

import java.util.UUID;

public class Employee implements PlayerRole{
    @Override
    public int getMaxPlayer() {
        return 0;
    }

    @Override
    public UUID pick() {
        return null;
    }
}
