<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="utf-8" />
<meta http-equiv="X-UA-Compatible" content="IE=9; IE=8; IE=7; IE=EDGE" />
<meta name="viewport" content="width=device-width, initial-scale=1, minimum-scale=1, maximum-scale=1" />
<meta name="format-detection" content="telephone=no" />
<meta name="apple-mobile-web-app-status-bar-style" content="black" />
<title>${requestScope.task.activityName}</title>
<link rel="stylesheet" href="<c:url value='/resources/skin/css/popup.css' />" />
<script type="text/javascript" src="<c:url value='/resources/skin/js/jquery-1.11.0.min.js' />"></script>
<script>
doAction = function(key) {
	var bizxml = "";
	
/* 	bizxml += "<ns1:start xmlns:ns1=\"http://xmlns.oracle.com/bpel/workflow/task\" xmlns:ns2=\"http://www.example.org\">\n";
	bizxml += "	<ns2:expense>\n";
	bizxml += "		<ns2:expenseNumber>0001</ns2:expenseNumber>\n";
	bizxml += "		<ns2:requesterId>bpmuser1</ns2:requesterId>\n";
	bizxml += "		<ns2:requestDate>2016-03-03T12:00:00</ns2:requestDate>\n";
	bizxml += "		<ns2:amount>10000</ns2:amount>\n"; 
	bizxml += "		<ns2:paymentType>corporation/individual</ns2:paymentType>\n"; 
	bizxml += "		<ns2:expenseType>차량유지비/접대비/복리후생비/출장비</ns2:expenseType>\n"; 
	bizxml += "		<ns2:justification>시스템 장애지원</ns2:justification>\n"; 
	bizxml += "		<ns2:dueDate>2016-03-05T12:00:00</ns2:dueDate>\n"; 
	bizxml += "		<ns2:outcome></ns2:outcome>\r\n"; 
	bizxml += "		<ns2:approval>\r\n";
	bizxml += "			<ns2:line>approvaluser1,approvaluser2</ns2:line>\r\n";
	bizxml += "			<ns2:type>SEQUENTIAL</ns2:type>\r\n";
	bizxml += "			<ns2:ruleset>비용처리승인규칙</ns2:ruleset>\r\n";
	bizxml += "		</ns2:approval>\r\n";
	bizxml += "	</ns2:expense>\r\n";
	bizxml += "</ns1:start>"; */
	
	var expenseNumber = $('#expenseNumber').val();
	var requesterId = $('#requesterId').val();
	var requestDate = $('#requestDate').val();
	var amount = $('#amount').val();
	var paymentType = $('#paymentType').val();
	var expenseType = $('#expenseType').val();
	var justification = $('#justification').val();
	var dueDate = $('#dueDate').val();
	var outcome = key;
	var currency = $('#currency').val();
	var docnum = $('#docnum').val();
	
	var line = $('#line').val();
	var type = $(':radio[name="type"]:checked').val();
	var ruleset = $('#ruleset').val();
	
	dueDate = dueDate.substring(0,10)+"T"+dueDate.substring(11,16)+":00";
	$("#dueDate").val(dueDate);
	
	//bizxml += "<ns1:start xmlns:ns1=\"http://xmlns.oracle.com/bpel/workflow/task\" xmlns:ns2=\"http://www.example.org\">\n";
	bizxml += "	<ns1:expense xmlns:ns1=\"http://www.example.org\">\n";
	bizxml += "		<ns1:expenseNumber>"+expenseNumber+"</ns1:expenseNumber>\n";
	bizxml += "		<ns1:requesterId>"+requesterId+"</ns1:requesterId>\n";
	bizxml += "		<ns1:requestDate>"+requestDate+"</ns1:requestDate>\n";
	bizxml += "		<ns1:amount>"+amount+"</ns1:amount>\n"; 
	bizxml += "		<ns1:paymentType>"+paymentType+"</ns1:paymentType>\n"; 
	bizxml += "		<ns1:expenseType>"+expenseType+"</ns1:expenseType>\n"; 
	bizxml += "		<ns1:justification>"+justification+"</ns1:justification>\n"; 
	bizxml += "		<ns1:dueDate>"+dueDate+"+09:00</ns1:dueDate>\n"; 
	bizxml += "		<ns1:outcome>"+outcome+"</ns1:outcome>\r\n"; 
	bizxml += "		<ns1:currency>"+currency+"</ns1:currency>\r\n"; 
	bizxml += "		<ns1:docnum>"+docnum+"</ns1:docnum>\r\n"; 
	bizxml += "		<ns1:approval>\r\n";
	bizxml += "			<ns1:line>"+line+"</ns1:line>\r\n";
	bizxml += "			<ns1:type>"+type+"</ns1:type>\r\n";
	bizxml += "			<ns1:ruleset>"+ruleset+"</ns1:ruleset>\r\n";
	bizxml += "		</ns1:approval>\r\n";
	bizxml += "	</ns1:expense>\r\n";
	//bizxml += "</ns1:start>";
	
	
	$("#outcome").val(outcome);
	$("#body_xml").val(bizxml);

	
	if(key=='SUBMIT'){
		if(line==""||line==null){
			alert("승인자를 지정하세요.");
			return false;
		}
		if(type==""||type==null){
			alert("병렬/순차 체크박스를 선택하세요.");
			return false;
		}
	}
	/*
	* 업무 취소, 최종결재 
	* GV_IFDEC = C/A/B (C:상신화면 오픈전에 호출 시 C, 최종 결재 완료되면 A, 업무 취소 시 B)
	*/ 
	if(confirm("처리 하시겠습니까?")) {
		sapRefresh();
		document.detail.submit();
	} else return;
}


