package common.netty.messages;

/**
 * This is an internal class for use with the notifyusermessage
 * 
 * @author Danny
 * @version 1.0
 * @see common.netty.messages.NotifyUserMessage
 */
public final class UserNotification {

	private String notifTitle, notifMessage;

	public void setNotifTitle(String notifTitle) {
		this.notifTitle = notifTitle;
	}

	public void setNotifMessage(String notifMessage) {
		this.notifMessage = notifMessage;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((notifMessage == null) ? 0 : notifMessage.hashCode());
		result = prime * result + ((notifTitle == null) ? 0 : notifTitle.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof UserNotification))
			return false;
		UserNotification other = (UserNotification) obj;
		if (notifMessage == null) {
			if (other.notifMessage != null)
				return false;
		} else if (!notifMessage.equals(other.notifMessage))
			return false;
		if (notifTitle == null) {
			if (other.notifTitle != null)
				return false;
		} else if (!notifTitle.equals(other.notifTitle))
			return false;
		return true;
	}

	public String getNotifTitle() {
		return notifTitle;
	}

	public String getNotifMessage() {
		return notifMessage;
	}

	/**
	 * @param notifTitle   -> the title of the notifcation
	 * @param notifMessage -> the body of the notification
	 * @since 1.0
	 * @see <a href=
	 *      "https://developer.android.com/guide/topics/ui/notifiers/notifications">API
	 *      Page</a>
	 */
	public UserNotification(String notifTitle, String notifMessage) {
		this.notifTitle = notifTitle;
		this.notifMessage = notifMessage;
	}

	@Override
	public String toString() {
		return "UserNotification{" +
				"notifTitle='" + notifTitle + '\'' +
				", notifMessage='" + notifMessage + '\'' +
				'}';
	}
}
