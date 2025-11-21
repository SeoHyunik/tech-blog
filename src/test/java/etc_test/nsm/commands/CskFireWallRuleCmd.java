package etc_test.nsm.commands;

import java.util.HashMap;
import java.util.Map;

public enum CskFireWallRuleCmd {
  CREATE_FIREWALL_RULE("createFirewallRule", createFirewallRuleParams()),
  LIST_FIREWALL_RULES("listFirewallRules", listFirewallRulesParams()),
  UPDATE_FIREWALL_RULE("updateEnterpriseSecurityFirewallRule", updateFirewallRulesParams()),
  DELETE_FIREWALL_RULE("deleteFirewallRule", deleteFirewallRuleParams());

  private final String command;
  private final Map<String, String> params;

  CskFireWallRuleCmd(String command, Map<String, String> params) {
    this.command = command;
    this.params = params;
  }

  public String getCommand() {
    return command;
  }

  public Map<String, String> getParams() {
    return params;
  }

  private static Map<String, String> createFirewallRuleParams() {
    Map<String, String> params = new HashMap<>();

    // 필수 파라미터
    params.put("vpcId", "e24e3026-21d7-412f-92c2-24a21b526f73");  // 필수: VPC 식별자
    params.put("action", null);                                   // 필수: 허용 여부
    params.put("protocol", "tcp");                                // 필수: TCP, UDP, ICMP 등
    params.put("startPort", "80");                                // 필수: 시작 포트
    params.put("endPort", "80");                                  // 필수: 끝 포트
    params.put("srcNat", "true");                                 // 필수: 출발지 NAT 여부
    params.put("zoneid", "e09f2ae7-af8e-43da-8174-48ed3e32645c"); // 필수: Zone ID
    // ✅ 다수 출발지 주소 (srcip → srcAddress)
    params.put("srcAddress",
        "172.26.1.165/32%2C172.26.1.166/32%2C172.26.1.167/32%2C172.26.1.168/32");
    // 목적지 주소
    params.put("dstAddress", "www.example.com");
    // ✅ 출발지/목적지 네트워크 → 기존 값 유지
    params.put("srcNetwork", "64c2765a-e729-46b3-ae37-168ebfdeb3a8");
    params.put("dstNetwork", "6e3eef2e-5dd5-42aa-bfac-dbcfe5799eba");
    params.put("virtualipid",
        "9ebd4f3e-928f-48d6-a1b4-e0571165b69e"); // Port Forwarding ID or Static NAT ID
    // 기타 선택 파라미터
    params.put("srcInterface", null);
    params.put("dstInterface", null);
    params.put("schedule", null);
    params.put("comment", "NSM_FW_TEST_MULTIPLE_SRC");
    params.put("response", "json");

    return params;
  }

  private static Map<String, String> listFirewallRulesParams() {
    Map<String, String> params = new HashMap<>();
    params.put("zoneid", "e09f2ae7-af8e-43da-8174-48ed3e32645c"); // 필수: Zone ID
    params.put("vpcId", "e24e3026-21d7-412f-92c2-24a21b526f73");  // 필수: VPC 식별자
    params.put("response", "json");                               // 선택: 응답 형식
    return params;
  }

  private static Map<String, String> updateFirewallRulesParams() {
    Map<String, String> params = new HashMap<>();
    params.put("id", null);                                       // 필수: 방화벽 ID
    params.put("before", null);                                   // 필수: after 있으면 X
    params.put("after", null);                                    // 필수: before 있으면 X
    params.put("zoneid", "e09f2ae7-af8e-43da-8174-48ed3e32645c"); // 필수: Zone ID
    params.put("response", "json");                               // 선택: 응답 형식
    return params;
  }

  private static Map<String, String> deleteFirewallRuleParams() {
    Map<String, String> params = new HashMap<>();
    params.put("id", null);                                       // 필수: 방화벽 ID
    params.put("zoneid", "e09f2ae7-af8e-43da-8174-48ed3e32645c"); // 필수: Zone ID
    params.put("response", "json");                               // 선택: 응답 형식
    return params;
  }
}
