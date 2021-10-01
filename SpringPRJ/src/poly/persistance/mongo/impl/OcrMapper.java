package poly.persistance.mongo.impl;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;

import poly.dto.OcrDTO;
import poly.persistance.mongo.IOcrMapper;
import poly.util.CmmUtil;

@Component("OcrMapper")
public class OcrMapper implements IOcrMapper {

	@Autowired
	private MongoTemplate mongodb;

	private Logger log = Logger.getLogger(this.getClass());

	/**
	 * MongoDB에 OCR결과 데이터 저장하기
	 */
	@Override
	public boolean insertOcrResult(OcrDTO pDTO, String colNm) throws Exception {

		log.info(this.getClass().getName() + ".insertOcrResult Start!");

		boolean res = false;

		if (pDTO == null) {
			pDTO = new OcrDTO();
		}

		// 기존에 등록된 컬렉션 이름이 존재하는지 체크하고, 컬렉션이 없는 경우 생성함
		if (!mongodb.collectionExists(colNm)) {

			mongodb.createCollection(colNm);

			// 컬렉션에 인덱스 생성(유니크)
			IndexOptions indexOptions = new IndexOptions().unique(true);
			mongodb.getCollection(colNm).createIndex(Indexes.descending("reg_dt"), indexOptions);
			mongodb.getCollection(colNm).createIndex(Indexes.descending("reg_day"));

		}

		// 저장할 컬렉션 객체 생성
		MongoCollection<Document> col = mongodb.getCollection(colNm);

		String save_file_name = CmmUtil.nvl(pDTO.getSave_file_name());
		String save_file_path = CmmUtil.nvl(pDTO.getSave_file_path());
		String original_file_name = CmmUtil.nvl(pDTO.getOriginal_file_name());
		String ext = CmmUtil.nvl(pDTO.getExt());
		String ocr_text = CmmUtil.nvl(pDTO.getOcr_text());
		String reg_id = CmmUtil.nvl(pDTO.getReg_id());
		String reg_dt = CmmUtil.nvl(pDTO.getReg_dt());
		String reg_day = CmmUtil.nvl(pDTO.getReg_dt()).substring(0, 8);

		log.info("save_file_name : " + save_file_name);
		log.info("save_file_path : " + save_file_path);
		log.info("original_file_name : " + original_file_name);
		log.info("ext : " + ext);
		log.info("ocr_text : " + ocr_text);
		log.info("reg_id : " + reg_id);
		log.info("reg_dt : " + reg_dt);
		log.info("reg_day : " + reg_day);

		Document doc = new Document();

		doc.append("save_file_name", save_file_name);
		doc.append("save_file_path", save_file_path);
		doc.append("original_file_name", original_file_name);
		doc.append("ext", ext);
		doc.append("ocr_text", ocr_text);
		doc.append("reg_id", reg_id);
		doc.append("reg_dt", reg_dt);
		doc.append("reg_day", reg_day);

		col.insertOne(doc);

		doc = null;

		res = true;

		log.info(this.getClass().getName() + ".insertOcrResult End!");

		return res;
	}

