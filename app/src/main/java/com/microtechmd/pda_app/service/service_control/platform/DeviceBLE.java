package com.microtechmd.pda_app.service.service_control.platform;


import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.CountDownTimer;
import android.util.Log;

import com.microtechmd.pda.library.entity.EntityMessage;
import com.microtechmd.pda.library.entity.ParameterComm;
import com.microtechmd.pda.library.parameter.ParameterGlobal;
import com.microtechmd.pda_app.service.service_control.service.ServiceBase;
import com.microtechmd.pda.library.utility.LogPDA;
import com.microtechmd.pda.library.utility.SPUtils;
import com.microtechmd.pda_app.entity.Broadcast;
import com.vise.baseble.ViseBle;
import com.vise.baseble.callback.IBleCallback;
import com.vise.baseble.callback.IConnectCallback;
import com.vise.baseble.callback.scan.IScanCallback;
import com.vise.baseble.callback.scan.ScanCallback;
import com.vise.baseble.common.PropertyType;
import com.vise.baseble.core.BluetoothGattChannel;
import com.vise.baseble.core.DeviceMirror;
import com.vise.baseble.core.DeviceMirrorPool;
import com.vise.baseble.exception.BleException;
import com.vise.baseble.model.BluetoothLeDevice;
import com.vise.baseble.model.BluetoothLeDeviceStore;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.UUID;

import static com.microtechmd.pda_app.ActivityPDA.SETTING_RF_ADDRESS;
import static com.vise.baseble.common.BleExceptionCode.GATT_ERR;


public final class DeviceBLE {
    private static final UUID UUID_SERVICE =
            UUID.fromString("0000F000-0000-1000-8000-00805F9B34FB");
    private static final UUID UUID_CHARACTERISTIC =
            UUID.fromString("0000F001-0000-1000-8000-00805F9B34FB");

    private static final long scanSpace = 20 * 60 * 1000;
    private ServiceBase mService;
    private static DeviceBLE sInstance = null;
    private static EntityMessage.Listener sMessageListener = null;
    private BluetoothLeDeviceStore bluetoothLeDeviceStore = new BluetoothLeDeviceStore();
    private BluetoothLeDevice mBluetoothLeDevice;
    private DeviceMirrorPool mDeviceMirrorPool;

    private byte[] receviceDataBuffer = new byte[76];
    private int pointer = 0;
    private int mIndex;

    private BleCallback mCallback = null;
    private LogPDA mLog;

    private CountDownTimer scanTimer;

    public interface BleCallback {
        void onStateChange(boolean connectFlag);

        void onDisconnect();
    }

    public void setCallback(BleCallback callback) {
        mCallback = callback;
    }

