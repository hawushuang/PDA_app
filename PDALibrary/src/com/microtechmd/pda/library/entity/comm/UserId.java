package com.microtechmd.pda.library.entity.comm;


import com.microtechmd.pda.library.entity.DataBundle;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class UserId extends DataBundle {
    public static final int BYTE_ARRAY_LENGTH = 32;

    private static final String IDENTIFIER = "user";
    private static final String KEY_USERID = IDENTIFIER + "_id";


    public UserId() {
        super();
    }


    public UserId(byte[] byteArray) {
        super(byteArray);
    }


    public UserId(final String address) {
        super();
        setAddress(address);
    }


    public String getAddress() {
        byte[] addressByte = getExtras(KEY_USERID);
        StringBuilder addressString = new StringBuilder();

        if (addressByte != null) {
            for (byte anAddressByte : addressByte) {
                String hex = Integer.toHexString(anAddressByte & 0xFF);
                addressString.append(hex.toUpperCase());
            }
        }

        return addressString.toString();
    }


    public void setAddress(final String address) {
        if (address.length() > BYTE_ARRAY_LENGTH) {
            return;
        }

        byte[] addressString = address.getBytes();

        byte[] addressByte = new byte[BYTE_ARRAY_LENGTH];

        for (int i = 0; i < BYTE_ARRAY_LENGTH; i++) {
            if (i < addressString.length) {
                addressByte[i] = addressString[i];
            } else {
                addressByte[i] = 0;
            }
        }

        setExtras(KEY_USERID, addressByte);
    }


    @Override
    public byte[] getByteArray() {
        final DataOutputStreamLittleEndian dataOutputStream;
        final ByteArrayOutputStream byteArrayOutputStream;

        byteArrayOutputStream = new ByteArrayOutputStream();
        dataOutputStream =
                new DataOutputStreamLittleEndian(byteArrayOutputStream);

        try {
            byteArrayOutputStream.reset();
            final byte[] address = getExtras(KEY_USERID);

            if (address != null) {
                dataOutputStream.write(address);
            }
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
            dataInputStream =
                    new DataInputStreamLittleEndian(byteArrayInputStream);

            try {
                clearBundle();
                final byte[] address = new byte[BYTE_ARRAY_LENGTH];
                dataInputStream.read(address, 0, BYTE_ARRAY_LENGTH);
                setExtras(KEY_USERID, address);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
