<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ page pageEncoding="UTF-8"%>
<c:set var="titleKey">
	<tiles:insertAttribute name="title" ignore="true" />
</c:set>
<c:set var="title" scope="request">
<spring:message code="${titleKey}" text="${titleKey}" />&nbsp;-&nbsp;<spring:message code="site.name" text="Smart Knowledgebase" />
</c:set>
<c:set var="apiList">
	<tiles:insertAttribute name="apiList" ignore="true" />
</c:set>
<!DOCTYPE html>
<html>
<head>
	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1">

<title>${title}</title>
	<link rel="shortcut icon" href="<c:url value="/resources/favicon.ico" />">	
	<link rel="stylesheet" href="<c:url value="/resources/css/jquery.mobile-1.4.5.min.css" />">

	<script src="<c:url value="/resources/js/jquery.js" />"></script>
	<script src="<c:url value="/resources/js/jquery.mobile-1.4.5.min.js" />"></script>
<c:if test="${not empty apiList}">
<script type="text/javascript" src="http://res.wx.qq.com/open/js/jweixin-1.0.0.js"></script>
<script type="text/javascript">

wx.config({
    debug: true,
    appId: '${appId}',
    timestamp:'${timestamp}',
    nonceStr: '${noncestr}',
    signature: '${signature}',
    jsApiList: [${apiList}] // 必填，需要使用的JS接口列表，所有JS接口列表见附录2
});
</script>
</c:if>
</head>
<body>
<div data-role="page" id="myPage" class="jqm-demos jqm-home">

<tiles:insertAttribute name="header" />
<tiles:insertAttribute name="content" />	

<tiles:insertAttribute name="footer" />


</div><!-- /page -->

</body>
</html>