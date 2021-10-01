<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@page import="poly.util.CmmUtil"%>
<%@page import="poly.dto.UserInfoDTO"%>
<%
//Controller로부터 전달받은 데이터
String res = CmmUtil.nvl((String)request.getAttribute("res"));
%>
<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<title>회원가입</title>
	<script type="text/javascript">
		if (<%=res%> == "0") {
			
			alert("회원가입이 성공했습니다.");
			top.location.href="/user/LoginForm.do"; // 로그인 페이지
			
		} else if (<%=res%> == "1") {
			
			alert("이미 가입된 아이디입니다.");
			top.location.href="/user/UserRegForm.do"; // 회원가입 페이지
	
		} else {
			
			alert("오류가 발생했습니다.");
			top.location.href="/user/UserRegForm.do"; // 회원가입 페이지
		}
	</script>
</head>
<body>
</body>
</html>