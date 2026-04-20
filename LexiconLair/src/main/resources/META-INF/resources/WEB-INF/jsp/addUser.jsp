<%@ include file="common/header.jspf"%>
<%@ include file="common/navigation.jspf"%>

<div class="container">
	<h1>Enter Word Details:</h1>
	<form:form method="POST" modelAttribute="user">

		<fieldset class="mb-3">
			<form:label path="username">Username:</form:label>
			<form:input type="text" path="username" />
			<form:errors path="username" cssClass="text-warning" />
		</fieldset>

		<fieldset class="mb-3">
			<form:label path="password">Password:</form:label>
			<form:input type="password" path="password" required="required" />
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
<%@ include file="common/footer.jspf"%>
<script type="text/javascript">
	$('#dateAdded').datepicker({
	    format: 'yyyy-mm-dd'
	});
	</script>
