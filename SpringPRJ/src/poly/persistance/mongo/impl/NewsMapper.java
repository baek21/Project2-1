package poly.persistance.mongo.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.simple.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Indexes;

import poly.dto.NewsInfoDTO;
import poly.persistance.mongo.INewsMapper;
import poly.util.CmmUtil;

@Component("NewsMapper")
public class NewsMapper implements INewsMapper {

	@Autowired
	private MongoTemplate mongodb;

	private Logger log = Logger.getLogger(this.getClass());
	
	// 웹 기사 정보 저장 컬렉션 이름
	final private String NEWSCOLN = "NewsInfo";

	@Override
	public boolean insertNewsInfo(List<NewsInfoDTO> pList) throws Exception {

		log.info(this.getClass().getName() + ".insertOcrResult Start!");

		boolean res = false;

		if (pList == null) {
			pList = new ArrayList<NewsInfoDTO>();
		}
		
		// 기존에 등록된 컬렉션 이름이 존재하면 실행
		if (mongodb.collectionExists(NEWSCOLN)) {
			// 기존 컬렉션 삭제
			mongodb.dropCollection(NEWSCOLN);
		}

		// 데이터를 저장할 컬렉션 생성
		mongodb.createCollection(NEWSCOLN);

		// 저장할 컬렉션 객체 생성
		MongoCollection<Document> col = mongodb.getCollection(NEWSCOLN);
		
		// 컬렉션에 인덱스 생성(유니크)
		// IndexOptions indexOptions = new IndexOptions().unique(true);
		// 컬렉션에 인덱스 생성
		col.createIndex(Indexes.ascending("newsArea"));		
		col.createIndex(Indexes.ascending("newsTitle"));

		Iterator<NewsInfoDTO> it = pList.iterator();

		log.info(this.getClass().getName() + "MongoDB에 영어 단어 정보 저장 시작");
		while (it.hasNext()) {

			NewsInfoDTO pDTO = (NewsInfoDTO) it.next();

			if (pDTO == null) {
				pDTO = new NewsInfoDTO();
			}

			String newsReg = CmmUtil.nvl(pDTO.getNewsReg());
			String newsArea = CmmUtil.nvl(pDTO.getNewsArea());
			String newsTitle = CmmUtil.nvl(pDTO.getNewsTitle());
			String newsContents = CmmUtil.nvl(pDTO.getNewsContents());

			Document doc = new Document();

			doc.append("newsReg", newsReg);
			doc.append("newsArea", newsArea);
			doc.append("newsTitle", newsTitle);
			doc.append("newsContents", newsContents);

			col.insertOne(doc);

			doc = null;
			pDTO = null;

		}

		it = null;
		col = null;
		res = true;

		log.info(this.getClass().getName() + ".insertOcrResult End!");

		return res;
	}

	@Override
	public Map<String, String> getNewsContentsAll(String area) throws Exception {
		
		log.info(this.getClass().getName() + ".getWordMeanList start!");
		
		// 조회 결과를 전달하기 위한 객체 생성하기
		Map<String, String> rMap = new HashMap<String, String>();
		
		MongoCollection<Document> col = mongodb.getCollection(NEWSCOLN);
				
			
        Document query = new Document();

        query.append("newsArea", CmmUtil.nvl(area)); // 웹 기사 분야 
        
        // 조회 결과 중 출력할 컬럼들(SQL의 SELECT절과 FROM절 가운데 컬럼들과 유사함)
        Document projection = new Document();
        
        // MongoDB는 무조건 ObjectID가 자동생성되며, ObjectID는 사용하지 않을 때, 조회할 필요가 없음
     	// ObjectID를 가지고 오지 않을 때 사용함
     	// projection.append("_id", 0);	
        projection.append("newsContents", "$newsContents");
        projection.append("_id", 0);
                        		
		// MongoDB의 find 명령어를 통해 조회할 경우 사용함
		// 조회하는 데이터의 양이 적은 경우, find를 사용하고, 데이터양이 많은 경우 무조건 Aggregate 사용한다
		// 결과 조회는 Find와 Aggregation이 분리되어 Find 쿼리는 FindIterable을 사용해야함
		FindIterable<Document> rs = col.find(query).projection(projection);
		
		// 해당 분야의 모든 기사 담을 변수
		String newsContents_all = "";
						
		// 저장 결과를 제어가능한 구조인 Iterator로 변경하기 위해 사용함
		Iterator<Document> cursor = rs.iterator();
		while (cursor.hasNext()) {
			
			// MongoDB의 저장되는 데이터를 가져올 때 저장과 동일하게 Document 객체로 가져옴
			Document doc = cursor.next();
			if(doc == null) {
				doc = new Document();
			}
			
			String contents = CmmUtil.nvl((String) doc.getString("newsContents")); // 웹 기사 본문
			
			log.info("newsContents : " + contents);
			
			// 모든 기사 합치기
			newsContents_all += contents + " ";
			
			doc = null;

		}
		
		// 메모리에서 강제로 비우기
		cursor = null;
		rs = null;
		col = null;
		query = null;
		projection = null;
		
		// return할 Map에 넣어주기
		rMap.put("newsArea", CmmUtil.nvl(area));
		rMap.put("newsContents_all", newsContents_all);

		log.info(this.getClass().getName() + ".getWordMeanList End!");
		
		return rMap;
	}

