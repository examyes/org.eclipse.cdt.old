package com.ibm.debug.epdc;

////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 1997, 2001, 2002 International Business Machines Corporation. All rights reserved.
// This program and the accompanying materials are made available under the terms of
// the Common Public License which accompanies this distribution.
//
// com/ibm/debug/epdc/EPDC.java, java-epdc, eclipse-dev, 20011128
// Version 1.86.1.4 (last modified 11/28/01 16:23:05)
////////////////////////////////////////////////////////////////////////////////

/**
 * Note: This class/interface is part of an interim API that is still under
 * development and expected to change significantly before reaching stability.
 * It is being made available at this early stage to solicit feedback from
 * pioneering adopters on the understanding that any code that uses this API
 * will almost certainly be broken (repeatedly) as the API evolves.
 */

//
//
// EPDC - Constants!
//
//
public interface EPDC
{
        // Packet types:

        public static final int RequestPacket                   =0;
        public static final int ReplyPacket                     =1;
        public static final int ChangePacket                    =2;

   // constants for request and reply types

   public static final int Remote_BreakpointLocation       =1;
   public static final int Remote_Execute                  =2;
   public static final int Remote_Expression               =3;
   public static final int Remote_ExpressionDisable        =4;
   public static final int Remote_ExpressionEnable         =5;
   public static final int Remote_ExpressionFree           =6;
   public static final int Remote_ExpressionValueModify    =7;
   public static final int Remote_PartGet                  =8;
   public static final int Remote_PointerDeref             =9;
   public static final int Remote_EntrySearch             =10;
   public static final int Remote_Registers               =11; /* soon to be obsolete */
   public static final int Remote_RegistersFree           =12; /* soon to be obsolete */
   public static final int Remote_Stack                   =13;
   public static final int Remote_StackFree               =14;
   public static final int Remote_StackBuildView          =15;
   public static final int Remote_FilePathVerify          =16;
   public static final int Remote_PartOpen                =17;
   public static final int Remote_Storage2                =18;
   public static final int Remote_StorageEnablementSet    =19;
   public static final int Remote_StorageFree             =20;
   public static final int Remote_StorageRangeSet2        =21;
   public static final int Remote_StorageStyleSet         =22;
   public static final int Remote_StorageUpdate           =23;
   public static final int Remote_StringFind              =24;
   public static final int Remote_TerminatePgm            =25;
   public static final int Remote_ThreadFreeze            =26;
   public static final int Remote_ThreadThaw              =27;
   public static final int Remote_ViewsVerify             =28;
   public static final int Remote_Initialize_Debug_Engine =29;
   public static final int Remote_PreparePgm              =30;
   public static final int Remote_StartPgm                =31;
   public static final int Remote_BreakpointClear         =32;
   public static final int Remote_BreakpointDisable       =33;
   public static final int Remote_BreakpointEnable        =34;
   public static final int Remote_BreakpointEvent         =35;
   public static final int Remote_PartSet                 =36;
   public static final int Remote_ExpressionSubTree       =37;
   public static final int Remote_ExpressionSubTreeDelete =38;
   public static final int Remote_ExpressionRepTypeSet    =39;
   public static final int Remote_LocalVariable           =40;
   public static final int Remote_LocalVariableFree       =41;
   public static final int Remote_Terminate_Debug_Engine  =42;
   public static final int Remote_EntryWhere              =43;
   public static final int Remote_CommandLogExecute       =44;
   public static final int Remote_PrepareChild            =45;
   public static final int Remote_ProcessAttach           =46;
   public static final int Remote_ProcessDetach           =47;
   public static final int Remote_ProcessListGet          =48;
   public static final int Remote_ProcessAttach2          =49;
   public static final int Remote_ContextConvert          =50;
   public static final int Remote_BreakpointEntryAutoSet2 =51;
   public static final int Remote_ModuleAdd               =52;
   public static final int Remote_ModuleRemove            =53;
   public static final int Remote_Registers2              =54;
   public static final int Remote_RegistersEnablementSet  =55;
   public static final int Remote_RegistersFree2          =56;
   public static final int Remote_RegistersValueSet       =57;
   public static final int Remote_StackEnablementSet      =58;
   public static final int Remote_StackOpenStorage        =59;
   public static final int Remote_StackSetBreakpoint      =60;
   public static final int Remote_PassThru                =61;
   public static final int Remote_PassThruEnablementSet   =62;
   public static final int Remote_PassThruFree            =63;
   public static final int Remote_PassThruSendCommand     =64;
   public static final int Remote_ThreadInfoGet           =65;
   public static final int Remote_Version                 =66;


   public static final int Remote_StackView               =84; /* soon to be obsolete */
   public static final int Remote_Storage                 =85; /* soon to be obsolete */
   public static final int Remote_StorageAddressStyleSet  =86; /* soon to be obsolete */
   public static final int Remote_StorageDisable          =87; /* soon to be obsolete */
   public static final int Remote_StorageEnable           =88; /* soon to be obsolete */
   public static final int Remote_StorageRangeSet         =89; /* soon to be obsolete */
   public static final int Remote_StorageUnitStyleSet     =90; /* soon to be obsolete */

   /* DBD calls */
   public static final int Remote_DBD_Calls              =200;
   public static final int Remote_ClassDetailsGet        =201;
   public static final int Remote_ClassDetailsFree       =202;
   public static final int Remote_ClassPartGet           =203;
   public static final int Remote_Halt                   =204;
   public static final int Remote_CommandLog             =205;
   public static final int Remote_CommandLogFree         =206;
   public static final int Remote_CommandLogGetText      =207;
   public static final int Remote_CommandLogSearch       =208;
   public static final int Remote_TypesNumGet            =209;
   public static final int Remote_RepForTypeSet          =210;
   public static final int Remote_ContextQualGet         =211;
   public static final int Remote_ContextFromAddrGet     =212;
   public static final int Remote_ExceptionStatusChange  =213;
   public static final int Remote_StorageUsageCheckSet   =214;
   public static final int Remote_JobsListGet            =215;
   public static final int Remote_RegistersDetailsGet    =216;
   public static final int Remote_PMDebuggingModeSet     =217;
   public static final int Remote_StackDetailsGet        =218;
   public static final int Remote_ProcessDetailsGet      =219;
   public static final int Remote_PassThruDetailsGet     =220;
   public static final int Remote_BreakpointEntryAutoSet =221;
   public static final int Remote_EnvironmentDetailsGet  =222;
   public static final int Remote_EnvironmentSet         =223;
   public static final int Remote_FilePathSet            =224;
   public static final int Remote_ProcessActiveListGet   =225;
   public static final int Remote_ViewFileInfoSet        =226;
   public static final int Remote_ViewSearchPath         =227;
   public static final int Remote_GetStatusInfo          =228;
   public static final int Remote_GetEngineSettings      =229;
   public static final int Remote_PutEngineSettings      =230;

