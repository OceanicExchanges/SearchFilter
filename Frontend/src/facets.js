'use strict'

import * as d3 from 'd3'
import crossfilter from 'crossfilter2'

/**
 * Generate a facets object.
 * @param selector in which facets adds the SVG element
 * @param width of the SVG element
 * @param height of the SVG element
 * @param margin from the border of the SVG element to the content
 * @param primaryColor for the rectangles and bars (use D3 color convention)
 * @returns {{update: (function(*=): *), toggle: (function():
 *   toggleConnections), initialize: (function(*=): initialize), draw:
 *   (function(): draw)}}
 */
export const facets = function (selector, width, height, margin, primaryColor) {
  if (margin === undefined) {
    margin = 20
  }
  if (primaryColor === undefined) {
    primaryColor = 'black'
  }
  const canvas = d3.select(selector)
    .append('svg')
    .attr('width', width + 2 * margin)
    .attr('height', height + 2 * margin)
    .attr('id', 'facets-svg')
    .append('g')
    .attr('transform', `translate(${margin},${margin})`)

  const data = {
    // List of functions that facets calls on update
    updateFunctions: []
  }

  const parameters = {
    opacity: 1.0,
    transitionTime: 1300,
    paddingFactor: 0.01
  }

  /**
   * put new data into facet view
   * @param d
   */
  const initialize = function (d) {
    data.extended = false
    data.raw = d
    data.types = d.types
    data.dimensions = d.dimensions
    data.dimensionHeight = Math.floor(height / data.dimensions.length - margin)
    data.blockHeight = Math.floor(data.dimensionHeight)
    data.blockWidth = Math.floor(width - 2 * margin)
    data.crossfilter = crossfilter(d)
    data.filterCategories = new Array(data.dimensions.length).fill(null).map(() => new Set())
    data.filterNotCategories = new Array(data.dimensions.length).fill(null).map(() => new Set())
    data.filterRanges = new Array(data.dimensions.length).fill(null)
    data.brushes = new Array(data.dimensions.length).fill(undefined)
    data.brushElements = new Array(data.dimensions.length).fill(undefined)
    data.axes = new Array(data.dimensions.length).fill(undefined)
    data.axisElements = new Array(data.dimensions.length).fill(undefined)
    data.crossfilterDimensions = []
    data.crossfilterGroups = []
    data.dimensions.forEach(function (dimension) {
      let crossfilterDimension = data.crossfilter.dimension(d => Array.from(d[dimension]), true)
      data.crossfilterDimensions.push(crossfilterDimension)
      switch (data.types[dimension]) {
        case 'string':
          data.crossfilterGroups.push(crossfilterDimension.group())
          break
        case 'number':
          data.crossfilterGroups.push(crossfilterDimension.group(d => d))
          break
        case 'date':
          data.crossfilterGroups.push(crossfilterDimension.group(d => new Date(`${d.getFullYear()}`)))
          break
        default:
          console.log(`Unsupported data type ${data.types[dimension]}`)
          break
      }
    })
    data.scales = data.dimensions.map(function (d) {
      let horizontalScale
      switch (data.types[d]) {
        case 'date':
          horizontalScale = d3.scaleTime().range([0, data.blockWidth])
          break
        case 'string':
        case 'number':
          horizontalScale = d3.scaleLinear().range([0, data.blockWidth])
          break
        default:
          console.log(`Unsupported data type ${data.types[d]}`)
          break
      }
      return {
        'horizontal': horizontalScale,
        'vertical': d3.scaleLinear().range([0, data.blockHeight])
      }
    })
    // Draw the labels for each dimension
    data.labelLength = maximumLabelLength()
    canvas.selectAll('*').remove()
    canvas.selectAll('.dimension-label')
      .data(data.dimensions)
      .enter()
      .append('g')
      .attr('transform', (d, i) => `translate(0,${dimensionTranslateY(i, data.dimensionHeight, margin)})`)
      .attr('class', 'dimension-label')
      .append('text')
      .style('text-anchor', 'left')
      .text(d => d)
    // Prepare dimensions
    canvas.selectAll('.dimension')
      .data(data.dimensions)
      .enter()
      .append('g')
      .attr('class', d => `dimension dimension-type-${data.types[d]}`)
      .append('g')
      .attr('class', 'elements')
    canvas.selectAll('.dimension')
      .each(function (d, i) {
        switch (data.types[d]) {
          case 'number':
          case 'date':
            // Brushes
            data.brushes[i] = d3.brushX().extent([[0, 0], [data.blockWidth, data.blockHeight]])
            data.brushElements[i] = d3.select(this)
              .append('g')
              .attr('class', 'brush')
              .call(data.brushes[i].on('end', brushDraw))
            // Axes
            data.axes[i] = d3.axisBottom(data.scales[i].horizontal)
            // TODO verticalAxis = d3.axisLeft(data.scales[i].vertical)
            const axisElement = d3.select(this)
              .append('g')
              .attr('transform', `translate(0,${data.blockHeight})`)
              .call(data.axes[i])
            data.axisElements[i] = axisElement
            break
          case 'string':
          default:
            break
        }
      })
    canvas.append('g')
      .attr('id', 'connections')
      .attr('transform', `translate(${data.labelLength},0)`)
      .selectAll('g')
      .data(data.dimensions)
      .enter()
      .append('g')
      .attr('id', (d, i) => `connection-${i}`)
    return this
  }

  const labelLength = function (labels, c) {
    const dimensionLengths = []
    if (c === undefined) {
      c = '.text-label'
    }
    canvas.selectAll(c)
      .data(labels)
      .enter()
      .append('text')
      .text(d => d)
      .each(function () {
        const l = this.getComputedTextLength()
        dimensionLengths.push(l)
        this.remove()
      })
    return dimensionLengths
  }

  /**
   * @returns {number} The maximum length of the labels in pixels
   */
  const maximumLabelLength = function () {
    const dimensionLengths = labelLength(data.dimensions)
    return Math.max(...dimensionLengths)
  }

  const rectangleClick = function (event, rectangleElement) {
    if (event.type === 'contextmenu') {
      event.preventDefault()
    }
    // f returns true iff element has been left-clicked already
    const f = d => data.filterCategories[rectangleElement.dimensionIndex].has(d)
    // g returns true iff element has been right-clicked already
    const g = d => data.filterNotCategories[rectangleElement.dimensionIndex].has(d)
    const not = f => d => !f(d)
    // Lookup table for the possible cases. The first word signifies three
    // states (free, left clicked, right clicked) of the rectangle. The second
    // word signifies the current action.
    const cases = {
      freeLeft: function (element) {
        data.filterCategories[rectangleElement.dimensionIndex].add(rectangleElement.key)
        d3.select(element).style('fill', 'blue')
        data.crossfilterDimensions[rectangleElement.dimensionIndex].filterFunction(f)
      },
      freeRight: function (element) {
        data.filterNotCategories[rectangleElement.dimensionIndex].add(rectangleElement.key)
        d3.select(element).style('fill', 'red')
        data.crossfilterDimensions[rectangleElement.dimensionIndex].filterFunction(not(g))
      },
      leftLeft: function (element) {
        data.filterCategories[rectangleElement.dimensionIndex].delete(rectangleElement.key)
        d3.select(element).style('fill', primaryColor)
        data.crossfilterDimensions[rectangleElement.dimensionIndex].filterAll()
      },
      leftRight: function (element) {
        data.filterCategories[rectangleElement.dimensionIndex].delete(rectangleElement.key)
        d3.select(element).style('fill', primaryColor)
        data.crossfilterDimensions[rectangleElement.dimensionIndex].filterAll()
      },
      rightLeft: function (element) {
        data.filterCategories[rectangleElement.dimensionIndex].delete(rectangleElement.key)
        d3.select(element).style('fill', primaryColor)
        data.crossfilterDimensions[rectangleElement.dimensionIndex].filterAll()
      },
      rightRight: function (element) {
        data.filterNotCategories[rectangleElement.dimensionIndex].delete(rectangleElement.key)
        d3.select(element).style('fill', primaryColor)
        data.crossfilterDimensions[rectangleElement.dimensionIndex].filterAll()
      }
    }
    const first = function () {
      if (data.filterCategories[rectangleElement.dimensionIndex].has(rectangleElement.key)) {
        return 'left'
      } else if (data.filterNotCategories[rectangleElement.dimensionIndex].has(rectangleElement.key)) {
        return 'right'
      } else {
        return 'free'
      }
    }
    const second = function () {
      if (event.type === 'click') {
        return 'Left'
      } else if (event.type === 'contextmenu') {
        return 'Right'
      } else {
        console.log(`Unsupported data event type ${event.type}`)
        return 'Nope'
      }
    }
    cases[`${first()}${second()}`](this)
    callBackState()
    update()
  }

  const callBackState = function () {
    // Call back with updated dataset
    const state = {
      include: {},
      exclude: {}
    }
    data.dimensions.forEach(function (dimension, i) {
      switch (data.types[i]) {
        case 'string':
          state.include[dimension] = data.filterCategories[i]
          state.exclude[dimension] = data.filterNotCategories[i]
          break
        case 'date':
        case 'number':
          state.include[dimension] = data.filterRanges[i]
          break
        default:
      }
    })
    data.updateFunctions.forEach(function (f) {
      f({
        data: data.crossfilter.allFiltered(),
        state: state
      })
    })
  }

  const brushDraw = function (event, dimensionName) {
    const dimension = data.dimensions.indexOf(dimensionName)
    if (event.selection !== null) {
      const selection = event.selection
      const selectionDomain = selection.map(data.scales[dimension].horizontal.invert)
      data.crossfilterDimensions[dimension].filterRange(selectionDomain)
      data.filterRanges[dimension] = selection
    } else {
      data.crossfilterDimensions[dimension].filter(null)
      data.filterRanges[dimension] = null
    }
    callBackState()
    update()
  }

  /**
   * Draw graphical elements initially
   */
  const draw = function () {
    const dimensionCounts = data.crossfilterGroups.map(d => d.top(Infinity))
    const stacked = stack(dimensionCounts, data, parameters)
    drawRectangles(stacked)
    drawHistograms(stacked)
    drawConnections(stacked, dimensionCounts, data)
    return this
  }

  /**
   * Update graphical elements
   */
  const update = function () {
    const dimensionCounts = data.crossfilterGroups.map(d => d.top(Infinity))
    const stacked = stack(dimensionCounts, data, parameters)
    updateRectangles(stacked)
    updateHistograms(stacked)
    updateConnections(stacked, dimensionCounts)
  }

  /**
   * Initially draw connections
   */
  const drawConnections = function (stacked, dimensionCounts) { updateConnections(stacked, dimensionCounts) }

  const updateConnections = function (stacked, dimensionCounts) {
    if (!data.extended) {
      canvas.select('#connections').selectAll('g line').remove()
      return
    } // else (data.extended === true) => update elements
    if (stacked === undefined) {
      dimensionCounts = data.crossfilterGroups.map(d => d.top(Infinity))
      stacked = stack(dimensionCounts, data, parameters)
    }
  }

  const drawRectangles = function (stacked) { updateRectangles(stacked) }

  const updateRectangles = function (stacked) {
    const dimensionJoin = canvas.selectAll('.dimension-type-string')
      .data(stacked.filter(d => data.types[d.index] === 'string'))
      .attr('transform', d => dimensionTypeStringTranslate(d, data, margin))
    const attributeJoin = dimensionJoin
      .select('.elements')
      .selectAll('.rect-element')
      .data(d => d.filter(d => d.value > 0), d => d.key)
    attributeJoin.exit().remove()
    const labelText = function (d) {
      const label = d.key
      const rectangleWidth = data.scales[d.dimensionIndex].horizontal(d.value)
      const labelWidth = labelLength([label])[0]
      if (rectangleWidth < labelWidth) {
        const proportion = rectangleWidth / labelWidth
        const index = Math.floor(label.length * proportion)
        return label.slice(0, index)
      } else {
        return d.key
      }
    }
    const attributeElementGroup = attributeJoin.enter()
      .append('g')
      .attr('class', 'rect-element')
      .attr('transform', (d, i) => `translate(${rectangleX(d, i, data)},0)`)
    attributeElementGroup
      .append('rect')
      .attr('class', 'attribute-rectangle')
      .classed('attribute-rectangle-selected', rectangleSelected)
      .classed('attribute-rectangle-empty', rectangleEmpty)
      .attr('width', rectangleWidth)
      .attr('height', rectangleHeight(data))
      .attr('opacity', parameters.opacity)
      .attr('fill', primaryColor)
      .on('click', rectangleClick)
      .on('contextmenu', rectangleClick)
    attributeElementGroup
      .append('text')
      .attr('class', 'attribute attribute-text')
      .attr('y', rectangleHeight(data) / 4)
      .style('font-size', `${rectangleHeight(data) / 4}px`)
      .text(labelText)
    attributeElementGroup
      .append('text')
      .attr('class', 'attribute attribute-count')
      .attr('y', data.blockHeight - rectangleHeight(data) / 4)
      .style('font-size', `${rectangleHeight(data) / 4}px`)
      .text(d => d.value)
    attributeJoin.transition()
      .duration(parameters.transitionTime)
      .attr('transform', (d, i) => `translate(${rectangleX(d, i, data)},0)`)
    attributeJoin
      .select('rect')
      .classed('attribute-rectangle-selected', rectangleSelected)
      .classed('attribute-rectangle-empty', rectangleEmpty)
      .transition()
      .duration(parameters.transitionTime)
      .attr('width', rectangleWidth)
    attributeJoin
      .select('.attribute-text')
      .text(labelText)
    attributeJoin
      .select('.attribute-count')
      .text(d => d.value)
  }

  const drawHistograms = function (stacked) { updateHistograms(stacked) }

  const barWidth = 5

  const updateHistograms = function (stacked) {
    const dateElements = canvas.selectAll('.dimension-type-date,.dimension-type-number')
      .data(stacked.filter(d => data.types[d.index] === 'date' || data.types[d.index] === 'number'))
      .attr('transform', d => dimensionTypeDateTranslate(d, data, margin))
    const histogram = dateElements
      .select('.elements')
      .selectAll('rect')
      .data(d => d, d => d.key)
    histogram.exit().remove()
    histogram.enter()
      .append('rect')
      .attr('x', d => histogramX(d, data, barWidth))
      .attr('y', d => histogramY(d, data))
      .attr('height', d => histogramHeight(d, data))
      .attr('width', barWidth)
      .attr('opacity', parameters.opacity)
      .attr('fill', primaryColor)
    histogram.transition()
      .duration(parameters.transitionTime)
      .attr('x', d => histogramX(d, data, barWidth))
      .attr('y', d => histogramY(d, data))
      .attr('height', d => histogramHeight(d, data))
    data.axisElements.forEach(function (d, i) {
      if (d !== undefined) {
        d.transition().duration(parameters.transitionTime).call(data.axes[i].scale(data.scales[i].horizontal))
      }
    })
  }

  const updateBlockHeight = function () {
    data.scales.forEach(function (scale) { scale.vertical.range([0, data.blockHeight]) })
    canvas.selectAll('.dimension')
      .each(function (d, i) {
        switch (data.types[i]) {
          case 'number':
          case 'date':
            data.brushElements[i].attr('transform', `translate(0,${data.dimensionHeight - data.blockHeight})`)
            data.brushElements[i].call(data.brushes[i].extent([[0, 0], [data.blockWidth, data.blockHeight]]))
            break
          case 'string':
          default:
            break
        }
      })
    const rectangles = canvas.selectAll('.dimension-type-string').select('.elements')
      .selectAll('rect')
    rectangles.transition()
      .duration(parameters.transitionTime)
      .attr('height', data.blockHeight)
    const dateElements = canvas.selectAll('.dimension-type-date')
      .transition()
      .duration(parameters.transitionTime)
      .attr('transform', d => dimensionTypeDateTranslate(d, data, margin))
    dateElements.select('.elements')
      .selectAll('rect')
      .transition()
      .duration(parameters.transitionTime)
      .attr('y', d => histogramY(d, data))
      .attr('height', d => histogramHeight(d, data))
  }

  const updateLabels = function () {
    const opacity = data.extended ? 0 : 1
    canvas.selectAll('.attribute-text').style('opacity', opacity)
    canvas.selectAll('.attribute-count').style('opacity', opacity)
  }

  const toggleConnections = function () {
    data.extended = !data.extended
    data.blockHeight = blockHeight(data, data.extended)
    updateBlockHeight()
    updateLabels()
    updateConnections()
    return this
  }

  const blockHeight = function (data, extended) {
    if (extended) {
      return Math.floor(data.dimensionHeight / 8)
    } else {
      return Math.floor(data.dimensionHeight)
    }
  }

  const stack = function (dimensionCounts, data, parameters) {
    for (let i = 0, n = dimensionCounts.length; i < n; ++i) {
      const dimension = dimensionCounts[i]
      dimension.index = i
      switch (data.types[i]) {
        case 'date':
        case 'number':
          dimension.forEach(function (d) { d.dimensionIndex = i })
          data.scales[i].horizontal.domain(d3.extent(dimension, d => d.key))
          data.scales[i].vertical.domain([0, d3.max(dimension, d => d.value)])
          break
        default:
          const padding = parameters.paddingFactor * dimension.map(e => e.value).reduce((a, c) => a + c, 0)
          let endPadding = 0
          let sum = 0
          for (let j = 0, m = dimension.length; j < m; ++j) {
            if (dimension[j].value > 0) {
              dimension[j].begin = sum + endPadding
              sum += dimension[j].value + endPadding
              dimension[j].end = sum
              dimension[j].dimensionIndex = i
              endPadding = padding
            }
          }
          data.scales[i].horizontal.domain([0, sum])
          break
      }
    }
    return dimensionCounts
  }

  const dimensionTypeTranslateX = function (i, data) {
    return data.labelLength
  }

  const dimensionTypeDateTranslateY = function (i, data, margin) {
    return i * (data.dimensionHeight + margin)
  }

  const dimensionTypeDateTranslate = function (d, data, margin) {
    return `translate(${dimensionTypeTranslateX(d.index, data)},${dimensionTypeDateTranslateY(d.index, data, margin) + (data.blockHeight - blockHeight(data, false))})`
  }

  const dimensionTranslateY = function (i, height, margin) {
    return i * (height + margin)
  }

  const dimensionTypeStringTranslateY = function (i, data, margin) {
    return dimensionTranslateY(i, data.dimensionHeight, margin)
  }

  const dimensionTypeStringTranslate = function (d, data, margin) {
    return `translate(${dimensionTypeTranslateX(d.index, data)},${dimensionTypeStringTranslateY(d.index, data, margin)})`
  }

  const histogramX = function (d, data, barWidth) {
    return data.scales[d.dimensionIndex].horizontal(d.key) - barWidth / 2
  }

  const histogramY = function (d, data) {
    return data.dimensionHeight - data.scales[d.dimensionIndex].vertical(d.value)
  }

  const histogramHeight = function (d, data) {
    return data.scales[d.dimensionIndex].vertical(d.value)
  }

  const rectangleX = function (d, i, data) {
    return data.scales[d.dimensionIndex].horizontal(d.begin)
  }

  const rectangleWidth = function (d) {
    return data.scales[d.dimensionIndex].horizontal(d.value)
  }

  const rectangleSelected = function (d) {
    return data.filterCategories[d.dimensionIndex].has(d.key)
  }

  const rectangleEmpty = function (d) {
    return d.key.toLowerCase() === 'empty'
  }

  const rectangleHeight = function (data) {
    return data.blockHeight
  }

  return {
    initialize: initialize,
    draw: draw,
    update: function (f) {
      data.updateFunctions.push(f)
      return this
    },
    toggle: toggleConnections
  }
}
