<template>
  <div class="app">
    <div class="header">
      <search-bar ref="searchBar" @searchBarEvent="searchBarEvent"/>
      <search-button @filterSearchEvent="filterSearchEvent"/>
      <export-button @exportEvent="exportEvent"/>
    </div>
    <info-line v-bind="infoLineData" @tagEvent="tagEvent"/>
    <div class="grid">
      <div class="grid45">
        <div id="data-loader"></div>
        <div id="geo-map"></div>
        <div id="documentCount"></div>
        <div id="textLength"></div>
        <div id="language-count"></div>
      </div>
      <div class="grid10">
        <div id="cluster-count"></div>
      </div>
      <div class="grid45">
        <div id="text-loader"></div>
        <div v-if="items.length > 0">
          <list v-bind:items="items"
                v-bind:terms="terms"
                @moreLikeThisEvent="moreLikeThisEvent"
                @selectionEvent="selectionEvent"
                @pageEvent="pageEvent"/>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import SearchBar from './components/SearchBar.vue'
import Button from './components/Button.vue'
import ExportButton from './components/ExportButton.vue'
import InfoLine from './components/InfoLine.vue'
import List from './components/List.vue'
import {query, download} from './query.js'
import {ViewCoordinator} from './viewcoordinator.js'
import {SearchState} from './searchstate.js'

export default {
  name: 'App',
  components: {
    'search-bar': SearchBar,
    'search-button': Button,
    'export-button': ExportButton,
    'info-line': InfoLine,
    'list': List
  },
  methods: {
    updateBasicInformation: function (basicInformation) {
      let t = this
      t.infoLineData.totalHits = basicInformation.totalHits
      t.infoLineData.hitsServed = basicInformation.hitsServed
      t.infoLineData.computationTime = basicInformation.computationTime
    },
    updateSelected: function (selected) {
      this.infoLineData.selected = selected
    },
    updateDocumentData: function (data) {
      this.updateBasicInformation(data.basicInformation)
      this.viewCoordinator.setDocumentData(data)
      this.hideLoader('data')
    },
    updateTextData: function (data) {
      this.items = data.documents
      this.hideLoader('text')
    },
    sendQuery: function (queryString) {
      this.displayLoader('data')
      query('query?' + encodeURI(queryString), this.updateDocumentData)
      this.displayLoader('text')
      query('text?page=1&' + encodeURI(queryString), this.updateTextData)
    },
    searchBarEvent: function (searchText) {
      this.searchState.clear()
      this.searchState.setTerms(searchText)
      let queryString = this.searchState.parameter()
      this.viewCoordinator.clear()
      this.items = []
      this.sendQuery(queryString)
      this.terms = this.searchState.terms
    },
    filterSearchEvent: function () {
      let searchBarText = this.$refs.searchBar.getInput()
      this.searchState.setTerms(searchBarText)
      let queryString = this.searchState.parameter()
      this.viewCoordinator.clear()
      this.items = []
      this.sendQuery(queryString)
    },
    exportEvent: function () {
      let searchBarText = this.$refs.searchBar.getInput()
      this.searchState.setTerms(searchBarText)
      let queryString = this.searchState.parameter()
      download('export?' + queryString)
    },
    moreLikeThisEvent: function (id) {
      this.displayLoader()
      this.displayLoader('data')
      query('like?id=' + id, this.updateDocumentData)
      this.displayLoader('text')
      query('textLike?page=1&id=' + id, this.updateTextData)
    },
    selectionEvent: function (selection) {
      this.searchState.selectionEvent(selection)
    },
    deselectionEvent: function (deselection) {
      this.searchState.deselectionEvent(deselection)
    },
    selectionClearEvent: function (id) {
      this.searchState.selectionClearEvent(id)
    },
    pageEvent: function (page) {
      let queryString = this.searchState.lastQueryString
      this.displayLoader('text')
      query('text?page=' + page + '&' + encodeURI(queryString),
        this.updateTextData)
    },
    tagEvent: function (id) {
    },
    filterEvent: function () {
      let t = this
      let searchBarText = t.$refs.searchBar.getInput()
      t.searchState.setTerms(searchBarText)
      let queryString = this.searchState.parameter()
      this.displayLoader('text')
      query('text?page=1&' + encodeURI(queryString), this.updateTextData)
    },
    displayLoader: function (type) {
      let loader = document.getElementById(`${type}-loader`)
      loader.style.display = 'initial'
    },
    hideLoader: function (type) {
      let loader = document.getElementById(`${type}-loader`)
      loader.style.display = 'none'
    }
  },
  mounted: function () {
    this.searchState = new SearchState(this.infoLineData)
    this.viewCoordinator = new ViewCoordinator(this, this.searchState)
  },
  data: function () {
    return {
      items: [],
      terms: [],
      viewCoordinator: undefined,
      infoLineData: {
        tags: []
      }
    }
  }
}
</script>

<style>
  body {
    font-family: 'Avenir', Helvetica, Arial, sans-serif;
    font-size: 20px;
  }

  .app {
    box-sizing: border-box;
  }

  .header {
    display: flex;
  }

  ul {
    width: 100%;
    list-style-type: none;
    padding: 0;
    margin: 0;
    border: 4px solid #4575b4;
  }

  li {
    border: 4px solid white;
    margin-top: -4px; /* Prevent double borders */
    background-color: white;
    padding: 12px;
    text-decoration: none;
    font-size: 18px;
    color: black;
    display: block;
  }

  li:hover {
    cursor: default;
  }

  .grid {
    display: flex;
  }

  .grid45 {
    position: relative;
    flex-basis: 45%;
  }

  .grid10 {
    position: relative;
    flex-basis: 10%;
  }

  #text-loader, #data-loader {
    display: none;
    position: absolute;
    top: 50%;
    left: 50%;
    border: 16px solid #f3f3f3;
    border-radius: 50%;
    border-top: 16px solid #666666;
    width: 60px;
    height: 60px;
    animation: spin 2s linear infinite;
  }
  @keyframes spin {
    0% { transform: rotate(0deg); }
    100% { transform: rotate(360deg); }
  }
</style>
