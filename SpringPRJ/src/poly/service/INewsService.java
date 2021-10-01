package poly.service;

import java.util.List;
import java.util.Map;

public interface INewsService {

	//웹상에서 웹 기사 정보 크롤링해서 저장하기
	boolean insertNewsInfo() throws Exception;

	//mongoDB서 웹 기사 정보 가져오기
	Map<String, String> getNewsContentsAll(String area) throws Exception;
	
	//웹 기사 본문 단어의미 크롤링 하기
	boolean getNewsWordMeanFromWEB(String newsArea, Map<String, Integer> rMap) throws Exception;
	
	//웹 기사 분야 리스트 가져오기
	List<Map<String, Object>> getNewsAreaList() throws Exception;
	
	//웹 기사 분야별 영단어 리스트 가져오기
	List<Map<String, Object>> newsWord(String newsArea) throws Exception;
	

}
