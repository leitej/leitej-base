/*******************************************************************************
 * Copyright (C) 2011 Julio Leite
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

package leitej.locale.message;

/**
 * Hard-coded translation.
 * 
 * @author  Julio Leite
 */
final class MessagesEng implements MessagesItf {

	final static String[] MESSAGES = {
	"#####################################",
	"# Message file for english language #",
	"#####################################",
		"key_message		=Output text.",
		"Key_use_parameter	=Output #0 and #1",
		"lt.NewInstance		=new instance",
		"lt.NewInstanceDeny	=new instance (not predictable!)",
		"lt.Init				=initialized",
		"lt.End				=ended",
		"lt.FaultDetected		=Fault detected",
		"lt.Closed				=closed",
		"lt.FileCreated		=Created file: #0",
	"#EXCEPTION",
		"lt.CausedBy	=Caused by: #0",
		"lt.AtMethod	=at",
	"#LOG",
		"lt.LogClose				=Logger closing",
		"lt.LogAppendErrorOpen		=Can't open an appender log",
		"lt.FileNull				=try open file 'null'",
	"#THREAD",
		"lt.ThreadAlreadyWork					=Only give work when not digesting other work! (#0)",
		"lt.ThreadNormalizerRule				=This method can only be invoked by the thread normalizer",
		"lt.ThreadRescuerRule					=This method can only be invoked by the thread rescuer",
		"lt.ThreadExecutionerRule				=This method can only be invoked by the thread executioner",
		"lt.ThreadRescuerStop					=stopping the rescuer thread",
		"lt.ThreadExecutionerStop				=stopping the executioner thread",
		"lt.ThreadNew							=Instantiated new thread #0",
		"lt.ThreadNewDeny						=Can't create more threads to pool (max: #0 - all working!)",
		"lt.ThreadAlreadyClose					=Only give work before close the pool!",
		"lt.ThreadTaskEnter					=taskStruct: #0, Enter queue todoSet size: #1",
		"lt.ThreadTaskLeave					=taskStruct: #0, Leave queue todoSet size: #1",
		"lt.ThreadTaskWork						=put to work #0 at #1",
		"lt.ThreadTaskLeaveAtypically			=XThreadData '#0' atypically leave queue",
		"lt.ThreadOffer						=Thread id: #0 recovering to offer",
		"lt.ThreadTaskNull						=Received a null when trying to get task that worked on thread id: #0 (this shouldn't happen)",
		"lt.ThreadThreadNull					=Received a null when trying to get thread id: #0 (this shouldn't happen)",
		"lt.ThreadWorkOnNull					=The parameter xThreadData can't be null",
		"lt.ThreadEmbebedPoolNull				=The parameter myPool can't be null",
		"lt.ThreadPoolNameNull					=The parameter name can't be null",
		"lt.AtypicallyStoppedThreadDetected	=Thread '#0' stopped atypically with unknown reason",
	"#UTIL",
		"lt.AgnArgNull						=object is null. if method is static then send the class!!!",
		"lt.QueueCloseOffer				=Already closed - can't offer!",
		"lt.QueueClosePool					=Already closed - can't poll!",
		"lt.SWatch							=Stopwatch",
		"lt.SWatchTable					=table",
		"lt.SWatchNone						=none",
		"lt.SWatchTotal					=Total",
		"lt.BlockExit						=Blocked: #0 - For thread: #1",
		"lt.BlockInvoke					=Blocking invoke: #0",
		"lt.ConsolePause					=Paused. Hit Enter to continue ...",
		"lt.FileLengthTooBig				=File(#0) length too big for an array of bytes",
		"lt.IncompleteReadFile				=Could not completely read file(#0)",
		"lt.ConsoleNull					=The system did not give me a console",
		"lt.IllegalNegativeArg				=Can't calculate with negative argument",
		"lt.MoneyNegativeIndex				=IndexUnit is negative",
		"lt.MoneyArgInvalidFormat			=Value is not a valid representation of euro format",
		"lt.MoneyArgTooHigh				=Value is too high for the type long",
		"lt.NegativeValue					=The #0 argument is less then 0",
		"lt.CNFTenuredSpace				=Could not find tenured space",
		"lt.CNFTenuredSpaceSet				=Percentage: #0",
		"lt.CNFTenuredSpaceWarn			=Notify #0 listeners",
		"lt.HMUWWrongPercentage			=Percentage not in range (0.0 < x=#0 <= 1.0)",
		"lt.CSBytesExceeded				=Passed the limit imposed on the volume of data",
		"lt.STDWHookStarted				=Shutdown hook already started",
		"lt.OtherInstanceActive			=There are already an instance active",
		"lt.HexaInvalid					=Data has an invalid hex value",
	"#XML",
		"lt.XmlInvalid						=Invalid XML syntax",
		"lt.XmlInvalidStreamEnd			=Invalid XML syntax, unnexpected end of stream",
		"lt.XmlInvalidEndTag				=Invalid XML syntax, unnexpected end_tag '#0'",
		"lt.XmlInvalidTag					=Invalid XML tag '#0'",
		"lt.XmlInvalidElementName			=Invalid XML element name '#0'",
		"lt.XmlInvalidElementAfterRootEnd	=Invalid XML syntax, unnexpected tag after element root end!",
		"lt.XmlInvalidElementConstruct		=Invalid XML syntax, unnexpected end_tag_name '#0'",
		"lt.XmlOmParserAlreadyDone			=Already scanned !make new instance!",
		"lt.XmlOmInvalidSyntax				=Invalid XMLOM, expected open element '#0'",
		"lt.XmlOmInvalidData				=Invalid XMLOM, data parser, fail to set '#0' in object '#1'",
		"lt.XmlOmInvalidCommentLeaf		=Invalid XMLOM, leaf object can not have comment",
		"lt.XmlOmInvalidCommentArray		=Invalid XMLOM, array object can not have comment",
		"lt.XmlOmInvalidCommentLoopObj		=Invalid XMLOM, object refered by ID can not have comment",
		"lt.XmlOmInvalidCommentSyntax		=Invalid XMLOM, expected open element for the comments '#0'",
		"lt.XmlOmInvalidMapElementNull		=Invalid XMLOM syntax, map element can't be null '#0'",
		"lt.XmlOmArrayImplementBug			=Something wrong (Defined an array in 'ArrayElement.ARRAY_CLASS' element which isn't implemented!)",
		"lt.XmlOmIgnoredTag				=read map elements -> ignore tag '#0'",
		"lt.XmlOmInvalidSyntaxRootInit		=Invalid XML syntax, expected root_element '#0'",
		"lt.XmlOmInvalidSyntaxRootEnd		=Invalid XML syntax, expected end root_element '#0'",
		"lt.XmlOmInvalidValueByte			=Invalid byte - '#0'",
		"lt.XmlOmInvalidValueShort			=Invalid short - '#0'",
		"lt.XmlOmInvalidValueInt			=Invalid int - '#0'",
		"lt.XmlOmInvalidValueLong			=Invalid long - '#0'",
		"lt.XmlOmInvalidValueFloat			=Invalid float - '#0'",
		"lt.XmlOmInvalidValueDouble		=Invalid double - '#0'",
		"lt.XmlOmInvalidValueBoolean		=Invalid boolean - '#0'",
		"lt.XmlOmInvalidValueChar			=Invalid char - '#0'",
		"lt.XmlOmInvalidInvokeValueOf		=Invalid call '#0' from class '#1' with arg '#2'",
		"lt.XmlOmInvalidPrimitiveArray		=A primitive array can't have a null element",
		"lt.XmlOmMissesAttribClass			=Invalid XMLOM element without attribute type '#0'",
		"lt.XmlOmInvalidAttribClass		=Invalid XMLOM element attribute class '#0'",
		"lt.XmlOmInvalidAttribId			=Invalid value for attribute id '#0'",
		"lt.XmlOmUntrustedClass			=#0 is not considered a trusted class",
		"lt.XmlOmProducerAlreadyDone		=Already finalized the XML",
		"lt.XmlOmProcessObject				=processing object: #0",
		"lt.XmlOmNullTrustClass			=Null is not a valid trust class name",
		"lt.XmlOmNotRemovingObject			=Can not remove object after write to stream",
	"#DB",
		"lt.MngDriverName					=Driver Name: #0",
		"lt.MngClosed						=Closed",
	"#LTM",
		"lt.LtmErase						=Erased All Long Term Memory"
	};

	@Override
	public String[] getMessages() {
		return MESSAGES;
	}

}
