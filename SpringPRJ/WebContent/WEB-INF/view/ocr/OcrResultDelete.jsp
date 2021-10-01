<%@page import="poly.util.CmmUtil"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
        
<%
// Controller로부터 전달받은 데이터
String res = CmmUtil.nvl((String)request.getAttribute("DeleteResult"));
%>

<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<title>처리페이지</title>
	<script type="text/javascript">

		if ("<%=res%>" == "0") {
			
			alert("삭제 성공했습니다.");
			top.location.href="/ocr/OcrResult_List.do";
			
		} else if ("<%=res%>" == "1") {
			
			alert("삭제 실패했습니다. 아이디가 일치하지 않습니다.");
			top.location.href="/ocr/OcrResult_List.do";
		
		} else if ("<%=res%>" == "2"){
			
			alert("로그인 후 이용 가능합니다.");
			top.location.href="/user/LoginForm.do";
		
		} else {
			
			alert("오류가 발생했습니다.");
			top.location.href="/ocr/OcrResult_List.do";
	
		}
		
	</script>
</head>
<body>

</body>
</html>