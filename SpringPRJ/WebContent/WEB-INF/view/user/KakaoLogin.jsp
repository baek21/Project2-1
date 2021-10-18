<%@page import="java.net.URLEncoder"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
</head>
<body>
<%
String client_id = "110a9e0b91af88905005827ac500c075";// 앱 REST API 키;
String redirect_uri = URLEncoder.encode("http://localhost:8090/user/KakaoCallback.do", "UTF-8");
String response_type = "code";
String apiURL;
apiURL = "https://kauth.kakao.com/oauth/authorize?";
apiURL += "client_id=" + client_id;
apiURL += "&redirect_uri=" + redirect_uri;
apiURL += "&response_type=" + response_type;
%>
<a href="<%=apiURL %>">로그인</a>
</body>
</html>