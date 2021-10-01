<%@include file="/WEB-INF/view/SideMenu.jsp"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<!-- 인터넷창 이름 -->
<title>영어 단어장 목록</title>

<script type="text/javascript">

	// 페이지 로딩 후 실행
	$(window).on("load", function(){
		// 로그인 체크
		loginCheck();
		// 테이블 만들기
		makeTable();
		// 영어 단어 목록 가져오기
		getList();
	})

	// 로그인 상태 확인
	function loginCheck(){
		if ("<%=SS_USER_ID%>"=="") {
			alert("로그인 후 이용 가능합니다.");
			top.location.href="/user/LoginForm.do";
		}
	}

	// 상세보기 이동
	function doDetail(reg_id, reg_dt) {
		if("<%=SS_USER_ID%>"==reg_id) {			
			location.href="/ocr/WordMean_List.do?reg_id=" + reg_id + "&reg_dt=" + reg_dt;
		} else {
			alert("잘못된 접근 입니다.");
			top.location.href="/main.do";// 메인 페이지
		}
	}
	
	// 삭제하기 실행
	function doDelete(reg_id, reg_dt){
		if("<%=SS_USER_ID%>"==reg_id) {
			if(confirm("삭제 하시겠습니까?")){
				location.href="/ocr/OcrResultDelete.do?reg_id=" + reg_id + "&reg_dt=" + reg_dt;
			}
		} else {
			alert("잘못된 접근 입니다.");
			top.location.href="/main.do";// 메인 페이지
		}		
	}
	
	// 다운로드 실행
	function doDownLoad(reg_id, reg_dt){
		if("<%=SS_USER_ID%>"==reg_id) {
			location.href="/ocr/txtDownLoad.do?reg_id=" + reg_id + "&reg_dt=" + reg_dt;		
		} else {
			alert("잘못된 접근 입니다.");
			top.location.href="/main.do";// 메인 페이지
		}		
	}
	
	// 영어 단어장 목록 만들기
	function makeTable() {

		var makeTable = '';
		
		makeTable += '<div id="fluid" class="container-fluid">';
		makeTable += 	'<div class="card shadow mb-4">';
		makeTable += 		'<div class="card-header py-3">';
		makeTable += 			'<h6 class="m-0 font-weight-bold text-primary">영어 단어장 목록</h6>';
		makeTable += 		'</div>';
		
		makeTable += 		'<div class="card-body">';
		makeTable += 			'<div class="table-responsive">';
		makeTable +=	 			'<table class="table table-bordered" id="dataTable" width="100%" cellspacing="0">';
		
		makeTable +=					'<thead>';
		makeTable +=						'<tr>';
		makeTable += 							'<th><center>파일명</center></th>';
		makeTable += 							'<th><center>등록일</center></th>';
		makeTable +=							'<th><center>다운로드</center></th>';
		makeTable +=							'<th><center>삭제</center></th>';
		makeTable +=		 				'</tr>';
		makeTable += 					'</thead>';
		
		makeTable +=		 			'<tfoot>';
		makeTable += 						'<tr>';
		makeTable += 							'<th><center>파일명</center></th>';
		makeTable += 							'<th><center>등록일</center></th>';
		makeTable +=							'<th><center>다운로드</center></th>';
		makeTable +=							'<th><center>삭제</center></th>';
		makeTable += 						'</tr>';
		makeTable += 					'</tfoot>';        
		
		// 데이터 넣을 요소
		makeTable += 					'<tbody id="tbdi">';
		makeTable += 					'</tbody>';
		
		makeTable +=		 		'</table>';
		makeTable += 			'</div>';
		makeTable += 		'</div>';
		
		makeTable += 	'</div>';
		makeTable += '</div>';
		
		// 사용자에게 보여줄 영역 
		$("#content").append(makeTable);
	}
	
	// 목록 가져오기
	function getList(){
		// Ajax 호출
		$.ajax({
			url : "/ocr/getOcrResultAll.do",
			type : "post",
			dataType : "JSON",
			contenType : "application/json; charset=UTF-8",
			success : function(json){

				var i = 0;
				while(i < json.length){
					
					var trf = document.createElement('tr');
					var getInfo = "'" + json[i].reg_id + "', '" + json[i].reg_dt + "'";
					trf.innerHTML =	
						'<th><a href="javascript:doDetail(' + getInfo + ');" style="text-decoration-line: none;">' + json[i].original_file_name + '</a></th>' +
						'<th><center>' +
							json[i].reg_dt.substring(0, 4)+ '년 ' +
							json[i].reg_dt.substring(4, 6)+ '월 ' +
							json[i].reg_dt.substring(6, 8)+ '일 ' +
							json[i].reg_dt.substring(8, 10)+ '시 ' +
							json[i].reg_dt.substring(10, 12)+ '분 ' +
							json[i].reg_dt.substring(12)+ '초' +										
						'</th></center>' +
						'<th><center><a href="javascript:doDownLoad(' + getInfo + ');" style="text-decoration-line: none;">다운로드</a></center></th>' +
						'<th><center><a href="javascript:doDelete(' + getInfo + ');" style="text-decoration-line: none;">삭제</a></center></th>';

					document.getElementById('tbdi').appendChild(trf);
					
					i++;
				}
			}
		})
	}
		
	
	
</script>
