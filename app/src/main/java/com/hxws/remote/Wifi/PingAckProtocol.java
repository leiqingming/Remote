package com.hxws.remote.Wifi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PingAckProtocol extends BasicProtocol {

    private static final String TAG = "PingAckProtocol";

    private static final byte PACKET_CMD_PING_ACK = 0x03;

    private static final int LEN = 1;

    private int id = 0;

    private byte result;
    private byte reserved;  //保留

    @Override
    public int getLength() {
        return super.getLength() + LEN;
    }

    private int b2i(byte x) {
        return ((int) x) & 0xFF;
    }

    private int getChecksum() {
        return b2i(result)
                + b2i(reserved);
    }

    public byte getCommand() {
        return PACKET_CMD_PING_ACK;
    }

    @Override
    public byte[] genContentData() throws IOException {
        id++;
        this.setCommand(getCommand());
        this.setLen(getLength());
        this.setId(id);
        this.setChecksum(getChecksum());
        byte[] base = super.genContentData();

        ByteArrayOutputStream baos = new ByteArrayOutputStream(getLength());
        baos.write(base, 0, base.length);           //协议头
        baos.write(result);     //协议体
        //baos.write(reserved);

        return baos.toByteArray();
    }

    public void setPingAckResult(byte ret) {
        result = ret;
    }

}
