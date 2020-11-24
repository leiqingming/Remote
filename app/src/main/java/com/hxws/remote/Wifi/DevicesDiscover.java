package com.hxws.remote.Wifi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class DevicesDiscover extends BasicProtocol {

    private static final byte PACKET_CMD_DISCOVER = 0x01;

    private static final int LEN = 0;

    private int id = 0;

    @Override
    public int getLength() {
        return super.getLength() + LEN;
    }

    public byte getCommand() {
        return PACKET_CMD_DISCOVER;
    }

    public int getId() {
        return id;
    }

    @Override
    public byte[] genContentData() throws IOException {
        this.setCommand(getCommand());
        this.setLen(getLength());
        this.setId(getId());
        this.setChecksum(LEN);

        byte[] base = super.genContentData();

        ByteArrayOutputStream baos = new ByteArrayOutputStream(getLength());
        baos.write(base, 0, base.length);           //协议头

        return baos.toByteArray();
    }


}
