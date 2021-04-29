package com.ftc8813.pycontrol.robot;

import java.nio.ByteBuffer;

public interface SubpacketOut
{
    void store(ByteBuffer data);
    int getPacketSize();
    boolean update(REVHub hub);
}
