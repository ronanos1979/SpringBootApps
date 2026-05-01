<%@ include file="../common/header.jspf"%>
<%@ include file="../common/navigation.jspf"%>

<div class="container">
	<h1>Enter Author:</h1>
	<form:form method="POST" modelAttribute="author">
		<form:hidden path="id" />

		<fieldset class="mb-3">
			<form:label path="firstName">First Name:</form:label>
			<form:input type="text" path="firstName" required="required" />
			<form:errors path="firstName" cssClass="text-warning" />
		</fieldset>

		<fieldset class="mb-3">
			<form:label path="lastName">Last Name:</form:label>
			<form:input type="text" path="lastName" required="required" />
			<form:errors path="lastName" cssClass="text-warning" />
		</fieldset>

		<input type="submit" class="btn btn-success" />
	</form:form>
</div>
<%@ include file="../common/footer.jspf"%>
