package com.ftc8813.pycontrol.robot;

import com.qualcomm.hardware.lynx.LynxServoController;
import com.qualcomm.robotcore.hardware.PwmControl;
import com.qualcomm.robotcore.hardware.ServoController;

import java.nio.ByteBuffer;

public class RecvServoData
{
    /*
     0x00 | uint8 | what data do we need to update?
      - LSB [0] : Enable/disable states
      -     [1] : Port 0 position
      -     [2] : Port 1 position
      -     [3] : Port 2 position
      -     [4] : Port 3 position
      -     [5] : Port 4 position
      -     [6] : Port 5 position
      - MSB [7] : Set PWM ranges
      
     Servo Enable: 1 byte
     0x00 | bit 0 | port 0 enable
          | bit 1 | port 1 enable
          | bit 2 | port 2 enable
          | bit 3 | port 3 enable
          | bit 4 | port 4 enable
          | bit 5 | port 5 enable
          | bit 6 | (reserved, always 0)
          | bit 7 | (reserved, always 0)
          
     Servo position: 2 bytes
     0x00 | uint16 | position (scaled to 0.0-1.0)
     
     Servo PWM ranges (x6): 36 bytes
     0x00 | uint16 | usFrame      (time between each pulse, typically 20000)
     0x02 | uint16 | usPulseLower (minimum high pulse time, corresponding to a position of 0. Typically 600)
     0x04 | uint16 | usPulseUpper (maximum high pulse time, corresponding to a position of 1. Typically 2400)
     */
    
    private boolean update_state;
    private final int[] states = new int[6];
    
    private final boolean[] update_positions = new boolean[6];
    private final int[] positions = new int[6];
    
    private boolean update_ranges;
    private final int[][] ranges = new int[6][3];
    
    public void read(ByteBuffer payload)
    {
        int flags = payload.get() & 0xFF;
        update_state = (flags & 0x1) != 0;
        
        for (int i = 0; i < 6; i++)
            update_positions[i] = (flags & (2 << i)) != 0;
        
        update_ranges = (flags & 0x80) != 0;
        
        if (update_state)
        {
            int state_flags = payload.get() & 0xFF;
            for (int i = 0; i < 6; i++)
                states[i] = (state_flags & (1 << i)) >> i;
        }
        
        for (int i = 0; i < 6; i++)
        {
            if (update_positions[i]) positions[i] = payload.getShort() & 0xFFFF;
        }
        
        if (update_ranges)
        {
            for (int i = 0; i < 6; i++)
            {
                ranges[i][0] = payload.getShort() & 0xFFFF;
                ranges[i][1] = payload.getShort() & 0xFFFF;
                ranges[i][2] = payload.getShort() & 0xFFFF;
            }
        }
    }
    
    public void update(REVHub hub)
    {
        LynxServoController controller = hub.getServoController();
        
        if (update_state)
        {
            for (int i = 0; i < 6; i++)
            {
                // these enable/disable functions have given me nack errors before, which is odd
                if (states[i] == 1) controller.setServoPwmEnable(i);
                else                controller.setServoPwmDisable(i);
            }
        }
        
        for (int i = 0; i < 6; i++)
        {
            if (update_positions[i])
            {
                controller.setServoPosition(i, positions[i] / 65535.0);
            }
        }
        
        if (update_ranges)
        {
            for (int i = 0; i < 6; i++)
            {
                PwmControl.PwmRange range = new PwmControl.PwmRange(ranges[i][1], ranges[i][2], ranges[i][0]);
                controller.setServoPwmRange(i, range);
            }
        }
    }
}
