<%@ include file="common/header.jspf" %>
<%@ include file="common/navigation.jspf" %>
<div class="container">
	<div>Welcome to the Lexicon Lair!</div>
	<div> Welcome: ${name}</div>
	<hr/>
	<div><a href="/list-users">Manage your Users</a></div>
	<div><a href="/list-words">Manage your Words</a></div>
	<div><a href="/list-authors">Manage your Authors</a></div>
	<div><a href="/list-books">Manage your Books</a></div>
</div>	
<%@ include file="common/footer.jspf" %>
	