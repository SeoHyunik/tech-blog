package etc_test.nsm.commands;

import java.util.HashMap;
import java.util.Map;

public enum CskWafCmd {
  CREATE_WAF("createWAF", createWAF()),
  DELETE_WAF("deleteWAF", deleteWAF()),
  LIST_WAF_WEBSERVERS("listWAFWebServers", listWAFWebServers()),
  LIST_WAF_WEBSITES("listWAFWebSites", listWAFWebSites());

  private final String command;
  private final Map<String, String> params;

  CskWafCmd(String command, Map<String, String> params) {
    this.command = command;
    this.params = params;
  }

  public String getCommand() {
    return command;
  }

  public Map<String, String> getParams() {
    return params;
  }

  private static Map<String, String> createWAF() {
    Map<String, String> params = new HashMap<>();
    params.put("name", "finTestWaf17");                               // 필수: WAF 이름
    params.put("zoneid", "e09f2ae7-af8e-43da-8174-48ed3e32645c");     // 필수: Zone ID
    params.put("networkid", "32c3e499-196d-4793-9014-112d60f27371");  // 필수: 네트워크 ID
    params.put("waf1consoleport", "5960");                            // 필수: Console Port
    params.put("waf1sshport", "5961");                                // 필수: SSH Port
    params.put("waf1dbport", "5962");                                 // 필수: DB Port
    params.put("type", "public");                                     // 필수: public/private
    params.put("spec", "standard");                                   // 필수: standard/premium
    params.put("managetype", "self");                                 // 필수: self/managed
    params.put("response", "json");                                   // 선택: 응답 형식
    return params;
  }

  private static Map<String, String> deleteWAF() {
    Map<String, String> params = new HashMap<>();
    params.put("id", "4601");                                         // 필수: WAF ID
    params.put("zoneid", "e09f2ae7-af8e-43da-8174-48ed3e32645c");     // 필수: Zone ID
    params.put("response", "json");                                   // 선택: 응답 형식
    return params;
  }

  private static Map<String, String> listWAFWebServers() {
    Map<String, String> params = new HashMap<>();
    params.put("id", "4674");                                         // 필수: WAF ID
    params.put("zoneid", "e09f2ae7-af8e-43da-8174-48ed3e32645c");     // 필수: Zone ID
    params.put("response", "json");                                   // 선택: 응답 형식
    return params;
  }

  private static Map<String, String> listWAFWebSites() {
    Map<String, String> params = new HashMap<>();
    params.put("id", "4674");                                         // 필수: WAF ID
    params.put("zoneid", "e09f2ae7-af8e-43da-8174-48ed3e32645c");     // 필수: Zone ID
    params.put("response", "json");                                   // 선택: 응답 형식
    return params;
  }
}
