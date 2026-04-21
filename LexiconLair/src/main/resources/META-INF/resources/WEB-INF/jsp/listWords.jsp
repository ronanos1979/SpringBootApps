<%@ include file="common/header.jspf" %>
<%@ include file="common/navigation.jspf" %>

<div class="container">
	<div>Welcome ${name}</div>
	<hr>
	<h1>The Words are:</h1>
	<table class="table">
		<thead>
			<tr>				
				<th>Id</th>
				<th>Text</th>
				<th>Locale</th>
				<th></th>
				<th></th>
			</tr>	
		</thead>
		<tbody>	
			<c:forEach items="${words}" var="word">
				<tr>	
					<td>${word.id}</td>
					<td>${word.text}</td>
					<td>${word.locale}</td>
					<td><a href="update-word?id=${word.id}" class="btn btn-primary">Update</a></td>
					<td>
						<form method="post" action="delete-word" class="d-inline">
							<input type="hidden" name="id" value="${word.id}"/>
							<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
							<button type="submit" class="btn btn-warning">Delete</button>
						</form>
					</td>
					
				</tr>		
			</c:forEach>
		</tbody>
	</table>
<a href="add-word" class="btn btn-success">Add Word</a>
</div>
<%@ include file="common/footer.jspf" %>
	
