// Basic libusb-like driver intented for PTP devices
// Copyright Daniel Cook - Apache License
package camlib;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;

import dev.danielc.fujiapp.Backend;

public class SimpleUSB {
    public UsbDevice dev = null;
    UsbInterface interf = null;

    public int vendorID = 0;
    public String deviceName;

    UsbEndpoint endpointOut = null;
    UsbEndpoint endpointIn = null;

    int timeout = 1000;
    public int endpoints = 0;

    UsbDeviceConnection connection;
    UsbManager inMan;

    public boolean havePermission() {
        return inMan.hasPermission(dev);
    }

    public void waitPermission(Context ctx) throws Exception {
        Backend.print("试图获得许可...");
        PendingIntent permissionIntent = PendingIntent.getBroadcast(
                ctx, 0, new Intent(ctx.getPackageName() + ".USB_PERMISSION"), PendingIntent.FLAG_IMMUTABLE);

        if (permissionIntent == null) {
            throw new Exception("USB 权限被拒绝！");
        }

        inMan.requestPermission(dev, permissionIntent);
    }

    public void getEndpoints() throws Exception {
        endpointOut = null;
        endpointIn = null;
        endpoints = interf.getEndpointCount();
        for (int e = 0; e < interf.getEndpointCount(); e++) {
            UsbEndpoint ep = interf.getEndpoint(e);
            if (ep.getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) {
                if (ep.getDirection() == UsbConstants.USB_DIR_OUT) {
                    endpointOut = ep;
                    Log.d("usb", "端点输出: " + endpointOut.getAddress());
                    //max = ep.getMaxPacketSize();
                } else {
                    endpointIn = ep;
                    Log.d("usb", "端点输入: " + endpointIn.getAddress());
                }
            }
        }

        if (endpointOut == null || endpointIn == null) {
            throw new Exception("找不到 USB 端点");
        }
    }

    public void getUsbDevices(UsbManager man) throws Exception {
        inMan = man;
        for (UsbDevice d : man.getDeviceList().values()) {
            if (d.getDeviceProtocol() == 0) {
                vendorID = d.getVendorId();
                deviceName = d.getDeviceName();
                dev = d;
                return;
            }
        }

        throw new Exception("找不到 USB 设备");
    }

    // Loop till we hit the first PTP device
    public void getInterface() throws Exception {
        for (int i = 0; i < dev.getInterfaceCount(); i++) {
            interf = dev.getInterface(i);
            if (interf.getInterfaceClass() == 6) {
                return;
            }
        }

        throw new Exception("找不到 USB 接口");
    }

    public void openConnection() throws Exception {
        connection = inMan.openDevice(dev);
        if (connection == null) {
            throw new Exception("无法连接到 USB 设备");
        }
    }

    public int getFileDescriptor() {
        return connection.getFileDescriptor();
    }

    public int write(byte[] data, int length) throws Exception {
        try {
            return connection.bulkTransfer(endpointOut, data, length, timeout);
        } catch (Error e) {
            return -5;
        }
    }

    public int read(byte[] data, int length) throws Exception {
        try {
            return connection.bulkTransfer(endpointIn, data, length, timeout);
        } catch (Error e) {
            return -5;
        }
    }

    public void closeConnection() {
        connection.releaseInterface(interf);
        connection.close();
        endpointOut = null;
        endpointIn = null;
        dev = null;
        interf = null;
        connection = null;
    }

    public int reqMessage(int type, int id, byte data[]) {
        return connection.controlTransfer(type, id, 0, 0, data, data.length, timeout);
    }

    public int clear() {
        byte[] data = new byte[33];
        return connection.controlTransfer(0xa0, 0x67, 0, 0, data, data.length, timeout);
    }

    public int reset() {
        return reqMessage(0x20, 0x66, new byte[0]);
    }
}
