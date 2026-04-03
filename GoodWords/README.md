# myfirstwebapp
## Notes from UDemy Course

## GitHub Links
https://github.com/in28minutes/spring-boot-master-class/blob/master/02.Spring-Boot-Web-Application-V2/99-step-by-step-changes.md#step01
and
https://github.com/in28minutes/spring-boot-master-class/blob/master/02.Spring-Boot-Web-Application-V2/99-step-by-step-changes.md#step05

# Notes
Passing from controller to JSP is by putting it into a model.
Spring MVC provides a model map.

### JSP
JSP added Expression language EL

## Model 1 Architecture
All the logic was in the JSPs. There were no controllers. The logic for the database and view logic and flow logic all existed in the JSP´s. It ended up with very complex JSP´s which were difficult to maintain.

## Model 2 Architecture
Model is the data which is used to generate the view.
View is responsible for showing information to the user.
Controller/Servlets control the entire flow.

Browser -> Servlet -> View -> Model

## Model 2 Architecture with Front Controller
Browser -> Front Controller -> Servlet1 -> Model
								-> View1 -> Model
								-> Servlet2 -> Model
								
This is used so that we can share logic for authentication etc. between servlets.

## Dispatcher Servlet
Dispatcher Servlet is the Spring MVC implementation of the Model 2 Front Controller.
Receives all requests
First identifies correct Controller method.
Executes the correct controller method.
That returns the model and the view name.
Dispatcher Servlet talks to View Resolver then renders the view.

1. Identifies correct controller method
LoginController.gotoLoginPage
2. Executes controller method
Puts data into model
Returns view name (login)
3. Identify correct view.
Executes View Resolver 
/WEB-INF/jsp/login.jsp
4. Executes View
Output of View from JSP sent back to browser.

## JSTL Changes
Course Update: Spring Boot 3.2.x JSTL Change


3 Important Changes
Change 01: Use jakarta.servlet.jsp.jstl instead of glassfish-jstl

Change 02: Use jakarta.tags.core instead of http://java.sun.com/jsp/jstl/core as taglib

Change 03: Run mvn clean install to update libraries



Details: https://github.com/in28minutes/master-spring-and-spring-boot/blob/main/spring-boot-3.2.x-changes.md


Change 01: Use jakarta.servlet.jsp.jstl instead of glassfish-jstl
Spring Boot 3.2.x and greater

<dependency>
    <groupId>org.glassfish.web</groupId>
    <artifactId>jakarta.servlet.jsp.jstl</artifactId>
</dependency>
jakarta.servlet.jsp.jstl replaces glassfish-jstl (recommended for Spring Boot <= 3.1.x)

<dependency>
     <groupId>org.eclipse.jetty</groupId>
     <artifactId>glassfish-jstl</artifactId>
</dependency>


Change 02: Use jakarta.tags.core instead of http://java.sun.com/jsp/jstl/core as taglib
Spring Boot 3.2.X and greater
Use

<%@ taglib prefix="c" uri="jakarta.tags.core" %>
Spring Boot 3.1.X and lower
Use

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>


Change 03: Run mvn clean install to update libraries
And then execute the mvn clean install command


								
## JSTL
https://docs.oracle.com/javaee/5/jstl/1.1/docs/tlddocs/c/tld-summary.html


## Stay up to date
Use thoughtworks radar

## Validations with Spring Boot
1. Spring Boot Started Validation
	add to pom.xml
2. Command Bean (Form Backing Object)
	Command Beans implement 2 way binding jsp and Controller. This 2 way binding is from the bean to the form and from the form to the bean. The bean to the form for initial values presented to user. The form to the bean after the user inputs the values and submits.
	Use Spring Form Tag Libraries
	https://www.javatpoint.com/spring-mvc-form-tag-library
	
	
3. Add Validations to Bean
4. Display Validation Errors in the View



##  JDBC to Spring JDBC

### JDBC Example
* Write a lot of SQL queries
* And a lot of JAVA Code

```
	String DELETE_TODO_QUERY=DELETE FROM TODOY WHERE ID=?";
public void deleteTodo (int id) {
	PreparedStatement st = null;
	try {
		st = db.conn.prepareStatement(DELETE_TODO_QUERY);
		st.setInt(1, id);
		st.execute();
	} catch (SQLException e) {
		logger.fatal("Query Failed: " + DELETE_TODO_QUERY, e);
	} finally {
		if (st != null) {
			try {st.close();]
			catch (SQLException e) {}
		}
	}
]	
```

### Spring JDBC Example
* Write a lot of SQL Queryes
* But not as much JAVA Code

```
String DELETE_TODO_QUERY=DELETE FROM TODOY WHERE ID=?";
public void deleteTodo(int id) {
	jdbcTemplate.update(DELETE_TODO_QUERY, id);	
}
```

### JPA Java Persistence API
* Do not worry about queries
* Just Map Entities to Tables
* Don't really write special code, just JAVA code

```
return entityManager.find(Person.class, id);
```

### Spring Data JPA
* Make JPA even more simple

```
public interface TodoRespoiutory extends JpaRepository<Todo, Integer>{}
```

## Database
### MySQL
```
create database todos;
CREATE USER 'todos-user'@'localhost';
GRANT ALL PRIVILEGES ON todos.* To 'todos-user'@'localhost';
alter user 'todos-user'@'localhost' IDENTIFIED BY 'dummytodos';
```



## Docker

### Docker MySQL
docker run --detach --env MYSQL_ROOT_PASSWORD=dummypassword --env MYSQL_USER=todos-user --env MYSQL_PASSWORD=dummytodos --env MYSQL_DATABASE=todos --name mysql --publish 3306:3306 mysql:8-oracle


### Docker commands

```
docker ls
docker ps -a
docker stop mycontainer
```





	
