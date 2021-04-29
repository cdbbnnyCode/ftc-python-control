package com.ftc8813.pycontrol.robot.motor;

import com.ftc8813.pycontrol.robot.REVHub;
import com.ftc8813.pycontrol.robot.SubpacketOut;

import java.nio.ByteBuffer;

public class MotorVelocityPacket implements SubpacketOut
{
    private final int motor;
    private int lastVel;
    
    public MotorVelocityPacket(int motor)
    {
        this.motor = motor;
    }
    
    @Override
    public void store(ByteBuffer data)
    {
        data.putInt(lastVel);
    }
    
    @Override
    public int getPacketSize()
    {
        return 4;
    }
    
    @Override
    public boolean update(REVHub hub)
    {
        int vel = hub.getBulkData().getVelocity(motor);
        if (vel != lastVel)
        {
            lastVel = vel;
            return true;
        }
        
        return false;
    }
}
