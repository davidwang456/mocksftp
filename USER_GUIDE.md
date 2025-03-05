# SFTP服务器用户指南

本文档提供了如何连接和使用基于Netty的SFTP服务器的详细说明。

## 目录

1. [服务器启动](#服务器启动)
2. [连接到服务器](#连接到服务器)
3. [基本SFTP命令](#基本sftp命令)
4. [高级用法](#高级用法)
5. [故障排除](#故障排除)

## 服务器启动

在使用SFTP客户端连接之前，您需要先启动SFTP服务器。

### 启动SimpleServer（传统实现）

```bash
# 编译项目
mvn clean package

# 运行SimpleServer
java -cp target/mocksftp-1.0-SNAPSHOT-jar-with-dependencies.jar com.example.mocksftp.SimpleServer
```

### 启动NettyServer（高性能实现）

```bash
# 编译项目
mvn clean package

# 运行NettyServer
java -cp target/mocksftp-1.0-SNAPSHOT-jar-with-dependencies.jar com.example.mocksftp.NettyServer
```

服务器启动后，您应该能看到类似以下的输出：
```
创建tmp目录成功
创建用户目录成功
SFTP服务器正在启动，监听端口：2222
SFTP服务器启动成功
```

## 连接到服务器

一旦服务器启动，您可以使用命令行SFTP客户端连接到服务器。

### 默认连接信息

- **主机**: localhost
- **端口**: 2222
- **用户名**: admin
- **密码**: admin

### Windows系统

在Windows系统中，您可以使用PowerShell或命令提示符：

```bash
# 使用Windows内置的SFTP客户端
sftp -P 2222 admin@localhost
```

如果Windows没有内置SFTP客户端，您可以安装OpenSSH或使用第三方工具如PuTTY的PSFTP：

```bash
# 使用PSFTP
psftp -P 2222 admin@localhost
```

### Linux/macOS系统

在Linux或macOS系统中，直接使用终端：

```bash
sftp -P 2222 admin@localhost
```

### 输入密码

当提示输入密码时，输入`admin`：

```
admin@localhost's password: admin
```

## 基本SFTP命令

成功连接后，您将进入SFTP交互式shell，可以使用以下命令操作文件：

| 命令 | 描述 | 示例 |
|------|------|------|
| `ls` | 列出远程目录内容 | `sftp> ls` |
| `lls` | 列出本地目录内容 | `sftp> lls` |
| `cd` | 切换远程目录 | `sftp> cd documents` |
| `lcd` | 切换本地目录 | `sftp> lcd downloads` |
| `pwd` | 显示远程工作目录 | `sftp> pwd` |
| `lpwd` | 显示本地工作目录 | `sftp> lpwd` |
| `mkdir` | 创建远程目录 | `sftp> mkdir new_folder` |
| `rmdir` | 删除远程目录 | `sftp> rmdir old_folder` |
| `put` | 上传文件 | `sftp> put local_file.txt` |
| `get` | 下载文件 | `sftp> get remote_file.txt` |
| `rm` | 删除远程文件 | `sftp> rm unwanted_file.txt` |
| `exit` | 退出SFTP会话 | `sftp> exit` |

## 高级用法

### 非交互式命令

您可以在一个命令中执行SFTP操作，无需进入交互式shell：

```bash
# 上传文件
sftp -P 2222 admin@localhost <<< $'put local_file.txt'

# 下载文件
sftp -P 2222 admin@localhost <<< $'get remote_file.txt'

# 执行多个命令
sftp -P 2222 admin@localhost <<< $'cd documents\nput report.pdf\nexit'
```

### 使用配置文件

为了简化连接，您可以在~/.ssh/config中添加配置：

```
Host mocksftp
    HostName localhost
    Port 2222
    User admin
```

然后简单地使用：

```bash
sftp mocksftp
```

### 批量传输文件

使用通配符传输多个文件：

```bash
# 上传多个文件
sftp> put *.txt

# 下载多个文件
sftp> get *.pdf
```

### 递归传输目录

传输整个目录及其内容：

```bash
# 递归上传目录
sftp> put -r local_directory

# 递归下载目录
sftp> get -r remote_directory
```

## 故障排除

### 连接问题

| 问题 | 可能的原因 | 解决方案 |
|------|------------|----------|
| 连接被拒绝 | 服务器未运行或端口被占用 | 确认服务器正在运行，并检查端口是否正确 |
| 认证失败 | 用户名或密码错误 | 确保使用正确的凭据（admin/admin） |
| 超时 | 网络问题或防火墙限制 | 检查网络连接和防火墙设置 |

### 文件传输问题

| 问题 | 可能的原因 | 解决方案 |
|------|------------|----------|
| 权限被拒绝 | 文件权限不足 | 检查服务器上的文件和目录权限 |
| 磁盘空间不足 | 服务器或客户端磁盘已满 | 清理不必要的文件释放空间 |
| 文件不存在 | 路径错误或文件已删除 | 确认文件路径和名称正确 |

### 服务器日志

如果遇到问题，请查看服务器控制台输出和日志文件（位于logs目录）以获取更多信息。

## 性能优化

使用NettyServer而不是SimpleServer可以获得更好的性能，特别是在高并发场景下。NettyServer利用了Netty的异步非阻塞IO模型，能够处理更多的并发连接。

---

如有任何问题或需要进一步的帮助，请联系系统管理员或查阅项目文档。 