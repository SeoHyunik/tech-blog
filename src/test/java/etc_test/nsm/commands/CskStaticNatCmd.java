package etc_test.nsm.commands;

import java.util.HashMap;
import java.util.Map;

public enum CskStaticNatCmd {
  LIST_STATIC_NATS("listStaticNat", listStaticNat()),
  ENABLE_STATIC_NAT("enableStaticNat", enableStaticNat()),
  DISABLE_STATIC_NAT("disableStaticNat", enableStaticNat());

  private final String command;
  private final Map<String, String> params;

  CskStaticNatCmd(String command, Map<String, String> params) {
    this.command = command;
    this.params = params;
  }

  public String getCommand() {
    return command;
  }

  public Map<String, String> getParams() {
    return params;
  }

  private static Map<String, String> listStaticNat() {
    Map<String, String> params = new HashMap<>();
    params.put("zoneid", "e09f2ae7-af8e-43da-8174-48ed3e32645c"); // 필수: Zone ID
    params.put("response", "json");                               // 선택: 응답 형식
    return params;
  }

  private static Map<String, String> enableStaticNat() {
    Map<String, String> params = new HashMap<>();
    params.put("ipaddressid", "41b1b21d-28d7-45a6-8125-1e025edeef86");      // 필수: Public IP ID
    params.put("virtualmachineid", "c70b04e1-347a-450c-a2e7-1a16f1d3ecc2"); // 필수: VM ID
    params.put("zoneid", "e09f2ae7-af8e-43da-8174-48ed3e32645c");           // 필수: Zone ID
    params.put("response", "json");                                         // 선택: 응답 형식
    return params;
  }

  private static Map<String, String> disableStaticNat() {
    Map<String, String> params = new HashMap<>();
    params.put("ipaddressid", "41b1b21d-28d7-45a6-8125-1e025edeef86");      // 필수: Public IP ID
    params.put("zoneid", "e09f2ae7-af8e-43da-8174-48ed3e32645c");           // 필수: Zone ID
    params.put("response", "json");                                         // 선택: 응답 형식
    return params;
  }
}
