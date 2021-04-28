package com.ftc8813.pycontrol.robot;

public class SendHubData
{
    /*
     Sent data format:
     0x00 | uint8 | number of hubs connected
     0x01 | uint8 | number of hub packets to expect
     
     Hub packet:
     0x00 | uint8 | bitfield of what data has updated
     */
}
