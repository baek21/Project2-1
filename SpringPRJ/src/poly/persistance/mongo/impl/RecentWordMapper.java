package poly.persistance.mongo.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.mongodb.client.MongoCollection;

import poly.persistance.mongo.IRecentWordMapper;
import poly.util.DateUtil;

@Component("RecentWordMapper")
public class RecentWordMapper implements IRecentWordMapper {
	
	@Autowired
	private MongoTemplate mongodb;

	private Logger log = Logger.getLogger(this.getClass());

	// mongodb에서 사용자가 최근 3일 동안 추출한 영어단어 리스트 가져오기
	@Override
	public List<Map<String, Object>> getRecentWord(String userId) throws Exception {
		
		log.info(this.getClass().getName() + ".getRecentWord start.");
		
		// 결과 담을 객체 생성하기
		List<Map<String, Object>> rList = new LinkedList<Map<String, Object>>();
		
		// 가져올 mongodb 컬렉션 객체
		MongoCollection<Document> col = mongodb.getCollection("OcrResult_"+userId);
		
		// 사용자의 OcrResult 컬렉션에서 최근 3일간 저장된 document 값 가져오기 위한 반복문
		for (int i = 0; i >= -2; i--) { 
			
			// 날짜
			String recentDate = DateUtil.getCalendarDate(i);
			
			// 검색조건
			Document query = new Document();
			
	        query.append("reg_day", recentDate);
			
			// 조회 결과 중 출력할 컬럼
			// 사용자의 OcrResult 컬렉션에서 최근 3일간 저장된 document의 reg_dt(유니크키) 값 가져오기
	        Document projection = new Document();
	
	        projection.append("reg_dt", "$reg_dt");
	        projection.append("_id", 0);
	        // 수정 중입니다.
	        

		
		}
		log.info(this.getClass().getName() + ".getRecentWord end.");
		
		
		return rList;
	}
	
	
}
