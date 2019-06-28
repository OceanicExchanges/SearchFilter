'use strict'

import * as d3 from 'd3'

/**
 * This class represents a view. A view is a d3 visualization.
 */
export class View {
  /**
   * The constructor
   * creates and SVG for a given container ID element and sets the width and
   * height.
   */
  constructor (containerID, viewCoordinator, height, width) {
    if (containerID.charAt(0) !== '#') {
      containerID = '#' + containerID
    }
    let t = this
    t.containerID = containerID
    t.dispatch = viewCoordinator.dispatch
    t.viewCoordinator = viewCoordinator
    t.margin = 40
    t.width = width || 600
    t.height = height || 600
    t.initSVG(containerID)
    t.transition = 0
    t.defaultGray = '#666666'
  }

  initSVG (containerID) {
    // Clear old DOM elements
    d3.select(containerID).selectAll('*').remove()
    // Add SVG element to the container
    let t = this
    t.svgFull = d3.select(t.containerID)
      .append('svg')
      .attr('width', t.width + 2 * t.margin)
      .attr('height', t.height + 2 * t.margin)
    t.svg = t.svgFull
      .append('g')
      .attr('transform', 'translate(' + t.margin + ' ' + t.margin + ')')
  }

  setColorScale (color) {
    let t = this
    t.color = color
  }

  setKeyFunction (keyFunction) {
    let t = this
    t.keyFunction = keyFunction
  }

  addTitle (titleString) {
    let t = this
    t.svg.append('text')
      .attr('x', (t.width / 2))
      .attr('y', 0 - (t.margin / 2))
      .attr('text-anchor', 'middle')
      .style('font-size', '12px')
      .text(titleString)
  }
}