function btnCheck(){
	var checkRadio = $(':radio[name="type"]:checked').val();
	var str = $('#appList').val();
	if(checkRadio=='PARALLEL'){
		str = str.replace(/\→/gi,",");
	}
	else if(checkRadio=='SEQUENTIAL'){
		str = str.replace(/\,/gi,"→");
	}
	$('#appList').val(str);
}

$(document).ready(function(){
	$('#approvalBtn').click(function(){
		var width = 500; //팝업창 넓이
		var height = 310; //팝업창 높이
 		var left = (screen.width - width) / 2 ; 
 		var top = (screen.height - height) / 2 ;
		window.open('<spring:url value='/bpm/workflow/approvalList' />','승인자리스트','width='+width+', height='+height+', left='+left+', top='+top);
	});
	
	$('.typeRadio').click(function(){
		btnCheck();
	});
	
	/*
	* SOA RFC 테스트
	*/
	$("#testSubmit").click(function(){
		var currency = $('#currency').val();
		var docnum = $('#docnum').val();
		
		//alert("currency : "+currency+", docnum : "+docnum);
		submitSoaRFC(currency, docnum, 'C');
		//submitiframe(currency, docnum);
	});
	
	/**
	* SAP GUI
	*/
	$('#sapgui').click(function(){
		//alert();
		invokeClientApp();
	});
	
	/**
	* BPM INFO
	*/
	$('#bpminfo').click(function(){
		//alert();
		$('#bpmhidden').toggle("slow", function(){
			//alert();
		});
	});
	
	/**
	* 닫기
	*/
	$('#sapClose').click(function(){
		sapClose();
		
	});

	//onBeforeUnload 이벤트 지정
	$(window).bind('beforeunload', function(){
	    sapClose();
	});

	//Form Submit 등 onBeforeUnload Event가 발생하길 원하지 않는 경우, 이벤트 해제
	$('form').submit(function(){
	    $(window).unbind('beforeunload');
	});
	
});

