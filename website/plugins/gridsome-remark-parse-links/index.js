const visit = require('unist-util-visit')
const path = require('path')

const EXTERNAL_REGEX = /^https?:\/\//
const FILE_EXTENSION_REGEX = /(?:\/README|\/index)?(?:\.md|\.html)$/

const isExternal = (str) => EXTERNAL_REGEX.test(str)

module.exports = function attacher () {
  return async function transform (tree, file, callback) {
    if (!file?.data?.node?.path) return callback()

    visit(tree, 'link', (node) => {
      if (!node.url || isExternal(node.url)) return

      let [link, ...anchor] = node.url.split('#')

      // Remove file extension (.md or .html)
      link = link.replace(FILE_EXTENSION_REGEX, '')

      // Use nodejs's path utils to join the target path with the current page's path
      const absolutePagePath = path.join(file.data.node.path, link)

      node.url = [absolutePagePath, ...anchor].join('#')
    })

    callback()
  }
}
