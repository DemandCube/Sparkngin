/*
 *  Log deliver server
 *  Based on zeromq
 *  Author: Cui Yingjie (cuiyingjie@gmail.com)
 *
 */

#include <iostream>
#include <fstream>
#include <string>
#include <exception>
#include <fcntl.h>
#include <unistd.h>
#include <sys/types.h>
#include <sys/stat.h>
#include "zhelpers.hpp"


using namespace std;

const char* ARG_OPTION_PORT         = "-p" ;
const char* ARG_OPTION_DAEMONIZE    = "-d" ;

const int DEF_PORT                  = 8001 ;

//define return result
const int ERR_PARAMETER             = -1 ;
const int ERR_FAIELD_IN_OPENING     = -2 ;
const int ERR_FAIELD_IN_FCNTL       = -3 ;
const int ERR_UNKNOWN_EXCEPT        = -4 ;
const int ERR_TERM_EXCEPT           = -5 ;

struct CmdOption
{
    CmdOption()
    {
        iPort = DEF_PORT ;
        bDaemonize = false ;
    }

    int iPort ;
    bool bDaemonize ;
    string sLogPipeFile ;
};

static void fn_v_Usage( const string& sApp)
{
    cout << "Usage:" << endl ;
    cout << sApp << " [option] log_pipe_file" << endl ;
    cout << "Options:" << endl ;
    cout << "   -d      :Run as daemonize" << endl ;
    cout << "   -p port :Listening port " << endl ;

}

static void fn_v_Daemonize(void) {
    signal(SIGTTOU, SIG_IGN);
    signal(SIGTTIN, SIG_IGN);
    signal(SIGTSTP, SIG_IGN);

    if (0 != fork())
        exit(0);

    if (-1 == setsid())
        exit(0);

    signal(SIGHUP, SIG_IGN);

    if (0 != fork())
        exit(0);
}

bool fn_b_ParseArgs( int argc, char* argv[] , CmdOption& option )
{
    if( argc < 2 )
    {
        return false ;
    }

    for( int i = 1 ; i < argc ; i++ )
    {
        if( strcmp( argv[ i ] , ARG_OPTION_DAEMONIZE ) == 0 )
            option.bDaemonize = true ;
        else if(  strcmp( argv[ i ] , ARG_OPTION_PORT ) == 0 )
        {
            if( i >= argc - 1 )
            {
                return false ;
            }

            option.iPort = atoi( argv[ i + 1 ] ) ;
            i++ ;
        }
        else
            option.sLogPipeFile = argv[ i ] ;
    }

    if( option.sLogPipeFile.length() <= 0 )
    {
        return false ;
    }

    return true ; 
}

int main( int argc, char* argv[] ) 
{
    CmdOption option ;

    if( !fn_b_ParseArgs( argc, argv, option ) )
    {
        fn_v_Usage( argv[ 0 ] ) ;
        return ERR_PARAMETER ;
    }

    if( option.bDaemonize )
        fn_v_Daemonize() ;

    char bindAddr[ 30 ] ;

    sprintf( bindAddr, "tcp://*:%d" , option.iPort ) ;
    
    //  Prepare our context and publisher
    zmq::context_t context(1);
    zmq::socket_t publisher(context, ZMQ_PUB);
    publisher.bind( bindAddr );

    cout << "Bind on " << bindAddr << endl ;

    char read_buf[ 4096 + 1 ] ;

    while (1) 
    {
        FILE* plogfile = fopen(option.sLogPipeFile.c_str(), "rt");

        if( plogfile == NULL )
        {
            cerr << "failed in opending " << option.sLogPipeFile << endl ;
            sleep(10);
            continue;
        }

        int flags ;

        if ((flags = fcntl(fileno(plogfile),F_GETFL)) == -1) {
            cerr << "fcntl returned -1 for " << option.sLogPipeFile << endl;
            sleep(10);
            continue;
        }

        /* clear O_NONBLOCK  and reset file flags                 */
        flags &= (O_NONBLOCK);
        if ((fcntl(fileno(plogfile),F_SETFL,flags)) == -1) {
            cerr << "fcntl returned -1 for " << option.sLogPipeFile << endl;
            sleep(10);
            continue;
        }

        try
        {
            while (1) 
            {
                const char* pcszStr = fgets( read_buf , sizeof( read_buf ) , plogfile ) ;
             
                if( pcszStr != NULL )
                {
                    s_send (publisher, pcszStr ) ;
                }
                else
                {
                    break;
                }
            }

            fclose( plogfile ) ;
         
            sleep(1);
        }
        catch (const zmq::error_t& ex)
        {
            cerr << ex.what() << endl ;
            if (ex.num() == ETERM)
            {
                fclose( plogfile ) ;
                return ERR_TERM_EXCEPT ;
            }
        }
        catch(...)
        {
             cerr << "Unknown exception catched." << endl ;
             return ERR_UNKNOWN_EXCEPT ;
        }
    }

    return 0;
}


