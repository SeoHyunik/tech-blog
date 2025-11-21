package etc_test.programmers;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class lv2_01 {

  public static void main(String[] args) {
    System.out.println(solution(
        new int[]{0, 2, 3, 3, 1, 2, 0, 0, 0, 0, 4, 2, 0, 6, 0, 4, 2, 13, 3, 5, 10, 0, 1, 5}, 3, 5));
  }

  public static int solution(int[] players, int m, int k) {
    int answer = 0; // 최소 증설 횟수
    int maxPlayers =
        Arrays.stream(players).max().isPresent() ? Arrays.stream(players).max().getAsInt() : 0;
    int vmReady = maxPlayers / m;
    System.out.println("vmReady: " + vmReady);
    System.out.println("-----------------------------------");
    Map<String, Integer> vmMap = new LinkedHashMap<>();
    for (int i = 1; i <= vmReady; i++) {
      vmMap.put("vm" + i, 0);
    }

    int runningVms = 0;

    for (int player : players) {
      if (runningVms > 0) {
        for (int i = 1; i <= vmReady; i++) {
          String vmKey = "vm" + i;
          if (vmMap.get(vmKey) > 0) {
            vmMap.put(vmKey, vmMap.get(vmKey) - 1);
            if (vmMap.get(vmKey) == 0) {
              runningVms--;
            }
          }
        }
      }
      System.out.println("player: " + player);
      int requiredVms = player / m;
      if (requiredVms > runningVms) {
        System.out.println("requiredVms: " + requiredVms);
        for (int j = 0; j < requiredVms; j++) {
          for (int i = 1; i <= vmReady; i++) {
            String vmKey = "vm" + i;
            if (vmMap.get(vmKey) == 0 && runningVms < requiredVms) {
              vmMap.put(vmKey, k);
              answer++;
              runningVms++;
              break;
            }
          }
        }
      }
      System.out.println("runningVms: " + runningVms);
      System.out.println("vmMap: " + vmMap);
      System.out.println("-----------------------------------");
    }
    System.out.println("answer: " + answer);
    return answer;
  }
}

/*이용자 / VM 수 / 증설 횟수
 * 0 - 0 - 0
 * 2 - 0 - 0
 * 3 - 1 - 1 (vm1 count 5)
 * 3 - 1 - 0 (vm1 count 4)
 * 1 - 1 - 0 (vm1 count 3)
 * 2 - 1 - 0 (vm1 count 2)
 * 0 - 1 - 0 (vm1 count 1)
 * 0 - 0 - 0 (vm1 종료)
 * 0 - 0 - 0
 * 0 - 0 - 0
 * 4 - 1 - 1 (vm2 count 5)
 * 2 - 1 - 0 (vm2 count 4)
 * 0 - 1 - 0 (vm2 count 3)
 * 6 - 2 - 1 (vm3 count 5) (vm2 count 2)
 * 0 - 2 - 0 (vm3 count 4) (vm2 count 1)
 * 4 - 1 - 0 (vm3 count 3) (vm2 종료)
 * 2 - 1 - 0 (vm3 count 2)
 * 13 - 4 - 3 (vm6 count 5) (vm5 count 5) (vm4 count 5) (vm3 count 1)
 * 3 - 3 - 0 (vm6 count 4) (vm5 count 4) (vm4 count 4) (vm3 종료)
 * 5 - 3 - 0 (vm6 count 3) (vm5 count 3) (vm4 count 3)
 * 10 - 3 - 0 (vm6 count 2) (vm5 count 2) (vm4 count 2)
 * 0 - 3 - 0 (vm6 count 1) (vm5 count 1) (vm4 count 1)
 * 1 - 0 - 0 (vm6 종료) (vm5 종료) (vm4 종료)
 * 5 - 1 - 1 (vm7 count 5)
 * */
