<%@ include file="common/header.jspf" %>
<%@ include file="common/navigation.jspf" %>

<div class="container">
	<div>Welcome ${name}</div>
	<hr>
	<h1>The Dictionary Entries are:</h1>
	<table class="table">
		<thead>
			<tr>				
				<th>Id</th>
				<th>Entry Word</th>
				<th>Definition</th>
				<th>Added By</th>								
				<th>Date Added</th>
				<th>External Lookup Completed</th>
				<th></th>
				<th></th>
			</tr>	
		</thead>
		<tbody>	
			<c:forEach items="${dictionaryEntries}" var="dictionaryEntry">			
				<tr>	
					<td>${dictionaryEntry.id}</td>  				
					<td>${dictionaryEntry.entryWord}</td>
					<td>${dictionaryEntry.definition}</td>	
					<td>${dictionaryEntry.username}</td>					
					<td>${dictionaryEntry.dateAdded}</td>
					<td>${dictionaryEntry.externalLookupCompleted}</td>									
					<td><a href="update-word?id=${dictionaryEntry.id}" class="btn btn-primary">Update</a></td>
					<td><a href="delete-word?id=${dictionaryEntry.id}" class="btn btn-warning">Delete</a></td>
					
				</tr>		
			</c:forEach>
		</tbody>
	</table>
<a href="add-dictionary-entry" class="btn btn-success">Add Dictionary Entry</a>
</div>
<%@ include file="common/footer.jspf" %>
	