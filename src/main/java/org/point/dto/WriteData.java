package org.point.dto;

import org.point.enums.CmdType;

/**
 * 写入事件数据
 */
public class WriteData extends BaseData {

    private static final long serialVersionUID = 375624924502494377L;

    // 命令类型
    private CmdType type;

    // 客户端发送消息内容
    private String cmd;

    public CmdType getType() {
        return type;
    }

    public void setType(CmdType type) {
        this.type = type;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }
}
