'use strict'

export let search = (function () {
  const state = {
    terms: [],
    inclusions: [],
    exclusions: []
  }
  // Constants for text search
  const primary = 'primary'
  const inclusions = 'inclusions'
  const exclusions = 'exclusions'

  return {
    text: function (searchText) {
      state.terms = searchText.split(' ').filter(term => term.length > 0)
    },
    include: function (term) { state.inclusions.push(term) },
    exclude: function (term) { state.exclusions.push(term) },
    clear: function () {
      state.terms = []
      state.inclusions = []
      state.exclusions = []
    },
    query: function () {
      let query = `${primary}=${state.terms.join(',')}`
      if (state.inclusions.length > 0) {
        query += `&${inclusions}=${state.inclusions.join(',')}`
      }
      if (state.exclusions.length > 0) {
        query += `&${exclusions}=${state.exclusions.join(',')}`
      }
      return query
    },
    update: function (facetsState) {
      console.log(facetsState)
    }
  }
})()
