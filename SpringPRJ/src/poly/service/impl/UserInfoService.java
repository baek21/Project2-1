package poly.service.impl;

import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import poly.dto.UserInfoDTO;
import poly.persistance.mongo.IUserInfoMapper;
import poly.service.IUserInfoService;

@Service("UserInfoService")
public class UserInfoService implements IUserInfoService {

	@Resource(name = "UserInfoMapper")
	private IUserInfoMapper userInfoMapper;

	// 로그
	private Logger log = Logger.getLogger(this.getClass());

	// 회원가입
	@Override
	public int insertUserInfo(UserInfoDTO pDTO) throws Exception {

		log.info(this.getClass().getName() + ".regUserInfo start!");

		// controller에서 값이 정상적으로 못 넘어오는 경우를 대비하기 위해 사용함
		if (pDTO == null) {
			pDTO = new UserInfoDTO();
		}

		int res = userInfoMapper.insertUserInfo(pDTO);
		// 메모리 정리
		pDTO = null;

		// 로그
		log.info(this.getClass().getName() + ".regUserInfo end!");

		return res;

	}

	@Override
	public int getUserLoginCheck(UserInfoDTO pDTO) throws Exception {

		// 로그
		log.info(this.getClass().getName() + ".getUserLoginCheck start!");
		
		// controller에서 값이 정상적으로 못 넘어오는 경우를 대비하기 위해 사용함
		if (pDTO == null) {
			pDTO = new UserInfoDTO();
		}


		// 로그인을 위해 아이디와 비밀번호가 일치하는지 확인하기 위한 mapper 호출하기
		int res = userInfoMapper.getUserLoginCheck(pDTO);
		// 메모리 정리
		pDTO=null;
		
		log.info(this.getClass().getName() + ".getUserLoginCheck end!");

		return res;
	}
	
	// 업데이트할 회원정보 가져오기
	@Override
	public Map<String, String> getUserInfo(UserInfoDTO pDTO) throws Exception {
		// 로그
		log.info(this.getClass().getName() + ".getUserInfo start!");
		
		// controller에서 값이 정상적으로 못 넘어오는 경우를 대비하기 위해 사용함
		if (pDTO == null) {
			pDTO = new UserInfoDTO();
		}

		// 로그인을 위해 아이디와 비밀번호가 일치하는지 확인하기 위한 mapper 호출하기
		Map<String, String> rMap = userInfoMapper.getUserInfo(pDTO);
		
		// 메모리 정리
		pDTO=null;
		
		log.info(this.getClass().getName() + ".getUserInfo end!");
		
		return rMap;
	}
	
	// 회원정보 업데이트
	@Override
	public String userUpdate(UserInfoDTO pDTO) throws Exception {
		log.info(this.getClass().getName() + ".userUpdate start!");

		// controller에서 값이 정상적으로 못 넘어오는 경우를 대비하기 위해 사용함
		if (pDTO == null) {
			pDTO = new UserInfoDTO();
		}
		
		// 업데이트 결과(0 실패, 1 성공)
		String res = userInfoMapper.userUpdate(pDTO);
		
		// 메모리 정리
		pDTO = null;

		// 로그
		log.info(this.getClass().getName() + ".userUpdate end!");

		return res;
	}

	//회원 탈퇴
	@Override
	public String deleteUser(String user_id) throws Exception {
		log.info(this.getClass().getName() + ".deleteUser start!");
		
		String res = userInfoMapper.deleteUser(user_id);

		log.info(this.getClass().getName() + ".deleteUser end!");
		return res;
	}

}