   // PM Debugging state

   public static final int InputUnlocked   =0;
   public static final int InputLocked     =1;

   // Platform IDs

   public static final short PLATFORM_ID_OS2       =1;
   public static final short PLATFORM_ID_MVS       =2;
   public static final short PLATFORM_ID_VM370     =3;
   public static final short PLATFORM_ID_AS400     =4;
   public static final short PLATFORM_ID_AIX       =5;
   public static final short PLATFORM_ID_NT        =6; // use for W95,98 and W2K as well
   public static final short PLATFORM_ID_JVM       =7;
   public static final short PLATFORM_ID_HPUX      =8;
   public static final short PLATFORM_ID_SUN       =9;
   public static final short PLATFORM_ID_LINUX     =10;
   public static final short LAST_PLATFORM_ID      =11;

   // Engine IDs

   public static final short BE_TYPE_IPMD         = 1;
   public static final short BE_TYPE_SLD          = 2;
   public static final short BE_TYPE_DBX          = 3;
   public static final short BE_TYPE_PICL         = 4;
   public static final short BE_TYPE_CEL          = 5;
   public static final short BE_TYPE_WILEY        = 6;
   public static final short BE_TYPE_JAVA_PICL    = 7;
   public static final short BE_TYPE_GDB		  = 8;
   public static final short LAST_BE_TYPE         = 9;

   /**
    * @deprecated Use BE_TYPE_JAVA_PICL instead
    */

   public final static byte PICL = 4;

   public final static int VERSION_OK = 0;
   public final static int VERSION_ERROR = 1;

   // Default Settings
   public final static int StorageUsageCheckEnable = 0x80000000;
   public final static int DebuggerBusyBoxEnable =   0x40000000;
   public final static int AutoSetEntryBkpEnable =   0x20000000;
   public final static int RunMinimizedEnable =      0x10000000; //added according to v306
   public final static int DateBkpEnable =           0x08000000;

   // PM Debugging Action
   public final static int NoPaint = 0;

   // PM Debugging Color
   public final static int Black   = 0;

   // PM Debugging Mode
   public final static int Synchronous = 0;

   // ProcessDetachActions
   public final static int ProcessRelease = 0;
   public final static int ProcessKeep    = 1;
   public final static int ProcessKill    = 2;


   // Language IDs

   public static final byte LANG_C          =1;
   public static final byte LANG_CPP        =2;
   public static final byte LANG_PLX86      =3;
   public static final byte LANG_PLI        =4;
   public static final byte LANG_RPG        =5;
   public static final byte LANG_COBOL      =6;
   public static final byte LANG_ALP_ASM    =7;
   public static final byte LANG_OPM_RPG    =8;
   public static final byte LANG_CL_400     =9;
   public static final byte LANG_JAVA       =10;  // @ASB
   public static final byte LANG_FORTRAN    =11;

   // Program Types (AS400)
   public static final int DefaultPgmType = 0x00000000;
   public static final int ServicePgm     = 0x80000000;

   // Module Types (AS400)
   public static final int ProgType400Default  = 0;
   public static final int ProgType400Service  = 1;
   public static final int ProgType400Java     = 2;


