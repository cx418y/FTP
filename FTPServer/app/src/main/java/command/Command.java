package command;

import android.os.Build;
import android.os.Looper;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import Response.Code;
import server.ServerHandleThread;

public class Command {
    private String command = "" ;
    private String args = "" ;
    private String response = "";
    private ServerHandleThread serverHandleThread;
    private Execute commandExecute;

    public Command(String commandLine, ServerHandleThread serverHandleThread){
        this.serverHandleThread = serverHandleThread;
        this.commandExecute = new Execute(this.serverHandleThread);

        String[]temp = commandLine.split(" ");
        int a = commandLine.indexOf(" ");

        //无参数的命令
        if( a == -1 ){
            command = commandLine;
            System.out.println("command: "+command);
        }
        //有参数的命令
        else {
            command = commandLine.substring(0,a);
            args =commandLine.substring(a+1);
            System.out.println("command: "+command+"args: "+ args);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String execute() throws IOException{
        if (Looper.myLooper() == null) {
            Looper.prepare();
        }
        switch (command){
            case "USER":
                response = commandExecute.USERCommand(args);
                break;
            case "PASS":
                response = commandExecute.PASSCommand(args);
                break;
            case "NOOP":
                response = commandExecute.NOOPCommand();
                break;
            case "QUIT":
                response = commandExecute.QUITCommand();
                break;
            case "PASV":
                response = commandExecute.PASVCommand();
                break;
            case "PORT":
                response = commandExecute.PORTCommand(args);
                break;
            case"TYPE":
                response = commandExecute.TYPECommand(args);
                break;

            case"MODE":
                response = commandExecute.MODECommand(args);
                break;

            case"STRU":
                response = commandExecute.STRUCommand(args);
                break;

            case"RETR":
                response = commandExecute.RETRCommand(args);
                break;

            case"STOR":
                response = commandExecute.STORCommand(args);
                break;

            case"LIST":
                response = commandExecute.LISTCommand(args);
                break;
            case"SHOW":
                response = commandExecute.SHOWCommand(args);
                break;

            case "PASVFAST":
                response = commandExecute.PASVFast();
                break;

            case "PORTFAST":
                response = commandExecute.PORTFast(args);
                break;

            case "RETRFAST":
                response = commandExecute.RETEFast(args);
                break;

            case "STORFAST":
                response = commandExecute.STORFast(args);
                break;

            default:
                response = Code.COMMAND_NOT_EXIST+": 命令不存在";
                break;
        }
        System.out.println("resp:" + response);
        return response;
    }

}
