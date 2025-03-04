# MockSFTP

基于Netty和Maverick Synergy的SFTP服务器，支持加密传输和压缩传输。

## 功能特性

- 基于Netty 4.1.101.Final构建
- 使用Maverick Synergy 3.0.1.Final-1实现SFTP协议
- 支持加密传输
- 支持压缩传输
- 自动生成主机密钥
- 简单的用户认证（可自定义）
- 可配置的根目录

## 系统要求

- Java 11或更高版本
- Maven 3.6或更高版本

## 快速开始

### 编译项目

```bash
mvn clean package
```

编译成功后，将在`target`目录下生成可执行JAR文件。

### 运行服务器

```bash
java -jar target/mocksftp-1.0-SNAPSHOT-jar-with-dependencies.jar
```

默认情况下，服务器将在以下配置下运行：
- 监听端口：2222
- 主机密钥：hostkey.pem（如果不存在，将自动生成）
- SFTP根目录：sftp-root（如果不存在，将自动创建）

### 命令行参数

可以通过命令行参数自定义服务器配置：

```bash
java -jar target/mocksftp-1.0-SNAPSHOT-jar-with-dependencies.jar -p 2222 -k hostkey.pem -r sftp-root
```

参数说明：
- `-p <端口>`: 指定服务器监听端口
- `-k <密钥路径>`: 指定主机密钥文件路径
- `-r <根目录>`: 指定SFTP根目录

## 连接到服务器

可以使用任何SFTP客户端连接到服务器，例如：

```bash
sftp -P 2222 username@localhost
```

当前实现接受任何用户名和密码组合。

## 自定义

### 用户认证

默认情况下，服务器接受任何用户名和密码组合。如需实现自定义认证，请修改`SftpServer.java`中的`setPasswordAuthenticator`方法。

### 文件系统权限

文件系统权限控制在`MockSftpSubsystemFactory.java`的`MockFileSystemPolicy`内部类中定义。可以根据需要修改各种权限检查方法。

## 日志

日志配置在`src/main/resources/logback.xml`文件中。默认情况下，日志将输出到控制台和`logs/mocksftp.log`文件。

## 开源SFTP工具对比

以下是一些常见开源SFTP工具的对比，可以帮助您选择适合自己需求的工具：

### 1. OpenSSH SFTP

**优点：**
- 最广泛使用的SFTP实现，几乎所有Linux/Unix系统默认安装
- 安全性高，持续更新和维护
- 命令行界面简单高效
- 支持多种认证方式（密码、密钥）

**缺点：**
- 命令行界面对新手不友好
- 缺乏图形界面（需要第三方工具）
- 高级功能配置较复杂

### 2. FileZilla

**优点：**
- 提供直观的图形用户界面
- 跨平台支持（Windows、Linux、macOS）
- 支持多种协议（FTP、FTPS、SFTP）
- 支持断点续传和文件队列

**缺点：**
- 安装包可能包含捆绑软件
- 某些高级功能在免费版中不可用
- 大文件传输时可能不稳定

### 3. WinSCP

**优点：**
- Windows平台下功能丰富的图形界面
- 支持多种协议（SFTP、SCP、FTP等）
- 集成文件编辑器和命令行界面
- 支持脚本和自动化操作

**缺点：**
- 仅限Windows平台
- 界面可能显得复杂
- 某些高级功能需要深入学习

### 4. SSHFS

**优点：**
- 将远程SFTP服务器挂载为本地文件系统
- 无需专门的客户端，可直接使用本地文件管理器
- 操作透明，用户体验好
- 支持所有标准文件操作

**缺点：**
- 性能可能不如直接SFTP传输
- 配置和使用在Windows上较复杂
- 网络不稳定时可能导致挂载点无响应

### 5. Maverick Synergy

**优点：**
- Java实现，跨平台兼容性好
- 支持加密和压缩传输
- 可嵌入到其他Java应用中
- 性能优化良好

**缺点：**
- 主要是库而非独立工具，需要开发集成
- 文档相对较少
- 社区支持不如其他主流工具

### 6. Apache MINA SSHD

**优点：**
- 纯Java实现的SSH/SFTP服务器和客户端库
- 轻量级设计
- 易于集成到Java应用中
- 支持多种认证方式

**缺点：**
- 主要面向开发者而非终端用户
- 需要编程知识才能使用
- 功能相比商业解决方案较少

### 7. Cyberduck

**优点：**
- 美观的图形界面
- 支持多种云存储和协议（包括SFTP）
- 跨平台（macOS和Windows）
- 集成编辑器和版本控制

**缺点：**
- 免费版功能有限
- 在处理大量文件时可能变慢
- Linux支持有限

### 8. PuTTY/PSFTP

**优点：**
- 轻量级且可靠
- 无需安装，可直接运行
- 与PuTTY密钥管理集成
- 命令行界面简洁高效

**缺点：**
- 原生仅支持Windows（虽然有非官方移植版）
- 缺乏图形界面
- 功能相对基础

## 许可证

本项目采用MIT许可证。

## 打包错误解决方案

如果在打包过程中遇到API不兼容的错误，可以尝试以下解决方案：

### 1. 使用最新版本的依赖

确保在pom.xml中使用最新版本的Maverick Synergy依赖：

```xml
<properties>
    <maverick.version>3.1.2</maverick.version>
</properties>

<dependencies>
    <!-- Maverick Synergy SFTP Server -->
    <dependency>
        <groupId>com.sshtools</groupId>
        <artifactId>maverick-synergy-server</artifactId>
        <version>${maverick.version}</version>
    </dependency>
    
    <!-- Maverick Synergy SFTP -->
    <dependency>
        <groupId>com.sshtools</groupId>
        <artifactId>maverick-synergy-fuse</artifactId>
        <version>${maverick.version}</version>
    </dependency>
    
    <!-- Maverick Synergy VFS -->
    <dependency>
        <groupId>com.sshtools</groupId>
        <artifactId>maverick-synergy-vfs</artifactId>
        <version>${maverick.version}</version>
    </dependency>
</dependencies>
```

### 2. 使用简化版的实现

由于Maverick Synergy API可能会随着版本更新而变化，可以使用SimpleSftpServer类作为一个框架，根据最新的API文档进行实现。

### 3. 参考官方示例

可以参考Maverick Synergy的官方示例和文档，了解最新的API用法：

- [Maverick Synergy GitHub](https://github.com/sshtools/maverick-synergy)
- [Maverick Synergy 文档](https://www.jadaptive.com/maverick-synergy/docs/)

### 4. 检查类路径

确保所有必要的依赖都已正确添加到类路径中，并且没有版本冲突。

### 5. 使用兼容的Java版本

确保使用与Maverick Synergy兼容的Java版本（推荐Java 11或更高版本）。 # mocksftp
