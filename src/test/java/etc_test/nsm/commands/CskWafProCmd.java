package etc_test.nsm.commands;

import java.util.HashMap;
import java.util.Map;

public enum CskWafProCmd {
  CREATE_WAFPRO("createWAFPro", createWAFPro()),
  DELETE_WAFPRO("deleteWAFPro", deleteWAFPro());

  private final String command;
  private final Map<String, String> params;

  CskWafProCmd(String command, Map<String, String> params) {
    this.command = command;
    this.params = params;
  }

  public String getCommand() {
    return command;
  }

  public Map<String, String> getParams() {
    return params;
  }

  private static Map<String, String> createWAFPro() {
    Map<String, String> params = new HashMap<>();
    params.put("name", "finTestWafPro02");                            // 필수: WAF Pro 이름
    params.put("networkid", "90243d2b-34b4-46c2-8f69-73f3ae0c6450");  // 필수: 네트워크 ID
    params.put("response", "json");                                   // 선택: 응답 형식
    params.put("spec", "2vcore / 8gb");                               // 필수: 사양
    params.put("type", "public");                                     // 필수: public/private
    params.put("zoneid", "e09f2ae7-af8e-43da-8174-48ed3e32645c");     // 필수: Zone ID
    return params;
  }

  private static Map<String, String> deleteWAFPro() {
    Map<String, String> params = new HashMap<>();
    params.put("id", "834");                                          // 필수: WAF Pro ID
    params.put("response", "json");                                   // 선택: 응답 형식
    return params;
  }
}
