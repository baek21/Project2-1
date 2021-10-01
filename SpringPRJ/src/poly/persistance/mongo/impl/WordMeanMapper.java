package poly.persistance.mongo.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.json.simple.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Indexes;

import poly.dto.GetWordMeanDTO;
import poly.dto.WordMeanDTO;
import poly.persistance.mongo.IWordMeanMapper;
import poly.util.CmmUtil;

@Component("WordMeanMapper")
public class WordMeanMapper implements IWordMeanMapper {

	@Autowired
	private MongoTemplate mongodb;

	private Logger log = Logger.getLogger(this.getClass());

	@Override
	public boolean insertWordMean(List<WordMeanDTO> rList, String colNm) throws Exception {

		log.info(this.getClass().getName() + ".insertWordMean Start!");

		boolean res = false;
		
		if (rList == null) {
			rList = new ArrayList<WordMeanDTO>();
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
			
		Iterator<WordMeanDTO> it = rList.iterator();

		log.info(this.getClass().getName() + "MongoDB에 영어 단어 정보 저장 시작");
		while (it.hasNext()) {			
			
			WordMeanDTO pDTO = (WordMeanDTO) it.next();

			if (pDTO == null) {
				pDTO = new WordMeanDTO();
			}
			
			String word = CmmUtil.nvl(pDTO.getWord());
			int frequency = Integer.parseInt(pDTO.getFrequency());
			JSONArray meanArr = pDTO.getMean();
			String reg_id = CmmUtil.nvl(pDTO.getReg_id());
			String reg_dt = CmmUtil.nvl(pDTO.getReg_dt());
			
			Document doc = new Document();
			
			doc.append("word", word);
			doc.append("frequency", frequency);
			doc.append("mean", meanArr);
			doc.append("reg_id", reg_id);
			doc.append("reg_dt", reg_dt);
			
			col.insertOne(doc);
			
			doc = null;
			meanArr=null;
			pDTO=null;

		}
		
		// 메모리 정리
		it=null;
		col=null;
		rList=null;

		res = true;

		log.info(this.getClass().getName() + ".insertWordMean End!");

		return res;
	}

	@Override
	public List<GetWordMeanDTO> getWordMeanList(String colNm) throws Exception {

		log.info(this.getClass().getName() + ".getWordMeanList start!");
		
		// 조회 결과를 전달하기 위한 객체 생성하기
		List<GetWordMeanDTO> rList = new LinkedList<GetWordMeanDTO>();
		
		MongoCollection<Document> col = mongodb.getCollection(colNm);
		
		// 조회 결과 중 출력할 컬럼들(SQL의 SELECT절과 FROM절 가운데 컬럼들과 유사함)
		Document projection = new Document();
		
		// MongoDB는 무조건 ObjectID가 자동생성되며, ObjectID는 사용하지 않을 때, 조회할 필요가 없음
		// ObjectID를 가지고 오지 않을 때 사용함
		projection.append("_id", 0);		
		projection.append("word", "$word");
		projection.append("mean", "$mean");
		projection.append("frequency", "$frequency");
		projection.append("reg_id", "$reg_id");
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
			String reg_id = CmmUtil.nvl((String) doc.getString("reg_id")); // 등록자
			String reg_dt = CmmUtil.nvl((String) doc.getString("reg_dt")); // 등록일
			
			log.info("word : " + word);
			log.info("mean : " + mean);
			log.info("frequency : " + frequency);
			log.info("reg_id : " + reg_id);
			log.info("reg_dt : " + reg_dt);
			
			GetWordMeanDTO rDTO = new GetWordMeanDTO();
			
			rDTO.setWord(word);
			rDTO.setMean(mean);
			rDTO.setFrequency(frequency);
			rDTO.setReg_id(reg_id);
			rDTO.setReg_dt(reg_dt);

			rList.add(rDTO); // List에 데이터 저장

			rDTO = null;
			doc = null;

		}
		
		// 메모리에서 강제로 비우기
		cursor = null;
		rs = null;
		col = null;
		projection = null;
		sort = null;

		log.info(this.getClass().getName() + ".getWordMeanList End!");
		return rList;
	}

}
