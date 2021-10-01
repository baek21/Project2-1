package poly.controller;

import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import poly.dto.MailDTO;
import poly.dto.UserInfoDTO;
import poly.service.IMailService;
import poly.service.IUserInfoService;
import poly.util.CmmUtil;
import poly.util.DateUtil;
import poly.util.EncryptUtil;

/*
 * controller 선언해야만 spring 프레임워크에서 controller인지 인식 가능
 * 자바 서블릿 역할 수행
 */
@Controller
public class UserInfoController {
	private Logger log = Logger.getLogger(this.getClass());

	/*
	 * 비즈니스 로직(중요 로직을 수행하기 위해 사용되는 서비스를 메모리에 적재(싱글톤패턴 적용됨)
	 */
	@Resource(name = "UserInfoService")
	private IUserInfoService userInfoService;

	@Resource(name = "MailService")
	private IMailService mailService;

	/**
	 * 회원가입 화면으로 이동
	 */
	@RequestMapping(value = "user/UserRegForm")
	public String userRegForm() {
		log.info(this.getClass().getName() + ".user/UserRegFrom ok...");

		return "/user/UserRegForm";
	}

	/**
	 * 회원가입 로직 처리
	 */
	@RequestMapping(value = "user/insertUserInfo", method = RequestMethod.POST)
	public String insertUserInfo(HttpServletRequest request, HttpServletResponse response, ModelMap model)
			throws Exception {

		log.info(this.getClass().getName() + ".insertUserInfo start...");

		// 회원가입 결과에 대한 메시지를 전달할 변수
		// 0 성공, 1 중복 아이디, 2 시스템 에러
		int res = 999;

		// 웹(회원정보 입력화면)에서 받는 정보를 저장할 변수
		UserInfoDTO pDTO = null;

		try {
			
			String user_id = CmmUtil.nvl(request.getParameter("user_id")); // 아이디
			String user_name = CmmUtil.nvl(request.getParameter("user_name")); // 이름
			String password = CmmUtil.nvl(request.getParameter("password")); // 비밀번호
			String email = CmmUtil.nvl(request.getParameter("email").toString()); // 이메일
			
			log.info("user_id : " + user_id);
			log.info("user_name : " + user_name);
			log.info("password : " + password);
			log.info("email : " + email);

			// 웹(회원정보 입력화면)에서 받는 정보를 저장할 변수를 메모리에 올리기
			pDTO = new UserInfoDTO();

			pDTO.setUser_id(user_id);
			pDTO.setUser_name(user_name);

			// 비밀번호는 절대로 복호화되지 않도록 해시 알고리즘으로 암호화함
			pDTO.setPassword(EncryptUtil.encHashSHA256(password));
			
			// 민감 정보인 이메일은 AES128-CBC로 암호화함
			pDTO.setEmail(EncryptUtil.encAES128CBC(email));

			pDTO.setReg_id("Admin");
			pDTO.setReg_dt(DateUtil.getDateTime("yyyyMMddhhmmss"));
			pDTO.setChg_id("Admin");
			pDTO.setChg_dt(DateUtil.getDateTime("yyyyMMddhhmmss"));

			
			 // 회원가입 결과
			res = userInfoService.insertUserInfo(pDTO);
			
			// 이메일 발송
			if(!"".equals(email)) {
				
				log.info("메시지 보낼 이메일 : " + email);
				
				MailDTO eDTO = new MailDTO();
				
				String toMail = email;
				String title = "Eword 회원가입을 환영합니다.";
				String content = user_id + "님 회원가입을 환영합니다. 본인이 아니시라면 관리자에게 문의 하시기 바랍니다.\n"
						+ "Eword http://13.124.9.63:8080/main.do\n";
				
				eDTO.setToMail(toMail);
				eDTO.setTitle(title);
				eDTO.setContents(content);
				
				// 결과(0 실패, 1 성공)
				int sendRes = mailService.doSendMail(eDTO);
				
				log.info("메일 전송 결과 : " + sendRes);
				
				eDTO = null;
				
			}

		} catch (Exception e) {
			
			// 시스템 에러
			res = 2;
						
			log.info(e.toString());
						
			e.printStackTrace();

		} finally {
			
			log.info(this.getClass().getName() + ".insertUserInfo end!");

			// 회원가입 여부 결과 메시지 전달하기
			model.addAttribute("res", String.valueOf(res));

			// 변수 초기화(메모리 효율화 시키기 위해 초기화)
			pDTO = null;

		}

		return "/user/MsgRegResult";

	}

	/**
	 * 로그인을 위한 입력 화면으로 이동
	 */
	@RequestMapping(value = "user/LoginForm")
	public String loginForm() {
		log.info(this.getClass().getName() + ".user/loginForm ok!");

		return "/user/LoginForm";
	}

