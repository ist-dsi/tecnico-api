<template>
  <ul :class="menu.class">
    <template v-if="menu.parent">
      <li class="back">
        <a
          :href="menu.parent.parent.entries[0].path"
          @click="back($event)"
        >
          Back
        </a>
      </li>
      <li class="parent">
        <component
          :is="linkComponent(menu.parent)"
          :href="menu.parent.path"
          :to="menu.parent.path"
          :title="menu.parent.title"
        >
          {{ menu.parent.title }}
        </component>
      </li>
    </template>
    <li
      v-for="entry in menu.entries"
      :key="entry.id"
      :class="entry.class"
    >
      <a
        v-if="linkComponent(entry) === 'a'"
        :href="entry.path"
        :title="entry.title"
        target="_blank"
      >
        {{ entry.title }}
      </a>
      <g-link
        v-else
        :to="entry.path"
        :title="entry.title"
        event=""
        @click.native.prevent="open($event, entry)"
      >
        {{ entry.title }}
      </g-link>

      <left-menu-entries
        v-if="entry.children"
        :depth="depth + 1"
        :menu="entry.children"
        @scroll-top="$emit('scroll-top')"
      />
    </li>
  </ul>
</template>

<script>
export default {
  name: 'LeftMenuEntries',
  props: {
    menu: {
      type: Object,
      required: true
    },
    depth: {
      type: Number,
      default: 0
    }
  },
  methods: {
    back (event) {
      event.stopPropagation()
      event.preventDefault()
      this.menu.parent.parent.class = this.menu.parent.parent.class.replace(
        'moves-out',
        ''
      )
      this.menu.class += ' is-hidden'
    },
    open (event, entry) {
      if (!entry.children) {
        this.$router.push({ path: entry.path })
      } else {
        event.stopPropagation()
        event.preventDefault()
        this.menu.class += ' moves-out'
        entry.children.class = entry.children.class.replace('is-hidden', '')
        this.$emit('scroll-top')
      }
    },
    linkComponent (entry) {
      return /^https?:\/\//.test(entry.path) ? 'a' : 'g-link'
    }
  }
}
</script>
