package love.moon.flume;


import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.flume.Channel;
import org.apache.flume.Context;
import org.apache.flume.Event;
import org.apache.flume.Transaction;
import org.apache.flume.conf.Configurable;
import org.apache.flume.sink.AbstractSink;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import static org.apache.flume.sink.elasticsearch.ElasticSearchSinkConstants.*;

/**
 * @auther dongnan
 * @date 2019/10/1 0:28
 * @describe
 */
@Slf4j
public class ElasticSearchSink extends AbstractSink implements Configurable {
    private String hostNames;
    private String indexName;
    private String clusterName;
    private int batchSize;
    static TransportClient client;

    public void configure(Context context) {
        hostNames = context.getString(HOSTNAMES);
        indexName = context.getString(INDEX_NAME);
        clusterName = context.getString(CLUSTER_NAME);
        batchSize = context.getInteger("batchSize", 100);
        Preconditions.checkNotNull(batchSize > 0, "batchSize must be a positive number!!");
    }

    @Override
    public void start() {
        log.info("ElasticSearchSink start...");
        String HOST_IP = hostNames.split(":")[0];
        Integer TCP_PORT = Integer.parseInt(hostNames.split(":")[1]);
        log.info("host:{},port:{},clusterName:" + clusterName, HOST_IP, TCP_PORT);
        Settings settings = Settings.builder().put("cluster.name", clusterName).build();
        try {
            TransportAddress transportAddress = new InetSocketTransportAddress(InetAddress.getByName(HOST_IP), TCP_PORT);
            client = new PreBuiltTransportClient(settings).addTransportAddress(transportAddress);
        } catch (UnknownHostException e) {
            log.error(e.getMessage(), e);
        }

    }


    public Status process() {
        log.info("ElasticSearchSink process");
        Status result = Status.READY;
        Channel channel = getChannel();
        Transaction txn = channel.getTransaction();
        txn.begin();
        try {

            for (int i = 0; i < batchSize; i++) {
                Event event = channel.take();
                if (event == null) {
                    log.info("event is null 1");
                    result= Status.BACKOFF;
                    break;
                }
                Map<String, String> head = event.getHeaders();
                for (String key : head.keySet()) {
                    log.info("key:{},value:{}", key, head.get(key));
                }
                Map<String, String> map = new HashMap<String, String>();
                map.put("clientTime", String.valueOf(System.currentTimeMillis()));
                String eventStr=new String(event.getBody());
                if (StringUtils.isEmpty(eventStr)) {
                    log.info("event data is empty");
                    break;
                }
                JSONObject jsonObject= JSONObject.fromObject(eventStr);
               log.info("event:{}",jsonObject.toString());
                map.put("detail", jsonObject.toString());
                IndexRequestBuilder builder = client.prepareIndex(indexName, "test_index").setSource(map);
                IndexResponse response = builder.execute().actionGet();
                log.info("response:{}", response.toString());
            }
            txn.commit();
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            txn.rollback();
            if (t instanceof Error) {
                throw (Error) t;
            }
            return Status.BACKOFF;
        } finally {
            txn.close();
        }
        return result;

    }

    @Override
    public void stop() {
        super.stop();
    }

}
