import ch.qos.logback.core.joran.spi.ConsoleTarget
import ch.qos.logback.core.util.FileSize

def environment = System.getenv().getOrDefault("ENVIRONMENT", "production")

def defaultLevel = INFO
def defaultTarget = ConsoleTarget.SystemErr

if (environment == "dev") {
    defaultLevel = DEBUG
    defaultTarget = ConsoleTarget.SystemOut

    // Silence warning about missing native PRNG
    logger("io.ktor.util.random", ERROR)
}

appender("ROLLING", RollingFileAppender) {
    append = true
    encoder(PatternLayoutEncoder) {
        pattern = "%d %p %c{1.} [%t] %m%n"
    }
    rollingPolicy(TimeBasedRollingPolicy) {
        fileNamePattern = "log/basura-%d{yyyy-MM-dd}.log"
        maxHistory = 30
        totalSizeCap = FileSize.valueOf("2GB")
    }
}

appender("FULL_STACKTRACE", FileAppender) {
    append = true
    file = "log/error.log"
    encoder(PatternLayoutEncoder) {
        pattern = "%d %p %c{1.} [%t] %m%n"
    }
}

appender("CONSOLE", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%boldGreen(%d{yyyy-MM-dd}) %boldYellow(%d{HH:mm:ss}) %gray(|) %highlight(%5level) %gray(|) %boldMagenta(%40.40logger{40}) %gray(|) %msg%n"
        withJansi = true
    }

    target = defaultTarget
}

logger("StackTrace", ERROR, ['FULL_STACKTRACE'], false)
root(defaultLevel, ["ROLLING", "CONSOLE", "FULL_STACKTRACE"])