package com.demo.jobs;

import com.demo.pojo.WordCount;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;
import org.apache.flink.util.OutputTag;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * socket作为数据源，输出打印
 * windows环境安装netcat快速建立socket
 * 打开命令提示符输入指令:nc -l -p 9000
 */
public class WordCountJob {
    private static Properties properties;
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

        // 获取socket输入的数据
        DataStreamSource<String> text = env.socketTextStream(properties.getProperty("host"), Integer.parseInt(properties.getProperty("port")), properties.getProperty("delimiter"));

        //打断算子链
        //text.disableChaining();
        //打断所有算子链
        //env.disableOperatorChaining();
        //计算数据

        DataStream<WordCount> windowCount = text.flatMap(new FlatMapFunction<String, WordCount>() {
            public void flatMap(String value, Collector<WordCount> out) throws Exception {
                //分割
                String[] splits = value.split("\\s");
                for (String word:splits) {
                    out.collect(new WordCount(word,1L));
                }
            }
        })//打平操作，把每行的单词转为<word,count>类型的数据
                .startNewChain()//开始新的算子链
                .keyBy("word")//针对相同的word数据进行分组
                .timeWindow(Time.seconds(2),Time.seconds(1))//指定计算数据的窗口大小和滑动窗口大小
                .sum("count");
        //把数据打印到控制台
        windowCount.print("stream1").setParallelism(1);//使用一个并行度
        // 这一行代码一定要实现，否则不执行
        env.execute("从socket读取文本，统计词频");
    }
}
