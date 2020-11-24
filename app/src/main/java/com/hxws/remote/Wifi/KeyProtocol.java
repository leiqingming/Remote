package com.hxws.remote.Wifi;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class KeyProtocol extends BasicProtocol {

    private static final String TAG = "KeyProtocol";

    private static final byte PACKET_CMD_KEY_EVENT = 0x08;

    private static final int LEN = 6;

    private int id = 0;

    public int ALT_MASK = 0x0001;
    public int CTRL_MASK = 0x0002;
    public int SHIFT_MASK = 0x0004;
    public int ALL_MASK = 0x0000;

    public int metaState = 0;

    private byte action;
    private byte code_h;
    private byte code_l;
    private byte flag;
    private byte meta_state_h;
    private byte meta_state_l;

    @Override
    public int getLength() {
        return super.getLength() + LEN;
    }

    private int b2i(byte x) {
        return ((int) x) & 0xFF;
    }

    private int getChecksum() {
        return b2i(action)
                + b2i(code_h)
                + b2i(code_l)
                + b2i(flag)
                + b2i(meta_state_h)
                + b2i(meta_state_l);
    }

    private void setAction(byte act) {
        action = act;
    }

    public byte getCommand() {
        return PACKET_CMD_KEY_EVENT;
    }

    private void setCode(int code) {
        code_h = (byte) ((code & 0xFF00) >> 8);
        code_l = (byte) (code & 0xFF);
    }

    private void setFlag(byte flg) {
        flag = flg;
    }

    private void setKeyState(int metaState) {
        meta_state_h = 0x00;//预留
        if (metaState == 1) {
            meta_state_l = (byte) (meta_state_l | ALT_MASK);
        } else if (metaState == 2) {
            meta_state_l = (byte) (meta_state_l | CTRL_MASK);
        } else if (metaState == 3) {
            meta_state_l = (byte) (meta_state_l | SHIFT_MASK);
        } else if (metaState == 0) {
            meta_state_l = (byte) (meta_state_l & ALL_MASK);
        }

    }


    //    private int getCode() {
//        return (int)code_h << 8 | code_l;
//    }
    public void setID(int ID) {

        id = ID;
    }


    public int getId() {
        return id;
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
        baos.write(code_h);
        baos.write(code_l);
        baos.write(flag);
        baos.write(meta_state_h);
        baos.write(meta_state_l);

        //this.addBody(baos.toByteArray());

        return baos.toByteArray();
    }

    public void setCmdKeyEvent(int ID, byte action, int code, byte flg, int keyState) {
        setID(ID);
        setAction(action);
        setCode(code);
        setFlag(flg);
        setKeyState(keyState);
    }


    //if ((metaState & SHIFT_MASK) != 0)


}
