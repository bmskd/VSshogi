package com.example;

import java.io.FileInputStream;
import java.io.InputStream;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class App {
	static String JSON_FILE_NAME = "/Users/msk/eclipse-workspace/gw-game-6fd2a-38f2d7de4a42.json";
	static String URL = "https://gw-game-6fd2a.firebaseio.com/";

	public static void main(String[] args) throws Exception {
		System.out.println("JavaFirebaseSampleApp 実行開始");

		InputStream stream_json = new FileInputStream(JSON_FILE_NAME);

		FirebaseOptions options = new FirebaseOptions.Builder()
				.setServiceAccount(stream_json)
				.setDatabaseUrl(URL)
				.build();

		FirebaseApp.initializeApp(options);

		DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

		reference.child("key1").setValue("こんにちは！");
		reference.child("key2").setValue("ファイアーベース");
		reference.child("key3").setValue("よろしくお願いします！");

		while (true) {
			System.out.println("実行中です。");
			Thread.sleep(5000);
		}
	}
}
