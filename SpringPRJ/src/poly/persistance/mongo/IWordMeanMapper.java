package poly.persistance.mongo;

import java.util.List;

import poly.dto.GetWordMeanDTO;
import poly.dto.WordMeanDTO;

public interface IWordMeanMapper {
	
	/**
	 * MongoDB에 크롤링으로 가져온 영어 단어 데이터 저장하기
	 * @param pList 크롤링으로 가져온 영어 단어 정보
	 * @param ColNm 저장할 컬렉션 이름
	 */
	public boolean insertWordMean(List<WordMeanDTO> pList, String colNm) throws Exception;
	
	
	/**
	  * MongoDB에서 영어 단어 데이터 가져오기
	 * @param colNm 데이터 가져올 컬렉션 이름
	 * @return
	 */
	public List<GetWordMeanDTO> getWordMeanList(String colNm) throws Exception;
}
