'use strict'

import * as d3 from 'd3'
import * as cf from 'crossfilter2'

import {TextLength} from './textlength.js'
import {DocumentCount} from './documentcount.js'
import {WorldMap} from './worldmap'
import {LanguageCount} from './languagecount.js'

const NUMBER_LENGTH_BINS = 10

export class ViewCoordinator {
  constructor (vue, searchState) {
    let t = this
    t.vue = vue
    t.searchState = searchState
  }

  /**
   * Pass visualization data to the view coordinator.
   *
   * @param data is the entire object
   * @param data.basicInformation contains basic information about all the
   *   documents
   * @param data.documents contains an array with information of all documents
   */
  setDocumentData (data) {
    let t = this
    t.basicInformation = data.basicInformation
    t.documents = data.documents
    let parseDate = d3.timeParse('%Y-%m-%d')
    t.documents.forEach(function (d) {
      d.date = parseDate(d.date)
      d.id = +d.id
    })
    // Event handling
    t.dispatch = d3.dispatch('dateStart', 'dateEnd', 'dateClear',
      'lengthStart', 'lengthEnd', 'lengthClear',
      'mapStart', 'mapEnd', 'languageSelection',
      'languageClear')
    t.cf = cf(t.documents)
    t.createDimensionsGroups()
    t.createViews()
    t.prepareVisualization()
    t.updateVisualization()
    t.setupDispatch()
  }

  createDimensionsGroups () {
    let t = this
    // Date dimension
    t.dateDimension = t.cf.dimension(function (d) {
      return d.date
    })
    let dateGroup = function (d) {
      return new Date('' + d.getFullYear())
    }
    t.dateCountGroup = t.dateDimension.group(dateGroup)
    // Length dimension
    t.lengthDimension = t.cf.dimension(function (d) {
      return d.textLength
    })
    t.textLengthMin = t.lengthDimension.bottom(1)[0].textLength
    t.textLengthRange = t.lengthDimension.top(1)[0].textLength - t.textLengthMin
    let lengthBins = function (d) {
      return Math.floor(((d - t.textLengthMin) / t.textLengthRange) *
        NUMBER_LENGTH_BINS)
    }
    t.lengthDimensionGroup = t.lengthDimension.group(lengthBins).reduceCount()
    // Latitude and longitude dimensions
    t.longitudeDimension = t.cf.dimension(function (d) {
      return d.longitude
    })
    t.latitudeDimension = t.cf.dimension(function (d) {
      return d.latitude
    })
    t.locationDimension = t.cf.dimension(function (d) {
      return d.id
    })
    t.languageDimension = t.cf.dimension(function (d) {
      return d.language
    }, true)
    t.languageGroup = t.languageDimension.group()
  }

  /**
   * Count the number of text lengths in each group and return them. The
   * grouping maps each element to an index in [0, NUMBER_BINS]. This range
   * needs to be scaled to the text lengths, so an additional field
   * 'scaledKey' is introduced.
   */
  textLengthData () {
    let t = this
    let bins = t.lengthDimensionGroup.top(Infinity)
    for (let i in bins) {
      let bin = bins[i]
      bin.scaledKey = t.textLengthMin + bin.key / NUMBER_LENGTH_BINS *
        t.textLengthRange
    }
    return bins
  }

  prepareVisualization () {
    let t = this
    t.textLength.prepare()
    t.documentCount.prepare()
    t.worldMap.prepare()
    t.languageCount.prepare()
  }

  createViews () {
    let t = this
    t.documentCount = new DocumentCount('documentCount', this, 90)
    t.textLength = new TextLength('textLength', this, 90)
    t.worldMap = new WorldMap('geo-map', this, 190)
    t.languageCount = new LanguageCount('language-count', this, 90)
  }

  setupDispatch () {
    let t = this
    t.dispatch.on('dateStart', function () {
      t.searchState.clearDateRange()
      t.dateDimension.filterAll()
    })
    t.dispatch.on('dateEnd', function (range) {
      t.searchState.setDateRange(range)
      t.dateDimension.filterRange(range)
      t.updateVisualization()
    })
    t.dispatch.on('dateClear', function () {
      t.searchState.clearDateRange()
      t.dateDimension.filterAll()
      t.updateVisualization()
    })
    t.dispatch.on('lengthStart', function () {
      t.searchState.clearLengthRange()
      t.lengthDimension.filterAll()
    })
    t.dispatch.on('lengthEnd', function (range) {
      t.searchState.setLengthRange(range)
      t.lengthDimension.filterRange(range)
      t.updateVisualization()
    })
    t.dispatch.on('lengthClear', function () {
      t.searchState.clearLengthRange()
      t.lengthDimension.filterAll()
      t.updateVisualization()
    })
    t.dispatch.on('mapEnd', function (ranges) {
      t.latitudeDimension.filterRange(ranges.latitudeRange)
      t.longitudeDimension.filterRange(ranges.longitudeRange)
      t.searchState.setLatitudeRange(ranges.latitudeRange)
      t.searchState.setLongitudeRange(ranges.longitudeRange)
      t.updateVisualization()
    })
    t.dispatch.on('mapStart', function () {
      t.longitudeDimension.filterAll()
      t.latitudeDimension.filterAll()
      t.searchState.clearLatitudeRange()
      t.searchState.clearLongitudeRange()
      t.updateVisualization()
    })
    t.dispatch.on('languageSelection', function (language) {
      t.languageDimension.filterExact(language)
      t.updateVisualization()
    })
    t.dispatch.on('languageClear', function () {
      t.languageDimension.filterAll()
      t.updateVisualization()
    })
  }

  updateVisualization () {
    let t = this
    t.documentCount.update(t.dateCountGroup.all())
    t.textLength.update(t.textLengthData())
    t.worldMap.update(t.locationDimension.top(Infinity))
    t.languageCount.update(t.languageGroup.reduceCount().top(Infinity))
    t.vue.updateSelected(t.locationDimension.top(Infinity).length)
    t.vue.filterEvent()
  }

  clear () {
    let t = this
    if (t.textLength !== undefined) {
      t.textLength.update([])
    }
    if (t.documentCount !== undefined) {
      t.documentCount.update([])
    }
    if (t.documentCount !== undefined) {
      t.worldMap.update([])
    }
    if (t.languageCount !== undefined) {
      t.languageCount.update([])
    }
  }
}
