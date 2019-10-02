import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * @auther dongnan
 * @date 2019/10/1 1:18
 * @describe
 */
@Slf4j
public class ElasticSearchTest {

    static TransportClient client;
    String indexName="test20190918";
    String indexType="test_index";

    @BeforeClass
    public static void setUp() {
        String HOST_IP = "192.168.0.111";
        String clusterName = "my-test-es";
        int TCP_PORT = 9300;
        log.info("host:{},port:{},clusterName:" + clusterName, HOST_IP, TCP_PORT);
        Settings settings = Settings.builder().put("cluster.name", clusterName).build();
        try {
            TransportAddress transportAddress = new InetSocketTransportAddress(InetAddress.getByName(HOST_IP), TCP_PORT);
             client = new PreBuiltTransportClient(settings).addTransportAddress(transportAddress);
            System.out.println();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void saveDateTest1() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", "Tony");
        map.put("clientTime", "2222222");
        map.put("detail","detail test");
        IndexRequestBuilder builder = client.prepareIndex(indexName, indexType).setSource(map);
        IndexResponse response = builder.execute().actionGet();
        log.info("response:{}", response.toString());
        System.out.println("批量创建索引成功");

    }


    @Test
    public void saveDateTest2() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("name", "Tony");
        IndexRequest request = client.prepareIndex(indexName, indexType).setSource(map).request();
        BulkRequestBuilder bulkRequest = client.prepareBulk().add(request); //add

        BulkResponse bulkResponse = bulkRequest.execute().actionGet();
        if (bulkResponse.hasFailures()) {
            System.out.println("批量创建索引错误！");
        }
        client.close();
        System.out.println("批量创建索引成功");

    }
}
