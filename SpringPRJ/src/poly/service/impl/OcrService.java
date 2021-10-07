package poly.service.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import poly.dto.OcrDTO;
import poly.persistance.mongo.IOcrMapper;
import poly.service.IOcrService;
import poly.util.CmmUtil;

@Service("OcrService")
public class OcrService implements IOcrService {

	@Resource(name = "OcrMapper")
	private IOcrMapper ocrMapper;

	// 로그 파일 생성 및 로그 출력을 위한 log4j 프레임워크의 자바 객체
	private Logger log = Logger.getLogger(this.getClass());

	/**
	 * OCR 및 MondoDB에 결과 저장
	 */
	@Override
	public OcrDTO ReadTextFromImage(OcrDTO pDTO) throws Exception {

		log.info(this.getClass().getName() + ".ReadTextFromImage start!");

		File imageFile = new File(CmmUtil.nvl(pDTO.getSave_file_path()) + "/" + CmmUtil.nvl(pDTO.getSave_file_name()));

		// OCR 기술 사용을 위한 Tesseract 플랫폼 객체 생성
		ITesseract instance = new Tesseract();

		// OCR 분석에 필요한 기준 데이터(이미 각 나라의 언어별로 학습시킨 데이터 위치 폴더)
		// 저장 경로는 물리경로를 사용함(전체경로)
		instance.setDatapath("c:/tess-data");

		// 한국어 학습 데이터 선택(기본 값은 영어)
		// instance.setLanguage("kor"); // 한국어 설정
		instance.setLanguage("eng"); // 영어 설정

		// 이미지 파일로부터 텍스트 읽기
		String ocr_result = instance.doOCR(imageFile);

		// 읽은 문장을 pDTO에 저장하기
		pDTO.setOcr_text(ocr_result);
		
		// 결과 저장할 컬렉션 이름
		String colNm = "OcrResult_" + pDTO.getReg_id();

		// MongoDB에 저장하기
		ocrMapper.insertOcrResult(pDTO, colNm);
		
		// 업로드된 이미지 파일 존재하면
		if (imageFile.exists()) {
			// 삭제
			imageFile.delete();
		}
		

		log.info(this.getClass().getName() + ".ReadTextFromImage end!");

		return pDTO;
	}

	@Override
	public List<OcrDTO> getOcrResultAll(String session_id) throws Exception {

		log.info(this.getClass().getName() + ".getOcrResultAll start!");

		// 조회할 컬렉션 이름
		String colNm = "OcrResult_" + session_id;

		List<OcrDTO> rList = ocrMapper.getOcrResultAll(colNm);
		if (rList == null) {
			rList = new ArrayList<OcrDTO>();

		}

		log.info(this.getClass().getName() + ".getOcrResultAll end!");

		return rList;
	}

	// 삭제하기
	@Override
	public void ocrResultDelete(HashMap<String, String> rMap) throws Exception {

		log.info(this.getClass().getName() + ".ocrResultDelete start!");

		ocrMapper.ocrResultDelete(rMap);

		log.info(this.getClass().getName() + ".ocrResultDelete end!");
	}

	@Override
	public Map<String, String> getImageInfo(String reg_id, String reg_dt) throws Exception {
		
		log.info(this.getClass().getName() + ".getImageInfo start!");
		
		log.info("reg_id : " + reg_id);
		log.info("reg_dt : " + reg_dt);
		
		Map<String, String> rMap = ocrMapper.getImageInfo(reg_id, reg_dt);
		
		log.info("사용자 이미지 정보 : " + rMap);
						
		log.info(this.getClass().getName() + ".getImageInfo end!");
		
		return rMap;
	}

}
