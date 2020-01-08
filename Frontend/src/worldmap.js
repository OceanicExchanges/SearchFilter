'use strict'

import {View} from './view.js'
import * as d3 from 'd3'

const URL = 'static/world-110m.geojson'

export class WorldMap extends View {
  prepare () {
    let t = this
    t.projection = d3.geoMercator().scale(t.width / 2 / Math.PI)
      .translate([t.width / 2, t.height])
    let path = d3.geoPath().projection(t.projection)
    t.keyFunction = function (d) {
      return d.id
    }

    function brushStart () {
      t.dispatch.call('mapStart')
    }

    function brushEnd () {
      const selection = d3.event.selection
      if (selection !== null) {
        let upperLeft = t.projection.invert(selection[0])
        let lowerRight = t.projection.invert(selection[1])
        t.dispatch.call('mapEnd', t, {
          longitudeRange: [upperLeft[0], lowerRight[0]],
          latitudeRange: [lowerRight[1], upperLeft[1]]
        })
      } else {
        t.dispatch.call('mapStart')
      }
    }

    t.prepared = false
    t.data = undefined
    t.brush = d3.brush()
      .on('start', brushStart)
      .on('end', brushEnd)
    d3.json(URL).then(function (geoJSON) {
      t.svgFull.append('path').attr('d', path(geoJSON)).attr('fill', t.defaultGray)
      t.svgFull.append('g')
        .attr('class', 'brush')
        .call(t.brush)
      t.prepared = true
      if (t.data !== undefined) {
        t.update(t.data)
      }
    })
  }

  update (data) {
    let t = this
    if (t.prepared) {
      let updateSelection = t.svgFull.selectAll('circle')
        .data(data.filter(function (d) {
          return 'latitude' in d && 'longitude' in d
        }), t.keyFunction)
      updateSelection.exit().remove()
      updateSelection.enter()
        .append('circle')
        .attr('cx', function (d) {
          return t.projection([d.longitude, d.latitude])[0]
        })
        .attr('cy', function (d) {
          return t.projection([d.longitude, d.latitude])[1]
        })
        .attr('r', function (d) {
          return d.hasOwnProperty('radius') ? d.radius : 2
        })
        .attr('fill', '#d73027')
    } else {
      t.data = data
    }
  }
}