   public static final int    ExecRc_OK                     =0;   /* request complete                  */
   public static final int    ExecRc_Error                  =1;   /* request rejected                  */
   public static final int    ExecRc_NotInit                =2;   /* not initialized                   */
   public static final int    ExecRc_BadParm                =3;   /* bad parameter                     */
   public static final int    ExecRc_NoNameMatch            =4;   /* no variable names match pattern   */
   public static final int    ExecRc_InvalidPPid            =5;   /* invalid part id                   */
   public static final int    ExecRc_FileOpenErr            =6;   /* error on opening file             */
   public static final int    ExecRc_FileNotFound           =7;   /* specified file not found          */
   public static final int    ExecRc_UnRecogFileFm          =8;   /* file format not recognized        */
   public static final int    ExecRc_FileLoadErr            =9;   /* error in loading the file         */
   public static final int    ExecRc_PartNotInVwFile        =10;  /* part not in view file             */
   public static final int    ExecRc_ViewNotAvail           =11;  /* requested view not available      */
   public static final int    ExecRc_GotoOutOfBlock         =12;  /* goto out of block not allowed     */
   public static final int    ExecRc_TriggerBkp             =13;  /* activate breakpoint               */
   public static final int    ExecRc_BkptInRdOnlyMem        =14;  /* overlay HK not possible           */
   public static final int    ExecRc_NoFreqDataAvail        =15;  /* no frequency data available       */
   public static final int    ExecRc_ExpTooLarge            =16;  /* expression too large              */
   public static final int    ExecRc_TerminateDebugger     =126;  /* terminate debugger immediately    */
   public static final int    ExecRc_JobName               =127;  /* bad job name                      */
   public static final int    ExecRc_ProgName              =128;  /* bad program name                  */
   public static final int    ExecRc_CmdName               =129;  /* bad command name                  */
   public static final int    ExecRc_BadExpr               =130;  /* bad expression                    */
   public static final int    ExecRc_BadValue              =131;  /* bad expression value              */
   public static final int    ExecRc_BadType               =132;  /* bad data type for expression      */
   public static final int    ExecRc_BadSelector           =133;  /* bad data item selector            */
   public static final int    ExecRc_BadContext            =134;  /* bad exec context                  */
   public static final int    ExecRc_NoNew                 =136;  /* no new items                      */
   public static final int    ExecRc_NoChangedParts        =137;  /* no changed parts                  */
   public static final int    ExecRc_PartialTreeRtd        =138;  /* only a partial tree returned      */
   public static final int    ExecRc_NoClue                =239;  /* stopped in unknown program        */
   public static final int    ExecRc_NewProg               =240;  /* new program started               */
   public static final int    ExecRc_PgmExcep              =241;  /* exception in program              */
   public static final int    ExecRc_AttnInt               =242;  /* attention interrupt               */
   public static final int    ExecRc_ParentTerminated      =243;  /* parent terminated before child    */
   public static final int    ExecRc_OpNotSupported        =255;  /* request not supported             */
   public static final int    ExecRc_FindFailed            =300;  /* find failed                       */
   public static final int    ExecRc_ThreadBlocked         =301;  /* thread is blocked                 */
   public static final int    ExecRc_NoMemory              =302;  /* out of memory                     */
   public static final int    ExecRc_BadStack              =303;  /* invalid stack or unable to grow   */
   public static final int    ExecRc_PageReadError         =304;  /* page read error                   */
   public static final int    ExecRc_BadData               =305;  /* invalid data in EStdString        */
   public static final int    ExecRc_BadLineNum            =306;  /* invalid line number               */
   public static final int    ExecRc_BadBrkId              =307;  /* invalid breakpoint id             */
   public static final int    ExecRc_BadBrkType            =308;  /* invalid breakpoint type           */
   public static final int    ExecRc_BadBrkAction          =309;  /* invalid breakpoint action         */
   public static final int    ExecRc_BadBrkEvery           =310;  /* invalid every count for breakpoint*/
   public static final int    ExecRc_BadOperand            =311;  /* invalid expression operand        */
   public static final int    ExecRc_BadOperator           =312;  /* invalid expression operator       */
   public static final int    ExecRc_BadMonId              =313;  /* invalid monitor id                */
   public static final int    ExecRc_BadThdId              =314;  /* invalid thread id                 */
   public static final int    ExecRc_BadAddress            =315;  /* invalid or illegal address        */
   public static final int    ExecRc_Exception             =316;  /* non-continuable exception         */
   public static final int    ExecRc_DupBrkPt              =317;  /* duplicate breakpoint              */
   public static final int    ExecRc_InitFailure           =318;  /* failure in initialization         */
   public static final int    ExecRc_BadStorParm           =319;  /* bad storage monitor parameter     */
   public static final int    ExecRc_InvalidSrcFileIndex   =320;  /* invalid source file index         */
   public static final int    ExecRc_BadEntryId            =321;  /* invalid entry id                  */
   public static final int    ExecRc_AmbiguousContext      =322;  /* ambiguous context                 */
   public static final int    ExecRc_BPonStack             =323;  /* change address BP on stack        */
   public static final int    ExecRc_BadDllName            =324;  /* invalid DLL or file name given    */
   public static final int    ExecRc_StkPtrBad             =325;  /* invalid stack pointer             */
   public static final int    ExecRc_BadBrkFrom            =326;  /* invalid from count for breakpoint */
   public static final int    ExecRc_BadBrkTo              =327;  /* invalid to   count for breakpoint */
   public static final int    ExecRc_DllNotFound           =328;  /* DLL was not found                 */
   public static final int    ExecRc_InvalidStringFormat   =329;  /* Engine does not support UI's string encoding */

   /* Why stop constants
    * Used by  ERepExecute, ERepStartPgm, ERepProcessAttach, ERepProcessAttach2,
    *          ERepProcessDetach
    */
   /** if NOGO requested                 */
   public static final short    Why_none                      =0;
   /** stopped at breakpoint             */
   public static final short    Why_break                     =1;
   /** watchpoint                        */
   public static final short    Why_Watchpoint                =2;
   /** execution complete                */
   public static final short    Why_done                      =3;
   /** child process execution complete  */
   public static final short    Why_ChildDone                 =4;
   /** notication - exception            */
   public static final short    Why_PgmExcept                 =5;
   /** no handler for program exception  */
   public static final short    Why_PgmExcept_Nohandler       =6;
   /** no retry for program exception    */
   public static final short    Why_PgmExcept_NoRetry         =7;
   /** only run for program exception    */
   public static final short    Why_PgmExcept_OnlyRun         =8;
   /** storage usage check occurred      */
   public static final short    Why_StorageUsageCheck         =9;
   /** resource interlock occurred       */
   public static final short    Why_ResourceInterlock         =10;
   /** execution complete - did not stop */
   public static final short    Why_DoneNoStop                =11;
   /** process changed                   */
   public static final short    Why_ProcessChanged            =12;
   /** process execution complete        */
   public static final short    Why_ProcessDone               =13;
   /** execution complete - close debug  */
   public static final short    Why_DoneCloseDebugger         =14;
   /** notification - fork               */
   public static final short    Why_PgmForked                 =15;
   /** notification - exec               */
   public static final short    Why_PgmExeced                 =16;
   /** remote procedure call             */
   public static final short    Why_RemoteCall                =126;
   /** other ...                         */
   public static final short    Why_Other                     =127;
   /** whymsg field present              */
   public static final short    Why_msg                       =128;

   /* How To Execute constants */
   public static final byte    Exec_Step                     =(byte)1;   /* simple step                       */
   public static final byte    Exec_StepOver                 =(byte)2;   /* step 'over' calls                 */
   public static final byte    Exec_StepInto                 =(byte)3;   /* step 'into' calls                 */
   public static final byte    Exec_StepReturn               =(byte)4;   /* step until we return N levels     */
   public static final byte    Exec_Go                       =(byte)5;   /* go until some break               */
   public static final byte    Exec_GoBypass                 =(byte)6;   /* restore exception execution       */
   public static final byte    Exec_GoTo                     =(byte)7;   /* go to a point in the program      */
   public static final byte    Exec_RunToCursor              =(byte)8;   /* run specific thread to cursor     */
   public static final int    Exec_Undo                     =9;   /* undo the last execution           */
   public static final byte    Exec_GoException              =(byte)10;  /* resume exception execution        */
   public static final byte    Exec_GoExceptionRun           =(byte)11;  /* resume exception execution/go     */
   public static final byte    Exec_ForkFollowChild          =(byte)12;  /* switch to child after fork        */
   public static final byte    Exec_ForkFollowParent         =(byte)13;  /* stay with parent after fork       */

