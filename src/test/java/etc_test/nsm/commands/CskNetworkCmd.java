package etc_test.nsm.commands;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum CskNetworkCmd {
    CREATE_NETWORK("createNetwork", createNetworkParams()),
    DELETE_NETWORK("deleteNetwork", deleteNetworkParams()),
    LIST_NETWORKS("listNetworks", listNetworksParams());


    private final String command;
    private final Map<String, String> params;

    CskNetworkCmd(String command, Map<String, String> params) {
        this.command = command;
        this.params = params;
    }

    private static Map<String, String> createNetworkParams() {
        Map<String, String> params = new HashMap<>();

        // 필수 파라미터 (Ipcount 값 없는 경우)
        params.put("name", "XXX");                                      // 필수: 네트워크 이름
        params.put("networkofferingid", null);                          // 필수: 네트워크 Offering ID
        params.put("zoneid", "e09f2ae7-af8e-43da-8174-48ed3e32645c");   // 필수: Zone ID
        params.put("displaytext", "tcp");                               // 필수: displaytext
        params.put("auto_yn", "true");                                  // 필수: 빌링을 청구 관련 정보 (확인 필요)

        // 필수 파라미터 (Ipcount 값 사용하는 경우)
//        params.put("zoneid", "e09f2ae7-af8e-43da-8174-48ed3e32645c");   // 필수: Zone ID
//        params.put("ipcount", "XXX");                                   // 필수: Subnet 내 ip 개수 (32, 64, 128)
//        params.put("displaytext", "XXX");                               // 필수: displaytext
//        params.put("auto_yn", "true");                                  // 필수: 빌링을 청구 관련 정보 (확인 필요)
//
        // 선택 파라미터

        return params;
    }

    private static Map<String, String> deleteNetworkParams() {
        Map<String, String> params = new HashMap<>();
        params.put("id", "XXX");                  // 필수: NETWORK ID
        return params;
    }

    private static Map<String, String> listNetworksParams() {
        Map<String, String> params = new HashMap<>();
        params.put("zoneid", "XXX");              // 선택: Zone ID
        params.put("externalYn", "Y");            // 선택: External NetworkId 정보 추가 여부
        return params;
    }

}
