'use strict'

import {stopWords} from './stopwords.js'

/**
 * This class allows you to dynamically select words in a text. The basic idea
 * is that a text is divided into words and each word is provided with a class
 * derived from the word itself. Clicking on words applies CSS to the class of
 * the word and invokes callback functions.
 */
export class TextMark {
  constructor (element, text, textSelector) {
    let t = this
    t.element = element
    t.text = text
    t.textSelector = textSelector || 'text'
    t.terms = t.text.split(/(\s+)/)
    t.html = ''
    let stopWordSet = new Set(stopWords)
    for (let i = 0; i < t.terms.length; ++i) {
      let term = t.terms[i]
      let className = TextMark.className(term)
      if (stopWordSet.has(className)) {
        t.html += '<span class="text">' + term + '</span>'
      } else {
        t.html += '<span class="' + t.textSelector + ' ' + className + '">' +
          term + '</span>'
      }
    }
    t.setupLeftClick()
    t.setupRightClick()
    t.setupSelection()
    t.leftSelector = 'leftSelector'
    t.rightSelector = 'rightSelector'
    t.left = {add: undefined, remove: undefined}
    t.right = {add: undefined, remove: undefined}
  }

  /**
   * This function removes punctuation switches all characters to lower case of
   * a term so that it can be used as a class name.
   * @param term
   * @returns {string}
   */
  static className (term) {
    return term.replace(/[.,/#!$%^&*;:{}=\-_`~()]/g, '').toLowerCase()
  }

  static addClassToElement (element, addClass) {
    element.classList.add(addClass)
  }

  static removeClassFromElement (element, removeClass) {
    element.classList.remove(removeClass)
  }

  static toggleClassElement (element, toggleClass) {
    return element.classList.toggle(toggleClass)
  }

  setupLeftClick () {
    let t = this
    let click = function (event) {
      let text = TextMark.className(event.target.innerText)
      t.toggleClass(text, t.leftSelector, t.left)
    }
    t.element.addEventListener('click', click, false)
  }

  setupRightClick () {
    let t = this
    let rightClick = function (event) {
      event.preventDefault()
      let text = TextMark.className(event.target.innerText)
      t.toggleClass(text, t.rightSelector, t.right)
    }
    t.element.addEventListener('contextmenu', rightClick, false)
  }

  setupSelection () {
    let t = this
    t.element.innerHTML = t.html
    let select = function () {
      let selection = window.getSelection()
      let selectionString = selection.toString()
      let selectionTerms = selectionString.split(/(\s+)/)
      for (let i in selectionTerms) {
        let term = selectionTerms[i]
        let className = TextMark.className(term)
        t.addClass(className, t.leftSelector, t.left)
      }
      selection.removeAllRanges()
    }
    t.element.addEventListener('mouseup', select, false)
  }

  /**
   * Set the selector for the left-click or selection.
   */
  setLeftSelector (leftSelector) {
    this.leftSelector = leftSelector
    return this
  }

  /**
   * Set the selector for the right-click.
   */
  setRightSelector (rightSelector) {
    this.rightSelector = rightSelector
    return this
  }

  /**
   * Register a callback function that is invoked when a word has been added
   * with a left-click.
   */
  leftAddCallback (leftAddCallback) {
    this.left.add = leftAddCallback
    return this
  }

  /**
   * Register a callback function that is invoked when a word has been removed
   * with a left-click.
   */
  leftRemoveCallback (leftRemoveCallback) {
    this.left.remove = leftRemoveCallback
    return this
  }

  /**
   * Register a callback function that is invoked when a word has been added
   * with a right-click.
   */
  rightAddCallback (rightAddCallback) {
    this.right.add = rightAddCallback
    return this
  }

  /**
   * Register a callback function that is invoked when a word has been removed
   * with a right-click.
   */
  rightRemoveCallback (rightRemoveCallback) {
    this.right.remove = rightRemoveCallback
    return this
  }

  /**
   * This function adds the class in addClass to all elements that have
   * contain className. This function invokes the corresponding callback once.
   * @param className
   * @param addClass
   * @param callbacks
   */
  addClass (className, addClass, callbacks) {
    let t = this
    let elements = t.element.getElementsByClassName(className)
    for (let i = 0; i < elements.length; ++i) {
      TextMark.addClassToElement(elements[i], addClass)
    }
    if (callbacks !== undefined) {
      if (elements.length > 0 && callbacks.add !== undefined) {
        callbacks.add(elements[0])
      }
    }
  }

  /**
   * This function removes the class in removeClass from all elements that
   * contain className. This function invokes the corresponding callback once.
   */
  removeClass (className, removeClass, callbacks) {
    let t = this
    let elements = t.element.getElementsByClassName(className)
    for (let i = 0; i < elements.length; ++i) {
      TextMark.removeClassFromElement(elements[i], removeClass)
    }
    if (callbacks !== undefined) {
      if (elements.length > 0 && callbacks.remove !== undefined) {
        callbacks.remove(elements[0])
      }
    }
  }

  /**
   * This function toggles the class from toggleClass in all elements with
   * className. The third arguments should, if provided, contain two callback
   * functions ('add' and 'remove') that are called once depending on the
   * toggle result.
   */
  toggleClass (className, toggleClass, callbacks) {
    let t = this
    let elements = t.element.getElementsByClassName(className)
    let added = false
    for (let i = 0; i < elements.length; ++i) {
      added = TextMark.toggleClassElement(elements[i], toggleClass)
    }
    if (callbacks !== undefined) {
      if (elements.length > 0 && added && callbacks.add !== undefined) {
        callbacks.add(elements[0])
      } else if (elements.length > 0 && !added &&
        callbacks.remove !== undefined) {
        callbacks.remove(elements[0])
      }
    }
  }

  /**
   * Remove a given selector from all elements. Without the selector all marks
   * are removed.
   *
   * @param selector
   */
  clear (selector) {
    let t = this
    if (selector !== undefined) {
      t.removeClass('text', selector)
    } else {
      t.element.innerHTML = t.html
    }
  }
}
