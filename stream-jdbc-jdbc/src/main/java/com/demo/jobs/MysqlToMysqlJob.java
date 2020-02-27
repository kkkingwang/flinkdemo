package com.demo.jobs;

import com.demo.pojo.User;
import com.demo.reader.JobReader;
import com.demo.writer.JobWriter;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class MysqlToMysqlJob {
    public static void main(String[] args) throws Exception {
        //创建运行环境
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        //获取数据源——(自定义数据源)mysql
        DataStreamSource<User> user = env.addSource(new JobReader());
        user.name("从mysql读取数据");
        //数据处理过程
        DataStream<User> subUser = user
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
                }).name("过滤北京的用户");
        //写入数据-处理结果
        subUser.addSink(new JobWriter()).name("写入flink测试库");
        env.execute("从mysql读取数据，数据清洗之后写入mysql数据库");

    }
}
