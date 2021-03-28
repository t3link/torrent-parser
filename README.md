这是一个专为 `private tracker` 写的一个种子文件解析器

默认以 `UTF-8` 编码处理，对于非 `UTF-8` 编码将直接抛出异常，因为没有意义，不符合标准

运行要求 `JDK15+` 

`mvn clean verify` 启动项目并生成测试用例覆盖报告

---

### 1. 解析

```java
// 读取文件流 bencode 编码
var info = LibParser.read(ins, "your_site_source_name");

// 将来生成新种子的 info_hash
var hash = info.hash();

// 种子的文件结构，包含了文件的名称、大小等基本信息
var files = info.files();

// 文件树节点对象，可以序列化为 json 存入数据库
var root = files.getRoot();
// 打印文本格式的文件树
var print = T3tTree.print(root);
```

### 2. 构建

```java
// 构建站点的私有属性
var extra = new ExtraInfo("https://example.com", null);
var bytes = LibParser.write(info, extra);
```