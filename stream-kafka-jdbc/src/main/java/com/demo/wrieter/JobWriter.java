package com.demo.wrieter;

import com.demo.pojo.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.sink.RichSinkFunction;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.Properties;

@Slf4j
public class JobWriter extends RichSinkFunction<User> {
    private Connection connection;
    private PreparedStatement ps = null;
    private static Properties properties;

    private static String driverClass = "database.driver";
    private static String url = "sink.url";
    private static String user = "sink.user";
    private static String password = "sink.password";
    private static String sql = "sink.sql";

    /**
     * 初始化配置
     */
    private static void init(){
        properties = new Properties();
        try{
            InputStream inputStream = JobWriter.class.getResourceAsStream("/dbConfig.properties");
            properties.load(inputStream);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 初始化数据库连接（open中放入初始化的操作）
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
     * 数据入库
     * @param user
     * @param context
     */
    @Override
    public void invoke(User user, Context context){
        try{
            String id = user.getId();
            String userName = user.getUserName();
            String passWord = user.getPassWord();
            String trueName = user.getTrueName();
            String idCardNo = user.getIdCardNo();
            String isValid = user.getIsValid();
            String updateTime = user.getUpdateTime();
            String createTime = user.getCreateTime();
            ps.setString(1, id);
            ps.setString(2, userName);
            ps.setString(3, passWord);
            ps.setString(4, trueName);
            ps.setString(5, idCardNo);
            ps.setString(6, isValid);
            ps.setString(7, updateTime);
            ps.setString(8, createTime);
            int count = ps.executeUpdate();
            log.info("插入{}条记录。", count);
        }catch (Exception e){
            log.error("RunException:{}"+e);
        }
    }

    /**
     * 关闭数据库连接
     */
    @Override
    public void close(){
        try{
            super.close();
            if (connection != null) {
                connection.close();
            }
            if (ps != null) {
                ps.close();
            }
        }catch (Exception e){
            log.error("RunException:{}"+e);
        }
    }

}