$(function(){
	
	/*
	* SAP RFC ajax
	*/
	submitSoaRFC = function(currency, docnum, ifdec) {
		
		var sap_rfc_url = "http://oraclebpm:7003/soa-infra/resources/default/callSAPRFCFunction/SAPRFCRESTService/bpmrfc";
		var sap_rfc_param = "?GV_DOCNUM="+docnum+"&GV_IFDEC="+ifdec+"&GV_PROID=SSC030_MN";

		console.log(sap_rfc_url+sap_rfc_param);
		
		$.ajax({
			type : "get" ,
			url : sap_rfc_url+sap_rfc_param ,
			dataType : "json" ,
			contentType : "application/json; charset=utf-8" ,
			//dataType: 'jsonp',
			//jsonp : "callback",
    		//jsonpCallback: "callback",
			success:function(data){
			
				console.log(data);
				//var jsonObject = JSON.parse(data /* your original json data */);
				//jsonToHtml(0, jsonObject, html);
				
				if(data.E_RETURN.TYPE == 'S'){
					//alert("정상적으로 처리 되었습니다.");
					$('#soa_rfc_msg').html("성공 : "+data.E_RETURN.MESSAGE);
					/* if(key == 'SUBMIT'){
						doAction(key);
					} */
					soaiframe();
					
				} else {
					//alert(data.E_RETURN.MESSAGE);
					$('#soa_rfc_msg').html("오류 : "+data.E_RETURN.MESSAGE);
				}
			},
			beforeSend: function (xhrObj) {
				console.log(xhrObj);
			}, 
			error : function(request, status, error) {
				console.log("error");
				console.log(request.E_RETURN.MESSAGE);
				
			},
			complete : function(data) {
				//document.location.reload();
				console.log(data);
			}
		});
	};
	
	/**
	* sap iframe
	*/
	sapiframe = function(currency, tcode, docnum) {
		var sap_rfc_url = "http://192.168.3.25:8000/sap/bc/gui/sap/its/webgui";
		
		var sap_rfc_param = "?sap-client=200&sap-user=BPMUSER&sap-password=123456&sap-language=&~transaction="+tcode+" sanc_userid=;p_bukrs=1000;p_empno=304694;"+docnum;
	    $("#sapiframe").attr("target", "sapiframe");
	    $("#sapiframe").attr("src", sap_rfc_url+sap_rfc_param);
	    $("#sapiframe").attr("action", sap_rfc_url+sap_rfc_param);
	    //$("#submitiframe").submit();
	    $('#sapurl').html(sap_rfc_url+sap_rfc_param);
	    //window.open(sap_rfc_url+sap_rfc_param, docnum, 'width=1018px, height=600px');
	};
	
	/**
	* soa iframe
	*/
	soaiframe = function(){
		var currency = $('#currency').val();
		var docnum = $('#docnum').val();
		var tcode  = $('#tcode').val();
		var paramKey = '';
		var activityName = $('#activityName').val();
		
		if(activityName  != 'SAP 전표처리'){
			console.log('법인카드 기안/승인 : '+activityName);
			// 기안/승인
			tcode = '*ZCDR0040';
			paramKey = 'PA_DOC=';
		}else{
			console.log(activityName);
			// 전표처리
			tcode = 'ZCDR0010_POC';
			paramKey = 'p_docnum=';
		}
		
		sapiframe(currency, tcode, paramKey+docnum);
	};
	
	/**
	* SAP 닫기
	*/
	sapClose = function(){
		sapRefresh();
		//admin
		//bpmAdmin();
		self.close();
	};
	
	sapRefresh = function(){
		// init TO-DO
		window.opener.myTodoSearch();
		// init 프로세스현황
		window.opener.prcStateSearch();
		// init 프로세스 지연현황
		window.opener.prcDelaySearch();
		// init 담당자현황
		window.opener.prcCharge();
		//admin
		//bpmAdmin();
	};
	
});

$(window).load(function(){
	var currency = $('#currency').val();
	var docnum = $('#docnum').val();
	
	var activityName = $('#activityName').val();
	if(activityName == '법인카드 기안'){
		submitSoaRFC(currency, docnum, 'C');
	}else{
		soaiframe();
	}
	
});

