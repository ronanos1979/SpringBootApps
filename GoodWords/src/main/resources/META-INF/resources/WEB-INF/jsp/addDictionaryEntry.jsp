<%@ include file="common/header.jspf"%>
<%@ include file="common/navigation.jspf"%>

<div class="container">
	<h1>Enter Word Details:</h1>
	<form:form method="POST" modelAttribute="dictionaryEntry">


		<fieldset class="mb-3">
			<form:label path="entryWord">Entry Word:</form:label>
			<form:input type="text" path="entryWord" required="required" />
			<form:errors path="entryWord" cssClass="text-warning" />
		</fieldset>


		<fieldset class="mb-3">
			<form:label path="username">Username Who Created:</form:label>
			<form:input type="text" path="username" />
			<form:errors path="username" cssClass="text-warning" />
		</fieldset>

		<fieldset class="mb-3">
			<form:label path="definition">Definition</form:label>
			<form:input type="text" path="definition" required="required" />
			<form:errors path="definition" cssClass="text-warning" />
		</fieldset>		

		<fieldset class="mb-3">
			<form:label path="dateAdded">Date Added:</form:label>
			<form:input type="hidden" path="dateAdded"/>
			<form:errors path="dateAdded" cssClass="text-warning" />
		</fieldset>

		<fieldset class="mb-3">
			<form:label path="externalLookupCompleted">External Lookup Completed:</form:label>
			<form:checkbox path="externalLookupCompleted" />
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
