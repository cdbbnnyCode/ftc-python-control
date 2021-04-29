package com.ftc8813.pycontrol.robot.motor;

import com.ftc8813.pycontrol.robot.REVHub;
import com.ftc8813.pycontrol.robot.SubpacketIn;
import com.ftc8813.pycontrol.robot.SubpacketOut;

import java.nio.ByteBuffer;

public class MotorTargetPosPacket implements SubpacketIn, SubpacketOut
{
    private final int motor;
    private int lastTarget = 0;
    
    public MotorTargetPosPacket(int motor)
    {
        this.motor = motor;
    }
    
    @Override
    public void load(ByteBuffer data, REVHub hub)
    {
        int target = data.getInt();
        hub.getMotorController().setMotorTargetPosition(motor, target);
    }
    
    @Override
    public void store(ByteBuffer data)
    {
        data.putInt(lastTarget);
    }
    
    @Override
    public int getPacketSize()
    {
        return 4;
    }
    
    @Override
    public boolean update(REVHub hub)
    {
        int target = hub.getMotorController().getMotorTargetPosition(motor);
        if (target != lastTarget)
        {
            lastTarget = target;
            return true;
        }
        
        return false;
    }
}
