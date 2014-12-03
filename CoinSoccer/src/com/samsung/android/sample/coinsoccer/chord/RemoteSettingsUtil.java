package com.samsung.android.sample.coinsoccer.chord;

import java.nio.ByteBuffer;

import com.samsung.android.sample.coinsoccer.settings.GameSettings;
import com.samsung.android.sample.coinsoccer.settings.PlayerSettings;
import com.samsung.android.sample.coinsoccer.settings.Which;

public class RemoteSettingsUtil {

	public static byte[] toBytes(GameSettings gameSettings) {
		ByteBuffer buffer = ByteBuffer.allocate(3 * 4);
		buffer.putInt(gameSettings.scoreLimit);
		buffer.putInt(gameSettings.turnLimit);
		buffer.putInt(gameSettings.timePerTurnMillis);
		return buffer.array();
	}

	public static GameSettings toGameSettings(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		return new GameSettings(buffer.getInt(), buffer.getInt(), buffer.getInt());
	}

	public static byte[] toBytes(PlayerSettings playerSettings) {
		byte[] nameBytes = playerSettings.name.getBytes();
		ByteBuffer buffer = ByteBuffer.allocate(1 + 4 + nameBytes.length);
		buffer.put((byte) playerSettings.which.ordinal());
		buffer.putInt(playerSettings.color);
		for (int i = 0; i < nameBytes.length; i++) {
			buffer.put(nameBytes[i]);
		}
		return buffer.array();
	}

	public static PlayerSettings toPlayerSettings(byte[] bytes) {
		ByteBuffer buffer = ByteBuffer.wrap(bytes);
		Which which = Which.forOrdinal(buffer.get());
		int color = buffer.getInt();
		int pos = buffer.position();
		String name = new String(bytes, pos, bytes.length - pos);
		return new PlayerSettings(which, color, name);
	}
}
