package poly.service;

import java.util.List;
import java.util.Map;

public interface IRecentWordService {
	
	// 최근 3일 동안 추출한 영어 단어 리스트
	List<Map<String, Object>> recentWord(String userId) throws Exception;

}
