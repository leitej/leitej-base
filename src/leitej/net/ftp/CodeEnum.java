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
			return "lt.FTPCode_110";
		case _120:
			return "lt.FTPCode_120";
		case _125:
			return "lt.FTPCode_125";
		case _150:
			return "lt.FTPCode_150";
		case _200:
			return "lt.FTPCode_200";
		case _202:
			return "lt.FTPCode_202";
		case _211:
			return "lt.FTPCode_211";
		case _212:
			return "lt.FTPCode_212";
		case _213:
			return "lt.FTPCode_213";
		case _214:
			return "lt.FTPCode_214";
		case _215:
			return "lt.FTPCode_215";
		case _220:
			return "lt.FTPCode_220";
		case _221:
			return "lt.FTPCode_221";
		case _225:
			return "lt.FTPCode_225";
		case _226:
			return "lt.FTPCode_226";
		case _227:
			return "lt.FTPCode_227";
		case _230:
			return "lt.FTPCode_230";
		case _250:
			return "lt.FTPCode_250";
		case _257:
			return "lt.FTPCode_257";
		case _331:
			return "lt.FTPCode_331";
		case _332:
			return "lt.FTPCode_332";
		case _350:
			return "lt.FTPCode_350";
		case _421:
			return "lt.FTPCode_421";
		case _425:
			return "lt.FTPCode_425";
		case _426:
			return "lt.FTPCode_426";
		case _450:
			return "lt.FTPCode_450";
		case _451:
			return "lt.FTPCode_451";
		case _452:
			return "lt.FTPCode_452";
		case _500:
			return "lt.FTPCode_500";
		case _500_nps:
			return "lt.FTPCode_500_nps";
		case _501:
			return "lt.FTPCode_501";
		case _502:
			return "lt.FTPCode_502";
		case _503:
			return "lt.FTPCode_503";
		case _503_luf:
			return "lt.FTPCode_503_luf";
		case _504:
			return "lt.FTPCode_504";
		case _530:
			return "lt.FTPCode_530";
		case _532:
			return "lt.FTPCode_532";
		case _550:
			return "lt.FTPCode_550";
		case _550_ned:
			return "lt.FTPCode_550_ned";
		case _550_ind:
			return "lt.FTPCode_550_ind";
		case _550_nef:
			return "lt.FTPCode_550_nef";
		case _550_cndf:
			return "lt.FTPCode_550_cndf";
		case _550_cnrd:
			return "lt.FTPCode_550_cnrd";
		case _550_fe:
			return "lt.FTPCode_550_fe";
		case _550_dcnc:
			return "lt.FTPCode_550_dcnc";
		case _550_npf:
			return "lt.FTPCode_550_npf";
		case _550_fel:
			return "lt.FTPCode_550_fel";
		case _550_cwf:
			return "lt.FTPCode_550_cwf";
		case _551:
			return "lt.FTPCode_551";
		case _552:
			return "lt.FTPCode_552";
		case _553:
			return "lt.FTPCode_553";
		default:
			throw new ImplementationLtRtException(this.toString());
		}
	}

}
