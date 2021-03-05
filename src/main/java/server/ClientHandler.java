package server;

import java.io.*;
import java.net.Socket;

/**
 * Обработчик входящих клиентов
 */
public class ClientHandler implements Runnable {
	private final Socket socket;

	public ClientHandler(Socket socket) {
		this.socket = socket;
		System.out.println("Клиент подключился:" + socket.getInetAddress());
	}

	@Override
	public void run() {
		try (DataOutputStream out = new DataOutputStream(socket.getOutputStream());
		     DataInputStream in = new DataInputStream(socket.getInputStream())){
			while (true) {
					String command = in.readUTF();
					if ("upload".equals(command)) {
						try {
							File file = new File("server" + File.separator + in.readUTF());
							if (!file.exists()) {
								file.createNewFile();
							}
							long size = in.readLong();
							FileOutputStream fos = new FileOutputStream(file);
							byte[] buffer = new byte[256];
							for (int i = 0; i < (size + 255) / 256; i++) { // FIXME
								int read = in.read(buffer);
								fos.write(buffer, 0, read);
							}
							fos.close();
							out.writeUTF("DONE");
						} catch (Exception e) {
							out.writeUTF("ERROR");
						}
					} else if ("download".equals(command)) {
						File file = new File("server/" + File.separator + in.readUTF());
						long length = file.length();
						out.writeLong(length);
						FileInputStream fis = new FileInputStream(file);
						int read = 0;
						byte[] buffer = new byte[256];
						while ((read = fis.read(buffer)) != -1) {
							out.write(buffer, 0, read);
						}
						out.flush();

					} else if ("remove".equals(command)) {
						String fileName = in.readUTF();
						File file = new File(fileName);
						file.delete();
						out.writeUTF("DONE");
					}


			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
