module.exports = {
  extends: ['stylelint-config-standard', 'stylelint-config-recommended-scss'],
  rules: {
    'no-descending-specificity': null,
    'at-rule-allowed-list': [
      'include',
      'import',
      'media',
      'extend',
      'font-face',
      'mixin',
      'keyframes',
      'if',
      'for',
      'each',
      'function',
      'return'
    ],
    'rule-empty-line-before': null,
    'at-rule-empty-line-before': null,
    'selector-pseudo-element-no-unknown': [ true, { ignorePseudoElements: ['v-deep'] } ],
    'indentation': [2, { ignore: ['value'] } ]
  }
}
