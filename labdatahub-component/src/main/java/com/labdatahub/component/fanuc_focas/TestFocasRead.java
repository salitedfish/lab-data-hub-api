//由AI修改
package com.labdatahub.component.fanuc_focas;

/**
 * 独立测试：验证 FanucFocas2TcpClient 真机读系统参数 6711/6712/6713
 * 编译运行：javac -encoding UTF-8 -cp <target/classes> TestFocasRead.java
 *           java -cp <target/classes>;. com.labdatahub.component.fanuc_focas.TestFocasRead
 */
public class TestFocasRead {
    public static void main(String[] args) throws Exception {
        String ip = args.length > 0 ? args[0] : "192.168.1.2";
        Integer port = args.length > 1 ? Integer.parseInt(args[1]) : 8193;
        int[] params = {6711, 6712, 6713};
        for (int p : params) {
            try {
                Long v = FanucFocas2TcpClient.readParam(ip, port, p);
                System.out.println("param " + p + " = " + v);
            } catch (Exception e) {
                System.out.println("param " + p + " 异常: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }
        }
        System.out.println("DONE");
    }
}
