package com.ftc8813.pycontrol;

import com.ftc8813.pycontrol.robot.HubState;
import com.ftc8813.pycontrol.robot.REVHub;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.RobotLog;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

public class PyRobotController
{
    private PythonServer server;
    private HardwareMap hardwareMap;
    private final List<REVHub> hubs = new ArrayList<>();
    private final List<HubState> hubStates = new ArrayList<>();
    private static final String TAG = "Python Robot Data IO";
    private final JsonObject hwmap = new JsonObject();
    
    public PyRobotController(PythonServer server, HardwareMap hardwareMap)
    {
        this.server = server;
        this.hardwareMap = hardwareMap;
        if (server.started() || server.closed()) throw new IllegalArgumentException("Server already started or closed");
        
        for (LynxModule module : hardwareMap.getAll(LynxModule.class))
        {
            REVHub hub = new REVHub(module);
            hubs.add(hub);
            hubStates.add(new HubState(hub));
        }
        
        loadHwmap();
    }
    
    private void loadHwmap()
    {
        // supported devices:
        // DcMotor
        JsonObject motors = new JsonObject();
        hwmap.add("dcMotor", motors);
    
        for (Map.Entry<String, DcMotor> entry : hardwareMap.dcMotor.entrySet())
        {
            String key = entry.getKey();
            DcMotor motor = entry.getValue();
            int port = motor.getPortNumber();
            int hub = 0;
            for (REVHub h : hubs)
            {
                if (h.getMotorController() == motor.getController())
                {
                    hub = h.getAddress();
                    break;
                }
            }
            JsonArray pair = new JsonArray();
            pair.add(port);
            pair.add(hub);
        
            motors.add(key, pair);
        }
    }
    
    public void setupProcessors()
    {
        server.registerProcessor(0xFD, (cmd, payload, resp) -> {
            ByteBuffer hubinfo = ByteBuffer.allocate(hubs.size());
            for (REVHub hub : hubs)
            {
                hubinfo.put((byte)hub.getAddress());
            }
        
            hubinfo.flip();
            resp.respond(hubinfo);
        });
        server.registerProcessor(0xFC, (cmd, payload, resp) -> {
            String json = new Gson().toJson(hwmap);
            byte[] data = json.getBytes(StandardCharsets.UTF_8);
            
            resp.respond(ByteBuffer.wrap(data));
        });
    
        server.registerProcessor(0xFB, (cmd, payload, resp) -> {
            byte addr = payload.get();
            for (HubState state : hubStates)
            {
            }
        });
    }
}