   // BREAKPOINT CONSTANTS FOR EReqBreakpointLocation and EReqBreakpointEvent
   // Breakpoint actions
   public static final short    SetBkp                        =(short)1;   /* set breakpoint                   */
   public static final int    ClearBkp                      =2;   /* clear breakpoint        NOT USED */
   public static final short    ReplaceBkp                    =(short)3;   /* replace breakpoint               */
   public static final short    EnableBkp                     =(short)4;   /* enable breakpoint       NOT USED */
   public static final int    DisableBkp                    =5;   /* disable breakpoint      NOT USED */

   // Breakpoint types (also used for breakpoint change packet)
   public static final short    LineBkpType                   =(short)0;   /* line breakpoint                  */
   public static final int    AddressBkpType                =1;   /* address breakpoint               */
   public static final short    EntryBkpType                  =(short)2;   /* entry breakpoint                 */
   public static final int    ExitBkpType                   =3;   /* exit breakpoint         NOT USED */
   public static final int    ExpressionBkpType             =4;   /* expression breakpoint   NOT USED */
   public static final int    LoadBkpType                   =5;   /* load breakpoint                  */
   public static final int    ChangeAddrBkpType             =6;   /* change address breakpoint        */
   public static final int    ChangeVarBkpType              =7;   /* change variable bkp     NOT USED */
   public static final int    OccurrenceBkpType             =8;   /* occurrence breakpoint   NOT USED */
   public static final int    TerminationBkpType            =9;   /* termination breakpoint  NOT USED */
   public static final int    EventBkpType                  =10;  /* event breakpoint (DE-specific)   */
   public static final int    StmtBkpType                   =11;  /* statement breakpoint             */

   // Breakpoint attributes
   public static final short    BkpEnable               =(short)0x8000;    /* breakpoint enabled               */
   public static final short    BkpDefer                =(short)0x4000;    /* breakpoint deferred              */
   public static final short    BkpCaseSensitive        =(short)0x2000;    /* breakpoint info case sensitive   */
   public static final short    BkpReadOnly             =(short)0x0200;    /* breakpoint cannot be modified    */

   // Entry Search attributes
   public static final byte   CaseSensitive           =0x00;      /* Search Entry case sensitive      */
   public static final byte   CaseInsensitive         =0x01;      /* Search Entry case insensitive    */

   public static final int    StringFindCaseSensitive = 0x80000000;

   // Alignment constants, used by ERepGetPassThruColumns, ERepGetProcessColumns, ERepStackDetailsGet
   public static final int    Centered                =0;         /* centered                         */
   public static final int    LeftJustified           =1;         /* left justified                   */
   public static final int    RightJustified          =2;         /* right justified                  */

   // EReqExpression
   // Variable: MonAttributes
   public static final byte MonEnable                  = (byte)0x80;     /* monitor enabled                  */
   public static final byte MonDefer                   = (byte)0x40;     /* defer monitor */

   // Standard Expression Node Types
   public static final int StdOperatorNode            = 1;        /* standard operator node  NOT USED */
   public static final int StdOperandNode             = 2;        /* standard operand node   NOT USED */
   public static final short StdScalarNode            = (short)3; /* standard scalar node             */
   public static final short StdStructNode            = (short)4; /* standard structure node          */
   public static final short StdArrayNode             = (short)5; /* standard array node              */
   public static final int StdDataItemListNode        = 6;        /* standard data item node NOT USED */
   public static final int StdByteListNode            = 7;        /* standard byte list node NOT USED */
   public static final short StdPointerNode           = (short)8; /* standard pointer node   NOT USED */
   public static final int StdTypeCastNode            = 9;        /* standard typecast node  NOT USED */
   public static final int StdAttributeNode           = 10;       /* standard attribute node NOT USED */
   public static final short StdClassNode             = (short)11;       /* standard class node              */
   public static final int StdDescClassNode           = 12;       /* standard desc class node         */
   public static final int StdDescBaseClassNode       = 13;       /* standard base class node         */
   public static final int StdDescDataMemNode         = 14;       /* standard data member node        */
   public static final int StdDescFriendClassNode     = 15;       /* standard friend class node       */
   public static final int StdDescMemberFcnNode       = 16;       /* standard member function node    */
   public static final int StdDescFriendFcnNode       = 17;       /* standard friend function node    */

   // Attribute constants for EStdClassItem
   public static final int StdClassIsBaseClass        = 0x80;     /* class is base class              */
   public static final int StdClassIsVirtual          = 0x40;     /* class is virtual                 */

   // Attribute constants for EStdDescClassItem
   public static final int StdDescClassIsStruct       = 0x80;     /* class is structure               */

   // Protection Types constants, used by EStdDescBaseClassItem, EStdDescDataMemItem, EStdDescMemFcnItem
   public static final int StdProtIsPrivate           = 1;        /* protection type is private       */
   public static final int StdProtIsProtected         = 2;        /* protection type is protected     */
   public static final int StdProtIsPublic            = 3;        /* protection type is public        */

   // Attribute constants for EStdDescBaseClassItem
   public static final int StdDescBaseClassIsVirtual  = 0x80;     /* base class is virtual            */

   // Attribute constants for EStdDsecDataMemItem
   public static final int StdDescDataMemIsStatic     = 0x80;     /* data member is static            */

   // Attribute constants for EStdDescMemFcnItem
   public static final int StdDescMemFcnIsStatic      = 0x80;     /* member function is static        */
   public static final int StdDescMemFcnIsInline      = 0x40;     /* member function is inline        */
   public static final int StdDescMemFcnIsConst       = 0x20;     /* member function is constant      */
   public static final int StdDescMemFcnIsVolatile    = 0x10;     /* member function is volatile      */
   public static final int StdDescMemFcnIsVirtual     = 0x08;     /* member function is virtual       */

   // Type constants for EStdDescMemFcnItem
   public static final int StdMemFcnIsRegular         = 1;        /* member function is regular       */
   public static final int StdMemFcnIsConstructor     = 2;        /* member function is constructor   */
   public static final int StdMemFcnIsDestructor      = 3;        /* member function is destructor    */

