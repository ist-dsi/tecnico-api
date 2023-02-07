<template>
  <div
    class="site-side-menu-layout"
    :class="{
      'site-side-menu-layout--menu-open': navIsOpen,
      'site-side-menu-layout--page-lock': pageIsLocked
    }"
  >
    <slot name="page-top" />

    <header class="site-header">
      <header-bar
        :nav-is-open.sync="navIsOpen"
        :search-is-open.sync="searchIsOpen"
      />
      <left-menu :menu="sidebarItems" />
    </header>
    <main class="main-content main-content__content">
      <slot name="top" />
      <slot />
      <slot name="bottom" />
    </main>

    <Footer />

    <slot name="page-bottom" />
  </div>
</template>

<static-query>
query {
  metadata {
    sidebar
  }
}
</static-query>

<script>
import HeaderBar from '~/components/HeaderBar.vue'
import LeftMenu from '~/components/LeftMenu.vue'
import Footer from '~/components/Footer.vue'

export default {
  name: 'DefaultLayout',

  components: {
    HeaderBar,
    Footer,
    LeftMenu
  },

  data () {
    return {
      navIsOpen: false,
      searchIsOpen: false
    }
  },

  computed: {
    sidebarItems () {
      return JSON.parse(this.$static.metadata.sidebar)
    },

    pageIsLocked: function () {
      if (this.navIsOpen || this.searchIsOpen) {
        return true
      } else {
        return false
      }
    }
  }
}
</script>
