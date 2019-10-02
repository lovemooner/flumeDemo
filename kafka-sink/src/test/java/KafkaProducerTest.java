import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Properties;
import java.util.UUID;

public class KafkaProducerTest {


    private static Producer<String, String> kafkaProducer;
    private static String BOOTSTRAP_SERVERS = "192.168.0.111:9092";
    private String TOPIC = "test1";

    @BeforeClass
    public static void setUp() {
        Properties props = new Properties();
        props.put("bootstrap.servers", BOOTSTRAP_SERVERS);
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        kafkaProducer = new KafkaProducer<String, String>(props);
    }

    @Test
    public void produce() {
        String key = "key" + UUID.randomUUID();
        String data = "hello kafka message:" + key;
        kafkaProducer.send(new ProducerRecord<String, String>(TOPIC, key, data));
        System.out.println("producer:" + data);
    }

}
