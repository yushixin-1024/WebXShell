package org.point.event;

import org.point.dto.WriteData;
import org.springframework.context.ApplicationEvent;

/**
 * 写入命令行-事件类
 */
public class CmdEvent extends ApplicationEvent {

    private static final long serialVersionUID = -8622759480962446045L;

    private final WriteData source;

    public CmdEvent(WriteData source) {
        super(source);
        this.source = source;
    }

    @Override
    public WriteData getSource() {
        return source;
    }
}
