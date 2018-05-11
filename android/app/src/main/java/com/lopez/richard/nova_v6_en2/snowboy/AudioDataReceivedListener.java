package com.lopez.richard.nova_v6_en2.snowboy;

public interface AudioDataReceivedListener {
	void start();
    void onAudioDataReceived(byte[] data, int length);
    void stop();
}
