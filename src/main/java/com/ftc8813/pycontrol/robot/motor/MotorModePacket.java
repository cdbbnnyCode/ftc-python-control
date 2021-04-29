package com.ftc8813.pycontrol.robot.motor;

import com.ftc8813.pycontrol.robot.REVHub;
import com.ftc8813.pycontrol.robot.SubpacketIn;
import com.ftc8813.pycontrol.robot.SubpacketOut;
import com.qualcomm.hardware.lynx.LynxDcMotorController;
import com.qualcomm.robotcore.hardware.DcMotor;

import java.nio.ByteBuffer;

/*
 Motor mode: 1 byte
 x x x x x x x x
 M_D M_C M_B M_A
*/
public class MotorModePacket implements SubpacketIn, SubpacketOut
{
    private DcMotor.RunMode[] lastModes = new DcMotor.RunMode[4];
    
    private byte data = 0;
    
    @Override
    public void load(ByteBuffer data, REVHub hub)
    {
        int modes = data.get() & 0xFF;
        LynxDcMotorController controller = hub.getMotorController();
        
        for (int i = 0; i < 4; i++)
        {
            int mode = (modes >> (2*i)) & 0x03;
            controller.setMotorMode(i, DcMotor.RunMode.values()[mode]);
        }
    }
    
    @Override
    public void store(ByteBuffer data)
    {
        data.put(this.data);
    }
    
    @Override
    public int getPacketSize()
    {
        return 1;
    }
    
    @Override
    public boolean update(REVHub hub)
    {
        boolean needsUpdate = false;
        data = 0;
        for (int i = 0; i < 4; i++)
        {
            DcMotor.RunMode mode = hub.getMotorController().getMotorMode(i);
            int modeIdx = mode.ordinal();
            data |= (modeIdx) << (2*i);
            
            if (mode != lastModes[i])
            {
                lastModes[i] = mode;
                needsUpdate = true;
            }
        }
        return needsUpdate;
    }
}
