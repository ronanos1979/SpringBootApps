<%@ include file="../common/header.jspf"%>
<%@ include file="../common/navigation.jspf"%>

<div class="container">
	<h1>Enter Book Details:</h1>
	<form:form method="POST" modelAttribute="bookForm">


		<fieldset class="mb-3">
			<form:label path="title">Title:</form:label>
			<form:input type="text" path="title" required="required" />
			<form:errors path="title" cssClass="text-warning" />
		</fieldset>


		<fieldset class="mb-3">
			<form:label path="authorId">Author:</form:label>
			<form:select path="authorId">
				<form:option value="" label="-- Select an author --" />
				<form:options items="${authors}" itemValue="id" itemLabel="displayName" />
			</form:select>
			<form:errors path="authorId" cssClass="text-warning" />
		</fieldset>

		<input type="submit" class="btn btn-success" />
	</form:form>
</div>
<%@ include file="../common/footer.jspf"%>
<script type="text/javascript">
	$('#dateAdded').datepicker({
	    format: 'yyyy-mm-dd'
	});
	</script>
