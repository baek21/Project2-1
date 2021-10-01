package poly.util;

import java.io.File;

public class FileUtil {
	
	/**
	 * 현재 날짜를 기준으로 년/월/일 폴더 생성하기
	 * 
	 * @param 파일이 저장되는 ROOT폴더
	 * @return 파일이 저장되기 위해 생성된 전체 폴더 경로
	 */
	public static String mkdirForDate(String uploadDir) {
		
		String path = uploadDir + DateUtil.getDateTime("/yyyy/MM/dd"); // 폴더 경로
		
		File Folder = new File(path);
		
		if(!Folder.exists()) {
			Folder.mkdirs(); // 폴더 생성합니다.
		}
		
		return path;
	}	
	
	/**
	 * 현재 날짜를 기준으로 년/월/일/아이디 폴더 생성하기
	 * 사용자에 따라 폴더 구분하기 위해 만든 함수
	 *
	 * @param path 파일경로
	 * @param userId 사용자 아이디
	 * @return 전체 폴더 경로
	 */
	public static String mkdirForDate(String path, String userId) {
		
		String filePath = path + DateUtil.getDateTime("/yyyy/MM/dd") + "/" + userId; // 폴더 경로
		
		File Folder = new File(filePath);
		
		if(!Folder.exists()) {
			Folder.mkdirs(); // 폴더 생성합니다.
		}
		
		return filePath;
	}
}
