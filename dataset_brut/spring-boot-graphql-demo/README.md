# GraphQL Spring Boot basic usage 

[中文](README-zh.md)

> Create a basic demo application by SpringBoot and GraphQL, save data in MongoDB 


## Create Application

- Add dependencies

```groovy
dependencies {
    implementation('org.springframework.boot:spring-boot-starter-web')
    implementation('org.springframework.boot:spring-boot-starter-data-mongodb')

    compileOnly('org.projectlombok:lombok')
    testImplementation('org.springframework.boot:spring-boot-starter-test')
}
```

### Add Interface

- Add Model

```java
@Data
@Builder
@Document
@AllArgsConstructor
@NoArgsConstructor
public class Post {

    @Id
    private String id;

    private String title;

    private String content;

    @CreatedDate
    private Date createDate;
}
```

- Add Repository 

```java
public interface PostRepository extends MongoRepository<Post, String> {
}
```

- Modify Configuration

```properties
# MongoDB Config
spring.data.mongodb.host=localhost
spring.data.mongodb.port=27017
#spring.data.mongodb.username=
#spring.data.mongodb.password=
spring.data.mongodb.database=graphql
```

- Add data initializer

```java
@Component
@Slf4j
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private PostRepository postRepository;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<Post> posts = initPost();
        posts.forEach(post -> log.info("Post: {}", post));
    }

    private List<Post> initPost() {
        postRepository.deleteAll();

        return Stream.of("Post one", "Post two")
                .map(title -> {
                    Post post = Post.builder()
                            .title(title)
                            .content("Content of " + title)
                            .build();
                    return postRepository.save(post);
                })
                .collect(Collectors.toList());
    }
}
```

### Configure GraphQL  

- Add dependencies of  GraphQL

```groovy
    implementation('com.graphql-java:graphql-spring-boot-starter:5.0.2')
    // Provide UI    
    implementation('com.graphql-java:graphiql-spring-boot-starter:5.0.2')
    // For Resolver    
    implementation('com.graphql-java:graphql-java-tools:5.2.4')
```

- Add GraphQL definition script post.grahpqls

```graphql
# io.github.helloworlde.graphql.model.Post corresponding Model
type Post {
    id: ID,
    title: String,
    content: String,
    createDate: String
}


# io.github.helloworlde.graphql.resolver.PostMutation.updatePost argument post
input PostInput{
    title: String!,
    content: String!
}

# query for io.github.helloworlde.graphql.resolver.PostQuery
type Query{
    posts: [Post]
    post(id: ID!): Post
}

# modify for io.github.helloworlde.graphql.resolver.PostMutation
type Mutation{
    createPost(post: PostInput): Post!
    updatePost(id: ID!, post: PostInput): Post!
    deletePost(id: ID!): String
}
```

#### Add Interface Resolver

- Query Resolver

```java
@Component
public class PostQuery implements GraphQLQueryResolver {

    @Autowired
    private PostRepository postRepository;

    public List<Post> posts() {
        return postRepository.findAll();
    }

    public Optional<Post> post(String id) {
        return postRepository.findById(id);
    }

}
```

- Modify Resolver

```java
@Component
public class PostMutation implements GraphQLMutationResolver {

    @Autowired
    private PostRepository postRepository;

    public Post createPost(Post post) {
        Post newPost = Post.builder()
                .title(post.getTitle())
                .content(post.getContent())
                .build();

        return postRepository.save(newPost);
    }

    public Post updatePost(String id, Post post) throws Exception {
        Post currentPost = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post " + id + " Not Exist"));

        currentPost.setTitle(post.getTitle());
        currentPost.setContent(post.getContent());

        return postRepository.save(currentPost);
    }

    public String deletePost(String id) throws Exception {
        postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Post " + id + " Not Exist"));

        postRepository.deleteById(id);
        return id;
    }
}
```

## Test 

- Startup Application

- Access [http://localhost:8080/graphiql](http://localhost:8080/graphiql)

![GraphQL](/img/GraphQL.png)

#### Query  

- Query List

```graphql
{
  posts {
    id
    title
    content
    createDate
  }
}
```

```json
{
  "data": {
    "posts": [
      {
        "id": "5c50245b7ed65eacb3372aba",
        "title": "Post one",
        "content": "Content of Post one",
        "createDate": "Tue Jan 29 18:00:59 CST 2019"
      },
      {
        "id": "5c50245b7ed65eacb3372abb",
        "title": "Post two",
        "content": "Content of Post two",
        "createDate": "Tue Jan 29 18:00:59 CST 2019"
      }
    ]
  }
}
```

- Query Specified Post by id

```graphql
{
  post(id: "5c50245b7ed65eacb3372aba") {
    id
    title
    content
    createDate
  }
}
```

```json
{
  "data": {
    "post": {
      "id": "5c50245b7ed65eacb3372aba",
      "title": "Post one",
      "content": "Content of Post one",
      "createDate": "Tue Jan 29 18:00:59 CST 2019"
    }
  }
}
```

#### Modify

- Create

```graphql
mutation {
  createPost(post: {title: "New Posts", content: "New Post Content"}) {
    id
    title
    content
    createDate
  }
}
```

```json
{
  "data": {
    "createPost": {
      "id": "5c5027197ed65eaf47a0854d",
      "title": "New Posts",
      "content": "New Post Content",
      "createDate": "Tue Jan 29 18:12:41 CST 2019"
    }
  }
}
```

- Update 

```graphql
mutation {
  updatePost(id: "5c5027197ed65eaf47a0854d", post: {title: "Update Posts", content: "Update Post Content"}) {
    id
    title
    content
    createDate
  }
}
```

```json
{
  "data": {
    "updatePost": {
      "id": "5c5027197ed65eaf47a0854d",
      "title": "Update Posts",
      "content": "Update Post Content",
      "createDate": "Tue Jan 29 18:12:41 CST 2019"
    }
  }
}
```

- Delete

```graphql
mutation {
  deletePost(id: "5c5027197ed65eaf47a0854d")
}
```

```json
{
  "data": {
    "deletePost": "5c5027197ed65eaf47a0854d"
  }
}
```

--- 

### References

- [GraphQL](http://graphql.cn/)
- [Spring Boot + GraphQL + MongoDB](https://medium.com/oril/spring-boot-graphql-mongodb-8733002b728a)
- [graphql-spring-boot](https://github.com/graphql-java-kickstart/graphql-spring-boot)
- [learn-graphql](https://github.com/zhouyuexie/learn-graphql)
- [graphql-mongodb-server](https://github.com/leonardomso/graphql-mongodb-server)