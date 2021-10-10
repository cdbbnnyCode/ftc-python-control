package com.ftc8813.pycontrol.robot;
import com.qualcomm.robotcore.util.RobotLog;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HubState
{
    private REVHub hub;
    
    public HubState(REVHub hub)
    {
        this.hub = hub;
    }
    
    public int getAddress()
    {
        return hub.getAddress();
    }
    
    public void readData(ByteBuffer packet)
    {
    
    }
}
