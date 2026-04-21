<%@ include file="common/header.jspf" %>
<%@ include file="common/navigation.jspf" %>

<div class="container">
	<div>Welcome ${name}</div>
	<hr>
	<h1>The Authors are:</h1>
	<table class="table">
		<thead>
			<tr>				
				<th>Id</th>
				<th>First Name</th>
				<th>Last Name</th>
			</tr>	
		</thead>
		<tbody>	
			<c:forEach items="${authors}" var="author">
				<tr>	
					<td>${author.id}</td>
					<td>${author.firstName}</td>
					<td>${author.lastName}</td>
					<td><a href="update-author?id=${author.id}" class="btn btn-primary">Update</a></td>
					<td>
						<form method="post" action="delete-author" class="d-inline">
							<input type="hidden" name="id" value="${author.id}"/>
							<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
							<button type="submit" class="btn btn-warning">Delete</button>
						</form>
					</td>
					
				</tr>		
			</c:forEach>
		</tbody>
	</table>
<a href="add-author" class="btn btn-success">Add Author</a>
</div>
<%@ include file="common/footer.jspf" %>
	
