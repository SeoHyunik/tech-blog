package etc_test.nsm;

import etc_test.nsm.commands.CskAsyncJobCmd;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.TreeMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CsGateWay {

  public static void main(String[] args) throws Exception {
    // 테스트 환경 API Key 및 Secret Key 설정
    String cloudstackAPIKey = "IAN6O0gYfcE1GQhScBKvaBy4QLoRC4TDTaFAPuGn4o7FLvmPC8juhOIVJbFbx4WwqpHMqyLGmbT2X7_V6E-Tuw";
    String cloudstackSecretKey = "9CUoaDNxuXA_QqPrlznquWwPwTA3kdMoOS59kVROr1f1079dFqIScLXFiWxrKV2P8Hs2wvnwk_syUtnNFdfryA";

    // 사용할 API 명령어 선택 (예: CREATE_FIREWALL_RULE)
//    CskPortForwardingRuleCmd selectedCommand = CskPortForwardingRuleCmd.DELETE_PORTFORWARDING_RULE;
//    CskFireWallRuleCmd selectedCommand = CskFireWallRuleCmd.LIST_FIREWALL_RULES;
    CskAsyncJobCmd selectedCommand = CskAsyncJobCmd.QUERY_ASYNC_JOB;
//    CskStaticNatCmd selectedCommand = CskStaticNatCmd.LIST_STATIC_NATS;
//    CskStaticRouteCmd selectedCommand = CskStaticRouteCmd.LIST_STATIC_ROUTES;
//    CskWafCmd selectedCommand = CskWafCmd.CREATE_WAF;
//    CskWafProCmd selectedCommand = CskWafProCmd.DELETE_WAFPRO;

    // 요청 파라미터 설정 (Enum에서 가져옴)
    TreeMap<String, String> queryReq = new TreeMap<>(selectedCommand.getParams());
    queryReq.put("command", selectedCommand.getCommand());

    // API 요청 URL 생성 및 curl 출력
    String uri = getSignatureQueryString(queryReq, cloudstackAPIKey, cloudstackSecretKey);
    String apiUrl = "http://localhost/rest/service2/policy/api?" + uri;
    String curlCommand = String.format(
        "curl -ki -X GET -H 'Content-Type:application/json;charset=UTF-8' '%s'", apiUrl);

    log.info("Generated CURL Command:\n{}", curlCommand);
  }

  private static String makeQueryArgs(TreeMap<String, String> req) throws Exception {
    StringBuilder query = new StringBuilder();
    for (Map.Entry<String, String> ent : req.entrySet()) {
      if (ent.getValue() == null) {
        continue; // 🔹 null 값인 경우 QueryString에서 제외
      }

      query.append(query.length() != 0 ? "&" : "");
      query.append(ent.getKey()).append("=");

      query.append(URLEncoder.encode(ent.getValue(), StandardCharsets.UTF_8));
    }
    return query.toString().replace("+", "%20");
  }

  public static String getSignatureQueryString(TreeMap<String, String> req, String apiKey,
      String sKey) throws Exception {
    req.put("apikey", apiKey);
    String queryArgs = makeQueryArgs(req);
    log.info("Query Args: {}", queryArgs);

    String signature = sha1sign(queryArgs.toLowerCase(), sKey);
    log.info("Generated Signature: {}", signature);

    return queryArgs + "&signature=" + URLEncoder.encode(signature, StandardCharsets.UTF_8);
  }

  public static String sha1sign(String str, String sKey) throws Exception {
    Mac sha1mac = Mac.getInstance("HmacSHA1");
    SecretKeySpec signingKey = new SecretKeySpec(sKey.getBytes(), "HmacSHA1");
    sha1mac.init(signingKey);
    byte[] result = sha1mac.doFinal(str.getBytes());
    return Base64.getEncoder().encodeToString(result);
  }
}
