<%@page import="java.util.HashMap"%>
<%@page import="java.util.Map"%>
<%@include file="/WEB-INF/view/SideMenu.jsp"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
Map<String, String> rMap = (HashMap<String, String>) request.getAttribute("rMap");
if(rMap==null){
	rMap = new HashMap<String, String>();
}
%>

<title>회원정보 수정</title>

<!-- 자바스크립트 -->
<script type="text/javascript">
	// 본인 확인 결과
	if("<%=rMap.get("result")%>" == "0") {
		
		alert("비밀번호가 일치하지 않습니다.");
		top.location.href="/user/UserExam.do"; // 본인 확인 페이지
		
	} else if("<%=rMap.get("result")%>" == "1") {
		
		makeCard();
		
	} else {
		
		alert("잘못된 접근입니다.");
		top.location.href="/main.do"; // 본인 확인 페이지
	}
	
	//회원가입 정보의 유효성 체크하기
	function doRegUserCheck(f){
		
		if(f.user_id.value==""){
			alert("아이디를 입력하세요.");
			f.user_id.focus();
			return false;
		}
		if(f.user_name.value==""){
			alert("이름를 입력하세요.");
			f.user_name.focus();
			return false;
		}	
		if(f.password.value!=f.password2.value){
			alert("비밀번호가 일치하지 않습니다.")
			f.password.focus();
			return false;
		}
	}
	
	// 회원 탈퇴
	function deleteUser(){
		if(confirm("탈퇴 하시겠습니까?")){
			location.href="/user/deleteUser.do?user_id=<%=rMap.get("user_id")%>&user_email=<%=rMap.get("user_email")%>";
		}
		
	}
	
	
	//카드 만들기
	function makeCard() {
	
		var basicCard = '';
	
		//Basic Card
		basicCard +='<center>';
		basicCard +='<div class="p-5 bg-white shadow" style="max-width: 600px; border-radius: 1em;">';
		basicCard +=	'<div class="text-center">';
		basicCard +=		'<h1 class="h4 text-gray-900 mb-4">회원정보 관리</h1>';
		basicCard +=	'</div>';
		basicCard +=	'<form name="f" method="post" class="user" action="/user/UserUpdate.do" onsubmit="return doRegUserCheck(this);">';
		basicCard +=		'<div class="form-group">';
		basicCard +=			'<input type="text" name = "user_id" class="form-control form-control-user" id="userId" value="<%=rMap.get("user_id")%>" readonly>';
		basicCard +=		'</div>';
		basicCard +=		'<div class="form-group">';
		basicCard +=			'<input type="text" name = "user_name" class="form-control form-control-user" id="userName" placeholder="이름 입력" value="<%=rMap.get("user_name")%>" required>';
		basicCard +=		'</div>';
		basicCard +=		'<div class="form-group">';
		basicCard +=			'<input type="password" name = "password" class="form-control form-control-user" id="userInputPassword" placeholder="비밀번호 변경 시 입력">';
		basicCard +=		'</div>';
		basicCard +=		'<div class="form-group">';
		basicCard +=			'<input type="password" name = "password2" class="form-control form-control-user" id="userRepeatPassword" placeholder="위와 동일한 비밀번호 입력">';
		basicCard +=		'</div>';
		basicCard +=		'<div class="form-group">';
		basicCard +=		'<input type="email" name = "email" class="form-control form-control-user" id="userInputEmail" placeholder="(선택 사항)이메일 입력" value="<%=rMap.get("user_email")%>">';
		basicCard +=		'</div>';
		basicCard +=		'<button type="submit" class="btn btn-primary btn-user btn-block">수정</button>';
		basicCard +=		'<center><a class="small" href="javascript:deleteUser();" style="text-decoration-line: none;">탈퇴</a></center>';
		basicCard +=	'</form>';
		basicCard +='</div>';
		basicCard +='</center>';
		
		
		$("#content").append(basicCard);
	}

</script>
