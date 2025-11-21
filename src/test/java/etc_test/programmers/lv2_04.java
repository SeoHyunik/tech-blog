package etc_test.programmers;

import java.util.Arrays;
import java.util.PriorityQueue;

public class lv2_04 {

  private static final int CLEANING_MIN = 10;

  public static void main(String[] args) {
    String[][] bookTime = {
        {"15:00", "17:00"},
        {"16:40", "18:20"},
        {"14:20", "15:20"},
        {"14:10", "19:20"},
        {"18:20", "21:20"}
    };
    System.out.println(solution(bookTime));
  }

  public static int solution(String[][] book_time) {
    int answer = 0;
    // 1. 분으로 변환 및 예약 끝 시간에만 + 10
    int[][] bookTimeInMins = convertToMinutes(book_time);
    // 2. 시작 시각 오름차순으로 정렬
    Arrays.sort(bookTimeInMins, (a, b) -> a[0] == b[0] ? a[1] - b[1] : a[0] - b[0]);
    // System.out.println("정렬된 Minutes: " + Arrays.deepToString(bookTimeInMins));
    // 3. 우선순위 큐에 방이 비는 시각들 저장
    PriorityQueue<Integer> pq = new PriorityQueue<>();
    for (int[] time : bookTimeInMins) {
      int start = time[0];
      int end = time[1];
      // 4. 현재 예약 시작 이전에 비는 방은 재사용 -> 제거
      while (!pq.isEmpty() && pq.peek() <= start) {
        pq.poll();
      }
      // 5. 현재 예약을 방에 배정
      pq.offer(end);
      // 6. 방의 수의 최댓값
      answer = Math.max(answer, pq.size());
    }

    return answer;
  }

  private static int[][] convertToMinutes(String[][] bookTime) {
    int[][] bookTimeInMins = new int[bookTime.length][2];
    for (int i = 0; i < bookTime.length; i++) {
      for (int j = 0; j < 2; j++) {
        int h = (bookTime[i][j].charAt(0) - '0') * 10 + bookTime[i][j].charAt(1) - '0';
        int m = (bookTime[i][j].charAt(3) - '0') * 10 + bookTime[i][j].charAt(4) - '0';
        int minutes = h * 60 + m;
        if (j == 1) {// 퇴실 시간이라면 청소시간까지 추가
          minutes += CLEANING_MIN;
        }
        bookTimeInMins[i][j] = minutes;
      }
    }
    return bookTimeInMins;
  }
}
