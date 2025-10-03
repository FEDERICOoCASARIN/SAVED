<template>
	<div class="flex flex-col size-full" id="chatWindow">
		<template v-for="(chatMsg, index) in chat" :key="chatMsg.id"> <!-- Unread banner -->
			<div v-if="shouldShowUnreadBanner(index)" :ref="'unreadBanner'"
				class="flex items-center justify-center py-2 px-4 my-2" id="unread-banner">
				<div class="flex-1 h-px bg-red-300"></div>
				<div class="px-3 py-1 bg-red-100 text-red-600 text-xs font-medium rounded-full border border-red-200">
					{{ getUnreadCount(index) }} unread message{{ getUnreadCount(index) > 1 ? 's' : '' }}
				</div>
				<div class="flex-1 h-px bg-red-300"></div>
			</div>

			<ChatMsg :msg="chatMsg.message" :isMine="isMyMessage(chatMsg)" :isRead="isMessageRead(index)"
				:time="chatMsg.time" :senderName="chatMsg.sender" :showName="shouldShowSenderName(index)" />
		</template>
	</div>
</template>
<script>
import ChatMsg from "./ChatMsg.vue";
import { useAuthStore } from "@/stores/auth";

export default {
	components: {
		ChatMsg,
	},
	setup() {
		const authStore = useAuthStore();
		return { authStore };
	},
	props: {
		chat: {
			type: Object,
			required: true,
		},
		clearText: {
			type: Function,
			required: true,
		},
		unreadMessageId: {
			type: String,
			default: null,
		},
		markAsRead: {
			type: Function,
			required: true,
		},
		unreadCount: {
			type: [String, Number],
			default: 0,
		},
	},
	computed: {
		isCompany() {
			return this.authStore.isUser; // isUser returns true for COMPANY role
		},
		unreadStartIndex() {
			// Find the index of the message with the unread messageId
			if (this.unreadMessageId) {
				return this.chat.findIndex(msg => msg.id === this.unreadMessageId);
			}
			return -1;
		},
		calculatedUnreadCount() {
			// Count messages from the unread start index to the end that are not from current user
			if (this.unreadStartIndex !== -1) {
				return this.chat.slice(this.unreadStartIndex).filter(msg => !this.isMyMessage(msg)).length;
			}
			return 0;
		},
		totalUnreadMessages() {
			// Prefer the passed unreadCount prop, otherwise calculate
			return parseInt(this.unreadCount) || this.calculatedUnreadCount;
		}
	},
	methods: {
		isMyMessage(chatMsg) {
			// Check if the message sender matches the current logged-in user
			const currentUser = this.authStore.getUser;
			if (!currentUser) return false;
			
			// Compare against the username from auth store
			const currentUsername = currentUser.username?.string || currentUser.username;
			return chatMsg.sender === currentUsername;
		},
		isMessageRead(index) {
			// Only show read receipts for messages sent by current user (my messages)
			if (!this.isMyMessage(this.chat[index])) {
				return false; // Don't show read receipts for messages from others
			}

			// Calculate if this message is read based on unread count
			// Messages are read if they are not among the last N unread messages
			const totalMessages = this.chat.length;
			const myMessages = this.chat.filter(msg => this.isMyMessage(msg));
			const myMessageIndex = myMessages.findIndex(msg => msg.id === this.chat[index].id);
			const totalMyMessages = myMessages.length;

			// If there are unread messages, the last N of my messages are unread
			if (this.totalUnreadMessages > 0) {
				return myMessageIndex < (totalMyMessages - this.totalUnreadMessages);
			}

			// If no unread count, all messages are considered read
			return true;
		},
		shouldShowUnreadBanner(index) {
			// Show banner before the first unread message
			return this.unreadStartIndex !== -1 && index === this.unreadStartIndex && this.totalUnreadMessages > 0;
		},
		shouldShowSenderName(index) {
			// Show sender name if it's the first message or if the previous message was from a different sender
			if (index === 0) {
				return true;
			}
			const currentSender = this.chat[index].sender;
			const previousSender = this.chat[index - 1].sender;
			return currentSender !== previousSender;
		},
		getUnreadCount(index) {
			return this.totalUnreadMessages;
		},
		scrollToBottom() {
			const chatContainer = document.getElementById("chatWindow");
			if (chatContainer) {
				chatContainer.scrollTop = chatContainer.scrollHeight;
			}
		},
		scrollToUnreadBanner() {
			this.$nextTick(() => {
				const chatContainer = document.getElementById("chatWindow");
				const unreadBanner = document.getElementById("unread-banner");

				if (chatContainer && unreadBanner) {
					// Scroll to the unread banner with some offset for better visibility
					const bannerTop = unreadBanner.offsetTop;
					const containerHeight = chatContainer.clientHeight;
					const scrollPosition = Math.max(0, bannerTop - containerHeight / 3);

					chatContainer.scrollTop = scrollPosition;
				}
			});
		},
		scrollToAppropriatePosition() {
			// If there are unread messages, scroll to the banner, otherwise scroll to bottom
			if (this.unreadStartIndex !== -1 && this.totalUnreadMessages > 0) {
				this.scrollToUnreadBanner();
			} else {
				this.scrollToBottom();
			}
		},
	},
	watch: {
		chat() {
			// When chat changes, scroll to appropriate position
			this.$nextTick(() => {
				this.scrollToAppropriatePosition();
			});
		}
	},
	mounted() {
		// Scroll to appropriate position when component mounts
		this.$nextTick(() => {
			this.scrollToAppropriatePosition();
		});
	},
	updated() {
		this.$nextTick(() => {
			this.scrollToAppropriatePosition();
		});
	},
};
</script>
