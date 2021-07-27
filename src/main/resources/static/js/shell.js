// 连接端点
const endpoint='/shell/ws';
// 一对一前缀
const user = '/user';
// 向服务端发送消息前缀
const app = '/app';
// Ctrl+L指令
const Ctrl_L = 'clear';
// Ctrl+C指令
const Ctrl_C = '\x03\x03';
// Tab键指令
const Tab = '\x09';
// Delete键指令
const Delete = ' \b';
// BackSpace键指令
const Backspace = '\b \b';
// Enter键指令
const Enter = '\r';

/**
 * 定义客户端
 * @constructor
 */
ShellClient = function () {}

/**
 * 获取WebSocket访问路径
 * 1.只在建立STOMP客户端连接时使用
 * @returns {string}
 */
ShellClient.prototype.getUrl = function () {
    let protocol = window.location.protocol;
    let host = window.location.host;
    return protocol + '//' + host + endpoint;
}

/**
 * 建立STOMP客户端连接
 */
ShellClient.prototype.connect = function (terminal) {
    error(terminal.clientId)
    let url = this.getUrl();
    let sockJS = new SockJS(url);
    // 配置STOMP客户端
    this.connection = Stomp.over(sockJS);
    this.connection.connect({}, function (frame) {
        // 连接成功
        if ( frame.command === 'CONNECTED' ) {
            // 订阅来自SSH服务器的信息
            terminal.client.subscribeSSH(terminal);
            // ssh会话信息
            let info = {
                "host": "10.32.3.29",
                "port": "22",
                "username": "weblogic",
                "password": "weblogic123",
                "clientId": terminal.clientId
            };
            /*let info = {
                "host": "192.168.1.3",
                "port": "22",
                "username": "root",
                "password": "123",
                "clientId": terminal.clientId
            };*/
            // 发送连接数据
            terminal.client.sendConnect(info);
        } else {
            alert('连接失败!');
            error(frame);
        }
    });
}

/**
 * 发送连接数据
 * @param info 连接信息
 */
ShellClient.prototype.sendConnect = function (info) {
    let destination = app + '/connect';
    this.connection.send( destination, {}, JSON.stringify(info) );
}

/**
 * 发送命令行
 * @param type 命令类型
 * @param cmd 命令行
 */
ShellClient.prototype.sendCmd = function (terminal, type, cmd) {
    let data = {
        'clientId': terminal.clientId,
        'type': type,
        'cmd': cmd
    };
    let destination =  app + '/cmd';
    this.connection.send( destination, {}, JSON.stringify(data) );
}

/**
 * 订阅来自SSH服务器的消息
 */
ShellClient.prototype.subscribeSSH = function (terminal) {
    let destination = user + '/' + terminal.clientId + '/read';
    this.connection.subscribe(destination, function (frame) {
        let payload = JSON.parse(frame.body);
        let message = payload.message;
        let length = !message ? 0 : message.length;
        let startIndex = terminal.startIndex;
        if ( !message ) return;
        switch ( payload.type ) {
            case 'Tab':
                // 重新计算cmdLength
                terminal.cmdLength = length;
                // 光标初始化
                terminal._core.buffer.x = startIndex;
                break;
            case 'Arrow':
                // 重新计算cmdLength
                terminal.cmdLength = length;
                // 光标初始化
                terminal._core.buffer.x = startIndex;
                // 偏移量设置
                terminal.offset = length;
                break;
            default:
        }
        // 写入返回数据到缓冲区
        terminal.write(message);
    });
}

/**
 * 关闭STOMP客户端连接
 */
ShellClient.prototype.disconnect = function () {
    // 关闭连接
    this.connection.disconnect(function (frame) {
        console.log(frame);
    });
}

/**
 * 获取当前缓存行索引
 * @param terminal
 * @returns {number}
 */
function getCurrentLineIndex(terminal) {
    // 缓存当前行索引
    let y = terminal._core.buffer.y;
    // 缓冲区基准线(因为rows=46,当缓存到45时就不更新了,获取缓存行时使用y+yBase)
    let yBase = terminal._core.buffer.ybase;
    return y + yBase;
}

