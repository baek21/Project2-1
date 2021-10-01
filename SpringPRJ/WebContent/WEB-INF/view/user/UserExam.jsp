<%@include file="/WEB-INF/view/SideMenu.jsp"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%

%>

<title>본인 확인</title>

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
		basicCard += 		'<h1 class="h4 text-gray-900 mb-4">본인 확인</h1>';
		basicCard += 	'</div>';
		basicCard += 	'<form name="f" class="user" method="post" action="/user/getUserInfo.do">';
		basicCard += 		'<div class="form-group">';
		basicCard += 			'<input type="password" name="password" class="form-control form-control-user" id="inputPassword" placeholder="비밀번호" required>';
		basicCard += 		'</div>';
		basicCard += 		'<button type="submit" class="btn btn-primary btn-user btn-block">확인</button>'
		basicCard += 	'</form>';
		basicCard += '</div></center>';
	
		
		$("#content").append(basicCard);
	}

</script>