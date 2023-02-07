# Formatter

In order to guarantee code style consistency across the project, we're using
`formatter-maven-plugin` with a custom style (based on IntelliJ's default style).

## Running the Formatter

To run the formatter, execute:

```bash
mvn formatter:format
```

If you just want to validate that the files adhere to the formatting config, execute:

```bash
mvn formatter:validate
```

This command is also executed on CI, on every commit.

## IDE Integration

### IntelliJ IDEA

Go to `File > Settings > Editor > Code Style > Java` and import the `formatter-config.xml`
as an _Eclipse XML Profile_.

Keep in mind this applies to other repositories you might have in the same project as well.

### Visual Studio Code

**This repository already comes with this preconfigured, so the steps below are not necessary.**

Just change the following option on your VSCode settings (for this project):

```json
{
  "java.format.settings.url": "formatter-config.xml"
}
```
