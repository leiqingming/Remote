package com.hxws.remote.Wifi;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class StringProtocol extends BasicProtocol {

    private static final String TAG = "StringProtocol";

    private static final byte PACKET_CMD_STRING = 0x10;

    private int id = 0;

    private static byte charCount;

    private byte[] mBytes;

    private byte reserved;

    //private static int LEN =  charCount + 1 + charCount * 2;
    private int LEN =  0;

    public void setCharCount(byte charCount1)
    {
        charCount = charCount1;
    }

    public byte getCharCount()
    {
        return charCount;
    }

    public void setStr(byte[] str)
    {
        mBytes = str;
    }

    void setStrLen(int charCnt)
    {
        LEN = 1 + 1 + charCnt * 2;
    }

    int getStrLen()
    {
        return LEN;
    }


    @Override
    public int getLength() {
        return super.getLength() + getStrLen();
    }

    public byte getCommand() {
        return PACKET_CMD_STRING;
    }

    private int b2i(byte x) {
        return ((int) x) & 0xFF;
    }

    private int byteArray2i(byte[] src)
    {
        //byte b = 0;
        int c = 0;
        for (int i = 0;i < src.length;i++)
        {
            int current = (int)src[i] & 0xFF;
            c += current;
        }

        Log.d(TAG, "byteArray2i: c:" + c);
        return c;
    }

    private int getChecksum() {
        return b2i(charCount)
                + b2i(reserved)
                + byteArray2i(mBytes)
                ;
    }

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

        baos.write(charCount);
        baos.write(reserved);

        baos.write(mBytes);

        //this.addBody(baos.toByteArray());

        //this.Traverse(this.body);

        //this.clearBody(this.body);

        Log.d(TAG, "genContentData: getChecksum:"+ getChecksum());

        return baos.toByteArray();
    }

    public void setPacketCmdString(int id,byte charCnt,byte[] b)
    {
        setID(id);
        setCharCount(charCnt);
        setStrLen(charCnt);
        setStr(b);
    }

}
