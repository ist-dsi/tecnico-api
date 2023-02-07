<template>
  <div class="navigation">
    <ul class="utility-menu">
      <li class="utility-menu__item utility-menu__item--site-logo">
        <g-link to="/">
          <h1 class="site-logo">
            <svg
              class="site-logo__symbol"
              width="30"
              height="37"
              viewBox="0 0 30 37"
              xmlns="http://www.w3.org/2000/svg"
              xmlns:xlink="http://www.w3.org/1999/xlink"
            >
              <defs>
                <path
                  id="a"
                  d="M29.719 36.251H0V.182h29.719z"
                />
              </defs>
              <g
                fill="none"
                fill-rule="evenodd"
              >
                <mask
                  id="b"
                  fill="#fff"
                >
                  <use xlink:href="#a" />
                </mask>
                <path
                  d="M22.77 11.57h-1.237v9.336h-2.678V11.57H17.62V9.698h5.15v1.873zm-6.596-4.844v17.157c0 2.224-1.796 3.375-4.01 3.375-2.215 0-4.027-.971-4.027-2.86h.003c0-.74.599-1.342 1.337-1.342a1.34 1.34 0 0 1 1.336 1.343h.002c0 1.37.109 2.301 1.348 2.301 1.332 0 1.332-.934 1.332-2.817l.002-17.157c0-2.222 1.795-3.374 4.01-3.374 2.214 0 4.026.97 4.026 2.86h-.003a1.34 1.34 0 0 1-1.337 1.341 1.34 1.34 0 0 1-1.337-1.341h-.001c0-1.371-.109-2.302-1.348-2.302-1.333 0-1.333.933-1.333 2.816zM8.142 9.698h2.67l.004 11.2H8.139l.003-11.2zM0 .182V15.32c0 13.63 14.832 20.93 14.832 20.93s14.887-7.3 14.887-20.93V.182H0z"
                  fill="#009DE0"
                  mask="url(#b)"
                />
              </g>
            </svg>
            <span class="site-logo__full-name">API Documentation</span>
            <span class="site-logo__short-name">
              <abbr title="API Documentation"> API Docs </abbr>
            </span>
          </h1>
        </g-link>
      </li>
      <li
        v-click-outside="closeNav"
        class="utility-menu__item utility-menu__item--nav-trigger"
      >
        <a
          href="#"
          class="utility-menu__link utility-menu__link--icon"
          @click.stop.prevent="toggleNav($event)"
        >
          <i
            :class="{ 'burger-icon--open': navIsOpen }"
            class="burger-icon"
          >
            <span class="burger-icon__dashes" />
          </i>
        </a>
      </li>
    </ul>
  </div>
</template>

<script>
export default {
  name: 'HeaderBar',
  components: {},
  directives: {
    'click-outside': {
      bind: function (el, binding, vNode) {
        // Provided expression must evaluate to a function.
        if (typeof binding.value !== 'function') {
          const compName = vNode.context.name
          let warn = `[Vue-click-outside:] provided expression '${binding.expression}' is not a function, but has to be`
          if (compName) {
            warn += `Found in component '${compName}'`
          }

          console.warn(warn)
        }
        // Define Handler and cache it on the element
        const bubble = binding.modifiers.bubble
        const handler = (e) => {
          if (bubble || (!el.contains(e.target) && el !== e.target)) {
            binding.value(e)
          }
        }
        el.__vueClickOutside__ = handler

        // add Event Listeners
        document.addEventListener('click', handler)
      },

      unbind: function (el, binding) {
        // Remove Event Listeners
        document.removeEventListener('click', el.__vueClickOutside__)
        el.__vueClickOutside__ = null
      }
    }
  },
  props: {
    navIsOpen: {
      type: Boolean,
      required: true
    },
    searchIsOpen: {
      type: Boolean,
      required: true
    }
  },
  mounted () {
    window.addEventListener('resize', this.resizeReconfiguration)
  },
  methods: {
    openNav () {
      this.$emit('update:navIsOpen', true)
    },
    closeNav () {
      this.$emit('update:navIsOpen', false)
    },
    resizeReconfiguration () {
      this.closeNav()
    },
    toggleNav (e) {
      this.$emit('update:navIsOpen', !this.navIsOpen)
    },
    linkComponent (entry) {
      return entry.external ? 'a' : 'g-link'
    },
    targetComponent (entry) {
      return entry.external ? '_blank' : ''
    }
  }
}
</script>
