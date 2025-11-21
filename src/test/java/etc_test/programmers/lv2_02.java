package etc_test.programmers;

import java.util.Arrays;

public class lv2_02 {

  public static void main(String[] args) {
    int[][] targets = {
        {5, 8}, {1, 3}, {2, 4}, {4, 5}, {7, 10}, {9, 12}, {12, 13}
    };
    System.out.println("최소 요격 미사일 수: " + solution(targets));
  }

  public static int solution(int[][] targets) {
    // 1️⃣ 모든 폭격 미사일 구간을 '끝점(e)' 기준으로 오름차순 정렬
    //    → 빨리 끝나는 미사일부터 처리해야 겹치는 범위를 놓치지 않음
    Arrays.sort(targets, (a, b) -> a[1] == b[1] ? a[0] - b[0] : a[1] - b[1]);

    System.out.println("정렬된 targets:");
    for (int[] t : targets) {
      System.out.println(Arrays.toString(t));
    }

    int shots = 0;              // 발사 횟수 (정답)
    int lastEnd = Integer.MIN_VALUE; // 마지막으로 요격한 미사일의 '끝점' (점은 e-ε로 생각)

    // 2️⃣ 정렬된 구간들을 순서대로 확인
    for (int i = 0; i < targets.length; i++) {
      int s = targets[i][0]; // 시작점
      int e = targets[i][1]; // 끝점

      System.out.printf("%n현재 구간: (s=%d, e=%d)\n", s, e);
      System.out.printf("현재 lastEnd(이전 미사일 끝 기준): %d\n", lastEnd);

      // 3️⃣ 이전에 쏜 요격 미사일이 현재 구간을 커버하는지 확인
      //     (이전 점은 lastEnd - ε 이므로, s < lastEnd 여야 포함)
      if (s >= lastEnd) {
        // 커버되지 않으면 새로 요격 미사일 발사
        shots++;
        lastEnd = e; // 새로운 미사일의 기준 e로 갱신
        System.out.printf("🚀 새로 발사! 총 발사 횟수: %d, lastEnd 갱신 → %d\n", shots, lastEnd);
      } else {
        // 이전 미사일로 커버 가능
        System.out.println("✅ 이전 요격 미사일로 커버됨 (추가 발사 X)");
      }
    }

    // 4️⃣ 최종 결과
    System.out.printf("%n💡 모든 폭격 미사일 요격 완료! 최소 발사 횟수: %d\n", shots);
    return shots;
  }
}
