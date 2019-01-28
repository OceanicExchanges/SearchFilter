<template>
  <div class="app">
    <div class="header">
      <search-bar ref="searchBar" @searchBarEvent="searchBarEvent"/>
      <search-button @filterSearchEvent="filterSearchEvent"/>
    </div>
    <info-line v-bind="infoLineData" @tagEvent="tagEvent"/>
    <div class="grid">
      <div class="grid61">
        <div class="grid">
          <div class="grid50">
            <div id="documentCount"></div>
            <div id="language-count"></div>
          </div>
          <div class="grid50">
            <div id="textLength"></div>
            <div id="geo-map"></div>
          </div>
        </div>
      </div>
      <div class="grid39" v-if="items.length > 0">
        <list v-bind:items="items"
              v-bind:terms="terms"
              @moreLikeThisEvent="moreLikeThisEvent"
              @selectionEvent="selectionEvent"
              @pageEvent="pageEvent"/>
      </div>
    </div>
    <div id="tooltip"></div>
  </div>
</template>

<script>
import SearchBar from './components/SearchBar.vue'
import Button from './components/Button.vue'
import InfoLine from './components/InfoLine.vue'
import List from './components/List.vue'
import {query} from './query.js'
import {ViewCoordinator} from './viewcoordinator.js'
import {SearchState} from './searchstate.js'

export default {
  name: 'App',
  components: {
    'search-bar': SearchBar,
    'search-button': Button,
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
    },
    updateTextData: function (data) {
      this.items = data.documents
    },
    sendQuery: function (queryString) {
      query('query?' + encodeURI(queryString), this.updateDocumentData)
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
    moreLikeThisEvent: function (id) {
      query('like?id=' + id, this.updateDocumentData)
      query('textLike?page=1&id=' + id, this.updateTextData)
    },
    topicMoreLikeThisEvent: function (topicTerms) {
      query('query?primary=' + topicTerms.join(','), this.updateDocumentData)
      query('text?page=1&primary=' + topicTerms.join(','), this.updateTextData)
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
      query('text?page=1&' + encodeURI(queryString), this.updateTextData)
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
  }

  li {
    border: 1px solid #ddd;
    margin-top: -1px; /* Prevent double borders */
    background-color: #f6f6f6;
    padding: 12px;
    text-decoration: none;
    font-size: 18px;
    color: black;
    display: block;
  }

  li:hover {
    background-color: #eee;
    cursor: default;
  }

  .grid {
    display: flex;
  }

  .grid61 {
    flex-basis: 61%;
  }

  .grid39 {
    flex-basis: 39%;
  }

  .grid50 {
    flex-basis: 49%;
  }

  div#tooltip {
    position: absolute;
    text-align: center;
    padding: 2px;
    font: 12px sans-serif;
    width: 100px;
    background: lightgrey;
    border-radius: 2px;
    border: 1px solid black;
    pointer-events: none;
    opacity: 0;
  }
</style>
