package com;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.Set;

/**
 * MultiThread Ftp Resauest handler pour écouter des commands de client au
 * serveur
 * 
 * @author JIN Benli et ZHAO Xuening
 * 
 */
public class FtpRequest extends Thread // Ftp
{
	Socket csocket;
	Socket dsocket;
	int id;
	String cmd = "";
	String param = "";
	String user;
	String remoteHost = " ";
	int remotePort = 0;
	String dir = FtpServer.initDir;
	String rootdir = "/";
	int state = 0;
	String reply;
	PrintWriter out;
	int type = FtpState.FTYPE_IMAGE;
	String requestfile = "";
	boolean isrest = false;

	/** pqrser des commandes en numéro */
	int parseInput(String s) //
	{
		int p = 0;
		int i = -1;

		// sil existe des commandes avec paramètre, séparer les dans variable de
		// cmd et param
		p = s.indexOf(" ");
		if (p == -1)
			cmd = s;
		else
			cmd = s.substring(0, p);

		if (p >= s.length() || p == -1)
			param = "";
		else
			param = s.substring(p + 1, s.length());
		cmd = cmd.toUpperCase();

		// cmdParam
		if (cmd.equals("USER"))
			i = 1;
		if (cmd.equals("PASS"))
			i = 2;
		if (cmd.equals("CDUP"))
			i = 3;
		if (cmd.equals("CWD"))
			i = 4;
		if (cmd.equals("QUIT")) // sorti le sys
			i = 5;
		if (cmd.equals("RETR"))
			i = 6;
		if (cmd.equals("STOR"))
			i = 7;
		if (cmd.equals("PWD")) // répertoire courant
			i = 8;
		if (cmd.equals("LIST"))
			i = 9;
		if (cmd.equals("PORT"))
			i = 10;
		return i;
	}// parseInput() end

	/** vqlider l'existence de path donné dans param */
	int validatePath(String s)
	{
		File f = new File(s);
		if (f.exists() && !f.isDirectory())
		{
			String s1 = s.toLowerCase();
			String s2 = rootdir.toLowerCase();
			if (s1.startsWith(s2))
				return 1;
			else
				return 0;
		}
		f = new File(addTail(dir) + s);
		if (f.exists() && !f.isDirectory())
		{
			String s1 = (addTail(dir) + s).toLowerCase();
			String s2 = rootdir.toLowerCase();
			if (s1.startsWith(s2))
				return 2;
			else
				return 0;
		}
		return 0;
	}// validatePath() end

	/**
	 * vérifier la vraibilité de password dans liste de chaine qui a géré par
	 * FtpServer.java
	 */
	public boolean checkPASS(String s)
	{
		for (int i = 0; i < FtpServer.usersInfo.size(); i++)
		{
			if (((UserInfo) FtpServer.usersInfo.get(i)).user.equals(user)
					&& ((UserInfo) FtpServer.usersInfo.get(i)).password
							.equals(s)) //
			{
				rootdir = ((UserInfo) FtpServer.usersInfo.get(i)).workDir;
				dir = ((UserInfo) FtpServer.usersInfo.get(i)).workDir;
				return true;
			}
		}
		return false;
	}// checkPASS() end

	/** répondre à la commande USER pour un nom de compte */
	public boolean commandUSER() // User
	{
		if (cmd.equals("USER"))
		{
			reply = "331 User name okay, need password";
			user = param;
			state = FtpState.FS_WAIT_PASS;
			return false;
		}
		else
		{
			reply = "501 Syntax error in parameters or arguments";
			return true;
		}

	}// commandUser() end

	/** répondre à la commande PASS pour un password */
	public boolean commandPASS()
	{
		if (cmd.equals("PASS"))
		{
			if (checkPASS(param))
			{
				reply = "230 User logged in, proceed";
				state = FtpState.FS_LOGIN;
				System.out.println("Message: user " + user + " Form "
						+ remoteHost + "Login");
				System.out.print("->");
				return false;
			}
			else
			{
				reply = "530 Not logged in";
				return true;
			}
		}
		else
		{
			reply = "501 Syntax error in parameters or arguments";
			return true;
		}

	}// commandPass() end

	/** donner erreur si commande unvalid */
	void errCMD()
	{
		reply = "500 Syntax error, command unrecognized";
	}

	/** répondre à la commande CDUP égale CWD .. */
	public boolean commandCDUP()
	{
		dir = FtpServer.initDir;
		File f = new File(dir);
		if (f.getParent() != null && (!dir.equals(rootdir)))
		{
			dir = f.getParent();
			reply = "200 Command okay";
		}
		else
		{
			reply = "550 Current directory has no parent";
		}

		return false;
	}// commandCDUP() end

