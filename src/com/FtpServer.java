package com;

import java.io.*;
import java.net.*;
import java.util.*;

/**
 * niveau du serveur de FTP
 * 
 * @author JIN Benli et ZHAO Xuening
 * 
 */
public class FtpServer
{

	private int counter; // serveur compteur qui enregiste le nombre total de
							// requete
	public static String initDir; // repertoire initial ou on lance notre prog
	public static ArrayList users = new ArrayList(); // état des user courant
	public static ArrayList usersInfo = new ArrayList(); // état détail des user

	public FtpServer()
	{
		// Thread spécial pour récevoir les requetes
		FtpConsole fc = new FtpConsole();
		fc.start();

		// UserInfo
		loadUsersInfo();

		// Counter pour accueil le nème user
		int counter = 1;
		int i = 0;
		try
		{

			// écouter les demandes de connexion sur un port TCP > 1023
			ServerSocket s = new ServerSocket(35001);
			for (;;)
			{
				//
				Socket incoming = s.accept();
				BufferedReader in = new BufferedReader(new InputStreamReader(
						incoming.getInputStream()));
				PrintWriter out = new PrintWriter(incoming.getOutputStream(),
						true);
				out.println("220 Service ready for new user," + counter);

				// create thread pour nouveau user
				FtpRequest h = new FtpRequest(incoming, i);
				h.start();

				// ajouter dans user courant
				users.add(h);
				counter++;
				i++;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

	} // FtpServer() end

	/**
	 * lire un fichier de configuration local, les infos de user sont enregistré
	 * dans ce fichier
	 */
	public void loadUsersInfo() // lire info de user.cfg
	{
		String s = "user.cfg";
		int p1 = 0;
		int p2 = 0;

		// prendre tous les info de fichier à liste de UserInfo
		if (new File(s).exists())
		{
			try
			{
				BufferedReader fin = new BufferedReader(new InputStreamReader(
						new FileInputStream(s)));
				String line;
				String field;

				int i = 0;
				while ((line = fin.readLine()) != null) // user.cfg lire par
														// ligne
				{
					// System.out.println(line);
					UserInfo tempUserInfo = new UserInfo();
					p1 = 0;
					p2 = 0;
					i = 0;
					while ((p2 = line.indexOf("|", p1)) != -1)
					{
						field = line.substring(p1, p2);
						p2 = p2 + 1;
						p1 = p2;
						switch (i)
						// split par espace
						{
						case 0:
							tempUserInfo.user = field;
							// System.out.println(tempUserInfo.user);
							break;
						case 1:
							tempUserInfo.password = field;
							// System.out.println(tempUserInfo.password);
							break;
						case 2:
							tempUserInfo.workDir = field;
							// System.out.println(tempUserInfo.workDir);
							break;
						}
						i++;
					} // while((p2 = line.indexOf("|",p1))!=-1) end
					usersInfo.add(tempUserInfo);
				}// while((line = fin.readLine())!=null) end
				fin.close();
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}// if(new File(s).exists()) end
	}// loadUsersInfo() end

	/** main fonction */
	public static void main(String[] args)
	{
		if (args.length != 0)
		{
			initDir = args[0];
		}
		else
		{
			initDir = "/tmp";
		}
		FtpServer ftpServer = new FtpServer();

	} // main end
}