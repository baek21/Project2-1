package poly.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

	/**
	 * 날짜, 시간 출력하기
	 * 
	 * @param fm 날짜 출력 형식
	 * @return
	 */
	public static String getDateTime(String fm) {

		Date today = new Date();
		System.out.println(today);

		SimpleDateFormat date = new SimpleDateFormat(fm);

		return date.format(today);

	}

	/**
	 * 날짜, 시간 출력하기
	 * 
	 * @return 기본값은 년.월.일
	 */
	public static String getDateTime() {
		return getDateTime("yyyy.MM.dd");
	}
	
	
	/**
	 * Calendar 출력
	 * 예) i값 0 오늘, 1 내일, -1 어제
	 * @param i
	 * @return
	 */
	public static String getCalendarDate(int i) {
		
		Calendar cal = Calendar.getInstance();
		String format = "yyyyMMdd";
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		cal.add(cal.DATE, i);
		String date = sdf.format(cal.getTime());
				
		return date;
		
	}

}
