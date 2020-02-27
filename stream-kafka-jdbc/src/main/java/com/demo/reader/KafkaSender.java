package com.demo.reader;

import com.alibaba.fastjson.JSON;
import com.demo.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.Random;
import java.util.UUID;

@Slf4j
public class KafkaSender {
    private static Properties properties;
    private static final String kafkaTopic = "user";
    private static KafkaProducer<String, String> producer;
    private static Random random;
    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 初始化配置
     */
    public static void init(){
        properties = new Properties();
        try{
            InputStream inputStream = KafkaSender.class.getResourceAsStream("/kafka.properties");
            properties.load(inputStream);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        init();
        producer = new KafkaProducer<String, String>(properties);
        while(true){
            Thread.sleep(100);//发送时间间隔
            sendMessageToKafka();//发送kafka信息
        }
    }

    /**
     * 发送kafka信息
     */
    private static void sendMessageToKafka() {
        random = new Random();
        //有效的非北京用户
        User user1 = new User(UUID.randomUUID().toString().replace("-", ""), "admin"+random.nextInt(10), "123"+random.nextInt(10), "张三", "320256468"+random.nextInt(9), "1", sdf.format(new Date()), sdf.format(new Date()));
        //有效的北京用户
        User user2 = new User(UUID.randomUUID().toString().replace("-", ""), "admin"+random.nextInt(10), "123"+random.nextInt(10), "李四", "110256468"+random.nextInt(9), "1", sdf.format(new Date()), sdf.format(new Date()));
        //无效的用户
        User user3 = new User(UUID.randomUUID().toString().replace("-", ""), "admin"+random.nextInt(10), "123"+random.nextInt(10), "王五", "110256468"+random.nextInt(9), "0", sdf.format(new Date()), sdf.format(new Date()));
        String msgContent1 = JSON.toJSONString(user1);
        String msgContent2 = JSON.toJSONString(user2);
        String msgContent3 = JSON.toJSONString(user3);
        log.info("{}", msgContent1);
        log.info("{}", msgContent2);
        log.info("{}", msgContent3);
        ProducerRecord producerRecord1 = new ProducerRecord<String, String>(kafkaTopic, null, null, msgContent1);
        ProducerRecord producerRecord2 = new ProducerRecord<String, String>(kafkaTopic, null, null, msgContent2);
        ProducerRecord producerRecord3 = new ProducerRecord<String, String>(kafkaTopic, null, null, msgContent3);
        //发送topic
        producer.send(producerRecord1);
        producer.send(producerRecord2);
        producer.send(producerRecord3);
        producer.flush();
    }
}
