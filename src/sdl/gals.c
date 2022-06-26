#include <endian.h>
#include <SDL2/SDL_net.h>
#include <assert.h>

#include "gals.h"

static TCPsocket S = NULL;

int tcp_recv_u64 (void) {
    uint64_t v_;
    int i = 0;
    int N = sizeof(v_);
    while (i < N) {
        i += SDLNet_TCP_Recv(S, &((char*)&v_)[i], N-i);
    }
    return be64toh(v_);
}

int tcp_recv_s32 (void) {
    uint32_t v_;
    int i = 0;
    int N = sizeof(v_);
    while (i < N) {
        i += SDLNet_TCP_Recv(S, &((char*)&v_)[i], N-i);
    }
    return be32toh(v_);
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

void gals_wait (uint64_t* now, int* evt) {
    *now = tcp_recv_u64();
    *evt = tcp_recv_s32();
}

void gals_emit (int evt) {
    tcp_send_s32(evt);
}
