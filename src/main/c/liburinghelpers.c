#include<liburing.h>

extern void scalanative_io_uring_cqe_seen(struct io_uring* ring, struct io_uring_cqe* cqe) {
  io_uring_cqe_seen(ring, cqe);
}

extern void scalanative_io_uring_prep_poll_add(struct io_uring_sqe *sqe, int fd,
					  short poll_mask) {
  io_uring_prep_poll_add(sqe, fd, poll_mask);
}

extern void scalanative_io_uring_prep_poll_remove(struct io_uring_sqe *sqe, void *user_data) {
  io_uring_prep_poll_remove(sqe, user_data);
}
extern void scalanative_io_uring_prep_timeout(struct io_uring_sqe *sqe,
					 struct __kernel_timespec *ts,
					 unsigned count, unsigned flags) {
  io_uring_prep_timeout(sqe, ts, count, flags);
}
extern void scalanative_io_uring_prep_timeout_remove(struct io_uring_sqe *sqe,
					 __u64 user_data, unsigned flags) {
  io_uring_prep_timeout_remove(sqe, user_data, flags);
}
extern void scalanative_io_uring_prep_readv(struct io_uring_sqe *sqe, int fd,
				       const struct iovec *iovecs,
				       unsigned nr_vecs, off_t offset) {
  io_uring_prep_readv(sqe, fd, iovecs, nr_vecs, offset);
}
extern void scalanative_io_uring_prep_writev(struct io_uring_sqe *sqe, int fd,
				       const struct iovec *iovecs,
				       unsigned nr_vecs, off_t offset) {
  io_uring_prep_writev(sqe, fd, iovecs, nr_vecs, offset);
}
extern int scalanative_io_uring_wait_cqe(struct io_uring *ring,
				    struct io_uring_cqe **cqe_ptr) {
  return io_uring_wait_cqe(ring, cqe_ptr);
}
extern int scalanative_io_uring_peek_cqe(struct io_uring *ring,
				    struct io_uring_cqe **cqe_ptr) {
  return io_uring_peek_cqe(ring, cqe_ptr);
}
extern void *scalanative_io_uring_cqe_get_data(struct io_uring_cqe *cqe) {
  return io_uring_cqe_get_data(cqe);
}
extern void scalanative_io_uring_sqe_set_data(struct io_uring_sqe *sqe, void *data) {
  io_uring_sqe_set_data(sqe, data);
}
extern void scalanative_io_uring_prep_accept(struct io_uring_sqe *sqe, int fd,
					struct sockaddr *addr,
					socklen_t *addrlen, int flags) {
  io_uring_prep_accept(sqe, fd, addr, addrlen, flags);
}