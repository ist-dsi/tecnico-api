# Project Structure

Since this project is just a wrapper over existing functionalities,
its structure is quite simple.

The structure is mostly divided in two layers:
the **controllers** and the **serializers**.

## Controllers

The controllers are located in the `org.fenixedu.api.ui` package, and extend
the `BaseController` class.  
Their job is to register and handle routes.

Shorthand functions for the serializers and some utils are located in the `BaseController` class,
in order to keep the code a bit cleaner and easier to read.  
Error handlers for convertion exceptions, as well as API Errors
(thrown by login in the controllers), are also in the `BaseController` class.

Each controller class relates to a part of the domain (e.g. courses, degrees, personal, spaces, etc.).

Controllers should check for permissions (when applicable), validate parameter/payload
values, as well as calling the respective serializers.

## Serializers

The serializers are located in the `org.fenixedu.api.serializer` package, and
extend the `DomainObjectSerializer` class.  
Their job is to convert a domain object into a `JsonElement` (usually `JsonObject`).

When a new serializer is created, an field in `FenixEduAPISerializer` (in the same package)
must be created.
The [`@Getter` annotation from lombok](https://projectlombok.org/features/GetterSetter)
will automatically create a getter for it (keeping the code clean!).

For the most part, serializers should have public methods like `serialize` and
`serializeExtended` (a variation that includes more information).
Depending on the object we're serializing, it might make sense to name these
a bit differently (e.g. `CurricularCourseSerializer` where we have
`serializeWithCurricularInformation`).  
Normally we don't include JavaDocs on these functions since it's pretty explicit
what they do, but if their behaviour is complex/unclear, it's always
good practice to add some explanation.

Serializers can also call other serializers and even have additional logic
if needed (e.g. OccupationSerializer checks for occupation's intervals).

If it's possible for the content to be extended (e.g. PersonSerializer with role serialization),
injection of sub-serializers is allowed and encouraged.
