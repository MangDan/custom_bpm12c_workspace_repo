<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!-- 
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
<title>서울 반도체 BPMS에 오신것을 환영합니다.</title>
<link href="<c:url value='/resources/css/bpms/cascade/bpms_default.css' />" rel="stylesheet">
-->
<!DOCTYPE html>
<html>
<head>
<meta charset="utf-8" />
<meta http-equiv="x-ua-compatible" content="ie=edge" />
<meta name="viewport" content="width=device-width, initial-scale=1, minimum-scale=1, maximum-scale=1" />
<meta name="format-detection" content="telephone=no" />
<meta name="apple-mobile-web-app-status-bar-style" content="black" />
<title>서울 반도체 BPMS에 오신것을 환영합니다.</title>
<link rel="stylesheet" href="<c:url value='/resources/skin/css/common.css' />" /> 
<script type="text/javascript" src="<c:url value='/resources/skin/js/jquery-1.11.0.min.js' />"></script> 
<script type="text/javascript" src="<c:url value='/resources/skin/js/ui.js' />"></script>
<script type="text/javascript" src="<c:url value='/resources/skin/js/mobilerange.min.js' />"></script>
<script type="text/javascript" src="<c:url value='/resources/skin/js/bxslider.js' />"></script>
<script type="text/javascript" src="<c:url value='/resources/skin/js/jquery.easing.min.1.3.js' />"></script>
<script type="text/javascript" src="<c:url value='/resources/skin/js/jquery.jcontent.0.8.min.js' />"></script>
<!--[if lt IE 9]>
	<link rel="stylesheet" type="text/css" href="<c:url value='/resources/skin/css/ie8.min.css' />" />
	<script type="text/javascript" src="<c:url value='/resources/skin/js/ui8.js' />"></script>
<![endif]-->
<script type="text/javascript">
<!--
function getCookie(cookieName)
{
	var cookieValue = document.cookie;
	var cookieStartsAt = cookieValue.indexOf(" " + cookieName + "=");
	
	if (cookieStartsAt == -1)
	{
		cookieStartsAt = cookieValue.indexOf(cookieName + "=");
	}
  
	if (cookieStartsAt == -1)
	{
		cookieValue = null;
	}
	else
	{
		cookieStartsAt = cookieValue.indexOf("=", cookieStartsAt) + 1;
		var cookieEndsAt = cookieValue.indexOf(";", cookieStartsAt);
		if (cookieEndsAt == -1)
		{
			cookieEndsAt = cookieValue.length;
		}
		cookieValue = unescape(cookieValue.substring(cookieStartsAt, cookieEndsAt));
	}

	return cookieValue;
}

function setCookie(cookieName, cookieValue, expireDayAfter)
{
	try
	{
		cookieValue = escape(cookieValue);

		var expireDate = new Date;
		expireDate.setDate(expireDate.getDate() + expireDayAfter); 

		document.cookie = cookieName + "=" + cookieValue + "; path=/; expires=" + expireDate.toGMTString() + ";";
	}
	catch(e)
	{
		alert("setCookie()\n\n"+e);
	}
}
function submitForm(){
    try{
        
		if(loginform.idsave_check.checked) {
			
			saveLogin(loginform.j_username.value);
	 	
	 	} else {
			saveLogin("");
		}
		
		if(loginform.j_username.value == "" ){
			
			alert('Please check you ID');
			loginform.j_username.focus();  
			return;
			
		}else if(loginform.j_password.value == ''){
			
			alert('Please check your password');
			loginform.j_password.focus(); 
			return;    

		}else if(loginform.j_password.value == 'welcome1'){
			loginform.submit(); 
		
		}else{
			loginform.submit();
		}
        
    }catch(e){
            alert(location.href+"\n\n"+"submitForm()"+"\n\n"+e.description);
    }
}


function saveLogin(id) {
	
	if(id != "") {
		setCookie("j_username", id, 7);
	}else{
		setCookie("j_username", id, -1);
	}
}

function getLogin() { 

	var f = document.loginform;

	var id = getCookie("j_username");

	if(id != "" && id != null) {
		f.j_username.value = id;
		f.idsave_check.checked = true;
		f.j_password.focus();
	}
}

function MM_preloadImages() { //v3.0
  getLogin();
  var d=document; if(d.images){ if(!d.MM_p) d.MM_p=new Array();
    var i,j=d.MM_p.length,a=MM_preloadImages.arguments; for(i=0; i<a.length; i++)
    if (a[i].indexOf("#")!=0){ d.MM_p[j]=new Image; d.MM_p[j++].src=a[i];}}
}

function MM_swapImgRestore() { //v3.0
  var i,x,a=document.MM_sr; for(i=0;a&&i<a.length&&(x=a[i])&&x.oSrc;i++) x.src=x.oSrc;
}

function MM_findObj(n, d) { //v4.01
  var p,i,x;  if(!d) d=document; if((p=n.indexOf("?"))>0&&parent.frames.length) {
    d=parent.frames[n.substring(p+1)].document; n=n.substring(0,p);}
  if(!(x=d[n])&&d.all) x=d.all[n]; for (i=0;!x&&i<d.forms.length;i++) x=d.forms[i][n];
  for(i=0;!x&&d.layers&&i<d.layers.length;i++) x=MM_findObj(n,d.layers[i].document);
  if(!x && d.getElementById) x=d.getElementById(n); return x;
}

