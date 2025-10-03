<template>
	<transition name="fade-slide" appear>
		<div class="flex flex-col h-full p-6 space-y-6">
			<!-- Modern Header -->
			<div class="flex items-center justify-between">
				<div class="flex items-center space-x-3">
					<div class="p-2 bg-gradient-to-br from-orange-500 to-orange-600 rounded-xl shadow-lg">
						<ChatSVG class="size-6 text-white" />
					</div>
					<h1 class="text-3xl font-bold text-gray-900">
						{{ isCompany ? 'Chat' : 'Messages' }}
					</h1>
				</div>
			</div>

			<div
				class="flex rounded-2xl overflow-hidden bg-white shadow-xl border border-gray-200 flex-1 h-[calc(100%-8rem)]">
				<!-- Chat List Sidebar - Only show for admins -->
				<div v-if="!isCompany" class="flex flex-col w-80 flex-shrink-0 bg-gray-50 border-r border-gray-200">
					<!-- Enhanced Chat List Header -->
					<div class="flex h-16 bg-white border-b border-gray-200 items-center px-4 shadow-sm">
						<div class="w-full">
							<!-- Enhanced Search Bar -->
							<div class="relative">
								<SearchSVG
									class="absolute left-3 top-1/2 transform -translate-y-1/2 size-4 text-gray-400" />
								<input type="text"
									:placeholder="searchQuery ? `${filteredChats.length} conversation${filteredChats.length !== 1 ? 's' : ''} found` : 'Search conversations...'"
									class="w-full pl-10 pr-10 py-2.5 text-sm bg-gray-50 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-orange-500 focus:bg-white transition-all duration-200"
									v-model="searchQuery" />
								<button v-if="searchQuery" @click="clearSearch"
									class="absolute right-3 top-1/2 transform -translate-y-1/2 p-1 hover:bg-gray-200 rounded-full transition-colors duration-200">
									<svg class="size-3 text-gray-400" fill="currentColor" viewBox="0 0 20 20">
										<path fill-rule="evenodd"
											d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z"
											clip-rule="evenodd" />
									</svg>
								</button>
							</div>
						</div>
					</div>
					<div class="flex flex-col flex-1 overflow-y-auto scrollbar-thin scrollbar-thumb-gray-300 scrollbar-track-transparent"
						name="chat-list">
						<template v-if="filteredChats.length > 0">
							<ChatItem v-for="chat in filteredChats" :key="chat.participant" :chat="chat"
								:chatMessages="messages[chat.participant] || []"
								:isRead="chatReadStatus[chat.participant] || false"
								:selected="chat.participant == selectedChat" @click="
									() => {
										// Store previous selection before changing
										this.previousSelectedChat = this.selectedChat;
										this.selectedChat = chat.participant;
										this.selectChat();
									}
								" />
						</template>
						<template v-else>
							<div class="flex flex-col items-center justify-center flex-1 p-8 text-center">
								<div class="p-3 bg-gray-100 rounded-2xl mb-3">
									<SearchSVG class="size-8 text-gray-400" />
								</div>
								<p class="text-sm text-gray-500 mb-1">No conversations found</p>
								<p class="text-xs text-gray-400">Try searching for a different contact name</p>
							</div>
						</template>
					</div>
				</div>
				<div class="flex flex-col flex-1 bg-white">
					<!-- Enhanced Chat Header - always show for companies, conditionally for admins -->
					<div v-if="isCompany || selectedChat"
						class="flex shrink-0 w-full h-16 bg-white border-b border-gray-200 items-center px-6 shadow-sm">
						<div class="flex items-center space-x-4">
							<div class="p-2.5 bg-gradient-to-br from-orange-100 to-orange-200 rounded-xl">
								<UserSVG class="size-5 text-orange-600" />
							</div>
							<div class="flex flex-col">
								<span class="text-lg font-semibold text-gray-900">
									{{ isCompany ? 'Admin' : selectedChat }}
								</span>
							</div>
						</div>
					</div>
					<div class="flex flex-col h-full overflow-hidden">
						<ChatWindow v-if="isCompany || selectedChat" :chat="getCurrentChatMessages()"
							:clearText="clearText" :unreadMessageId="getUnreadMessageId(getCurrentChatId())"
							:unreadCount="getCurrentChatUnreadCount(getCurrentChatId())"
							:markAsRead="() => markChatAsRead(getCurrentChatId())" class="flex-1 overflow-y-auto" />
						<div v-if="isCompany || selectedChat"
							class="flex p-4 space-x-3 bg-white border-t border-gray-200">
							<div class="relative w-full">
								<textarea
									class="flex resize-none w-full min-h-[44px] px-4 py-3 max-h-40 bg-gray-50 border border-gray-200 rounded-2xl focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-orange-500 focus:bg-white transition-all duration-200 placeholder-gray-400"
									:placeholder="isCompany ? 'Send a message to admin...' : 'Type your message...'"
									v-model="text" ref="textarea" @input="autoResize" @keyup.enter.exact="sendChatMsg"
									@keydown.enter.exact.prevent rows="1" />
							</div>
							<button
								class="flex cursor-pointer bg-gradient-to-br from-orange-500 to-orange-600 hover:from-orange-600 hover:to-orange-700 transition-all duration-200 size-11 justify-center items-center text-white rounded-xl shadow-lg hover:shadow-xl transform hover:scale-105 disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none"
								@click="sendChatMsg" :disabled="!text.trim()">
								<SendSVG class="size-5" />
							</button>
						</div>
						<div v-else class="flex size-full items-center justify-center text-center">
							<div class="flex flex-col items-center space-y-3">
								<div class="p-4 bg-gray-100 rounded-2xl">
									<ChatSVG class="size-12 text-gray-400" />
								</div>
								<p class="text-gray-500 max-w-sm">Select a conversation to start messaging</p>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</transition>
