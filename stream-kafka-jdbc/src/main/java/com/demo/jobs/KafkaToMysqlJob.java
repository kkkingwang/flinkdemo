package com.demo.jobs;

import com.alibaba.fastjson.JSONObject;
import com.demo.pojo.User;
import com.demo.wrieter.JobWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.serialization.SimpleStringSchema;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010;
import org.apache.flink.util.Collector;

import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class KafkaToMysqlJob {
    private static Properties properties;
    private static String kafkaTopic = "user";

    private static void init(){
        properties = new Properties();
        try{
            InputStream inputStream = KafkaToMysqlJob.class.getResourceAsStream("/kafka.properties");
            properties.load(inputStream);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        init();
        //设置运行环境
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        //获取数据源——kafka
        DataStreamSource<String> user = env.addSource(new FlinkKafkaConsumer010<String>(kafkaTopic, new SimpleStringSchema(), properties));
        user.name("从kafka获取数据");
        user.print();
        //打断算子链
        //user.disableChaining();
        //打断所有算子链
        //env.disableOperatorChaining();
        //数据处理过程
        DataStream<User> subUser = user
                .map(new MapFunction<String, User>() {
                    @Override
                    public User map(String key) throws Exception {
                        User user = JSONObject.parseObject(key, User.class);
                        return user;
                    }
                }).name("JSON串转换为User对象")
                .flatMap(new FlatMapFunction<User, User>() {
                    @Override
                    public void flatMap(User user, Collector<User> collector) throws Exception {
                        String isValid = user.getIsValid();
                        if(StringUtils.isNotBlank(isValid) && isValid.equals("1")){
                            collector.collect(user);
                        }
                    }
                }).name("有效的用户")
                .startNewChain().name("开始新的算子链")
                .filter(new FilterFunction<User>() {
                    @Override
                    public boolean filter(User user) throws Exception {
                        boolean isBJUser = false;
                        String idCardNo = user.getIdCardNo();
                        if(StringUtils.isNotBlank(idCardNo) && !idCardNo.substring(0,2).equals("11")){
                            isBJUser = true;
                        }
                        return isBJUser;
                    }
                }).name("过滤北京的用户")
                .setParallelism(2).name("设置并行度")
                .keyBy(new KeySelector<User, String>() {//根据field分组，相当于mysql的group by
                    @Override
                    public String getKey(User user) throws Exception {
                        String createDate = user.getCreateTime().substring(0,11);
                        return createDate;
                    }
                })
//                .sum("").name("求和")
                ;
        //写入数据-处理结果
        subUser.addSink(new JobWriter()).name("写入flink测试库");
        env.execute("从kafka读取数据，数据清洗之后写入mysql数据库");
    }
}
