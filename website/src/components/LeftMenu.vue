<template>
  <nav class="main-menu">
    <div
      ref="navMenu"
      class="menu"
    >
      <left-menu-entries
        :menu="preparedMenu"
        @scroll-top="scrollToTop"
      />
    </div>
  </nav>
</template>

<script>
import LeftMenuEntries from './LeftMenuEntries'

export default {
  name: 'LeftMenu',
  components: {
    LeftMenuEntries
  },
  props: {
    menu: {
      type: Array,
      required: true
    }
  },
  data: function () {
    return {
      preparedMenu: {}
    }
  },
  watch: {
    menu () {
      this.preparedMenu = this.prepare()
    },
    $route () {
      this.preparedMenu = this.prepare()
    }
  },
  created () {
    this.preparedMenu = this.prepare()
  },
  methods: {
    prepare () {
      const path = this.$route.params[0] || this.$route.path
      const parts = path.split('/').filter(part => part !== '')

      if (this.menu.length) {
        const preparedMenu = this.buildFrom({ entries: this.menu }) || {}
        const current = this.findCurrent({
          entries: preparedMenu.entries,
          parts
        })
        this.buildClasses({ menu: preparedMenu })
        current && this.buildCurrent({ current })
        return preparedMenu
      }
      return {}
    },

    buildFrom ({ entries, parent }) {
      if (!entries.length) return null
      parent = parent || null
      const result = {
        parent: parent,
        class: ''
      }
      result.entries = this.buildEntriesFrom({ entries, parent: result })
      return result
    },

    buildEntriesFrom ({ entries, parent }) {
      parent = parent || null
      return entries.map((entry) => {
        const result = {
          ...entry,
          parent,
          class: ''
        }
        result.children = this.buildFrom({
          entries: entry.children || [],
          parent: result
        })
        return result
      })
    },

    buildClasses ({ menu }) {
      this.buildEntriesClasses({ entries: menu.entries })
      menu.class = menu.parent ? ' children is-hidden' : ''
    },

    buildEntriesClasses ({ entries }) {
      for (const entry of entries) {
        entry.children && this.buildClasses({ menu: entry.children })
        entry.class = entry.children ? ' has-children' : ''
      }
    },

    buildCurrent ({ current }) {
      current.class += ' current'
      let curr = current.children ? current : current.parent.parent
      while (curr && curr.parent) {
        curr.children.class = curr.children.class.replace('is-hidden', '')
        curr.parent.class += ' moves-out'
        curr = curr.parent ? curr.parent.parent : false
      }
    },

    findCurrent ({ entries, parts }) {
      const entry = entries.find(
        (entry) => entry.path.split('/').pop() === parts[0]
      )
      parts.shift()
      if (parts.length > 0 && entry && entry.children) {
        return this.findCurrent({ entries: entry.children.entries, parts })
      }
      return entry
    },

    scrollToTop () {
      if (document.body.clientWidth >= 75 * 16) { // 75 rem * 16 px
        // Desktop view
        document.querySelector('html').scroll({ behavior: 'smooth', top: 0 })
      } else {
        // Mobile view
        // Wait for the sliding animation to end, otherwise the animation is in diagonal
        setTimeout(() =>
          this.$refs.navMenu.scroll({ behavior: 'smooth', top: 0 }), 250)
      }
    }
  }
}
</script>
