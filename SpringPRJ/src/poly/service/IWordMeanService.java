package poly.service;

import java.util.List;
import java.util.Map;

import poly.dto.GetWordMeanDTO;
import poly.dto.OcrDTO;

public interface IWordMeanService {

	//웹상에서 영단어 정보 가져오기
	boolean getWordMeanInfoFromWEB(OcrDTO rDTO, Map<String, Integer> rMap) throws Exception;
	
	//mongoDB에서 영단어 리스트 가져오기
	List<GetWordMeanDTO> getWordMeanList(String colNm) throws Exception;

}
