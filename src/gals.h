int gals_connect (int port, int fps);
void gals_disconnect (void);
void gals_wait (uint64_t* now, int* evt);
void gals_emit (int evt);
