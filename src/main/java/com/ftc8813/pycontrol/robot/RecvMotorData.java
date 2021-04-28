package com.ftc8813.pycontrol.robot;

import com.qualcomm.hardware.lynx.LynxDcMotorController;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;

import java.nio.ByteBuffer;

public class RecvMotorData
{
    /*
     Motor controller data header: 1 byte
     0x00 | uint8 | bitfield of what needs to update here
      - LSB [0] : Modes
      -     [1] : Zero power behavior/enable state
      -     [2] : Motor powers
      -     [3] : Motor target velocity (only effective in RUN_USING_ENCODER)
      -     [4] : Motor target position (only effective in RUN_TO_POSITION)
      -     [5] : PIDF coefficients
      -     [6] : (reserved, should always be 0)
      - MSB [7] : (reserved, should always be 0)
     
     Motor modes: 1 byte
     0 = RUN_WITHOUT_ENCODER, 1 = RUN_USING_ENCODER, 2 = RUN_TO_POSITION, 3 = STOP_AND_RESET_ENCODER
     0x00 | bits 1,0 | Motor 0 mode
          | bits 3,2 | Motor 1 mode
          | bits 5,4 | Motor 2 mode
          | bits 7,6 | Motor 3 mode
          
     Zero power behaviors: 1 byte
     0 = FLOAT, 1 = BRAKE
     0x00 | bit 0 | motor 0 behavior
          | bit 1 | motor 1 behavior
          | bit 2 | motor 2 behavior
          | bit 3 | motor 3 behavior
     0 = disabled, 1 = enabled
          | bit 4 | motor 0 enable
          | bit 5 | motor 1 enable
          | bit 6 | motor 2 enable
          | bit 7 | motor 3 enable
          
     Motor powers (NaN = do not set): 16 bytes
     0x00 | float32 | motor 0 power
     0x04 | float32 | motor 1 power
     0x08 | float32 | motor 2 power
     0x0C | float32 | motor 3 power
     
     Motor target velocities (NaN = do not set): 16 bytes
     0x00 | float32 | motor 0 velocity
     0x04 | float32 | motor 1 velocity
     0x08 | float32 | motor 2 velocity
     0x0C | float32 | motor 3 velocity
     
     Motor target positions: 17 bytes
     0x00 | bit 0   | set motor 0 target position
          | bit 1   | set motor 1 target position
          | bit 2   | set motor 2 target position
          | bit 3   | set motor 3 target position
          | bit 4-7 | (reserved, set to 0)
     0x01 | int32   | motor 0 target position
     0x05 | int32   | motor 1 target position
     0x09 | int32   | motor 2 target position
     0x0D | int32   | motor 3 target position
     
     Motor PIDF coefficients (x4 motors): 80 bytes
     0x00 | float32 | motor position P coefficient
     0x04 | float32 | motor velocity P coefficient
     0x08 | float32 | motor velocity I coefficient
     0x0C | float32 | motor velocity D coefficient
     0x10 | float32 | motor velocity F coefficient
     */
    private boolean update_motor_modes;
    private final int[] motor_modes = new int[4];
    
    private boolean update_motor_zero_powers;
    private final int[] motor_zero_powers = new int[4];
    private final int[] motor_enables     = new int[4];
    
    private boolean update_motor_powers;
    private final float[] motor_powers = new float[4];
    
    private boolean update_motor_velocities;
    private final float[] motor_velocities = new float[4];
    
    private boolean update_motor_positions;
    private int motor_positions_mask;
    private final int[] motor_positions = new int[4];
    
    private boolean update_motor_pidf;
    private final float[][] motor_pidf = new float[4][5];
    
    private static final DcMotor.RunMode[] RUN_MODES = {
            DcMotor.RunMode.RUN_WITHOUT_ENCODER,
            DcMotor.RunMode.RUN_USING_ENCODER,
            DcMotor.RunMode.RUN_TO_POSITION,
            DcMotor.RunMode.STOP_AND_RESET_ENCODER
    };
    
    private static final DcMotor.ZeroPowerBehavior[] ZERO_POWER_BEHAVIORS = {
            DcMotor.ZeroPowerBehavior.FLOAT,
            DcMotor.ZeroPowerBehavior.BRAKE
    };
    
