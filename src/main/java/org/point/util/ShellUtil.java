package org.point.util;

import cn.hutool.extra.ssh.JschUtil;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.Session;
import lombok.extern.slf4j.Slf4j;
import org.point.dto.ConnectData;
import org.point.enums.CmdType;

import java.util.ArrayList;
import java.util.List;

/**
 * Shell工具类
 */
@Slf4j
public class ShellUtil {

    /**
     * 替换byte[]中的特殊字符
     * \u0000 初始化字符
     * \u0008 '\b' (由Backspace/Delete指令产生)
     * @param src 原byte[]
     * @return byte[]
     */
    public static byte[] replaceSpecial(byte[] src) {
        List<Byte> list = new ArrayList<>();
        for (byte b : src) {
            if (b != 0 && b!= 8) {
                list.add(b);
            }
        }
        byte[] dest = new byte[ list.size() ];
        for (int i = 0; i < list.size(); i++) {
            dest[i] = list.get(i);
        }
        return dest;
    }

    /**
     * 获取byte[]
     * 1.偏移量为0时,返回源byte[]
     * 2.偏移量>0时,返回新byte[],从offset开始的len个字节
     * @param src 源byte[]
     * @param type 命令类型
     * @param length 命令行字节长度
     * @return byte[]
     */
    public static byte[] getDescBytes(byte[] src, CmdType type, int length) {
        byte[] bytes;
        int available = src.length;
        int offset = getOffset(type, length);
        if ( offset == 0 ) {
            bytes = src;
        } else {
            int len = available - offset;
            byte[] desc = new byte[len];
            System.arraycopy(src, offset, desc, 0, len);
            bytes = desc;
        }
        return replaceSpecial(bytes);
    }

    /**
     * 获取偏移量
     * 1.如果是Enter命令,返回字节长度-1
     * 2.否则返回0
     * @param type 命令类型
     * @param length 命令行字节长度
     * @return 偏移量
     */
    public static int getOffset(CmdType type, int length) {
        if ( CmdType.Enter.equals(type) ) {
            // 因为ssh返回数据时,会将原命令返回,导致页面展示会出现命令重复显示的问题,-1是去掉\r的1个字节长度,(length - 1)是字节数组的偏移量
            return length - 1;
        }
        return 0;
    }

    /**
     * 获取ChannelShell
     * @param info 会话连接信息
     * @return ChannelShell
     */
    public static ChannelShell getChannel(ConnectData info) {
        Session session = getSession(info);
        return JschUtil.openShell(session);
    }

    /**
     * 获取Session
     * @param info 会话连接信息
     * @return Session
     */
    public static Session getSession(ConnectData info) {
        return JschUtil.getSession(info.getHost(), info.getPort(), info.getUsername(), info.getPassword());
    }

    private ShellUtil() {}
}