	/**
	 * 로그인 처리 및 결과 알려주는 화면으로 이동
	 */
	@RequestMapping(value = "user/getUserLoginCheck", method = RequestMethod.POST)
	public String getUserLoginCheck(HttpSession session, HttpServletRequest request, HttpServletResponse response,
			ModelMap model) throws Exception {
		log.info(this.getClass().getName() + ".getUserLoginCheck start!");

		// 로그인 처리 결과를 저장할 변수(로그인 성공 : 0, 아이디, 비밀번호 불일치로 인한 실패 : 1, 가입되지 않음 : 2, 시스템 에러: 3)
		int res = 999;

		// 웹(회원정보 입력화면)에서 받는 정보를 저장할 변수
		UserInfoDTO pDTO = null;

		try {

			String user_id = CmmUtil.nvl(request.getParameter("user_id")); // 아이디
			String password = CmmUtil.nvl(request.getParameter("password")); // 비밀번호
			
			log.info("user_id : " + user_id);
			//log.info("password : " + password);

			// 웹(회원정보 입력화면)에서 받는 정보를 저장할 변수를 메모리에 올리기
			pDTO = new UserInfoDTO();

			pDTO.setUser_id(user_id);

			// 비밀번호는 절대로 복호화되지 않도록 해시 알고리즘으로 암호화함
			pDTO.setPassword(EncryptUtil.encHashSHA256(password));

			// 로그인을 위해 아이디와 비밀번호가 일치하는지 확인하기 위한 userInfoService 호출하기
			res = userInfoService.getUserLoginCheck(pDTO);

			if (res == 0) { // 로그인 성공
				
				session.setAttribute("SS_USER_ID", user_id);
			}
		} catch (Exception e) {
			
			// 시스템 에러
			res = 3;
			
			log.info(e.toString());
			
			e.printStackTrace();
			
		} finally {
			
			log.info(this.getClass().getName() + ".getUserLoginCheck end!");

			/*
			 * 로그인 처리 결과를 jsp에 전달하기 위해 변수 사용 숫자 유형의 데이터 타입은 값을 전달하고 받는데 불편함이 있어 문자
			 * 유형(String)으로 강제 형변환하여 jsp에 전달한다.
			 */
			model.addAttribute("res", String.valueOf(res));

			// 변수 초기화(메모리 효율화 시키기 위해 사용함)
			pDTO = null;
		}

		return "/user/LoginResult";

	}

	// 로그아웃
	@RequestMapping(value = "user/Logout")
	public String logout(HttpSession session) {
		log.info(this.getClass().getName() + ".logout ok!!");
		
		session.removeAttribute("SS_USER_ID");

		return "/user/Logout";
	}
	
	// 회원정보 가져오기
	@RequestMapping(value = "user/getUserInfo", method = RequestMethod.POST)
	public String getUserInfo(HttpSession session, HttpServletRequest request, HttpServletResponse response,
			ModelMap model) throws Exception {

		log.info(this.getClass().getName() + "updateUserInfo start!");

		// 로그인 아이디
		String user_id = CmmUtil.nvl((String) session.getAttribute("SS_USER_ID"));

		// 입력한 비밀번호
		String password = CmmUtil.nvl((String) request.getParameter("password"));

		// 웹(회원정보 입력화면)에서 받는 정보를 저장할 변수
		UserInfoDTO pDTO = new UserInfoDTO();

		// 접속한 아이디
		pDTO.setUser_id(user_id);
		// 비밀번호는 절대로 복호화되지 않도록 해시 알고리즘으로 암호화함
		pDTO.setPassword(EncryptUtil.encHashSHA256(password));

		// 확인 결과 가져오기
		Map<String, String> rMap = userInfoService.getUserInfo(pDTO);
		
		model.addAttribute("rMap", rMap);
		
		log.info(rMap);
		
		// 메모리 정리
		pDTO = null;
		rMap = null;

		log.info(this.getClass().getName() + "updateUserInfo end!");

		return "/user/UserInfoUpdate";
	}
	
	// 회원 본인 확인
	@RequestMapping(value = "user/UserExam")
	public String userExam() {
		log.info(this.getClass().getName() + ".UserExam ok!!");

		return "/user/UserExam";
	}
	
