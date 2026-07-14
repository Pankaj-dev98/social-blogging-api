# Blog-post API

[GITHUB](https://github.com/Pankaj-dev98/mct-m4-submission-geekster)

## Frameworks and languages used
- Source code: Java 21
- Base framework: SpringBoot with inbuilt TomCat server
- Dependencies: spring-boot-starter-web, Spring data JPA, Spring-boot-dev-tools, MySQL-jdbc-driver, Project Lombok, SpringDoc-api, spring-doc openApi(Swagger)

## Data flow
- Controller: Rest service handler methods are declared here. Entities have separate controllers which know which function to call when an endpoint is hit.
- Services: Each entity has its own service class which are springboot managed beans that handle business logic of the API. These methods implement the system requirements and have references to the data access layer.
- Repository: This is the persistence layer that performs CRUD on the database.

## Data structures used
- All collections have been implemented using the List<T> interface, which is a requirement for @Entity classes. Sets have not been implemented to avoid recursive hashing of dependent entities.

## Project summary
- This is a small-sized but well-built application that allows user to perform many social operations such as
  * User can sign up to be recognized as a `user` of the app.
  * He must be signed-in to be able to access the available API methods.
  * User can post a blog-post which he's henceforth considered the `owner` of.
  * User can comment of any post available in the application. These comments can later be retrieved as they belong to a user as well as the host post.
  * There are comprehensive title-search, pagination features available for easier interactions.

## Data Model diagram:

![](/images/DataModel.png)