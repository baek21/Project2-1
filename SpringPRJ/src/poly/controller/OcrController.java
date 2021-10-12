package poly.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import poly.dto.GetWordMeanDTO;
import poly.dto.OcrDTO;
import poly.service.IOcrService;
import poly.service.ITextAnalysisService;
import poly.service.IWordMeanService;
import poly.util.CmmUtil;
import poly.util.DateUtil;
import poly.util.FileUtil;

@Controller
public class OcrController {
	private Logger log = Logger.getLogger(this.getClass());

	/*
	 * 비즈니스 로직(중요 로직을 수행하기 위해 사용되는 서비스를 메모리에 적재(싱글톤패턴 적용됨))
	 */
	@Resource(name = "OcrService")
	private IOcrService ocrService;

	@Resource(name = "TextAnalysisService")
	private ITextAnalysisService textAnalysisService;

	@Resource(name = "WordMeanService")
	private IWordMeanService wordMeanService;

	// 업로드되는 파일이 저장되는 기본 폴더 설정(자바에서 경로는/로 표현함)
	final private String FILE_UPLOAD_SAVE_PATH = "c:/upload"; // c:/upload 폴더에 저장
	final private String FILE_DOWNLOAD_SAVE_PATH = "c:/download"; // c:/download 폴더에서 다운로드
	
	/**
	 * 이미지 인식을 위한 파일업로드 화면 호출
	 */
	@RequestMapping(value = "ocr/Image_Upload")
	public String Image_Upload() {
		log.info(this.getClass().getName() + ".Image_Upload!");

		return "/ocr/Image_Upload";
	}

	/**
	 * 추출 기록 리스트(Ocr결과 목록) 보여주는 페이지
	 */
	@RequestMapping(value = "ocr/OcrResult_List")
	public String OcrResult_List() {
		log.info(this.getClass().getName() + ".OcrResult_List!");

		return "/ocr/OcrResult_List";
	}

	/**
	 * 영어 단어장 목록 보여주는 페이지
	 */
	@RequestMapping(value = "ocr/WordMean_List")
	public String WordMean_List(HttpServletRequest request, HttpServletResponse response, ModelMap model)
			throws Exception {

		log.info(this.getClass().getName() + ".WordMean_List start!");

		String reg_id = CmmUtil.nvl((String) request.getParameter("reg_id")); // 등록자
		String reg_dt = CmmUtil.nvl((String) request.getParameter("reg_dt")); // 등록일

		log.info("웹에서 넘어온 reg_id : " + reg_id);
		log.info("웹에서 넘어온 reg_dt : " + reg_dt);
		
				
		model.addAttribute("reg_id", reg_id);
		model.addAttribute("reg_dt", reg_dt);

		log.info(this.getClass().getName() + ".WordMean_List end!");

		return "/ocr/WordMean_List";
	}
	
	/**
	 * 영어 단어장 이미지 처리 페이지
	 */
	@RequestMapping(value = "ocr/WordMean_Img")
	public void WordMean_Img(HttpServletRequest request, HttpServletResponse response) throws Exception{
		
		log.info(this.getClass().getName() + ".WordMean_Img start!");
		
		// 해당 정보 가져오기
		String reg_id = CmmUtil.nvl((String) request.getParameter("reg_id")); // 등록자
		String reg_dt = CmmUtil.nvl((String) request.getParameter("reg_dt")); // 등록일
		
		// MongoDB에서 사용자 이미지 정보 가져오기
		Map<String, String> rMap = ocrService.getImageInfo(reg_id, reg_dt);
		
		// 이미지 파일 경로
		String imgPath = CmmUtil.nvl((String) rMap.get("save_file_path")) + "/" + CmmUtil.nvl((String) rMap.get("save_file_name"));
		// 이미지 원본 이름
		String imgNm = CmmUtil.nvl((String) rMap.get("original_file_name"));
		
		rMap = null;
		
		log.info(imgPath);
		File fi = new File(imgPath);
		
		log.info(fi);
		
		// 이미지 파일이 존재하면 실행
		if(fi.exists()) {
			
			log.info("이미지 출력 시작");
			
			// 이미지를 jsp에 출력하기
			OutputStream ops = response.getOutputStream();
			FileInputStream fin = new FileInputStream(imgPath);
			
		    int length;
		    byte[] buffer = new byte[10];
		    while((length=fin.read(buffer)) != -1){
		    	ops.write(buffer, 0, length);
		    }
		    
		    log.info("이미지 출력 끝");
		}
		
		log.info(this.getClass().getName() + ".WordMean_Img end!");
		
		
	}

