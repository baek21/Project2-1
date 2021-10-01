package poly.persistance.mongo;

import java.util.HashMap;
import java.util.List;

import poly.dto.OcrDTO;

public interface IOcrMapper {
		
	/**
	 * MongoDB에 OCR결과 데이터 저장하기
	 * @param pDTO 
	 * @param colNm 저장할 컬렉션 이름
	 * @return res = false(실패) / true(성공)
	 */
	public boolean insertOcrResult(OcrDTO pDTO, String colNm) throws Exception;
	
	/**
	 * MongoDB에 저장된 OCR결과 데이터 모두 가져오기
	 * @param colNm 가져올 컬렉션 이름
	 * @return 컬렉션에 저장되어 있는 OCR결과 목록
	 */
	public List<OcrDTO> getOcrResultAll(String colNm) throws Exception;

	/**
	 * MongoDB에서 OCR결과 데이터 삭제
	 * @param rMap
	 */
	public void ocrResultDelete(HashMap<String, String> rMap) throws Exception;
	
}
