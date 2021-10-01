package poly.service;

import java.util.List;
import java.util.Map;

public interface IUserTextService {
	
	// 사용자가 입력한 텍스트에서 추출한 단어 의미 검색
	List<Map<String, Object>> getWordMeanFromWeb(Map<String, Integer> rMap) throws Exception;

}
