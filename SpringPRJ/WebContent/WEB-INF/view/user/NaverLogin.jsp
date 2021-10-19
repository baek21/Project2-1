<%@page import="java.math.BigInteger"%>
<%@page import="java.security.SecureRandom"%>
<%@page import="java.net.URLEncoder"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
	<meta charset="UTF-8">
	<title>네이버 로그인</title>
</head>
<body>
  <%
    String clientId = "Zr9qkKrxbEI0orbLK8fF";//애플리케이션 클라이언트 아이디값";
    String redirectURI = URLEncoder.encode("http://localhost:8090/user/NaverCallback.do", "UTF-8");
    SecureRandom random = new SecureRandom();
    String state = new BigInteger(130, random).toString();
    String navarApiURL = "https://nid.naver.com/oauth2.0/authorize?response_type=code";
    navarApiURL += "&client_id=" + clientId;
    navarApiURL += "&redirect_uri=" + redirectURI;
    navarApiURL += "&state=" + state;
    session.setAttribute("state", state);
 %>
</body>
</html>