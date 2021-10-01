package poly.service;

import java.util.List;
import java.util.Map;

public interface ITextAnalysisService {
	
	/**
	 * 자연어 처리
	 * 형태소 분석(명사)
	 * @param 분석할 문장
	 * @return 분석 결과
	 */
	List<String> WordNouns(String text) throws Exception;
	
	/**
	 * 단어별 출현 빈도수 분석
	 * @param 추출된 단어 모음(List)
	 * @return 분석 결과
	 */
	Map<String, Integer> WordCount(List<String> pList) throws Exception;
	
	/**
	 * 분석할 문장의 자연어 처리 및 빈도수 분석 수행
	 * @param 분석할 문장
	 * @return 분석 결과
	 */
	Map<String, Integer> wordAnalysis(String text) throws Exception;
	
	

}