    public void read(ByteBuffer buf)
    {
        int update_mask = buf.get() & 0xFF;
        update_motor_modes       = (update_mask & 0x01) != 0;
        update_motor_zero_powers = (update_mask & 0x02) != 0;
        update_motor_powers      = (update_mask & 0x04) != 0;
        update_motor_velocities  = (update_mask & 0x08) != 0;
        update_motor_positions   = (update_mask & 0x10) != 0;
        update_motor_pidf        = (update_mask & 0x20) != 0;
        
        if (update_motor_modes)
        {
            int v = buf.get() & 0xFF;
            motor_modes[0] = (v & 0x03);
            motor_modes[1] = (v & 0x0C) >> 2;
            motor_modes[2] = (v & 0x30) >> 4;
            motor_modes[3] = (v & 0xC0) >> 6;
        }
        
        if (update_motor_zero_powers)
        {
            int v = buf.get() & 0xFF;
            motor_zero_powers[0] = (v & 0x01);
            motor_zero_powers[1] = (v & 0x02) >> 1;
            motor_zero_powers[2] = (v & 0x04) >> 2;
            motor_zero_powers[3] = (v & 0x08) >> 3;
            
            motor_enables[0] = (v & 0x10) >> 4;
            motor_enables[1] = (v & 0x20) >> 5;
            motor_enables[2] = (v & 0x40) >> 6;
            motor_enables[3] = (v & 0x80) >> 7;
        }
        
        if (update_motor_powers)
        {
            motor_powers[0] = buf.getFloat();
            motor_powers[1] = buf.getFloat();
            motor_powers[2] = buf.getFloat();
            motor_powers[3] = buf.getFloat();
        }
        
        if (update_motor_velocities)
        {
            motor_velocities[0] = buf.getFloat();
            motor_velocities[1] = buf.getFloat();
            motor_velocities[2] = buf.getFloat();
            motor_velocities[3] = buf.getFloat();
        }
        
        if (update_motor_positions)
        {
            motor_positions_mask = buf.get() & 0xFF;
            motor_positions[0] = buf.getInt();
            motor_positions[1] = buf.getInt();
            motor_positions[2] = buf.getInt();
            motor_positions[3] = buf.getInt();
        }
        
        if (update_motor_pidf)
        {
            for (int i = 0; i < 4; i++)
            {
                motor_pidf[i][0] = buf.getFloat();
                motor_pidf[i][1] = buf.getFloat();
                motor_pidf[i][2] = buf.getFloat();
                motor_pidf[i][3] = buf.getFloat();
            }
        }
    }
    
    public void update(REVHub hub)
    {
        LynxDcMotorController controller = hub.getMotorController();
        if (update_motor_modes)
        {
            for (int i = 0; i < 4; i++)
                controller.setMotorMode(i, RUN_MODES[motor_modes[i]]);
        }
        
        if (update_motor_zero_powers)
        {
            for (int i = 0; i < 4; i++)
            {
                controller.setMotorZeroPowerBehavior(i, ZERO_POWER_BEHAVIORS[motor_zero_powers[i]]);
                if (motor_enables[i] == 1) controller.setMotorEnable(i);
                else                       controller.setMotorDisable(i);
            }
        }
        
        if (update_motor_powers)
        {
            for (int i = 0; i < 4; i++)
                if (!Float.isNaN(motor_powers[i])) controller.setMotorPower(i, motor_powers[i]);
        }
        
        if (update_motor_velocities)
        {
            for (int i = 0; i < 4; i++)
                if (!Float.isNaN(motor_velocities[i])) controller.setMotorVelocity(i, motor_velocities[i]);
        }
        
        if (update_motor_positions)
        {
            for (int i = 0; i < 4; i++)
                if ((motor_positions_mask & (1 << i)) != 0) controller.setMotorTargetPosition(i, motor_positions[i]);
        }
        
        if (update_motor_pidf)
        {
            for (int i = 0; i < 4; i++)
            {
                controller.setPIDFCoefficients(i, DcMotor.RunMode.RUN_TO_POSITION,
                        new PIDFCoefficients(motor_pidf[i][0], 0, 0, 0));
                
                controller.setPIDFCoefficients(i, DcMotor.RunMode.RUN_USING_ENCODER,
                        new PIDFCoefficients(motor_pidf[i][1], motor_pidf[i][2], motor_pidf[i][3], motor_pidf[i][4]));
            }
        }
    }
}
