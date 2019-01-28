'use strict'

import * as d3 from 'd3'

import {View} from './view.js'

export class TopicView extends View {
  constructor (containerID, dispatch, viewCoordinator, height) {
    super(containerID, dispatch, viewCoordinator, height)
    this.initTooltip()
  }

  initTooltip () {
    let t = this
    t.div = d3.select('#tooltip')
      .style('opacity', 0.0)
  }

  receiveTopicMouseoverEvent (topic) {
    let t = this
    t.svg.selectAll(t.dotMainClass)
      .transition()
      .duration(t.transition)
      .attr('opacity', function (d, i) {
        return t.keyFunction(d, i) !== topic ? 0.2 : 1.0
      })
  }

  receiveTopicMouseoutEvent () {
    let t = this
    t.svg.selectAll(t.dotMainClass).transition()
      .duration(t.transition)
      .attr('opacity', 1.0)
  }

  setTopicMouseoverEvent () {
    let t = this
    t.svg.selectAll(t.dotMainClass).attr('opacity', 1.0)
      .on('mouseover', function (d, i) {
        let key = t.keyFunction(d, i)
        let topicHTML = t.getTopicHTML(key)
        t.div.transition()
          .duration(t.transition)
          .style('opacity', 0.95)
        let x = d3.event.pageX + 8
        let y = d3.event.pageY + 8
        if (window.innerWidth < x + 400) {
          x = x - (x + 400 - window.innerWidth)
        }
        t.div.html(topicHTML)
          .style('left', (x + 8) + 'px')
          .style('top', (y + 8) + 'px')
        t.dispatch.call('topicMouseover', t, key)
      })
  }

  setTopicMouseoutEvent () {
    let t = this
    t.svg.selectAll(t.dotMainClass).attr('opacity', 1.0)
      .on('mouseout', function (d, i) {
        let key = t.keyFunction(d, i)
        t.div.transition()
          .duration(150)
          .style('opacity', 0)
        t.dispatch.call('topicMouseout', t, key)
      })
  }

  setTopicMousemoveEvent () {
    let t = this
    t.svg.selectAll(t.dotMainClass)
      .on('mousemove', function () {
        if (window.innerWidth > d3.event.pageX + 400) {
          t.div.style('left', (d3.event.pageX + 8) + 'px')
            .style('top', (d3.event.pageY + 8) + 'px')
        }
      })
  }

  setTopicClickEvent () {
    let t = this
    t.svg.selectAll(t.dotMainClass)
      .on('click', function (d, i) {
        let topic
        if (d.hasOwnProperty('key')) {
          topic = d.key
        } else {
          topic = t.keyFunction(d, i)
        }
        t.dispatch.call('topicTerms', t, topic)
      })
  }

  getTopicHTML (key) {
    let t = this
    let topicText = t.viewCoordinator.getFullTopicText(key)
    return '<p>' + topicText + '</p>'
  }
}
