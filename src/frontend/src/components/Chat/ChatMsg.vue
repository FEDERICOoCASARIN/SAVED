<template>
	<!-- My Messages (Right aligned) -->
	<div v-if="isMine" class="flex w-full justify-end p-2 px-6 mb-2 group">
		<div class="flex flex-col items-end max-w-[80%] sm:max-w-[70%]">
			<!-- Message bubble -->
			<div class="relative">
				<div
					class="bg-gradient-to-br from-orange-500 to-orange-600 text-white rounded-2xl rounded-br-md px-5 py-4 shadow-lg border-2 border-orange-400/30 min-w-20 my-message-bubble">
					<!-- Message content and timestamp in flex layout -->
					<div class="flex items-end justify-between gap-3">
						<!-- Message content -->
						<div
							class="text-base leading-relaxed whitespace-pre-wrap break-words text-white font-medium flex-1">
							{{ msg }}
						</div>

						<!-- Time and read status -->
						<div class="flex items-center space-x-1 flex-shrink-0 self-end">
							<span class="text-xs text-orange-100 opacity-90 font-medium">
								{{ time }}
							</span> <!-- Read receipt indicators -->
							<div v-if="shouldShowReadReceipt" class="flex items-center ml-2">
								<!-- Single checkmark for unread -->
								<svg v-if="!isRead" class="w-3 h-3 text-orange-100 opacity-60" fill="currentColor"
									viewBox="0 0 20 20">
									<path fill-rule="evenodd"
										d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z"
										clip-rule="evenodd" />
								</svg>
								<!-- Double checkmark for read -->
								<ReadReceiptSVG v-else class="w-3 h-3 text-orange-100 opacity-90" />
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>

	<!-- Other Messages (Left aligned) -->
	<div v-else class="flex w-full justify-start p-2 px-6 mb-2 group">
		<div class="flex flex-col items-start max-w-[80%] sm:max-w-[70%]">
			<!-- Sender name for group chats -->
			<div v-if="showName" class="text-xs text-gray-500 mb-1 ml-3 font-medium">
				{{ senderName }}
			</div>

			<!-- Message bubble -->
			<div class="relative">
				<div
					class="bg-gray-50 border border-gray-200 text-gray-700 rounded-2xl rounded-bl-md px-5 py-4 shadow-sm min-w-20 other-message-bubble hover:bg-gray-100 transition-colors duration-200">
					<!-- Message content and timestamp in flex layout -->
					<div class="flex items-end justify-between gap-3">
						<!-- Message content -->
						<div class="text-base leading-relaxed whitespace-pre-wrap break-words flex-1">
							{{ msg }}
						</div>

						<!-- Time -->
						<div class="flex-shrink-0 self-end">
							<span class="text-xs text-gray-400 font-medium">
								{{ time }}
							</span>
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</template>
<script>
import ReadReceiptSVG from '../svg/ReadReceiptSVG.vue';

export default {
	components: {
		ReadReceiptSVG,
	},
	props: {
		msg: {
			type: String,
			required: true,
		},
		isMine: {
			type: Boolean,
			required: true,
		},
		isRead: {
			type: Boolean,
			default: false,
		},
		time: {
			type: String,
			required: true,
		},
		senderName: {
			type: String,
			required: true,
		},
		showName: {
			type: Boolean,
			required: true,
		},
	},
	computed: {
		shouldShowReadReceipt() {
			return this.isMine; // Only show read receipts for messages I sent
		}
	},
	mounted() {
		// Add smooth entrance animation without affecting pointer events
		this.$nextTick(() => {
			if (this.$el && this.$el.classList) {
				this.$el.classList.add('message-appear');
			}
		});
	},
};
</script>

<style scoped>
/* Safe entrance animation */
.message-appear {
	animation: messageAppear 0.3s ease-out;
}

/* Custom message bubble animations */
@keyframes messageAppear {
	from {
		opacity: 0;
		transform: translateY(10px) scale(0.95);
	}

	to {
		opacity: 1;
		transform: translateY(0) scale(1);
	}
}

/* Custom gradient for better depth */
.bg-gradient-to-br {
	background-image: linear-gradient(to bottom right, var(--tw-gradient-stops));
}

/* Enhanced styling for my messages */
.my-message-bubble {
	background: linear-gradient(135deg, #FA812F 0%, #F97316 50%, #EA580C 100%);
	position: relative;
	overflow: hidden;
}

.my-message-bubble::before {
	content: '';
	position: absolute;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background: linear-gradient(135deg, rgba(255, 255, 255, 0.1) 0%, rgba(255, 255, 255, 0.05) 100%);
	pointer-events: none;
}

.my-message-bubble:hover {
	transform: translateY(-1px);
	box-shadow: 0 6px 14px rgba(250, 129, 47, 0.3);
	transition: all 0.2s ease-out;
}

/* Enhanced styling for other messages */
.other-message-bubble {
	background: linear-gradient(135deg, #F9FAFB 0%, #F3F4F6 100%);
}

.other-message-bubble:hover {
	transform: translateY(-1px);
	box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
	transition: all 0.2s ease-out;
}

/* Enhanced shadow on hover */
.hover\:shadow-md:hover {
	box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
}

/* Subtle pulse animation for unread messages */
@keyframes subtlePulse {

	0%,
	100% {
		opacity: 1;
	}

	50% {
		opacity: 0.9;
	}
}

.animate-pulse {
	animation: subtlePulse 2s ease-in-out infinite;
}
</style>
