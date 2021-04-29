package com.ftc8813.pycontrol.robot;

import java.nio.ByteBuffer;

public interface SubpacketIn
{
    void load(ByteBuffer data, REVHub hub);
}
