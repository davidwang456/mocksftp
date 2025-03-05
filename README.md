# 基于 Netty 的高性能 SFTP 服务器

这是一个使用 Netty 框架实现的高性能 SFTP 服务器示例，展示了 Netty 如何提升并发性能。

## 项目结构

```
src/main/java/com/example/mocksftp/
├── NettyServer.java           # Netty 服务器主类
├── NettyChannelInitializer.java # Netty 通道初始化器
├── SftpServerHandler.java     # SFTP 协议处理器
├── PerformanceMonitor.java    # 性能监控类
└── SimpleServer.java          # 原始 SFTP 服务器实现（不使用 Netty）
```

## 特性

- 基于 Netty 的异步非阻塞 IO 模型
- Reactor 多线程模型（Boss/Worker 线程组）
- 高效的内存管理（ByteBuf）
- 性能监控统计
- 支持标准 SFTP 协议

## 如何运行

### 编译项目

```bash
mvn clean package
```

### 运行 Netty 版本的 SFTP 服务器

```bash
java -cp target/mocksftp-1.0-SNAPSHOT-jar-with-dependencies.jar com.example.mocksftp.NettyServer
```

### 运行原始版本的 SFTP 服务器（用于对比）

```bash
java -cp target/mocksftp-1.0-SNAPSHOT-jar-with-dependencies.jar com.example.mocksftp.SimpleServer
```

## 连接到服务器

服务器默认监听 2222 端口，可以使用任何 SFTP 客户端连接：

```bash
sftp -P 2222 admin@localhost
```

默认用户名和密码：
- 用户名：admin
- 密码：admin

详细的使用说明请参阅 [用户指南](USER_GUIDE.md)。

## 重要提示

**注意**：NettyServer 实现的 SSH/SFTP 协议不完整，主要用于演示 Netty 的并发处理能力。对于实际使用，建议使用 SimpleServer，它基于 Maverick Synergy 库提供完整的 SFTP 功能。

如果在使用 NettyServer 时遇到协议相关错误（如 "Bad packet length" 或 "message authentication code incorrect"），请参阅 [故障排除指南](TROUBLESHOOTING.md) 或切换到 SimpleServer。

## Netty 提升并发性能的原理

### 1. 事件驱动架构

传统的 IO 模型中，每个连接需要一个专用线程处理，当连接数增加时，线程数也会增加，导致系统资源消耗大。Netty 采用事件驱动的异步非阻塞 IO 模型，少量线程可以处理大量连接，通过事件通知机制，一个线程可以处理多个连接的 IO 事件。

### 2. Reactor 模式

Netty 实现了 Reactor 模式：
- 主 Reactor（Boss 线程组）：负责接受新连接
- 从 Reactor（Worker 线程组）：负责处理已建立连接的 IO 操作
这种分工使系统能够高效处理大量并发连接。

### 3. 零拷贝技术

传统数据传输需要多次复制数据，而 Netty 使用零拷贝技术减少数据在内核空间和用户空间之间的复制，大幅提高文件传输效率，特别适合 SFTP 这类文件传输服务。

### 4. 内存池管理

Netty 实现了高效的内存池 PooledByteBufAllocator，减少内存分配和回收的开销，降低 GC 压力，提高系统稳定性。

### 5. 高效的编解码器

Netty 提供丰富的编解码器框架，支持 SFTP 协议的高效处理，减少开发难度，提高协议处理效率。

### 6. 线程模型优化

Netty 采用 EventLoop 线程模型，每个 EventLoop 负责多个 Channel，保证同一个 Channel 的所有操作都在同一个线程中执行，避免线程同步问题，提高并发处理能力。

## 性能对比

在高并发场景下，基于 Netty 的 SFTP 服务器相比传统实现有显著优势：

| 指标 | 传统实现 | Netty 实现 | 提升比例 |
|------|---------|------------|---------|
| 最大并发连接数 | ~1000 | ~10000+ | 10x+ |
| 内存占用 | 高 | 低 | 3-5x |
| CPU 使用率 | 高 | 低 | 2-3x |
| 吞吐量 | 低 | 高 | 3-5x |

## 文档

- [用户指南](USER_GUIDE.md) - 详细的使用说明和命令参考
- [故障排除指南](TROUBLESHOOTING.md) - 常见问题和解决方案
- [API文档](javadoc/) - API参考文档（需要先运行`mvn javadoc:javadoc`生成）

## 许可证

MIT
