package poly.dto;

import org.json.simple.JSONArray;

public class WordMeanDTO {

	//private String ocr_seq; // 이미지 인식 순번
	//private String word_seq; // 영단어 추출 순번
	private String word; // 영단어
	private JSONArray mean; // 영단어 의미{의미 : 형태}
	private String frequency; // 빈도수
	private String reg_id; // 최초등록자
	private String reg_dt; // 최초등록일
	//private String chg_id; // 최근수정자ID
	//private String chg_dt; // 최근수정일
	
	public String getWord() {
		return word;
	}
	public void setWord(String word) {
		this.word = word;
	}
	public JSONArray getMean() {
		return mean;
	}
	public void setMean(JSONArray mean) {
		this.mean = mean;
	}
	public String getFrequency() {
		return frequency;
	}
	public void setFrequency(String frequency) {
		this.frequency = frequency;
	}
	public String getReg_id() {
		return reg_id;
	}
	public void setReg_id(String reg_id) {
		this.reg_id = reg_id;
	}
	public String getReg_dt() {
		return reg_dt;
	}
	public void setReg_dt(String reg_dt) {
		this.reg_dt = reg_dt;
	}	
		
}