/**
 * 获取整行字符串内容
 * @param terminal Terminal
 * @returns {string} 命令行内容
 */
function getLineValue(terminal) {
    // 缓存当前行索引
    let currentLineIndex = getCurrentLineIndex(terminal);
    // 缓冲区最后一行
    let line = terminal.buffer.active.getLine(currentLineIndex);
    // 转为字符串
    return line.translateToString(false, 0, line.length);
}

/**
 * 获取光标开始索引
 * @param terminal Terminal
 * @returns {number} 命令行内容
 */
function getStartIndex(terminal) {
    // 整行内容字符串
    let value = getLineValue(terminal);
    // 固定提示长度:普通用户('$ '左侧长度), root用户('# '左侧长度)
    if ( value.indexOf('[root@') === 0 ) {
        return value.indexOf('#') + 2;
    }
    return value.indexOf('$') + 2;
}

/**
 * 获取命令行
 * @param terminal Terminal
 * @returns {string} 命令行内容
 */
function getCmd(terminal) {
    // 开始索引
    let start = getStartIndex(terminal);
    // 截取命令行偏移量(用于解决ArrowUp/ArrowDown切换命令后指令重复问题)
    let offset = terminal.offset;
    if ( !!offset && offset > 0 ) {
        start += offset;
    }
    let value = getLineValue(terminal);
    // 命令行从 '$ ' 或 '# ' 开始截取到最后
    return value.substr(start, terminal.cmdLength);
}

/**
 * 键盘输入操作
 * @param terminal Terminal
 */
