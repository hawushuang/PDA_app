package com.aa.cdemo;

public class JNIUtils {

    private static JNIUtils sInstance = null;

    private JNIUtils() {
    }


    public static synchronized JNIUtils getInstance() {
        if (sInstance == null) {
            sInstance = new JNIUtils();
        }

        return sInstance;
    }

    public native void init(double[][] sg, double low, double high);

    public native void destroy();

    public native double[] getLBGD();

    public native double[] getHBGD();

    public native double getMBG();

    public native double getMValue();

    public native double getSDBG();

    public native double getCV();

    public native double getJIndex();

    public native double getIQR();

    public native double[] getPT();

    public native double getAAC();

    public native double getAUC();

    public native double getLBGI();

    public native double getHBGI();

    public native double getADRR();

    public native double[] getGRADE();

    public native double getLAGE();

    public native double[] getMAGE();

    public native double getMAG();

    public native double getMODD();

    public native double getCONGA();


    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
}
