package com.ftc8813.pycontrol.robot.motor;

import com.ftc8813.pycontrol.robot.REVHub;
import com.ftc8813.pycontrol.robot.SubpacketOut;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

import java.nio.ByteBuffer;

public class MotorCurrentPacket implements SubpacketOut
{
    private final int motor;
    
    private float lastCurrent;
    
    public MotorCurrentPacket(int motor)
    {
        this.motor = motor;
    }
    
    @Override
    public void store(ByteBuffer data)
    {
        data.putFloat(lastCurrent);
    }
    
    @Override
    public int getPacketSize()
    {
        return 4;
    }
    
    @Override
    public boolean update(REVHub hub)
    {
        float current = (float)hub.getMotorController().getMotorCurrent(motor, CurrentUnit.AMPS);
        if (current != lastCurrent)
        {
            lastCurrent = current;
            return true;
        }
        
        return false;
    }
}
