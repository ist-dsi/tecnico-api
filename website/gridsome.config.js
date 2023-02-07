// This is where project configuration and plugin options are located.
// Learn more: https://gridsome.org/docs/config

// Changes here require a server restart.
// To restart press CTRL + C in terminal and run `gridsome develop`

module.exports = {
  siteName: 'Técnico API Documentation',
  outputDir: 'public',
  plugins: [
    {
      use: '@gridsome/source-filesystem',
      options: {
        path: 'docs/**/*.md',
        typeName: 'MarkdownPage',
        index: ['index', 'README'],
        remark: {
          // Config options can be added here
        }
      }
    },
    {
      use: 'gridsome-plugin-git-history',
      options: {
        typeName: 'MarkdownPage',
        targetPath: 'gitData',
        gitlog: {
          number: 1, // Limit to the last commit
          fields: ['subject', 'authorName', 'authorDate']
        }
      }
    }
  ],
  transformers: {
    remark: {
      plugins: [
        require('./plugins/gridsome-remark-parse-links'),
        'gridsome-plugin-remark-mermaid',
        '@gridsome/remark-prismjs',
        [
          require('./plugins/gridsome-remark-containers'),
          {
            defaultTitles: { tip: 'Tip', warning: 'Warning!', danger: 'Danger!', details: 'Expand' }
          }
        ],
        require('./plugins/gridsome-remark-table-data-labels')
      ]
    }
  },
  templates: {
    MarkdownPage: [
      {
        path: (node) => {
          return node.path.replace(/^\/(?:docs|git-source)\//, '/')
        }
      }
    ]
  }
}
