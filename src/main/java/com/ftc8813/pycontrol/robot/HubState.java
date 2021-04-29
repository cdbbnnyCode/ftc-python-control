package com.ftc8813.pycontrol.robot;

import com.ftc8813.pycontrol.robot.motor.MotorCurrentPacket;
import com.ftc8813.pycontrol.robot.motor.MotorEncoderPacket;
import com.ftc8813.pycontrol.robot.motor.MotorModePacket;
import com.ftc8813.pycontrol.robot.motor.MotorOvercurrentLevelPacket;
import com.ftc8813.pycontrol.robot.motor.MotorPIDParamsPacket;
import com.ftc8813.pycontrol.robot.motor.MotorPowerPacket;
import com.ftc8813.pycontrol.robot.motor.MotorStatusPacket;
import com.ftc8813.pycontrol.robot.motor.MotorTargetPosPacket;
import com.ftc8813.pycontrol.robot.motor.MotorVelocityPacket;
import com.ftc8813.pycontrol.robot.motor.MotorZeroEnablePacket;
import com.qualcomm.robotcore.util.RobotLog;

import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HubState
{
    private static class SubpacketOutWrapper
    {
        SubpacketOut packet;
        int id;
        boolean enable;
        
        SubpacketOutWrapper(int id, SubpacketOut packet, boolean enable)
        {
            this.packet = packet;
            this.id = id;
            this.enable = enable;
        }
    }
    
    private static SubpacketOutWrapper wrap(int id, SubpacketOut packet, boolean enable)
    {
        return new SubpacketOutWrapper(id, packet, enable);
    }
    
    private static SubpacketOutWrapper wrap(int id, SubpacketOut packet)
    {
        return new SubpacketOutWrapper(id, packet, true);
    }
    
    private final Map<Integer, SubpacketIn> inPackets;
    private final List<SubpacketOutWrapper> outPackets;
    private final REVHub hub;
    private static final String TAG = "Hub Data IO";
    
    public HubState(REVHub hub)
    {
        this.hub = hub;
        inPackets = new HashMap<>();
        outPackets = new ArrayList<>();
    
        // Create packet list
        
        //////////////////////
        // Motors
        MotorModePacket motorModes = new MotorModePacket();
        MotorZeroEnablePacket motorZeroEnables = new MotorZeroEnablePacket();
        
        inPackets.put(0x0001, motorModes);
        outPackets.add(wrap(0x0001, motorModes));
        
        inPackets.put(0x0002, motorZeroEnables);
        outPackets.add(wrap(0x0002, motorZeroEnables));
        
        outPackets.add(wrap(0x0003, new MotorStatusPacket()));
        
        for (int i = 0; i < 4; i++)
        {
            MotorPowerPacket powerPacket = new MotorPowerPacket(i);
            MotorTargetPosPacket targetPacket = new MotorTargetPosPacket(i);
            MotorOvercurrentLevelPacket levelPacket = new MotorOvercurrentLevelPacket(i);
            MotorPIDParamsPacket pidParamsPacket = new MotorPIDParamsPacket(i);
            
            inPackets.put(0x0008 + i, powerPacket);
            outPackets.add(wrap(0x0008 + i, powerPacket));
            
            inPackets.put(0x0010 + i, targetPacket);
            outPackets.add(wrap(0x0010 + i, targetPacket));
            
            inPackets.put(0x0020 + i, levelPacket);
            outPackets.add(wrap(0x0020 + i, levelPacket));
            
            inPackets.put(0x0030 + i, pidParamsPacket);
            outPackets.add(wrap(0x0030 + i, pidParamsPacket));
            
            outPackets.add(wrap(0x0040 + i, new MotorEncoderPacket(i)));
            outPackets.add(wrap(0x0050 + i, new MotorVelocityPacket(i)));
            outPackets.add(wrap(0x0060 + i, new MotorCurrentPacket(i)));
        }
        
        //////////////////////
        // Servos
    }
    
    public ByteBuffer update(ByteBuffer in)
    {
        while (in.remaining() > 0)
        {
            int id = in.getShort() & 0xFFFF;
            SubpacketIn inPacket = inPackets.get(id);
            
            if (inPacket == null)
            {
                RobotLog.ww(TAG, "Invalid subpacket ID received: %#04x; skipping further data", id);
                break;
            }
            inPacket.load(in, hub);
        }
        
        List<SubpacketOutWrapper> outPackets = new ArrayList<>();
        int outSize = 0;
        for (SubpacketOutWrapper wrapper : this.outPackets)
        {
            if (wrapper.enable && wrapper.packet.update(hub))
            {
                outPackets.add(wrapper);
                outSize += 2 + wrapper.packet.getPacketSize();
            }
        }
        
        ByteBuffer buf = ByteBuffer.allocate(outSize + 1);
        buf.put((byte)hub.getAddress());
        for (SubpacketOutWrapper out : outPackets)
        {
            buf.putShort((short)out.id);
            out.packet.store(buf);
        }
        
        buf.flip();
        
        return buf;
    }
}
