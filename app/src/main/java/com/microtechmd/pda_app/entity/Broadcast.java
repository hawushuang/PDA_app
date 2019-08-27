package com.microtechmd.pda_app.entity;

import com.microtechmd.pda.library.entity.DataBundle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by Administrator on 2017/12/25.
 */

public class Broadcast extends DataBundle {
    private static final int BROADCAST_LENGTH = 20;
    private static final int DATA_LENGTH = 18;

    private static final String IDENTIFIER = "broadcast";
    private static final String KEY_DATA = IDENTIFIER + "_data";
    private static final String KEY_RF_SIGNAL = IDENTIFIER + "_rf_signal";

    public Broadcast() {
        super();
    }

    private Broadcast(byte[] byteArray) {
        super();
        setByteArray(byteArray);
    }


    public byte[] getData() {
        return getExtras(KEY_DATA);
    }


    public int getRFSignal() {
        return (int) getByte(KEY_RF_SIGNAL);
    }


    private void setData(byte[] data) {
        setExtras(KEY_DATA, data);
    }


    private void setRFSignal(int signal) {
        setByte(KEY_RF_SIGNAL, (byte) signal);
    }


    @Override
    public byte[] getByteArray() {
        final DataBundle.DataOutputStreamLittleEndian dataOutputStream;
        final ByteArrayOutputStream byteArrayOutputStream;

        byteArrayOutputStream = new ByteArrayOutputStream();
        dataOutputStream =
                new DataBundle.DataOutputStreamLittleEndian(byteArrayOutputStream);

        try {
            byteArrayOutputStream.reset();

            final byte[] data = getData();

            if (data != null) {
                dataOutputStream.write(data);
            }

            dataOutputStream.writeByte((byte) getRFSignal());
            dataOutputStream.writeByte(0);
            dataOutputStream.close();
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

        if (byteArray.length >= BROADCAST_LENGTH) {
            final DataBundle.DataInputStreamLittleEndian dataInputStream;
            final ByteArrayInputStream byteArrayInputStream;

            byteArrayInputStream = new ByteArrayInputStream(byteArray);
            dataInputStream =
                    new DataBundle.DataInputStreamLittleEndian(byteArrayInputStream);

            try {
                clearBundle();
                final byte[] data = new byte[DATA_LENGTH];
                dataInputStream.read(data, 0, DATA_LENGTH);
                setData(data);
                setRFSignal((int) dataInputStream.readByte());
                dataInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
