/**
 * WebUtils.java
 *
 * @Title:
 * @Description:
 * @Copyright:微泰医疗器械(杭州)有限公司 Copyright (c) 2015
 * @Company:微泰医疗器械(杭州)有限公司
 * @Created on 2015-5-18 上午9:17:42
 * @author lijianhang
 * @version 1.0
 */

package com.microtechmd.pda_app.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.MalformedInputException;

/**
 * @author lijianhang
 */
public final class WebUtils {

    private WebUtils() {
    }

    public static void closeInputStream(InputStream inputStream) {

        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
            }
            inputStream = null;
        }
    }

    public static String inputStream2String(final InputStream is, final Charset charset)
            throws IOException {

        StringBuilder out = new StringBuilder();
        byte[] b = new byte[4096];
        byte[] savedBytes = new byte[1];
        boolean hasSavedBytes = false;
        CharsetDecoder decoder = charset.newDecoder();

        try {
            for (int n; (n = is.read(b)) != -1; ) {
                if (hasSavedBytes) {
                    byte[] bTmp = new byte[savedBytes.length + b.length];
                    System.arraycopy(savedBytes, 0, bTmp, 0, savedBytes.length);
                    System.arraycopy(b, 0, bTmp, savedBytes.length, b.length);
                    b = bTmp;
                    hasSavedBytes = false;
                    n = n + savedBytes.length;
                }

                CharBuffer charBuffer = decodeHelper(b, n, charset);
                if (charBuffer == null) {
                    int nrOfChars = 0;
                    while (charBuffer == null) {
                        nrOfChars++;
                        charBuffer = decodeHelper(b, n - nrOfChars, charset);
                        if (nrOfChars > 10 && nrOfChars < n) {
                            try {
                                charBuffer = decoder.decode(ByteBuffer.wrap(b, 0, n));
                            } catch (MalformedInputException ex) {
                                throw new IOException("File not in supported encoding (" + charset.displayName() + ")", ex);
                            }
                        }
                    }
                    savedBytes = new byte[nrOfChars];
                    hasSavedBytes = true;
                    for (int i = 0; i < nrOfChars; i++) {
                        savedBytes[i] = b[n - nrOfChars + i];
                    }
                }

                charBuffer.rewind(); // Bring the buffer's pointer to 0
                out.append(charBuffer.toString());
            }
            if (hasSavedBytes) {
//				try {
//					CharBuffer charBuffer = decoder.decode(ByteBuffer.wrap(savedBytes, 0, savedBytes.length));
//				} catch (MalformedInputException ex) {
//					throw new IOException("File not in supported encoding ("+ charset.displayName() + ")", ex);
//				}
                decoder.decode(ByteBuffer.wrap(savedBytes, 0, savedBytes.length));
            }
            return out.toString();
        } catch (Exception e) {
            throw new IOException(e.getMessage());
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    private static CharBuffer decodeHelper(byte[] byteArray, int numberOfBytes, Charset charset) {

        CharsetDecoder decoder = charset.newDecoder();

        try {
            return decoder.decode(ByteBuffer.wrap(byteArray, 0, numberOfBytes));
        } catch (CharacterCodingException e) {
            return null;
        }
    }

}
