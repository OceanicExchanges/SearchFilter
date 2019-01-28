<template>
  <div>
    <span>{{ totalHits !== undefined ? totalHits + ' total hits' : ''}}</span>
    <span>{{ hitsServed !== undefined ? hitsServed + ' hits served' : '' }}</span>
    <span>{{ computationTime !== undefined ? 'Request processed in ' + computationTime : ''}}</span>
    <span>{{ selected !== undefined ? selected + ' selected elements' : '' }}</span>
    <hide-tag v-for='item in tags'
              v-bind:key='item.id'
              @removeEvent="removeEvent"
              v-bind:text="item.text"
              v-bind:id="item.id"/>
  </div>
</template>
<script>
import HideTag from './HideTag.vue'

export default {
  name: 'InfoLine',
  props: ['totalHits', 'hitsServed', 'computationTime',
    'tags', 'selected'],
  methods: {
    removeEvent: function (id) {
      this.$emit('tagEvent', id)
    }
  },
  components: {
    'hide-tag': HideTag
  }
}
</script>
<style scoped>
  span {
    font-size: 12px;
    color: #666666;
    background-color: #fafafa;
    margin-left: 4px;
    margin-right: 4px;
    transition-duration: 0.1s;
  }

  span:hover {
    background-color: #eee;
  }
</style>
