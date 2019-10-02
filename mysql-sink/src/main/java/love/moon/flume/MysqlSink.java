package love.moon.flume;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.flume.Channel;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.Transaction;
import org.apache.flume.conf.Configurable;
import org.apache.flume.sink.AbstractSink;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by yangyibo on 17/1/5.
 */

/**
 * @auther dongnan
 * @date 2019/9/30 23:37
 * @describe
 */
@Slf4j
public class MysqlSink extends AbstractSink implements Configurable {

    private String hostname;
    private String port;
    private String databaseName;
    private String tableName;
    private String user;
    private String password;
    private PreparedStatement preparedStatement;
    private Connection conn;
    private int batchSize;

    public MysqlSink() {
        log.info("MysqlSink start...");
    }

    public void configure(Context context) {
        hostname = context.getString("hostname");
        Preconditions.checkNotNull(hostname, "hostname must be set!!");
        port = context.getString("port");
        Preconditions.checkNotNull(port, "port must be set!!");
        databaseName = context.getString("databaseName");
        Preconditions.checkNotNull(databaseName, "databaseName must be set!!");
        tableName = context.getString("tableName");
        Preconditions.checkNotNull(tableName, "tableName must be set!!");
        user = context.getString("user");
        Preconditions.checkNotNull(user, "user must be set!!");
        password = context.getString("password");
        Preconditions.checkNotNull(password, "password must be set!!");
        batchSize = context.getInteger("batchSize", 100);
        Preconditions.checkNotNull(batchSize > 0, "batchSize must be a positive number!!");
    }

    @Override
    public void start() {
        super.start();
        try {
            //调用Class.forName()方法加载驱动程序
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        String url = "jdbc:mysql://" + hostname + ":" + port + "/" + databaseName;
        //调用DriverManager对象的getConnection()方法，获得一个Connection对象

        try {
            log.info("connect to mysql");
            conn = DriverManager.getConnection(url, user, password);
            conn.setAutoCommit(false);
            //创建一个Statement对象
            preparedStatement = conn.prepareStatement("insert into " + tableName +
                    " (content,create_by) values (?,?)");

        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            System.exit(1);
        }

    }

    @Override
    public void stop() {
        super.stop();
        if (preparedStatement != null) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public Status process() {
        log.info("mysql sink process");
        Status result = Status.READY;
        Channel channel = getChannel();
        Transaction transaction = channel.getTransaction();
        Event event;
        String content;

        List<Info> infos = Lists.newArrayList();
        transaction.begin();
        try {
            for (int i = 0; i < batchSize; i++) {
                event = channel.take();
                if (event != null) {//对事件进行处理
                    content = new String(event.getBody());
                    log.info("handle content:{}",content);
                    if(StringUtils.isNotEmpty(content)) {
                        Info info = new Info();
                        info.setContent(content);
                        info.setCreateBy("test");
                        infos.add(info);
                    }
                } else {
                    result = Status.BACKOFF;
                    break;
                }
            }

            if (infos.size() > 0) {
                preparedStatement.clearBatch();
                for (Info info : infos) {
                    log.info("write to mysql,content:{},createby:{}", info.getContent(), info.getCreateBy());
                    preparedStatement.setString(1, info.getContent());
                    preparedStatement.setString(2, info.getCreateBy());
                    preparedStatement.addBatch();
                }
                preparedStatement.executeBatch();
                conn.commit();
            }
            transaction.commit();
        } catch (Exception e) {
            try {
                transaction.rollback();
            } catch (Exception e2) {
                log.error("Exception in rollback. Rollback might not have been" +
                        "successful.", e2);
            }
            log.error("Failed to commit transaction." +
                    "Transaction rolled back.", e);
            Throwables.propagate(e);
        } finally {
            transaction.close();
        }
        return result;
    }
}
