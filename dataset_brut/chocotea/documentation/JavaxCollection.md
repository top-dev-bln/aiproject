# ☕️ JavaxCollection
This annotation is to be used together with `@JavaxRequest` for API(S) written using [javax](https://mvnrepository.com/artifact/javax.ws.rs/javax.ws.rs-api).

## Fields
### name
This is the name of the collection, default is "Sample Collection".

### createTest
This is a boolean value to enable creating tests for the requests or not, default is false;

### baseUrl
This is the base url that should be used across the collection, default is "{{BASE_URL}}"

## Example
This example shows a spring collection variables defined
```java
@JavaxCollection(
        name = "susan more collection", 
        createTest = true, 
        baseUrl = "https://example.com")
public class PathGenerator {
    
    @JavaxRequest(requestBean = Info.class, auth = Auth.Type.bearer, authValue = "ABCTOKEN")
    @PostMapping(value = "/{resortID}/seasons/{seasonID}/days/{dayID}/skiers/{skierID}")
    public String generate(
            @PathVariable( "resortID" )  int resortID,
            @RequestParam("id") @NotNull @ChocoRandom(dynamic = DynamicVariables.randomCatchPhrase) String id,
            @RequestBody Info resource, 
            @RequestHeader("Authorization") String authorization){
        ....
    }
    
}
```
