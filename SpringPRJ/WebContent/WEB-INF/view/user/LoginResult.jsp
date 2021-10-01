<%@page import="poly.util.CmmUtil"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
	//controller에 저장된 세션으로 로그인할 때 생성됨
	// 로그인 성공 : 1, 아이디, 비밀번호 불일치로 인한 실패 : 0, 시스템 에러 : 2
	String SS_USER_ID = CmmUtil.nvl((String)session.getAttribute("SS_USER_ID"));
	String res = CmmUtil.nvl((String)request.getAttribute("res").toString());
%>

<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<title>로그인</title>
	<script type="text/javascript">
		
			if (<%=res%> == "0") {
				// 로그인 성공
				top.location.href="/main.do"; // 메인 페이지
				
			} else if (<%=res%> == "1") {
				// 비밀번호 일치하지 않음
				alert("가입하지 않은 아이디이거나, 잘못된 비밀번호입니다.");
				top.location.href="/user/LoginForm.do"; // 로그인 페이지
			
			} else if ((<%=res%> == "2")){
				// 등록되지 않은 아이디
				alert("가입하지 않은 아이디이거나, 잘못된 비밀번호입니다.");
				top.location.href="/user/LoginForm.do"; // 로그인 페이지
			
			} else {
			
				alert("오류가 발생했습니다.");
				top.location.href="//user/LoginForm.do"; // 로그인 페이지
			}
			
		</script>
</head>
<body>
</body>
</html>