# Maverick-Synergy日志配置指南

## 将Maverick-Synergy日志输出到指定文件的方法

要将Maverick-Synergy的日志输出到特定的日志文件中，您可以使用以下几种方法：

### 1. 通过配置文件设置

最简单的方法是创建或修改`logging.properties`配置文件：

```properties
# 启用文件日志
maverick.log.file=true
maverick.log.file.level=DEBUG
# 设置日志文件路径
maverick.log.file.path=/path/to/your/custom.log
# 配置日志文件轮转
maverick.log.file.maxFiles=10
maverick.log.file.maxSize=20MB
```

将此文件放在应用程序的类路径根目录下，系统会自动加载。

### 2. 通过系统属性设置

如果您需要在不修改配置文件的情况下指定日志文件，可以通过Java系统属性：

```java
// 在应用程序启动前设置
System.setProperty("maverick.log.file", "true");
System.setProperty("maverick.log.file.level", "DEBUG");
System.setProperty("maverick.log.file.path", "/path/to/your/custom.log");
```

如果是在命令行启动应用程序，可以这样设置：

```
java -Dmaverick.log.file=true -Dmaverick.log.file.level=DEBUG -Dmaverick.log.file.path=/path/to/your/custom.log -jar your-application.jar
```

### 3. 通过代码设置自定义日志上下文

对于更灵活的控制，您可以通过代码直接设置日志上下文：

```java
// 创建文件日志上下文
File logFile = new File("/path/to/your/custom.log");
FileLoggingContext fileContext = new FileLoggingContext(Level.DEBUG, logFile);

// 为默认日志上下文添加文件日志
DefaultLoggerContext defaultContext = (DefaultLoggerContext)Log.getDefaultContext();
defaultContext.addContext(fileContext);

// 或者，设置为当前线程的日志上下文
Log.setupCurrentContext(fileContext);
```

### 4. 为特定SSH连接设置单独的日志文件

如果您需要为每个SSH连接创建单独的日志文件：

```java
// 配置连接日志
ConnectionLoggingContext connLogger = new ConnectionLoggingContext("ssh");
// 可以自定义文件名格式
connLogger.setFilenameFormat("logs/ssh_${timestamp}_${remoteAddr}.log");
// 为连接启用日志
connLogger.startLogging(sshConnection, Level.DEBUG);

// 连接结束后关闭日志
connLogger.close(sshConnection);
```

### 5. 动态修改日志配置

您可以在应用程序运行时动态修改日志配置：

```java
// 获取默认日志上下文
DefaultLoggerContext context = (DefaultLoggerContext)Log.getDefaultContext();
// 启用文件日志并设置路径
context.setProperty("maverick.log.file", "true");
context.setProperty("maverick.log.file.path", "/path/to/your/new.log");
// 应用更改
context.reloadConfiguration();
```

## 注意事项

1. 确保目标文件所在目录存在或具有创建权限
2. 合理设置日志级别以避免生成过多日志
3. 对于生产环境，建议配置日志轮转防止单个日志文件过大
4. 在高并发环境中，应谨慎使用文件日志，可能会影响性能

## 日志系统架构

Maverick-Synergy项目中的日志系统采用了灵活、可配置的设计，支持多种日志输出方式，并提供了不同的日志级别来满足各种场景的需求。

### 核心组件

- `Log`：静态工具类，提供简单的日志API接口
- `LoggerContext`：日志上下文接口，定义日志记录的基本操作
- `DefaultLoggerContext`：默认日志上下文实现，管理多个日志输出目标
- `FileLoggingContext`：文件日志实现，支持日志轮转
- `ConsoleLoggingContext`：控制台日志实现

### 日志级别

```java
public enum Level {
    NONE,   // 不记录任何日志
    ERROR,  // 仅记录错误信息
    WARN,   // 记录警告和错误信息
    INFO,   // 记录一般信息、警告和错误信息
    DEBUG,  // 记录调试信息及以上级别
    TRACE   // 记录所有详细信息
}
```

### 线程上下文

- 支持每个线程独立的日志上下文
- 允许为不同SSH连接设置独立的日志配置

## 完整配置示例

以下是一个完整的`logging.properties`配置文件示例，包含了各种常用设置：

```properties
# 全局日志级别设置
maverick.log.level=INFO

# 控制台日志设置
maverick.log.console=true
maverick.log.console.level=INFO
# 彩色日志输出（仅支持部分终端）
maverick.log.console.color=true

# 文件日志设置
maverick.log.file=true
maverick.log.file.level=DEBUG
maverick.log.file.path=logs/synergy.log
maverick.log.file.maxFiles=10
maverick.log.file.maxSize=20MB
# 是否追加到现有文件 
maverick.log.file.append=true

# 连接日志设置
maverick.log.connection=true
maverick.log.connection.level=DEBUG
# 文件名格式支持变量替换
maverick.log.connection.filenameFormat=logs/${timestamp}_${uuid}_${user}.log
maverick.log.connection.maxFiles=5
maverick.log.connection.maxSize=10MB

# 禁用文件变化监控线程
maverick.log.nothread=false

# 连接过滤器设置（仅记录特定IP的连接日志）
maverick.log.connection.filter.ip=192.168.1.*,10.0.0.*
# 按用户名过滤
maverick.log.connection.filter.username=admin,operator
```

## 日志输出格式

默认情况下，Maverick-Synergy的日志格式如下：

```
日期时间 [线程名称] 日志级别 - 日志内容
```

例如：

```
23 Jan 2024 14:32:45,123 [main-thread] INFO - 服务器启动成功，监听端口22
```

异常堆栈跟踪会完整保留并输出：

```
23 Jan 2024 14:35:12,456 [session-thread] ERROR - 连接失败: 认证错误
java.io.IOException: Authentication failed
    at com.sshtools.server.SshServerConnection.authenticate(SshServerConnection.java:243)
    at com.sshtools.server.SshServerConnection$AuthenticationThread.run(SshServerConnection.java:512)
    ...
``` 