</template>

<script>
import UserSVG from "../svg/UserSVG.vue";
import ChatItem from "./ChatItem.vue";
import ChatWindow from "./ChatWindow.vue";
import SendSVG from "../svg/SendSVG.vue";
import SearchSVG from "../svg/SearchSVG.vue";
import ChatSVG from "../svg/ChatSVG.vue";
import { useAuthStore } from "@/stores/auth.js";

export default {
	components: {
		ChatItem,
		ChatWindow,
		SendSVG,
		UserSVG,
		SearchSVG,
		ChatSVG,
	},
	data() {
		return {
			chats: { overview: [] }, // Will be populated from API
			messages: {}, // Store messages per chat
			text: "",
			searchQuery: "",
			selectedChat: null,
			previousSelectedChat: null,
			chatReadStatus: {},
			persistentUnreadBanners: {},
			authStore: useAuthStore(),
			messagePollingInterval: null,
		};
	},
	created() {
		this.fetchChats();
		// For companies, automatically set selectedChat to their own username
		if (this.isCompany) {
			this.selectedChat = this.authStore.getUser.username.string;
		}

		// Start polling for new messages every 3 seconds
		this.startMessagePolling();

		// Pause polling when tab is not visible to save resources
		document.addEventListener('visibilitychange', this.handleVisibilityChange);
	},
	beforeUnmount() {
		// Clean up polling when component is destroyed
		this.stopMessagePolling();
		document.removeEventListener('visibilitychange', this.handleVisibilityChange);
	},
	computed: {
		isCompany() {
			return this.authStore.isUser; // isUser returns true for COMPANY role
		},
		filteredChats() {
			if (!this.searchQuery.trim()) {
				return this.chats.overview;
			}

			const query = this.searchQuery.toLowerCase().trim();
			return this.chats.overview.filter(chat => {
				// Search only by participant name
				return chat.participant.toLowerCase().includes(query);
			});
		}
	},
	watch: {
		filteredChats(newChats) {
			// If the currently selected chat is not in the filtered results, clear selection
			if (this.selectedChat && !newChats.some(chat => chat.participant === this.selectedChat)) {
				this.selectedChat = null;
			}
		},
		$route() {
			// Clear all persistent unread banners when navigating to a different route
			if (this.$route) {
				this.persistentUnreadBanners = {};
			}
		}
	},
	methods: {
		startMessagePolling() {
			// Poll for new messages every 3 seconds
			this.messagePollingInterval = setInterval(() => {
				this.pollForNewMessages();
			}, 3000);
		},
		stopMessagePolling() {
			if (this.messagePollingInterval) {
				clearInterval(this.messagePollingInterval);
				this.messagePollingInterval = null;
			}
		},
		async pollForNewMessages() {
			try {
				// Refresh chat overview to check for new conversations
				await this.fetchChats();

				// If we have a selected chat, refresh its messages
				const currentChatId = this.getCurrentChatId();
				if (currentChatId) {
					const previousMessages = this.messages[currentChatId] || [];
					const previousCount = previousMessages.length;

					await this.fetchMessages(currentChatId);

					// Check if new messages arrived
					const newMessages = this.messages[currentChatId] || [];
					if (newMessages.length > previousCount) {
						// New messages detected - you could add notification logic here
						console.log(`${newMessages.length - previousCount} new message(s) received`);
					}
				}
			} catch (error) {
				console.error('Error polling for new messages:', error);
			}
		},
		handleVisibilityChange() {
			if (document.hidden) {
				// Tab is not visible, stop polling to save resources
				this.stopMessagePolling();
			} else {
				// Tab is visible again, restart polling
				this.startMessagePolling();
			}
		},
		autoResize() {
			const textarea = this.$refs.textarea;
			if (!textarea) return;

			textarea.style.height = "auto";
			textarea.style.height = textarea.scrollHeight + "px";

			const maxHeight = 160; // max-h-40
			if (textarea.scrollHeight > maxHeight) {
				textarea.style.overflowY = "auto";
			} else {
				textarea.style.overflowY = "hidden";
			}
		},
		clearText() {
			this.text = "";
			const textarea = this.$refs.textarea;
			if (textarea) {
				textarea.style.height = "auto";
			}
		},
		clearSearch() {
			this.searchQuery = "";
		},
		async fetchChats() {
			try {
				let res;
				if (this.isCompany) {
					// For companies, get their specific chat with admin
					const username = this.authStore.getUser.username.string;
					res = await fetch(`/api/chats/${username}`);
				} else {
					// For admins, get all chats
					res = await fetch("/api/chats");
				}
				const data = await res.json();

				if (this.isCompany) {
					// For companies, the response should be a single chat object, wrap it in an array
					this.chats.overview = data ? [data] : [];
					// Automatically fetch messages for their own chat
					const username = this.authStore.getUser.username.string;
					this.fetchMessages(username);
				} else {
					this.chats.overview = data;
					// Optionally, fetch messages for the first chat
					if (this.chats.overview.length > 0) {
						const first = this.chats.overview[0].participant || this.chats.overview[0].id;
						this.fetchMessages(first);
					}
				}
			} catch (e) {
				console.error("Failed to fetch chats", e);
			}
		},
		async fetchMessages(chatId) {
			try {
				// Use the correct message endpoint: /api/messages/{company}
				const res = await fetch(`/api/messages/${chatId}`);
				const data = await res.json();
				// Transform backend Message objects to frontend format
				this.messages[chatId] = data.map(msg => {
					// Backend sends time in HH:mm format (e.g. "14:30")
					// Use it directly since it's already in the correct display format
					const formattedTime = msg.time || 'N/A';

					return {
						id: msg.messageId,
						message: msg.body,
						sender: msg.sender,
						time: formattedTime,
						date: new Date().toLocaleDateString() // Use current date as fallback
					};
				});
			} catch (e) {
				console.error("Failed to fetch messages", e);
			}
		},
		selectChat() {
			// Clear the persistent banner for the previous chat when switching
			if (this.previousSelectedChat && this.previousSelectedChat !== this.selectedChat) {
				this.clearPersistentUnreadBanner(this.previousSelectedChat);
				// Also mark the previous chat as fully read in the overview
				this.markChatAsRead(this.previousSelectedChat);
			}

			// If the new chat has unread messages, preserve the banner info before marking as read
			if (this.selectedChat) {
				const chatOverview = this.chats.overview.find(c => c.participant === this.selectedChat);
				if (chatOverview && chatOverview.unread) {
					// Store the unread info for persistent banner display
					this.persistentUnreadBanners[this.selectedChat] = {
						messageId: chatOverview.unread,
						count: this.getUnreadCountFromOverview(chatOverview)
					};
				}

				// Mark chat as read for sidebar purposes only
				this.chatReadStatus[this.selectedChat] = true;
				this.fetchMessages(this.selectedChat);
			}

			// Update the previous selected chat
			this.previousSelectedChat = this.selectedChat;
		},
		markChatAsRead(chatParticipant) {
			// Only clear the overview unread status (for sidebar)
			// Don't clear persistent banner data here
			const chatOverview = this.chats.overview.find(c => c.participant === chatParticipant);
			if (chatOverview) {
				chatOverview.unread = null;
			}
		},
		clearPersistentUnreadBanner(chatParticipant) {
			// Separate method to clear persistent banner data
			this.persistentUnreadBanners[chatParticipant] = null;
		},
		getUnreadCountFromOverview(chatOverview) {
			// Helper method to calculate unread count from chat overview
			if (!chatOverview || !chatOverview.unread) return 0;

			// Find the unread message index
			const chat = this.messages[chatOverview.participant];
			if (!chat) return 0;

			const unreadStartIndex = chat.findIndex(msg => msg.id === chatOverview.unread);
			if (unreadStartIndex === -1) return 0;

			// Count messages from unread start that are not from current user
			const currentUser = this.isCompany ? (this.authStore.getUser.username?.string || this.authStore.getUser.username || "Company") : 'Admin';
			return chat.slice(unreadStartIndex).filter(msg => msg.sender !== currentUser).length;
		},
		getUnreadMessageId(chatParticipant) {
			// First check if we have a persistent banner for this chat
			if (this.persistentUnreadBanners[chatParticipant]) {
				return this.persistentUnreadBanners[chatParticipant].messageId;
			}

			// If no persistent banner, don't show any unread indicator
			// (the chat overview unread data gets cleared when navigating away)
			return null;
		},
		getCurrentChatUnreadCount(chatParticipant) {
			// First check if we have a persistent banner for this chat
			if (this.persistentUnreadBanners[chatParticipant]) {
				return this.persistentUnreadBanners[chatParticipant].count;
			}

			// If no persistent banner, return 0
			return 0;
		},
		sendChatMsg() {
			const currentChatId = this.getCurrentChatId();
			if (!currentChatId || !this.text.trim()) return;

			const message = {
				id: Date.now(), // Temporary ID for optimistic update
				sender: this.authStore.getUser.username?.string || this.authStore.getUser.username,
				message: this.text,
				date: new Date().toLocaleDateString(),
				time: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
			};

			// Optimistically add message
			if (!this.messages[currentChatId]) this.messages[currentChatId] = [];
			this.messages[currentChatId].push(message);
			this.clearPersistentUnreadBanner(currentChatId);

			const chatOverview = this.chats.overview.find(c => c.participant === currentChatId);
			if (chatOverview) {
				chatOverview.lastMsg = { sender: message.sender, message: message.message };
			}
			this.clearText();

			// Send message using the correct endpoint: POST /api/messages
			const requestBody = {
				company: currentChatId,
				body: message.message
			};

			fetch("/api/messages", {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify(requestBody)
			}).then(() => {
				// Refresh messages to get the correct ID from backend
				this.fetchMessages(currentChatId);
			}).catch(e => console.error("Failed to send message:", e));
		},
		receiveMessage(chatParticipant, message) {
			// Method to handle incoming messages (to be called when messages arrive via WebSocket/API)
			// Clear the persistent unread banner when a new message arrives in the current chat
			if (chatParticipant === this.selectedChat) {
				this.clearPersistentUnreadBanner(chatParticipant);
			}

			// Add the message to the chat (this would typically be handled by your message receiving logic)
			// This is just a placeholder for the integration point
		},
		getCurrentChatId() {
			return this.isCompany ? (this.authStore.getUser.username?.string || this.authStore.getUser.username) : this.selectedChat;
		},
		getCurrentChatMessages() {
			const chatId = this.getCurrentChatId();
			return this.messages[chatId] || [];
		},
	},
	beforeUnmount() {
		// Clear all persistent unread banners when navigating away from chat completely
		this.persistentUnreadBanners = {};
	},
};
</script>

<style>
.fade-slide-enter-active,
.fade-slide-leave-active {
	transition: all 0.4s ease;
}

.fade-slide-enter-from {
	opacity: 0;
	transform: translateY(20px);
}

.fade-slide-enter-to {
	opacity: 1;
	transform: translateY(0);
}

/* Custom scrollbar styles */
.scrollbar-thin {
	scrollbar-width: thin;
}

.scrollbar-thumb-gray-300::-webkit-scrollbar-thumb {
	background-color: #d1d5db;
	border-radius: 6px;
}

.scrollbar-track-transparent::-webkit-scrollbar-track {
	background: transparent;
}

.scrollbar-thin::-webkit-scrollbar {
	width: 6px;
}

.scrollbar-thin::-webkit-scrollbar-thumb {
	background-color: #d1d5db;
	border-radius: 6px;
}

.scrollbar-thin::-webkit-scrollbar-track {
	background: transparent;
}

.scrollbar-thin::-webkit-scrollbar-thumb:hover {
	background-color: #9ca3af;
}
</style>