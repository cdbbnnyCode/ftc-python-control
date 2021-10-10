package com.ftc8813.pycontrol.robot;

import com.qualcomm.hardware.lynx.LynxDcMotorController;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;

import java.nio.ByteBuffer;

public class MotorState
{
    private int outputMask = 0xFFFFFF;
    private boolean requestCoeffs = false;
    private REVHub hub;
    private LynxDcMotorController controller;
    
    public MotorState(REVHub hub)
    {
        this.hub = hub;
        this.controller = hub.getMotorController();
    }
    
    private void setMotorMode(int motor, byte mode)
    {
        controller.setMotorMode(motor, DcMotor.RunMode.values()[mode & 0x3]);
        controller.setMotorZeroPowerBehavior(motor, DcMotor.ZeroPowerBehavior.values()[(mode & 0x4) >> 2]);
    }
    
    private void setCoeffs(int motor, float p, float i, float d, float f, float pkp)
    {
        controller.setPIDFCoefficients(motor, DcMotor.RunMode.RUN_USING_ENCODER,
                new PIDFCoefficients(p, i, d, f));
        controller.setPIDFCoefficients(motor, DcMotor.RunMode.RUN_TO_POSITION,
                new PIDFCoefficients(pkp, 0, 0, 0));
    }
    
    private void resetEncoders(int motor)
    {
        DcMotor.RunMode mode = controller.getMotorMode(motor);
        controller.setMotorMode(motor, DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        controller.setMotorMode(motor, mode);
    }
    
    private void stopAll()
    {
        controller.setMotorPower(0, 0);
        controller.setMotorPower(1, 0);
        controller.setMotorPower(2, 0);
        controller.setMotorPower(3, 0);
    }
    
    public void readPacket(int flags, ByteBuffer data)
    {
        if ((flags & 0x000001) != 0) outputMask = (data.get() << 16) | (data.get() << 8) | data.get();
        if ((flags & 0x000002) != 0) controller.setMotorPower(0, data.getFloat());
        if ((flags & 0x000004) != 0) controller.setMotorPower(1, data.getFloat());
        if ((flags & 0x000008) != 0) controller.setMotorPower(2, data.getFloat());
        if ((flags & 0x000010) != 0) controller.setMotorPower(3, data.getFloat());
        if ((flags & 0x000020) != 0) setMotorMode(0, data.get());
        if ((flags & 0x000040) != 0) setMotorMode(1, data.get());
        if ((flags & 0x000080) != 0) setMotorMode(2, data.get());
        if ((flags & 0x000100) != 0) setMotorMode(3, data.get());
        if ((flags & 0x000200) != 0) controller.setMotorTargetPosition(0, data.getInt());
        if ((flags & 0x000400) != 0) controller.setMotorTargetPosition(1, data.getInt());
        if ((flags & 0x000800) != 0) controller.setMotorTargetPosition(2, data.getInt());
        if ((flags & 0x001000) != 0) controller.setMotorTargetPosition(3, data.getInt());
        if ((flags & 0x002000) != 0) setCoeffs(0, data.getFloat(), data.getFloat(), data.getFloat(), data.getFloat(), data.getFloat());
        if ((flags & 0x004000) != 0) setCoeffs(1, data.getFloat(), data.getFloat(), data.getFloat(), data.getFloat(), data.getFloat());
        if ((flags & 0x008000) != 0) setCoeffs(2, data.getFloat(), data.getFloat(), data.getFloat(), data.getFloat(), data.getFloat());
        if ((flags & 0x010000) != 0) setCoeffs(3, data.getFloat(), data.getFloat(), data.getFloat(), data.getFloat(), data.getFloat());
        if ((flags & 0x020000) != 0) resetEncoders(0);
        if ((flags & 0x040000) != 0) resetEncoders(1);
        if ((flags & 0x080000) != 0) resetEncoders(2);
        if ((flags & 0x100000) != 0) resetEncoders(3);
        if ((flags & 0x200000) != 0) stopAll();
        if ((flags & 0x400000) != 0) requestCoeffs = true;
    }
}