	/**
	 * 회원정보 업데이트
	 */
	@RequestMapping(value = "user/UserUpdate", method = RequestMethod.POST)
	public String userUpdate(HttpSession session, HttpServletRequest request, HttpServletResponse response, ModelMap model)
			throws Exception {

		log.info(this.getClass().getName() + ".userUpdate start...");

		// 회원가입 결과에 대한 메시지를 전달할 변수
		// 업데이트 결과(0 실패, 1 성공)
		String res = "";

		// 웹(회원정보 입력화면)에서 받는 정보를 저장할 변수
		UserInfoDTO pDTO = null;

		try {
			
			String user_id = CmmUtil.nvl(request.getParameter("user_id")); // 아이디
			String user_name = CmmUtil.nvl(request.getParameter("user_name")); // 이름
			String password = CmmUtil.nvl(request.getParameter("password")); // 비밀번호
			String email = CmmUtil.nvl(request.getParameter("email").toString()); // 이메일
			
			log.info("user_id : " + user_id);
			log.info("user_name : " + user_name);
			//log.info("password : " + password);
			log.info("email : " + email);

			// 웹(회원정보 입력화면)에서 받는 정보를 저장할 변수를 메모리에 올리기
			pDTO = new UserInfoDTO();

			pDTO.setUser_id(user_id);
			pDTO.setUser_name(user_name);
			
			// 변경할 비밀번호가 있으면
			if(!"".equals(password)) {
				// 비밀번호는 절대로 복호화되지 않도록 해시 알고리즘으로 암호화함
				pDTO.setPassword(EncryptUtil.encHashSHA256(password));
			}
			
			// 민감 정보인 이메일은 AES128-CBC로 암호화함
			pDTO.setEmail(EncryptUtil.encAES128CBC(email));
			
			pDTO.setChg_id(user_id);
			pDTO.setChg_dt(DateUtil.getDateTime("yyyyMMddhhmmss"));

			/*
			 * 업데이트 결과(0 실패, 1 성공)
			 */
			res = userInfoService.userUpdate(pDTO);
			
			// 이메일 발송
			if(!"".equals(email)) {
				
				log.info("메시지 보낼 이메일 : " + email);
				
				MailDTO eDTO = new MailDTO();
				
				String toMail = email;
				String title = "Eword 회원정보가 수정 되었습니다.";
				String content = user_id + "님 회원정보가 수정 되었습니다. 본인이 아니시라면 관리자에게 문의 하시기 바랍니다.\n"
						+ "Eword http://13.124.9.63:8080/main.do\n";
				
				eDTO.setToMail(toMail);
				eDTO.setTitle(title);
				eDTO.setContents(content);

				int sendRes = mailService.doSendMail(eDTO);
				
				log.info("메일 전송 결과 : " + sendRes);
				
				eDTO = null;
				
			}
			
		} catch (Exception e) {
			
			// 예외발생
			res = e.toString();
						
			log.info(e.toString());
						
			e.printStackTrace();

		} finally {
			
			model.addAttribute("res", res);
			
			log.info(this.getClass().getName() + ".userUpdate end!");

			// 변수 초기화(메모리 효율화 시키기 위해 초기화)
			pDTO = null;

		}

		return "/user/MsgUpdateResult";

	}
	
	/**
	 * 회원 탈퇴
	 */
	@RequestMapping(value = "user/deleteUser")
	public String deleteUser(HttpSession session, HttpServletRequest request, HttpServletResponse response, ModelMap model)
			throws Exception {
		
		log.info(this.getClass().getName() + ".deleteUser start...");
		
		// 탈퇴 결과(2 성공, 3 실패)
		String res = "3";
				
		try {
			
			String user_id = CmmUtil.nvl(request.getParameter("user_id")); // 아이디
			String email = CmmUtil.nvl(request.getParameter("user_email")); // 이메일
						
			log.info("user_id : " + user_id);
			
			res = userInfoService.deleteUser(user_id);
			
			log.info("회원 탈퇴 결과 : " + res);
			
			// 이메일 발송
			if(!"".equals(email)) {
				
				log.info("메시지 보낼 이메일 : " + email);
				
				MailDTO eDTO = new MailDTO();
				
				String toMail = email;
				String title = "Eword 회원탈퇴 처리 되었습니다.";
				String content = user_id + "님 회원탈퇴 처리 되었습니다. 그동안 이용해 주셔서 감사합니다.\n"
						+ "Eword http://13.124.9.63:8080/main.do\n";
				
				eDTO.setToMail(toMail);
				eDTO.setTitle(title);
				eDTO.setContents(content);

				int sendRes = mailService.doSendMail(eDTO);
				
				log.info("메일 전송 결과 : " + sendRes);
				
				eDTO = null;
				
			}
			
		} catch (Exception e) {
			
			// 예외 발생
			res = e.toString();
			
			e.printStackTrace();
			
			log.info(e.toString());
			
		} finally {
			
			session.removeAttribute("SS_USER_ID");
			model.addAttribute("res", res);
			
			log.info(this.getClass().getName() + ".deleteUser end!");
			
		}
		
		return "/user/MsgUpdateResult";
		
	}
	
	

}
