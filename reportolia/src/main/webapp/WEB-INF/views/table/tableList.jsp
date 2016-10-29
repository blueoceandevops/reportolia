<%@ page contentType="text/html" %> 
<%@ taglib prefix="html" tagdir="/WEB-INF/tags/html" %> 
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<html:page>
	<jsp:attribute name="pageTitle"><spring:message code="dbTables.title" /></jsp:attribute>
	
	<jsp:attribute name="scripts">js/reportolia/jquery/plugins/springy/springy.js,js/reportolia/jquery/plugins/springy/springyui.js</jsp:attribute>
	
	<jsp:attribute name="scriptBody">
		
		var MSG_RETRIEVE = '<spring:message code="msg.confirm.metadata.retrieve" />',
			BTN_RETRIEVE = '<spring:message code="dbTables.button.retrieveFromDb" />',
			BTN_ADD_MANUALLY = '<spring:message code="dbTables.button.addManually" />'
			LBL_IS_SECURED = '<spring:message code="dbTables.isSecured" />',
			LBL_HAS_SECURITY_FILTER = '<spring:message code="dbTables.hasSecurityFilter" />'
			;
			
		
		function onLoad() {
			$('#tabs').tabs({
			  active: 0
			});
			
			var tbl = r3p.jTable('tableList', {
	            title: '<spring:message code="dbTables.description"/>',
	           
	            sorting: true, //Enable sorting
	            defaultSorting: 'name ASC', //Set default sorting
	            height: 800,
	            actions: {
	                listAction: 'r3pTablesLoad.go', 
	                deleteAction: 'r3pTableDelete.go',
	                updateAction: 'r3pTableSave.go'
	            },
	            fields: {
	                id: {
	                    key: true,
	                    create: false,
	                    edit: false,
	                    list: false
	                },
	                name: {
	                    title: r3pMsg.LBL_NAME
	                },
	                label: {
	                    title: r3pMsg.LBL_LABEL
	                },
	                secured: {
	                	title: LBL_IS_SECURED,
	                	width: '20%',
	                	type: 'checkbox',
	                	values: { 'false' : r3pMsg.OPT_NO, 'true' : r3pMsg.OPT_YES },
	                	display: function(data) {
	                    	if (data.record.secured) return '&#10004;';
	                    	return '';
	                    }
	                },
	                securityFilter: {
	                	title: LBL_HAS_SECURITY_FILTER,
	                	width: '20%',
	                	type: 'checkbox',
	                	values: { 'false' : r3pMsg.OPT_NO, 'true' : r3pMsg.OPT_YES },
	                	display: function(data) {
	                    	if (data.record.securityFilter) return '&#10004;';
	                    	return '';
	                    }
	                }
	            },
	            toolbar: {
				    items: [{
				        text: BTN_RETRIEVE,
				        click: function () {
				            openDlgMetadata();
				        }
				    }]
				}
	        });
	        
		        
		    <c:if test="${isTableListEmpty}">
				retrieveFromDb();
			</c:if>
			<c:if test="${!isTableListEmpty}">
		        loadTableList();
		    </c:if> 
		}
		
		function loadTableList() {
			$('#tableList').jtable('load');
		}
		
		function retrieveFromDb() {
			var dlgButtons = {};
			dlgButtons[BTN_RETRIEVE] = function() {
				openDlgMetadata();
				r3p.closeDlg(this);
			};
			dlgButtons[BTN_ADD_MANUALLY] = function() {
				r3p.closeDlg(this);
			};
			
			r3p.showConfirm(MSG_RETRIEVE, null, {
				buttons: dlgButtons
			});
		}
	</jsp:attribute>
	<jsp:attribute name="body">
		<div id="tabs">
		
			<jsp:include page="../tableTabs.jsp"/>
			
			<div id="tabTables">
				<div id="tableList"></div>
				
			</div>
			
		</div>
		
		<div id="dlgMetadata" class="clsHidden"><jsp:include page="../metadata/dlgMetadata.jsp"/></div>
		
	</jsp:attribute>
</html:page>
