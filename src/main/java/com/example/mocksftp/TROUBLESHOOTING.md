# SFTP服务器故障排除指南

本文档提供了使用基于Netty的SFTP服务器时可能遇到的常见问题及其解决方案。

## 目录

1. [连接错误](#连接错误)
2. [认证问题](#认证问题)
3. [协议错误](#协议错误)
4. [文件传输问题](#文件传输问题)
5. [服务器配置问题](#服务器配置问题)

## 连接错误

### 连接被拒绝

**症状**: `Connection refused`

**可能原因**:
- 服务器未启动
- 端口号错误
- 防火墙阻止连接

**解决方案**:
- 确认服务器正在运行
- 验证端口号（默认2222）
- 检查防火墙设置

### 连接超时

**症状**: `Connection timed out`

**可能原因**:
- 网络问题
- 服务器地址错误

**解决方案**:
- 检查网络连接
- 确认服务器地址正确

## 认证问题

### 认证失败

**症状**: `Authentication failed`

**可能原因**:
- 用户名或密码错误
- 认证方法不支持

**解决方案**:
- 使用正确的凭据（admin/admin）
- 确保使用密码认证

## 协议错误

### Bad packet length

**症状**: `Bad packet length 1397966943` 或类似错误

**可能原因**:
- SSH/SFTP协议实现不完整
- 客户端和服务器之间的协议版本不兼容
- 数据包格式错误

**解决方案**:
1. **使用SimpleServer替代NettyServer**:
   ```bash
   java -cp target/mocksftp-1.0-SNAPSHOT-jar-with-dependencies.jar com.example.mocksftp.SimpleServer
   ```
   SimpleServer使用Maverick Synergy库的标准实现，可能更稳定。

2. **修改NettyServer实现**:
   - 确保正确实现SSH协议握手
   - 确保正确处理密钥交换
   - 确保正确实现消息格式

3. **使用兼容的SFTP客户端**:
   - 尝试不同版本的SFTP客户端
   - 尝试使用FileZilla等图形化SFTP客户端

### Message authentication code incorrect

**症状**: `message authentication code incorrect`

**可能原因**:
- MAC（消息认证码）计算错误
- 加密密钥不匹配
- 数据包被篡改或损坏

**解决方案**:
1. **检查服务器实现**:
   - 确保正确实现SSH加密和MAC算法
   - 确保密钥交换过程正确

2. **使用调试模式**:
   ```bash
   sftp -vvv -P 2222 admin@localhost
   ```
   查看详细的调试信息，了解错误发生的具体阶段。

3. **尝试不同的加密算法**:
   ```bash
   sftp -o MACs=hmac-sha1 -P 2222 admin@localhost
   ```
   或
   ```bash
   sftp -o Ciphers=aes128-ctr -P 2222 admin@localhost
   ```

## 文件传输问题

### 权限被拒绝

**症状**: `Permission denied`

**可能原因**:
- 文件权限不足
- 目录权限不足

**解决方案**:
- 检查服务器上的文件和目录权限
- 确保tmp目录和用户目录存在且可写

### 文件不存在

**症状**: `No such file or directory`

**可能原因**:
- 文件路径错误
- 文件已被删除

**解决方案**:
- 确认文件路径和名称正确
- 使用`ls`命令检查文件是否存在

## 服务器配置问题

### 端口冲突

**症状**: 服务器启动失败，提示端口已被使用

**可能原因**:
- 另一个程序正在使用相同的端口

**解决方案**:
- 修改服务器代码，使用不同的端口
- 关闭使用该端口的其他程序

### 内存不足

**症状**: 服务器崩溃，OutOfMemoryError

**可能原因**:
- JVM内存配置不足
- 内存泄漏

**解决方案**:
- 增加JVM内存配置
- 检查代码中的内存泄漏问题

## 特定于NettyServer的问题

如果您在使用NettyServer时遇到协议相关错误，而SimpleServer工作正常，可能是因为NettyServer的SFTP协议实现不完整。以下是一些可能的解决方案：

1. **使用完整的SSH/SFTP库**:
   - 考虑在NettyServer中集成Apache MINA SSHD或JSch等成熟的SSH/SFTP库
   - 这些库提供了完整的SSH/SFTP协议实现

2. **完善协议实现**:
   - 确保正确实现SSH密钥交换
   - 确保正确实现SSH加密和MAC算法
   - 确保正确处理SFTP子系统初始化

3. **使用Wireshark分析**:
   - 使用Wireshark捕获成功的SFTP会话（使用SimpleServer）
   - 捕获失败的SFTP会话（使用NettyServer）
   - 比较两者的差异，找出问题所在

4. **临时解决方案**:
   - 如果您需要立即使用SFTP功能，请使用SimpleServer
   - 或者使用其他SFTP客户端，如FileZilla，它可能对协议实现的容错性更好

---

如果您仍然遇到问题，请提供详细的错误日志和服务器输出，以便进一步诊断。 