function MM_swapImage() { //v3.0
  var i,j=0,x,a=MM_swapImage.arguments; document.MM_sr=new Array; for(i=0;i<(a.length-2);i+=3)
   if ((x=MM_findObj(a[i]))!=null){document.MM_sr[j++]=x; if(!x.oSrc) x.oSrc=x.src; x.src=a[i+2];}
}
$(document).ready(function(){
	$('#j_username').focus();
	
	$( ":button" ).click(function() {
		
		var f = document.loginform;
		loginform.j_username.value = this.id;
		loginform.j_password.value = "welcome1";
		loginform.submit();
	});
});

//-->
</script>
</head>
<body class="login">
<form name="loginform" id="loginform" action="j_spring_security_check" method="post">
<div id="wrapper">
	<div class="login_wrap">
		<p>
			<img src="<c:url value='/resources/skin/images/login/logo.jpg' />" alt="서울반도체로고" />
			<span class="login_txt_tit01">WELCOME TO THE</span>
			<span class="login_txt_tit02">SEOUL SEMICONDUCTOR <em>BPM</em></span>
			<span class="login_txt_tit03">HOME SEOUL SEMICONDUCTOR. APPS, TOOLS AND NEWS. FOR ALL EMPLOYEES.</span>
		</p>

		<div class="login_txtbox">

			<!-- 20140707_v01_수정 <span class="leaf_left"><img src="<c:url value='/resources/skin/images/login/login_img01.png' />" alt="" /></span> -->
			<!-- 20140707_v01_수정 <span class="leaf_right"><img src="<c:url value='/resources/skin/images/login/login_img02.png' />" alt="" /></span> -->
			<!-- 20140707_v01_수정 <span class="leaf_right2"><img src="<c:url value='/resources/skin/images/login/login_img03.png' />" alt="" /></span> -->

			<div>

				<ul>
					<li>
						<dl>
							<dt><span>User ID</span></dt>
							<dd>
								<!--
								<input name="j_username" type="text" value="weblogic" style="FONT-SIZE: 9pt; color:#363636; FONT-FAMILY:굴림; background-color:#FFFFFF; BORDER-RIGHT: #C9C9C9 1px solid; BORDER-TOP: #C9C9C9 1px solid; BORDER-LEFT: #C9C9C9 1px solid; BORDER-BOTTOM: #C9C9C9 1px solid" size="18" onkeydown="javascript: if (event.keyCode == 13){submitForm();}"> 
								 -->
								<input id="j_username" name="j_username" type="text" value="" style="width: 350px;" onkeyup="javascript: if (event.keyCode == 13){submitForm();}" />
							</dd>
						</dl>
					</li>
					<li>
						<dl>
							<dt><span>Password</span></dt>
							<dd>
							<!-- 
								<input name="j_password" type="password" value="welcome1" style="FONT-SIZE: 9pt; color:#363636; FONT-FAMILY:굴림; background-color:#FFFFFF; BORDER-RIGHT: #C9C9C9 1px solid; BORDER-TOP: #C9C9C9 1px solid; BORDER-LEFT: #C9C9C9 1px solid; BORDER-BOTTOM: #C9C9C9 1px solid" size="18" onkeydown="javascript: if (event.keyCode == 13){submitForm();}">
							 -->
								<input name="j_password" type="password" value="welcome1" style="width: 350px;" onkeydown="javascript: if (event.keyCode == 13){submitForm();}"/>
							</dd>
						</dl>
					</li>
				</ul>

				<a href="javascript:submitForm();" class="btn_lgin"><span>LOGIN</span></a>

				<!-- <div class="pass_txt">Passwords are case sensitive</div> -->
				<table class="pass_txt">
					<tr>
						<td>&nbsp;<input type="button" value="노홍철(대리)" id="draftera">&nbsp;<input type="button" value="하동훈(대리)" id="sapusera">&nbsp;<input type="button" value="정형돈(과장)" id="sapuserb">&nbsp;<input type="button" value="정준하(과장)" id="accounta">&nbsp;<input type="button" value="박명수(차장)" id="approvaluser1">&nbsp;<input type="button" value="유재석(부장)" id="approvaluser2">&nbsp;<input type="button" value="BPM관리자" id="bpmadmin"></td>
					</tr>
				</table>
				
				
				
				<!-- (20140716_v01) -->
				 <!-- <dl class="error_txt_input">
					<dt><img src="<c:url value='/resources/skin/images/icon/icon_warning.gif' />" alt="warning" /></dt>
					<dd>To reset your Portal account password, enter  your new password
							(Caution : If you are HVCC domain users, then PC login password is also changed the same password within few minutes) </dd>
				 </dl> -->
				<!-- //(20140716_v01) -->

			</div>
		</div>

		<!-- <dl>
			<dt>WARNING</dt>
			<dd>
				<p>All information and data acquired from Seoul Semiconductor or developed or acquired for Seoul Semiconductor<br />
				shall be confidential and proprietary to Seoul Semiconductor.<br />
				Such information and data shall be used only in performing services for Seoul Semicconductor<br />
				and shall not be disclosed or caused to be disclosed to any third part without written authorization from Seoul Semiconductor.
				</p>
				<p>
				By supplying my CDS ID and Password below, l agree to read and adhere to seoul semiconductor<br />
				Global IT Policies before accessing information on the web.<br />
				I understand my responsibility to protect Seoul Semiconductor customer and supplier information per Seoul Semiconductor Policies.
				</p>
			</dd>
		</dl> -->
		
	</div>
</div>
</form>
</body>
</html>