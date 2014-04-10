package com;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * consonle pour le serveur, les commandes nécessarie dans serveur pour ajouter
 * supprimer lister des clients, fermer le serveur
 * 
 * @author JIN Benli et ZHAO Xuening
 * 
 */
public class FtpConsole extends Thread //
{
	BufferedReader cin;
	String conCmd;
	String conParam;

	int consoleQUIT()
	{
		System.exit(0);
		return 0;
	}// consoleQUIT() end

	boolean consoleLISTUSER() // LISTUSER
	{
		System.out.println("username \t\t workdirectory");
		for (int i = 0; i < FtpServer.usersInfo.size(); i++)
		{
			System.out.println(((UserInfo) FtpServer.usersInfo.get(i)).user
					+ " \t\t\t "
					+ ((UserInfo) FtpServer.usersInfo.get(i)).workDir);
		}
		return false;
	}// consoleLISTUSER() end

	boolean consoleLIST() // List IP
	{
		int i = 0;
		for (i = 0; i < FtpServer.users.size(); i++)
		{
			System.out.println((i + 1)
					+ ":"
					+ ((FtpRequest) (FtpServer.users.get(i))).user
					+ " From "
					+ ((FtpRequest) (FtpServer.users.get(i))).csocket
							.getInetAddress().toString());
		}

		return false;
	}// consoleLIST() end

	boolean validateUserName(String s) //
	{
		for (int i = 0; i < FtpServer.usersInfo.size(); i++)
		{
			if (((UserInfo) FtpServer.usersInfo.get(i)).user.equals(s))
				return false;
		}
		return true;
	}// validateUserName() end

	boolean consoleADDUSER() // ajouter des users
	{
		System.out.print("please enter username:");
		try
		{
			cin = new BufferedReader(new InputStreamReader(System.in));
			UserInfo tempUserInfo = new UserInfo();
			String line = cin.readLine();
			if (line != "")
			{
				if (!validateUserName(line)) // valider l'existence du client
				{
					System.out.println("user " + line + " already exists!");
					return false; // si existe, on crée pas
				}
			}
			else
			{
				System.out.println("username cannot be null!");
				return false;
			}
			tempUserInfo.user = line;
			System.out.print("enter password :");
			line = cin.readLine();
			if (line != "")
				tempUserInfo.password = line;
			else
			{
				System.out.println("password cannot be null!");
				return false;
			}
			System.out.print("enter the initial directory: ");
			line = cin.readLine();
			if (line != "")
			{
				File f = new File(line);
				if (!f.exists())
					f.mkdirs();
				tempUserInfo.workDir = line;
			}
			else
			{
				System.out.println("the directory cannot be null!");
				return false;
			}
			FtpServer.usersInfo.add(tempUserInfo);
			saveUserInfo();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			return false;
		}
		return false;
	}// consoleADDUSER() end

	void saveUserInfo() // enregister dans user.cfg
	{
		String s = "";
		try
		{
			BufferedWriter fout = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream("user.cfg")));
			for (int i = 0; i < FtpServer.usersInfo.size(); i++)
			{
				s = ((UserInfo) FtpServer.usersInfo.get(i)).user + "|"
						+ ((UserInfo) FtpServer.usersInfo.get(i)).password
						+ "|" + ((UserInfo) FtpServer.usersInfo.get(i)).workDir
						+ "|";
				fout.write(s); // username,password,"|"
				fout.newLine();
			}
			fout.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}// saveUserInfo() end

	boolean consoleDELUSER() //
	{
		String s = "";
		// System.out.println(conParam);
		if (conParam.equals(""))
		{
			System.out.println("usage:deluser username");
			return false; //
		}
		for (int i = 0; i < FtpServer.usersInfo.size(); i++)
		{
			s = ((UserInfo) FtpServer.usersInfo.get(i)).user;
			if (s.equals(conParam))
			{
				System.out.println("User " + conParam + " deleted");
				FtpServer.usersInfo.remove(i);
				saveUserInfo();
				return false;
			}
		}
		System.out.println("User " + conParam + " not exists");
		return false;

	}// consoleDELUSER() end

	boolean consoleHELP() // Help
	{
		if (conParam.equals(""))
		{
			System.out.println("adduser :add new user");
			System.out.println("deluser <username> :delete a user");
			System.out.println("quit  :quit");
			System.out.println("list  :list all user connect to server");
			System.out.println("listuser : list all account of this server");
			System.out.println("help :show  this help");
		}
		else if (conParam.equals("adduser"))
			System.out.println("adduser :add new user");
		else if (conParam.equals("deluser"))
			System.out.println("deluser <username> :delete a user");
		else if (conParam.equals("quit"))
			System.out.println("quit  :quit");
		else if (conParam.equals("list"))
			System.out.println("list  :list all user connect to server");
		else if (conParam.equals("listuser"))
			System.out.println("listuser : list all account of this server");
		else if (conParam.equals("help"))
			System.out.println("help :show  this help");
		else
			return false;
		return false;

	}// consoleHELP() end

	boolean consoleERR() // "bad command!"
	{
		System.out.println("bad command!");
		return false;
	}// consoleERR() end

	public FtpConsole() // "ftp server started!"
	{
		System.out.println("ftp server started!");
		cin = new BufferedReader(new InputStreamReader(System.in));
	}

	public void run() //
	{
		boolean ok = false;
		String input = "";
		while (!ok)
		{
			System.out.print("->");
			try
			{
				input = cin.readLine();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			switch (parseInput(input))
			//
			{
			case 1:
				consoleQUIT();
				break;
			case 8:
				ok = consoleLISTUSER();
				break;
			case 0:
				ok = consoleLIST();
				break;
			case 2:
				ok = consoleADDUSER();
				break;
			case 3:
				ok = consoleDELUSER();
				break;
			case 7:
				ok = consoleHELP();
				break;
			case -1:
				ok = consoleERR();
				break;
			}
		}// while end
	}// run() end

	int parseInput(String s) //
	{
		String upperCmd;
		int p = 0;
		conCmd = "";
		conParam = "";
		p = s.indexOf(" ");
		if (p == -1)
			conCmd = s;
		else
			conCmd = s.substring(0, p); // conCmd

		if (p >= s.length() || p == -1)
			conParam = "";
		else
			conParam = s.substring(p + 1, s.length());
		upperCmd = conCmd.toUpperCase();

		//

		if (upperCmd.equals("LIST"))
			return 0;
		else if (upperCmd.equals("QUIT") || upperCmd.equals("EXIT"))
			return 1;
		else if (upperCmd.equals("ADDUSER"))
			return 2;
		else if (upperCmd.equals("DELUSER"))
			return 3;
		else if (upperCmd.equals("EDITUSER"))
			return 4;
		else if (upperCmd.equals("ADDDIR"))
			return 5;
		else if (upperCmd.equals("REMOVEDIR"))
			return 6;
		else if (upperCmd.equals("HELP") || upperCmd.equals("?"))
			return 7;
		else if (upperCmd.equals("LISTUSER"))
			return 8;
		return -1;
	}// parseInput end
}
