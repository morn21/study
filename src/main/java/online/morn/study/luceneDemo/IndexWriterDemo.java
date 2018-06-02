package online.morn.study.luceneDemo;

import online.morn.study.luceneDemo.ik.IKAnalyzer4Lucene7;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * 写索引Demo
 * @auther Horner 2018/5/18 0:10
 */
public class IndexWriterDemo implements Closeable {
    private final static String FILE_PITH = "C:/Document/lucene-index";
    private IndexWriter indexWriter;

    public IndexWriterDemo() throws IOException {
        Analyzer analyzer = new IKAnalyzer4Lucene7(true);//分词器
        IndexWriterConfig config = new IndexWriterConfig(analyzer);//索引配置
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);//设置索引库的打开模式：新建、追加、新建或追加
        //索引存放目录
        Directory directory = FSDirectory.open(Paths.get(FILE_PITH));//存放到文件系统中
        //Directory directory = new RAMDirectory();//存放到内存中
        this.indexWriter = new IndexWriter(directory, config);//创建索引写对象
    }

    public void addDocument(Document doc) throws IOException {
        this.indexWriter.addDocument(doc);//将文档添加到索引
        //this.indexWriter.deleteDocuments(terms);//删除文档
        //this.indexWriter.updateDocument(term, doc);//修改文档
        this.indexWriter.flush();//刷新
        this.indexWriter.commit();//提交
        //writer.rollback();//回滚
    }

    public static Field makeFiled(String fieldName, String fieldValue, boolean tokenized){
        FieldType onlyStoredType = new FieldType();
        onlyStoredType.setTokenized(tokenized);//是否分词
        onlyStoredType.setIndexOptions(IndexOptions.DOCS);//索引选项：是否存储 词频、位置、偏移量
        onlyStoredType.setStored(true);//是否存储
        onlyStoredType.freeze();
        return new Field(fieldName, fieldValue, onlyStoredType);
    }

    @Override
    public void close() throws IOException {
        indexWriter.close();//关闭 会自动提交
    }

    public static void main(String[] args) throws IOException {
        IndexWriterDemo indexWriterDemo = new IndexWriterDemo();

        Document doc = new Document();//创建document
        doc.add(IndexWriterDemo.makeFiled("title","我叫小花啦啦啦",true));
        doc.add(IndexWriterDemo.makeFiled("content","这就是最真实的数据啦",true));
        doc.add(IndexWriterDemo.makeFiled("product","太阳能热水器",true));
        indexWriterDemo.addDocument(doc);

        Document doc2 = new Document();//创建document
        doc2.add(IndexWriterDemo.makeFiled("title","我叫小花啦啦啦2",true));
        doc2.add(IndexWriterDemo.makeFiled("content","这就是最真实的数据啦2",true));
        doc2.add(IndexWriterDemo.makeFiled("product","太阳能热水器2",true));
        indexWriterDemo.addDocument(doc2);
    }
}