function keyboardInput(terminal) {
    if (terminal._initialized) {
        return;
    }
    terminal._initialized = true;

    /**
     * 方式一
     * 1.每输入一个字符,就会发送到ssh,然后显示ssh返回的字符,所以这种方式支持Tab命令自动补全
     * 2.不支持 Backspace(从最后往前删/从中间往前删), Delete删除输入字符
     * 3.连续点击上下键,提示的历史命令会全部拼接在一行中
     * 4.光标向前移动后无法插入字符,只能覆盖
     */
    /*terminal.onData(key => {
        log('key:['+key+']')
        terminal.client.sendCmd(terminal, 'Other', key);
    });*/

    /**
     * 方式二
     * 1.输入完命令后,一次性发送
     */
    // 键盘事件
    terminal.onKey(e => {
        // 设置光标所在行 TODO 只能读取一行的数据,命令行长度跨行时会出现问题
        let start = getStartIndex(terminal);
        terminal.startIndex = start;
        // 键盘key值
        let key = e.key;
        // 键盘DomKey值
        let domKey = e.domEvent.key;
        // 光标所在位置
        let cursorX = terminal._core.buffer.x;
        // 键盘keyCode对应
        switch ( domKey ) {
            case 'Delete':
                // 最后一个字符再后面的位置
                let end = start + terminal.cmdLength;
                if ( cursorX > end ) {
                    break;
                }
                terminal.write(Delete);
                terminal.cmdLength = terminal.cmdLength > 0 ? --terminal.cmdLength: 0;
                end = start + terminal.cmdLength;
                // 获取整行字符串内容
                let lineValue = getLineValue(terminal);
                // 获取当前光标后面的数据 TODO 仅支持一行
                let moveValue = lineValue.substring(cursorX + 1, end + 1) + ' ';
                // 光标后面内容前移,' '是为了覆盖最后一位遗留字符
                terminal.write(moveValue, function () {
                    // 恢复光标位置
                    terminal._core.buffer.x = cursorX;
                });
                break;
            case 'Backspace':
                if ( cursorX > start ) {
                    if ( cursorX < start + terminal.cmdLength ) {
                        // 最后一个字符再后面的位置
                        let end = start + terminal.cmdLength;
                        // 获取整行字符串内容
                        let lineValue = getLineValue(terminal);
                        // 获取当前光标后面的数据 TODO 仅支持一行
                        let moveValue = lineValue.substring(cursorX, end) + ' ';
                        // 从当前光标前面一个位置覆写
                        terminal._core.buffer.x = --cursorX;
                        terminal.write(moveValue, function () {
                            // 恢复光标位置
                            terminal._core.buffer.x = cursorX;
                        });
                    } else {
                        terminal.write(Backspace);
                    }
                    terminal.cmdLength = terminal.cmdLength > 0 ? --terminal.cmdLength: 0;
                }
                break;
            case 'ArrowLeft':
                // 左键
                if ( cursorX > start ) {
                    terminal._core.buffer.x = --cursorX;
                }
                break;
            case 'ArrowRight':
                // 右键
                if ( cursorX < start + terminal.cmdLength ) {
                    terminal._core.buffer.x = ++cursorX;
                }
                break;
            case 'ArrowUp':case 'ArrowDown':
                // 上下键指令
                terminal.client.sendCmd(terminal, 'Arrow', key);
                break;
            case 'Tab':
                // 触发自动补全
                // TODO 补全后不支持再次使用Alt+Insert/Shift+Insert
                let tabCmd = getCmd(terminal);
                if ( tabCmd && terminal.tabCount === 0 ) {
                    terminal.client.sendCmd(terminal, 'Tab', tabCmd + Tab);
                    // Tab指令次数+1
                    terminal.tabCount++;
                }
                break;
            case 'Enter':
                // Enter键
                let enterCmd = getCmd(terminal);
                if ( terminal.tabCount === 1 ) {
                    enterCmd = '';
                }
                terminal.client.sendCmd(terminal, 'Enter', enterCmd + Enter);
                // 命令行长度置零
                terminal.cmdLength = 0;
                // 偏移量置零
                terminal.offset = 0;
                // Tab指令次数置零
                terminal.tabCount = 0;
                break;
            case 'Escape':case 'Insert':
                // TODO Vim Vi命令支持
                //terminal.client.sendCmd(terminal, 'Other', key);
                break;
            case 'Home':case 'End':case 'PageUp':case 'PageDown':
                break;
            case 'F1':case 'F2':case 'F3':case 'F4':case 'F5':case 'F6':case 'F7':case 'F8':case 'F9':case 'F10':case 'F11':case 'F12':
                break;
            default:
                // 可输入字符
                if ( !e.domEvent.altKey && !e.domEvent.ctrlKey && !e.domEvent.metaKey ) {
                    // 最后一个字符再后面的位置
                    let end = start + terminal.cmdLength;
                    // 获取整行字符串内容
                    let lineValue = getLineValue(terminal);
                    // 获取当前光标后面的数据 TODO 仅支持一行
                    let moveValue = lineValue.substring(cursorX, end);
                    // 当前光标处写入新增字符
                    terminal.write(key);
                    // 从当前光标(包含)到最后的字符串全部后移1位
                    if ( moveValue ) {
                        terminal.write(moveValue, function () {
                            // 光标位置后移1位
                            terminal._core.buffer.x = ++cursorX;
                        });
                    }
                    // 命令行长度+1
                    terminal.cmdLength++;
                }
        }
        // 刷新可视窗口的缓存
        terminal.refresh(0, terminal.rows - 1);
    });
}

/**
 * 打开一个Terminal终端
 */
