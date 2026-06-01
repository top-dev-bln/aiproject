# Excel Spring Boot Starter

[![Maven Central](https://img.shields.io/maven-central/v/com.pig4cloud.excel/excel-spring-boot-starter.svg)](https://central.sonatype.com/artifact/com.pig4cloud.excel/excel-spring-boot-starter)

`excel-spring-boot-starter` 是一个基于 `Apache FESOD` 实现的 Spring Boot Starter，用于简化 Excel 的读写操作。`Apache FESOD` 是一个 Java 开源项目，旨在以尽可能低的内存消耗实现对 Excel 文件的读写。

- 更多详细的使用说明，请参考文档：[https://www.yuque.com/pig4cloud/ogf9nv](https://www.yuque.com/pig4cloud/ogf9nv)

## 功能概述

- 轻松集成到 Spring Boot 项目中，快速实现 Excel 文件的导入和导出。
- 通过注解配置导入和导出的 Excel 文件格式。
- 提供了简洁易用的 API，极大地减少了手动处理 Excel 文件的工作量。

## 依赖引用

项目已经上传至 Maven 中央仓库，只需引入以下依赖即可使用：

| 版本    | 支持版本            |
|-------|-------------------|
| 4.0.0 | 适配 Spring Boot 4.x |
| 3.4.3 | 适配 Spring Boot 3.x |
| 1.2.7 | 适配 Spring Boot 2.x |

在 `pom.xml` 中添加以下依赖：

```xml
<dependency>
  <groupId>com.pig4cloud.excel</groupId>
  <artifactId>excel-spring-boot-starter</artifactId>
  <version>${lastVersion}</version>
</dependency>
```

## 使用文档

更多详细的使用说明，请参考文档：[https://www.yuque.com/pig4cloud/ogf9nv](https://www.yuque.com/pig4cloud/ogf9nv)
