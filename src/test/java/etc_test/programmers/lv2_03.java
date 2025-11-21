package etc_test.programmers;

import java.util.ArrayDeque;
import java.util.Arrays;

public class lv2_03 {

  // 상, 하, 좌, 우 이동을 위한 방향 벡터 (row 변화량)        // 방향 순서는 임의지만 일관되게 유지
  static final int[] DR = {-1, 1, 0, 0};                    // 위로 -1, 아래로 +1, 좌·우는 0
  // 상, 하, 좌, 우 이동을 위한 방향 벡터 (col 변화량)
  static final int[] DC = {0, 0, -1, 1};                    // 좌로 -1, 우로 +1, 위·아래는 0

  public static int solution(String[] board) {
    int rows = board.length;                                // 보드의 세로 길이(행 수)
    int cols = board[0].length();                           // 보드의 가로 길이(열 수) — 모든 문자열 길이가 같다고 가정

    char[][] grid = new char[rows][cols];                   // 보드를 문자 2차원 배열로 변환할 버퍼
    int sr = -1, sc = -1;                                   // 시작점 R의 행(sr), 열(sc)
    int gr = -1, gc = -1;                                   // 목표점 G의 행(gr), 열(gc)

    for (int r = 0; r < rows; r++) {                        // 보드 전체를 스캔하는 바깥 루프 (행)
      for (int c = 0; c < cols; c++) {                      // 각 행의 모든 열을 도는 안쪽 루프 (열)
        char ch = board[r].charAt(c);                       // 문자열 board[r]의 c번째 문자를 가져옴
        grid[r][c] = ch;                                    // grid에 복사하여 랜덤 접근을 빠르게 함
        if (ch == 'R') { // 시작점 R의 좌표를 기록
          sr = r;
          sc = c;
        }
        if (ch == 'G') { // 목표점 G의 좌표를 기록
          gr = r;
          gc = c;
        }
      }
    }

    // 안전장치: R 또는 G가 없으면 도달 불가로 간주 (-1 반환)
    if (sr == -1 || sc == -1 || gr == -1 || gc == -1) {     // 입력이 비정상인 경우를 방어
      return -1;                                            // 시작 또는 목표가 없으면 불가능
    }

    int[][] dist = new int[rows][cols];                     // 각 칸까지의 최소 이동(미끄러짐) 횟수를 기록할 배열
    for (int[] d : dist) {
      Arrays.fill(d, -1);                               // -1로 초기화해서 "미방문" 상태를 표기
    }

    ArrayDeque<int[]> q = new ArrayDeque<>();               // BFS 큐: 각 원소는 {행, 열}
    dist[sr][sc] = 0;                                       // 시작점의 최소 이동 횟수는 0
    q.add(new int[]{sr, sc});                               // 시작점을 큐에 넣어 탐색 시작

    while (!q.isEmpty()) {                                  // 큐가 빌 때까지 BFS 반복
      int[] cur = q.poll();                                 // 현재 정지 지점을 하나 꺼냄
      int r = cur[0];                                       // 현재 행
      int c = cur[1];                                       // 현재 열

      if (r == gr && c == gc) {                             // 목표 지점에 도착했다면
        return dist[r][c];                                  // 그때의 이동 횟수가 최소이므로 반환 (BFS 특성)
      }

      for (int d = 0; d < 4; d++) {                         // 4방향(상,하,좌,우)에 대해 시도
        int nr = r;                                         // 다음 정지 지점의 행(초기값: 현재 위치)
        int nc = c;                                         // 다음 정지 지점의 열(초기값: 현재 위치)

        // "미끄러짐" 시뮬레이션: 벽/장애물을 만나기 전까지 계속 전진
        while (true) {                                      // 해당 방향으로 한 칸씩 전진하며 검사
          int tr = nr + DR[d];                              // 후보 위치의 행 (temporary row)
          int tc = nc + DC[d];                              // 후보 위치의 열 (temporary col)

          // 경계를 벗어나거나 장애물('D')이면 현재(nr,nc)에서 멈춰야 함
          if (tr < 0 || tr >= rows || tc < 0 || tc >= cols  // 보드를 벗어나는지 검사
              || grid[tr][tc] == 'D') {                     // 또는 장애물에 부딪히는지 검사
            break;                                          // 더 전진 불가이므로 루프 탈출 → (nr,nc)가 정지 지점
          }
          nr = tr;                                          // 전진이 가능하면 실제 위치를 갱신
          nc = tc;                                          // 열도 동일하게 갱신
        }

        if (dist[nr][nc] == -1) {                           // 이 정지 지점을 아직 방문하지 않았다면
          dist[nr][nc] = dist[r][c] + 1;                    // 현재 지점에서 한 번 더 이동했으므로 +1
          q.add(new int[]{nr, nc});                         // 다음 탐색 대상으로 큐에 삽입
        }
      }
    }

    return -1;                                              // 큐가 빌 때까지도 목표에 도달하지 못하면 불가능
  }

  public static void main(String[] args) {
    String[] board = {
        "...D..R",
        ".D.G...",
        "....D.D",
        "D....D.",
        "..D...."
    };
    System.out.println(solution(board));
  }
}
