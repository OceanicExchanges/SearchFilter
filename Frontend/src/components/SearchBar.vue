<template>
  <input id="searchBar"
         type="search"
         v-model="inputText"
         v-bind:placeholder="dynamicPlaceholder"
         @keyup.enter="emitInput"/>
</template>
<script>
export default {
  name: 'SearchBar',
  data () {
    return {
      dynamicPlaceholder: 'Search...',
      inputText: ''
    }
  },
  methods: {
    emitInput: function () {
      this.$emit('searchBarEvent', this.inputText)
    }
  }
}
</script>
<style scoped>
  #searchBar {
    background: url('../assets/search.svg') no-repeat 8px 8px;
    width: calc(100% - 50px);
    padding: 12px 20px 12px 50px;
    border: 1px solid #ddd;
    float: left;
    font-size: 20px;
    outline: none
  }
</style>
