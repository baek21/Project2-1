package poly.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import poly.service.INewsService;
import poly.service.ITextAnalysisService;
import poly.util.CmmUtil;

@Controller
public class NewsController {
	private Logger log = Logger.getLogger(this.getClass());

	/*
	 * 비즈니스 로직(중요 로직을 수행하기 위해 사용되는 서비스를 메모리에 적재(싱글톤패턴 적용됨))
	 */
	@Resource(name = "TextAnalysisService")
	private ITextAnalysisService textAnalysisService;

	@Resource(name = "NewsService")
	private INewsService newsService;
	
	/**
	 * 어제의 웹 기사 수집하기(자동으로 실행하도록 코드 짜기)
	 * API에서 영어 단어 의미 가져와서 분야별로 MongoDB에 저장
	 */
	@RequestMapping(value = "news/InsertNewsInfo")
	@ResponseBody
	public String InsertNewsInfo(HttpServletRequest request, HttpServletResponse response) throws Exception {

		log.info(this.getClass().getName() + ".InsertNewsInfo start!");

		boolean res = false;
		
		// 웹 기사 수집 MongoDB에 저장
		res = newsService.insertNewsInfo();
		log.info("웹 기사 수집해서 MongoDB에 저장하기" + res);
		
		// MongoDB에서 웹 기사 분야 리스트 가져오기
		List<Map<String, Object>> rList = newsService.getNewsAreaList();
		log.info("웹 기사 분야 리스트 : " + rList);
		
		// 웹 기사 분야마다 실행하기
		Iterator<Map<String, Object>> it = rList.iterator();
		while(it.hasNext()) {
			
			Map<String, Object> newsAreaMap = (Map<String, Object>) it.next();
			
			// 웹 기사 분야
			String newsArea = CmmUtil.nvl(newsAreaMap.get("newsArea").toString());
			log.info("newsArea : " + newsArea);
			// 해당 분야의 모든 웹 기사 본문 내용 합쳐서 가져오기
			Map<String, String> pMap = newsService.getNewsContentsAll(newsArea);
			if(pMap == null) {
				pMap = new HashMap<String, String>();
			}
			
			// 본문 내용 단어 추출
			Map<String, Integer> rMap = textAnalysisService.wordAnalysis(CmmUtil.nvl(pMap.get("newsContents_all")));
			if(rMap == null) {
				rMap = new HashMap<String, Integer>();
				
				// 메모리 정리
				newsAreaMap = null;
			}
			
			// API에서 단어 의미 가져와서 MongoDB에 저장하기
			res = newsService.getNewsWordMeanFromWEB(newsArea, rMap);
			log.info("API에서 단어 의미 가져와서 MongoDB에 저장하기" + res);
			
			// 메모리 정리
			pMap = null;
			rMap = null;
			
		}
		
		// 메모리 정리
		it = null;
		rList=null;
		
		
		log.info(this.getClass().getName() + ".InsertNewsInfo end!");

		return "success";
	}
	
	/**
	 * 웹 기사의 모든 분야 리스트 가져오기
	 *
	 */
	@RequestMapping(value = "news/NewsAreaList")
	@ResponseBody
	public List<Map<String, Object>> newsAreaList(HttpSession session, HttpServletRequest request, HttpServletResponse response, ModelMap model)
			throws Exception {

		log.info(this.getClass().getName() + ".newsAreaList start!");
		
		List<Map<String, Object>> rList = newsService.getNewsAreaList();
		if (rList == null) {
			rList = new ArrayList<Map<String, Object>>();
		}
		log.info("rList : " + rList);

		log.info(this.getClass().getName() + ".newsAreaList end!");

		return rList;

	}
	
	/**
	 * 분야별 웹 기사 단어 가져오기
	 */
	@RequestMapping(value = "news/newsWord")
	@ResponseBody
	public List<Map<String, Object>> newsWord(HttpSession session, HttpServletRequest request, HttpServletResponse response, ModelMap model)
			throws Exception {

		log.info(this.getClass().getName() + ".newsWord start!");
		
		// 분야 CmmUtil.nvl(request.getParameter("newsArea"))
		
		String newsArea = CmmUtil.nvl(request.getParameter("newsArea")); // 분야

		List<Map<String, Object>> rList = newsService.newsWord(newsArea);
		if (rList == null) {
			rList = new ArrayList<Map<String, Object>>();
		}
		log.info("rList : " + rList);

		log.info(this.getClass().getName() + ".newsWord end!");

		return rList;

	}
	
	/**
	 *  메인페이지 워드클라우드
	 */
	@RequestMapping(value = "news/NewsCloud")
	public String newsCloud(HttpServletRequest request, HttpServletResponse response) throws Exception {
		log.info(this.getClass().getName() + ".newsCloud start!");
		log.info(this.getClass().getName() + ".newsCloud end!");
		return "/news/NewsCloud";
		
	}
	/**
	 *  웹 기사 분야별 영어 단어 보여주는 페이지
	 */
	@RequestMapping(value = "news/NewsWordInfo")
	public String newsWordInfo(HttpServletRequest request, HttpServletResponse response, ModelMap model) throws Exception {
		
		log.info(this.getClass().getName() + ".newsWordInfo start!");
		
		String newsArea = CmmUtil.nvl((String) request.getParameter("newsArea")); // 웹 기사 분야
		
		log.info("웹에서 넘어온 newsArea : " + newsArea);
		
		model.addAttribute("newsArea", newsArea);
		
		log.info(this.getClass().getName() + ".newsWordInfo end!");
		return "/news/NewsWordInfo";
		
	}
	
}