   // change info types -- ONLY available to package
   static final int StdStat_Noth     =0x00000000;  /* nothing changed                   */
   static final int StdStat_Ptab     =0x80000000;  /* parts table changed               */
   static final int StdStat_Btab     =0x40000000;  /* breakpoint table changed          */
   static final int StdStat_Prog     =0x20000000;  /* program state changed             */
   static final int StdStat_Mvar     =0x10000000;  /* monitored variable changed        */
   static final int StdStat_ThdState =0x01000000;  /* thread state changed              */
   static final int StdStat_MStor    =0x00800000;  /* monitored storage changed         */
   static final int StdStat_MStack   =0x00400000;  /* monitored stack changed           */
   static final int StdStat_Module   =0x00100000;  /* module entry (MTE) changed        */
   static final int StdStat_MRegs    =0x00040000;  /* monitored registers changed       */
   static final int StdStat_Process  =0x00010000;  /* process info changed              */
   static final int StdStat_FCT      =0x00008000;  /* FCT bits changed                  */

   static final int UNDEFINED_INFO           =0;   /* does not correspond to any bit    */
   static final int PARTS_TABLE_INFO         =1;   /* parts table changed info          */
   static final int BREAKPOINT_TABLE_INFO    =2;   /* breakpoint table changed info     */
   static final int PROGRAM_STATE_INFO       =3;   /* program state changed info        */
   static final int MONITORED_VARIABLE_INFO  =4;   /* monitored variable changed info   */
   static final int PROGRAM_COMMAND_INFO     =5;   /* call from pgm w/ cmd      NOT USED*/
   static final int PROGRAM_OUTPUT_INFO      =6;   /* output fr pgm pending     NOT USED*/
   static final int PROGRAM_INPUT_INFO       =7;   /* pgm waiting for input     NOT USED*/
   static final int THREAD_STATE_INFO        =8;   /* thread state changed info         */
   static final int STORAGE_INFO             =9;   /* monitored storage changed info    */
   static final int STACK_INFO               =10;  /* monitored stack changed info      */
   static final int PM_QUEUE_INFO            =11;  /* PM queue changed info     NOT USED*/
   static final int MODULE_ENTRY_INFO        =12;  /* module entry (MTE) changed info   */
   static final int LOG_CHANGED_INFO         =13;  /* command log changed info          */
   static final int REGISTERS_INFO           =14;  /* monitored registers changed info  */
   static final int PASSTHRU_INFO            =15;  /* PassThru window changed info      */
   static final int PROCESS_INFO             =16;  /* Process changed info              */
   static final int FCT_INFO                 =17;  /* FCT bits changed info             */

   /* Startup constants */
   // View Options
   public final static short Viewtype_simp = 1;
   public final static short Viewtype_prefix = 2;
   public final static short Viewtype_dynam = 3;

   public final static short View_Class_Unk       = 0;
   public final static short View_Class_Struc     = 1;  //not used
   public final static short View_Class_Source    = 2;
   public final static short View_Class_Mixed     = 3;
   public final static short View_Class_Disasm    = 4;
   public final static short View_Class_Listing   = 5;
   public final static short View_Class_Annotated = 6; //not used

   public final static int MONITOR_CAPABLE = 0x80;
   public final static int LINEBP_CAPABLE = 0x40;

   /* Function Control Table Flags */
   // Startup
   public final static int FCT_PROCEDURE_NAME_ACCEPTED   = 0x80000000;     // not used in version 1
   public final static int FCT_DEBUG_APPLICATION_INIT    = 0x40000000;     // support debugging app init
   public final static int FCT_JOB_NAME                  = 0x20000000;     // support job name (AS400)
   public final static int FCT_PROGRAM_FILELIST          = 0x10000000;     // support BE sending list of progs to debug
   public final static int FCT_HOST_ADDRESS		 = 0x08000000;     // support host address

   // General Functions
   public final static int FCT_MULTIPLE_THREADS          = 0x80000000;     // support multiple threads
   public final static int FCT_MULTIPLE_PROCESSES        = 0x40000000;     // not used in version 1
   public final static int FCT_PM_DEBUGGING              = 0x20000000;     // support debugging PM apps
   public final static int FCT_FILE_LIST_AVAILABLE       = 0x10000000;     // support file list
   public final static int FCT_CHILD_PROCESSES           = 0x08000000;     // support child processes
   public final static int FCT_INCLUDE_FILES             = 0x04000000;     // support include files
   public final static int FCT_ENVIRONMENT_MODIFY        = 0x02000000;     // support modify environment
   public final static int FCT_FILE_PATH_AVAILABLE       = 0x01000000;     // support file path
   public final static int FCT_DEBUG_ON_DEMAND           = 0x00800000;     // support debugging on demand
   public final static int FCT_STARTUP                   = 0x00400000;     // support startup dialog
   public final static int FCT_PROCESS_LIST_STARTUP      = 0x00200000;     // support process list startup
   public final static int FCT_POST_MORTEM_DEBUG         = 0x00100000;     // postmortem debug is on
   public final static int FCT_POST_MORTEM_CAPABLE       = 0x00080000;     // backend support postmortem

   // File Options
   public final static int FCT_CHANGE_SOURCE_FILE        = 0x80000000;     // support changing source file
   public final static int FCT_FILE_RESTART              = 0x40000000;     // support restart of program
   public final static int FCT_MODULE_ADD                = 0x20000000;     // support addition of modules
   public final static int FCT_MODULE_REMOVE             = 0x10000000;     // support removal of modules
   public final static int FCT_PROCESS_ATTACH            = 0x08000000;     // support attach to process
   public final static int FCT_PROCESS_DETACH            = 0x04000000;     // support detach from process
   public final static int FCT_PROCESS_DETACH_KILL       = 0x02000000;     // support detach and kill
   public final static int FCT_PROCESS_DETACH_KEEP       = 0x01000000;     // support detach and keep
   public final static int FCT_PROCESS_DETACH_RELEASE    = 0x00800000;     // support detach and release
   public final static int FCT_PROCESS_ATTACH_PATH       = 0x00400000;     // support attach process path
   public final static int FCT_LOCAL_SOURCE_FILES        = 0x00200000;     // support local src files

