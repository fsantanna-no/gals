#include <assert.h>
#include <stdio.h>
#include <stdlib.h>
#include <uv.h>

enum EVT {
    EVT_NONE,
    EVT_START,
    EVT_XXX
};

typedef struct {
    enum EVT id;
    int data;
} evt_t;

evt_t QUEUE[16];
int   QUEUE_I = 0;
int   QUEUE_N = 0;

void queue_add (evt_t evt) {
    assert(QUEUE_N < 16);
    QUEUE_N++;
    QUEUE[QUEUE_I] = evt;
    QUEUE_I = (QUEUE_I + 1) % 16;
}

evt_t queue_rem () {
    if (QUEUE_N == 0) {
        evt_t evt = { EVT_NONE, 0 };
        return evt;
    } else {
        QUEUE_N--;
        QUEUE_I = (QUEUE_I - 1) % 16;
        return QUEUE[QUEUE_I];
    }
}

void app (uint64_t now, int id, int data);

void  main (void) {
    uv_loop_t* loop = uv_default_loop();

    while (1) {
        uv_run(loop, UV_RUN_DEFAULT);
        evt_t evt = queue_rem();
        app(uv_now(loop), evt.id, evt.data);
        uv_sleep(100);
    }

    uv_loop_close(loop);
    free(loop);
}

void app (uint64_t now, int id, int data) {
    printf("now=%ld, id=%d, data=%d\n", now, id, data);

    static int nxt = 0;
    if (nxt == 0) {
        nxt = rand() % 10000;
    } else if (now >= nxt) {
        nxt = now + rand() % 10000;
        evt_t evt = { EVT_XXX, rand()%100 };
        queue_add(evt);
    }
}
