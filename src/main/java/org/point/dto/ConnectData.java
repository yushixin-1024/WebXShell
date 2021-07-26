package org.point.dto;

/**
 * Shell会话连接数据
 */
public class ConnectData extends BaseData {

    private static final long serialVersionUID = -7016077789084992830L;

    // 主机
    private String host;

    // 端口
    private Integer port;

    // 用户名
    private String username;

    // 密码
    private String password;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
