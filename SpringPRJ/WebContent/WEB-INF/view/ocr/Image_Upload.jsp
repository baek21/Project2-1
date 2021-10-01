<%@include file="/WEB-INF/view/SideMenu.jsp"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<!-- 인터넷창 이름 -->
<title>영어 단어 추출하기</title>

<!-- 자바스크립트 -->
<script type="text/javascript">
	
	//페이지 로딩 완료 후 실행함
	$(window).on("load", function(){
		makeCard();
		makeForm();
	})
	
	//로그인 상태 확인
	function loginCheck(){
		if ("<%=SS_USER_ID%>" == "") {
			alert("로그인 후 이용 가능합니다.");
			top.location.href="/user/LoginForm.do";
		}
	}

	function submit_click() {
		var fileCheck = document.getElementById("imgUpload").value;
		if (fileCheck) {
			//화면의 높이와 너비를 구합니다.
			var maskHeight = $(document).height();
			var maskWidth = window.document.body.clientWidth;

			//화면에 출력할 마스크를 설정해줍니다.
			var mask = "<div id='mask' style='position:absolute; z-index:100; background-color:white; display:none; left:0; top:0;'></div>";
			var loadingImg = '';

			loadingImg += "<div id='loadingImg'>";
			loadingImg += "<img src='/img/LoadingImg.gif' style='position: absolute; top: 50%; left: 50%; transform:translate(-50%, -50%); z-index:101; display: block; margin: 0px auto; border-radius:50%;'/>";
			loadingImg += "</div>";

			//화면에 레이어 추가
			$('body').append(mask).append(loadingImg)

			//마스크의 높이와 너비를 화면 것으로 만들어 전체 화면을 채웁니다.
			$('#mask').css({
				'width' : maskWidth,
				'height' : maskHeight,
				'opacity' : '0.3'
			});

			//마스크 표시
			$('#mask').show();

			//로딩중 이미지 표시
			$('#loadingImg').show();
			
			//form 전송하기
			document.form1.submit();
		} else {
			alert("파일을 선택해 주십시오.");
			return false;
		}

	}
	// 업로드할 파일 선택(변화 감지)하면 함수 실행
	$(function() {
		$("#imgUpload").on('change', function() {
			readURL(this);
		});
	});

	// 사용자가 업로드할 파일 이미지 보여주기
	function readURL(input) {
		
		// 사용자 파일 유무 확인
		if (input.files && input.files[0]) {

			var reader = new FileReader();

			reader.onload = function(e) {
				$('#sUI').attr('src', e.target.result);
			}

			reader.readAsDataURL(input.files[0]);

			$("#sUI").show();// display 속성을 block 으로 바꾼다.

		} else {

			$("#sUI").hide();// display 속성을 none 으로 바꾼다.

		}
	}
	
	// 카드 만들기
	function makeCard() {

		var basicCard = '';

		//Basic Card Example
		basicCard += '<div class="container-fluid">';
		basicCard += '<div class="card shadow mb-4">';
		basicCard += '<div class="card-header py-3">';
		basicCard += '<h6 class="m-0 font-weight-bold text-primary">영어 단어 추출하기</h6>';
		basicCard += '</div>';
		basicCard += '<div id="bCC" class="card-body">';
		basicCard += '이미지 파일( jpg, jpeg, png, gif )에서 영어 단어를 추출하여 단어장을 만들어 드립니다. ';
		basicCard += '영어 단어장은 마이페이지에서 확인 하실 수 있습니다.';
		basicCard += '</div>';
		basicCard += '</div>';
		basicCard += '</div>';

		
		$("#content").append(basicCard);
	}

	// 이미지 파일 업로드 폼 만들기
	function makeForm() {

		var uploadForm = '';

		uploadForm += '<br/><br/><center><img id="sUI" src="#" alt="your image" style="width: 40%; height: auto; max-width: 500px; max-height: 500px;"/></center>';
		uploadForm += '<form name="form1" method="post" enctype="multipart/form-data" action="/ocr/ReadTextFromImage.do"><br/>';
		uploadForm += '<input type="file" id="imgUpload" name="fileUpload" accept=".jpg, .jpeg, .png, .gif" onclick="loginCheck()"/><br/>';
		uploadForm += '<input type="button" value="추출 시작" onclick="submit_click()" style="margin-top: 5px"/>';
		uploadForm += '</form>';

		$("#bCC").append(uploadForm);

		$("#sUI").hide();// display 속성을 none 으로 바꾼다. 
	}
	
	
</script>