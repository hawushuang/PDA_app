package com.microtechmd.pda_app.service.service_control.platform;


import com.microtechmd.pda.library.entity.EntityMessage;

public class JNIInterface
{
	private static JNIInterface sInstance = null;
	private static Callback sCallback = null;


	public interface Callback
	{
		int onHandleEvent(int address, int sourcePort, int targetPort,
                          int event);


		int onHandleCommand(int address, int sourcePort, int targetPort,
                            int mode, int operation, int parameter, byte[] data);


		int writeDevice(int address, byte[] data);
	}


	private JNIInterface()
	{
	}


	public static synchronized JNIInterface getInstance()
	{
		if (sInstance == null)
		{
			sInstance = new JNIInterface();
		}

		return sInstance;
	}


	public static void setCallback(Callback callback)
	{
		sCallback = callback;
	}


	private static int handleEvent(int address, int sourcePort, int targetPort,
		int event)
	{
		if (sCallback == null)
		{
			return EntityMessage.FUNCTION_FAIL;
		}

		return sCallback.onHandleEvent(address, sourcePort, targetPort, event);
	}


	private static int handleCommand(int address, int sourcePort,
		int targetPort, int mode, int operation, int parameter, byte[] data)
	{
		if (sCallback == null)
		{
			return EntityMessage.FUNCTION_FAIL;
		}

		return sCallback.onHandleCommand(address, sourcePort, targetPort, mode,
			operation, parameter, data);
	}


	private static int writeDevice(int address, byte[] data)
	{
		if (sCallback == null)
		{
			return EntityMessage.FUNCTION_FAIL;
		}

		return sCallback.writeDevice(address, data);
	}


	public native int send(int address, int sourcePort, int targetPort, int mode,
		int operation, int parameter, byte[] data);


	public native int receive(int address, byte[] data);


	public native int query(int address);


	public native int switchFrame(int address, int value);


	public native int switchLink(int address, int value);

	public native int turnOff();

	public native int ready(byte[] data);

	public native float test(float[][] a);
	static
	{
		System.loadLibrary("jni_interface");
	}
}
