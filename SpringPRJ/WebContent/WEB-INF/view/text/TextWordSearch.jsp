<%@include file="/WEB-INF/view/SideMenu.jsp"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<!-- 인터넷창 이름 -->
<title>영어 단어 추출하기</title>

<!-- 자바스크립트 -->
<script type="text/javascript">
	
//페이지 로딩 완료 후 실행함
	$(window).on("load", function(){
		makeContent();
	})
	
	// 사용자에게 보여줄 요소 만들기
	function makeContent() {

		var basicContent = '';
		
		// 입력, 결과 담을 요소
		basicContent += '<div id="fluid" class="container-fluid">';
		
		// 영어 문장 입력
		basicContent += '<div class="card shadow mb-4">';
		basicContent += '<div class="card-header py-3">';
		basicContent += '<h6 class="m-0 font-weight-bold text-primary">영어 단어 추출하기</h6>';
		basicContent += '</div>';
		basicContent += '<div class="card-body">';
		basicContent += '입력한 영어 문장에서 단어를 분석하여 보여 드립니다. <font color="red">영어 단어장에 저장되지 않습니다.</font>';
		basicContent += '<br/><br/>';
		basicContent += '<textarea id="iT" style="width: 100%; height: 100%; resize: none; overflow: hidden; outline: none; margin-botton: 5px;" placeholder="영어 문장 입력" onkeyup="fn_checkByte(this);"></textarea>';
		basicContent += '<div style="float: right;"><sup><span id="nowByte">0</span>/' + maxByte + 'bytes</sup></div>';
		basicContent += '<input type="button" style="margin-right: 5px;" value="추출 시작" onclick="getResult()"/>';
		basicContent += '<input type="button" value="전체 초기화" onclick="rText()"/>';
		basicContent += '</div>';
		basicContent += '</div>';
		
		// 추출 결과
		basicContent +=	'<div id="conRow" class="row"></div>';		
				
		basicContent += '</div>';
		
		// 사용자에게 보여줄 영역 
		$("#content").append(basicContent);
	}
	
	// textarea 초기화 실행
	function rText(){
		// 입력값 초기화
		document.getElementById("iT").value='';
		document.getElementById("nowByte").innerText=0;
		
		// 영어 단어 결과 제거
		// <div id="conRow" class="row"></div>제거
		$("#conRow").remove();
		// <div id="conRow" class="row"></div> 생성
		var dr = document.createElement("div");
		dr.id = "conRow";
		dr.className = "row";
		
		// <div id="fluid" class="container-fluid"></div>의 하위 요소로 삽입
		document.getElementById("fluid").appendChild(dr);
		
		rHeight();
	}	
	
	// textarea 입력 값으로 입력창 높이 조절
	function rHeight() {
		
		var textEle = $('#iT');
		textEle[0].style.height = 'auto';
		var textEleHeight = textEle.prop('scrollHeight');
		textEle.css('height', textEleHeight);
		
		// 스크롤을 화면 하단에 고정
		$('html, body').scrollTop($('#iT')[0].scrollHeight);
	};
	
	// textarea에 값을 입력할 때 실행
	// 입력창 높이 조절 + textarea 바이트 수 체크하는 함수
	const maxByte = 1500; //최대 바이트(중요)★★★★★
	function fn_checkByte(obj){
		
		// 입력창 높이 조절
		rHeight();
		
		// 바이트 수 체크 시작
	    const text_val = obj.value; //입력한 문자
	    const text_len = text_val.length; //입력한 문자수
	    
	    let totalByte=0;
	    let i=0;
	    while(i<text_len){
	    	
	    	const each_char = text_val.charAt(i);
	        const uni_char = escape(each_char) //유니코드 형식으로 변환
	        
	        if(uni_char.length>4){
	        	// 한글 : 2Byte
	            totalByte += 2;
	        }else{
	        	// 영문,숫자,특수문자 : 1Byte
	            totalByte += 1;
	        }
	    	
	    	i++;
	    }
	    
		if(totalByte>maxByte){
			document.getElementById("nowByte").innerText = totalByte;
			document.getElementById("nowByte").style.color = "red";
		}else{
			document.getElementById("nowByte").innerText = totalByte;
			document.getElementById("nowByte").style.color = "";
		}
	}

	//추출 시작 버튼 클릭시 실행
	function getResult() {
		
		// 로그인 상태 확인
		if ("<%=SS_USER_ID%>" == "") {
			alert("로그인 후 이용 가능합니다.");
			top.location.href="/user/LoginForm.do";
			return false;
		}
		
		// 입력값이 없으면 실행
		if (document.getElementById("iT").value==''){
			alert("영어 문장을 입력해 주십시오.");
			return false;
		}

		// 최대 입력값(maxByte) 초과하면 실행
		if (document.getElementById("nowByte").textContent > maxByte){
			alert('최대 ' + maxByte + 'Byte까지만 입력가능합니다.');
			return false;
		}
		
		// 마스크 전체화면에 만들기
		loadingMask();
		
		// <div id="conRow" class="row"></div>제거
		$("#conRow").remove();
		
		// id가 iT인 textarea의 입력값
		var insertText = document.getElementById("iT").value;
		
		// Ajax 호출
		$.ajax({
			url : "/text/ReadText.do?",
			type : "post",
			data : {textContents: insertText},
			dataType : "JSON",
			contenType : "application/json; charset=UTF-8",
			success : function(json){
				
				// <div id="conRow" class="row"></div> 생성
				var dr = document.createElement("div");
				dr.id = "conRow";
				dr.className = "row";
				
				// <div id="fluid" class="container-fluid"></div>의 하위 요소로 삽입
				document.getElementById("fluid").appendChild(dr);
											
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
				$("div").remove("#loadingImg");
				$("div").remove("#mask");
			}
		})
	}
	
	function loadingMask() {
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
	}
	
</script>