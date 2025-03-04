package com.example.mocksftp;

import java.io.File;

import com.sshtools.client.SshClient;
import com.sshtools.client.SshClient.SshClientBuilder;
import com.sshtools.client.SshClientContext;
import com.sshtools.client.sftp.SftpClient;
import com.sshtools.client.sftp.SftpClient.SftpClientBuilder;
import com.sshtools.synergy.ssh.Connection;

/**
 * 简单的SFTP客户端
 */
public class SimpleClient {
    public static void main(String[] args) {
        try {
            System.out.println("正在创建SFTP客户端...");
            
            // 创建SSH客户端
            SshClient ssh = SshClientBuilder.create()
                .withHostname("localhost")
                .withPort(2222)
                .withUsername("admin")
                .withPassword("admin")
                .build();
            
        	SftpClient sftp = SftpClientBuilder.create()
        			.withClient(ssh)
        			.build();
            
            System.out.println("正在连接到SFTP服务器...");
            
            // 检查文件是否存在
            File file = new File("D:\\software\\tmp\\POC-C1.zip");
            if (!file.exists()) {
                System.out.println("错误：文件不存在 - " + file.getAbsolutePath());
                return;
            }
            sftp.put("D:\\software\\tmp\\POC-C1.zip");
            
            // 关闭连接
            try {
                ssh.disconnect();
                System.out.println("已断开连接");
            } catch (Exception e) {
                System.out.println("断开连接时发生错误: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.out.println("发生错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}