<template>
  <Layout>
    <template #top>
      <h1 v-if="$page.markdownPage.title">
        {{ $page.markdownPage.title }}
      </h1>
    </template>

    <!-- eslint-disable-next-line vue/no-v-html -->
    <div
      class="theme-default-content"
      v-html="$page.markdownPage.content"
    />

    <template #bottom>
      <last-updated />
    </template>
  </Layout>
</template>

<page-query>

query($id: ID!) {
  markdownPage(id: $id) {
    title
    content
    lastUpdated
    excerpt
  }
}
</page-query>

<script>
import LastUpdated from '../components/LastUpdated.vue'

export default {
  name: 'MarkdownPage',

  components: { LastUpdated },

  data () {
    return {}
  },

  metaInfo () {
    const title = this.$page.markdownPage.title
    const description = (this.$page.markdownPage.excerpt || '').replace(`${title} `, '')
    return {
      title,
      meta: [{ name: 'description', content: description }]
    }
  }
}
</script>

<style lang="scss">
.theme-default-content {
  margin: 2rem 0 3.5rem;

  h1:first-child {
    display: none;
  }

  .header-anchor {
    display: none;
  }
}
</style>
