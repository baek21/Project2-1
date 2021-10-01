package poly.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import poly.service.ITextAnalysisService;
import poly.util.CmmUtil;

@Service("TextAnalysisService")
public class TextAnalysisService implements ITextAnalysisService {
	
	// 로그 파일 생성 및 로그 출력을 위한 log4j 프레임워크의 자바 객체
	private Logger log = Logger.getLogger(this.getClass());
	
	// 자연어 처리 형태소 분석기인 komoran를 메모리에 올리기 위해 WordAnalysisService 클래스 내 전역 변수로 설정함
	Komoran nlp = null;

	@Override
	public List<String> WordNouns(String text) throws Exception {
		
		log.info(this.getClass().getName() + ".WordNouns start!");
		
		log.info("분석할 문장 : " + text);
		
		// 분석할 문장에 대해 정제 (씉데없는 특수문자 제거)
		String replace_text = text.replaceAll("[^가-힣a-zA-Z0-9]", " ");
		
		log.info("한국어, 영어, 숫자 제어 단어 모두 한칸으로 변환시킨 문장 : " + replace_text);
		
		// 분석할 문장의 앞 뒤에 존재할 수 있는 필요없는 공백제거
		String trim_text = replace_text.trim();
		
		log.info("분석할 문장의 앞 뒤에 존재할 수 있는 필요없는 공백제거 : " + trim_text);
		
		// 형태소 분석 시작(문장 소문자로 변경)
		KomoranResult analyzeResultList = this.nlp.analyze(trim_text.toLowerCase());
		
		// 형태소 분석 결과 가져오기(영어 단어)
		// https://komorandocs.readthedocs.io/ko/latest/firststep/postypes.html
		List<String> rList = analyzeResultList.getMorphesByTags("SL");
		if(rList==null) {
			rList = new ArrayList<String>();
		}
		
		//분석 결과 확인을 위한 로그 찍기
		/*Iterator<String> it = rList.iterator();
		while(it.hasNext()) {
			 // 추출된 단어
			 String word = CmmUtil.nvl(it.next());
			 log.info("word : " + word);
		}*/
		 
		log.info(this.getClass().getName() + ".WordNouns End!");
				
		return rList;
	}

	@Override
	public Map<String, Integer> WordCount(List<String> pList) throws Exception {
		
		log.info(this.getClass().getName() + ".WordCount start!");
		
		if(pList == null) {
			pList = new ArrayList<String>();
		}
		
		// 단어 빈도수(사과, 3) 결과를 저장하기 위해 Map 객체 생성함
		Map<String, Integer> rMap = new HashMap<>();
		
		// List에 존재하는 중복되는 단어들의 중복제거를 위해 Set 데이터타입에 데이터를 저장함
		// rSet 변수는 중복된 데이터가 저장되지 않기 때문에 중복되지 않은 단어만 저장하고 나머지는 자동 삭제됨
		Set<String> rSet = new HashSet<String>(pList);
		
		
		// 중복이 제거된 단어 모음에 빈도수를 구하기 위해 반복문 사용함
		Iterator<String> it = rSet.iterator();		
		while(it.hasNext()) {
			
			// 중복 제거된 단어
			String word = CmmUtil.nvl(it.next());

			// 불필요한 단어 제외
			if(word.length() > 2) {
				
				// 단어가 중복 저장되어 있는 pList로부터 단어의 빈도수 가져오기
				int frequency = Collections.frequency(pList, word);
				
				log.info("word : " + word);
				log.info("frequency : " + frequency);
				
				rMap.put(word, frequency);
			}
		}
		
		log.info(this.getClass().getName() + ".WordCount End!");
		
		return rMap;
	}

	@Override
	public Map<String, Integer> wordAnalysis(String text) throws Exception {
		
		log.info(this.getClass().getName() + ".TextAnalysisService start!");
		
		// MLP 분석 객체 메모리 로딩
		this.nlp = new Komoran(DEFAULT_MODEL.LIGHT); // 학습데이터 경량화 버전(웹서비스에 적합함)
	
		// 문장의 명사를 추출하기 위한 형태소 분석 실행
		List<String> rList = this.WordNouns(text);
		if(rList == null) {
			rList = new ArrayList<String>();
		}
		
		// 추출된 명사 모음(리스트)의 명사 단어별 빈도수 계산
		Map<String, Integer> rMap = this.WordCount(rList);
		if(rMap == null) {
			rMap = new HashMap<String, Integer>();
		}
		
		// 메모리 정리
		rList=null;
		
		log.info(this.getClass().getName() + ".TextAnalysisService End!");
		
		return rMap;
	}

}
