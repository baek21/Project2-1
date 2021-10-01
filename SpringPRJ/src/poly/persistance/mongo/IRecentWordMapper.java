package poly.persistance.mongo;

import java.util.List;
import java.util.Map;

public interface IRecentWordMapper {
	/**
	 * 최근 3일 동안 MongoDB에 저장된 사용자의 영어단어 리스트 모두 가져오기
	 * @return 컬렉션에 저장되어 있는 OCR결과 목록
	 */
	public List<Map<String, Object>> getRecentWord(String userId) throws Exception;

}
