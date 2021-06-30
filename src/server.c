#if 0
#!/bin/sh
gcc -g -Wall -o xserver server.c -luv
exit
#endif

#include <assert.h>
#include <stdio.h>
#include <stdlib.h>
#include <uv.h>

void on_alloc (uv_handle_t* handle, size_t size, uv_buf_t* buf);
void on_connect (uv_stream_t* server, int status);
void on_read (uv_stream_t* stream, ssize_t nread, const uv_buf_t* buf);

uv_loop_t loop;

void main (void) {
    uv_loop_init(&loop);

    uv_tcp_t tcp;
    struct sockaddr_in addr;
    uv_tcp_init(&loop, &tcp);
    uv_ip4_addr("0.0.0.0", 10000, &addr);
    uv_tcp_bind(&tcp, (const struct sockaddr*)&addr, 0);
    assert(0 == uv_listen((uv_stream_t*) &tcp, 128, on_connect));

    uv_run(&loop, UV_RUN_DEFAULT);
    return;
}

void on_connect (uv_stream_t* server, int status) {
    assert(status >= 0);
    puts("connect ok");
    uv_tcp_t* client = (uv_tcp_t*) malloc(sizeof(uv_tcp_t));
    uv_tcp_init(&loop, client);
    assert(0 == uv_accept(server, (uv_stream_t*) client));
    uv_read_start((uv_stream_t*) client, on_alloc, on_read);
}

void on_alloc (uv_handle_t* handle, size_t size, uv_buf_t* buf) {
    printf("alloc ok: %ld\n", size);
	*buf = uv_buf_init(malloc(size), size);
}

void on_read (uv_stream_t* client, ssize_t nread, const uv_buf_t* buf) {
    if (nread == UV_EOF) {
        puts("read done");
        uv_close((uv_handle_t*) client, (uv_close_cb) free);
    } else {
        assert(nread > 0);
        printf("read ok: %ld\n", nread);
    }
    free(buf->base);
}
