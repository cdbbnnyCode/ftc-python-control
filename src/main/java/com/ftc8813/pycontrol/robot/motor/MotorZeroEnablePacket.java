package com.ftc8813.pycontrol.robot.motor;

import com.ftc8813.pycontrol.robot.REVHub;
import com.ftc8813.pycontrol.robot.SubpacketIn;
import com.ftc8813.pycontrol.robot.SubpacketOut;
import com.qualcomm.hardware.lynx.LynxDcMotorController;
import com.qualcomm.robotcore.hardware.DcMotor;

import java.nio.ByteBuffer;

public class MotorZeroEnablePacket implements SubpacketIn, SubpacketOut
{
    private byte lastData = 0x0F;
    
    /*
     Format: 1 byte
     x  x  x  x  x  x  x  x
     ZD ZC ZB ZA ED DC EB EA
     */
    @Override
    public void load(ByteBuffer data, REVHub hub)
    {
        int v = data.get();
        LynxDcMotorController controller = hub.getMotorController();
        
        for (int i = 0; i < 4; i++)
        {
            boolean zero = (v & (0x10 << i)) != 0;
            boolean enable = (v & (0x01 << i)) != 0;
            
            if (zero) controller.setMotorZeroPowerBehavior(i, DcMotor.ZeroPowerBehavior.FLOAT);
            else      controller.setMotorZeroPowerBehavior(i, DcMotor.ZeroPowerBehavior.BRAKE);
            
            if (enable) controller.setMotorEnable(i);
            else        controller.setMotorDisable(i);
        }
    }
    
    @Override
    public void store(ByteBuffer data)
    {
        data.put(lastData);
    }
    
    @Override
    public int getPacketSize()
    {
        return 1;
    }
    
    @Override
    public boolean update(REVHub hub)
    {
        LynxDcMotorController controller = hub.getMotorController();
        byte nextData = 0;
        for (int i = 0; i < 4; i++)
        {
            boolean z = controller.getMotorZeroPowerBehavior(i) != DcMotor.ZeroPowerBehavior.FLOAT;
            boolean e = controller.isMotorEnabled(i);
            if (z) nextData |= (0x10 << i);
            if (e) nextData |= (0x01 << i);
        }
        if (lastData != nextData)
        {
            lastData = nextData;
            return true;
        }
        
        return false;
    }
}
