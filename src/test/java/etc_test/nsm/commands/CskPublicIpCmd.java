package etc_test.nsm.commands;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum CskPublicIpCmd {
    ASSOCIATE_IP_ADDRESS("associateIpAddress", associateIpAddressParams()),
    DISASSOCIATE_IP_ADDRESS("disassociateIpAddress", disassociateIpAddressParams()),
    GET_NUM_OF_IPS("getNumOfIps", getNumOfIpsParams()),
    LIST_ENT_PUBLIC_CIDRS("listEntPublicCidrs", listEntPublicCidrsParams()),
    LIST_PUBLIC_IP_ADDRESSES("listPublicIpAddresses", listPublicIpAddressesParams());

    private final String command;
    private final Map<String, String> params;

    CskPublicIpCmd(String command, Map<String, String> params) {
        this.command = command;
        this.params = params;
    }

    private static Map<String, String> associateIpAddressParams() {
        Map<String, String> params = new HashMap<>();

        // 필수 파라미터
        params.put("zoneid", "e09f2ae7-af8e-43da-8174-48ed3e32645c");// 필수: Zone ID

        // 선택 파라미터
        params.put("usageplantype", "XXX");             // 선택: 요금제 정보(hourly / monthly)
        params.put("account", "XXX");                   // 선택: 계정 이름
        params.put("domainid", "XXX");                  // 선택: 도메인 IO
        params.put("networkid", "XXX");                 // 선택: 네트워크 ID

        return params;
    }

    private static Map<String, String> disassociateIpAddressParams() {
        Map<String, String> params = new HashMap<>();
        params.put("id", "XXX");                  // 필수: Public IP ID
        return params;
    }

    private static Map<String, String> getNumOfIpsParams() {
        Map<String, String> params = new HashMap<>();
        params.put("zoneid", "XXX");              // 필수: Zone ID
        return params;
    }

    private static Map<String, String>  listEntPublicCidrsParams() {
        Map<String, String> params = new HashMap<>();
        params.put("zoneid", "XXX");              // 필수: Zone ID
        return params;
    }


    private static Map<String, String> listPublicIpAddressesParams() {
        Map<String, String> params = new HashMap<>();
        params.put("zoneid", "XXX");              // 선택: Zone ID
        params.put("id", null);                   // 선택: Public IP ID

        // NSM API 필터 값
        params.put("cidr", null);       // 선택: cidr
        params.put("publicIpId", null); // 선택: Public IP ID

        return params;
    }
}
