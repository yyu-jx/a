package com.superyoudi.kdbservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.superyoudi.kdbservice.dto.KnowledgeExtractionResult;
import com.superyoudi.kdbservice.service.KnowledgeGraphService;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = KdbServiceApplication.class)
@TestPropertySource(
        properties = {
                "spring.ai.dashscope.api-key=${DASHSCOPE_API_KEY}",
                "spring.neo4j.uri=bolt://localhost:7687",
                "spring.neo4j.authentication.username=neo4j",
                "spring.neo4j.authentication.password=12345678",
                "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
        })
@ComponentScan(basePackages = {"com.superyoudi", "com.superyoudi.aiservice"})
class KdbServiceTests {

    @Autowired
    private KnowledgeGraphService knowledgeGraphService;

    @Test
    public void testSearchByQuestion() {
        String string = knowledgeGraphService.retrieveKnowledgeGraph("什么是页表?");
        System.out.println(string);
    }

    @Test
    public void storeKnowledgeDocuments() throws JsonProcessingException {
        ObjectMapper LENIENT_MAPPER = JsonMapper.builder()
                .enable(JsonReadFeature.ALLOW_UNQUOTED_FIELD_NAMES)  // 允许无引号字段名
                .enable(JsonReadFeature.ALLOW_SINGLE_QUOTES)         // 允许单引号
                .enable(JsonReadFeature.ALLOW_TRAILING_COMMA)        // 允许尾随逗号
                .build()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Document document1 = new Document("""
                在计算机的世界里，提起“效率”二字，你很难绕过三个紧密咬合的核心概念：缓存、局部性原理与页表。它们并非孤立的知识点，而是一套为了弥合硬件速度鸿沟、实现虚拟内存系统的组合拳。
                故事的起点，是一道横亘在处理器与内存之间的天堑。CPU完成一次计算只需不到一纳秒，而从内存取一次数据却需要几十到上百纳秒。如果让高速的CPU每次都直接等待慢速的内存，无异于让跑车在泥泞的乡间小路上行驶。为了解决这个矛盾，人们在CPU和内存之间架起了一座高速桥梁——缓存。缓存是一种容量小但速度极快的存储器，它保存着CPU近期可能会频繁访问的数据副本。当CPU索取数据时，会先向缓存伸手，如果数据恰好在其中，即“缓存命中”，就能避免对内存的漫长访问；如果不在，才不得不经历一次“缓存未命中”的惩罚，去内存中寻找并更新缓存。
                但这引出了一个根本问题：缓存凭什么能预知CPU将来需要什么数据？这就引出了支撑它的哲学基石——局部性原理。程序在执行时，并非随机地四处乱跳，而是表现出强烈的集群性，这分为两种。时间局部性指，如果一个数据项被访问，那它在不久的将来很可能再次被访问，比如循环中的计数器变量会被反复读写。空间局部性则指，如果一个数据项被访问，那么它地址附近的数据也极可能随后被访问，因为指令通常是顺序执行的，而数组、结构体等数据结构在内存中也是连续存放的。
                缓存正是利用了这两种局部性来工作。利用时间局部性，它将近期访问过的数据保留在缓存中，以备CPU不久后再次需要。利用空间局部性，它在一次未命中而不得不访问内存时，不仅取回所需的那个数据，还会把相邻的一整块数据（称为一个缓存行，通常64字节）都取上来。这样做看似浪费，实则是一种战略性的预判——既然程序大概率会顺序访问，那一次多拿点，后续就能连续命中。可以说，没有局部性原理作为软件的固有特质，缓存硬件就成了盲人摸象，效率提升将无从谈起。
                那么，这一切又与页表有何关联？页表是操作系统实现虚拟内存的核心数据结构，它负责将每个进程看到的虚拟地址翻译成物理内存地址，不仅让进程间内存隔离，还允许程序使用比实际物理内存更大的地址空间。此时，一个精妙的关联闭环出现了：局部性原理不仅体现在数据访问上，也同样支配着内存地址的访问模式，而页表与缓存的关系更是双重的。
                一方面，程序连续的虚拟地址空间本身就体现了空间局部性，使得地址翻译过程也会频繁访问相同或相邻的页表条目。为了加速这一过程，CPU内部专门设有“转译后备缓冲区”（TLB），它就是一种专门缓存页表条目的高速缓存，依靠时间局部性让地址翻译也快起来。另一方面，在获取最终数据时，缓存与页表又构成了微妙的协作。物理缓存可以设计在虚拟地址翻译之前或之后进行查找，但无论哪种方式，都需要保证同一物理地址不会因不同的虚拟映射而产生缓存一致性问题。当缓存未命中而从物理内存加载数据时，操作系统通过查询页表，将连续的虚拟地址片段映射到可能不连续的物理页框上，这种映射结合空间局部性，让缓存行的预取机制依然高效——即便物理上并不连续，但在一个页（通常4KB）的内部，数据的物理地址仍是连续的。
                由此，三个知识点形成了一个坚不可摧的铁三角：局部性原理是理论基础，揭示了程序行为的可预测性；缓存是硬件实践，利用这种可预测性来隐藏内存延迟；页表则是操作系统的宏大设计，在构建虚拟内存世界的同时，其地址翻译的局部性也被TLB缓存所加速，并巧妙地融入到物理缓存的运作模型中。理解这个闭环，正是从“写代码”迈向“理解计算机系统”的关键一步。
                """);
        String json1 = """
                {
                                     "knowledgePoints": [
                                       {
                                         "name": "缓存",
                                         "description": "一种容量小但速度极快的存储器，位于CPU和内存之间，用于保存CPU近期可能频繁访问的数据副本。"
                                       },
                                       {
                                         "name": "局部性原理",
                                         "description": "程序在执行时访问模式表现出强烈集群性的内在特质，分为时间局部性和空间局部性。"
                                       },
                                       {
                                         "name": "页表",
                                         "description": "操作系统用于将虚拟地址翻译为物理地址的核心数据结构，实现进程内存隔离与虚拟内存。"
                                       },
                                       {
                                         "name": "缓存命中",
                                         "description": "CPU请求的数据恰好存在于缓存中的状态，能避免慢速的内存访问。"
                                       },
                                       {
                                         "name": "缓存未命中",
                                         "description": "CPU请求的数据不在缓存中，必须从内存中获取并更新缓存的状态。"
                                       },
                                       {
                                         "name": "时间局部性",
                                         "description": "一个数据项被访问后，在不久的将来很可能再次被访问的局部性原理。"
                                       },
                                       {
                                         "name": "空间局部性",
                                         "description": "一个数据项被访问后，其地址附近的数据也极可能随后被访问的局部性原理。"
                                       },
                                       {
                                         "name": "缓存行",
                                         "description": "缓存与内存之间一次传输的最小数据块单位，通常为64字节。"
                                       },
                                       {
                                         "name": "转译后备缓冲区",
                                         "description": "CPU内部专门用于缓存近期使用过的页表条目的高速缓存，简称TLB。"
                                       }
                                     ],
                                     "knowledgeRelations": [
                                       {
                                         "relation": "PREREQUISITE_OF",
                                         "sourceKnowledgePoint": "局部性原理",
                                         "targetKnowledgePoint": "缓存"
                                       },
                                       {
                                         "relation": "APPLIED_IN",
                                         "sourceKnowledgePoint": "时间局部性",
                                         "targetKnowledgePoint": "缓存"
                                       },
                                       {
                                         "relation": "APPLIED_IN",
                                         "sourceKnowledgePoint": "空间局部性",
                                         "targetKnowledgePoint": "缓存行"
                                       },
                                       {
                                         "relation": "IS_A",
                                         "sourceKnowledgePoint": "时间局部性",
                                         "targetKnowledgePoint": "局部性原理"
                                       },
                                       {
                                         "relation": "IS_A",
                                         "sourceKnowledgePoint": "空间局部性",
                                         "targetKnowledgePoint": "局部性原理"
                                       },
                                       {
                                         "relation": "APPLIED_IN",
                                         "sourceKnowledgePoint": "局部性原理",
                                         "targetKnowledgePoint": "页表"
                                       },
                                       {
                                         "relation": "IS_A",
                                         "sourceKnowledgePoint": "转译后备缓冲区",
                                         "targetKnowledgePoint": "缓存"
                                       },
                                       {
                                         "relation": "APPLIED_IN",
                                         "sourceKnowledgePoint": "转译后备缓冲区",
                                         "targetKnowledgePoint": "页表"
                                       },
                                       {
                                         "relation": "RELATED_TO",
                                         "sourceKnowledgePoint": "缓存",
                                         "targetKnowledgePoint": "页表"
                                       },
                                       {
                                         "relation": "DIFFERENTIATES_FROM",
                                         "sourceKnowledgePoint": "缓存命中",
                                         "targetKnowledgePoint": "缓存未命中"
                                       }
                                     ]
                                   }
                """;
        KnowledgeExtractionResult result = LENIENT_MAPPER.readValue(json1, KnowledgeExtractionResult.class);
        knowledgeGraphService.saveKnowledgePoints(result, document1);

        Document doc2 = new Document("""
                虚拟内存：计算机的“内存魔术师”
                在现代计算机的复杂体系中，虚拟内存是一项革命性的技术，它巧妙地化解了物理内存（RAM）容量有限与多任务处理需求无限之间的矛盾。简单来说，虚拟内存是一种内存管理技术，它为每个运行的程序创造了一个独立的、连续的、巨大的“虚拟”地址空间，而程序本身并不需要关心这些数据实际存放在物理内存的哪个角落，甚至无需关心它是否真的存在于物理内存中。这项技术使得系统能够运行远超物理内存容量的大型程序，并高效地支持多任务并发执行。
                
                要理解虚拟内存，我们需要首先引入几个核心概念。
                
                物理地址与虚拟地址
                物理地址是数据在真实物理内存芯片（RAM）中的实际位置编号，是硬件层面的寻址依据。而虚拟地址则是程序“看到”的地址空间，是操作系统为每个进程（运行中的程序）提供的逻辑视图。从程序的角度看，它拥有从0到最大地址的完整、连续的内存空间，可以随心所欲地使用。然而，这个虚拟地址并不能直接访问物理内存，它需要通过一个“翻译官”进行转换，这个翻译官就是内存管理单元（MMU），它是CPU内部的一个硬件组件。
                
                页与页表
                虚拟内存将虚拟地址空间和物理地址空间都划分为大小固定的块。虚拟地址空间的块被称为页（Page），物理地址空间的块被称为页框（Page Frame），两者大小通常一致（例如4KB）。页和页框的映射关系记录在一个核心数据结构——页表（Page Table）中。页表好比一张巨大的地图，操作系统为每个进程维护一张页表，其中记录了该进程的每一个虚拟页被映射到了哪个物理页框，或者是否被映射。当CPU执行一条访问内存的指令时，MMU会先根据虚拟地址在页表中查找对应的物理地址。如果找到了有效的映射，则地址转换成功，CPU可以顺利读写数据。
                
                页错误（缺页中断）
                如果MMU在页表中没有找到虚拟页对应的物理页框，或者发现该页没有“有效”的映射（例如该页被标记为不在内存中），就会触发一个称为页错误（Page Fault）或缺页中断的异常。此时，控制权从硬件交还给操作系统内核。操作系统收到这个中断后，会执行一系列操作来处理它：
                
                它会在物理内存中寻找一个空闲的页框。
                
                如果找到了，就将该虚拟页所需的数据从磁盘（硬盘或固态硬盘）上的交换文件（Swap File）或页面文件（Paging File）中读入这个空闲页框。
                
                如果没有空闲页框，操作系统就必须选择一个已存在的页框“牺牲”掉，将其内容写回磁盘（如果内容已被修改），腾出空间给新的页面。这个“牺牲”策略的选择至关重要，常用的算法有最近最少使用（LRU）算法等。
                
                将新的物理页框号填入页表，并更新页表项的状态为“有效”。
                
                最后，重新执行触发缺页中断的那条指令，这次MMU能够成功完成地址转换，程序得以继续运行。
                
                这个过程对于程序是完全透明的，程序只感觉到一次短暂、不易察觉的停顿。
                
                换页与抖动
                这种在内存和磁盘之间交换页面的行为被称为换页（Paging）或交换（Swapping）。由于磁盘的读写速度比物理内存慢几个数量级，频繁的换页会严重拖慢系统性能。当系统物理内存严重不足，导致操作系统花费大量时间在换页上，而不是执行真正的计算任务时，系统就陷入了抖动（Thrashing）状态。此时，CPU利用率极低，磁盘指示灯常亮，系统响应变得极其缓慢，几乎停滞。
                
                TLB：加速地址转换
                由于页表存放在内存中，意味着每次内存访问都需要先查一次页表（一次内存访问），再进行实际数据访问（又一次内存访问），这无疑会使性能大幅下降。为了克服这一问题，CPU引入了一个小而快的高速缓存，即转换检测缓冲区（Translation Lookaside Buffer，TLB），也称为快表。TLB专门用于存储近期最常使用的虚拟地址到物理地址的转换关系。当MMU需要进行地址转换时，它会首先在TLB中查找。如果命中（找到匹配项），则可以极快地完成转换，绕过页表查询，从而极大地加速了内存访问。
                
                综上所述，虚拟内存是一个由操作系统、硬件（MMU、TLB）和磁盘共同协作实现的精妙系统。它通过将有限的物理内存抽象为巨大的虚拟地址空间，并利用“按需换页”的技术，不仅简化了程序开发者的工作（无需手动管理内存覆盖），更让计算机能够同时运行大量程序，极大地挖掘了硬件资源的潜力。尽管它带来的性能开销（尤其是缺页中断处理）是系统设计者需要不断优化的课题，但不可否认，虚拟内存是现代多任务操作系统不可或缺的基石，是计算机科学中“以时间换空间”和“抽象分层”思想的杰出典范。
                """);

        String json2 = """
                {
                "knowledgePoints": [
                {
                "name": "虚拟内存",
                "description": "一种内存管理技术，为每个程序提供独立的虚拟地址空间，使系统能运行远超物理内存容量的程序。"
                },
                {
                "name": "物理地址",
                "description": "数据在物理内存芯片中的实际位置编号，是硬件寻址的依据。"
                },
                {
                "name": "虚拟地址",
                "description": "操作系统为进程提供的逻辑地址空间视图，程序所见到的地址。"
                },
                {
                "name": "内存管理单元",
                "description": "CPU内部负责将虚拟地址转换为物理地址的硬件组件，简称MMU。"
                },
                {
                "name": "页",
                "description": "虚拟地址空间中被划分为大小固定的块。"
                },
                {
                "name": "页框",
                "description": "物理地址空间中被划分为大小固定的块，与页大小一致。"
                },
                {
                "name": "页表",
                "description": "记录虚拟页与物理页框映射关系的核心数据结构，每个进程维护一张。"
                },
                {
                "name": "页错误",
                "description": "当MMU无法在页表中找到有效映射时触发的异常，也称缺页中断。"
                },
                {
                "name": "交换文件",
                "description": "磁盘上用于暂存被换出内存的页面数据的文件。"
                },
                {
                "name": "最近最少使用算法",
                "description": "一种页面置换策略，选择最久未被访问的页框作为牺牲对象，简称LRU。"
                },
                {
                "name": "换页",
                "description": "在物理内存与磁盘之间交换页面的行为，也称交换。"
                },
                {
                "name": "抖动",
                "description": "因物理内存严重不足，系统花费大量时间在换页上导致性能急剧下降的状态。"
                },
                {
                "name": "转换检测缓冲区",
                "description": "CPU内用于缓存近期虚拟地址到物理地址转换关系的高速缓存，简称TLB或快表。"
                }
                ],
                "knowledgeRelations": [
                {
                "relation": "PREREQUISITE_OF",
                "sourceKnowledgePoint": "虚拟地址",
                "targetKnowledgePoint": "内存管理单元"
                },
                {
                "relation": "PREREQUISITE_OF",
                "sourceKnowledgePoint": "物理地址",
                "targetKnowledgePoint": "内存管理单元"
                },
                {
                "relation": "HAS_A",
                "sourceKnowledgePoint": "虚拟内存",
                "targetKnowledgePoint": "页"
                },
                {
                "relation": "HAS_A",
                "sourceKnowledgePoint": "虚拟内存",
                "targetKnowledgePoint": "页框"
                },
                {
                "relation": "HAS_A",
                "sourceKnowledgePoint": "虚拟内存",
                "targetKnowledgePoint": "页表"
                },
                {
                "relation": "IS_A",
                "sourceKnowledgePoint": "页错误",
                "targetKnowledgePoint": "页"
                },
                {
                "relation": "PREREQUISITE_OF",
                "sourceKnowledgePoint": "页表",
                "targetKnowledgePoint": "内存管理单元"
                },
                {
                "relation": "PREREQUISITE_OF",
                "sourceKnowledgePoint": "页错误",
                "targetKnowledgePoint": "换页"
                },
                {
                "relation": "APPLIED_IN",
                "sourceKnowledgePoint": "最近最少使用算法",
                "targetKnowledgePoint": "换页"
                },
                {
                "relation": "HAS_A",
                "sourceKnowledgePoint": "换页",
                "targetKnowledgePoint": "交换文件"
                },
                {
                "relation": "IS_A",
                "sourceKnowledgePoint": "抖动",
                "targetKnowledgePoint": "换页"
                },
                {
                "relation": "APPLIED_IN",
                "sourceKnowledgePoint": "转换检测缓冲区",
                "targetKnowledgePoint": "内存管理单元"
                },
                {
                "relation": "PREREQUISITE_OF",
                "sourceKnowledgePoint": "转换检测缓冲区",
                "targetKnowledgePoint": "页表"
                },
                {
                "relation": "RELATED_TO",
                "sourceKnowledgePoint": "虚拟内存",
                "targetKnowledgePoint": "内存管理单元"
                }
                ]
                }
                """;
        KnowledgeExtractionResult extractionResult = LENIENT_MAPPER.readValue(json2, KnowledgeExtractionResult.class);
        knowledgeGraphService.saveKnowledgePoints(extractionResult, doc2);
    }

