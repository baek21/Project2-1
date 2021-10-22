<%@page import="java.net.URLEncoder"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>카카오 로그인</title>
</head>
<body>
<%
String client_id = "";// REST API 키;
String redirect_uri = URLEncoder.encode("http://localhost:8090/user/KakaoCallback.do", "UTF-8");
String response_type = "code";
String kakaoApiURL;
kakaoApiURL = "https://kauth.kakao.com/oauth/authorize?";
kakaoApiURL += "client_id=" + client_id;
kakaoApiURL += "&redirect_uri=" + redirect_uri;
kakaoApiURL += "&response_type=" + response_type;
%>
</body>
</html>