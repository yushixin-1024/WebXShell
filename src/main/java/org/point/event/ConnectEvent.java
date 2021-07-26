package org.point.event;

import org.point.dto.ConnectData;
import org.springframework.context.ApplicationEvent;

/**
 * SSH连接-事件类
 */
public class ConnectEvent extends ApplicationEvent {

    private static final long serialVersionUID = 5878032528010312779L;

    private final ConnectData source;

    public ConnectEvent(ConnectData source) {
        super(source);
        this.source = source;
    }

    @Override
    public ConnectData getSource() {
        return source;
    }
}