   // Storage options
   public final static int FCT_STORAGE_ADDRESS_FLAT      = 0x80000000;     // support flat
   public final static int FCT_STORAGE_ADDRESS_1616      = 0x40000000;     // support 16:16
   public final static int FCT_STORAGE_ADDRESS_FLAT_1616 = 0x20000000;     // support flat & 16:16
   public final static int FCT_STORAGE_CONTENT_HEX_CHAR  = 0x10000000;     // support hex and char
   public final static int FCT_STORAGE_CONTENT_CHAR      = 0x08000000;     // support character
   public final static int FCT_STORAGE_CONTENT_16INT     = 0x04000000;     // support 16-bit int
   public final static int FCT_STORAGE_CONTENT_16UINT    = 0x02000000;     // support 16-bit uint
   public final static int FCT_STORAGE_CONTENT_16INTHEX  = 0x01000000;     // support 16-bit int hex
   public final static int FCT_STORAGE_CONTENT_32INT     = 0x00800000;     // support 32-bit int
   public final static int FCT_STORAGE_CONTENT_32UINT    = 0x00400000;     // support 32-bit uint
   public final static int FCT_STORAGE_CONTENT_32INTHEX  = 0x00200000;     // support 32-bit int hex
   public final static int FCT_STORAGE_CONTENT_32FLOAT   = 0x00100000;     // support 32-bit float
   public final static int FCT_STORAGE_CONTENT_64FLOAT   = 0x00080000;     // support 64-bit float
   public final static int FCT_STORAGE_CONTENT_88FLOAT   = 0x00040000;     // support 88-bit float
   public final static int FCT_STORAGE_CONTENT_16PTR     = 0x00020000;     // support 16-bit ptrs
   public final static int FCT_STORAGE_CONTENT_1616PTR   = 0x00010000;     // support 16:16-bit ptrs
   public final static int FCT_STORAGE_CONTENT_32PTR     = 0x00008000;     // support 32-bit prs

   public final static int FCT_STORAGE_CONTENT_HEX_EBCDIC= 0x00004000;     // support hex and ebcdic
   public final static int FCT_STORAGE_CONTENT_EBCDIC    = 0x00002000;     // support ebcdic

   public final static int FCT_STORAGE_CONTENT_HEX_ASCII = 0x00001000;     // support hex and ASCII
   public final static int FCT_STORAGE_CONTENT_ASCII     = 0x00000800;     // support ASCII
   public final static int FCT_STORAGE_CONTENT_IEEE_32   = 0x00000400;     // support IEEE 32-bit float
   public final static int FCT_STORAGE_CONTENT_IEEE_64   = 0x00000200;     // support IEEE 64-bit float

   public final static int FCT_STORAGE_CONTENT_64INT     = 0x00000100;     /* support 64-bit int     */
   public final static int FCT_STORAGE_CONTENT_64UINT    = 0x00000080;     /* support 64-bit uint    */
   public final static int FCT_STORAGE_CONTENT_64INTHEX  = 0x00000040;     /* support 64-bit int hex */
   public final static int FCT_STORAGE_CONTENT_64PTR     = 0x00000020;     // support 64-bit ptr

   public final static int FCT_STORAGE_ENABLE_TOGGLE     = 0x00000001;     // support storage monitor enable/disable
   public final static int FCT_STORAGE_EXPR_ENABLE_TOGGLE= 0x00000002;     // support storage monitor expr enable/disable

   // Breakpoint Options
   public final static int FCT_LINE_BREAKPOINT           = 0x80000000;     // support line bkpts
   public final static int FCT_STATEMENT_BREAKPOINT      = 0x40000000;     // support stmt bkpts
   public final static int FCT_FUNCTION_BREAKPOINT       = 0x20000000;     // support function bkpts
   public final static int FCT_ADDRESS_BREAKPOINT        = 0x10000000;     // support address bkpts
   public final static int FCT_CHANGE_ADDRESS_BREAKPOINT = 0x08000000;     // support chg addr bkpts
   public final static int FCT_LOAD_BREAKPOINT           = 0x04000000;     // support load bkpts
   public final static int FCT_EVENT_BREAKPOINT          = 0x02000000;     // support event bkpts
   public final static int FCT_BREAKPOINT_ENABLE_TOGGLE  = 0x01000000;     // support enablemt bkpts
   public final static int FCT_BREAKPOINT_MODIFY         = 0x00800000;     // support modify bkpts
   public final static int FCT_BREAKPOINT_DEFERRED       = 0x00400000;     // support deferred bkpts
   public final static int FCT_BREAKPOINT_ENTRY_AUTOSET  = 0x00200000;     // support auto set entry
   public final static int FCT_BREAKPOINT_EXPRESSION     = 0x00100000;     // support expr on bkpts
   public final static int FCT_BREAKPOINT_MONITOR_8BYTES = 0x00080000;     // support monitor 8 bytes
   public final static int FCT_BREAKPOINT_MONITOR_4BYTES = 0x00040000;     // support monitor 4 bytes
   public final static int FCT_BREAKPOINT_MONITOR_2BYTES = 0x00020000;     // support monitor 2 bytes
   public final static int FCT_BREAKPOINT_MONITOR_1BYTES = 0x00010000;     // support monitor 1 bytes
   public final static int FCT_BREAKPOINT_MONITOR_0_128  = 0x00008000;     // support byte range
   public final static int FCT_BREAKPOINT_DATE           = 0x00004000;     // support date breakpoints
   public final static int FCT_BREAKPOINT_NO_THREADS     = 0x00002000;     // do not support threads
   public final static int FCT_BREAKPOINT_NO_FREQUENCY   = 0x00001000;     // do not support frequency

   // Monitor options
   public final static int FCT_MONITOR_ENABLE_TOGGLE     = 0x80000000;     // support enablement mon. expr.

