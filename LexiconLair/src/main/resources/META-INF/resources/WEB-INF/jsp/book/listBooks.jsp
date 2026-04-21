<%@ include file="../common/header.jspf" %>
<%@ include file="../common/navigation.jspf" %>

<div class="container">
	<div>Welcome ${name}</div>
	<hr>
	<h1>The Books are:</h1>
	<table class="table">
		<thead>
			<tr>				
				<th>Id</th>
				<th>Book</th>
				<th>Author</th>
				<th></th>
				<th></th>
			</tr>	
		</thead>
		<tbody>	
			<c:forEach items="${books}" var="book">
				<tr>	
					<td>${book.id}</td>
					<td>${book.title}</td>
					<td>${book.author.displayName}</td>
					<td><a href="update-book?id=${word.id}" class="btn btn-primary">Update</a></td>
					<td>
						<form method="post" action="delete-book" class="d-inline">
							<input type="hidden" name="id" value="${book.id}"/>
							<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
							<button type="submit" class="btn btn-warning">Delete</button>
						</form>
					</td>
				</tr>		
			</c:forEach>
		</tbody>
	</table>
<a href="add-book" class="btn btn-success">Add Book</a>
</div>
<%@ include file="../common/footer.jspf" %>
	