package etc_test.nsm.commands;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum CskAccountCmd {
    NSM_CREATE_ACCOUNT("NSMcreateAccount", nsmCreateAccountParams()),
    NSM_DELETE_ACCOUNT("NSMdeleteAccount", nsmDeleteAccountParams());

    private final String command;
    private final Map<String, String> params;

    CskAccountCmd(String command, Map<String, String> params) {
        this.command = command;
        this.params = params;
    }

    private static Map<String, String> nsmCreateAccountParams() {
        Map<String, String> params = new HashMap<>();
        // 필수 파라미터
        // 선택 파라미터
        return params;
    }

    private static Map<String, String> nsmDeleteAccountParams() {
        Map<String, String> params = new HashMap<>();
        return params;
    }
}
