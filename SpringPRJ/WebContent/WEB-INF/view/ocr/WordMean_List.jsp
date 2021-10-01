<%@include file="/WEB-INF/view/SideMenu.jsp"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
String reg_id = CmmUtil.nvl((String)request.getAttribute("reg_id").toString());
String reg_dt = CmmUtil.nvl((String)request.getAttribute("reg_dt").toString());
%>

<title>영어 단어장</title>

<script type="text/javascript">

	//페이지 로딩 완료 후 실행함
	$(window).on("load", function(){
		loginCheck();
		makeContent();
		getResult();
	})
	
	//로그인 상태 확인
	function loginCheck(){
		if ("<%=SS_USER_ID%>" == "") {
				alert("로그인 후 이용 가능합니다.");
				top.location.href="/user/LoginForm.do";
		}
	}
	
	// 사용자에게 보여줄 요소 만들기
	function makeContent() {

		var basicContent = '';
		
		// 영어 단어 담을 요소
		basicContent += '<div id="fluid" class="container-fluid">';
		basicContent +=	'<div id="conRow" class="row"></div>';		
		basicContent += '</div>';
		
		// 사용자에게 보여줄 영역 
		$("#content").append(basicContent);
	}
	
	// 영어 단어 가져오기
	function getResult() {

		// Ajax 호출
		$.ajax({
			url : "/ocr/getWordMeanList.do?reg_id=" + "<%=reg_id%>" + "&reg_dt=" + "<%=reg_dt%>",
			type : "post",
			dataType : "JSON",
			contenType : "application/json; charset=UTF-8",
			success : function(json){

				var i = 0;
				while(i < json.length){
					
					var sti = i.toString();
					
					// <div id="cxcm(+i)" class="col-xl-3 col-md-6 mb-2"></div> 생성
					var cxcmmID	= "cxcm" + sti;		
					var cxcmm = document.createElement("div");
					cxcmm.id = cxcmmID;
					cxcmm.className = "col-xl-3 col-md-6 mb-2";
					
					// <div id="conRow" class="row"></div>의 하위 요소로 삽입
					document.getElementById("conRow").appendChild(cxcmm);
										
					// <div id = "cblwshp2(+i)" class="card border-left-warning shadow h-100 py-2"></div> 생성
					var cblwshp2ID = "cblwshp2" +sti; 
					var cblwshp2 = document.createElement("div");
					cblwshp2.id = cblwshp2ID;
					cblwshp2.className = "card border-left-warning shadow h-100 py-2";
					
					// <div id="cxcm(+i)" class="col-xl-3 col-md-6 mb-2"></div>의 하위 요소로 삽입
					document.getElementById(cxcmmID).appendChild(cblwshp2);
					
					// <div class="card-body"></div> 생성
					var cb = document.createElement("div");
					cb.className = "card-body";
					// 내용 추가
					cb.innerHTML =
									"<span class=\"badge badge-warning\" style=\"margin-right:5px;\">빈도수</span>" +
									json[i].frequency +
									"<div style=\"font-size: xx-large;\">" + json[i].word + "</div>" +
									"<div id='mean" + i + "'></div>";
									
					// <div id = "cblwshp2(+i)" class="card border-left-warning shadow h-100 py-2"></div>의 하위 요소로 삽입
					document.getElementById(cblwshp2ID).appendChild(cb);
					
					// json[i].mean의 값은 list안에 Map이 있는 형태
					var meanList = json[i].mean;			
					var j = 0;					
					while(j < meanList.length){
						
						// <span style="margin-right: 5px;"></span> 생성
						var mS = document.createElement("span");
						mS.style.cssText = "margin-right: 5px;";
						// 내용 추가
						mS.innerHTML =
							"<span class=\"badge badge-primary\" style=\"margin-right:5px;\">" + meanList[j].word_class + "</span>" +
							meanList[j].word_mean;
							// <div id="mean(+i)"></div>의 하위 요소로 삽입
							document.getElementById("mean"+ i).appendChild(mS);
							
						j++;
					}
					
					i++;
				}

			}
		})
	}
	
</script>
