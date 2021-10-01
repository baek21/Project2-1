package poly.service.impl;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Service;

import poly.dto.GetWordMeanDTO;
import poly.dto.OcrDTO;
import poly.dto.WordMeanDTO;
import poly.persistance.mongo.IWordMeanMapper;
import poly.service.IWordMeanService;
import poly.util.CmmUtil;

@Service("WordMeanService")
public class WordMeanService implements IWordMeanService {

	@Resource(name="WordMeanMapper")
	private IWordMeanMapper wordMeanMapper; // MongoDB에 저장할 Mapper
	
	// 로그 파일 생성 및 로그 출력을 위한 log4j 프레임워크의 자바 객체
	private Logger log = Logger.getLogger(this.getClass());

	/**
	 * JSON 결과를 받아오기 위한 함수 URL을 파라미터로 전달하면, 자동으로 JSON 결과를 String 변수에 저장하고 결과값 반환
	 *
	 * @param 호출URL
	 * @return JSON 결과
	 */
	private String getUrlForJSON(String callUrl) {

		log.info(this.getClass().getName() + ".getUrlForJSON start");
		log.info("Requested URL : " + callUrl);

		StringBuilder sb = new StringBuilder();
		URLConnection urlConn = null;
		InputStreamReader in = null;

		// json 결과값이 저장되는 변수
		String json = "";

		// SSL 적용된 사이트일 경우, 데이터 증명을 위해 사용
		HostnameVerifier allHostsValid = new HostnameVerifier() {
			@Override
			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};
		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

		try {

			// 웹 사이트 접속을 위한 URL 파싱
			URL url = new URL(callUrl);

			// 접속
			urlConn = url.openConnection();

			// 접속하면, 응답을 60초(60 * 1000ms)동안 기다림
			if (urlConn != null) {
				urlConn.setReadTimeout(60 * 1000);
			}
			if (urlConn != null && urlConn.getInputStream() != null) {
				in = new InputStreamReader(urlConn.getInputStream(), Charset.forName("UTF-8"));

				BufferedReader bufferedReader = new BufferedReader(in);

				// 주어진 문자 입력 스트림 inputStream에 대해 기본 크기의 버퍼를 갖는 객체를 생성.
				if (bufferedReader != null) {
					int cp;
					while ((cp = bufferedReader.read()) != -1) {
						sb.append((char) cp);
					}
					bufferedReader.close();
				}

			}
			in.close();
		} catch (Exception e) {
			throw new RuntimeException("Exception URL : " + callUrl, e);
		}

		json = sb.toString(); // json 결과 저장

		log.info(this.getClass().getName() + ".getUrlForJSON end!");

		return json;
	}

	/**
	 * api를 통한 영단어 정보 가져오기(크롤링 하기)
	 */
	@Override
	public boolean getWordMeanInfoFromWEB(OcrDTO rDTO, Map<String, Integer> rMap) throws Exception {

		// 로그 찍기
		log.info(this.getClass().getName() + ".getWordMeanInfoFromWEB start!");

		// 크롤링 결과
		boolean res = false;
		
		if (rDTO == null) {
			rDTO = new OcrDTO();
		}
		
		if(rMap == null) {
			rMap = new HashMap<String, Integer>();
		}
		
		// String 변수의 문자열을 json 형태의 데이터 구조로 변경하기 위한 객체를 메모리에 올림
		JSONParser parser = new JSONParser();
					
		List<WordMeanDTO> pList = new ArrayList<WordMeanDTO>();

		// 추출된 영단어가 저장된 rMap의 값 가져오기
		// 크롤링으로 의미 검색
		Iterator<String> it = rMap.keySet().iterator();
		while (it.hasNext()) { // rMap에서 가져온 단어들 검색하기 위한 반복문

			String key = (String) it.next();
			String frequency = rMap.get(key).toString();
			//String json = getUrlForJSON("http://:5000/wordMeanAPI?search_word=" + key);
			String json = getUrlForJSON("http://127.0.0.1:8001/wordMeanAPI?search_word=" + key);
			log.info("json : " + json);

			// String 변수의 문자열을 json 형태의 데이터 구조로 변경하기 위해 자바 최상위 Object 변화
			Object obj = parser.parse(json);
			
			// 변환된 Object 객체를 json 데이터 구조로 변경
			JSONObject jsonObject = (JSONObject) obj;
			
			String word = CmmUtil.nvl(jsonObject.get("word").toString());
			JSONArray meanArr = (JSONArray) jsonObject.get("mean");
			log.info("word : " + word);
			log.info("mean : " + meanArr);
			
			if (meanArr != null) {
				log.info("mean is not null");
				
				WordMeanDTO pDTO = new WordMeanDTO();
				
				pDTO.setWord(word);
				pDTO.setFrequency(frequency);
				pDTO.setMean(meanArr);
				pDTO.setReg_id(rDTO.getReg_id());
				pDTO.setReg_dt(rDTO.getReg_dt());
				
				pList.add(pDTO);

				pDTO = null;
				meanArr = null;
			}else {
				log.info("mean is null");
			}
			
			// 메모리 정리
			jsonObject=null;
			obj=null;
		}
		
		String colNm = "WordMean_" + rDTO.getReg_id() + "_" + rDTO.getReg_dt() ;// 생성할 컬렉션명
		
		//MongoDB Collection 저장하기
		wordMeanMapper.insertWordMean(pList, colNm);
		
		res = true;
		
		// 메모리 정리
		parser=null;
		it=null;
		pList=null;
		rDTO=null;
		rMap=null;

		// 로그 찍기
		log.info(this.getClass().getName() + ".getWordMeanInfoFromWEB end!");

		return res;
	}
	
	@Override
	public List<GetWordMeanDTO> getWordMeanList(String colNm) throws Exception {
		
		log.info(this.getClass().getName() + ".getWordMeanList start!");
				
		List<GetWordMeanDTO> rList = wordMeanMapper.getWordMeanList(colNm);
		if(rList == null) {
			rList = new ArrayList<GetWordMeanDTO>();
			
		}
		
		log.info(this.getClass().getName() + ".getWordMeanList end!");
		
		return rList;
	}
	
	
}
