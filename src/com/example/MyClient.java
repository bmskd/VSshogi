package com.example;

import java.awt.Container;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class MyClient extends JFrame implements MouseListener, MouseMotionListener {
	private JButton buttonArray[];//ボタン用の配列
	private Container c;
	private ImageIcon blackIcon, whiteIcon, boardIcon;
	private String[] deck = new String[5];
	private int a = 0;
	PrintWriter out;//出力用のライター

	public MyClient() {
		//名前の入力ダイアログを開く
		String myName = JOptionPane.showInputDialog(null, "名前を入力してください", "名前の入力", JOptionPane.QUESTION_MESSAGE);
		if (myName.equals("")) {
			myName = "No name";//名前がないときは，"No name"とする
		}

		//ウィンドウを作成する
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//ウィンドウを閉じるときに，正しく閉じるように設定する
		setTitle("MyClient");//ウィンドウのタイトルを設定する
		setSize(600, 500);//ウィンドウのサイズを設定する
		c = getContentPane();//フレームのペインを取得する

		//アイコンの設定
		whiteIcon = new ImageIcon("White.jpg");
		blackIcon = new ImageIcon("Black.jpg");
		boardIcon = new ImageIcon("GreenFrame.jpg");

		c.setLayout(null);//自動レイアウトの設定を行わない
		//ボタンの生成
		buttonArray = new JButton[10];//ボタンの配列を10個作成する[0]から[9]まで使える
		for (int i = 0; i < 10; i++) {
			buttonArray[i] = new JButton(boardIcon);//ボタンにアイコンを設定する
			c.add(buttonArray[i]);//ペインに貼り付ける
			if (i < 5) {
				buttonArray[i].setBounds(i * 80 + 100, 140, 60, 60);//ボタンの大きさと位置を設定する．(x座標，y座標,xの幅,yの幅）
				buttonArray[i].addMouseListener(this);//ボタンをマウスでさわったときに反応するようにする
				buttonArray[i].setActionCommand(Integer.toString(i));//ボタンに配列の情報を付加する（ネットワークを介してオブジェクトを識別するため）
			} else if (i > 4) {
				buttonArray[i].setBounds((i - 5) * 80 + 100, 220, 60, 60);//ボタンの大きさと位置を設定する．(x座標，y座標,xの幅,yの幅）
				buttonArray[i].addMouseListener(this);//ボタンをマウスでさわったときに反応するようにする
				buttonArray[i].setActionCommand(Integer.toString(i));//ボタンに配列の情報を付加する（ネットワークを介してオブジェクトを識別するため）
			}
		}

		//サーバに接続する
		Socket socket = null;
		try {
			//"localhost"は，自分内部への接続．localhostを接続先のIP Address（"133.42.155.201"形式）に設定すると他のPCのサーバと通信できる
			//10000はポート番号．IP Addressで接続するPCを決めて，ポート番号でそのPC上動作するプログラムを特定する
			socket = new Socket("localhost", 10000);
		} catch (UnknownHostException e) {
			System.err.println("ホストの IP アドレスが判定できません: " + e);
		} catch (IOException e) {
			System.err.println("エラーが発生しました: " + e);
		}

		MesgRecvThread mrt = new MesgRecvThread(socket, myName);//受信用のスレッドを作成する
		mrt.start();//スレッドを動かす（Runが動く）
	}

	//メッセージ受信のためのスレッド
	public class MesgRecvThread extends Thread {

		Socket socket;
		String myName;

		public MesgRecvThread(Socket s, String n) {
			socket = s;
			myName = n;
		}

		//通信状況を監視し，受信データによって動作する
		public void run() {
			try {
				InputStreamReader sisr = new InputStreamReader(socket.getInputStream());
				BufferedReader br = new BufferedReader(sisr);
				out = new PrintWriter(socket.getOutputStream(), true);
				out.println(myName);//接続の最初に名前を送る
				while (true) {
					String inputLine = br.readLine();//データを一行分だけ読み込んでみる
					if (inputLine != null) {//読み込んだときにデータが読み込まれたかどうかをチェックする
						System.out.println(inputLine);//デバッグ（動作確認用）にコンソールに出力する
						String[] inputTokens = inputLine.split(" "); //入力データを解析するために、スペースで切り分ける
						String cmd = inputTokens[0];//コマンドの取り出し．１つ目の要素を取り出す
						if (cmd.equals("MOVE")) {//cmdの文字と"MOVE"が同じか調べる．同じ時にtrueとなる
							//MOVEの時の処理(コマの移動の処理)
							String theBName = inputTokens[1];//ボタンの名前（番号）の取得
							int theBnum = Integer.parseInt(theBName);//ボタンの名前を数値に変換する
							int x = Integer.parseInt(inputTokens[2]);//数値に変換する
							int y = Integer.parseInt(inputTokens[3]);//数値に変換する
							buttonArray[theBnum].setLocation(x, y);//指定のボタンを位置をx,yに設定する
						}
					} else {
						break;
					}

				}
				socket.close();
			} catch (IOException e) {
				System.err.println("エラーが発生しました: " + e);
			}
		}
	}

	public static void main(String[] args) {
		MyClient net = new MyClient();
		net.setVisible(true);
	}

	public void mouseClicked(MouseEvent e) {//ボタンをクリックしたときの処理
		System.out.println("クリック");
		JButton theButton = (JButton) e.getComponent();//クリックしたオブジェクトを得る．型が違うのでキャストする
		String theArrayIndex = theButton.getActionCommand();//ボタンの配列の番号を取り出す

		if (a < 5) {
			deck[a] = theArrayIndex;
			a++;
		} else {
			System.out.println("5つ選択済み");
		}
		for (int i = 0; i < a; i++) {
			System.out.print("deck = " + deck[i] + ", ");
		}
		System.out.println();

		//送信情報を作成する（受信時には，この送った順番にデータを取り出す．スペースがデータの区切りとなる）
		//		String msg = "CLICK" + " " + theArrayIndex;

		//サーバに情報を送る
		//		out.println(msg);//送信データをバッファに書き出す
		//		out.flush();//送信データをフラッシュ（ネットワーク上にはき出す）する

		//		Icon theIcon = theButton.getIcon();//theIconには，現在のボタンに設定されたアイコンが入る
		//		//		System.out.println(theIcon);//デバッグ（確認用）に，クリックしたアイコンの名前を出力する
		//
		//		if (theIcon == whiteIcon) {//アイコンがwhiteIconと同じなら
		//			theButton.setIcon(blackIcon);//blackIconに設定する
		//		} else {
		//			theButton.setIcon(whiteIcon);//whiteIconに設定する
		//		}
		repaint();//画面のオブジェクトを描画し直す
	}

	public void mouseEntered(MouseEvent e) {//マウスがオブジェクトに入ったときの処理
		//		System.out.println("マウスが入った");
	}

	public void mouseExited(MouseEvent e) {//マウスがオブジェクトから出たときの処理
		//		System.out.println("マウス脱出");
	}

	public void mousePressed(MouseEvent e) {//マウスでオブジェクトを押したときの処理（クリックとの違いに注意）
		//		System.out.println("マウスを押した");
	}

	public void mouseReleased(MouseEvent e) {//マウスで押していたオブジェクトを離したときの処理
		//		System.out.println("マウスを放した");
	}

	public void mouseDragged(MouseEvent e) {//マウスでオブジェクトとをドラッグしているときの処理
		System.out.println("マウスをドラッグ");
		JButton theButton = (JButton) e.getComponent();//型が違うのでキャストする
		String theArrayIndex = theButton.getActionCommand();//ボタンの配列の番号を取り出す

		Point theMLoc = e.getPoint();//発生元コンポーネントを基準とする相対座標
		System.out.println(theMLoc);//デバッグ（確認用）に，取得したマウスの位置をコンソールに出力する
		Point theBtnLocation = theButton.getLocation();//クリックしたボタンを座標を取得する
		theBtnLocation.x += theMLoc.x - 15;//ボタンの真ん中当たりにマウスカーソルがくるように補正する
		theBtnLocation.y += theMLoc.y - 15;//ボタンの真ん中当たりにマウスカーソルがくるように補正する

		//送信情報を作成する（受信時には，この送った順番にデータを取り出す．スペースがデータの区切りとなる）
		String msg = "MOVE" + " " + theArrayIndex + " " + theBtnLocation.x + " " + theBtnLocation.y;

		//サーバに情報を送る
		out.println(msg);//送信データをバッファに書き出す
		out.flush();//送信データをフラッシュ（ネットワーク上にはき出す）する

		repaint();//オブジェクトの再描画を行う
	}

	public void mouseMoved(MouseEvent e) {//マウスがオブジェクト上で移動したときの処理
		System.out.println("マウス移動");
		int theMLocX = e.getX();//マウスのx座標を得る
		int theMLocY = e.getY();//マウスのy座標を得る
		System.out.println(theMLocX + "," + theMLocY);//コンソールに出力する
	}
}