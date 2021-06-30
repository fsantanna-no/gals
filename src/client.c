#if 0
#!/bin/sh
gcc -g -Wall -o xclient client.c -luv
exit
#endif

#include <assert.h>
#include <stdio.h>
#include <stdlib.h>
#include <uv.h>

void on_alloc (uv_handle_t* handle, size_t size, uv_buf_t* buf);
void on_connect (uv_connect_t* req, int status);
void on_write (uv_write_t* req, int status);
void on_read (uv_stream_t* stream, ssize_t nread, const uv_buf_t* buf);
void on_write_start (uv_write_t* req, int status);

void app (uint64_t now, int id, int data);

static int state = 0;

void main (void) {
    uv_loop_t loop;
    uv_loop_init(&loop);

    uv_tcp_t tcp;
    uv_tcp_init(&loop, &tcp);

    struct sockaddr_in dest;
    uv_ip4_addr("127.0.0.1", 10000, &dest);

    uv_connect_t conn;
    uv_tcp_connect(&conn, &tcp, (struct sockaddr*)&dest, on_connect);

    while (1) {
        int more = uv_run(&loop, UV_RUN_NOWAIT);
        if (more && state==1) {
            app(uv_now(&loop), 0, 0);
        }
    }
    return;
}

void on_connect (uv_connect_t* conn, int status) {
    assert(status >= 0);
    puts("connect ok");
    uv_read_start(conn->handle, on_alloc, on_read);
}

void on_read (uv_stream_t* client, ssize_t nread, const uv_buf_t* buf) {
    if (nread == UV_EOF) {
        puts("read done");
        uv_close((uv_handle_t*) client, (uv_close_cb) free);
        return;
    }

    assert(nread > 0);
    printf("read ok: %ld\n", nread);

    if (state == 0) {
        state = 1;
        assert(nread == 1);
        assert(buf->base[0] == 0);
        puts("recv START");
    } else {
        assert(0);
    }
    free(buf->base);
}

void on_write (uv_write_t* req, int status) {
    assert(status == 0);
    puts("write ok");
    free(req);
}

void on_alloc (uv_handle_t* handle, size_t size, uv_buf_t* buf) {
    printf("alloc ok: %ld\n", size);
	*buf = uv_buf_init(malloc(size), size);
}

void app (uint64_t now, int id, int data) {
    printf("now=%ld, id=%d, data=%d\n", now, id, data);

#if 0
    static int nxt = 0;
    if (nxt == 0) {
        nxt = rand() % 10000;
    } else if (now >= nxt) {
        nxt = now + rand() % 10000;
        evt_t evt = { EVT_XXX, rand()%100 };
        queue_add(evt);
    }
#endif
}

