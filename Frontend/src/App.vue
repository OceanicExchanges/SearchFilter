<template>
  <div class="app">
    <div class="header">
      <search-bar ref="searchBar" @searchBarEvent="searchBarEvent"/>
      <export-button @exportEvent="exportEvent"/>
    </div>
    <div id="vis"></div>
    <div id="text" v-if="texts.length > 0">
      <list v-bind:items="texts"/>
    </div>
  </div>
</template>

<script>
import SearchBar from './components/SearchBar.vue'
import ExportButton from './components/ExportButton.vue'
import InfoLine from './components/InfoLine.vue'
import List from './components/List.vue'
import {query} from './query.js'
import {search} from './search.js'
import {facets} from './facets.js'

export default {
  name: 'App',
  components: {
    'search-bar': SearchBar,
    'export-button': ExportButton,
    'info-line': InfoLine,
    'list': List
  },
  methods: {
    searchBarEvent: function (searchText) {
      search.text(searchText)
      query(`text?page=1&${search.query()}`, this.updateTexts)
      query(`query?&${search.query()}`, this.updateFilter)
    },
    exportEvent: function () {
    },
    updateTexts: function (texts) {
      this.texts = texts.documents
    },
    updateFilter: function (data) {
      this.filter.initialize(this.convert(data.documents))
        .draw()
        .update(this.filterEvent)
    },
    filterEvent: function (state) {
      search.update(state)
    },
    convert: function (data) {
      const newData = []
      const dimensions = ['corpus', 'date', 'language', 'textLength']
      for (const element of data) {
        newData.push({
          cluster: new Set([element.cluster]),
          corpus: new Set([element.corpus]),
          date: new Set([new Date(element.date)]),
          language: new Set([element.language]),
          textLength: new Set([element.textLength])
        })
      }
      newData['dimensions'] = dimensions
      newData['types'] = {
        'corpus': 'string',
        'date': 'date',
        'language': 'string',
        'textLength': 'number'
      }
      newData['dimensions'].forEach(function (d, i) { newData['types'][i] = newData['types'][d] })
      return newData
    }
  },
  mounted: function () {
    const width = window.innerWidth - 100
    this.filter = facets('#vis', width, 400, 20, '#aaaaaa')
    search.clear()
  },
  data: function () {
    return {
      texts: [],
      filter: undefined
    }
  }
}
</script>

<style>
</style>
