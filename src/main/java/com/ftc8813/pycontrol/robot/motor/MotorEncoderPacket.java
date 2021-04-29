package com.ftc8813.pycontrol.robot.motor;

import com.ftc8813.pycontrol.robot.REVHub;
import com.ftc8813.pycontrol.robot.SubpacketOut;

import java.nio.ByteBuffer;

public class MotorEncoderPacket implements SubpacketOut
{
    private final int motor;
    private int lastEnc;
    
    public MotorEncoderPacket(int motor)
    {
        this.motor = motor;
    }
    
    @Override
    public void store(ByteBuffer data)
    {
        data.putInt(lastEnc);
    }
    
    @Override
    public int getPacketSize()
    {
        return 4;
    }
    
    @Override
    public boolean update(REVHub hub)
    {
        int enc = hub.getBulkData().getEncoder(motor);
        if (enc != lastEnc)
        {
            lastEnc = enc;
            return true;
        }
        
        return false;
    }
}
