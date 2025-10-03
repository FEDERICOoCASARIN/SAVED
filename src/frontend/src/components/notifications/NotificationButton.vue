<template>
     <div
            class="px-3 py-2 w-fit flex items-center gap-2 justify-center  transition-colors rounded-xl cursor-pointer"
          >
            <NotificationsSVG class="w-5 h-5" />
    </div>
</template>
<script>
import NotificationsSVG from "../svg/NotificationsSVG.vue";
export default{
	components: {
		NotificationsSVG,
    }
}
</script>