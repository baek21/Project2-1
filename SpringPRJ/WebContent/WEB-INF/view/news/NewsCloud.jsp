<%@include file="/WEB-INF/view/SideMenu.jsp"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>

<title>워드 클라우드</title>

<!-- 워드 클라우드 코어 -->
<script src="/js/jqcloud.js"></script>
<!-- 워드 클라우드 스타일-->
<link href="/css/jqcloud.css" rel="stylesheet">
<script>

	//페이지 로딩 후 실행
	$(window).on("load", function(){
		getList();
	})

	//목록 가져오기
	function getList(){
		// Ajax 호출
		$.ajax({
			url : "/news/NewsAreaList.do",
			type : "post",
			dataType : "JSON",
			contenType : "application/json; charset=UTF-8",
			success : function(json){
				var jsonArray = [];
				
				var i = 0;
				while(i < json.length){
					
					var jsonData = {};
					
					jsonData.text = json[i].newsArea.toString();
					jsonData.weight = parseInt(json[i].newsAreaCnt.toString());
					jsonData.link = "javascript:doDetail('" + json[i].newsArea.toString() + "');";
					
					jsonArray.push(jsonData);
					
					josnData = null;
										
					i++;
				}
				
				var kwd = '<center><div id="keywords"></div></center>';
					
				// 사용자에게 보여줄 영역 
				$("#content").append(kwd);
						
				var words = jsonArray;
				
				jsonArray = null;
				
				//console.log("words : " + JSON.stringify(words));
				
				$(function(){
					$("#keywords").jQCloud(words, {
						width:600,
						height:700,
						colors: ["#FFB400", "#0000FF", "#0078FF", "#86A5FF", "#00A5FF"],
						fontSize: { from: 0.1, to: 0.02 }
					});
				});
			}
		});
	}
	
	// 상세보기 이동
	function doDetail(newsArea) {
			location.href="/news/NewsWordInfo.do?newsArea=" + newsArea;
	}

</script>
