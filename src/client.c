#if 0
#!/bin/sh
gcc -g -Wall -o xclient client.c -luv
exit
#endif

#include <assert.h>
#include <stdio.h>
#include <stdlib.h>
#include <uv.h>

void on_connect (uv_connect_t* req, int status);
void on_write (uv_write_t* req, int status);

void main (void) {
    uv_loop_t loop;
    uv_loop_init(&loop);

    uv_tcp_t tcp;
    uv_tcp_init(&loop, &tcp);

    struct sockaddr_in dest;
    uv_ip4_addr("127.0.0.1", 10000, &dest);

    uv_connect_t conn;
    uv_tcp_connect(&conn, &tcp, (struct sockaddr*)&dest, on_connect);

    uv_run(&loop, UV_RUN_DEFAULT);
    return;
}

void on_connect (uv_connect_t* conn, int status) {
    assert(status >= 0);
    puts("connect ok");

    uv_stream_t* stream = conn->handle;

    uv_buf_t buf = {
        .base = "1234567890",
        .len  = 10
    };

    uv_write_t* req = malloc(sizeof(uv_write_t));
    uv_write(req, stream, &buf, 1, on_write);
}

void on_write (uv_write_t* req, int status) {
    assert(status == 0);
    puts("write ok");
    free(req);
}
