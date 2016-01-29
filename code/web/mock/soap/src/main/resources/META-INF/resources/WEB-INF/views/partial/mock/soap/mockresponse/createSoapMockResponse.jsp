<%--
 Copyright 2016 Karl Dahlgren

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
--%>

<%@ include file="../../../../includes.jspf"%>
<c:url var="create_soap_mock_response_url"  value="/web/soap/project/${soapProjectId}/port/${soapPortId}/operation/${soapOperation.id}/create/response" />
<div class="content-top">
    <h1><spring:message code="soap.createsoapmockresponse.header.createmockresponse"/></h1>
</div>
<form:form action="${create_soap_mock_response_url}" method="POST">
    <div class="content-summary">
        <table class="formTable">
            <tr>
                <td class="column1"><form:label path="name"><spring:message code="soap.createsoapmockresponse.label.name"/></form:label></td>
                <td class="column2"><form:input path="name" id="soapMockResponseNameInput"/></td>
            </tr>
            <tr>
                <td class="column1"><form:label path="httpStatusCode"><spring:message code="soap.createsoapmockresponse.label.httpstatuscode"/></form:label></td>
                <td class="column2"><form:input path="httpStatusCode" id="soapMockResponseHttpStatusCodeInput"/></td>
                <td><label id="httpCodeDefinitionLabel"><spring:message code="soap.createsoapmockresponse.label.httpstatuscodedefinition"/>:&nbsp;</label><label id="httpCodeLabel"></label></td>
            </tr>
        </table>
    </div>
    <div>
        <h2 class="decorated"><span><spring:message code="soap.createsoapmockresponse.header.body"/></span></h2>
        <div class="editor">
            <form:textarea id="body" path="body"/>
        </div>
    </div>

    <div>
        <h2 class="decorated"><span><spring:message code="soap.createsoapmockresponse.header.headers"/></span></h2>

        <fieldset>
            <legend><spring:message code="soap.createsoapmockresponse.field.addheader"/></legend>
            <table class="formTable">
                <tr>
                    <td class="column1"><form:label path="name"><spring:message code="soap.createsoapmockresponse.label.headername"/></form:label></td>
                    <td class="column2"><input type="text" name="headerName" id="headerNameInput"></td>
                </tr>
                <tr>
                    <td class="column1"><form:label path="name"><spring:message code="soap.createsoapmockresponse.label.headervalue"/></form:label></td>
                    <td class="column2"><input type="text" name="headerValue" id="headerValueInput"></td>
                </tr>
            </table>
            <button class="button-success pure-button" onclick="addHeader()" type="button"><i class="fa fa-plus"></i>  <span><spring:message code="soap.createsoapmockresponse.button.addheader"/></span></button>
        </fieldset>

        <div class="table-frame">
            <table class="entityTable" id="headerTable">
                <col width="4%">
                <col width="48%">
                <col width="48%">
                <tr>
                    <th></th>
                    <th><spring:message code="soap.createsoapmockresponse.column.headername"/></th>
                    <th><spring:message code="soap.createsoapmockresponse.column.headervalue"/></th>
                </tr>
            </table>
        </div>
    </div>

    <button class="button-success pure-button" type="submit" name="submit"><i class="fa fa-plus"></i>  <span><spring:message code="soap.createsoapmockresponse.button.createmockresponse"/></span></button>
    <a href="<c:url value="/web/soap/project/${soapProjectId}/port/${soapPortId}/operation/${soapOperation.id}"/>" class="button-error pure-button"><i class="fa fa-times"></i> <span><spring:message code="soap.createsoapmockresponse.button.cancel"/></span></a>
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
</form:form>
<script src=<c:url value="/resources/js/headerTable.js"/>></script>
<script src=<c:url value="/resources/js/editor.js"/>></script>
<script>
    $("#soapMockResponseNameInput").attr('required', '');
    $("#soapMockResponseHttpStatusCodeInput").attr('required', '');
    enableTab('body');
    initiateHttpResponseCode('soapMockResponseHttpStatusCodeInput','httpCodeLabel', 'httpCodeDefinitionLabel');
</script>