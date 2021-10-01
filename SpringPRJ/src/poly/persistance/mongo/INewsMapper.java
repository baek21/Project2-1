package poly.persistance.mongo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import poly.dto.NewsInfoDTO;

public interface INewsMapper {

	/**
	 * MongoDB에 웹 기사 데이터 저장하기
	 * @param pDTO 
	 * @param colNm 저장할 컬렉션 이름
	 * @return res = false(실패) / true(성공)
	 */
	public boolean insertNewsInfo(List<NewsInfoDTO> pList) throws Exception;
	
	/**
	 * MongoDB에 웹 기사 데이터 가져오기
	 */
	public Map<String, String> getNewsContentsAll(String area) throws Exception;
	
	/**
	 * 크롤링으로 가져온 웹 기사 본문 영어 단어 뜻 저장하기
	 */
	public boolean insertNewsWordMean(List<HashMap<String, Object>> pList, String colNm) throws Exception;
	
	/**
	 * 수집한 웹 기사 분야 리스트 가져오기
	 */
	public List<Map<String, Object>> getNewsAreaList() throws Exception;
	
	/**
	 * 웹 기사 분야별 영단어 리스트 가져오기
	 */
	public List<Map<String, Object>> newsWord(String newsArea) throws Exception;

}
