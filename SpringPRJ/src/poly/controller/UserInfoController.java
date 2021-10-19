package poly.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
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
			if (!"".equals(email)) {

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
	public String loginForm(HttpSession session) {
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

		// 로그인 처리 결과를 저장할 변수(로그인 성공 : 0, 아이디, 비밀번호 불일치로 인한 실패 : 1, 가입되지 않음 : 2, 시스템 에러:
		// 3)
		int res = 999;

		// 웹(회원정보 입력화면)에서 받는 정보를 저장할 변수
		UserInfoDTO pDTO = null;

		try {

			String user_id = CmmUtil.nvl(request.getParameter("user_id")); // 아이디
			String password = CmmUtil.nvl(request.getParameter("password")); // 비밀번호

			log.info("user_id : " + user_id);
			// log.info("password : " + password);

			// 웹(회원정보 입력화면)에서 받는 정보를 저장할 변수를 메모리에 올리기
			pDTO = new UserInfoDTO();

			pDTO.setUser_id(user_id);

			// 비밀번호는 절대로 복호화되지 않도록 해시 알고리즘으로 암호화함
			pDTO.setPassword(EncryptUtil.encHashSHA256(password));

			// 로그인을 위해 아이디와 비밀번호가 일치하는지 확인하기 위한 userInfoService 호출하기
			res = userInfoService.getUserLoginCheck(pDTO);

			if (res == 0) { // 로그인 성공

				session.setAttribute("SS_USER_ID", user_id);
				session.setAttribute("BLIND_ID", user_id.substring(0, 4) + "****");
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
		session.removeAttribute("BLIND_ID");

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
	public String userUpdate(HttpSession session, HttpServletRequest request, HttpServletResponse response,
			ModelMap model) throws Exception {

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
			// log.info("password : " + password);
			log.info("email : " + email);

			// 웹(회원정보 입력화면)에서 받는 정보를 저장할 변수를 메모리에 올리기
			pDTO = new UserInfoDTO();

			pDTO.setUser_id(user_id);
			pDTO.setUser_name(user_name);

			// 변경할 비밀번호가 있으면
			if (!"".equals(password)) {
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
			if (!"".equals(email)) {

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
	public String deleteUser(HttpSession session, HttpServletRequest request, HttpServletResponse response,
			ModelMap model) throws Exception {

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
			if (!"".equals(email)) {

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
			session.removeAttribute("BLIND_ID");
			model.addAttribute("res", res);

			log.info(this.getClass().getName() + ".deleteUser end!");

		}

		return "/user/MsgUpdateResult";

	}

	// 네이버 아이디로 로그인
	@RequestMapping(value = "user/NaverLogin")
	public String naverLogin() {
		log.info(this.getClass().getName() + ".user/NaverLogin ok");

		return "/user/NaverLogin";
	}

	// 네이버 아이디로 로그인 결과
	@RequestMapping(value = "user/NaverCallback")
	public String naverCallback(HttpSession session, HttpServletRequest request, HttpServletResponse response,
			ModelMap model) throws Exception {
		log.info(this.getClass().getName() + ".naverCallback start");

		String clientId = "Zr9qkKrxbEI0orbLK8fF";// 애플리케이션 클라이언트 아이디값";
		String clientSecret = "AZKCuLBjcT";// 애플리케이션 클라이언트 시크릿값";
		String code = request.getParameter("code");
		String state = request.getParameter("state");
		String redirectURI = URLEncoder.encode("http://localhost:8090/user/NaverCallback.do", "UTF-8");
		String apiURL;
		apiURL = "https://nid.naver.com/oauth2.0/token?grant_type=authorization_code&";
		apiURL += "client_id=" + clientId;
		apiURL += "&client_secret=" + clientSecret;
		apiURL += "&redirect_uri=" + redirectURI;
		apiURL += "&code=" + code;
		apiURL += "&state=" + state;
		String access_token = "";
		String refresh_token = "";

		log.info("apiURL=" + apiURL);

		try {
			URL url = new URL(apiURL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			int responseCode = con.getResponseCode();
			BufferedReader br;
			log.info("responseCode=" + responseCode);
			if (responseCode == 200) { // 정상 호출
				br = new BufferedReader(new InputStreamReader(con.getInputStream()));
			} else { // 에러 발생
				br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			}
			String inputLine;
			StringBuffer res = new StringBuffer();
			while ((inputLine = br.readLine()) != null) {
				res.append(inputLine);
			}
			br.close();
			if (responseCode == 200) {

				// res 파싱
				JSONParser parser = new JSONParser();
				Object obj = parser.parse(res.toString());
				JSONObject jsonObj = (JSONObject) obj;

				// token 값
				access_token += (String) jsonObj.get("access_token");
				refresh_token += (String) jsonObj.get("refresh_token");
				log.info("access_token 값 : " + access_token);
				log.info("refresh_token 값 : " + refresh_token);

				// 프로필 값
				// https://developers.naver.com/docs/login/profile/profile.md 참고
				// 이메일 수집 동의 여부에 따라서 이메일 key 값이 없을 수 있음 / NullPointerException 조심
				String naverProfile = profile(access_token);
				log.info("naverProfile 값 : " + naverProfile);
				
				jsonObj = (JSONObject) parser.parse(naverProfile);
				JSONObject jsonObj2 = (JSONObject) jsonObj.get("response");

				String id = (String) jsonObj2.get("id");
				String name = (String) jsonObj2.get("name");
				String email = "";
				// 이메일 값이 있다면
				if(!"null".equals(String.valueOf(jsonObj2.get("email")))) {
					email = jsonObj2.get("email").toString();
				}
				
				log.info("id : " + id);
				log.info("email : " + email);
				log.info("name : " + name);
				
				Map<String, String> rMap = new HashMap<String, String>();
				
				rMap.put("id", id);
				rMap.put("email", EncryptUtil.encAES128CBC(email));
				rMap.put("name", name);
				rMap.put("sns_type", "naver");
				rMap.put("reg_dt", DateUtil.getDateTime("yyyyMMddhhmmss"));
				rMap.put("chg_dt", DateUtil.getDateTime("yyyyMMddhhmmss"));
            
				log.info(rMap.get("id"));
				log.info(rMap.get("email"));
				log.info(rMap.get("name"));
				log.info(rMap.get("sns_type"));
				log.info(rMap.get("reg_dt"));
				log.info(rMap.get("chg_dt"));
            
				// 회원가입 결과
				// 0 성공, 1 중복 아이디(본인확인), 2 시스템 에러
				int result = userInfoService.insertSnsUserInfo(rMap);

				if (!"".equals(email) && result == 0) {// 이메일 발송

					log.info("메시지 보낼 이메일 : " + email);

					MailDTO eDTO = new MailDTO();

					String toMail = email;
					String title = "Eword 회원가입을 환영합니다.";
					String content = name + "님 회원가입을 환영합니다. 본인이 아니시라면 관리자에게 문의 하시기 바랍니다.\n"
							+ "Eword http://13.124.9.63:8080/main.do\n";

					eDTO.setToMail(toMail);
					eDTO.setTitle(title);
					eDTO.setContents(content);

					// 결과(0 실패, 1 성공)
					int sendRes = mailService.doSendMail(eDTO);

					log.info("메일 전송 결과 : " + sendRes);

					eDTO = null;

					session.setAttribute("SS_USER_ID", rMap.get("id"));

				}
				if (result != 2) {

					log.info("회원정보 확인 후 세션 적용");
					session.setAttribute("SS_USER_ID", rMap.get("id"));
					session.setAttribute("BLIND_ID", rMap.get("id").substring(0, 4) + "****");
				}
				model.addAttribute("res", String.valueOf(result));
				rMap = null;
			}
		} catch (Exception e) {
			log.info("오류 코드 : " + e);
		} finally {
			log.info(this.getClass().getName() + ".naverCallback end");
		}

		return "/user/NaverCallback";
	}

	// ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ 네이버 회원 프로필 조회 함수들 ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓
	public static String profile(String accessToken) {

		// accessToken(네이버 로그인 접근 토큰);
		String header = "Bearer " + accessToken; // Bearer 다음에 공백 추가

		String apiURL = "https://openapi.naver.com/v1/nid/me";

		Map<String, String> requestHeaders = new HashMap<>();
		requestHeaders.put("Authorization", header);
		String responseBody = get(apiURL, requestHeaders);

		// System.out.println(responseBody);

		return responseBody;
	}

	private static String get(String apiUrl, Map<String, String> requestHeaders) {
		HttpURLConnection con = connect(apiUrl);
		try {
			con.setRequestMethod("GET");
			for (Map.Entry<String, String> header : requestHeaders.entrySet()) {
				con.setRequestProperty(header.getKey(), header.getValue());
			}

			int responseCode = con.getResponseCode();
			if (responseCode == HttpURLConnection.HTTP_OK) { // 정상 호출
				return readBody(con.getInputStream());
			} else { // 에러 발생
				return readBody(con.getErrorStream());
			}
		} catch (IOException e) {
			throw new RuntimeException("API 요청과 응답 실패", e);
		} finally {
			con.disconnect();
		}
	}

	private static HttpURLConnection connect(String apiUrl) {
		try {
			URL url = new URL(apiUrl);
			return (HttpURLConnection) url.openConnection();
		} catch (MalformedURLException e) {
			throw new RuntimeException("API URL이 잘못되었습니다. : " + apiUrl, e);
		} catch (IOException e) {
			throw new RuntimeException("연결이 실패했습니다. : " + apiUrl, e);
		}
	}

	private static String readBody(InputStream body) {
		InputStreamReader streamReader = new InputStreamReader(body);

		try (BufferedReader lineReader = new BufferedReader(streamReader)) {
			StringBuilder responseBody = new StringBuilder();

			String line;
			while ((line = lineReader.readLine()) != null) {
				responseBody.append(line);
			}

			return responseBody.toString();
		} catch (IOException e) {
			throw new RuntimeException("API 응답을 읽는데 실패했습니다.", e);
		}
	}
	// ↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑ 네이버 회원 프로필 조회 함수들 ↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑

	// 카카오로 로그인
	@RequestMapping(value = "user/KakaoLogin")
	public String kakaoLogin(HttpSession session, HttpServletRequest request, HttpServletResponse response,
			ModelMap model) throws Exception {
		log.info(this.getClass().getName() + ".kakaoLogin start");

		log.info(this.getClass().getName() + ".kakaoLogin end");

		return "/user/KakaoLogin";
	}

	// 카카오로 로그인 결과
	@RequestMapping(value = "user/KakaoCallback")
	public String kakaoCallback(HttpSession session, HttpServletRequest request, HttpServletResponse response,
			ModelMap model) throws Exception {
		log.info(this.getClass().getName() + ".kakaoCallback start");
		
		// 토큰 받기
		// parameter 값
		String grant_type = "authorization_code";
		String client_id = "110a9e0b91af88905005827ac500c075";// REST API 키;
		String redirect_uri = URLEncoder.encode("http://localhost:8090/user/KakaoCallback.do", "UTF-8");
		String code = request.getParameter("code");// 사용자가 [동의하고 계속하기] 선택, 로그인 진행 시 얻은 토큰 요청 인가 코드
		
		String apiURL;
		apiURL = "https://kauth.kakao.com/oauth/token?";
		apiURL += "grant_type=" + grant_type;
		apiURL += "&client_id=" + client_id;
		apiURL += "&redirect_uri=" + redirect_uri;
		apiURL += "&code=" + code;
		log.info("apiURL=" + apiURL);
		
		// Response 값
		String access_token = ""; // 사용자 액세스 토큰 값
		String refresh_token = ""; // 사용자 리프레시 토큰 값
		
		try {
			URL url = new URL(apiURL);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			int responseCode = con.getResponseCode();
			log.info("responseCode : " + responseCode);
			BufferedReader br;
			if (responseCode == 200) { // 정상 호출
				br = new BufferedReader(new InputStreamReader(con.getInputStream()));
			} else { // 에러 발생
				br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			}
			String inputLine;
			StringBuffer res = new StringBuffer();
			while ((inputLine = br.readLine()) != null) {
				res.append(inputLine);
			}
			br.close();
			
			if (responseCode == 200) {
				
				// 결과값 파싱
				JSONParser parser = new JSONParser();
				Object obj = parser.parse(res.toString());
				JSONObject jsonObj = (JSONObject) obj;
				
				// access_token 값
				access_token += (String) jsonObj.get("access_token");
				refresh_token += (String) jsonObj.get("refresh_token");
				log.info("access_token 값 : " + access_token);
				log.info("refresh_token 값 : " + refresh_token);
				
				//{"id":1954933070,"connected_at":"2021-10-19T03:12:51Z","properties":{"nickname":"백준호"},"kakao_account":{"profile_nickname_needs_agreement":false,"profile":{"nickname":"백준호"},"has_email":true,"email_needs_agreement":true}}
				//{"id":1954933070,"connected_at":"2021-10-19T08:16:05Z","properties":{"nickname":"백준호"},"kakao_account":{"profile_nickname_needs_agreement":false,"profile":{"nickname":"백준호"},"has_email":true,"email_needs_agreement":false,"is_email_valid":true,"is_email_verified":true,"email":"baek_0101@naver.com"}}
				// kakaoProfile 함수로 프로필 값 가져오기
				// 이메일 수집 동의 여부에 따라서 이메일 key 값이 없을 수 있음 / NullPointerException 조심
				String kakaoProfile = kakaoProfile(access_token);
				
				log.info("kakaoProfile 값 : " + kakaoProfile);
				
				jsonObj = (JSONObject) parser.parse(kakaoProfile);
				JSONObject jsonObj2 = (JSONObject) jsonObj.get("kakao_account");
				
				String id = jsonObj.get("id").toString();
				String nickname = ((JSONObject) jsonObj2.get("profile")).get("nickname").toString();
				String email = "";
				
				// 이메일 값이 있다면
				if(!"null".equals(String.valueOf(jsonObj2.get("email")))) {
					email = jsonObj2.get("email").toString();
				}
								
				log.info("id : " + id);
				log.info("nickname : " + nickname);
				log.info("email : " + email);
				
				Map<String, String> rMap = new HashMap<String, String>();
				
				rMap.put("id", id);
				rMap.put("email", EncryptUtil.encAES128CBC(email));
				rMap.put("name", nickname);
				rMap.put("sns_type", "kakao");
				rMap.put("reg_dt", DateUtil.getDateTime("yyyyMMddhhmmss"));
				rMap.put("chg_dt", DateUtil.getDateTime("yyyyMMddhhmmss"));
            
				log.info(rMap.get("id"));
				log.info(rMap.get("email"));
				log.info(rMap.get("name"));
				log.info(rMap.get("sns_type"));
				log.info(rMap.get("reg_dt"));
				log.info(rMap.get("chg_dt"));
            
				// 회원가입 결과
				// 0 성공, 1 중복 아이디(본인확인), 2 시스템 에러
				int result = userInfoService.insertSnsUserInfo(rMap);

				if (!"".equals(email) && result == 0) {// 이메일 발송

					log.info("메시지 보낼 이메일 : " + email);

					MailDTO eDTO = new MailDTO();

					String toMail = email;
					String title = "Eword 회원가입을 환영합니다.";
					String content = nickname + "님 회원가입을 환영합니다. 본인이 아니시라면 관리자에게 문의 하시기 바랍니다.\n"
							+ "Eword http://13.124.9.63:8080/main.do\n";

					eDTO.setToMail(toMail);
					eDTO.setTitle(title);
					eDTO.setContents(content);

					// 결과(0 실패, 1 성공)
					int sendRes = mailService.doSendMail(eDTO);

					log.info("메일 전송 결과 : " + sendRes);

					eDTO = null;

					session.setAttribute("SS_USER_ID", rMap.get("id"));

				}
				if (result != 2) {

					log.info("회원정보 확인 후 세션 적용");
					session.setAttribute("SS_USER_ID", rMap.get("id"));
					session.setAttribute("BLIND_ID", rMap.get("id").substring(0, 4) + "****");
				}
				model.addAttribute("res", String.valueOf(result));
				rMap = null;
			}
		} catch (Exception e) {
			log.info("오류 코드 : " + e);
		} finally {
			log.info(this.getClass().getName() + ".kakaoCallback end");
		}
		
		return "/user/KakaoCallback";
	}
	// ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ 카카오 회원 프로필 조회 함수 ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓ ↓
	public static String kakaoProfile(String access_token) throws Exception{

		URL url = new URL("https://kapi.kakao.com/v2/user/me");
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setRequestMethod("GET");
		
		// 요청에 필요한 Header에 포함될 내용
        con.setRequestProperty("Authorization", "Bearer " + access_token);
		
		int responseCode = con.getResponseCode();
		BufferedReader br;
		if (responseCode == 200) { // 정상 호출
			br = new BufferedReader(new InputStreamReader(con.getInputStream()));
		} else { // 에러 발생
			br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
		}
		String inputLine;
		StringBuffer res = new StringBuffer();
		while ((inputLine = br.readLine()) != null) {
			res.append(inputLine);
		}
		br.close();
			
			
		return res.toString();
	}
	// ↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑ 카카오 회원 프로필 조회 함수 ↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑ ↑

}