function invokeClientApp(appCommand){

	var docnum = $('#docnum').val();
	var tcode  = $('#tcode').val();
	var paramKey = '';
	var activityName = $('#activityName').val();
	
	if(activityName == '법인카드 기안'){
		console.log('법인카드 기안'+activityName);
		// 기안/승인
		tcode = '*ZCDR0040';
		paramKey = 'PA_DOC=';
	}else{
		console.log(activityName);
		// 전표처리
		tcode = 'ZCDR0010_POC';
		paramKey = 'p_docnum=';
	}
	
    var appParams = "-u=BPMUSER -pw=123456 -gui=192.168.3.25 -language=KO -sid=SEQ -type=Transaction -clt=200 -cmd="+tcode+" p_bukrs=1000;p_empno=304694;"+paramKey+docnum;      // Client 실행 프로그램
    var appName = "sapshcut.exe";                  // Client 실행 프로그램에 추가될 파라메타
	
	$('#sapguiurl').html(appName+" "+appParams);
	
	/* if (document.all) {
		alert("document.all");
		var au = HWAUCtrl;
		au.Run("open", appName, appParams, -1, 0);
	} else {
		alert("else");
		var au = document.HWAUCtrl;
		//var au = document.getElement("HWAUCtrl");
		var param = "open," + appName + "," + appParams + ",-1, 0";
		//au.InvokeN("Run", "8,8,8,3,3", param);
		au.Run("open", appName, appParams, -1, 0);
	} */
	/* var au = 'HWAUCtrl';
	alert(au);
	
	au.Run("open", appName, appParams, -1, 0); */
	
	/* var au = $('#HWAUCtrl');
	var param = "open," + appName + "," + appParams + ",-1, 0";
	alert(au);
	au.InvokeN("Run", "8,8,8,3,3", param); */
	
	var au = new ActiveXObject("WScript.Shell");
	//au.Run("open", appName, appParams, -1, 0);
	console.log("\"C:\\Program Files (x86)\\SAP\\FrontEnd\\SAPgui\\"+appName+"\" "+appParams);
	//au.Run("\"C:\\Program Files (x86)\\SAP\\FrontEnd\\SAPgui\\"+appName+"\" "+appParams);
	console.log(appName+" "+appParams);
	au.Run(appName+" "+appParams);
	
	return;
}
</script>
</head>

