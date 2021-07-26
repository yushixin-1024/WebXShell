package org.point.dto;

import com.jcraft.jsch.ChannelShell;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

/**
 * Channel通道输入输出流
 */
public class MetaData implements Serializable {

    private static final long serialVersionUID = 1809044470297723901L;

    // 连接通道
    private ChannelShell channel;

    // 输入流,读取数据
    private InputStream is;

    // 输出流,写入数据
    private OutputStream os;

    /**
     * 构建对象实例
     * @param channel 通道
     * @param is 管道输入流
     * @param os 管道输出流
     * @return MetaData
     */
    public static MetaData build(ChannelShell channel, InputStream is, OutputStream os) {
        MetaData data = new MetaData();
        data.setChannel(channel);
        data.setIs(is);
        data.setOs(os);
        return data;
    }

    public ChannelShell getChannel() {
        return channel;
    }

    public void setChannel(ChannelShell channel) {
        this.channel = channel;
    }

    public InputStream getIs() {
        return is;
    }

    public void setIs(InputStream is) {
        this.is = is;
    }

    public OutputStream getOs() {
        return os;
    }

    public void setOs(OutputStream os) {
        this.os = os;
    }
}