   // Window options
   public final static int FCT_LOCAL_VARIABLES           = 0x80000000;     // support local variables
   public final static int FCT_REGISTERS                 = 0x40000000;     // support registes
   public final static int FCT_STACK                     = 0x20000000;     // support stack
   public final static int FCT_STORAGE                   = 0x10000000;     // support storage
   public final static int FCT_WINDOW_ANALYSIS           = 0x08000000;     // support window analysis
   public final static int FCT_MESSAGE_QUEUE_MONITOR     = 0x04000000;     // support msg queue monitors
   public final static int FCT_INHERITANCE_VIEW          = 0x02000000;     // support inheritance view
   public final static int FCT_COMMAND_LOG               = 0x01000000;     // support command log
   public final static int FCT_PASSTHRU                  = 0x00800000;     // support PassThru window

   // Run options
   public final static int FCT_THREAD_ENABLED            = 0x80000000;     // support thread enablement
   public final static int FCT_STEP_OVER                 = 0x40000000;     // support step over
   public final static int FCT_STEP_INTO                 = 0x20000000;     // support step into
   public final static int FCT_STEP_DEBUG                = 0x10000000;     // support step debug
   public final static int FCT_STEP_RETURN               = 0x08000000;     // support step return
   public final static int FCT_RUN_TO_LOCATION           = 0x04000000;     // support run to location
   public final static int FCT_JUMP_TO_LOCATION          = 0x02000000;     // support step to location
   public final static int FCT_HALT                      = 0x01000000;     // support run halting
   public final static int FCT_STORAGE_USAGE_CHECK       = 0x00800000;     // support storage usage check

   // Exception options
   public final static int FCT_EXCEPTION_FILTER          = 0x80000000;     // support exception filtering
   public final static int FCT_EXCEPTION_EXAMINE         = 0x40000000;     // support examine-retry
   public final static int FCT_EXCEPTION_STEP            = 0x20000000;     // support step exception
   public final static int FCT_EXCEPTION_RUN             = 0x10000000;     // support run exception

   // Stack options
   public final static int FCT_STACK_REMAINING_SZE       = 0x80000000;     // support remaining stack size
   public final static int FCT_STACK_SET_BREAKPOINT      = 0x40000000;     // support set bkpt from stack
   public final static int FCT_STACK_OPEN_STORAGE        = 0x20000000;     // support open stg from stack

   // Registers attributes
   public final static int RegistersEnabled              = 0x80000000;     // enable the registers

   // Registers flags
   public final static int RegisterDeleted               = 0x80000000;
   public final static int RegisterNew                   = 0x40000000;
   public final static int RegisterNameChanged           = 0x20000000;
   public final static int RegisterValueChanged          = 0x10000000;

   // Register Types
   public final static int ConstantRegister				 = 0x80000000;     // registers are read only
   public final static int SingleBitRegister			 = 0x40000000;	   // registers are a single bit wide

   // Exception status
   public final static int EXCEPTION_DISABLED            = 0;
   public final static int EXCEPTION_ENABLED             = 1;

   // Change Packets
   public final static byte NO_COMPRESSION = 0;

   // Part Table Changes
   public final static int InUse = 0x80;
   public final static int Verified = 0x40;
   public final static int PartNew = 0x20;
   public final static int PartDeleted = 0x10;
   public final static int PartChanged = 0x08;
   public final static int StmtHook = 0x04;
   public final static int SymbolTbl = 0x02;
   public final static int CallHook = 0x01;

   public final static int VIEW_VALIDATED = 0x80;

   public final static int VIEW_VERIFIED = 0x80;
   public final static int VIEW_VERIFY_ATTEMPTED = 0x40;
   public final static int VIEW_CHANGE_TEXT_VALID = 0x20;
   public final static int VIEW_LOCAL = 0x10;
   public final static int VIEW_VERIFY_ATTEMPTED_FE = 0x08;
   public final static int VIEW_NO_SWITCH = 0x04;

   public final static byte SourceLineExecutable = (byte)0x80;
   public final static byte SourceLineGetLocal    = (byte)0x40;

   // Module Changes
   public final static int ModuleEntryInvalid = 0x80;
   public final static int ModuleEntryNew = 0x40;
   public final static int ModuleEntryDeleted = 0x20;
   public final static int ModuleEntryHasDebugData = 0x08;
   public final static int ModuleEntryHasParts = 0x04;

   // Thread Information
   public final static short StdThdRunnable = 0;
   public final static short StdThdSuspended = 1;
   public final static short StdThdBlocked = 2;
   public final static short StdThdCritSect = 3;
   public final static short StdThdUnknown = 4;

   public final static short StdThdThawed = 0;
   public final static short StdThdFrozen = 1;
   public final static short StdThdTerminated = 2;

   // Constants for thread attribute type, as used in ERepGetNextThread (307):

   public final static byte ThreadMiscAttr = 0;
   public final static byte ThreadNameOrTID = 1;
   public final static byte ThreadState = 2;
   public final static byte ThreadPriority = 3;
   public final static byte ThreadGroup = 4;
   public final static byte ThreadBlockingThread = 5;

   // Breakpoint table change packets
   // Breakpoint Flags
   public final static int BkpDeleted = 0x8000;
   public final static int BkpNew = 0x4000;
   public final static int BkpInvalid = 0x2000;
   public final static int BkpEnabled = 0x1000;
   public final static int BkpChanged = 0x0800;

   // Breakpoint Types
/* public final static int BkpDefer = 0x4000; */   // defined above
   public final static int BkpDeferActive = 0x2000;
   public final static int BkpDeferAmbiguous = 0x1000;
   public final static int BkpDeferFailed = 0x0800;
   public final static int BkpAutoSetEntry = 0x0400;

   // Stack table change packets
   // Stack status
   public final static int STACK_ENTRY_CHANGED = 0x04;      /* stack entry has changed          */
   public final static int STACK_ENTRY_NEW = 0x02;          /* stack entry is new               */
   public final static int STACK_ENTRY_DELETE = 0x01;       /* stack entry has been deleted     */
   public final static int STACK_ENTRY_NOCHANGE = 0x00;     /* stack entry has not changed      */

