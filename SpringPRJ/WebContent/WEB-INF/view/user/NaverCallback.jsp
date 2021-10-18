<%@page import="poly.util.CmmUtil"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
	//controller에 저장된 세션으로 로그인할 때 생성됨
	// 로그인 성공 : 0, 1
	// 시스템 에러 : 2
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
			
			top.location.href="/main.do"; // 로그인 페이지
			
		} else if (<%=res%> == "1") {
			
			top.location.href="/main.do"; // 회원가입 페이지
	
		} else {
			
			alert("오류가 발생했습니다.");
			top.location.href="/user/UserRegForm.do"; // 회원가입 페이지
		}
	</script>
</head>
<body>
</body>
</html>