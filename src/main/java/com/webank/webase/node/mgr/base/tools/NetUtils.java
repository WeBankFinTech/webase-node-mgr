package com.webank.webase.node.mgr.base.tools;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.apache.commons.lang3.ArrayUtils;

import lombok.extern.slf4j.Slf4j;

/**
 *
 */

@Slf4j
public class NetUtils {

    public static boolean checkAddress(String ip, int port, int timeout) {
        int newTimeout = timeout > 0 ? timeout : 2000;

        try {
            try (Socket crunchifySocket = new Socket()) {
                // Connects this socket to the server with a specified timeout value.
                crunchifySocket.connect(new InetSocketAddress(ip, port), newTimeout);
            }
            // Return true if connection successful
            return true;
        } catch (IOException e) {
            log.error("Connect to host:[{}] and port:[{}] with timeout:[{}] is error.", ip, port, newTimeout, e);
        }
        return false;
    }

    /**
     *  Check ip:port accessible.
     * @param address format ip:port, like 129.204.174.191:6004;
     * @param timeout
     * @return
     */
    public static boolean checkAddress(String address, int timeout) {
        String[] ipPortArray = address.split(":");
        if (ArrayUtils.getLength(ipPortArray) != 2){
            log.error("Address:[{}] format error, should be [ip:port], like 129.204.174.191:6004.", address);
            return false;
        }
        try {
            int port = Integer.parseInt(ipPortArray[1]);
            return checkAddress(ipPortArray[0], port, timeout);
        } catch (Exception e) {
            log.error("Address:[{}] port error, should be [ip:port], like 129.204.174.191:6004.", address);
        }
        return false;
    }

}