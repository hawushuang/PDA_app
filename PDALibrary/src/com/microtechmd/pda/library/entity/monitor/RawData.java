package com.microtechmd.pda.library.entity.monitor;

import android.util.Log;

import com.microtechmd.pda.library.entity.DataBundle;
import com.microtechmd.pda.library.entity.DataBundleUnsigned;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class RawData extends DataBundleUnsigned {
    public static final int BYTE_ARRAY_LENGTH = 17;

    private static final String IDENTIFIER = "raw_data";
    private static final String KEY_P1 = IDENTIFIER + "_p1";
    private static final String KEY_P2 = IDENTIFIER + "_p2";
    private static final String KEY_P3 = IDENTIFIER + "_p3";
    private static final String KEY_P4 = IDENTIFIER + "_p4";
    private static final String KEY_P5 = IDENTIFIER + "_p5";
    private static final String KEY_P6 = IDENTIFIER + "_p6";
    private static final String KEY_P7 = IDENTIFIER + "_p7";
    private static final String KEY_P8 = IDENTIFIER + "_p8";
    private static final String KEY_P9 = IDENTIFIER + "_p9";


    public RawData() {
        super();
    }

    public RawData(byte[] byteArray) {
        super(byteArray);
    }

    public int getP1() {
        return ((int) getByte(KEY_P1)) & 0xFF;
    }

    public int getP2() {
        return getInt(KEY_P2);
    }

    public int getP3() {
        return getInt(KEY_P3);
    }

    public int getP4() {
        return getInt(KEY_P4);
    }

    public int getP5() {
        return getInt(KEY_P5);
    }

    public int getP6() {
        return getInt(KEY_P6);
    }

    public int getP7() {
        return getInt(KEY_P7);
    }

    public int getP8() {
        return getInt(KEY_P8);
    }

    public int getP9() {
        return getInt(KEY_P9);
    }

    public void setP1(int event) {
        setByte(KEY_P1, (byte) event);
    }

    public void setP2(int index) {
        setInt(KEY_P2, index);
    }

    public void setP3(int index) {
        setInt(KEY_P3, index);
    }

    public void setP4(int index) {
        setInt(KEY_P4, index);
    }

    public void setP5(int index) {
        setInt(KEY_P5, index);
    }

    public void setP6(int index) {
        setInt(KEY_P6, index);
    }

    public void setP7(int index) {
        setInt(KEY_P7, index);
    }

    public void setP8(int index) {
        setInt(KEY_P8, index);
    }

    public void setP9(int index) {
        setInt(KEY_P9, (index));
    }

    @Override
    public byte[] getByteArray() {
        final DataOutputStreamLittleEndian dataOutputStream;
        final ByteArrayOutputStream byteArrayOutputStream;

        byteArrayOutputStream = new ByteArrayOutputStream();
        dataOutputStream = new DataOutputStreamLittleEndian(byteArrayOutputStream);

        try {
            byteArrayOutputStream.reset();
            dataOutputStream.writeByte((byte) getP1());
            dataOutputStream.writeShortLittleEndian((short) getP2());
            dataOutputStream.writeShortLittleEndian((short) getP3());
            dataOutputStream.writeShortLittleEndian((short) getP4());
            dataOutputStream.writeShortLittleEndian((short) getP5());
            dataOutputStream.writeShortLittleEndian((short) getP6());
            dataOutputStream.writeShortLittleEndian((short) getP7());
            dataOutputStream.writeShortLittleEndian((short) getP8());
            dataOutputStream.writeShortLittleEndian((short) getP9());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return byteArrayOutputStream.toByteArray();
    }

    @Override
    public void setByteArray(byte[] byteArray) {
        if (byteArray == null) {
            return;
        }

        if (byteArray.length >= BYTE_ARRAY_LENGTH) {
            final DataInputStreamLittleEndian dataInputStream;
            final ByteArrayInputStream byteArrayInputStream;

            byteArrayInputStream = new ByteArrayInputStream(byteArray);
            dataInputStream = new DataInputStreamLittleEndian(byteArrayInputStream);

            try {
                clearBundle();
                setP1((int) dataInputStream.readByte());
                setP2(dataInputStream.readShortLittleEndian());
                setP3(dataInputStream.readShortLittleEndian());
                setP4(dataInputStream.readShortLittleEndian());
                setP5(dataInputStream.readShortLittleEndian());
                setP6(dataInputStream.readShortLittleEndian());
                setP7(dataInputStream.readShortLittleEndian());
                setP8(dataInputStream.readShortLittleEndian());
                setP9(dataInputStream.readShortLittleEndian());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