	@Override
	public boolean insertNewsWordMean(List<HashMap<String, Object>> pList, String colNm) throws Exception {
		log.info(this.getClass().getName() + ".insertNewsWordMean Start!");

		boolean res = false;
		
		if (pList == null) {
			pList = new ArrayList<HashMap<String, Object>>();
		}
		
		// 기존에 등록된 컬렉션 이름이 존재하면 실행
		if (mongodb.collectionExists(colNm)) {
			// 기존 컬렉션 삭제
			mongodb.dropCollection(colNm);
		}
		
		// 컬렉션 생성
		mongodb.createCollection(colNm);
		
		// 저장할 컬렉션 객체 생성
		MongoCollection<Document> col = mongodb.getCollection(colNm);
		
		// 컬렉션에 인덱스 생성
		col.createIndex(Indexes.descending("frequency"));
		col.createIndex(Indexes.ascending("word"));
		
		// 컬렉션에 복합 인덱스 생성
		col.createIndex(Indexes.compoundIndex(Indexes.descending("frequency"), Indexes.ascending("word")));
			
		Iterator<HashMap<String, Object>> it = pList.iterator();

		log.info(this.getClass().getName() + "MongoDB에 영어 단어 정보 저장 시작");
		while (it.hasNext()) {			
			
			HashMap<String, Object> pMap = (HashMap<String, Object>) it.next();

			if (pMap == null) {
				pMap = new HashMap<String, Object>();
			}
			
			String word = CmmUtil.nvl(pMap.get("word").toString());
			int frequency = Integer.parseInt(pMap.get("frequency").toString());
			JSONArray meanArr = (JSONArray) pMap.get("meanArr");
			String reg_dt = CmmUtil.nvl(pMap.get("reg_dt").toString());
			
			Document doc = new Document();
			
			doc.append("word", word);
			doc.append("frequency", frequency);
			doc.append("mean", meanArr);
			doc.append("reg_dt", reg_dt);
			
			col.insertOne(doc);
			
			doc = null;
			meanArr = null;
			pMap = null;

		}

		res = true;

		log.info(this.getClass().getName() + ".insertNewsWordMean End!");

		return res;
	}
	
