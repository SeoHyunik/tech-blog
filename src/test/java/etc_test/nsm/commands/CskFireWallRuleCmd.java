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
    params.put("action", null);                                   // 필수: 허용 여부 (allow/deny)
    params.put("protocol", "tcp");                                // 필수: TCP, UDP, ICMP 등
    params.put("startPort", "80");                                // 필수: TCP/UDP일 경우 필수
    params.put("endPort", "80");                                  // 필수: TCP/UDP일 경우 필수
    params.put("srcNat", "true");                                 // 필수: 출발지 NAT 여부 (true/false)
    params.put("zoneid", "e09f2ae7-af8e-43da-8174-48ed3e32645c"); // 필수: Zone ID

    // 선택 파라미터
    params.put("srcAddress", "0.0.0.0/0");                        // 선택: 출발지 주소
    params.put("dstAddress", null);                               // 선택: 목적지 주소
    params.put("portForwardingId",
        "9ebd4f3e-928f-48d6-a1b4-e0571165b69e");                  // 선택: 포트 포워딩 ID
    params.put("staticNatId", null);                              // 선택: Static NAT ID
    params.put("srcNetwork",
        "64c2765a-e729-46b3-ae37-168ebfdeb3a8");                  // 선택: 출발지 네트워크
    params.put("dstNetwork",
        "6e3eef2e-5dd5-42aa-bfac-dbcfe5799eba");                  // 선택: 목적지 네트워크
    params.put("srcInterface", null);                             // 선택: 출발지 인터페이스
    params.put("dstInterface", null);                             // 선택: 목적지 인터페이스
    params.put("schedule", null);                                 // 선택: 방화벽 스케줄
    params.put("comment", "NSM_FW_TEST_SHI_01");                  // 선택: 설명
    params.put("response", "json");                               // 선택: 응답 형식

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
