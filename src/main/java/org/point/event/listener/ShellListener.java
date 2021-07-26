package org.point.event.listener;

import cn.hutool.core.util.StrUtil;
import com.jcraft.jsch.ChannelShell;
import lombok.extern.slf4j.Slf4j;
import org.point.dto.ConnectData;
import org.point.dto.MetaData;
import org.point.dto.ReadData;
import org.point.dto.WriteData;
import org.point.enums.CmdType;
import org.point.event.ConnectEvent;
import org.point.event.ReadEvent;
import org.point.event.CmdEvent;
import org.point.util.ShellUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 事件监听类
 */
@Slf4j
@Component
public class ShellListener implements Serializable {

    private static final long serialVersionUID = 692647322397928182L;

    // 元信息集合
    private static final Map<Integer, MetaData> map = new ConcurrentHashMap<>(64);
    // 向用户发送消息模板类
    @Autowired
    private SimpMessagingTemplate template;

    /**
     * 连接事件
     * 1.客户端打开SSH会话
     * @param event 连接事件对象
     */
    @EventListener(classes = ConnectEvent.class)
    public void connect(ConnectEvent event) {
        ConnectData connectData = event.getSource();
        ChannelShell channel = ShellUtil.getChannel(connectData);
        Integer channelId = connectData.getChannelId();
        try {
            InputStream is = channel.getInputStream();
            OutputStream os = channel.getOutputStream();
            map.put(channelId, MetaData.build(channel, is, os) );
            // 连接后读取,同步阻塞读取
            readData( new ReadEvent(ReadData.build(channelId, 150L, CmdType.Connect, 0)) );
        } catch (IOException e) {
            log.error("获取通道流数据失败", e);
        }
    }

    /**
     * 写入数据事件
     * 1.客户端发送命令行
     * @param event 写入数据事件对象
     */
    @EventListener(classes = CmdEvent.class)
    public void writeData(CmdEvent event) {
        WriteData writeData = event.getSource();
        Integer channelId = writeData.getChannelId();
        MetaData metaData = map.get(channelId);
        try {
            CmdType type = writeData.getType();
            String cmd = writeData.getCmd();
            byte[] bytes = cmd.getBytes();
            // 管道输出流
            OutputStream os = metaData.getOs();
            os.write(bytes);
            os.flush();
            log.info("内容:[{}], 写入[{}]字节", cmd, bytes.length);
            // 写入后读取,同步阻塞读取
            readData( new ReadEvent(ReadData.build(channelId, 10L, type, bytes.length)) );
        } catch (IOException e) {
            log.error("写入数据失败", e);
        }
    }

    /**
     * 读取数据事件
     * 1.ssh响应数据
     * @param event 读取数据事件对象
     */
    @EventListener(classes = ReadEvent.class)
    public void readData(ReadEvent event) {
        ReadData readData = event.getSource();
        Integer channelId = readData.getChannelId();
        long sleep = readData.getSleep();
        CmdType type = readData.getType();
        int offset = readData.getOffset();
        MetaData metaData = map.get(channelId);
        try {
            // 管道输入流
            InputStream is = metaData.getIs();
            // SSH服务器返回数据需要时间,循环读取
            while ( true ) {
                // 获取所有可读数据
                int available = is.available();
                byte[] src = new byte[available];
                if ( available > 0 ) {
                    is.read(src);
                    // 删除输入流中的命令行
                    byte[] dest = ShellUtil.getDescBytes(src, type, offset);
                    String message = new String(dest);
                    log.info("内容:[{}],读取[{}]字节", message, dest.length);
                    if ( StrUtil.isEmpty(message) ) {
                        break;
                    }
                    Map<String, Object> payload = new HashMap<>();
                    payload.put("type", type.name());
                    payload.put("message", message);
                    template.convertAndSendToUser( channelId.toString(), "/read", payload );
                    break;
                }
                // 阻塞等待指定间隔时长
                Thread.sleep(sleep);
                log.info("while循环 --> 等待读取数据...");
            }
        } catch (IOException | InterruptedException e) {
            log.error("读取数据失败", e);
        }
    }
}
