'use strict'

import * as d3 from 'd3'
import * as crossfilter from 'crossfilter2'

import {TextLength} from './textlength.js'
import {DocumentCount} from './documentcount.js'
import {WorldMap} from './worldmap'
import {LanguageCount} from './languagecount.js'

const NUMBER_BINS = 10

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
      if (!('location' in d)) {
        d.location = [181.0, 91.0]
      }
    })
    t.setTopicData()
    // Event handling
    t.dispatch = d3.dispatch('dateStart', 'dateEnd', 'dateClear',
      'lengthStart', 'lengthEnd', 'lengthClear',
      'mapStart', 'mapEnd', 'languageSelection',
      'languageClear')
    t.crossFilter = crossfilter(t.documents)
    t.createDimensions()
    t.createGroups()
    t.createViews()
    t.setupColors()
    t.prepareVisualization()
    t.updateVisualization()
    t.setupDispatch()
    t.setupLinkDispatch()
  }

  createDimensions () {
    let t = this
    t.dateDimension = t.crossFilter.dimension(function (d) {
      return d.date
    })
    t.dateDimensionCount = t.crossFilter.dimension(function (d) {
      return d.date
    })
    t.lengthDimension = t.crossFilter.dimension(function (d) {
      return d.textLength
    })
    t.longitudeDimension = t.crossFilter.dimension(function (d) {
      return d.location[0]
    })
    t.latitudeDimension = t.crossFilter.dimension(function (d) {
      return d.location[1]
    })
    t.languageDimension = t.crossFilter.dimension(function (d) {
      return d.language[0]
    })
  }

  createGroups () {
    let t = this
    // Define group and reduction functions
    let dateGroup = function (d) {
      return new Date('' + d.getFullYear())
    }
    t.textLengthMin = t.lengthDimension.bottom(1)[0].textLength
    t.textLengthRange = t.lengthDimension.top(1)[0].textLength - t.textLengthMin
    let lengthBins = function (d) {
      return Math.floor(((d - t.textLengthMin) / t.textLengthRange) *
        NUMBER_BINS)
    }
    t.groupAll = t.crossFilter.groupAll()
    // Length of texts in bins
    t.lengthDimensionGroup = t.lengthDimension.group(lengthBins).reduceCount()
    // Number of documents in groups per year
    t.documentCountGroup = t.dateDimension.group(dateGroup)
    t.documentCountGroup = t.documentCountGroup.reduceCount()
    t.dateCountGroup = t.dateDimensionCount.group(dateGroup)
    t.languageGroup = t.languageDimension.group()
  }

  setTopicData () {
    let t = this
    t.topicTerms = t.basicInformation.topicTerms
    t.fullTopicText = {}
    t.shortTopicText = {}
    for (let i = 0; i < t.topicTerms.length; ++i) {
      let topicTerm = t.topicTerms[i]
      t.fullTopicText[topicTerm.topic] = topicTerm.topicTerms.join(' ')
      t.shortTopicText[topicTerm.topic] = topicTerm.topicTerms
        .slice(0, 3).join(' ')
    }
  }

  getFullTopicText (topic) {
    return this.fullTopicText[topic]
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
      bin.scaledKey = t.textLengthMin + bin.key / NUMBER_BINS *
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
    t.documentCount = new DocumentCount('documentCount', this, 100)
    t.textLength = new TextLength('textLength', this, 100)
    t.worldMap = new WorldMap('geo-map', this, 100)
    t.languageCount = new LanguageCount('language-count', this, 100)
  }

  setupColors () {
    let t = this
    t.topicColor = d3.scaleOrdinal(d3.schemeCategory10)
  }

  setupDispatch () {
    let t = this
    t.dispatch.on('dateStart', function () {
      t.searchState.clearDateRange()
      t.dateDimensionCount.filterAll()
    })
    t.dispatch.on('dateEnd', function (range) {
      t.searchState.setDateRange(range)
      t.dateDimensionCount.filterRange(range)
      t.updateVisualization()
    })
    t.dispatch.on('dateClear', function () {
      t.searchState.clearDateRange()
      t.dateDimensionCount.filterAll()
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
    t.dispatch.on('topicTerms', function (topic) {
      t.vue.topicMoreLikeThisEvent(t.getFullTopicText(topic))
    })
    t.dispatch.on('mapEnd', function (ranges) {
      t.longitudeDimension.filterRange(ranges.longitudeRange)
      t.latitudeDimension.filterRange(ranges.latitudeRange)
      t.searchState.setLongitudeRange(ranges.longitudeRange)
      t.searchState.setLatitudeRange(ranges.latitudeRange)
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
    let textLengthData = t.textLengthData()
    t.textLength.update(textLengthData)
    let reducedCount = t.topicDistributionCount()
    t.documentCount.update(reducedCount)
    // TODO Location is split into two dimensions, which makes it hard to filter
    // on the location as a whole.
    t.worldMap.update(t.latitudeDimension.top(Infinity))
    t.languageCount.update(t.languageGroup.reduceCount().top(Infinity))
    t.vue.updateSelected(t.countFiltered())
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
