package org.point.dto;

import java.io.Serializable;

/**
 * 通用事件数据
 */
public class BaseData implements Serializable {

    private static final long serialVersionUID = -8987042483505477551L;

    // 客户端ID
    protected Integer channelId;

    public Integer getChannelId() {
        return channelId;
    }

    public void setChannelId(Integer channelId) {
        this.channelId = channelId;
    }
}
