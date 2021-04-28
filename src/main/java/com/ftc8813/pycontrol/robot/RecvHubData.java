package com.ftc8813.pycontrol.robot;

import com.qualcomm.hardware.lynx.LynxController;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.hardware.HardwareMap;

import java.nio.ByteBuffer;
import java.util.List;

public class RecvHubData
{
    /*
     Received data format:
     0x00 | uint8 | number of hub packets to expect
     
     Hub packet:
     0x00 | uint8 | REV hub ID
     0x01 | uint8 | bitfield listing which modules need to update
      - LSB [0] : Motor controller
      -     [1] : Servo controller
      -     [2] : Digital IO (change pin direction / output level)
      -     [3] : Direct I2C write
      -     [4] : Direct I2C read
      -     [5] : Built-in I2C sensor write
      -     [6] : Built-in I2C sensor read
      - MSB [7] : REV hub LED
     
     */
    private int hubID;
    
    private boolean updateMotorData;
    private RecvMotorData motorData = new RecvMotorData();
    
    private boolean updateServoData;
    
    private boolean updateDigitalData;
    
    private boolean updateI2CWrite;
    
    private boolean updateI2CRead;
    
    private boolean updateI2CBuiltinWrite;
    
    private boolean updateI2CBuiltinRead;
    
    private boolean updateLED;
    
    public RecvHubData(HardwareMap hardwareMap)
    {
    
    }
    
    public void read(ByteBuffer payload)
    {
        int flags = payload.get() & 0xFF;
        updateMotorData = (flags & 0x01) != 0;
        updateServoData = (flags & 0x02) != 0;
        updateDigitalData = (flags & 0x04) != 0;
        updateI2CWrite = (flags & 0x08) != 0;
        updateI2CRead = (flags & 0x10) != 0;
        updateI2CBuiltinWrite = (flags & 0x20) != 0;
        updateI2CBuiltinRead = (flags & 0x40) != 0;
        updateLED = (flags & 0x80) != 0;
        
        if (updateMotorData) motorData.read(payload);
        
        if (updateServoData)
            throw new IllegalArgumentException("updateServoData unimplemented");
    
        if (updateDigitalData)
            throw new IllegalArgumentException("updateDigitalData unimplemented");
    
        if (updateI2CWrite)
            throw new IllegalArgumentException("updateI2CWrite unimplemented");
        
        if (updateI2CRead)
            throw new IllegalArgumentException("updateI2CRead unimplemented");
        
        if (updateI2CBuiltinWrite)
            throw new IllegalArgumentException("updateI2CBuiltinWrite unimplemented");
        
        if (updateI2CBuiltinRead)
            throw new IllegalArgumentException("updateI2CBuiltinRead unimplemented");
        
        if (updateLED)
            throw new IllegalArgumentException("updateLED unimplemented");
    }
    
    public void update(List<REVHub> hubs)
    {
        REVHub hub = null;
        for (REVHub h : hubs)
        {
            if (h.module.getModuleAddress() == hubID)
            {
                hub = h;
                break;
            }
        }
        
        if (hub == null) throw new IllegalArgumentException("Invalid REV hub ID: " + hubID);
        
        if (updateMotorData) motorData.update(hub);
    
        if (updateServoData)
            throw new IllegalArgumentException("updateServoData unimplemented");
    
        if (updateDigitalData)
            throw new IllegalArgumentException("updateDigitalData unimplemented");
    
        if (updateI2CWrite)
            throw new IllegalArgumentException("updateI2CWrite unimplemented");
    
        if (updateI2CRead)
            throw new IllegalArgumentException("updateI2CRead unimplemented");
    
        if (updateI2CBuiltinWrite)
            throw new IllegalArgumentException("updateI2CBuiltinWrite unimplemented");
    
        if (updateI2CBuiltinRead)
            throw new IllegalArgumentException("updateI2CBuiltinRead unimplemented");
    
        if (updateLED)
            throw new IllegalArgumentException("updateLED unimplemented");
    }
    
}
