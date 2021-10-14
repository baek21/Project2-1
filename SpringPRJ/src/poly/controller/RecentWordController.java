package poly.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import poly.service.IRecentWordService;
import poly.util.CmmUtil;

@Controller
public class RecentWordController {
	
	private Logger log = Logger.getLogger(this.getClass());
	
	/*
	 * 비즈니스 로직(중요 로직을 수행하기 위해 사용되는 서비스를 메모리에 적재(싱글톤패턴 적용됨))
	 */
	@Resource(name = "RecentWordService")
	private IRecentWordService recentWordService;
	
	/**
	 * 최근 3일 동안 추출한 영어단어
	 */
	@RequestMapping(value = "ocr/getRecentWord")
	@ResponseBody
	public List<Map<String, Object>> getRecentWord(HttpSession session, HttpServletRequest request, HttpServletResponse response)
			throws Exception {

		log.info(this.getClass().getName() + ".getRecentWord start!");

		// 로그인 아이디
		String user_id = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));
		
		List<Map<String, Object>> rList = recentWordService.recentWord(user_id);
		if(rList == null) {
			rList = new ArrayList<>();
		}

		log.info(this.getClass().getName() + ".getRecentWord end!");

		return rList;

	}
	
	/**
	 * 추출 기록 리스트(Ocr결과 목록) 보여주는 페이지
	 */
	@RequestMapping(value = "recent/WordChart")
	public String OcrResult_List() {
		log.info(this.getClass().getName() + ".WordChart!");

		return "/recent/WordChart";
	}
}
