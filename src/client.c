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
void on_write_emit (uv_write_t* req, int status);
void on_read (uv_stream_t* stream, ssize_t nread, const uv_buf_t* buf);
void on_write_start (uv_write_t* req, int status);

void app (uint64_t now, int id, int data);

static uv_stream_t* client;
static uv_loop_t    loop;
static uint64_t     late  = 0;
static int          state = 0;

typedef struct {
    int      evt;
    uint64_t now;
} pkt_t;

void on_alloc (uv_handle_t* handle, size_t size, uv_buf_t* buf) {
    printf("alloc ok: %ld\n", size);
	*buf = uv_buf_init(malloc(size), size);
}

int main (void) {
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
            app(uv_now(&loop) - late, 0, 0);
        }
    }
    return 0;
}

void on_connect (uv_connect_t* conn, int status) {
    assert(status >= 0);
    puts("connect ok");
    uv_read_start(conn->handle, on_alloc, on_read);
    client = conn->handle;
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
        late = uv_now(&loop);
        app(0, 0, 0);
        puts("recv START");
    } else {
        assert(0);
    }
    free(buf->base);
}

void app (uint64_t now, int id, int data) {
    //printf("now=%ld, id=%d, data=%d\n", now, id, data);
    static int nxt = 0;
    if (now > nxt) {
        nxt = now + rand() % 10000;
        static pkt_t pkt;
        pkt.evt = rand()%16;
        pkt.now = now;
        uv_buf_t buf = {
            .base = (char*) &pkt,
            .len  = sizeof(pkt_t)
        };
        printf("emit %d\n", pkt.evt);
        uv_write_t* req = malloc(sizeof(uv_write_t));
        uv_write(req, (uv_stream_t*) client, &buf, 1, on_write_emit);

    }
}

void on_write_emit (uv_write_t* req, int status) {
    assert(status == 0);
    puts("emit ok");
    free(req);
}

