package bgu.spl.net.impl;

import bgu.spl.net.api.MessageEncoderDecoder;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class MessageEncoderDecoderImpl<T> implements MessageEncoderDecoder {
    private byte[] bytes = new byte[1 << 10]; //start with 1k
    private int len = 0;
    private short opcode = -1;

    @Override
    public String decodeNextByte(byte nextByte) {
        if (nextByte == '\0' & (len != 0 | opcode != -1)) {
            pushByte((byte) ' '); //push space between arguments
        } else if (nextByte == ';') {
            String toSend = popString();
            bytes = new byte[1 << 10];
            opcode = -1;
            return toSend;
        } else pushByte(nextByte); //push 0 for opcode
        if (len == 2 & opcode == -1) {
            opcode = bytesToShort(bytes);
            len = 0;
        }
        return null;
    }

    private void pushByte(byte nextByte) {
        if (len >= bytes.length) {
            bytes = Arrays.copyOf(bytes, len * 2);
        }
        bytes[len++] = nextByte;
    }

    private String popString() {
        String result = new String(bytes, 0, len, StandardCharsets.UTF_8);
        len = 0;
        return opcode + " " + result;
    }

    public short bytesToShort(byte[] byteArr) {
        short result = (short) ((byteArr[0] & 0xff) << 8);
        result += (short) (byteArr[1] & 0xff);
        return result;
    }

    @Override
    public byte[] encode(Object message) {
        String[] args = message.toString().split(" ");
        String msg;
        if(args.length == 2)
            msg = ";";
        else
            msg = message.toString().substring(args[0].length() + args[1].length() + 2) + ";";
        byte[] bytes = (msg).getBytes();
        byte[] bytesArr = new byte[5 + bytes.length - 1];
        bytesArr[0] = (byte) ((Short.parseShort(args[0]) >> 8) & 0xFF);
        bytesArr[1] = (byte) (Short.parseShort(args[0]) & 0xFF);
        bytesArr[2] = (byte) ((Short.parseShort(args[1]) >> 8) & 0xFF);
        bytesArr[3] = (byte) (Short.parseShort(args[1]) & 0xFF);
        for (int i = 0; i < bytes.length; i++) {
            bytesArr[i+4] = bytes[i];
        }
        return bytesArr;
    }
}
