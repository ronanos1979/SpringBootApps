<%@ include file="../common/header.jspf"%>
<%@ include file="../common/navigation.jspf"%>

<div class="container">
	<h1>Enter User Details:</h1>
	<form:form method="POST" modelAttribute="user">
		<form:hidden path="id" />

		<fieldset class="mb-3">
			<form:label path="username">Username:</form:label>
			<form:input type="text" path="username" required="required" />
			<form:errors path="username" cssClass="text-warning" />
		</fieldset>

		<fieldset class="mb-3">
			<form:label path="password">Password:</form:label>
			<form:password path="password" showPassword="false" />
			<form:errors path="password" cssClass="text-warning" />
		</fieldset>

		<fieldset class="mb-3">
			<form:label path="email">Email</form:label>
			<form:input type="text" path="email" required="required" />
			<form:errors path="email" cssClass="text-warning" />
		</fieldset>		

		<fieldset class="mb-3">
			<form:label path="firstName">First Name:</form:label>
			<form:input type="text" path="firstName"/>
			<form:errors path="firstName" cssClass="text-warning" />
		</fieldset>

		<fieldset class="mb-3">
			<form:label path="lastName">Last Name:</form:label>
			<form:input type="text" path="lastName"/>
			<form:errors path="lastName" cssClass="text-warning" />
		</fieldset>
		

		<input type="submit" class="btn btn-success" />
	</form:form>
</div>
<%@ include file="../common/footer.jspf"%>
