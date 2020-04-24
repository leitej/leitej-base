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

import leitej.exception.ImplementationLtRtException;

/**
 *
 * @author Julio Leite
 */
public enum CodeEnum {
	_110, // Restart marker reply
	_120, // Service ready in nnn minutes
	_125, // Data connection already open; transfer starting
	_150, // File status okay; about to open data connection
	_200, // Command okay
	_202, // Command not implemented, superfluous at this site
	_211, // System status, or system help reply
	_212, // Directory status
	_213, // File status
	_214, // Help message
	_215, // NAME system type
	_220, // Service ready for new user
	_221, // Service closing control connection. Logged out if appropriate
	_225, // Data connection open; no transfer in progress
	_226, // Closing data connection. Requested file action successful (for example, file
			// transfer or file abort)
	_227, // Entering Passive Mode (h1,h2,h3,h4,p1,p2)
	_230, // User logged in, proceed
	_250, // Requested file action okay, completed
	_257, // Pathname created
	_331, // User name okay, need password
	_332, // Need account for login
	_350, // Requested file action pending further information
	_421, // Service not available, closing control connection. This may be a reply to any
			// command if the service knows it must shut down
	_425, // Can't open data connection
	_426, // Connection closed; transfer aborted
	_450, // Requested file action not taken. File unavailable (e.g., file busy)
	_451, // Requested action aborted: local error in processing
	_452, // Requested action not taken. Insufficient storage space in system
	_500, // Syntax error, command unrecognized. This may include errors such as command
			// line too long
	_500_nps, // Can't establish data connection: no PORT specified
	_501, // Syntax error in parameters or arguments
	_502, // Command not implemented
	_503, // Bad sequence of commands
	_503_luf, // Login with USER first
	_504, // Command not implemented for that parameter
	_530, // Not logged in
	_532, // Need account for storing files
	_550, // Requested action not taken. File unavailable (e.g., file not found, no
			// access)
	_550_ned, // Nonexistent directory
	_550_ind, // Is not a directory
	_550_nef, // Nonexistent file
	_550_cndf, // Could not delete file
	_550_cnrd, // Could not remove directory
	_550_fe, // File exists
	_550_dcnc, // Directory could not be created
	_550_npf, // Not a plain file
	_550_fel, // File exists in that location
	_550_cwf, // Can't write to file
	_551, // Requested action aborted: page type unknown
	_552, // Requested file action aborted. Exceeded storage allocation (for current
			// directory or dataset)
	_553; // Requested action not taken. File name not allowed

	int getCode() {
		switch (this) {
		case _110:
			return 110;
		case _120:
			return 120;
		case _125:
			return 125;
		case _150:
			return 150;
		case _200:
			return 200;
		case _202:
			return 202;
		case _211:
			return 211;
		case _212:
			return 212;
		case _213:
			return 213;
		case _214:
			return 214;
		case _215:
			return 215;
		case _220:
			return 220;
		case _221:
			return 221;
		case _225:
			return 225;
		case _226:
			return 226;
		case _227:
			return 227;
		case _230:
			return 230;
		case _250:
			return 250;
		case _257:
			return 257;
		case _331:
			return 331;
		case _332:
			return 332;
		case _350:
			return 350;
		case _421:
			return 421;
		case _425:
			return 425;
		case _426:
			return 426;
		case _450:
			return 450;
		case _451:
			return 451;
		case _452:
			return 452;
		case _500:
		case _500_nps:
			return 500;
		case _501:
			return 501;
		case _502:
			return 502;
		case _503:
		case _503_luf:
			return 503;
		case _504:
			return 504;
		case _530:
			return 530;
		case _532:
			return 532;
		case _550:
		case _550_ned:
		case _550_ind:
		case _550_nef:
		case _550_cndf:
		case _550_cnrd:
		case _550_fe:
		case _550_dcnc:
		case _550_npf:
		case _550_fel:
		case _550_cwf:
			return 550;
		case _551:
			return 551;
		case _552:
			return 552;
		case _553:
			return 553;
		default:
			throw new ImplementationLtRtException(this.toString());
		}
	}

	public String getMessageKey() {
		switch (this) {
		case _110:
			return "110 Restart marker reply";
		case _120:
			return "120 Service ready in #0 minutes";
		case _125:
			return "125 Data connection already open; transfer starting";
		case _150:
			return "150 File status okay; about to open data connection";
		case _200:
			return "200 Command okay";
		case _202:
			return "202 Command not implemented, superfluous at this site";
		case _211:
			return "211 System status, or system help reply";
		case _212:
			return "212 Directory status";
		case _213:
			return "213 File status";
		case _214:
			return "214 Help message";
		case _215:
			return "215 NAME system type";
		case _220:
			return "220 Service ready for new user";
		case _221:
			return "221 Service closing control connection. Logged out if appropriate";
		case _225:
			return "225 Data connection open; no transfer in progress";
		case _226:
			return "226 Closing data connection. Requested file action successful (for example, file transfer or file abort)";
		case _227:
			return "227 Entering Passive Mode (#0)";
		case _230:
			return "230 User logged in, proceed";
		case _250:
			return "250 Requested file action okay, completed";
		case _257:
			return "257 Pathname created";
		case _331:
			return "331 User name okay, need password";
		case _332:
			return "332 Need account for login";
		case _350:
			return "350 Requested file action pending further information";
		case _421:
			return "421 Service not available, closing control connection. This may be a reply to any command if the service knows it must shut down";
		case _425:
			return "425 Can't open data connection";
		case _426:
			return "426 Connection closed; transfer aborted";
		case _450:
			return "450 Requested file action not taken. File unavailable (e.g., file busy)";
		case _451:
			return "451 Requested action aborted: local error in processing";
		case _452:
			return "452 Requested action not taken. Insufficient storage space in system";
		case _500:
			return "500 Syntax error, command unrecognized. This may include errors such as command line too long";
		case _500_nps:
			return "500 Can't establish data connection: no PORT specified";
		case _501:
			return "501 Syntax error in parameters or arguments";
		case _502:
			return "502 Command not implemented";
		case _503:
			return "503 Bad sequence of commands";
		case _503_luf:
			return "503 Login with USER first";
		case _504:
			return "504 Command not implemented for that parameter";
		case _530:
			return "530 Not logged in";
		case _532:
			return "532 Need account for storing files";
		case _550:
			return "550 Requested action not taken. File unavailable (e.g., file not found, no access)";
		case _550_ned:
			return "550 Nonexistent directory";
		case _550_ind:
			return "550 Is not a directory";
		case _550_nef:
			return "550 Nonexistent file";
		case _550_cndf:
			return "550 Could not delete file";
		case _550_cnrd:
			return "550 Could not remove directory";
		case _550_fe:
			return "550 File exists";
		case _550_dcnc:
			return "550 Directory could not be created";
		case _550_npf:
			return "550 Not a plain file";
		case _550_fel:
			return "550 File exists in that location";
		case _550_cwf:
			return "550 Can't write to file";
		case _551:
			return "551 Requested action aborted: page type unknown";
		case _552:
			return "552 Requested file action aborted. Exceeded storage allocation (for current directory or dataset)";
		case _553:
			return "553 Requested action not taken. File name not allowed";
		default:
			throw new ImplementationLtRtException(this.toString());
		}
	}

}
