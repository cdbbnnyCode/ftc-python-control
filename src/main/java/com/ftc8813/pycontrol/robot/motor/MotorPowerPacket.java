package com.ftc8813.pycontrol.robot.motor;

import com.ftc8813.pycontrol.robot.REVHub;
import com.ftc8813.pycontrol.robot.SubpacketIn;
import com.ftc8813.pycontrol.robot.SubpacketOut;

import java.nio.ByteBuffer;

public class MotorPowerPacket implements SubpacketIn, SubpacketOut
{
    private final int motor;
    private float lastPower = 0;
    
    public MotorPowerPacket(int motor)
    {
        this.motor = motor;
    }
    
    @Override
    public void load(ByteBuffer data, REVHub hub)
    {
        float power = data.getFloat();
        hub.getMotorController().setMotorPower(motor, power);
    }
    
    @Override
    public void store(ByteBuffer data)
    {
        data.putFloat(lastPower);
    }
    
    @Override
    public int getPacketSize()
    {
        return 4;
    }
    
    @Override
    public boolean update(REVHub hub)
    {
        float power = (float)hub.getMotorController().getMotorPower(motor);
        if (power != lastPower)
        {
            lastPower = power;
            return true;
        }
        
        return false;
    }
}
