# OpenAPI

In order to have great and compehensible documentation, we've chosen to use
[OpenAPI v3.1.0](https://github.com/OAI/OpenAPI-Specification/blob/main/versions/3.1.0.md).  
This specification is frontend-independent and may also be used to [generate SDKs](https://openapi-generator.tech/).

For this project, we've opted for the [Rapidoc frontend](https://rapidocweb.com/),
due to its simplicity and compatibility with OpenAPI v3.1.0.

A version of this frontend with this project's specification is available in production at `/fenixedu-api/`.

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

Before commiting, you may validate the OpenAPI config by running `npm run validate`
in the `openapi` folder.
