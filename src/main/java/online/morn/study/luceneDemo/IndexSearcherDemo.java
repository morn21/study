package online.morn.study.luceneDemo;

import online.morn.study.luceneDemo.ik.IKAnalyzer4Lucene7;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
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
    public static void main(String[] args) throws IOException, ParseException {
        IndexSearcherDemo indexSearcherDemo = new IndexSearcherDemo();

        Analyzer analyzer = new IKAnalyzer4Lucene7(true);//分词器
        QueryParser queryParser = new QueryParser("title", analyzer);//查询语法分析器
        Query query = queryParser.parse("Thinkpad");//通过parse解析输入（分词），生成query对象
        IndexSearcher indexSearcher = indexSearcherDemo.getIndexSearcher();//搜索器

        //TopDocs topDocs = indexSearcherDemo.getTopDocs(query);
        TopDocs topDocs = indexSearcher.search(query, 10); // 前10条

        System.out.println(topDocs.totalHits);//总命中数
        for (ScoreDoc sdoc : topDocs.scoreDocs) {//遍历topN结果的scoreDocs,取出文档id对应的文档信息
            Document hitDoc = indexSearcher.doc(sdoc.doc);//根据文档id取存储的文档
            System.out.println(hitDoc.get("title"));//取文档的字段
        }
    }
}