    @Test
    public void testSearchKnowledgePoints() {

        // 测试检索第一个文档中的知识点
        String knowledgeName1 = "缓存";
        String result1 = knowledgeGraphService.retrieveKnowledgePoint(knowledgeName1);
        System.out.println("检索知识点：【" + knowledgeName1 + "】");
        System.out.println(result1);
        System.out.println("========================================\n");

        // 测试检索第二个文档中的知识点
        String knowledgeName2 = "虚拟内存";
        String result2 = knowledgeGraphService.retrieveKnowledgePoint(knowledgeName2);
        System.out.println("检索知识点：【" + knowledgeName2 + "】");
        System.out.println(result2);
        System.out.println("========================================\n");

        // 测试检索不存在的知识点
        String knowledgeName3 = "不存在的知识点";
        String result3 = knowledgeGraphService.retrieveKnowledgePoint(knowledgeName3);
        System.out.println("检索知识点：【" + knowledgeName3 + "】");
        System.out.println(result3);
        System.out.println("========================================\n");

        // 测试检索有多个关系的知识点（如：局部性原理）
        String knowledgeName4 = "局部性原理";
        String result4 = knowledgeGraphService.retrieveKnowledgePoint(knowledgeName4);
        System.out.println("检索知识点：【" + knowledgeName4 + "】");
        System.out.println(result4);
        System.out.println("========================================\n");

        // 测试检索被引用的知识点（如：页表）
        String knowledgeName5 = "页表";
        String result5 = knowledgeGraphService.retrieveKnowledgePoint(knowledgeName5);
        System.out.println("检索知识点：【" + knowledgeName5 + "】");
        System.out.println(result5);
        System.out.println("========================================\n");

        // 验证查询结果不为空
        assertNotNull(result1);
        assertNotNull(result2);
        assertNotNull(result3);
        assertNotNull(result4);
        assertNotNull(result5);

        // 验证存在知识点的结果包含特定内容
        assertTrue(result1.contains("知识点描述:"));
        assertTrue(result1.contains("知识点关系:"));
        assertTrue(result1.contains("缓存"));
        assertTrue(result1.contains("RELATED_TO"));

        // 验证不存在的知识点返回特定信息
        assertTrue(result3.contains("未查询到对应的知识点信息"));
    }
}
