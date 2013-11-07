

a) Install zeromq at first
    wget http://download.zeromq.org/zeromq-4.0.1.tar.gz
    tar xvzf zeromq-4.0.1.tar.gz
    ./configure
    make
    make install

b) Compile
    cd SparknginP/src
    make

c) Create named pipe file
    mkfifo /var/log/access_pipe_log
    chmod 666 /var/log/access_pipe_log

d) Configure Nginx
    Edit nginx.conf, add below lines into location or server section. 

        log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
                  '$status $body_bytes_sent "$http_referer" '
                  '"$http_user_agent" "$http_x_forwarded_for"';

        access_log  /var/log/access_pipe_log  main;


e) Test
    1) You should install pyzmq at first.
        e.g. 
            easy_install pyzmq

    2)  cd SparknginP/src
        ./logpub -d /var/log/access_pipe_log
        cd SparknginP/test
        python test.py
        
        Visit nginx web server, log data will be printed by test.py. 

            