function openShell () {
    let terminal = new Terminal({
        cols: 205,
        rows: 46,
        // 响铃
        bellStyle: 'sound',
        // 光标闪烁
        cursorBlink: true,
        // 启用时,光标将设置为下一行的开头
        convertEol: true,
        // 是否应禁用输入
        disableStdin: true,
        allowTransparency: true,
        // 回滚
        scrollback: 1024,
        // 制表宽度
        tabStopWidth: 4,
        screenKeys: true,
        screenReaderMode: true,
        // 设置字体
        fontFamily: "consolas",
        // 字体大小
        fontSize: 17,
        // 主题
        theme: {
            // 光标颜色
            cursor: '#00FF00',
            // 选择文本颜色#FFFFFFF0
            selection: '#FFFFFF'
        }
    });
    // Div
    let terminalJQ = $('#terminal');
    // 关联Div
    terminal.open( terminalJQ.get(0) );
    // 偏移量
    terminal.offset = 0;
    // 命令行长度
    terminal.cmdLength = 0;
    // 缓存行中光标开始索引
    terminal.startIndex = 0;
    // Tab指令次数
    terminal.tabCount = 0;
    // 客户端ID
    terminal.clientId = uuid();
    error(terminal.clientId)
    // 终端获取焦点
    terminal.focus();
    // 支持输入法
    let inputing = false;
    terminalJQ.on('compositionstart', function () {
        inputing = true;
    });
    terminalJQ.on('compositionend', function (e) {
        inputing = false;
        let value = e.target.value;
        terminal.write(value);
        terminal.cmdLength = value.length;
        terminal.offset = 0;
    });
    // 组合快捷键功能
    terminal.attachCustomKeyEventHandler(function (e) {
        let key = e.key;
        let shiftKey = e.shiftKey;
        let ctrlKey = e.ctrlKey;
        let altKey = e.altKey;
        // 仅处理keydown事件
        if ( e.type !== 'keydown' ) return;
        // 禁用事件默认行为
        if ( shiftKey || ctrlKey || altKey ) {
            e.preventDefault();
        }
        // Shift组合键
        if (shiftKey) {
            switch ( key ) {
                case 'Insert':
                    // Shift+Insert,粘贴数据
                    // 获取剪切板数据
                    navigator.clipboard.readText().then(data => {
                        if ( !data ) return;
                        terminal.write(data);
                        terminal.cmdLength += data.length;
                        terminal.offset = 0;
                    }).catch(data => {
                        alert('获取剪切板数据失败');
                        error(data);
                    });
                    return;
                default:
            }
        }
        // Alt组合键
        if (altKey) {
            switch ( key ) {
                case 'Insert':
                    // Alt+Insert,粘贴选中数据
                    let selection = terminal.getSelection();
                    if( selection ) {
                        terminal.write(selection);
                        terminal.cmdLength += selection.length;
                        terminal.offset = 0;
                    }
                    return;
                default:
            }
        }
        // Ctrl组合键
        if (ctrlKey) {
            switch ( key ) {
                case 'c':
                    // Ctrl+C,触发中断指令
                    terminal.client.sendCmd(terminal, 'Ctrl_C', Ctrl_C);
                    return;
                case 'l':
                    // Ctrl+L,触发clear清屏指令
                    terminal.client.sendCmd(terminal, 'Ctrl_L', Ctrl_L + Enter);
                    return;
                case 'Insert':
                    // Ctrl+Insert,触发复制到剪切板
                    let selection = terminal.getSelection();
                    if( selection ) {
                        navigator.clipboard.writeText(selection).catch(data => {
                            alert('写入剪切板数据失败');
                            error(data);
                        });
                    }
                    return;
                default:
            }
        }
        // Ctrl+Shift组合键
        if ( ctrlKey && shiftKey ) {
            switch ( key ) {
                case 'B':case 'b':
                    // Ctrl+Shift+B,触发滚动缓冲区清除
                    terminal.clear();
                    return;
                default:
            }
        }
    });
    // 创建客户端
    terminal.client = new ShellClient();
    // 打开WebSocket连接
    terminal.client.connect(terminal);
    // 键盘输入
    keyboardInput(terminal);
}

/**
 * 生成UUID
 */
function uuid() {
    function gen() {
        return (((1 + Math.random()) * 0x10000) | 0).toString(16).substring(1);
    }
    return (gen() + '-' + gen() + '-' + gen() + '-' + gen() + '-' + gen());
}

/**
 * 控制台调试日志
 * @param data 日志内容
 */
function log(data) {
    console.log(data);
}

/**
 * 控制台错误日志
 * @param data 日志内容
 */
function error(data) {
    console.error(data);
}