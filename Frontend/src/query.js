'use strict'

import {_} from 'utiljs'

/**
 * General HTTP GET query function.
 *
 * relativeUrl: the end part of the URL. Depending on the environment the URL
 * is extended.
 * callback: the callback function that is called on success with the data as
 * argument.
 */
export function query (relativeUrl, callback) {
  let url = relativeUrl
  if (process.env.NODE_ENV === 'development') {
    if (relativeUrl.startsWith('query')) {
      url = 'http://localhost:8080/static/query.json'
    } else if (relativeUrl.startsWith('like')) {
      url = 'http://localhost:8080/static/query.json'
    } else if (relativeUrl.startsWith('static')) {
      url = '../' + relativeUrl
    } else {
      url = 'http://localhost:8080/static/text.json'
    }
  }
  _.httpGetJSON(url, callback)
}

export function download (relativeUrl) {
  if (process.env.NODE_ENV === 'development') {
    window.open('static/export.csv', '_self')
  } else {
    window.open(relativeUrl, '_self')
  }
}
