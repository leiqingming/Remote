package com.hxws.remote.Wifi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MotionProtocol extends BasicProtocol {
    private static final String TAG = "MotionProtocol";

    private static final byte PACKET_CMD_MOTION_EVENT = 0x09;

    /********************** MotionEvent **********************/

    private static final byte MOTION_ACTION_DOWN = 0x01;
    private static final byte MOTION_ACTION_UP = 0x02;
    private static final byte MOTION_ACTION_MOVE = 0x03;

    /********************************************************/

    private static final int LEN = 5;

    private int id = 0;

    private byte action;
    private byte x_h;
    private byte x_l;
    private byte y_h;
    private byte y_l;

    @Override
    public int getLength() {
        return super.getLength() + LEN;
    }

    private int b2i(byte x) {
        return ((int) x) & 0xFF;
    }

    public void setID(int ID) {
        id = ID;
    }

    public int getId() {
        return id;
    }

    private int getChecksum() {
        return b2i(action) + b2i(x_h) + b2i(x_l) + b2i(y_h) + b2i(y_l);
    }

    private void setAction(byte act) {
        action = act;
    }

    public byte getCommand() {
        return PACKET_CMD_MOTION_EVENT;
    }

    public int getX() {
        int result = (((int) x_h << 8) & 0x7F00) | (int) x_l;
        if ((x_h & 0x80) != 0) {
            return 0 - result;
        }

        return result;
    }

    public int getY() {
        int result = (((int) y_h << 8) & 0x7F00) | (int) y_l;
        if ((y_h & 0x80) != 0) {
            return 0 - result;
        }
        return result;
    }

    private void setX(int x) {
        int absVal = x > 0 ? x : 0 - x;

        x_h = (byte) ((absVal & 0x7F00) >> 8);
        x_l = (byte) (absVal & 0xFF);
        if (x < 0) {
            x_h |= 0x80;
        }
    }

    private void setY(int y) {
        int absVal = y > 0 ? y : 0 - y;

        y_h = (byte) ((absVal & 0x7F00) >> 8);
        y_l = (byte) (absVal & 0xFF);
        if (y < 0) {
            y_h |= 0x80;
        }
    }

    @Override
    public byte[] genContentData() throws IOException {
        this.setCommand(getCommand());
        this.setLen(getLength());
        this.setId(getId());
        this.setChecksum(getChecksum());
        byte[] base = super.genContentData();

        ByteArrayOutputStream baos = new ByteArrayOutputStream(getLength());
        baos.write(base, 0, base.length);           //协议头
        baos.write(action);       //协议体
        baos.write(x_h);
        baos.write(x_l);
        baos.write(y_h);
        baos.write(y_l);

        return baos.toByteArray();
    }


    public void setPacketCmdMotionEvent(int ID, byte action, int x, int y) {
        setID(ID);
        setAction(action);
        setX(x);
        setY(y);
    }

}
