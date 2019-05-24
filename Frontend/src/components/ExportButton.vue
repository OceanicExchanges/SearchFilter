<template>
  <button id="export-button" v-on:click="clicked" type="button"/>
</template>
<script>

export default {
  name: 'ExportButton',
  methods: {
    clicked: function () {
      this.$emit('exportEvent')
    }
  }
}
</script>
<style scoped>
  button {
    margin-left: -1px;
    border: 1px solid #ddd;
    padding: 23px 24px 24px 23px;
    background: white url('../assets/download.svg') no-repeat 8px 8px;
    float: left;
    transition-duration: 0.1s;
    outline: none;
  }

  button:hover {
    background-color: #eee;
  }
</style>
