#!/usr/bin/perl -w
if (!defined($ARGV[0]) || !defined($ARGV[1]) || !defined($ARGV[2]) || !defined($ARGV[3]) || !defined($ARGV[4]) || !defined($ARGV[5]))
{
    print("command usage:\n");
   print("auth.pl USER, PASSWORD, PATH, PORT, TIMEOUT, TICKET\n");
}
else
{
  $userIN    = $ARGV[0];
  $pwdIN     = $ARGV[1];
  $pathIN    = $ARGV[2];
  $portIN    = $ARGV[3];
  $timeoutIN = $ARGV[4];
  $ticketIN  = $ARGV[5];

  @passwdStruct = getpwnam($userIN);

  if (@passwdStruct == 0)
  {
     print("invalid user name\n");
     0;
  }
  else
  {
    $passwd=$passwdStruct[1];
    $encryptedPWD = crypt($pwdIN, $passwd);
    $match = $passwd cmp $encryptedPWD;
    $classpath=$ENV{CLASSPATH};
    if ($match == 0) 
    {
	print("success\n");
	system("su -lp $userIN -c 'java -cp $classpath -DA_PLUGIN_PATH=$pathIN org.eclipse.cdt.dstore.core.server.Server $portIN $timeoutIN $ticketIN'");
	1;
    }
    else
    {
	print("incorrect password\n");
       0;
    }
  }
}
