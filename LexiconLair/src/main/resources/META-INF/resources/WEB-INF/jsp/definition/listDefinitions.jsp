<%@ include file="../common/header.jspf" %>
<%@ include file="../common/navigation.jspf" %>

<div class="container">
	<div>Welcome ${name}</div>
	<hr>
	<h1>The Definitions are:</h1>
	<table class="table">
		<thead>
			<tr>				
				<th>Id</th>
				<th>Word</th>
				<th>Language</th>
				<th>Definition Text</th>
				<th>Part Of Speech</th>
				<th>Example</th>
				<th>Source API</th>
				<th>Cached At</th>
			</tr>	
		</thead>
		<tbody>	
			<c:forEach items="${definitions}" var="definition">
				<tr>	
					<td>${definition.id}</td>
					<td>${definition.word.text}</td>
					<td>${definition.word.language}</td>
					<td>${definition.definitionText}</td>
					<td>${definition.partOfSpeech}</td>
					<td>${definition.example}</td>
					<td><a href="${definition.sourceApi}">${definition.sourceApi}</a></td>
					<td>${definition.cachedAt}</td>
					<td>
						<form method="post" action="delete-definition" class="d-inline">
							<input type="hidden" name="id" value="${definition.id}"/>
							<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
							<button type="submit" class="btn btn-warning">Delete</button>
						</form>
					</td>
				</tr>		
			</c:forEach>
		</tbody>
	</table>
</div>
<%@ include file="../common/footer.jspf" %>
	