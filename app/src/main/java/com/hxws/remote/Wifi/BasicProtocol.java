package com.hxws.remote.Wifi;


import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class BasicProtocol {

    private static final int LENGTH = 8;

    //引导码
    private static final byte flag = 0x48;

    //命令
    private byte Command;

    private byte len_h;             //协议长度
    private byte len_l;

    private byte id_h;              //应答
    private byte id_l;

    private byte checksum_h;        //协议体的字节累加和
    private byte checksum_l;        //高低位

    //ArrayList body

    ArrayList<byte[]> body = new ArrayList<>();

    void addBody(byte[] bd)
    {
        body.add(bd);
        //Log.d("BasicProtocol", "addBody: "+ Arrays.toString(body.));
    }

    public void Traverse( ArrayList<byte[]> Abody)
    {
        for (int i = 0;i < Abody.size();i++)
        {
            Log.d("BasicProtocol", "addBody: "+ Arrays.toString(Abody.get(i)));
        }
    }

    public void clearBody(ArrayList<byte[]> Abody)
    {
        Abody.clear();
    }

    public int getLength() {
        return LENGTH;
    }

    void setCommand(byte command) {
        Command = command;
    }

    public byte getCommand() {
        return Command;
    }


    void setLen(int len) {
        len_h = (byte) ((len & 0xFF00) >> 8);
        len_l = (byte) (len & 0xFF);
    }


    void setChecksum(int checksum) {
        checksum_h = (byte) ((checksum & 0xFF00) >> 8);
        checksum_l = (byte) (checksum & 0xFF);
    }

    void setId(int id) {
        id_h = (byte) ((id & 0xFF00) >> 8);
        id_l = (byte) ((id & 0xFF));
    }

    public byte[] genContentData() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(LENGTH);
        baos.write(flag);
        baos.write(getCommand());
        baos.write(len_h);
        baos.write(len_l);
        baos.write(id_h);
        baos.write(id_l);
        baos.write(checksum_h);
        baos.write(checksum_l);

//        if (!body.isEmpty())
//        {
//            baos.write(body.get(body.indexOf(body.size())));
//            Log.d("BasicProtocol", "addBody: "+ Arrays.toString(body.get(body.indexOf(body.size()))));
//        }
        return baos.toByteArray();
    }


}
