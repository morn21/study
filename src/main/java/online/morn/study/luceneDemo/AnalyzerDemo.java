package online.morn.study.luceneDemo;

import online.morn.study.luceneDemo.ik.IKAnalyzer4Lucene7;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;

public class AnalyzerDemo {

    private static void doToken(TokenStream ts) throws IOException {
        ts.reset();
        CharTermAttribute cta = ts.getAttribute(CharTermAttribute.class);
        while (ts.incrementToken()) {
            System.out.print(cta.toString() + "|");
        }
        System.out.println();
        ts.end();
        ts.close();
    }

    public static void main(String[] args) throws IOException {
        String etext = "Analysis is one of the main causes of slow indexing. Simply put, the more you analyze the slower analyze the indexing (in most cases).";
        String chineseText = "张三说的确实在理。";
        // String chineseText = "中华人民共和国简称中国。";
        try (StandardAnalyzer ana = new StandardAnalyzer();) {
            System.out.println("标准分词器，英文分词原句：" + etext);
            TokenStream ts = ana.tokenStream("content", etext);
            System.out.println("标准分词器，英文分词效果：");
            doToken(ts);
            ts = ana.tokenStream("content", chineseText);
            System.out.println("标准分词器，中文分词效果：");
            doToken(ts);
        } catch (IOException e) {

        }
        System.out.println("----------------------------------------------------------------------");

        // smart中文分词器
        try (Analyzer smart = new SmartChineseAnalyzer()) {
            TokenStream ts = smart.tokenStream("content", etext);
            System.out.println("smart中文分词器，英文分词效果：");
            doToken(ts);
            ts = smart.tokenStream("content", chineseText);
            System.out.println("smart中文分词器，中文分词效果：");
            doToken(ts);
        }

        System.out.println("----------------------------------------------------------------------");

        // IKAnalyzer 细粒度切分
        try (Analyzer ik = new IKAnalyzer4Lucene7();) {
            TokenStream ts = ik.tokenStream("content", etext);
            System.out.println("IKAnalyzer中文分词器 细粒度切分，英文分词效果：");
            doToken(ts);
            ts = ik.tokenStream("content", chineseText);
            System.out.println("IKAnalyzer中文分词器 细粒度切分，中文分词效果：");
            doToken(ts);
        }

        System.out.println("----------------------------------------------------------------------");

        // IKAnalyzer 智能切分
        try (Analyzer ik = new IKAnalyzer4Lucene7(true);) {
            TokenStream ts = ik.tokenStream("content", etext);
            System.out.println("IKAnalyzer中文分词器 智能切分，英文分词效果：");
            doToken(ts);
            ts = ik.tokenStream("content", chineseText);
            System.out.println("IKAnalyzer中文分词器 智能切分，中文分词效果：");
            doToken(ts);
        }
    }
}
