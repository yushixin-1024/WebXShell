package org.point.dto;

import org.point.enums.CmdType;

/**
 * 读取事件数据
 */
public class ReadData extends BaseData {

    private static final long serialVersionUID = 6962688800957174141L;

    // 循环读取sleep毫秒间隔时长
    private long sleep;

    // 命令类型
    private CmdType type;

    // 命令行内容字节长度(截取字节数组的偏移量)
    private int offset;

    /**
     * 构建对象实例
     * @param channelId 客户端通道ID
     * @param sleep 睡眠间隔时长
     * @param offset 偏移量
     * @return ReadData
     */
    public static ReadData build(Integer channelId, long sleep, CmdType type, int offset) {
        ReadData data = new ReadData();
        data.setChannelId(channelId);
        data.setSleep(sleep);
        data.setType(type);
        data.setOffset(offset);
        return data;
    }

    public long getSleep() {
        return sleep;
    }

    public void setSleep(long sleep) {
        this.sleep = sleep;
    }

    public CmdType getType() {
        return type;
    }

    public void setType(CmdType type) {
        this.type = type;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }
}
