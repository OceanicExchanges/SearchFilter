'use strict'

/**
 * The search state contains all information for performing searches. This
 * class provides functions to update these information.
 */
export class SearchState {
  constructor (infoLineData) {
    this.infoLineData = infoLineData
    this.lastQueryString = ''
  }

  static selectionAddEvent (id, term, set) {
    if (!(id in set)) {
      set[id] = [term]
    } else {
      if (!(term in set[id])) {
        set[id].push(term)
      }
    }
  }

  static selectionRemoveEvent (id, term, set) {
    let index = set[id].indexOf(term)
    set[id].splice(index, 1)
  }

  static termsArray (terms) {
    if (typeof terms === 'string') {
      return terms.split(' ')
    } else if (Array.isArray(terms)) {
      return terms
    }
  }

  /**
   * Clears the state, such that an empty search would be performed.
   */
  clear () {
    this.terms = []
    this.addTerms = []
    this.dateRange = undefined
    this.lengthRange = undefined
    this.longitudeRange = []
    this.latitudeRange = []
    this.selections = {}
    this.exclusions = {}
  }

  selectionEvent (selection) {
    let t = this
    switch (selection.type) {
      case 'addInclude':
        SearchState.selectionAddEvent(selection.id, selection.term, t.selections)
        break
      case 'removeInclude':
        SearchState.selectionRemoveEvent(selection.id, selection.term, t.selections)
        break
      case 'addExclude':
        SearchState.selectionAddEvent(selection.id, selection.term, t.exclusions)
        break
      case 'removeExclude':
        SearchState.selectionRemoveEvent(selection.id, selection.term, t.exclusions)
        break
      case 'clear':
        t.selectionClearEvent(selection.id)
        break
    }
  }

  selectionClearEvent (id) {
    let t = this
    t.selections[id] = []
    t.exclusions[id] = []
  }

  addTerms (terms) {
    this.addTerms.push(SearchState.termsArray(terms))
  }

  setTerms (terms) {
    this.terms = SearchState.termsArray(terms)
  }

  /**
   * Set a date range, such that only documents in that range will be searched
   * for.
   */
  setDateRange (dateRange) {
    this.dateRange = dateRange
  }

  clearDateRange () {
    this.dateRange = undefined
  }

  /**
   * Set a text length range, such that only documents in that range will be
   * searched for.
   */
  setLengthRange (lengthRange) {
    this.lengthRange = lengthRange
  }

  clearLengthRange () {
    this.lengthRange = undefined
  }

  setLongitudeRange (longitudeRange) {
    this.longitudeRange = longitudeRange
  }

  clearLongitudeRange () {
    this.longitudeRange = []
  }

  setLatitudeRange (latitudeRange) {
    this.latitudeRange = latitudeRange
  }

  clearLatitudeRange () {
    this.latitudeRange = []
  }

  /**
   * Return a range that can be used for searching.
   */
  getDateRange () {
    let t = this
    let dateRange = []
    if (t.dateRange !== undefined) {
      dateRange.push(t.dateRange[0].toISOString().slice(0, 10))
      dateRange.push(t.dateRange[1].toISOString().slice(0, 10))
    }
    return dateRange
  }

  /**
   * Return a range that can be used for searching.
   */
  getLengthRange () {
    let t = this
    let lengthRange = []
    if (t.lengthRange !== undefined) {
      lengthRange.push(Math.floor(t.lengthRange[0]))
      lengthRange.push(Math.floor(t.lengthRange[1]))
    }
    return lengthRange
  }

  /**
   * Return a string that contains the URL search parameters.
   * @returns {string} the search URL parameters
   */
  parameter () {
    let parameters = {
      primary: this.terms.join(','),
      selections: [].concat.apply([], Object.values(this.selections)).join(','),
      exclusions: [].concat.apply([], Object.values(this.exclusions)).join(','),
      time: this.getDateRange().join(','),
      length: this.getLengthRange().join(','),
      longitude: this.longitudeRange.join(','),
      latitude: this.latitudeRange.join(',')
    }
    parameters = Object.keys(parameters).filter(function (key) {
      return parameters[key].length > 0
    }).map(function (key) {
      return key + '=' + parameters[key]
    })
    this.lastQueryString = parameters.join('&')
    return this.lastQueryString
  }
}
