FenixEdu API
=========

> API Module for Application Family

## Using this module

To use this module, follow the steps below:

- Install the module:

```sh
mvn clean install
```
- Add this module to fenix-webapp [pom.xml](https://github.com/ist-dsi/fenix-webapp/blob/master/pom.xml) dependencies list:

```xml
<dependency>
    <groupId>org.fenixedu</groupId>
    <artifactId>fenixedu-api</artifactId>
    <version>DEV-SNAPSHOT</version>
</dependency>
```

## Troubleshooting

Installing this module will trigger npm scripts to install the frontend, therefore, if there are some errors while ``mvn clean install``, the requirements for the frontend application are available [here](https://repo.dsi.tecnico.ulisboa.pt/FenixEdu/application/fenixedu-api/blob/master/src/main/frontend/README.md).