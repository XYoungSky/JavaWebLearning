# JavaWeb 图书管理系统

一个基于柏马的教程的 [JavaWeb 后端](https://www.itbaima.cn/zh-CN/document/ycpagby2v7j4p728)知识点的教学项目。项目不使用 Spring，直接展示 Jakarta Servlet、MyBatis、MySQL、Thymeleaf、Cookie、Session、Filter、Listener、文件传输和 Tomcat 的工作方式。

## 环境与启动

- JDK 17 或更高版本
- Maven Wrapper（仓库已包含）
- MySQL 8
- Tomcat 11

1. 检查 `src/main/resources/mybatis-config.xml` 中的数据库地址是否能从当前机器访问。
2. 在该配置指向的 `study` 数据库执行 `schema.sql`。脚本可以重复执行，应用不会自动修改数据库结构。
3. 构建项目：

   ```bash
   ./mvnw clean package
   ```

4. 将 `target/javaweb-library.war` 部署到 Tomcat 11 的 `webapps` 目录。
5. 访问 `http://localhost:8080/javaweb-library/`。

演示管理员用户名为 `admin`，密码为 `admin123`。密码以 PBKDF2 哈希保存在数据库中，‘记住我’ Cookie 只保存用户名。

## 功能与路由

| 路由 | 方法 | 功能 |
| --- | --- | --- |
| `/login` | GET / POST | 登录、记住用户名 |
| `/logout` | POST | 注销并销毁 Session |
| `/books` | GET | 图书列表 |
| `/books/create` | POST | 添加图书 |
| `/books/delete` | POST | 删除未借出的图书 |
| `/students` | GET | 学生列表 |
| `/loans` | GET | 借阅记录 |
| `/loans/create` | POST | 登记借阅 |
| `/loans/return` | POST | 归还图书 |
| `/lab` | GET | 开发者实验室 |
| `/api/time` | GET | XHR 时间接口 |

成功写操作全部采用 POST-Redirect-GET。借阅与归还在单个 MyBatis 事务中更新借阅记录和图书状态，并通过 `SELECT ... FOR UPDATE` 避免并发重复借阅。

## 三层架构

- `servlet`：读取请求、调用业务层、选择模板或重定向。
- `service`：参数校验、业务规则与事务边界。
- `mapper`：注解 SQL 和 XML Mapper 两种 MyBatis 写法。
- `domain`：贯穿三层的数据实体。
- `filter` / `listener`：认证、编码、日志以及应用和 Session 生命周期。

## 知识点覆盖

| 笔记知识点 | 项目位置或行为 |
| --- | --- |
| HTTP 请求/响应 | 开发者实验室请求检查器、响应状态与响应头 |
| Servlet 与生命周期 | `LabServlet` 的构造、`init`、`service`、`destroy` 和 `loadOnStartup` |
| GET / POST 与表单 | 登录、图书、借阅等 Servlet |
| `@WebServlet` 与初始化参数 | 所有路由及 `LabServlet` 的 `@WebInitParam` |
| MyBatis + MySQL | 登录查询、图书 XML Mapper、借阅事务 |
| 上传和下载 | `/lab/files/upload` 与 `/lab/files/download` |
| XHR | 实验室服务器时间组件 |
| 转发与重定向 | 实验室对比按钮及登录后的 PRG |
| ServletContext | 共享 `SqlSessionFactory`、计数器、全局初始化参数 |
| Cookie | 记住用户名、实验 Cookie 创建/读取/删除 |
| Session | 登录态、实验室 Session 信息和立即失效 |
| Filter | UTF-8/请求日志过滤器、认证过滤器 |
| Listener | 应用初始化、Session 创建和销毁计数 |
| JSP | `/lab/jsp` 隔离的旧式视图演示 |
| Thymeleaf | 变量、属性、表达式、条件、switch、循环、迭代状态和片段布局 |
| Tomcat 类加载 | 实验室展示当前 Web 应用类加载器链 |

## 教程来源：

- https://www.itbaima.cn/zh-CN/document/ycpagby2v7j4p728
- https://www.bilibili.com/video/BV1CL4y1i7qR/
