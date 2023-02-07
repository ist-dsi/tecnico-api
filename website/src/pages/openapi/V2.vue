<template>
  <div>
    <rapi-doc
      spec-url="/openapi/v2/openapi.yaml"
      show-header="false"
      primary-color="#009DE0"
      show-method-in-nav-bar="as-colored-text"
      use-path-in-nav-bar="true"
      render-style="read"
      allow-spec-url-load="false"
      allow-spec-file-load="false"
      allow-spec-file-download="true"
    />
    <script
      type="module"
      src="https://unpkg.com/rapidoc/dist/rapidoc-min.js"
    />
  </div>
</template>

<script>
export default {
  name: 'OpenAPIv2Docs',

  metaInfo: {
    title: 'Técnico API v2 OpenAPI Docs'
  }
}
</script>
