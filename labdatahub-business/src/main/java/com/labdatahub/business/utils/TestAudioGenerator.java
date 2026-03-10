package com.labdatahub.business.utils;

/**
 * @Description:
 * @Author: ruoyi
 * @CreateTime: 2025-11-15
 */
import javax.sound.sampled.*;
import java.io.*;

public class TestAudioGenerator {

    /**
     * 创建一个简单的测试WAV文件
     */
    public static void createTestWavFile(String filePath) throws Exception {
        AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
        byte[] audioData = generateTestAudioData();

        try (ByteArrayInputStream bais = new ByteArrayInputStream(audioData);
             AudioInputStream audioInputStream = new AudioInputStream(bais, format, audioData.length / format.getFrameSize())) {

            AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, new File(filePath));
        }
    }

    /**
     * 生成测试音频数据（简单的正弦波）
     */
    private static byte[] generateTestAudioData() {
        int durationMs = 3000; // 3秒
        int sampleRate = 16000;
        int numSamples = durationMs * sampleRate / 1000;
        byte[] audioData = new byte[numSamples * 2]; // 16bit = 2字节

        for (int i = 0; i < numSamples; i++) {
            double time = i / (double) sampleRate;
            double frequency = 440.0; // A4 音调
            double amplitude = 0.5 * Short.MAX_VALUE;
            short sample = (short) (amplitude * Math.sin(2 * Math.PI * frequency * time));

            // 小端序
            audioData[2 * i] = (byte) (sample & 0xFF);
            audioData[2 * i + 1] = (byte) ((sample >> 8) & 0xFF);
        }

        return audioData;
    }
}
