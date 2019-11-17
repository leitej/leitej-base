/*******************************************************************************
 * Copyright Julio Leite
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package leitej.net.ftp;

/**
 *
 * @author Julio Leite
 */
enum CommandEnum {

	/* ACCESS CONTROL */
	USER, // send username
	PASS, // send password
	ACCT, // send account information
	CWD, // change working directory
	CDUP, // CWD to the parent of the current directory
	SMNT, // structure Mount
	REIN, // reinitialize the connection
	QUIT, // terminate the connection

	/* TRANSFER PARAMETERS */
	PORT, // open a data port
	PASV, // enter passive mode
	TYPE, // set transfer type
	STRU, // set file transfer structure
	MODE, // set transfer mode

	/* SERVICE COMMANDS */
	RETR, // retrieve a remote file
	STOR, // store a file on the remote host
	STOU, // store a file uniquely
	APPE, // append to a remote file
	ALLO, // the number of bytes of storage to be reserved for the file
	REST, // file transfer is to be restarted
	RNFR, // rename from
	RNTO, // rename to
	ABOR, // abort a file transfer
	DELE, // delete a remote file
	RMD, // remove a remote directory
	MKD, // make a remote directory
	PWD, // print working directory
	LIST, // list remote files
	NLST, // name list of remote directory
	SITE, // site-specific commands
	SYST, // return system type
	STAT, // return server status
	HELP, // return help on using the server
	NOOP, // do nothing

	/* SPECIAL COMMANDS */
//	MDTM,	//return the modification time of a file
//	SIZE,	//return the size of a file
//	FEAT,	//list all new FTP features that the server supports beyond those described in RFC 959
	OPTS; // This command allows an FTP client to define a parameter that will be used by
			// a subsequent command

}
