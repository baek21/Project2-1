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
	<title>회원정보 수정</title>
	<script type="text/javascript">

		// 회원정보 수정 결과 (0 실패, 1 성공, 시스템 에러)
		if("<%=res%>" == "0"){
			
			alert("회원정보 수정 실패했습니다.");
			top.location.href="/user/UserExam.do"; // 본인 확인 페이지
			
		} else if("<%=res%>" == "1"){
			
			alert("회원정보 수정 성공했습니다.");
			top.location.href="/user/UserExam.do"; // 본인 확인 페이지
			
		// 회원탈퇴 결과 (2 성공, 3 실패, 시스템에러)
		} else if("<%=res%>" == "2"){
			
			alert("탈퇴처리가 완료되었습니다.");
			top.location.href="/main.do"; // 메인 페이지
			
		} else  if("<%=res%>" == "3"){
			
			alert("탈퇴처리가 실패했습니다.");
			top.location.href="/main.do"; // 메인 페이지
			
		} else {
			
			alert("오류가 발생했습니다 ");
			top.location.href="/main.do"; // 본인 확인 페이지
		}
	</script>
</head>
<body>
</body>
</html>