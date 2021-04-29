package com.ftc8813.pycontrol.robot.motor;

import com.ftc8813.pycontrol.robot.REVHub;
import com.ftc8813.pycontrol.robot.SubpacketIn;
import com.ftc8813.pycontrol.robot.SubpacketOut;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

import java.nio.ByteBuffer;

public class MotorOvercurrentLevelPacket implements SubpacketIn, SubpacketOut
{
    private final int motor;
    private float lastLevel = 0;
    
    public MotorOvercurrentLevelPacket(int motor)
    {
        this.motor = motor;
    }
    
    @Override
    public void load(ByteBuffer data, REVHub hub)
    {
        float v = data.getFloat();
        hub.getMotorController().setMotorCurrentAlert(motor, v, CurrentUnit.AMPS);
    }
    
    @Override
    public void store(ByteBuffer data)
    {
        data.putFloat(lastLevel);
    }
    
    @Override
    public int getPacketSize()
    {
        return 4;
    }
    
    @Override
    public boolean update(REVHub hub)
    {
        float v = (float)hub.getMotorController().getMotorCurrentAlert(motor, CurrentUnit.AMPS);
        if (v != lastLevel)
        {
            lastLevel = v;
            return true;
        }
        
        return false;
    }
}
