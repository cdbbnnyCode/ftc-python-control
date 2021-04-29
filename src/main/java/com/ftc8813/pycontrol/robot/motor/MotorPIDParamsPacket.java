package com.ftc8813.pycontrol.robot.motor;

import com.ftc8813.pycontrol.robot.REVHub;
import com.ftc8813.pycontrol.robot.SubpacketIn;
import com.ftc8813.pycontrol.robot.SubpacketOut;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;

import java.nio.ByteBuffer;

public class MotorPIDParamsPacket implements SubpacketIn, SubpacketOut
{
    private final int motor;
    private float lastPosP;
    private float lastVelP;
    private float lastVelI;
    private float lastVelD;
    private float lastVelF;
    
    public MotorPIDParamsPacket(int motor)
    {
        this.motor = motor;
    }
    
    
    @Override
    public void load(ByteBuffer data, REVHub hub)
    {
        float kp = data.getFloat();
        float vp = data.getFloat();
        float vi = data.getFloat();
        float vd = data.getFloat();
        float vf = data.getFloat();
        
        hub.getMotorController().setPIDFCoefficients(motor, DcMotor.RunMode.RUN_TO_POSITION,
                new PIDFCoefficients(kp, 0, 0, 0));
        hub.getMotorController().setPIDFCoefficients(motor, DcMotor.RunMode.RUN_USING_ENCODER,
                new PIDFCoefficients(vp, vi, vd, vf));
    }
    
    @Override
    public void store(ByteBuffer data)
    {
        data.putFloat(lastPosP);
        data.putFloat(lastVelP);
        data.putFloat(lastVelI);
        data.putFloat(lastVelD);
        data.putFloat(lastVelF);
    }
    
    @Override
    public int getPacketSize()
    {
        return 20;
    }
    
    @Override
    public boolean update(REVHub hub)
    {
        PIDFCoefficients posCoeffs = hub.getMotorController().getPIDFCoefficients(motor, DcMotor.RunMode.RUN_TO_POSITION);
        PIDFCoefficients velCoeffs = hub.getMotorController().getPIDFCoefficients(motor, DcMotor.RunMode.RUN_USING_ENCODER);
        
        float kp = (float)posCoeffs.p;
        float vp = (float)velCoeffs.p;
        float vi = (float)velCoeffs.i;
        float vd = (float)velCoeffs.d;
        float vf = (float)velCoeffs.f;
        
        if (kp != lastPosP || vp != lastVelP || vi != lastVelI || vd != lastVelD || vf != lastVelF)
        {
            lastPosP = kp;
            lastVelP = vp;
            lastVelI = vi;
            lastVelD = vd;
            lastVelF = vf;
            
            return true;
        }
        return false;
    }
}
