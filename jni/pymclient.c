#include "main_activity_LocalSocketClientActivity.h"
#include <sys/socket.h>
#include <sys/un.h>
#include <stddef.h>
#include <string.h>
#include <stdlib.h>
#include <android/log.h>

#ifdef __cplusplus
extern "C"
{
#endif

#define  LOG_TAG "pcmclient"
#define  LOGE(...) __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

/*
 * Class:     main_activity_LocalSocketClientActivity
 * Method:    startHeartBeat
 */
JNIEXPORT jint JNICALL Java_main_activity_LocalSocketClientActivity_startHeartBeat(
		JNIEnv * env, jobject object)
{
	int socketID;
	struct sockaddr_un serverAddr;
	char path[] = "pym_local_socket\0";
	int ret;

	socketID = socket_local_client(path, ANDROID_SOCKET_NAMESPACE_ABSTRACT,
			SOCK_STREAM);
	if (socketID < 0)
	{
		return socketID;
	}

	ret = close(socketID);
	if (ret < 0)
	{
		return CLOSE_ERR;
	}

	return NO_ERR;
}

/* creat client socket */
int socket_local_client(const char *name, int namespaceId, int type)
{
	int socketID;
	int ret, j, ii = 0;

	socketID = socket(AF_LOCAL, type, 0);
	if (socketID < 0)
	{
		return CREATE_ERR;
	}

	ret = socket_local_client_connect(socketID, name, namespaceId, type);

	int count, sum = 0;
	char buf[55];

	for (ii = 0; ii < 10; ii++)
	{
		sleep(1);
		for (j = 0; j < (int) (50 * ((float) rand() / RAND_MAX)); j++)
			buf[j] = '*';
		buf[j] = '\0';
		count = write(socketID, buf, strlen(buf));
	}
	sleep(1);
	strcpy(buf, "9");
	count = write(socketID, buf, strlen(buf));

	memset(buf, 0, 55);

	count = read(socketID, buf, sizeof(buf));

	//	rett = write(socketID, buf, strlen(buf));
	LOGE("read buffer count=%d,read buf=%s",count,buf);

	if (ret < 0)
	{
		close(socketID);

		return ret;
	}

	return socketID;
}

/* connect to relavent fileDescriptor*/
int socket_local_client_connect(int fd, const char *name, int namespaceId,
		int type)
{
	struct sockaddr_un addr;
	socklen_t socklen;
	size_t namelen;
	int ret;

	ret = socket_make_sockaddr_un(name, namespaceId, &addr, &socklen);
	if (ret < 0)
	{
		return ret;
	}

	if (connect(fd, (struct sockaddr *) &addr, socklen) < 0)
	{
		return CONNECT_ERR;
	}

	return fd;
}

/* construct sockaddr_un */
int socket_make_sockaddr_un(const char *name, int namespaceId,
		struct sockaddr_un *p_addr, socklen_t *socklen)
{
	size_t namelen;

	MEM_ZERO(p_addr, sizeof(*p_addr));
#ifdef HAVE_LINUX_LOCAL_SOCKET_NAMESPACE

	namelen = strlen(name);

	// Test with length +1 for the *initial* '\0'.
	if ((namelen + 1) > sizeof(p_addr->sun_path))
	{
		return LINUX_MAKE_ADDRUN_ERROR;
	}
	p_addr->sun_path[0] = 0;
	memcpy(p_addr->sun_path + 1, name, namelen);

#else

	namelen = strlen(name) + strlen(FILESYSTEM_SOCKET_PREFIX);

	/* unix_path_max appears to be missing on linux */
	if (namelen > (sizeof(*p_addr) - offsetof(struct sockaddr_un, sun_path) - 1))
	{
		return NO_LINUX_MAKE_ADDRUN_ERROR;
	}

	strcpy(p_addr->sun_path, FILESYSTEM_SOCKET_PREFIX);
	strcat(p_addr->sun_path, name);

#endif

	p_addr->sun_family = AF_LOCAL;
	*socklen = namelen + offsetof(struct sockaddr_un, sun_path) + 1;

	return NO_ERR;
}

#ifdef __cplusplus
}
#endif

