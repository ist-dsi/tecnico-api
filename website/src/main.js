// This is the main.js file. Import global CSS and scripts here.
// The Client API can be used here. Learn more: gridsome.org/docs/client-api
import '~/styles/index.scss'

import DefaultLayout from '~/layouts/Default.vue'

export default function (Vue, { router, head, isClient }) {
  // import font styles from static server
  head.link.push({
    rel: 'stylesheet',
    href: 'https://static.tecnico.ulisboa.pt/fonts/klavika-nova/index.css'
  })
  head.link.push({
    rel: 'stylesheet',
    href: 'https://static.tecnico.ulisboa.pt/fonts/source-sans-pro/index.css'
  })

  // Set default layout as a global component
  Vue.component('Layout', DefaultLayout)
}
