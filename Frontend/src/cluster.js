'use strict'

import {View} from './view.js'
import * as d3 from 'd3'

export class Cluster extends View {
  prepare () {
    let t = this
    t.defaultGray = '#aaaaaa'
    t.x = d3.scaleLog()
      .range([0, t.width])
    t.xAxis = d3.axisTop()
      .scale(t.x)
    t.y = d3.scaleBand()
      .range([0, t.height])
      .padding(0.2)
    t.yAxis = d3.axisLeft()
      .scale(t.y)
    t.mainShape = 'rect'
    t.mainClass = 'barCluster'
    t.dotMainClass = '.barCluster'
    t.svg.append('g')
      .attr('class', 'x axis')
    t.svg.append('g')
      .attr('class', 'y axis')
    t.addTitle('Reprinting Clusters')
  }

  update (data) {
    let t = this
    t.x.domain([1, d3.max(data, function (d) { return d.value })])
    t.y.domain(data.map(function (d) { return d.key }))
    let updateSelection = t.svg.selectAll(t.dotMainClass)
      .data(data.filter(function (d) { return d.value > 0 }), function (d) { return d.key })
    updateSelection.exit()
      .remove()
    updateSelection.transition()
      .duration(t.transition)
      .attr('width', function (d) { return t.x(d.value) })
      .attr('y', function (d) { return t.y(d.key) })
    updateSelection.enter()
      .append(t.mainShape)
      .attr('class', t.mainClass)
      .attr('y', function (d) { return t.y(d.key) })
      .attr('height', t.y.bandwidth())
      .attr('x', 0)
      .attr('width', function (d) { return t.x(d.value) })
      .attr('fill', t.defaultGray)
      .on('click', function (d) {
        if (d.key === t.viewCoordinator.searchState.cluster) {
          d3.selectAll(t.dotMainClass)
            .attr('fill', t.defaultGray)
          t.dispatch.call('clusterClear')
        } else {
          d3.selectAll(t.dotMainClass)
            .attr('fill', t.defaultGray)
          d3.select(this)
            .attr('fill', 'red')
          t.dispatch.call('clusterSelection', t, d.key)
        }
      })
    let labelSelection = t.svg
      .selectAll('.label')
      .data(data.filter(function (d) { return d.value > 0 }), function (d) { return d.key })
    labelSelection.exit()
      .remove()
    labelSelection.transition()
      .duration(t.transition)
      .attr('width', function (d) { return t.x(d.value) })
      .attr('y', function (d) { return t.y(d.key) + t.y.bandwidth() / 2 + 5 })
    labelSelection.enter()
      .append('text')
      .attr('class', 'label')
      .attr('y', function (d) { return t.y(d.key) + t.y.bandwidth() / 2 + 5 })
      .attr('height', t.y.bandwidth())
      .attr('x', 0)
      .attr('width', function (d) { return t.x(d.value) })
      .text(function (d) { return d.key })
      .style('pointer-events', 'none')
    t.svg.select('.x.axis')
      .transition()
      .duration(t.transition)
      .call(t.xAxis.ticks(3, '.0s'))
    t.svg.select('.y.axis')
      .transition()
      .duration(t.transition)
      .selectAll('text')
      .remove()
  }
}
