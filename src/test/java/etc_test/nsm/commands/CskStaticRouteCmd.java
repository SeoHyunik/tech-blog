package etc_test.nsm.commands;

import java.util.HashMap;
import java.util.Map;

public enum CskStaticRouteCmd {
  LIST_STATIC_ROUTES("listStaticRoutes", listStaticRoutes()),
  CREATE_STATIC_ROUTE("createStaticRoute", createStaticRoute()),
  DELETE_STATIC_ROUTE("deleteStaticRoute", deleteStaticRoute());

  private final String command;
  private final Map<String, String> params;

  CskStaticRouteCmd(String command, Map<String, String> params) {
    this.command = command;
    this.params = params;
  }

  public String getCommand() {
    return command;
  }

  public Map<String, String> getParams() {
    return params;
  }

  private static Map<String, String> listStaticRoutes() {
    Map<String, String> params = new HashMap<>();
    params.put("zoneid", "e09f2ae7-af8e-43da-8174-48ed3e32645c"); // 필수: Zone ID
    params.put("response", "json");                               // 선택: 응답 형식
    return params;
  }

  private static Map<String, String> createStaticRoute() {
    Map<String, String> params = new HashMap<>();
    params.put("gateway", "192.168.210.43");                          // 필수: Gateway
    params.put("cidr", "192.168.210.41/5");                           // 필수: Cidr
    params.put("networkid", "07bd2925-cb10-4c87-bcca-368ede9646b7");  // 필수: NSM Network ID
    params.put("zoneid", "e09f2ae7-af8e-43da-8174-48ed3e32645c");     // 필수: Zone ID
    params.put("response", "json");                                   // 선택: 응답 형식
    return params;
  }

  private static Map<String, String> deleteStaticRoute() {
    Map<String, String> params = new HashMap<>();
    params.put("id", "60f6217d-d315-4d41-9329-76b9225ac1cf");     // 필수: StaticRoute ID
    params.put("zoneid", "e09f2ae7-af8e-43da-8174-48ed3e32645c"); // 필수: Zone ID
    params.put("response", "json");                               // 선택: 응답 형식
    return params;
  }
}
