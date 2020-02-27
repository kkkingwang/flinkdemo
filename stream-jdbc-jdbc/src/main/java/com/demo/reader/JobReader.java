package com.demo.reader;

import com.demo.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.source.RichSourceFunction;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

@Slf4j
public class JobReader extends RichSourceFunction<User> {
    private Connection connection;
    private PreparedStatement ps = null;
    private static Properties properties;

    private static String driverClass = "database.driver";
    private static String url = "source.url";
    private static String user = "source.user";
    private static String password = "source.password";
    private static String sql = "source.sql";

    /**
     * 初始化配置
     */
    private static void init(){
        properties = new Properties();
        try{
            InputStream inputStream = JobReader.class.getResourceAsStream("/dbConfig.properties");
            properties.load(inputStream);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 用来打开数据库连接，获取数据库配置
     * @param parameters
     * @throws Exception
     */
    @Override
    public void open(Configuration parameters) throws Exception {
        init();
        super.open(parameters);
        String driver = properties.getProperty(driverClass);
        String sourceUrl = properties.getProperty(url);
        String sourceUser = properties.getProperty(user);
        String sourPassword = properties.getProperty(password);
        String sourceSql = properties.getProperty(sql);
        Class.forName(driver);
        connection = DriverManager.getConnection(sourceUrl, sourceUser, sourPassword);
        ps = connection.prepareStatement(sourceSql);
    }

    /**
     * 执行查询语句，获取结果
     * @param sourceContext
     * @throws Exception
     */
    @Override
    public void run(SourceContext<User> sourceContext) throws Exception {
        try{
            ResultSet rs = ps.executeQuery();
            while(rs.next()){
                String id = rs.getString("id");
                String userName = rs.getString("userName");
                String passWord = rs.getString("passWord");
                String trueName = rs.getString("trueName");
                String idCardNo = rs.getString("idCardNo");
                String isValid = rs.getString("isValid");
                String updateTime = rs.getString("updateTime");
                String createTime = rs.getString("createTime");
                User user = new User(id, userName, passWord, trueName, idCardNo, isValid, updateTime, createTime);
                sourceContext.collect(user);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 关闭数据库连接
     */
    @Override
    public void cancel() {
        try{
            super.close();
            if (connection != null) {
                connection.close();
            }
            if (ps != null) {
                ps.close();
            }
        }catch (Exception e){
            log.error("runException:{}", e);
        }
    }
}
