# Documentation project

To run this project manually, you need to set the following environment variables:

- `GITLAB_API_USERNAME`: Your GitLab username
- `GITLAB_API_ACCESSTOKEN`: A GitLab access token with the scope `read_api` or `api`, which you can create in your [GitLab profile](https://repo.dsi.tecnico.ulisboa.pt/-/profile/personal_access_tokens).

```bash
export GITLAB_API_USERNAME=<YOUR_USERNAME>
export GITLAB_API_ACCESSTOKEN=<VALUE_OF_ACCESS_TOKEN>
```

## How to create and edit content

1. Create markdown content in the `docs/` folder
   - Markdown tutorial in https://repo.dsi.tecnico.ulisboa.pt/documentation/docs-info/-/blob/master/documents-format.md
2. Each service/subservice will appear in the sidebar, following the hierarchy.

### Use in GitLab with WEB IDE

Good for simple edits

### Use in localhost

Good for edits to Gridsome configuration or lots of changes to content, because it is possible to preview without waiting for the CI to run

#### Configuration

1. Have NodeJS version 16 -> https://nodejs.org/en/download/
2. Run in shell
   - `git clone git@repo.dsi.tecnico.ulisboa.pt:documentation/documentation.projects.dsi.tecnico.ulisboa.pt.git'`
3. In project folder run in shell
   - `npm i`
4. Run in shell:
   - `npm run dev`
5. Go to http://localhost:8080/
