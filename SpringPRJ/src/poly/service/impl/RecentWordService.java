package poly.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import poly.persistance.mongo.IRecentWordMapper;
import poly.service.IRecentWordService;
import poly.util.CmmUtil;

@Service("RecentWordService")
public class RecentWordService implements IRecentWordService{
	
	@Resource(name = "RecentWordMapper")
	private IRecentWordMapper recentWordMapper;

	// 로그 파일 생성 및 로그 출력을 위한 log4j 프레임워크의 자바 객체
	private Logger log = Logger.getLogger(this.getClass());

	@Override
	public List<Map<String, Object>> recentWord(String userId) throws Exception {

		log.info(this.getClass().getName() + ".recentWord start!");
		
		// 결과를 저장하기 위한 객체 생성
		List<Map<String, Object>> rList = new ArrayList<>();
		HashMap<String, Object> rMap = new HashMap<>();
		
		// 최근 3일 동안 추출한 영어 단어 리스트명
		List<String> recentList = recentWordMapper.getRecentList(userId);
		if(recentList == null) {
			recentList = new ArrayList<>();
		}
		
		// 최근 3일 동안 추출한 영어 단어(중복 포함)
		List<String> wordList = recentWordMapper.getRecentWord(userId, recentList);
		if(wordList == null) {
			wordList = new ArrayList<>();
		}
		
		// 리스트에서 중복제거
		//Set<String> rSet = new HashSet<String>(wordList);
		// 정렬을 위해 TreeSet 사용(영어단어 오름차순)
		TreeSet<String> rSet = new TreeSet<String>(wordList);
		
		// 중복이 제거된 단어 모음에 빈도수를 구하기 위해 반복문 사용함
		Iterator<String> it = rSet.iterator();		
		while(it.hasNext()) {
			
			rMap = new HashMap<>();
			
			// 중복 제거된 단어
			String word = CmmUtil.nvl(it.next());
						
			// 단어가 중복 저장되어 있는 wordList로부터 단어의 빈도수 가져오기
			int frequency = Collections.frequency(wordList, word);
			
			log.info("word : " + word);
			log.info("frequency : " + frequency);
			
			rMap.put("word", word);
			rMap.put("frequency", frequency);
			
			rList.add(rMap);
			
			rMap = null;
		}
		
		log.info("빈도수 구하기 종료");
		
		// 비교함수 Comparator를 사용하여 빈도수 내림차순으로 정렬
		Collections.sort(rList, new Comparator<Map<String, Object>>() {
			// compare로 값을 비교
			@Override
			public int compare(Map<String, Object> map1, Map<String, Object> map2) {
				Integer frequency1 = (Integer) map1.get("frequency");
				Integer frequency2 = (Integer) map2.get("frequency");
				return frequency2.compareTo(frequency1);
			}			
		});
		
		log.info(this.getClass().getName() + ".recentWord end!");
		
		return rList;
	}
	
	

}
