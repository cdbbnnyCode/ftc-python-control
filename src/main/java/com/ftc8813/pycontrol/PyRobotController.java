package com.ftc8813.pycontrol;

import com.ftc8813.pycontrol.robot.HubState;
import com.ftc8813.pycontrol.robot.REVHub;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.RobotLog;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class PyRobotController
{
    private PythonServer server;
    private final List<REVHub> hubs = new ArrayList<>();
    private final List<HubState> hubStates = new ArrayList<>();
    private static final String TAG = "Python Robot Data IO";
    
    public PyRobotController(PythonServer server, HardwareMap hardwareMap)
    {
        if (server.started() || server.closed()) throw new IllegalArgumentException("Server already started or closed");
        
        for (LynxModule module : hardwareMap.getAll(LynxModule.class))
        {
            REVHub hub = new REVHub(module);
            hubs.add(hub);
            hubStates.add(new HubState(hub));
        }
        
        server.registerProcessor(0xFD, (cmd, payload, resp) -> {
            ByteBuffer hubinfo = ByteBuffer.allocate(hubs.size());
            for (REVHub hub : hubs)
            {
                hubinfo.put((byte)hub.getAddress());
            }
            
            hubinfo.flip();
            resp.respond(hubinfo);
        });
        
        for (int i = 0; i < hubStates.size(); i++)
        {
            final HubState state = hubStates.get(i);
            server.registerProcessor(0xF0 - i, (cmd, payload, resp) -> {
                ByteBuffer out = state.update(payload);
                
                resp.respond(out);
            });
        }
    }
}