<body>
<div>
	<form name="detail"  method="post" action="<spring:url value="/bpm/workflow/doAction" />" >
		<table class="board_reservation_month">
			<thead>
				
			<th id="bpminfo" style="height:30px; font-size: 20px;" colspan="2" >${requestScope.task.activityName}</th>
			</thead>
			<tbody id="bpmhidden" style="display:none;">
				<tr>
					<th>비용번호</th>
					<td>${requestScope.task.textAttribute10}</td>
				</tr>
				<tr>
					<th>지불구분</th>
					<td>${requestScope.task.textAttribute12 eq 'corporation' ? '법인' : (requestScope.task.textAttribute12 eq 'individual' ? '개인' : requestScope.task.textAttribute12)}</td>
				</tr>
				<tr>
					<th>비용구분</th>
					<td>${requestScope.task.textAttribute13}</td>
				</tr>
				<tr>
					<th>처리기한</th>
					<input type="hidden" value="${requestScope.task.dateAttribute2}" />
					<td><fmt:formatDate type="both" dateStyle="full" value="${requestScope.task.dateAttribute2.time}"/></td>
				</tr>
				<tr>
					<th>금액</th>
					<td><fmt:formatNumber type="currency" value="${requestScope.task.numberAttribute1}" pattern="###,###" /></td>
				</tr>
				<tr>
					<th>SAP DOC_NO</th>
					<td>${requestScope.task.textAttribute15}</td>
				</tr>
				<tr>
					<th>SAP WEB URL</th>
					<td id="sapurl"></td>
				</tr>
				<tr>
					<th>SAP GUI URL</th>
					<td id="sapguiurl"></td>
				</tr>
				<c:choose>
					<c:when test="${requestScope.task.activityName=='법인카드 기안'}">
				<tr>
					<th>SAP TEST</th>
					<td>
					<input type="button" id="testSubmit" name="testSubmit" style="width:100px;height:30px;" value="SAP TEST" />
					</td>
				</tr>
					</c:when>
					<c:when test="${requestScope.task.activityName=='SAP 전표처리'}">
				<tr>
					<th>SAP GUI</th>
					<td>
					<input type="button" id="sapgui" name="sapgui" style="width:100px;height:30px;" value="SAP GUI" />
					<c:choose>
					<c:when test="${fn:length(requestScope.task.customActions) > 0}">
						<c:forEach var="action" begin="0" items="${requestScope.task.customActions}" varStatus="ts">
							<input type="button" style="width:100px;height:30px;" onClick="doAction('${action.key}')" value="${action.value eq 'CANCEL' ? '취소' : (action.value eq '제출' ? '제출' : action.value) }" />
							<c:set var="tc" value="${ts.index}" />
						</c:forEach>
					</c:when>
			</c:choose>
					</td>
				</tr>
					</c:when>
				</c:choose>
			</tbody>
			<tbody>
				<c:choose>
					<c:when test="${requestScope.task.activityName=='법인카드 기안'}">
						<tr>
							<th>승인자지정</th>
							<td><input type="text" id="appList" name="appList" value="" size="50%" readonly/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
								<input type="button" id="approvalBtn" value="승인자리스트" style="width:100px;height:30px;" /></td>
						</tr>
						<tr>
							<th>병렬/순차</th>
							<td>
								<input type="radio" name="type" value="PARALLEL" class="typeRadio" checked>병렬
								<input type="radio" name="type" value="SEQUENTIAL" class="typeRadio">순차
							</td>
						</tr>
					</c:when>
				</c:choose>
			</tbody>
		</table>
		<br>
		<div class="modal-footer" align="center"><strong id="soa_rfc_msg" ></strong>
		</div>
		<div class="modal-footer" align="right" >
			
			<c:choose>
				<c:when test="${fn:length(requestScope.task.customActions) > 0}">
					<c:forEach var="action" begin="0" items="${requestScope.task.customActions}" varStatus="ts">
						<c:choose>
							<c:when test="${requestScope.task.activityName != 'SAP 전표처리'}">
							<input type="button" style="width:100px;height:30px;" onClick="doAction('${action.key}')" value="${action.value eq 'CANCEL' ? '취소' : (action.value eq '제출' ? '제출' : action.value) }" />
							</c:when>
							<c:otherwise>
								<input type="button" id="sapClose" name="sapClose" style="width:100px;height:30px;" value="닫기" />
							</c:otherwise>
						</c:choose>
						<c:set var="tc" value="${ts.index}" />
					</c:forEach>
				</c:when>
			</c:choose>
		</div>
		<div class="modal-footer" align="left" style="width:$(window).width()/2;">
			
		</div>
		<br>
		<input type="hidden" id="expenseNumber" name="expenseNumber" value="${requestScope.task.textAttribute10}" />
		<input type="hidden" id="requesterId" name="requesterId" value="${requestScope.task.textAttribute11}" />
		<input type="hidden" id="requestDate" name="requestDate" value="${requestScope.task.dateAttribute1}" />
		<input type="hidden" id="amount" name="amount" value="<fmt:formatNumber type="number" value="${requestScope.task.numberAttribute1}" pattern="######" />" />
		<input type="hidden" id="paymentType" name="paymentType" value="${requestScope.task.textAttribute12}" />
		<input type="hidden" id="expenseType" name="expenseType" value="${requestScope.task.textAttribute13}" />
		<input type="hidden" id="justification" name="justification" value="${requestScope.task.textAttribute14}" />
		<input type="hidden" id="dueDate" name="dueDate" value="<fmt:formatDate value="${requestScope.task.dateAttribute2.time}" type="date" dateStyle="long" pattern="yyyy-MM-dd HH:mm" />" />
		<input type="hidden" id="outcome" name="outcome" value="${requestScope.task.outcome}" />
		<input type="hidden" id="currency" name="currency" value="${requestScope.task.textAttribute16}" />
		<input type="hidden" id="docnum" name="docnum" value="${requestScope.task.textAttribute15}" />
		<input type="hidden" id="tcode" name="tcode" value="${requestScope.task.textAttribute2}" />
		
		<input type="hidden" id="line" name="line" value="" />
		<input type="hidden" id="ruleset" name="ruleset" value="" />

		<textarea id="body_xml" name="body_xml" style="display:none"></textarea>
		<input type="hidden" id="tasknumber" name="tasknumber" value="${requestScope.task.number}" />
		<input type="hidden" id="activityName" name="activityName" value="${requestScope.task.activityName}" />
		
	</form>
</div>
<div>
	<iframe id="sapiframe" name="sapiframe" src="" style="width:100%; height:auto; min-height: 500px; overflow: auto;">
	</iframe>
</div>
<OBJECT ID="HWAUCtrl"
	CLASSID="CLSID:427BD09A-B354-4AF3-89CC-7EB3B315554B"
	WIDTH="1" HEIGHT="1" VIEWASTEXT>
</OBJECT>
</body>
</html>