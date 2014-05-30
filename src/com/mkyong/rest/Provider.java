package com.mkyong.rest;

import java.io.*;
import java.net.*;

import org.apache.commons.io.IOUtils;

import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;

public class Provider {
	ServerSocket providerSocket;
	Socket connection = null;
	ObjectOutputStream out;
	ObjectInputStream in;
	String message;
	StreamSpeechRecognizer recognizer;

	Provider() {
		try {

			Configuration configuration = new Configuration();

			// Load model from the jar
			configuration
					.setAcousticModelPath("resource:/models/acoustic/wsj_8kHz");

			// You can also load model from folder
			// configuration.setAcousticModelPath("file:en-us");

			configuration
					.setDictionaryPath("resource:/models/acoustic/wsj_8kHz/dict/cmudict.0.6d");

			configuration
					.setLanguageModelPath("resource:/models/language/en-us.lm.dmp");
			System.out.println("Loaded models !");

			recognizer = new StreamSpeechRecognizer(
					configuration);
			System.out.println("Streaming speech recognizer ...");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void run() {
		try {
			// 1. creating a server socket
			providerSocket = new ServerSocket(2004);
			// 2. Wait for connection
			System.out.println("Waiting for connection");
			connection = providerSocket.accept();
			System.out.println("Connection received from "
					+ connection.getInetAddress().getHostName());
			// 3. get Input and Output streams
			out = new ObjectOutputStream(connection.getOutputStream());
			out.flush();
			in = new ObjectInputStream(connection.getInputStream());
			sendMessage("Connection successful");
			// 4. The two parts communicate via the input and output streams
			do {
				try {
					recognizer.startRecognition(new ByteArrayInputStream((byte[]) in.readObject()));
		            
		            System.out.println("Start recording ...");
		            SpeechResult result = recognizer.getResult();
		            
		            if(result != null) {
		            	sendMessage(result.getHypothesis());
		            }
		            recognizer.stopRecognition();
//					message = (String) in.readObject();
//					System.out.println("client>" + message);
//					if (message.equals("bye"))
						sendMessage("bye");
						break;
				} catch (ClassNotFoundException classnot) {
					System.err.println("Data received in unknown format");
				}
			} while (true);
		} catch (IOException ioException) {
			ioException.printStackTrace();
		} finally {
			// 4: Closing connection
			try {
				in.close();
				out.close();
				providerSocket.close();
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
	}

	void sendMessage(String msg) {
		try {
			out.writeObject(msg);
			out.flush();
			System.out.println("server>" + msg);
		} catch (IOException ioException) {
			ioException.printStackTrace();
		}
	}

	public static void main(String args[]) {
		Provider server = new Provider();
		while (true) {
			server.run();
		}
	}
}