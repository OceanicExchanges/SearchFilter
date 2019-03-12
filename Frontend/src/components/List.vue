<template>
  <div>
    <ul>
      <li v-for='item in items' v-bind:key="item.id">
        <list-item
            v-bind:item="item"
            v-bind:terms="terms"
            v-bind:link="item.link"
            @moreLikeThisEvent="moreLikeThisEvent"
            @selectionEvent="selectionEvent"/>
      </li>
    </ul>
    <div class="pagination">
      <a class="element" href="#" ref="previous">Previous</a>
      <a class="element">Page {{ page }}</a>
      <a class="element" href="#" ref="next">Next</a>
    </div>
  </div>
</template>
<script>
import ListItem from './ListItem.vue'

export default {
  name: 'List',
  components: {
    'list-item': ListItem
  },
  props: ['items', 'terms'],
  data: function () {
    return {
      page: 1
    }
  },
  mounted: function () {
    let t = this
    t.$refs.previous.addEventListener('click', t.previous, false)
    t.$refs.next.addEventListener('click', t.next, false)
  },
  methods: {
    moreLikeThisEvent: function (id) {
      this.$emit('moreLikeThisEvent', id)
    },
    selectionEvent: function (selection) {
      this.$emit('selectionEvent', selection)
    },
    deselectionEvent: function (deselection) {
      this.$emit('deselectionEvent', deselection)
    },
    selectionClearEvent: function (id) {
      this.$emit('selectionClearEvent', id)
    },
    previous: function () {
      let t = this
      if (t.page > 1) {
        t.page -= 1
        this.$emit('pageEvent', t.page)
      }
    },
    next: function () {
      let t = this
      t.page += 1
      t.$emit('pageEvent', t.page)
    }
  }
}
</script>
<style scoped>
  ul {
    overflow-y: scroll;
    height: 800px;
  }

  a {
    background-color: #eee;
    text-decoration: none;
    display: inline-block;
    color: black;
  }

  a:hover {
    background-color: #ddd;
    color: black;
  }

  .pagination {
    display: flex;
  }

  .element {
    text-align: center;
    width: 33.4%;
  }
</style>
