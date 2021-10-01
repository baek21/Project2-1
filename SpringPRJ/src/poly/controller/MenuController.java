package poly.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MenuController {
	private Logger log = Logger.getLogger(this.getClass());
	
	// 모든 웹 페이지에 포함할 페이지
	@RequestMapping(value = "SideMenu")
	public String sideMenu(HttpServletRequest request, HttpServletResponse response) throws Exception {
		log.info(this.getClass().getName() + ".sideMenu start!");
		log.info(this.getClass().getName() + ".sideMenu end!");
		return "/SideMenu";

	}
	// 메인 페이지 설정
	@RequestMapping(value = "main")
	public String main(HttpServletRequest request, HttpServletResponse response) throws Exception {
		log.info(this.getClass().getName() + ".main start!");
		log.info(this.getClass().getName() + ".main end!");
		return "/news/NewsCloud";
		
	}
	
}
