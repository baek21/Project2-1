<%@include file="/WEB-INF/view/SideMenu.jsp"%>
<%@include file="/WEB-INF/view/user/NaverLogin.jsp"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<title>로그인</title>
<!-- 자바스크립트 -->
<script type="text/javascript">

	//페이지 로딩 완료 후 실행함
	$(window).on("load", function(){
		makeCard();
	})

	//카드 만들기
	function makeCard() {
	
		var basicCard = '';
	
		//Basic Card
		basicCard += '<center><div class="p-5 bg-white shadow" style="max-width: 600px; border-radius: 1em;">';
		basicCard += 	'<div class="text-center">';
		basicCard += 		'<h1 class="h4 text-gray-900 mb-4">로그인</h1>';
		basicCard += 	'</div>';
		basicCard += 	'<form name="f" class="user" method="post" action="/user/getUserLoginCheck.do">';
		basicCard += 		'<div class="form-group">';
		basicCard +=	 		'<input type="text" name="user_id" class="form-control form-control-user" id="inputUserId" placeholder="아이디" required autofocus>';
		basicCard += 		'</div>';
		basicCard += 		'<div class="form-group">';
		basicCard += 			'<input type="password" name="password" class="form-control form-control-user" id="inputPassword" placeholder="비밀번호" required>';
		basicCard += 		'</div>';
		basicCard += 		'<button type="submit" class="btn btn-primary btn-block">로그인</button>'
		basicCard += 	'</form>';
		basicCard += 	'<a href="<%=apiURL%>"><img height="35" src="/img/NaverLoginButton.png"/></a>';
		basicCard += 	'<hr>';
		basicCard += 	'<div class="text-center">';
		basicCard += 		'<a class="small" href="forgot-password.html">비밀번호 찾기</a>';
		basicCard += 	'</div>';
		basicCard += 	'<div class="text-center">';
		basicCard += 		'<a class="small" href="/user/UserRegForm.do">회원가입</a>';
		basicCard += 	'</div>';
		basicCard += '</div></center>';
	
		
		$("#content").append(basicCard);
	}

</script>