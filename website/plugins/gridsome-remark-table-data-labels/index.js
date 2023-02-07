const visit = require('unist-util-visit')
const toString = require('mdast-util-to-string')

module.exports = function attacher () {
  return async function transform (tree, file, callback) {
    visit(tree, 'table', (node) => {
      let isFirstRow = true
      const headers = []
      visit(node, 'tableRow', (rowNode) => {
        let i = 0
        visit(rowNode, 'tableCell', (cellNode) => {
          if (isFirstRow) {
            headers.push(toString(cellNode))
          } else {
            const data = cellNode.data || (cellNode.data = {})
            const hProperties = data.hProperties || (data.hProperties = {})
            hProperties['data-label'] = headers[i++]
          }
        })
        isFirstRow = false
      })
    })

    callback()
  }
}
