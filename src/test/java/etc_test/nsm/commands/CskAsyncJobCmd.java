package etc_test.nsm.commands;

import java.util.HashMap;
import java.util.Map;

public enum CskAsyncJobCmd {
  QUERY_ASYNC_JOB("queryAsyncJobResult", queryAsyncJobResult());

  private final String command;
  private final Map<String, String> params;

  CskAsyncJobCmd(String command, Map<String, String> params) {
    this.command = command;
    this.params = params;
  }

  public String getCommand() {
    return command;
  }

  public Map<String, String> getParams() {
    return params;
  }

  private static Map<String, String> queryAsyncJobResult() {
    Map<String, String> params = new HashMap<>();
    params.put("jobid", "36f88e976316aac86f2f35d45fbce74f");      // 필수: JOB ID
    params.put("response", "json");                               // 선택: 응답 형식
    return params;
  }
}
