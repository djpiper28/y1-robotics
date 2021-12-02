package common.netty.messages;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import common.EmotionsInterface;

/**
 * This abstract class will read the raw bytes from the IOStream and call the
 * functions for when it receives the different message types
 * 
 * @author Danny
 * @version 1.1
 */
public abstract class MessagesIOHandler {

	protected EmotionsInterface emotionsInterface;
	private DataInputStream inputStream;
	private DataOutputStream outputStream;
	public static final int PORT = 5421;
	final private boolean[] pollingThreadActive = new boolean[1];

	/**
	 * Alternate constructor for implementation
	 * 
	 * @param emotionsInterface Changes to emotions will call the on change methods
	 *                          in the interface
	 * @since 1.0
	 */
	protected MessagesIOHandler(EmotionsInterface emotionsInterface) {
		this.emotionsInterface = emotionsInterface;
	}

	/**
	 * Univsersal constructor to create and start the message IO handler
	 * 
	 * @param inputStream       Bluetooth input stream
	 * @param outputStream      Bluetooth output stream
	 * @param emotionsInterface Changes to emotions will call the on change methods
	 *                          in the interface
	 * @since 1.0
	 */
	public MessagesIOHandler(InputStream inputStream, OutputStream outputStream, EmotionsInterface emotionsInterface) {
		this.setIOStreams(inputStream, outputStream);
		this.emotionsInterface = emotionsInterface;

		// Start bluetooth polling thread
		this.startPollingThread();
	}

	/**
	 * Changes the IOStreams
	 * 
	 * @param inputStream  Bluetooth input stream
	 * @param outputStream Bluetooth output stream
	 * @since 1.0
	 */
	public void setIOStreams(InputStream inputStream, OutputStream outputStream) {
		this.inputStream = new DataInputStream(new BufferedInputStream(inputStream));
		this.outputStream = new DataOutputStream(new BufferedOutputStream(outputStream));
	}

	/**
	 * Start the polling thread if it isn't already active
	 * 
	 * @since 1.0
	 */
	public final synchronized void startPollingThread() {
		if (!this.pollingThreadActive[0]) {
			this.pollingThreadActive[0] = true;

			Thread pollingThread = new Thread("Bluetooth polling thread.") {
				@Override
				public void run() {
					while (pollingThreadActive[0]) {
						try {
							poll();
						} catch (IOException e) {
							pollingThreadActive[0] = false;
							onIOException(e);
						}

						Thread.yield();
					}
				}
			};

			pollingThread.setDaemon(true);
			pollingThread.start();
		}
	}

	/**
	 * STops the polling thread if it is active
	 * 
	 * @since 1.0
	 */
	public final void stopPollingThread() {
		this.pollingThreadActive[0] = false;
	}

	/**
	 * @return boolean of whether the thread is running
	 * @since 1.0
	 */
	public final boolean isRunning() {
		return this.pollingThreadActive[0];
	}

	/**
	 * The poll method will be called when the input stream has data to be read and
	 * will read the next message and it will call the on change methods
	 * 
	 * @throws IOException
	 * @since 1.0
	 */
	private final void poll() throws IOException {
		// Read input
		int length = this.inputStream.readInt();

		byte[] data = new byte[length];
		for (int i = 0; i < length; i++)
			data[i] = inputStream.readByte();

		try {
			switch (Message.getMessageType(data)) {
			case FIELD_CHANGE:
				// This calls the setter which in turn will call the onUpdate method
				new EmotionUpdateMessage().fromBytesArray(data, this.emotionsInterface);
				break;

			case NOTIFY_USER:
				UserNotification notif = new UserNotification("", "");

				new NotifyUserMessage().fromBytesArray(data, notif);

				this.onNotification(notif);
				break;

			case SIGNAL_STRENGTH_RESP:
				this.onSignalStrength((new SignalStrengthMessage()).fromBytes(data));
				break;

			case SIGNAL_STRENGTH_REQ:
				this.onSignalStrengthReq();
				break;

			case DEAD_ROBOT:
				this.onDeathMessage();
				break;

			default:
				System.out.println("ERROR: Bad message type not found.");
				break;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * This method is blocking and will send the desired message to the user, an
	 * exception will be thrown if the socket is interrupted or if there is no
	 * socket
	 * 
	 * @param message to send
	 * @throws IOException
	 * @since 1.0
	 */
	public synchronized void sendMessage(Message message) throws IOException {
		if (this.outputStream == null)
			throw new IOException();

		byte[] data = message.getByteArray();

		try {
			this.outputStream.writeInt(data.length);
			for (int i = 0; i < data.length; i++)
				this.outputStream.writeByte(data[i]);
			this.outputStream.flush();
		} catch (IOException e) {
			e.printStackTrace();
			this.onIOException(null);
			throw e;
		}
	}

	/**
	 * This method should be overriden by the implementation of the this class. This
	 * method will called when a user notification event is called. It should be set
	 * to an empty function if the implementation does not wupport user
	 * notifications (robot)
	 * 
	 * @param notification
	 * @since 1.0
	 */
	protected abstract void onNotification(UserNotification notification);

	/**
	 * When the polling thread has an exception this method is called. This runs on
	 * the same thread as the polling thread
	 * 
	 * @since 1.0
	 */
	protected abstract void onIOException(IOException e);

	/**
	 * Override this method to react to signal strength receive.
	 * 
	 * @param strength
	 * @since 1.1
	 */
	protected abstract void onSignalStrength(double strength);

	protected abstract void onSignalStrengthReq();

	protected abstract void onDeathMessage();

}