	/**
	 * MongoDB에 저장된 OCR결과 데이터 모두 가져오기
	 */
	@Override
	public List<OcrDTO> getOcrResultAll(String colNm) throws Exception {

		log.info(this.getClass().getName() + ".getOcrResultAll Start!");

		// 조회 결과를 전달하기 위한 객체 생성하기
		List<OcrDTO> rList = new LinkedList<OcrDTO>();

		MongoCollection<Document> col = mongodb.getCollection(colNm);

		// 조회 결과 중 출력할 컬럼들(SQL의 SELECT절과 FROM절 가운데 컬럼들과 유사함)
		Document projection = new Document();

		// MongoDB는 무조건 ObjectID가 자동생성되며, ObjectID는 사용하지 않을 때, 조회할 필요가 없음
		// ObjectID를 가지고 오지 않을 때 사용함
		// projection.append("_id", 0);

		projection.append("save_file_name", "$save_file_name");
		projection.append("save_file_path", "$save_file_path");
		projection.append("original_file_name", "$original_file_name");
		projection.append("reg_id", "$reg_id");
		projection.append("reg_dt", "$reg_dt");

		// 정렬 - 등록일 내림차순으로
		Document sort = new Document();

		sort.append("reg_dt", -1);

		// MongoDB의 find 명령어를 통해 조회할 경우 사용함
		// 조회하는 데이터의 양이 적은 경우, find를 사용하고, 데이터양이 많은 경우 무조건 Aggregate 사용한다
		// 결과 조회는 Find와 Aggregation이 분리되어 Find 쿼리는 FindIterable을 사용해야함
		FindIterable<Document> rs = col.find(new Document()).projection(projection).sort(sort);

		// 저장 결과를 제어가능한 구조인 Iterator로 변경하기 위해 사용함
		Iterator<Document> cursor = rs.iterator();
		while (cursor.hasNext()) {

			// MongoDB의 저장되는 데이터를 가져올 때 저장과 동일하게 Document 객체로 가져옴
			Document doc = cursor.next();
			if (doc == null) {
				doc = new Document();
			}

			String save_file_name = CmmUtil.nvl(doc.getString("save_file_name")); // 서버에 저장된 파일명
			String save_file_path = CmmUtil.nvl(doc.getString("save_file_path")); // 서버에 저장된 파일경로
			String original_file_name = CmmUtil.nvl(doc.getString("original_file_name")); // 업로드된 원래 파일명
			String reg_id = CmmUtil.nvl(doc.getString("reg_id")); // 등록자
			String reg_dt = CmmUtil.nvl(doc.getString("reg_dt")); // 등록일

			// log.info("-----------ㅡMongoDB에서 가져온 값-------------");
			// log.info(save_file_name);
			// log.info(save_file_path);
			// log.info(original_file_name);
			// log.info(reg_id);
			// log.info(reg_dt);
			// log.info("------------------------------------------");

			OcrDTO rDTO = new OcrDTO();

			rDTO.setSave_file_name(save_file_name);
			rDTO.setSave_file_path(save_file_path);
			rDTO.setOriginal_file_name(original_file_name);
			rDTO.setReg_id(reg_id);
			rDTO.setReg_dt(reg_dt);

			// 레코드 결과를 List에 저장
			rList.add(rDTO);

			rDTO = null;
			doc = null;
		}

		// 메모리에서 강제로 비우기
		cursor = null;
		rs = null;
		col = null;
		projection = null;
		sort = null;

		log.info(this.getClass().getName() + ".getOcrResultAll End!");

		return rList;
	}

	/**
	 * MongoDB에서 OCR결과 레코드, 영어단어 컬렉션 삭제
	 */
	@Override
	public void ocrResultDelete(HashMap<String, String> rMap) throws Exception {
		log.info(this.getClass().getName() + ".ocrResultDelete start!");

		String reg_id = rMap.get("reg_id");
		String reg_dt = rMap.get("reg_dt");

		// 검색할 컬렉션 이름
		String colNm_wordMean = "WordMean_" + reg_id + "_" + reg_dt;
		String colNm_ocrResult = "OcrResult_" + reg_id;

		log.info("reg_id : " + reg_id);
		log.info("reg_dt : " + reg_dt);
		log.info("영어단어 정보 저장된 컬렉션 : " + colNm_wordMean);
		log.info("OCR결과 저장된 컬렉션 : " + colNm_ocrResult);

		// MongoDB에서 영어단어 정보 저장된 컬렉션 삭제
		mongodb.dropCollection(colNm_wordMean);
		log.info("컬렉션 삭제. collection : " + colNm_wordMean);

		// MongoDB의 OCR결과 컬렉션에서 해당 레코드 찾을 쿼리
		Document query = new Document();

		query.append("reg_dt", reg_dt);

		// MongoDB의 OCR결과 컬렉션에서 가져올 값
		Document projection = new Document();

		projection.append("save_file_name", "$save_file_name");
		projection.append("save_file_path", "$save_file_path");

		FindIterable<Document> rs = mongodb.getCollection(colNm_ocrResult).find(query).projection(projection);

		// 업로드 파일 주소 가져온 후 파일 삭제
		Iterator<Document> cursor = rs.iterator();
		while (cursor.hasNext()) {

			// MongoDB의 저장되는 데이터를 가져올 때 저장과 동일하게 Document 객체로 가져옴
			Document doc = cursor.next();
			if (doc == null) {
				doc = new Document();
			}

			String save_file_name = CmmUtil.nvl(doc.getString("save_file_name")); // 서버에 저장된 파일명
			String save_file_path = CmmUtil.nvl(doc.getString("save_file_path")); // 서버에 저장된 파일경로
			String full_file_path = save_file_path + "/" + save_file_name;

			File deleteFile = new File(full_file_path);

			// 파일 존재하면 삭제
			if (deleteFile.exists()) {

				deleteFile.delete();
				log.info("서버에 업로드된 이미지 파일 삭제. filePath : " + full_file_path);

			} else {
				log.info("서버에 업로드된 이미지 파일 없음");
			}

			doc = null;
		}

		// OCR결과 컬렉션에서 해당 레코드 삭제
		mongodb.getCollection(colNm_ocrResult).deleteOne(query);
		log.info("레코드 삭제. collection : " + colNm_ocrResult + ", query : " + query);

		// 메모리에서 강제로 비우기
		cursor = null;
		rs = null;
		query = null;
		projection = null;

		log.info(this.getClass().getName() + ".ocrResultDelete End!");

	}

}
