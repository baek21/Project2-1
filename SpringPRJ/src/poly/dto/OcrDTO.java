package poly.dto;

public class OcrDTO {
	
	//private String user_id; // 회원아이디
	private String save_file_name; // 서버에 저장된 파일명
	private String save_file_path; // 서버에 저장된 파일 경로
	private String original_file_name; // 원본 파일명
	private String ext; // 원본 파일의 확장자
	private String ocr_text; // 이미지 인식 문자열
	private String reg_id; // 최초등록자
	private String reg_dt; // 최초등록일(시분초)
	private String reg_day; // 최초등록일
	//private String chg_id; // 최근수정자
	//private String chg_dt; // 최근수정일
	public String getSave_file_name() {
		return save_file_name;
	}
	public void setSave_file_name(String save_file_name) {
		this.save_file_name = save_file_name;
	}
	public String getSave_file_path() {
		return save_file_path;
	}
	public void setSave_file_path(String save_file_path) {
		this.save_file_path = save_file_path;
	}
	public String getOriginal_file_name() {
		return original_file_name;
	}
	public void setOriginal_file_name(String original_file_name) {
		this.original_file_name = original_file_name;
	}
	public String getExt() {
		return ext;
	}
	public void setExt(String ext) {
		this.ext = ext;
	}
	public String getOcr_text() {
		return ocr_text;
	}
	public void setOcr_text(String ocr_text) {
		this.ocr_text = ocr_text;
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