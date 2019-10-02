 import net.sf.json.JSONObject;;
import org.junit.Test;

/**
 * @auther dongnan
 * @date 2019/10/2 2:13
 * @describe
 */
public class JsonTest {

    @Test
    public void test1(){
    String eventStr="{ \"time\": \"2019-10-02T01:44:29+08:00\", \"remote_addr\": \"192.168.0.9\",\"costime\": \"0.018\",\"realtime\": \"0.019\",\"status\": 302,\"x_forwarded\": \"\",\"referer\": \"\",\"request\": \"POST /event HTTP/1.1\",\"upstr_addr\": \"112.80.248.75:80\",\"bytes\":222,\"body\":\"{\\r\\n      \\\"p1\\\": \\\"cc1\\\",\\r\\n      \\\"p2\\\": 1543815741000,,\\r\\n      \\\"p3\\\": \\\"ACTIONA\\\",\\r\\n      \\\"p4\\\": {\\r\\n        \\\"p5\\\": \\\"bc\\\"\\r\\n      }\\r\\n      \\r\\n }\",\"agent\": \"PostmanRuntime/7.17.1\" }";
        JSONObject jsonObject= JSONObject.fromObject(eventStr);
        String bodyStr=jsonObject.getString("body");
        System.out.println(bodyStr);
        JSONObject eventObject= JSONObject.fromObject(bodyStr);
        System.out.println();
    }
}
