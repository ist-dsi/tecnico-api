# OAuth2 Scopes

## Adding a new scope

To add a new scope, two changes must be made:

- adding it to the `APIScope` enum, in order to allow it to be used for an endpoint
- adding it to the scopes list under _Personal > External Applications > Application Management_ (on Fénix)

To add it to the `pt.ist.tecnicoapi.util.APIScope` enum, simply add a new value to the enum.
The scope key should follow the format `read:<scope>` or `write:<scope>`, where `<scope>` uses
[snake_case](https://en.wikipedia.org/wiki/Snake_case).

To add it to the scopes list (on the database), a Fénix Administrator must go to
_Personal > External Applications > Application Management_ and click the _Create_ button
under _Scopes_.  
Fill in the fields accordingly and click _Create_.

## Limit an endpoint to a scope

By default, endpoints do not require authentication. To require a specific scope,
call the `requireOAuthScope` method in the beginning of the route handler.  
The value of the `Authorization` header must be passed to this method.

Example:

```java
@RequestMapping(value = "/example", method = RequestMethod.GET)
public ResponseEntity<?> example(@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) final String accessToken) {
    requireOAuthScope(accessToken, APIScope.EXAMPLE);
    // ...
}
```

## Testing scoped endpoints with Insomnia

In order to test an endpoint that requires a scope, you must pass a valid access token
in the `Authorization` header.

[Insomnia](https://insomnia.rest/) (and similar software) gives you an easy way to authenticate
using OAuth2.

Before setting it up, an application must be created on Fénix under
_Personal > External Applications > Manage Applications_. Under _My Applications_,
click _Create_.  
Fill in an arbritary name and description and select the scopes you want to test.
Under _Redirect Url_, you can put `http://localhost:8000`, but it doesn't matter as long
as it is the same as on Insomnia below.  
After creating the application, you can get the _Client ID_ and _Client Secret_ by clicking
the _Details_ button.

When making a request, under the _Auth_ tab, select _OAuth 2.0_. Fill in the fields
accordingly:

- **Grant Type:** `Authorization Code`
- **Authorization URL:** `http://localhost:8080/oauth/userdialog`
- **Access Token URL:** `http://localhost:8080/oauth/access_token`
- **Client ID:** The one generated in the step above
- **Client Secret:** The one generated in the step above
- **Use PKCE:** No
- **Redirect URL:** The same one you put in the application in the step above

Then, click the "Fetch Tokens" button and login.
