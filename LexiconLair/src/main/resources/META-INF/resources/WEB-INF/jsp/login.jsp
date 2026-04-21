<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
<link href="/webjars/bootstrap/5.3.8/css/bootstrap.css" rel="stylesheet">
<title>Welcome to the Login Page</title>
</head>
<body>
<div class="container mt-4">
	<h1>Welcome to the Login Page</h1>
	<c:if test="${param.error != null}">
		<div class="alert alert-danger">Invalid username or password.</div>
	</c:if>
	<c:if test="${param.logout != null}">
		<div class="alert alert-success">You have been logged out.</div>
	</c:if>
	<form method="post" action="/login">
		<div class="mb-3">
			<label for="username" class="form-label">Username</label>
			<input id="username" type="text" name="username" class="form-control" required="required"/>
		</div>
		<div class="mb-3">
			<label for="password" class="form-label">Password</label>
			<input id="password" type="password" name="password" class="form-control" required="required"/>
		</div>
		<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
		<input type="submit" class="btn btn-primary" value="Sign In"/>
	</form>
	<script src="/webjars/bootstrap/5.3.8/js/bootstrap.js"></script>
</div>
</body>
</html>