	/**
	 * Ocr결과 및 영어 단어 의미 검색(크롤링)
	 * RequestMethod는 웹에서의 방식과 컨트롤러 방식이 일치해야 오류가 안난다.
	 */
	@RequestMapping(value = "ocr/ReadTextFromImage", method = RequestMethod.POST)
	public String ReadTextFromImage(HttpSession session, HttpServletRequest request, HttpServletResponse response,
			ModelMap model, @RequestParam(value = "fileUpload") MultipartFile mf) throws Exception {

		log.info(this.getClass().getName() + ".ReadTextFromImage start!");

		String result = "";

		// 로그인 아이디
		String user_id = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));

		// 업로드하는 실제 파일명
		// 다운로드 기능 구현시, 임의로 정의된 파일명을 원래대로 만들어주기 위한 목적
		String originalFileName = mf.getOriginalFilename();

		// 파일 확장자 가져오기
		String ext = originalFileName.substring(originalFileName.lastIndexOf(".") + 1, originalFileName.length())
				.toLowerCase();

		OcrDTO pDTO;
		OcrDTO rDTO;
		Map<String, Integer> rMap;
		
		try {
			// 로그인 상태일때 실행
			if(user_id != "") {
				// 이미지 파일만 실행되도록 함
				if (ext.equals("jpeg") || ext.equals("jpg") || ext.equals("gif") || ext.equals("png")) {

					// 웹서버에 저장되는 파일 이름
					// 업로드하는 파일 이름에 한글, 특수 문자들이 저장될 수 있기 때문에 강제로 영어와 숫자로 구성된 파일명으로 변경해서 저장한다.
					// 리눅스나 유닉스 등 운영체제는 다국어 지원에 취약하기 때문이다.
					String saveFileName = DateUtil.getDateTime("HHmmss") + "." + ext;

					// 웹서버에 업로드한 파일 저장하는 물리적 경로
					String saveFilePath = FileUtil.mkdirForDate(FILE_UPLOAD_SAVE_PATH, user_id);
										
					// 업로드 되는 파일을 서버에 저장
					String fullFileInfo = saveFilePath + "/" + saveFileName;
					
					
					//mf.transferTo(new File(fullFileInfo));
					
					// --------------------------
					//윈도우 일때
					FileOutputStream fos = new FileOutputStream(fullFileInfo);
					// 파일 저장할 경로 + 파일명을 파라미터로 넣고 fileOutputStream 객체 생성하고
	                InputStream is = mf.getInputStream();
	                // file로 부터 inputStream을 가져온다.
                	int readCount = 0;
                    byte[] buffer = new byte[1024];
                    // 파일을 읽을 크기 만큼의 buffer를 생성하고
                    // ( 보통 1024, 2048, 4096, 8192 와 같이 배수 형식으로 버퍼의 크기를 잡는 것이 일반적이다.)
                    while ((readCount = is.read(buffer)) != -1) {
                        //  파일에서 가져온 fileInputStream을 설정한 크기 (1024byte) 만큼 읽고
                        
                        fos.write(buffer, 0, readCount);
                        // 위에서 생성한 fileOutputStream 객체에 출력하기를 반복한다
                    }
                    // --------------------------
                    
                    
					pDTO = new OcrDTO();

					pDTO.setSave_file_name(saveFileName);
					pDTO.setSave_file_path(saveFilePath);
					pDTO.setOriginal_file_name(originalFileName);
					pDTO.setExt(ext);
					pDTO.setReg_id(user_id);
					pDTO.setReg_dt(DateUtil.getDateTime("yyyyMMddHHmmss"));

					// 업로드된 이미지로부터 단어 읽기
					// OcrService에서 pDTO.setOcr_text()에 읽은 문장 입력하고,
					// MongoDB에 저장 후 pDTO 반환
					rDTO = ocrService.ReadTextFromImage(pDTO);
					if (rDTO == null) {
						rDTO = new OcrDTO();
					}

					// 읽은 문장에서 단어 추출하기
					rMap = textAnalysisService.wordAnalysis(CmmUtil.nvl(rDTO.getOcr_text()));
					if (rMap == null) {
						rMap = new HashMap<String, Integer>();
					}

					// MongoDB에 저장하기
					wordMeanService.getWordMeanInfoFromWEB(rDTO, rMap);

					log.info(this.getClass().getName() + "몽고DB에 크롤링한 단어 의미 저장 완료");

					// 성공
					result = "0";

				} else {

					// 실패(이미지 파일이 아님)
					result = "1";
				}
			} else {
				// 실패(미로그인)
				result = "2";
			}

		} catch (Exception e) {

			// 예외발생
			result = e.toString();

			log.info(e.toString());

			e.printStackTrace();

		} finally {

			// 메모리 정리
			pDTO = null;
			rDTO = null;
			rMap = null;

			model.addAttribute("ReadResult", result);
			
			log.info(this.getClass().getName() + ".ReadTextFromImage end!");
		}

		return "/ocr/ReadTextFromImage";
	}
	
	/**
	 * Ocr결과 목록 가져오기
	 * 
	 */
	@RequestMapping(value = "ocr/getOcrResultAll", method = RequestMethod.POST)
	@ResponseBody
	public List<OcrDTO> getOcrResultAll(HttpSession session, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		log.info(this.getClass().getName() + ".getOcrResultAll start!");

		String session_id = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID")); // 접속된 아이디

		List<OcrDTO> rList = ocrService.getOcrResultAll(session_id);
		if (rList == null) {
			rList = new ArrayList<OcrDTO>();
		}

		log.info(this.getClass().getName() + ".getOcrResultAll end!");

		return rList;

	}
	
	/**
	 * 영어 단어 목록 가져와서 json 형태로 페이지에 표시하기
	 */
	@RequestMapping(value = "ocr/getWordMeanList", method = RequestMethod.POST)
	@ResponseBody
	public List<GetWordMeanDTO> getWordMeanList(HttpSession session, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		log.info(this.getClass().getName() + ".getWordMeanList start!");
		// 해당 정보 가져오기
		String reg_id = CmmUtil.nvl((String) request.getParameter("reg_id")); // 등록자
		String reg_dt = CmmUtil.nvl((String) request.getParameter("reg_dt")); // 등록일

		log.info("reg_id : " + reg_id);
		log.info("reg_dt : " + reg_dt);

		// 검색할 컬렉션 이름
		String colNm = "WordMean_" + reg_id + "_" + reg_dt;

		List<GetWordMeanDTO> rList = wordMeanService.getWordMeanList(colNm);
		if (rList == null) {
			rList = new ArrayList<GetWordMeanDTO>();
		}

		log.info(this.getClass().getName() + ".getWordMeanList end!");

		return rList;

	}
	

	/**
	 * 영어 단어 목록 삭제
	 */
	@RequestMapping(value = "ocr/OcrResultDelete")
	public String ocrResultDelete(HttpSession session, HttpServletRequest request, HttpServletResponse response,
			ModelMap model) throws Exception {

		log.info(this.getClass().getName() + ".ocrResultDelete start!");

		// 결과
		String result = "";
		
		// 서비스로 넘겨줄 객체
		HashMap<String, String> rMap;

		try {

			// 로그인 아이디
			String user_id = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));
			
			log.info("user_id : " + user_id);

			// 해당 OCR결과 정보 가져오기
			String reg_id = CmmUtil.nvl((String) request.getParameter("reg_id")); // 등록자
			String reg_dt = CmmUtil.nvl((String) request.getParameter("reg_dt")); // 등록일

			log.info("reg_id : " + reg_id);
			log.info("reg_dt : " + reg_dt);

			// 로그인 상태일때 실행
			if (user_id != "") {		
				
				// 아이디와 등록자가 일치할 경우 실행
				if (user_id.equals(reg_id)) {
				
					rMap = new HashMap<>();
					rMap.put("reg_id", reg_id);
					rMap.put("reg_dt", reg_dt);
	
					ocrService.ocrResultDelete(rMap);
	
					// 결과 성공
					result = "0";
					log.info("결과 : 성공");
					
				} else {
					
					// 결과 실패(등록자와 아이디가 일치하지 않음)
					result = "1";
					log.info("결과 : 실패(등록자와 아이디가 일치하지 않음)");
					
				}
				
			} else {
				
				// 결과 실패(미로그인)
				result = "2";
				log.info("결과 : 실패(미로그인)");
			}		
			
		} catch (Exception e) {

			// 예외발생
			result = e.toString();

			log.info(e.toString());

			e.printStackTrace();

		} finally {
			
			rMap = null;			
			model.addAttribute("DeleteResult", result);
		}

		log.info(this.getClass().getName() + ".ocrResultDelete end!");

		return "/ocr/OcrResultDelete";

	}

	/**
	 * 영어 단어 목록 .txt파일 다운로드 기능
	 */
	@RequestMapping(value = "ocr/txtDownLoad")
	public void txtDownLoad(HttpSession session, HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		log.info(this.getClass().getName() + ".txtDownLoad start!");

		// 로그인 아이디
		String user_id = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));

		log.info("user_id : " + user_id);

		// 해당 OCR결과 정보 가져오기
		String reg_id = CmmUtil.nvl((String) request.getParameter("reg_id")); // 등록자
		String reg_dt = CmmUtil.nvl((String) request.getParameter("reg_dt")); // 등록일

		log.info("reg_id : " + reg_id);
		log.info("reg_dt : " + reg_dt);
		
		try {

			// 아이디와 등록자가 일치할 경우 실행
			if (user_id.equals(reg_id)) {
				
				// 검색할 컬렉션 이름
				String colNm = "WordMean_" + reg_id + "_" + reg_dt;
				
				// 텍스트 파일에 입력할 내용
				List<GetWordMeanDTO> rList = wordMeanService.getWordMeanList(colNm);
				if (rList == null) {
					rList = new ArrayList<GetWordMeanDTO>();
				}
								
				// 웹서버에 업로드한 파일 저장하는 물리적 경로
				String saveFilePath = FileUtil.mkdirForDate(FILE_DOWNLOAD_SAVE_PATH, user_id);
				String saveFileExt = ".txt";
				String saveFileName = "Image" + reg_dt + "_English_Word" + saveFileExt;
				String fullFileInfo = saveFilePath + "/" + saveFileName;
								
				log.info("사용자가 다운로드할 텍스트 파일 생성. path : " + fullFileInfo);
				
				// // 텍스트 파일 생성하는 기본 코드
				// ---------------------------------
				// // 파일 객체 생성
				// File file = new File(fullFileInfo);
				// 
				// // true 기존 내용 이어쓰기, false 기존 내용 덮어쓰기
				// FileWriter fw = new FileWriter(file, false);
				// 
				// // 파일 안에 문자열 쓰기
				// fw.write(content);
				// fw.flush();
				// 
				// // 객체 닫기
				// fw.close();
				// ---------------------------------
				
				// 기본 코드와 BufferedWriter 조합
				// 파일 크기가 크면 BufferedWriter와 혼합하여 사용하는 것이 기록 속도가 더 빠르다
				
				log.info("텍스트 파일 내용 쓰기 시작");
				
				// true 기존 내용 이어쓰기, false 기존 내용 덮어쓰기				
				BufferedWriter bw = new BufferedWriter(new FileWriter(fullFileInfo, false));

				// 내용 쓰기
				bw.write("――――――――――――――――――――――――――\n");
				bw.write("빈도수\t단어\t\t형태\t의미");
				
				// GetWordMeanDTO의 Mean은 Object. 형변환해서 입력.
								
				Iterator<GetWordMeanDTO> it = rList.iterator(); 
				while(it.hasNext()) {
					
					GetWordMeanDTO rMap = it.next();
					
					bw.write("\n――――――――――――――――――――――――――\n\n");					
					bw.write(CmmUtil.nvl(rMap.getFrequency()) + "\t" + CmmUtil.nvl(rMap.getWord()) +  "\t\t");
					
					// mean : [{"word_mean": "의미1","word_class": "형태1"}, {"word_mean": "의미2","word_class": "형태2"}]
					// List<Document> 형태
					List<Document> mean = (ArrayList<Document>) rMap.getMean();

					int i = 0;
					while(i < mean.size()) {
												
						//log.info(meanArr);
						//log.info("i : " + i + ",jsonOb.size() = " + jsonOb.size()); 
																		
						String word_class = CmmUtil.nvl((String) mean.get(i).get("word_class").toString());
						String word_mean = CmmUtil.nvl((String) mean.get(i).get("word_mean").toString());
						
						bw.write(word_class + "\t" + word_mean + "\n");
						bw.write("\t\t\t");

						i++;
					}
					
					// 메모리 정리
					rMap = null;
					mean = null;
					
				}
				
				// 메모리 정리
				it = null;
				rList=null;
				
				bw.write("\n――――――――――――――――――――――――――");
				
				// 스트림 비우기(스트림 닫기전에 실행) 
				bw.flush();
				
				// 스트림 닫기
				bw.close();
				
				log.info("텍스트 파일 내용 쓰기 종료");				
				
				// 사용자에게 보내줄 파일
				File file = new File(fullFileInfo);
				// 파일 크기
				long fileLength = file.length();
				
		        response.setHeader("Content-Disposition", "attachment; filename=\"" + saveFileName + "\";");
		        response.setHeader("Content-Transfer-Encoding", "binary"); 
		        response.setHeader("Content-Type",  saveFileExt);
		        response.setHeader("Content-Length", "" + fileLength);
		        response.setHeader("Pragma", "no-cache;");
		        response.setHeader("Expires", "-1;");
				
		        FileInputStream fis = new FileInputStream(fullFileInfo);
		        OutputStream out = response.getOutputStream();
		        int readCount = 0;
	            byte[] buffer = new byte[1024];
	            // 파일 읽을 만큼 크기의 buffer를 생성한 후 
	            while ((readCount = fis.read(buffer)) != -1) {
	                out.write(buffer, 0, readCount);
	                // outputStream에 씌워준다
	            }
	            
			}

		} catch (Exception e) {
			
			log.info(e.toString());

			e.printStackTrace();

		} finally {
			
			
			log.info(this.getClass().getName() + ".txtDownLoad end!");

		}
	}
	

}
