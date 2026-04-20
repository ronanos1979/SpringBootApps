<%@ include file="common/header.jspf"%>
<%@ include file="common/navigation.jspf"%>

<div class="container">
	<h1>Enter Word Details:</h1>
	<form:form method="POST" modelAttribute="book">


		<fieldset class="mb-3">
			<form:label path="text">Text:</form:label>
			<form:input type="text" path="text" required="required" />
			<form:errors path="text" cssClass="text-warning" />
		</fieldset>


		<fieldset class="mb-3">
			<form:label path="locale">Locale:</form:label>
			<form:input type="text" path="locale" />
			<form:errors path="locale" cssClass="text-warning" />
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