	@Override
	public List<Map<String, Object>> getNewsAreaList() throws Exception {
		
		log.info(this.getClass().getName() + ".getNewsAreaList start!");
		
		// 조회 결과를 전달하기 위한 객체 생성하기
		List<Map<String, Object>> rList = new ArrayList<Map<String, Object>>();
		
		// MongoDB 조회 쿼리
		List<? extends Bson> pipeline = Arrays.asList(
                new Document()
                .append("$group", new Document()
                        .append("_id", new Document()
                                .append("newsArea", "$newsArea")
                        )
                        .append("COUNT(newsArea)", new Document()
                                .append("$sum", 1)
                        )
                ), 
        new Document()
                .append("$project", new Document()
                        .append("newsArea", "$_id.newsArea")
                        .append("newsAreaCnt", "$COUNT(newsArea)")
                        .append("_id", 0)
                )
        );
		
		MongoCollection<Document> col = mongodb.getCollection(NEWSCOLN);
		AggregateIterable<Document> rs = col.aggregate(pipeline).allowDiskUse(true);
						
		// 저장 결과를 제어가능한 구조인 Iterator로 변경하기 위해 사용함
		Iterator<Document> cursor = rs.iterator();
		while (cursor.hasNext()) {
			
			// MongoDB의 저장되는 데이터를 가져올 때 저장과 동일하게 Document 객체로 가져옴
			Document doc = cursor.next();
			if(doc == null) {
				doc = new Document();
			}
			
			String newsArea = CmmUtil.nvl((String) doc.getString("newsArea")); // 웹 기사 분야
			String newsAreaCnt = CmmUtil.nvl((String) doc.getInteger("newsAreaCnt").toString()); // 웹 기사 분야
			
			Map<String, Object> rMap = new HashMap<String, Object>();
			
			rMap.put("newsArea", newsArea);
			rMap.put("newsAreaCnt", newsAreaCnt);
			
			rList.add(rMap); // List에 데이터 저장			
			
			rMap = null;
			doc = null;

		}
		
		// 메모리에서 강제로 비우기
		cursor = null;
		rs = null;
		col = null;
		pipeline=null;
		
		log.info(this.getClass().getName() + ".getNewsAreaList End!");
					
		return rList;
	}

	@Override
	public List<Map<String, Object>> newsWord(String newsArea) throws Exception {
		
		log.info(this.getClass().getName() + ".newsWord start!");
		
		// 조회 결과를 전달하기 위한 객체 생성하기
		List<Map<String, Object>> rList = new LinkedList<Map<String, Object>>();
		
		String colNm = "NewsArea_" + newsArea;
		
		MongoCollection<Document> col = mongodb.getCollection(colNm);
		
		// 조회 결과 중 출력할 컬럼들(SQL의 SELECT절과 FROM절 가운데 컬럼들과 유사함)
		Document projection = new Document();
		
		// MongoDB는 무조건 ObjectID가 자동생성되며, ObjectID는 사용하지 않을 때, 조회할 필요가 없음
		// ObjectID를 가지고 오지 않을 때 사용함
		// projection.append("_id", 0);		
		projection.append("_id", 0);		
		projection.append("word", "$word");
		projection.append("mean", "$mean");
		projection.append("frequency", "$frequency");
		projection.append("reg_dt", "$reg_dt");	
		
		// 정렬 - 빈도수 내림차순 / 영어단어 오름차순 
		Document sort = new Document();
		
		sort.append("frequency", -1);
		sort.append("word", 1);
		
		// MongoDB의 find 명령어를 통해 조회할 경우 사용함
		// 조회하는 데이터의 양이 적은 경우, find를 사용하고, 데이터양이 많은 경우 무조건 Aggregate 사용한다
		// 결과 조회는 Find와 Aggregation이 분리되어 Find 쿼리는 FindIterable을 사용해야함
		FindIterable<Document> rs = col.find(new Document()).projection(projection).sort(sort);
				
		// 저장 결과를 제어가능한 구조인 Iterator로 변경하기 위해 사용함
		Iterator<Document> cursor = rs.iterator();
		while (cursor.hasNext()) {
			
			// MongoDB의 저장되는 데이터를 가져올 때 저장과 동일하게 Document 객체로 가져옴
			Document doc = cursor.next();
			if(doc == null) {
				doc = new Document();
			}
			
			String word = CmmUtil.nvl((String) doc.getString("word")); // 영어 단어
			Object mean = (Object) doc.get("mean"); // 영어 단어 의미(json형태)
			String frequency = CmmUtil.nvl(doc.getInteger("frequency").toString()); // 빈도수
			String reg_dt = CmmUtil.nvl((String) doc.getString("reg_dt")); // 등록일
			
			log.info("word : " + word);
			log.info("mean : " + mean);
			log.info("frequency : " + frequency);
			log.info("reg_dt : " + reg_dt);
			
			Map<String, Object> rMap = new HashMap<String, Object>();
			
			rMap.put("word", word);
			rMap.put("mean", mean);
			rMap.put("frequency", frequency);
			rMap.put("reg_dt", reg_dt);
			
			rList.add(rMap); // List에 데이터 저장

			rMap = null;
			mean = null;
			doc = null;

		}
		
		// 메모리에서 강제로 비우기
		cursor = null;
		rs = null;
		col = null;
		projection = null;
		sort = null;

		log.info(this.getClass().getName() + ".newsWord End!");
		return rList;
	}

}
