<script setup lang="ts">
import { ref } from 'vue'
import SideBar from '../components/SideBar.vue'

const isCollapsed = ref(false)

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}
</script>

<template>
  <div class="flex">
    <SideBar :is-collapsed="isCollapsed" @toggle="toggleSidebar" />


  </div>
</template>