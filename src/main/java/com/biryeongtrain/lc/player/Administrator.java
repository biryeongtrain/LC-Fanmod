package com.biryeongtrain.lc.player;

import java.util.UUID;

public class Administrator implements PlayerRole{
    @Override
    public int getMaxPlayer() {
        return 1;
    }

    @Override
    public UUID pick() {
        return null;
    }
}
