'use strict'

import {View} from './view.js'
import * as d3 from 'd3'

export class TextLength extends View {
  prepare () {
    let t = this
    t.x = d3.scaleLinear()
      .range([0, t.width])
    t.xAxis = d3.axisBottom().scale(t.x)
    t.y = d3.scaleLog()
      .range([t.height, 0])
    t.yAxis = d3.axisLeft().scale(t.y)
    t.mainShape = 'rect'
    t.mainClass = 'bar'
    t.dotMainClass = '.bar'

    // Define brushes
    function brushStart () {
      t.dispatch.call('lengthStart')
    }

    function brushEnd () {
      if (d3.event.selection !== null) {
        let selectionRange = d3.event.selection
        let selectionDomain = selectionRange.map(t.x.invert)
        t.dispatch.call('lengthEnd', t, selectionDomain)
      } else {
        t.dispatch.call('lengthClear')
      }
    }

    t.brush = d3.brushX()
    t.brush.on('start', brushStart)
    t.brush.on('end', brushEnd)
    t.svg.append('g')
      .attr('class', 'brush')
      .call(t.brush)
    // Add axes
    t.svg.append('g').attr('class', 'x axis')
    t.svg.append('g').attr('class', 'y axis')
    t.addTitle('Number of Documents for different Doument Lengths')
  }

  update (data) {
    let t = this
    t.x.domain(d3.extent(data, function (d) {
      return d.scaledKey
    }))
    t.y.domain(d3.extent(data, function (d) {
      return d.value + 1
    }))
    let updateSelection = t.svg.selectAll(t.dotMainClass)
      .data(data, function (d) {
        return d.key
      })
    updateSelection.exit().remove()
    updateSelection.transition().duration(t.transition)
      .attr('y', function (d) {
        return t.y(d.value + 1)
      })
      .attr('height', function (d) {
        return t.height - t.y(d.value + 1)
      })
    updateSelection.enter()
      .append(t.mainShape)
      .attr('class', t.mainClass)
      .attr('fill', '#666666')
      .attr('x', function (d) {
        return t.x(d.scaledKey)
      })
      .attr('y', function (d) {
        return t.y(d.value + 1)
      })
      .attr('width', t.width / data.length)
      .attr('height', function (d) {
        return t.height - t.y(d.value + 1)
      })
    t.svg.select('.x.axis').transition()
      .duration(t.transition)
      .attr('transform', 'translate(0,' + t.height + ')')
      .call(t.xAxis)
      .selectAll('text')
      .style('text-anchor', 'end')
      .attr('transform', 'rotate(-45)')
    t.svg.select('.y.axis').transition()
      .duration(t.transition)
      .call(t.yAxis.ticks(5, '.3s'))
  }
}
