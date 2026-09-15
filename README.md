# JGCB

基于 Spring Boot 3、Java 17、MySQL 和 Vue 3 的管理与互动应用，包含用户、签到、活动、回忆录、宠物和多人房间功能。

## 目录

- `src/main/java`：后端源码。
- `src/main/resources/sql`：功能相关数据库脚本。
- `local-dev-schema.sql`：本地开发数据库基础表结构。
- `jgcb-ui`：Vue 3 / Vite 前端源码及静态素材。

## 本地运行

准备 Java 17、Maven、Node.js / npm 和 MySQL。

1. 在 MySQL 中执行 `local-dev-schema.sql`，并按需要执行 `src/main/resources/sql` 下的功能脚本。
2. 将 `src/main/resources/application.example.yml` 复制为同目录的 `application.yml`，根据本机情况修改数据库地址和用户名。设置环境变量 `DB_PASSWORD` 和 `JWT_SECRET`（随机生成，至少 32 字节），或在本地配置中填写对应值。本地配置不会提交到 Git。
3. 在项目根目录运行 `mvn spring-boot:run`，后端默认端口为 8080。
4. 在 `jgcb-ui` 中依次运行 `npm ci` 和 `npm run dev`，访问 `http://localhost:3000`。

首次启动会创建默认管理员 `admin / 123456`，部署前请修改密码。

## 打包

在 `jgcb-ui` 目录执行 `npm ci` 和 `npm run build`，将生成的 `dist` 目录内容复制到 `src/main/resources/static`，然后在根目录执行：

```sh
mvn clean package
java -jar target/jgcb-1.0.0.jar
```

根目录的 `start.bat` 使用同目录下的 `jgcb-1.0.0.jar`；如使用此脚本，请先从 `target` 复制打包产物。

依赖、构建产物、日志、本地备份、用户上传文件和真实配置不纳入源码仓库。
