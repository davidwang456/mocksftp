package com.example.mocksftp;

import java.io.IOException;
import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.net.Socket;
import java.net.InetSocketAddress;

import com.sshtools.common.files.AbstractFileFactory;
import com.sshtools.common.files.vfs.VFSFileFactory;
import com.sshtools.common.files.vfs.VirtualFileFactory;
import com.sshtools.common.files.vfs.VirtualMountTemplate;
import com.sshtools.common.permissions.PermissionDeniedException;
import com.sshtools.common.policy.FileFactory;
import com.sshtools.common.ssh.SshConnection;
import com.sshtools.server.InMemoryPasswordAuthenticator;
import com.sshtools.server.SshServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 简单的SFTP服务端
 */
public class SimpleServer {
    private static final Logger logger = LoggerFactory.getLogger(SimpleServer.class);
    
    public static void main(String[] args) throws IOException {
        try {
            // 创建tmp目录
            File tmpDir = new File("tmp");
            if (!tmpDir.exists()) {
                if (tmpDir.mkdir()) {
                    logger.info("创建tmp目录成功");
                    System.out.println("创建tmp目录成功");
                } else {
                    logger.error("创建tmp目录失败");
                    System.out.println("创建tmp目录失败");
                    return;
                }
            }
            
            // 创建用户目录
            File adminDir = new File("tmp/admin");
            if (!adminDir.exists()) {
                if (adminDir.mkdir()) {
                    logger.info("创建用户目录成功");
                    System.out.println("创建用户目录成功");
                } else {
                    logger.error("创建用户目录失败");
                    System.out.println("创建用户目录失败");
                    return;
                }
            }
            
            // 创建服务器实例
            final int port = 2222;
            SshServer server = new SshServer(port);
            
            // 添加关闭钩子
            Runtime.getRuntime().addShutdownHook(new Thread() { 
                public void run() { 
                    logger.info("关闭SFTP服务器");
                    System.out.println("关闭SFTP服务器");
                    server.close(); 
                } 
            });
            
            // 添加认证器
            server.addAuthenticator(new InMemoryPasswordAuthenticator()
                    .addUser("admin", "admin".toCharArray()));
            
            // 设置文件工厂
            server.setFileFactory(new FileFactory() {
                public AbstractFileFactory<?> getFileFactory(SshConnection con) 
                        throws IOException, PermissionDeniedException {
                    logger.info("用户 {} 连接", con.getUsername());
                    System.out.println("用户 " + con.getUsername() + " 连接");
                    return new VirtualFileFactory(
                        new VirtualMountTemplate("/", "tmp/" + con.getUsername(), 
                        new VFSFileFactory(), true));
                }
            });
            
            // 允许网关转发
            server.getForwardingPolicy().allowGatewayForwarding();
            
            // 启动服务器
            logger.info("SFTP服务器正在启动，监听端口：{}", port);
            System.out.println("SFTP服务器正在启动，监听端口：" + port);
            server.start();
            
            // 等待服务器启动完成
            Thread.sleep(1000);
            
            // 检查服务器是否启动成功
            boolean isRunning = isPortListening("localhost", port);
            if (isRunning) {
                logger.info("SFTP服务器启动成功");
                System.out.println("SFTP服务器启动成功");
            } else {
                logger.error("SFTP服务器启动失败");
                System.out.println("SFTP服务器启动失败");
            }
            
            // 保持主线程不退出
            CountDownLatch latch = new CountDownLatch(1);
            try {
                latch.await();
            } catch (InterruptedException e) {
                logger.error("服务器被中断", e);
                System.out.println("服务器被中断: " + e.getMessage());
            }
            
        } catch (Exception e) {
            logger.error("启动SFTP服务器时发生错误", e);
            System.out.println("启动SFTP服务器时发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 检查端口是否被监听
     */
    private static boolean isPortListening(String host, int port) {
        Socket socket = null;
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), 1000);
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    // 忽略关闭异常
                }
            }
        }
    }
}