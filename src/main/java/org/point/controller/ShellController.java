package org.point.controller;

import lombok.extern.slf4j.Slf4j;
import org.point.dto.ConnectData;
import org.point.dto.ReadData;
import org.point.dto.WriteData;
import org.point.enums.CmdType;
import org.point.event.CmdEvent;
import org.point.event.ConnectEvent;
import org.point.event.ReadEvent;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Shell-控制层
 */
@Slf4j
@Controller
public class ShellController implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    /**
     * 跳转到首页
     */
    @RequestMapping("/index")
    public String toIndex() {
        return "index";
    }

    /**
     * WebSocket连接成功后,连接SSH服务器
     * 1.客户端发送连接消息
     * @param info 会话连接信息
     */
    @MessageMapping("/connect")
    public void sendConnect(ConnectData info) {
        applicationContext.publishEvent( new ConnectEvent(info) );
    }

    /**
     * 接收客户端命令行消息
     * 1.由客户端发送
     * @param data 写入事件数据
     */
    @MessageMapping("/cmd")
    public void sendCmd(WriteData data) {
        applicationContext.publishEvent( new CmdEvent(data) );
    }

    /**
     * 发送消息到客户端
     * 1.客户端订阅该路径,有数据时读取
     * @param clientId 读取事件数据
     */
    @MessageMapping("/read")
    public void readData(Integer clientId) {
        applicationContext.publishEvent( new ReadEvent(ReadData.build(clientId, 10L, CmdType.Enter, 0)) );
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
