<%@ page import="java.util.Enumeration" %><%--
/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
--%>

<%@ include file="/html/portal/init.jsp" %>

<%
    String currentURL = PortalUtil.getCurrentURL(request);

    String referer = ParamUtil.getString(request, WebKeys.REFERER, currentURL);

    if (referer.equals(themeDisplay.getPathMain() + "/portal/update_reminder_query")) {
        referer = themeDisplay.getPathMain() + "?doAsUserId=" + themeDisplay.getDoAsUserId();
    }
%>

<aui:form action='<%= themeDisplay.getPathMain() + "/portal/update_reminder_query" %>' autocomplete='<%= PropsValues.COMPANY_SECURITY_PASSWORD_REMINDER_QUERY_FORM_AUTOCOMPLETE ? "on" : "off" %>' cssClass="update-reminder-query" method="post" name="fm">
    <aui:input name="p_auth" type="hidden" value="<%= AuthTokenUtil.getToken(request) %>" />
    <aui:input name="doAsUserId" type="hidden" value="<%= themeDisplay.getDoAsUserId() %>" />
    <aui:input name="<%= Constants.CMD %>" type="hidden" value="<%= Constants.UPDATE %>" />
    <aui:input name="<%= WebKeys.REFERER %>" type="hidden" value="<%= referer %>" />

    <c:if test="<%= SessionErrors.contains(request, UserReminderQueryException.class.getName()) %>">
        <div class="alert alert-danger">
            <liferay-ui:message key="reminder-query-and-answer-cannot-be-empty" />
        </div>
    </c:if>

    <c:forEach var = "i" begin = "1" end = "5">
        <aui:fieldset>
            <%@ include file="/html/portal/update_reminder_query_question.jspf" %>

            <aui:input autocomplete="off" cssClass="reminder-query-answer" label="answer" maxlength="<%= ModelHintsConstants.TEXT_MAX_LENGTH %>" name="reminderQueryAnswer${i}" showRequiredLabel="<%= false %>" size="50" type="text" value="<%= user.getReminderQueryAnswer() %>">
                <aui:validator name="required" />
            </aui:input>
        </aui:fieldset>
    </c:forEach>

    <aui:button-row>
        <aui:button cssClass="btn-lg" type="submit" />
    </aui:button-row>
</aui:form>