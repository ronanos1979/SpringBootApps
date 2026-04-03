<%@ include file="common/header.jspf" %>
<%@ include file="common/navigation.jspf" %>

<div class="container">
	<div>Welcome ${name}</div>
	<hr>
	<h1>The Words are:</h1>
	<table class="table">
		<thead>
			<tr>
				<!-- <th>Id</th> -->
				<th>Word</th>				
				<th>Description</th>
				<th>Target Date</th>
				<th>Done</th>
				<th></th>
				<th></th>
			</tr>	
		</thead>
		<tbody>	
			<c:forEach items="${words}" var="word">			
				<tr>
					<!--  <td>${word.id}</td>  -->
					<td>${word.lookupWord}</td>					
					<td>${word.description}</td>
					<td>${word.targetDate}</td>
					<td>${word.done}</td>
					<td><a href="update-word?id=${word.id}" class="btn btn-primary">Update</a></td>
					<td><a href="delete-word?id=${word.id}" class="btn btn-warning">Delete</a></td>
				</tr>		
			</c:forEach>
		</tbody>
	</table>
<a href="add-word" class="btn btn-success">Add Word</a>
</div>
<%@ include file="common/footer.jspf" %>
	