	/** répondre à la commande CWD pour changer des répertoire */
	public boolean commandCWD()
	{
		File f = new File(param);
		String s = "";
		String s1 = "";
		if (dir.endsWith("/"))
			s = dir;
		else
			s = dir + "/";
		File f1 = new File(s + param);

		if (f.isDirectory() && f.exists())
		{
			if (param.equals("..") || param.equals("..\\"))
			{
				if (dir.compareToIgnoreCase(rootdir) == 0)
				{
					reply = "550 The directory does not exists";
					// return false;
				}
				else
				{
					s1 = new File(dir).getParent();
					if (s1 != null)
					{
						dir = s1;
						reply = "250 Requested file action okay, directory change to "
								+ dir;
					}
					else
						reply = "550 The directory does not exists";
				}
			}
			else if (param.equals(".") || param.equals(".\\"))
			{
			}
			else
			{
				dir = param;
				reply = "250 Requested file action okay, directory change to "
						+ dir;
			}
		}
		else if (f1.isDirectory() && f1.exists())
		{
			dir = s + param;
			reply = "250 Requested file action okay, directory change to "
					+ dir;
		}
		else
			reply = "501 Syntax error in parameters or arguments";

		return false;
	} // commandCDW() end

	/** écouter la commande QUIT pour sortir la connexion */
	public boolean commandQUIT() // QUITFtp
	{
		reply = "221 Service closing control connection";
		return true;
	}// commandQuit() end

	/** écouter la commande PORT pour récuperer les info de client, ex: IP, PORT */
	public boolean commandPORT()
	{
		int p1 = 0;
		int p2 = 0;
		int[] a = new int[6];
		int i = 0;
		try
		{
			while ((p2 = param.indexOf(",", p1)) != -1)
			{
				a[i] = Integer.parseInt(param.substring(p1, p2));
				p2 = p2 + 1;
				p1 = p2;
				i++;
			}
			a[i] = Integer.parseInt(param.substring(p1, param.length()));
		}
		catch (NumberFormatException e)
		{
			reply = "501 Syntax error in parameters or arguments";
			return false;
		}

		remoteHost = a[0] + "." + a[1] + "." + a[2] + "." + a[3];
		remotePort = a[4] * 256 + a[5];
		reply = "200 Command okay";
		return false;
	}// commandPort() end

	/**
	 * répondre à la commande LIST et envoyer à client la list de répertoire
	 * courant, devoir faire après PORT commande
	 */
	public boolean commandLIST()
	{
		try
		{
			dsocket = new Socket(remoteHost, remotePort);
			PrintWriter dout = new PrintWriter(dsocket.getOutputStream(), true);
			if (param.equals("") || param.equals("LIST"))
			{
				out.println("150 Opening ASCII mode data connection for /bin/ls. ");
				File f = new File(dir);
				File[] files = f.listFiles();

				String temp_file_structure = "";

				for (File temp_file : files)
				{

					if (temp_file.isFile())
					{

						temp_file_structure = "-rwxr--r-- 1 owner group "
								+ temp_file.length() + " Feb 21 04:37 " + " "
								+ temp_file.getName();

					}
					else if (f.isDirectory())
					{

						temp_file_structure = "drwxr--r-- 1 owner group "
								+ temp_file.length() + " Feb 21 04:37 " + " "
								+ temp_file.getName() + "/";

					}

					dout.println(temp_file_structure);

				}
				// String[] dirStructure = f.list();
				// String fileType;
				// for (int i = 0; i < dirStructure.length; i++)
				// {
				// if (dirStructure[i].indexOf(".") != -1)
				// {
				// fileType = "-";
				// }
				// else
				// {
				// fileType = "d ";
				// }
				// dout.println(fileType + dirStructure[i]);
				// }
			}
			dout.close();
			dsocket.close();
			reply = "226 Transfer complete !";
		}
		catch (Exception e)
		{
			e.printStackTrace();
			reply = "451 Requested action aborted: local error in processing";
			return false;
		}

		return false;
	}// commandLIST() end

	/**
	 * répondre à la commande RETR pour télécharger un fichier indiqué, devoir
	 * faire après PORT commande
	 */
	public boolean commandRETR()
	{
		requestfile = param;
		File f = new File(requestfile);
		if (!f.exists())
		{
			f = new File(addTail(dir) + param);
			if (!f.exists())
			{
				reply = "550 File not found";
				return false;
			}
			requestfile = addTail(dir) + param;
		}

		if (isrest)
		{

		}
		else
		{
			if (type == FtpState.FTYPE_IMAGE)
			{
				try
				{
					out.println("150 Opening Binary mode data connection for "
							+ requestfile);
					dsocket = new Socket(remoteHost, remotePort);
					BufferedInputStream fin = new BufferedInputStream(
							new FileInputStream(requestfile));
					PrintStream dout = new PrintStream(
							dsocket.getOutputStream(), true);
					byte[] buf = new byte[1024];
					int l = 0;
					while ((l = fin.read(buf, 0, 1024)) != -1)
					{
						dout.write(buf, 0, l);
					}
					fin.close();
					dout.close();
					dsocket.close();
					reply = "226 Transfer complete !";

				}
				catch (Exception e)
				{
					e.printStackTrace();
					reply = "451 Requested action aborted: local error in processing";
					return false;
				}

			}
			if (type == FtpState.FTYPE_ASCII)
			{
				try
				{
					out.println("150 Opening ASCII mode data connection for "
							+ requestfile);
					dsocket = new Socket(remoteHost, remotePort);
					BufferedReader fin = new BufferedReader(new FileReader(
							requestfile));
					PrintWriter dout = new PrintWriter(
							dsocket.getOutputStream(), true);
					String s;
					while ((s = fin.readLine()) != null)
					{
						dout.println(s);
					}
					fin.close();
					dout.close();
					dsocket.close();
					reply = "226 Transfer complete !";
				}
				catch (Exception e)
				{
					e.printStackTrace();
					reply = "451 Requested action aborted: local error in processing";
					return false;
				}
			}
		}
		return false;

	}// commandRETR() end

