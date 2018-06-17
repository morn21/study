package online.morn.study.lucene.demo;

import online.morn.study.lucene.demo.ik.IKAnalyzer4Lucene7;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.search.*;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * 读索引Demo
 * @auther Horner 2018/6/2 14:53
 */
public class IndexSearcherDemo implements Closeable {
    private final static String FILE_PITH = "C:/Document/lucene-index";
    private Directory directory;
    private IndexReader indexReader;

    public IndexSearcherDemo() throws IOException {
        //索引存放目录
        this.directory = FSDirectory.open(Paths.get(FILE_PITH));//存放到文件系统中
        //索引读取器
        this.indexReader = DirectoryReader.open(directory);
    }

    public IndexSearcher getIndexSearcher() {
        IndexSearcher indexSearcher = new IndexSearcher(this.indexReader);
        return indexSearcher;
    }

    @Override
    public void close() throws IOException {
        // 使用完毕，关闭、释放资源
        indexReader.close();
        directory.close();
    }

    /**
     * lucene 搜索基本流程示例
     */
    public static void main(String[] args) throws IOException, ParseException, QueryNodeException {
        IndexSearcherDemo indexSearcherDemo = new IndexSearcherDemo();
        IndexSearcher indexSearcher = indexSearcherDemo.getIndexSearcher();//搜索器

        // 查询解析器 最后也会拆解上如下的基本查询
        Analyzer analyzer = new IKAnalyzer4Lucene7(true);//分词器
        QueryParser queryParser = new QueryParser("name", analyzer);//查询语法分析器
        //queryParser.setPhraseSlop(2);//移动因子
        Query query = queryParser.parse("Thinkpad");//通过parse解析输入（分词），生成query对象

        //(1)QueryParser 查询解析器
        //Query query1 = queryParser.parse("(name:\"联想笔记本电脑\" OR simpleIntro:英特尔) AND type:电脑 AND price:999900");
        //Query query2 = queryParser.parse("(\"联想笔记本电脑\" OR simpleIntro:英特尔) AND type:电脑 AND price:999900");// 等同query1 走构建时传入的默认字段
        //Query query3 = queryParser.parse("(\"联想笔记本电脑\" OR simpleIntro:英特尔) AND type:电脑 AND price:[800000 TO 1000000]");//一个错误的示范
        //BooleanQuery bquery = new BooleanQuery.Builder().add(queryParser.parse("(\"联想笔记本电脑\" OR simpleIntro:英特尔) AND type:电脑 "),BooleanClause.Occur.MUST).add(IntPoint.newRangeQuery("price", 800000, 1000000),BooleanClause.Occur.MUST).build();// query3是查不出结果的 该这么查询
        /*
        //(2)MultiFieldQueryParser 多默认字段解析器
        String[] multiDefaultFields = { "name", "type", "simpleIntro" };// 传统查询解析器-多默认字段
        MultiFieldQueryParser multiFieldQueryParser = new MultiFieldQueryParser(multiDefaultFields, analyzer);
        multiFieldQueryParser.setDefaultOperator(QueryParser.Operator.OR);// 设置默认的操作
        Query query4 = multiFieldQueryParser.parse("笔记本电脑 AND price:1999900");
        */
        /*
        //(3)StandardQueryParser 标准解析器
        StandardQueryParser queryParserHelper = new StandardQueryParser(analyzer);
        //queryParserHelper.setMultiFields(CharSequence[] fields);//设置默认字段
        //queryParserHelper.setPhraseSlop(8);
        //Query query = queryParserHelper.parse("a AND b", "defaultField");
        Query query5 = queryParserHelper.parse("(\"联想笔记本电脑\" OR simpleIntro:英特尔) AND type:电脑 AND price:1999900","name");
        */
        doSearch(query,indexSearcher);

        //九种常用的基本查询：
        // 1、词项查询
        Query query1 = new TermQuery(new Term("name", "thinkpad"));
        System.out.println("************** 词项查询 ******************");
        doSearch(query1, indexSearcher);

        // 2、布尔查询
        Query query2 = new TermQuery(new Term("simpleIntro", "英特尔"));
        BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
        booleanQueryBuilder.add(query1, BooleanClause.Occur.SHOULD);//第一个字句需要以或开始
        booleanQueryBuilder.add(query2, BooleanClause.Occur.MUST);
        BooleanQuery booleanQuery = booleanQueryBuilder.build();
        // 可像下一行这样写
        // BooleanQuery booleanQuery = new BooleanQuery.Builder().add(query1, Occur.SHOULD).add(query2, Occur.MUST).build();
        System.out.println("************** 布尔查询 ******************");
        doSearch(booleanQuery, indexSearcher);

        // 3、PhraseQuery 短语查询
        // String name = "ThinkPad X1 Carbon 20KH0009CD/25CD 超极本轻薄笔记本电脑联想";
        PhraseQuery phraseQuery1 = new PhraseQuery("name", "thinkpad", "carbon");
        System.out.println("************** phrase 短语查询  ******************");
        doSearch(phraseQuery1, indexSearcher);
        PhraseQuery phraseQuery2 = new PhraseQuery(1, "name", "thinkpad", "carbon");//1是移动因子
        System.out.println("************** phrase 短语查询  ******************");
        doSearch(phraseQuery2, indexSearcher);

        // slop示例
        PhraseQuery phraseQuery2Slop = new PhraseQuery(3, "name", "carbon", "thinkpad");
        System.out.println("********** phrase slop 短语查询  ***************");
        doSearch(phraseQuery2Slop, indexSearcher);
        PhraseQuery phraseQuery3 = new PhraseQuery("name", "笔记本电脑", "联想");
        System.out.println("************** phrase 短语查询  ******************");
        doSearch(phraseQuery3, indexSearcher);

        // slop示例
        PhraseQuery phraseQuery3Slop = new PhraseQuery(2, "name", "联想", "笔记本电脑");
        System.out.println("************** phrase s 短语查询  ******************");
        doSearch(phraseQuery3Slop, indexSearcher);
        PhraseQuery phraseQuery4 = new PhraseQuery.Builder()
                .add(new Term("name", "笔记本电脑"), 4) // 4、5是这个词的位置，和 0、1等同
                .add(new Term("name", "联想"), 5).build();
        System.out.println("********** phrase Builder 1 短语查询  **************");
        doSearch(phraseQuery4, indexSearcher);

        // 4 MultiPhraseQuery 多重短语查询
        Term[] terms = new Term[2];
        terms[0] = new Term("name", "笔记本");
        terms[1] = new Term("name", "笔记本电脑");
        Term t = new Term("name", "联想");
        MultiPhraseQuery multiPhraseQuery = new MultiPhraseQuery.Builder().add(terms).add(t).build();
        System.out.println("************** multiPhraseQuery 短语查询  ******************");
        doSearch(multiPhraseQuery, indexSearcher);

        // 对比 PhraseQuery在同位置加入多个词 ，同位置的多个词都需匹配，所以查不出。
        PhraseQuery pquery = new PhraseQuery.Builder().add(terms[0], 0).add(terms[1], 0).add(t, 1).build();
        System.out.println("************** multiPhraseQuery  对比 PhraseQuery 短语查询  ******************");
        doSearch(pquery, indexSearcher);

        // 5 SpanNearQuery 临近查询
        SpanTermQuery tq1 = new SpanTermQuery(new Term("name", "thinkpad"));
        SpanTermQuery tq2 = new SpanTermQuery(new Term("name", "carbon"));
        SpanNearQuery spanNearQuery = new SpanNearQuery(new SpanQuery[] { tq1, tq2 }, 1, true);
        System.out.println("************** SpanNearQuery 临近查询  ************");
        doSearch(spanNearQuery, indexSearcher);

        // 下面的例子词是反序的
        SpanNearQuery spanNearQuery2 = new SpanNearQuery(new SpanQuery[] { tq2, tq1 }, 1, true);
        System.out.println("************** SpanNearQuery 临近查询 2 1,true************");
        doSearch(spanNearQuery2, indexSearcher);

        SpanNearQuery spanNearQuery3 = new SpanNearQuery(new SpanQuery[] { tq2, tq1 }, 3, true);//true按顺序匹配
        System.out.println("************** SpanNearQuery 临近查询 3  3, true************");
        doSearch(spanNearQuery3, indexSearcher);

        SpanNearQuery spanNearQuery4 = new SpanNearQuery(new SpanQuery[] { tq2, tq1 }, 3, false);//false可以不按顺序匹配
        System.out.println("************** SpanNearQuery 临近查询 4  3, false************");
        doSearch(spanNearQuery4, indexSearcher);

        // SpanNearQuery 临近查询 gap slop 使用 1
        SpanTermQuery ctq1 = new SpanTermQuery(new Term("name", "张三"));
        SpanTermQuery ctq2 = new SpanTermQuery(new Term("name", "在理"));
        SpanNearQuery.Builder spanNearQueryBuilder = SpanNearQuery.newOrderedNearQuery("name");
        spanNearQueryBuilder.addClause(ctq1).addGap(0).setSlop(2).addClause(ctq2);//跨度0 移动因子2
        System.out.println("************** SpanNearQuery 临近查询  ************");
        doSearch(spanNearQueryBuilder.build(), indexSearcher);

        // SpanNearQuery 临近查询 gap slop 使用 2
        SpanNearQuery.Builder spanNearQueryBuilder2 = SpanNearQuery.newOrderedNearQuery("name");
        spanNearQueryBuilder2.addClause(ctq1).addGap(2).setSlop(0).addClause(ctq2);//跨度2 移动因子0
        System.out.println("************** SpanNearQuery 临近查询  ************");
        doSearch(spanNearQueryBuilder2.build(), indexSearcher);

        // SpanNearQuery 临近查询 gap slop 使用 3
        SpanNearQuery.Builder spanNearQueryBuilder3 = SpanNearQuery.newOrderedNearQuery("name");
        spanNearQueryBuilder3.addClause(ctq1).addGap(1).setSlop(1).addClause(ctq2);
        System.out.println("************** SpanNearQuery 临近查询  ************");
        doSearch(spanNearQueryBuilder3.build(), indexSearcher);

        // 6 TermRangeQuery 词项范围查询
        TermRangeQuery termRangeQuery = TermRangeQuery.newStringRange("name", "carbon", "张三", false, true);
        System.out.println("********** TermRangeQuery 词项范围查询  ***********");
        doSearch(termRangeQuery, indexSearcher);

        // 7 PrefixQuery、WildcardQuery、RegexpQuery 前缀查询、通配符查询、正则查询（效率低）
        // PrefixQuery 前缀查询（查询时会补全为 XXX* 的格式）
        PrefixQuery prefixQuery = new PrefixQuery(new Term("name", "think"));
        System.out.println("********** PrefixQuery 前缀查询  ***********");
        doSearch(prefixQuery, indexSearcher);

        // WildcardQuery 通配符查询
        WildcardQuery wildcardQuery = new WildcardQuery(new Term("name", "think*"));
        System.out.println("********** WildcardQuery 通配符  ***********");
        doSearch(wildcardQuery, indexSearcher);

        // WildcardQuery 通配符查询
        WildcardQuery wildcardQuery2 = new WildcardQuery(new Term("name", "厉害了???"));
        System.out.println("********** WildcardQuery 通配符  ***********");
        doSearch(wildcardQuery2, indexSearcher);

        // RegexpQuery 正则表达式查询
        RegexpQuery regexpQuery = new RegexpQuery(new Term("name", "厉害.{4}"));
        System.out.println("**********RegexpQuery 正则表达式查询***********");
        doSearch(regexpQuery, indexSearcher);

        // 8 FuzzyQuery 模糊查询（不同数据库的like，允许最大两个不同字符的相似匹配）
        FuzzyQuery fuzzyQuery = new FuzzyQuery(new Term("name", "thind"));
        System.out.println("**********FuzzyQuery 模糊查询***********");
        doSearch(fuzzyQuery, indexSearcher);

        // FuzzyQuery 模糊查询
        FuzzyQuery fuzzyQuery2 = new FuzzyQuery(new Term("name", "thinkd"), 2);
        System.out.println("**********FuzzyQuery 模糊查询***********");
        doSearch(fuzzyQuery2, indexSearcher);

        // FuzzyQuery 模糊查询
        FuzzyQuery fuzzyQuery3 = new FuzzyQuery(new Term("name", "thinkpaddd"));
        System.out.println("**********FuzzyQuery 模糊查询***********");
        doSearch(fuzzyQuery3, indexSearcher);

        // FuzzyQuery 模糊查询
        FuzzyQuery fuzzyQuery4 = new FuzzyQuery(new Term("name", "thinkdaddd"));
        System.out.println("**********FuzzyQuery 模糊查询***********");
        doSearch(fuzzyQuery4, indexSearcher);

        // 9 数值查询
        // 精确值查询
        Query exactQuery = IntPoint.newExactQuery("price", 1999900);
        System.out.println("********** pointRangeQuery 数值精确查询  ***********");
        doSearch(exactQuery, indexSearcher);

        // PointRangeQuery 数值范围查询
        Query pointRangeQuery = IntPoint.newRangeQuery("price", 499900,1000000);
        System.out.println("********** pointRangeQuery 数值范围查询  ***********");
        doSearch(pointRangeQuery, indexSearcher);

        // 集合查询
        Query setQuery = IntPoint.newSetQuery("price", 1999900, 1000000,2000000);
        System.out.println("********** pointRangeQuery 数值集合查询  ***********");
        doSearch(setQuery, indexSearcher);

        // 使用完毕，关闭、释放资源
        indexSearcherDemo.close();
    }

    private static void doSearch(Query query, IndexSearcher indexSearcher) throws IOException {
        System.out.println("query:  " + query.toString());//打印输出查询
        TopDocs topDocs = indexSearcher.search(query, 10); // 前10条
        System.out.println("**** 查询结果 ");
        System.out.println("总命中数：" + topDocs.totalHits);//总命中数
        for (ScoreDoc sdoc : topDocs.scoreDocs) {//遍历topN结果的scoreDocs,取出文档id对应的文档信息
            // 根据文档id取存储的文档
            Document hitDoc = indexSearcher.doc(sdoc.doc);
            System.out.println("-------------- docId=" + sdoc.doc + ",score="
                    + sdoc.score);
            // 取文档的字段
            System.out.println("prodId:" + hitDoc.get("prodId"));
            System.out.println("name:" + hitDoc.get("name"));
            System.out.println("simpleIntro:" + hitDoc.get("simpleIntro"));
            System.out.println("price:" + hitDoc.get("price"));
            System.out.println();
        }
    }
}
