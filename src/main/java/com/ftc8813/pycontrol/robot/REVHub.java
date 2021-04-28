package com.ftc8813.pycontrol.robot;

import com.qualcomm.hardware.lynx.LynxAnalogInputController;
import com.qualcomm.hardware.lynx.LynxController;
import com.qualcomm.hardware.lynx.LynxDcMotorController;
import com.qualcomm.hardware.lynx.LynxDigitalChannelController;
import com.qualcomm.hardware.lynx.LynxI2cDeviceSynch;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.lynx.LynxServoController;
import com.qualcomm.hardware.lynx.LynxVoltageSensor;
import com.qualcomm.robotcore.util.RobotLog;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

public class REVHub
{
    public final LynxModule module;
    
    private LynxDcMotorController motorController;
    private LynxServoController servoController;
    private LynxDigitalChannelController digitalController;
    private LynxAnalogInputController analogController;
    private LynxVoltageSensor voltageSensor;
    private List<LynxI2cDeviceSynch> i2cDevices;
    
    private static final String TAG = "REV Hub wrapper";
    
    public REVHub(LynxModule module)
    {
        this.module = module;
        try
        {
            Field c = LynxModule.class.getDeclaredField("controllers");
            c.setAccessible(true);
            List<LynxController> controllers = (List<LynxController>)c.get(module);
            i2cDevices = new ArrayList<>();
    
            for (LynxController controller : controllers)
            {
                if (controller instanceof LynxDcMotorController)
                {
                    if (motorController != null) RobotLog.ww(TAG, "Multiple LynxDcMotorControllers in module");
                    motorController = (LynxDcMotorController)controller;
                }
                else if (controller instanceof LynxServoController)
                {
                    if (servoController != null) RobotLog.ww(TAG, "Multiple LynxServoControllers in module");
                    servoController = (LynxServoController)controller;
                }
                else if (controller instanceof LynxDigitalChannelController)
                {
                    if (digitalController != null) RobotLog.ww(TAG, "Multiple LynxDigitalChannelControllers in module");
                    digitalController = (LynxDigitalChannelController)controller;
                }
                else if (controller instanceof LynxAnalogInputController)
                {
                    if (analogController != null) RobotLog.ww(TAG, "Multiple LynxAnalogInputControllers in module");
                    analogController = (LynxAnalogInputController)controller;
                }
                else if (controller instanceof LynxVoltageSensor)
                {
                    if (voltageSensor != null) RobotLog.ww(TAG, "Multiple LynxVoltageSensors in module");
                    voltageSensor = (LynxVoltageSensor)controller;
                }
                else if (controller instanceof LynxI2cDeviceSynch)
                {
                    i2cDevices.add((LynxI2cDeviceSynch)controller);
                }
                else
                {
                    RobotLog.ww(TAG, "Unexpected controller with type %s", controller.getClass().getSimpleName());
                }
            }
        }
        catch (NoSuchFieldException | IllegalAccessException e)
        {
            throw new IllegalStateException("Error finding LynxControllers; has this changed in an SDK update?");
        }
    }
    
    public LynxDcMotorController getMotorController()
    {
        return motorController;
    }
    
    public LynxServoController getServoController()
    {
        return servoController;
    }
    
    public LynxDigitalChannelController getDigitalController()
    {
        return digitalController;
    }
    
    public LynxAnalogInputController getAnalogController()
    {
        return analogController;
    }
    
    public LynxVoltageSensor getVoltageSensor()
    {
        return voltageSensor;
    }
    
    public List<LynxI2cDeviceSynch> getI2cDevices()
    {
        return Collections.unmodifiableList(i2cDevices);
    }
}
