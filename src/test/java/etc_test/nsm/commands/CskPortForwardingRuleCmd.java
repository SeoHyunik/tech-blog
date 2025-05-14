package etc_test.nsm.commands;

import java.util.HashMap;
import java.util.Map;

public enum CskPortForwardingRuleCmd {
  CREATE_PORTFORWARDING_RULE("createPortForwardingRule", createPortForwardingRuleParams()),
  LIST_PORTFORWARDING_RULES("listPortForwardingRules", listPortForwardingRulesParams()),
  DELETE_PORTFORWARDING_RULE("deletePortForwardingRule", deletePortForwardingRuleParams());

  private final String command;
  private final Map<String, String> params;

  CskPortForwardingRuleCmd(String command, Map<String, String> params) {
    this.command = command;
    this.params = params;
  }

  public String getCommand() {
    return command;
  }

  public Map<String, String> getParams() {
    return params;
  }

  private static Map<String, String> createPortForwardingRuleParams() {
    Map<String, String> params = new HashMap<>();
    params.put("ipaddressid",
        "1db22375-8412-4d52-a997-354a4090c899");                  // 필수: Public IP ID
    params.put("privateport", "37");                              // 필수: 내부 시작 포트
    params.put("publicport", "537");                              // 필수: 외부 시작 포트
    params.put("privateendport", "37");                           // 필수: 내부 종료 포트
    params.put("publicendport", "537");                           // 필수: 외부 종료 포트
    params.put("zoneid", "e09f2ae7-af8e-43da-8174-48ed3e32645c"); // 필수: Zone ID
    params.put("response", "json");                               // 필수: 응답 형식
    params.put("protocol", "tcp");
    params.put("virtualmachineid", "c70b04e1-347a-450c-a2e7-1a16f1d3ecc2");

    return params;
  }


  private static Map<String, String> listPortForwardingRulesParams() {
    Map<String, String> params = new HashMap<>();
    params.put("zoneid", "e09f2ae7-af8e-43da-8174-48ed3e32645c"); // 필수: Zone ID
    params.put("response", "json");                               // 선택: 응답 형식
    return params;
  }

  private static Map<String, String> deletePortForwardingRuleParams() {
    Map<String, String> params = new HashMap<>();
    params.put("id",
        "ea57759f-5e73-4f44-a8c0-eeb6ce5f05a1");                  // 필수: Portforwarding ID
    params.put("zoneid", "e09f2ae7-af8e-43da-8174-48ed3e32645c"); // 필수: Zone ID
    params.put("response", "json");                               // 선택: 응답 형식
    return params;
  }
}
