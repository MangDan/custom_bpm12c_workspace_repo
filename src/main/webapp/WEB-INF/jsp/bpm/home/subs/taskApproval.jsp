<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8"%>
<table class="board_01" summary="BBS Article Control">
	<caption>담당자현황</caption>
	
	<thead>
	<!-- 
		<tr> 
			<td colspan="7"> <table width="100%" cellspacing="0" cellpadding="3">
				<tr> 
				  <td class="table_search_line" colspan="11"></td>
				</tr>
				<tr class="bg_gray_F7F7F7"> 
				  <td width="120" align="right">제목 : </td>
				  <td width="188"> 
					<input name="title" id="task_title" type="text" value="${param.title}">
				  </td>
				  <td width="70" align="right">&nbsp;</td>
				  <td width="80" align="right">상태 : </td>
				  <td><select name="task_state" class="input_combobox">
					  <option value="" ${param.task_state == '' ? 'selected' : ''}>전체</option>
					  <option value="ASSIGNED" ${param.task_state == 'ASSIGNED' ? 'selected' : ''}>진행중</option>
					  <option value="COMPLETED" ${param.task_state == 'COMPLETED' ? 'selected' : ''}>완료</option>
					</select> </td>
				  
				  <td width="80" align="right" nowrap>게시물 수 : <br></td>
				  <td colspan="2">
					<select name="tasmaxRows" class="input_combobox">
					  <option value="5" ${param.maxRows == '5' ? 'selected' : ''}>5</option>
					  <option value="10" ${param.maxRows == '10' ? 'selected' : ''}>10</option>
					  <option value="15" ${param.maxRows == '15' ? 'selected' : ''}>15</option>
					  <option value="20" ${param.maxRows == '20' ? 'selected' : ''}>20</option>
					  <option value="300" ${param.maxRows == '300' ? 'selected' : ''}>300</option>
					</select>
				  </td>
				  
				  <td width="">&nbsp;</td>
				  <td width="62" align="right">&nbsp; </td>
				  <td>&nbsp;</td>
				  <td width="200" align="right"><button id="searchbtn1" onclick="javascript:searchTasks('#task_list')">search</button>
				</tr>
			  </table></td>
		  </tr>
		  -->
		<tr>
			<th scope="col">담당자</th>
			<th scope="col">제목</th>
			<th scope="col">도착일시</th>
			<th scope="col">경과시간</th>
		</tr>
	</thead>
	<tbody>
		<c:choose>
		<c:when test="${fn:length(requestScope.result) > 0}">
		<c:set var="tc" value="0" />
		<c:forEach var="task" begin="0" items="${requestScope.result}" varStatus="ts">
		<tr> 
			<td align="center">${task.assigneeDisplayName == 'draftera' ? '기안자A' : (task.assigneeDisplayName == 'sapusera' ? '전표처리담당자A' : (task.assigneeDisplayName == 'sapuserb' ? '전표처리담당자B' : (task.assigneeDisplayName == 'approvaluser1' ? '결재자1' : (task.assigneeDisplayName == 'bpmuser1' ? 'BPM담당자1' : (task.assigneeDisplayName == 'approvaluser1' ? '결재자1' : (task.assigneeDisplayName == 'approvaluser2' ? '결재자2' : (task.assigneeDisplayName == 'approvaluser3' ? '결재자3' : task.assigneeDisplayName)))))))}</td>
			<td align="center">${task.title}</td>
			<td align="center">${task.createDate}</td>
			<td align="center"><font color="red">${task.processDelayTime == '' ? '1분미만' : task.processDelayTime}</font></td>
		</tr>
		<c:set var="tc" value="${ts.index}" />
		</c:forEach>
		<c:forEach var="lc" begin="${tc + 1}" end="4" step="1">
		<tr class="table_padding"> 
			<td align="center" colspan="4">&nbsp;</td>
		</tr>
		</c:forEach>
		</c:when>
		<c:otherwise>
		<tr class="table_padding"> 
			<td align="center" colspan="4">&nbsp; DATA 가 없습니다. &nbsp;</td>
		</tr>
		<c:forEach var="lc" begin="1" end="4" step="1">
		<tr class="table_padding"> 
			<td align="center" colspan="4">&nbsp;</td>
		</tr>
		</c:forEach>
		</c:otherwise>
		</c:choose>
	</tbody>
</table>