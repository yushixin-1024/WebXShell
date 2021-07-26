import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Main {

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("10.32.3.29", 22);
        OutputStream os = socket.getOutputStream();
        os.write("pwd".getBytes());
        os.flush();
        InputStream is = socket.getInputStream();
        byte[] bytes = new byte[is.available()];
        is.read(bytes);
        /*
            Connected to the target VM, address: '127.0.0.1:64943', transport: 'socket'
            SSH-2.0-OpenSSH_6.6.1

            Disconnected from the target VM, address: '127.0.0.1:64943', transport: 'socket'
         */
        System.out.println(new String(bytes));
    }
}
