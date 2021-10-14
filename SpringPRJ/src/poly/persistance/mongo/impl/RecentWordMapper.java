package poly.persistance.mongo.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import poly.dto.GetWordMeanDTO;
import poly.persistance.mongo.IRecentWordMapper;
import poly.util.CmmUtil;
import poly.util.DateUtil;

@Component("RecentWordMapper")
public class RecentWordMapper implements IRecentWordMapper {
	
	@Autowired
	private MongoTemplate mongodb;

	private Logger log = Logger.getLogger(this.getClass());

	@Override
	public List<String> getRecentList(String userId) throws Exception {
		
		log.info(this.getClass().getName() + ".getRecentWord start.");
		
		// 결과 담을 객체 생성하기
		List<String> rList = new ArrayList<>();
		
		// 가져올 mongodb 컬렉션 객체
		MongoCollection<Document> col = mongodb.getCollection("OcrResult_"+userId);
		
		// 사용자의 OcrResult 컬렉션에서 최근 3(i = 0, -1, -2)일간 저장된 document(컬렉션명) 값 가져오기 위한 반복문
		int i = 0;
		while (i >= -2) { 
			
			// 날짜
			String recentDate = DateUtil.getCalendarDate(i);
			
			log.info("recentDate : " + recentDate);
			
			// 검색조건
			Document query = new Document();
			
	        query.append("reg_day", recentDate);
			
			// 조회 결과 중 출력할 컬럼
			// 사용자의 OcrResult 컬렉션에서 최근 3일간 저장된 document의 reg_dt(유니크키) 값 가져오기
	        Document projection = new Document();
	
	        projection.append("reg_dt", "$reg_dt");
	        projection.append("_id", 0);
	        
	        // 정렬 - 등록일 내림차순으로
			Document sort = new Document();

			sort.append("reg_dt", -1);

			// MongoDB의 find 명령어를 통해 조회할 경우 사용함
			// 조회하는 데이터의 양이 적은 경우, find를 사용하고, 데이터양이 많은 경우 무조건 Aggregate 사용한다
			// 결과 조회는 Find와 Aggregation이 분리되어 Find 쿼리는 FindIterable을 사용해야함
			FindIterable<Document> rs = col.find(query).projection(projection).sort(sort);
			
			// 저장 결과를 제어가능한 구조인 Iterator로 변경하기 위해 사용함
			Iterator<Document> cursor = rs.iterator();
			while (cursor.hasNext()) {

				// MongoDB의 저장되는 데이터를 가져올 때 저장과 동일하게 Document 객체로 가져옴
				Document doc = cursor.next();
				if (doc == null) {
					doc = new Document();
				}
				
				String reg_dt = CmmUtil.nvl(doc.getString("reg_dt")); // 등록일
				
				rList.add(reg_dt);
				
				doc = null;
			}
			cursor = null;
			rs = null;
			sort = null;
			projection = null;
			query = null;
			
	        i--;
		}
		log.info(this.getClass().getName() + ".getRecentWord end.");
				
		return rList;
	}
	
	@Override
	public List<String> getRecentWord(String userId, List<String> rList) throws Exception {

		log.info(this.getClass().getName() + ".getRecentWord start!");
		
		if (rList == null) {
			rList = new ArrayList<>();
		}
		
		// 조회 결과를 전달하기 위한 객체 생성하기
		List<String> wordList = new ArrayList<>();
		
		Iterator<String> it = rList.iterator();
		while(it.hasNext()) {
			
			String colNm = "WordMean_" + userId + "_" + (String) it.next();
			log.info("colNm : " + colNm);
			MongoCollection<Document> col = mongodb.getCollection(colNm);
			
			// 조회 결과 중 출력할 컬럼들(SQL의 SELECT절과 FROM절 가운데 컬럼들과 유사함)
			Document projection = new Document();
			
			// MongoDB는 무조건 ObjectID가 자동생성되며, ObjectID는 사용하지 않을 때, 조회할 필요가 없음
			// ObjectID를 가지고 오지 않을 때 0 사용함
			projection.append("_id", 0);		
			projection.append("word", "$word");
						
			// MongoDB의 find 명령어를 통해 조회할 경우 사용함
			// 조회하는 데이터의 양이 적은 경우, find를 사용하고, 데이터양이 많은 경우 무조건 Aggregate 사용한다
			// 결과 조회는 Find와 Aggregation이 분리되어 Find 쿼리는 FindIterable을 사용해야함
			FindIterable<Document> rs = col.find(new Document()).projection(projection);
					
			// 저장 결과를 제어가능한 구조인 Iterator로 변경하기 위해 사용함
			Iterator<Document> cursor = rs.iterator();
			while (cursor.hasNext()) {
				
				// MongoDB의 저장되는 데이터를 가져올 때 저장과 동일하게 Document 객체로 가져옴
				Document doc = cursor.next();
				if(doc == null) {
					doc = new Document();
				}
				
				String word = doc.getString("word"); // 영어 단어
				
				log.info("word : " + word);

				wordList.add(word); // List에 데이터 저장

				doc = null;

			}
			
			// 메모리에서 강제로 비우기
			cursor = null;
			rs = null;
			projection = null;
			col = null;
			
		}
		
		it = null;
		
		log.info(this.getClass().getName() + ".getRecentWord End!");
				
		return wordList;
	}

	
	
}
