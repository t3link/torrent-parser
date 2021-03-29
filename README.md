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
// 序列化文件树
var json = JsonUtil.toJson(root);
// 反序列化
var des = JsonUtil.fromJson(json, FileNode.class);
```
打印文件树
```text
complex 2.19 GB
|--- 🥰😗🎆🎑🎀🐈.mkv 320.00 MB
|--- Ⅰ 640.00 MB
|   |--- dá nàng dá bìng  cuàn mì pō.mkv 320.00 MB
|   |--- 囙嚻齉龘靐爨羃醗炁勥烎龖.mkv 320.00 MB
|--- Ⅱ 640.00 MB
|   |--- Österreich öfter weiß.mkv 320.00 MB
|   |--- أهلاً بك في التحكم في حسابك.mkv 320.00 MB
|--- Ⅲ 640.00 MB
    |--- これまでは高価な培養液でもほとんど.mkv 320.00 MB
    |--- 붉은 해가 동쪽에서 떠오르다 달성하여 홍기.mkv 320.00 MB
```
序列化文件树
```json
{
    "id":0,
    "type":"FOLDER",
    "name":"complex",
    "size":"2.19 GB",
    "children":[
        {
            "id":1,
            "type":"VIDEO",
            "name":"🥰😗🎆🎑🎀🐈.mkv",
            "size":"320.00 MB"
        },
        {
            "id":3,
            "type":"FOLDER",
            "name":"Ⅰ",
            "size":"640.00 MB",
            "children":[
                {
                    "id":2,
                    "type":"VIDEO",
                    "name":"dá nàng dá bìng  cuàn mì pō.mkv",
                    "size":"320.00 MB"
                },
                {
                    "id":4,
                    "type":"VIDEO",
                    "name":"囙嚻齉龘靐爨羃醗炁勥烎龖.mkv",
                    "size":"320.00 MB"
                }
            ]
        },
        {
            "id":6,
            "type":"FOLDER",
            "name":"Ⅱ",
            "size":"640.00 MB",
            "children":[
                {
                    "id":5,
                    "type":"VIDEO",
                    "name":"Österreich öfter weiß.mkv",
                    "size":"320.00 MB"
                },
                {
                    "id":7,
                    "type":"VIDEO",
                    "name":"أهلاً بك في التحكم في حسابك.mkv",
                    "size":"320.00 MB"
                }
            ]
        },
        {
            "id":9,
            "type":"FOLDER",
            "name":"Ⅲ",
            "size":"640.00 MB",
            "children":[
                {
                    "id":8,
                    "type":"VIDEO",
                    "name":"これまでは高価な培養液でもほとんど.mkv",
                    "size":"320.00 MB"
                },
                {
                    "id":10,
                    "type":"VIDEO",
                    "name":"붉은 해가 동쪽에서 떠오르다 달성하여 홍기.mkv",
                    "size":"320.00 MB"
                }
            ]
        }
    ]
}
```
### 2. 构建

```java
// 构建站点的私有属性
var extra = new ExtraInfo("https://example.com", null);
var bytes = LibParser.write(info, extra);
```