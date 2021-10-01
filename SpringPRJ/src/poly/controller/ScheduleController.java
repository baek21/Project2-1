package poly.controller;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import poly.service.INewsService;
import poly.service.ITextAnalysisService;
import poly.util.CmmUtil;

@Configuration
@EnableScheduling
@Controller
public class ScheduleController {
	
	private Logger log = Logger.getLogger(this.getClass());
	
	@Resource(name = "TextAnalysisService")
	private ITextAnalysisService textAnalysisService;

	@Resource(name = "NewsService")
	private INewsService newsService;
	
	
	// 매일 02시 30분에 실행 
	@Scheduled(cron="0 30 2 * * *")
	public void insertNewsInfoSchedule() throws Exception{
		try {
			log.info(this.getClass().getName() + ".insertNewsInfoSchedule start!");

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
			
			
			log.info(this.getClass().getName() + ".insertNewsInfoSchedule end!");
		} catch (Exception e) {
									
			log.info(e.toString());
						
			e.printStackTrace();
		
		}
	}
	
	
}