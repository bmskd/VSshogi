package com.example;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

//�X���b�h���i�e�N���C�A���g�ɉ����āj
class ClientProcThread extends Thread {
	private int number;//�����̔ԍ�
	private Socket incoming;
	private InputStreamReader myIsr;
	private BufferedReader myIn;
	private PrintWriter myOut;
	private String myName;//�ڑ��҂̖��O

	public ClientProcThread(int n, Socket i, InputStreamReader isr, BufferedReader in, PrintWriter out) {
		number = n;
		incoming = i;
		myIsr = isr;
		myIn = in;
		myOut = out;
	}

	public void run() {
		try {
			myOut.println("Hello, client No." + number + "! Enter 'Bye' to exit.");//���񂾂��Ă΂��

			myName = myIn.readLine();//���߂Đڑ������Ƃ��̈�s�ڂ͖��O

			while (true) {//�������[�v�ŁC�\�P�b�g�ւ̓��͂��Ď�����
				String str = myIn.readLine();
				System.out.println("Received from client No."+number+"("+myName+"), Messages: "+str);
				if (str != null) {//���̃\�P�b�g�i�o�b�t�@�j�ɓ��͂����邩���`�F�b�N
					if (str.toUpperCase().equals("BYE")) {
						myOut.println("Good bye!");
						break;
					}
					MyServer.SendAll(str, myName);//�T�[�o�ɗ������b�Z�[�W�͐ڑ����Ă���N���C�A���g�S���ɔz��
				}
			}
		} catch (Exception e) {
			//�����Ƀv���O���������B����Ƃ��́C�ڑ����؂ꂽ�Ƃ�
			System.out.println("Disconnect from client No."+number+"("+myName+")");
			MyServer.SetFlag(number, false);//�ڑ����؂ꂽ�̂Ńt���O��������
		}
	}
}

class MyServer{

	private static int maxConnection=100;//�ő�ڑ���
	private static Socket[] incoming;//��t�p�̃\�P�b�g
	private static boolean[] flag;//�ڑ������ǂ����̃t���O
	private static InputStreamReader[] isr;//���̓X�g���[���p�̔z��
	private static BufferedReader[] in;//�o�b�t�@�����O���ɂ��e�L�X�g�ǂݍ��ݗp�̔z��
	private static PrintWriter[] out;//�o�̓X�g���[���p�̔z��
	private static ClientProcThread[] myClientProcThread;//�X���b�h�p�̔z��
	private static int member;//�ڑ����Ă��郁���o�[�̐�

	//�S���Ƀ��b�Z�[�W�𑗂�
	public static void SendAll(String str, String myName){
		//����ꂽ�������b�Z�[�W��ڑ����Ă���S���ɔz��
		for(int i=1;i<=member;i++){
			if(flag[i] == true){
				out[i].println(str);
				out[i].flush();//�o�b�t�@���͂��o�������o�b�t�@�ɂ���S�Ẵf�[�^�������ɑ��M����
				System.out.println("Send messages to client No."+i);
			}
		}
	}

	//�t���O�̐ݒ���s��
	public static void SetFlag(int n, boolean value){
		flag[n] = value;
	}

	//main�v���O����
	public static void main(String[] args) {
		//�K�v�Ȕz����m�ۂ���
		incoming = new Socket[maxConnection];
		flag = new boolean[maxConnection];
		isr = new InputStreamReader[maxConnection];
		in = new BufferedReader[maxConnection];
		out = new PrintWriter[maxConnection];
		myClientProcThread = new ClientProcThread[maxConnection];

		int n = 1;
		member = 0;//�N���ڑ����Ă��Ȃ��̂Ń����o�[���͂O

		try {
			System.out.println("The server has launched!");
			ServerSocket server = new ServerSocket(10000);//10000�ԃ|�[�g�𗘗p����
			while (true) {
				incoming[n] = server.accept();
				flag[n] = true;
				System.out.println("Accept client No." + n);
				//�K�v�ȓ��o�̓X�g���[�����쐬����
				isr[n] = new InputStreamReader(incoming[n].getInputStream());
				in[n] = new BufferedReader(isr[n]);
				out[n] = new PrintWriter(incoming[n].getOutputStream(), true);

				myClientProcThread[n] = new ClientProcThread(n, incoming[n], isr[n], in[n], out[n]);//�K�v�ȃp�����[�^��n���X���b�h���쐬
				myClientProcThread[n] .start();//�X���b�h���J�n����
				member = n;//�����o�[�̐����X�V����
				n++;
			}
		} catch (Exception e) {
			System.err.println("ソケット作成時にエラーが発生しました " + e);
		}
	}
}
