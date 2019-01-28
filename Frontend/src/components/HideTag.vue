<template>
  <span v-on:click="removeEvent()">{{ text }}</span>
</template>

<script>
export default {
  name: 'HideTag',
  props: ['text', 'id'],
  methods: {
    removeEvent: function () {
      this.$emit('removeEvent', 42)
    }
  }
}
</script>

<style>
  span {
    cursor: pointer;
  }
</style>
