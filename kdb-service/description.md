# 模块描述: 知识库服务 kdb-service

这个模块主要为AI模块与业务模块提供知识库检索、构建与检索增强生成服务。

## 部署与依赖

模块的pom添加了neo4j的spring data与spring ai vector store依赖，故请在docker部署neo4j并监听7687端口。

部署完成后，浏览器访问7474端口，登录后即可查看数据库。账户密码可在模块配置文件中查看。

此模块依赖AI模块。


检索所有数据库中数据的Cypher代码：
```genericsql
MATCH (n) 
OPTIONAL MATCH (n)-[r]->(m) 
RETURN n, r, m
```

## 功能说明

模块提供了知识图谱的数据抽取、构建与检索功能，提供了附加的检索增强生成功能。

模块调用AI抽取原始文本的知识点并构建知识点间关系。

`com.superyoudi.kdbservice.config`内存放了Neo4j的向量数据库配置。不过由于yml突然能自动配置了，所以我就注释掉了自定义的bean初始化。

`com.superyoudi.kdbservice.dto.KnowledgeExtractionResult`存放AI抽取出的知识点与关系的JSON映射的对象。

`com.superyoudi.kdbservice.graph`存放知识图谱的数据模型。其中：

- `KnowledgePointNode`为知识点节点
- `KnowledgePointRelation`为知识点关系
- `KnowledgeTextFragmentNode`存放抽取的原始文本。

`com.superyoudi.kdbservice.repository`提供了知识图谱数据模型的CRUD功能。

`com.superyoudi.kdbservice.service.KnowledgeGraphService`提供了检索增强生成与原始文本抽取的功能。