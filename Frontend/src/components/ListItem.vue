<template>
  <div>
    <img title="Clear all selections" v-on:click="clearMarks" width="16px"
         src="../assets/clear.svg"/>
    <img v-if="link" title="Open document source"
         v-on:click="sourceLink" width="16px"
         src="../assets/source.svg"/>
    {{ item.date }} {{ item.title.length > 0 ? item.title.length : item.placeOfPublication }} {{ item.corpus }}
    <div ref="text"/>
  </div>
</template>
<script>
import {TextMark} from './../textmark.js'

export default {
  name: 'ListItem',
  props: ['index', 'item', 'terms', 'link'],
  watch: {
    terms: function () {
      this.markTerms(this.terms)
    }
  },
  mounted: function () {
    let t = this
    t.textMark = new TextMark(t.$refs.text, t.item.text)
    t.textMark.leftAddCallback(t.addInclude)
    t.textMark.leftRemoveCallback(t.removeInclude)
    t.textMark.rightAddCallback(t.addExclude)
    t.textMark.rightRemoveCallback(t.removeExclude)
    t.textMark.setLeftSelector('mark-include')
    t.textMark.setRightSelector('mark-exclude')
    t.markTerms(t.terms)
  },
  methods: {
    markTerms: function (terms) {
      let t = this
      t.textMark.clear('mark-search')
      for (let i in t.terms) {
        let term = t.terms[i]
        t.textMark.addClass(TextMark.className(term), 'mark-search')
      }
    },
    addInclude: function (term) {
      let t = this
      t.$emit('selectionEvent', {
        type: 'addInclude',
        id: t.item.id,
        term: term.innerText
      })
    },
    removeInclude: function (term) {
      let t = this
      t.$emit('selectionEvent', {
        type: 'removeInclude',
        id: t.item.id,
        term: term.innerText
      })
    },
    addExclude: function (term) {
      let t = this
      t.$emit('selectionEvent', {
        type: 'addExclude',
        id: t.item.id,
        term: term.innerText
      })
    },
    removeExclude: function (term) {
      let t = this
      t.$emit('selectionEvent', {
        type: 'removeExclude',
        id: t.item.id,
        term: term.innerText
      })
    },
    clearMarks: function () {
      let t = this
      t.textMark.clear('mark-include')
      t.textMark.clear('mark-exclude')
      t.$emit('selectionEvent', {type: 'clear', id: t.item.id})
    },
    sourceLink: function () {
      window.open(this.item.link)
    }
  }
}
</script>
<style>
  .mark-include {
    background-color: #4575b4;
  }

  .mark-exclude {
    background-color: #d73027;
  }

  .mark-search {
    background-color: #abd9e9;
  }

  .text {
    cursor: pointer;
    transition-duration: 0.1s;
  }

  .text:hover {
    background-color: #8aeaf8;
  }
</style>
