package com.ftc8813.pycontrol;

import com.ftc8813.pycontrol.robot.REVHub;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.hardware.HardwareMap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class PyRobotController
{
    private PythonServer server;
    private List<REVHub> hubs = new ArrayList<>();
    
    public PyRobotController(PythonServer server, HardwareMap hardwareMap)
    {
        if (server.started() || server.closed()) throw new IllegalArgumentException("Server already started or closed");
        
        for (LynxModule module : hardwareMap.getAll(LynxModule.class))
        {
            hubs.add(new REVHub(module));
        }
    }
}