	/**
	 * répondre à la commande STOR pour upload un fichier indiqué, devoir faire
	 * après PORT commande
	 */
	public boolean commandSTOR()
	{
		if (param.equals(""))
		{
			reply = "501 Syntax error in parameters or arguments";
			return false;
		}
		requestfile = addTail(dir) + param;
		if (type == FtpState.FTYPE_IMAGE)
		{
			try
			{
				out.println("150 Opening Binary mode data connection for "
						+ requestfile);
				dsocket = new Socket(remoteHost, remotePort);
				BufferedOutputStream fout = new BufferedOutputStream(
						new FileOutputStream(requestfile));
				BufferedInputStream din = new BufferedInputStream(
						dsocket.getInputStream());
				byte[] buf = new byte[1024];
				int l = 0;
				while ((l = din.read(buf, 0, 1024)) != -1)
				{
					fout.write(buf, 0, l);
				}// while()
				din.close();
				fout.close();
				dsocket.close();
				reply = "226 Transfer complete !";
			}
			catch (Exception e)
			{
				e.printStackTrace();
				reply = "451 Requested action aborted: local error in processing";
				return false;
			}
		}
		if (type == FtpState.FTYPE_ASCII)
		{
			try
			{
				out.println("150 Opening ASCII mode data connection for "
						+ requestfile);
				dsocket = new Socket(remoteHost, remotePort);
				PrintWriter fout = new PrintWriter(new FileOutputStream(
						requestfile));
				BufferedReader din = new BufferedReader(new InputStreamReader(
						dsocket.getInputStream()));
				String line;
				while ((line = din.readLine()) != null)
				{
					fout.println(line);
				}
				din.close();
				fout.close();
				dsocket.close();
				reply = " 226 Transfer complete !";
			}
			catch (Exception e)
			{
				e.printStackTrace();
				reply = "451 Requested action aborted: local error in processing";
				return false;
			}
		}
		return false;
	}// commandSTOR() end

	/** écouter la commande PWD */
	public boolean commandPWD() // pwd
	{
		reply = "257 " + dir + " is current directory.";
		return false;
	}// commandPWD() end

	String addTail(String s)
	{
		if (!s.endsWith("/"))
			s = s + "/";
		return s;
	}

	public FtpRequest(Socket s, int i)
	{
		csocket = s;
		id = i;
	}

	/**
	 * multithread fonction qui charge à analyser les commandes réçu par
	 * FtpServer
	 */
	public void run() //
	{
		String str = "";
		int parseResult;

		try
		{
			BufferedReader in = new BufferedReader(new InputStreamReader(
					csocket.getInputStream()));
			out = new PrintWriter(csocket.getOutputStream(), true);
			state = FtpState.FS_WAIT_LOGIN;
			boolean finished = false;
			while (!finished)
			{
				str = in.readLine();
				if (str == null)
					finished = true;
				else
				{
					parseResult = parseInput(str);
					System.out
							.println("Command:" + cmd + " Parameter:" + param);
					// System.out.println(remoteHost);
					// System.out.println(remotePort);
					System.out.print("->");
					switch (state)
					{
					case FtpState.FS_WAIT_LOGIN:
						finished = commandUSER();
						break;
					case FtpState.FS_WAIT_PASS:
						finished = commandPASS();
						break;
					case FtpState.FS_LOGIN:
					{
						switch (parseResult)
						{
						case -1:
							errCMD();
							break;
						case 3:
							finished = commandCDUP();
							break;
						case 4:
							finished = commandCWD();
							break;
						case 5:
							finished = commandQUIT();
							break;
						case 6:
							finished = commandRETR();
							break;
						case 7:
							finished = commandSTOR();
							break;
						case 8:
							finished = commandPWD();
							break;
						case 9:
							finished = commandLIST();
							break;
						case 10:
							finished = commandPORT();
							break;
						}// switch(parseResult) end
					}// case FtpState.FS_LOGIN: end
						break;

					}// switch(state) end
				} // else
				out.println(reply);
			} // while
			csocket.close();
		} // try
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
