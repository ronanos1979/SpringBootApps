<%@ include file="../common/header.jspf" %>
<%@ include file="../common/navigation.jspf" %>

<div class="container">
	<div>Welcome ${name}</div>
	<hr>
	<h1>The App Users are:</h1>
	<table class="table">
		<thead>
			<tr>				
				<th>Id</th>
				<th>Username</th>
				<th>Email</th>
				<th>FirstName</th>
				<th>LastName</th>
				<th>Updated By</th>
				<th>Last Updated</th>
			</tr>	
		</thead>
		<tbody>	
			<c:forEach items="${users}" var="user">
				<tr>	
					<td>${user.id}</td>
					<td>${user.username}</td>
					<td>${user.email}</td>
					<td>${user.firstName}</td>
					<td>${user.lastName}</td>
					<td>${user.updatedBy}</td>
					<td>${user.lastUpdated}</td>
					<td><a href="update-user?id=${user.id}" class="btn btn-primary">Update</a></td>
					<td>
						<form method="post" action="delete-user" class="d-inline">
							<input type="hidden" name="id" value="${user.id}"/>
							<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
							<button type="submit" class="btn btn-warning">Delete</button>
						</form>
					</td>
					
				</tr>		
			</c:forEach>
		</tbody>
	</table>
<a href="add-user" class="btn btn-success">Add User</a>
</div>
<%@ include file="../common/footer.jspf" %>
	