    /**
     * 扫描回调
     */
    private ScanCallback scanCallback = new ScanCallback(new IScanCallback() {
        @Override
        public void onDeviceFound(final BluetoothLeDevice bluetoothLeDevice) {
            sMessageListener.onReceive(new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_CONTROL,
                    ParameterGlobal.ADDRESS_LOCAL_VIEW, ParameterGlobal.PORT_COMM,
                    ParameterGlobal.PORT_COMM, EntityMessage.OPERATION_NOTIFY,
                    ParameterComm.BLE_SCAN,
                    null));
            bluetoothLeDeviceStore.addDevice(bluetoothLeDevice);
            byte[] addressByte = SPUtils.getbytes(mService, SETTING_RF_ADDRESS);
            if (bluetoothLeDevice.getScanRecord().length > 6 && addressByte != null) {
                byte[] address = Arrays.copyOfRange(bluetoothLeDevice.getScanRecord(),
                        bluetoothLeDevice.getScanRecord().length - 6, bluetoothLeDevice.getScanRecord().length);
                if (Arrays.equals(addressByte, address)) {
                    Log.e("设置序列号", Arrays.toString(addressByte));
                    byte[] broadcastData = Arrays.copyOfRange(bluetoothLeDevice.getScanRecord(),
                            11, bluetoothLeDevice.getScanRecord().length);
                    Broadcast broadcast = new Broadcast();
                    broadcast.setByteArray(broadcastData);
                    int signal2 = broadcast.getRFSignal();
                    int signal = bluetoothLeDevice.getRssi() + 130;
                    mBluetoothLeDevice = bluetoothLeDevice;
                    sMessageListener.onReceive(new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_CONTROL,
                            ParameterGlobal.ADDRESS_LOCAL_VIEW, ParameterGlobal.PORT_COMM,
                            ParameterGlobal.PORT_COMM, EntityMessage.OPERATION_NOTIFY,
                            ParameterComm.PARAM_RF_SIGNAL,
                            new byte[]{(byte) signal}));
                    sMessageListener.onReceive(new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_CONTROL,
                            ParameterGlobal.ADDRESS_LOCAL_CONTROL, ParameterGlobal.PORT_COMM,
                            ParameterGlobal.PORT_COMM, EntityMessage.OPERATION_NOTIFY,
                            ParameterComm.PARAM_BROADCAST_DATA,
                            broadcast.getData()));
                }
            }

        }

        @Override
        public void onScanFinish(BluetoothLeDeviceStore bluetoothLeDeviceStore) {
        }

        @Override
        public void onScanTimeout() {
        }
    });

    /**
     * 连接回调
     */
    private IConnectCallback connectCallback = new IConnectCallback() {

        @Override
        public void onConnectSuccess(DeviceMirror deviceMirror) {
            BluetoothGattService service = deviceMirror.getBluetoothGatt().getService(UUID_SERVICE);
            if (service == null) {
                mCallback.onStateChange(false);
                return;
            }
            BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID_CHARACTERISTIC);
            if (characteristic == null) {
                mCallback.onStateChange(false);
                return;
            }
            int charaProp = characteristic.getProperties();
            if ((charaProp & BluetoothGattCharacteristic.PROPERTY_WRITE) > 0) {
                bindChannel(deviceMirror, PropertyType.PROPERTY_WRITE, service.getUuid(), characteristic.getUuid());
            } else if ((charaProp & BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                bindChannel(deviceMirror, PropertyType.PROPERTY_READ, service.getUuid(), characteristic.getUuid());
                deviceMirror.readData();
            }
            if ((charaProp & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                bindChannel(deviceMirror, PropertyType.PROPERTY_NOTIFY, service.getUuid(), characteristic.getUuid());
                deviceMirror.registerNotify(false);
            }
//            else if ((charaProp & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0) {
//                bindChannel(deviceMirror, PropertyType.PROPERTY_INDICATE, service.getUuid(), characteristic.getUuid());
//                deviceMirror.registerNotify(true);
//            }
//            if (message != null) {
//                send(message);
//            }
            mCallback.onStateChange(true);
            receviceDataBuffer = new byte[76];
            pointer = 0;
        }

        @Override
        public void onConnectFailure(BleException exception) {
//            sMessageListener.onReceive(new EntityMessage(ParameterGlobal.ADDRESS_REMOTE_SLAVE,
//                    ParameterGlobal.ADDRESS_LOCAL_VIEW, ParameterGlobal.PORT_COMM,
//                    ParameterGlobal.PORT_COMM, EntityMessage.OPERATION_ACKNOWLEDGE,
//                    ParameterComm.PARAM_RF_LOCAL_ADDRESS, new byte[]{EntityMessage.FUNCTION_FAIL}
//            ));
            mCallback.onStateChange(false);
            disconnect();
            Log.e("连接蓝牙失败", exception.toString());
            if (exception.getCode() == GATT_ERR) {

            }

        }

        @Override
        public void onDisconnect(boolean isActive) {
//            mCallback.onStateChange(false);
            Log.e("蓝牙断开连接", "" + isActive);
            mCallback.onDisconnect();
        }
    };

    /**
     * 接收数据回调
     */
    private IBleCallback receiveCallback = new IBleCallback() {
        @Override
        public void onSuccess(final byte[] data, BluetoothGattChannel bluetoothGattInfo, BluetoothLeDevice bluetoothLeDevice) {
            if (data != null) {

                int index = (data[data.length - 1] & 0xf0) >> 4;
                int last = data[data.length - 1] & 0x0f;

                Log.e("aaa", "返回数据 " + Arrays.toString(data) + "index：" + index + "   last:" + last);
//                int last = data[data.length - 1];
                byte[] add = new byte[data.length - 1];
                System.arraycopy(data, 0, add, 0, add.length);
                if (index != mIndex || (pointer + add.length) > 76) {
                    mIndex = index;
                    receviceDataBuffer = new byte[76];
                    pointer = 0;
                }
                if (last != 0) {
                    System.arraycopy(add, 0, receviceDataBuffer, pointer, add.length);
                    pointer += add.length;
                    byte[] receiveData = new byte[pointer];
                    System.arraycopy(receviceDataBuffer, 0, receiveData, 0, receiveData.length);
                    receviceDataBuffer = new byte[76];
                    pointer = 0;

                    JNIInterface.getInstance().receive(ParameterGlobal.ADDRESS_REMOTE_MASTER, receiveData);
                } else {
//                    Log.e("aaa", "返回数据拼接: " + (pointer + add.length));
                    System.arraycopy(add, 0, receviceDataBuffer, pointer, add.length);
                    pointer += add.length;
                }
            }
        }

        @Override
        public void onFailure(BleException exception) {
        }
    };
    /**
     * 操作数据回调
     */
    private IBleCallback bleCallback = new IBleCallback() {
        @Override
        public void onSuccess(final byte[] data, BluetoothGattChannel bluetoothGattInfo, BluetoothLeDevice bluetoothLeDevice) {
            if (data == null) {
                return;
            }
            DeviceMirror deviceMirror = mDeviceMirrorPool.getDeviceMirror(bluetoothLeDevice);
            if (deviceMirror != null) {
                deviceMirror.setNotifyListener(bluetoothGattInfo.getGattInfoKey(), receiveCallback);
            }
        }

        @Override
        public void onFailure(BleException exception) {
        }
    };

    private class JNICallback
            implements JNIInterface.Callback {
        public int onHandleEvent(int address, int sourcePort, int targetPort,
                                 int event) {
            if (sMessageListener == null) {
                return EntityMessage.FUNCTION_FAIL;
            }
            mLog.Error(getClass(), "jniHandleEvent: " + "address:" + address +
                    "sourcePort:" + sourcePort + "targetPort:" + targetPort +
                    "event:" + event);
            sMessageListener.onReceive(new EntityMessage(address, address,
                    sourcePort, targetPort, event));

            return EntityMessage.FUNCTION_OK;
        }


        public int onHandleCommand(int address, int sourcePort, int targetPort,
                                   int mode, int operation, int parameter, byte[] data) {
            if (sMessageListener == null) {
                return EntityMessage.FUNCTION_FAIL;
            }
            mLog.Error(getClass(), "jniHandleCommand: " + "address:" + address +
                    "sourcePort:" + sourcePort + "targetPort:" + targetPort +
                    "mode:" + mode + "operation: " + operation + "parameter: " + parameter +
                    "data:" + Arrays.toString(data));
            sMessageListener.onReceive(new EntityMessage(address, address,
                    sourcePort, targetPort, mode, operation, parameter, data));

            return EntityMessage.FUNCTION_OK;
        }

        @Override
        public int writeDevice(int address, byte[] data) {
            byte[] writeData = new byte[data.length + 1];
            System.arraycopy(data, 0, writeData, 0, data.length);
            writeData[writeData.length - 1] = 1;
            write(writeData);
            mLog.Error(getClass(), "writeDevice: " + Arrays.toString(writeData) + "地址：" + address);
            return EntityMessage.FUNCTION_OK;
        }
    }


    private DeviceBLE(ServiceBase service) {
        mService = service;
        mDeviceMirrorPool = ViseBle.getInstance().getDeviceMirrorPool();
        mLog = new LogPDA();
    }


    public static synchronized DeviceBLE getInstance(
            ServiceBase service) {
        sMessageListener = service;

        if (sInstance == null) {
            sInstance = new DeviceBLE(service);
            JNIInterface.setCallback(sInstance.new JNICallback());
        }

        return sInstance;
    }

    public void startScan() {
        ViseBle.getInstance().startScan(scanCallback);
        if (scanTimer == null) {
            scanTimer = new CountDownTimer(scanSpace, scanSpace) {
                @Override
                public void onTick(long l) {

                }

                @Override
                public void onFinish() {
                    byte[] addressByte = SPUtils.getbytes(mService, SETTING_RF_ADDRESS);
                    if (addressByte != null) {
                        ViseBle.getInstance().startScan(scanCallback);
                        mLog.Error(DeviceBLE.this.getClass(), "30分钟后重新启动扫描");
                    }
                }
            }.start();
        } else {
            scanTimer.cancel();
            scanTimer.start();
        }
    }

    public void stopScan() {
        ViseBle.getInstance().stopScan(scanCallback);
        sMessageListener.onReceive(new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_CONTROL,
                ParameterGlobal.ADDRESS_LOCAL_VIEW, ParameterGlobal.PORT_COMM,
                ParameterGlobal.PORT_COMM, EntityMessage.OPERATION_NOTIFY,
                ParameterComm.BLE_NORMAL,
                null));
    }

    public void connect() {
        if (mBluetoothLeDevice != null) {
            stopScan();
            ViseBle.getInstance().connect(mBluetoothLeDevice, connectCallback);
            sMessageListener.onReceive(new EntityMessage(ParameterGlobal.ADDRESS_LOCAL_CONTROL,
                    ParameterGlobal.ADDRESS_LOCAL_VIEW, ParameterGlobal.PORT_COMM,
                    ParameterGlobal.PORT_COMM, EntityMessage.OPERATION_NOTIFY,
                    ParameterComm.BLE_CONNECTING,
                    null));
        } else {
            mCallback.onStateChange(false);
//            mLog.Error(getClass(), "连接蓝牙失败");
        }
    }

    public void disconnect() {
        ViseBle.getInstance().disconnect(mBluetoothLeDevice);
    }

    public boolean isConnected() {
        return ViseBle.getInstance().isConnect(mBluetoothLeDevice);
    }

    private void write(byte[] data) {
        DeviceMirror mDeviceMirror = mDeviceMirrorPool.getDeviceMirror(mBluetoothLeDevice);
        if (mDeviceMirror != null) {
            mDeviceMirror.writeData(data);
        }
    }

    private void bindChannel(DeviceMirror deviceMirror, PropertyType propertyType, UUID serviceUUID,
                             UUID characteristicUUID) {
        if (deviceMirror != null) {
            BluetoothGattChannel bluetoothGattChannel = new BluetoothGattChannel.Builder()
                    .setBluetoothGatt(deviceMirror.getBluetoothGatt())
                    .setPropertyType(propertyType)
                    .setServiceUUID(serviceUUID)
                    .setCharacteristicUUID(characteristicUUID)
                    .setDescriptorUUID(null)
                    .builder();
            deviceMirror.bindChannel(bleCallback, bluetoothGattChannel);
        }
    }

    public int send(final EntityMessage message) {
        if ((message.getTargetAddress() != ParameterGlobal.ADDRESS_REMOTE_MASTER)) {
            return EntityMessage.FUNCTION_FAIL;
        }

        return JNIInterface.getInstance().send(message.getTargetAddress(),
                message.getSourcePort(), message.getTargetPort(), message.getMode(),
                message.getOperation(), message.getParameter(), message.getData());
    }


    public int query(int address) {
        return JNIInterface.getInstance().query(address);
    }


    public int switchLink(int address, int value) {
        return JNIInterface.getInstance().switchLink(address, value);
    }

    public void turnOff() {
        JNIInterface.getInstance().turnOff();
    }

    public void ready(byte[] data) {
        JNIInterface.getInstance().ready(data);
    }
}
