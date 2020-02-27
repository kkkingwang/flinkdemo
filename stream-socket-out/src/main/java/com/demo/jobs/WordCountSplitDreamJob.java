package com.demo.jobs;

import com.demo.pojo.WordCount;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 以wordCount为例，分流处理
 */
public class WordCountSplitDreamJob {
    private static Properties properties;
    //侧数据流标识ID
    private static String outputTagId = "rejected";
    //定义侧数据流标识
    private static final OutputTag<WordCount> outputTag = new OutputTag<WordCount>(outputTagId){};
    private static void init() {
        properties = new Properties();
        try {
            InputStream inputStream = WordCountJob.class.getResourceAsStream("/socket.properties");
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        init();
        //设置运行环境
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //获取数据源
        DataStreamSource<String> text = env.socketTextStream(properties.getProperty("host"), Integer.parseInt(properties.getProperty("port")), properties.getProperty("delimiter"));
        //*************************************基于Side-Output分流处理(比Split...Select...更为灵活)*********************************************
        //在ProcessFunction中操作数据，生成主流和侧流
        SingleOutputStreamOperator<WordCount> tokenized = text.process(new Tokennized());

        // 获取侧输出
        DataStream<String> rejectedWords = tokenized
                .getSideOutput(outputTag)
                .map(new MapFunction<WordCount, String>() {
                    @Override
                    public String map(WordCount wordCount) throws Exception {
                        return wordCount.getWord();
                    }
                });
        //主流
        DataStream<WordCount> windowCounts = tokenized
                .keyBy("word")
                .sum("count");

        // 将数据打印到控制台，并设置并行度
        windowCounts.print().name("词频输出");
        rejectedWords.print().name("输出单词hello");
        // 这一行代码一定要实现，否则不执行
        env.execute("从socket读取文本，统计词频");

    }

    /**
     * 自定义规则，分流处理
     */
    private static class Tokennized extends ProcessFunction<String, WordCount> {
        private static final long serialVersionUID = 1L;

        @Override
        public void processElement(String str, Context context, Collector<WordCount> collector) throws Exception {
            //分割输入的内容
            String[] words = str.split("\\s");
            for (String word : words) {
                collector.collect(new WordCount(word, 1L));
            }
            //形成主流和分流
            for (String word : words) {
                //测流，输出特定数据
                if(word.equals("hello")){
                    context.output(outputTag, new WordCount(word, 1L));
                //主流
                }else{
                    collector.collect(new WordCount(word, 1L));
                }
            }
        }
    }
}
