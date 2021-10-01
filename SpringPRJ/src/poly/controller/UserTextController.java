package poly.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import poly.service.ITextAnalysisService;
import poly.service.IUserTextService;
import poly.util.CmmUtil;

@Controller
public class UserTextController {
	private Logger log = Logger.getLogger(this.getClass());

	/*
	 * 비즈니스 로직(중요 로직을 수행하기 위해 사용되는 서비스를 메모리에 적재(싱글톤패턴 적용됨))
	 */
	@Resource(name = "UserTextService")
	private IUserTextService userTextService;

	@Resource(name = "TextAnalysisService")
	private ITextAnalysisService textAnalysisService;

	/**
	 * 사용자가 영어 단어 추출할 텍스트 입력하는 페이지
	 */
	@RequestMapping(value = "text/TextWordSearch")
	public String textWordSearch() {
		log.info(this.getClass().getName() + ".textWordSearch!");

		return "/text/TextWordSearch";
	}

	/**
	 * 사용자가 입력한 텍스트의 영어 단어 의미 검색(크롤링)
	 * RequestMethod는 웹에서의 방식과 컨트롤러 방식이 일치해야 오류가 안난다.
	 */
	@RequestMapping(value = "text/ReadText", method = RequestMethod.POST)
	@ResponseBody
	public List<Map<String, Object>> readText(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {

		log.info(this.getClass().getName() + ".ReadText start!");

		// 사용자가 입력한 텍스트
		String textContents = CmmUtil.nvl((String) request.getParameter("textContents"));
		log.info("textContents : " + textContents);
		
		// 단어 추출 결과 담을 객체
		Map<String, Integer> rMap = new HashMap<String, Integer>();

		// 크롤링 결과 담을 객체
		List<Map<String, Object>> rList = new ArrayList<Map<String, Object>>();

		try {
			
			if(!"".equals(textContents)) {
				// 읽은 문장에서 단어 추출하기
				rMap = textAnalysisService.wordAnalysis(textContents);
				if (rMap == null) {
					rMap = new HashMap<String, Integer>();
				}
				log.info(this.getClass().getName() + "텍스트에서 단어 추출 완료");

				// 크롤링으로 단어 의미 가져오기
				rList = userTextService.getWordMeanFromWeb(rMap);
				if (rList == null) {
					rList = new ArrayList<Map<String, Object>>();
				}

				log.info(this.getClass().getName() + "크롤링으로 단어 의미 가져오기 완료");

			} else {
				
				log.info(this.getClass().getName() + "textContents 값 없음");
				
			}

			
		} catch (Exception e) {

			// 예외발생

			log.info(e.toString());

			e.printStackTrace();

		} finally {

			// 메모리 정리
			rMap = null;

			//model.addAttribute("textContents", textContents);
			//model.addAttribute("rList", rList);

			log.info(this.getClass().getName() + ".ReadText end!");
		}

		return rList;
	}

}
