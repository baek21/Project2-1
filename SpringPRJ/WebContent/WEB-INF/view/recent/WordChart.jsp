<%@include file="/WEB-INF/view/SideMenu.jsp"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>영어 단어 차트</title>
	<script type="text/javascript">
	
	google.charts.load('current', {'packages':['bar']});
	google.charts.setOnLoadCallback(drawStuff);
	
		// 페이지 로딩 후 실행
		$(window).on("load", function(){
			// 로그인 체크
			loginCheck();
			// 카드 만들기(차트 만들기 전에 수행해야함)
			makeCard();
			// 차트 만들기
			drawStuff();
		})
		
		// 화면 크기 변화에 따라 차트 다시 그리기 
		// resizeEnd 이벤트 트리거 생성
		$(window).resize(function() {
			if(this.resizeTO) clearTimeout(this.resizeTO);
			this.resizeTO = setTimeout(function() {
				$(this).trigger('resizeEnd');
			}, 500);// resize 끝나고 0.5초 후 resizeEnd 실행
		});
		// resizeEnd 이벤트
		$(window).on('resizeEnd', function() {
			drawStuff();// 차트 그리기
		});

	
		// 로그인 상태 확인
		function loginCheck(){
			if ("<%=SS_USER_ID%>"=="") {
				alert("로그인 후 이용 가능합니다.");
				top.location.href="/user/LoginForm.do";
			}
		}
		  
		// 카드 만들기
		function makeCard() {
	
			var basicCard = '';
	
			//Basic Card Example
			basicCard += '<div class="container-fluid">';
			basicCard += '<div class="card shadow mb-4">';
			basicCard += '<div class="card-header py-3">';
			basicCard += '<h6 class="m-0 font-weight-bold text-primary">영어 단어 빈도수</h6>';
			basicCard += '</div>';
			basicCard += '<div id="bCC" class="card-body">';
			basicCard += '최근 3일간 생성한 영어 단어장에서 자주 나오는 영어 단어를 차트 형식으로 보여줍니다.';
			basicCard += '<center><div id="top_x_div" style="margin:20px;"></div></center>';
			basicCard += '</div>';
			basicCard += '</div>';
			basicCard += '</div>';
	
			
			$("#content").append(basicCard);
		}
		
		function drawStuff(){
			// Ajax 호출
			$.ajax({
				url : "/ocr/getRecentWord.do",
				type : "post",
				dataType : "JSON",
				contenType : "application/json; charset=UTF-8",
				success : function(json){
					// https://developers.google.com/chart/interactive/docs/gallery/barchart 참고
					var jsonData = [];
					var i = 0;
					while(i < json.length){
						
						jsonData.push([json[i].word, json[i].frequency]);
						
						console.log(json[i].word.toString());
						console.log(parseInt(json[i].frequency.toString()));
						
						i++;
					}
					//var wordData = jsonData;
					//jsonData = null;
					console.log(jsonData);
					
					var data = new google.visualization.DataTable();
					data.addColumn('string','Word');
					data.addColumn('number','빈도수');
					data.addRows(jsonData);
					
					jsonData = null;
					
					var options = {
						  title: 'Chess opening moves',
						  height: json.length*30,
						  legend: { position: 'none' },
						  //chart: { title: '영어 단어 차트',
						  //         subtitle: '최근 3일간 추출한 영어 단어의 빈도수를 차트 형식으로 보여줍니다.' },
						  bars: 'horizontal', // Required for Material Bar Charts.
						  axes: {
						    x: {
						      0: { side: 'top', label: '빈도수' } // Top x-axis.
						    }
						  },
						  bar: { groupWidth: "90%" },
						  vAxis: {minValue: 0},
						};
							
					var chart = new google.charts.Bar(document.getElementById('top_x_div'));
					chart.draw(data, options);
				}
			
			});
			
		}
		
		
	</script>
</head>
<body>
	
</body>
</html>