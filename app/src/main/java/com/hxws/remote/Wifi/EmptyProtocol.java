package com.hxws.remote.Wifi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class EmptyProtocol extends BasicProtocol {

    private static final String TAG = "PingAckProtocol";

    private static final byte PACKET_CMD_EMPTY = 0x00;

    private int ID = 0;

    @Override
    public int getLength() {
        return super.getLength();
    }

    public byte getCommand() {
        return PACKET_CMD_EMPTY;
    }

    @Override
    public byte[] genContentData() throws IOException {
        ID++;
        this.setCommand(getCommand());
        this.setLen(getLength());
        this.setId(ID);
        this.setChecksum(0);
        byte[] base = super.genContentData();

        ByteArrayOutputStream baos = new ByteArrayOutputStream(getLength());
        baos.write(base, 0, base.length);           //协议头

        return baos.toByteArray();
    }
}
