<template>
	<div :class="'flex items-center hover:bg-white hover:shadow-sm transition-all duration-200 space-x-3 p-4 mx-2 my-1 rounded-xl cursor-pointer relative focus:outline-none ' +
		(selected ? 'bg-white shadow-md border border-orange-100' : '')
		" tabindex="-1">
		<div class="relative">
			<div class="p-2 bg-gradient-to-br from-gray-100 to-gray-200 rounded-xl">
				<UserSVG class="size-6 text-gray-600" />
			</div>
			<div v-if="unreadCount > 0"
				class="absolute -top-1 -right-1 w-5 h-5 bg-orange-500 text-white text-xs font-semibold rounded-full flex items-center justify-center">
				{{ unreadCount > 9 ? '9+' : unreadCount }}
			</div>
		</div>
		<div class="flex-1 min-w-0">
			<div class="flex items-center justify-between mb-1">
				<span class="truncate font-semibold text-gray-900 text-sm">
					{{ chat.participant }}
				</span>
				<span class="text-xs text-gray-400 shrink-0 ml-2">
					{{ formatTime(chat.lastMsg.time) }}
				</span>
			</div>
			<div class="flex items-center text-sm text-gray-600">
				<ReadReceiptSVG :class="'size-4 shrink-0 mr-1.5 transition ' +
					(this.chat.unread && !this.isRead ? 'text-gray-400' : 'text-orange-500')
					" v-if="sentByMe" />
				<span class="truncate text-xs" :class="unreadCount > 0 ? 'font-medium text-gray-900' : 'text-gray-500'">
					{{ truncateMessage(chat.lastMsg.message) }}
				</span>
			</div>
		</div>
	</div>
</template>

<script>
import ReadReceiptSVG from "../svg/ReadReceiptSVG.vue";
import UserSVG from "../svg/UserSVG.vue";

export default {
	components: {
		UserSVG,
		ReadReceiptSVG,
	},
	props: {
		chat: {
			type: Object,
			required: true,
		},
		chatMessages: {
			type: Array,
			required: true,
		},
		isRead: {
			type: Boolean,
			default: false,
		},
		selected: {
			type: Boolean,
		},
	},
	computed: {
		sentByMe() {
			return this.chat.lastMsg.sender == "Admin";
		},
		unreadCount() {
			// If the chat has been marked as read in the sidebar, show 0
			if (this.isRead) {
				return 0;
			}

			// If there's no unread messageId or the last message was sent by me, return 0
			if (!this.chat.unread || this.sentByMe) {
				return 0;

			}

			// Find the index of the unread message
			const unreadIndex = this.chatMessages.findIndex(msg => msg.id === this.chat.unread);
			if (unreadIndex === -1) return 0;

			// Count messages from unread index to end that are not from Admin
			return this.chatMessages.slice(unreadIndex).filter(msg => msg.sender !== 'Admin').length;
		},
		seen() {
			return this.chat.unread && this.sentByMe ? 1 : 0;
		},
	},
	methods: {
		formatTime(timeString) {
			// Simple time formatting - you can enhance this
			if (!timeString) return '';
			try {
				const time = new Date(`2000-01-01 ${timeString}`);
				return time.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
			} catch {
				return timeString;
			}
		},
		truncateMessage(message) {
			if (!message) return '';
			return message.length > 35 ? message.substring(0, 35) + '...' : message;
		}
	}
};
</script>

<style scoped>
/* Prevent focus outline flashing when switching chats */
div:focus {
	outline: none !important;
}

/* Remove any browser default focus styles */
div:focus-visible {
	outline: none !important;
}
</style>
