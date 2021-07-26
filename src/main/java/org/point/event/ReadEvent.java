package org.point.event;

import org.point.dto.ReadData;
import org.springframework.context.ApplicationEvent;

/**
 * 读取数据-事件类
 */
public class ReadEvent extends ApplicationEvent {

    private static final long serialVersionUID = 6036847432049363508L;

    private final ReadData source;

    public ReadEvent(ReadData source) {
        super(source);
        this.source = source;
    }

    @Override
    public ReadData getSource() {
        return source;
    }
}
