package etc_test.programmers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class lv2_05 {

  public static void main(String[] args) {
    String[] maps = {"X591X", "X1X5X", "X231X", "1XXX1"};
    System.out.println(Arrays.toString(solution(maps)));
  }

  public static int[] solution(String[] maps) {
    int rowCount = maps.length; // 행 개수
    int colCount = maps[0].length(); // 열 개수

    boolean[][] visited = new boolean[rowCount][colCount]; // 방문 체크

    List<Integer> islandFoods = new ArrayList<>(); // 각 섬의 총 식량(체류 일수) 저장 리스트

    // 상하좌우 방향 정의
    int[] dr = {-1, 1, 0, 0};
    int[] dc = {0, 0, -1, 1};

    // 전체 지도 돌기
    for (int r = 0; r < rowCount; r++) {
      for (int c = 0; c < colCount; c++) {
        // 1. 바다일 땐 skip
        if (maps[r].charAt(c) == 'X') {
          continue;
        }
        // 2. 이미 방문했다면 skip
        if (visited[r][c]) {
          continue;
        }
        // 3. 새로운 섬의 시작점 발견 -> BFS로 이 섬 전체를 탐색
        int foodSum = bfs(maps, visited, r, c, dr, dc);
        // 4. 이 섬의 총 식량 저장
        islandFoods.add(foodSum);
      }
    }

    // edge case: 섬이 없다면 -1 리턴
    if (islandFoods.isEmpty()) {
      return new int[]{-1};
    }

    // 오름차순 정렬 및 반환
    return islandFoods.stream()
        .mapToInt(Integer::intValue)
        .sorted()
        .toArray();
  }

  private static int bfs(String[] maps, boolean[][] visited, int startR, int startC, int[] dr,
      int[] dc) {
    int rowCount = maps.length;
    int colCount = maps[0].length();

    Queue<int[]> q = new LinkedList<>();

    // 시작점 큐에 넣고 방문 처리
    q.offer(new int[]{startR, startC});
    visited[startR][startC] = true;

    int sum = 0; // 해당 섬 전체 식량 합 초기화

    while (!q.isEmpty()) {
      int[] cur = q.poll();
      int r = cur[0];
      int c = cur[1];

      // 현재 칸의 식량 더하기 (이미 X 검사를 했으니 maps[r].charAt[c]는 1~9)
      sum += maps[r].charAt(c) - '0'; // 숫자로 바꿔줘야 함

      // 4방향 탐색
      for (int i = 0; i < 4; i++) {
        int nr = r + dr[i];
        int nc = c + dc[i];

        // 지도 밖으로 나가게 되면 패스
        if (nr < 0 || nr >= rowCount || nc < 0 || nc >= colCount) {
          continue;
        }

        // 이미 방문했으면 패스
        if (visited[nr][nc]) {
          continue;
        }

        // 바다면 패스
        if (maps[nr].charAt(nc) == 'X') {
          continue;
        }

        // 여기까지 왔다면 땅
        visited[nr][nc] = true; // 방문 처리
        q.offer(new int[]{nr, nc}); // 큐에 추가
      }
    }
    return sum;
  }
}
