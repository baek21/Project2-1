package poly.persistance.mongo.comm;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

public abstract class AbstractMongoDBComon {

	// 로그 파일 생성 및 로그 출력을 위한 log4j 프레임워크의 자바 객체
	private Logger log = Logger.getLogger(this.getClass());

	@Autowired
	private MongoTemplate mongodb;

	/**
	 * 인덱스 컬럼이 여러개일때 컬렉션 생성
	 * 
	 * @param 생성할 컬렉션명
	 * @param 생성할 인덱스
	 * @return 생성결과
	 */
	protected boolean createCollection(String colNm) throws Exception {

		log.info(this.getClass().getName() + ".createCollection Start!");

		boolean res = false;

		// 기존에 등록된 컬렉션 이름이 존재하는지 체크하고, 컬렉션이 없는 경우 생성함
		if (!mongodb.collectionExists(colNm)) {

			mongodb.createCollection(colNm);
			
			res = true;
		}

		log.info(this.getClass().getName() + ".createCollection End!");

		return res;

	}

}
