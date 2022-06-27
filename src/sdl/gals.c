#include <endian.h>
#include <SDL2/SDL_net.h>
#include <assert.h>

#include "gals.h"

static TCPsocket S = NULL;

void tcp_recv_n (int N, char* buf) {
    int i = 0;
    while (i < N) {
        i += SDLNet_TCP_Recv(S, &buf[i], N-i);
    }
}

int tcp_recv_u64 (void) {
    uint64_t v;
    tcp_recv_n(sizeof(v), (char*)&v);
    return be64toh(v);
}

int tcp_recv_s32 (void) {
    uint32_t v;
    tcp_recv_n(sizeof(v), (char*)&v);
    return be32toh(v);
}

void tcp_send_s32 (int v) {
    uint32_t _v = htobe32(v);
    assert(SDLNet_TCP_Send(S, &_v, sizeof(_v)) == sizeof(_v));
}

int gals_connect (int port, int fps) {
	assert(SDLNet_Init() == 0);
	IPaddress ip;
	assert(SDLNet_ResolveHost(&ip, "localhost", port) == 0);
	S = SDLNet_TCP_Open(&ip);
	assert(S != NULL);

    tcp_send_s32(fps);
    return tcp_recv_s32();
}

void gals_disconnet (void) {
	SDLNet_TCP_Close(S);
	SDLNet_Quit();
}

void gals_wait (uint64_t* now, int* evt, int* pay1, int* pay2) {
    *now = tcp_recv_u64();
    *evt = tcp_recv_s32();
    if (pay1 != NULL) {
        *pay1 = tcp_recv_s32();
    } else {
        tcp_recv_s32();
    }
    if (pay2 != NULL) {
        *pay2 = tcp_recv_s32();
    } else {
        tcp_recv_s32();
    }
}

void gals_emit (int evt, int pay1, int pay2) {
    tcp_send_s32(evt);
    tcp_send_s32(pay1);
    tcp_send_s32(pay2);
}
