<%@include file="/WEB-INF/view/SideMenu.jsp"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>

<title>회원가입</title>

<script>
	//페이지 로딩 완료 후 실행함
	$(window).on("load", function(){
		makeCard();
	})
	
	//회원가입 정보의 유효성 체크하기
	function doRegUserCheck(f){
		
		if(f.user_id.value.length<4){
			alert("아이디를 4자리 이상 입력하세요.");
			f.user_id.focus();
			return false;
		}
		if(f.user_name.value==""){
			alert("이름를 입력하세요.");
			f.user_name.focus();
			return false;
		}
		if(f.password.value.length<4){
			alert("비밀번호를 4자리 이상 입력하세요.");
			f.password.focus();
			return false;
		}
		if(f.password2.value==""){
			alert("비밀번호을 입력하세요.");
			f.password2.focus();
			return false;
		}
		if(f.password.value!=f.password2.value){
			alert("비밀번호가 일치하지 않습니다.")
			f.password.focus();
			return false;
		}
	}

	//카드 만들기
	function makeCard() {
	
		var basicCard = '';
	
		//Basic Card
		basicCard +='<center>';
		basicCard +='<div class="p-5 bg-white shadow" style="max-width: 600px; border-radius: 1em;">';
		basicCard +=	'<div class="text-center">';
		basicCard +=		'<h1 class="h4 text-gray-900 mb-4">회원가입</h1>';
		basicCard +=	'</div>';
		basicCard +=	'<form name="f" method="post" class="user" action="/user/insertUserInfo.do" onsubmit="return doRegUserCheck(this);">';
		basicCard +=		'<div class="form-group">';
		basicCard +=			'<input type="text" name = "user_id" class="form-control form-control-user" id="userId" placeholder="아이디 입력 ( 4자리 이상 )" required autofocus>';
		basicCard +=		'</div>';
		basicCard +=		'<div class="form-group">';
		basicCard +=			'<input type="text" name = "user_name" class="form-control form-control-user" id="userName" placeholder="이름 입력" required>';
		basicCard +=		'</div>';
		basicCard +=		'<div class="form-group">';
		basicCard +=			'<input type="password" name = "password" class="form-control form-control-user" id="userInputPassword" placeholder="비밀번호 입력 ( 4자리 이상 )" required>';
		basicCard +=		'</div>';
		basicCard +=		'<div class="form-group">';
		basicCard +=			'<input type="password" name = "password2" class="form-control form-control-user" id="userRepeatPassword" placeholder="위와 동일한 비밀번호 입력" required>';
		basicCard +=		'</div>';
		basicCard +=		'<div class="form-group">';
		basicCard +=		'<input type="email" name = "email" class="form-control form-control-user" id="userInputEmail" placeholder="( 선택 사항 ) 이메일 입력">';
		basicCard +=		'</div>';
		basicCard +=		'<button type="submit" class="btn btn-primary btn-user btn-block">회원가입</button>';
		basicCard +=	'</form>';
		basicCard +=	'<hr>';
		basicCard +=	'<div class="text-center">';
		basicCard +=		'<a class="small" href="forgot-password.html">비밀번호 찾기</a>';
		basicCard +=	'</div>';
		basicCard +=	'<div class="text-center">';
		basicCard +=		'<a class="small" href="/user/LoginForm.do">로그인</a>';
		basicCard +=	'</div>';
		basicCard +='</div>';
		basicCard +='</center>';
	
		
		$("#content").append(basicCard);
	}
</script>