   // Monitor change packets
   // Status flags for ERepGetNextMonitorExpr
   public final static int MonDeleted           = 0x8000;      /* monitor has been deleted         */
   public final static int MonNew               = 0x4000;      /* monitor is new                   */
   public final static int MonInvalid           = 0x2000;      /* monitor is invalid               */
   public final static int MonEnabled           = 0x1000;      /* monitor is enabled               */
   public final static int MonValuesChanged     = 0x0800;      /* monitor values changed           */
   public final static int MonTreeStructChanged = 0x0400;      /* monitor tree struct changed      */
   public final static int MonEnablementChanged = 0x0200;      /* monitor enablement changed       */
   public final static int MonValidityChanged   = 0x0100;      /* monitor validity changed         */
   public final static int MonNameChanged       = 0x0080;      /* monitor name changed             */

   // Monitor Types for ERepGetNextMonitorExpr
   public final static short MonTypeProgram       = (short)0;           /* program monitor                  */
   public final static short MonTypePrivate       = (short)1;           /* private monitor                  */
   public final static short MonTypePopup         = (short)2;           /* popup monitor                    */
   public final static short MonTypeLocal         = (short)3;           /* local variable monitor           */

   // Flags for EReqStorage2:

   public final static int StorageEnabled       = 0x80000000;  /* storage enabled */
   public final static int StorageExprEnabled   = 0x40000000;  /* storage expression enabled */

   // Storage Unit Styles for ERepGetNextMonitorStorageID
   public final static short StorageStyleUnknown           = (short) 0;  /* unknown                          */
   public final static short StorageStyleByteHexCharacter  = (short)1;  /* bytes in hex and character       */
   public final static short StorageStyleByteCharacter     = (short) 2;  /* bytes in character               */
   public final static short StorageStyle16BitIntSigned    = (short) 3;  /* 16 bit integer signed            */
   public final static short StorageStyle16BitIntUnsigned  = (short)4;  /* 16 bit integer unsigned          */
   public final static short StorageStyle16BitIntHex       = (short) 5;  /* 16 bit integer hex               */
   public final static short StorageStyle32BitIntSigned    = (short) 6;  /* 32 bit integer signed            */
   public final static short StorageStyle32BitIntUnsigned  = (short) 7;  /* 32 bit integer unsigned          */
   public final static short StorageStyle32BitIntHex       = (short) 8;  /* 32 bit integer hex               */
   public final static short StorageStyle32BitFloat        = (short) 9;  /* 32 bit float                     */
   public final static short StorageStyle64BitFloat        = (short) 10; /* 64 bit float                     */
   public final static short StorageStyle80BitFloat        = (short) 11; /* 80 bit float                     */
   public final static short StorageStyle16BitNear         = (short) 12; /* 16 bit near                      */
   public final static short StorageStyle16BitFar          = (short) 13; /* 16 bit far                       */
   public final static short StorageStyle32BitFlat         = (short) 14; /* 32 bit flat                      */

   public final static short StorageStyleByteHexEBCDIC     = (short) 15; /* bytes in hex and EBCDIC          */
   public final static short StorageStyleByteEBCDIC        = (short) 16; /* bytes in EBCDIC                  */

   public final static short StorageStyleByteHexDisasm     = (short) 17; /* bytes in hex and disassembly     */

   public final static short StorageStyleByteHexASCII      = (short) 18; /* bytes in hex and ASCII           */
   public final static short StorageStyleByteASCII         = (short) 19; /* bytes in ASCII                   */
   public final static short StorageStyle32IEEE            = (short) 20; /* IEEE 32-bit float */
   public final static short StorageStyle64IEEE            = (short) 21; /* IEEE 64-bit float */

   public final static short StorageStyle64BitIntSigned    = (short) 22; /* 64 bit integer signed            */
   public final static short StorageStyle64BitIntUnsigned  = (short) 23; /* 64 bit integer unsigned          */
   public final static short StorageStyle64BitIntHex       = (short) 24; /* 64 bit integer hex               */
   public final static short StorageStyle64BitFlat         = (short) 25; /* 64 bit integer hex               */

   // Storage Address Styles for ERepGetNextMonitorStorageId
   public final static short StorageAddrStyleUnknown       = (short) 0;  /* unknown                          */
   public final static short StorageAddrStyleFlat          = (short)1;  /* flat                             */
   public final static short StorageAddrStyleSegmented     = (short) 2;  /* segmented                        */
   public final static short StorageAddrStyleFlatSegmented = (short) 3;  /* flat and segmented               */
   public final static short StorageAddrStyleAddress       = (short) 4;  /* bits                             */
   public final static short StorageAddrStyleAlet          = (short) 5;  /* ALET                             */

   // AttributeIndex for ERepGetNextMonitorStorageId
   public final static int StorageUnitAllocated    = 0x0004;   /* storage unit allocated           */
   public final static int StorageUnitChanged      = 0x0002;   /* storage unit changed             */
   public final static int StorageUnitUpdateable   = 0x0001;   /* storage unit updateable          */

   // Flags for ERepGetNextMonitorStorageId
   public final static int MonStorDeleted          = 0x8000;   /* storage deleted                  */
   public final static int MonStorNew              = 0x4000;   /* storage new                      */
   public final static int MonStorEnabled          = 0x2000;   /* storage enabled                  */
   public final static int MonStorEnablementChanged= 0x1000;   /* storage enablement changed       */
   public final static int MonStorContentsChanged  = 0x0800;   /* storage contents changed         */
   public final static int MonStorAddressChanged   = 0x0400;   /* storage address changed          */
   public final static int MonStorStartBoundry     = 0x0200;   /* storage start boundary           */
   public final static int MonStorEndBoundry       = 0x0100;   /* storage end boundary             */
   public final static int MonStorNewLinesReturned = 0x0080;   /* storage new lines returned       */
   public final static int MonStorExprEnabled      = 0x0040;   /* storage expression enabled       */
   public final static int MonStorExprNotAllocated = 0x0020;   /* storage expression not allocated */
   public final static int MonStorStyleChanged     = 0x0010;   /* storage display style changed    */

   // Supported String Encodings
   public final static byte StrEncode_Ext_ASCII     = (byte) 0x80;     /* Extended ASCIIencoding */
   public final static byte StrEncode_UTF8          = (byte) 0x40;     /* UTF-8 encoding */

   public final static String jIBMCopyright = "(c) Copyright IBM Corporation 1997, 1998 - All Rights Reserved";

   public final byte SourceLinePlaceHolder = (byte)0xFF;

}
