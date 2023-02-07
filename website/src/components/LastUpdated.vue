<template>
  <div v-if="lastUpdated">
    <hr>
    <p style="margin-bottom: 1em;">
      <strong>Last Updated: </strong>
      {{ lastUpdated }}
    </p>
  </div>
</template>

<script>
export default {
  computed: {
    lastUpdated () {
      const lastUpdatedStr = this.$page.markdownPage.lastUpdated
      if (lastUpdatedStr) {
        const options = {
          weekday: 'long',
          year: 'numeric',
          month: 'long',
          day: 'numeric',
          hour: 'numeric',
          minute: 'numeric',
          second: 'numeric'
        }
        return new Date(lastUpdatedStr).toLocaleString('en-GB', options)
      }
      return null
    }
  }
}
</script>
