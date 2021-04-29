package com.ftc8813.pycontrol.robot.motor;

import com.ftc8813.pycontrol.robot.REVHub;
import com.ftc8813.pycontrol.robot.SubpacketOut;

import java.nio.ByteBuffer;

public class MotorStatusPacket implements SubpacketOut
{
    /*
     Format:
     x  x  x  x  x  x  x  x
     CD CC CB CA TD TC TB TA
     '----.----' '----.----'
     overcurrent  at target
     */
    private byte lastStatus;
    
    @Override
    public void store(ByteBuffer data)
    {
        data.put(lastStatus);
    }
    
    @Override
    public int getPacketSize()
    {
        return 1;
    }
    
    @Override
    public boolean update(REVHub hub)
    {
        byte status = 0;
        for (int i = 0; i < 4; i++)
        {
            boolean overcurrent = hub.getBulkData().isOverCurrent(i);
            boolean atTarget    = hub.getBulkData().isAtTarget(i);
            
            if (overcurrent) status |= (0x10 << i);
            if (atTarget)    status |= (0x01 << i);
        }
        
        if (status != lastStatus)
        {
            lastStatus = status;
            return true;
        }
        
        return false;
    }
}
