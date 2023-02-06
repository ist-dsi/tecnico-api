# OpenAPI

In order to have great and compehensible documentation, we've chosen to use
[OpenAPI v3.1.0](https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.1.0.md).  
This specification is frontend-independent and may also be used to [generate SDKs](https://openapi-generator.tech/).

For this project, we've opted for the [Rapidoc frontend](https://rapidocweb.com/),
due to its simplicity and compatibility with OpenAPI v3.1.0.

A version of this frontend with this project's specification can be made available
in production at `/tecnico-api/docs` by the site administrator.

All branches merged into the default branch (i.e. `master`) must update
the OpenAPI documentation with the respective changes, in order to avoid
having outdated documentation.

## Previewing Documentation in Development

In order to check the end result without having to recompile the project, you can
launch a dev server for Rapidoc.  
The server is launched on [`localhost:3000`](http://localhost:3000) and does **not**
have hot reload (meaning you have to refresh the page after saving a file).

```
cd openapi
npm install && npm run start
```

## Validating the Config

Before committing, you may validate the OpenAPI config by running `npm run validate`
in the `openapi` folder.

## Adding the Menu Entry

To add the menu entry for the OpenAPI documentation, that is, for the static files
to be served, you must go to _System Management > Portal Configuration > Manage Menu_.
Then, click _Install_, search for _Técnico API Frontend_ and click _Install Application_.

## Project Good-Practices

### operationId

All route definitions must contain an `operationId`.
The value of this field should match the name of the Java controller function for the endpoint.

### Authenticated Routes

All routes that required an OAuth scope must have 401 and 403 on their responses definition.

For routes that don't already have a 401 and 403, it's easy to do this:

```yaml
responses:
  # ... other responses
  "401":
    $ref: "../responses/OAuthUnauthorizedResponse.yaml"
  "403":
    $ref: "../responses/OAuthForbiddenResponse.yaml"
```

For routes that already have one (or both) of these responses, you have to manually merge the response object.  
For example:

```yaml
responses:
  # ... other responses
  "401":
    $ref: "../responses/OAuthUnauthorizedResponse.yaml"
  "403":
    description: |-
      [original description of response]

      ---

      OAuth errors may still apply:  
      [original description of oauth response]
    content:
      application/json:
        schema:
          $ref: "../schemas/Error.yaml"
        examples:
          # ... errors of endpoint response

          # manually add all examples for the oauth response (example below for 403 forbidden)
          app-banned:
            $ref: "../responses/OAuthForbiddenResponse.yaml#/content/application%2Fjson/examples/app-banned"
          insufficient-scopes:
            $ref: "../responses/OAuthForbiddenResponse.yaml#/content/application%2Fjson/examples/insufficient-scopes"
```
